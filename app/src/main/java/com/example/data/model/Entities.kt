package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val role: String, // "ADMIN", "CASHIER", "CLERK"
    val pin: String = "1234",
    val email: String = "",
    val branch: String = "Main Branch",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val colorHex: String = "#0D9488",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "branches")
data class BranchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val code: String = "",
    val address: String = "",
    val phone: String = "",
    val isMain: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val receiptHeader: String = "TR COFFEE • កាហ្វេ ទីរ៉ូ",
    val receiptSubtitle: String = "Official Sales Receipt & Tax Invoice",
    val receiptVatTin: String = "VAT TIN: K001-90213847",
    val receiptFooter: String = "Thank you for shopping with us! • Goods returnable within 7 days",
    val taxPercent: Double = 8.0,
    val receiptGap: Int = 12 // Spacing gap between receipt sections in dp/lines
)

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val receiptNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val cashierId: Long,
    val cashierName: String,
    val subtotal: Double,
    val taxAmount: Double,
    val discountPercent: Double = 0.0,
    val discountAmount: Double = 0.0,
    val totalAmount: Double,
    val paymentMethod: String, // "CASH", "CARD", "DIGITAL"
    val amountTendered: Double,
    val changeGiven: Double,
    val itemsCount: Int,
    val notes: String = "",
    val branchName: String = "Main Branch",
    val taxPercent: Double = 8.0,
    val receiptGap: Int = 12
)

@Entity(tableName = "sale_items")
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val sku: String,
    val unitPrice: Double,
    val costPrice: Double,
    val quantity: Int,
    val itemTotal: Double,
    val imageUrl: String = ""
)

@Entity(tableName = "stock_movements")
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val type: String, // "SALE", "RESTOCK", "ADJUSTMENT_DAMAGE", "ADJUSTMENT_RETURN", "AUDIT"
    val quantityDelta: Int, // e.g. -2 for sale, +10 for restock, -1 for damaged
    val resultingStock: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val userName: String,
    val reason: String = ""
)
