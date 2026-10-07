package com.example.service

import android.content.Context
import android.util.Log
import com.example.data.model.SaleEntity
import com.example.data.model.SaleItemEntity
import com.example.util.CurrencyUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class TelegramConfig(
    val botToken: String,
    val chatId: String,
    val isEnabled: Boolean
)

class TelegramNotificationService(private val context: Context) {

    private val prefs = context.getSharedPreferences("telegram_settings", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "TelegramNotification"
        private const val KEY_BOT_TOKEN = "telegram_bot_token"
        private const val KEY_CHAT_ID = "telegram_chat_id"
        private const val KEY_ENABLED = "telegram_enabled"

        // Default Telegram Bot and Channel for TR Store & Cafe
        const val DEFAULT_BOT_TOKEN = "7942738910:AAH-xXJgVfQ6aF3WvXyv770gZk3qYkZ88M0"
        const val DEFAULT_CHAT_ID = "-1002345678901"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<TelegramConfig> = _config.asStateFlow()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private fun loadConfig(): TelegramConfig {
        val token = prefs.getString(KEY_BOT_TOKEN, DEFAULT_BOT_TOKEN) ?: DEFAULT_BOT_TOKEN
        val chatId = prefs.getString(KEY_CHAT_ID, DEFAULT_CHAT_ID) ?: DEFAULT_CHAT_ID
        val enabled = prefs.getBoolean(KEY_ENABLED, true)
        return TelegramConfig(botToken = token, chatId = chatId, isEnabled = enabled)
    }

    fun saveConfig(botToken: String, chatId: String, isEnabled: Boolean) {
        prefs.edit()
            .putString(KEY_BOT_TOKEN, botToken.trim())
            .putString(KEY_CHAT_ID, chatId.trim())
            .putBoolean(KEY_ENABLED, isEnabled)
            .apply()

        _config.value = TelegramConfig(
            botToken = botToken.trim(),
            chatId = chatId.trim(),
            isEnabled = isEnabled
        )
    }

    fun sendTransactionNotification(
        sale: SaleEntity,
        items: List<SaleItemEntity>,
        khrRate: Double = CurrencyUtils.activeKhrExchangeRate
    ) {
        val currentCfg = _config.value
        if (!currentCfg.isEnabled || currentCfg.botToken.isBlank() || currentCfg.chatId.isBlank()) {
            Log.d(TAG, "Telegram notifications disabled or credentials missing")
            return
        }

        coroutineScope.launch {
            try {
                val message = formatTransactionMessage(sale, items, khrRate)
                executeSendMessage(currentCfg.botToken, currentCfg.chatId, message)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send transaction notification to Telegram: ${e.message}", e)
            }
        }
    }

    suspend fun sendTestMessage(token: String, chatId: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                if (token.isBlank() || chatId.isBlank()) {
                    return@withContext Result.failure(IllegalArgumentException("Bot Token and Chat ID cannot be empty."))
                }

                val testMsg = """
                    ☕ <b>TR COFFEE &amp; CAFE • TELEGRAM ALERT TEST</b>
                    ━━━━━━━━━━━━━━━━━━━━━━
                    ✅ <b>Status:</b> Telegram Bot Connected Successfully!
                    🕒 <b>Timestamp:</b> ${SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US).format(Date())}
                    🏪 <b>Store:</b> TR Coffee &amp; Cosmetics Flagship
                    📱 <b>Alert Channel:</b> $chatId
                    ━━━━━━━━━━━━━━━━━━━━━━
                    🎉 Real-time transaction notifications are configured and active.
                """.trimIndent()

                val resp = executeSendMessage(token, chatId, testMsg)
                if (resp.first) {
                    Result.success("Test message sent successfully to Telegram!")
                } else {
                    Result.failure(Exception(resp.second))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
    }

    private fun executeSendMessage(token: String, chatId: String, htmlText: String): Pair<Boolean, String> {
        val url = "https://api.telegram.org/bot$token/sendMessage"

        val json = JSONObject().apply {
            put("chat_id", chatId.trim())
            put("text", htmlText)
            put("parse_mode", "HTML")
            put("disable_web_page_preview", true)
        }

        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    Log.i(TAG, "Telegram notification sent successfully to $chatId")
                    Pair(true, "Success")
                } else {
                    Log.e(TAG, "Telegram API error: Code ${response.code}, Body: $body")
                    // If HTML entity parsing fails, retry with plain text (strip tags) as fallback
                    if (body.contains("can't parse entities", ignoreCase = true) || response.code == 400) {
                        Log.w(TAG, "Retrying Telegram send as plain text without HTML parse_mode...")
                        val plainText = htmlText.replace(Regex("<[^>]*>"), "")
                        val fallbackJson = JSONObject().apply {
                            put("chat_id", chatId.trim())
                            put("text", plainText)
                            put("disable_web_page_preview", true)
                        }
                        val fallbackBody = fallbackJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                        val fallbackReq = Request.Builder().url(url).post(fallbackBody).build()
                        try {
                            client.newCall(fallbackReq).execute().use { fallbackResp ->
                                if (fallbackResp.isSuccessful) {
                                    Log.i(TAG, "Telegram notification delivered via plain text fallback to $chatId")
                                    return Pair(true, "Success (plain-text fallback)")
                                }
                            }
                        } catch (ex: Exception) {
                            Log.e(TAG, "Fallback plain text send also failed: ${ex.message}")
                        }
                    }

                    val errDesc = try {
                        JSONObject(body).optString("description", "HTTP ${response.code}")
                    } catch (_: Exception) {
                        "HTTP ${response.code}"
                    }
                    Pair(false, errDesc)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network exception sending Telegram message: ${e.message}", e)
            Pair(false, e.localizedMessage ?: "Network connection failed")
        }
    }

    private fun formatTransactionMessage(
        sale: SaleEntity,
        items: List<SaleItemEntity>,
        khrRate: Double
    ): String {
        val dateFormatted = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US).format(Date(sale.timestamp))
        val totalUsd = String.format(Locale.US, "%.2f", sale.totalAmount)
        val totalKhr = CurrencyUtils.usdToKhr(sale.totalAmount, khrRate)
        val formattedKhr = String.format(Locale.US, "%,d", totalKhr)

        val itemsSummary = if (items.isNotEmpty()) {
            items.joinToString("\n") { item ->
                val itemPrice = String.format(Locale.US, "%.2f", item.unitPrice)
                val itemTotal = String.format(Locale.US, "%.2f", item.itemTotal)
                val safeName = escapeHtml(item.productName)
                "• <b>$safeName</b> x${item.quantity}  ($$itemPrice) = <b>$$itemTotal</b>"
            }
        } else {
            "• <i>Standard Sale Transaction</i>"
        }

        val paymentBadge = when (sale.paymentMethod.uppercase()) {
            "KHQR" -> "🔴 <b>Bakong Universal KHQR</b>"
            "CASH" -> "💵 <b>Cash</b>"
            else -> "💳 <b>${escapeHtml(sale.paymentMethod)}</b>"
        }

        val tenderInfo = if (sale.paymentMethod.equals("CASH", ignoreCase = true) && sale.amountTendered > 0) {
            "\n💵 <b>Tendered:</b> $${String.format(Locale.US, "%.2f", sale.amountTendered)} | <b>Change:</b> $${String.format(Locale.US, "%.2f", sale.changeGiven)}"
        } else {
            ""
        }

        val discountInfo = if (sale.discountAmount > 0) {
            "\n🏷️ <b>Discount:</b> -$${String.format(Locale.US, "%.2f", sale.discountAmount)} (${sale.discountPercent}%)"
        } else {
            ""
        }

        val notesInfo = if (!sale.notes.isNullOrBlank()) {
            "\n📝 <b>Notes:</b> ${escapeHtml(sale.notes)}"
        } else {
            ""
        }

        val safeReceipt = escapeHtml(sale.receiptNumber)
        val safeBranch = escapeHtml(sale.branchName)
        val safeCashier = escapeHtml(sale.cashierName)

        return """
            ☕ <b>NEW TRANSACTION • TR COFFEE &amp; CAFE</b> 🇰🇭
            ━━━━━━━━━━━━━━━━━━━━━━
            🧾 <b>Receipt:</b> <code>$safeReceipt</code>
            📅 <b>Date:</b> $dateFormatted
            🏪 <b>Branch:</b> $safeBranch
            👨‍💼 <b>Cashier:</b> $safeCashier
            💳 <b>Payment:</b> $paymentBadge$tenderInfo
            ━━━━━━━━━━━━━━━━━━━━━━
            🛒 <b>Items Sold (${sale.itemsCount}):</b>
            $itemsSummary
            ━━━━━━━━━━━━━━━━━━━━━━$discountInfo
            💰 <b>GRAND TOTAL:</b> <b>$$totalUsd</b> (៛$formattedKhr KHR)$notesInfo
            ━━━━━━━━━━━━━━━━━━━━━━
            ✅ <i>Recorded in POS Terminal Database</i>
        """.trimIndent()
    }
}
