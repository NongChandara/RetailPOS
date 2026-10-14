package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.printer.FormattedThermalDoc
import com.example.printer.PrintResult
import com.example.printer.PrinterConnectionType
import com.example.printer.ThermalPrinterManager
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

@Composable
fun ThermalReceiptPreviewDialog(
    doc: FormattedThermalDoc,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val printerManager = remember { ThermalPrinterManager.getInstance(context) }
    val isPrinting by printerManager.isPrinting.collectAsState()
    val config by printerManager.config.collectAsState()

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
                .testTag("thermal_receipt_preview_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(RetailTealPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Print,
                                contentDescription = null,
                                tint = RetailTealPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = doc.jobType.label,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = RetailSlate900
                            )
                            Text(
                                text = "Thermal Paper Output (${config.paperWidth.mm}mm • ${config.connectionType.displayName})",
                                fontSize = 11.sp,
                                color = RetailSlate500
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = onOpenSettings) {
                            Icon(Icons.Filled.Settings, contentDescription = "Printer Settings", tint = RetailSlate700)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = RetailSlate500)
                        }
                    }
                }

                // Status banner if print result exists
                statusMessage?.let { status ->
                    Surface(
                        color = if (isError) Color(0xFFFEF2F2) else RetailStatusGreenContainer,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isError) Color(0xFFFCA5A5) else RetailStatusGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = status,
                            color = if (isError) RetailStatusRed else RetailStatusGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // Realistic Thermal Paper Simulation Card
                Surface(
                    color = Color(0xFFFCFCF9), // Thermal roll paper off-white
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .height(340.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Jagged Serrated Top Edge
                        ThermalPaperJaggedEdge(color = Color(0xFFFCFCF9))

                        // Scrollable receipt content in monospace font
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = doc.previewText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = if (config.paperWidth.mm == 58) 10.sp else 11.sp,
                                color = Color(0xFF1E293B),
                                lineHeight = if (config.paperWidth.mm == 58) 14.sp else 16.sp
                            )
                        }

                        // Jagged Serrated Bottom Tear Edge
                        ThermalPaperJaggedEdge(color = Color(0xFFFCFCF9), isBottom = true)
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Copy text
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Thermal Receipt", doc.previewText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Receipt text copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy", fontSize = 12.sp)
                    }

                    // Share
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "TR Coffee - ${doc.jobType.label}")
                                putExtra(Intent.EXTRA_TEXT, doc.previewText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Thermal Receipt"))
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontSize = 12.sp)
                    }

                    // Print to Hardware Thermal Printer
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                statusMessage = "Sending to ${config.connectionType.displayName}..."
                                isError = false
                                val result = when (config.connectionType) {
                                    PrinterConnectionType.VIRTUAL_PREVIEW -> {
                                        PrintResult.Success("Receipt simulated on-screen")
                                    }
                                    else -> {
                                        printerManager.printTestSlip(forcePreview = false)
                                    }
                                }
                                when (result) {
                                    is PrintResult.Success -> {
                                        statusMessage = "✅ ${result.message}"
                                        isError = false
                                    }
                                    is PrintResult.Error -> {
                                        statusMessage = "❌ ${result.errorMessage}"
                                        isError = true
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.4f).height(44.dp)
                    ) {
                        if (isPrinting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Print Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Draws a realistic jagged thermal paper cutter tear-off edge.
 */
@Composable
private fun ThermalPaperJaggedEdge(color: Color, isBottom: Boolean = false) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
    ) {
        val toothWidth = 14f
        val toothHeight = 6f
        val count = (size.width / toothWidth).toInt() + 1
        val path = Path()

        if (!isBottom) {
            path.moveTo(0f, toothHeight)
            for (i in 0 until count) {
                val startX = i * toothWidth
                val midX = startX + toothWidth / 2
                val endX = (i + 1) * toothWidth
                path.lineTo(midX, 0f)
                path.lineTo(endX, toothHeight)
            }
            path.lineTo(size.width, size.height)
            path.lineTo(0f, size.height)
            path.close()
        } else {
            path.moveTo(0f, 0f)
            for (i in 0 until count) {
                val startX = i * toothWidth
                val midX = startX + toothWidth / 2
                val endX = (i + 1) * toothWidth
                path.lineTo(midX, toothHeight)
                path.lineTo(endX, 0f)
            }
            path.lineTo(size.width, 0f)
            path.close()
        }

        drawPath(path, color = Color(0xFFE2E8F0))
    }
}
