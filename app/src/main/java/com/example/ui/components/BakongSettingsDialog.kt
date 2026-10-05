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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.MainViewModel
import com.example.ui.theme.RetailSlate100
import com.example.ui.theme.RetailSlate300
import com.example.ui.theme.RetailSlate500
import com.example.ui.theme.RetailSlate700
import com.example.ui.theme.RetailSlate900

@Composable
fun BakongSettingsDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val currentAccountId by viewModel.bakongAccountId.collectAsState()
    val currentMerchantName by viewModel.bakongMerchantName.collectAsState()
    val khrRate by viewModel.khrExchangeRate.collectAsState()

    var accountIdInput by remember { mutableStateOf(currentAccountId) }
    var merchantNameInput by remember { mutableStateOf(currentMerchantName) }
    var isSaved by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 20.dp)
                .testTag("bakong_settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFEE2E2),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🔴", fontSize = 18.sp)
                            }
                        }
                        Column {
                            Text(
                                text = "Bakong Universal KHQR",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = RetailSlate900
                            )
                            Text(
                                text = "NBC Payment Gateway & Counter Stand",
                                fontSize = 11.sp,
                                color = RetailSlate500
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = RetailSlate500)
                    }
                }

                // Account Inputs
                Card(
                    colors = CardDefaults.cardColors(containerColor = RetailSlate100),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Merchant Account Configuration",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = RetailSlate900
                        )

                        OutlinedTextField(
                            value = accountIdInput,
                            onValueChange = {
                                accountIdInput = it
                                isSaved = false
                            },
                            label = { Text("Bakong Account ID", fontSize = 11.sp) },
                            placeholder = { Text("e.g. trstore@aclb", fontSize = 11.sp) },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            trailingIcon = {
                                IconButton(onClick = {
                                    clipboardManager.setText(AnnotatedString(accountIdInput))
                                }) {
                                    Icon(
                                        Icons.Filled.ContentCopy,
                                        contentDescription = "Copy Bakong ID",
                                        modifier = Modifier.size(16.dp),
                                        tint = RetailSlate500
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bakong_account_input")
                        )

                        OutlinedTextField(
                            value = merchantNameInput,
                            onValueChange = {
                                merchantNameInput = it
                                isSaved = false
                            },
                            label = { Text("Store / Merchant Name", fontSize = 11.sp) },
                            placeholder = { Text("TR STORE & CAFE", fontSize = 11.sp) },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bakong_merchant_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    accountIdInput = "trstore@aclb"
                                    merchantNameInput = "TR STORE & CAFE"
                                    viewModel.updateBakongSettings("trstore@aclb", "TR STORE & CAFE")
                                    isSaved = true
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset Default", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    viewModel.updateBakongSettings(accountIdInput, merchantNameInput)
                                    isSaved = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BakongRed),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("save_bakong_settings_button")
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isSaved) "Saved!" else "Save Bakong Settings", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Live Stand Preview
                Text(
                    text = "Live Stand & Terminal Preview",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = RetailSlate900
                )

                BakongKhqrCard(
                    usdAmount = 1.0,
                    khrRate = khrRate,
                    merchantName = merchantNameInput.ifBlank { "TR STORE & CAFE" },
                    bakongAccountId = accountIdInput.ifBlank { "trstore@aclb" },
                    billNumber = "STAND-DEMO",
                    modifier = Modifier.fillMaxWidth()
                )

                // Partner banks
                Surface(
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Accepted NBC Banking Apps",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = BakongDarkRed
                        )
                        Text(
                            text = "ABA Mobile • ACLEDA • Wing Bank • Sathapana • Canadia • Bakong",
                            fontSize = 10.sp,
                            color = RetailSlate700
                        )
                    }
                }

                // Done button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = RetailSlate900),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
