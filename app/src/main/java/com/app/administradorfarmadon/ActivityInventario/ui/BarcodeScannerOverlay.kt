package com.app.administradorfarmadon.ActivityInventario.ui

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.activity.compose.BackHandler
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.PhotoCamera
import com.app.administradorfarmadon.ActivityInventario.reference.BarcodeScanResult
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

@androidx.camera.core.ExperimentalGetImage
@Composable
fun BarcodeScannerOverlay(
    onBarcodeDetected: (BarcodeScanResult) -> Unit,
    onImageFallbackDetected: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    BackHandler {
        onDismiss()
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var hasFlash by remember { mutableStateOf(false) }
    var isFlashOn by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var cameraProviderRef by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    val detected = remember { java.util.concurrent.atomic.AtomicBoolean(false) }
    val lastCandidate = remember { java.util.concurrent.atomic.AtomicReference<String?>(null) }
    val stableCount = remember { java.util.concurrent.atomic.AtomicInteger(0) }

    val candidateStartedAt = remember {
        java.util.concurrent.atomic.AtomicLong(0L)
    }

    val minStableReads = 6
    val minStableMillis = 900L

    var scannerMessage by remember {
        mutableStateOf("Enfoca el código y mantén visible la etiqueta")
    }

    var scannerSuccess by remember {
        mutableStateOf(false)
    }

    var showImageFallback by remember {
        mutableStateOf(false)
    }

    var lastFrameBase64 by remember {
        mutableStateOf<String?>(null)
    }

    var isSendingImageFallback by remember {
        mutableStateOf(false)
    }

    val lastFrameCaptureTime = remember {
        java.util.concurrent.atomic.AtomicLong(0L)
    }

    val mainHandler = remember {
        Handler(Looper.getMainLooper())
    }

    val scanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_CODE_128
            )
            .build()

        BarcodeScanning.getClient(options)
    }

    LaunchedEffect(Unit) {
        delay(2600)

        if (!detected.get()) {
            showImageFallback = true
            scannerMessage = "No logro leer el código. Puedes identificar por imagen."
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraControl?.enableTorch(false)
            cameraProviderRef?.unbindAll()
            cameraExecutor.shutdown()
            scanner.close()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    cameraProviderRef = cameraProvider

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        if (detected.get()) {
                            imageProxy.close()
                            return@setAnalyzer
                        }

                        val mediaImage = imageProxy.image

                        if (mediaImage != null) {
                            val now = System.currentTimeMillis()

                            if (now - lastFrameCaptureTime.get() > 850L) {
                                lastFrameCaptureTime.set(now)

                                try {
                                    val fullBitmap = imageProxy.toBitmap()
                                    val rotatedFull = rotateBitmap(
                                        fullBitmap,
                                        imageProxy.imageInfo.rotationDegrees.toFloat()
                                    )

                                    val fullBase64 = rotatedFull.toOptimizedBase64()

                                    mainHandler.post {
                                        lastFrameBase64 = fullBase64
                                    }
                                } catch (e: Exception) {
                                    Log.e(
                                        "BarcodeScanner",
                                        "Error capturando imagen fallback",
                                        e
                                    )
                                }
                            }

                            val scanRect = buildBarcodeScanRect(
                                imageProxy.width,
                                imageProxy.height
                            )

                            imageProxy.setCropRect(scanRect)

                            val image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )

                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    if (detected.get()) return@addOnSuccessListener

                                    for (barcode in barcodes) {
                                        if (!barcode.isInsideScanRect(scanRect)) continue

                                        val value = barcode.rawValue?.trim().orEmpty()
                                        if (value.isBlank()) continue

                                        if (!isValidBarcodeForInventory(value, barcode.format)) {
                                            continue
                                        }

                                        val nowRead = System.currentTimeMillis()
                                        val previous = lastCandidate.get()

                                        if (previous == value) {
                                            stableCount.incrementAndGet()
                                        } else {
                                            lastCandidate.set(value)
                                            stableCount.set(1)
                                            candidateStartedAt.set(nowRead)
                                        }

                                        val stableReads = stableCount.get()
                                        val stableTime = nowRead - candidateStartedAt.get()

                                        if (stableReads < minStableReads || stableTime < minStableMillis) {
                                            mainHandler.post {
                                                scannerMessage = when {
                                                    stableReads < 2 -> "Enfoca el código y mantén visible la etiqueta"
                                                    stableReads < minStableReads -> "Confirmando lectura..."
                                                    else -> "Mantén el producto quieto un momento..."
                                                }
                                            }

                                            continue
                                        }

                                        if (!detected.compareAndSet(false, true)) {
                                            return@addOnSuccessListener
                                        }

                                        val base64 = lastFrameBase64 ?: run {
                                            val bitmap = imageProxy.toBitmap()
                                            val rotated = rotateBitmap(
                                                bitmap,
                                                imageProxy.imageInfo.rotationDegrees.toFloat()
                                            )
                                            rotated.toOptimizedBase64()
                                        }

                                        try {
                                            val toneGen = ToneGenerator(
                                                AudioManager.STREAM_ALARM,
                                                100
                                            )

                                            toneGen.startTone(
                                                ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,
                                                180
                                            )
                                        } catch (e: Exception) {
                                            Log.e("Scanner", "Error playing beep", e)
                                        }

                                        mainHandler.post {
                                            scannerSuccess = true
                                            scannerMessage = "Código leído correctamente"
                                        }

                                        mainHandler.postDelayed(
                                            {
                                                onBarcodeDetected(
                                                    BarcodeScanResult(
                                                        code = value,
                                                        imageBase64 = base64
                                                    )
                                                )
                                            },
                                            280L
                                        )

                                        return@addOnSuccessListener
                                    }
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()

                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )

                        cameraControl = camera.cameraControl
                        hasFlash = camera.cameraInfo.hasFlashUnit()
                    } catch (exc: Exception) {
                        Log.e("BarcodeScanner", "Use case binding failed", exc)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val boxWidth = 330.dp.toPx()
            val boxHeight = 160.dp.toPx()

            val left = (canvasWidth - boxWidth) / 2
            val top = (canvasHeight - boxHeight) / 2

            val checkpoint = drawContext.canvas.nativeCanvas.saveLayer(null, null)

            drawRect(
                Color.Black.copy(alpha = 0.62f)
            )

            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(
                    boxWidth,
                    boxHeight
                ),
                cornerRadius = CornerRadius(
                    18.dp.toPx(),
                    18.dp.toPx()
                ),
                blendMode = BlendMode.Clear
            )

            drawContext.canvas.nativeCanvas.restoreToCount(checkpoint)

            drawRoundRect(
                color = Color.White.copy(alpha = 0.92f),
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(
                    boxWidth,
                    boxHeight
                ),
                cornerRadius = CornerRadius(
                    18.dp.toPx(),
                    18.dp.toPx()
                ),
                style = Stroke(
                    width = 2.8.dp.toPx()
                )
            )

            val lineLength = 32.dp.toPx()
            val strokeWidth = 5.dp.toPx()

            drawLine(
                Color.White,
                Offset(left, top + lineLength),
                Offset(left, top),
                strokeWidth
            )

            drawLine(
                Color.White,
                Offset(left, top),
                Offset(left + lineLength, top),
                strokeWidth
            )

            drawLine(
                Color.White,
                Offset(left + boxWidth - lineLength, top),
                Offset(left + boxWidth, top),
                strokeWidth
            )

            drawLine(
                Color.White,
                Offset(left + boxWidth, top),
                Offset(left + boxWidth, top + lineLength),
                strokeWidth
            )

            drawLine(
                Color.White,
                Offset(left, top + boxHeight - lineLength),
                Offset(left, top + boxHeight),
                strokeWidth
            )

            drawLine(
                Color.White,
                Offset(left, top + boxHeight),
                Offset(left + lineLength, top + boxHeight),
                strokeWidth
            )

            drawLine(
                Color.White,
                Offset(left + boxWidth - lineLength, top + boxHeight),
                Offset(left + boxWidth, top + boxHeight),
                strokeWidth
            )

            drawLine(
                Color.White,
                Offset(left + boxWidth, top + boxHeight),
                Offset(left + boxWidth, top + boxHeight - lineLength),
                strokeWidth
            )

            drawLine(
                color = if (scannerSuccess) {
                    CreateGreen.copy(alpha = 0.95f)
                } else {
                    Color.Red.copy(alpha = 0.72f)
                },
                start = Offset(
                    left + 14.dp.toPx(),
                    top + boxHeight / 2
                ),
                end = Offset(
                    left + boxWidth - 14.dp.toPx(),
                    top + boxHeight / 2
                ),
                strokeWidth = 2.4.dp.toPx()
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.background(
                        Color.Black.copy(alpha = 0.52f),
                        CircleShape
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White
                    )
                }

                if (hasFlash) {
                    IconButton(
                        onClick = {
                            isFlashOn = !isFlashOn
                            cameraControl?.enableTorch(isFlashOn)
                        },
                        modifier = Modifier.background(
                            Color.Black.copy(alpha = 0.52f),
                            CircleShape
                        )
                    ) {
                        Icon(
                            imageVector = if (isFlashOn) {
                                Icons.Default.FlashOn
                            } else {
                                Icons.Default.FlashOff
                            },
                            contentDescription = "Flash",
                            tint = if (isFlashOn) {
                                Color.Yellow
                            } else {
                                Color.White
                            }
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (showImageFallback && !scannerSuccess) {
                Button(
                    onClick = {
                        val image = lastFrameBase64 ?: return@Button

                        if (detected.compareAndSet(false, true)) {
                            isSendingImageFallback = true
                            scannerMessage = "Analizando imagen del producto..."

                            onImageFallbackDetected(image)
                        }
                    },
                    enabled = !isSendingImageFallback && lastFrameBase64 != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .heightIn(min = 56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CreateGreenDark,
                        contentColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.22f),
                        disabledContentColor = Color.White.copy(alpha = 0.55f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    if (isSendingImageFallback) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "Analizando...",
                            fontWeight = FontWeight.Black
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "Identificar por imagen",
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            Surface(
                color = if (scannerSuccess) {
                    CreateGreen.copy(alpha = 0.92f)
                } else {
                    Color.Black.copy(alpha = 0.72f)
                },
                shape = RoundedCornerShape(16.dp),
                border = if (scannerSuccess) {
                    null
                } else {
                    BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.16f)
                    )
                },
                shadowElevation = 8.dp,
                modifier = Modifier.padding(bottom = 42.dp)
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = 18.dp,
                        vertical = 11.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (!scannerSuccess) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(15.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )

                        Spacer(modifier = Modifier.width(9.dp))
                    }

                    Text(
                        text = scannerMessage,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


private fun isValidBarcodeForInventory(
    value: String,
    format: Int
): Boolean {
    val clean = value.trim()

    return when (format) {
        Barcode.FORMAT_EAN_13 -> {
            clean.length == 13 &&
                    clean.all { it.isDigit() } &&
                    isGs1ChecksumValid(clean)
        }

        Barcode.FORMAT_EAN_8 -> {
            clean.length == 8 &&
                    clean.all { it.isDigit() } &&
                    isGs1ChecksumValid(clean)
        }

        Barcode.FORMAT_UPC_A -> {
            clean.length == 12 &&
                    clean.all { it.isDigit() } &&
                    isGs1ChecksumValid(clean)
        }

        Barcode.FORMAT_UPC_E -> {
            clean.length in 6..8 &&
                    clean.all { it.isDigit() }
        }

        Barcode.FORMAT_CODE_128 -> {
            clean.length in 4..40
        }

        else -> false
    }
}

private fun Bitmap.toOptimizedBase64(
    maxSide: Int = 900,
    quality: Int = 72
): String {
    val biggestSide = maxOf(width, height)

    val bitmapToEncode = if (biggestSide > maxSide) {
        val scale = maxSide.toFloat() / biggestSide.toFloat()
        Bitmap.createScaledBitmap(
            this,
            (width * scale).toInt().coerceAtLeast(1),
            (height * scale).toInt().coerceAtLeast(1),
            true
        )
    } else {
        this
    }

    val output = ByteArrayOutputStream()
    bitmapToEncode.compress(
        Bitmap.CompressFormat.JPEG,
        quality,
        output
    )

    return Base64.encodeToString(
        output.toByteArray(),
        Base64.NO_WRAP
    )
}

private fun isGs1ChecksumValid(code: String): Boolean {
    if (code.length < 2 || !code.all { it.isDigit() }) return false

    val digits = code.map { it.digitToInt() }
    val checkDigit = digits.last()
    val body = digits.dropLast(1)

    val sum = body
        .asReversed()
        .mapIndexed { index, digit ->
            digit * if (index % 2 == 0) 3 else 1
        }
        .sum()

    val calculated = (10 - (sum % 10)) % 10

    return calculated == checkDigit
}

/**
 * V18.2: Utilidades de conversión para captura de frames en el escáner.
 */
private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
    if (angle == 0f) return source
    val matrix = Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}

private fun Bitmap.toBase64(): String {
    val outputStream = ByteArrayOutputStream()
    // Redimensionar si es muy grande para ahorrar ancho de banda
    val scaled = if (width > 640 || height > 640) {
        val scale = 640f / Math.max(width, height)
        Bitmap.createScaledBitmap(this, (width * scale).toInt(), (height * scale).toInt(), true)
    } else this
    
    scaled.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
    return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
}

private fun buildBarcodeScanRect(width: Int, height: Int): Rect {
    // Ampliamos el área lógica de escaneo para que coincida con el nuevo cuadro visual
    val boxWidth = (width * 0.88f).toInt()
    val boxHeight = (height * 0.45f).toInt()
    val left = (width - boxWidth) / 2
    val top = (height - boxHeight) / 2
    return Rect(left, top, left + boxWidth, top + boxHeight)
}

private fun Barcode.isInsideScanRect(scanRect: Rect): Boolean {
    val box = boundingBox ?: return true
    return scanRect.contains(box.centerX(), box.centerY())
}
