package com.example.printer

enum class PrinterConnectionType(val displayName: String, val iconDescription: String) {
    VIRTUAL_PREVIEW("Virtual / On-Screen Preview", "Simulate realistic thermal receipt on device"),
    BLUETOOTH("Bluetooth Thermal Printer", "Connect to paired 58mm/80mm Bluetooth printer"),
    NETWORK("Network (Wi-Fi / LAN IP)", "Connect to kitchen or counter printer via IP:Port"),
    USB("USB OTG Printer", "Direct USB connection to POS thermal printer"),
    SYSTEM_PRINT("Android Print Spooler", "Use standard Android print service")
}

enum class PrinterPaperWidth(val mm: Int, val charWidth: Int, val dotWidth: Int) {
    MM_58(58, 32, 384),
    MM_80(80, 48, 576)
}

enum class PrintJobType(val label: String) {
    CUSTOMER_RECEIPT("Customer Receipt"),
    ORDER_TICKET("Kitchen / Bar Order Ticket"),
    Z_REPORT("End-of-Day Z-Report"),
    TEST_PAGE("Thermal Test Slip")
}

data class ThermalPrinterConfig(
    val connectionType: PrinterConnectionType = PrinterConnectionType.VIRTUAL_PREVIEW,
    val paperWidth: PrinterPaperWidth = PrinterPaperWidth.MM_80,
    val ipAddress: String = "192.168.1.100",
    val port: Int = 9100,
    val bluetoothAddress: String = "",
    val bluetoothName: String = "",
    val autoPrintReceipt: Boolean = true,
    val autoPrintOrder: Boolean = true,
    val receiptCopies: Int = 1,
    val orderCopies: Int = 1,
    val openCashDrawer: Boolean = true,
    val cutPaper: Boolean = true,
    val beepKitchenTicket: Boolean = true,
    val printKhmerAsBitmap: Boolean = true
)

data class DiscoveredBluetoothPrinter(
    val name: String,
    val address: String,
    val isLikelyPrinter: Boolean = false
)

sealed class PrintResult {
    data class Success(val message: String, val rawReceiptText: String = "") : PrintResult()
    data class Error(val errorMessage: String) : PrintResult()
}
