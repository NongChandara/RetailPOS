package com.example

import com.example.data.model.ActivityCategory
import com.example.data.model.ActivityLogEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SecureActivityLoggingTest {

    @Test
    fun testPosTransactionSecureLogCreationAndVerification() {
        val log = ActivityLogEntity.createSecureLog(
            staffId = 2L,
            staffName = "Alex Chen",
            staffRole = "CASHIER",
            category = ActivityCategory.POS_TRANSACTION,
            action = "SALE_COMPLETED",
            entityType = "SALE",
            entityId = "RCP-2026-001",
            details = "Completed POS Sale #RCP-2026-001 for $45.20 via CARD",
            metadataJson = """{"subtotal":40.0,"tax":5.20,"items":3}""",
            timestamp = 1758445200000L
        )

        assertEquals(2L, log.staffId)
        assertEquals("Alex Chen", log.staffName)
        assertEquals(ActivityCategory.POS_TRANSACTION.code, log.category)
        assertTrue(log.integrityHash.isNotBlank())
        assertEquals(64, log.integrityHash.length) // SHA-256 hex string is 64 chars
        assertTrue(log.verifyIntegrity())
    }

    @Test
    fun testInventoryModificationSecureLogCreationAndVerification() {
        val log = ActivityLogEntity.createSecureLog(
            staffId = 1L,
            staffName = "Sarah Miller",
            staffRole = "ADMIN",
            category = ActivityCategory.INVENTORY_MODIFICATION,
            action = "STOCK_RESTOCK",
            entityType = "STOCK",
            entityId = "BEV-001",
            details = "Restocked 24 cans of Cold Brew Coffee",
            metadataJson = """{"delta":24,"newStock":50}""",
            timestamp = 1758445300000L
        )

        assertEquals(1L, log.staffId)
        assertEquals(ActivityCategory.INVENTORY_MODIFICATION.code, log.category)
        assertTrue(log.verifyIntegrity())
    }

    @Test
    fun testTamperDetectionOnStaffId() {
        val legitimateLog = ActivityLogEntity.createSecureLog(
            staffId = 1L,
            staffName = "Sarah Miller",
            staffRole = "ADMIN",
            category = ActivityCategory.INVENTORY_MODIFICATION,
            action = "STOCK_RESTOCK",
            entityType = "STOCK",
            entityId = "BEV-001",
            details = "Restocked 24 cans of Cold Brew Coffee",
            metadataJson = """{"delta":24}""",
            timestamp = 1758445300000L
        )

        assertTrue(legitimateLog.verifyIntegrity())

        // Attacker attempts to change staff attribution to someone else without recalculating hash
        val tamperedLog = legitimateLog.copy(staffId = 3L, staffName = "David Ross")
        assertFalse(tamperedLog.verifyIntegrity())
    }

    @Test
    fun testTamperDetectionOnDetailsAndCategory() {
        val originalLog = ActivityLogEntity.createSecureLog(
            staffId = 2L,
            staffName = "Alex Chen",
            staffRole = "CASHIER",
            category = ActivityCategory.POS_TRANSACTION,
            action = "SALE_COMPLETED",
            entityType = "SALE",
            entityId = "RCP-9999",
            details = "Total amount paid: $100.00",
            metadataJson = """{"total":100.0}""",
            timestamp = 1758445400000L
        )

        assertTrue(originalLog.verifyIntegrity())

        // Tamper with payload
        val alteredAmountLog = originalLog.copy(details = "Total amount paid: $10.00")
        assertFalse(alteredAmountLog.verifyIntegrity())

        // Tamper with action
        val alteredActionLog = originalLog.copy(action = "REFUND_ISSUED")
        assertFalse(alteredActionLog.verifyIntegrity())
    }

    @Test
    fun testDeterministicHashForIdenticalPayload() {
        val hash1 = ActivityLogEntity.computeHash(
            timestamp = 1000000L,
            staffId = 1L,
            category = "POS_TRANSACTION",
            action = "SALE_COMPLETED",
            entityId = "RCP-1",
            details = "Sale details"
        )
        val hash2 = ActivityLogEntity.computeHash(
            timestamp = 1000000L,
            staffId = 1L,
            category = "POS_TRANSACTION",
            action = "SALE_COMPLETED",
            entityId = "RCP-1",
            details = "Sale details"
        )
        assertEquals(hash1, hash2)

        val hash3 = ActivityLogEntity.computeHash(
            timestamp = 1000000L,
            staffId = 2L, // Different staff ID
            category = "POS_TRANSACTION",
            action = "SALE_COMPLETED",
            entityId = "RCP-1",
            details = "Sale details"
        )
        assertNotEquals(hash1, hash3)
    }
}
