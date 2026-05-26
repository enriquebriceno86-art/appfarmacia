package com.app.administradorfarmadon.ActivityInventario.ui

import android.graphics.Rect
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.app.administradorfarmadon.ActivityInventario.ProductUtils
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

@ExperimentalGetImage
@Composable
fun LiveOcrScannerOverlay(
    onResultDetected: (lote: String?, vencimiento: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    BackHandler {
        onDismiss()
    }

    var hasFlash by remember { mutableStateOf(false) }
    var isFlashOn by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var cameraProviderRef by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    var detectedLote by remember { mutableStateOf<String?>(null) }
    var detectedVenc by remember { mutableStateOf<String?>(null) }
    var isConfirmed by remember { mutableStateOf(false) }

    var scannerMessage by remember {
        mutableStateOf("Alinea lote y vencimiento dentro del marco")
    }

    var scannerSuccess by remember {
        mutableStateOf(false)
    }

    val mainHandler = remember {
        android.os.Handler(android.os.Looper.getMainLooper())
    }

    val lotStabilityMap = remember { mutableStateMapOf<String, Int>() }
    val vencStabilityMap = remember { mutableStateMapOf<String, Int>() }
    val stabilityThreshold = 2

    var lastAnalysisTime by remember {
        mutableLongStateOf(0L)
    }

    val recognizer = remember {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    val boxWidthPercent = 0.84f
    val boxHeightPercent = 0.16f // Reducido para mayor precisión

    // Guardamos la PreviewView en un remember para evitar recreaciones en recomposiciones
    val previewView = remember { PreviewView(context) }

    // Gestión segura del ciclo de vida de CameraX
    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProviderRef = cameraProvider

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val resolutionSelector = ResolutionSelector.Builder()
                    .setResolutionStrategy(
                        ResolutionStrategy(
                            android.util.Size(1280, 720),
                            ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                        )
                    )
                    .build()

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setResolutionSelector(resolutionSelector)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    if (isConfirmed) {
                        imageProxy.close()
                        return@setAnalyzer
                    }

                    val now = System.currentTimeMillis()

                    if (now - lastAnalysisTime < 250L) {
                        imageProxy.close()
                        return@setAnalyzer
                    }

                    lastAnalysisTime = now


                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                        val bufferW = imageProxy.width
                        val bufferH = imageProxy.height
                        val isPortrait = rotationDegrees == 90 || rotationDegrees == 270

                        val rectW = if (isPortrait) (bufferW * boxHeightPercent).toInt() else (bufferW * boxWidthPercent).toInt()
                        val rectH = if (isPortrait) (bufferH * boxWidthPercent).toInt() else (bufferH * boxHeightPercent).toInt()

                        val left = (bufferW - rectW) / 2
                        val top = (bufferH - rectH) / 2
                        val right = left + rectW
                        val bottom = top + rectH

                        try {
                            imageProxy.setCropRect(Rect(left, top, right, bottom))
                        } catch (e: Exception) {
                            Log.e("LiveOcrScanner", "Error setting crop rect", e)
                        }

                        // V21.2: En algunas versiones de ML Kit, fromMediaImage ignora el cropRect. 
                        // Usamos un bitmap recortado para garantizar que SOLO procese lo que está dentro del cuadro.
                        val bitmap = try {
                            val fullBitmap = imageProxy.toBitmap()
                            // Aseguramos que las coordenadas del recorte no excedan los límites
                            val cropLeft = left.coerceIn(0, fullBitmap.width - 1)
                            val cropTop = top.coerceIn(0, fullBitmap.height - 1)
                            val cropWidth = rectW.coerceAtMost(fullBitmap.width - cropLeft)
                            val cropHeight = rectH.coerceAtMost(fullBitmap.height - cropTop)
                            
                            if (cropWidth > 0 && cropHeight > 0) {
                                android.graphics.Bitmap.createBitmap(fullBitmap, cropLeft, cropTop, cropWidth, cropHeight)
                            } else fullBitmap
                        } catch (e: Exception) {
                            null
                        }

                        val image = if (bitmap != null) {
                            InputImage.fromBitmap(bitmap, rotationDegrees)
                        } else {
                            InputImage.fromMediaImage(mediaImage, rotationDegrees)
                        }

                        recognizer.process(image)
                            .addOnSuccessListener { visionText ->
                                if (isConfirmed) return@addOnSuccessListener

                                // V30.0: Usamos la lógica compartida de extracción
                                val smartResult = ProductUtils.extractLoteAndVencSmart(visionText.text)

                                val lote = smartResult.lote
                                val normalizedVenc = smartResult.vencimiento

                                if (lote == null && normalizedVenc != null) {
                                    Log.d("LiveOcrScanner", "Vencimiento detectado sin lote. OCR=${visionText.text}")
                                }

                                if (lote == null && normalizedVenc == null) {
                                    Log.d("LiveOcrScanner", "OCR sin datos útiles=${visionText.text}")
                                }

                                if (lote != null && lote.length >= 4) {
                                    if (lotStabilityMap.size > 20) {
                                        lotStabilityMap.clear()
                                    }

                                    val count = (lotStabilityMap[lote] ?: 0) + 1
                                    lotStabilityMap[lote] = count

                                    if (count >= stabilityThreshold) {
                                        detectedLote = lote
                                    }
                                }

                                if (normalizedVenc != null) {
                                    if (vencStabilityMap.size > 20) {
                                        vencStabilityMap.clear()
                                    }

                                    val count = (vencStabilityMap[normalizedVenc] ?: 0) + 1
                                    vencStabilityMap[normalizedVenc] = count

                                    if (count >= stabilityThreshold) {
                                        detectedVenc = normalizedVenc
                                    }
                                }

                                mainHandler.post {
                                    scannerMessage = when {
                                        detectedLote != null && detectedVenc != null -> "Lote leído correctamente"
                                        detectedLote != null -> "Lote encontrado, buscando vencimiento..."
                                        detectedVenc != null -> "Vencimiento encontrado, buscando lote..."
                                        else -> "Alinea lote y vencimiento dentro del marco"
                                    }
                                }

                                val confianzaAlta =
                                    smartResult.confianzaLote >= 70 &&
                                            smartResult.confianzaVencimiento >= 65

                                if (detectedLote != null && detectedVenc != null && confianzaAlta) {
                                    if (detectedLote!!.length < 3) return@addOnSuccessListener

                                    isConfirmed = true

                                    try {
                                        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                                        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                                    } catch (e: Exception) {
                                        Log.e("Scanner", "Error playing beep", e)
                                    }

                                    mainHandler.post {
                                        scannerSuccess = true
                                        scannerMessage = "Lote leído correctamente"
                                    }

                                    mainHandler.postDelayed(
                                        {
                                            onResultDetected(detectedLote, detectedVenc)
                                        },
                                        350L
                                    )
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
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
                cameraControl = camera.cameraControl
                hasFlash = camera.cameraInfo.hasFlashUnit()

                // ENFOQUE INICIAL AUTOMÁTICO AL CENTRO
                val factory = previewView.meteringPointFactory
                val centerPoint = factory.createPoint(0.5f, 0.5f)
                val action = FocusMeteringAction.Builder(centerPoint, FocusMeteringAction.FLAG_AF)
                    .setAutoCancelDuration(3, TimeUnit.SECONDS)
                    .build()
                cameraControl?.startFocusAndMetering(action)

            } catch (exc: Exception) {
                Log.e("LiveOcrScanner", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraControl?.enableTorch(false)
            cameraProviderRef?.unbindAll()
            cameraExecutor.shutdown()
            recognizer.close()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // TAP TO FOCUS: Enfocar donde el usuario toque
                        val factory = previewView.meteringPointFactory
                        val point = factory.createPoint(offset.x, offset.y)
                        val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                            .build()
                        cameraControl?.startFocusAndMetering(action)
                    }
                }
        )

        // Dibujo del área de escaneo (ROI Overlay)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val boxWidth = canvasWidth * boxWidthPercent
            val boxHeight = canvasHeight * boxHeightPercent
            val left = (canvasWidth - boxWidth) / 2
            val top = (canvasHeight - boxHeight) / 2

            val checkPoint = drawContext.canvas.nativeCanvas.saveLayer(null, null)
            drawRect(Color.Black.copy(alpha = 0.6f))

            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(boxWidth, boxHeight),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                blendMode = BlendMode.Clear
            )
            drawContext.canvas.nativeCanvas.restoreToCount(checkPoint)

            drawRoundRect(
                color = Color.White.copy(alpha = 0.8f),
                topLeft = Offset(left, top),
                size = Size(boxWidth, boxHeight),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )
        }

        // Componentes e interfaz visual de control
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

                Text(
                    text = "Leer lote y vencimiento",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

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
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }

            Spacer(modifier = Modifier.height(96.dp))

            Surface(
                color = Color.Black.copy(alpha = 0.48f),
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = 0.18f)
                )
            ) {
                Text(
                    text = "Enfoca lote y vencimiento",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 7.dp
                    )
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 54.dp)
            ) {
                if (detectedLote != null || detectedVenc != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(0.90f),
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(22.dp),
                        border = BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = 0.22f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            PremiumOcrDetectionRow(
                                label = "LOTE",
                                value = detectedLote
                            )

                            PremiumOcrDetectionRow(
                                label = "VENCE",
                                value = detectedVenc
                            )
                        }
                    }
                }

                Surface(
                    color = if (scannerSuccess) {
                        CreateGreen.copy(alpha = 0.94f)
                    } else {
                        Color.Black.copy(alpha = 0.72f)
                    },
                    shape = RoundedCornerShape(16.dp),
                    border = if (scannerSuccess) {
                        null
                    } else {
                        BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = 0.18f)
                        )
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 10.dp
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

                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Text(
                            text = scannerMessage,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )



                    }
                }

                if (detectedLote != null && detectedVenc != null) {
                    OutlinedButton(
                        onClick = {
                            isConfirmed = true
                            onResultDetected(detectedLote, detectedVenc)
                        },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = 0.38f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White,
                            containerColor = Color.Black.copy(alpha = 0.22f)
                        )
                    ) {

                        Text(
                            text = "Confirmar lote y vencimiento",
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

        }




    }
}

@Composable
private fun PremiumOcrDetectionRow(
    label: String,
    value: String?
) {
    val hasValue = !value.isNullOrBlank()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (hasValue) {
            CreateGreen.copy(alpha = 0.18f)
        } else {
            Color.White.copy(alpha = 0.08f)
        },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (hasValue) {
                CreateGreen.copy(alpha = 0.35f)
            } else {
                Color.White.copy(alpha = 0.14f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 13.dp,
                vertical = 11.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.size(30.dp),
                color = if (hasValue) {
                    CreateGreen.copy(alpha = 0.95f)
                } else {
                    Color.White.copy(alpha = 0.14f)
                },
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (hasValue) "✓" else "•",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.68f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )

                Text(
                    text = value ?: "Buscando...",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1
                )
            }
        }
    }
}
