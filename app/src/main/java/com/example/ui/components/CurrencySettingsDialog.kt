package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.MainViewModel
import com.example.ui.theme.RetailSlate100
import com.example.ui.theme.RetailSlate300
import com.example.ui.theme.RetailSlate500
import com.example.ui.theme.RetailSlate700
import com.example.ui.theme.RetailSlate900
import com.example.ui.theme.RetailTealLight
import com.example.ui.theme.RetailTealPrimary
import com.example.util.CurrencyMode
import com.example.util.CurrencyUtils

@Composable
fun CurrencySettingsDialog(
    viewModel: MainViewModel,
    currentMode: CurrencyMode,
    currentRate: Double,
    onDismiss: () -> Unit
) {
    var selectedMode by remember { mutableStateOf(currentMode) }
    var rateInputText by remember { mutableStateOf(currentRate.toInt().toString()) }

    val activeRate = rateInputText.toDoubleOrNull() ?: currentRate

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("currency_settings_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                .size(36.dp)
                                .background(RetailTealPrimary.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.CurrencyExchange,
                                contentDescription = null,
                                tint = RetailTealPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Currency & Riel (KHR)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = RetailSlate900
                            )
                            Text(
                                text = "Cambodia Dual Currency Setting",
                                fontSize = 11.sp,
                                color = RetailSlate500
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = RetailSlate300.copy(alpha = 0.5f))

                // Display Mode Selection
                Text(
                    text = "Display Currency Mode",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RetailSlate700
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        CurrencyMode.DUAL to "Dual ($ / ៛)",
                        CurrencyMode.KHR to "KHR (៛)",
                        CurrencyMode.USD to "USD ($)"
                    ).forEach { (mode, label) ->
                        val isSelected = selectedMode == mode
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) RetailTealPrimary else RetailSlate100,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedMode = mode }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = mode.symbol,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color.White else RetailTealPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = mode.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else RetailSlate700
                                )
                            }
                        }
                    }
                }

                // Exchange Rate Configuration
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Exchange Rate (1 USD = ? KHR)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RetailSlate700
                        )
                        Text(
                            text = "1$ = ${CurrencyUtils.formatKhrRaw(activeRate.toLong())}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RetailTealPrimary
                        )
                    }

                    // Quick rate presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(4000, 4100, 4150, 4200).forEach { presetRate ->
                            val isCurrent = rateInputText == presetRate.toString()
                            OutlinedButton(
                                onClick = { rateInputText = presetRate.toString() },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isCurrent) RetailTealPrimary.copy(alpha = 0.1f) else Color.Transparent
                                )
                            ) {
                                Text(
                                    "$presetRate ៛",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent) RetailTealPrimary else RetailSlate700
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = rateInputText,
                        onValueChange = { rateInputText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Custom Riel Exchange Rate") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Live Preview Card
                Surface(
                    color = RetailSlate100,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "PREVIEW EXAMPLES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = RetailSlate500
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Standard Item ($2.50):", fontSize = 12.sp, color = RetailSlate700)
                            Text(
                                text = CurrencyUtils.formatCurrency(2.50, selectedMode, activeRate),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = RetailTealPrimary
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Transaction ($10.00):", fontSize = 12.sp, color = RetailSlate700)
                            Text(
                                text = CurrencyUtils.formatCurrency(10.00, selectedMode, activeRate),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = RetailTealPrimary
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            viewModel.setCurrencyMode(selectedMode)
                            val rate = rateInputText.toDoubleOrNull() ?: currentRate
                            viewModel.setKhrExchangeRate(rate)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Apply", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
