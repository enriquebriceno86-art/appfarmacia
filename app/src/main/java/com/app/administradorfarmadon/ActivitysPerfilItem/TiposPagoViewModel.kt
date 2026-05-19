package com.app.administradorfarmadon.ActivitysPerfilItem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.administradorfarmadon.ClasesDatabase.SessionManager
import com.google.firebase.database.FirebaseDatabase
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class PaymentFormState(
    val id: String? = null,
    val title: String = "",
    val category: String = "efectivo",
    val activo: Boolean = true,
    val permiteVuelto: Boolean = false,
    val solicitaMontoRecibido: Boolean = false,
    val calculaVuelto: Boolean = false,
    val permiteReferencia: Boolean = false,
    val usaQr: Boolean = false,
    val disponibleMixto: Boolean = true,
    val banco: String = "",
    val tipoCuenta: String = "",
    val numeroCuenta: String = "",
    val titularBanco: String = "",
    val documentoBanco: String = "",
    val telefonoBilletera: String = "",
    val titularBilletera: String = "",
    val aliasBilletera: String = "",
    val qrUrl: String = "",
    val instrucciones: String = "",
    val descripcion: String = "",
    val orden: String = "",
    val fieldErrors: Map<String, String> = emptyMap(),
    val warnings: List<String> = emptyList()
)

data class TiposPagoUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isAiReviewLoading: Boolean = false,
    val suggestions: List<PaymentMethodSuggestion> = emptyList(),
    val methods: List<MetodoPagoConfig> = emptyList(),
    val aiInsights: List<PaymentAiInsight> = emptyList(),
    val showEditor: Boolean = false,
    val form: PaymentFormState = PaymentFormState(),
    val country: String = "",
    val errorMessage: String? = null
)

class TiposPagoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TiposPagoUiState())
    val uiState: StateFlow<TiposPagoUiState> = _uiState.asStateFlow()

    private val metodosRef = FirebaseDatabase.getInstance().getReference("ConfiguracionTienda").child("metodosPago")

    init {
        loadMethods()
    }

    fun loadMethods() {
        viewModelScope.launch {
            val country = SessionManager.paisOperacion
            _uiState.update { it.copy(isLoading = true, country = country, errorMessage = null) }
            try {
                val snapshot = metodosRef.get().await()
                val methods = snapshot.children.mapNotNull { it.getValue(MetodoPagoConfig::class.java) }
                    .sortedWith(compareBy<MetodoPagoConfig>({ it.orden }, { it.titulo.lowercase() }))
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        methods = methods,
                        suggestions = buildSuggestions(country, methods)
                    )
                }
                runAiReview()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun showCreateEditor(suggestion: PaymentMethodSuggestion? = null) {
        val nextOrder = ((_uiState.value.methods.maxOfOrNull { it.orden } ?: 0) + 1).toString()
        val form = suggestion?.toForm(nextOrder) ?: PaymentFormState(orden = nextOrder)
        _uiState.update { it.copy(showEditor = true, form = form, errorMessage = null) }
    }

    fun showEditEditor(method: MetodoPagoConfig) {
        _uiState.update { it.copy(showEditor = true, form = method.toForm(), errorMessage = null) }
    }

    fun dismissEditor() {
        _uiState.update { it.copy(showEditor = false, form = PaymentFormState(), errorMessage = null) }
    }

    fun updateForm(transform: (PaymentFormState) -> PaymentFormState) {
        _uiState.update { it.copy(form = transform(it.form), errorMessage = null) }
    }

    fun saveMethod() {
        val form = _uiState.value.form
        
        // Ejecutar validación guiada
        val validation = PaymentMethodValidationRules.validate(form)
        if (!validation.isValid) {
            _uiState.update { it.copy(
                form = it.form.copy(
                    fieldErrors = validation.fieldErrors,
                    warnings = validation.globalWarnings
                ),
                errorMessage = "Revisa los campos marcados en rojo antes de continuar."
            ) }
            return
        }

        val method = form.toMethod(form.id ?: "")

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val result = PaymentMethodsRepository.saveMethod(
                method = method,
                localQrUri = if (form.qrUrl.startsWith("content://")) Uri.parse(form.qrUrl) else null,
                existingMethods = _uiState.value.methods
            )

            result.onSuccess {
                _uiState.update { it.copy(showEditor = false, form = PaymentFormState(), isSaving = false) }
                loadMethods()
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
            }
        }
    }

    fun deleteMethod(method: MetodoPagoConfig) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val result = PaymentMethodsRepository.deleteMethod(method.id, _uiState.value.methods)
            
            result.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
                loadMethods()
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
            }
        }
    }

    fun seedMissingSuggestions() {
        val state = _uiState.value
        val missing = buildSuggestions(state.country, state.methods)
        if (missing.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val maxOrder = state.methods.maxOfOrNull { it.orden } ?: 0
                
                missing.forEachIndexed { index, suggestion ->
                    val method = suggestion.toForm((maxOrder + index + 1).toString()).toMethod("")
                    PaymentMethodsRepository.saveMethod(
                        method = method,
                        existingMethods = state.methods
                    ).getOrThrow()
                }

                _uiState.update { it.copy(isSaving = false) }
                loadMethods()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun refreshAiReview() {
        viewModelScope.launch {
            runAiReview()
        }
    }

    fun handleAiInsight(insight: PaymentAiInsight) {
        when (insight.actionType) {
            "ADD_SUGGESTED" -> {
                val suggestion = _uiState.value.suggestions.firstOrNull {
                    it.title.equals(insight.targetMethodTitle.orEmpty(), ignoreCase = true)
                } ?: return
                showCreateEditor(suggestion)
            }

            "EDIT_EXISTING" -> {
                val method = _uiState.value.methods.firstOrNull {
                    it.titulo.equals(insight.targetMethodTitle.orEmpty(), ignoreCase = true)
                } ?: return
                showEditEditor(method)
            }
        }
    }

    fun saveQrImageForMethod(methodId: String, qrUri: String) {
        if (methodId.isBlank() || qrUri.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val method = _uiState.value.methods.firstOrNull { it.id == methodId } ?: return@launch
                val result = PaymentMethodsRepository.saveMethod(
                    method = method,
                    localQrUri = Uri.parse(qrUri),
                    existingMethods = _uiState.value.methods
                )
                
                result.onSuccess {
                    _uiState.update { it.copy(isSaving = false) }
                    loadMethods()
                }.onFailure { e ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
            }
        }
    }

    private fun buildSuggestions(country: String, methods: List<MetodoPagoConfig>): List<PaymentMethodSuggestion> {
        val existingKeys = methods.map { "${it.titulo.trim().lowercase()}|${it.categoria.trim().lowercase()}" }.toSet()
        return PaymentMethodCatalog.suggestionsForCountry(country).filterNot { suggestion ->
            "${suggestion.title.trim().lowercase()}|${suggestion.category.trim().lowercase()}" in existingKeys
        }
    }

    private suspend fun runAiReview() {
        val snapshot = _uiState.value
        if (snapshot.methods.isEmpty() && snapshot.suggestions.isEmpty()) {
            _uiState.update { it.copy(aiInsights = emptyList(), isAiReviewLoading = false) }
            return
        }

        _uiState.update { it.copy(isAiReviewLoading = true) }
        val insights = runCatching {
            PaymentAiRepository.reviewPaymentSetup(
                country = snapshot.country,
                methods = snapshot.methods,
                suggestions = snapshot.suggestions
            )
        }.getOrElse { emptyList() }

        _uiState.update {
            it.copy(
                aiInsights = insights,
                isAiReviewLoading = false
            )
        }
    }

    private fun PaymentMethodSuggestion.toForm(order: String): PaymentFormState {
        return PaymentFormState(
            title = title,
            category = category,
            descripcion = description,
            usaQr = usaQr,
            permiteReferencia = permiteReferencia,
            permiteVuelto = permiteVuelto,
            solicitaMontoRecibido = solicitaMontoRecibido,
            calculaVuelto = calculaVuelto,
            disponibleMixto = disponibleMixto,
            orden = order
        )
    }

    private fun MetodoPagoConfig.toForm(): PaymentFormState {
        return PaymentFormState(
            id = id,
            title = titulo,
            category = categoria,
            activo = activo,
            permiteVuelto = permiteVuelto,
            solicitaMontoRecibido = solicitaMontoRecibido,
            calculaVuelto = calculaVuelto,
            permiteReferencia = permiteReferencia,
            usaQr = usaQR,
            disponibleMixto = disponibleMixto,
            banco = banco,
            tipoCuenta = tipoCuenta,
            numeroCuenta = numeroCuenta,
            titularBanco = titularBanco,
            documentoBanco = documentoBanco,
            telefonoBilletera = telefonoBilletera,
            titularBilletera = titularBilletera,
            aliasBilletera = aliasBilletera,
            qrUrl = qrUrl,
            instrucciones = instrucciones,
            descripcion = descripcion,
            orden = orden.toString()
        )
    }

    private fun PaymentFormState.toMethod(id: String): MetodoPagoConfig {
        return MetodoPagoConfig(
            id = id,
            titulo = title.trim(),
            categoria = category.trim(),
            activo = activo,
            permiteVuelto = permiteVuelto,
            solicitaMontoRecibido = solicitaMontoRecibido,
            calculaVuelto = calculaVuelto,
            permiteReferencia = permiteReferencia,
            usaQR = usaQr,
            disponibleMixto = disponibleMixto,
            orden = orden.toIntOrNull() ?: 0,
            banco = banco.trim(),
            tipoCuenta = tipoCuenta.trim(),
            numeroCuenta = numeroCuenta.trim(),
            titularBanco = titularBanco.trim(),
            documentoBanco = documentoBanco.trim(),
            telefonoBilletera = telefonoBilletera.trim(),
            titularBilletera = titularBilletera.trim(),
            aliasBilletera = aliasBilletera.trim(),
            qrUrl = qrUrl.trim(),
            instrucciones = instrucciones.trim(),
            descripcion = descripcion.trim(),
            placeholder = title.trim()
        )
    }

}
