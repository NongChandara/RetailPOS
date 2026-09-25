package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.AppDatabase
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RetailRepository(private val database: AppDatabase) {
    private val productDao = database.productDao()
    private val userDao = database.userDao()
    private val saleDao = database.saleDao()
    private val stockMovementDao = database.stockMovementDao()
    private val activityLogDao = database.activityLogDao()
    private val categoryDao = database.categoryDao()
    private val branchDao = database.branchDao()
    private val clientDao = database.clientDao()

    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val lowStockProducts: Flow<List<ProductEntity>> = productDao.getLowStockProducts()
    val allCategories: Flow<List<String>> = productDao.getAllCategories()
    val allCategoriesList: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val allBranchesList: Flow<List<BranchEntity>> = branchDao.getAllBranches()
    val allClients: Flow<List<ClientEntity>> = clientDao.getAllClients()
    val allSales: Flow<List<SaleEntity>> = saleDao.getAllSales()
    val allMovements: Flow<List<StockMovementEntity>> = stockMovementDao.getAllMovements()
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val allLogs: Flow<List<ActivityLogEntity>> = activityLogDao.getAllLogs()

    fun getLogsByStaff(staffId: Long): Flow<List<ActivityLogEntity>> = activityLogDao.getLogsByStaff(staffId)
    fun getLogsByCategory(category: String): Flow<List<ActivityLogEntity>> = activityLogDao.getLogsByCategory(category)
    fun getLogsByStaffAndCategory(staffId: Long, category: String): Flow<List<ActivityLogEntity>> =
        activityLogDao.getLogsByStaffAndCategory(staffId, category)

    suspend fun logActivity(log: ActivityLogEntity): Long = withContext(Dispatchers.IO) {
        activityLogDao.insertLog(log)
    }

    fun searchProducts(query: String): Flow<List<ProductEntity>> = productDao.searchProducts(query)

    fun getProductsByCategory(category: String): Flow<List<ProductEntity>> = productDao.getProductsByCategory(category)

    suspend fun getProductById(id: Long): ProductEntity? = withContext(Dispatchers.IO) {
        productDao.getProductById(id)
    }

    suspend fun insertProduct(
        product: ProductEntity,
        operatorName: String,
        staffId: Long = 1L,
        staffRole: String = "STAFF"
    ): Long = withContext(Dispatchers.IO) {
        val newId = productDao.insertProduct(product)
        if (product.stockQuantity > 0) {
            stockMovementDao.insertMovement(
                StockMovementEntity(
                    productId = newId,
                    productName = product.name,
                    type = "RESTOCK",
                    quantityDelta = product.stockQuantity,
                    resultingStock = product.stockQuantity,
                    userName = operatorName,
                    reason = "New product created"
                )
            )
        }

        // Secure Activity Audit Logging
        val auditLog = ActivityLogEntity.createSecureLog(
            staffId = staffId,
            staffName = operatorName,
            staffRole = staffRole,
            category = ActivityCategory.INVENTORY_MODIFICATION,
            action = "PRODUCT_CREATED",
            entityType = "PRODUCT",
            entityId = product.sku,
            details = "Added product '${product.name}' (SKU: ${product.sku}, Cat: ${product.category}) with initial stock ${product.stockQuantity} ${product.unit} @ $${String.format("%.2f", product.sellingPrice)}",
            metadataJson = """{"productId":$newId,"sku":"${product.sku}","initialStock":${product.stockQuantity},"price":${product.sellingPrice},"cost":${product.costPrice}}"""
        )
        activityLogDao.insertLog(auditLog)

        newId
    }

    suspend fun updateProduct(
        product: ProductEntity,
        operatorName: String = "Staff",
        staffId: Long = 1L,
        staffRole: String = "STAFF"
    ) = withContext(Dispatchers.IO) {
        productDao.updateProduct(product)

        // Secure Activity Audit Logging
        val auditLog = ActivityLogEntity.createSecureLog(
            staffId = staffId,
            staffName = operatorName,
            staffRole = staffRole,
            category = ActivityCategory.INVENTORY_MODIFICATION,
            action = "PRODUCT_UPDATED",
            entityType = "PRODUCT",
            entityId = product.sku,
            details = "Updated catalog parameters for '${product.name}' (SKU: ${product.sku}) - Price: $${String.format("%.2f", product.sellingPrice)}, Cost: $${String.format("%.2f", product.costPrice)}, Min Stock: ${product.minStockThreshold}",
            metadataJson = """{"productId":${product.id},"sku":"${product.sku}","price":${product.sellingPrice},"cost":${product.costPrice},"threshold":${product.minStockThreshold}}"""
        )
        activityLogDao.insertLog(auditLog)
    }

    suspend fun deleteProduct(
        product: ProductEntity,
        operatorName: String = "Staff",
        staffId: Long = 1L,
        staffRole: String = "STAFF"
    ) = withContext(Dispatchers.IO) {
        productDao.deleteProduct(product)

        // Secure Activity Audit Logging
        val auditLog = ActivityLogEntity.createSecureLog(
            staffId = staffId,
            staffName = operatorName,
            staffRole = staffRole,
            category = ActivityCategory.INVENTORY_MODIFICATION,
            action = "PRODUCT_DELETED",
            entityType = "PRODUCT",
            entityId = product.sku,
            details = "Deleted product catalog item '${product.name}' (SKU: ${product.sku}) with ${product.stockQuantity} remaining stock units",
            metadataJson = """{"productId":${product.id},"sku":"${product.sku}","name":"${product.name}","stockAtDeletion":${product.stockQuantity}}"""
        )
        activityLogDao.insertLog(auditLog)
    }

    suspend fun adjustStock(
        productId: Long,
        quantityDelta: Int,
        type: String,
        operatorName: String,
        reason: String,
        staffId: Long = 1L,
        staffRole: String = "STAFF"
    ) = withContext(Dispatchers.IO) {
        val current = productDao.getProductById(productId) ?: return@withContext
        val newStock = (current.stockQuantity + quantityDelta).coerceAtLeast(0)
        productDao.updateStock(productId, newStock)
        stockMovementDao.insertMovement(
            StockMovementEntity(
                productId = productId,
                productName = current.name,
                type = type,
                quantityDelta = quantityDelta,
                resultingStock = newStock,
                userName = operatorName,
                reason = reason
            )
        )

        // Secure Activity Audit Logging
        val deltaPrefix = if (quantityDelta >= 0) "+$quantityDelta" else "$quantityDelta"
        val auditLog = ActivityLogEntity.createSecureLog(
            staffId = staffId,
            staffName = operatorName,
            staffRole = staffRole,
            category = ActivityCategory.INVENTORY_MODIFICATION,
            action = if (type.contains("RESTOCK", ignoreCase = true)) "STOCK_RESTOCK" else "STOCK_ADJUSTMENT",
            entityType = "STOCK",
            entityId = current.sku,
            details = "Stock adjusted for '${current.name}' (SKU: ${current.sku}): $deltaPrefix units (${current.stockQuantity} -> $newStock). Type: $type. Reason: $reason",
            metadataJson = """{"productId":$productId,"sku":"${current.sku}","previousStock":${current.stockQuantity},"newStock":$newStock,"delta":$quantityDelta,"type":"$type","reason":"$reason"}"""
        )
        activityLogDao.insertLog(auditLog)
    }

    suspend fun processSale(
        sale: SaleEntity,
        items: List<SaleItemEntity>,
        cashierRole: String = "CASHIER"
    ): Long = withContext(Dispatchers.IO) {
        database.withTransaction {
            val saleId = saleDao.insertSale(sale)
            val updatedItems = items.map { it.copy(saleId = saleId) }
            saleDao.insertSaleItems(updatedItems)

            // Decrement stock in real-time and log stock movements
            for (item in updatedItems) {
                val currentProduct = productDao.getProductById(item.productId)
                if (currentProduct != null) {
                    val newStock = (currentProduct.stockQuantity - item.quantity).coerceAtLeast(0)
                    productDao.updateStock(currentProduct.id, newStock)

                    stockMovementDao.insertMovement(
                        StockMovementEntity(
                            productId = currentProduct.id,
                            productName = currentProduct.name,
                            type = "SALE",
                            quantityDelta = -item.quantity,
                            resultingStock = newStock,
                            userName = sale.cashierName,
                            reason = "POS Sale #${sale.receiptNumber}"
                        )
                    )
                }
            }

            // Secure Activity Audit Logging for POS Transaction
            val totalItemsCount = items.sumOf { it.quantity }
            val formattedTotal = String.format("%.2f", sale.totalAmount)
            val auditLog = ActivityLogEntity.createSecureLog(
                staffId = sale.cashierId,
                staffName = sale.cashierName,
                staffRole = cashierRole,
                category = ActivityCategory.POS_TRANSACTION,
                action = "SALE_COMPLETED",
                entityType = "SALE",
                entityId = sale.receiptNumber,
                details = "Completed POS Sale #${sale.receiptNumber} by ${sale.cashierName} (Staff ID #${sale.cashierId}) for $$formattedTotal via ${sale.paymentMethod} ($totalItemsCount items)",
                metadataJson = """{"saleId":$saleId,"receipt":"${sale.receiptNumber}","subtotal":${sale.subtotal},"tax":${sale.taxAmount},"discount":${sale.discountAmount},"total":${sale.totalAmount},"paymentMethod":"${sale.paymentMethod}","itemsCount":$totalItemsCount}""",
                timestamp = sale.timestamp
            )
            activityLogDao.insertLog(auditLog)

            saleId
        }
    }

    suspend fun getSaleItems(saleId: Long): List<SaleItemEntity> = withContext(Dispatchers.IO) {
        saleDao.getSaleItemsList(saleId)
    }

    suspend fun insertUser(user: UserEntity): Long = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
    }

    suspend fun updateUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.deleteUser(user)
    }

    suspend fun getUserByPin(pin: String): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserByPin(pin)
    }

    // Category Operations
    suspend fun insertCategory(category: CategoryEntity): Long = withContext(Dispatchers.IO) {
        categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(oldName: String, category: CategoryEntity, operatorName: String, staffId: Long = 1L): Unit = withContext(Dispatchers.IO) {
        categoryDao.updateCategory(category)
        if (oldName != category.name) {
            productDao.updateCategoryName(oldName, category.name)
        }
        val auditLog = ActivityLogEntity.createSecureLog(
            staffId = staffId,
            staffName = operatorName,
            staffRole = "ADMIN",
            category = ActivityCategory.INVENTORY_MODIFICATION,
            action = "CATEGORY_UPDATED",
            entityType = "CATEGORY",
            entityId = category.name,
            details = "Updated category '$oldName' -> '${category.name}'",
            metadataJson = """{"oldName":"$oldName","newName":"${category.name}"}"""
        )
        activityLogDao.insertLog(auditLog)
    }

    suspend fun deleteCategory(category: CategoryEntity, fallbackCategory: String = "General", operatorName: String, staffId: Long = 1L): Unit = withContext(Dispatchers.IO) {
        categoryDao.deleteCategory(category)
        productDao.reassignCategory(category.name, fallbackCategory)
        val auditLog = ActivityLogEntity.createSecureLog(
            staffId = staffId,
            staffName = operatorName,
            staffRole = "ADMIN",
            category = ActivityCategory.INVENTORY_MODIFICATION,
            action = "CATEGORY_DELETED",
            entityType = "CATEGORY",
            entityId = category.name,
            details = "Deleted category '${category.name}' (reassigned products to '$fallbackCategory')",
            metadataJson = """{"deletedCategory":"${category.name}","fallbackCategory":"$fallbackCategory"}"""
        )
        activityLogDao.insertLog(auditLog)
    }

    // Branch Operations
    suspend fun insertBranch(branch: BranchEntity): Long = withContext(Dispatchers.IO) {
        branchDao.insertBranch(branch)
    }

    suspend fun updateBranch(oldName: String, branch: BranchEntity, operatorName: String, staffId: Long = 1L): Unit = withContext(Dispatchers.IO) {
        branchDao.updateBranch(branch)
        if (oldName != branch.name) {
            productDao.updateBranchName(oldName, branch.name)
        }
        val auditLog = ActivityLogEntity.createSecureLog(
            staffId = staffId,
            staffName = operatorName,
            staffRole = "ADMIN",
            category = ActivityCategory.INVENTORY_MODIFICATION,
            action = "BRANCH_UPDATED",
            entityType = "BRANCH",
            entityId = branch.name,
            details = "Updated branch '$oldName' -> '${branch.name}' (Code: ${branch.code})",
            metadataJson = """{"oldName":"$oldName","newName":"${branch.name}","code":"${branch.code}"}"""
        )
        activityLogDao.insertLog(auditLog)
    }

    suspend fun updateBranchReceiptConfig(
        branch: BranchEntity,
        operatorName: String,
        staffId: Long = 1L
    ): Unit = withContext(Dispatchers.IO) {
        branchDao.updateBranch(branch)
        val auditLog = ActivityLogEntity.createSecureLog(
            staffId = staffId,
            staffName = operatorName,
            staffRole = "ADMIN",
            category = ActivityCategory.STAFF_SECURITY,
            action = "RECEIPT_CONFIG_UPDATED",
            entityType = "BRANCH_RECEIPT",
            entityId = branch.name,
            details = "Updated receipt template & tax rate (${branch.taxPercent}%) for branch '${branch.name}'",
            metadataJson = """{"branch":"${branch.name}","taxPercent":${branch.taxPercent},"receiptGap":${branch.receiptGap}}"""
        )
        activityLogDao.insertLog(auditLog)
    }

    suspend fun deleteBranch(branch: BranchEntity, fallbackBranch: String = "Main Branch", operatorName: String, staffId: Long = 1L): Unit = withContext(Dispatchers.IO) {
        branchDao.deleteBranch(branch)
        productDao.reassignBranch(branch.name, fallbackBranch)
        val auditLog = ActivityLogEntity.createSecureLog(
            staffId = staffId,
            staffName = operatorName,
            staffRole = "ADMIN",
            category = ActivityCategory.INVENTORY_MODIFICATION,
            action = "BRANCH_DELETED",
            entityType = "BRANCH",
            entityId = branch.name,
            details = "Deleted branch '${branch.name}' (reassigned products to '$fallbackBranch')",
            metadataJson = """{"deletedBranch":"${branch.name}","fallbackBranch":"$fallbackBranch"}"""
        )
        activityLogDao.insertLog(auditLog)
    }

    // Client Profile Operations
    fun searchClients(query: String): Flow<List<ClientEntity>> = clientDao.searchClients(query)

    suspend fun getClientById(id: Long): ClientEntity? = withContext(Dispatchers.IO) {
        clientDao.getClientById(id)
    }

    suspend fun insertClient(client: ClientEntity, operatorName: String, staffId: Long = 1L): Long = withContext(Dispatchers.IO) {
        val newId = clientDao.insertClient(client)
        val auditLog = ActivityLogEntity.createSecureLog(
            staffId = staffId,
            staffName = operatorName,
            staffRole = "STAFF",
            category = ActivityCategory.STAFF_SECURITY,
            action = "CLIENT_CREATED",
            entityType = "CLIENT",
            entityId = "$newId",
            details = "Created client profile '${client.name}' (${client.phone}) - Tier: ${client.tier}",
            metadataJson = """{"clientId":$newId,"name":"${client.name}","tier":"${client.tier}","phone":"${client.phone}"}"""
        )
        activityLogDao.insertLog(auditLog)
        newId
    }

    suspend fun updateClient(client: ClientEntity, operatorName: String, staffId: Long = 1L): Unit = withContext(Dispatchers.IO) {
        clientDao.updateClient(client.copy(updatedAt = System.currentTimeMillis()))
        val auditLog = ActivityLogEntity.createSecureLog(
            staffId = staffId,
            staffName = operatorName,
            staffRole = "STAFF",
            category = ActivityCategory.STAFF_SECURITY,
            action = "CLIENT_UPDATED",
            entityType = "CLIENT",
            entityId = "${client.id}",
            details = "Updated client profile #${client.id} '${client.name}' - Tier: ${client.tier}, Points: ${client.loyaltyPoints}",
            metadataJson = """{"clientId":${client.id},"name":"${client.name}","tier":"${client.tier}","points":${client.loyaltyPoints}}"""
        )
        activityLogDao.insertLog(auditLog)
    }

    suspend fun deleteClient(client: ClientEntity, operatorName: String, staffId: Long = 1L): Unit = withContext(Dispatchers.IO) {
        clientDao.deleteClient(client)
        val auditLog = ActivityLogEntity.createSecureLog(
            staffId = staffId,
            staffName = operatorName,
            staffRole = "ADMIN",
            category = ActivityCategory.STAFF_SECURITY,
            action = "CLIENT_DELETED",
            entityType = "CLIENT",
            entityId = "${client.id}",
            details = "Deleted client profile '${client.name}' (#${client.id})",
            metadataJson = """{"clientId":${client.id},"name":"${client.name}"}"""
        )
        activityLogDao.insertLog(auditLog)
    }

    suspend fun adjustClientPoints(client: ClientEntity, delta: Int, reason: String, operatorName: String, staffId: Long = 1L): Unit = withContext(Dispatchers.IO) {
        val newPoints = (client.loyaltyPoints + delta).coerceAtLeast(0)
        clientDao.updateLoyaltyPoints(client.id, delta)
        val auditLog = ActivityLogEntity.createSecureLog(
            staffId = staffId,
            staffName = operatorName,
            staffRole = "STAFF",
            category = ActivityCategory.STAFF_SECURITY,
            action = if (delta >= 0) "CLIENT_POINTS_EARNED" else "CLIENT_POINTS_REDEEMED",
            entityType = "CLIENT",
            entityId = "${client.id}",
            details = "Adjusted points for '${client.name}': ${if (delta >= 0) "+$delta" else "$delta"} pts ($reason). New balance: $newPoints",
            metadataJson = """{"clientId":${client.id},"delta":$delta,"newPoints":$newPoints,"reason":"$reason"}"""
        )
        activityLogDao.insertLog(auditLog)
    }
}
