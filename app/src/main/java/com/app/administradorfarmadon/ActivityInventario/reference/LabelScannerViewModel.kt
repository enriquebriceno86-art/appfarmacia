package com.app.administradorfarmadon.ActivityInventario.reference

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import android.util.Base64

/**
 * ViewModel del escáner OCR de etiquetas (lote + vencimiento).
 *
 * Recibe un bitmap (de la cámara), lo comprime a JPEG en base64 y se lo
 * envía al `CategorySuggestionRepository.escanearEtiquetaConIA`. El estado
 * se expone como `StateFlow` para que la UI muestre loading/result/error.
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

    /** Procesa el bitmap capturado por la cámara. */
    fun procesar(bitmap: Bitmap) {
        trabajoActual?.cancel()
        trabajoActual = viewModelScope.launch {
            _state.value = UiState(status = Status.LOADING)
            val base64 = comprimirAJpegBase64(bitmap)
            val resultado = CategorySuggestionRepository.escanearEtiquetaConIA(base64)
            _state.value = if (resultado == null) {
                UiState(status = Status.ERROR)
            } else {
                UiState(status = Status.READY, resultado = resultado)
            }
        }
    }

    /** Limpia el estado tras consumir el resultado (al aplicar o cerrar). */
    fun reset() {
        trabajoActual?.cancel()
        _state.value = UiState()
    }

    /**
     * Reduce el bitmap a un ancho máximo razonable (1024px) y lo comprime a
     * JPEG 85%. Mantiene un tamaño chico para no inflar el request payload.
     */
    private fun comprimirAJpegBase64(bitmap: Bitmap): String {
        val maxAncho = 1024
        val escalado = if (bitmap.width > maxAncho) {
            val ratio = maxAncho.toFloat() / bitmap.width
            Bitmap.createScaledBitmap(
                bitmap,
                maxAncho,
                (bitmap.height * ratio).toInt(),
                true
            )
        } else bitmap

        val stream = ByteArrayOutputStream()
        escalado.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        val bytes = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
