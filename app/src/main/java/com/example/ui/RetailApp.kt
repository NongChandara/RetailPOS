package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.clients.ClientsScreen
import com.example.ui.components.CurrencySettingsDialog
import com.example.ui.components.LowStockAlertSettingsDialog
import com.example.ui.components.SecureActivityLogView
import com.example.ui.inventory.InventoryTrackingScreen
import com.example.ui.pos.PosScreen
import com.example.ui.sales.SalesHistoryScreen
import com.example.ui.stock.StockScreen
import com.example.util.CurrencyMode
import com.example.ui.theme.RetailSlate100
import com.example.ui.theme.RetailSlate300
import com.example.ui.theme.RetailSlate500
import com.example.ui.theme.RetailSlate700
import com.example.ui.theme.RetailSlate900
import com.example.ui.theme.RetailStatusAmber
import com.example.ui.theme.RetailStatusAmberContainer
import com.example.ui.theme.RetailTealLight
import com.example.ui.theme.RetailTealPrimary
import com.example.ui.users.UsersScreen

enum class RetailTab(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
) {
    POS("POS", Icons.Filled.PointOfSale, "tab_pos"),
    STOCK("Stock", Icons.Filled.Inventory2, "tab_stock"),
    TRACKING("Tracking", Icons.Filled.Timeline, "tab_tracking"),
    SALES("Sales", Icons.Filled.ReceiptLong, "tab_sales"),
    CLIENTS("Clients", Icons.Filled.PersonPin, "tab_clients"),
    USERS("Staff", Icons.Filled.People, "tab_users")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetailApp(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(RetailTab.POS) }
    val currentUser by viewModel.currentUser.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()
    val currencyMode by viewModel.currencyMode.collectAsState()
    val khrExchangeRate by viewModel.khrExchangeRate.collectAsState()
    val isMonitoringActive by viewModel.isMonitoringActive.collectAsState()
    val thresholdOverride by viewModel.lowStockThresholdOverride.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showLowStockDialog by remember { mutableStateOf(false) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToastMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape),
                            color = Color.White,
                            shadowElevation = 1.dp
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_tr_coffee_logo),
                                contentDescription = "TR Coffee Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "TR Coffee",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = RetailSlate900
                                )
                                Text(
                                    text = "®",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color(0xFFD32F2F),
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            }
                            Text(
                                text = "កាហ្វេ ទីរ៉ូ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F4D2A)
                            )
                        }
                    }
                },
                actions = {
                    // Currency Switcher Pill (USD / KHR / Dual)
                    Surface(
                        color = RetailTealPrimary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .clickable { showCurrencyDialog = true }
                            .padding(end = 6.dp)
                            .testTag("currency_switcher_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currencyMode.symbol,
                                color = RetailTealPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = currencyMode.label,
                                color = RetailTealPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Low Stock Alert Pill
                    Surface(
                        color = if (lowStockProducts.isNotEmpty()) RetailStatusAmberContainer else RetailSlate100,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .clickable { showLowStockDialog = true }
                            .padding(end = 6.dp)
                            .testTag("low_stock_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (lowStockProducts.isNotEmpty()) Icons.Filled.Warning else Icons.Filled.NotificationsActive,
                                contentDescription = "Alert",
                                tint = if (lowStockProducts.isNotEmpty()) RetailStatusAmber else RetailSlate500,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (lowStockProducts.isNotEmpty()) "${lowStockProducts.size} Low" else "Alerts",
                                color = if (lowStockProducts.isNotEmpty()) RetailStatusAmber else RetailSlate700,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Active Cashier Pill
                    Surface(
                        color = RetailSlate100,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .clickable { currentTab = RetailTab.USERS }
                            .padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(RetailTealPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser?.name?.firstOrNull()?.uppercase() ?: "U",
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = currentUser?.name?.split(" ")?.firstOrNull() ?: "Staff",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RetailSlate900
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                RetailTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            if (tab == RetailTab.STOCK && lowStockProducts.isNotEmpty()) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = RetailStatusAmber) {
                                            Text("${lowStockProducts.size}")
                                        }
                                    }
                                ) {
                                    Icon(tab.icon, contentDescription = tab.title)
                                }
                            } else {
                                Icon(tab.icon, contentDescription = tab.title)
                            }
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RetailTealPrimary,
                            selectedTextColor = RetailTealPrimary,
                            indicatorColor = Color(0xFFCCFBF1),
                            unselectedIconColor = RetailSlate700,
                            unselectedTextColor = RetailSlate700
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                RetailTab.POS -> PosScreen(viewModel = viewModel)
                RetailTab.STOCK -> StockScreen(viewModel = viewModel)
                RetailTab.TRACKING -> InventoryTrackingScreen(viewModel = viewModel)
                RetailTab.SALES -> SalesHistoryScreen(viewModel = viewModel)
                RetailTab.CLIENTS -> ClientsScreen(viewModel = viewModel)
                RetailTab.USERS -> UsersScreen(viewModel = viewModel)
            }
        }

        if (showCurrencyDialog) {
            CurrencySettingsDialog(
                viewModel = viewModel,
                currentMode = currencyMode,
                currentRate = khrExchangeRate,
                onDismiss = { showCurrencyDialog = false }
            )
        }

        if (showLowStockDialog) {
            LowStockAlertSettingsDialog(
                viewModel = viewModel,
                isMonitoringActive = isMonitoringActive,
                currentThresholdOverride = thresholdOverride,
                onDismiss = { showLowStockDialog = false }
            )
        }
    }
}
