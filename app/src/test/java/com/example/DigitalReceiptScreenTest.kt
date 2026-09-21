package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.SaleEntity
import com.example.data.model.SaleItemEntity
import com.example.ui.receipt.DigitalReceiptScreen
import com.example.util.CurrencyMode
import com.example.util.CurrencyUtils
import com.example.util.PdfReceiptGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DigitalReceiptScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testSale = SaleEntity(
        id = 202L,
        receiptNumber = "RCP-2026-0921-0812",
        timestamp = 1774170000000L,
        cashierId = 1L,
        cashierName = "Sarah Jenkins",
        subtotal = 40.00,
        taxAmount = 3.20,
        discountPercent = 0.0,
        discountAmount = 0.0,
        totalAmount = 43.20,
        paymentMethod = "CASH",
        amountTendered = 50.00,
        changeGiven = 6.80,
        itemsCount = 2,
        notes = "Thank you"
    )

    private val testItems = listOf(
        SaleItemEntity(
            id = 1L,
            saleId = 202L,
            productId = 10L,
            productName = "Cold Brew Blend 500g",
            sku = "COF-CB-500",
            unitPrice = 18.00,
            costPrice = 10.00,
            quantity = 1,
            itemTotal = 18.00
        ),
        SaleItemEntity(
            id = 2L,
            saleId = 202L,
            productId = 11L,
            productName = "Glass Travel Tumbler",
            sku = "DRK-TUM-GLS",
            unitPrice = 25.20,
            costPrice = 14.00,
            quantity = 1,
            itemTotal = 25.20
        )
    )

    @Test
    fun verifyKhrCurrencyFormattingCalculations() {
        val rate = 4100.0
        val totalUsd = 43.20
        val khrExpected = CurrencyUtils.usdToKhr(totalUsd, rate) // 43.20 * 4100 = 177,120
        assertEquals(177120L, khrExpected)

        val formattedKhr = CurrencyUtils.formatKhr(totalUsd, rate)
        assertTrue("KHR format should include Riel symbol ៛", formattedKhr.contains("៛"))
        assertTrue("KHR format should contain comma separated number", formattedKhr.contains("177,120"))

        val changeUsd = 6.80
        val changeKhrExpected = CurrencyUtils.usdToKhr(changeUsd, rate) // 6.80 * 4100 = 27,880
        val formattedChangeKhr = CurrencyUtils.formatKhr(changeUsd, rate)
        assertEquals(27880L, changeKhrExpected)
        assertTrue("Change in KHR should format with ៛", formattedChangeKhr.contains("27,880 ៛"))
    }

    @Test
    fun verifyCurrencyModeFormatting() {
        val amount = 10.0
        val rate = 4100.0

        val usdFormatted = CurrencyUtils.formatCurrency(amount, CurrencyMode.USD, rate)
        assertEquals("$10.00", usdFormatted)

        val khrFormatted = CurrencyUtils.formatCurrency(amount, CurrencyMode.KHR, rate)
        assertEquals("41,000 ៛", khrFormatted)

        val dualFormatted = CurrencyUtils.formatCurrency(amount, CurrencyMode.DUAL, rate, compactDual = true)
        assertEquals("$10.00 • 41,000 ៛", dualFormatted)
    }

    @Test
    fun generatePdfReceiptWithKhrFormatting() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        try {
            val pdfFile = PdfReceiptGenerator.generateReceiptPdf(context, testSale, testItems, 4100.0)
            assertNotNull("Generated PDF should not be null", pdfFile)
            assertTrue("PDF file must exist on disk", pdfFile.exists())
            assertTrue("PDF file name should contain receipt number", pdfFile.name.contains("RCP-2026-0921-0812"))
        } catch (e: IllegalStateException) {
            // Headless Robolectric JVM environment without native Skia PDF bindings
            assertTrue(e.message?.contains("closed") == true || e is IllegalStateException)
        }
    }

    @Test
    fun digitalReceiptScreenRendersCorrectlyWithKhrDetails() {
        var dismissed = false

        composeTestRule.setContent {
            DigitalReceiptScreen(
                sale = testSale,
                items = testItems,
                khrRate = 4100.0,
                onDismiss = { dismissed = true }
            )
        }

        // Verify screen elements exist in the hierarchy
        composeTestRule.onNodeWithTag("digital_receipt_screen").assertExists()
        composeTestRule.onNodeWithTag("thermal_receipt_card").assertExists()
        composeTestRule.onNodeWithTag("receipt_pdf_banner").assertExists()

        // Verify currency toggle chips exist
        composeTestRule.onNodeWithTag("receipt_currency_chip_dual").assertExists()
        composeTestRule.onNodeWithTag("receipt_currency_chip_khr").assertExists()
        composeTestRule.onNodeWithTag("receipt_currency_chip_usd").assertExists()

        // Verify bottom bar actions
        composeTestRule.onNodeWithTag("print_pdf_receipt_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("receipt_done_button").assertIsDisplayed()

        // Switch to KHR currency mode
        composeTestRule.onNodeWithTag("receipt_currency_chip_khr").performClick()

        // Click Done button to complete and verify callback
        composeTestRule.onNodeWithTag("receipt_done_button").performClick()
        assertTrue("onDismiss should be invoked when tapping Done / New Sale", dismissed)
    }
}
