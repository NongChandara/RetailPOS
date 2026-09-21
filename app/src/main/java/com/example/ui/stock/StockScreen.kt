package com.example.ui.stock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ProductEntity
import com.example.ui.MainViewModel
import com.example.ui.StockFilter
import com.example.ui.components.StockBadge
import com.example.ui.components.formatCurrency
import com.example.ui.theme.RetailSlate100
import com.example.ui.theme.RetailSlate300
import com.example.ui.theme.RetailSlate500
import com.example.ui.theme.RetailSlate700
import com.example.ui.theme.RetailSlate900
import com.example.ui.theme.RetailStatusAmber
import com.example.ui.theme.RetailStatusAmberContainer
import com.example.ui.theme.RetailStatusGreen
import com.example.ui.theme.RetailStatusGreenContainer
import com.example.ui.theme.RetailStatusRed
import com.example.ui.theme.RetailStatusRedContainer
import com.example.ui.theme.RetailTealPrimary

@Composable
fun StockScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val allProducts by viewModel.allProducts.collectAsState()
    val filteredProducts by viewModel.filteredStockProducts.collectAsState()
    val searchQuery by viewModel.stockSearchQuery.collectAsState()
    val stockFilter by viewModel.stockFilter.collectAsState()
    val categoryFilter by viewModel.stockCategoryFilter.collectAsState()

    var productToAdjust by remember { mutableStateOf<ProductEntity?>(null) }
    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val totalSkus = allProducts.size
    val totalUnits = allProducts.sumOf { it.stockQuantity }
    val lowStockCount = allProducts.count { it.stockQuantity in 1..it.minStockThreshold }
    val outOfStockCount = allProducts.count { it.stockQuantity <= 0 }

    val categories = listOf("All", "Beverages", "Bakery", "Snacks", "Electronics", "Home & Goods", "Personal Care", "Apparel")

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 72.dp)
        ) {
            // KPI Summary Cards
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    StockMetricCard(
                        title = "Total SKUs",
                        value = "$totalSkus",
                        icon = Icons.Filled.Inventory2,
                        containerColor = RetailSlate100,
                        accentColor = RetailSlate900
                    )
                }
                item {
                    StockMetricCard(
                        title = "Total Stock",
                        value = "$totalUnits",
                        icon = Icons.Filled.TrendingUp,
                        containerColor = RetailStatusGreenContainer,
                        accentColor = RetailStatusGreen
                    )
                }
                item {
                    StockMetricCard(
                        title = "Low Stock",
                        value = "$lowStockCount",
                        icon = Icons.Filled.Warning,
                        containerColor = RetailStatusAmberContainer,
                        accentColor = RetailStatusAmber
                    )
                }
                item {
                    StockMetricCard(
                        title = "Out of Stock",
                        value = "$outOfStockCount",
                        icon = Icons.Filled.TrendingDown,
                        containerColor = RetailStatusRedContainer,
                        accentColor = RetailStatusRed
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setStockSearchQuery(it) },
                placeholder = { Text("Filter products by name or SKU...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = RetailSlate500) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setStockSearchQuery("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("stock_search_field")
            )

            // Stock Status Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = stockFilter == StockFilter.ALL,
                    onClick = { viewModel.setStockFilter(StockFilter.ALL) },
                    label = { Text("All ($totalSkus)") },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RetailTealPrimary,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = stockFilter == StockFilter.LOW_STOCK,
                    onClick = { viewModel.setStockFilter(StockFilter.LOW_STOCK) },
                    label = { Text("Low Stock ($lowStockCount)") },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RetailStatusAmber,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = stockFilter == StockFilter.OUT_OF_STOCK,
                    onClick = { viewModel.setStockFilter(StockFilter.OUT_OF_STOCK) },
                    label = { Text("Out of Stock ($outOfStockCount)") },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RetailStatusRed,
                        selectedLabelColor = Color.White
                    )
                )
            }

            // Category scroll row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = categoryFilter.equals(cat, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setStockCategoryFilter(cat) },
                        label = { Text(cat, fontSize = 12.sp) },
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            // Products Inventory List
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No inventory items found", color = RetailSlate500)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        StockProductCard(
                            product = product,
                            onAdjust = { productToAdjust = product },
                            onEdit = { productToEdit = product },
                            onDelete = { viewModel.deleteProduct(product) }
                        )
                    }
                }
            }
        }

        // Add Product Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = RetailTealPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_product_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Product")
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Product", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }

    // Adjust Stock Dialog
    productToAdjust?.let { product ->
        AdjustStockDialog(
            product = product,
            onDismiss = { productToAdjust = null },
            onConfirm = { delta, type, reason ->
                viewModel.adjustStock(product, delta, type, reason)
                productToAdjust = null
            }
        )
    }

    // Add or Edit Product Dialog
    if (showAddDialog) {
        AddEditProductDialog(
            product = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, sku, category, cost, price, stock, threshold, unit ->
                viewModel.addProduct(name, sku, category, cost, price, stock, threshold, unit)
                showAddDialog = false
            }
        )
    }

    productToEdit?.let { product ->
        AddEditProductDialog(
            product = product,
            onDismiss = { productToEdit = null },
            onSave = { name, sku, category, cost, price, stock, threshold, unit ->
                viewModel.updateProduct(
                    product.copy(
                        name = name,
                        sku = sku,
                        category = category,
                        costPrice = cost,
                        sellingPrice = price,
                        stockQuantity = stock,
                        minStockThreshold = threshold,
                        unit = unit,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                productToEdit = null
            }
        )
    }
}

@Composable
fun StockMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    accentColor: Color
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.width(130.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 12.sp, color = RetailSlate700, fontWeight = FontWeight.Medium)
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = accentColor)
        }
    }
}

