package com.example.ui.scanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.SystemClock
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.ProductEntity
import com.example.ui.MainViewModel
import com.example.ui.ScanResult
import com.example.ui.components.formatCurrency
import com.example.ui.theme.RetailSlate100
import com.example.ui.theme.RetailSlate300
import com.example.ui.theme.RetailSlate700
import com.example.ui.theme.RetailSlate900
import com.example.ui.theme.RetailStatusGreen
import com.example.ui.theme.RetailTealLight
import com.example.ui.theme.RetailTealPrimary
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerBottomSheet(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onCheckoutClicked: () -> Unit = onDismiss
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val cart by viewModel.cart.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val lastScanResult by viewModel.lastScannedResult.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Sound feedback ToneGenerator
    val toneGenerator = remember {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 95)
        } catch (_: Exception) {
            null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                toneGenerator?.release()
            } catch (_: Exception) {}
            viewModel.clearLastScannedResult()
        }
    }

    var manualBarcodeInput by remember { mutableStateOf("") }
    var isFlashOn by remember { mutableStateOf(false) }
    var useFrontCamera by remember { mutableStateOf(false) }
    var activeCamera by remember { mutableStateOf<Camera?>(null) }

    // Throttle & duplicate scan debouncing
    var lastScannedCode by remember { mutableStateOf("") }
    var lastScanTimestamp by remember { mutableLongStateOf(0L) }

    fun processBarcodeDetection(code: String) {
        val now = SystemClock.uptimeMillis()
        if (code == lastScannedCode && (now - lastScanTimestamp < 1400L)) {
            // Debounce repeated reads of same barcode
            return
        }

        lastScannedCode = code
        lastScanTimestamp = now

        val result = viewModel.scanBarcode(code)
        if (result.success) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
            } catch (_: Exception) {}
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = RetailSlate900,
        modifier = Modifier
            .fillMaxSize()
            .testTag("barcode_scanner_sheet")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (hasCameraPermission) {
                // Live Camera Stream
                CameraPreviewView(
                    isFlashOn = isFlashOn,
                    useFrontCamera = useFrontCamera,
                    onBarcodeScanned = { code ->
                        processBarcodeDetection(code)
                    },
                    onCameraReady = { cam ->
                        activeCamera = cam
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Viewfinder scanning overlay with animated reticle
                ScannerOverlay(
                    modifier = Modifier.fillMaxSize(),
                    isScanning = true
                )
            } else {
                // Permission Fallback Screen
                PermissionFallbackContent(
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                )
            }

            // Top Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(RetailTealPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.QrCodeScanner,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Barcode Scanner",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Point camera at product barcode or SKU",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (hasCameraPermission) {
                        // Flash Toggle
                        IconButton(
                            onClick = {
                                isFlashOn = !isFlashOn
                                activeCamera?.cameraControl?.enableTorch(isFlashOn)
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (isFlashOn) RetailTealPrimary else Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                if (isFlashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                                contentDescription = "Toggle Torch",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Camera Switcher
                        IconButton(
                            onClick = {
                                useFrontCamera = !useFrontCamera
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                Icons.Filled.Cameraswitch,
                                contentDescription = "Switch Camera",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Close Button
                    IconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.25f),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("close_scanner_button")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close scanner")
                    }
                }
            }

            // Floating Scan Result Notification Banner
            androidx.compose.animation.AnimatedVisibility(
                visible = lastScanResult != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 70.dp, start = 16.dp, end = 16.dp)
            ) {
                lastScanResult?.let { res ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (res.success) RetailStatusGreen else Color(0xFFD32F2F)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scan_result_banner")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                if (res.success) Icons.Filled.CheckCircle else Icons.Filled.Error,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (res.success) "Item Added to Cart" else "Scan Alert",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = res.message,
                                    color = Color.White.copy(alpha = 0.95f),
                                    fontSize = 12.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = { viewModel.clearLastScannedResult() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Dismiss",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Control Deck (Manual entry, quick sample chips, cart summary)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Color(0xFF0F172A).copy(alpha = 0.92f)
                    )
                    .padding(16.dp)
            ) {
                // Manual Barcode / SKU Entry
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = manualBarcodeInput,
                        onValueChange = { manualBarcodeInput = it },
                        placeholder = {
                            Text(
                                "Or enter Barcode / SKU manually...",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = null,
                                tint = RetailTealLight,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (manualBarcodeInput.isNotBlank()) {
                                    processBarcodeDetection(manualBarcodeInput)
                                    manualBarcodeInput = ""
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = RetailTealPrimary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("manual_barcode_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (manualBarcodeInput.isNotBlank()) {
                                processBarcodeDetection(manualBarcodeInput)
                                manualBarcodeInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(54.dp)
                            .testTag("manual_barcode_submit_button")
                    ) {
                        Text("Add", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Barcode Test Chips (great for testing or fast cashier shortcuts)
                Text(
                    text = "QUICK TEST BARCODES / SKUS:",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(bottom = 2.dp)
                ) {
                    items(allProducts.take(8)) { prod ->
                        val code = if (prod.barcode.isNotBlank()) prod.barcode else prod.sku
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.clickable {
                                processBarcodeDetection(code)
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Filled.QrCode,
                                    contentDescription = null,
                                    tint = RetailTealLight,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Column {
                                    Text(
                                        text = prod.name.take(18) + if (prod.name.length > 18) "…" else "",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "$code • ${formatCurrency(prod.sellingPrice)}",
                                        color = RetailTealLight,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Cart Summary Bar
                val totalQty = cart.sumOf { it.quantity }
                val subtotal = cart.sumOf { it.subtotal }
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.ShoppingCart,
                                contentDescription = null,
                                tint = RetailTealLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (totalQty == 0) "Cart is empty" else "$totalQty item${if (totalQty > 1) "s" else ""} in cart",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (totalQty > 0) {
                                    Text(
                                        text = "Subtotal: ${formatCurrency(subtotal)}",
                                        color = RetailTealLight,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onCheckoutClicked,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (totalQty > 0) RetailStatusGreen else Color.White.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("scanner_done_button")
                        ) {
                            Text(
                                text = if (totalQty > 0) "Review & Pay" else "Done",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreviewView(
    isFlashOn: Boolean,
    useFrontCamera: Boolean,
    onBarcodeScanned: (String) -> Unit,
    onCameraReady: (Camera) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember { BarcodeScanning.getClient() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            try {
                barcodeScanner.close()
            } catch (_: Exception) {}
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                @OptIn(ExperimentalGetImage::class)
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val image = InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )
                                barcodeScanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        for (barcode in barcodes) {
                                            val rawValue = barcode.rawValue
                                            if (!rawValue.isNullOrBlank()) {
                                                ContextCompat.getMainExecutor(ctx).execute {
                                                    onBarcodeScanned(rawValue)
                                                }
                                            }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w("BarcodeScanner", "Scan error", e)
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            } else {
                                imageProxy.close()
                            }
                        }
                    }

                val cameraSelector = if (useFrontCamera) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }

                try {
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                    camera.cameraControl.enableTorch(isFlashOn)
                    onCameraReady(camera)
                } catch (exc: Exception) {
                    Log.e("BarcodeScanner", "Camera binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        update = {
            // Re-evaluates on orientation or recomposition
        },
        modifier = modifier
    )
}

@Composable
fun ScannerOverlay(
    modifier: Modifier = Modifier,
    isScanning: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_line")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_animation"
    )

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Reticle bounding box dimensions (centered)
        val boxWidth = canvasWidth * 0.72f
        val boxHeight = boxWidth * 0.65f // Barcode aspect ratio
        val boxLeft = (canvasWidth - boxWidth) / 2f
        val boxTop = (canvasHeight - boxHeight) / 2f - (canvasHeight * 0.06f) // slightly above center to make room for bottom dock

        // Dark dimming around the cutout
        drawRect(
            color = Color.Black.copy(alpha = 0.45f),
            size = size
        )

        // Clear or highlight the target area
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(boxLeft, boxTop),
            size = Size(boxWidth, boxHeight),
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
        )

        // Corner Guides (Teal brackets)
        val cornerLength = 32.dp.toPx()
        val strokeWidth = 4.dp.toPx()
        val bracketColor = Color(0xFF14B8A6) // RetailTealPrimary

        // Top-Left Corner
        drawLine(
            color = bracketColor,
            start = Offset(boxLeft, boxTop + cornerLength),
            end = Offset(boxLeft, boxTop),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = bracketColor,
            start = Offset(boxLeft, boxTop),
            end = Offset(boxLeft + cornerLength, boxTop),
            strokeWidth = strokeWidth
        )

        // Top-Right Corner
        drawLine(
            color = bracketColor,
            start = Offset(boxLeft + boxWidth - cornerLength, boxTop),
            end = Offset(boxLeft + boxWidth, boxTop),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = bracketColor,
            start = Offset(boxLeft + boxWidth, boxTop),
            end = Offset(boxLeft + boxWidth, boxTop + cornerLength),
            strokeWidth = strokeWidth
        )

        // Bottom-Left Corner
        drawLine(
            color = bracketColor,
            start = Offset(boxLeft, boxTop + boxHeight - cornerLength),
            end = Offset(boxLeft, boxTop + boxHeight),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = bracketColor,
            start = Offset(boxLeft, boxTop + boxHeight),
            end = Offset(boxLeft + cornerLength, boxTop + boxHeight),
            strokeWidth = strokeWidth
        )

        // Bottom-Right Corner
        drawLine(
            color = bracketColor,
            start = Offset(boxLeft + boxWidth - cornerLength, boxTop + boxHeight),
            end = Offset(boxLeft + boxWidth, boxTop + boxHeight),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = bracketColor,
            start = Offset(boxLeft + boxWidth, boxTop + boxHeight - cornerLength),
            end = Offset(boxLeft + boxWidth, boxTop + boxHeight),
            strokeWidth = strokeWidth
        )

        // Moving Laser scan line
        if (isScanning) {
            val laserCurrentY = boxTop + (boxHeight * laserY)
            drawLine(
                color = Color(0xFF2DD4BF),
                start = Offset(boxLeft + 12.dp.toPx(), laserCurrentY),
                end = Offset(boxLeft + boxWidth - 12.dp.toPx(), laserCurrentY),
                strokeWidth = 2.5.dp.toPx()
            )
        }
    }
}

@Composable
fun PermissionFallbackContent(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.PhotoCamera,
                contentDescription = null,
                tint = RetailTealLight,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Camera Permission Needed",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "To scan physical barcodes and SKUs directly into your POS checkout cart, please allow camera access.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("grant_camera_permission_button")
        ) {
            Text("Grant Camera Permission", fontWeight = FontWeight.Bold)
        }
    }
}
