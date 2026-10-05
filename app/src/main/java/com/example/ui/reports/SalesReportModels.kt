package com.example.ui.reports

import com.example.data.model.SaleEntity
import com.example.data.model.SaleItemEntity

enum class ReportPeriod(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    WEEK("Last 7 Days"),
    MONTH("Last 30 Days"),
    ALL("All Time")
}

data class StaffSalesPerformance(
    val cashierId: Long,
    val cashierName: String,
    val role: String = "Staff",
    val transactionsCount: Int,
    val totalRevenue: Double,
    val averageTicket: Double,
    val percentageOfTotal: Double
)

data class ProductSalesPerformance(
    val productId: Long,
    val productName: String,
    val sku: String,
    val quantitySold: Int,
    val totalRevenue: Double,
    val unitPrice: Double
)

data class CategorySalesPerformance(
    val categoryName: String,
    val quantitySold: Int,
    val totalRevenue: Double,
    val percentageOfTotal: Double
)

data class HourlySalesData(
    val hourLabel: String,
    val orderCount: Int,
    val totalRevenue: Double
)

data class SalesReportSummary(
    val period: ReportPeriod,
    val periodLabel: String,
    val dateRangeDisplay: String,
    val filteredSales: List<SaleEntity>,
    val filteredSaleItems: List<SaleItemEntity>,
    val totalGrossRevenue: Double,
    val totalCogs: Double,
    val grossProfit: Double,
    val profitMarginPercent: Double,
    val transactionsCount: Int,
    val averageTicket: Double,
    val totalItemsSold: Int,
    val totalDiscounts: Double,
    val totalTax: Double,
    val cashTotal: Double,
    val cashCount: Int,
    val cardTotal: Double,
    val cardCount: Int,
    val digitalTotal: Double,
    val digitalCount: Int,
    val staffPerformance: List<StaffSalesPerformance>,
    val topProducts: List<ProductSalesPerformance>,
    val categorySales: List<CategorySalesPerformance>,
    val hourlySales: List<HourlySalesData>
)
