package com.example.ui.inventory

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StockMovementEntity
import com.example.ui.MainViewModel
import com.example.ui.components.SecureActivityLogView
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDateTime
import com.example.ui.theme.RetailSlate100
import com.example.ui.theme.RetailSlate300
import com.example.ui.theme.RetailSlate500
import com.example.ui.theme.RetailSlate700
import com.example.ui.theme.RetailSlate900
import com.example.ui.theme.RetailStatusAmber
import com.example.ui.theme.RetailStatusAmberContainer
import com.example.ui.theme.RetailStatusBlue
import com.example.ui.theme.RetailStatusBlueContainer
import com.example.ui.theme.RetailStatusGreen
import com.example.ui.theme.RetailStatusGreenContainer
import com.example.ui.theme.RetailStatusRed
import com.example.ui.theme.RetailStatusRedContainer
import com.example.ui.theme.RetailTealLight
import com.example.ui.theme.RetailTealPrimary

@Composable
fun InventoryTrackingScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.allProducts.collectAsState()
    val movements by viewModel.allMovements.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") }

    val totalCostValue = products.sumOf { it.costPrice * it.stockQuantity }
    val totalRetailValue = products.sumOf { it.sellingPrice * it.stockQuantity }
    val potentialProfit = (totalRetailValue - totalCostValue).coerceAtLeast(0.0)

    val filteredMovements = movements.filter { mov ->
        when (selectedFilter) {
            "SALE" -> mov.type.contains("SALE", ignoreCase = true)
            "RESTOCK" -> mov.type.contains("RESTOCK", ignoreCase = true)
            "ADJUSTMENT" -> !mov.type.contains("SALE", ignoreCase = true) && !mov.type.contains("RESTOCK", ignoreCase = true)
            else -> true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("inventory_tracking_screen")
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Live Inventory Valuation Hero Card
        Surface(
            color = RetailSlate900,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Real-Time Stock Valuation",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Surface(
                        color = RetailTealPrimary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "LIVE AUDIT",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Cost Value", color = RetailSlate500, fontSize = 11.sp)
                        Text(
                            formatCurrency(totalCostValue),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Column {
                        Text("Retail Value", color = RetailSlate500, fontSize = 11.sp)
                        Text(
                            formatCurrency(totalRetailValue),
                            color = RetailTealLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Column {
                        Text("Gross Margin", color = RetailSlate500, fontSize = 11.sp)
                        Text(
                            formatCurrency(potentialProfit),
                            color = RetailStatusGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        var currentTrackingMode by remember { mutableStateOf("MOVEMENTS") }

        // Switch between Physical Stock Movements and Secure Cryptographic Audit Trail
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(RetailSlate100)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                color = if (currentTrackingMode == "MOVEMENTS") Color.White else Color.Transparent,
                shape = RoundedCornerShape(9.dp),
                shadowElevation = if (currentTrackingMode == "MOVEMENTS") 1.dp else 0.dp,
                modifier = Modifier
                    .weight(1f)
                    .clickable { currentTrackingMode = "MOVEMENTS" }
                    .testTag("subtab_movements")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.History,
                        contentDescription = null,
                        tint = if (currentTrackingMode == "MOVEMENTS") RetailTealPrimary else RetailSlate500,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Stock Movements",
                        fontSize = 12.sp,
                        fontWeight = if (currentTrackingMode == "MOVEMENTS") FontWeight.Bold else FontWeight.Medium,
                        color = if (currentTrackingMode == "MOVEMENTS") RetailSlate900 else RetailSlate500
                    )
                }
            }

            Surface(
                color = if (currentTrackingMode == "AUDIT") Color.White else Color.Transparent,
                shape = RoundedCornerShape(9.dp),
                shadowElevation = if (currentTrackingMode == "AUDIT") 1.dp else 0.dp,
                modifier = Modifier
                    .weight(1f)
                    .clickable { currentTrackingMode = "AUDIT" }
                    .testTag("subtab_audit")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Shield,
                        contentDescription = null,
                        tint = if (currentTrackingMode == "AUDIT") RetailTealPrimary else RetailSlate500,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Secure Audit Log",
                        fontSize = 12.sp,
                        fontWeight = if (currentTrackingMode == "AUDIT") FontWeight.Bold else FontWeight.Medium,
                        color = if (currentTrackingMode == "AUDIT") RetailSlate900 else RetailSlate500
                    )
                }
            }
        }

        if (currentTrackingMode == "AUDIT") {
            SecureActivityLogView(viewModel = viewModel, modifier = Modifier.weight(1f))
        } else {
            // Movement Filter Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Pair("ALL", "All Logs (${movements.size})"),
                    Pair("SALE", "Sales"),
                    Pair("RESTOCK", "Restocks"),
                    Pair("ADJUSTMENT", "Adjustments")
                ).forEach { (key, label) ->
                    val isSelected = selectedFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = key },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RetailTealPrimary,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Movement Audit List
            if (filteredMovements.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.History, contentDescription = null, tint = RetailSlate300, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No stock movements recorded yet", color = RetailSlate500, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 20.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredMovements, key = { it.id }) { movement ->
                        MovementItemCard(movement = movement)
                    }
                }
            }
        }
    }
}

@Composable
fun MovementItemCard(movement: StockMovementEntity) {
    val isPositive = movement.quantityDelta > 0
    val (typeBg, typeColor) = when {
        movement.type.contains("SALE") -> Pair(RetailStatusBlueContainer, RetailStatusBlue)
        movement.type.contains("RESTOCK") -> Pair(RetailStatusGreenContainer, RetailStatusGreen)
        movement.type.contains("DAMAGE") -> Pair(RetailStatusRedContainer, RetailStatusRed)
        else -> Pair(RetailStatusAmberContainer, RetailStatusAmber)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("movement_card_${movement.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(typeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPositive) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                        contentDescription = null,
                        tint = typeColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = movement.productName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = RetailSlate900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = movement.reason.ifBlank { "Stock Adjustment" },
                        fontSize = 11.sp,
                        color = RetailSlate500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${formatDateTime(movement.timestamp)} • by ${movement.userName}",
                        fontSize = 10.sp,
                        color = RetailSlate500
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Text(
                    text = if (isPositive) "+${movement.quantityDelta}" else "${movement.quantityDelta}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = if (isPositive) RetailStatusGreen else RetailStatusRed
                )
                Spacer(modifier = Modifier.height(3.dp))
                Surface(
                    color = RetailSlate100,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Balance: ${movement.resultingStock}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RetailSlate700,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
