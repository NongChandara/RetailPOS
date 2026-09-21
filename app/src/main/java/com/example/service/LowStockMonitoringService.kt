package com.example.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class LowStockMonitoringService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Run an immediate check when service is explicitly started
        triggerImmediateCheck(this)
        return START_NOT_STICKY
    }

    companion object {
        const val PERIODIC_WORK_TAG = "retail_pos_periodic_low_stock_check"
        const val IMMEDIATE_WORK_TAG = "retail_pos_immediate_low_stock_check"

        /**
         * Schedules periodic background monitoring using WorkManager.
         * Default interval is 15 minutes (the WorkManager minimum interval).
         */
        fun schedulePeriodicMonitoring(
            context: Context,
            intervalMinutes: Long = 15,
            thresholdOverride: Int? = null
        ) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false)
                .build()

            val inputDataBuilder = Data.Builder()
            thresholdOverride?.let { inputDataBuilder.putInt("THRESHOLD_OVERRIDE", it) }

            val periodicRequest = PeriodicWorkRequestBuilder<LowStockAlertWorker>(
                intervalMinutes.coerceAtLeast(15),
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setInputData(inputDataBuilder.build())
                .addTag(PERIODIC_WORK_TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_TAG,
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicRequest
            )
        }

        /**
         * Cancels periodic background monitoring.
         */
        fun cancelPeriodicMonitoring(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_TAG)
        }

        /**
         * Triggers an immediate one-off background stock scan.
         */
        fun triggerImmediateCheck(
            context: Context,
            thresholdOverride: Int? = null
        ) {
            val inputDataBuilder = Data.Builder()
            thresholdOverride?.let { inputDataBuilder.putInt("THRESHOLD_OVERRIDE", it) }

            val oneTimeRequest = OneTimeWorkRequestBuilder<LowStockAlertWorker>()
                .setInputData(inputDataBuilder.build())
                .addTag(IMMEDIATE_WORK_TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                IMMEDIATE_WORK_TAG,
                ExistingWorkPolicy.REPLACE,
                oneTimeRequest
            )
        }

        /**
         * Checks if periodic work is currently enqueued or running.
         */
        fun isPeriodicMonitoringActive(context: Context): Boolean {
            return try {
                val workInfos = WorkManager.getInstance(context)
                    .getWorkInfosForUniqueWork(PERIODIC_WORK_TAG)
                    .get()
                workInfos.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }
            } catch (_: Exception) {
                false
            }
        }
    }
}
