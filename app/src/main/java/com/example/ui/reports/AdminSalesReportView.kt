package com.example.ui.reports

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SaleEntity
import com.example.ui.MainViewModel
import com.example.ui.components.RoleBadge
import com.example.ui.components.formatDateTime
import com.example.ui.theme.RetailSlate100
import com.example.ui.theme.RetailSlate300
import com.example.ui.theme.RetailSlate500
import com.example.ui.theme.RetailSlate700
import com.example.ui.theme.RetailSlate900
import com.example.ui.theme.RetailStatusAmber
import com.example.ui.theme.RetailStatusBlue
import com.example.ui.theme.RetailStatusBlueContainer
import com.example.ui.theme.RetailStatusGreen
import com.example.ui.theme.RetailStatusGreenContainer
import com.example.ui.theme.RetailStatusPurple
import com.example.ui.theme.RetailStatusPurpleContainer
import com.example.ui.theme.RetailStatusRed
import com.example.ui.theme.RetailTealLight
import com.example.ui.theme.RetailTealPrimary
import java.util.Locale

@Composable
fun AdminSalesReportView(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val isCurrentUserAdmin by viewModel.isCurrentUserAdmin.collectAsState()
    val summary by viewModel.salesReportSummary.collectAsState()
    val reportPeriod by viewModel.reportPeriod.collectAsState()
    val cashierFilter by viewModel.reportCashierFilter.collectAsState()
    val branchFilter by viewModel.reportBranchFilter.collectAsState()
    val paymentFilter by viewModel.reportPaymentFilter.collectAsState()

    val allUsers by viewModel.allUsers.collectAsState()
    val allBranches by viewModel.allBranches.collectAsState()

    var showZReportDialog by remember { mutableStateOf(false) }
    var showAdminAuthDialog by remember { mutableStateOf(false) }

    // If active user is not admin, show Admin Security Gate
    if (!isCurrentUserAdmin) {
        AdminSecurityGate(
            onUnlockAdmin = { showAdminAuthDialog = true },
            modifier = modifier
        )

        if (showAdminAuthDialog) {
            AdminUnlockDialog(
                viewModel = viewModel,
                onDismiss = { showAdminAuthDialog = false },
                onSuccess = { showAdminAuthDialog = false }
            )
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_sales_report_view")
    ) {
        // 1. Report Header & Title with Export actions
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = RetailSlate900),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("admin_report_header_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(RetailTealPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Assessment,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Executive Sales Report",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = "Store Audit • ${summary.dateRangeDisplay}",
                                    color = RetailSlate500,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Surface(
                            color = RetailTealPrimary.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "ADMIN ONLY",
                                color = RetailTealLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = RetailSlate700.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("AUDIT OPERATOR", color = RetailSlate500, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = currentUser?.name ?: "Sarah Miller (Admin)",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Share Report Button
                            Button(
                                onClick = { viewModel.shareSalesReport(context, summary) },
                                colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp).testTag("btn_share_sales_report")
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("Share", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // Print Z-Report
                            OutlinedButton(
                                onClick = { showZReportDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = BorderStroke(1.dp, RetailSlate500),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp).testTag("btn_z_report_dialog")
                            ) {
                                Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("Z-Report", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 2. Report Period Quick Filter Pills
        item {
            Column {
                Text(
                    text = "Report Period",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RetailSlate900,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ReportPeriod.values()) { period ->
                        val isSelected = reportPeriod == period
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setReportPeriod(period) },
                            label = {
                                Text(
                                    text = period.label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RetailTealPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_period_${period.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // 3. Multi-Dimension Filters: Cashier, Branch, Payment Method
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, RetailSlate100),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.FilterList,
                            contentDescription = null,
                            tint = RetailTealPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Filter Dimensions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = RetailSlate900
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Cashier Filter Dropdown
                        CashierFilterMenu(
                            selectedCashier = cashierFilter,
                            allUsers = allUsers.map { it.name },
                            onSelect = { viewModel.setReportCashierFilter(it) },
                            modifier = Modifier.weight(1f)
                        )

                        // Payment Filter Dropdown
                        PaymentFilterMenu(
                            selectedPayment = paymentFilter,
                            onSelect = { viewModel.setReportPaymentFilter(it) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 4. Primary Hero Financial Card: Gross Revenue & Profit Margins
        item {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, RetailTealPrimary.copy(alpha = 0.2f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth().testTag("hero_financial_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TOTAL GROSS REVENUE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RetailSlate500,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", summary.totalGrossRevenue)}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = RetailSlate900
                            )
                            Text(
                                text = viewModel.formatKhr(summary.totalGrossRevenue),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = RetailTealPrimary
                            )
                        }

                        Surface(
                            color = RetailStatusGreenContainer,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = RetailStatusGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", summary.profitMarginPercent)}% Margin",
                                    color = RetailStatusGreen,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = RetailSlate100)
                    Spacer(modifier = Modifier.height(12.dp))

                    // 3-Column Breakdown: Gross Profit, Cost of Goods (COGS), Tax Collected
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Gross Profit", fontSize = 11.sp, color = RetailSlate500)
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", summary.grossProfit)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = RetailStatusGreen
                            )
                            Text(
                                text = viewModel.formatKhr(summary.grossProfit),
                                fontSize = 10.sp,
                                color = RetailSlate500
                            )
                        }

                        Column {
                            Text("Cost of Goods", fontSize = 11.sp, color = RetailSlate500)
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", summary.totalCogs)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = RetailSlate700
                            )
                            Text(
                                text = "COGS",
                                fontSize = 10.sp,
                                color = RetailSlate500
                            )
                        }

                        Column {
                            Text("Discounts Given", fontSize = 11.sp, color = RetailSlate500)
                            Text(
                                text = "-$${String.format(Locale.US, "%.2f", summary.totalDiscounts)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = RetailStatusRed
                            )
                            Text(
                                text = "+$${String.format(Locale.US, "%.2f", summary.totalTax)} Tax",
                                fontSize = 10.sp,
                                color = RetailSlate500
                            )
                        }
                    }
                }
            }
        }

        // 5. Secondary Operational Metrics: Orders, AOV, Total Units
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Orders
                MetricSmallCard(
                    title = "Orders",
                    value = "${summary.transactionsCount}",
                    subtitle = "transactions",
                    icon = Icons.Filled.Receipt,
                    iconBgColor = RetailStatusBlueContainer,
                    iconTint = RetailStatusBlue,
                    modifier = Modifier.weight(1f)
                )

                // Average Order Value
                MetricSmallCard(
                    title = "Avg Ticket",
                    value = "$${String.format(Locale.US, "%.2f", summary.averageTicket)}",
                    subtitle = "per customer",
                    icon = Icons.Filled.AccountBalanceWallet,
                    iconBgColor = RetailStatusGreenContainer,
                    iconTint = RetailStatusGreen,
                    modifier = Modifier.weight(1f)
                )

                // Total Units Sold
                MetricSmallCard(
                    title = "Units Sold",
                    value = "${summary.totalItemsSold}",
                    subtitle = "items rung up",
                    icon = Icons.Filled.Payments,
                    iconBgColor = RetailStatusPurpleContainer,
                    iconTint = RetailStatusPurple,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 6. Cash Drawer & Payment Tenders Reconciliation Card (X/Z Report reconciliation)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, RetailSlate100),
                modifier = Modifier.fillMaxWidth().testTag("drawer_reconciliation_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Payment Methods & Drawer Audit",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = RetailSlate900
                            )
                            Text(
                                text = "Reconciliation for cashier shift & cash drawers",
                                fontSize = 11.sp,
                                color = RetailSlate500
                            )
                        }
                        Surface(
                            color = RetailSlate100,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${summary.transactionsCount} Tenders",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RetailSlate700,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Multi-Segment Proportion Bar
                    PaymentProportionBar(
                        cashTotal = summary.cashTotal,
                        digitalTotal = summary.digitalTotal,
                        cardTotal = summary.cardTotal,
                        totalRevenue = summary.totalGrossRevenue
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3 Payment Tender Rows
                    PaymentMethodRow(
                        name = "Cash In Drawer",
                        subtitle = "Physical bills & coins collected",
                        amount = summary.cashTotal,
                        khrText = viewModel.formatKhr(summary.cashTotal),
                        count = summary.cashCount,
                        percentage = if (summary.totalGrossRevenue > 0) (summary.cashTotal / summary.totalGrossRevenue) * 100.0 else 0.0,
                        icon = Icons.Filled.Money,
                        indicatorColor = RetailStatusGreen
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = RetailSlate100)

                    PaymentMethodRow(
                        name = "Bakong KHQR / Digital",
                        subtitle = "QR scan & instant bank transfers",
                        amount = summary.digitalTotal,
                        khrText = viewModel.formatKhr(summary.digitalTotal),
                        count = summary.digitalCount,
                        percentage = if (summary.totalGrossRevenue > 0) (summary.digitalTotal / summary.totalGrossRevenue) * 100.0 else 0.0,
                        icon = Icons.Filled.QrCode,
                        indicatorColor = RetailStatusBlue
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = RetailSlate100)

                    PaymentMethodRow(
                        name = "Credit & Debit Cards",
                        subtitle = "Visa, MasterCard, UnionPay",
                        amount = summary.cardTotal,
                        khrText = viewModel.formatKhr(summary.cardTotal),
                        count = summary.cardCount,
                        percentage = if (summary.totalGrossRevenue > 0) (summary.cardTotal / summary.totalGrossRevenue) * 100.0 else 0.0,
                        icon = Icons.Filled.CreditCard,
                        indicatorColor = RetailStatusPurple
                    )
                }
            }
        }

        // 7. Cashier & Staff Sales Performance Leaderboard
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, RetailSlate100),
                modifier = Modifier.fillMaxWidth().testTag("staff_sales_leaderboard_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Staff Sales Performance",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = RetailSlate900
                            )
                            Text(
                                text = "Revenue contribution & cashier accountability",
                                fontSize = 11.sp,
                                color = RetailSlate500
                            )
                        }

                        Surface(
                            color = RetailTealPrimary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${summary.staffPerformance.size} Staff",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RetailTealPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (summary.staffPerformance.isEmpty()) {
                        Text(
                            text = "No staff sales recorded in this timeframe.",
                            fontSize = 12.sp,
                            color = RetailSlate500,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        summary.staffPerformance.forEachIndexed { index, staff ->
                            StaffPerformanceRow(
                                rank = index + 1,
                                staff = staff,
                                viewModel = viewModel
                            )
                            if (index < summary.staffPerformance.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = RetailSlate100)
                            }
                        }
                    }
                }
            }
        }

        // 8. Top Selling Products in Range
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, RetailSlate100),
                modifier = Modifier.fillMaxWidth().testTag("top_products_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Top Selling Products",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = RetailSlate900
                    )
                    Text(
                        text = "Ranked by sales volume & demand",
                        fontSize = 11.sp,
                        color = RetailSlate500
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (summary.topProducts.isEmpty()) {
                        Text(
                            text = "No itemized sales data in this timeframe.",
                            fontSize = 12.sp,
                            color = RetailSlate500,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        summary.topProducts.take(6).forEachIndexed { index, prod ->
                            TopProductRow(
                                rank = index + 1,
                                product = prod,
                                totalRevenue = summary.totalGrossRevenue,
                                viewModel = viewModel
                            )
                            if (index < 5 && index < summary.topProducts.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = RetailSlate100)
                            }
                        }
                    }
                }
            }
        }

        // 9. Category Revenue Breakdown
        if (summary.categorySales.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, RetailSlate100),
                    modifier = Modifier.fillMaxWidth().testTag("category_sales_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Sales by Product Category",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = RetailSlate900
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        summary.categorySales.forEach { cat ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = cat.categoryName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = RetailSlate900
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "$${String.format(Locale.US, "%.2f", cat.totalRevenue)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RetailSlate900
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(${String.format(Locale.US, "%.1f", cat.percentageOfTotal)}%)",
                                            fontSize = 11.sp,
                                            color = RetailSlate500
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { (cat.percentageOfTotal / 100.0).toFloat().coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = RetailTealPrimary,
                                    trackColor = RetailSlate100
                                )
                            }
                        }
                    }
                }
            }
        }

        // 10. Filtered Transactions List Preview
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transactions in Scope (${summary.filteredSales.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = RetailSlate900
                    )
                    Text(
                        text = "Tap to view receipt",
                        fontSize = 11.sp,
                        color = RetailSlate500
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (summary.filteredSales.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No transactions found matching active filters.",
                            color = RetailSlate500,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    summary.filteredSales.take(15).forEach { sale ->
                        ReportSaleTransactionRow(
                            sale = sale,
                            viewModel = viewModel,
                            onClick = { viewModel.viewPastReceipt(sale) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }

        // Spacing bottom padding
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // End-of-Day Z-Report Dialog Modal
    if (showZReportDialog) {
        ZReportModalDialog(
            summary = summary,
            viewModel = viewModel,
            onDismiss = { showZReportDialog = false }
        )
    }
}

@Composable
private fun MetricSmallCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, RetailSlate100),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 11.sp, color = RetailSlate500, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = RetailSlate900)
            Text(subtitle, fontSize = 9.sp, color = RetailSlate500)
        }
    }
}

