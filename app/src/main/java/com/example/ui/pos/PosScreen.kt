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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.data.model.ClientEntity
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

enum class PosViewMode {
    LIST,
    GRID
}

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
    val bakongAccountId by viewModel.bakongAccountId.collectAsState()
    val bakongMerchantName by viewModel.bakongMerchantName.collectAsState()

    var posViewMode by rememberSaveable { mutableStateOf(PosViewMode.GRID) }
    var showCartSheet by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var showBranchReceiptDialog by remember { mutableStateOf(false) }
    var showEditTaxDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val allClients by viewModel.allClients.collectAsState()

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

            // Quick Promotion Action: Buy 10 Get 1 Free Example
            Surface(
                color = Color(0xFFFFFBEB),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .clickable { 
                        viewModel.applyBuy10Get1Example()
                        showCartSheet = true
                    }
                    .testTag("pos_buy10_get1_example_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎁", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Free Items for Clients (ទំនិញថែមជូនភ្ញៀវ)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = "Type any free quantity for client or try B10G1 example",
                                fontSize = 10.sp,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                    Surface(
                        color = Color(0xFFD97706),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Try Example",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Category Chips Row with View Mode Switcher (List / Grid)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
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

                // View Mode Toggle Segmented Control: [ ☰ List | ⊞ Grid ]
                Surface(
                    color = RetailSlate100,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RetailSlate300.copy(alpha = 0.6f)),
                    modifier = Modifier.testTag("pos_view_mode_toggle")
                ) {
                    Row(
                        modifier = Modifier.padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // List Toggle
                        Surface(
                            color = if (posViewMode == PosViewMode.LIST) Color.White else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            shadowElevation = if (posViewMode == PosViewMode.LIST) 1.dp else 0.dp,
                            modifier = Modifier
                                .clickable { posViewMode = PosViewMode.LIST }
                                .testTag("pos_toggle_list_view")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.ViewList,
                                    contentDescription = "List View",
                                    tint = if (posViewMode == PosViewMode.LIST) RetailTealPrimary else RetailSlate500,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "List",
                                    fontSize = 11.sp,
                                    fontWeight = if (posViewMode == PosViewMode.LIST) FontWeight.Bold else FontWeight.Medium,
                                    color = if (posViewMode == PosViewMode.LIST) RetailSlate900 else RetailSlate500
                                )
                            }
                        }

                        // Grid Toggle
                        Surface(
                            color = if (posViewMode == PosViewMode.GRID) Color.White else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            shadowElevation = if (posViewMode == PosViewMode.GRID) 1.dp else 0.dp,
                            modifier = Modifier
                                .clickable { posViewMode = PosViewMode.GRID }
                                .testTag("pos_toggle_grid_view")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.GridView,
                                    contentDescription = "Grid View",
                                    tint = if (posViewMode == PosViewMode.GRID) RetailTealPrimary else RetailSlate500,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Grid",
                                    fontSize = 11.sp,
                                    fontWeight = if (posViewMode == PosViewMode.GRID) FontWeight.Bold else FontWeight.Medium,
                                    color = if (posViewMode == PosViewMode.GRID) RetailSlate900 else RetailSlate500
                                )
                            }
                        }
                    }
                }
            }

            // Products Catalog (List or Grid)
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
            } else if (posViewMode == PosViewMode.GRID) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pos_products_grid")
                ) {
                    items(products, key = { it.id }) { product ->
                        val inCartQty = cart.find { it.product.id == product.id }?.quantity ?: 0
                        PosProductGridCard(
                            product = product,
                            cartQuantity = inCartQty,
                            onAddToCart = { viewModel.addToCart(product) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pos_products_list")
                ) {
                    items(products, key = { it.id }) { product ->
                        val inCartQty = cart.find { it.product.id == product.id }?.quantity ?: 0
                        PosProductRow(
                            product = product,
                            cartQuantity = inCartQty,
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
                            Text("Check Bill", fontWeight = FontWeight.Bold)
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
                onUpdateItemDiscount = { id, pct -> viewModel.updateItemDiscount(id, pct) },
                onUpdateItemCustomPrice = { id, price -> viewModel.updateItemCustomPrice(id, price) },
                onToggleBuy10Get1 = { id -> viewModel.toggleBuy10Get1Free(id) },
                onUpdateItemFreeQty = { id, qty -> viewModel.setItemFreeQuantity(id, qty) },
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

    // Checkout Dialog (Check Bill & Settle Sale)
    if (showCheckoutDialog) {
        CheckoutDialog(
            cart = cart,
            taxRate = taxRate,
            billDiscountPercent = discountPercent,
            khrRate = khrRate,
            currentUser = currentUser,
            clients = allClients,
            bakongAccountId = bakongAccountId,
            merchantName = bakongMerchantName,
            onUpdateQty = { id, delta -> viewModel.updateCartQuantity(id, delta) },
            onRemoveItem = { id -> viewModel.removeFromCart(id) },
            onUpdateItemDiscount = { id, pct -> viewModel.updateItemDiscount(id, pct) },
            onUpdateItemPrice = { id, price -> viewModel.updateItemCustomPrice(id, price) },
            onSetBillDiscount = { pct -> viewModel.setDiscountPercent(pct) },
            onToggleBuy10Get1 = { id -> viewModel.toggleBuy10Get1Free(id) },
            onUpdateItemFreeQty = { id, qty -> viewModel.setItemFreeQuantity(id, qty) },
            onVerifyAdminPin = { pin -> viewModel.verifyAdminPin(pin) },
            onCreateClient = { name, phone, email, tier, address, notes, callback ->
                coroutineScope.launch {
                    val created = viewModel.createAndSelectClient(name, phone, email, tier, address, notes)
                    callback(created)
                }
            },
            onDismiss = { showCheckoutDialog = false },
            onConfirmPayment = { method, tendered, client ->
                viewModel.processCheckout(method, tendered, client = client)
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
            onSaveBranchReceipt = { branch, header, subtitle, vatTin, addr, phone, footer, taxPct, gap, setAsActive, wifiName, wifiPassword ->
                viewModel.updateBranchReceiptConfig(branch, header, subtitle, vatTin, addr, phone, footer, taxPct, gap, setAsActive, wifiName, wifiPassword)
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
    cartQuantity: Int = 0,
    onAddToCart: () -> Unit
) {
    val isInCart = cartQuantity > 0
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isInCart) Color(0xFFF0FDFA) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isInCart) 2.dp else 1.dp),
        shape = RoundedCornerShape(12.dp),
        border = if (isInCart) androidx.compose.foundation.BorderStroke(1.5.dp, RetailTealPrimary) else null,
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
                Box {
                    ProductThumbnail(
                        imageUrl = product.imageUrl,
                        productName = product.name,
                        category = product.category,
                        modifier = Modifier.size(54.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                    if (isInCart) {
                        Surface(
                            color = RetailTealPrimary,
                            shape = CircleShape,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(18.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$cartQuantity",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = product.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = RetailSlate900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isInCart) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = RetailTealPrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "In Cart (x$cartQuantity)",
                                    color = RetailTealPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
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
                    Text(if (isInCart) "+$cartQuantity" else "Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PosProductGridCard(
    product: ProductEntity,
    cartQuantity: Int = 0,
    onAddToCart: () -> Unit
) {
    val isInCart = cartQuantity > 0
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isInCart) Color(0xFFF0FDFA) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isInCart) 2.dp else 1.dp),
        shape = RoundedCornerShape(14.dp),
        border = if (isInCart) {
            androidx.compose.foundation.BorderStroke(1.5.dp, RetailTealPrimary)
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, RetailSlate300.copy(alpha = 0.5f))
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = product.stockQuantity > 0, onClick = onAddToCart)
            .testTag("product_grid_${product.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp),
                contentAlignment = Alignment.Center
            ) {
                ProductThumbnail(
                    imageUrl = product.imageUrl,
                    productName = product.name,
                    category = product.category,
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Stock status badge on top-left
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                ) {
                    StockBadge(quantity = product.stockQuantity, threshold = product.minStockThreshold)
                }

                // In-Cart Badge on top-right
                if (isInCart) {
                    Surface(
                        color = RetailTealPrimary,
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.ShoppingCart,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "x$cartQuantity",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category tag
            Text(
                text = product.category.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = RetailTealPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Name
            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = RetailSlate900,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 17.sp,
                modifier = Modifier.height(34.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Bottom row: Price & Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formatCurrency(product.sellingPrice),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = RetailTealPrimary
                    )
                    Text(
                        text = "SKU: ${product.sku}",
                        fontSize = 10.sp,
                        color = RetailSlate500,
                        maxLines = 1
                    )
                }

                IconButton(
                    onClick = onAddToCart,
                    enabled = product.stockQuantity > 0,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (product.stockQuantity > 0) RetailTealPrimary else RetailSlate300)
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Add to Cart",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// Dialogs for Item & Bill Editing in POS
// ==========================================

@Composable
fun EditItemPriceDialog(
    item: CartItem,
    onDismiss: () -> Unit,
    onSave: (Double?) -> Unit
) {
    var priceText by remember {
        mutableStateOf(String.format(Locale.US, "%.2f", item.unitPrice))
    }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Edit Unit Price",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = item.product.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = RetailSlate900
                )
                Text(
                    text = "Catalog Base Price: ${formatCurrency(item.product.sellingPrice)}",
                    fontSize = 12.sp,
                    color = RetailSlate500
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = {
                        priceText = it
                        errorText = null
                    },
                    label = { Text("New Unit Price ($)") },
                    leadingIcon = { Text("$", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("edit_price_input")
                )
                if (errorText != null) {
                    Text(errorText!!, color = Color.Red, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = priceText.toDoubleOrNull()
                    if (p != null && p >= 0.0) {
                        onSave(p)
                        onDismiss()
                    } else {
                        errorText = "Please enter a valid price >= 0"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary)
            ) {
                Text("Save Price")
            }
        },
        dismissButton = {
            Row {
                if (item.customUnitPrice != null) {
                    TextButton(onClick = {
                        onSave(null)
                        onDismiss()
                    }) {
                        Text("Reset Original")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun EditItemDiscountDialog(
    item: CartItem,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var discountText by remember {
        mutableStateOf(if (item.discountPercent > 0) "${item.discountPercent.toInt()}" else "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Item Discount (%)", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(item.product.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("Select preset or enter custom percent:", fontSize = 12.sp, color = RetailSlate500)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0.0, 5.0, 10.0, 15.0, 20.0).forEach { pct ->
                        OutlinedButton(
                            onClick = { discountText = "${pct.toInt()}" },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(2.dp)
                        ) {
                            Text("${pct.toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                OutlinedTextField(
                    value = discountText,
                    onValueChange = { discountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Discount Percent (%)") },
                    trailingIcon = { Text("%", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("item_discount_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pct = discountText.toDoubleOrNull() ?: 0.0
                    onSave(pct.coerceIn(0.0, 100.0))
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary)
            ) {
                Text("Apply Discount")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CustomBillDiscountDialog(
    currentDiscount: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var discountText by remember {
        mutableStateOf(if (currentDiscount > 0) "${currentDiscount.toInt()}" else "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Bill Discount (%)", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Apply percent discount to entire bill subtotal:", fontSize = 12.sp, color = RetailSlate500)
                OutlinedTextField(
                    value = discountText,
                    onValueChange = { discountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Discount Percent (%)") },
                    trailingIcon = { Text("%", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("custom_bill_discount_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pct = discountText.toDoubleOrNull() ?: 0.0
                    onSave(pct.coerceIn(0.0, 100.0))
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary)
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditItemFreeQuantityDialog(
    item: CartItem,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var freeQtyText by remember {
        mutableStateOf(if (item.freeItemsCount > 0) "${item.freeItemsCount}" else "1")
    }
    val currentFree = freeQtyText.filter { it.isDigit() }.toIntOrNull() ?: 0
    val safeFree = currentFree.coerceIn(0, item.quantity)
    val freeSavings = safeFree * item.unitPrice
    val netLine = (item.grossSubtotal - freeSavings).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🎁", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Free Items to Client", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RetailSlate900)
                    Text("ថែមជូនភ្ញៀវឥតគិតថ្លៃ (Type Number)", fontSize = 11.sp, color = Color(0xFF15803D), fontWeight = FontWeight.SemiBold)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = RetailSlate100,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(item.product.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RetailSlate900)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Ordered Qty:", fontSize = 11.sp, color = RetailSlate500)
                            Text("${item.quantity} units (${formatCurrency(item.grossSubtotal)})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RetailSlate900)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Unit Price:", fontSize = 11.sp, color = RetailSlate500)
                            Text(formatCurrency(item.unitPrice), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RetailTealPrimary)
                        }
                    }
                }

                Text("Type number of free units or tap presets:", fontSize = 12.sp, color = RetailSlate500, fontWeight = FontWeight.SemiBold)

                // Quick Presets Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        "0 (None)" to 0,
                        "1 Free" to 1,
                        "2 Free" to 2,
                        "B10G1" to (if (item.quantity >= 11) item.quantity / 11 else 1)
                    ).forEach { (label, qty) ->
                        val isSel = safeFree == qty
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) Color(0xFF15803D) else Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) Color(0xFF15803D) else RetailSlate300),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { freeQtyText = "$qty" }
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else RetailSlate700,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp)
                            )
                        }
                    }
                }

                // Free Qty Input with Stepper Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = RetailSlate100,
                        border = androidx.compose.foundation.BorderStroke(1.dp, RetailSlate300),
                        modifier = Modifier
                            .size(44.dp)
                            .clickable {
                                val next = (safeFree - 1).coerceAtLeast(0)
                                freeQtyText = "$next"
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("-", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = RetailSlate900)
                        }
                    }

                    OutlinedTextField(
                        value = freeQtyText,
                        onValueChange = { freeQtyText = it.filter { c -> c.isDigit() } },
                        label = { Text("Free Units (ថែមជូន)") },
                        placeholder = { Text("0") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("item_free_quantity_input"),
                        trailingIcon = {
                            Text("Free", fontWeight = FontWeight.Bold, color = Color(0xFF15803D), modifier = Modifier.padding(end = 12.dp))
                        }
                    )

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFDCFCE7),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                        modifier = Modifier
                            .size(44.dp)
                            .clickable {
                                val next = (safeFree + 1).coerceAtMost(item.quantity)
                                freeQtyText = "$next"
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("+", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF15803D))
                        }
                    }
                }

                // Live Summary Banner
                Surface(
                    color = if (safeFree > 0) Color(0xFFF0FDF4) else RetailSlate100,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (safeFree > 0) Color(0xFFBBF7D0) else RetailSlate300),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Paid Units:", fontSize = 11.sp, color = RetailSlate500)
                            Text("${(item.quantity - safeFree).coerceAtLeast(0)} units", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RetailSlate900)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Free Units to Client:", fontSize = 11.sp, color = Color(0xFF15803D), fontWeight = FontWeight.Bold)
                            Text("$safeFree units (-${formatCurrency(freeSavings)})", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF15803D))
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Net Line Subtotal:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RetailSlate900)
                            Text(formatCurrency(netLine), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = RetailTealPrimary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(safeFree)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D))
            ) {
                Text("Apply Free ($safeFree units)", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                if (item.freeItemsCount > 0) {
                    TextButton(onClick = {
                        onSave(0)
                        onDismiss()
                    }) {
                        Text("Clear Free", color = Color.Red)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
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
    onUpdateItemDiscount: (Long, Double) -> Unit = { _, _ -> },
    onUpdateItemCustomPrice: (Long, Double?) -> Unit = { _, _ -> },
    onToggleBuy10Get1: (Long) -> Unit = {},
    onUpdateItemFreeQty: ((Long, Int) -> Unit)? = null,
    onScanMore: () -> Unit = {},
    onClearCart: () -> Unit,
    onProceedToCheckout: () -> Unit
) {
    var itemToEditPrice by remember { mutableStateOf<CartItem?>(null) }
    var itemToEditDiscount by remember { mutableStateOf<CartItem?>(null) }
    var itemToEditFreeQty by remember { mutableStateOf<CartItem?>(null) }
    var showCustomBillDiscountDialog by remember { mutableStateOf(false) }

    itemToEditPrice?.let { item ->
        EditItemPriceDialog(
            item = item,
            onDismiss = { itemToEditPrice = null },
            onSave = { newPrice -> onUpdateItemCustomPrice(item.product.id, newPrice) }
        )
    }

    itemToEditDiscount?.let { item ->
        EditItemDiscountDialog(
            item = item,
            onDismiss = { itemToEditDiscount = null },
            onSave = { pct -> onUpdateItemDiscount(item.product.id, pct) }
        )
    }

    itemToEditFreeQty?.let { item ->
        EditItemFreeQuantityDialog(
            item = item,
            onDismiss = { itemToEditFreeQty = null },
            onSave = { freeQty ->
                if (onUpdateItemFreeQty != null) {
                    onUpdateItemFreeQty(item.product.id, freeQty)
                } else {
                    onToggleBuy10Get1(item.product.id)
                }
            }
        )
    }

    if (showCustomBillDiscountDialog) {
        CustomBillDiscountDialog(
            currentDiscount = discountPercent,
            onDismiss = { showCustomBillDiscountDialog = false },
            onSave = { pct -> onSetDiscount(pct) }
        )
    }

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

        // Cart items list with item-level controls
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .height(300.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(cart, key = { it.product.id }) { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RetailSlate100, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Promo banner for Free Items
                    if (item.freeItemsCount > 0) {
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { itemToEditFreeQty = item }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "🎁 FREE TO CLIENT (${item.freeItemsCount} Free Unit • Tap to edit)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                                Text(
                                    text = "-${formatCurrency(item.freeDiscountAmount)}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                    } else if (item.quantity >= 10 && item.freeItemsCount == 0) {
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { itemToEditFreeQty = item }
                        ) {
                            Text(
                                text = "🎉 Customer ordered ${item.quantity} units! Tap to give free items to client",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF92400E),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ProductThumbnail(
                            imageUrl = item.product.imageUrl,
                            productName = item.product.name,
                            category = item.product.category,
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.product.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${formatCurrency(item.unitPrice)} each",
                                    fontSize = 12.sp,
                                    color = if (item.customUnitPrice != null) RetailTealPrimary else RetailSlate500,
                                    fontWeight = if (item.customUnitPrice != null) FontWeight.Bold else FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Edit Price",
                                    fontSize = 11.sp,
                                    color = RetailTealPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .clickable { itemToEditPrice = item }
                                        .padding(2.dp)
                                )
                            }
                        }

                        // Stepper
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp)
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
                                modifier = Modifier.padding(horizontal = 8.dp),
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

                        Column(horizontalAlignment = Alignment.End) {
                            if (item.itemDiscountAmount > 0) {
                                Text(
                                    text = formatCurrency(item.grossSubtotal),
                                    fontSize = 11.sp,
                                    color = RetailSlate500,
                                    style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                                )
                            }
                            Text(
                                text = formatCurrency(item.subtotal),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = RetailTealPrimary
                            )
                        }

                        IconButton(
                            onClick = { onRemoveItem(item.product.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete item", tint = Color.Red.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                        }
                    }

                    // Item-level discount row + Buy 10 Get 1 Free chip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Item Disc:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = RetailSlate500)
                        listOf(0.0, 5.0, 10.0, 15.0).forEach { pct ->
                            val isSel = !item.isBuy10Get1Free && item.discountPercent == pct
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) RetailTealPrimary else Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) RetailTealPrimary else RetailSlate300),
                                modifier = Modifier.clickable { onUpdateItemDiscount(item.product.id, pct) }
                            ) {
                                Text(
                                    text = "${pct.toInt()}%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else RetailSlate700,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        val isCustom = !item.isBuy10Get1Free && item.discountPercent > 0.0 && !listOf(5.0, 10.0, 15.0).contains(item.discountPercent)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isCustom) Color(0xFFD97706) else Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isCustom) Color(0xFFD97706) else RetailSlate300),
                            modifier = Modifier.clickable { itemToEditDiscount = item }
                        ) {
                            Text(
                                text = if (isCustom) "${item.discountPercent.toInt()}%" else "Custom%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCustom) Color.White else Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }

                        // Button to type number for free to Clients
                        val hasFree = item.freeItemsCount > 0
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (hasFree) Color(0xFF15803D) else Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (hasFree) Color(0xFF15803D) else Color(0xFF16A34A)),
                            modifier = Modifier
                                .clickable { itemToEditFreeQty = item }
                                .testTag("cart_free_qty_btn_${item.product.id}")
                        ) {
                            Text(
                                text = if (hasFree) "🎁 Free: ${item.freeItemsCount} (-${formatCurrency(item.freeDiscountAmount)})" else "🎁 +Free (ថែម)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (hasFree) Color.White else Color(0xFF166534),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

        // Bill Discounts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🏷️ Bill Discount (% Off)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = RetailSlate700
            )
            if (discountPercent > 0) {
                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${discountPercent.toInt()}% OFF ACTIVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF16A34A),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(32.dp)
                ) {
                    Text("${pct.toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            val isCustomBillDisc = discountPercent > 0.0 && !listOf(5.0, 10.0, 15.0, 20.0).contains(discountPercent)
            OutlinedButton(
                onClick = { showCustomBillDiscountDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isCustomBillDisc) Color(0xFFD97706) else Color.Transparent,
                    contentColor = if (isCustomBillDisc) Color.White else Color(0xFFB45309)
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                modifier = Modifier.weight(1.2f).height(32.dp)
            ) {
                Text(
                    text = if (isCustomBillDisc) "${discountPercent.toInt()}%" else "Custom%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Calculations breakdown
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
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
                    Text("Bill Discount (${discountPercent.toInt()}%)", color = Color(0xFF16A34A), fontSize = 13.sp)
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
            Text("Proceed to Check Bill & Pay (${formatCurrency(grandTotal)})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
fun CheckoutDialog(
    cart: List<CartItem>,
    taxRate: Double,
    billDiscountPercent: Double,
    khrRate: Double = CurrencyUtils.activeKhrExchangeRate,
    currentUser: UserEntity? = null,
    clients: List<ClientEntity> = emptyList(),
    bakongAccountId: String = "trstore@aclb",
    merchantName: String = "TR STORE & CAFE",
    onUpdateQty: (Long, Int) -> Unit,
    onRemoveItem: (Long) -> Unit,
    onUpdateItemDiscount: (Long, Double) -> Unit,
    onUpdateItemPrice: (Long, Double?) -> Unit,
    onSetBillDiscount: (Double) -> Unit,
    onToggleBuy10Get1: ((Long) -> Unit)? = null,
    onUpdateItemFreeQty: ((Long, Int) -> Unit)? = null,
    onVerifyAdminPin: (suspend (String) -> UserEntity?)? = null,
    onCreateClient: ((String, String, String, String, String, String, (ClientEntity) -> Unit) -> Unit)? = null,
    onDismiss: () -> Unit,
    onConfirmPayment: (String, Double, ClientEntity?) -> Unit
) {
    val isAdmin = currentUser?.role?.equals("ADMIN", ignoreCase = true) == true
    var adminUnlockedForSession by remember { mutableStateOf(false) }
    var adminAuthorizingUser by remember { mutableStateOf<UserEntity?>(null) }
    val isBakongAuthorized = isAdmin || adminUnlockedForSession

    var adminPinInput by remember { mutableStateOf("") }
    var adminPinError by remember { mutableStateOf<String?>(null) }
    var isVerifyingPin by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var selectedClient by remember { mutableStateOf<ClientEntity?>(null) }
    var clientTabMode by remember { mutableStateOf("EXISTING") } // "EXISTING" (Select Old Client) or "NEW" (Fill New Client)
    var clientSearchQuery by remember { mutableStateOf("") }

    var newClientName by remember { mutableStateOf("") }
    var newClientPhone by remember { mutableStateOf("") }
    var newClientEmail by remember { mutableStateOf("") }
    var newClientTier by remember { mutableStateOf("BRONZE") }
    var newClientAddress by remember { mutableStateOf("") }
    var newClientNotes by remember { mutableStateOf("") }
    var newClientError by remember { mutableStateOf<String?>(null) }
    var isSavingClient by remember { mutableStateOf(false) }

    var selectedMethod by remember { mutableStateOf("CASH") }
    var tenderCurrency by remember { mutableStateOf("USD") } // "USD" or "KHR"

    var itemToEditPrice by remember { mutableStateOf<CartItem?>(null) }
    var itemToEditDiscount by remember { mutableStateOf<CartItem?>(null) }
    var itemToEditFreeQty by remember { mutableStateOf<CartItem?>(null) }
    var showCustomBillDiscountDialog by remember { mutableStateOf(false) }

    itemToEditPrice?.let { item ->
        EditItemPriceDialog(
            item = item,
            onDismiss = { itemToEditPrice = null },
            onSave = { newPrice -> onUpdateItemPrice(item.product.id, newPrice) }
        )
    }

    itemToEditDiscount?.let { item ->
        EditItemDiscountDialog(
            item = item,
            onDismiss = { itemToEditDiscount = null },
            onSave = { pct -> onUpdateItemDiscount(item.product.id, pct) }
        )
    }

    itemToEditFreeQty?.let { item ->
        EditItemFreeQuantityDialog(
            item = item,
            onDismiss = { itemToEditFreeQty = null },
            onSave = { freeQty ->
                if (onUpdateItemFreeQty != null) {
                    onUpdateItemFreeQty(item.product.id, freeQty)
                } else {
                    onToggleBuy10Get1?.invoke(item.product.id)
                }
            }
        )
    }

    if (showCustomBillDiscountDialog) {
        CustomBillDiscountDialog(
            currentDiscount = billDiscountPercent,
            onDismiss = { showCustomBillDiscountDialog = false },
            onSave = { pct -> onSetBillDiscount(pct) }
        )
    }

    // Dynamic calculations from current cart
    val totalItems = cart.sumOf { it.quantity }
    val grossSubtotal = cart.sumOf { it.grossSubtotal }
    val itemDiscountsTotal = cart.sumOf { it.itemDiscountAmount }
    val subtotalAfterItemDisc = (grossSubtotal - itemDiscountsTotal).coerceAtLeast(0.0)
    val billDiscountAmount = subtotalAfterItemDisc * (billDiscountPercent / 100.0)
    val totalDiscount = itemDiscountsTotal + billDiscountAmount
    val afterDiscount = (grossSubtotal - totalDiscount).coerceAtLeast(0.0)
    val taxAmount = afterDiscount * taxRate
    val grandTotal = afterDiscount + taxAmount
    val grandTotalKhr = CurrencyUtils.usdToKhr(grandTotal, khrRate)

    var amountTenderedText by remember(tenderCurrency, grandTotal) {
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Check Bill & Complete Sale",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = RetailSlate900
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = RetailTealPrimary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "POS #TR-${System.currentTimeMillis() % 10000}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RetailTealPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Cashier: ${currentUser?.name ?: "Staff"} (${currentUser?.role ?: "CASHIER"}) • $totalItems items",
                            fontSize = 12.sp,
                            color = RetailSlate500
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = RetailSlate700)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Scrollable Check Bill body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (cart.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No items on this ticket.", color = RetailSlate500, fontSize = 14.sp)
                        }
                    } else {
                        // ==========================================
                        // CLIENT SELECTION: Select Old Client or Fill New Client
                        // ==========================================
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (selectedClient != null) Color(0xFFF0FDF4) else RetailSlate100.copy(alpha = 0.7f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (selectedClient != null) Color(0xFF16A34A) else RetailSlate300
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("pos_check_bill_client_section")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Section Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.Person,
                                            contentDescription = null,
                                            tint = if (selectedClient != null) Color(0xFF15803D) else RetailTealPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Billed to Client (ចេញវិក្កយបត្រជូនអតិថិជន)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = RetailSlate900
                                        )
                                    }
                                    if (selectedClient != null) {
                                        Surface(
                                            color = Color(0xFFDCFCE7),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "✓ Linked to Bill",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF15803D),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "Select old client or fill new",
                                            fontSize = 11.sp,
                                            color = RetailSlate500,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                if (selectedClient != null) {
                                    val sc = selectedClient!!
                                    // Selected Client Details Card
                                    Surface(
                                        color = Color.White,
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Avatar Initials Circle
                                            val parsedColor = try {
                                                Color(android.graphics.Color.parseColor(sc.tierColorHex))
                                            } catch (_: Exception) {
                                                RetailTealPrimary
                                            }
                                            Surface(
                                                shape = CircleShape,
                                                color = parsedColor.copy(alpha = 0.15f),
                                                modifier = Modifier.size(42.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = sc.name.take(1).uppercase(),
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 18.sp,
                                                        color = parsedColor
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = sc.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = RetailSlate900
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = parsedColor.copy(alpha = 0.15f),
                                                        border = androidx.compose.foundation.BorderStroke(0.5.dp, parsedColor)
                                                    ) {
                                                        Text(
                                                            text = sc.tierDisplayName,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = parsedColor,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = "📞 ${sc.phone} • 🌟 ${sc.loyaltyPoints} pts • ${sc.visitsCount} visits",
                                                    fontSize = 11.sp,
                                                    color = RetailSlate700
                                                )
                                                val earnedPts = (grandTotal * 10).toInt()
                                                Text(
                                                    text = "🎁 Earning +$earnedPts loyalty points with this bill!",
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF15803D)
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                TextButton(
                                                    onClick = { selectedClient = null },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text("Change", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RetailTealPrimary)
                                                }
                                                TextButton(
                                                    onClick = { selectedClient = null },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text("Clear", fontSize = 10.sp, color = RetailSlate500)
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Mode Selector: "Select Old Client" vs "Fill New Client"
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (clientTabMode == "EXISTING") RetailTealPrimary else Color.White,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (clientTabMode == "EXISTING") RetailTealPrimary else RetailSlate300),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { clientTabMode = "EXISTING" }
                                                .testTag("tab_select_old_client")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    Icons.Filled.Person,
                                                    contentDescription = null,
                                                    tint = if (clientTabMode == "EXISTING") Color.White else RetailSlate700,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "👤 Select Old Client (${clients.size})",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (clientTabMode == "EXISTING") Color.White else RetailSlate700
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (clientTabMode == "NEW") RetailTealPrimary else Color.White,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (clientTabMode == "NEW") RetailTealPrimary else RetailSlate300),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { clientTabMode = "NEW" }
                                                .testTag("tab_fill_new_client")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    Icons.Filled.PersonAdd,
                                                    contentDescription = null,
                                                    tint = if (clientTabMode == "NEW") Color.White else RetailSlate700,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "➕ Fill New Client",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (clientTabMode == "NEW") Color.White else RetailSlate700
                                                )
                                            }
                                        }
                                    }

                                    // Tab 1: SELECT OLD CLIENT
                                    if (clientTabMode == "EXISTING") {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedTextField(
                                                value = clientSearchQuery,
                                                onValueChange = { clientSearchQuery = it },
                                                placeholder = { Text("Search old client by name, phone or tier...", fontSize = 12.sp) },
                                                leadingIcon = {
                                                    Icon(Icons.Filled.Search, contentDescription = null, tint = RetailSlate500, modifier = Modifier.size(18.dp))
                                                },
                                                trailingIcon = {
                                                    if (clientSearchQuery.isNotBlank()) {
                                                        IconButton(onClick = { clientSearchQuery = "" }) {
                                                            Icon(Icons.Filled.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                },
                                                singleLine = true,
                                                shape = RoundedCornerShape(10.dp),
                                                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                                    focusedContainerColor = Color.White,
                                                    unfocusedContainerColor = Color.White
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("check_bill_search_client_input")
                                            )

                                            val filteredClients = remember(clients, clientSearchQuery) {
                                                if (clientSearchQuery.isBlank()) clients.take(6)
                                                else clients.filter {
                                                    it.name.contains(clientSearchQuery, ignoreCase = true) ||
                                                    it.phone.contains(clientSearchQuery, ignoreCase = true) ||
                                                    it.tier.contains(clientSearchQuery, ignoreCase = true) ||
                                                    it.favoriteOrder.contains(clientSearchQuery, ignoreCase = true)
                                                }
                                            }

                                            if (filteredClients.isEmpty()) {
                                                Surface(
                                                    color = Color.White,
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(12.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Text(
                                                            text = "No old client found matching \"$clientSearchQuery\"",
                                                            fontSize = 11.sp,
                                                            color = RetailSlate500
                                                        )
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Button(
                                                            onClick = {
                                                                clientTabMode = "NEW"
                                                                if (clientSearchQuery.any { it.isDigit() }) {
                                                                    newClientPhone = clientSearchQuery
                                                                } else {
                                                                    newClientName = clientSearchQuery
                                                                }
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                                                            shape = RoundedCornerShape(8.dp),
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                                        ) {
                                                            Text("➕ Register \"$clientSearchQuery\" as New Client", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            } else {
                                                Column(
                                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = if (clientSearchQuery.isBlank()) "Select from Client List (អតិថិជន):" else "Matching Clients (${filteredClients.size}):",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = RetailSlate700
                                                    )
                                                    filteredClients.forEach { client ->
                                                        val cColor = try {
                                                            Color(android.graphics.Color.parseColor(client.tierColorHex))
                                                        } catch (_: Exception) {
                                                            RetailTealPrimary
                                                        }
                                                        Surface(
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = Color.White,
                                                            border = androidx.compose.foundation.BorderStroke(1.dp, RetailSlate300.copy(alpha = 0.8f)),
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clickable { selectedClient = client }
                                                                .testTag("select_client_${client.id}")
                                                        ) {
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.SpaceBetween
                                                            ) {
                                                                Row(
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    modifier = Modifier.weight(1f)
                                                                ) {
                                                                    Surface(
                                                                        shape = CircleShape,
                                                                        color = cColor.copy(alpha = 0.15f),
                                                                        modifier = Modifier.size(30.dp)
                                                                    ) {
                                                                        Box(contentAlignment = Alignment.Center) {
                                                                            Text(
                                                                                text = client.name.take(1).uppercase(),
                                                                                fontWeight = FontWeight.Bold,
                                                                                fontSize = 13.sp,
                                                                                color = cColor
                                                                            )
                                                                        }
                                                                    }
                                                                    Spacer(modifier = Modifier.width(8.dp))
                                                                    Column {
                                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                                            Text(
                                                                                text = client.name,
                                                                                fontWeight = FontWeight.Bold,
                                                                                fontSize = 12.sp,
                                                                                color = RetailSlate900
                                                                            )
                                                                            Spacer(modifier = Modifier.width(4.dp))
                                                                            Surface(
                                                                                shape = RoundedCornerShape(3.dp),
                                                                                color = cColor.copy(alpha = 0.12f)
                                                                            ) {
                                                                                Text(
                                                                                    text = client.tierDisplayName,
                                                                                    fontSize = 8.5.sp,
                                                                                    fontWeight = FontWeight.ExtraBold,
                                                                                    color = cColor,
                                                                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                                                                                )
                                                                            }
                                                                        }
                                                                        Text(
                                                                            text = "📞 ${client.phone} • ${client.loyaltyPoints} pts (${client.visitsCount} visits)",
                                                                            fontSize = 10.sp,
                                                                            color = RetailSlate500
                                                                        )
                                                                    }
                                                                }

                                                                Button(
                                                                    onClick = { selectedClient = client },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                                                                    shape = RoundedCornerShape(6.dp),
                                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                                    modifier = Modifier.height(30.dp)
                                                                ) {
                                                                    Text("Select", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Tab 2: FILL NEW CLIENT
                                    if (clientTabMode == "NEW") {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color.White,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCD34D)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "📝 Fill New Client Information (បំពេញអតិថិជនថ្មី)",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = RetailSlate900
                                                )

                                                OutlinedTextField(
                                                    value = newClientName,
                                                    onValueChange = {
                                                        newClientName = it
                                                        newClientError = null
                                                    },
                                                    label = { Text("Client Full Name (ឈ្មោះអតិថិជន) *", fontSize = 11.sp) },
                                                    singleLine = true,
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .testTag("fill_new_client_name_input")
                                                )

                                                OutlinedTextField(
                                                    value = newClientPhone,
                                                    onValueChange = {
                                                        newClientPhone = it
                                                        newClientError = null
                                                    },
                                                    label = { Text("Phone Number (លេខទូរស័ព្ទ) *", fontSize = 11.sp) },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                                    singleLine = true,
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .testTag("fill_new_client_phone_input")
                                                )

                                                // Tier selector chips
                                                Column {
                                                    Text("Membership Tier:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = RetailSlate700)
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        listOf("BRONZE", "SILVER", "GOLD", "VIP").forEach { tier ->
                                                            val isTierSel = newClientTier == tier
                                                            val tierColor = when (tier) {
                                                                "VIP" -> Color(0xFF7C3AED)
                                                                "GOLD" -> Color(0xFFD97706)
                                                                "SILVER" -> Color(0xFF64748B)
                                                                else -> Color(0xFFB45309)
                                                            }
                                                            Surface(
                                                                shape = RoundedCornerShape(6.dp),
                                                                color = if (isTierSel) tierColor else RetailSlate100,
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .clickable { newClientTier = tier }
                                                            ) {
                                                                Text(
                                                                    text = tier,
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    textAlign = TextAlign.Center,
                                                                    color = if (isTierSel) Color.White else RetailSlate700,
                                                                    modifier = Modifier.padding(vertical = 6.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                OutlinedTextField(
                                                    value = newClientNotes,
                                                    onValueChange = { newClientNotes = it },
                                                    label = { Text("Optional Notes or Address", fontSize = 11.sp) },
                                                    singleLine = true,
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                )

                                                if (newClientError != null) {
                                                    Text(
                                                        text = newClientError!!,
                                                        color = Color.Red,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Button(
                                                    onClick = {
                                                        if (newClientName.isBlank()) {
                                                            newClientError = "Please enter client name"
                                                            return@Button
                                                        }
                                                        if (newClientPhone.isBlank()) {
                                                            newClientError = "Please enter client phone number"
                                                            return@Button
                                                        }
                                                        isSavingClient = true
                                                        if (onCreateClient != null) {
                                                            onCreateClient(
                                                                newClientName.trim(),
                                                                newClientPhone.trim(),
                                                                newClientEmail.trim(),
                                                                newClientTier,
                                                                newClientAddress.trim(),
                                                                newClientNotes.trim()
                                                            ) { created ->
                                                                selectedClient = created
                                                                isSavingClient = false
                                                                newClientName = ""
                                                                newClientPhone = ""
                                                                newClientNotes = ""
                                                            }
                                                        } else {
                                                            selectedClient = ClientEntity(
                                                                name = newClientName.trim(),
                                                                phone = newClientPhone.trim(),
                                                                tier = newClientTier,
                                                                notes = newClientNotes.trim()
                                                            )
                                                            isSavingClient = false
                                                        }
                                                    },
                                                    enabled = !isSavingClient,
                                                    colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(44.dp)
                                                        .testTag("save_and_bill_new_client_button")
                                                ) {
                                                    Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Save & Bill to this Client (រក្សាទុក & ចេញវិក្កយបត្រ)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Section 1: Bill Items Review & Edit
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(RetailSlate100.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                                .border(1.dp, RetailSlate300, RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📋 Bill Items (${totalItems} items, ${cart.size} lines)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = RetailSlate900
                                )
                                Text(
                                    text = "Tap item to edit price or discount",
                                    fontSize = 11.sp,
                                    color = RetailSlate500
                                )
                            }

                            // Items List inside Check Bill
                            cart.forEach { item ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White, RoundedCornerShape(10.dp))
                                        .border(1.dp, RetailSlate300.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Promo banner for Free Items to Client
                                    if (item.freeItemsCount > 0) {
                                        Surface(
                                            color = Color(0xFFDCFCE7),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { itemToEditFreeQty = item }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "🎁 FREE TO CLIENT: ${item.freeItemsCount} Free Unit(s) • Tap to type/edit",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF15803D)
                                                )
                                                Text(
                                                    text = "-${formatCurrency(item.b10g1DiscountAmount)}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFF15803D)
                                                )
                                            }
                                        }
                                    } else if (item.quantity >= 10 && item.freeItemsCount == 0) {
                                        Surface(
                                            color = Color(0xFFFEF3C7),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { itemToEditFreeQty = item }
                                        ) {
                                            Text(
                                                text = "🎉 Ordered ${item.quantity} units! Tap here to type free items for client",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF92400E),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        ProductThumbnail(
                                            imageUrl = item.product.imageUrl,
                                            productName = item.product.name,
                                            category = item.product.category,
                                            modifier = Modifier.size(38.dp),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.product.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "Unit: ${formatCurrency(item.unitPrice)}",
                                                    fontSize = 11.sp,
                                                    color = if (item.customUnitPrice != null) RetailTealPrimary else RetailSlate500,
                                                    fontWeight = if (item.customUnitPrice != null) FontWeight.Bold else FontWeight.Normal
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Edit Price",
                                                    fontSize = 10.sp,
                                                    color = RetailTealPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier
                                                        .clickable { itemToEditPrice = item }
                                                        .padding(2.dp)
                                                )
                                            }
                                        }

                                        // Stepper
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = RetailSlate100,
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clickable { onUpdateQty(item.product.id, -1) }
                                            ) {
                                                Icon(
                                                    Icons.Filled.Remove,
                                                    contentDescription = "Decrease",
                                                    modifier = Modifier.padding(5.dp),
                                                    tint = RetailSlate900
                                                )
                                            }
                                            Text(
                                                text = "${item.quantity}",
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp),
                                                fontSize = 13.sp
                                            )
                                            Surface(
                                                shape = CircleShape,
                                                color = RetailSlate100,
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clickable { onUpdateQty(item.product.id, 1) }
                                            ) {
                                                Icon(
                                                    Icons.Filled.Add,
                                                    contentDescription = "Increase",
                                                    modifier = Modifier.padding(5.dp),
                                                    tint = RetailSlate900
                                                )
                                            }
                                        }

                                        // Subtotal & Delete
                                        Column(horizontalAlignment = Alignment.End) {
                                            if (item.itemDiscountAmount > 0) {
                                                Text(
                                                    text = formatCurrency(item.grossSubtotal),
                                                    fontSize = 10.sp,
                                                    color = RetailSlate500,
                                                    style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                                                )
                                            }
                                            Text(
                                                text = formatCurrency(item.subtotal),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (item.itemDiscountAmount > 0) Color(0xFF15803D) else RetailSlate900
                                            )
                                        }

                                        IconButton(
                                            onClick = { onRemoveItem(item.product.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    // Item Discount Quick Chips + B10G1 Promo Chip
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("Item Disc:", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = RetailSlate500)
                                        listOf(0.0, 5.0, 10.0, 15.0).forEach { pct ->
                                            val isSel = !item.isBuy10Get1Free && item.discountPercent == pct
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = if (isSel) RetailTealPrimary else RetailSlate100,
                                                modifier = Modifier.clickable { onUpdateItemDiscount(item.product.id, pct) }
                                            ) {
                                                Text(
                                                    text = "${pct.toInt()}%",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSel) Color.White else RetailSlate700,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        val isCustom = !item.isBuy10Get1Free && item.discountPercent > 0.0 && !listOf(5.0, 10.0, 15.0).contains(item.discountPercent)
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (isCustom) Color(0xFFD97706) else RetailSlate100,
                                            modifier = Modifier.clickable { itemToEditDiscount = item }
                                        ) {
                                            Text(
                                                text = if (isCustom) "${item.discountPercent.toInt()}%" else "Custom%",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCustom) Color.White else Color(0xFFB45309),
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }

                                        // Button to type number for free to Clients
                                        val hasFree = item.freeItemsCount > 0
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (hasFree) Color(0xFF15803D) else Color.White,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (hasFree) Color(0xFF15803D) else Color(0xFF16A34A)),
                                            modifier = Modifier
                                                .clickable { itemToEditFreeQty = item }
                                                .testTag("checkout_free_qty_btn_${item.product.id}")
                                        ) {
                                            Text(
                                                text = if (hasFree) "🎁 Free: ${item.freeItemsCount} to Client" else "🎁 +Free (ថែមជូន)",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (hasFree) Color.White else Color(0xFF166534),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Section 2: Bill Level Percent Discount
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFFBEB), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🏷️ Bill Discount (% Off)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                                if (billDiscountPercent > 0) {
                                    Surface(
                                        color = Color(0xFFFDE68A),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "${billDiscountPercent.toInt()}% BILL OFF ACTIVE (-${formatCurrency(billDiscountAmount)})",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF92400E),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(0.0, 5.0, 10.0, 15.0, 20.0, 25.0).forEach { pct ->
                                    val isSelected = billDiscountPercent == pct
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) Color(0xFFD97706) else Color.White,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFFD97706) else Color(0xFFFCD34D)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onSetBillDiscount(pct) }
                                    ) {
                                        Text(
                                            text = "${pct.toInt()}%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            color = if (isSelected) Color.White else Color(0xFF78350F),
                                            modifier = Modifier.padding(vertical = 6.dp)
                                        )
                                    }
                                }

                                val isCustomBill = billDiscountPercent > 0.0 && !listOf(5.0, 10.0, 15.0, 20.0, 25.0).contains(billDiscountPercent)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isCustomBill) Color(0xFFB45309) else Color.White,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isCustomBill) Color(0xFFB45309) else Color(0xFFFCD34D)),
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .clickable { showCustomBillDiscountDialog = true }
                                ) {
                                    Text(
                                        text = if (isCustomBill) "${billDiscountPercent.toInt()}%" else "Custom%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = if (isCustomBill) Color.White else Color(0xFFB45309),
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // Section 3: Financial Summary Card
                        Surface(
                            color = RetailSlate100,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Gross Subtotal", fontSize = 12.sp, color = RetailSlate500)
                                    Text(formatCurrency(grossSubtotal), fontSize = 12.sp, color = RetailSlate700)
                                }
                                val b10g1TotalSavings = cart.sumOf { it.b10g1DiscountAmount }
                                val b10g1TotalFree = cart.sumOf { it.freeItemsCount }
                                if (b10g1TotalSavings > 0) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("🎁 Free Items to Client ($b10g1TotalFree free unit)", fontSize = 12.sp, color = Color(0xFF15803D), fontWeight = FontWeight.Bold)
                                        Text("-${formatCurrency(b10g1TotalSavings)}", fontSize = 12.sp, color = Color(0xFF15803D), fontWeight = FontWeight.Bold)
                                    }
                                }
                                val otherItemDiscounts = (itemDiscountsTotal - b10g1TotalSavings).coerceAtLeast(0.0)
                                if (otherItemDiscounts > 0) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Item Discounts Total", fontSize = 12.sp, color = Color(0xFF16A34A))
                                        Text("-${formatCurrency(otherItemDiscounts)}", fontSize = 12.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (billDiscountAmount > 0) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Bill Discount (${billDiscountPercent.toInt()}%)", fontSize = 12.sp, color = Color(0xFF16A34A))
                                        Text("-${formatCurrency(billDiscountAmount)}", fontSize = 12.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (totalDiscount > 0) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Total Saved", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                        Text("-${formatCurrency(totalDiscount)}", fontSize = 12.sp, color = Color(0xFF15803D), fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    val taxPctStr = String.format(Locale.US, "%.1f", taxRate * 100.0).removeSuffix(".0")
                                    Text("Sales Tax ($taxPctStr%)", fontSize = 12.sp, color = RetailSlate500)
                                    Text(formatCurrency(taxAmount), fontSize = 12.sp, color = RetailSlate700)
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Grand Total Due", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RetailSlate900)
                                        Text("≈ ${CurrencyUtils.formatKhrRaw(grandTotalKhr)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = RetailSlate700)
                                    }
                                    Text(
                                        text = formatCurrency(grandTotal),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = RetailTealPrimary
                                    )
                                }
                            }
                        }

                        // Section 4: Payment Tender Selector
                        Text("Select Tender Type", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = RetailSlate700)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple("CASH", "Cash (USD/KHR)", Icons.Filled.Money),
                                Triple("KHQR", "Bakong KHQR", Icons.Filled.QrCode),
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

                        // KHQR / Bakong presentation
                        if (selectedMethod == "KHQR") {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("bakong_admin_controls_container")
                            ) {
                                // Bakong QR Payment Controls Component
                                com.example.ui.components.BakongKhqrCard(
                                    usdAmount = grandTotal,
                                    khrRate = khrRate,
                                    merchantName = merchantName,
                                    bakongAccountId = bakongAccountId,
                                    billNumber = "BILL-${System.currentTimeMillis() % 100000}",
                                    onConfirmPaid = {
                                        onConfirmPayment("KHQR", grandTotal, selectedClient)
                                    }
                                )
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
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Bottom Action Buttons
                val canConfirm = when (selectedMethod) {
                    "KHQR" -> cart.isNotEmpty()
                    "CARD" -> cart.isNotEmpty()
                    else -> cart.isNotEmpty() && (
                            (tenderCurrency == "KHR" && (amountTenderedText.filter { it.isDigit() }.toLongOrNull() ?: 0L) >= grandTotalKhr) ||
                            (tenderCurrency == "USD" && tenderedUsd >= grandTotal - 0.001)
                    )
                }

                if (selectedClient != null) {
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Billed to: ${selectedClient!!.name} (${selectedClient!!.phone}) • ${selectedClient!!.tierDisplayName}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { onConfirmPayment(selectedMethod, tenderedUsd, selectedClient) },
                        enabled = canConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(2f)
                            .height(48.dp)
                            .testTag("confirm_payment_button")
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Confirm", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedClient != null) "Complete Sale for ${selectedClient!!.name} (${formatCurrency(grandTotal)})" else "Complete Payment (${formatCurrency(grandTotal)})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
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
