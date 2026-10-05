package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ActivityLogDao
import com.example.data.dao.BranchDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.ClientDao
import com.example.data.dao.ProductDao
import com.example.data.dao.SaleDao
import com.example.data.dao.StockMovementDao
import com.example.data.dao.UserDao
import com.example.data.model.ActivityCategory
import com.example.data.model.ActivityLogEntity
import com.example.data.model.BranchEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.ClientEntity
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
        ActivityLogEntity::class,
        CategoryEntity::class,
        BranchEntity::class,
        ClientEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun userDao(): UserDao
    abstract fun saleDao(): SaleDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun categoryDao(): CategoryDao
    abstract fun branchDao(): BranchDao
    abstract fun clientDao(): ClientDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN telegram TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE users SET telegram = '@chandaranong' WHERE role = 'ADMIN' OR email LIKE '%chandara%'")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "retail_pos_db"
                )
                    .addMigrations(MIGRATION_8_9)
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

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                scope.launch(Dispatchers.IO) {
                    try {
                        db.execSQL("UPDATE users SET telegram = '@chandaranong' WHERE (role = 'ADMIN' OR email LIKE '%chandara%') AND (telegram IS NULL OR telegram = '')")
                        INSTANCE?.let { database ->
                            syncStandardStoreData(database)
                        }
                    } catch (_: Exception) {}
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val productDao = database.productDao()
            val userDao = database.userDao()
            val stockMovementDao = database.stockMovementDao()
            val categoryDao = database.categoryDao()
            val branchDao = database.branchDao()

            if (categoryDao.countCategories() == 0) {
                val initialCategories = listOf(
                    CategoryEntity(name = "Coffee & Beverages", description = "Signature coffees, espresso, iced drinks, teas, and frappes", colorHex = "#0EA5E9"),
                    CategoryEntity(name = "Bakery", description = "Freshly baked artisan croissants and pastries", colorHex = "#F59E0B"),
                    CategoryEntity(name = "Cosmetics", description = "Serums, sunscreens, hydrating creams, lip tints and cleansers", colorHex = "#EC4899"),
                    CategoryEntity(name = "Salon & Services", description = "Hydra facial spa, deluxe nail care, hair spa and mobile catering", colorHex = "#8B5CF6"),
                    CategoryEntity(name = "Gift Sets & Merch", description = "Deluxe holiday gift boxes and stainless thermal tumblers", colorHex = "#10B981")
                )
                categoryDao.insertCategories(initialCategories)
            }

            if (branchDao.countBranches() == 0) {
                val initialBranches = listOf(
                    BranchEntity(
                        name = "Main Branch",
                        code = "MB-01",
                        address = "123 Norodom Blvd, Daun Penh, Phnom Penh",
                        phone = "+855 23 888 999",
                        isMain = true,
                        receiptHeader = "TR COFFEE • Main Branch (កាហ្វេ ទីរ៉ូ)",
                        receiptSubtitle = "Official Sales Receipt & Tax Invoice",
                        receiptVatTin = "VAT TIN: K001-90213847",
                        receiptFooter = "Thank you for visiting TR Coffee! • Free Wi-Fi: TR_Coffee_Main (PW: trcoffee2026)",
                        taxPercent = 8.0,
                        receiptGap = 12,
                        wifiName = "TR_Coffee_Main",
                        wifiPassword = "trcoffee2026"
                    ),
                    BranchEntity(
                        name = "Downtown Branch",
                        code = "DT-02",
                        address = "842 Monivong Blvd, Boeung Keng Kang, Phnom Penh",
                        phone = "+855 23 888 888",
                        isMain = false,
                        receiptHeader = "TR COFFEE • Downtown Branch",
                        receiptSubtitle = "Official Sales Receipt & Tax Invoice",
                        receiptVatTin = "VAT TIN: K001-90213848",
                        receiptFooter = "Thank you for shopping Downtown! • Free Wi-Fi: TR_Downtown (PW: trcoffee2026)",
                        taxPercent = 10.0,
                        receiptGap = 12,
                        wifiName = "TR_Downtown",
                        wifiPassword = "trcoffee2026"
                    ),
                    BranchEntity(
                        name = "Warehouse Depot",
                        code = "WH-03",
                        address = "12 Veng Sreng Blvd, Pur Senchey, Phnom Penh",
                        phone = "+855 23 888 777",
                        isMain = false,
                        receiptHeader = "TR STORE • Wholesale & Distribution",
                        receiptSubtitle = "Warehouse Dispatch & Commercial Invoice",
                        receiptVatTin = "VAT TIN: K001-90213849",
                        receiptFooter = "Commercial wholesale terms apply • Free Wi-Fi: TR_Depot",
                        taxPercent = 5.0,
                        receiptGap = 12,
                        wifiName = "TR_Depot",
                        wifiPassword = "trcoffee2026"
                    )
                )
                branchDao.insertBranches(initialBranches)
            }

            if (userDao.countUsers() == 0) {
                val initialUsers = listOf(
                    UserEntity(
                        name = "Chandara Nong",
                        role = "ADMIN",
                        pin = "1234",
                        email = "nong.chandara@gmail.com",
                        telegram = "@chandaranong"
                    ),
                    UserEntity(
                        name = "Sarah Miller",
                        role = "ADMIN",
                        pin = "1111",
                        email = "sarah.admin@store.local",
                        telegram = "@sarah_admin"
                    ),
                    UserEntity(
                        name = "Alex Chen",
                        role = "CASHIER",
                        pin = "2222",
                        email = "alex.cashier@store.local",
                        telegram = "@alex_cashier"
                    ),
                    UserEntity(
                        name = "David Ross",
                        role = "CLERK",
                        pin = "3333",
                        email = "david.stock@store.local",
                        telegram = "@david_stock"
                    )
                )
                userDao.insertUsers(initialUsers)
            }

            if (productDao.countProducts() == 0) {
                val initialProducts = getStandardStoreProducts()
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
                    Triple(now - (2L * 60 * 60 * 1000), "CARD", listOf(Pair("BEV-001", 3), Pair("COS-001", 1))),
                    Triple(now - (5L * 60 * 60 * 1000), "CASH", listOf(Pair("BAK-001", 2), Pair("BEV-002", 2))),
                    Triple(now - (7L * 60 * 60 * 1000), "DIGITAL", listOf(Pair("SRV-001", 1), Pair("BEV-001", 1))),
                    // 1 day ago
                    Triple(now - (1 * oneDay + 3L * 3600000), "CARD", listOf(Pair("GFT-001", 1), Pair("BEV-006", 2))),
                    Triple(now - (1 * oneDay + 6L * 3600000), "CASH", listOf(Pair("BEV-001", 4), Pair("COS-002", 1))),
                    // 2 days ago
                    Triple(now - (2 * oneDay + 4L * 3600000), "CARD", listOf(Pair("SRV-002", 1), Pair("GFT-002", 1))),
                    Triple(now - (2 * oneDay + 8L * 3600000), "DIGITAL", listOf(Pair("BAK-001", 3), Pair("BEV-007", 2))),
                    // 3 days ago
                    Triple(now - (3 * oneDay + 2L * 3600000), "CARD", listOf(Pair("COS-001", 1), Pair("BEV-002", 3))),
                    Triple(now - (3 * oneDay + 7L * 3600000), "CASH", listOf(Pair("GFT-002", 2), Pair("BEV-008", 2))),
                    // 4 days ago
                    Triple(now - (4 * oneDay + 5L * 3600000), "DIGITAL", listOf(Pair("SRV-005", 1), Pair("BEV-001", 2))),
                    // 5 days ago
                    Triple(now - (5 * oneDay + 4L * 3600000), "CARD", listOf(Pair("BAK-001", 4), Pair("BEV-001", 4))),
                    Triple(now - (5 * oneDay + 9L * 3600000), "CASH", listOf(Pair("COS-003", 2), Pair("BEV-002", 3))),
                    // 6 days ago
                    Triple(now - (6 * oneDay + 3L * 3600000), "CARD", listOf(Pair("BEV-009", 1), Pair("GFT-002", 1))),
                    // Week 2 ago
                    Triple(now - (10 * oneDay), "CARD", listOf(Pair("GFT-001", 2), Pair("BEV-001", 2))),
                    Triple(now - (12 * oneDay), "CASH", listOf(Pair("BAK-001", 2), Pair("BEV-001", 3))),
                    // Week 3 ago
                    Triple(now - (17 * oneDay), "CARD", listOf(Pair("SRV-001", 1), Pair("COS-001", 1))),
                    Triple(now - (19 * oneDay), "DIGITAL", listOf(Pair("BEV-001", 5), Pair("BEV-006", 2))),
                    // Week 4 ago
                    Triple(now - (24 * oneDay), "CARD", listOf(Pair("COS-004", 1), Pair("GFT-002", 1))),
                    Triple(now - (26 * oneDay), "CASH", listOf(Pair("BAK-001", 3), Pair("BEV-002", 4))),
                    // Week 5 ago
                    Triple(now - (32 * oneDay), "CARD", listOf(Pair("SRV-002", 1), Pair("BEV-001", 2))),
                    // Week 6 ago
                    Triple(now - (39 * oneDay), "DIGITAL", listOf(Pair("GFT-001", 1), Pair("BEV-001", 3))),
                    // Week 7 ago
                    Triple(now - (47 * oneDay), "CARD", listOf(Pair("COS-002", 2), Pair("GFT-002", 1))),
                    // Week 8 ago
                    Triple(now - (54 * oneDay), "CASH", listOf(Pair("BAK-001", 4), Pair("BEV-003", 2))),
                    Triple(now - (58 * oneDay), "CARD", listOf(Pair("BEV-009", 2), Pair("BEV-001", 3)))
                )

                // Cache products by SKU
                val productMap = mutableMapOf(
                    "BEV-001" to Triple(1L, "Signature TR Iced Coffee", 2.20),
                    "BEV-002" to Triple(2L, "Iced Vanilla Caffe Latte", 2.50),
                    "BEV-003" to Triple(3L, "Iced Caramel Macchiato", 2.80),
                    "BEV-006" to Triple(6L, "Uji Matcha Green Tea Frappe", 3.00),
                    "BEV-007" to Triple(7L, "Chocolate Mocha Ice Blend", 3.20),
                    "BEV-008" to Triple(8L, "Kampot Honey Lime Iced Tea", 2.00),
                    "BEV-009" to Triple(9L, "TR Artisan Whole Bean Arabica 250g", 7.50),
                    "BAK-001" to Triple(10L, "French Golden Butter Croissant", 1.75),
                    "COS-001" to Triple(11L, "Brightening Vitamin C Glow Serum", 16.50),
                    "COS-002" to Triple(12L, "Ultra Shield SPF 50+ PA++++ Sunscreen", 14.00),
                    "COS-003" to Triple(13L, "Velvet Matte Silk Lip Tint #03 Rose", 9.50),
                    "COS-004" to Triple(14L, "Ceramide Barrier Repair Moisture Cream", 18.00),
                    "SRV-001" to Triple(17L, "Hydra Deep Clean Facial Glow Spa", 28.00),
                    "SRV-002" to Triple(18L, "Deluxe Gel Manicure & Hand Therapy", 18.00),
                    "SRV-005" to Triple(21L, "Botanical Scalp Detox & Relaxing Hair Spa", 22.00),
                    "GFT-001" to Triple(22L, "TR Deluxe Coffee & Skincare Luxury Gift Box", 35.00),
                    "GFT-002" to Triple(23L, "TR Matte Thermal Stainless Tumbler 500ml", 12.00)
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

            // Seed Client Profiles if empty
            val clientDao = database.clientDao()
            if (clientDao.countClients() == 0) {
                val seedClients = listOf(
                    ClientEntity(
                        name = "Sok Dara (សុខ តារា)",
                        phone = "012 889 900",
                        email = "sok.dara@gmail.com",
                        tier = "VIP",
                        loyaltyPoints = 450,
                        totalSpent = 185.50,
                        visitsCount = 38,
                        favoriteOrder = "Iced Latte 50% Sugar, Oat Milk",
                        notes = "Regular customer every morning at 8:30 AM. Prefers less ice.",
                        address = "BKK1, Phnom Penh"
                    ),
                    ClientEntity(
                        name = "Chan Chenda (ចាន់ ចិន្តា)",
                        phone = "098 776 543",
                        email = "chenda.c@outlook.com",
                        tier = "GOLD",
                        loyaltyPoints = 280,
                        totalSpent = 112.00,
                        visitsCount = 24,
                        favoriteOrder = "Green Tea Frappe with extra espresso shot",
                        notes = "Gold member since last year. Likes pastry pairing.",
                        address = "Toul Kork, Phnom Penh"
                    ),
                    ClientEntity(
                        name = "Michael Chen",
                        phone = "085 332 114",
                        email = "m.chen.biz@gmail.com",
                        tier = "SILVER",
                        loyaltyPoints = 140,
                        totalSpent = 64.00,
                        visitsCount = 16,
                        favoriteOrder = "Hot Double Espresso, Dark Roast",
                        notes = "Usually sits at outdoor table 4 with laptop.",
                        address = "Daun Penh, Phnom Penh"
                    ),
                    ClientEntity(
                        name = "Keo Vicheka (កែវ វិច្ឆិកា)",
                        phone = "015 667 889",
                        email = "vicheka.keo@gmail.com",
                        tier = "BRONZE",
                        loyaltyPoints = 50,
                        totalSpent = 22.50,
                        visitsCount = 6,
                        favoriteOrder = "Hot Cappuccino, Cinnamon sprinkle",
                        notes = "New member, likes mild roast coffee.",
                        address = "Chroy Changvar, Phnom Penh"
                    ),
                    ClientEntity(
                        name = "Seng Sophea (សេង សុភា)",
                        phone = "077 123 456",
                        email = "sophea.seng@yahoo.com",
                        tier = "GOLD",
                        loyaltyPoints = 310,
                        totalSpent = 135.00,
                        visitsCount = 29,
                        favoriteOrder = "Signature TR Iced Coffee, Normal Sweet",
                        notes = "Company team orders frequently for afternoon meetings.",
                        address = "Russian Market, Phnom Penh"
                    )
                )
                clientDao.insertClients(seedClients)
            }
        }

        fun getStandardStoreProducts(): List<ProductEntity> = listOf(
            // Coffee & Beverages
            ProductEntity(
                name = "Signature TR Iced Coffee",
                sku = "BEV-001",
                category = "Coffee & Beverages",
                costPrice = 0.90,
                sellingPrice = 2.20,
                stockQuantity = 40,
                minStockThreshold = 10,
                unit = "cup",
                barcode = "885901230007",
                imageUrl = "https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=600&q=80"
            ),
            ProductEntity(
                name = "Iced Vanilla Caffe Latte",
                sku = "BEV-002",
                category = "Coffee & Beverages",
                costPrice = 1.10,
                sellingPrice = 2.50,
                stockQuantity = 35,
                minStockThreshold = 10,
                unit = "cup",
                barcode = "885901230008",
                imageUrl = "https://images.unsplash.com/photo-1541167760496-1628856ab772?auto=format&fit=crop&w=600&q=80"
            ),
            ProductEntity(
                name = "Iced Caramel Macchiato",
                sku = "BEV-003",
                category = "Coffee & Beverages",
                costPrice = 1.20,
                sellingPrice = 2.80,
                stockQuantity = 28,
                minStockThreshold = 8,
                unit = "cup",
                barcode = "885901230009",
                imageUrl = "https://images.unsplash.com/photo-1461023058943-07fcbe16d735?auto=format&fit=crop&w=600&q=80"
            ),
            ProductEntity(
                name = "Handcrafted Hot Cappuccino",
                sku = "BEV-004",
                category = "Coffee & Beverages",
                costPrice = 0.95,
                sellingPrice = 2.20,
                stockQuantity = 30,
                minStockThreshold = 8,
                unit = "cup",
                barcode = "885901230010",
                imageUrl = "https://images.unsplash.com/photo-1534778101976-62847782c213?auto=format&fit=crop&w=600&q=80"
            ),
            ProductEntity(
                name = "Caffe Americano Dark Roast",
                sku = "BEV-005",
                category = "Coffee & Beverages",
                costPrice = 0.70,
                sellingPrice = 1.80,
                stockQuantity = 45,
                minStockThreshold = 10,
                unit = "cup",
                barcode = "885901230011",
                imageUrl = "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?auto=format&fit=crop&w=600&q=80"
            ),
            ProductEntity(
                name = "Uji Matcha Green Tea Frappe",
                sku = "BEV-006",
                category = "Coffee & Beverages",
                costPrice = 1.30,
                sellingPrice = 3.00,
                stockQuantity = 24,
                minStockThreshold = 6,
                unit = "cup",
                barcode = "885901230012",
                imageUrl = "https://images.unsplash.com/photo-1536256263959-770b48d82b0a?auto=format&fit=crop&w=600&q=80"
            ),
            ProductEntity(
                name = "Chocolate Mocha Ice Blend",
                sku = "BEV-007",
                category = "Coffee & Beverages",
                costPrice = 1.40,
                sellingPrice = 3.20,
                stockQuantity = 22,
                minStockThreshold = 6,
                unit = "cup",
                barcode = "885901230013",
                imageUrl = "https://images.unsplash.com/photo-1572490122747-3968b75cc699?auto=format&fit=crop&w=600&q=80"
            ),
            ProductEntity(
                name = "Kampot Honey Lime Iced Tea",
                sku = "BEV-008",
                category = "Coffee & Beverages",
                costPrice = 0.80,
                sellingPrice = 2.00,
                stockQuantity = 32,
                minStockThreshold = 8,
                unit = "cup",
                barcode = "885901230014",
                imageUrl = "https://images.unsplash.com/photo-1556679343-c7306c1976bc?auto=format&fit=crop&w=600&q=80"
            ),
            ProductEntity(
                name = "TR Artisan Whole Bean Arabica 250g",
                sku = "BEV-009",
                category = "Coffee & Beverages",
                costPrice = 3.20,
                sellingPrice = 7.50,
                stockQuantity = 20,
                minStockThreshold = 5,
                unit = "bag",
                barcode = "885901230016",
                imageUrl = "https://images.unsplash.com/photo-1587734195503-904fca47e0e9?auto=format&fit=crop&w=600&q=80"
            ),
            // Bakery
            ProductEntity(
                name = "French Golden Butter Croissant",
                sku = "BAK-001",
                category = "Bakery",
                costPrice = 0.75,
                sellingPrice = 1.75,
                stockQuantity = 4, // low stock alert (<5)
                minStockThreshold = 6,
                unit = "pcs",
                barcode = "885901230015",
                imageUrl = "https://images.unsplash.com/photo-1555507036-ab1f4038808a?auto=format&fit=crop&w=600&q=80"
            ),
            // Cosmetics & Skincare
            ProductEntity(
                name = "Brightening Vitamin C Glow Serum",
                sku = "COS-001",
                category = "Cosmetics",
                costPrice = 7.50,
                sellingPrice = 16.50,
                stockQuantity = 25,
                minStockThreshold = 6,
                unit = "bottle",
                barcode = "885901230001",
                imageUrl = "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=600&q=80"
            ),
            ProductEntity(
                name = "Ultra Shield SPF 50+ PA++++ Sunscreen",
                sku = "COS-002",
                category = "Cosmetics",
                costPrice = 6.20,
                sellingPrice = 14.00,
                stockQuantity = 3, // low stock alert (<5)
                minStockThreshold = 5,
                unit = "tube",
                barcode = "885901230002",
                imageUrl = "https://images.unsplash.com/photo-1556228720-195a672e8a03?auto=format&fit=crop&w=600&q=80"
            ),
            ProductEntity(
                name = "Velvet Matte Silk Lip Tint #03 Rose",
                sku = "COS-003",
                category = "Cosmetics",
                costPrice = 3.80,
                sellingPrice = 9.50,
                stockQuantity = 22,
                minStockThreshold = 5,
                unit = "pcs",
                barcode = "885901230003",
                imageUrl = "https://images.unsplash.com/photo-1586495777744-4413f21062fa?auto=format&fit=crop&w=600&q=80"
            ),
            ProductEntity(
                name = "Ceramide Barrier Repair Moisture Cream",
                sku = "COS-004",
                category = "Cosmetics",
                costPrice = 7.90,
                sellingPrice = 18.00,
                stockQuantity = 18,
                minStockThreshold = 5,
                unit = "jar",
                barcode = "885901230004",
                imageUrl = "https://images.unsplash.com/photo-1608248597359-0a618424a1b0?auto=format&fit=crop&w=600&q=80"
            ),
            ProductEntity(
                name = "Low-pH Tea Tree Calming Gel Cleanser",
                sku = "COS-005",
                category = "Cosmetics",
                costPrice = 4.50,
                sellingPrice = 11.00,
                stockQuantity = 26,
                minStockThreshold = 6,
                unit = "bottle",
                barcode = "885901230005",
                imageUrl = "https://images.unsplash.com/photo-1556228722-d0b71946059d?auto=format&fit=crop&w=600&q=80"
            ),
            ProductEntity(
                name = "Damask Rose Dew Hydrating Glow Mist",
                sku = "COS-006",
                category = "Cosmetics",
                costPrice = 5.20,
                sellingPrice = 12.50,
                stockQuantity = 20,
                minStockThreshold = 5,
                unit = "bottle",
                barcode = "885901230006",
                imageUrl = "https://images.unsplash.com/photo-1608248597359-0a618424a1b0?auto=format&fit=crop&w=600&q=80"
            ),
            // Salon Spa & Services
            ProductEntity(
                name = "Hydra Deep Clean Facial Glow Spa",
                sku = "SRV-001",
                category = "Salon & Services",
                costPrice = 9.00,
                sellingPrice = 28.00,
                stockQuantity = 50,
                minStockThreshold = 10,
                unit = "session",
                barcode = "885901230017",
                imageUrl = "https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?auto=format&fit=crop&w=600&q=80"
            ),
            ProductEntity(
                name = "Deluxe Gel Manicure & Hand Therapy",
                sku = "SRV-002",
                category = "Salon & Services",
                costPrice = 5.50,
                sellingPrice = 18.00,
                stockQuantity = 50,
                minStockThreshold = 10,
                unit = "session",
                barcode = "885901230018",
                imageUrl = "https://images.unsplash.com/photo-1632345031435-8727f6897d53?auto=format&fit=crop&w=600&q=80"
            ),
            ProductEntity(
                name = "Mobile Event Coffee & Barista Catering",
                sku = "SRV-003",
                category = "Salon & Services",
                costPrice = 45.00,
                sellingPrice = 120.00,
                stockQuantity = 15,
                minStockThreshold = 3,
                unit = "event",
                barcode = "885901230019",
                imageUrl = "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?auto=format&fit=crop&w=600&q=80"
            ),
            ProductEntity(
                name = "1-on-1 Skin & Makeup Consultation",
                sku = "SRV-004",
                category = "Salon & Services",
                costPrice = 3.00,
                sellingPrice = 15.00,
                stockQuantity = 40,
                minStockThreshold = 8,
                unit = "session",
                barcode = "885901230020",
                imageUrl = "https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?auto=format&fit=crop&w=600&q=80"
            ),
            ProductEntity(
                name = "Botanical Scalp Detox & Relaxing Hair Spa",
                sku = "SRV-005",
                category = "Salon & Services",
                costPrice = 7.00,
                sellingPrice = 22.00,
                stockQuantity = 40,
                minStockThreshold = 8,
                unit = "session",
                barcode = "885901230021",
                imageUrl = "https://images.unsplash.com/photo-1560066984-138dadb4c035?auto=format&fit=crop&w=600&q=80"
            ),
            // Luxury Gift Sets & Merchandise
            ProductEntity(
                name = "TR Deluxe Coffee & Skincare Luxury Gift Box",
                sku = "GFT-001",
                category = "Gift Sets & Merch",
                costPrice = 14.00,
                sellingPrice = 35.00,
                stockQuantity = 16,
                minStockThreshold = 5,
                unit = "box",
                barcode = "885901230022",
                imageUrl = "https://images.unsplash.com/photo-1549465220-1a8b9238cd48?auto=format&fit=crop&w=600&q=80"
            ),
            ProductEntity(
                name = "TR Matte Thermal Stainless Tumbler 500ml",
                sku = "GFT-002",
                category = "Gift Sets & Merch",
                costPrice = 4.50,
                sellingPrice = 12.00,
                stockQuantity = 25,
                minStockThreshold = 6,
                unit = "pcs",
                barcode = "885901230023",
                imageUrl = "https://images.unsplash.com/photo-1577937927133-66ef06acdf18?auto=format&fit=crop&w=600&q=80"
            )
        )

        suspend fun syncStandardStoreData(database: AppDatabase) {
            val productDao = database.productDao()
            val categoryDao = database.categoryDao()
            val userDao = database.userDao()

            // 1. Sync standard categories
            val standardCats = listOf(
                CategoryEntity(name = "Coffee & Beverages", description = "Signature coffees, espresso, iced drinks, teas, and frappes", colorHex = "#0EA5E9"),
                CategoryEntity(name = "Bakery", description = "Freshly baked artisan croissants and pastries", colorHex = "#F59E0B"),
                CategoryEntity(name = "Cosmetics", description = "Serums, sunscreens, hydrating creams, lip tints and cleansers", colorHex = "#EC4899"),
                CategoryEntity(name = "Salon & Services", description = "Hydra facial spa, deluxe nail care, hair spa and mobile catering", colorHex = "#8B5CF6"),
                CategoryEntity(name = "Gift Sets & Merch", description = "Deluxe holiday gift boxes and stainless thermal tumblers", colorHex = "#10B981")
            )
            standardCats.forEach { cat ->
                if (categoryDao.getCategoryByName(cat.name) == null) {
                    categoryDao.insertCategory(cat)
                }
            }

            // 2. Sync standard users
            val standardUsers = listOf(
                UserEntity(
                    name = "Chandara Nong",
                    role = "ADMIN",
                    pin = "1234",
                    email = "nong.chandara@gmail.com",
                    telegram = "@chandaranong"
                ),
                UserEntity(
                    name = "Sarah Miller",
                    role = "ADMIN",
                    pin = "1111",
                    email = "sarah.admin@store.local",
                    telegram = "@sarah_admin"
                ),
                UserEntity(
                    name = "Alex Chen",
                    role = "CASHIER",
                    pin = "2222",
                    email = "alex.cashier@store.local",
                    telegram = "@alex_cashier"
                ),
                UserEntity(
                    name = "David Ross",
                    role = "CLERK",
                    pin = "3333",
                    email = "david.stock@store.local",
                    telegram = "@david_stock"
                )
            )
            standardUsers.forEach { u ->
                val existing = userDao.getUserByPin(u.pin)
                if (existing == null) {
                    userDao.insertUser(u)
                }
            }

            // 3. Sync standard products (ensure all 23 items exist)
            val prods = getStandardStoreProducts()
            val hasCosmetics = productDao.getProductBySku("COS-001") != null
            if (!hasCosmetics) {
                // Remove outdated generic items that don't match store theme
                val obsoleteSkus = listOf("SNK-001", "SNK-002", "ACC-001", "ACC-002", "HOM-001", "CAR-001", "APP-001")
                obsoleteSkus.forEach { sku ->
                    productDao.getProductBySku(sku)?.let { productDao.deleteProduct(it) }
                }
                productDao.insertProducts(prods)
            } else {
                // Make sure every single product exists
                prods.forEach { p ->
                    if (productDao.getProductBySku(p.sku) == null) {
                        productDao.insertProduct(p)
                    }
                }
            }
        }
    }
}
