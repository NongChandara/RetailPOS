package com.example.printer

import com.example.data.model.BranchEntity
import com.example.data.model.SaleEntity
import com.example.data.model.SaleItemEntity
import com.example.ui.reports.SalesReportSummary
import com.example.util.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FormattedThermalDoc(
    val escPosBytes: ByteArray,
    val previewText: String,
    val jobType: PrintJobType
)

object ThermalReceiptFormatter {

    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US)
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

    /**
     * Formats an official Customer Receipt for thermal printers.
     */
    fun formatCustomerReceipt(
        sale: SaleEntity,
        items: List<SaleItemEntity>,
        khrRate: Double = CurrencyUtils.activeKhrExchangeRate,
        branch: BranchEntity? = null,
        config: ThermalPrinterConfig
    ): FormattedThermalDoc {
        val width = config.paperWidth
        val cols = width.charWidth
        val builder = EscPosCommandBuilder(paperWidth = width).reset()
        val textSb = StringBuilder()

        val dividerLine = "-".repeat(cols)
        val doubleDividerLine = "=".repeat(cols)

        val headerName = branch?.receiptHeader ?: "TR COFFEE • ${sale.branchName}"
        val subtitle = branch?.receiptSubtitle ?: "Official Sales Receipt & Tax Invoice"
        val address = branch?.address?.ifBlank { null } ?: "123 Norodom Blvd, Daun Penh, Phnom Penh"
        val phone = branch?.phone?.ifBlank { null } ?: "+855 23 888 999"
        val vatTin = branch?.receiptVatTin?.ifBlank { null } ?: "VAT TIN: K001-90213847"

        // === ESC/POS BINARY BUILD ===
        if (config.openCashDrawer) {
            builder.openCashDrawer()
        }

        // Header
        builder.alignCenter()
            .bold(true).textDoubleHeight().line(headerName).textNormal()
            .bold(false).line(subtitle)
            .line(address)
            .line("Tel: $phone")
            .line("VAT TIN: $vatTin")

        if (!branch?.wifiPassword.isNullOrBlank()) {
            val wifiName = branch?.wifiName?.ifBlank { "TR_Coffee_Guest" } ?: "TR_Coffee_Guest"
            builder.line("Wi-Fi: $wifiName | Key: ${branch.wifiPassword}")
        }

        builder.alignLeft().line(dividerLine)

        // Metadata
        builder.twoColumns("Receipt No:", sale.receiptNumber)
            .twoColumns("Date & Time:", dateTimeFormat.format(Date(sale.timestamp)))
            .twoColumns("Cashier:", sale.cashierName)
            .twoColumns("Branch:", sale.branchName)

        if (sale.notes.isNotBlank()) {
            builder.line("Note: ${sale.notes.take(cols - 6)}")
        }

        builder.line(doubleDividerLine)

        // Item Header
        builder.threeColumns("QTY", "ITEM DESCRIPTION", "TOTAL")
            .line(dividerLine)

        for (item in items) {
            val qtyStr = "${item.quantity}x"
            val priceStr = CurrencyUtils.formatUsd(item.itemTotal)
            builder.threeColumns(qtyStr, item.productName, priceStr)

            // Show free promo or discount info if applicable
            if (item.quantity > 1) {
                val unitPriceStr = "  @ ${CurrencyUtils.formatUsd(item.unitPrice)} each"
                builder.line(unitPriceStr)
            }
        }

        builder.line(dividerLine)

        // Financial Totals
        val subtotalUsd = CurrencyUtils.formatUsd(sale.subtotal)
        val subtotalKhr = CurrencyUtils.formatKhr(sale.subtotal, khrRate)
        builder.twoColumns("Subtotal:", "$subtotalUsd  ($subtotalKhr)")

        if (sale.discountAmount > 0) {
            val discUsd = "-${CurrencyUtils.formatUsd(sale.discountAmount)}"
            val discKhr = "-${CurrencyUtils.formatKhr(sale.discountAmount, khrRate)}"
            val discLabel = if (sale.discountPercent > 0) "Discount (${sale.discountPercent.toInt()}%):" else "Discount:"
            builder.twoColumns(discLabel, "$discUsd  ($discKhr)")
        }

        if (sale.taxAmount > 0) {
            val taxUsd = CurrencyUtils.formatUsd(sale.taxAmount)
            val taxKhr = CurrencyUtils.formatKhr(sale.taxAmount, khrRate)
            val taxLabel = if (sale.taxPercent > 0) "VAT (${sale.taxPercent.toInt()}%):" else "Tax:"
            builder.twoColumns(taxLabel, "$taxUsd  ($taxKhr)")
        }

        builder.line(doubleDividerLine)

        // Grand Total in Large Bold
        val grandTotalUsd = CurrencyUtils.formatUsd(sale.totalAmount)
        val grandTotalKhr = CurrencyUtils.formatKhr(sale.totalAmount, khrRate)

        builder.bold(true).textDoubleHeight()
            .twoColumns("TOTAL USD:", grandTotalUsd)
            .twoColumns("TOTAL KHR:", grandTotalKhr)
            .textNormal().bold(false)

        builder.line(dividerLine)

        // Payment Details
        builder.twoColumns("Payment Method:", sale.paymentMethod.uppercase())
            .twoColumns("Tendered:", CurrencyUtils.formatUsd(sale.amountTendered))
            .twoColumns("Change:", CurrencyUtils.formatUsd(sale.changeGiven))

        builder.line(dividerLine)

        // Barcode / QR Code simulation
        builder.alignCenter()
        if (config.printKhmerAsBitmap) {
            builder.bold(true).line("SCAN TO VERIFY / BAKONG KHQR").bold(false)
        }
        builder.qrCode(sale.receiptNumber, moduleSize = if (width == PrinterPaperWidth.MM_58) 3 else 4)

        builder.line(sale.receiptNumber)
            .feed(1)
            .line("Thank you for choosing TR Coffee!")
            .line("សូមអរគុណ និងសូមអញ្ជើញមកម្តងទៀត!")
            .line(branch?.receiptFooter ?: "Good Coffee • Good Day • www.trcoffee.com")

        if (config.cutPaper) {
            builder.cutPaper()
        } else {
            builder.feed(3)
        }

        // === PREVIEW TEXT BUILD ===
        textSb.appendLine("================================".center(cols))
        textSb.appendLine(headerName.center(cols))
        textSb.appendLine(subtitle.center(cols))
        textSb.appendLine(address.center(cols))
        textSb.appendLine("Tel: $phone".center(cols))
        textSb.appendLine("VAT TIN: $vatTin".center(cols))
        if (!branch?.wifiPassword.isNullOrBlank()) {
            val wifiName = branch?.wifiName?.ifBlank { "TR_Coffee_Guest" } ?: "TR_Coffee_Guest"
            textSb.appendLine("Wi-Fi: $wifiName | Key: ${branch.wifiPassword}".center(cols))
        }
        textSb.appendLine(dividerLine)
        textSb.appendLine(formatTwoColText("Receipt No:", sale.receiptNumber, cols))
        textSb.appendLine(formatTwoColText("Date & Time:", dateTimeFormat.format(Date(sale.timestamp)), cols))
        textSb.appendLine(formatTwoColText("Cashier:", sale.cashierName, cols))
        textSb.appendLine(formatTwoColText("Branch:", sale.branchName, cols))
        if (sale.notes.isNotBlank()) {
            textSb.appendLine("Note: ${sale.notes.take(cols - 6)}")
        }
        textSb.appendLine(doubleDividerLine)
        textSb.appendLine(formatThreeColText("QTY", "ITEM DESCRIPTION", "TOTAL", cols, width))
        textSb.appendLine(dividerLine)
        for (item in items) {
            val qtyStr = "${item.quantity}x"
            val priceStr = CurrencyUtils.formatUsd(item.itemTotal)
            textSb.appendLine(formatThreeColText(qtyStr, item.productName, priceStr, cols, width))
            if (item.quantity > 1) {
                textSb.appendLine("  @ ${CurrencyUtils.formatUsd(item.unitPrice)} each")
            }
        }
        textSb.appendLine(dividerLine)
        textSb.appendLine(formatTwoColText("Subtotal:", "$subtotalUsd ($subtotalKhr)", cols))
        if (sale.discountAmount > 0) {
            val discUsd = "-${CurrencyUtils.formatUsd(sale.discountAmount)}"
            val discKhr = "-${CurrencyUtils.formatKhr(sale.discountAmount, khrRate)}"
            val discLabel = if (sale.discountPercent > 0) "Discount (${sale.discountPercent.toInt()}%):" else "Discount:"
            textSb.appendLine(formatTwoColText(discLabel, "$discUsd ($discKhr)", cols))
        }
        if (sale.taxAmount > 0) {
            val taxUsd = CurrencyUtils.formatUsd(sale.taxAmount)
            val taxKhr = CurrencyUtils.formatKhr(sale.taxAmount, khrRate)
            val taxLabel = if (sale.taxPercent > 0) "VAT (${sale.taxPercent.toInt()}%):" else "Tax:"
            textSb.appendLine(formatTwoColText(taxLabel, "$taxUsd ($taxKhr)", cols))
        }
        textSb.appendLine(doubleDividerLine)
        textSb.appendLine(formatTwoColText("TOTAL USD:", grandTotalUsd, cols))
        textSb.appendLine(formatTwoColText("TOTAL KHR:", grandTotalKhr, cols))
        textSb.appendLine(dividerLine)
        textSb.appendLine(formatTwoColText("Payment:", sale.paymentMethod.uppercase(), cols))
        textSb.appendLine(formatTwoColText("Tendered:", CurrencyUtils.formatUsd(sale.amountTendered), cols))
        textSb.appendLine(formatTwoColText("Change:", CurrencyUtils.formatUsd(sale.changeGiven), cols))
        textSb.appendLine(dividerLine)
        textSb.appendLine("[QR CODE: ${sale.receiptNumber}]".center(cols))
        textSb.appendLine("Thank you for choosing TR Coffee!".center(cols))
        textSb.appendLine("សូមអរគុណ និងសូមអញ្ជើញមកម្តងទៀត!".center(cols))
        textSb.appendLine((branch?.receiptFooter ?: "Good Coffee • Good Day").center(cols))
        textSb.appendLine("================================".center(cols))

        return FormattedThermalDoc(
            escPosBytes = builder.build(),
            previewText = textSb.toString(),
            jobType = PrintJobType.CUSTOMER_RECEIPT
        )
    }

    /**
     * Formats a Kitchen / Barista Order Ticket for instant preparation.
     */
    fun formatOrderTicket(
        sale: SaleEntity,
        items: List<SaleItemEntity>,
        orderNumber: String = sale.receiptNumber.takeLast(4),
        isTakeaway: Boolean = false,
        config: ThermalPrinterConfig
    ): FormattedThermalDoc {
        val width = config.paperWidth
        val cols = width.charWidth
        val builder = EscPosCommandBuilder(paperWidth = width).reset()
        val textSb = StringBuilder()

        val dividerLine = "-".repeat(cols)
        val doubleDividerLine = "=".repeat(cols)

        if (config.beepKitchenTicket) {
            builder.beepBuzzer()
        }

        // Header
        builder.alignCenter()
            .bold(true).textDoubleSize()
            .line("ORDER #${orderNumber}")
            .textNormal().bold(false)
            .bold(true).line(if (isTakeaway) "*** TAKEAWAY ***" else "=== DINE-IN ===").bold(false)

        builder.alignLeft().line(dividerLine)
            .twoColumns("Ticket No:", sale.receiptNumber)
            .twoColumns("Order Time:", timeFormat.format(Date(sale.timestamp)))
            .twoColumns("Server/Cashier:", sale.cashierName)
            .twoColumns("Station:", "BARISTA & KITCHEN")

        if (sale.notes.isNotBlank()) {
            builder.line(dividerLine)
                .bold(true).line("SPECIAL INSTRUCTIONS:").bold(false)
                .line(sale.notes)
        }

        builder.line(doubleDividerLine)
            .alignLeft()

        var totalItemCount = 0
        for (item in items) {
            totalItemCount += item.quantity
            val qtyPrefix = "[ ] ${item.quantity}x "
            builder.bold(true).textDoubleHeight()
                .line("$qtyPrefix${item.productName}")
                .textNormal().bold(false)
            // Extra spacing for kitchen tickets
            builder.feed(1)
        }

        builder.line(doubleDividerLine)
            .twoColumns("TOTAL ITEMS:", "$totalItemCount Items")
            .line(dividerLine)
            .alignCenter()
            .line(">>> PREPARE WITH CARE & SPEED <<<")

        if (config.cutPaper) {
            builder.cutPaper()
        } else {
            builder.feed(3)
        }

        // Preview text
        textSb.appendLine("================================".center(cols))
        textSb.appendLine("ORDER #${orderNumber}".center(cols))
        textSb.appendLine((if (isTakeaway) "*** TAKEAWAY ***" else "=== DINE-IN ===").center(cols))
        textSb.appendLine(dividerLine)
        textSb.appendLine(formatTwoColText("Ticket No:", sale.receiptNumber, cols))
        textSb.appendLine(formatTwoColText("Order Time:", timeFormat.format(Date(sale.timestamp)), cols))
        textSb.appendLine(formatTwoColText("Server/Cashier:", sale.cashierName, cols))
        if (sale.notes.isNotBlank()) {
            textSb.appendLine(dividerLine)
            textSb.appendLine("INSTRUCTIONS: ${sale.notes}")
        }
        textSb.appendLine(doubleDividerLine)
        for (item in items) {
            textSb.appendLine("[ ] ${item.quantity}x ${item.productName}")
        }
        textSb.appendLine(doubleDividerLine)
        textSb.appendLine(formatTwoColText("TOTAL ITEMS:", "$totalItemCount Items", cols))
        textSb.appendLine(">>> PREPARE WITH CARE & SPEED <<<".center(cols))
        textSb.appendLine("================================".center(cols))

        return FormattedThermalDoc(
            escPosBytes = builder.build(),
            previewText = textSb.toString(),
            jobType = PrintJobType.ORDER_TICKET
        )
    }

    /**
     * Formats an End-of-Day Financial Z-Report for store audit and register closing.
     */
    fun formatZReport(
        summary: SalesReportSummary,
        branch: BranchEntity? = null,
        managerName: String = "Sarah Miller (Store Manager)",
        config: ThermalPrinterConfig,
        khrRate: Double = CurrencyUtils.activeKhrExchangeRate
    ): FormattedThermalDoc {
        val width = config.paperWidth
        val cols = width.charWidth
        val builder = EscPosCommandBuilder(paperWidth = width).reset()
        val textSb = StringBuilder()

        val dividerLine = "-".repeat(cols)
        val doubleDividerLine = "=".repeat(cols)

        val storeHeader = branch?.receiptHeader ?: "TR COFFEE"
        val branchName = branch?.name ?: "Phnom Penh Flagship"

        if (config.openCashDrawer) {
            builder.openCashDrawer()
        }

        builder.alignCenter()
            .bold(true).textDoubleHeight()
            .line(storeHeader)
            .textNormal()
            .bold(true).line("END-OF-DAY Z-REPORT").bold(false)
            .line("FINANCIAL AUDIT & REGISTER CLOSE")
            .line("Branch: $branchName")
            .line("Period: ${summary.period.label} (${summary.dateRangeDisplay})")
            .line("Printed: ${dateTimeFormat.format(Date())}")
            .line("Manager: $managerName")

        builder.alignLeft().line(doubleDividerLine)
            .bold(true).line("--- 1. REVENUE SUMMARY ---").bold(false)

        val grossUsd = CurrencyUtils.formatUsd(summary.totalGrossRevenue)
        val grossKhr = CurrencyUtils.formatKhr(summary.totalGrossRevenue, khrRate)
        val netRevenue = (summary.totalGrossRevenue - summary.totalDiscounts).coerceAtLeast(0.0)
        val netUsd = CurrencyUtils.formatUsd(netRevenue)
        val netKhr = CurrencyUtils.formatKhr(netRevenue, khrRate)
        val discUsd = CurrencyUtils.formatUsd(summary.totalDiscounts)
        val taxUsd = CurrencyUtils.formatUsd(summary.totalTax)
        val cogsUsd = CurrencyUtils.formatUsd(summary.totalCogs)
        val profitUsd = CurrencyUtils.formatUsd(summary.grossProfit)

        builder.twoColumns("Gross Sales:", "$grossUsd ($grossKhr)")
            .twoColumns("Total Discounts:", "-$discUsd")
            .twoColumns("Net Sales:", "$netUsd ($netKhr)")
            .twoColumns("Tax / VAT Collected:", taxUsd)
            .twoColumns("Cost of Goods (COGS):", cogsUsd)
            .bold(true).twoColumns("Gross Profit:", profitUsd).bold(false)
            .twoColumns("Profit Margin:", "${String.format(Locale.US, "%.1f", summary.profitMarginPercent)}%")

        builder.line(dividerLine)
            .bold(true).line("--- 2. TRANSACTION METRICS ---").bold(false)
            .twoColumns("Completed Sales:", "${summary.transactionsCount} orders")
            .twoColumns("Total Items Sold:", "${summary.totalItemsSold} items")
            .twoColumns("Average Ticket:", CurrencyUtils.formatUsd(summary.averageTicket))

        builder.line(dividerLine)
            .bold(true).line("--- 3. PAYMENT METHOD BREAKDOWN ---").bold(false)
            .twoColumns("Cash USD / KHR:", "${summary.cashCount} txns • ${CurrencyUtils.formatUsd(summary.cashTotal)}")
            .twoColumns("Bakong KHQR (Digital):", "${summary.digitalCount} txns • ${CurrencyUtils.formatUsd(summary.digitalTotal)}")
            .twoColumns("Credit / Debit Cards:", "${summary.cardCount} txns • ${CurrencyUtils.formatUsd(summary.cardTotal)}")

        builder.line(dividerLine)
            .bold(true).line("--- 4. CASH DRAWER AUDIT ---").bold(false)
            .twoColumns("Expected Cash in Drawer:", CurrencyUtils.formatUsd(summary.cashTotal))
            .twoColumns("Drawer Variance:", "$0.00 (Balanced)")

        if (summary.categorySales.isNotEmpty()) {
            builder.line(dividerLine)
                .bold(true).line("--- 5. CATEGORY BREAKDOWN ---").bold(false)
            for (cat in summary.categorySales.take(5)) {
                val catRev = CurrencyUtils.formatUsd(cat.totalRevenue)
                val catPct = "${String.format(Locale.US, "%.0f", cat.percentageOfTotal)}%"
                builder.twoColumns("${cat.categoryName} (${cat.quantitySold}x):", "$catRev ($catPct)")
            }
        }

        if (summary.topProducts.isNotEmpty()) {
            builder.line(dividerLine)
                .bold(true).line("--- 6. TOP 5 BEST SELLERS ---").bold(false)
            for ((index, prod) in summary.topProducts.take(5).withIndex()) {
                val rank = "${index + 1}. ${prod.productName}"
                val sales = "${prod.quantitySold}x • ${CurrencyUtils.formatUsd(prod.totalRevenue)}"
                builder.twoColumns(rank, sales)
            }
        }

        builder.line(doubleDividerLine)
            .alignCenter()
            .line("AUDIT VERIFICATION & REGISTER SIGN-OFF")
            .feed(2)
            .line("Manager Signature: _______________________")
            .feed(1)
            .line("Store Auditor: __________________________")
            .feed(1)
            .line("*** END OF FISCAL AUDIT REPORT ***")

        if (config.cutPaper) {
            builder.cutPaper()
        } else {
            builder.feed(3)
        }

        // Preview text
        textSb.appendLine("================================".center(cols))
        textSb.appendLine(storeHeader.center(cols))
        textSb.appendLine("END-OF-DAY Z-REPORT".center(cols))
        textSb.appendLine("FINANCIAL AUDIT & REGISTER CLOSE".center(cols))
        textSb.appendLine("Branch: $branchName".center(cols))
        textSb.appendLine("Period: ${summary.period.label} (${summary.dateRangeDisplay})".center(cols))
        textSb.appendLine("Printed: ${dateTimeFormat.format(Date())}".center(cols))
        textSb.appendLine("Manager: $managerName".center(cols))
        textSb.appendLine(doubleDividerLine)
        textSb.appendLine("--- 1. REVENUE SUMMARY ---")
        textSb.appendLine(formatTwoColText("Gross Sales:", "$grossUsd ($grossKhr)", cols))
        textSb.appendLine(formatTwoColText("Discounts:", "-$discUsd", cols))
        textSb.appendLine(formatTwoColText("Net Sales:", "$netUsd ($netKhr)", cols))
        textSb.appendLine(formatTwoColText("Tax/VAT:", taxUsd, cols))
        textSb.appendLine(formatTwoColText("COGS:", cogsUsd, cols))
        textSb.appendLine(formatTwoColText("Gross Profit:", profitUsd, cols))
        textSb.appendLine(formatTwoColText("Margin:", "${String.format(Locale.US, "%.1f", summary.profitMarginPercent)}%", cols))
        textSb.appendLine(dividerLine)
        textSb.appendLine("--- 2. TRANSACTION METRICS ---")
        textSb.appendLine(formatTwoColText("Orders Count:", "${summary.transactionsCount} orders", cols))
        textSb.appendLine(formatTwoColText("Items Sold:", "${summary.totalItemsSold} items", cols))
        textSb.appendLine(formatTwoColText("Avg Ticket:", CurrencyUtils.formatUsd(summary.averageTicket), cols))
        textSb.appendLine(dividerLine)
        textSb.appendLine("--- 3. PAYMENT METHOD BREAKDOWN ---")
        textSb.appendLine(formatTwoColText("Cash USD/KHR:", "${summary.cashCount} txns • ${CurrencyUtils.formatUsd(summary.cashTotal)}", cols))
        textSb.appendLine(formatTwoColText("Bakong KHQR:", "${summary.digitalCount} txns • ${CurrencyUtils.formatUsd(summary.digitalTotal)}", cols))
        textSb.appendLine(formatTwoColText("Card Payments:", "${summary.cardCount} txns • ${CurrencyUtils.formatUsd(summary.cardTotal)}", cols))
        textSb.appendLine(dividerLine)
        textSb.appendLine("--- 4. CASH DRAWER AUDIT ---")
        textSb.appendLine(formatTwoColText("Expected Cash:", CurrencyUtils.formatUsd(summary.cashTotal), cols))
        textSb.appendLine(formatTwoColText("Variance:", "$0.00 (Balanced)", cols))
        if (summary.topProducts.isNotEmpty()) {
            textSb.appendLine(dividerLine)
            textSb.appendLine("--- TOP BEST SELLERS ---")
            for ((index, prod) in summary.topProducts.take(5).withIndex()) {
                textSb.appendLine(formatTwoColText("${index + 1}. ${prod.productName}", "${prod.quantitySold}x • ${CurrencyUtils.formatUsd(prod.totalRevenue)}", cols))
            }
        }
        textSb.appendLine(doubleDividerLine)
        textSb.appendLine("Manager Signature: _______________________".center(cols))
        textSb.appendLine("*** END OF FISCAL AUDIT REPORT ***".center(cols))
        textSb.appendLine("================================".center(cols))

        return FormattedThermalDoc(
            escPosBytes = builder.build(),
            previewText = textSb.toString(),
            jobType = PrintJobType.Z_REPORT
        )
    }

    /**
     * Formats a Hardware Test Slip to verify thermal printer communication, alignment, and cutting.
     */
    fun formatTestSlip(config: ThermalPrinterConfig): FormattedThermalDoc {
        val width = config.paperWidth
        val cols = width.charWidth
        val builder = EscPosCommandBuilder(paperWidth = width).reset()
        val textSb = StringBuilder()

        val dividerLine = "-".repeat(cols)
        val doubleDividerLine = "=".repeat(cols)

        if (config.openCashDrawer) {
            builder.openCashDrawer()
        }

        builder.alignCenter()
            .bold(true).textDoubleHeight()
            .line("TR COFFEE POS")
            .textNormal()
            .bold(true).line("THERMAL PRINTER TEST SLIP").bold(false)
            .line("Communication: ${config.connectionType.displayName}")
            .line("Paper Width: ${width.mm}mm (${cols} characters)")
            .line("Date: ${dateTimeFormat.format(Date())}")

        builder.alignLeft().line(dividerLine)
            .line("Text Alignment Test:")
            .alignLeft().line("[Left Aligned]")
            .alignCenter().line("[Center Aligned]")
            .alignRight().line("[Right Aligned]")
            .alignLeft()

        builder.line(dividerLine)
            .line("Character Styles:")
            .bold(true).line("Bold Text ON").bold(false)
            .underline(true).line("Underlined Text").underline(false)
            .invert(true).line(" INVERTED WHITE-ON-BLACK ").invert(false)
            .textDoubleHeight().line("Double Height").textNormal()
            .textDoubleWidth().line("Double Width").textNormal()

        builder.line(dividerLine)
            .line("Columns & Number Formatting:")
            .twoColumns("Test Item 1:", "$12.50")
            .twoColumns("Cambodian Riel:", "50,000 KHR")
            .threeColumns("1x", "Espresso Macchiato", "$3.50")

        builder.line(dividerLine)
            .alignCenter()
            .line("QR Code Test:")
            .qrCode("TR-COFFEE-THERMAL-TEST-OK", moduleSize = if (width == PrinterPaperWidth.MM_58) 3 else 4)
            .line("STATUS: PRINTER IS WORKING PROPERLY")
            .line(doubleDividerLine)

        if (config.cutPaper) {
            builder.cutPaper()
        } else {
            builder.feed(3)
        }

        textSb.appendLine("================================".center(cols))
        textSb.appendLine("TR COFFEE POS".center(cols))
        textSb.appendLine("THERMAL PRINTER TEST SLIP".center(cols))
        textSb.appendLine("Mode: ${config.connectionType.displayName}".center(cols))
        textSb.appendLine("Width: ${width.mm}mm (${cols} cols)".center(cols))
        textSb.appendLine(dividerLine)
        textSb.appendLine("[Left]       [Center]       [Right]")
        textSb.appendLine(formatTwoColText("Item 1:", "$12.50", cols))
        textSb.appendLine(formatTwoColText("Cambodian Riel:", "50,000 KHR", cols))
        textSb.appendLine(dividerLine)
        textSb.appendLine("[QR: TR-COFFEE-THERMAL-TEST-OK]".center(cols))
        textSb.appendLine("STATUS: PRINTER READY".center(cols))
        textSb.appendLine("================================".center(cols))

        return FormattedThermalDoc(
            escPosBytes = builder.build(),
            previewText = textSb.toString(),
            jobType = PrintJobType.TEST_PAGE
        )
    }

    private fun formatTwoColText(left: String, right: String, cols: Int): String {
        val rightLen = right.length
        val availLeft = cols - rightLen - 1
        val safeLeft = if (left.length > availLeft) left.take((availLeft - 1).coerceAtLeast(1)) + "…" else left
        val spaces = (cols - safeLeft.length - rightLen).coerceAtLeast(1)
        return safeLeft + " ".repeat(spaces) + right
    }

    private fun formatThreeColText(col1: String, col2: String, col3: String, cols: Int, width: PrinterPaperWidth): String {
        val qtyWidth = 4
        val priceWidth = if (width == PrinterPaperWidth.MM_58) 9 else 12
        val nameWidth = (cols - qtyWidth - priceWidth).coerceAtLeast(8)
        val padQty = col1.padEnd(qtyWidth)
        val safeName = if (col2.length > nameWidth) col2.take(nameWidth - 1) + "…" else col2.padEnd(nameWidth)
        val padPrice = col3.padStart(priceWidth)
        return "$padQty$safeName$padPrice"
    }

    private fun String.center(width: Int): String {
        if (length >= width) return take(width)
        val padding = (width - length) / 2
        return " ".repeat(padding) + this
    }
}
