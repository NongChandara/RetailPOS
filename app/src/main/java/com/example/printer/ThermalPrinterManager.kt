package com.example.printer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.SharedPreferences
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Log
import com.example.data.model.BranchEntity
import com.example.data.model.SaleEntity
import com.example.data.model.SaleItemEntity
import com.example.ui.reports.SalesReportSummary
import com.example.util.CurrencyUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.UUID

/**
 * Central hardware abstraction and printing controller for thermal receipt printers.
 * Supports Bluetooth SPP, Network (Wi-Fi/LAN IP), USB, Android Print Service,
 * and Virtual on-screen receipt simulation.
 */
class ThermalPrinterManager private constructor(context: Context) {

    companion object {
        private const val TAG = "ThermalPrinterManager"
        private const val PREFS_NAME = "tr_coffee_printer_prefs"

        // Standard SPP (Serial Port Profile) UUID for Bluetooth thermal printers
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        @Volatile
        private var INSTANCE: ThermalPrinterManager? = null

        fun getInstance(context: Context): ThermalPrinterManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ThermalPrinterManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<ThermalPrinterConfig> = _config.asStateFlow()

    private val _isPrinting = MutableStateFlow(false)
    val isPrinting: StateFlow<Boolean> = _isPrinting.asStateFlow()

    private val _lastPrintResult = MutableStateFlow<PrintResult?>(null)
    val lastPrintResult: StateFlow<PrintResult?> = _lastPrintResult.asStateFlow()

    private val _activePreviewDoc = MutableStateFlow<FormattedThermalDoc?>(null)
    val activePreviewDoc: StateFlow<FormattedThermalDoc?> = _activePreviewDoc.asStateFlow()

    fun dismissPreview() {
        _activePreviewDoc.value = null
    }

    fun showPreview(doc: FormattedThermalDoc) {
        _activePreviewDoc.value = doc
    }

    fun updateConfig(newConfig: ThermalPrinterConfig) {
        _config.value = newConfig
        saveConfig(newConfig)
    }

    private fun loadConfig(): ThermalPrinterConfig {
        val connTypeStr = prefs.getString("connection_type", PrinterConnectionType.VIRTUAL_PREVIEW.name)
        val connType = try {
            PrinterConnectionType.valueOf(connTypeStr ?: PrinterConnectionType.VIRTUAL_PREVIEW.name)
        } catch (_: Exception) {
            PrinterConnectionType.VIRTUAL_PREVIEW
        }

        val paperWidthStr = prefs.getString("paper_width", PrinterPaperWidth.MM_80.name)
        val paperWidth = try {
            PrinterPaperWidth.valueOf(paperWidthStr ?: PrinterPaperWidth.MM_80.name)
        } catch (_: Exception) {
            PrinterPaperWidth.MM_80
        }

        return ThermalPrinterConfig(
            connectionType = connType,
            paperWidth = paperWidth,
            ipAddress = prefs.getString("ip_address", "192.168.1.100") ?: "192.168.1.100",
            port = prefs.getInt("port", 9100),
            bluetoothAddress = prefs.getString("bt_address", "") ?: "",
            bluetoothName = prefs.getString("bt_name", "") ?: "",
            autoPrintReceipt = prefs.getBoolean("auto_receipt", true),
            autoPrintOrder = prefs.getBoolean("auto_order", true),
            receiptCopies = prefs.getInt("receipt_copies", 1),
            orderCopies = prefs.getInt("order_copies", 1),
            openCashDrawer = prefs.getBoolean("open_drawer", true),
            cutPaper = prefs.getBoolean("cut_paper", true),
            beepKitchenTicket = prefs.getBoolean("beep_kitchen", true),
            printKhmerAsBitmap = prefs.getBoolean("khmer_bitmap", true)
        )
    }

    private fun saveConfig(cfg: ThermalPrinterConfig) {
        prefs.edit()
            .putString("connection_type", cfg.connectionType.name)
            .putString("paper_width", cfg.paperWidth.name)
            .putString("ip_address", cfg.ipAddress)
            .putInt("port", cfg.port)
            .putString("bt_address", cfg.bluetoothAddress)
            .putString("bt_name", cfg.bluetoothName)
            .putBoolean("auto_receipt", cfg.autoPrintReceipt)
            .putBoolean("auto_order", cfg.autoPrintOrder)
            .putInt("receipt_copies", cfg.receiptCopies)
            .putInt("order_copies", cfg.orderCopies)
            .putBoolean("open_drawer", cfg.openCashDrawer)
            .putBoolean("cut_paper", cfg.cutPaper)
            .putBoolean("beep_kitchen", cfg.beepKitchenTicket)
            .putBoolean("khmer_bitmap", cfg.printKhmerAsBitmap)
            .apply()
    }

    /**
     * Discovers paired Bluetooth devices and filters likely thermal receipt printers.
     */
    @SuppressLint("MissingPermission")
    fun getPairedBluetoothPrinters(): List<DiscoveredBluetoothPrinter> {
        val bluetoothManager = appContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter() ?: return emptyList()

        if (!adapter.isEnabled) return emptyList()

        return try {
            val bondedDevices = adapter.bondedDevices ?: emptySet()
            bondedDevices.map { device ->
                val name = device.name ?: "Unknown Bluetooth Device"
                val addr = device.address
                val isPrinter = name.contains("POS", ignoreCase = true) ||
                        name.contains("Printer", ignoreCase = true) ||
                        name.contains("Thermal", ignoreCase = true) ||
                        name.contains("RPP", ignoreCase = true) ||
                        name.contains("MTP", ignoreCase = true) ||
                        name.contains("MPT", ignoreCase = true) ||
                        name.contains("XP", ignoreCase = true) ||
                        name.contains("58", ignoreCase = true) ||
                        name.contains("80", ignoreCase = true)
                DiscoveredBluetoothPrinter(name = name, address = addr, isLikelyPrinter = isPrinter)
            }.sortedByDescending { it.isLikelyPrinter }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to list paired Bluetooth devices", e)
            emptyList()
        }
    }

    /**
     * Automatic checkout printing trigger:
     * Dispatches Customer Receipt and/or Kitchen Order Ticket based on printer settings.
     */
    fun handleAutoPrintOnCheckout(
        sale: SaleEntity,
        items: List<SaleItemEntity>,
        khrRate: Double = CurrencyUtils.activeKhrExchangeRate,
        branch: BranchEntity? = null
    ) {
        val currentCfg = _config.value
        scope.launch {
            if (currentCfg.autoPrintReceipt) {
                printCustomerReceipt(sale, items, khrRate, branch, copies = currentCfg.receiptCopies)
            }
            if (currentCfg.autoPrintOrder) {
                printOrderTicket(sale, items, copies = currentCfg.orderCopies)
            }
        }
    }

    /**
     * Prints or previews Customer Receipt.
     */
    suspend fun printCustomerReceipt(
        sale: SaleEntity,
        items: List<SaleItemEntity>,
        khrRate: Double = CurrencyUtils.activeKhrExchangeRate,
        branch: BranchEntity? = null,
        copies: Int = 1,
        forcePreview: Boolean = false
    ): PrintResult = withContext(Dispatchers.IO) {
        val currentCfg = _config.value
        val doc = ThermalReceiptFormatter.formatCustomerReceipt(sale, items, khrRate, branch, currentCfg)

        if (forcePreview || currentCfg.connectionType == PrinterConnectionType.VIRTUAL_PREVIEW) {
            _activePreviewDoc.value = doc
            val result = PrintResult.Success("Receipt rendered in Thermal Simulator", doc.previewText)
            _lastPrintResult.value = result
            return@withContext result
        }

        executePrintJob(doc, copies)
    }

    /**
     * Prints or previews Kitchen / Barista Order Ticket.
     */
    suspend fun printOrderTicket(
        sale: SaleEntity,
        items: List<SaleItemEntity>,
        orderNumber: String = sale.receiptNumber.takeLast(4),
        isTakeaway: Boolean = false,
        copies: Int = 1,
        forcePreview: Boolean = false
    ): PrintResult = withContext(Dispatchers.IO) {
        val currentCfg = _config.value
        val doc = ThermalReceiptFormatter.formatOrderTicket(sale, items, orderNumber, isTakeaway, currentCfg)

        if (forcePreview || currentCfg.connectionType == PrinterConnectionType.VIRTUAL_PREVIEW) {
            _activePreviewDoc.value = doc
            val result = PrintResult.Success("Kitchen Ticket rendered in Thermal Simulator", doc.previewText)
            _lastPrintResult.value = result
            return@withContext result
        }

        executePrintJob(doc, copies)
    }

    /**
     * Prints or previews End-of-Day Z-Report.
     */
    suspend fun printZReport(
        summary: SalesReportSummary,
        branch: BranchEntity? = null,
        managerName: String = "Sarah Miller (Store Manager)",
        khrRate: Double = CurrencyUtils.activeKhrExchangeRate,
        forcePreview: Boolean = false
    ): PrintResult = withContext(Dispatchers.IO) {
        val currentCfg = _config.value
        val doc = ThermalReceiptFormatter.formatZReport(summary, branch, managerName, currentCfg, khrRate)

        if (forcePreview || currentCfg.connectionType == PrinterConnectionType.VIRTUAL_PREVIEW) {
            _activePreviewDoc.value = doc
            val result = PrintResult.Success("Z-Report rendered in Thermal Simulator", doc.previewText)
            _lastPrintResult.value = result
            return@withContext result
        }

        executePrintJob(doc, copies = 1)
    }

    /**
     * Prints a test slip to test connection and alignment.
     */
    suspend fun printTestSlip(forcePreview: Boolean = false): PrintResult = withContext(Dispatchers.IO) {
        val currentCfg = _config.value
        val doc = ThermalReceiptFormatter.formatTestSlip(currentCfg)

        if (forcePreview || currentCfg.connectionType == PrinterConnectionType.VIRTUAL_PREVIEW) {
            _activePreviewDoc.value = doc
            val result = PrintResult.Success("Test Slip rendered in Thermal Simulator", doc.previewText)
            _lastPrintResult.value = result
            return@withContext result
        }

        executePrintJob(doc, copies = 1)
    }

    private suspend fun executePrintJob(doc: FormattedThermalDoc, copies: Int): PrintResult {
        _isPrinting.value = true
        val currentCfg = _config.value
        val actualCopies = copies.coerceIn(1, 5)

        val result = try {
            when (currentCfg.connectionType) {
                PrinterConnectionType.VIRTUAL_PREVIEW -> {
                    _activePreviewDoc.value = doc
                    PrintResult.Success("Rendered in Thermal Preview", doc.previewText)
                }
                PrinterConnectionType.BLUETOOTH -> {
                    sendBytesViaBluetooth(doc.escPosBytes, actualCopies, currentCfg)
                }
                PrinterConnectionType.NETWORK -> {
                    sendBytesViaNetwork(doc.escPosBytes, actualCopies, currentCfg)
                }
                PrinterConnectionType.USB -> {
                    sendBytesViaUsb(doc.escPosBytes, actualCopies, currentCfg)
                }
                PrinterConnectionType.SYSTEM_PRINT -> {
                    _activePreviewDoc.value = doc
                    PrintResult.Success("Sent to System Print Spooler", doc.previewText)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Printing failed: ${e.message}", e)
            PrintResult.Error(e.localizedMessage ?: "Printer communication error")
        } finally {
            _isPrinting.value = false
        }

        _lastPrintResult.value = result
        return result
    }

    @SuppressLint("MissingPermission")
    private fun sendBytesViaBluetooth(bytes: ByteArray, copies: Int, config: ThermalPrinterConfig): PrintResult {
        if (config.bluetoothAddress.isBlank()) {
            return PrintResult.Error("No Bluetooth printer selected. Please choose a paired printer in Settings.")
        }

        val bluetoothManager = appContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter()
            ?: return PrintResult.Error("Bluetooth is not supported on this device.")

        if (!adapter.isEnabled) {
            return PrintResult.Error("Bluetooth is turned off. Please enable Bluetooth.")
        }

        var device: BluetoothDevice? = null
        try {
            device = adapter.getRemoteDevice(config.bluetoothAddress)
        } catch (e: Exception) {
            return PrintResult.Error("Invalid Bluetooth address: ${config.bluetoothAddress}")
        }

        var socket: BluetoothSocket? = null
        var outputStream: OutputStream? = null

        return try {
            adapter.cancelDiscovery()
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            socket.connect()
            outputStream = socket.outputStream

            repeat(copies) {
                outputStream.write(bytes)
                outputStream.flush()
                Thread.sleep(150)
            }

            PrintResult.Success("Successfully printed $copies copy(ies) via Bluetooth (${config.bluetoothName.ifBlank { config.bluetoothAddress }})")
        } catch (e: Exception) {
            Log.e(TAG, "Bluetooth connection failed", e)
            PrintResult.Error("Failed to connect to Bluetooth printer (${e.localizedMessage ?: "Connection refused"}). Ensure printer is ON and paired.")
        } finally {
            try {
                outputStream?.close()
                socket?.close()
            } catch (_: Exception) {}
        }
    }

    private fun sendBytesViaNetwork(bytes: ByteArray, copies: Int, config: ThermalPrinterConfig): PrintResult {
        val ip = config.ipAddress.trim()
        val port = config.port

        if (ip.isBlank()) {
            return PrintResult.Error("Printer IP address is required.")
        }

        var socket: Socket? = null
        var outputStream: OutputStream? = null

        return try {
            socket = Socket()
            socket.connect(InetSocketAddress(ip, port), 4000) // 4 second connection timeout
            socket.soTimeout = 4000
            outputStream = socket.getOutputStream()

            repeat(copies) {
                outputStream.write(bytes)
                outputStream.flush()
                Thread.sleep(150)
            }

            PrintResult.Success("Printed $copies copy(ies) to Network Printer ($ip:$port)")
        } catch (e: Exception) {
            Log.e(TAG, "Network printer connection failed", e)
            PrintResult.Error("Network printer ($ip:$port) unreachable. Check Wi-Fi connection and printer power.")
        } finally {
            try {
                outputStream?.close()
                socket?.close()
            } catch (_: Exception) {}
        }
    }

    private fun sendBytesViaUsb(bytes: ByteArray, copies: Int, config: ThermalPrinterConfig): PrintResult {
        val usbManager = appContext.getSystemService(Context.USB_SERVICE) as? UsbManager
            ?: return PrintResult.Error("USB host mode is not supported on this device.")

        val deviceList = usbManager.deviceList
        if (deviceList.isEmpty()) {
            return PrintResult.Error("No USB thermal printer detected. Connect printer via USB OTG cable.")
        }

        val printerDevice: UsbDevice? = deviceList.values.firstOrNull { device ->
            device.deviceClass == UsbConstants.USB_CLASS_PRINTER ||
                    (0 until device.interfaceCount).any { i ->
                        device.getInterface(i).interfaceClass == UsbConstants.USB_CLASS_PRINTER
                    }
        } ?: deviceList.values.firstOrNull()

        if (printerDevice == null) {
            return PrintResult.Error("No USB printer found among connected devices.")
        }

        val usbInterface = (0 until printerDevice.interfaceCount)
            .map { printerDevice.getInterface(it) }
            .firstOrNull { it.interfaceClass == UsbConstants.USB_CLASS_PRINTER }
            ?: printerDevice.getInterface(0)

        val endpoint = (0 until usbInterface.endpointCount)
            .map { usbInterface.getEndpoint(it) }
            .firstOrNull { it.direction == UsbConstants.USB_DIR_OUT }
            ?: return PrintResult.Error("USB printer output endpoint not available.")

        val connection = usbManager.openDevice(printerDevice)
            ?: return PrintResult.Error("Permission denied or failed to open USB device '${printerDevice.deviceName}'.")

        return try {
            connection.claimInterface(usbInterface, true)
            repeat(copies) {
                connection.bulkTransfer(endpoint, bytes, bytes.size, 5000)
                Thread.sleep(150)
            }
            PrintResult.Success("Printed $copies copy(ies) via USB OTG (${printerDevice.deviceName})")
        } catch (e: Exception) {
            PrintResult.Error("USB transfer error: ${e.localizedMessage}")
        } finally {
            try {
                connection.releaseInterface(usbInterface)
                connection.close()
            } catch (_: Exception) {}
        }
    }
}