@Composable
private fun PaymentProportionBar(
    cashTotal: Double,
    digitalTotal: Double,
    cardTotal: Double,
    totalRevenue: Double
) {
    val total = if (totalRevenue > 0) totalRevenue else 1.0
    val cashWeight = (cashTotal / total).toFloat().coerceIn(0f, 1f)
    val digitalWeight = (digitalTotal / total).toFloat().coerceIn(0f, 1f)
    val cardWeight = (cardTotal / total).toFloat().coerceIn(0f, 1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(RetailSlate100)
    ) {
        if (cashWeight > 0.01f) {
            Box(
                modifier = Modifier
                    .weight(cashWeight.coerceAtLeast(0.01f))
                    .fillMaxSize()
                    .background(RetailStatusGreen)
            )
        }
        if (digitalWeight > 0.01f) {
            Box(
                modifier = Modifier
                    .weight(digitalWeight.coerceAtLeast(0.01f))
                    .fillMaxSize()
                    .background(RetailStatusBlue)
            )
        }
        if (cardWeight > 0.01f) {
            Box(
                modifier = Modifier
                    .weight(cardWeight.coerceAtLeast(0.01f))
                    .fillMaxSize()
                    .background(RetailStatusPurple)
            )
        }
    }
}

@Composable
private fun PaymentMethodRow(
    name: String,
    subtitle: String,
    amount: Double,
    khrText: String,
    count: Int,
    percentage: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    indicatorColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(indicatorColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = indicatorColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RetailSlate900)
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(color = RetailSlate100, shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text = "$count txns",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RetailSlate700,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(subtitle, fontSize = 11.sp, color = RetailSlate500)
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$${String.format(Locale.US, "%.2f", amount)}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = RetailSlate900
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${String.format(Locale.US, "%.1f", percentage)}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = indicatorColor
                )
                Text(
                    text = " • $khrText",
                    fontSize = 10.sp,
                    color = RetailSlate500
                )
            }
        }
    }
}

