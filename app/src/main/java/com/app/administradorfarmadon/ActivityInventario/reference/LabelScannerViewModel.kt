package com.app.administradorfarmadon.ActivityInventario.reference

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.app.administradorfarmadon.ActivityInventario.ProductUtils
import kotlinx.coroutines.tasks.await

/**
 * V30.0: ViewModel del escáner OCR de etiquetas (lote + vencimiento).
 * Recibe un bitmap, lo procesa localmente con ML Kit y extrae los datos.
 * Elimina la dependencia de Gemini para el escaneo de etiquetas.
 */
class LabelScannerViewModel : ViewModel() {

    enum class Status { IDLE, LOADING, READY, ERROR }

    data class UiState(
        val status: Status = Status.IDLE,
        val resultado: EtiquetaDetectada? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var trabajoActual: Job? = null
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /** Procesa el bitmap capturado por la cámara usando ML Kit local. */
    fun procesar(bitmap: Bitmap) {
        trabajoActual?.cancel()
        trabajoActual = viewModelScope.launch {
            _state.value = UiState(status = Status.LOADING)
            
            try {
                // Procesamiento local con ML Kit
                val image = InputImage.fromBitmap(bitmap, 0)
                val visionText = recognizer.process(image).await()
                
                // Extracción estructurada (Lógica compartida en ProductUtils)
                val smartResult = ProductUtils.extractLoteAndVencSmart(visionText.text)

                val lote = smartResult.lote
                val normalizedVenc = smartResult.vencimiento

                Log.d(
                    "LiveOcrScanner",
                    "OCR smart lote=${smartResult.lote}, venc=${smartResult.vencimiento}, confLote=${smartResult.confianzaLote}, confVenc=${smartResult.confianzaVencimiento}, raw=${smartResult.textoOcr}"
                )
                
                if (lote == null && normalizedVenc == null) {
                    _state.value = UiState(status = Status.ERROR)
                } else {
                    _state.value = UiState(
                        status = Status.READY,
                        resultado = EtiquetaDetectada(
                            loteNumero = lote,
                            vencimientoMmAa = normalizedVenc
                        )
                    )
                }
            } catch (e: Exception) {
                _state.value = UiState(status = Status.ERROR)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        recognizer.close()
    }

    /** Limpia el estado tras consumir el resultado (al aplicar o cerrar). */
    fun reset() {
        trabajoActual?.cancel()
        _state.value = UiState()
    }
}
