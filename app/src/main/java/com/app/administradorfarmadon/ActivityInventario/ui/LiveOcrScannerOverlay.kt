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
import java.util.concurrent.Executors
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

    val lotStabilityMap = remember { mutableStateMapOf<String, Int>() }
    val vencStabilityMap = remember { mutableStateMapOf<String, Int>() }
    val stabilityThreshold = 2

    val recognizer = remember {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    val boxWidthPercent = 0.84f
    val boxHeightPercent = 0.16f

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

                                val (lote, venc) = extractLoteAndVenc(visionText.text)
                                val normalizedVenc = normalizeDetectedDate(venc)

                                if (lote != null && lote.length >= 4) {
                                    val count = (lotStabilityMap[lote] ?: 0) + 1
                                    lotStabilityMap[lote] = count
                                    if (count >= stabilityThreshold) {
                                        detectedLote = lote
                                    }
                                }

                                if (normalizedVenc != null) {
                                    val count = (vencStabilityMap[normalizedVenc] ?: 0) + 1
                                    vencStabilityMap[normalizedVenc] = count
                                    if (count >= stabilityThreshold) {
                                        detectedVenc = normalizedVenc
                                    }
                                }

                                if (detectedLote != null && detectedVenc != null) {
                                    if (detectedLote!!.length < 3) return@addOnSuccessListener

                                    isConfirmed = true
                                    try {
                                        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                                        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                                    } catch (e: Exception) {
                                        Log.e("Scanner", "Error playing beep", e)
                                    }
                                    onResultDetected(detectedLote, detectedVenc)
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
            modifier = Modifier.fillMaxSize()
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

            Spacer(modifier = Modifier.height(100.dp))

            Text(
                text = "Alinea lote y vencimiento aquí",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 60.dp)
            ) {
                if (detectedLote != null || detectedVenc != null) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            DetectionRow("LOTE:", detectedLote)
                            Spacer(modifier = Modifier.height(8.dp))
                            DetectionRow("VENCE:", detectedVenc)
                        }
                    }
                }

                Surface(
                    color = if (detectedLote != null && detectedVenc != null) Color(0xFF0E8F63) else Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = if (detectedLote != null && detectedVenc != null) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (detectedLote == null || detectedVenc == null) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (detectedLote != null && detectedVenc != null)
                                "¡Detectado!" else "Escaneando con IA...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (detectedLote != null || detectedVenc != null) {
                    Button(
                        onClick = {
                            isConfirmed = true
                            onResultDetected(detectedLote, detectedVenc)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirmar ahora")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetectionRow(label: String, value: String?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value ?: "Buscando...",
            color = if (value != null) Color.White else Color.Gray.copy(alpha = 0.5f),
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

private fun extractLoteAndVenc(text: String): Pair<String?, String?> {
    val rawLines = text.lines().map { it.uppercase().trim() }.filter { it.isNotBlank() }

    var lote: String? = null
    var venc: String? = null

    val lotPrefixes = listOf("LOTE", "LOT", "BATCH", "B/", "L.", "L:", "FAB", "PROD", "SERIE", "SER", "CHGE", "BN")
    val expPrefixes = listOf("EXP", "VENC", "VTO", "CAD", "VAL", "V:", "USE BY", "BB", "BBD", "MAV", "F.V.", "FV", "F.C.", "FC", "BEST BY", "CONSUMIR ANTES DE", "EE")

    val lotRegexString = lotPrefixes.joinToString("|") { Pattern.quote(it) }
    val expRegexString = expPrefixes.joinToString("|") { Pattern.quote(it) }

    val combinedLotPattern = Pattern.compile("(?:$lotRegexString)\\s*[:.-]?\\s*([A-Z0-9-]{4,})")
    val combinedExpPattern = Pattern.compile("(?:$expRegexString)\\s*[:.-]?\\s*(\\d{2}[/\\-\\s]\\d{2}[/\\-\\s]\\d{2,4}|\\d{2}[/\\-\\s]\\d{2,4}|\\d{4}[/\\-\\s]\\d{2})")

    for (line in rawLines) {
        if (lote == null) {
            val m = combinedLotPattern.matcher(line)
            if (m.find()) lote = m.group(1)
        }
        if (venc == null) {
            val m = combinedExpPattern.matcher(line)
            if (m.find()) {
                val d = m.group(1)
                if (isValidMonthDate(d)) venc = d
            }
        }
    }

    if (lote == null || venc == null) {
        rawLines.forEachIndexed { index, line ->
            val matchingLotPrefix = lotPrefixes.firstOrNull { line.contains(it) }
            if (lote == null && matchingLotPrefix != null) {
                val startIndex = line.indexOf(matchingLotPrefix) + matchingLotPrefix.length
                val lineAfterLabel = line.substring(startIndex).trim()

                val cleanTokens = lineAfterLabel
                    .replace(Regex("[:.-]"), " ")
                    .split(" ")
                    .filter { it.matches(Regex("[A-Z0-9-]{2,15}")) }

                if (cleanTokens.isNotEmpty()) {
                    lote = cleanTokens.joinToString("-")
                } else {
                    val nextLine = rawLines.getOrNull(index + 1)
                    if (nextLine != null && nextLine.matches(Regex("[A-Z0-9-]{4,20}"))) {
                        lote = nextLine
                    }
                }
            }
            if (venc == null && expPrefixes.any { line.contains(it) }) {
                val lineAfterLabel = line.replace(Regex("(?:$expRegexString)[:.-]?"), "").trim()
                if (isValidMonthDate(lineAfterLabel)) {
                    venc = lineAfterLabel
                } else {
                    val nextLine = rawLines.getOrNull(index + 1)
                    if (nextLine != null) {
                        val potentialDate = nextLine.replace(" ", "")
                        if (isValidMonthDate(potentialDate)) {
                            venc = potentialDate
                        }
                    }
                }
            }
        }
    }

    if (venc == null) {
        val strictDatePattern = Pattern.compile("(\\d{2}[/-]\\d{2,4}|\\d{4}[/-]\\d{2})")
        val matcher = strictDatePattern.matcher(text.replace(" ", ""))
        while (matcher.find()) {
            val d = matcher.group(1)
            if (isValidMonthDate(d)) {
                venc = d
                break
            }
        }
    }

    return Pair(lote, venc)
}

private fun isValidMonthDate(date: String?): Boolean {
    if (date == null) return false
    val digits = date.filter { it.isDigit() }
    if (digits.length < 4) return false

    val parts = date.split(Regex("[/\\-\\s]+")).filter { it.isNotBlank() }

    return when {
        parts.size >= 3 -> {
            val d0 = parts[0].toIntOrNull() ?: 0
            val d1 = parts[1].toIntOrNull() ?: 0
            (d0 in 1..12) || (d1 in 1..12)
        }
        parts.size == 2 -> {
            val monthPart = if (parts[0].length == 4) parts[1] else parts[0]
            val month = monthPart.toIntOrNull() ?: 0
            month in 1..12
        }
        else -> {
            val month = digits.take(2).toIntOrNull() ?: 0
            month in 1..12
        }
    }
}

private fun normalizeDetectedDate(raw: String?): String? {
    if (raw == null) return null

    val clean = raw.uppercase()
        .replace(Regex("[/\\-\\s]+"), "/")
        .trim('/')

    val parts = clean.split("/").filter { it.isNotBlank() }

    if (parts.size >= 3) {
        val d0 = parts[0].toIntOrNull() ?: 0
        val d1 = parts[1].toIntOrNull() ?: 0
        val year = parts.last().takeLast(2)

        return when {
            d1 in 1..12 -> parts[1].padStart(2, '0') + "/" + year
            d0 in 1..12 -> parts[0].padStart(2, '0') + "/" + year
            else -> null
        }
    }

    if (!isValidMonthDate(clean)) return null
    val digits = clean.filter { it.isDigit() }
    return when {
        clean.matches(Regex("\\d{4}/\\d{2}")) -> clean.takeLast(2) + "/" + clean.substring(2, 4)
        clean.matches(Regex("\\d{2}/\\d{4}")) -> clean.take(2) + "/" + clean.takeLast(2)
        clean.matches(Regex("\\d{2}/\\d{2}")) -> clean
        digits.length == 4 -> {
            val m = digits.take(2).toIntOrNull() ?: 0
            if (m in 1..12) digits.take(2) + "/" + digits.takeLast(2) else null
        }
        else -> null
    }
}