@Composable
fun StockProductCard(
    product: ProductEntity,
    onAdjust: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val marginPct = if (product.sellingPrice > 0) {
        ((product.sellingPrice - product.costPrice) / product.sellingPrice) * 100.0
    } else 0.0

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().testTag("stock_item_${product.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = RetailSlate900
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "SKU: ${product.sku} • Category: ${product.category}",
                        fontSize = 12.sp,
                        color = RetailSlate500
                    )
                }
                StockBadge(quantity = product.stockQuantity, threshold = product.minStockThreshold)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = RetailSlate100)

            // Pricing & Margin breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Cost Price", fontSize = 11.sp, color = RetailSlate500)
                    Text(formatCurrency(product.costPrice), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Column {
                    Text("Retail Price", fontSize = 11.sp, color = RetailSlate500)
                    Text(formatCurrency(product.sellingPrice), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RetailTealPrimary)
                }
                Column {
                    Text("Margin", fontSize = 11.sp, color = RetailSlate500)
                    Text(
                        String.format("%.1f%%", marginPct),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (marginPct >= 30) Color(0xFF16A34A) else RetailSlate700
                    )
                }
                Column {
                    Text("Inventory Value", fontSize = 11.sp, color = RetailSlate500)
                    Text(
                        formatCurrency(product.costPrice * product.stockQuantity),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = RetailSlate700, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(4.dp))
                Button(
                    onClick = onAdjust,
                    colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp).testTag("adjust_stock_btn_${product.id}")
                ) {
                    Text("Restock / Adjust", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdjustStockDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onConfirm: (Int, String, String) -> Unit
) {
    var adjustType by remember { mutableStateOf("RESTOCK") }
    var quantityText by remember { mutableStateOf("10") }
    var reasonText by remember { mutableStateOf("Supplier delivery intake") }

    val qty = quantityText.toIntOrNull() ?: 0
    val delta = when (adjustType) {
        "RESTOCK", "RETURN" -> qty
        else -> -qty // DAMAGE, WRITE-OFF
    }
    val previewResultingStock = (product.stockQuantity + delta).coerceAtLeast(0)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Adjust Stock: ${product.name}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = RetailSlate900
                )

                Surface(
                    color = RetailSlate100,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Current Stock: ${product.stockQuantity} ${product.unit}", fontWeight = FontWeight.SemiBold)
                        Text(
                            "New: $previewResultingStock ${product.unit}",
                            fontWeight = FontWeight.Bold,
                            color = RetailTealPrimary
                        )
                    }
                }

                Text("Action Type", fontSize = 12.sp, color = RetailSlate500, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        Pair("RESTOCK", "Restock (+)"),
                        Pair("DAMAGE", "Damaged (-)"),
                        Pair("RETURN", "Return (+)"),
                        Pair("AUDIT", "Audit (-)")
                    ).forEach { (type, label) ->
                        val isSelected = adjustType == type
                        OutlinedButton(
                            onClick = {
                                adjustType = type
                                reasonText = when (type) {
                                    "RESTOCK" -> "Supplier delivery intake"
                                    "DAMAGE" -> "Damaged / expired item disposal"
                                    "RETURN" -> "Customer product return"
                                    else -> "Inventory stock count audit"
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) RetailTealPrimary else Color.Transparent,
                                contentColor = if (isSelected) Color.White else RetailSlate700
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
                        ) {
                            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Quantity (${product.unit})") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reasonText,
                    onValueChange = { reasonText = it },
                    label = { Text("Reason / Note") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(delta, adjustType, reasonText) },
                        enabled = qty > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("confirm_stock_adjust_btn")
                    ) {
                        Text("Apply Update")
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditProductDialog(
    product: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Double, Double, Int, Int, String) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var sku by remember { mutableStateOf(product?.sku ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "Beverages") }
    var costPriceText by remember { mutableStateOf(product?.costPrice?.toString() ?: "1.50") }
    var sellingPriceText by remember { mutableStateOf(product?.sellingPrice?.toString() ?: "3.50") }
    var stockText by remember { mutableStateOf(product?.stockQuantity?.toString() ?: "20") }
    var thresholdText by remember { mutableStateOf(product?.minStockThreshold?.toString() ?: "5") }
    var unit by remember { mutableStateOf(product?.unit ?: "pcs") }

    val categories = listOf("Beverages", "Bakery", "Snacks", "Electronics", "Home & Goods", "Personal Care", "Apparel")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (product == null) "Add New Product" else "Edit Product",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = RetailSlate900
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("product_name_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("SKU / Barcode") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit (pcs/kg/can)") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Category selector chips
                Text("Category", fontSize = 12.sp, color = RetailSlate500)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = costPriceText,
                        onValueChange = { costPriceText = it },
                        label = { Text("Cost Price ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sellingPriceText,
                        onValueChange = { sellingPriceText = it },
                        label = { Text("Sell Price ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { stockText = it },
                        label = { Text("Initial Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = thresholdText,
                        onValueChange = { thresholdText = it },
                        label = { Text("Low Alert Min") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val cost = costPriceText.toDoubleOrNull() ?: 0.0
                            val price = sellingPriceText.toDoubleOrNull() ?: 0.0
                            val stock = stockText.toIntOrNull() ?: 0
                            val threshold = thresholdText.toIntOrNull() ?: 5
                            onSave(name, sku, category, cost, price, stock, threshold, unit)
                        },
                        enabled = name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("save_product_btn")
                    ) {
                        Text(if (product == null) "Create Product" else "Save Changes")
                    }
                }
            }
        }
    }
}