@Composable
private fun StaffPerformanceRow(
    rank: Int,
    staff: StaffSalesPerformance,
    viewModel: MainViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = if (rank == 1) RetailStatusAmber.copy(alpha = 0.2f) else RetailSlate100,
                shape = CircleShape,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (rank == 1) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = RetailStatusAmber,
                            modifier = Modifier.size(15.dp)
                        )
                    } else {
                        Text(
                            text = "#$rank",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = RetailSlate700
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = staff.cashierName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = RetailSlate900
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    RoleBadge(role = staff.role)
                }
                Text(
                    text = "${staff.transactionsCount} orders • Avg: $${String.format(Locale.US, "%.2f", staff.averageTicket)}",
                    fontSize = 11.sp,
                    color = RetailSlate500
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$${String.format(Locale.US, "%.2f", staff.totalRevenue)}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = RetailTealPrimary
            )
            Text(
                text = "${String.format(Locale.US, "%.1f", staff.percentageOfTotal)}% of store sales",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = RetailSlate500
            )
        }
    }
}

@Composable
private fun TopProductRow(
    rank: Int,
    product: ProductSalesPerformance,
    totalRevenue: Double,
    viewModel: MainViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$rank.",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = RetailSlate500,
                modifier = Modifier.width(20.dp)
            )
            Column {
                Text(
                    text = product.productName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = RetailSlate900
                )
                Text(
                    text = "${product.quantitySold} units sold • SKU: ${product.sku}",
                    fontSize = 10.sp,
                    color = RetailSlate500
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$${String.format(Locale.US, "%.2f", product.totalRevenue)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = RetailSlate900
            )
            val share = if (totalRevenue > 0) (product.totalRevenue / totalRevenue) * 100.0 else 0.0
            Text(
                text = "${String.format(Locale.US, "%.1f", share)}% share",
                fontSize = 10.sp,
                color = RetailTealPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ReportSaleTransactionRow(
    sale: SaleEntity,
    viewModel: MainViewModel,
    onClick: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, RetailSlate100),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("report_sale_${sale.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when (sale.paymentMethod.uppercase()) {
                                "CASH" -> RetailStatusGreenContainer
                                "CARD" -> RetailStatusPurpleContainer
                                else -> RetailStatusBlueContainer
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (sale.paymentMethod.uppercase()) {
                            "CASH" -> Icons.Filled.Money
                            "CARD" -> Icons.Filled.CreditCard
                            else -> Icons.Filled.QrCode
                        },
                        contentDescription = null,
                        tint = when (sale.paymentMethod.uppercase()) {
                            "CASH" -> RetailStatusGreen
                            "CARD" -> RetailStatusPurple
                            else -> RetailStatusBlue
                        },
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = sale.receiptNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = RetailSlate900
                    )
                    Text(
                        text = "${formatDateTime(sale.timestamp)} • Cashier: ${sale.cashierName}",
                        fontSize = 10.sp,
                        color = RetailSlate500
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format(Locale.US, "%.2f", sale.totalAmount)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = RetailSlate900
                )
                Text(
                    text = "${sale.itemsCount} items",
                    fontSize = 10.sp,
                    color = RetailSlate500
                )
            }
        }
    }
}

