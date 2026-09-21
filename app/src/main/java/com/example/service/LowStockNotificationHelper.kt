package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.data.model.ProductEntity
import com.example.util.CurrencyUtils

object LowStockNotificationHelper {

    const val CHANNEL_ID = "retail_pos_low_stock_channel"
    const val CHANNEL_NAME = "Low Stock Alerts"
    const val CHANNEL_DESCRIPTION = "Alerts store managers when inventory falls below predefined safety thresholds"
    const val NOTIFICATION_ID = 4001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Posts a rich push notification alerting the manager about low-stock inventory items.
     */
    fun showLowStockAlert(
        context: Context,
        lowStockItems: List<ProductEntity>,
        managerName: String = "Store Manager",
        thresholdOverride: Int? = null,
        khrRate: Double = CurrencyUtils.DEFAULT_KHR_EXCHANGE_RATE
    ) {
        createNotificationChannel(context)

        val count = lowStockItems.size
        val title = "⚠️ Manager Alert: $count Low Stock ${if (count == 1) "Item" else "Items"}"

        // Intent to launch app and go directly to Stock tab
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_TAB", "STOCK")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build itemized breakdown for BigTextStyle
        val summaryBuilder = StringBuilder()
        summaryBuilder.append("Attention $managerName: The following items are below safety stock:\n")

        lowStockItems.take(5).forEach { item ->
            val effectiveThreshold = thresholdOverride ?: item.minStockThreshold
            val khrPrice = CurrencyUtils.formatKhr(item.sellingPrice, khrRate)
            summaryBuilder.append("• ${item.name}: ${item.stockQuantity} ${item.unit} left (Threshold: $effectiveThreshold) - $khrPrice\n")
        }

        if (count > 5) {
            summaryBuilder.append("... and ${count - 5} more items requiring restock.")
        } else {
            summaryBuilder.append("Tap to open inventory management and place restock orders.")
        }

        val contentText = if (count == 1) {
            "${lowStockItems.first().name} has only ${lowStockItems.first().stockQuantity} ${lowStockItems.first().unit} remaining."
        } else {
            "$count items need urgent restocking. Tap to view details."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(summaryBuilder.toString()))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_menu_view,
                "Review Stock",
                pendingIntent
            )
            .build()

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Notification permission might not be granted yet on Android 13+
        }
    }

    /**
     * Sends an immediate test alert to verify notification sound, vibration, and banner for the manager.
     */
    fun sendTestAlert(
        context: Context,
        managerName: String = "Store Manager"
    ) {
        createNotificationChannel(context)

        val title = "🔔 Manager Alert System Active"
        val message = "Hello $managerName, background low-stock push notifications are configured and active. You will be alerted when inventory falls below threshold."

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_TAB", "STOCK")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText("Background inventory monitoring is active and healthy.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(NOTIFICATION_ID + 1, notification)
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }
}
