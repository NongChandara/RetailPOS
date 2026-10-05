package com.example.ui.users

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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserEntity
import com.example.ui.MainViewModel
import com.example.ui.components.RoleBadge
import com.example.ui.reports.AdminSalesReportView
import com.example.ui.theme.RetailSlate100
import com.example.ui.theme.RetailSlate300
import com.example.ui.theme.RetailSlate500
import com.example.ui.theme.RetailSlate700
import com.example.ui.theme.RetailSlate900
import com.example.ui.theme.RetailStatusBlue
import com.example.ui.theme.RetailStatusGreen
import com.example.ui.theme.RetailStatusPurple
import com.example.ui.theme.RetailStatusPurpleContainer
import com.example.ui.theme.RetailTealLight
import com.example.ui.theme.RetailTealPrimary

enum class UsersSubTab {
    STAFF_DIRECTORY,
    SALES_REPORT
}

@Composable
fun UsersScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val users by viewModel.allUsers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showAddUserDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<UserEntity?>(null) }
    var userToDelete by remember { mutableStateOf<UserEntity?>(null) }
    var showPinSwitchDialog by remember { mutableStateOf(false) }
    var userToSwitchTo by remember { mutableStateOf<UserEntity?>(null) }
    val isCurrentUserAdmin = currentUser?.role?.equals("ADMIN", ignoreCase = true) == true
    var selectedSubTab by remember { mutableStateOf(UsersSubTab.STAFF_DIRECTORY) }
    var showEditAdminTgDialog by remember { mutableStateOf(false) }

    // Ensure non-admin users always stay on STAFF_DIRECTORY
    if (!isCurrentUserAdmin && selectedSubTab != UsersSubTab.STAFF_DIRECTORY) {
        selectedSubTab = UsersSubTab.STAFF_DIRECTORY
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 72.dp)
                .testTag("users_screen")
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Current Active Cashier / Staff Card
            Surface(
                color = RetailSlate900,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("active_user_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(RetailTealPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser?.name?.take(2)?.uppercase() ?: "US",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = currentUser?.name ?: "No User Logged In",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RoleBadge(role = currentUser?.role ?: "GUEST")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Active Session",
                                    color = RetailStatusGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            // Visible Telegram Account for Admin User
                            val adminTg = currentUser?.telegram?.ifBlank {
                                if (isCurrentUserAdmin) "@chandaranong" else ""
                            } ?: if (isCurrentUserAdmin) "@chandaranong" else ""

                            if (adminTg.isNotBlank()) {
                                Spacer(modifier = Modifier.height(5.dp))
                                Surface(
                                    color = Color(0xFF0284C7).copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .clickable(enabled = isCurrentUserAdmin) {
                                            showEditAdminTgDialog = true
                                        }
                                        .testTag("admin_telegram_active_badge")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.Send,
                                            contentDescription = "Telegram Account",
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Telegram: $adminTg",
                                            color = Color(0xFFE0F2FE),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (isCurrentUserAdmin) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                Icons.Filled.Edit,
                                                contentDescription = "Edit Telegram",
                                                tint = Color(0xFFBAE6FD),
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isCurrentUserAdmin) {
                            Surface(
                                color = RetailTealPrimary.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .clickable { selectedSubTab = UsersSubTab.SALES_REPORT }
                                    .padding(end = 6.dp)
                                    .testTag("admin_report_shortcut_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.Assessment,
                                        contentDescription = null,
                                        tint = RetailTealLight,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Report",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                userToSwitchTo = null
                                showPinSwitchDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp).testTag("switch_user_btn")
                        ) {
                            Icon(Icons.Filled.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Switch", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-Tab Switcher: Staff Directory vs Admin Sales Report (Only visible for Admin users)
            if (isCurrentUserAdmin) {
                Surface(
                    color = RetailSlate100,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, RetailSlate300.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("users_subtab_switcher")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Staff Directory Tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (selectedSubTab == UsersSubTab.STAFF_DIRECTORY) Color.White else Color.Transparent)
                                .clickable { selectedSubTab = UsersSubTab.STAFF_DIRECTORY }
                                .padding(vertical = 8.dp)
                                .testTag("tab_staff_directory"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Filled.People,
                                    contentDescription = null,
                                    tint = if (selectedSubTab == UsersSubTab.STAFF_DIRECTORY) RetailTealPrimary else RetailSlate500,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Staff Directory (${users.size})",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedSubTab == UsersSubTab.STAFF_DIRECTORY) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedSubTab == UsersSubTab.STAFF_DIRECTORY) RetailSlate900 else RetailSlate500
                                )
                            }
                        }

                        // Sales Report Tab (ADMIN ONLY)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (selectedSubTab == UsersSubTab.SALES_REPORT) Color.White else Color.Transparent)
                                .clickable { selectedSubTab = UsersSubTab.SALES_REPORT }
                                .padding(vertical = 8.dp)
                                .testTag("tab_admin_sales_report"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Filled.Assessment,
                                    contentDescription = null,
                                    tint = if (selectedSubTab == UsersSubTab.SALES_REPORT) RetailTealPrimary else RetailSlate500,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Sales Reports",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedSubTab == UsersSubTab.SALES_REPORT) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedSubTab == UsersSubTab.SALES_REPORT) RetailSlate900 else RetailSlate500
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            when (selectedSubTab) {
                UsersSubTab.STAFF_DIRECTORY -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Staff Directory (${users.size})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = RetailSlate900
                            )
                            Text(
                                text = "Tap profile to authenticate",
                                fontSize = 12.sp,
                                color = RetailSlate500
                            )
                        }

                        if (isCurrentUserAdmin) {
                            Button(
                                onClick = { showAddUserDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("menu_create_user_btn")
                            ) {
                                Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Create User", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Users List
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(users, key = { it.id }) { user ->
                            UserRowCard(
                                user = user,
                                isCurrent = user.id == currentUser?.id,
                                isAdmin = isCurrentUserAdmin,
                                onSelect = {
                                    if (user.id != currentUser?.id) {
                                        userToSwitchTo = user
                                        showPinSwitchDialog = true
                                    }
                                },
                                onViewReport = if (isCurrentUserAdmin && user.role.equals("ADMIN", ignoreCase = true)) {
                                    { selectedSubTab = UsersSubTab.SALES_REPORT }
                                } else null,
                                onEdit = { userToEdit = user },
                                onDelete = { userToDelete = user }
                            )
                        }
                    }
                }

                UsersSubTab.SALES_REPORT -> {
                    if (isCurrentUserAdmin) {
                        AdminSalesReportView(
                            viewModel = viewModel,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Add Staff FAB (Only visible on Staff Directory to Admin users)
        if (selectedSubTab == UsersSubTab.STAFF_DIRECTORY && isCurrentUserAdmin) {
            FloatingActionButton(
                onClick = { showAddUserDialog = true },
                containerColor = RetailTealPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("add_user_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add User")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Staff", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }

    // PIN Switch & Login Dialog
    if (showPinSwitchDialog) {
        PinSwitchDialog(
            targetUser = userToSwitchTo,
            onDismiss = {
                showPinSwitchDialog = false
                userToSwitchTo = null
            },
            onSubmitPin = { pin, onError ->
                val target = userToSwitchTo
                if (target != null) {
                    if (target.pin == pin) {
                        viewModel.setCurrentUser(target)
                        showPinSwitchDialog = false
                        userToSwitchTo = null
                    } else {
                        onError("Invalid PIN for ${target.name}. Try again.")
                    }
                } else {
                    viewModel.switchUserByPin(
                        pin = pin,
                        onSuccess = {
                            showPinSwitchDialog = false
                            userToSwitchTo = null
                        },
                        onError = onError
                    )
                }
            }
        )
    }

    // Add Staff Dialog
    if (showAddUserDialog) {
        AddEditUserDialog(
            user = null,
            onDismiss = { showAddUserDialog = false },
            onSave = { name, role, pin, email, telegram ->
                viewModel.addUser(name, role, pin, email, telegram)
                showAddUserDialog = false
            }
        )
    }

    // Edit Staff Dialog
    userToEdit?.let { user ->
        AddEditUserDialog(
            user = user,
            onDismiss = { userToEdit = null },
            onDelete = if (user.id != currentUser?.id) {
                {
                    userToDelete = user
                    userToEdit = null
                }
            } else null,
            onSave = { name, role, pin, email, telegram ->
                viewModel.updateUser(
                    user.copy(
                        name = name,
                        role = role,
                        pin = pin,
                        email = email,
                        telegram = telegram
                    )
                )
                userToEdit = null
            }
        )
    }

    // Delete User Confirmation Dialog
    userToDelete?.let { user ->
        DeleteUserConfirmDialog(
            user = user,
            onDismiss = { userToDelete = null },
            onConfirmDelete = {
                viewModel.deleteUser(user)
                userToDelete = null
            }
        )
    }

    // Quick Edit Admin Telegram Dialog
    if (showEditAdminTgDialog) {
        currentUser?.let { user ->
            EditAdminTelegramDialog(
                currentTelegram = user.telegram.ifBlank { "@chandaranong" },
                onDismiss = { showEditAdminTgDialog = false },
                onSave = { updatedTg ->
                    val updated = user.copy(telegram = updatedTg)
                    viewModel.updateUser(updated)
                    viewModel.setCurrentUser(updated)
                    showEditAdminTgDialog = false
                }
            )
        }
    }
}

@Composable
fun UserRowCard(
    user: UserEntity,
    isCurrent: Boolean,
    isAdmin: Boolean = false,
    onSelect: () -> Unit,
    onViewReport: (() -> Unit)? = null,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) Color(0xFFF0FDFA) else Color.White
        ),
        border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.5.dp, RetailTealPrimary) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .testTag("user_row_${user.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isCurrent) RetailTealPrimary else RetailSlate100),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isCurrent) Color.White else RetailSlate700
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = RetailSlate900
                        )
                        if (isCurrent) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• Active",
                                color = RetailTealPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RoleBadge(role = user.role)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = "PIN Protected",
                            tint = RetailSlate500,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "PIN: ••••",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = RetailSlate500
                        )
                    }

                    // Visible Telegram Account for User (Always visible for Admin)
                    val userTelegram = user.telegram.ifBlank {
                        if (user.role.equals("ADMIN", ignoreCase = true)) "@chandaranong" else ""
                    }
                    if (userTelegram.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = Color(0xFFE0F2FE),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                            modifier = Modifier.testTag("user_telegram_${user.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Send,
                                    contentDescription = "Telegram Account",
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Telegram: $userTelegram",
                                    color = Color(0xFF0369A1),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (user.role.equals("ADMIN", ignoreCase = true) && onViewReport != null) {
                    Surface(
                        color = RetailTealPrimary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clickable(onClick = onViewReport)
                            .testTag("user_report_btn_${user.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Assessment,
                                contentDescription = null,
                                tint = RetailTealPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Report", color = RetailTealPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (isAdmin) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = RetailSlate700, modifier = Modifier.size(18.dp))
                    }
                    if (!isCurrent) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PinSwitchDialog(
    targetUser: UserEntity? = null,
    onDismiss: () -> Unit,
    onSubmitPin: (String, (String) -> Unit) -> Unit
) {
    var pinText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(RetailSlate100),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = RetailSlate900)
                }

                Text(
                    text = if (targetUser != null) "Login: ${targetUser.name}" else "Staff & Manager Login",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = RetailSlate900
                )

                if (targetUser != null) {
                    RoleBadge(role = targetUser.role)
                    val tg = targetUser.telegram.ifBlank {
                        if (targetUser.role.equals("ADMIN", ignoreCase = true)) "@chandaranong" else ""
                    }
                    if (tg.isNotBlank()) {
                        Surface(
                            color = Color(0xFFE0F2FE),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFFBAE6FD))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Send, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Telegram: $tg", color = Color(0xFF0369A1), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Text(
                    text = if (targetUser != null) {
                        "Enter the 4-digit PIN for ${targetUser.name} (${targetUser.role}) to authenticate session."
                    } else {
                        "Quick switch to your cashier or manager profile using your 4-digit PIN."
                    },
                    fontSize = 12.sp,
                    color = RetailSlate500,
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = pinText,
                    onValueChange = {
                        if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                            pinText = it
                            errorMessage = null
                        }
                    },
                    placeholder = { Text("••••") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(0.6f).testTag("pin_input_field")
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onSubmitPin(pinText) { err ->
                                errorMessage = err
                            }
                        },
                        enabled = pinText.length >= 4,
                        colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("submit_pin_btn")
                    ) {
                        Text("Unlock / Login")
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditUserDialog(
    user: UserEntity?,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onSave: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(user?.name ?: "") }
    var role by remember { mutableStateOf(user?.role ?: "CASHIER") }
    var pin by remember { mutableStateOf(user?.pin ?: "1234") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var telegram by remember {
        mutableStateOf(
            user?.telegram?.ifBlank {
                if (user?.role.equals("ADMIN", ignoreCase = true)) "@chandaranong" else ""
            } ?: if (role.equals("ADMIN", ignoreCase = true)) "@chandaranong" else ""
        )
    }

    val roles = listOf("ADMIN", "CASHIER", "CLERK")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (user == null) "Add Staff Member" else "Edit Staff Profile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = RetailSlate900
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("user_name_field")
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (Optional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = telegram,
                    onValueChange = { telegram = it },
                    label = { Text("Telegram Account (@username or phone)") },
                    placeholder = { Text(if (role.equals("ADMIN", ignoreCase = true)) "@chandaranong" else "@username") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Send,
                            contentDescription = "Telegram",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("user_telegram_field")
                )

                var isPinMasked by remember { mutableStateOf(true) }

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pin = it },
                    label = { Text("Access PIN (4 digits)") },
                    visualTransformation = if (isPinMasked) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    trailingIcon = {
                        Text(
                            text = if (isPinMasked) "SHOW" else "HIDE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RetailTealPrimary,
                            modifier = Modifier
                                .clickable { isPinMasked = !isPinMasked }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("user_pin_field")
                )

                Text("Assigned Role", fontSize = 12.sp, color = RetailSlate500, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    roles.forEach { r ->
                        val isSelected = role.equals(r, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                role = r
                                if (r.equals("ADMIN", ignoreCase = true) && telegram.isBlank()) {
                                    telegram = "@chandaranong"
                                }
                            },
                            label = { Text(r, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (user != null && onDelete != null) {
                        OutlinedButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("delete_user_in_dialog_btn")
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete User",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = { onSave(name, role, pin, email, telegram) },
                            enabled = name.isNotBlank() && pin.length >= 4,
                            colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("save_user_btn")
                        ) {
                            Text(if (user == null) "Save Staff Member" else "Update User")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteUserConfirmDialog(
    user: UserEntity,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(6.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Delete User Account?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = RetailSlate900,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = RetailSlate100,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = user.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = RetailSlate900
                            )
                            RoleBadge(role = user.role)
                        }
                        if (user.email.isNotBlank()) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = user.email,
                                fontSize = 12.sp,
                                color = RetailSlate500
                            )
                        }
                        val userTg = user.telegram.ifBlank {
                            if (user.role.equals("ADMIN", ignoreCase = true)) "@chandaranong" else ""
                        }
                        if (userTg.isNotBlank()) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "✈️ $userTg",
                                fontSize = 11.sp,
                                color = Color(0xFF0284C7),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Text(
                    text = "Are you sure you want to delete this user? They will immediately lose access to the POS terminal.",
                    fontSize = 12.sp,
                    color = RetailSlate700,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onConfirmDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("confirm_delete_user_btn")
                    ) {
                        Text("Delete User", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun EditAdminTelegramDialog(
    currentTelegram: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var telegramInput by remember { mutableStateOf(currentTelegram) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(6.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF229ED9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Edit Admin Telegram", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = RetailSlate900)
                        Text("Real-time manager notification handle", fontSize = 11.sp, color = RetailSlate500)
                    }
                }

                Text(
                    text = "Enter your Telegram username (e.g. @chandaranong) or mobile number linked to Telegram to receive instant sales alerts.",
                    fontSize = 12.sp,
                    color = RetailSlate700
                )

                OutlinedTextField(
                    value = telegramInput,
                    onValueChange = { telegramInput = it },
                    label = { Text("Admin Telegram Handle") },
                    placeholder = { Text("e.g. @chandaranong") },
                    leadingIcon = {
                        Icon(Icons.Filled.Send, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_admin_telegram_dialog")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val trimmed = telegramInput.trim()
                            val formatted = if (trimmed.isNotBlank() && !trimmed.startsWith("@") && !trimmed.startsWith("+") && !trimmed.all { it.isDigit() }) {
                                "@$trimmed"
                            } else {
                                trimmed
                            }
                            onSave(formatted)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("save_admin_telegram_dialog_btn")
                    ) {
                        Text("Save Handle")
                    }
                }
            }
        }
    }
}
