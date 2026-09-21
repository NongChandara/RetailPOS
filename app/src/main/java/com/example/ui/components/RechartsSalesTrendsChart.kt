package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SaleEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

enum class SalesTrendPeriod(val label: String, val subtitle: String) {
    DAILY("Daily", "Past 7 Days"),
    WEEKLY("Weekly", "Past 8 Weeks"),
    MONTHLY("Monthly", "Past 6 Months")
}

enum class ChartDisplayType(val label: String) {
    COMPOSED("Composed"),
    AREA("Area Trend"),
    BAR("Bar Chart")
}

data class SalesTrendPoint(
    val id: String,
    val label: String,
    val fullLabel: String,
    val revenue: Double,
    val transactionCount: Int,
    val averageTicket: Double,
    val timestamp: Long
)

data class SalesTrendSummary(
    val period: SalesTrendPeriod,
    val totalRevenue: Double,
    val totalTransactions: Int,
    val averageTicket: Double,
    val peakPeriodLabel: String,
    val peakRevenue: Double,
    val growthRatePercent: Double?,
    val points: List<SalesTrendPoint>
)

object SalesTrendCalculator {

    fun computeSummary(sales: List<SaleEntity>, period: SalesTrendPeriod): SalesTrendSummary {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        return when (period) {
            SalesTrendPeriod.DAILY -> computeDailyTrends(sales, now, calendar)
            SalesTrendPeriod.WEEKLY -> computeWeeklyTrends(sales, now, calendar)
            SalesTrendPeriod.MONTHLY -> computeMonthlyTrends(sales, now, calendar)
        }
    }

    private fun computeDailyTrends(sales: List<SaleEntity>, now: Long, cal: Calendar): SalesTrendSummary {
        val daysCount = 7
        val points = mutableListOf<SalesTrendPoint>()
        val dayFormat = SimpleDateFormat("EEE d", Locale.US)
        val fullFormat = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.US)

        val dayInMillis = 24L * 60 * 60 * 1000

        // Calculate start of each day for the past 7 days (oldest to newest)
        for (i in (daysCount - 1) downTo 0) {
            cal.timeInMillis = now - (i * dayInMillis)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val startOfDay = cal.timeInMillis

            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val endOfDay = cal.timeInMillis

            val daySales = sales.filter { it.timestamp in startOfDay..endOfDay }
            val rev = daySales.sumOf { it.totalAmount }
            val count = daySales.size
            val avg = if (count > 0) rev / count else 0.0

            val dateObj = Date(startOfDay)
            points.add(
                SalesTrendPoint(
                    id = "day_$i",
                    label = if (i == 0) "Today" else dayFormat.format(dateObj),
                    fullLabel = fullFormat.format(dateObj),
                    revenue = rev,
                    transactionCount = count,
                    averageTicket = avg,
                    timestamp = startOfDay
                )
            )
        }

        // Calculate growth rate vs prior 7 days
        val currentRevenue = points.sumOf { it.revenue }
        val currentOrders = points.sumOf { it.transactionCount }
        val currentAvg = if (currentOrders > 0) currentRevenue / currentOrders else 0.0

        val priorStart = now - (daysCount * 2 * dayInMillis)
        val priorEnd = now - (daysCount * dayInMillis)
        val priorRevenue = sales.filter { it.timestamp in priorStart until priorEnd }.sumOf { it.totalAmount }

        val growth = if (priorRevenue > 0) {
            ((currentRevenue - priorRevenue) / priorRevenue) * 100.0
        } else if (currentRevenue > 0) {
            100.0
        } else null

        val peak = points.maxByOrNull { it.revenue }

