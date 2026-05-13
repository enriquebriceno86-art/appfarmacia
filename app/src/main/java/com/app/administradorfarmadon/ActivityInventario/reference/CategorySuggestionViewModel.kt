package com.app.administradorfarmadon.ActivityInventario.reference

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.util.Locale

/**
 * Orquesta el ciclo de vida del picker de categoría con IA.
 *
 * Flujo:
 *  INITIAL  → (usuario escribe nombre, debounce 800ms) → LOADING
 *  LOADING  → respuesta válida           → READY
 *           → falló / sin internet / sin key → FALLBACK_MANUAL
 *  READY    → usuario acepta              → ACCEPTED
 *           → usuario toca "Escribir manual" → MANUAL
 *  MANUAL   → usuario toca "Volver a sugerencia" → READY (si hay) / INITIAL
 *
 * El estado MANUAL/FALLBACK_MANUAL es el único que activa el text field
 * en la UI. INITIAL, LOADING y READY muestran solo tarjetas.
 */
class CategorySuggestionViewModel : ViewModel() {

    private val _state = MutableStateFlow(CategorySuggestionUiState())
    val state: StateFlow<CategorySuggestionUiState> = _state.asStateFlow()

    private var trabajoActual: Job? = null
    private var ultimaConsultaKey: String? = null

    /**
     * Se llama cada vez que cambia el nombre del producto.
     * Si el usuario ya eligió manual, no se sobrescribe ese modo
     * (respeta la decisión de quien escribió manualmente).
     */
    fun onNombreCambio(
        nombre: String,
        categoriasExistentes: List<String>
    ) {
        // Si el usuario eligió MANUAL/FALLBACK, no se reactiva la IA
        // a menos que llame explícitamente a `volverASugerenciaIA()`.
        val estadoActual = _state.value.status
        if (estadoActual == CategorySuggestionStatus.MANUAL ||
            estadoActual == CategorySuggestionStatus.FALLBACK_MANUAL
        ) {
            _state.value = _state.value.copy(queryName = nombre)
            return
        }

        trabajoActual?.cancel()

        val key = normalizar(nombre)
        if (key.length < 4) {
            _state.value = CategorySuggestionUiState(
                status = CategorySuggestionStatus.INITIAL,
                queryName = nombre
            )
            ultimaConsultaKey = null
            return
        }

        if (key == ultimaConsultaKey &&
            _state.value.status == CategorySuggestionStatus.READY
        ) {
            // Mismo nombre que la sugerencia ya en pantalla; no reconsultar.
            return
        }

        trabajoActual = viewModelScope.launch {
            delay(800)
            _state.value = CategorySuggestionUiState(
                status = CategorySuggestionStatus.LOADING,
                queryName = nombre
            )
            ultimaConsultaKey = key
            val resultado = CategorySuggestionRepository.sugerirCategoria(
                productName = nombre,
                categoriasExistentes = categoriasExistentes
            )
            if (normalizar(_state.value.queryName) != key) return@launch
            _state.value = if (resultado == null) {
                CategorySuggestionUiState(
                    status = CategorySuggestionStatus.FALLBACK_MANUAL,
                    queryName = nombre
                )
            } else {
                CategorySuggestionUiState(
                    status = CategorySuggestionStatus.READY,
                    suggestion = resultado,
                    queryName = nombre
                )
            }
        }
    }

    /**
     * Marca la sugerencia actual como aceptada y persiste el evento
     * en Firebase como registro de decisión auditable.
     */
    fun aceptar() {
        val actual = _state.value
        val suggestion = actual.suggestion ?: return
        if (actual.status != CategorySuggestionStatus.READY) return
        CategorySuggestionRepository.registrarDecisionAceptada(suggestion)
        _state.value = actual.copy(status = CategorySuggestionStatus.ACCEPTED)
    }

    /** El usuario presiona "Escribir otra manualmente". */
    fun cambiarAManual() {
        _state.value = _state.value.copy(status = CategorySuggestionStatus.MANUAL)
    }

    /** El usuario presiona "Volver a sugerencia IA" desde el modo manual. */
    fun volverASugerenciaIA(
        nombre: String,
        categoriasExistentes: List<String>
    ) {
        // Forzar nueva consulta limpiando la última key.
        ultimaConsultaKey = null
        _state.value = CategorySuggestionUiState(
            status = CategorySuggestionStatus.INITIAL,
            queryName = nombre
        )
        onNombreCambio(nombre, categoriasExistentes)
    }

    fun reset() {
        trabajoActual?.cancel()
        ultimaConsultaKey = null
        _state.value = CategorySuggestionUiState()
    }

    private fun normalizar(name: String): String {
        return Normalizer
            .normalize(name, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase(Locale.ROOT)
            .trim()
    }
}
