package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.ActivityCategory
import com.example.data.model.ActivityLogEntity
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

    init {
        // Automatically select the default admin or first user once loaded
        viewModelScope.launch {
            repository.allUsers.collect { users ->
                if (_currentUser.value == null && users.isNotEmpty()) {
                    _currentUser.value = users.firstOrNull { it.role == "ADMIN" } ?: users.first()
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

    val filteredStockProducts: StateFlow<List<ProductEntity>> = combine(
        allProducts,
        _stockSearchQuery,
        _stockFilter,
        _stockCategoryFilter
    ) { products, query, filter, cat ->
        products.filter { p ->
            val matchesQuery = query.isBlank() ||
                    p.name.contains(query, ignoreCase = true) ||
                    p.sku.contains(query, ignoreCase = true)
            val matchesCategory = (cat == "All" || p.category.equals(cat, ignoreCase = true))
            val matchesFilter = when (filter) {
                StockFilter.ALL -> true
                StockFilter.LOW_STOCK -> p.stockQuantity in 1..p.minStockThreshold
                StockFilter.OUT_OF_STOCK -> p.stockQuantity <= 0
            }
            matchesQuery && matchesCategory && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun setStockSearchQuery(query: String) {
        _stockSearchQuery.value = query
    }

    fun setStockFilter(filter: StockFilter) {
        _stockFilter.value = filter
    }

    fun setStockCategoryFilter(category: String) {
        _stockCategoryFilter.value = category
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
            notes = notes
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
                itemTotal = item.subtotal
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
        barcode: String = ""
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
            barcode = barcode.trim()
        )
        viewModelScope.launch {
            repository.insertProduct(
                product = newProduct,
                operatorName = operator,
                staffId = staffId,
                staffRole = staffRole
            )
            _toastMessage.value = "Product '$name' created with $initialStock $unit in stock"
        }
    }

    fun updateProduct(product: ProductEntity) {
        val user = _currentUser.value
        val operator = user?.name ?: "Staff"
        val staffId = user?.id ?: 1L
        val staffRole = user?.role ?: "STAFF"

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
        val operator = user?.name ?: "Staff"
        val staffId = user?.id ?: 1L
        val staffRole = user?.role ?: "STAFF"

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
}
