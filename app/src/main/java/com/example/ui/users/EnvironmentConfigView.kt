package com.example.ui.users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.MainViewModel

data class EnvVarItem(
    val key: String,
    val value: String,
    val description: String,
    val category: String,
    val isSensitive: Boolean = false,
    val icon: ImageVector
)

@Composable
fun EnvironmentConfigView(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val bakongAccountId by viewModel.bakongAccountId.collectAsState()
    val bakongMerchantName by viewModel.bakongMerchantName.collectAsState()
    val telegramConfig by viewModel.telegramConfig.collectAsState()

    val envVariables = remember {
        listOf(
            EnvVarItem(
                key = "TELEGRAM_BOT_TOKEN",
                value = BuildConfig.TELEGRAM_BOT_TOKEN,
                description = "Telegram Bot API authentication token for sending automated transaction receipts and low-stock alerts.",
                category = "Notification & Messaging",
                isSensitive = true,
                icon = Icons.Default.Send
            ),
            EnvVarItem(
                key = "TELEGRAM_CHAT_ID",
                value = BuildConfig.TELEGRAM_CHAT_ID,
                description = "Telegram chat or channel ID where transaction notifications and alerts are routed.",
                category = "Notification & Messaging",
                isSensitive = false,
                icon = Icons.Default.Send
            ),
            EnvVarItem(
                key = "BAKONG_ACCOUNT_ID",
                value = BuildConfig.BAKONG_ACCOUNT_ID,
                description = "Merchant Bakong / ACLEDA Account ID used to generate standard KHQR dynamic payment codes.",
                category = "Payments & Financial",
                isSensitive = false,
                icon = Icons.Default.Payment
            ),
            EnvVarItem(
                key = "BAKONG_MERCHANT_NAME",
                value = BuildConfig.BAKONG_MERCHANT_NAME,
                description = "Official store merchant business name embedded into KHQR payment bills.",
                category = "Payments & Financial",
                isSensitive = false,
                icon = Icons.Default.Storefront
            ),
            EnvVarItem(
                key = "DEFAULT_BRANCH_NAME",
                value = BuildConfig.DEFAULT_BRANCH_NAME,
                description = "Default outlet branch assigned upon application startup and inventory movements.",
                category = "Store Terminal",
                isSensitive = false,
                icon = Icons.Default.Settings
            ),
            EnvVarItem(
                key = "DEFAULT_CURRENCY",
                value = BuildConfig.DEFAULT_CURRENCY,
                description = "Primary store base currency code for price tags and digital receipts.",
                category = "Store Terminal",
                isSensitive = false,
                icon = Icons.Default.Settings
            ),
            EnvVarItem(
                key = "APPLICATION_ID",
                value = BuildConfig.APPLICATION_ID,
                description = "Android Application ID identifying this package build.",
                category = "System & Runtime",
                isSensitive = false,
                icon = Icons.Default.Terminal
            ),
            EnvVarItem(
                key = "BUILD_TYPE",
                value = BuildConfig.BUILD_TYPE,
                description = "Current Android build compilation variant (debug/release).",
                category = "System & Runtime",
                isSensitive = false,
                icon = Icons.Default.Terminal
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("env_config_list"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Overview Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Color(0xFF0D9488), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = "Environment",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Android Staff Environment",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "BuildConfig & .env Configuration Hub",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (BuildConfig.DEBUG) Color(0xFF14B8A6).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = BuildConfig.BUILD_TYPE.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (BuildConfig.DEBUG) Color(0xFF2DD4BF) else Color(0xFF34D399),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "All environment variables defined in .env / BuildConfig are loaded and accessible to staff below. You can inspect values, verify connections, and synchronize them directly to the active session.",
                        fontSize = 13.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.applyEnvironmentVariables()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("apply_env_vars_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync To Active Settings", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.testTelegramConnection(
                                    BuildConfig.TELEGRAM_BOT_TOKEN,
                                    BuildConfig.TELEGRAM_CHAT_ID
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("test_env_telegram_button")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Telegram", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Active Runtime Settings Summary Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Current Active Runtime State",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Bakong Account", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(bakongAccountId, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                        }
                        Column {
                            Text("Bakong Merchant", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(bakongMerchantName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                        }
                        Column {
                            Text("Telegram Alert", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(
                                if (telegramConfig.isEnabled) "ACTIVE" else "DISABLED",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (telegramConfig.isEnabled) Color(0xFF0D9488) else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }

        // Environment Variables List
        item {
            Text(
                text = "Compiled Environment Variables (${envVariables.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF0F172A),
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
        }

        items(envVariables.size) { idx ->
            val item = envVariables[idx]
            EnvVariableCard(
                item = item,
                onCopy = {
                    clipboardManager.setText(AnnotatedString(item.value))
                    viewModel.showToast("Copied ${item.key} to clipboard")
                }
            )
        }

        // Info & Instructions Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = Color(0xFF0D9488),
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "How to Update Environment Variables",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "To update or add new environment variables, configure them via the Secrets panel in AI Studio or modify .env. The Secrets Gradle Plugin automatically compiles them into BuildConfig.",
                            fontSize = 12.sp,
                            color = Color(0xFF475569),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun EnvVariableCard(
    item: EnvVarItem,
    onCopy: () -> Unit
) {
    var isRevealed by remember { mutableStateOf(!item.isSensitive) }

    val isPlaceholder = item.value.isBlank() || item.value.startsWith("YOUR_")
    val displayValue = if (item.isSensitive && !isRevealed) {
        if (item.value.length > 8) "${item.value.take(4)}••••••••${item.value.takeLast(4)}"
        else "••••••••••••"
    } else {
        item.value.ifBlank { "(empty / not set)" }
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Key + Category + Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = Color(0xFF0D9488),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.key,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = item.category,
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isPlaceholder) Color(0xFFFEF3C7) else Color(0xFFCCFBF1)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaceholder) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isPlaceholder) Color(0xFFD97706) else Color(0xFF0D9488),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPlaceholder) "DEFAULT" else "LOADED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPlaceholder) Color(0xFFB45309) else Color(0xFF0F766E)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = item.description,
                fontSize = 12.sp,
                color = Color(0xFF475569),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Value Display Box
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0F172A),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = displayValue,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (isPlaceholder) Color(0xFFFBBF24) else Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.isSensitive) {
                            IconButton(
                                onClick = { isRevealed = !isRevealed },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isRevealed) "Hide" else "Show",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Value",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