        return SalesTrendSummary(
            period = SalesTrendPeriod.DAILY,
            totalRevenue = currentRevenue,
            totalTransactions = currentOrders,
            averageTicket = currentAvg,
            peakPeriodLabel = peak?.label ?: "-",
            peakRevenue = peak?.revenue ?: 0.0,
            growthRatePercent = growth,
            points = points
        )
    }

    private fun computeWeeklyTrends(sales: List<SaleEntity>, now: Long, cal: Calendar): SalesTrendSummary {
        val weeksCount = 8
        val points = mutableListOf<SalesTrendPoint>()
        val weekFormat = SimpleDateFormat("MMM d", Locale.US)
        val weekSpanFormat = SimpleDateFormat("MMM d", Locale.US)

        val weekInMillis = 7L * 24 * 60 * 60 * 1000

        for (w in (weeksCount - 1) downTo 0) {
            val weekEnd = now - (w * weekInMillis)
            val weekStart = weekEnd - weekInMillis

            val weekSales = sales.filter {
                if (w == 0) it.timestamp in weekStart..weekEnd
                else it.timestamp >= weekStart && it.timestamp < weekEnd
            }
            val rev = weekSales.sumOf { it.totalAmount }
            val count = weekSales.size
            val avg = if (count > 0) rev / count else 0.0

            val startDateObj = Date(weekStart)
            val endDateObj = Date(weekEnd)

            points.add(
                SalesTrendPoint(
                    id = "week_$w",
                    label = if (w == 0) "Current" else weekFormat.format(startDateObj),
                    fullLabel = "${weekSpanFormat.format(startDateObj)} – ${weekSpanFormat.format(endDateObj)}",
                    revenue = rev,
                    transactionCount = count,
                    averageTicket = avg,
                    timestamp = weekStart
                )
            )
        }

        val currentRevenue = points.sumOf { it.revenue }
        val currentOrders = points.sumOf { it.transactionCount }
        val currentAvg = if (currentOrders > 0) currentRevenue / currentOrders else 0.0

        val priorStart = now - (weeksCount * 2 * weekInMillis)
        val priorEnd = now - (weeksCount * weekInMillis)
        val priorRevenue = sales.filter { it.timestamp in priorStart until priorEnd }.sumOf { it.totalAmount }

        val growth = if (priorRevenue > 0) {
            ((currentRevenue - priorRevenue) / priorRevenue) * 100.0
        } else if (currentRevenue > 0) {
            100.0
        } else null

        val peak = points.maxByOrNull { it.revenue }

        return SalesTrendSummary(
            period = SalesTrendPeriod.WEEKLY,
            totalRevenue = currentRevenue,
            totalTransactions = currentOrders,
            averageTicket = currentAvg,
            peakPeriodLabel = peak?.label ?: "-",
            peakRevenue = peak?.revenue ?: 0.0,
            growthRatePercent = growth,
            points = points
        )
    }

    private fun computeMonthlyTrends(sales: List<SaleEntity>, now: Long, cal: Calendar): SalesTrendSummary {
        val monthsCount = 6
        val points = mutableListOf<SalesTrendPoint>()
        val monthLabelFormat = SimpleDateFormat("MMM", Locale.US)
        val monthFullFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

        for (m in (monthsCount - 1) downTo 0) {
            cal.timeInMillis = now
            cal.add(Calendar.MONTH, -m)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val monthStart = cal.timeInMillis

            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val monthEnd = cal.timeInMillis

            val monthSales = sales.filter { it.timestamp in monthStart..monthEnd }
            val rev = monthSales.sumOf { it.totalAmount }
            val count = monthSales.size
            val avg = if (count > 0) rev / count else 0.0

            val dateObj = Date(monthStart)
            points.add(
                SalesTrendPoint(
                    id = "month_$m",
                    label = monthLabelFormat.format(dateObj),
                    fullLabel = monthFullFormat.format(dateObj),
                    revenue = rev,
                    transactionCount = count,
                    averageTicket = avg,
                    timestamp = monthStart
                )
            )
        }

        val currentRevenue = points.sumOf { it.revenue }
        val currentOrders = points.sumOf { it.transactionCount }
        val currentAvg = if (currentOrders > 0) currentRevenue / currentOrders else 0.0

        val peak = points.maxByOrNull { it.revenue }

        // Compare first 3 months vs last 3 months
        val recentRevenue = points.takeLast(3).sumOf { it.revenue }
        val earlierRevenue = points.take(3).sumOf { it.revenue }
        val growth = if (earlierRevenue > 0) {
            ((recentRevenue - earlierRevenue) / earlierRevenue) * 100.0
        } else if (recentRevenue > 0) {
            100.0
        } else null

        return SalesTrendSummary(
            period = SalesTrendPeriod.MONTHLY,
            totalRevenue = currentRevenue,
            totalTransactions = currentOrders,
            averageTicket = currentAvg,
            peakPeriodLabel = peak?.label ?: "-",
            peakRevenue = peak?.revenue ?: 0.0,
            growthRatePercent = growth,
            points = points
        )
    }
}

