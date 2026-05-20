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
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import android.graphics.*
import android.util.Base64
import com.app.administradorfarmadon.ActivityInventario.reference.BarcodeScanResult
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

@androidx.camera.core.ExperimentalGetImage
@Composable
fun BarcodeScannerOverlay(
    onBarcodeDetected: (BarcodeScanResult) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    var hasFlash by remember { mutableStateOf(false) }
    var isFlashOn by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var cameraProviderRef by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var isDetected by remember { mutableStateOf(false) }

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

    DisposableEffect(Unit) {
        onDispose {
            cameraControl?.enableTorch(false)
            cameraProviderRef?.unbindAll()
            cameraExecutor.shutdown()
            scanner.close()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
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
                        if (isDetected) {
                            imageProxy.close()
                            return@setAnalyzer
                        }

                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val scanRect = buildBarcodeScanRect(imageProxy.width, imageProxy.height)
                            imageProxy.setCropRect(scanRect)
                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    if (isDetected) return@addOnSuccessListener
                                    for (barcode in barcodes) {
                                        if (!barcode.isInsideScanRect(scanRect)) continue
                                        barcode.rawValue?.let { value ->
                                            if (value.isNotBlank()) {
                                                isDetected = true

                                                // V18.2: Capturar frame para Gemini Vision
                                                val bitmap = imageProxy.toBitmap()
                                                val rotated = rotateBitmap(bitmap, imageProxy.imageInfo.rotationDegrees.toFloat())
                                                val base64 = rotated.toBase64()

                                                // Pitido clásico de escáner
                                                try {
                                                    val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                                                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                                                } catch (e: Exception) {
                                                    Log.e("Scanner", "Error playing beep", e)
                                                }
                                                onBarcodeDetected(BarcodeScanResult(value, base64))
                                                return@addOnSuccessListener
                                            }
                                        }
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

        // Cuadro de escaneo profesional
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            // Cuadro (280dp x 180dp aprox)
            val boxWidth = 280.dp.toPx()
            val boxHeight = 180.dp.toPx()
            val left = (canvasWidth - boxWidth) / 2
            val top = (canvasHeight - boxHeight) / 2
            
            // 1. Fondo oscurecido con hueco
            val checkPoint = drawContext.canvas.nativeCanvas.saveLayer(null, null)
            drawRect(Color.Black.copy(alpha = 0.6f))
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(boxWidth, boxHeight),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                blendMode = BlendMode.Clear
            )
            drawContext.canvas.nativeCanvas.restoreToCount(checkPoint)
            
            // 2. Esquineras
            val lineLen = 30.dp.toPx()
            val strokeWidth = 4.dp.toPx()
            val color = Color.White
            
            // Top Left
            drawLine(color, Offset(left, top + lineLen), Offset(left, top), strokeWidth)
            drawLine(color, Offset(left, top), Offset(left + lineLen, top), strokeWidth)
            
            // Top Right
            drawLine(color, Offset(left + boxWidth - lineLen, top), Offset(left + boxWidth, top), strokeWidth)
            drawLine(color, Offset(left + boxWidth, top), Offset(left + boxWidth, top + lineLen), strokeWidth)
            
            // Bottom Left
            drawLine(color, Offset(left, top + boxHeight - lineLen), Offset(left, top + boxHeight), strokeWidth)
            drawLine(color, Offset(left, top + boxHeight), Offset(left + lineLen, top + boxHeight), strokeWidth)
            
            // Bottom Right
            drawLine(color, Offset(left + boxWidth - lineLen, top + boxHeight), Offset(left + boxWidth, top + boxHeight), strokeWidth)
            drawLine(color, Offset(left + boxWidth, top + boxHeight), Offset(left + boxWidth, top + boxHeight - lineLen), strokeWidth)

            // 3. Línea láser roja
            drawLine(
                color = Color.Red.copy(alpha = 0.6f),
                start = Offset(left + 12.dp.toPx(), top + boxHeight / 2),
                end = Offset(left + boxWidth - 12.dp.toPx(), top + boxHeight / 2),
                strokeWidth = 2.dp.toPx()
            )
        }

        // UI Overlay
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                }

                if (hasFlash) {
                    IconButton(
                        onClick = {
                            isFlashOn = !isFlashOn
                            cameraControl?.enableTorch(isFlashOn)
                        },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = if (isFlashOn) Color.Yellow else Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Surface(
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(bottom = 40.dp)
            ) {
                Text(
                    text = "Apunta al código de barras",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
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
    val boxWidth = (width * 0.74f).toInt()
    val boxHeight = (height * 0.34f).toInt()
    val left = (width - boxWidth) / 2
    val top = (height - boxHeight) / 2
    return Rect(left, top, left + boxWidth, top + boxHeight)
}

private fun Barcode.isInsideScanRect(scanRect: Rect): Boolean {
    val box = boundingBox ?: return true
    return scanRect.contains(box.centerX(), box.centerY())
}
