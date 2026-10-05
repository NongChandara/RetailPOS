package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.service.TelegramConfig
import com.example.ui.MainViewModel
import com.example.ui.theme.RetailSlate100
import com.example.ui.theme.RetailSlate300
import com.example.ui.theme.RetailSlate500
import com.example.ui.theme.RetailSlate700
import com.example.ui.theme.RetailSlate900
import com.example.ui.theme.RetailStatusGreen
import com.example.ui.theme.RetailTealPrimary
import kotlinx.coroutines.launch

@Composable
fun TelegramSettingsDialog(
    viewModel: MainViewModel,
    config: TelegramConfig,
    onDismiss: () -> Unit
) {
    var botToken by remember { mutableStateOf(config.botToken) }
    var chatId by remember { mutableStateOf(config.chatId) }
    var isEnabled by remember { mutableStateOf(config.isEnabled) }

    var testStatus by remember { mutableStateOf<String?>(null) }
    var isTesting by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    val currentUser by viewModel.currentUser.collectAsState()
    var isEditingAdminTg by remember { mutableStateOf(false) }
    var editedAdminTg by remember(currentUser?.telegram) {
        mutableStateOf(currentUser?.telegram?.ifBlank { "@chandaranong" } ?: "@chandaranong")
    }
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 20.dp)
                .testTag("telegram_settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF229ED9)), // Telegram Blue
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Send,
                                contentDescription = "Telegram",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Telegram Alerts",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = RetailSlate900
                            )
                            Text(
                                text = "Instant Push for Every Transaction",
                                fontSize = 11.sp,
                                color = RetailSlate500
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = RetailSlate500
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Admin User Telegram Account Card
                val adminTelegram = currentUser?.telegram?.ifBlank { "@chandaranong" } ?: "@chandaranong"
                val adminName = currentUser?.name ?: "Chandara Nong (Store Manager)"
                Surface(
                    color = Color(0xFFF0F9FF),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD)),
                    modifier = Modifier.fillMaxWidth().testTag("admin_telegram_profile_card")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                        .background(Color(0xFF229ED9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Send,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Admin Telegram Account",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0369A1)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = Color(0xFF0284C7),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "ADMIN USER",
                                                color = Color.White,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "$adminName • $adminTelegram",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = RetailSlate900
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    isEditingAdminTg = !isEditingAdminTg
                                    if (isEditingAdminTg) {
                                        editedAdminTg = adminTelegram
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp).testTag("btn_edit_admin_telegram")
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isEditingAdminTg) "Close" else "Edit",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (!isEditingAdminTg) {
                            Text(
                                text = "Instant receipt push & inventory alerts dispatched to this admin handle",
                                fontSize = 10.sp,
                                color = RetailSlate500,
                                modifier = Modifier.padding(start = 46.dp, top = 2.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFFBAE6FD).copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Edit Telegram Username or Phone",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RetailSlate700
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = editedAdminTg,
                                    onValueChange = { editedAdminTg = it },
                                    placeholder = { Text("@username or phone") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).testTag("input_edit_admin_telegram")
                                )
                                Button(
                                    onClick = {
                                        val trimmed = editedAdminTg.trim()
                                        val formatted = if (trimmed.isNotBlank() && !trimmed.startsWith("@") && !trimmed.startsWith("+") && !trimmed.all { it.isDigit() }) {
                                            "@$trimmed"
                                        } else {
                                            trimmed
                                        }
                                        currentUser?.let { user ->
                                            val updated = user.copy(telegram = formatted)
                                            viewModel.updateUser(updated)
                                            viewModel.setCurrentUser(updated)
                                        }
                                        isEditingAdminTg = false
                                    },
                                    enabled = editedAdminTg.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(48.dp).testTag("btn_save_admin_telegram")
                                ) {
                                    Text("Save", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Toggle Enable
                Surface(
                    color = if (isEnabled) Color(0xFFF0FDF4) else RetailSlate100,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isEnabled) Color(0xFFBBF7D0) else RetailSlate300
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Send Transactions to Telegram",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isEnabled) Color(0xFF15803D) else RetailSlate900
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isEnabled) "Active: Every checkout receipt will be sent automatically" else "Disabled: Transactions will only be saved locally",
                                fontSize = 11.sp,
                                color = RetailSlate700
                            )
                        }

                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { isEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF229ED9)
                            ),
                            modifier = Modifier.testTag("telegram_switch_toggle")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bot Token Input
                Text(
                    text = "Telegram Bot Token",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = RetailSlate900
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = botToken,
                    onValueChange = { botToken = it },
                    placeholder = { Text("e.g. 7942738910:AAH-xXJgVfQ6aF3WvXyv770gZk3qYkZ88M0", fontSize = 12.sp) },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("telegram_token_input")
                )
                Text(
                    text = "Obtain from @BotFather on Telegram (type /newbot)",
                    fontSize = 10.sp,
                    color = RetailSlate500,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Chat ID Input
                Text(
                    text = "Telegram Chat ID / Channel ID",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = RetailSlate900
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = chatId,
                    onValueChange = { chatId = it },
                    placeholder = { Text("e.g. -1002345678901 or 123456789", fontSize = 12.sp) },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("telegram_chat_id_input")
                )
                Text(
                    text = "Personal chat ID, group ID, or channel ID (e.g. from @userinfobot)",
                    fontSize = 10.sp,
                    color = RetailSlate500,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Status message after testing
                testStatus?.let { msg ->
                    Surface(
                        color = if (isSuccess) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSuccess) Color(0xFF86EFAC) else Color(0xFFFECACA)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSuccess) Icons.Filled.Check else Icons.Filled.Warning,
                                contentDescription = null,
                                tint = if (isSuccess) RetailStatusGreen else Color(0xFFDC2626),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                fontSize = 11.sp,
                                color = if (isSuccess) Color(0xFF15803D) else Color(0xFF991B1B),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Test Connection Button
                OutlinedButton(
                    onClick = {
                        isTesting = true
                        testStatus = null
                        coroutineScope.launch {
                            viewModel.testTelegramNotification(botToken, chatId) { success, resultMsg ->
                                isTesting = false
                                isSuccess = success
                                testStatus = resultMsg
                            }
                        }
                    },
                    enabled = !isTesting && botToken.isNotBlank() && chatId.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("telegram_test_btn")
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sending Test Alert...", fontSize = 12.sp)
                    } else {
                        Icon(imageVector = Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send Test Message to Telegram", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Info Box
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RetailSlate300.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = Color(0xFF229ED9),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "How Telegram Transaction Alerts Work",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RetailSlate900
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "When cashier completes a sale (Cash or KHQR), an instant receipt with receipt number, items, total (USD/KHR), and cashier info is pushed to your Telegram.",
                                fontSize = 10.sp,
                                color = RetailSlate700
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            viewModel.updateTelegramConfig(botToken, chatId, isEnabled)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF229ED9)),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("telegram_save_btn")
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save & Apply", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
