package com.example.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sku: String,
    val category: String,
    val costPrice: Double,
    val sellingPrice: Double,
    val stockQuantity: Int,
    val minStockThreshold: Int = 5,
    val unit: String = "pcs",
    val barcode: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Standard retail price accessor representing the product's selling price.
     */
    val price: Double
        get() = sellingPrice

    @Ignore
    constructor(
        name: String,
        price: Double,
        stockQuantity: Int,
        category: String,
        id: Long = 0L,
        sku: String = "SKU-${System.currentTimeMillis() % 100000}",
        costPrice: Double = price * 0.6,
        minStockThreshold: Int = 5,
        unit: String = "pcs",
        barcode: String = "",
        updatedAt: Long = System.currentTimeMillis()
    ) : this(
        id = id,
        name = name,
        sku = sku,
        category = category,
        costPrice = costPrice,
        sellingPrice = price,
        stockQuantity = stockQuantity,
        minStockThreshold = minStockThreshold,
        unit = unit,
        barcode = barcode,
        updatedAt = updatedAt
    )
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val role: String, // "ADMIN", "CASHIER", "CLERK"
    val pin: String = "1234",
    val email: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
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
    val notes: String = ""
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
    val itemTotal: Double
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
