package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ActivityLogDao
import com.example.data.dao.ProductDao
import com.example.data.dao.SaleDao
import com.example.data.dao.StockMovementDao
import com.example.data.dao.UserDao
import com.example.data.model.ActivityCategory
import com.example.data.model.ActivityLogEntity
import com.example.data.model.ProductEntity
import com.example.data.model.SaleEntity
import com.example.data.model.SaleItemEntity
import com.example.data.model.StockMovementEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        UserEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        StockMovementEntity::class,
        ActivityLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun userDao(): UserDao
    abstract fun saleDao(): SaleDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun activityLogDao(): ActivityLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "retail_pos_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val productDao = database.productDao()
            val userDao = database.userDao()
            val stockMovementDao = database.stockMovementDao()

            if (userDao.countUsers() == 0) {
                val initialUsers = listOf(
                    UserEntity(
                        name = "Sarah Miller",
                        role = "ADMIN",
                        pin = "1111",
                        email = "sarah.admin@store.local"
                    ),
                    UserEntity(
                        name = "Alex Chen",
                        role = "CASHIER",
                        pin = "2222",
                        email = "alex.cashier@store.local"
                    ),
                    UserEntity(
                        name = "David Ross",
                        role = "CLERK",
                        pin = "3333",
                        email = "david.stock@store.local"
                    )
                )
                userDao.insertUsers(initialUsers)
            }

            if (productDao.countProducts() == 0) {
                val initialProducts = listOf(
                    ProductEntity(
                        name = "Organic Cold Brew Coffee 330ml",
                        sku = "BEV-001",
                        category = "Beverages",
                        costPrice = 1.60,
                        sellingPrice = 3.95,
                        stockQuantity = 28,
                        minStockThreshold = 8,
                        unit = "can",
                        barcode = "011122233344"
                    ),
                    ProductEntity(
                        name = "Sparkling Spring Water 500ml",
                        sku = "BEV-002",
                        category = "Beverages",
                        costPrice = 0.50,
                        sellingPrice = 1.75,
                        stockQuantity = 45,
                        minStockThreshold = 12,
                        unit = "bottle",
                        barcode = "011122233345"
                    ),
                    ProductEntity(
                        name = "Artisan Sourdough Loaf",
                        sku = "BAK-001",
                        category = "Bakery",
                        costPrice = 2.20,
                        sellingPrice = 5.50,
                        stockQuantity = 4, // low stock!
                        minStockThreshold = 6,
                        unit = "loaf",
                        barcode = "011122233346"
                    ),
                    ProductEntity(
                        name = "Dark Chocolate Almond Bar 85g",
                        sku = "SNK-001",
                        category = "Snacks",
                        costPrice = 1.10,
                        sellingPrice = 2.80,
                        stockQuantity = 32,
                        minStockThreshold = 10,
                        unit = "bar",
                        barcode = "011122233347"
                    ),
                    ProductEntity(
                        name = "Sea Salt Kettle Chips 150g",
                        sku = "SNK-002",
                        category = "Snacks",
                        costPrice = 0.90,
                        sellingPrice = 2.49,
                        stockQuantity = 19,
                        minStockThreshold = 8,
                        unit = "pack",
                        barcode = "011122233348"
                    ),
                    ProductEntity(
                        name = "USB-C Fast Charging Cable 2m",
                        sku = "ACC-001",
                        category = "Electronics",
                        costPrice = 3.20,
                        sellingPrice = 9.99,
                        stockQuantity = 14,
                        minStockThreshold = 5,
                        unit = "pcs",
                        barcode = "011122233349"
                    ),
                    ProductEntity(
                        name = "Wireless Optical Mouse 2.4G",
                        sku = "ACC-002",
                        category = "Electronics",
                        costPrice = 5.80,
                        sellingPrice = 15.50,
                        stockQuantity = 2, // low stock!
                        minStockThreshold = 4,
                        unit = "pcs",
                        barcode = "011122233350"
                    ),
                    ProductEntity(
                        name = "Eco Bamboo Travel Cutlery Set",
                        sku = "HOM-001",
                        category = "Home & Goods",
                        costPrice = 4.00,
                        sellingPrice = 11.00,
                        stockQuantity = 9,
                        minStockThreshold = 5,
                        unit = "set",
                        barcode = "011122233351"
                    ),
                    ProductEntity(
                        name = "Natural Beeswax Lip Balm",
                        sku = "CAR-001",
                        category = "Personal Care",
                        costPrice = 1.00,
                        sellingPrice = 3.50,
                        stockQuantity = 0, // out of stock!
                        minStockThreshold = 5,
                        unit = "pcs",
                        barcode = "011122233352"
                    ),
                    ProductEntity(
                        name = "Recycled Cotton Canvas Tote",
                        sku = "APP-001",
                        category = "Apparel",
                        costPrice = 3.50,
                        sellingPrice = 8.50,
                        stockQuantity = 22,
                        minStockThreshold = 6,
                        unit = "pcs",
                        barcode = "011122233353"
                    )
                )
                productDao.insertProducts(initialProducts)

                // Log initial stock audit movements
                val movements = initialProducts.map { prod ->
                    StockMovementEntity(
                        productId = prod.id,
                        productName = prod.name,
                        type = "RESTOCK",
                        quantityDelta = prod.stockQuantity,
                        resultingStock = prod.stockQuantity,
                        timestamp = System.currentTimeMillis() - 86400000L,
                        userName = "Sarah Miller (Admin)",
                        reason = "Initial inventory intake"
                    )
                }
                stockMovementDao.insertMovements(movements)
            }
            val saleDao = database.saleDao()
            if (saleDao.countSales() == 0) {
                val now = System.currentTimeMillis()
                val oneDay = 24L * 60 * 60 * 1000

                // Historical timestamps distributed over past 60 days
                val saleTemplates = listOf(
                    // Today
                    Triple(now - (2L * 60 * 60 * 1000), "CARD", listOf(Pair("BEV-001", 3), Pair("SNK-001", 2))),
                    Triple(now - (5L * 60 * 60 * 1000), "CASH", listOf(Pair("BAK-001", 2), Pair("BEV-002", 2))),
                    Triple(now - (7L * 60 * 60 * 1000), "DIGITAL", listOf(Pair("ACC-001", 1), Pair("BEV-001", 1))),
                    // 1 day ago
                    Triple(now - (1 * oneDay + 3L * 3600000), "CARD", listOf(Pair("ACC-002", 1), Pair("ACC-001", 2))),
                    Triple(now - (1 * oneDay + 6L * 3600000), "CASH", listOf(Pair("BEV-001", 4), Pair("SNK-002", 3))),
                    // 2 days ago
                    Triple(now - (2 * oneDay + 4L * 3600000), "CARD", listOf(Pair("HOM-001", 2), Pair("APP-001", 1))),
                    Triple(now - (2 * oneDay + 8L * 3600000), "DIGITAL", listOf(Pair("BAK-001", 3), Pair("SNK-001", 4))),
                    // 3 days ago
                    Triple(now - (3 * oneDay + 2L * 3600000), "CARD", listOf(Pair("ACC-001", 3), Pair("BEV-002", 5))),
                    Triple(now - (3 * oneDay + 7L * 3600000), "CASH", listOf(Pair("APP-001", 2), Pair("SNK-002", 2))),
                    // 4 days ago
                    Triple(now - (4 * oneDay + 5L * 3600000), "DIGITAL", listOf(Pair("HOM-001", 1), Pair("ACC-002", 1))),
                    // 5 days ago
                    Triple(now - (5 * oneDay + 4L * 3600000), "CARD", listOf(Pair("BAK-001", 4), Pair("BEV-001", 5))),
                    Triple(now - (5 * oneDay + 9L * 3600000), "CASH", listOf(Pair("SNK-001", 6), Pair("BEV-002", 4))),
                    // 6 days ago
                    Triple(now - (6 * oneDay + 3L * 3600000), "CARD", listOf(Pair("ACC-001", 2), Pair("HOM-001", 2))),
                    // Week 2 ago
                    Triple(now - (10 * oneDay), "CARD", listOf(Pair("APP-001", 3), Pair("ACC-001", 2))),
                    Triple(now - (12 * oneDay), "CASH", listOf(Pair("BAK-001", 2), Pair("BEV-001", 3))),
                    // Week 3 ago
                    Triple(now - (17 * oneDay), "CARD", listOf(Pair("HOM-001", 2), Pair("ACC-002", 1))),
                    Triple(now - (19 * oneDay), "DIGITAL", listOf(Pair("BEV-001", 6), Pair("SNK-001", 4))),
                    // Week 4 ago
                    Triple(now - (24 * oneDay), "CARD", listOf(Pair("ACC-001", 4), Pair("APP-001", 1))),
                    Triple(now - (26 * oneDay), "CASH", listOf(Pair("BAK-001", 3), Pair("BEV-002", 6))),
                    // Week 5 ago
                    Triple(now - (32 * oneDay), "CARD", listOf(Pair("HOM-001", 3), Pair("ACC-001", 2))),
                    // Week 6 ago
                    Triple(now - (39 * oneDay), "DIGITAL", listOf(Pair("ACC-002", 2), Pair("BEV-001", 5))),
                    // Week 7 ago
                    Triple(now - (47 * oneDay), "CARD", listOf(Pair("APP-001", 4), Pair("HOM-001", 2))),
                    // Week 8 ago
                    Triple(now - (54 * oneDay), "CASH", listOf(Pair("BAK-001", 5), Pair("SNK-001", 5))),
                    Triple(now - (58 * oneDay), "CARD", listOf(Pair("ACC-001", 3), Pair("BEV-001", 4)))
                )

                // Cache products by SKU
                val productMap = mutableMapOf(
                    "BEV-001" to Triple(1L, "Organic Cold Brew Coffee 330ml", 3.95),
                    "BEV-002" to Triple(2L, "Sparkling Spring Water 500ml", 1.75),
                    "BAK-001" to Triple(3L, "Artisan Sourdough Loaf", 5.50),
                    "SNK-001" to Triple(4L, "Dark Chocolate Almond Bar 85g", 2.80),
                    "SNK-002" to Triple(5L, "Sea Salt Kettle Chips 150g", 2.49),
                    "ACC-001" to Triple(6L, "USB-C Fast Charging Cable 2m", 9.99),
                    "ACC-002" to Triple(7L, "Wireless Optical Mouse 2.4G", 15.50),
                    "HOM-001" to Triple(8L, "Eco Bamboo Travel Cutlery Set", 11.00),
                    "APP-001" to Triple(10L, "Recycled Cotton Canvas Tote", 8.50)
                )

                val cashiers = listOf("Sarah Miller", "Alex Chen", "David Ross")

                saleTemplates.forEachIndexed { idx, (time, method, itemsList) ->
                    var subtotal = 0.0
                    val saleItemsToInsert = mutableListOf<SaleItemEntity>()

                    itemsList.forEach { (sku, qty) ->
                        val prodInfo = productMap[sku]
                        if (prodInfo != null) {
                            val lineTotal = prodInfo.third * qty
                            subtotal += lineTotal
                            saleItemsToInsert.add(
                                SaleItemEntity(
                                    saleId = 0L, // will be updated
                                    productId = prodInfo.first,
                                    productName = prodInfo.second,
                                    sku = sku,
                                    unitPrice = prodInfo.third,
                                    costPrice = prodInfo.third * 0.45,
                                    quantity = qty,
                                    itemTotal = lineTotal
                                )
                            )
                        }
                    }

                    val tax = subtotal * 0.08
                    val total = subtotal + tax
                    val tendered = if (method == "CASH") Math.ceil(total / 5.0) * 5.0 else total
                    val change = if (method == "CASH") (tendered - total).coerceAtLeast(0.0) else 0.0

                    val receiptNum = "RCP-${1000 + idx}"
                    val sale = SaleEntity(
                        receiptNumber = receiptNum,
                        timestamp = time,
                        cashierId = (idx % 3 + 1).toLong(),
                        cashierName = cashiers[idx % cashiers.size],
                        subtotal = subtotal,
                        taxAmount = tax,
                        discountPercent = 0.0,
                        discountAmount = 0.0,
                        totalAmount = total,
                        paymentMethod = method,
                        amountTendered = tendered,
                        changeGiven = change,
                        itemsCount = itemsList.sumOf { it.second }
                    )

                    val saleId = saleDao.insertSale(sale)
                    val updatedItems = saleItemsToInsert.map { it.copy(saleId = saleId) }
                    saleDao.insertSaleItems(updatedItems)
                }
            }

            val activityLogDao = database.activityLogDao()
            if (activityLogDao.countLogs() == 0) {
                val now = System.currentTimeMillis()
                val oneHour = 3_600_000L
                val oneDay = 86_400_000L

                val seedLogs = listOf(
                    ActivityLogEntity.createSecureLog(
                        staffId = 1L,
                        staffName = "Sarah Miller",
                        staffRole = "ADMIN",
                        category = ActivityCategory.INVENTORY_MODIFICATION,
                        action = "PRODUCT_CREATED",
                        entityType = "PRODUCT",
                        entityId = "BEV-001",
                        details = "Created catalog entry for 'Organic Cold Brew Coffee 330ml' (SKU: BEV-001) with initial stock of 28 units",
                        metadataJson = """{"sku":"BEV-001","cost":1.60,"price":3.95,"category":"Beverages"}""",
                        timestamp = now - (2 * oneDay + 4 * oneHour)
                    ),
                    ActivityLogEntity.createSecureLog(
                        staffId = 3L,
                        staffName = "David Ross",
                        staffRole = "CLERK",
                        category = ActivityCategory.INVENTORY_MODIFICATION,
                        action = "STOCK_RESTOCK",
                        entityType = "STOCK",
                        entityId = "SNK-001",
                        details = "Restocked 30 units of 'Dark Chocolate Almond Bar 85g' from supplier shipment PO-2026-88",
                        metadataJson = """{"delta":30,"newStock":40,"type":"RESTOCK","po":"PO-2026-88"}""",
                        timestamp = now - (oneDay + 6 * oneHour)
                    ),
                    ActivityLogEntity.createSecureLog(
                        staffId = 2L,
                        staffName = "Alex Chen",
                        staffRole = "CASHIER",
                        category = ActivityCategory.POS_TRANSACTION,
                        action = "SALE_COMPLETED",
                        entityType = "SALE",
                        entityId = "RCP-1000",
                        details = "Processed POS Sale #RCP-1000: $34.50 via CARD (3 items). Cashier ID: #2 (Alex Chen)",
                        metadataJson = """{"receipt":"RCP-1000","amount":34.50,"method":"CARD","items":3}""",
                        timestamp = now - (oneDay + 2 * oneHour)
                    ),
                    ActivityLogEntity.createSecureLog(
                        staffId = 3L,
                        staffName = "David Ross",
                        staffRole = "CLERK",
                        category = ActivityCategory.INVENTORY_MODIFICATION,
                        action = "STOCK_DAMAGE_WRITEOFF",
                        entityType = "STOCK",
                        entityId = "BEV-001",
                        details = "Stock write-off of 2 damaged cans for 'Organic Cold Brew Coffee 330ml' (dropped during restocking)",
                        metadataJson = """{"delta":-2,"reason":"damaged_in_aisle","sku":"BEV-001"}""",
                        timestamp = now - (14 * oneHour)
                    ),
                    ActivityLogEntity.createSecureLog(
                        staffId = 2L,
                        staffName = "Alex Chen",
                        staffRole = "CASHIER",
                        category = ActivityCategory.POS_TRANSACTION,
                        action = "SALE_COMPLETED",
                        entityType = "SALE",
                        entityId = "RCP-1001",
                        details = "Processed POS Sale #RCP-1001: $18.90 via CASH with $1.10 change given (2 items)",
                        metadataJson = """{"receipt":"RCP-1001","amount":18.90,"method":"CASH","items":2}""",
                        timestamp = now - (3 * oneHour)
                    ),
                    ActivityLogEntity.createSecureLog(
                        staffId = 1L,
                        staffName = "Sarah Miller",
                        staffRole = "ADMIN",
                        category = ActivityCategory.INVENTORY_MODIFICATION,
                        action = "PRODUCT_PRICE_UPDATE",
                        entityType = "PRODUCT",
                        entityId = "ACC-001",
                        details = "Adjusted retail price for 'USB-C Fast Charging Cable 2m' from $8.99 to $9.99 per updated MSRP",
                        metadataJson = """{"sku":"ACC-001","oldPrice":8.99,"newPrice":9.99}""",
                        timestamp = now - (1 * oneHour)
                    )
                )
                activityLogDao.insertLogs(seedLogs)
            }
        }
    }
}