/**
 * Recharts-inspired interactive data visualization component for sales trends in Jetpack Compose.
 * Features:
 * - Responsive Cartesian Grid with dashed horizontal ticks & formatted Y-axis values
 * - Monotone Cubic Bezier Area Chart with vertical teal gradient
 * - Rounded Corner Bar Chart columns
 * - Dual-metric Composed Chart (Revenue Bars + Order Count Line)
 * - Interactive Cursor and Tooltip card triggered by touch/drag across data points
 * - Daily, Weekly, and Monthly time horizon switcher
 * - Metric Summary Badges with growth indicator
 */
@Composable
fun RechartsSalesTrendsChart(
    sales: List<SaleEntity>,
    modifier: Modifier = Modifier,
    initialPeriod: SalesTrendPeriod = SalesTrendPeriod.DAILY
) {
    var selectedPeriod by remember { mutableStateOf(initialPeriod) }
    var chartType by remember { mutableStateOf(ChartDisplayType.COMPOSED) }
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

    val summary = remember(sales, selectedPeriod) {
        SalesTrendCalculator.computeSummary(sales, selectedPeriod)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("recharts_sales_trends_component")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 1. Header with Title & Period Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0F766E).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ShowChart,
                                contentDescription = "Sales Trends",
                                tint = Color(0xFF0F766E),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sales Trends",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "RECHARTS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F766E),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "${selectedPeriod.subtitle} • Real-time Trends",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }

                // Granularity Switcher Pills (Daily, Weekly, Monthly)
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.testTag("period_selector_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        SalesTrendPeriod.values().forEach { period ->
                            val isSelected = selectedPeriod == period
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) Color(0xFF0F766E) else Color.Transparent)
                                    .clickable {
                                        selectedPeriod = period
                                        selectedPointIndex = null
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                    .testTag("period_${period.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = period.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF475569)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Chart View Mode Controls (Composed / Area / Bar)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        Triple(ChartDisplayType.COMPOSED, Icons.Filled.Layers, "Composed"),
                        Triple(ChartDisplayType.AREA, Icons.Filled.ShowChart, "Area"),
                        Triple(ChartDisplayType.BAR, Icons.Filled.BarChart, "Bar")
                    ).forEach { (type, icon, label) ->
                        val isSelected = chartType == type
                        Surface(
                            color = if (isSelected) Color(0xFFCCFBF1) else Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF0F766E) else Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable { chartType = type }
                                .testTag("chart_type_${type.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) Color(0xFF0F766E) else Color(0xFF64748B),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = label,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF0F766E) else Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }

                // Interactive tooltip hint
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.TouchApp,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (selectedPointIndex != null) "Point Selected" else "Touch to inspect",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Recharts Canvas Rendering Area with Tooltip Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .testTag("recharts_canvas_container")
            ) {
                RechartsCanvasChart(
                    points = summary.points,
                    chartType = chartType,
                    selectedIndex = selectedPointIndex,
                    onPointSelected = { index ->
                        selectedPointIndex = index
                    }
                )

                // Recharts Floating Tooltip Card (Rendered when a point is touched)
                selectedPointIndex?.let { idx ->
                    if (idx in summary.points.indices) {
                        val pt = summary.points[idx]
                        RechartsFloatingTooltip(
                            point = pt,
                            pointIndex = idx,
                            totalPoints = summary.points.size,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Recharts Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Revenue Legend Item
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp, 10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF0F766E))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Revenue ($)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF334155)
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                // Order Volume Legend Item
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF59E0B))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Transactions (Qty)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF334155)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. KPI Metric Highlights Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total Period Revenue
                Surface(
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Total Sales", fontSize = 10.sp, color = Color(0xFF64748B))
                            summary.growthRatePercent?.let { rate ->
                                val isPositive = rate >= 0
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isPositive) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                                        contentDescription = null,
                                        tint = if (isPositive) Color(0xFF16A34A) else Color(0xFFDC2626),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = "${if (isPositive) "+" else ""}${rate.roundToInt()}%",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPositive) Color(0xFF16A34A) else Color(0xFFDC2626)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatCurrency(summary.totalRevenue),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F766E)
                        )
                    }
                }

                // Total Orders
                Surface(
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Volume", fontSize = 10.sp, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${summary.totalTransactions} Orders",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                // Average Basket
                Surface(
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Average Ticket", fontSize = 10.sp, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatCurrency(summary.averageTicket),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color(0xFF2563EB)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Custom Canvas chart mimicking Recharts Cartesian Grid, Spline Area Curves,
 * Rounded Bars, Secondary Transaction Line, and Touch Hover Cursor.
 */
@Composable
private fun RechartsCanvasChart(
    points: List<SalesTrendPoint>,
    chartType: ChartDisplayType,
    selectedIndex: Int?,
    onPointSelected: (Int?) -> Unit
) {
    val density = LocalDensity.current

    val maxRevenue = remember(points) {
        val maxVal = points.maxOfOrNull { it.revenue } ?: 0.0
        if (maxVal <= 0.0) 100.0 else maxVal * 1.15 // Add 15% headroom for clean rendering
    }

    val maxOrders = remember(points) {
        val maxVal = points.maxOfOrNull { it.transactionCount } ?: 0
        if (maxVal <= 0) 10 else maxVal
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(points) {
                detectTapGestures(
                    onTap = { offset ->
                        val leftPad = 48.dp.toPx()
                        val rightPad = 16.dp.toPx()
                        val availableWidth = size.width - leftPad - rightPad
                        if (points.isNotEmpty() && offset.x in leftPad..(size.width - rightPad)) {
                            val relX = offset.x - leftPad
                            val step = availableWidth / max(1, points.size - 1)
                            val idx = (relX / step).roundToInt().coerceIn(0, points.size - 1)
                            onPointSelected(idx)
                        } else {
                            onPointSelected(null)
                        }
                    }
                )
            }
            .pointerInput(points) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val leftPad = 48.dp.toPx()
                        val rightPad = 16.dp.toPx()
                        val availableWidth = size.width - leftPad - rightPad
                        if (points.isNotEmpty() && offset.x in leftPad..(size.width - rightPad)) {
                            val relX = offset.x - leftPad
                            val step = availableWidth / max(1, points.size - 1)
                            val idx = (relX / step).roundToInt().coerceIn(0, points.size - 1)
                            onPointSelected(idx)
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val leftPad = 48.dp.toPx()
                        val rightPad = 16.dp.toPx()
                        val availableWidth = size.width - leftPad - rightPad
                        if (points.isNotEmpty() && change.position.x in leftPad..(size.width - rightPad)) {
                            val relX = change.position.x - leftPad
                            val step = availableWidth / max(1, points.size - 1)
                            val idx = (relX / step).roundToInt().coerceIn(0, points.size - 1)
                            onPointSelected(idx)
                        }
                    },
                    onDragEnd = {
                        // Keep the point selected on drag end for inspection
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (points.isEmpty()) return@Canvas

            val leftPad = 48.dp.toPx()
            val rightPad = 16.dp.toPx()
            val topPad = 22.dp.toPx()
            val bottomPad = 28.dp.toPx()

            val chartWidth = size.width - leftPad - rightPad
            val chartHeight = size.height - topPad - bottomPad
            val baselineY = size.height - bottomPad

            // Paint for text
            val textPaint = Paint().apply {
                color = android.graphics.Color.rgb(148, 163, 184) // Slate 400
                textSize = 9.5f * density.density
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                isAntiAlias = true
            }

            // A. Cartesian Grid (Horizontal lines at 0%, 25%, 50%, 75%, 100%)
            val gridSteps = 4
            val gridPathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

            for (step in 0..gridSteps) {
                val ratio = step.toFloat() / gridSteps
                val y = baselineY - (ratio * chartHeight)
                val gridVal = ratio * maxRevenue

                // Draw horizontal dashed grid line
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(leftPad, y),
                    end = Offset(size.width - rightPad, y),
                    strokeWidth = 1f,
                    pathEffect = gridPathEffect
                )

                // Format Y-axis value: e.g. "$1.2k" or "$500" or "$0"
                val labelText = formatAxisCurrency(gridVal)
                textPaint.textAlign = Paint.Align.RIGHT
                drawContext.canvas.nativeCanvas.drawText(
                    labelText,
                    leftPad - 8f,
                    y + (textPaint.textSize / 3f),
                    textPaint
                )
            }

            val stepX = if (points.size > 1) chartWidth / (points.size - 1) else chartWidth

            // Calculate exact (x, y) coordinates for each point
            val pointCoords = points.mapIndexed { i, pt ->
                val x = leftPad + (i * stepX)
                val y = baselineY - ((pt.revenue.toFloat() / maxRevenue.toFloat()) * chartHeight)
                Offset(x, y)
            }

            // B. Draw Bar Chart or Composed Bars
            if (chartType == ChartDisplayType.BAR || chartType == ChartDisplayType.COMPOSED) {
                val barWidth = (stepX * 0.48f).coerceIn(12.dp.toPx(), 28.dp.toPx())

                points.forEachIndexed { i, pt ->
                    val x = leftPad + (i * stepX) - (barWidth / 2f)
                    val barHeight = ((pt.revenue.toFloat() / maxRevenue.toFloat()) * chartHeight).coerceAtLeast(3f)
                    val barTop = baselineY - barHeight

                    val isSelected = selectedIndex == i

                    val barBrush = if (isSelected) {
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF0D9488), Color(0xFF0F766E)),
                            startY = barTop,
                            endY = baselineY
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF14B8A6), Color(0xFF0F766E).copy(alpha = 0.85f)),
                            startY = barTop,
                            endY = baselineY
                        )
                    }

                    drawRoundRect(
                        brush = barBrush,
                        topLeft = Offset(x, barTop),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    if (isSelected) {
                        // Accent highlight border for selected bar
                        drawRoundRect(
                            color = Color(0xFF042F2E),
                            topLeft = Offset(x - 1f, barTop - 1f),
                            size = Size(barWidth + 2f, barHeight + 2f),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                            style = Stroke(width = 1.5f.dp.toPx())
                        )
                    }
                }
            }

            // C. Draw Smooth Curved Monotone Spline (Area Chart Mode)
            if (chartType == ChartDisplayType.AREA) {
                if (pointCoords.size >= 2) {
                    val linePath = Path()
                    val areaPath = Path()

                    linePath.moveTo(pointCoords.first().x, pointCoords.first().y)
                    areaPath.moveTo(pointCoords.first().x, baselineY)
                    areaPath.lineTo(pointCoords.first().x, pointCoords.first().y)

                    for (i in 0 until pointCoords.size - 1) {
                        val p0 = pointCoords[i]
                        val p1 = pointCoords[i + 1]
                        val controlX = (p0.x + p1.x) / 2f

                        linePath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                        areaPath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                    }

                    areaPath.lineTo(pointCoords.last().x, baselineY)
                    areaPath.close()

                    // Gradient fill under the curve
                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0F766E).copy(alpha = 0.38f),
                                Color(0xFF0F766E).copy(alpha = 0.02f)
                            ),
                            startY = topPad,
                            endY = baselineY
                        )
                    )

                    // Curve stroke
                    drawPath(
                        path = linePath,
                        color = Color(0xFF0F766E),
                        style = Stroke(
                            width = 2.5f.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )

                    // Draw dots on line
                    pointCoords.forEachIndexed { i, coord ->
                        val isSelected = selectedIndex == i
                        drawCircle(
                            color = Color.White,
                            radius = if (isSelected) 5.dp.toPx() else 3.5f.dp.toPx(),
                            center = coord
                        )
                        drawCircle(
                            color = Color(0xFF0F766E),
                            radius = if (isSelected) 5.dp.toPx() else 3.5f.dp.toPx(),
                            center = coord,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }

            // D. Secondary Metric (Transaction Volume Line in Composed Chart Mode)
            if (chartType == ChartDisplayType.COMPOSED && pointCoords.size >= 2) {
                val orderCoords = points.mapIndexed { i, pt ->
                    val x = leftPad + (i * stepX)
                    val orderRatio = (pt.transactionCount.toFloat() / maxOrders.toFloat()).coerceIn(0f, 1f)
                    val y = baselineY - (orderRatio * chartHeight * 0.75f) // scale comfortably
                    Offset(x, y)
                }

                val orderPath = Path()
                orderPath.moveTo(orderCoords.first().x, orderCoords.first().y)
                for (i in 0 until orderCoords.size - 1) {
                    val p0 = orderCoords[i]
                    val p1 = orderCoords[i + 1]
                    val controlX = (p0.x + p1.x) / 2f
                    orderPath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                }

                drawPath(
                    path = orderPath,
                    color = Color(0xFFF59E0B),
                    style = Stroke(
                        width = 2f.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )
                )

                // Nodes for orders
                orderCoords.forEach { coord ->
                    drawCircle(
                        color = Color(0xFFF59E0B),
                        radius = 2.5f.dp.toPx(),
                        center = coord
                    )
                }
            }

            // E. X-Axis Baseline & Tick Labels
            drawLine(
                color = Color(0xFFCBD5E1),
                start = Offset(leftPad, baselineY),
                end = Offset(size.width - rightPad, baselineY),
                strokeWidth = 1.5f
            )

            textPaint.textAlign = Paint.Align.CENTER
            points.forEachIndexed { i, pt ->
                val x = leftPad + (i * stepX)
                val isSelected = selectedIndex == i

                textPaint.color = if (isSelected) {
                    android.graphics.Color.rgb(15, 118, 110) // Teal 700
                } else {
                    android.graphics.Color.rgb(100, 116, 139) // Slate 500
                }
                textPaint.typeface = if (isSelected) {
                    Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                } else {
                    Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                }

                drawContext.canvas.nativeCanvas.drawText(
                    pt.label,
                    x,
                    baselineY + 18.dp.toPx(),
                    textPaint
                )
            }

            // F. Interactive Cursor & Indicator for Selected Point
            selectedIndex?.let { idx ->
                if (idx in pointCoords.indices) {
                    val targetCoord = pointCoords[idx]

                    // 1. Vertical Cursor line (Recharts style)
                    drawLine(
                        color = Color(0xFF0F766E).copy(alpha = 0.6f),
                        start = Offset(targetCoord.x, topPad),
                        end = Offset(targetCoord.x, baselineY),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                    )

                    // 2. Outer Halo Ring on point
                    drawCircle(
                        color = Color(0xFF0F766E).copy(alpha = 0.22f),
                        radius = 12.dp.toPx(),
                        center = targetCoord
                    )

                    // 3. Crisp Inner Dot
                    drawCircle(
                        color = Color.White,
                        radius = 5.dp.toPx(),
                        center = targetCoord
                    )
                    drawCircle(
                        color = Color(0xFF0F766E),
                        radius = 5.dp.toPx(),
                        center = targetCoord,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }
    }
}

/**
 * Recharts-styled floating tooltip card showing comprehensive metrics for the inspected point.
 */
@Composable
private fun RechartsFloatingTooltip(
    point: SalesTrendPoint,
    pointIndex: Int,
    totalPoints: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF0F172A), // Slate 900
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = modifier
            .padding(top = 4.dp)
            .testTag("recharts_active_tooltip")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = point.fullLabel,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.White
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2DD4BF))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Revenue:", fontSize = 10.sp, color = Color(0xFF94A3B8))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = formatCurrency(point.revenue),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2DD4BF)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF59E0B))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Orders:", fontSize = 10.sp, color = Color(0xFF94A3B8))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${point.transactionCount} transactions",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            if (point.transactionCount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF60A5FA))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Average:", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = formatCurrency(point.averageTicket),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF60A5FA)
                    )
                }
            }
        }
    }
}

private fun formatAxisCurrency(amount: Double): String {
    return when {
        amount >= 1000.0 -> String.format(Locale.US, "$%.1fk", amount / 1000.0)
        amount <= 0.01 -> "$0"
        else -> String.format(Locale.US, "$%.0f", amount)
    }
}
