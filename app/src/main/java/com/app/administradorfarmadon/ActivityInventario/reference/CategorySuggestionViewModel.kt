package com.app.administradorfarmadon.ActivityInventario.reference

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.administradorfarmadon.BuildConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers
import java.text.Normalizer
import java.util.Locale

/**
 * Orquesta el ciclo de vida del picker de categoría con IA.
 *
 * Flujo:
 *  INITIAL  → (usuario escribe nombre, debounce 1500ms) → LOADING
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
    private var jobCorreccion: Job? = null
    private var jobAsistencia: Job? = null
    private var jobTipo: Job? = null
    private var ultimaConsultaKey: String? = null
    private var latestTypeSuggestionKey: String? = null
    private var latestCorrectionRequestId = 0L
    private var latestSearchRequestId = 0L
    private var latestBarcodeRequestId = 0L
    private var latestBarcodeKey: String? = null
    private var jobBarcode: Job? = null
    private var jobUsageInfo: Job? = null
    private var latestUsageInfoKey: String? = null

    // V17.7: Caché de sesión para sugerencias silenciosas (Nombre -> Categoría)
    private val cacheSugerenciasSilenciosas = mutableMapOf<String, String>()

    // V10.0: Evitar sugerencias que el usuario ya rechazó explícitamente
    private val correccionesRechazadas = mutableSetOf<String>()

    /**
     * Actualiza la interpretación local inmediatamente para dar feedback al usuario (chips).
     * No dispara ninguna petición de red.
     */
    fun actualizarInterpretacionLocal(nombre: String) {
        val interpretation = PharmaceuticalParser.parse(nombre)
        val normalizedCurrent = normalizar(nombre)
        val normalizedState = normalizar(_state.value.queryName)

        // P1 FIX: Si el nombre se limpia, reseteamos a INITIAL
        if (nombre.isBlank()) {
            jobCorreccion?.cancel()
            trabajoActual?.cancel()
            jobTipo?.cancel()
            latestCorrectionRequestId++
            latestSearchRequestId++
            _state.value = _state.value.copy(
                status = CategorySuggestionStatus.INITIAL,
                queryName = nombre,
                interpreted = interpretation,
                pendingCorrection = null,
                sugerenciaTipoManual = null,
                asistenciaManualCategorias = emptyList(),
                estaCargandoAsistencia = false
            )
            ultimaConsultaKey = null
            latestTypeSuggestionKey = null
            return
        }

        // P2 FIX: Si el nombre cambia, invalidamos sugerencias previas pero mantenemos queryName actualizado
        // V17.15: NO reseteamos sugerenciaTipoManual si el nombre no ha cambiado realmente.
        if (normalizedCurrent != normalizedState) {
            _state.value = _state.value.copy(
                status = CategorySuggestionStatus.INITIAL,
                queryName = nombre,
                interpreted = interpretation,
                pendingCorrection = null,
                sugerenciaTipoManual = null,
                asistenciaManualCategorias = emptyList() // V17.11: Limpieza inmediata para evitar sugerencias obsoletas
            )
            latestTypeSuggestionKey = null
        } else {
            // Actualización normal de texto e interpretación
            _state.value = _state.value.copy(
                queryName = nombre,
                interpreted = interpretation
            )
        }
    }

    /**
     * V12.0: Validador de seguridad centralizado para correcciones de IA.
     * V14.3: Simplificado al máximo para eliminar heurísticas locales.
     */
    object SafetyValidator {
        fun validate(original: String, candidate: String, mode: SafetyMode): Boolean {
            val origClean = original.lowercase().trim()
            val candClean = candidate.lowercase().trim()
            if (origClean == candClean) return true

            // Solo validamos que los números importantes (dosis/cantidad) no cambien drásticamente
            val origNums = extractNumberPairs(origClean)
            val candNums = extractNumberPairs(candClean)

            if (mode == SafetyMode.STRICT) {
                if (origNums.size != candNums.size) return false
                for (i in origNums.indices) {
                    if (origNums[i].first != candNums[i].first &&
                        !isSafeRetailDecimalShorthand(origClean, candClean, origNums[i].first, candNums[i].first)
                    ) {
                        return false
                    }
                }
            }

            if (mode == SafetyMode.RELAXED && hasUnsafeBareNumberExpansion(origClean, candClean, origNums, candNums)) {
                return false
            }

            if (!preservesSignificantToken(origClean, candClean)) return false

            return true
        }

        private fun extractNumberPairs(text: String): List<Pair<Double, String?>> {
            val regex = Regex("(\\d+(?:[.,]\\d+)?)\\s*([a-zA-Z%]+)?")
            return regex.findAll(text).map { match ->
                val value = match.groupValues[1].replace(",", ".").toDoubleOrNull() ?: 0.0
                val unitRaw = match.groupValues.getOrNull(2)
                val unit = if (unitRaw.isNullOrBlank()) null else unitRaw
                value to unit
            }.toList()
        }

        private fun preservesSignificantToken(original: String, candidate: String): Boolean {
            val originalTokens = significantTokens(original)
            if (originalTokens.isEmpty()) return true
            val candidateTokens = candidate.split(Regex("\\s+")).filter { it.isNotBlank() }

            return originalTokens.any { originalToken ->
                candidateTokens.any { candidateToken ->
                    candidateToken == originalToken ||
                        (originalToken.length >= 3 && candidateToken.startsWith(originalToken)) ||
                        (candidateToken.length >= 3 && originalToken.startsWith(candidateToken))
                }
            }
        }

        private fun isSafeRetailDecimalShorthand(
            original: String,
            candidate: String,
            originalNumber: Double,
            candidateNumber: Double
        ): Boolean {
            if (!hasLiquidRetailContext(original, candidate)) return false
            if (hasPharmaContext(original, candidate)) return false
            if (!hasLiterUnit(candidate)) return false
            if (originalNumber % 1.0 != 0.0) return false
            if (originalNumber !in 10.0..99.0) return false
            if (candidateNumber < 1.0 || candidateNumber >= 10.0) return false

            val originalDigits = originalNumber.toInt().toString()
            val candidateDigits = candidateNumber
                .toString()
                .trimEnd('0')
                .trimEnd('.')
                .replace(".", "")

            return originalDigits == candidateDigits
        }

        private fun hasUnsafeBareNumberExpansion(
            original: String,
            candidate: String,
            originalNumbers: List<Pair<Double, String?>>,
            candidateNumbers: List<Pair<Double, String?>>
        ): Boolean {
            if (originalNumbers.size != candidateNumbers.size) return false

            return originalNumbers.indices.any { index ->
                val originalNumber = originalNumbers[index]
                val candidateNumber = candidateNumbers[index]
                val originalHasNoUnit = originalNumber.second.isNullOrBlank()
                val numberChanged = originalNumber.first != candidateNumber.first

                originalHasNoUnit &&
                    numberChanged &&
                    !isSafeRetailDecimalShorthand(original, candidate, originalNumber.first, candidateNumber.first)
            }
        }

        private fun hasLiquidRetailContext(original: String, candidate: String): Boolean {
            val text = "$original $candidate"
            return liquidRetailTokens.any { token -> token in text }
        }

        private fun hasPharmaContext(original: String, candidate: String): Boolean {
            val text = "$original $candidate"
            return pharmaContextTokens.any { token -> token in text }
        }

        private fun hasLiterUnit(candidate: String): Boolean {
            return Regex("""\b\d+(?:[.,]\d+)?\s*(l|lt|litro|litros)\b""").containsMatchIn(candidate)
        }

        private fun significantTokens(text: String): List<String> {
            return text
                .replace(Regex("(\\d+(?:[.,]\\d+)?)([a-zA-Z%]+)"), "$1 $2")
                .replace(Regex("[^\\p{L}\\p{N}%]+"), " ")
                .split(Regex("\\s+"))
                .map { it.trim().trim('.', ',', '-', '_') }
                .filter { token ->
                    token.isNotBlank() &&
                        token.toDoubleOrNull() == null &&
                        token !in genericIdentityTokens &&
                        token !in measurementTokens
                }
        }

        private val genericIdentityTokens = setOf(
            "agua", "mesa", "embotellada", "mineral", "leche", "formula", "lactea",
            "infantil", "bebes", "bebe", "polvo", "producto", "marca", "generico",
            "tableta", "tabletas", "tablet", "tablets", "capsula", "capsulas",
            "capsule", "capsules", "jarabe", "crema", "gel", "unidad", "unidades",
            "pieza", "piezas", "frasco", "frascos", "pack", "caja", "cajas",
            "suplemento", "suplementos", "supplement", "supplements", "dietary",
            "daily", "multi", "vitaminico", "vitaminicos", "vitamin", "vitamins"
        )

        private val measurementTokens = setOf(
            "mg", "mcg", "g", "gr", "kg", "ml", "cl", "l", "lt", "litro", "litros", "ui", "iu", "%"
        )

        private val liquidRetailTokens = setOf(
            "agua", "bebida", "gaseosa", "refresco", "jugo", "litro", "litros", "ml", " l", "lt"
        )

        private val pharmaContextTokens = setOf(
            "mg", "mcg", "ui", "iu", "tableta", "tabletas", "capsula", "capsulas",
            "jarabe", "ampolla", "gotas", "antibiotico", "analgesico"
        )
    }

    enum class SafetyMode { STRICT, RELAXED }

    /**
     * El usuario rechaza la sugerencia de corrección.
     */
    fun descartarCorreccion(original: String, corregido: String) {
        correccionesRechazadas.add("${original.trim().lowercase()}|${corregido.trim().lowercase()}")
        _state.value = _state.value.copy(pendingCorrection = null)
    }

    /**
     * V10.1: Marca una corrección como procesada sin bloquearla permanentemente.
     * Evita que la burbuja reaparezca inmediatamente tras aplicar.
     */
    fun marcarCorreccionAplicada() {
        _state.value = _state.value.copy(pendingCorrection = null)
    }

    /**
     * Se llama para sincronizar el estado cuando cambia el nombre, 
     * pero ya no dispara la IA automáticamente.
     */
    fun onNombreCambio(
        nombre: String,
        categoriasExistentes: List<String>,
        mercadoActivo: String = "Perú",
        monedaSimbolo: String = "S/",
        onStateChange: (Boolean) -> Unit = {}
    ) {
        // En esta nueva versión, onNombreCambio solo limpia trabajos pendientes
        // si el nombre cambia radicalmente, pero la actualización de texto
        // e interpretación ya debió ocurrir en actualizarInterpretacionLocal.
        
        val normalizedName = normalizar(nombre)
        val key = normalizedName + "_$mercadoActivo"
        
        if (key != ultimaConsultaKey) {
            trabajoActual?.cancel()
            latestSearchRequestId++
        }
    }

    /**
     * V17.0: Disparo manual de la sugerencia IA (Modo Manual Silencioso).
     * V17.4: Debounce real de 500ms para evitar r\u00e1fagas innecesarias.
     */
    fun buscarSugerenciaIA(
        nombre: String,
        categoriasExistentes: List<String>,
        mercadoActivo: String = "Per\u00fa",
        monedaSimbolo: String = "S/",
        inmediato: Boolean = false, // V17.4: Soporte para salto de debounce (ej. IME Search)
        onStateChange: (Boolean) -> Unit = {}
    ) {
        val normalizedName = normalizar(nombre)
        android.util.Log.d("SilentAI", "buscarSugerenciaIA: '$nombre', inmediato: $inmediato")

        if (normalizedName.length < 3) {
            trabajoActual?.cancel()
            _state.value = _state.value.copy(
                status = CategorySuggestionStatus.INITIAL, 
                suggestion = null,
                asistenciaManualCategorias = emptyList(),
                estaCargandoAsistencia = false
            )
            return
        }

        trabajoActual?.cancel()
        latestSearchRequestId++
        val requestId = latestSearchRequestId

        // V17.11: Verificar caché antes de cualquier delay
        val sugerenciaCache = cacheSugerenciasSilenciosas[normalizedName]
        if (sugerenciaCache != null) {
            android.util.Log.d("SilentAI", "Sugerencia recuperada de cach\u00e9: '$sugerenciaCache'")
            aplicarResultadoSugerencia(nombre, sugerenciaCache, categoriasExistentes)
            // V17.10: Pre-cargar tipo inmediatamente desde el cach\u00e9 de categor\u00eda
            asistirManualConTipo(nombre, sugerenciaCache, inmediato = true)
            return
        }

        trabajoActual = viewModelScope.launch {
            // V17.4: Debounce de 300ms si no es inmediato
            if (!inmediato) {
                delay(300)
            }
            if (requestId != latestSearchRequestId) return@launch

            android.util.Log.d("SilentAI", "Lanzando petici\u00f3n de red...")
            
            // V17.5: Feedback de carga sutil (en el estado de asistencia manual)
            _state.value = _state.value.copy(
                estaCargandoAsistencia = true,
                asistenciaManualCategorias = emptyList() // Limpiamos previos mientras busca
            )
            if (requestId != latestSearchRequestId) {
                android.util.Log.w("CategoryAI", "Petición cancelada por nueva entrada")
                return@launch
            }

            val category = CategorySuggestionRepository.sugerirCategoriaSilenciosa(
                productName = nombre,
                mercadoActivo = mercadoActivo
            )
            
            if (requestId != latestSearchRequestId) return@launch
            
            if (!category.isNullOrBlank()) {
                // V17.7: Guardamos en caché para el futuro
                cacheSugerenciasSilenciosas[normalizedName] = category
                aplicarResultadoSugerencia(nombre, category, categoriasExistentes)

                // V17.10: PIPELINE DE IA - Pre-cargar tipo de forma silenciosa e inmediata
                // lanzamos la búsqueda de tipo usando la categoría que acabamos de predecir.
                asistirManualConTipo(nombre, category, inmediato = true)
            } else {
                _state.value = _state.value.copy(estaCargandoAsistencia = false)
            }
        }
    }

    private fun aplicarResultadoSugerencia(nombre: String, category: String, categoriasExistentes: List<String>) {
        _state.value = _state.value.copy(
            asistenciaManualCategorias = listOf(category),
            estaCargandoAsistencia = false,
            status = CategorySuggestionStatus.READY,
            suggestion = CategorySuggestion(
                productName = nombre,
                categoria = category,
                emoji = "✨",
                tipoControl = TipoControlDetectado.DESCONOCIDO,
                razonTipo = "",
                requiereReceta = false,
                razonReceta = "",
                modoIngreso = ModoIngresoDetectado.DESCONOCIDO,
                presentacionesSugeridas = emptyList(),
                existeEnLista = categoriasExistentes.any { it.equals(category, ignoreCase = true) },
                confianza = 100,
                razon = ""
            )
        )
    }

    /**
     * V14.4: Cuando el usuario toca un chip de opción sugerida tras un conflicto.
     */
    fun seleccionarChipOpcion(
        opcion: String,
        categoriasExistentes: List<String>,
        mercadoActivo: String = "Perú",
        monedaSimbolo: String = "S/",
        onStateChange: (Boolean) -> Unit = {}
    ) {
        // 1. Limpiar estado anterior
        reset()
        
        // 2. Disparar nueva búsqueda con el nombre limpio e inmediata (V17.4)
        buscarSugerenciaIA(opcion, categoriasExistentes, mercadoActivo, monedaSimbolo, true, onStateChange)
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
        categoriasExistentes: List<String>,
        mercadoActivo: String = "Perú",
        monedaSimbolo: String = "S/",
        onStateChange: (Boolean) -> Unit = {}
    ) {
        // Forzar nueva consulta limpiando la última key.
        ultimaConsultaKey = null
        latestSearchRequestId++
        latestCorrectionRequestId++
        _state.value = CategorySuggestionUiState(
            status = CategorySuggestionStatus.INITIAL,
            queryName = nombre
        )
        onNombreCambio(nombre, categoriasExistentes, mercadoActivo, monedaSimbolo, onStateChange)
    }

    /**
     * V13.7: Inicia el pre-calentamiento de los servidores de IA.
     * V13.8: Inicializa la caché persistente con el contexto de la aplicación.
     */
    fun warmup() {
        val geminiKey = BuildConfig.GEMINI_API_KEY
        val deepSeekKey = BuildConfig.DEEPSEEK_API_KEY
        CategorySuggestionRepository.warmup(geminiKey, deepSeekKey)
    }

    /**
     * V16.0: Asistencia silenciosa basada en el nombre del producto.
     * Genera chips de categorías sugeridas para el modo manual.
     */
    fun asistirManualConNombre(nombre: String, mercadoActivo: String = "Per\u00fa") {
        if (nombre.trim().length < 3) {
            _state.value = _state.value.copy(
                asistenciaManualCategorias = emptyList(),
                estaCargandoAsistencia = false
            )
            return
        }

        jobAsistencia?.cancel()
        _state.value = _state.value.copy(estaCargandoAsistencia = true)
        jobAsistencia = viewModelScope.launch {
            delay(700)
            // Llamamos al repo para que guarde en su caché interna
            val sugerencias = CategorySuggestionRepository.asistenteManualLigero(
                productName = nombre,
                mercadoActivo = mercadoActivo
            )
            _state.value = _state.value.copy(
                asistenciaManualCategorias = sugerencias.take(2),
                estaCargandoAsistencia = false
            )
        }
    }

    /**
     * V16.0: Asistencia tipo "YouTube" mientras escribe en el campo categoría.
     */
    fun asistirManualConTextoCategoria(texto: String, productName: String = "", mercadoActivo: String = "Per\u00fa") {
        // V16.5: Permitir b\u00fasqueda sin texto de categor\u00eda si hay un nombre de producto (Al recibir foco)
        if (texto.length < 2 && productName.isBlank()) {
            _state.value = _state.value.copy(
                asistenciaManualCategorias = emptyList(),
                estaCargandoAsistencia = false
            )
            return
        }

        jobAsistencia?.cancel()
        
        // V16.6: Si el usuario est\u00e1 escribiendo (texto no vac\u00edo), limpiamos resultados previos 
        // inmediatamente para dar feedback de que estamos buscando algo nuevo.
        _state.value = _state.value.copy(
            asistenciaManualCategorias = if (texto.isNotBlank()) emptyList() else _state.value.asistenciaManualCategorias,
            estaCargandoAsistencia = true
        )

        jobAsistencia = viewModelScope.launch {
            delay(if (texto.isBlank()) 0 else 350)
            val sugerencias = CategorySuggestionRepository.asistenteManualLigero(
                productName = productName,
                partialCategory = texto,
                mercadoActivo = mercadoActivo
            )
            _state.value = _state.value.copy(
                asistenciaManualCategorias = sugerencias.take(2),
                estaCargandoAsistencia = false
            )
        }
    }

    /**
     * V16.10: Asistencia silenciosa para el tipo de control (Unidad/Peso/L\u00edquido).
     * V17.2: Soporte para ejecuci\u00f3n inmediata (sin delay) si viene de una selecci\u00f3n IA.
     */
    fun asistirManualConTipo(nombre: String, categoria: String, inmediato: Boolean = false) {
        if (nombre.isBlank() || categoria.isBlank()) {
            jobTipo?.cancel()
            _state.value = _state.value.copy(
                sugerenciaTipoManual = null,
                estaCargandoTipo = false
            )
            latestTypeSuggestionKey = null
            return
        }

        val requestKey = "${normalizar(nombre)}|${normalizar(categoria)}"
        if (requestKey == latestTypeSuggestionKey) return

        jobTipo?.cancel()
        latestTypeSuggestionKey = requestKey
        _state.value = _state.value.copy(estaCargandoTipo = true)

        jobTipo = viewModelScope.launch {
            if (!inmediato) delay(350) // Debounce reducido para manual
            
            // Verificamos contexto post-delay
            if (requestKey != latestTypeSuggestionKey) return@launch

            val result = CategorySuggestionRepository.sugerirTipoControlManual(nombre, categoria)
            
            // Validación final de contexto
            if (requestKey == latestTypeSuggestionKey) {
                _state.value = _state.value.copy(
                    sugerenciaTipoManual = result,
                    estaCargandoTipo = false
                )
            }
        }
    }

    fun reset() {
        trabajoActual?.cancel()
        jobCorreccion?.cancel()
        jobAsistencia?.cancel()
        jobTipo?.cancel()
        ultimaConsultaKey = null
        latestTypeSuggestionKey = null
        latestBarcodeKey = null
        latestSearchRequestId++
        latestCorrectionRequestId++
        latestBarcodeRequestId++
        _state.value = CategorySuggestionUiState()
    }

    /**
     * V16.3: Limpia las sugerencias de asistencia manual (ej. al seleccionar una).
     */
    fun limpiarAsistenciaManual() {
        jobAsistencia?.cancel()
        _state.value = _state.value.copy(
            asistenciaManualCategorias = emptyList(),
            estaCargandoAsistencia = false
        )
    }

    private fun normalizar(name: String): String {
        return normalizeProductName(name)
    }

    fun identificarBarcode(
        barcode: String,
        imageBase64: String?,
        categoriasExistentes: List<String>
    ) {
        val normalizedBarcode = barcode.trim()

        if (normalizedBarcode.isBlank()) {
            latestBarcodeKey = null

            _state.value = _state.value.copy(
                barcodeAiResult = null,
                estaIdentificandoBarcode = false,
                barcodeAiRequestId = ++latestBarcodeRequestId,
                barcodeMismatchDetected = false,
                barcodeMismatchOriginalName = null
            )

            return
        }

        val current = _state.value

        // V18.2: Si es el mismo código, pero ahora traemos imagen y antes no,
        // permitimos re-identificar solo si antes NO se identificó.
        val yaIdentificadoSinImagen =
            latestBarcodeKey == normalizedBarcode &&
                    current.barcodeAiResult?.codigo == normalizedBarcode

        val forzarReidentificarConImagen =
            imageBase64 != null &&
                    yaIdentificadoSinImagen &&
                    current.barcodeAiResult.estado == "NO_IDENTIFICADO"

        if (
            !forzarReidentificarConImagen &&
            latestBarcodeKey == normalizedBarcode &&
            (
                    current.estaIdentificandoBarcode ||
                            (current.barcodeAiResult?.codigo == normalizedBarcode && current.barcodeAiResult.estado != "NO_IDENTIFICADO")
                    )
        ) {
            return
        }

        jobBarcode?.cancel()

        val requestId = ++latestBarcodeRequestId
        latestBarcodeKey = normalizedBarcode

        _state.value = _state.value.copy(
            estaIdentificandoBarcode = true,
            barcodeAiResult = null,
            barcodeAiRequestId = requestId
        )

        jobBarcode = viewModelScope.launch {
            val categoriasBaseRetail = listOf(
                "Farmacia", "Medicamentos", "Analgésicos", "Antibióticos", "Antiinflamatorios",
                "Vitaminas", "Suplementos", "Cuidado de la salud", "Abarrotes", "Alimentos",
                "Bebidas", "Snacks", "Galletas", "Cereales", "Conservas", "Lácteos", "Dulces",
                "Panadería", "Limpieza", "Cuidado personal", "Higiene personal", "Cosméticos",
                "Mascotas", "Alimentos para mascotas", "Comida para perros", "Comida para gatos",
                "Snacks para mascotas", "Accesorios para mascotas"
            )

            val categoriasParaIa = (categoriasExistentes + categoriasBaseRetail)
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinctBy { it.lowercase(Locale.getDefault()) }

            val result = withTimeoutOrNull(18_000) {
                CategorySuggestionRepository.buscarProductoPorBarcodeDuckDuckGo(
                    barcode = normalizedBarcode,
                    categoriasExistentes = categoriasParaIa
                )
            } ?: BarcodeAiResult(
                estado = "NO_IDENTIFICADO",
                codigo = normalizedBarcode,
                nombre = "",
                categoria = "",
                tipoControl = "DESCONOCIDO",
                requiereReceta = false,
                razon = "No se encontró este código en búsqueda web. Ingresa los datos manualmente para guardarlo.",
                presentacionVentaDefault = "",
                ventaFraccionadaPermitida = null,
                confianzaPresentacion = 0,
                razonPresentacion = "",
                presentacionesVentaSugeridasTexto = "",
                confianzaPresentacionesVenta = 0,
                razonPresentacionesVenta = ""
            )

            if (requestId == latestBarcodeRequestId) {
                _state.value = _state.value.copy(
                    barcodeAiResult = result,
                    estaIdentificandoBarcode = false
                )
            }
        }
    }


    fun identificarProductoPorImagen(
        imageBase64: String,
        categoriasExistentes: List<String>
    ) {
        if (imageBase64.isBlank()) {
            _state.value = _state.value.copy(
                barcodeAiResult = BarcodeAiResult(
                    estado = "NO_IDENTIFICADO",
                    codigo = "",
                    nombre = "",
                    categoria = "",
                    tipoControl = "DESCONOCIDO",
                    requiereReceta = false,
                    razon = "No se recibió imagen para analizar."
                ),
                estaIdentificandoBarcode = false,
                barcodeAiRequestId = ++latestBarcodeRequestId,
                barcodeMismatchDetected = false,
                barcodeMismatchOriginalName = null
            )
            return
        }

        jobBarcode?.cancel()

        val requestId = ++latestBarcodeRequestId
        latestBarcodeKey = null

        _state.value = _state.value.copy(
            estaIdentificandoBarcode = true,
            barcodeAiResult = null,
            barcodeAiRequestId = requestId,
            barcodeMismatchDetected = false,
            barcodeMismatchOriginalName = null
        )

        jobBarcode = viewModelScope.launch {
            val categoriasBaseRetail = listOf(
                "Farmacia",
                "Medicamentos",
                "Analgésicos",
                "Antibióticos",
                "Vitaminas",
                "Suplementos",
                "Abarrotes",
                "Alimentos",
                "Bebidas",
                "Snacks",
                "Galletas",
                "Cereales",
                "Conservas",
                "Lácteos",
                "Limpieza",
                "Cuidado personal",
                "Higiene personal",
                "Mascotas",
                "Alimentos para mascotas",
                "Comida para perros",
                "Comida para gatos",
                "Snacks para mascotas",
                "Accesorios para mascotas"
            )

            val categoriasParaIa = (categoriasExistentes + categoriasBaseRetail)
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinctBy { it.lowercase(Locale.getDefault()) }

            val result = withTimeoutOrNull(40_000) {
                CategorySuggestionRepository.identificarProductoPorImagen(
                    imageBase64 = imageBase64,
                    categoriasExistentes = categoriasParaIa
                )
            } ?: BarcodeAiResult(
                estado = "NO_IDENTIFICADO",
                codigo = "",
                nombre = "",
                categoria = "",
                tipoControl = "DESCONOCIDO",
                requiereReceta = false,
                razon = "Tiempo de espera agotado al analizar la imagen."
            )

            if (requestId == latestBarcodeRequestId) {
                _state.value = _state.value.copy(
                    barcodeAiResult = result,
                    estaIdentificandoBarcode = false
                )
            }
        }
    }

    /**
     * V18.8: Verifica si un nombre de producto escrito por el usuario coincide 
     * con la identidad real del código de barras escaneado.
     */
    fun verificarIntegridadBarcode(barcode: String, nombreActual: String) {
        val current = _state.value
        val resultIA = current.barcodeAiResult ?: return
        if (resultIA.estado != "IDENTIFICADO") return
        if (resultIA.nombre.isNullOrBlank()) return

        val tokensOriginal = barcodeIdentityTokens(resultIA.nombre)
        val tokensNew = barcodeIdentityTokens(nombreActual)
        val hasStrongMatch = tokensOriginal.any { original ->
            tokensNew.any { candidate ->
                original == candidate ||
                    (original.length >= 5 && candidate.length >= 5 &&
                        (original.startsWith(candidate) || candidate.startsWith(original)))
            }
        }

        if (hasStrongMatch) {
            limpiarConflictoBarcode()
            return
        }

        // V31.5: Si el nombre cambió drásticamente y no hay match, desactivamos 'barcodeAiAppliedState'.
        // Esto rompe el "blindaje" en CrearProducto.kt y permite que el inventario se limpie
        // si el usuario decide cambiar el tipo de control del nuevo producto manual.
        _state.value = _state.value.copy(
            barcodeMismatchDetected = true,
            barcodeMismatchOriginalName = resultIA.nombre,
            barcodeAiAppliedState = false
        )
    }

    fun limpiarConflictoBarcode() {
        val current = _state.value
        if (!current.barcodeMismatchDetected && current.barcodeMismatchOriginalName == null && current.barcodeAiAppliedState) return

        _state.value = current.copy(
            barcodeMismatchDetected = false,
            barcodeMismatchOriginalName = null,
            barcodeAiAppliedState = true
        )
    }

    /**
     * V20.0: Busca información de uso del producto.
     */
    fun buscarInfoUsoProducto(nombre: String, categoria: String = "") {
        val key = "${nombre.trim().lowercase()}|${categoria.trim().lowercase()}"
        if (key == latestUsageInfoKey || nombre.isBlank()) return

        jobUsageInfo?.cancel()
        latestUsageInfoKey = key

        val catClean = categoria.trim().lowercase()
        val nonMedicalCategories = listOf(
            "bebida", "bebidas",
            "snack", "snacks",
            "golosina", "golosinas",
            "alimento", "alimentos",
            "limpieza",
            "aseo",
            "higiene",
            "refresco", "refrescos",
            "gaseosa", "gaseosas",
            "galleta", "galletas",
            "helado", "helados",
            "cuidado personal",
            "cosmetico", "cosmético",
            "maquillaje",
            "accesorio",
            "despensa",
            "licor",
            "cerveza",
            "jugo", "jugos",
            "agua",
            "papeleria", "papelería"
        )

        if (nonMedicalCategories.any { catClean.contains(it) }) {
            _state.update {
                it.copy(
                    estaCargandoInfoUso = false,
                    infoUsoProducto = null
                )
            }
            return
        }

        jobUsageInfo = viewModelScope.launch(Dispatchers.IO) {
            try {
                _state.update { it.copy(estaCargandoInfoUso = true) }

                val result = CategorySuggestionRepository.obtenerInformacionUsoProducto(nombre)

                _state.update {
                    it.copy(
                        estaCargandoInfoUso = false,
                        infoUsoProducto = result
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        estaCargandoInfoUso = false,
                        infoUsoProducto = null
                    )
                }
            }
        }
    }

    private fun barcodeIdentityTokens(text: String): List<String> {
        val genericTokens = setOf(
            "agua", "mesa", "embotellada", "mineral", "leche", "formula", "lactea",
            "infantil", "bebes", "bebe", "polvo", "producto", "marca", "generico",
            "tableta", "tabletas", "tablet", "tablets", "capsula", "capsulas",
            "capsule", "capsules", "jarabe", "crema", "gel", "unidad", "unidades",
            "pieza", "piezas", "frasco", "frascos", "pack", "caja", "cajas",
            "suplemento", "suplementos", "supplement", "supplements", "dietary",
            "daily", "multi", "vitaminico", "vitaminicos", "vitamin", "vitamins"
        )
        val measurementTokens = setOf(
            "mg", "mcg", "g", "gr", "kg", "ml", "cl", "l", "lt", "litro", "litros", "ui", "iu", "%"
        )

        return text
            .lowercase()
            .replace(Regex("(\\d+(?:[.,]\\d+)?)([a-zA-Z%]+)"), "$1 $2")
            .replace(Regex("[^\\p{L}\\p{N}%]+"), " ")
            .split(Regex("\\s+"))
            .map { it.trim() }
            .filter { token ->
                token.length > 2 &&
                    token.toDoubleOrNull() == null &&
                    token !in genericTokens &&
                    token !in measurementTokens
            }
            .distinct()
    }
}
