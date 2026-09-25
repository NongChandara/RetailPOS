package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.data.repository.RetailRepository
import com.example.service.LowStockMonitoringService
import com.example.service.LowStockNotificationHelper
import com.example.util.CurrencyMode
import com.example.util.CurrencyUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class CartItem(
    val product: ProductEntity,
    val quantity: Int
) {
    val subtotal: Double get() = product.sellingPrice * quantity
}

data class ScanResult(
    val success: Boolean,
    val product: ProductEntity? = null,
    val message: String
)

enum class StockFilter {
    ALL, LOW_STOCK, OUT_OF_STOCK
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = RetailRepository(database)

    init {
        // Ensure initial seed data is loaded on first launch
        viewModelScope.launch {
            AppDatabase.populateInitialData(database)
        }
        // Initialize background low-stock monitoring
        LowStockMonitoringService.schedulePeriodicMonitoring(
            context = application,
            intervalMinutes = 15,
            thresholdOverride = null
        )
    }

    // Currency & Cambodian Riel (KHR) Exchange Rate State
    private val _currencyMode = MutableStateFlow(CurrencyMode.DUAL)
    val currencyMode: StateFlow<CurrencyMode> = _currencyMode.asStateFlow()

    private val _khrExchangeRate = MutableStateFlow(CurrencyUtils.DEFAULT_KHR_EXCHANGE_RATE)
    val khrExchangeRate: StateFlow<Double> = _khrExchangeRate.asStateFlow()

    fun setCurrencyMode(mode: CurrencyMode) {
        _currencyMode.value = mode
        CurrencyUtils.activeCurrencyMode = mode
        _toastMessage.value = "Currency display set to ${mode.label} (${mode.symbol})"
    }

    fun setKhrExchangeRate(rate: Double) {
        if (rate > 0) {
            _khrExchangeRate.value = rate
            CurrencyUtils.activeKhrExchangeRate = rate
            _toastMessage.value = "Exchange rate set: $1 USD = ${CurrencyUtils.formatKhrRaw(rate.toLong())}"
        }
    }

    fun formatPrice(amountInUsd: Double, compact: Boolean = false): String {
        return CurrencyUtils.formatCurrency(amountInUsd, _currencyMode.value, _khrExchangeRate.value, compact)
    }

    fun formatKhr(amountInUsd: Double): String {
        return CurrencyUtils.formatKhr(amountInUsd, _khrExchangeRate.value)
    }

    fun formatUsd(amountInUsd: Double): String {
        return CurrencyUtils.formatUsd(amountInUsd)
    }

    fun usdToKhr(amountInUsd: Double): Long {
        return CurrencyUtils.usdToKhr(amountInUsd, _khrExchangeRate.value)
    }

    fun khrToUsd(amountInKhr: Long): Double {
        return CurrencyUtils.khrToUsd(amountInKhr, _khrExchangeRate.value)
    }

    // Low Stock Background Monitoring State & Controls
    private val _isMonitoringActive = MutableStateFlow(true)
    val isMonitoringActive: StateFlow<Boolean> = _isMonitoringActive.asStateFlow()

    private val _lowStockThresholdOverride = MutableStateFlow<Int?>(null) // null = per-item minStockThreshold
    val lowStockThresholdOverride: StateFlow<Int?> = _lowStockThresholdOverride.asStateFlow()

    fun setLowStockThresholdOverride(threshold: Int?) {
        _lowStockThresholdOverride.value = threshold
        val msg = if (threshold != null) "Alert threshold set to $threshold units" else "Using per-item default thresholds"
        _toastMessage.value = msg
        if (_isMonitoringActive.value) {
            LowStockMonitoringService.schedulePeriodicMonitoring(
                context = getApplication(),
                intervalMinutes = 15,
                thresholdOverride = threshold
            )
        }
    }

    fun toggleBackgroundMonitoring(enabled: Boolean) {
        _isMonitoringActive.value = enabled
        val context = getApplication<Application>()
        if (enabled) {
            LowStockMonitoringService.schedulePeriodicMonitoring(
                context = context,
                intervalMinutes = 15,
                thresholdOverride = _lowStockThresholdOverride.value
            )
            _toastMessage.value = "Background inventory monitoring activated (every 15 mins)"
        } else {
            LowStockMonitoringService.cancelPeriodicMonitoring(context)
            _toastMessage.value = "Background inventory monitoring paused"
        }
    }

    fun triggerImmediateStockScan() {
        val context = getApplication<Application>()
        LowStockMonitoringService.triggerImmediateCheck(
            context = context,
            thresholdOverride = _lowStockThresholdOverride.value
        )
        _toastMessage.value = "Scanning inventory levels across all catalog items..."
    }

