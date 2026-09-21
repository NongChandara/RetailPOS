package com.example.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.data.AppDatabase
import com.example.data.model.ActivityCategory
import com.example.data.model.ActivityLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class LowStockAlertWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val database = AppDatabase.getDatabase(applicationContext)
            val productDao = database.productDao()
            val userDao = database.userDao()
            val activityLogDao = database.activityLogDao()

            // Optional custom threshold passed in worker inputData (key: "THRESHOLD_OVERRIDE")
            val rawThreshold = inputData.getInt("THRESHOLD_OVERRIDE", -1)
            val thresholdOverride = if (rawThreshold >= 0) rawThreshold else null

            // Retrieve all products
            val allProducts = productDao.getAllProducts().first()
            val lowStockProducts = allProducts.filter { product ->
                val threshold = thresholdOverride ?: product.minStockThreshold
                product.stockQuantity <= threshold
            }

            // Find store manager / admin to attribute the notification
            val users = userDao.getAllUsers().first()
            val manager = users.firstOrNull { it.role == "MANAGER" || it.role == "ADMIN" }
                ?: users.firstOrNull()

            val managerName = manager?.name ?: "Store Manager"
            val managerId = manager?.id ?: 1L
            val managerRole = manager?.role ?: "ADMIN"

            if (lowStockProducts.isNotEmpty()) {
                // Post push notification to manager
                LowStockNotificationHelper.showLowStockAlert(
                    context = applicationContext,
                    lowStockItems = lowStockProducts,
                    managerName = managerName,
                    thresholdOverride = thresholdOverride
                )

                // Create cryptographic audit record of the background alert
                val affectedSkus = lowStockProducts.joinToString(", ") { "${it.sku} (${it.stockQuantity} ${it.unit})" }
                val auditLog = ActivityLogEntity.createSecureLog(
                    staffId = managerId,
                    staffName = managerName,
                    staffRole = managerRole,
                    category = ActivityCategory.INVENTORY_MODIFICATION,
                    action = "LOW_STOCK_BACKGROUND_ALERT",
                    entityType = "INVENTORY_ALERT",
                    entityId = "ALERT-${System.currentTimeMillis()}",
                    details = "Automated background scan identified ${lowStockProducts.size} item(s) below threshold. Manager push notification dispatched. SKUs: $affectedSkus",
                    metadataJson = """{"itemCount":${lowStockProducts.size},"thresholdOverride":$thresholdOverride,"skus":"$affectedSkus"}"""
                )
                activityLogDao.insertLog(auditLog)
            }

            Result.success(
                workDataOf(
                    "lowStockCount" to lowStockProducts.size,
                    "timestamp" to System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            Result.failure(workDataOf("error" to (e.message ?: "Unknown worker error")))
        }
    }
}