@Composable
private fun CashierFilterMenu(
    selectedCashier: String,
    allUsers: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            color = RetailSlate100,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                Text("Cashier Scope", fontSize = 10.sp, color = RetailSlate500)
                Text(
                    text = if (selectedCashier == "ALL") "All Cashiers" else selectedCashier,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = RetailSlate900
                )
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("All Cashiers", fontWeight = if (selectedCashier == "ALL") FontWeight.Bold else FontWeight.Normal) },
                onClick = {
                    onSelect("ALL")
                    expanded = false
                }
            )
            allUsers.distinct().forEach { userName ->
                DropdownMenuItem(
                    text = { Text(userName, fontWeight = if (selectedCashier == userName) FontWeight.Bold else FontWeight.Normal) },
                    onClick = {
                        onSelect(userName)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PaymentFilterMenu(
    selectedPayment: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            color = RetailSlate100,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                Text("Tender Method", fontSize = 10.sp, color = RetailSlate500)
                Text(
                    text = when (selectedPayment.uppercase()) {
                        "ALL" -> "All Payments"
                        "CASH" -> "Cash Only"
                        "CARD" -> "Card Only"
                        "DIGITAL" -> "KHQR / Digital"
                        else -> selectedPayment
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = RetailSlate900
                )
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("All Payments") },
                onClick = { onSelect("ALL"); expanded = false }
            )
            DropdownMenuItem(
                text = { Text("Cash Only") },
                onClick = { onSelect("CASH"); expanded = false }
            )
            DropdownMenuItem(
                text = { Text("KHQR / Digital") },
                onClick = { onSelect("DIGITAL"); expanded = false }
            )
            DropdownMenuItem(
                text = { Text("Credit / Debit Card") },
                onClick = { onSelect("CARD"); expanded = false }
            )
        }
    }
}

