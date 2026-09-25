package com.example.data.model

/**
 * Domain model representing an inventory product item.
 *
 * @param id Unique identifier of the product item
 * @param name Name of the product
 * @param sku Stock Keeping Unit identifier
 * @param price Retail selling price per unit
 * @param stockQuantity Available quantity in stock
 * @param category Optional category name
 * @param costPrice Optional wholesale acquisition cost
 * @param barcode Optional barcode string
 * @param minStockThreshold Threshold below which product is flagged as low stock
 */
data class Product(
    val id: Long = 0,
    val name: String,
    val sku: String,
    val price: Double,
    val stockQuantity: Int,
    val category: String = "General",
    val costPrice: Double = 0.0,
    val barcode: String = "",
    val branch: String = "Main Branch",
    val minStockThreshold: Int = 5,
    val imageUrl: String = ""
) {
    val isLowStock: Boolean
        get() = stockQuantity in 1..minStockThreshold

    val isOutOfStock: Boolean
        get() = stockQuantity <= 0

    companion object {
        fun fromEntity(entity: ProductEntity): Product = Product(
            id = entity.id,
            name = entity.name,
            sku = entity.sku,
            price = entity.sellingPrice,
            stockQuantity = entity.stockQuantity,
            category = entity.category,
            costPrice = entity.costPrice,
            barcode = entity.barcode,
            branch = entity.branch,
            minStockThreshold = entity.minStockThreshold,
            imageUrl = entity.imageUrl
        )
    }

    fun toEntity(unit: String = "pcs"): ProductEntity = ProductEntity(
        id = id,
        name = name,
        sku = sku,
        category = category,
        costPrice = if (costPrice > 0) costPrice else price * 0.6,
        sellingPrice = price,
        stockQuantity = stockQuantity,
        minStockThreshold = minStockThreshold,
        unit = unit,
        barcode = barcode,
        branch = branch,
        imageUrl = imageUrl,
        updatedAt = System.currentTimeMillis()
    )
}
