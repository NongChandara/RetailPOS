package com.example.ui.users

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import com.example.ui.theme.RetailSlate100
import com.example.ui.theme.RetailSlate300
import com.example.ui.theme.RetailSlate500
import com.example.ui.theme.RetailSlate700
import com.example.ui.theme.RetailSlate900
import com.example.ui.theme.RetailStatusBlue
import com.example.ui.theme.RetailStatusGreen
import com.example.ui.theme.RetailTealPrimary

@Composable
fun UsersScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val users by viewModel.allUsers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showAddUserDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<UserEntity?>(null) }
    var showPinSwitchDialog by remember { mutableStateOf(false) }
    var userToSwitchTo by remember { mutableStateOf<UserEntity?>(null) }

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
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

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        onSelect = {
                            if (user.id != currentUser?.id) {
                                userToSwitchTo = user
                                showPinSwitchDialog = true
                            }
                        },
                        onEdit = { userToEdit = user },
                        onDelete = { viewModel.deleteUser(user) }
                    )
                }
            }
        }

        // Add Staff FAB
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
            onSave = { name, role, pin, email ->
                viewModel.addUser(name, role, pin, email)
                showAddUserDialog = false
            }
        )
    }

    // Edit Staff Dialog
    userToEdit?.let { user ->
        AddEditUserDialog(
            user = user,
            onDismiss = { userToEdit = null },
            onSave = { name, role, pin, email ->
                viewModel.updateUser(
                    user.copy(
                        name = name,
                        role = role,
                        pin = pin,
                        email = email
                    )
                )
                userToEdit = null
            }
        )
    }
}

@Composable
fun UserRowCard(
    user: UserEntity,
    isCurrent: Boolean,
    onSelect: () -> Unit,
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
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = RetailSlate700, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
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
    onSave: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(user?.name ?: "") }
    var role by remember { mutableStateOf(user?.role ?: "CASHIER") }
    var pin by remember { mutableStateOf(user?.pin ?: "1234") }
    var email by remember { mutableStateOf(user?.email ?: "") }

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
                            onClick = { role = r },
                            label = { Text(r, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
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
                        onClick = { onSave(name, role, pin, email) },
                        enabled = name.isNotBlank() && pin.length >= 4,
                        colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("save_user_btn")
                    ) {
                        Text("Save Staff Member")
                    }
                }
            }
        }
    }
}
