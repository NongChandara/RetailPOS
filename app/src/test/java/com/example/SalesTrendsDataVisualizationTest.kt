package com.example

import com.example.data.model.SaleEntity
import com.example.ui.components.SalesTrendCalculator
import com.example.ui.components.SalesTrendPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SalesTrendsDataVisualizationTest {

    @Test
    fun computeSummary_dailyPeriodGenerates7DataPointsWithAccurateTotals() {
        val now = System.currentTimeMillis()
        val oneDay = 24L * 60 * 60 * 1000

        val sales = listOf(
            SaleEntity(
                id = 1L,
                receiptNumber = "RCP-01",
                timestamp = now - (1000 * 60 * 30), // Today
                cashierId = 1L,
                cashierName = "Cashier",
                subtotal = 100.0,
                taxAmount = 8.0,
                discountPercent = 0.0,
                discountAmount = 0.0,
                totalAmount = 108.0,
                paymentMethod = "CARD",
                amountTendered = 108.0,
                changeGiven = 0.0,
                itemsCount = 2
            ),
            SaleEntity(
                id = 2L,
                receiptNumber = "RCP-02",
                timestamp = now - (oneDay + 1000 * 60 * 60), // Yesterday
                cashierId = 1L,
                cashierName = "Cashier",
                subtotal = 50.0,
                taxAmount = 4.0,
                discountPercent = 0.0,
                discountAmount = 0.0,
                totalAmount = 54.0,
                paymentMethod = "CASH",
                amountTendered = 60.0,
                changeGiven = 6.0,
                itemsCount = 1
            )
        )

        val summary = SalesTrendCalculator.computeSummary(sales, SalesTrendPeriod.DAILY)

        assertEquals(SalesTrendPeriod.DAILY, summary.period)
        assertEquals(7, summary.points.size)
        assertEquals(162.0, summary.totalRevenue, 0.01)
        assertEquals(2, summary.totalTransactions)
        assertEquals(81.0, summary.averageTicket, 0.01)
        assertTrue(summary.peakRevenue >= 108.0)
    }

    @Test
    fun computeSummary_weeklyPeriodGenerates8DataPoints() {
        val now = System.currentTimeMillis()
        val oneWeek = 7L * 24 * 60 * 60 * 1000

        val sales = listOf(
            SaleEntity(
                id = 1L,
                receiptNumber = "RCP-W1",
                timestamp = now - (1000 * 60 * 60),
                cashierId = 1L,
                cashierName = "Cashier",
                subtotal = 200.0,
                taxAmount = 16.0,
                discountPercent = 0.0,
                discountAmount = 0.0,
                totalAmount = 216.0,
                paymentMethod = "CARD",
                amountTendered = 216.0,
                changeGiven = 0.0,
                itemsCount = 5
            ),
            SaleEntity(
                id = 2L,
                receiptNumber = "RCP-W2",
                timestamp = now - (oneWeek * 2),
                cashierId = 1L,
                cashierName = "Cashier",
                subtotal = 150.0,
                taxAmount = 12.0,
                discountPercent = 0.0,
                discountAmount = 0.0,
                totalAmount = 162.0,
                paymentMethod = "CARD",
                amountTendered = 162.0,
                changeGiven = 0.0,
                itemsCount = 3
            )
        )

        val summary = SalesTrendCalculator.computeSummary(sales, SalesTrendPeriod.WEEKLY)

        assertEquals(SalesTrendPeriod.WEEKLY, summary.period)
        assertEquals(8, summary.points.size)
        assertEquals(378.0, summary.totalRevenue, 0.01)
        assertEquals(2, summary.totalTransactions)
        assertEquals(189.0, summary.averageTicket, 0.01)
    }

    @Test
    fun computeSummary_monthlyPeriodGenerates6DataPoints() {
        val now = System.currentTimeMillis()

        val sales = listOf(
            SaleEntity(
                id = 1L,
                receiptNumber = "RCP-M1",
                timestamp = now,
                cashierId = 1L,
                cashierName = "Cashier",
                subtotal = 300.0,
                taxAmount = 24.0,
                discountPercent = 0.0,
                discountAmount = 0.0,
                totalAmount = 324.0,
                paymentMethod = "CARD",
                amountTendered = 324.0,
                changeGiven = 0.0,
                itemsCount = 6
            )
        )

        val summary = SalesTrendCalculator.computeSummary(sales, SalesTrendPeriod.MONTHLY)

        assertEquals(SalesTrendPeriod.MONTHLY, summary.period)
        assertEquals(6, summary.points.size)
        assertEquals(324.0, summary.totalRevenue, 0.01)
        assertEquals(1, summary.totalTransactions)
    }

    @Test
    fun computeSummary_emptySalesHandlesGracefullyWithoutError() {
        val emptySales = emptyList<SaleEntity>()

        SalesTrendPeriod.values().forEach { period ->
            val summary = SalesTrendCalculator.computeSummary(emptySales, period)
            assertNotNull(summary)
            assertEquals(0.0, summary.totalRevenue, 0.001)
            assertEquals(0, summary.totalTransactions)
            assertEquals(0.0, summary.averageTicket, 0.001)
            assertTrue(summary.points.isNotEmpty())
            assertTrue(summary.points.all { it.revenue == 0.0 && it.transactionCount == 0 })
        }
    }
}
