package com.app.administradorfarmadon.ActivitysPerfilItem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.administradorfarmadon.ClasesDatabase.SessionManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class StoreConfigViewModel : ViewModel() {


    private val _states = MutableStateFlow<List<String>>(emptyList())
    val states = _states.asStateFlow()

    private val _cities = MutableStateFlow<List<String>>(emptyList())
    val cities = _cities.asStateFlow()

    private val _uiState = MutableStateFlow(StoreConfigUIState())
    val uiState: StateFlow<StoreConfigUIState> = _uiState.asStateFlow()

    // En StoreConfigViewModel.kt


    private val database = FirebaseDatabase.getInstance()
    private val configRef = database.getReference("ConfiguracionTienda").child("datosGenerales")

    init {
        loadConfig()
    }

    private fun loadConfig() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val snapshot = configRef.get().await()
                val config = snapshot.toStoreConfig()

                // Inicializar estados y ciudades según lo cargado
                if (config.pais.isNotBlank()) {
                    _states.value = CountryCatalog.getStates(config.pais)
                    if (config.estado.isNotBlank()) {
                        _cities.value = CountryCatalog.getCities(config.pais, config.estado)
                    }
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        config = config,
                        savedConfig = config,
                        errorMessage = null,
                        fieldErrors = emptyMap(),
                        dirtySections = emptySet(),
                        activeSection = null,
                        lastSavedSection = null,
                        saveSuccessMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun onConfigChanged(newConfig: StoreConfig) {
        applyConfigChange(newConfig)
    }

    private fun applyConfigChange(
        newConfig: StoreConfig,
        preferredSection: StoreConfigSection? = null
    ) {
        _uiState.update { current ->
            val editedSection = detectEditedSection(current.config, newConfig)
            val dirtySections = detectDirtySections(newConfig, current.savedConfig)
            current.copy(
                config = newConfig,
                errorMessage = null,
                lastSavedSection = null,
                saveSuccessMessage = null,
                fieldErrors = current.fieldErrors.filterKeys { key -> key !in changedFields(current.config, newConfig) },
                dirtySections = dirtySections,
                activeSection = preferredSection?.takeIf { it in dirtySections }
                    ?: editedSection?.takeIf { it in dirtySections }
                    ?: current.activeSection?.takeIf { it in dirtySections }
                    ?: dirtySections.firstOrNull()
            )
        }
    }

    fun onCountrySelected(countryName: String) {
        val countryInfo = CountryCatalog.getCountry(countryName)

        // Cargamos los estados/departamentos del catálogo inmediatamente
        _states.value = CountryCatalog.getStates(countryName)
        _cities.value = emptyList() // Limpiamos ciudades previas

        val current = _uiState.value
        val newConfig = if (countryInfo != null) {
            current.config.copy(
                pais = countryInfo.name,
                estado = "",
                ciudad = "",
                monedaCodigo = countryInfo.currencyCode,
                monedaSimbolo = countryInfo.currencySymbol,
                tipoDocumentoFiscal = countryInfo.fiscalDocLabel,
                nombreImpuesto = countryInfo.defaultTaxName,
                porcentajeImpuesto = if (current.config.cobraImpuestos) countryInfo.defaultTaxRate else current.config.porcentajeImpuesto
            )
        } else {
            current.config.copy(pais = countryName, estado = "", ciudad = "")
        }
        applyConfigChange(newConfig, preferredSection = StoreConfigSection.MERCADO)
    }


    fun onStateSelected(stateName: String) {
        val paisActual = _uiState.value.config.pais

        // Buscamos las ciudades en el catálogo usando el país y el estado
        _cities.value = CountryCatalog.getCities(paisActual, stateName)

        val newConfig = _uiState.value.config.copy(estado = stateName, ciudad = "")
        applyConfigChange(newConfig, preferredSection = StoreConfigSection.MERCADO)
    }

    fun onDiscreteConfigChanged(section: StoreConfigSection, newConfig: StoreConfig) {
        onConfigChanged(newConfig)
        saveSection(section)
    }

    fun saveDirtySections() {
        val dirtySections = _uiState.value.dirtySections.toList()
        if (dirtySections.isEmpty()) return
        saveSections(dirtySections)
    }

    fun saveSection(section: StoreConfigSection) {
        saveSections(listOf(section))
    }

    private fun saveSections(sections: List<StoreConfigSection>) {
        val distinctSections = sections.distinct()
        val config = _uiState.value.config
        val validationErrors = buildMap {
            distinctSections.forEach { section ->
                putAll(StoreConfigRules.validateSection(config, section))
            }
        }
        if (validationErrors.isNotEmpty()) {
            val targetSection = distinctSections.firstOrNull { section ->
                validationErrors.keys.any { it in sectionFieldKeys(section) }
            } ?: distinctSections.first()
            _uiState.update {
                it.copy(
                    errorMessage = buildValidationMessage(distinctSections),
                    fieldErrors = it.fieldErrors + validationErrors,
                    activeSection = targetSection,
                    saveSuccess = false,
                    saveSuccessMessage = null,
                    lastSavedSection = null
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    savingSection = distinctSections.firstOrNull(),
                    errorMessage = null,
                    saveSuccess = false,
                    saveSuccessMessage = null,
                    lastSavedSection = null,
                    fieldErrors = it.fieldErrors - distinctSections.flatMap(::sectionFieldKeys).toSet(),
                    activeSection = distinctSections.firstOrNull()
                )
            }
            try {
                distinctSections.forEach { section ->
                    configRef.updateChildren(config.toFirebaseMap(section)).await()
                }
                if (StoreConfigSection.MERCADO in distinctSections) {
                    syncSession(config)
                }
                _uiState.update { current ->
                    val newSavedConfig = distinctSections.fold(current.savedConfig) { saved, section ->
                        mergeSection(saved, current.config, section)
                    }
                    val newDirtySections = detectDirtySections(current.config, newSavedConfig)
                    current.copy(
                        isSaving = false,
                        savingSection = null,
                        saveSuccess = true,
                        saveSuccessMessage = buildSuccessMessage(distinctSections),
                        lastSavedSection = distinctSections.firstOrNull(),
                        savedConfig = newSavedConfig,
                        dirtySections = newDirtySections,
                        activeSection = current.activeSection?.takeIf { it in newDirtySections }
                            ?: newDirtySections.firstOrNull()
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        savingSection = null,
                        errorMessage = e.message ?: "No se pudieron guardar los cambios",
                        activeSection = distinctSections.firstOrNull()
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(saveSuccess = false, lastSavedSection = null, saveSuccessMessage = null) }
    }

    private fun buildSuccessMessage(sections: List<StoreConfigSection>): String {
        return if (sections.size == 1) {
            "${sections.first().displayName} guardado con exito"
        } else {
            "Se guardaron ${sections.size} secciones con exito"
        }
    }

    private fun buildValidationMessage(sections: List<StoreConfigSection>): String {
        return if (sections.size == 1) {
            "Revisa los campos de ${sections.first().displayName.lowercase()} antes de guardar"
        } else {
            "Revisa los campos pendientes antes de guardar todos los cambios"
        }
    }

    private fun syncSession(config: StoreConfig) {
        SessionManager.monedaCodigo = config.monedaCodigo
        SessionManager.monedaSimbolo = config.monedaSimbolo
        SessionManager.paisOperacion = config.pais
    }

    private fun DataSnapshot.toStoreConfig(): StoreConfig {
        // 1. Obtenemos el país guardado, si no hay nada queda vacío para obligar a elegir
        val countryName = child("pais").getValue(String::class.java).orEmpty().trim()
        val countryInfo = CountryCatalog.getCountry(countryName)

        // 2. Recuperamos el Estado/Departamento que guardamos en Firebase
        val estadoGuardado = child("estado").getValue(String::class.java).orEmpty().trim()

        val nombreComercial = child("nombreComercial").getValue(String::class.java)
            .orEmpty()
            .trim()
            .ifBlank { child("nombreTienda").getValue(String::class.java).orEmpty().trim() }

        val documentoFiscal = child("nroDocumentoFiscal").getValue(String::class.java)
            .orEmpty()
            .trim()
            .ifBlank { child("identificacionFiscal").getValue(String::class.java).orEmpty().trim() }

        return StoreConfig(
            nombreComercial = nombreComercial,
            razonSocial = child("razonSocial").getValue(String::class.java).orEmpty().trim(),
            tipoNegocio = child("tipoNegocio").getValue(String::class.java).orEmpty().trim().ifBlank {
                "FARMACIA"
            },
            tipoDocumentoFiscal = child("tipoDocumentoFiscal").getValue(String::class.java)
                .orEmpty()
                .trim()
                .ifBlank { countryInfo?.fiscalDocLabel ?: "" },
            nroDocumentoFiscal = documentoFiscal,
            pais = countryName,

            // --- AQUÍ CONECTAMOS EL ESTADO ---
            estado = estadoGuardado,

            ciudad = child("ciudad").getValue(String::class.java).orEmpty().trim(),
            direccion = child("direccion").getValue(String::class.java).orEmpty().trim(),
            monedaCodigo = child("monedaCodigo").getValue(String::class.java).orEmpty().trim().ifBlank {
                countryInfo?.currencyCode ?: ""
            },
            monedaSimbolo = child("monedaSimbolo").getValue(String::class.java).orEmpty().trim().ifBlank {
                countryInfo?.currencySymbol ?: ""
            },
            cobraImpuestos = child("cobraImpuestos").getValue(Boolean::class.java)
                ?: child("cobrarImpuestos").getValue(Boolean::class.java)
                ?: false,
            nombreImpuesto = child("nombreImpuesto").getValue(String::class.java).orEmpty().trim().ifBlank {
                countryInfo?.defaultTaxName ?: ""
            },
            porcentajeImpuesto = child("porcentajeImpuesto").getValue(Double::class.java) ?: 0.0,
            usaReferenciaComercialIA = child("usaReferenciaComercialIA").getValue(Boolean::class.java) ?: false,
            usarMargenMinimo = child("usarMargenMinimo").getValue(Boolean::class.java) ?: false,
            margenMinimoDefault = child("margenMinimoDefault").getValue(Double::class.java) ?: 0.0,
            controlarLotes = child("controlarLotes").getValue(Boolean::class.java) ?: false,
            controlarVencimientos = child("controlarVencimientos").getValue(Boolean::class.java) ?: false,
            mensajeTicket = child("mensajeTicket").getValue(String::class.java).orEmpty().trim()
        )
    }

    private fun StoreConfig.toFirebaseMap(section: StoreConfigSection): Map<String, Any> {
        val fullMap = linkedMapOf<String, Any>(
            "nombreComercial" to nombreComercial.trim(),
            "nombreTienda" to nombreComercial.trim(),
            "razonSocial" to razonSocial.trim(),
            "tipoNegocio" to tipoNegocio.trim(),
            "tipoDocumentoFiscal" to tipoDocumentoFiscal.trim(),
            "nroDocumentoFiscal" to nroDocumentoFiscal.trim(),
            "identificacionFiscal" to nroDocumentoFiscal.trim(),
            "direccion" to direccion.trim(),

            // --- AQUÍ AGREGAMOS EL ESTADO ---
            "estado" to estado.trim(), //

            "ciudad" to ciudad.trim(),
            "pais" to pais.trim(),
            "monedaCodigo" to monedaCodigo.trim(),
            "monedaSimbolo" to monedaSimbolo.trim(),
            "cobrarImpuestos" to cobraImpuestos,
            "cobraImpuestos" to cobraImpuestos,
            "nombreImpuesto" to nombreImpuesto.trim(),
            "porcentajeImpuesto" to porcentajeImpuesto,
            "mensajeTicket" to mensajeTicket.trim(),
            "usaReferenciaComercialIA" to usaReferenciaComercialIA,
            "usarMargenMinimo" to usarMargenMinimo,
            "margenMinimoDefault" to margenMinimoDefault,
            "controlarLotes" to controlarLotes,
            "controlarVencimientos" to controlarVencimientos
        )
        return fullMap.filterKeys { it in firebaseKeysForSection(section) }
    }

    private fun detectDirtySections(current: StoreConfig, saved: StoreConfig): Set<StoreConfigSection> {
        return buildSet {
            StoreConfigSection.entries.forEach { section ->
                if (isSectionDirty(section, current, saved)) add(section)
            }
        }
    }

    private fun isSectionDirty(section: StoreConfigSection, current: StoreConfig, saved: StoreConfig): Boolean {
        return when (section) {
            StoreConfigSection.NEGOCIO ->
                current.nombreComercial != saved.nombreComercial ||
                    current.razonSocial != saved.razonSocial ||
                    current.tipoNegocio != saved.tipoNegocio

            StoreConfigSection.MERCADO ->
                current.pais != saved.pais ||
                    current.estado != saved.estado ||
                    current.ciudad != saved.ciudad ||
                    current.direccion != saved.direccion ||
                    current.monedaCodigo != saved.monedaCodigo ||
                    current.monedaSimbolo != saved.monedaSimbolo

            StoreConfigSection.FISCAL ->
                current.tipoDocumentoFiscal != saved.tipoDocumentoFiscal ||
                    current.nroDocumentoFiscal != saved.nroDocumentoFiscal ||
                    current.cobraImpuestos != saved.cobraImpuestos ||
                    current.nombreImpuesto != saved.nombreImpuesto ||
                    current.porcentajeImpuesto != saved.porcentajeImpuesto

            StoreConfigSection.OPERACION ->
                current.usaReferenciaComercialIA != saved.usaReferenciaComercialIA ||
                    current.usarMargenMinimo != saved.usarMargenMinimo ||
                    current.margenMinimoDefault != saved.margenMinimoDefault ||
                    current.controlarLotes != saved.controlarLotes ||
                    current.controlarVencimientos != saved.controlarVencimientos ||
                    current.mensajeTicket != saved.mensajeTicket
        }
    }

    private fun detectEditedSection(oldConfig: StoreConfig, newConfig: StoreConfig): StoreConfigSection? {
        return when {
            oldConfig.nombreComercial != newConfig.nombreComercial ||
                oldConfig.razonSocial != newConfig.razonSocial ||
                oldConfig.tipoNegocio != newConfig.tipoNegocio -> StoreConfigSection.NEGOCIO

            oldConfig.pais != newConfig.pais ||
                oldConfig.estado != newConfig.estado ||
                oldConfig.ciudad != newConfig.ciudad ||
                oldConfig.direccion != newConfig.direccion ||
                oldConfig.monedaCodigo != newConfig.monedaCodigo ||
                oldConfig.monedaSimbolo != newConfig.monedaSimbolo -> StoreConfigSection.MERCADO

            oldConfig.tipoDocumentoFiscal != newConfig.tipoDocumentoFiscal ||
                oldConfig.nroDocumentoFiscal != newConfig.nroDocumentoFiscal ||
                oldConfig.cobraImpuestos != newConfig.cobraImpuestos ||
                oldConfig.nombreImpuesto != newConfig.nombreImpuesto ||
                oldConfig.porcentajeImpuesto != newConfig.porcentajeImpuesto -> StoreConfigSection.FISCAL

            oldConfig.usaReferenciaComercialIA != newConfig.usaReferenciaComercialIA ||
                oldConfig.usarMargenMinimo != newConfig.usarMargenMinimo ||
                oldConfig.margenMinimoDefault != newConfig.margenMinimoDefault ||
                oldConfig.controlarLotes != newConfig.controlarLotes ||
                oldConfig.controlarVencimientos != newConfig.controlarVencimientos ||
                oldConfig.mensajeTicket != newConfig.mensajeTicket -> StoreConfigSection.OPERACION

            else -> null
        }
    }

    private fun mergeSection(savedConfig: StoreConfig, currentConfig: StoreConfig, section: StoreConfigSection): StoreConfig {
        return when (section) {
            StoreConfigSection.NEGOCIO -> savedConfig.copy(
                nombreComercial = currentConfig.nombreComercial,
                razonSocial = currentConfig.razonSocial,
                tipoNegocio = currentConfig.tipoNegocio
            )

            StoreConfigSection.MERCADO -> savedConfig.copy(
                pais = currentConfig.pais,
                estado = currentConfig.estado,
                ciudad = currentConfig.ciudad,
                direccion = currentConfig.direccion,
                monedaCodigo = currentConfig.monedaCodigo,
                monedaSimbolo = currentConfig.monedaSimbolo
            )

            StoreConfigSection.FISCAL -> savedConfig.copy(
                tipoDocumentoFiscal = currentConfig.tipoDocumentoFiscal,
                nroDocumentoFiscal = currentConfig.nroDocumentoFiscal,
                cobraImpuestos = currentConfig.cobraImpuestos,
                nombreImpuesto = currentConfig.nombreImpuesto,
                porcentajeImpuesto = currentConfig.porcentajeImpuesto
            )

            StoreConfigSection.OPERACION -> savedConfig.copy(
                usaReferenciaComercialIA = currentConfig.usaReferenciaComercialIA,
                usarMargenMinimo = currentConfig.usarMargenMinimo,
                margenMinimoDefault = currentConfig.margenMinimoDefault,
                controlarLotes = currentConfig.controlarLotes,
                controlarVencimientos = currentConfig.controlarVencimientos,
                mensajeTicket = currentConfig.mensajeTicket
            )
        }
    }

    private fun changedFields(oldConfig: StoreConfig, newConfig: StoreConfig): Set<String> {
        val changed = mutableSetOf<String>()
        if (oldConfig.nombreComercial != newConfig.nombreComercial) changed += "nombreComercial"
        if (oldConfig.razonSocial != newConfig.razonSocial) changed += "razonSocial"
        if (oldConfig.tipoNegocio != newConfig.tipoNegocio) changed += "tipoNegocio"
        if (oldConfig.tipoDocumentoFiscal != newConfig.tipoDocumentoFiscal) changed += "tipoDocumentoFiscal"
        if (oldConfig.nroDocumentoFiscal != newConfig.nroDocumentoFiscal) changed += "nroDocumentoFiscal"
        if (oldConfig.pais != newConfig.pais) changed += "pais"
        if (oldConfig.estado != newConfig.estado) changed += "estado"
        if (oldConfig.ciudad != newConfig.ciudad) changed += "ciudad"
        if (oldConfig.direccion != newConfig.direccion) changed += "direccion"
        if (oldConfig.monedaCodigo != newConfig.monedaCodigo) changed += "monedaCodigo"
        if (oldConfig.monedaSimbolo != newConfig.monedaSimbolo) changed += "monedaSimbolo"
        if (oldConfig.cobraImpuestos != newConfig.cobraImpuestos) changed += "cobraImpuestos"
        if (oldConfig.nombreImpuesto != newConfig.nombreImpuesto) changed += "nombreImpuesto"
        if (oldConfig.porcentajeImpuesto != newConfig.porcentajeImpuesto) changed += "porcentajeImpuesto"
        if (oldConfig.usaReferenciaComercialIA != newConfig.usaReferenciaComercialIA) changed += "usaReferenciaComercialIA"
        if (oldConfig.usarMargenMinimo != newConfig.usarMargenMinimo) changed += "usarMargenMinimo"
        if (oldConfig.margenMinimoDefault != newConfig.margenMinimoDefault) changed += "margenMinimoDefault"
        if (oldConfig.controlarLotes != newConfig.controlarLotes) changed += "controlarLotes"
        if (oldConfig.controlarVencimientos != newConfig.controlarVencimientos) changed += "controlarVencimientos"
        if (oldConfig.mensajeTicket != newConfig.mensajeTicket) changed += "mensajeTicket"
        return changed
    }

    private fun sectionFieldKeys(section: StoreConfigSection): Set<String> {
        return when (section) {
            StoreConfigSection.NEGOCIO -> setOf("nombreComercial", "razonSocial")
            StoreConfigSection.MERCADO -> setOf("pais", "estado", "ciudad", "direccion", "monedaCodigo", "monedaSimbolo")
            StoreConfigSection.FISCAL -> setOf("tipoDocumentoFiscal", "nroDocumentoFiscal", "cobraImpuestos", "nombreImpuesto", "porcentajeImpuesto")
            StoreConfigSection.OPERACION -> setOf("usaReferenciaComercialIA", "usarMargenMinimo", "margenMinimoDefault", "controlarLotes", "controlarVencimientos", "mensajeTicket")
        }
    }

    private fun firebaseKeysForSection(section: StoreConfigSection): Set<String> {
        return when (section) {
            StoreConfigSection.NEGOCIO -> setOf("nombreComercial", "nombreTienda", "razonSocial", "tipoNegocio")

            // AGREGA "estado" AQUÍ:
            StoreConfigSection.MERCADO -> setOf("direccion", "estado", "ciudad", "pais", "monedaCodigo", "monedaSimbolo")

            StoreConfigSection.FISCAL -> setOf("tipoDocumentoFiscal", "nroDocumentoFiscal", "identificacionFiscal", "cobrarImpuestos", "cobraImpuestos", "nombreImpuesto", "porcentajeImpuesto")
            StoreConfigSection.OPERACION -> setOf("mensajeTicket", "usaReferenciaComercialIA", "usarMargenMinimo", "margenMinimoDefault", "controlarLotes", "controlarVencimientos")
        }
    }


}