    fun sendTestAlertNotification() {
        val context = getApplication<Application>()
        val manager = _currentUser.value?.name ?: "Store Manager"
        LowStockNotificationHelper.sendTestAlert(context, manager)
        _toastMessage.value = "Test push notification sent to manager device"
    }

    // Live Database Flows
    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<ProductEntity>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSales: StateFlow<List<SaleEntity>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMovements: StateFlow<List<StockMovementEntity>> = repository.allMovements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allActivityLogs: StateFlow<List<ActivityLogEntity>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategoriesList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBranches: StateFlow<List<BranchEntity>> = repository.allBranchesList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamic category names from registered categories and existing product records
    val categoryNames: StateFlow<List<String>> = combine(allCategories, allProducts) { categories, products ->
        val set = linkedSetOf<String>()
        categories.forEach { if (it.name.isNotBlank()) set.add(it.name) }
        products.forEach { if (it.category.isNotBlank()) set.add(it.category) }
        if (set.isEmpty()) {
            listOf("Beverages", "Bakery", "Snacks", "Electronics", "Home & Goods", "Personal Care", "Apparel")
        } else {
            set.toList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamic branch names from registered branches and existing product records
    val branchNames: StateFlow<List<String>> = combine(allBranches, allProducts) { branches, products ->
        val set = linkedSetOf<String>()
        branches.forEach { if (it.name.isNotBlank()) set.add(it.name) }
        products.forEach { if (it.branch.isNotBlank()) set.add(it.branch) }
        if (set.isEmpty()) {
            listOf("Main Branch", "Downtown Branch", "Warehouse Depot")
        } else {
            set.toList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Activity Log Filters
    private val _activityCategoryFilter = MutableStateFlow("ALL")
    val activityCategoryFilter: StateFlow<String> = _activityCategoryFilter.asStateFlow()

    private val _activityStaffFilter = MutableStateFlow<Long?>(null) // null = all staff
    val activityStaffFilter: StateFlow<Long?> = _activityStaffFilter.asStateFlow()

    private val _activitySearchQuery = MutableStateFlow("")
    val activitySearchQuery: StateFlow<String> = _activitySearchQuery.asStateFlow()

    val filteredActivityLogs: StateFlow<List<ActivityLogEntity>> = combine(
        allActivityLogs,
        _activityCategoryFilter,
        _activityStaffFilter,
        _activitySearchQuery
    ) { logs, catFilter, staffFilter, query ->
        logs.filter { log ->
            val matchesCategory = (catFilter == "ALL" || log.category.equals(catFilter, ignoreCase = true))
            val matchesStaff = (staffFilter == null || log.staffId == staffFilter)
            val matchesQuery = query.isBlank() ||
                    log.details.contains(query, ignoreCase = true) ||
                    log.action.contains(query, ignoreCase = true) ||
                    log.entityId.contains(query, ignoreCase = true) ||
                    log.staffName.contains(query, ignoreCase = true)
            matchesCategory && matchesStaff && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current Cashier / Staff
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _activeBranch = MutableStateFlow<BranchEntity?>(null)
    val activeBranch: StateFlow<BranchEntity?> = _activeBranch.asStateFlow()

    val isCurrentUserAdmin: StateFlow<Boolean> = _currentUser
        .map { it?.role?.equals("ADMIN", ignoreCase = true) == true }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        // Automatically select the default admin or first user once loaded
        viewModelScope.launch {
            repository.allUsers.collect { users ->
                if (_currentUser.value == null && users.isNotEmpty()) {
                    _currentUser.value = users.firstOrNull { it.role == "ADMIN" } ?: users.first()
                }
            }
        }

        // Automatically select the main or active branch for POS & receipt follow-up
        viewModelScope.launch {
            repository.allBranchesList.collect { branches ->
                if (branches.isNotEmpty()) {
                    val current = _activeBranch.value
                    if (current == null) {
                        val mainBranch = branches.firstOrNull { it.isMain } ?: branches.first()
                        _activeBranch.value = mainBranch
                        _taxRate.value = (mainBranch.taxPercent / 100.0).coerceAtLeast(0.0)
                    } else {
                        // Refresh active branch if updated in database
                        branches.firstOrNull { it.id == current.id }?.let { updated ->
                            _activeBranch.value = updated
                        }
                    }
                }
            }
        }
    }

    // POS State
    private val _posSearchQuery = MutableStateFlow("")
    val posSearchQuery: StateFlow<String> = _posSearchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _taxRate = MutableStateFlow(0.08) // 8% default sales tax
    val taxRate: StateFlow<Double> = _taxRate.asStateFlow()

    private val _discountPercent = MutableStateFlow(0.0)
    val discountPercent: StateFlow<Double> = _discountPercent.asStateFlow()

    // Filtered POS products
    val posFilteredProducts: StateFlow<List<ProductEntity>> = combine(
        allProducts,
        _posSearchQuery,
        _selectedCategory
    ) { products, query, cat ->
        products.filter { p ->
            val matchesCategory = (cat == "All" || p.category.equals(cat, ignoreCase = true))
            val matchesQuery = query.isBlank() ||
                    p.name.contains(query, ignoreCase = true) ||
                    p.sku.contains(query, ignoreCase = true) ||
                    p.barcode.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Stock Management Filters
    private val _stockSearchQuery = MutableStateFlow("")
    val stockSearchQuery: StateFlow<String> = _stockSearchQuery.asStateFlow()

    private val _stockFilter = MutableStateFlow(StockFilter.ALL)
    val stockFilter: StateFlow<StockFilter> = _stockFilter.asStateFlow()

    private val _stockCategoryFilter = MutableStateFlow("All")
    val stockCategoryFilter: StateFlow<String> = _stockCategoryFilter.asStateFlow()

    private val _stockBranchFilter = MutableStateFlow("All Branches")
    val stockBranchFilter: StateFlow<String> = _stockBranchFilter.asStateFlow()

    val filteredStockProducts: StateFlow<List<ProductEntity>> = combine(
        allProducts,
        _stockSearchQuery,
        _stockFilter,
        _stockCategoryFilter,
        _stockBranchFilter
    ) { products, query, filter, cat, branch ->
        products.filter { p ->
            val matchesQuery = query.isBlank() ||
                    p.name.contains(query, ignoreCase = true) ||
                    p.sku.contains(query, ignoreCase = true)
            val matchesCategory = (cat == "All" || p.category.equals(cat, ignoreCase = true))
            val matchesBranch = (branch == "All Branches" || p.branch.equals(branch, ignoreCase = true))
            val matchesFilter = when (filter) {
                StockFilter.ALL -> true
                StockFilter.LOW_STOCK -> p.stockQuantity in 1..p.minStockThreshold
                StockFilter.OUT_OF_STOCK -> p.stockQuantity <= 0
            }
            matchesQuery && matchesCategory && matchesBranch && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setStockSearchQuery(query: String) {
        _stockSearchQuery.value = query
    }

    fun setStockFilter(filter: StockFilter) {
        _stockFilter.value = filter
    }

    fun setStockCategoryFilter(category: String) {
        _stockCategoryFilter.value = category
    }

    fun setStockBranchFilter(branch: String) {
        _stockBranchFilter.value = branch
    }

    // Completed Sale / Receipt State
    private val _completedSale = MutableStateFlow<Pair<SaleEntity, List<SaleItemEntity>>?>(null)
    val completedSale: StateFlow<Pair<SaleEntity, List<SaleItemEntity>>?> = _completedSale.asStateFlow()

    // Status Message for user feedback
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Barcode Scanning Feedback State
    private val _lastScannedResult = MutableStateFlow<ScanResult?>(null)
    val lastScannedResult: StateFlow<ScanResult?> = _lastScannedResult.asStateFlow()

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    fun clearLastScannedResult() {
        _lastScannedResult.value = null
    }

    fun scanBarcode(code: String): ScanResult {
        val query = code.trim()
        if (query.isBlank()) {
            val res = ScanResult(success = false, product = null, message = "Empty barcode detected")
            _lastScannedResult.value = res
            return res
        }

        val products = allProducts.value
        // Match priority: exact barcode, exact SKU, then contains
        val matched = products.firstOrNull { it.barcode.equals(query, ignoreCase = true) }
            ?: products.firstOrNull { it.sku.equals(query, ignoreCase = true) }
            ?: products.firstOrNull { it.barcode.isNotBlank() && it.barcode.contains(query, ignoreCase = true) }
            ?: products.firstOrNull { it.sku.contains(query, ignoreCase = true) }

        if (matched == null) {
            val res = ScanResult(success = false, product = null, message = "No product found for code '$query'")
            _lastScannedResult.value = res
            _toastMessage.value = "Unrecognized barcode: $query"
            return res
        }

        if (matched.stockQuantity <= 0) {
            val res = ScanResult(success = false, product = matched, message = "'${matched.name}' is out of stock!")
            _lastScannedResult.value = res
            _toastMessage.value = "'${matched.name}' is out of stock!"
            return res
        }

        val inCartQty = _cart.value.firstOrNull { it.product.id == matched.id }?.quantity ?: 0
        if (inCartQty + 1 > matched.stockQuantity) {
            val res = ScanResult(
                success = false,
                product = matched,
                message = "Maximum stock reached (${matched.stockQuantity} in stock)"
            )
            _lastScannedResult.value = res
            _toastMessage.value = "Only ${matched.stockQuantity} available for '${matched.name}'"
            return res
        }

        addToCart(matched)
        val formattedPrice = String.format(Locale.US, "$%.2f", matched.sellingPrice)
        val newQtyInCart = inCartQty + 1
        val res = ScanResult(
            success = true,
            product = matched,
            message = "Added: ${matched.name} • $formattedPrice ($newQtyInCart in cart)"
        )
        _lastScannedResult.value = res
        return res
    }

    // POS Cart Actions
    fun addToCart(product: ProductEntity) {
        if (product.stockQuantity <= 0) {
            _toastMessage.value = "Item '${product.name}' is out of stock!"
            return
        }
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = current[index]
            if (existing.quantity + 1 > product.stockQuantity) {
                _toastMessage.value = "Only ${product.stockQuantity} units available in stock!"
                return
            }
            current[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            current.add(CartItem(product = product, quantity = 1))
        }
        _cart.value = current
    }

    fun updateCartQuantity(productId: Long, delta: Int) {
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val item = current[index]
            val newQty = item.quantity + delta
            if (newQty <= 0) {
                current.removeAt(index)
            } else {
                if (newQty > item.product.stockQuantity) {
                    _toastMessage.value = "Cannot exceed stock limit (${item.product.stockQuantity})!"
                    return
                }
                current[index] = item.copy(quantity = newQty)
            }
            _cart.value = current
        }
    }

    fun removeFromCart(productId: Long) {
        _cart.value = _cart.value.filter { it.product.id != productId }
    }

    fun clearCart() {
        _cart.value = emptyList()
        _discountPercent.value = 0.0
    }

    fun setDiscountPercent(percent: Double) {
        _discountPercent.value = percent
    }

    fun setPosSearchQuery(query: String) {
        _posSearchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    // Checkout processing
    fun processCheckout(
        paymentMethod: String,
        amountTendered: Double,
        notes: String = ""
    ) {
        val items = _cart.value
        if (items.isEmpty()) return

        val user = _currentUser.value ?: UserEntity(name = "Cashier", role = "CASHIER")
        val subtotal = items.sumOf { it.subtotal }
        val discountAmount = subtotal * (_discountPercent.value / 100.0)
        val afterDiscount = (subtotal - discountAmount).coerceAtLeast(0.0)
        val taxAmount = afterDiscount * _taxRate.value
        val grandTotal = afterDiscount + taxAmount
        val changeGiven = if (amountTendered >= grandTotal) (amountTendered - grandTotal) else 0.0

        val receiptNum = "REC-${SimpleDateFormat("yyMMdd", Locale.US).format(Date())}-${Random.nextInt(1000, 9999)}"

        val activeBr = _activeBranch.value
        val branchName = activeBr?.name ?: "Main Branch"
        val taxPct = _taxRate.value * 100.0
        val receiptGap = activeBr?.receiptGap ?: 12

        val saleEntity = SaleEntity(
            receiptNumber = receiptNum,
            timestamp = System.currentTimeMillis(),
            cashierId = user.id,
            cashierName = user.name,
            subtotal = subtotal,
            taxAmount = taxAmount,
            discountPercent = _discountPercent.value,
            discountAmount = discountAmount,
            totalAmount = grandTotal,
            paymentMethod = paymentMethod,
            amountTendered = amountTendered,
            changeGiven = changeGiven,
            itemsCount = items.sumOf { it.quantity },
            notes = notes,
            branchName = branchName,
            taxPercent = taxPct,
            receiptGap = receiptGap
        )

        val saleItems = items.map { item ->
            SaleItemEntity(
                saleId = 0, // Assigned by repository transaction
                productId = item.product.id,
                productName = item.product.name,
                sku = item.product.sku,
                unitPrice = item.product.sellingPrice,
                costPrice = item.product.costPrice,
                quantity = item.quantity,
                itemTotal = item.subtotal,
                imageUrl = item.product.imageUrl
            )
        }

        viewModelScope.launch {
            val saleId = repository.processSale(saleEntity, saleItems, cashierRole = user.role)
            val completedSaleRecord = saleEntity.copy(id = saleId)
            _completedSale.value = Pair(completedSaleRecord, saleItems)
            clearCart()
            _toastMessage.value = "Sale #$receiptNum completed successfully!"
        }
    }

    fun dismissReceipt() {
        _completedSale.value = null
    }

    fun viewPastReceipt(sale: SaleEntity) {
        viewModelScope.launch {
            val items = repository.getSaleItems(sale.id)
            _completedSale.value = Pair(sale, items)
        }
    }

    // Stock Management actions
    fun adjustStock(
        product: ProductEntity,
        delta: Int,
        type: String,
        reason: String
    ) {
        val user = _currentUser.value
        val operator = user?.name ?: "System User"
        val staffId = user?.id ?: 1L
        val staffRole = user?.role ?: "STAFF"

        viewModelScope.launch {
            repository.adjustStock(
                productId = product.id,
                quantityDelta = delta,
                type = type,
                operatorName = operator,
                reason = reason,
                staffId = staffId,
                staffRole = staffRole
            )
            val actionName = if (delta > 0) "+$delta units added" else "$delta units adjusted"
            _toastMessage.value = "Stock updated: $actionName for ${product.name} (Staff ID: #$staffId)"
        }
    }

    fun addProduct(
        name: String,
        sku: String,
        category: String,
        costPrice: Double,
        sellingPrice: Double,
        initialStock: Int,
        minThreshold: Int,
        unit: String,
        barcode: String = "",
        branch: String = "Main Branch",
        imageUrl: String = ""
    ) {
        val user = _currentUser.value
        val operator = user?.name ?: "Store Manager"
        val staffId = user?.id ?: 1L
        val staffRole = user?.role ?: "ADMIN"

        val newProduct = ProductEntity(
            name = name,
            sku = sku.ifBlank { "SKU-${Random.nextInt(10000, 99999)}" },
            category = category,
            costPrice = costPrice,
            sellingPrice = sellingPrice,
            stockQuantity = initialStock,
            minStockThreshold = minThreshold,
            unit = unit,
            barcode = barcode.trim(),
            branch = branch.trim().ifBlank { "Main Branch" },
            imageUrl = imageUrl.trim()
        )
        viewModelScope.launch {
            repository.insertProduct(
                product = newProduct,
                operatorName = operator,
                staffId = staffId,
                staffRole = staffRole
            )
            _toastMessage.value = "Product '$name' created in $branch with $initialStock $unit in stock"
        }
    }

    fun updateProduct(product: ProductEntity) {
        val user = _currentUser.value
        val isAdmin = user?.role?.equals("ADMIN", ignoreCase = true) == true || isCurrentUserAdmin.value
        if (!isAdmin) {
            _toastMessage.value = "Action restricted: Only Admin users can edit items"
            return
        }
        val operator = user?.name ?: "Admin"
        val staffId = user?.id ?: 1L
        val staffRole = user?.role ?: "ADMIN"

        viewModelScope.launch {
            repository.updateProduct(
                product = product,
                operatorName = operator,
                staffId = staffId,
                staffRole = staffRole
            )
            _toastMessage.value = "Product '${product.name}' updated"
        }
    }

    fun deleteProduct(product: ProductEntity) {
        val user = _currentUser.value
        val isAdmin = user?.role?.equals("ADMIN", ignoreCase = true) == true || isCurrentUserAdmin.value
        if (!isAdmin) {
            _toastMessage.value = "Action restricted: Only Admin users can delete items"
            return
        }
        val operator = user?.name ?: "Admin"
        val staffId = user?.id ?: 1L
        val staffRole = user?.role ?: "ADMIN"

        viewModelScope.launch {
            repository.deleteProduct(
                product = product,
                operatorName = operator,
                staffId = staffId,
                staffRole = staffRole
            )
            _toastMessage.value = "Product '${product.name}' removed"
        }
    }

    // Category Management
    fun addCategory(name: String, description: String = "", colorHex: String = "#0D9488") {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        val user = _currentUser.value
        val operator = user?.name ?: "Store Manager"
        val staffId = user?.id ?: 1L
        viewModelScope.launch {
            val cat = CategoryEntity(
                name = trimmed,
                description = description.trim(),
                colorHex = colorHex
            )
            repository.insertCategory(cat)
            _toastMessage.value = "Category '$trimmed' created"
        }
    }

    fun editCategory(category: CategoryEntity, newName: String, newDescription: String = "", newColorHex: String = "#0D9488") {
        val trimmedNew = newName.trim()
        if (trimmedNew.isBlank()) return
        val user = _currentUser.value
        val operator = user?.name ?: "Store Manager"
        val staffId = user?.id ?: 1L
        viewModelScope.launch {
            val updated = category.copy(
                name = trimmedNew,
                description = newDescription.trim(),
                colorHex = newColorHex
            )
            repository.updateCategory(
                oldName = category.name,
                category = updated,
                operatorName = operator,
                staffId = staffId
            )
            _toastMessage.value = "Category updated: '${category.name}' -> '$trimmedNew'"
        }
    }

    fun deleteCategory(category: CategoryEntity, fallbackCategory: String = "General") {
        val user = _currentUser.value
        val operator = user?.name ?: "Store Manager"
        val staffId = user?.id ?: 1L
        viewModelScope.launch {
            repository.deleteCategory(
                category = category,
                fallbackCategory = fallbackCategory,
                operatorName = operator,
                staffId = staffId
            )
            _toastMessage.value = "Category '${category.name}' deleted (reassigned to '$fallbackCategory')"
        }
    }

    // Branch Management
    fun addBranch(name: String, code: String = "", address: String = "", phone: String = "", isMain: Boolean = false) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        val user = _currentUser.value
        val operator = user?.name ?: "Store Manager"
        val staffId = user?.id ?: 1L
        viewModelScope.launch {
            val branch = BranchEntity(
                name = trimmed,
                code = code.trim().ifBlank { "BR-${Random.nextInt(10, 99)}" },
                address = address.trim(),
                phone = phone.trim(),
                isMain = isMain
            )
            repository.insertBranch(branch)
            _toastMessage.value = "Branch '$trimmed' created"
        }
    }

    fun editBranch(branch: BranchEntity, newName: String, newCode: String = "", newAddress: String = "", newPhone: String = "", isMain: Boolean = false) {
        val trimmedNew = newName.trim()
        if (trimmedNew.isBlank()) return
        val user = _currentUser.value
        val operator = user?.name ?: "Store Manager"
        val staffId = user?.id ?: 1L
        viewModelScope.launch {
            val updated = branch.copy(
                name = trimmedNew,
                code = newCode.trim(),
                address = newAddress.trim(),
                phone = newPhone.trim(),
                isMain = isMain
            )
            repository.updateBranch(
                oldName = branch.name,
                branch = updated,
                operatorName = operator,
                staffId = staffId
            )
            _toastMessage.value = "Branch updated: '${branch.name}' -> '$trimmedNew'"
        }
    }

    fun deleteBranch(branch: BranchEntity, fallbackBranch: String = "Main Branch") {
        val user = _currentUser.value
        val operator = user?.name ?: "Store Manager"
        val staffId = user?.id ?: 1L
        viewModelScope.launch {
            repository.deleteBranch(
                branch = branch,
                fallbackBranch = fallbackBranch,
                operatorName = operator,
                staffId = staffId
            )
            _toastMessage.value = "Branch '${branch.name}' deleted (reassigned to '$fallbackBranch')"
        }
    }

    // Branch Receipt & Tax Customization
    fun setActiveBranch(branch: BranchEntity) {
        _activeBranch.value = branch
        _taxRate.value = (branch.taxPercent / 100.0).coerceAtLeast(0.0)
        _toastMessage.value = "Active POS branch: ${branch.name} (Tax: ${branch.taxPercent}%)"
    }

    fun setTaxPercent(percent: Double, updateBranchDefault: Boolean = false) {
        val sanitized = percent.coerceIn(0.0, 100.0)
        _taxRate.value = sanitized / 100.0
        if (updateBranchDefault) {
            val branch = _activeBranch.value
            if (branch != null) {
                viewModelScope.launch {
                    val updated = branch.copy(taxPercent = sanitized)
                    val user = _currentUser.value
                    val operator = user?.name ?: "Store Manager"
                    val staffId = user?.id ?: 1L
                    repository.updateBranchReceiptConfig(updated, operator, staffId)
                    _activeBranch.value = updated
                    _toastMessage.value = "Default tax for ${branch.name} set to ${sanitized}%"
                }
                return
            }
        }
        _toastMessage.value = "Tax rate set to ${sanitized}%"
    }

    fun setTaxRate(rate: Double) {
        _taxRate.value = rate.coerceAtLeast(0.0)
    }

    fun updateBranchReceiptConfig(
        branch: BranchEntity,
        receiptHeader: String,
        receiptSubtitle: String,
        receiptVatTin: String,
        address: String,
        phone: String,
        receiptFooter: String,
        taxPercent: Double,
        receiptGap: Int,
        setAsActive: Boolean = false
    ) {
        val user = _currentUser.value
        val operator = user?.name ?: "Store Manager"
        val staffId = user?.id ?: 1L
        viewModelScope.launch {
            val updated = branch.copy(
                receiptHeader = receiptHeader.trim().ifBlank { "TR COFFEE • ${branch.name}" },
                receiptSubtitle = receiptSubtitle.trim().ifBlank { "Official Sales Receipt & Tax Invoice" },
                receiptVatTin = receiptVatTin.trim(),
                address = address.trim(),
                phone = phone.trim(),
                receiptFooter = receiptFooter.trim(),
                taxPercent = taxPercent.coerceIn(0.0, 100.0),
                receiptGap = receiptGap.coerceIn(4, 40)
            )
            repository.updateBranchReceiptConfig(updated, operator, staffId)
            if (setAsActive || _activeBranch.value?.id == branch.id) {
                _activeBranch.value = updated
                _taxRate.value = updated.taxPercent / 100.0
            }
            _toastMessage.value = "Receipt & tax settings saved for '${branch.name}'"
        }
    }

    // User / Staff Management
    fun switchUserByPin(pin: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val matchedUser = repository.getUserByPin(pin)
            if (matchedUser != null) {
                _currentUser.value = matchedUser
                _toastMessage.value = "Switched user to ${matchedUser.name} (${matchedUser.role})"
                logUserSwitchActivity(matchedUser)
                onSuccess()
            } else {
                onError("Invalid PIN code. Try again.")
            }
        }
    }

    suspend fun verifyAdminPin(pin: String): UserEntity? {
        val matchedUser = repository.getUserByPin(pin)
        return if (matchedUser != null && matchedUser.role.equals("ADMIN", ignoreCase = true)) {
            val auditLog = ActivityLogEntity.createSecureLog(
                staffId = matchedUser.id,
                staffName = matchedUser.name,
                staffRole = matchedUser.role,
                category = ActivityCategory.STAFF_SECURITY,
                action = "ADMIN_BAKONG_AUTH",
                entityType = "PAYMENT",
                entityId = "BAKONG-AUTH-${System.currentTimeMillis()}",
                details = "Admin ${matchedUser.name} verified credentials to expose Bakong KHQR payment controls",
                metadataJson = """{"adminId":${matchedUser.id},"authorizedMethod":"KHQR"}"""
            )
            repository.logActivity(auditLog)
            matchedUser
        } else {
            null
        }
    }

    fun setCurrentUser(user: UserEntity) {
        _currentUser.value = user
        _toastMessage.value = "Switched to ${user.name} (${user.role})"
        logUserSwitchActivity(user)
    }

    private fun logUserSwitchActivity(user: UserEntity) {
        viewModelScope.launch {
            val auditLog = ActivityLogEntity.createSecureLog(
                staffId = user.id,
                staffName = user.name,
                staffRole = user.role,
                category = ActivityCategory.STAFF_SECURITY,
                action = "STAFF_SESSION_SWITCHED",
                entityType = "STAFF",
                entityId = "STAFF-${user.id}",
                details = "Active POS terminal operator switched to ${user.name} (${user.role}, Staff ID #${user.id})",
                metadataJson = """{"staffId":${user.id},"name":"${user.name}","role":"${user.role}"}"""
            )
            repository.logActivity(auditLog)
        }
    }

    // Activity Log Management
    fun setActivityCategoryFilter(category: String) {
        _activityCategoryFilter.value = category
    }

    fun setActivityStaffFilter(staffId: Long?) {
        _activityStaffFilter.value = staffId
    }

    fun setActivitySearchQuery(query: String) {
        _activitySearchQuery.value = query
    }

    data class IntegrityVerificationResult(
        val totalLogs: Int,
        val verifiedLogs: Int,
        val corruptedLogs: Int,
        val isAllValid: Boolean
    )

    fun verifyAllLogsIntegrity(): IntegrityVerificationResult {
        val logs = allActivityLogs.value
        var verified = 0
        var corrupted = 0
        for (log in logs) {
            if (log.verifyIntegrity()) {
                verified++
            } else {
                corrupted++
            }
        }
        return IntegrityVerificationResult(
            totalLogs = logs.size,
            verifiedLogs = verified,
            corruptedLogs = corrupted,
            isAllValid = corrupted == 0
        )
    }

    fun addUser(name: String, role: String, pin: String, email: String) {
        val newUser = UserEntity(
            name = name,
            role = role,
            pin = pin,
            email = email
        )
        viewModelScope.launch {
            repository.insertUser(newUser)
            _toastMessage.value = "Staff member '$name' added ($role)"
        }
    }

    fun updateUser(user: UserEntity) {
        viewModelScope.launch {
            repository.updateUser(user)
            _toastMessage.value = "User '${user.name}' updated"
        }
    }

    fun deleteUser(user: UserEntity) {
        if (_currentUser.value?.id == user.id) {
            _toastMessage.value = "Cannot delete currently active user!"
            return
        }
        viewModelScope.launch {
            repository.deleteUser(user)
            _toastMessage.value = "Staff member '${user.name}' removed"
        }
    }

    // ==========================================
    // Client Profiles State & Operations
    // ==========================================
    val allClients: StateFlow<List<ClientEntity>> = repository.allClients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _clientSearchQuery = MutableStateFlow("")
    val clientSearchQuery: StateFlow<String> = _clientSearchQuery.asStateFlow()

    private val _clientTierFilter = MutableStateFlow("ALL")
    val clientTierFilter: StateFlow<String> = _clientTierFilter.asStateFlow()

    private val _selectedClient = MutableStateFlow<ClientEntity?>(null)
    val selectedClient: StateFlow<ClientEntity?> = _selectedClient.asStateFlow()

    val filteredClients: StateFlow<List<ClientEntity>> = combine(
        allClients,
        _clientSearchQuery,
        _clientTierFilter
    ) { clients, query, tier ->
        clients.filter { client ->
            val matchesQuery = query.isBlank() ||
                    client.name.contains(query, ignoreCase = true) ||
                    client.phone.contains(query, ignoreCase = true) ||
                    client.email.contains(query, ignoreCase = true) ||
                    client.favoriteOrder.contains(query, ignoreCase = true)

            val matchesTier = tier == "ALL" || client.tier.equals(tier, ignoreCase = true)

            matchesQuery && matchesTier
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setClientSearchQuery(query: String) {
        _clientSearchQuery.value = query
    }

    fun setClientTierFilter(tier: String) {
        _clientTierFilter.value = tier
    }

    fun selectClient(client: ClientEntity?) {
        _selectedClient.value = client
    }

    fun createClient(
        name: String,
        phone: String,
        email: String = "",
        tier: String = "BRONZE",
        initialPoints: Int = 0,
        favoriteOrder: String = "",
        notes: String = "",
        address: String = ""
    ) {
        val operator = _currentUser.value?.name ?: "Staff"
        val staffId = _currentUser.value?.id ?: 1L
        viewModelScope.launch {
            val newClient = ClientEntity(
                name = name.trim(),
                phone = phone.trim(),
                email = email.trim(),
                tier = tier,
                loyaltyPoints = initialPoints,
                favoriteOrder = favoriteOrder.trim(),
                notes = notes.trim(),
                address = address.trim()
            )
            repository.insertClient(newClient, operator, staffId)
            _toastMessage.value = "Client profile '${newClient.name}' created!"
        }
    }

    fun updateClient(client: ClientEntity) {
        val operator = _currentUser.value?.name ?: "Staff"
        val staffId = _currentUser.value?.id ?: 1L
        viewModelScope.launch {
            repository.updateClient(client, operator, staffId)
            if (_selectedClient.value?.id == client.id) {
                _selectedClient.value = client
            }
            _toastMessage.value = "Client '${client.name}' profile updated"
        }
    }

    fun deleteClient(client: ClientEntity) {
        val operator = _currentUser.value?.name ?: "Staff"
        val staffId = _currentUser.value?.id ?: 1L
        viewModelScope.launch {
            repository.deleteClient(client, operator, staffId)
            if (_selectedClient.value?.id == client.id) {
                _selectedClient.value = null
            }
            _toastMessage.value = "Client '${client.name}' deleted"
        }
    }

    fun adjustClientPoints(client: ClientEntity, delta: Int, reason: String) {
        val operator = _currentUser.value?.name ?: "Staff"
        val staffId = _currentUser.value?.id ?: 1L
        viewModelScope.launch {
            repository.adjustClientPoints(client, delta, reason, operator, staffId)
            val updated = client.copy(
                loyaltyPoints = (client.loyaltyPoints + delta).coerceAtLeast(0),
                updatedAt = System.currentTimeMillis()
            )
            if (_selectedClient.value?.id == client.id) {
                _selectedClient.value = updated
            }
            val action = if (delta >= 0) "added" else "redeemed"
            _toastMessage.value = "${if (delta >= 0) "+$delta" else "$delta"} points $action for ${client.name}!"
        }
    }
}
