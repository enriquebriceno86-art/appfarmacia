package com.app.administradorfarmadon.ActivityInventario.ui

import android.graphics.Rect
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
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
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    var hasFlash by remember { mutableStateOf(false) }
    var isFlashOn by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var cameraProviderRef by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    
    // Resultados parciales para feedback en vivo
    var detectedLote by remember { mutableStateOf<String?>(null) }
    var detectedVenc by remember { mutableStateOf<String?>(null) }
    var isConfirmed by remember { mutableStateOf(false) }

    val recognizer = remember {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    // Definimos el cuadro de escaneo (ROI) en porcentajes para que sea responsivo
    val boxWidthPercent = 0.84f
    val boxHeightPercent = 0.14f

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
                        if (isConfirmed) {
                            imageProxy.close()
                            return@setAnalyzer
                        }

                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                            
                            // Nivel 2: Recorte Real (ROI) Robusto
                            // ML Kit procesará solo lo que esté dentro de este Rect.
                            // Calculamos el centro del buffer original.
                            val bufferW = imageProxy.width
                            val bufferH = imageProxy.height

                            // Importante: El cajón visual en la UI es horizontal.
                            // Si la rotación es 90/270 (Portrait), el buffer está "acostado".
                            // Entonces, el ancho visual (boxWidthPercent) se mapea al ALTO del buffer.
                            val isPortrait = rotationDegrees == 90 || rotationDegrees == 270
                            
                            val rectW: Int
                            val rectH: Int
                            
                            if (isPortrait) {
                                // En portrait: ancho_visual -> alto_buffer, alto_visual -> ancho_buffer
                                rectW = (bufferW * boxHeightPercent).toInt()
                                rectH = (bufferH * boxWidthPercent).toInt()
                            } else {
                                // En landscape: directo
                                rectW = (bufferW * boxWidthPercent).toInt()
                                rectH = (bufferH * boxHeightPercent).toInt()
                            }

                            val left = (bufferW - rectW) / 2
                            val top = (bufferH - rectH) / 2
                            val right = left + rectW
                            val bottom = top + rectH
                            
                            // Aplicamos el crop al ImageProxy (coordenadas del sensor)
                            try {
                                imageProxy.setCropRect(Rect(left, top, right, bottom))
                            } catch (e: Exception) {
                                Log.e("LiveOcrScanner", "Error setting crop rect", e)
                            }

                            val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
                            
                            recognizer.process(image)
                                .addOnSuccessListener { visionText ->
                                    if (isConfirmed) return@addOnSuccessListener
                                    
                                    val (lote, venc) = extractLoteAndVenc(visionText.text)
                                    val normalizedVenc = normalizeDetectedDate(venc)
                                    
                                    if (lote != null && lote != detectedLote) {
                                        detectedLote = lote
                                    }
                                    if (normalizedVenc != null && normalizedVenc != detectedVenc) {
                                        detectedVenc = normalizedVenc
                                    }

                                    // Si detectamos ambos, confirmamos automáticamente
                                    if (detectedLote != null && detectedVenc != null) {
                                        isConfirmed = true
                                        // Pitido clásico de escáner
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
                        Log.e("LiveOcrScanner", "Use case binding failed", exc)
                    }

                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Nivel 1: Visual Overlay (Cajón de lectura con fondo oscurecido)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            val boxWidth = canvasWidth * boxWidthPercent
            val boxHeight = canvasHeight * boxHeightPercent
            val left = (canvasWidth - boxWidth) / 2
            val top = (canvasHeight - boxHeight) / 2
            
            // 1. Fondo oscurecido con un hueco (BlendMode.Clear)
            val checkPoint = drawContext.canvas.nativeCanvas.saveLayer(null, null)
            
            // Fondo semi-transparente
            drawRect(Color.Black.copy(alpha = 0.6f))
            
            // Hueco transparente (Cajón)
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(boxWidth, boxHeight),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                blendMode = BlendMode.Clear
            )
            
            drawContext.canvas.nativeCanvas.restoreToCount(checkPoint)
            
            // 2. Borde del cajón
            drawRoundRect(
                color = Color.White.copy(alpha = 0.8f),
                topLeft = Offset(left, top),
                size = Size(boxWidth, boxHeight),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )
        }

        // UI Controls & Text
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
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
            
            // Instrucción sobre el cuadro
            Text(
                text = "Alinea lote y vencimiento aquí",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Feedback de detección en tiempo real
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
                    Text(
                        text = if (detectedLote != null && detectedVenc != null) 
                            "¡Detectado!" else "Escaneando...",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
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

/**
 * Lógica de extracción de LOTE y Vencimiento.
 */
private fun extractLoteAndVenc(text: String): Pair<String?, String?> {
    val lines = text.lines().map { it.uppercase() }
    
    var lote: String? = null
    var venc: String? = null

    // Regex para Lote optimizado:
    // 1. Prefijo fuerte (LOTE, LOT, BATCH) + alfanumérico de min 4 chars
    // 2. Prefijo L (solo si tiene separador claro como L: L. L-) + alfanumérico de min 4 chars
    val lotePattern = Pattern.compile("(?:(?:LOTE|LOT|BATCH)\\s*[:.-]?\\s*|\\bL\\s*[:.-]\\s*)([A-Z0-9-]{4,})")
    
    // Regex para Vencimiento optimizada:
    // Prefijos CAD, EXP, VENC, VTO, VAL, V:
    val vencPattern = Pattern.compile(
        "(?:EXP|VENC|VTO|V:|V[eE]nc|VAL|CAD)\\s*[:.-]?\\s*" +
        "(\\d{2}[/\\-\\s]\\d{2,4}|\\d{4}[/\\-\\s]\\d{2}|(?:ENE|FEB|MAR|ABR|MAY|JUN|JUL|AGO|SEP|OCT|NOV|DIC)\\s*\\d{4})"
    )

    for (line in lines) {
        // Caso Especial: "Lote Vencimiento" pegados sin labels (ej: "25HAF0099 07/27")
        // Buscamos algo alfanumérico largo seguido de una fecha MM/AA
        val combinedPattern = Pattern.compile("([A-Z0-9-]{5,})\\s+(\\d{2}[/\\-]\\d{2})")
        val combinedMatcher = combinedPattern.matcher(line)
        if (combinedMatcher.find()) {
            val potentialLote = combinedMatcher.group(1)
            val potentialVenc = combinedMatcher.group(2)
            // Validamos que la fecha tenga mes 01-12
            if (isValidMonthDate(potentialVenc)) {
                lote = potentialLote
                venc = potentialVenc
                continue // Si lo hallamos así, es muy fiable, pasamos a la siguiente línea
            }
        }

        if (lote == null) {
            val loteMatcher = lotePattern.matcher(line)
            if (loteMatcher.find()) {
                lote = loteMatcher.group(1)
            }
        }
        
        if (venc == null) {
            val vencMatcher = vencPattern.matcher(line)
            if (vencMatcher.find()) {
                val foundDate = vencMatcher.group(1)
                if (isValidMonthDate(foundDate)) {
                    venc = foundDate
                }
            }
        }
    }
    
    // Heurística secundaria si no hay prefijos claros
    if (venc == null) {
        // Prioridad 1: Detectar MM/YY o YYYY-MM (con separadores claros)
        val strictDatePattern = Pattern.compile("(\\d{2}[/-]\\d{2,4}|\\d{4}-\\d{2})")
        val strictMatcher = strictDatePattern.matcher(text.uppercase())
        while (strictMatcher.find()) {
            val foundDate = strictMatcher.group(1)
            if (isValidMonthDate(foundDate)) {
                venc = foundDate
                break
            }
        }
        
        // Prioridad 2: Detectar MM YY (con espacio) solo si no se halló lo anterior
        if (venc == null) {
            val spaceDatePattern = Pattern.compile("(\\d{2}\\s\\d{2,4})")
            val spaceMatcher = spaceDatePattern.matcher(text.uppercase())
            while (spaceMatcher.find()) {
                val foundDate = spaceMatcher.group(1)
                if (isValidMonthDate(foundDate)) {
                    venc = foundDate
                    break
                }
            }
        }
    }

    return Pair(lote, venc)
}

/**
 * Valida si una cadena de fecha tiene un mes válido (01-12).
 */
private fun isValidMonthDate(date: String?): Boolean {
    if (date == null) return false
    val digits = date.filter { it.isDigit() }
    if (digits.length < 2) return false
    
    val monthPart = if (date.contains("/") || date.contains("-") || date.contains(" ")) {
        val firstPart = date.split(Regex("[/\\-\\s]"))[0]
        if (firstPart.length == 4) { // YYYY/MM
            date.split(Regex("[/\\-\\s]"))[1]
        } else { // MM/YY
            firstPart
        }
    } else {
        digits.take(2)
    }
    
    val month = monthPart.toIntOrNull() ?: return false
    return month in 1..12
}

/**
 * Normaliza la fecha detectada al formato MM/AA.
 */
private fun normalizeDetectedDate(raw: String?): String? {
    if (raw == null) return null
    if (!isValidMonthDate(raw)) return null // Protección extra
    
    val clean = raw.uppercase().replace("-", "/").replace(" ", "/")
    
    // Formato ENE 2027 o similar
    val months = mapOf(
        "ENE" to "01", "FEB" to "02", "MAR" to "03", "ABR" to "04", "MAY" to "05", "JUN" to "06",
        "JUL" to "07", "AGO" to "08", "SEP" to "09", "OCT" to "10", "NOV" to "11", "DIC" to "12"
    )
    
    for ((name, num) in months) {
        if (clean.contains(name)) {
            val year = clean.filter { it.isDigit() }.takeLast(2)
            if (year.length == 2) return "$num/$year"
        }
    }

    // Formato numérico
    val digits = clean.filter { it.isDigit() }
    return when {
        // MM/YYYY -> MM/YY
        clean.matches(Regex("\\d{2}/\\d{4}")) -> clean.take(2) + "/" + clean.takeLast(2)
        // YYYY/MM -> MM/YY
        clean.matches(Regex("\\d{4}/\\d{2}")) -> clean.takeLast(2) + "/" + clean.substring(2, 4)
        // MM/YY -> MM/YY
        clean.matches(Regex("\\d{2}/\\d{2}")) -> clean
        // Si tiene 4 dígitos pegados MMyy o yyMM, es ambiguo, pero probamos MMyy
        digits.length == 4 -> {
            val m = digits.take(2).toIntOrNull() ?: 0
            if (m in 1..12) digits.take(2) + "/" + digits.takeLast(2) else null
        }
        else -> null
    }
}
