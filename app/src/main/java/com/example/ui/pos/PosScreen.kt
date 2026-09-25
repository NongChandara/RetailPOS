package com.example.ui.pos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.ProductThumbnail
import com.example.ui.receipt.DigitalReceiptScreen
import java.io.File
import java.util.Locale
import com.example.data.model.ProductEntity
import com.example.data.model.SaleEntity
import com.example.data.model.SaleItemEntity
import com.example.data.model.UserEntity
import com.example.data.model.BranchEntity
import com.example.ui.CartItem
import com.example.ui.MainViewModel
import kotlinx.coroutines.launch
import com.example.ui.components.BranchReceiptAndTaxDialog
import com.example.ui.components.EditTaxPercentDialog
import com.example.ui.components.StockBadge
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDateTime
import com.example.ui.scanner.BarcodeScannerBottomSheet
import com.example.util.CurrencyMode
import com.example.util.CurrencyUtils
import com.example.util.PdfReceiptGenerator
import com.example.ui.theme.RetailSlate100
import com.example.ui.theme.RetailSlate300
import com.example.ui.theme.RetailSlate500
import com.example.ui.theme.RetailSlate700
import com.example.ui.theme.RetailSlate900
import com.example.ui.theme.RetailStatusGreen
import com.example.ui.theme.RetailTealLight
import com.example.ui.theme.RetailTealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.posFilteredProducts.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val searchQuery by viewModel.posSearchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val taxRate by viewModel.taxRate.collectAsState()
    val discountPercent by viewModel.discountPercent.collectAsState()
    val completedSale by viewModel.completedSale.collectAsState()
    val khrRate by viewModel.khrExchangeRate.collectAsState()
    val currencyMode by viewModel.currencyMode.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val activeBranch by viewModel.activeBranch.collectAsState()
    val allBranches by viewModel.allBranches.collectAsState()

    var showCartSheet by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var showBranchReceiptDialog by remember { mutableStateOf(false) }
    var showEditTaxDialog by remember { mutableStateOf(false) }

    val totalItems = cart.sumOf { it.quantity }
    val subtotal = cart.sumOf { it.subtotal }
    val discountAmount = subtotal * (discountPercent / 100.0)
    val afterDiscount = (subtotal - discountAmount).coerceAtLeast(0.0)
    val taxAmount = afterDiscount * taxRate
    val grandTotal = afterDiscount + taxAmount

    val categoryNames by viewModel.categoryNames.collectAsState()
    val categories = remember(categoryNames) {
        listOf("All") + categoryNames.filter { it.isNotBlank() }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (cart.isNotEmpty()) 80.dp else 0.dp)
        ) {
            // Search and Barcode Scan Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setPosSearchQuery(it) },
                    placeholder = { Text("Search name, SKU or barcode...") },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = "Search", tint = RetailSlate500)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setPosSearchQuery("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pos_search_field")
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { showBarcodeScanner = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RetailTealPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                    modifier = Modifier
                        .height(56.dp)
                        .testTag("pos_scan_barcode_button")
                ) {
                    Icon(
                        Icons.Filled.QrCodeScanner,
                        contentDescription = "Scan Barcode",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // Active Branch & Tax Configuration Banner
            Surface(
                color = RetailSlate100,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .clickable { showBranchReceiptDialog = true }
                    .testTag("pos_branch_tax_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Storefront,
                            contentDescription = null,
                            tint = RetailTealPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = activeBranch?.name ?: "Main Branch",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RetailSlate900
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val currentTaxPct = String.format(Locale.US, "%.1f", taxRate * 100).removeSuffix(".0")
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Tax: $currentTaxPct%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showBranchReceiptDialog = true }
                    ) {
                        Text(
                            text = "Receipt & Tax",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RetailTealPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Filled.Tune,
                            contentDescription = null,
                            tint = RetailTealPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Category Chips Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory.equals(category, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setSelectedCategory(category) },
                        label = { Text(category, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RetailTealPrimary,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
                }
            }

            // Products Catalog List
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No products found matching '$searchQuery'",
                        color = RetailSlate500,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(products, key = { it.id }) { product ->
                        PosProductRow(
                            product = product,
                            onAddToCart = { viewModel.addToCart(product) }
                        )
                    }
                }
            }
        }

        // Floating Cart Bottom Bar
        if (cart.isNotEmpty()) {
            Surface(
                color = RetailSlate900,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .testTag("pos_cart_bottom_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { showCartSheet = true }
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(RetailTealPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.ShoppingCart,
                                contentDescription = "Cart",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "$totalItems ${if (totalItems == 1) "item" else "items"} in cart",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Total: ${formatCurrency(grandTotal)}",
                                color = RetailTealLight,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Row {
                        OutlinedButton(
                            onClick = { showCartSheet = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(RetailSlate500)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("view_cart_button")
                        ) {
                            Text("View Cart")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { showCheckoutDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("checkout_button")
                        ) {
                            Text("Pay", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quick Barcode Scan Floating Action Button
        FloatingActionButton(
            onClick = { showBarcodeScanner = true },
            containerColor = RetailTealPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 16.dp,
                    bottom = if (cart.isNotEmpty()) 86.dp else 16.dp
                )
                .testTag("pos_barcode_scan_fab")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Icon(
                    Icons.Filled.QrCodeScanner,
                    contentDescription = "Scan Barcode",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Scan Item", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    // Barcode Scanner Bottom Sheet
    if (showBarcodeScanner) {
        BarcodeScannerBottomSheet(
            viewModel = viewModel,
            onDismiss = { showBarcodeScanner = false },
            onCheckoutClicked = {
                showBarcodeScanner = false
                showCartSheet = true
            }
        )
    }

    // Cart Modal Bottom Sheet
    if (showCartSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCartSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            CartSheetContent(
                cart = cart,
                subtotal = subtotal,
                discountPercent = discountPercent,
                discountAmount = discountAmount,
                taxRate = taxRate,
                taxAmount = taxAmount,
                grandTotal = grandTotal,
                onEditTaxClicked = { showEditTaxDialog = true },
                onUpdateQty = { id, delta -> viewModel.updateCartQuantity(id, delta) },
                onRemoveItem = { id -> viewModel.removeFromCart(id) },
                onSetDiscount = { pct -> viewModel.setDiscountPercent(pct) },
                onScanMore = {
                    showCartSheet = false
                    showBarcodeScanner = true
                },
                onClearCart = {
                    viewModel.clearCart()
                    showCartSheet = false
                },
                onProceedToCheckout = {
                    showCartSheet = false
                    showCheckoutDialog = true
                }
            )
        }
    }

    // Checkout Dialog
    if (showCheckoutDialog) {
        CheckoutDialog(
            grandTotal = grandTotal,
            khrRate = khrRate,
            currentUser = currentUser,
            onVerifyAdminPin = { pin -> viewModel.verifyAdminPin(pin) },
            onDismiss = { showCheckoutDialog = false },
            onConfirmPayment = { method, tendered ->
                viewModel.processCheckout(method, tendered)
                showCheckoutDialog = false
            }
        )
    }

    // Digital Receipt Dialog
    completedSale?.let { (sale, items) ->
        val saleBranch = allBranches.firstOrNull { it.name.equals(sale.branchName, ignoreCase = true) } ?: activeBranch
        ReceiptDialog(
            sale = sale,
            items = items,
            khrRate = khrRate,
            branch = saleBranch,
            onEditReceiptConfig = { showBranchReceiptDialog = true },
            onDismiss = { viewModel.dismissReceipt() }
        )
    }

    // Branch Receipt & Tax Settings Dialog
    if (showBranchReceiptDialog) {
        BranchReceiptAndTaxDialog(
            branches = allBranches,
            activeBranch = activeBranch,
            initialBranch = activeBranch,
            onDismiss = { showBranchReceiptDialog = false },
            onSaveBranchReceipt = { branch, header, subtitle, vatTin, addr, phone, footer, taxPct, gap, setAsActive ->
                viewModel.updateBranchReceiptConfig(branch, header, subtitle, vatTin, addr, phone, footer, taxPct, gap, setAsActive)
            },
            onSetActiveBranch = { branch ->
                viewModel.setActiveBranch(branch)
            }
        )
    }

    // Quick Tax Percent Dialog
    if (showEditTaxDialog) {
        EditTaxPercentDialog(
            currentTaxPercent = taxRate * 100.0,
            cartSubtotal = afterDiscount,
            onDismiss = { showEditTaxDialog = false },
            onApplyTaxPercent = { newTaxPct, updateBranchDefault ->
                viewModel.setTaxPercent(newTaxPct, updateBranchDefault)
            }
        )
    }
}

@Composable
fun PosProductRow(
    product: ProductEntity,
    onAddToCart: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = product.stockQuantity > 0, onClick = onAddToCart)
            .testTag("product_row_${product.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                ProductThumbnail(
                    imageUrl = product.imageUrl,
                    productName = product.name,
                    category = product.category,
                    modifier = Modifier.size(54.dp),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = RetailSlate900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SKU: ${product.sku}",
                            fontSize = 12.sp,
                            color = RetailSlate500
                        )
                        if (product.barcode.isNotBlank()) {
                            Text(
                                text = " • #${product.barcode}",
                                fontSize = 12.sp,
                                color = RetailTealPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = " • ${product.category}",
                            fontSize = 12.sp,
                            color = RetailSlate500
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    StockBadge(quantity = product.stockQuantity, threshold = product.minStockThreshold)
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Text(
                    text = formatCurrency(product.sellingPrice),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = RetailTealPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onAddToCart,
                    enabled = product.stockQuantity > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RetailTealPrimary,
                        disabledContainerColor = RetailSlate300
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp).testTag("add_to_cart_${product.id}")
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CartSheetContent(
    cart: List<CartItem>,
    subtotal: Double,
    discountPercent: Double,
    discountAmount: Double,
    taxRate: Double = 0.08,
    taxAmount: Double,
    grandTotal: Double,
    onEditTaxClicked: (() -> Unit)? = null,
    onUpdateQty: (Long, Int) -> Unit,
    onRemoveItem: (Long) -> Unit,
    onSetDiscount: (Double) -> Unit,
    onScanMore: () -> Unit = {},
    onClearCart: () -> Unit,
    onProceedToCheckout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Shopping Cart (${cart.sumOf { it.quantity }} items)",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = RetailSlate900
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = onScanMore,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("cart_scan_more_button")
                ) {
                    Icon(
                        Icons.Filled.QrCodeScanner,
                        contentDescription = "Scan more",
                        tint = RetailTealPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scan More", fontSize = 12.sp, color = RetailTealPrimary)
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = onClearCart) {
                    Icon(Icons.Filled.Delete, contentDescription = "Clear cart", tint = Color.Red)
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Cart items list
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .height(260.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(cart, key = { it.product.id }) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RetailSlate100, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ProductThumbnail(
                        imageUrl = item.product.imageUrl,
                        productName = item.product.name,
                        category = item.product.category,
                        modifier = Modifier.size(42.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.product.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${formatCurrency(item.product.sellingPrice)} each",
                            fontSize = 12.sp,
                            color = RetailSlate500
                        )
                    }

                    // Stepper
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { onUpdateQty(item.product.id, -1) }
                        ) {
                            Icon(
                                Icons.Filled.Remove,
                                contentDescription = "Decrease",
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                        Text(
                            text = "${item.quantity}",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp),
                            fontSize = 14.sp
                        )
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { onUpdateQty(item.product.id, 1) }
                        ) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = "Increase",
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }

                    Text(
                        text = formatCurrency(item.subtotal),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = RetailTealPrimary,
                        modifier = Modifier.width(64.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        // Discounts
        Text(
            text = "Apply Discount",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = RetailSlate500
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(0.0, 5.0, 10.0, 15.0, 20.0).forEach { pct ->
                val isSelected = discountPercent == pct
                OutlinedButton(
                    onClick = { onSetDiscount(pct) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) RetailTealPrimary else Color.Transparent,
                        contentColor = if (isSelected) Color.White else RetailSlate700
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(34.dp)
                ) {
                    Text("${pct.toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Calculations breakdown
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal", color = RetailSlate500, fontSize = 13.sp)
                Text(formatCurrency(subtotal), fontSize = 13.sp)
            }
            if (discountPercent > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Discount (${discountPercent.toInt()}%)", color = Color(0xFF16A34A), fontSize = 13.sp)
                    Text("-${formatCurrency(discountAmount)}", color = Color(0xFF16A34A), fontSize = 13.sp)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val taxPctStr = String.format(Locale.US, "%.1f", taxRate * 100.0).removeSuffix(".0")
                    Text("Sales Tax ($taxPctStr%)", color = RetailSlate500, fontSize = 13.sp)
                    if (onEditTaxClicked != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .clickable { onEditTaxClicked() }
                                .testTag("cart_edit_tax_btn")
                        ) {
                            Text(
                                text = "Edit",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(formatCurrency(taxAmount), fontSize = 13.sp)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Due", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = RetailSlate900)
                Text(formatCurrency(grandTotal), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = RetailTealPrimary)
            }
        }

        Button(
            onClick = onProceedToCheckout,
            colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("cart_proceed_pay_button")
        ) {
            Text("Proceed to Payment (${formatCurrency(grandTotal)})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
fun CheckoutDialog(
    grandTotal: Double,
    khrRate: Double = CurrencyUtils.activeKhrExchangeRate,
    currentUser: UserEntity? = null,
    onVerifyAdminPin: (suspend (String) -> UserEntity?)? = null,
    onDismiss: () -> Unit,
    onConfirmPayment: (String, Double) -> Unit
) {
    val isAdmin = currentUser?.role?.equals("ADMIN", ignoreCase = true) == true
    var adminUnlockedForSession by remember { mutableStateOf(false) }
    var adminAuthorizingUser by remember { mutableStateOf<UserEntity?>(null) }
    val isBakongAuthorized = isAdmin || adminUnlockedForSession

    var adminPinInput by remember { mutableStateOf("") }
    var adminPinError by remember { mutableStateOf<String?>(null) }
    var isVerifyingPin by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var selectedMethod by remember { mutableStateOf("CASH") }
    var tenderCurrency by remember { mutableStateOf("USD") } // "USD" or "KHR"

    val grandTotalKhr = CurrencyUtils.usdToKhr(grandTotal, khrRate)
    var amountTenderedText by remember(tenderCurrency) {
        mutableStateOf(
            if (tenderCurrency == "KHR") "$grandTotalKhr" else String.format(Locale.US, "%.2f", grandTotal)
        )
    }

    // Calculations based on currency
    val tenderedUsd: Double
    val changeUsd: Double
    val changeKhr: Long

    if (tenderCurrency == "KHR") {
        val tenderedKhr = amountTenderedText.filter { it.isDigit() }.toLongOrNull() ?: grandTotalKhr
        val rawDiff = (tenderedKhr - grandTotalKhr).coerceAtLeast(0)
        changeKhr = rawDiff
        changeUsd = CurrencyUtils.khrToUsd(rawDiff, khrRate)
        tenderedUsd = CurrencyUtils.khrToUsd(tenderedKhr, khrRate)
    } else {
        val tenderedUsdVal = amountTenderedText.toDoubleOrNull() ?: grandTotal
        val rawDiff = (tenderedUsdVal - grandTotal).coerceAtLeast(0.0)
        changeUsd = rawDiff
        changeKhr = CurrencyUtils.usdToKhr(rawDiff, khrRate)
        tenderedUsd = tenderedUsdVal
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Complete Sale",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = RetailSlate900
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                // Amount banner showing both USD and KHR
                Surface(
                    color = RetailSlate100,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Total Amount Due", fontSize = 12.sp, color = RetailSlate500)
                        Text(
                            text = String.format(Locale.US, "$%.2f", grandTotal),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = RetailTealPrimary
                        )
                        Text(
                            text = "≈ ${CurrencyUtils.formatKhrRaw(grandTotalKhr)} (Rate: $1 = ${CurrencyUtils.formatKhrRaw(khrRate.toLong())})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RetailSlate700
                        )
                    }
                }

                // Payment Method Selector (CASH, KHQR, CARD)
                Text("Select Tender Type", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = RetailSlate700)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple("CASH", "Cash (USD/KHR)", Icons.Filled.Money),
                        Triple(
                            "KHQR",
                            if (isBakongAuthorized) "KHQR Bakong" else "KHQR (Admin)",
                            if (isBakongAuthorized) Icons.Filled.QrCode else Icons.Filled.Lock
                        ),
                        Triple("CARD", "Credit Card", Icons.Filled.CreditCard)
                    ).forEach { (id, label, icon) ->
                        val isSelected = selectedMethod == id
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) RetailTealPrimary else RetailSlate100,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tender_method_${id.lowercase()}")
                                .clickable {
                                    selectedMethod = id
                                    if (id == "KHQR" || id == "CARD") {
                                        amountTenderedText = String.format(Locale.US, "%.2f", grandTotal)
                                    }
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    icon,
                                    contentDescription = label,
                                    tint = if (isSelected) Color.White else RetailSlate700,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = if (isSelected) Color.White else RetailSlate700
                                )
                            }
                        }
                    }
                }

                // KHQR / Bakong presentation (Exposed only for Admin users or admin-authorized sessions)
                if (selectedMethod == "KHQR") {
                    if (isBakongAuthorized) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("bakong_admin_controls_container")
                        ) {
                            // Admin Role Security Verification Badge
                            Surface(
                                color = Color(0xFFF0FDF4),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                modifier = Modifier.fillMaxWidth().testTag("admin_authorized_badge")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFF15803D),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isAdmin) {
                                            "Admin Privileges Verified: ${currentUser?.name ?: "Admin"} (ADMIN)"
                                        } else {
                                            "Admin Authorization Granted by ${adminAuthorizingUser?.name ?: "Store Administrator"}"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF15803D)
                                    )
                                }
                            }

                            // Bakong QR Payment Controls Component
                            com.example.ui.components.BakongKhqrCard(
                                usdAmount = grandTotal,
                                khrRate = khrRate,
                                merchantName = "TR STORE & CAFE",
                                bakongAccountId = "trstore@aclb",
                                billNumber = "BILL-${System.currentTimeMillis() % 100000}",
                                onConfirmPaid = {
                                    onConfirmPayment("KHQR", grandTotal)
                                }
                            )
                        }
                    } else {
                        // Hide Bakong QR payment controls and require Admin authentication
                        Surface(
                            color = Color(0xFFFFFBEB),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                            modifier = Modifier.fillMaxWidth().testTag("bakong_admin_restricted_panel")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFEF3C7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Lock,
                                        contentDescription = "Admin Restricted",
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Text(
                                    text = "Admin Role Required for Bakong QR",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = RetailSlate900,
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "Bakong QR payment controls and digital settlement are restricted to Admin users. Currently logged in: ${currentUser?.name ?: "Staff"} (${currentUser?.role ?: "CASHIER"}).",
                                    fontSize = 12.sp,
                                    color = RetailSlate700,
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "Enter Store Admin PIN to expose Bakong QR controls:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RetailSlate900
                                )

                                OutlinedTextField(
                                    value = adminPinInput,
                                    onValueChange = {
                                        if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                            adminPinInput = it
                                            adminPinError = null
                                        }
                                    },
                                    placeholder = { Text("••••") },
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth(0.65f)
                                        .testTag("admin_pin_auth_input")
                                )

                                if (adminPinError != null) {
                                    Text(
                                        text = adminPinError ?: "",
                                        color = Color.Red,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            isVerifyingPin = true
                                            val adminUser = onVerifyAdminPin?.invoke(adminPinInput)
                                            isVerifyingPin = false
                                            if (adminUser != null) {
                                                adminAuthorizingUser = adminUser
                                                adminUnlockedForSession = true
                                                adminPinInput = ""
                                                adminPinError = null
                                            } else {
                                                adminPinError = "Invalid Admin PIN. Only Admin users can authorize Bakong QR."
                                            }
                                        }
                                    },
                                    enabled = adminPinInput.length >= 4 && !isVerifyingPin,
                                    colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("unlock_bakong_btn")
                                ) {
                                    Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        if (isVerifyingPin) "Verifying..." else "Authorize & Expose Bakong QR",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Cash specific options
                if (selectedMethod == "CASH") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Currency selection pills for Cash
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = tenderCurrency == "USD",
                                onClick = {
                                    tenderCurrency = "USD"
                                    amountTenderedText = String.format(Locale.US, "%.2f", grandTotal)
                                },
                                label = { Text("Tender in USD ($)", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = tenderCurrency == "KHR",
                                onClick = {
                                    tenderCurrency = "KHR"
                                    amountTenderedText = "$grandTotalKhr"
                                },
                                label = { Text("Tender in KHR (៛)", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Text("Quick Tender Presets", fontSize = 12.sp, color = RetailSlate500)

                        if (tenderCurrency == "KHR") {
                            // Quick Riel bills presets
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf<Pair<String, Long>>(
                                    Pair("Exact", grandTotalKhr),
                                    Pair("20k ៛", 20000L),
                                    Pair("50k ៛", 50000L),
                                    Pair("100k ៛", 100000L),
                                    Pair("200k ៛", 200000L)
                                ).filter { it.second >= grandTotalKhr || it.first == "Exact" }.take(4).forEach { (lbl, amt) ->
                                    OutlinedButton(
                                        onClick = { amountTenderedText = "$amt" },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                                    ) {
                                        Text(lbl, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = amountTenderedText,
                                onValueChange = { amountTenderedText = it.filter { char -> char.isDigit() } },
                                label = { Text("Amount Tendered (KHR ៛)") },
                                trailingIcon = { Text("៛", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            // Quick USD bills presets
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf<Pair<String, Double>>(
                                    Pair("Exact", grandTotal),
                                    Pair("$10", 10.0),
                                    Pair("$20", 20.0),
                                    Pair("$50", 50.0),
                                    Pair("$100", 100.0)
                                ).filter { it.second >= grandTotal || it.first == "Exact" }.take(4).forEach { (lbl, amt) ->
                                    OutlinedButton(
                                        onClick = { amountTenderedText = String.format(Locale.US, "%.2f", amt) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                    ) {
                                        Text(lbl, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = amountTenderedText,
                                onValueChange = { amountTenderedText = it },
                                label = { Text("Amount Tendered ($ USD)") },
                                leadingIcon = { Text("$", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Change Due Box (Shows both USD & KHR change!)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFECFDF5), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Change Due", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF047857))
                                Text("In KHR: ${CurrencyUtils.formatKhrRaw(changeKhr)}", fontSize = 11.sp, color = Color(0xFF065F46))
                            }
                            Text(
                                text = String.format(Locale.US, "$%.2f", changeUsd),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = Color(0xFF047857)
                            )
                        }
                    }
                }

                val canConfirm = when (selectedMethod) {
                    "KHQR" -> isBakongAuthorized
                    "CARD" -> true
                    else -> (tenderCurrency == "KHR" && (amountTenderedText.filter { it.isDigit() }.toLongOrNull() ?: 0L) >= grandTotalKhr) ||
                            (tenderCurrency == "USD" && tenderedUsd >= grandTotal - 0.001)
                }

                Button(
                    onClick = { onConfirmPayment(selectedMethod, tenderedUsd) },
                    enabled = canConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_payment_button")
                ) {
                    Icon(Icons.Filled.Check, contentDescription = "Confirm", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Complete Checkout", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun ReceiptDialog(
    sale: SaleEntity,
    items: List<SaleItemEntity>,
    khrRate: Double = CurrencyUtils.activeKhrExchangeRate,
    branch: BranchEntity? = null,
    onEditReceiptConfig: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        DigitalReceiptScreen(
            sale = sale,
            items = items,
            khrRate = khrRate,
            branch = branch,
            onEditReceipt = onEditReceiptConfig,
            onDismiss = onDismiss
        )
    }
}
