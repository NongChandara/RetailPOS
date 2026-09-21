package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

enum class CurrencyMode(val label: String, val symbol: String, val description: String) {
    USD("USD", "$", "US Dollars"),
    KHR("KHR", "៛", "Cambodian Riel"),
    DUAL("Dual", "$ / ៛", "USD & Riel")
}

object CurrencyUtils {
    // Standard retail exchange rate in Cambodia: 1 USD = 4,100 KHR (also common: 4,000 KHR)
    const val DEFAULT_KHR_EXCHANGE_RATE = 4100.0

    var activeCurrencyMode: CurrencyMode = CurrencyMode.DUAL
    var activeKhrExchangeRate: Double = DEFAULT_KHR_EXCHANGE_RATE

    private val usdFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private val khrFormatSymbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ','
    }
    private val khrDecimalFormat = DecimalFormat("#,###", khrFormatSymbols)

    fun usdToKhr(usdAmount: Double, rate: Double = DEFAULT_KHR_EXCHANGE_RATE): Long {
        return (usdAmount * rate).roundToLong()
    }

    fun khrToUsd(khrAmount: Long, rate: Double = DEFAULT_KHR_EXCHANGE_RATE): Double {
        if (rate <= 0.0) return 0.0
        return khrAmount / rate
    }

    fun formatUsd(amount: Double): String {
        return usdFormatter.format(amount)
    }

    fun formatKhr(usdAmount: Double, rate: Double = DEFAULT_KHR_EXCHANGE_RATE): String {
        val riel = usdToKhr(usdAmount, rate)
        return "${khrDecimalFormat.format(riel)} ៛"
    }

    fun formatKhrRaw(rielAmount: Long): String {
        return "${khrDecimalFormat.format(rielAmount)} ៛"
    }

    /**
     * Formats amount according to preferred CurrencyMode.
     * USD: "$2.50"
     * KHR: "10,250 ៛"
     * DUAL: "$2.50 (10,250 ៛)" or "$2.50 • 10,250 ៛"
     */
    fun formatCurrency(
        usdAmount: Double,
        mode: CurrencyMode = activeCurrencyMode,
        rate: Double = activeKhrExchangeRate,
        compactDual: Boolean = false
    ): String {
        return when (mode) {
            CurrencyMode.USD -> formatUsd(usdAmount)
            CurrencyMode.KHR -> formatKhr(usdAmount, rate)
            CurrencyMode.DUAL -> {
                val usdStr = formatUsd(usdAmount)
                val khrStr = formatKhr(usdAmount, rate)
                if (compactDual) {
                    "$usdStr • $khrStr"
                } else {
                    "$usdStr ($khrStr)"
                }
            }
        }
    }
}