@Composable
private fun AdminSecurityGate(
    onUnlockAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(RetailStatusAmber.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = "Admin Protected",
                        tint = RetailStatusAmber,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Administrator Access Required",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = RetailSlate900,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Financial sales reports, store profit margins, and drawer reconciliation are protected. Switch to or authenticate as an Administrator (e.g., Sarah Miller) to view.",
                    fontSize = 13.sp,
                    color = RetailSlate500,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onUnlockAdmin,
                    colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("btn_unlock_admin_report")
                ) {
                    Icon(Icons.Filled.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unlock with Admin PIN", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun AdminUnlockDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val allUsers by viewModel.allUsers.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Enter Admin PIN",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = RetailSlate900
                )
                Text(
                    text = "Default Admin PIN is 1111 (Sarah Miller)",
                    fontSize = 12.sp,
                    color = RetailSlate500,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 6) {
                            pin = it
                            errorMessage = null
                        }
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    label = { Text("4-digit Admin PIN") },
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it, color = RetailStatusRed) } },
                    modifier = Modifier.fillMaxWidth().testTag("admin_pin_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val adminUser = allUsers.firstOrNull { it.role.equals("ADMIN", ignoreCase = true) && it.pin == pin }
                            if (adminUser != null) {
                                viewModel.setCurrentUser(adminUser)
                                onSuccess()
                            } else {
                                errorMessage = "Incorrect Admin PIN. Try 1111."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("btn_verify_admin_pin")
                    ) {
                        Text("Authorize", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ZReportModalDialog(
    summary: SalesReportSummary,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val formattedText = remember(summary) { viewModel.generateSalesReportText(summary) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "End-of-Day Z-Report",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = RetailSlate900
                        )
                        Text(
                            text = "${summary.period.label} Closing Audit",
                            fontSize = 12.sp,
                            color = RetailSlate500
                        )
                    }

                    Surface(
                        color = RetailStatusGreenContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "VERIFIED",
                            color = RetailStatusGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable receipt monospace text
                Surface(
                    color = RetailSlate100,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .height(300.dp)
                ) {
                    LazyColumn(modifier = Modifier.padding(12.dp)) {
                        item {
                            Text(
                                text = formattedText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = RetailSlate900,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }

                    Button(
                        onClick = { viewModel.shareSalesReport(context, summary) },
                        colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export / Share", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
