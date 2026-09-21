package com.example.ui.receipt

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SaleEntity
import com.example.data.model.SaleItemEntity
import com.example.ui.components.formatDateTime
import com.example.ui.theme.RetailSlate100
import com.example.ui.theme.RetailSlate300
import com.example.ui.theme.RetailSlate500
import com.example.ui.theme.RetailSlate700
import com.example.ui.theme.RetailSlate800
import com.example.ui.theme.RetailSlate900
import com.example.ui.theme.RetailStatusGreen
import com.example.ui.theme.RetailStatusGreenContainer
import com.example.ui.theme.RetailTealContainer
import com.example.ui.theme.RetailTealDark
import com.example.ui.theme.RetailTealPrimary
import com.example.util.CurrencyMode
import com.example.util.CurrencyUtils
import com.example.util.PdfReceiptGenerator
import java.io.File
import java.util.Locale

/**
 * Full-screen digital receipt view presented after transaction completion or when reviewing past sales.
 * Correctly applies Cambodian Riel (KHR) currency formatting, dual currency conversion,
 * live PDF generation, printing, and sharing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalReceiptScreen(
    sale: SaleEntity,
    items: List<SaleItemEntity>,
    khrRate: Double = CurrencyUtils.activeKhrExchangeRate,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCurrencyMode by remember { mutableStateOf(CurrencyMode.DUAL) }
    var pdfFile by remember { mutableStateOf<File?>(null) }
    var isGeneratingPdf by remember { mutableStateOf(false) }

    // Generate PDF receipt on launch
    LaunchedEffect(sale.id, khrRate) {
        isGeneratingPdf = true
        try {
            pdfFile = PdfReceiptGenerator.generateReceiptPdf(context, sale, items, khrRate)
        } catch (e: Exception) {
            android.util.Log.e("DigitalReceiptScreen", "Failed to generate receipt PDF", e)
        } finally {
            isGeneratingPdf = false
        }
    }

    // Calculations in USD and KHR
    val totalKhr = CurrencyUtils.usdToKhr(sale.totalAmount, khrRate)
    val subtotalKhr = CurrencyUtils.usdToKhr(sale.subtotal, khrRate)
    val discountKhr = CurrencyUtils.usdToKhr(sale.discountAmount, khrRate)
    val taxKhr = CurrencyUtils.usdToKhr(sale.taxAmount, khrRate)
    val tenderedKhr = CurrencyUtils.usdToKhr(sale.amountTendered, khrRate)
    val changeKhr = CurrencyUtils.usdToKhr(sale.changeGiven, khrRate)

    fun formatPriceByMode(usdAmount: Double): String {
        return when (selectedCurrencyMode) {
            CurrencyMode.USD -> CurrencyUtils.formatUsd(usdAmount)
            CurrencyMode.KHR -> CurrencyUtils.formatKhr(usdAmount, khrRate)
            CurrencyMode.DUAL -> CurrencyUtils.formatCurrency(usdAmount, CurrencyMode.DUAL, khrRate, compactDual = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Digital Receipt",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = RetailSlate900
                        )
                        Text(
                            text = "Receipt #${sale.receiptNumber} • Phnom Penh",
                            fontSize = 11.sp,
                            color = RetailSlate500
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = RetailSlate800)
                    }
                },
                actions = {
                    // Quick Share PDF
                    IconButton(
                        onClick = {
                            val file = pdfFile ?: PdfReceiptGenerator.generateReceiptPdf(context, sale, items, khrRate).also { pdfFile = it }
                            PdfReceiptGenerator.shareReceiptPdf(context, file, sale.receiptNumber)
                        },
                        modifier = Modifier.testTag("topbar_share_receipt_button")
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share PDF", tint = RetailTealPrimary)
                    }
                    // Quick Print
                    IconButton(
                        onClick = {
                            val file = pdfFile ?: PdfReceiptGenerator.generateReceiptPdf(context, sale, items, khrRate).also { pdfFile = it }
                            PdfReceiptGenerator.printReceiptPdf(context, file, sale.receiptNumber)
                        },
                        modifier = Modifier.testTag("topbar_print_receipt_button")
                    ) {
                        Icon(Icons.Filled.Print, contentDescription = "Print", tint = RetailSlate700)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                tonalElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, RetailSlate300),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Secondary Action: Print
                    OutlinedButton(
                        onClick = {
                            val file = pdfFile ?: PdfReceiptGenerator.generateReceiptPdf(context, sale, items, khrRate).also { pdfFile = it }
                            PdfReceiptGenerator.printReceiptPdf(context, file, sale.receiptNumber)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("print_pdf_receipt_button")
                    ) {
                        Icon(Icons.Filled.Print, contentDescription = "Print", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Print", fontWeight = FontWeight.SemiBold)
                    }

                    // Primary Action: New Sale / Done
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(2f)
                            .height(48.dp)
                            .testTag("receipt_done_button")
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Done", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("New Sale / Done", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("digital_receipt_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Transaction Success Status Banner
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = "Success",
                                tint = RetailStatusGreen,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Transaction Complete • ជោគជ័យ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF166534)
                            )
                            Text(
                                text = "Receipt #${sale.receiptNumber} • ${formatDateTime(sale.timestamp)}",
                                fontSize = 12.sp,
                                color = RetailSlate500
                            )
                        }
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Receipt Number", sale.receiptNumber))
                                Toast.makeText(context, "Receipt # copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("copy_receipt_number_button")
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = RetailSlate500, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // 2. Interactive Currency Mode Selector & KHR Exchange Rate Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, RetailSlate300),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.CurrencyExchange,
                                contentDescription = "Currency",
                                tint = RetailTealPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Receipt Currency Display",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = RetailSlate900
                            )
                        }

                        // Live Exchange Rate Pill
                        Surface(
                            color = RetailTealContainer,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Text(
                                text = "1 USD = ${CurrencyUtils.formatKhrRaw(khrRate.toLong())}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RetailTealDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedCurrencyMode == CurrencyMode.DUAL,
                            onClick = { selectedCurrencyMode = CurrencyMode.DUAL },
                            label = { Text("Dual ($ / ៛)", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RetailTealPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("receipt_currency_chip_dual")
                        )
                        FilterChip(
                            selected = selectedCurrencyMode == CurrencyMode.KHR,
                            onClick = { selectedCurrencyMode = CurrencyMode.KHR },
                            label = { Text("KHR (៛)", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RetailTealPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("receipt_currency_chip_khr")
                        )
                        FilterChip(
                            selected = selectedCurrencyMode == CurrencyMode.USD,
                            onClick = { selectedCurrencyMode = CurrencyMode.USD },
                            label = { Text("USD ($)", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RetailTealPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("receipt_currency_chip_usd")
                        )
                    }
                }
            }

            // 3. PDF Document Banner
            Surface(
                color = Color(0xFFF0FDF4),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("receipt_pdf_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.PictureAsPdf,
                                contentDescription = "PDF Document",
                                tint = Color(0xFF15803D),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isGeneratingPdf) "Generating PDF Document..." else "PDF Receipt Ready",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF166534)
                            )
                            Text(
                                text = "Receipt_${sale.receiptNumber}.pdf • Itemized & KHR Formatted",
                                fontSize = 11.sp,
                                color = RetailSlate500
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // View PDF button
                        OutlinedButton(
                            onClick = {
                                val file = pdfFile ?: PdfReceiptGenerator.generateReceiptPdf(context, sale, items, khrRate).also { pdfFile = it }
                                PdfReceiptGenerator.viewReceiptPdf(context, file)
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("view_pdf_receipt_button")
                        ) {
                            Text("View", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        // Share PDF button
                        Button(
                            onClick = {
                                val file = pdfFile ?: PdfReceiptGenerator.generateReceiptPdf(context, sale, items, khrRate).also { pdfFile = it }
                                PdfReceiptGenerator.shareReceiptPdf(context, file, sale.receiptNumber)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("share_pdf_receipt_button")
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = "Share", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 4. Authentic Digital Thermal Receipt Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFFBEB), // Warm thermal paper tint
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("thermal_receipt_card")
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Store Header
                    Text(
                        text = "RETAIL POS STORE",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp,
                        color = RetailSlate900
                    )
                    Text(
                        text = "123 Norodom Blvd • Daun Penh, Phnom Penh",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = RetailSlate500,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Tel: +855 23 888 999 • VAT TIN: K001-90213847",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = RetailSlate500
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Transaction Metadata Pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFEF3C7), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "RECEIPT: ${sale.receiptNumber}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = RetailSlate900
                        )
                        Text(
                            text = "TERM #01",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = RetailSlate700
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Date: ${formatDateTime(sale.timestamp)}",
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = RetailSlate500
                        )
                        Text(
                            text = "Cashier: ${sale.cashierName}",
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = RetailSlate700
                        )
                    }

                    Text(
                        text = "--------------------------------------------------",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = RetailSlate300,
                        maxLines = 1
                    )

                    // Itemized Product Rows
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Header row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("ITEM / QTY", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = RetailSlate500)
                            Text("PRICE", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = RetailSlate500)
                        }

                        items.forEach { item ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${item.quantity}x ${item.productName}",
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold,
                                        color = RetailSlate900,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = formatPriceByMode(item.itemTotal),
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = RetailSlate900
                                    )
                                }
                                Text(
                                    text = "  SKU: ${item.sku} @ ${formatPriceByMode(item.unitPrice)} each",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = RetailSlate500
                                )
                            }
                        }
                    }

                    Text(
                        text = "--------------------------------------------------",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = RetailSlate300,
                        maxLines = 1
                    )

                    // Calculations & Totals
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("SUBTOTAL (${items.sumOf { it.quantity }} items)", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = RetailSlate700)
                            Text(formatPriceByMode(sale.subtotal), fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = RetailSlate900)
                        }

                        if (sale.discountAmount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("DISCOUNT (${sale.discountPercent.toInt()}%)", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFFB45309))
                                Text("-${formatPriceByMode(sale.discountAmount)}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFFB45309))
                            }
                        }

                        val taxPercent = if (sale.subtotal - sale.discountAmount > 0) {
                            ((sale.taxAmount / (sale.subtotal - sale.discountAmount)) * 100 + 0.5).toInt()
                        } else 8

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("SALES TAX ($taxPercent%)", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = RetailSlate700)
                            Text(formatPriceByMode(sale.taxAmount), fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = RetailSlate900)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Grand Total Box - Highlighted Dual KHR & USD
                        Surface(
                            color = RetailTealDark,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "TOTAL (USD):",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.White
                                    )
                                    Text(
                                        text = CurrencyUtils.formatUsd(sale.totalAmount),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.White
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "TOTAL (KHR ៛):",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFFCCFBF1)
                                    )
                                    Text(
                                        text = CurrencyUtils.formatKhrRaw(totalKhr),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF5EEAD4)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Exchange rate reference
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("EXCHANGE RATE", fontSize = 10.sp, color = RetailSlate500, fontFamily = FontFamily.Monospace)
                            Text("1 USD = ${CurrencyUtils.formatKhrRaw(khrRate.toLong())}", fontSize = 10.sp, color = RetailSlate500, fontFamily = FontFamily.Monospace)
                        }

                        // Payment & Tender Details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("PAYMENT METHOD", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = RetailSlate700)
                            Text("${sale.paymentMethod}", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = RetailSlate900)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("AMOUNT TENDERED", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = RetailSlate700)
                            Text(
                                text = "${CurrencyUtils.formatUsd(sale.amountTendered)} (${CurrencyUtils.formatKhrRaw(tenderedKhr)})",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = RetailSlate900
                            )
                        }

                        if (sale.changeGiven > 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFECFDF5), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CHANGE DUE",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF047857)
                                )
                                Text(
                                    text = "${CurrencyUtils.formatUsd(sale.changeGiven)} • ${CurrencyUtils.formatKhrRaw(changeKhr)}",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF047857)
                                )
                            }
                        }
                    }

                    // KHQR Digital Badge if paid with KHQR
                    if (sale.paymentMethod == "KHQR") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = Color(0xFFF0FDF4),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFDC2626), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("KHQR", color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Bakong Universal KHQR Paid", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF166534))
                                    Text("Ref: BAKONG-${sale.receiptNumber}", fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, color = RetailSlate500)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Simulated Barcode for scanning returns
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .background(Color.White, RoundedCornerShape(4.dp))
                            .border(0.5.dp, RetailSlate300, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "||| || | |||| || | ||| |||| | ||| |||",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 18.sp,
                            letterSpacing = 2.sp,
                            color = RetailSlate900
                        )
                    }

                    // Security verification footer
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Shield, contentDescription = "Verified", tint = RetailTealPrimary, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SHA-256 Tamper-Evident Audit Record Active",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = RetailSlate500
                        )
                    }

                    Text(
                        text = "Thank you for shopping with us! • សូមអរគុណ!",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = RetailSlate700,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Goods sold are returnable within 7 days with this digital receipt.",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = RetailSlate500,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
