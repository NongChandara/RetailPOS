package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.security.MessageDigest

enum class ActivityCategory(val displayName: String, val code: String) {
    POS_TRANSACTION("POS Transactions", "POS_TRANSACTION"),
    INVENTORY_MODIFICATION("Inventory Modifications", "INVENTORY_MODIFICATION"),
    STAFF_SECURITY("Staff & Security", "STAFF_SECURITY");

    companion object {
        fun fromCode(code: String): ActivityCategory {
            return values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: POS_TRANSACTION
        }
    }
}

@Entity(
    tableName = "activity_logs",
    indices = [
        Index("staffId"),
        Index("timestamp"),
        Index("category")
    ]
)
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val staffId: Long,
    val staffName: String,
    val staffRole: String, // "ADMIN", "CASHIER", "CLERK"
    val category: String, // "POS_TRANSACTION", "INVENTORY_MODIFICATION", "STAFF_SECURITY"
    val action: String, // e.g. "SALE_COMPLETED", "STOCK_ADJUSTMENT", "PRODUCT_CREATED", "PRODUCT_UPDATED", "PRODUCT_DELETED", "STAFF_SESSION_SWITCHED"
    val entityType: String, // "SALE", "PRODUCT", "STOCK", "STAFF"
    val entityId: String, // e.g. Receipt #, SKU, User ID
    val details: String, // Human-readable narrative description
    val metadataJson: String = "{}", // Extended attributes in JSON format
    val integrityHash: String = "", // SHA-256 tamper-evident hash
    val isVerified: Boolean = true
) {
    companion object {
        private const val AUDIT_SALT = "RETAIL_SECURE_AUDIT_SALT_2026_v1"

        /**
         * Computes a canonical SHA-256 cryptographic digest across the log fields to guarantee
         * tamper-evidence and audit trail integrity.
         */
        fun computeHash(
            timestamp: Long,
            staffId: Long,
            category: String,
            action: String,
            entityId: String,
            details: String
        ): String {
            val payload = "$timestamp|$staffId|$category|$action|$entityId|$details|$AUDIT_SALT"
            val digest = MessageDigest.getInstance("SHA-256").digest(payload.toByteArray(Charsets.UTF_8))
            return digest.joinToString("") { "%02x".format(it) }
        }

        /**
         * Factory function to create a validated, tamper-evident activity log entry.
         */
        fun createSecureLog(
            staffId: Long,
            staffName: String,
            staffRole: String,
            category: ActivityCategory,
            action: String,
            entityType: String,
            entityId: String,
            details: String,
            metadataJson: String = "{}",
            timestamp: Long = System.currentTimeMillis()
        ): ActivityLogEntity {
            val hash = computeHash(
                timestamp = timestamp,
                staffId = staffId,
                category = category.code,
                action = action,
                entityId = entityId,
                details = details
            )
            return ActivityLogEntity(
                timestamp = timestamp,
                staffId = staffId,
                staffName = staffName,
                staffRole = staffRole,
                category = category.code,
                action = action,
                entityType = entityType,
                entityId = entityId,
                details = details,
                metadataJson = metadataJson,
                integrityHash = hash,
                isVerified = true
            )
        }
    }

    /**
     * Re-calculates the cryptographic hash and verifies whether the log record is untampered.
     */
    fun verifyIntegrity(): Boolean {
        if (integrityHash.isBlank()) return false
        val expected = computeHash(
            timestamp = timestamp,
            staffId = staffId,
            category = category,
            action = action,
            entityId = entityId,
            details = details
        )
        return integrityHash.equals(expected, ignoreCase = true)
    }
}
