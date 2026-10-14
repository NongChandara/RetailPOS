package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.printer.DiscoveredBluetoothPrinter
import com.example.printer.PrintResult
import com.example.printer.PrinterConnectionType
import com.example.printer.PrinterPaperWidth
import com.example.printer.ThermalPrinterConfig
import com.example.printer.ThermalPrinterManager
import com.example.ui.theme.RetailSlate100
import com.example.ui.theme.RetailSlate300
import com.example.ui.theme.RetailSlate500
import com.example.ui.theme.RetailSlate700
import com.example.ui.theme.RetailSlate900
import com.example.ui.theme.RetailStatusGreen
import com.example.ui.theme.RetailStatusGreenContainer
import com.example.ui.theme.RetailStatusRed
import com.example.ui.theme.RetailTealLight
import com.example.ui.theme.RetailTealPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThermalPrinterSettingsDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val printerManager = remember { ThermalPrinterManager.getInstance(context) }
    val currentConfig by printerManager.config.collectAsState()
    val isPrinting by printerManager.isPrinting.collectAsState()

    var connType by remember { mutableStateOf(currentConfig.connectionType) }
    var paperWidth by remember { mutableStateOf(currentConfig.paperWidth) }
    var ipAddress by remember { mutableStateOf(currentConfig.ipAddress) }
    var port by remember { mutableStateOf(currentConfig.port.toString()) }
    var btAddress by remember { mutableStateOf(currentConfig.bluetoothAddress) }
    var btName by remember { mutableStateOf(currentConfig.bluetoothName) }
    var autoReceipt by remember { mutableStateOf(currentConfig.autoPrintReceipt) }
    var autoOrder by remember { mutableStateOf(currentConfig.autoPrintOrder) }
    var receiptCopies by remember { mutableStateOf(currentConfig.receiptCopies) }
    var orderCopies by remember { mutableStateOf(currentConfig.orderCopies) }
    var openDrawer by remember { mutableStateOf(currentConfig.openCashDrawer) }
    var cutPaper by remember { mutableStateOf(currentConfig.cutPaper) }
    var beepKitchen by remember { mutableStateOf(currentConfig.beepKitchenTicket) }
    var khmerBitmap by remember { mutableStateOf(currentConfig.printKhmerAsBitmap) }

    var testStatus by remember { mutableStateOf<String?>(null) }
    var isTestError by remember { mutableStateOf(false) }

    var pairedBtPrinters by remember { mutableStateOf<List<DiscoveredBluetoothPrinter>>(emptyList()) }

    fun refreshBtPrinters() {
        pairedBtPrinters = printerManager.getPairedBluetoothPrinters()
    }

    LaunchedEffect(Unit) {
        refreshBtPrinters()
    }

    fun saveSettings() {
        val parsedPort = port.toIntOrNull() ?: 9100
        val updated = currentConfig.copy(
            connectionType = connType,
            paperWidth = paperWidth,
            ipAddress = ipAddress,
            port = parsedPort,
            bluetoothAddress = btAddress,
            bluetoothName = btName,
            autoPrintReceipt = autoReceipt,
            autoPrintOrder = autoOrder,
            receiptCopies = receiptCopies,
            orderCopies = orderCopies,
            openCashDrawer = openDrawer,
            cutPaper = cutPaper,
            beepKitchenTicket = beepKitchen,
            printKhmerAsBitmap = khmerBitmap
        )
        printerManager.updateConfig(updated)
        Toast.makeText(context, "Thermal printer configuration saved!", Toast.LENGTH_SHORT).show()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("thermal_printer_settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(RetailTealPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Print,
                                contentDescription = "Thermal Printer",
                                tint = RetailTealPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Thermal Printer Setup",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = RetailSlate900
                            )
                            Text(
                                text = "ESC/POS 58mm & 80mm • Auto-Print",
                                fontSize = 12.sp,
                                color = RetailSlate500
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = RetailSlate500)
                    }
                }

                HorizontalDivider(color = RetailSlate300)

                // 1. Connection Mode Selector
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "PRINTER CONNECTION TYPE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = RetailSlate500
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Triple(PrinterConnectionType.VIRTUAL_PREVIEW, "Simulator", Icons.Filled.PhoneAndroid),
                            Triple(PrinterConnectionType.BLUETOOTH, "Bluetooth", Icons.Filled.Bluetooth),
                            Triple(PrinterConnectionType.NETWORK, "Network IP", Icons.Filled.Dns),
                            Triple(PrinterConnectionType.USB, "USB OTG", Icons.Filled.Usb)
                        ).forEach { (type, label, icon) ->
                            val isSelected = connType == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { connType = type },
                                label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RetailTealPrimary,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 2. Hardware-specific config inputs
                when (connType) {
                    PrinterConnectionType.VIRTUAL_PREVIEW -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = RetailSlate100),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.PhoneAndroid, contentDescription = null, tint = RetailTealPrimary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Virtual Thermal Simulator Active", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        "Generates realistic thermal receipt roll preview with paper tear, barcodes, and mono fonts directly on-screen. Ideal for testing and paperless operations.",
                                        fontSize = 11.sp,
                                        color = RetailSlate500
                                    )
                                }
                            }
                        }
                    }

                    PrinterConnectionType.BLUETOOTH -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = RetailSlate100),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Select Paired Bluetooth Printer", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    IconButton(onClick = { refreshBtPrinters() }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Filled.Refresh, contentDescription = "Scan", tint = RetailTealPrimary)
                                    }
                                }

                                if (pairedBtPrinters.isEmpty()) {
                                    Text(
                                        "No paired Bluetooth devices found. Please pair your POS thermal printer (e.g. POS-58, POS-80) in Android Bluetooth Settings first.",
                                        fontSize = 12.sp,
                                        color = RetailSlate700
                                    )
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        pairedBtPrinters.forEach { device ->
                                            val isSelected = btAddress == device.address
                                            Surface(
                                                color = if (isSelected) RetailStatusGreenContainer else Color.White,
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.dp, if (isSelected) RetailStatusGreen else RetailSlate300),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        btAddress = device.address
                                                        btName = device.name
                                                    }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(device.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                            if (device.isLikelyPrinter) {
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Surface(
                                                                    color = RetailTealPrimary.copy(alpha = 0.15f),
                                                                    shape = RoundedCornerShape(4.dp)
                                                                ) {
                                                                    Text("Thermal", fontSize = 9.sp, color = RetailTealPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                                                }
                                                            }
                                                        }
                                                        Text(device.address, fontSize = 11.sp, color = RetailSlate500)
                                                    }
                                                    if (isSelected) {
                                                        Icon(Icons.Filled.Check, contentDescription = "Selected", tint = RetailStatusGreen)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    PrinterConnectionType.NETWORK -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = RetailSlate100),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Network Printer (Wi-Fi / Ethernet)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = ipAddress,
                                        onValueChange = { ipAddress = it },
                                        label = { Text("Printer IP Address") },
                                        placeholder = { Text("192.168.1.100") },
                                        singleLine = true,
                                        modifier = Modifier.weight(2f)
                                    )
                                    OutlinedTextField(
                                        value = port,
                                        onValueChange = { port = it },
                                        label = { Text("Port") },
                                        placeholder = { Text("9100") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Text("Standard ESC/POS thermal printers listen on RAW port 9100 over LAN.", fontSize = 11.sp, color = RetailSlate500)
                            }
                        }
                    }

                    PrinterConnectionType.USB -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = RetailSlate100),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Usb, contentDescription = null, tint = RetailTealPrimary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "Connect printer via USB OTG cable to Android device. Grant USB device permissions when prompted upon checkout.",
                                    fontSize = 12.sp,
                                    color = RetailSlate700
                                )
                            }
                        }
                    }

                    PrinterConnectionType.SYSTEM_PRINT -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = RetailSlate100),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Print, contentDescription = null, tint = RetailTealPrimary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "Dispatches receipts through standard Android Print Spooler (supports Sunmi, RawBT, and Google Cloud Print services).",
                                    fontSize = 12.sp,
                                    color = RetailSlate700
                                )
                            }
                        }
                    }
                }

                // 3. Paper Width Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "THERMAL PAPER ROLL WIDTH",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = RetailSlate500
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = if (paperWidth == PrinterPaperWidth.MM_58) RetailTealPrimary.copy(alpha = 0.15f) else RetailSlate100,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (paperWidth == PrinterPaperWidth.MM_58) RetailTealPrimary else RetailSlate300),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { paperWidth = PrinterPaperWidth.MM_58 }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("58mm (2-Inch)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("32 columns • Compact portable", fontSize = 11.sp, color = RetailSlate500)
                            }
                        }

                        Surface(
                            color = if (paperWidth == PrinterPaperWidth.MM_80) RetailTealPrimary.copy(alpha = 0.15f) else RetailSlate100,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (paperWidth == PrinterPaperWidth.MM_80) RetailTealPrimary else RetailSlate300),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { paperWidth = PrinterPaperWidth.MM_80 }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("80mm (3-Inch)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("48 columns • Standard desktop POS", fontSize = 11.sp, color = RetailSlate500)
                            }
                        }
                    }
                }

                // 4. Automatic Printing Toggles
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "AUTOMATION & WORKFLOW",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = RetailSlate500
                    )

                    // Auto Customer Receipt
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-Print Customer Receipt", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Print receipt automatically when checkout completes", fontSize = 11.sp, color = RetailSlate500)
                        }
                        Switch(
                            checked = autoReceipt,
                            onCheckedChange = { autoReceipt = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = RetailTealPrimary)
                        )
                    }

                    // Auto Kitchen / Bar Order Ticket
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-Print Kitchen / Bar Order Ticket", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Send preparation ticket to barista / kitchen on checkout", fontSize = 11.sp, color = RetailSlate500)
                        }
                        Switch(
                            checked = autoOrder,
                            onCheckedChange = { autoOrder = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = RetailTealPrimary)
                        )
                    }

                    // Cash Drawer Kick
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Kick Open Cash Drawer", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Pulse RJ-11 cash drawer solenoid on checkout", fontSize = 11.sp, color = RetailSlate500)
                        }
                        Switch(
                            checked = openDrawer,
                            onCheckedChange = { openDrawer = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = RetailTealPrimary)
                        )
                    }

                    // Auto Cut Paper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Cut Paper Automatically", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Send ESC/POS partial cut command after printing", fontSize = 11.sp, color = RetailSlate500)
                        }
                        Switch(
                            checked = cutPaper,
                            onCheckedChange = { cutPaper = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = RetailTealPrimary)
                        )
                    }

                    // Kitchen Buzzer Alert
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Kitchen Buzzer Beep Alert", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Sound printer buzzer to alert barista of new orders", fontSize = 11.sp, color = RetailSlate500)
                        }
                        Switch(
                            checked = beepKitchen,
                            onCheckedChange = { beepKitchen = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = RetailTealPrimary)
                        )
                    }
                }

                // Test Status Alert
                testStatus?.let { status ->
                    Surface(
                        color = if (isTestError) Color(0xFFFEF2F2) else RetailStatusGreenContainer,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (isTestError) Color(0xFFFCA5A5) else RetailStatusGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = status,
                            color = if (isTestError) RetailStatusRed else RetailStatusGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Test Print Button
                    OutlinedButton(
                        onClick = {
                            saveSettings()
                            coroutineScope.launch {
                                testStatus = "Sending test slip to ${connType.displayName}..."
                                isTestError = false
                                val result = printerManager.printTestSlip(forcePreview = connType == PrinterConnectionType.VIRTUAL_PREVIEW)
                                when (result) {
                                    is PrintResult.Success -> {
                                        testStatus = "✅ ${result.message}"
                                        isTestError = false
                                    }
                                    is PrintResult.Error -> {
                                        testStatus = "❌ ${result.errorMessage}"
                                        isTestError = true
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("thermal_printer_test_btn")
                    ) {
                        if (isPrinting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                        } else {
                            Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text("Test Print", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    // Save and Done Button
                    Button(
                        onClick = {
                            saveSettings()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("thermal_printer_save_btn")
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save & Apply", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
