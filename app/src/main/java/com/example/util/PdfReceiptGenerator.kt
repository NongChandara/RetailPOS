package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.model.BranchEntity
import com.example.data.model.SaleEntity
import com.example.data.model.SaleItemEntity
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReceiptGenerator {

    /**
     * Generates a PDF receipt file for a given sale and itemized list.
     * Dimensions are calibrated for standard retail receipt width (384 points / 5.33 inches)
     * with dynamic height based on the number of itemized rows.
     */
    fun generateReceiptPdf(
        context: Context,
        sale: SaleEntity,
        items: List<SaleItemEntity>,
        khrExchangeRate: Double = CurrencyUtils.DEFAULT_KHR_EXCHANGE_RATE,
        branch: BranchEntity? = null
    ): File {
        val gap = (branch?.receiptGap ?: sale.receiptGap).coerceIn(4, 40)
        val pageWidth = 384
        val baseHeaderHeight = 190 + gap
        val itemsHeight = (items.size * 32).coerceAtLeast(40)
        val totalsAndFooterHeight = 320 + gap
        val pageHeight = baseHeaderHeight + itemsHeight + totalsAndFooterHeight

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // Background - subtle warm receipt off-white paper tint
        val bgPaint = Paint().apply {
            color = Color.rgb(254, 253, 248)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)

        // Outer subtle border
        val borderPaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        canvas.drawRoundRect(RectF(8f, 8f, (pageWidth - 8).toFloat(), (pageHeight - 8).toFloat()), 6f, 6f, borderPaint)

        val textPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(15, 23, 42) // Slate 900
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }

        val mutedPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(100, 116, 139) // Slate 500
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textSize = 9.5f
        }

        val dividerPaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            style = Paint.Style.STROKE
            strokeWidth = 1f
            pathEffect = DashPathEffect(floatArrayOf(4f, 4f), 0f)
        }

        val leftMargin = 22f
        val rightMargin = (pageWidth - 22).toFloat()
        val contentWidth = rightMargin - leftMargin
        val centerX = pageWidth / 2f

        var currentY = 30f

        // 1. Store Header with TR Coffee Logo
        try {
            val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.img_tr_coffee_logo)
            if (logoBitmap != null) {
                val logoSize = 44f
                val logoLeft = centerX - (logoSize / 2f)
                val destRect = RectF(logoLeft, currentY, logoLeft + logoSize, currentY + logoSize)
                canvas.drawBitmap(logoBitmap, null, destRect, null)
                currentY += logoSize + 10f
            }
        } catch (_: Exception) {
            currentY += 8f
        }

        val receiptHeader = branch?.receiptHeader ?: "TR COFFEE • ${sale.branchName}"
        val receiptSubtitle = branch?.receiptSubtitle ?: "Official Sales Receipt & Tax Invoice"
        val branchAddress = branch?.address?.ifBlank { null } ?: "123 Norodom Blvd, Daun Penh, Phnom Penh"
        val branchPhone = branch?.phone?.ifBlank { null } ?: "+855 23 888 999"
        val branchVatTin = branch?.receiptVatTin?.ifBlank { null } ?: "VAT TIN: K001-90213847"

        textPaint.textSize = 15f
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textPaint.color = Color.rgb(15, 77, 42)
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(receiptHeader, centerX, currentY, textPaint)
        textPaint.color = Color.rgb(15, 23, 42) // Reset color to Slate 900

        currentY += 15f
        textPaint.textSize = 9.5f
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(receiptSubtitle, centerX, currentY, mutedPaint)

        currentY += 13f
        canvas.drawText("$branchAddress • $branchPhone", centerX, currentY, mutedPaint)

        currentY += 13f
        canvas.drawText(branchVatTin, centerX, currentY, mutedPaint)

        // Divider
        currentY += 12f
        canvas.drawLine(leftMargin, currentY, rightMargin, currentY, dividerPaint)

        // 2. Transaction Metadata
        currentY += 16f
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 10f
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        canvas.drawText("RECEIPT #:", leftMargin, currentY, textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(sale.receiptNumber, rightMargin, currentY, textPaint)

        currentY += 14f
        val dateFormat = SimpleDateFormat("MMM dd, yyyy  hh:mm a", Locale.US)
        val formattedDate = dateFormat.format(Date(sale.timestamp))
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        canvas.drawText("DATE/TIME:", leftMargin, currentY, textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(formattedDate, rightMargin, currentY, textPaint)

        currentY += 14f
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("CASHIER:", leftMargin, currentY, textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("${sale.cashierName} (Terminal #01)", rightMargin, currentY, textPaint)

        currentY += 14f
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("PAYMENT:", leftMargin, currentY, textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(sale.paymentMethod, rightMargin, currentY, textPaint)

        // Divider
        currentY += 12f
        canvas.drawLine(leftMargin, currentY, rightMargin, currentY, dividerPaint)

        // 3. Itemized Table Header
        currentY += 8f
        val headerBgPaint = Paint().apply {
            color = Color.rgb(241, 245, 249) // Slate 100
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(leftMargin, currentY, rightMargin, currentY + 18f), 3f, 3f, headerBgPaint)

        val headerTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(30, 41, 59)
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textSize = 9.5f
        }

        val colQtyX = leftMargin + 180f
        val colPriceX = leftMargin + 245f
        val colTotalX = rightMargin - 4f

        headerTextPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("ITEM DESCRIPTION", leftMargin + 6f, currentY + 13f, headerTextPaint)
        headerTextPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("QTY", colQtyX, currentY + 13f, headerTextPaint)
        headerTextPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("PRICE", colPriceX, currentY + 13f, headerTextPaint)
        canvas.drawText("TOTAL", colTotalX, currentY + 13f, headerTextPaint)

        currentY += 24f

        // 4. Item Rows
        for (item in items) {
            // Product Name (truncated if exceeds space)
            textPaint.textAlign = Paint.Align.LEFT
            textPaint.textSize = 10f
            textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)

            val maxItemWidth = colQtyX - leftMargin - 14f
            val itemTitle = truncateText(item.productName, textPaint, maxItemWidth)
            canvas.drawText(itemTitle, leftMargin + 4f, currentY, textPaint)

            // Quantity
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            canvas.drawText("${item.quantity}", colQtyX, currentY, textPaint)

            // Unit Price
            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(String.format(Locale.US, "$%.2f", item.unitPrice), colPriceX, currentY, textPaint)

            // Item Total
            textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            canvas.drawText(String.format(Locale.US, "$%.2f", item.itemTotal), colTotalX, currentY, textPaint)

            // SKU subline if available
            currentY += 12f
            mutedPaint.textAlign = Paint.Align.LEFT
            mutedPaint.textSize = 8.5f
            canvas.drawText("SKU: ${item.sku}", leftMargin + 4f, currentY, mutedPaint)

            currentY += 18f
        }

        // Divider
        currentY += 4f
        canvas.drawLine(leftMargin, currentY, rightMargin, currentY, dividerPaint)

        // 5. Calculations Summary
        currentY += 16f
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 10f
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        canvas.drawText("SUBTOTAL (${items.sumOf { it.quantity }} items):", leftMargin, currentY, textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(String.format(Locale.US, "$%.2f", sale.subtotal), rightMargin, currentY, textPaint)

        if (sale.discountAmount > 0) {
            currentY += 14f
            val discountPaint = Paint(textPaint).apply {
                color = Color.rgb(180, 83, 9) // Amber
            }
            discountPaint.textAlign = Paint.Align.LEFT
            canvas.drawText("DISCOUNT (${sale.discountPercent.toInt()}%):", leftMargin, currentY, discountPaint)
            discountPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(String.format(Locale.US, "-$%.2f", sale.discountAmount), rightMargin, currentY, discountPaint)
        }

        currentY += 14f
        val taxPercentStr = if (sale.taxPercent > 0.0) {
            String.format(Locale.US, "%.1f", sale.taxPercent).removeSuffix(".0")
        } else if (branch != null && branch.taxPercent > 0.0) {
            String.format(Locale.US, "%.1f", branch.taxPercent).removeSuffix(".0")
        } else if (sale.subtotal - sale.discountAmount > 0) {
            String.format(Locale.US, "%.1f", ((sale.taxAmount / (sale.subtotal - sale.discountAmount)) * 100)).removeSuffix(".0")
        } else "8"
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("SALES TAX ($taxPercentStr%):", leftMargin, currentY, textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(String.format(Locale.US, "$%.2f", sale.taxAmount), rightMargin, currentY, textPaint)

        // Grand Total Box (USD + KHR)
        currentY += 12f
        val totalBgPaint = Paint().apply {
            color = Color.rgb(15, 118, 110) // Teal 700
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(leftMargin, currentY, rightMargin, currentY + 44f), 5f, 5f, totalBgPaint)

        val totalTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textSize = 12.5f
        }
        totalTextPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("TOTAL AMOUNT DUE:", leftMargin + 10f, currentY + 18f, totalTextPaint)
        totalTextPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(String.format(Locale.US, "$%.2f", sale.totalAmount), rightMargin - 10f, currentY + 18f, totalTextPaint)

        val khrTotalPaint = Paint(totalTextPaint).apply {
            textSize = 11f
            color = Color.rgb(204, 251, 241) // Teal 100
        }
        khrTotalPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("KHR EQUIVALENT:", leftMargin + 10f, currentY + 34f, khrTotalPaint)
        khrTotalPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(CurrencyUtils.formatKhr(sale.totalAmount, khrExchangeRate), rightMargin - 10f, currentY + 34f, khrTotalPaint)

        currentY += 54f
        mutedPaint.textAlign = Paint.Align.CENTER
        mutedPaint.textSize = 8.5f
        canvas.drawText("Exchange Rate: 1 USD = ${CurrencyUtils.formatKhrRaw(khrExchangeRate.toLong())}", centerX, currentY, mutedPaint)

        currentY += 16f

        // Payment Tender Details
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 10f
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        canvas.drawText("TENDERED (${sale.paymentMethod}):", leftMargin, currentY, textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        val tenderStr = String.format(Locale.US, "$%.2f", sale.amountTendered)
        val tenderKhr = CurrencyUtils.formatKhr(sale.amountTendered, khrExchangeRate)
        canvas.drawText("$tenderStr ($tenderKhr)", rightMargin, currentY, textPaint)

        if (sale.changeGiven > 0) {
            currentY += 14f
            val changePaint = Paint(textPaint).apply {
                color = Color.rgb(4, 120, 87) // Emerald
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            }
            changePaint.textAlign = Paint.Align.LEFT
            canvas.drawText("CHANGE DUE:", leftMargin, currentY, changePaint)
            changePaint.textAlign = Paint.Align.RIGHT
            val changeStr = String.format(Locale.US, "$%.2f", sale.changeGiven)
            val changeKhr = CurrencyUtils.formatKhr(sale.changeGiven, khrExchangeRate)
            canvas.drawText("$changeStr ($changeKhr)", rightMargin, currentY, changePaint)
        }

        // 6. Barcode simulation
        currentY += 22f
        drawSimulatedBarcode(canvas, centerX, currentY, sale.receiptNumber)

        currentY += 34f
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 9.5f
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        canvas.drawText("* ${sale.receiptNumber} *", centerX, currentY, textPaint)

        // 7. Footer
        currentY += 18f
        mutedPaint.textAlign = Paint.Align.CENTER
        mutedPaint.textSize = 9f
        val footerText = branch?.receiptFooter?.ifBlank { null } ?: "Thank you for supporting our local business!"
        canvas.drawText(footerText, centerX, currentY, mutedPaint)

        currentY += 12f
        canvas.drawText("Items returnable within 7 days with this digital receipt.", centerX, currentY, mutedPaint)

        currentY += 12f
        mutedPaint.textSize = 8f
        canvas.drawText("Tax Invoice Generated by RetailPOS Android System", centerX, currentY, mutedPaint)

        pdfDocument.finishPage(page)

        // Save PDF to cache directory
        val receiptsDir = File(context.cacheDir, "receipts").apply { mkdirs() }
        val pdfFile = File(receiptsDir, "Receipt_${sale.receiptNumber}.pdf")
        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return pdfFile
    }

    /**
     * Draws authentic-looking barcode pattern on Canvas for the receipt.
     */
    private fun drawSimulatedBarcode(canvas: Canvas, centerX: Float, startY: Float, code: String) {
        val barcodeHeight = 22f
        val barcodeWidth = 240f
        val startX = centerX - (barcodeWidth / 2f)

        val barPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            style = Paint.Style.FILL
            isAntiAlias = false
        }

        // Seed bar patterns pseudo-deterministically from code string
        var curX = startX
        val hash = code.hashCode().toLong()
        var bitIndex = 0
        while (curX < startX + barcodeWidth) {
            val isThick = ((hash shr (bitIndex % 32)) and 1L) == 1L
            val barWidth = if (isThick) 3.5f else 1.5f
            val spaceWidth = if (isThick) 2f else 3f

            canvas.drawRect(curX, startY, curX + barWidth, startY + barcodeHeight, barPaint)
            curX += (barWidth + spaceWidth)
            bitIndex++
        }
    }

    private fun truncateText(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var truncated = text
        while (truncated.isNotEmpty() && paint.measureText("$truncated...") > maxWidth) {
            truncated = truncated.dropLast(1)
        }
        return if (truncated.isNotEmpty()) "$truncated..." else text
    }

    /**
     * Opens system Share dialog with the PDF attached as an application/pdf document.
     */
    fun shareReceiptPdf(context: Context, pdfFile: File, receiptNumber: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Receipt #$receiptNumber from Retail POS Store")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Here is your official PDF receipt for transaction #$receiptNumber from Retail POS Store. Thank you for your purchase!"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Receipt PDF")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Opens the generated PDF using any installed PDF viewer on device.
     */
    fun viewReceiptPdf(context: Context, pdfFile: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(viewIntent, "Open PDF Receipt")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "No PDF viewer found on device: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Sends the receipt directly to the connected thermal printer using Android PrintManager.
     */
    fun printReceiptPdf(context: Context, pdfFile: File, receiptNumber: String) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            Toast.makeText(context, "Android PrintManager unavailable on device", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(context, "Sending receipt #$receiptNumber to thermal printer...", Toast.LENGTH_SHORT).show()

        val printAdapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder("Receipt_$receiptNumber.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()
                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                var input: FileInputStream? = null
                var output: FileOutputStream? = null
                try {
                    input = FileInputStream(pdfFile)
                    output = FileOutputStream(destination?.fileDescriptor)
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } >= 0) {
                        output.write(buffer, 0, bytesRead)
                    }
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                } finally {
                    try {
                        input?.close()
                        output?.close()
                    } catch (_: Exception) {}
                }
            }
        }

        // Configure PrintAttributes for standard POS 80mm / 58mm roll thermal printers
        val thermalMediaSize = PrintAttributes.MediaSize(
            "THERMAL_RECEIPT_80MM",
            "Thermal Receipt (80mm Roll)",
            3150, // 80mm width in mils (~3.15 in)
            11000 // continuous receipt roll length in mils
        )

        val printAttributes = PrintAttributes.Builder()
            .setMediaSize(thermalMediaSize)
            .setResolution(PrintAttributes.Resolution("thermal_203", "Thermal Head 203 DPI", 203, 203))
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME)
            .build()

        printManager.print("Receipt-$receiptNumber", printAdapter, printAttributes)
    }
}
