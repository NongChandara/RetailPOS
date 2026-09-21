package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ActivityCategory
import com.example.data.model.ActivityLogEntity
import com.example.ui.MainViewModel
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
import com.example.ui.theme.RetailStatusPurple
import com.example.ui.theme.RetailStatusPurpleContainer
import com.example.ui.theme.RetailStatusRed
import com.example.ui.theme.RetailStatusRedContainer
import com.example.ui.theme.RetailTealLight
import com.example.ui.theme.RetailTealPrimary

@Composable
fun SecureActivityLogView(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val filteredLogs by viewModel.filteredActivityLogs.collectAsState()
    val allLogs by viewModel.allActivityLogs.collectAsState()
    val users by viewModel.allUsers.collectAsState()
    val categoryFilter by viewModel.activityCategoryFilter.collectAsState()
    val staffFilter by viewModel.activityStaffFilter.collectAsState()
    val searchQuery by viewModel.activitySearchQuery.collectAsState()

    var selectedLogForDetails by remember { mutableStateOf<ActivityLogEntity?>(null) }
    var verificationResultDialog by remember { mutableStateOf<MainViewModel.IntegrityVerificationResult?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("secure_activity_log_view")
    ) {
        // Cryptographic Security Header Hero Card
        Surface(
            color = RetailSlate900,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("audit_security_hero_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(RetailTealPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Shield,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Secure Audit Trail",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "SHA-256 Tamper-Evident • Staff ID Tracked",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Action Button: Verify Integrity
                    Button(
                        onClick = {
                            verificationResultDialog = viewModel.verifyAllLogsIntegrity()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F766E),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("verify_integrity_btn")
                    ) {
                        Icon(
                            Icons.Filled.VerifiedUser,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Verify Hashes",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val posCount = allLogs.count { it.category == ActivityCategory.POS_TRANSACTION.code }
                    val invCount = allLogs.count { it.category == ActivityCategory.INVENTORY_MODIFICATION.code }
                    val staffCount = allLogs.count { it.category == ActivityCategory.STAFF_SECURITY.code }

                    SecurityMetricPill(
                        label = "POS Sales",
                        value = "$posCount",
                        accentColor = RetailStatusGreen,
                        modifier = Modifier.weight(1f)
                    )
                    SecurityMetricPill(
                        label = "Inventory Mods",
                        value = "$invCount",
                        accentColor = RetailStatusBlue,
                        modifier = Modifier.weight(1f)
                    )
                    SecurityMetricPill(
                        label = "Staff & Sec",
                        value = "$staffCount",
                        accentColor = RetailStatusAmber,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setActivitySearchQuery(it) },
            placeholder = { Text("Search logs by receipt #, SKU, staff, action...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = null,
                    tint = RetailSlate500,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { viewModel.setActivitySearchQuery("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RetailTealPrimary,
                unfocusedBorderColor = RetailSlate300,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .testTag("activity_search_input")
        )

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = categoryFilter == "ALL",
                onClick = { viewModel.setActivityCategoryFilter("ALL") },
                label = { Text("All Activities (${allLogs.size})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RetailTealPrimary,
                    selectedLabelColor = Color.White
                ),
                modifier = Modifier.testTag("filter_cat_all")
            )
            FilterChip(
                selected = categoryFilter == ActivityCategory.POS_TRANSACTION.code,
                onClick = { viewModel.setActivityCategoryFilter(ActivityCategory.POS_TRANSACTION.code) },
                leadingIcon = {
                    Icon(Icons.Filled.PointOfSale, contentDescription = null, modifier = Modifier.size(14.dp))
                },
                label = { Text("POS Transactions", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RetailStatusGreen,
                    selectedLabelColor = Color.White
                ),
                modifier = Modifier.testTag("filter_cat_pos")
            )
            FilterChip(
                selected = categoryFilter == ActivityCategory.INVENTORY_MODIFICATION.code,
                onClick = { viewModel.setActivityCategoryFilter(ActivityCategory.INVENTORY_MODIFICATION.code) },
                leadingIcon = {
                    Icon(Icons.Filled.Inventory2, contentDescription = null, modifier = Modifier.size(14.dp))
                },
                label = { Text("Inventory Modifications", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RetailStatusBlue,
                    selectedLabelColor = Color.White
                ),
                modifier = Modifier.testTag("filter_cat_inventory")
            )
            FilterChip(
                selected = categoryFilter == ActivityCategory.STAFF_SECURITY.code,
                onClick = { viewModel.setActivityCategoryFilter(ActivityCategory.STAFF_SECURITY.code) },
                leadingIcon = {
                    Icon(Icons.Filled.Security, contentDescription = null, modifier = Modifier.size(14.dp))
                },
                label = { Text("Staff & Security", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RetailStatusAmber,
                    selectedLabelColor = Color.White
                ),
                modifier = Modifier.testTag("filter_cat_staff")
            )
        }

        // Staff Member Filter (Tracking by Staff User ID)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Staff ID:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = RetailSlate500,
                modifier = Modifier.padding(end = 2.dp)
            )

            // All Staff Option
            Surface(
                color = if (staffFilter == null) RetailSlate900 else RetailSlate100,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .clickable { viewModel.setActivityStaffFilter(null) }
                    .testTag("filter_staff_all")
            ) {
                Text(
                    text = "All Staff",
                    color = if (staffFilter == null) Color.White else RetailSlate700,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            // User-specific pills
            users.forEach { user ->
                val isSelected = staffFilter == user.id
                Surface(
                    color = if (isSelected) RetailTealPrimary else RetailSlate100,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .clickable {
                            viewModel.setActivityStaffFilter(if (isSelected) null else user.id)
                        }
                        .testTag("filter_staff_${user.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White else RetailTealPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.name.firstOrNull()?.uppercase() ?: "U",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) RetailTealPrimary else Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${user.name.split(" ").first()} (ID #${user.id})",
                            color = if (isSelected) Color.White else RetailSlate900,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Active filter status summary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredLogs.size} logs recorded",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = RetailSlate700
            )

            if (staffFilter != null || categoryFilter != "ALL" || searchQuery.isNotBlank()) {
                Text(
                    text = "Reset filters",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RetailTealPrimary,
                    modifier = Modifier
                        .clickable {
                            viewModel.setActivityCategoryFilter("ALL")
                            viewModel.setActivityStaffFilter(null)
                            viewModel.setActivitySearchQuery("")
                        }
                        .testTag("reset_filters_btn")
                )
            }
        }

        // Logs Stream
        if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Fingerprint,
                        contentDescription = null,
                        tint = RetailSlate300,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No Activity Logs Found",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = RetailSlate700
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Adjust filters or search criteria above.",
                        fontSize = 12.sp,
                        color = RetailSlate500
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("activity_logs_list")
            ) {
                items(filteredLogs, key = { it.id }) { log ->
                    ActivityLogItemCard(
                        log = log,
                        onClick = { selectedLogForDetails = log }
                    )
                }
            }
        }
    }

    // Detail Inspection Dialog
    selectedLogForDetails?.let { log ->
        ActivityLogDetailDialog(
            log = log,
            onDismiss = { selectedLogForDetails = null }
        )
    }

    // Integrity Verification Summary Dialog
    verificationResultDialog?.let { result ->
        AlertDialog(
            onDismissRequest = { verificationResultDialog = null },
            icon = {
                Icon(
                    if (result.isAllValid) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                    contentDescription = null,
                    tint = if (result.isAllValid) RetailStatusGreen else RetailStatusRed,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = if (result.isAllValid) "Integrity Verified" else "Integrity Warning",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = if (result.isAllValid)
                            "All ${result.totalLogs} activity logs passed canonical SHA-256 cryptographic verification. Zero tampering detected across POS transactions and inventory modifications."
                        else
                            "Detected ${result.corruptedLogs} log(s) whose SHA-256 hash does not match original stored parameters! Potential tampering detected.",
                        fontSize = 14.sp,
                        color = RetailSlate700
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = RetailSlate100,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Audited Records:", fontSize = 12.sp, color = RetailSlate500)
                                Text("${result.totalLogs}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Verified Hashes:", fontSize = 12.sp, color = RetailSlate500)
                                Text("${result.verifiedLogs}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RetailStatusGreen)
                            }
                            if (result.corruptedLogs > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Compromised Records:", fontSize = 12.sp, color = RetailSlate500)
                                    Text("${result.corruptedLogs}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RetailStatusRed)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { verificationResultDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary)
                ) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun SecurityMetricPill(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color(0xFF94A3B8),
                maxLines = 1
            )
        }
    }
}

@Composable
fun ActivityLogItemCard(
    log: ActivityLogEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPos = log.category == ActivityCategory.POS_TRANSACTION.code
    val isInv = log.category == ActivityCategory.INVENTORY_MODIFICATION.code

    val categoryColor = when {
        isPos -> RetailStatusGreen
        isInv -> RetailStatusBlue
        else -> RetailStatusAmber
    }

    val categoryBg = when {
        isPos -> RetailStatusGreenContainer
        isInv -> RetailStatusBlueContainer
        else -> RetailStatusAmberContainer
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("log_item_${log.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Category Pill & Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = categoryBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                isPos -> Icons.Filled.PointOfSale
                                isInv -> Icons.Filled.Inventory2
                                else -> Icons.Filled.Security
                            },
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = log.action.replace("_", " "),
                            color = categoryColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = formatDateTime(log.timestamp),
                    fontSize = 11.sp,
                    color = RetailSlate500
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Narrative Details
            Text(
                text = log.details,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = RetailSlate900,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = RetailSlate100, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Staff Identification Block + SHA-256 Checksum Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Staff attribution
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(RetailTealPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = log.staffName.firstOrNull()?.uppercase() ?: "S",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = log.staffName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RetailSlate900
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    // Prominent Staff ID Badge
                    Surface(
                        color = RetailSlate100,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Staff ID: #${log.staffId}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = RetailTealPrimary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                // Tamper verification tag
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            tint = RetailStatusGreen,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "SHA: ${log.integrityHash.take(6)}...",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = RetailSlate700,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityLogDetailDialog(
    log: ActivityLogEntity,
    onDismiss: () -> Unit
) {
    val isHashValid = remember(log) { log.verifyIntegrity() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .testTag("log_detail_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(RetailTealPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Shield,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Audit Record Details",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = RetailSlate900
                            )
                            Text(
                                text = "Log ID #${log.id} • ${log.action}",
                                fontSize = 11.sp,
                                color = RetailSlate500
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Clear, contentDescription = "Close", tint = RetailSlate500)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Integrity Status Banner
                Surface(
                    color = if (isHashValid) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isHashValid) Color(0xFFBBF7D0) else Color(0xFFFECACA)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isHashValid) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                            contentDescription = null,
                            tint = if (isHashValid) RetailStatusGreen else RetailStatusRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isHashValid) "Cryptographically Verified (SHA-256)" else "Checksum Failure (Tampered)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isHashValid) RetailStatusGreen else RetailStatusRed
                            )
                            Text(
                                text = if (isHashValid) "Record matches secure immutable HMAC signature" else "Stored hash differs from recalculation",
                                fontSize = 10.sp,
                                color = RetailSlate700
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Staff Attribution Section
                Text("STAFF IDENTIFICATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RetailSlate500)
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = RetailSlate100,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(RetailTealPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = log.staffName.firstOrNull()?.uppercase() ?: "S",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(log.staffName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RetailSlate900)
                                Text("Role: ${log.staffRole}", fontSize = 11.sp, color = RetailSlate700)
                            }
                        }

                        Surface(
                            color = RetailTealPrimary,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Staff User ID: #${log.staffId}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Details Section
                Text("ACTION & ENTITY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RetailSlate500)
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = RetailSlate100,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        DetailRow("Category", log.category)
                        DetailRow("Action", log.action)
                        DetailRow("Target Entity", "${log.entityType}: ${log.entityId}")
                        DetailRow("Timestamp", formatDateTime(log.timestamp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Summary Narrative
                Text("DETAILS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RetailSlate500)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.details,
                    fontSize = 13.sp,
                    color = RetailSlate900,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Raw SHA-256 Digest Box
                Text("SHA-256 INTEGRITY DIGEST", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RetailSlate500)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = RetailSlate900,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = log.integrityHash,
                        color = Color(0xFF22D3EE),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(8.dp),
                        lineHeight = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Details", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = RetailSlate500)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = RetailSlate900)
    }
}
