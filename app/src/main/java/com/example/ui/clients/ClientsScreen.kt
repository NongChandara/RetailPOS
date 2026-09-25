package com.example.ui.clients

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Loyalty
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClientEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.RetailSlate100
import com.example.ui.theme.RetailSlate300
import com.example.ui.theme.RetailSlate500
import com.example.ui.theme.RetailSlate700
import com.example.ui.theme.RetailSlate900
import com.example.ui.theme.RetailStatusGreen
import com.example.ui.theme.RetailStatusGreenContainer
import com.example.ui.theme.RetailTealPrimary

private val BrandGreen = Color(0xFF0F4D2A)
private val BrandGold = Color(0xFFD97706)
private val BrandPurple = Color(0xFF7C3AED)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clients by viewModel.filteredClients.collectAsState()
    val allClients by viewModel.allClients.collectAsState()
    val searchQuery by viewModel.clientSearchQuery.collectAsState()
    val activeTierFilter by viewModel.clientTierFilter.collectAsState()
    val selectedClient by viewModel.selectedClient.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var clientToEdit by remember { mutableStateOf<ClientEntity?>(null) }
    var clientToDelete by remember { mutableStateOf<ClientEntity?>(null) }
    var clientForPoints by remember { mutableStateOf<ClientEntity?>(null) }

    // Summary calculations
    val totalClients = allClients.size
    val vipCount = allClients.count { it.tier.equals("VIP", ignoreCase = true) }
    val goldCount = allClients.count { it.tier.equals("GOLD", ignoreCase = true) }
    val totalPointsCirculating = allClients.sumOf { it.loyaltyPoints }

    Box(modifier = modifier.fillMaxSize().background(RetailSlate100)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = BrandGreen.copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.Badge,
                                    contentDescription = null,
                                    tint = BrandGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Client Profiles",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = RetailSlate900
                            )
                            Text(
                                text = "កម្រងព័ត៌មានអតិថិជន • Loyalty & Preferences",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = BrandGreen
                            )
                        }
                    }
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("btn_add_client")
                ) {
                    Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Client", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stat Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ClientStatCard(
                    title = "Total Clients",
                    value = "$totalClients",
                    sub = "Registered",
                    icon = Icons.Filled.Person,
                    color = BrandGreen,
                    modifier = Modifier.weight(1f)
                )
                ClientStatCard(
                    title = "VIP & Gold",
                    value = "${vipCount + goldCount}",
                    sub = "$vipCount VIP • $goldCount Gold",
                    icon = Icons.Filled.WorkspacePremium,
                    color = BrandGold,
                    modifier = Modifier.weight(1f)
                )
                ClientStatCard(
                    title = "Loyalty Pool",
                    value = "$totalPointsCirculating",
                    sub = "Points Issued",
                    icon = Icons.Filled.Loyalty,
                    color = BrandPurple,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setClientSearchQuery(it) },
                placeholder = { Text("Search by name, phone, email, or order preference...") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = RetailSlate500)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setClientSearchQuery("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear search", tint = RetailSlate500)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandGreen,
                    unfocusedBorderColor = RetailSlate300,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_client_search")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tier Filter Chips
            val tiers = listOf("ALL", "VIP", "GOLD", "SILVER", "BRONZE")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(tiers) { tier ->
                    val isSelected = activeTierFilter == tier
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setClientTierFilter(tier) },
                        label = {
                            val count = if (tier == "ALL") allClients.size else allClients.count { it.tier.equals(tier, ignoreCase = true) }
                            Text(
                                text = when (tier) {
                                    "ALL" -> "All ($count)"
                                    "VIP" -> "⭐ VIP ($count)"
                                    "GOLD" -> "👑 Gold ($count)"
                                    "SILVER" -> "🥈 Silver ($count)"
                                    "BRONZE" -> "🥉 Bronze ($count)"
                                    else -> "$tier ($count)"
                                },
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandGreen,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = RetailSlate700
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) BrandGreen else RetailSlate300,
                            enabled = true,
                            selected = isSelected
                        ),
                        modifier = Modifier.testTag("chip_tier_$tier")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Clients List
            if (clients.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = RetailSlate300.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = RetailSlate500,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No clients matching \"$searchQuery\"" else "No clients in this category",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RetailSlate700
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap '+ New Client' to create a customer loyalty profile",
                            fontSize = 12.sp,
                            color = RetailSlate500
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("clients_list")
                ) {
                    items(clients, key = { it.id }) { client ->
                        ClientItemCard(
                            client = client,
                            viewModel = viewModel,
                            onViewDetails = { viewModel.selectClient(client) },
                            onEdit = { clientToEdit = client },
                            onDelete = { clientToDelete = client },
                            onAdjustPoints = { clientForPoints = client },
                            onCall = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${client.phone}")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Cannot open dialer: ${client.phone}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onCopyPhone = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Client Phone", client.phone)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied ${client.phone}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Client Dialog
    if (showAddDialog) {
        ClientFormDialog(
            title = "New Client Profile",
            initialClient = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, email, tier, points, favorite, notes, address ->
                viewModel.createClient(
                    name = name,
                    phone = phone,
                    email = email,
                    tier = tier,
                    initialPoints = points,
                    favoriteOrder = favorite,
                    notes = notes,
                    address = address
                )
                showAddDialog = false
            }
        )
    }

    // Edit Client Dialog
    clientToEdit?.let { client ->
        ClientFormDialog(
            title = "Edit Client Profile",
            initialClient = client,
            onDismiss = { clientToEdit = null },
            onConfirm = { name, phone, email, tier, points, favorite, notes, address ->
                viewModel.updateClient(
                    client.copy(
                        name = name,
                        phone = phone,
                        email = email,
                        tier = tier,
                        loyaltyPoints = points,
                        favoriteOrder = favorite,
                        notes = notes,
                        address = address
                    )
                )
                clientToEdit = null
            }
        )
    }

    // Adjust Points Dialog
    clientForPoints?.let { client ->
        AdjustLoyaltyPointsDialog(
            client = client,
            onDismiss = { clientForPoints = null },
            onConfirm = { delta, reason ->
                viewModel.adjustClientPoints(client, delta, reason)
                clientForPoints = null
            }
        )
    }

    // Delete Confirmation Dialog
    clientToDelete?.let { client ->
        AlertDialog(
            onDismissRequest = { clientToDelete = null },
            title = { Text("Delete Client Profile?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to delete '${client.name}' (${client.phone})? This client profile and accumulated ${client.loyaltyPoints} loyalty points will be permanently deleted.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteClient(client)
                        clientToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { clientToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Selected Client Detail Sheet / Dialog
    selectedClient?.let { client ->
        ClientDetailsDialog(
            client = client,
            viewModel = viewModel,
            onDismiss = { viewModel.selectClient(null) },
            onEdit = {
                clientToEdit = client
                viewModel.selectClient(null)
            },
            onAdjustPoints = {
                clientForPoints = client
            }
        )
    }
}

@Composable
fun ClientStatCard(
    title: String,
    value: String,
    sub: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 11.sp, color = RetailSlate500, fontWeight = FontWeight.Medium)
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = RetailSlate900)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = sub, fontSize = 10.sp, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ClientItemCard(
    client: ClientEntity,
    viewModel: MainViewModel,
    onViewDetails: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAdjustPoints: () -> Unit,
    onCall: () -> Unit,
    onCopyPhone: () -> Unit
) {
    val tierColor = when (client.tier.uppercase()) {
        "VIP" -> BrandPurple
        "GOLD" -> BrandGold
        "SILVER" -> Color(0xFF64748B)
        else -> Color(0xFFB45309) // Bronze
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() }
            .testTag("client_card_${client.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Avatar + Name + Tier Badge + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Avatar with Tier border
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = tierColor.copy(alpha = 0.12f),
                    border = BorderStroke(2.dp, tierColor)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = client.name.firstOrNull()?.uppercase() ?: "C",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = tierColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name & Phone
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = client.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = RetailSlate900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Phone,
                            contentDescription = null,
                            tint = RetailSlate500,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = client.phone,
                            fontSize = 12.sp,
                            color = RetailSlate700,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Tier Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = tierColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, tierColor.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    Text(
                        text = client.tier.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = tierColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Middle Stats Row: Loyalty Points + Total Spent + Total Visits
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = RetailSlate100,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Loyalty Points with quick button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onAdjustPoints() }
                    ) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = BrandGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "${client.loyaltyPoints} pts",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandGold
                            )
                            Text(
                                text = "Loyalty (+/-)",
                                fontSize = 9.sp,
                                color = RetailSlate500
                            )
                        }
                    }

                    // Total Spent
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = viewModel.formatUsd(client.totalSpent),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandGreen
                        )
                        Text(
                            text = viewModel.formatKhr(client.totalSpent),
                            fontSize = 9.sp,
                            color = RetailSlate500
                        )
                    }

                    // Visits
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${client.visitsCount} visits",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = RetailSlate900
                        )
                        Text(
                            text = "Activity",
                            fontSize = 9.sp,
                            color = RetailSlate500
                        )
                    }
                }
            }

            // Favorite Drink / Note preview if present
            if (client.favoriteOrder.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrandGreen.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text("☕", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = client.favoriteOrder,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = BrandGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Call button
                IconButton(
                    onClick = onCall,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.Call,
                        contentDescription = "Call",
                        tint = BrandGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Copy phone
                IconButton(
                    onClick = onCopyPhone,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.ContentCopy,
                        contentDescription = "Copy phone",
                        tint = RetailSlate500,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Points action button
                OutlinedButton(
                    onClick = onAdjustPoints,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.6f)),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("+/- Points", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandGold)
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Edit button
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = RetailSlate700,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ClientFormDialog(
    title: String,
    initialClient: ClientEntity?,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        phone: String,
        email: String,
        tier: String,
        points: Int,
        favorite: String,
        notes: String,
        address: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(initialClient?.name ?: "") }
    var phone by remember { mutableStateOf(initialClient?.phone ?: "") }
    var email by remember { mutableStateOf(initialClient?.email ?: "") }
    var tier by remember { mutableStateOf(initialClient?.tier ?: "BRONZE") }
    var pointsText by remember { mutableStateOf((initialClient?.loyaltyPoints ?: 0).toString()) }
    var favoriteOrder by remember { mutableStateOf(initialClient?.favoriteOrder ?: "") }
    var notes by remember { mutableStateOf(initialClient?.notes ?: "") }
    var address by remember { mutableStateOf(initialClient?.address ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = BrandGreen.copy(alpha = 0.12f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; errorMessage = null },
                        label = { Text("Full Name (ឈ្មោះអតិថិជន) *") },
                        placeholder = { Text("e.g. Sok Dara (សុខ តារា)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_client_name")
                    )
                }

                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it; errorMessage = null },
                        label = { Text("Phone Number (លេខទូរស័ព្ទ) *") },
                        placeholder = { Text("e.g. 012 345 678") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_client_phone")
                    )
                }

                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email (Optional)") },
                        placeholder = { Text("client@gmail.com") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Member Tier (កម្រិតសមាជិក)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RetailSlate700)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("BRONZE", "SILVER", "GOLD", "VIP").forEach { itemTier ->
                            val isSelected = tier == itemTier
                            FilterChip(
                                selected = isSelected,
                                onClick = { tier = itemTier },
                                label = {
                                    Text(
                                        when (itemTier) {
                                            "VIP" -> "⭐ VIP"
                                            "GOLD" -> "👑 Gold"
                                            "SILVER" -> "🥈 Silver"
                                            else -> "🥉 Bronze"
                                        },
                                        fontSize = 11.sp
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = pointsText,
                        onValueChange = { pointsText = it.filter { char -> char.isDigit() } },
                        label = { Text("Loyalty Points (ពិន្ទុសន្សំ)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = favoriteOrder,
                        onValueChange = { favoriteOrder = it },
                        label = { Text("Favorite Order / Taste (កាហ្វេដែលចូលចិត្ត)") },
                        placeholder = { Text("e.g. Iced Latte 50% sugar, oat milk") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Branch / Area") },
                        placeholder = { Text("e.g. BKK1, Phnom Penh") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Preferences") },
                        placeholder = { Text("Special dietary preferences, regular visiting times...") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (errorMessage != null) {
                    item {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Please enter the client's name"
                        return@Button
                    }
                    if (phone.isBlank()) {
                        errorMessage = "Please enter the client's phone number"
                        return@Button
                    }
                    val points = pointsText.toIntOrNull() ?: 0
                    onConfirm(name, phone, email, tier, points, favoriteOrder, notes, address)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
            ) {
                Text(if (initialClient == null) "Create Profile" else "Save Changes")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AdjustLoyaltyPointsDialog(
    client: ClientEntity,
    onDismiss: () -> Unit,
    onConfirm: (delta: Int, reason: String) -> Unit
) {
    var deltaText by remember { mutableStateOf("10") }
    var isAdding by remember { mutableStateOf(true) }
    var reason by remember { mutableStateOf("Manual adjustment / promotion") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Loyalty, contentDescription = null, tint = BrandGold, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Loyalty Points: ${client.name}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Current Balance Banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BrandGold.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Current Balance:", fontWeight = FontWeight.Medium, color = RetailSlate700)
                        Text("${client.loyaltyPoints} Points", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = BrandGold)
                    }
                }

                // Add or Redeem Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isAdding = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAdding) BrandGreen else RetailSlate100,
                            contentColor = if (isAdding) Color.White else RetailSlate700
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+ Add Points", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { isAdding = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isAdding) MaterialTheme.colorScheme.error else RetailSlate100,
                            contentColor = if (!isAdding) Color.White else RetailSlate700
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("- Redeem", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // Quick presets
                Text("Quick Presets:", fontSize = 11.sp, color = RetailSlate500, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(10, 25, 50, 100).forEach { preset ->
                        OutlinedButton(
                            onClick = {
                                deltaText = preset.toString()
                                reason = if (isAdding) "Earned $preset points bonus" else "Redeemed $preset points reward"
                            },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("$preset pts", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Custom Points input
                OutlinedTextField(
                    value = deltaText,
                    onValueChange = { deltaText = it.filter { char -> char.isDigit() }; errorMessage = null },
                    label = { Text(if (isAdding) "Points to Add" else "Points to Redeem") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Reason input
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason (for audit trail)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = deltaText.toIntOrNull() ?: 0
                    if (amount <= 0) {
                        errorMessage = "Please enter points greater than 0"
                        return@Button
                    }
                    if (!isAdding && amount > client.loyaltyPoints) {
                        errorMessage = "Cannot redeem more than client's balance (${client.loyaltyPoints} pts)"
                        return@Button
                    }
                    val finalDelta = if (isAdding) amount else -amount
                    onConfirm(finalDelta, reason)
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isAdding) BrandGreen else MaterialTheme.colorScheme.error)
            ) {
                Text(if (isAdding) "Confirm Add Points" else "Confirm Redemption")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ClientDetailsDialog(
    client: ClientEntity,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onAdjustPoints: () -> Unit
) {
    val context = LocalContext.current
    val tierColor = when (client.tier.uppercase()) {
        "VIP" -> BrandPurple
        "GOLD" -> BrandGold
        "SILVER" -> Color(0xFF64748B)
        else -> Color(0xFFB45309)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = tierColor.copy(alpha = 0.15f),
                        border = BorderStroke(2.dp, tierColor)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = client.name.firstOrNull()?.uppercase() ?: "C",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = tierColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(client.name, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                        Text(client.tierDisplayName, fontSize = 11.sp, color = tierColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Loyalty Card Box
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = tierColor.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, tierColor.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("LOYALTY BALANCE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = tierColor)
                                    Text("${client.loyaltyPoints} PTS", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = tierColor)
                                }
                                Button(
                                    onClick = onAdjustPoints,
                                    colors = ButtonDefaults.buttonColors(containerColor = tierColor),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("+/- Points", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Contact & Info
                item {
                    DetailRow(icon = Icons.Filled.Phone, label = "Phone", value = client.phone) {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Phone", client.phone))
                        Toast.makeText(context, "Copied phone number", Toast.LENGTH_SHORT).show()
                    }
                }

                if (client.email.isNotBlank()) {
                    item {
                        DetailRow(icon = Icons.Filled.Person, label = "Email", value = client.email)
                    }
                }

                if (client.address.isNotBlank()) {
                    item {
                        DetailRow(icon = Icons.Filled.Place, label = "Branch/Area", value = client.address)
                    }
                }

                // Stats
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = RetailSlate300.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Spent", fontSize = 11.sp, color = RetailSlate500)
                            Text(viewModel.formatUsd(client.totalSpent), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BrandGreen)
                            Text(viewModel.formatKhr(client.totalSpent), fontSize = 10.sp, color = RetailSlate500)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Visits", fontSize = 11.sp, color = RetailSlate500)
                            Text("${client.visitsCount} times", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = RetailSlate900)
                        }
                    }
                }

                // Favorite order
                if (client.favoriteOrder.isNotBlank()) {
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = RetailSlate300.copy(alpha = 0.5f))
                        Text("☕ Favorite Order & Preferences", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandGreen)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BrandGreen.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            Text(
                                text = client.favoriteOrder,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = BrandGreen,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                // Notes
                if (client.notes.isNotBlank()) {
                    item {
                        Text("Staff Notes", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RetailSlate700)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = RetailSlate100,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            Text(
                                text = client.notes,
                                fontSize = 12.sp,
                                color = RetailSlate700,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onEdit,
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
            ) {
                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Edit Profile")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onCopy: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onCopy != null) Modifier.clickable { onCopy() } else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = null, tint = RetailSlate500, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(label, fontSize = 10.sp, color = RetailSlate500)
                Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = RetailSlate900)
            }
        }
        if (onCopy != null) {
            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = RetailSlate500, modifier = Modifier.size(14.dp))
        }
    }
}
