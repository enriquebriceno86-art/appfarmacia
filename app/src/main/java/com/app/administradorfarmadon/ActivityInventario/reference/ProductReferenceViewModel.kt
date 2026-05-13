package com.app.administradorfarmadon.ActivityInventario.reference

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductReferenceViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ProductReferenceRepository.from(application)

    private val _referenceState = MutableStateFlow(ProductReferenceUiState())
    val referenceState: StateFlow<ProductReferenceUiState> = _referenceState.asStateFlow()

    private var referenceJob: Job? = null
    private var lastReferenceSearchName: String? = null

    fun startProductReferenceSearch(productName: String, category: String?) {
        val normalized = normalizeProductName(productName)
        if (normalized.isBlank()) return
        if (lastReferenceSearchName == normalized) return

        lastReferenceSearchName = normalized
        referenceJob?.cancel()
        referenceJob = viewModelScope.launch {
            _referenceState.value = ProductReferenceUiState(
                status = ProductReferenceStatus.LOADING
            )
            val result = repository.enrichProductReference(
                productName = productName,
                category = category
            )
            _referenceState.value = ProductReferenceUiState(
                status = result.status,
                reference = result,
                errorMessage = when (result.status) {
                    ProductReferenceStatus.ERROR -> "No se pudo buscar información confiable en español."
                    ProductReferenceStatus.NOT_FOUND -> "No se encontró referencia confiable en español."
                    else -> null
                }
            )
        }
    }

    fun retry(productName: String, category: String?) {
        lastReferenceSearchName = null
        startProductReferenceSearch(productName, category)
    }

    fun setEditedReference(reference: ProductReference) {
        _referenceState.value = ProductReferenceUiState(
            status = reference.status,
            reference = reference,
            errorMessage = null
        )
    }

    fun reset() {
        referenceJob?.cancel()
        referenceJob = null
        lastReferenceSearchName = null
        _referenceState.value = ProductReferenceUiState()
    }
}
