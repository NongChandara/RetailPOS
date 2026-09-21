package com.example.ui.sales

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SaleEntity
import com.example.ui.MainViewModel
import com.example.ui.components.RechartsSalesTrendsChart
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDateTime
import com.example.ui.pos.ReceiptDialog
import com.example.ui.theme.RetailSlate100
import com.example.ui.theme.RetailSlate300
import com.example.ui.theme.RetailSlate500
import com.example.ui.theme.RetailSlate700
import com.example.ui.theme.RetailSlate900
import com.example.ui.theme.RetailStatusBlue
import com.example.ui.theme.RetailStatusBlueContainer
import com.example.ui.theme.RetailStatusGreen
import com.example.ui.theme.RetailStatusGreenContainer
import com.example.ui.theme.RetailTealLight
import com.example.ui.theme.RetailTealPrimary

@Composable
fun SalesHistoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val sales by viewModel.allSales.collectAsState()
    val completedSalePair by viewModel.completedSale.collectAsState()

    var activeView by remember { mutableStateOf("TRENDS") } // "TRENDS" or "RECEIPTS"

    val totalRevenue = sales.sumOf { it.totalAmount }
    val transactionsCount = sales.size
    val averageTicket = if (transactionsCount > 0) totalRevenue / transactionsCount else 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("sales_history_screen")
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Top Dashboard Navigation Switcher
        Surface(
            color = RetailSlate100,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, RetailSlate300.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_view_switcher")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Trends Dashboard Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (activeView == "TRENDS") Color.White else Color.Transparent)
                        .clickable { activeView = "TRENDS" }
                        .padding(vertical = 8.dp)
                        .testTag("tab_trends_dashboard"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShowChart,
                            contentDescription = null,
                            tint = if (activeView == "TRENDS") RetailTealPrimary else RetailSlate500,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Trends Dashboard",
                            fontSize = 12.sp,
                            fontWeight = if (activeView == "TRENDS") FontWeight.Bold else FontWeight.Medium,
                            color = if (activeView == "TRENDS") RetailSlate900 else RetailSlate500
                        )
                    }
                }

                // Receipts Ledger Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (activeView == "RECEIPTS") Color.White else Color.Transparent)
                        .clickable { activeView = "RECEIPTS" }
                        .padding(vertical = 8.dp)
                        .testTag("tab_receipts_ledger"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = if (activeView == "RECEIPTS") RetailTealPrimary else RetailSlate500,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Receipts Ledger (${sales.size})",
                            fontSize = 12.sp,
                            fontWeight = if (activeView == "RECEIPTS") FontWeight.Bold else FontWeight.Medium,
                            color = if (activeView == "RECEIPTS") RetailSlate900 else RetailSlate500
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (activeView == "TRENDS") {
            // TRENDS DASHBOARD VIEW
            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("trends_dashboard_scroll_content")
            ) {
                // 1. Recharts Data Visualization Component
                item {
                    RechartsSalesTrendsChart(
                        sales = sales,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 2. High-Level Performance Ledger Banner
                item {
                    Surface(
                        color = RetailSlate900,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "All-Time Sales Ledger",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Surface(
                                    color = RetailTealPrimary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "AGGREGATE",
                                        color = RetailTealLight,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Total Revenue", color = RetailSlate500, fontSize = 11.sp)
                                    Text(
                                        formatCurrency(totalRevenue),
                                        color = RetailTealLight,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp
                                    )
                                }
                                Column {
                                    Text("Transactions", color = RetailSlate500, fontSize = 11.sp)
                                    Text(
                                        "$transactionsCount",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                }
                                Column {
                                    Text("Average Sale", color = RetailSlate500, fontSize = 11.sp)
                                    Text(
                                        formatCurrency(averageTicket),
                                        color = RetailStatusGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Recent Transactions Header & List
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Transactions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = RetailSlate900
                        )
                        if (sales.isNotEmpty()) {
                            Text(
                                text = "View All (${sales.size}) →",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RetailTealPrimary,
                                modifier = Modifier
                                    .clickable { activeView = "RECEIPTS" }
                                    .padding(4.dp)
                            )
                        }
                    }
                }

                if (sales.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No sales recorded yet. Ring up transactions in POS!",
                                color = RetailSlate500,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    // Display top 5 most recent sales
                    items(sales.take(5), key = { it.id }) { sale ->
                        SaleRowCard(
                            sale = sale,
                            onClick = { viewModel.viewPastReceipt(sale) }
                        )
                    }
                }
            }
        } else {
            // FULL RECEIPTS LEDGER VIEW
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "All Completed Transactions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = RetailSlate900
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (sales.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = RetailSlate300,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No sales recorded yet.\nRing up transactions in POS to build your sales ledger!",
                                color = RetailSlate500,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 20.dp, top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(sales, key = { it.id }) { sale ->
                            SaleRowCard(
                                sale = sale,
                                onClick = { viewModel.viewPastReceipt(sale) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal receipt dialog for re-viewing any sale
    completedSalePair?.let { (sale, items) ->
        ReceiptDialog(
            sale = sale,
            items = items,
            onDismiss = { viewModel.dismissReceipt() }
        )
    }
}

@Composable
fun SaleRowCard(
    sale: SaleEntity,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("sale_card_${sale.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = sale.receiptNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        color = RetailSlate900
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    PaymentMethodBadge(method = sale.paymentMethod)
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${formatDateTime(sale.timestamp)} • Cashier: ${sale.cashierName}",
                    fontSize = 11.sp,
                    color = RetailSlate500
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${sale.itemsCount} items sold",
                    fontSize = 11.sp,
                    color = RetailSlate700
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(sale.totalAmount),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = RetailTealPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Color(0xFFF0FDF4),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF86EFAC))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.PictureAsPdf,
                            contentDescription = null,
                            tint = Color(0xFF15803D),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "PDF Receipt",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF15803D)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodBadge(method: String) {
    val (icon, label) = when (method.uppercase()) {
        "CARD" -> Pair(Icons.Filled.CreditCard, "Card")
        "DIGITAL" -> Pair(Icons.Filled.QrCode, "QR/NFC")
        else -> Pair(Icons.Filled.Money, "Cash")
    }

    Surface(
        color = RetailSlate100,
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = RetailSlate700, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RetailSlate700)
        }
    }
}
