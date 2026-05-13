package com.app.administradorfarmadon.ActivityInventario

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import java.time.LocalDate
import java.time.YearMonth
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteProducto
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.PresentacionProducto
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.SugerenciaPresentacion
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.toMoldeProductos
import com.app.administradorfarmadon.ActivityInventario.domain.PresentacionRules
import com.app.administradorfarmadon.ActivityInventario.ui.CreateProductControlType
import com.app.administradorfarmadon.ActivityInventario.ui.CreateProductPresentation
import com.app.administradorfarmadon.ActivityInventario.ui.CreateProductScreen
import com.app.administradorfarmadon.ActivityInventario.ui.CreateProductState
import com.app.administradorfarmadon.ActivityInventario.ui.CreateProductStep
import com.app.administradorfarmadon.ActivityInventario.ui.ProductCreatedSummary
import com.app.administradorfarmadon.ActivityInventario.ui.SmartProductHint
import com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestion
import com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionStatus
import com.app.administradorfarmadon.ActivityInventario.reference.CategorySuggestionViewModel
import com.app.administradorfarmadon.ActivityInventario.reference.LabelScannerViewModel
import com.app.administradorfarmadon.ActivityInventario.reference.TipoControlDetectado
import com.app.administradorfarmadon.ActivityInventario.reference.ProductReference
import com.app.administradorfarmadon.ActivityInventario.reference.ProductReferenceViewModel
import com.app.administradorfarmadon.ClasesDatabase.PresentacionesTiendaConfigManager
import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import com.app.administradorfarmadon.R
import com.google.firebase.database.FirebaseDatabase
import java.text.Normalizer
import java.util.Locale
import java.util.UUID
import com.app.administradorfarmadon.ActivityInventario.ui.CreateProductStockEntryMode
import com.app.administradorfarmadon.ActivityInventario.ui.StockControlMode
import com.app.administradorfarmadon.ActivityInventario.ui.getEffectiveStockControlMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.google.android.material.card.MaterialCardView


class CrearProducto : AppCompatActivity() {

    private val productReferenceViewModel: ProductReferenceViewModel by viewModels()
    // Feature de sugerencia de categoría por IA (Gemini).
    private val categorySuggestionViewModel: CategorySuggestionViewModel by viewModels()
    // Feature de escaneo OCR de etiquetas (cámara + Gemini Vision).
    private val labelScannerViewModel: LabelScannerViewModel by viewModels()
    private var createProductUiState by mutableStateOf(CreateProductState())
    private var createProductLoading by mutableStateOf(false)
    private var createProductSuccessSummary by mutableStateOf<ProductCreatedSummary?>(null)
    private var createProductShowReference by mutableStateOf(false)
    private var createProductReferenceDismissed by mutableStateOf(false)
    private val createProductCategoryOptions = mutableStateListOf<String>()
    private val createProductExistingLots = mutableStateListOf<String>()
    private val createProductExistingNormalizedNames = mutableStateListOf<String>()
    private var ultimoProductoCreadoCompose: MoldeProductos? = null
    private var createProductCategoryValidationJob: Job? = null
    private var createProductNameValidationJob: Job? = null
    // Fix (M5): recuerda el último (nombre|categoria) por el que ya se pidió
    // la búsqueda de referencia para evitar lanzar requests duplicados cuando
    // el usuario va y vuelve entre pasos sin cambiar esos campos.
    private var ultimaReferenciaSolicitada: String? = null

    private val listaCategoria = mutableListOf<CategoriaProductos>()
    private val listaPresentaciones = mutableListOf<PresentacionProducto>()
    private var presentacionPrincipalElegidaManualmente = false
    // Fix (M1): se eliminaron campos heredados del antiguo flujo XML que
    // ya no se leen en ningún punto: `listaNombreAutoComplete`,
    // `reglaPresentacionBaseObligatoriaActiva`,
    // `nombrePresentacionBaseObligatoriaActual`,
    // `ultimoPrecioPresentacionBaseObligatoria`, `imagenUri`,
    // `cameraImageUri`. También se quitaron sus reseteos en `limpiarFormularioCompose`.

    private var statusBarColorOriginal: Int? = null
    private var statusBarLightOriginal: Boolean? = null







    companion object {
        private const val PATH_INVENTARIO = "Inventario"
        private const val PATH_CATEGORIAS = "CategoriasInventario"
        private const val PATH_NOMBRES = "NombresProductos"
        private const val PATH_NOMBRES_NORMALIZADOS = "NombresProductosNormalizados"
        private const val PATH_PRODUCTOS = "Productos"
        private const val PATH_BUSQUEDA = "BusquedaProductos"
        private const val PATH_PRESENTACIONES = "Presentaciones"

        private const val CLOUDINARY_CLOUD_NAME = "dluvatyh7"
        private const val CLOUDINARY_UPLOAD_PRESET = "productos_app"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                manejarAtrasFlujoComposeCrearProducto()
            }
        })

        aplicarStatusBarBlanca()

        createProductUiState = construirEstadoInicialCreateProduct()

        setContent {
            val referenceState by productReferenceViewModel.referenceState.collectAsState()
            // Sugerencia IA de categoría (Gemini). El chip que vive dentro
            // de CategoryAutocompleteField se actualiza vía este state.
            val categorySuggestionState by categorySuggestionViewModel.state.collectAsState()
            val labelScannerState by labelScannerViewModel.state.collectAsState()

            // Launcher de la cámara que captura un Bitmap (preview).
            // El bitmap se le pasa al ViewModel para procesarlo con Gemini.
            val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview()
            ) { bitmap ->
                if (bitmap != null) labelScannerViewModel.procesar(bitmap)
            }

            // Launcher del permiso CAMERA. Si el usuario aprueba, abrimos la
            // cámara; si rechaza, no hacemos nada (el botón sigue disponible
            // por si el usuario re-intenta).
            val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
            ) { granted ->
                if (granted) cameraLauncher.launch(null)
            }

            MaterialTheme {
                CreateProductScreen(
                    state = createProductUiState,
                    categoryOptions = createProductCategoryOptions.toList(),
                    smartHint = null,
                    existingLotNumbers = createProductExistingLots.toSet(),
                    // Fix (F1): el botón "Siguiente"/"Guardar" antes solo se
                    // desactivaba si había error en `name`. Ahora refleja la
                    // validez completa del paso actual mediante un chequeo
                    // puro (sin mutar el estado), de modo que el usuario ve
                    // visualmente cuándo puede avanzar.
                    nextEnabled = !createProductLoading &&
                            puedeAvanzarPasoCreateProduct(
                                createProductUiState,
                                createProductExistingLots.toSet()
                            ),
                    referenceState = referenceState,
                    categorySuggestionState = categorySuggestionState,
                    onAcceptCategorySuggestion = ::aceptarSugerenciaCategoria,
                    onSwitchToManualCategory = ::cambiarACategoriaManual,
                    onBackToAiCategory = ::volverASugerenciaIaCategoria,
                    labelScannerState = labelScannerState,
                    onRequestLabelScan = {
                        // Si ya tenemos permiso, abrir cámara directo; si no,
                        // pedirlo y al aprobar el launcher de permiso abre
                        // la cámara automáticamente.
                        val granted = androidx.core.content.ContextCompat
                            .checkSelfPermission(this, android.Manifest.permission.CAMERA) ==
                            android.content.pm.PackageManager.PERMISSION_GRANTED
                        if (granted) cameraLauncher.launch(null)
                        else permissionLauncher.launch(android.Manifest.permission.CAMERA)
                    },
                    onConsumeLabelScan = { labelScannerViewModel.reset() },
                    loading = createProductLoading,
                    successSummary = createProductSuccessSummary,
                    showReferenceScreen = createProductShowReference,
                    showReferenceSection = !createProductReferenceDismissed,
                    onBack = ::manejarAtrasFlujoComposeCrearProducto,
                    onStateChange = ::actualizarCreateProductState,
                    onNext = ::avanzarPasoCreateProduct,
                    onPrevious = ::retrocederPasoCreateProduct,
                    onSave = ::guardarProductoDesdeCompose,
                    onCreateAnother = ::reiniciarFlujoComposeCrearProducto,
                    onViewProduct = ::abrirProductoRecienCreado,
                    onRetryReference = {
                        productReferenceViewModel.retry(
                            productName = createProductUiState.name,
                            category = createProductUiState.category
                        )
                    },
                    onOpenReference = { createProductShowReference = true },
                    onDismissReference = { createProductShowReference = false },
                    onConfirmReference = {
                        createProductReferenceDismissed = false
                        createProductShowReference = false
                        guardarReferenciaSugeridaEnProducto(it)
                    },
                    onSkipReference = {
                        reiniciarFlujoComposeCrearProducto()
                    }
                )
            }
        }

        window.decorView.post {
            PresentacionesTiendaConfigManager.precargar()
            obtenerCategoriasDeProductos()
            obtenerNombresDeProductos()
            cargarPresentacionesGuardadas()
            // Fix (F4): los lotes existentes antes solo se pedían al llegar
            // al paso LOTE_INICIAL. Esa carga asíncrona muchas veces llegaba
            // después de que el usuario ya había escrito un lote, dejando
            // pasar duplicados sin advertencia. Ahora se precarga junto con
            // las demás listas para que la validación tenga el set listo.
            obtenerLotesExistentesParaCrearProducto()
        }
    }

    private fun cargarPresentacionesGuardadas() {
        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child("PresentacionesGuardadas")
            .get()
            .addOnSuccessListener { snapshot ->
                val opciones = mutableListOf<com.app.administradorfarmadon.ActivityInventario.ui.SavedPresentation>()
                snapshot.children.forEach { ctTypeSnap ->
                    val controlType = ctTypeSnap.key ?: return@forEach
                    ctTypeSnap.children.forEach { entry ->
                        val nombre = entry.child("nombre").getValue(String::class.java)
                            ?: entry.key.orEmpty()
                        val equiv = entry.child("equivalencia").getValue(String::class.java)
                            ?: ""
                        if (nombre.isNotBlank()) {
                            opciones.add(
                                com.app.administradorfarmadon.ActivityInventario.ui.SavedPresentation(
                                    name = nombre,
                                    equivalenceBase = equiv,
                                    controlType = controlType
                                )
                            )
                        }
                    }
                }
                createProductUiState = createProductUiState.copy(savedPresentationOptions = opciones)
            }
    }

    private fun guardarPresentacionEnDb(saved: com.app.administradorfarmadon.ActivityInventario.ui.SavedPresentation) {
        if (saved.name.isBlank() || saved.controlType.isBlank()) return
        val key = saved.name.lowercase(Locale.getDefault()).replace(Regex("[^a-z0-9]+"), "_")
        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child("PresentacionesGuardadas")
            .child(saved.controlType)
            .child(key)
            .setValue(mapOf("nombre" to saved.name, "equivalencia" to saved.equivalenceBase))
    }

    override fun onDestroy() {
        restaurarStatusBarOriginal()
        super.onDestroy()
    }



    /**
     * Fix (M4): antes el cuerpo era `CreateProductState(controlType = null)`
     * y un comentario "// ... resto de valores". `null` ya es el valor
     * por defecto en el data class, así que se reduce a usar el
     * constructor directamente. Se mantiene como función para que el
     * llamado en `limpiarFormularioCompose` quede expresivo.
     */
    private fun construirEstadoInicialCreateProduct(): CreateProductState {
        return CreateProductState()
    }

    private fun manejarAtrasFlujoComposeCrearProducto() {
        if (createProductShowReference) {
            createProductShowReference = false
            return
        }
        if (createProductSuccessSummary != null) {
            reiniciarFlujoComposeCrearProducto()
            return
        }
        if (createProductUiState.currentStep != CreateProductStep.PRODUCTO) {
            retrocederPasoCreateProduct()
            return
        }
        finish()
    }

    private fun actualizarCreateProductState(nuevoEstado: CreateProductState) {
        val estadoAnterior = createProductUiState

        // Persistir cualquier nueva presentacion guardada por el usuario
        val nuevasPresentaciones = nuevoEstado.savedPresentationOptions.filterNot { nueva ->
            estadoAnterior.savedPresentationOptions.any { existente ->
                existente.name.equals(nueva.name, ignoreCase = true) &&
                    existente.controlType == nueva.controlType
            }
        }
        nuevasPresentaciones.forEach { guardarPresentacionEnDb(it) }

        val estadoNormalizado = if (
            nuevoEstado.controlType != createProductUiState.controlType &&
            nuevoEstado.controlType != null
        ) {
            nuevoEstado.copy(
                receivedPresentation = "",
                presentations = sugerirPresentacionesIniciales(
                    nuevoEstado.controlType,
                    nuevoEstado.receivedPresentation
                ),
                mainPresentationId = "",
                draftPresentationName = "",
                addPresentationExpanded = false
            )
        } else {
            nuevoEstado
        }

        // Fix (M3): se eliminó la llamada a `normalizarPresentacionesSegunLote`.
        // Esa función dependía de `state.receivedPresentation`, un campo
        // heredado del flujo XML que el flujo Compose actual nunca setea,
        // así que siempre devolvía la lista sin cambios y solo generaba
        // recomposiciones inútiles. La normalización de `mainPresentationId`
        // se mantiene aquí para garantizar que apunte a una presentación
        // existente.
        val presentacionesNormalizadas = estadoNormalizado.presentations
        val mainIdNormalizado = when {
            presentacionesNormalizadas.isEmpty() -> ""
            presentacionesNormalizadas.any { it.id == estadoNormalizado.mainPresentationId } ->
                estadoNormalizado.mainPresentationId
            else -> presentacionesNormalizadas.first().id
        }

        createProductUiState = estadoNormalizado.copy(
            presentations = presentacionesNormalizadas,
            mainPresentationId = mainIdNormalizado
        )

        if (estadoAnterior.name != createProductUiState.name) {
            programarValidacionNombreCompose(createProductUiState.name)
            // Sugerencia IA: re-evaluar la categoría cuando cambia el nombre.
            // El propio ViewModel aplica debounce de 800ms y respeta el modo
            // manual si el usuario ya decidió escribir.
            categorySuggestionViewModel.onNombreCambio(
                nombre = createProductUiState.name,
                categoriasExistentes = createProductCategoryOptions.toList()
            )
        }
        if (estadoAnterior.category != createProductUiState.category) {
            programarValidacionCategoriaCompose(createProductUiState.category)
        }
    }

    /**
     * Se invoca AUTOMÁTICAMENTE desde la tarjeta READY (la UI llama esto
     * sin que el usuario tenga que tocar un botón "Usar"). Hace varias cosas:
     *  1. Auto-rellena `state.category` con la categoría sugerida.
     *  2. Auto-rellena `state.controlType` con el tipo detectado por la IA
     *     (UNIDAD/PESO/LIQUIDO). Si la IA devolvió DESCONOCIDO no se toca
     *     el controlType: el usuario lo elegirá manualmente al ir a manual.
     *  3. Agrega la categoría a las opciones locales si era nueva.
     *  4. Le indica al ViewModel que la sugerencia fue aceptada, lo que
     *     dispara el registro de la decisión en Firebase
     *     (`Inventario/DecisionesCategoria/{key}`).
     */
    private fun aceptarSugerenciaCategoria(suggestion: CategorySuggestion) {
        val categoria = suggestion.categoria.trim()
        if (categoria.isBlank()) return

        if (!createProductCategoryOptions.any { it.equals(categoria, ignoreCase = true) }) {
            createProductCategoryOptions.add(categoria)
        }

        val controlTypeDetectado = when (suggestion.tipoControl) {
            TipoControlDetectado.UNIDAD -> CreateProductControlType.UNIDAD
            TipoControlDetectado.PESO -> CreateProductControlType.PESO
            TipoControlDetectado.LIQUIDO -> CreateProductControlType.LIQUIDO
            TipoControlDetectado.DESCONOCIDO -> null
        }

        val cambios = mutableMapOf<String, Any?>()
        var nuevoEstado = createProductUiState
        if (nuevoEstado.category != categoria) {
            nuevoEstado = nuevoEstado.copy(
                category = categoria,
                errors = nuevoEstado.errors - "category"
            )
            cambios["category"] = categoria
        }
        if (controlTypeDetectado != null && nuevoEstado.controlType != controlTypeDetectado) {
            // Convertimos las sugerencias de la IA a nuestro modelo de UI.
            val presentacionesIA = suggestion.presentacionesSugeridas.map { sug ->
                CreateProductPresentation(
                    id = sug.nombre.lowercase().replace(" ", "_") + "_" + UUID.randomUUID().toString().take(4),
                    name = sug.nombre,
                    equivalenceText = sug.equivalenciaUnidadBase.toString(),
                    salePriceText = "",
                    isAiSuggested = true
                )
            }

            nuevoEstado = nuevoEstado.copy(
                controlType = controlTypeDetectado,
                // Si la IA trajo presentaciones, las usamos; si no, fallback al default vacío.
                presentations = presentacionesIA.ifEmpty { sugerirPresentacionesIniciales(controlTypeDetectado, "") },
                mainPresentationId = presentacionesIA.firstOrNull()?.id ?: "",
                errors = nuevoEstado.errors - "controlType"
            )
            cambios["controlType"] = controlTypeDetectado.name
        }
        // Auto-rellenar el switch de receta con la detección de la IA. El
        // usuario siempre puede toggle el switch si la IA se equivocó: el
        // switch aparece junto con la tarjeta una vez READY.
        if (nuevoEstado.requiresPrescription != suggestion.requiereReceta) {
            nuevoEstado = nuevoEstado.copy(
                requiresPrescription = suggestion.requiereReceta
            )
            cambios["requiresPrescription"] = suggestion.requiereReceta
        }

        // --- NUEVO: ASIGNAR KEYWORDS DE IA AL ESTADO ---
        if (suggestion.keywords.isNotEmpty()) {
            nuevoEstado = nuevoEstado.copy(
                keywords = suggestion.keywords,
                // Por defecto, todas las sugerencias de la IA se seleccionan inicialmente
                selectedKeywords = suggestion.keywords.toSet()
            )
        }

        if (cambios.isNotEmpty() || suggestion.keywords.isNotEmpty()) {
            createProductUiState = nuevoEstado
        }
        categorySuggestionViewModel.aceptar()
    }

    /**
     * El usuario tocó "Cambiar manualmente". Se limpian AMBOS campos
     * (categoría + tipo de control) para que escriba/elija desde cero, y
     * el ViewModel cambia a modo MANUAL, que es el único estado donde la
     * UI muestra el text field + el selector de tipo tradicional.
     */
    private fun cambiarACategoriaManual() {
        createProductUiState = createProductUiState.copy(
            category = "",
            controlType = null,
            presentations = emptyList(),
            mainPresentationId = "",
            // En manual reseteamos a defaults seguros: receta=false (la mayoría
            // de productos OTC no la requiere) y activo=true (el comportamiento
            // natural al crear un producto nuevo).
            requiresPrescription = false,
            active = true,
            errors = createProductUiState.errors - "category" - "controlType"
        )
        categorySuggestionViewModel.cambiarAManual()
    }

    /**
     * Desde el modo manual, el usuario tocó "Volver a sugerencia IA":
     * limpia el campo y vuelve a consultar Gemini con el nombre actual.
     */
    private fun volverASugerenciaIaCategoria() {
        createProductUiState = createProductUiState.copy(
            category = "",
            errors = createProductUiState.errors - "category"
        )
        categorySuggestionViewModel.volverASugerenciaIA(
            nombre = createProductUiState.name,
            categoriasExistentes = createProductCategoryOptions.toList()
        )
    }



    private fun normalizeProductName(name: String): String {
        return name
            .trim()
            .lowercase(Locale.getDefault())
            .replace(Regex("\\s+"), " ")
    }

    private fun existeNombreDuplicadoCompose(name: String): Boolean {
        val normalized = normalizeProductName(name)
        if (normalized.isBlank()) return false
        return createProductExistingNormalizedNames.contains(normalized)
    }

    private fun buildNormalizedNameKey(normalizedName: String): String {
        return sanitizarNombreArchivo(normalizedName)
    }

    private fun programarValidacionNombreCompose(name: String) {
        createProductNameValidationJob?.cancel()

        val normalized = normalizeProductName(name)

        createProductUiState = createProductUiState.copy(
            errors = createProductUiState.errors - "name"
        )

        if (normalized.isBlank()) return

        createProductNameValidationJob = lifecycleScope.launch {
            delay(450)

            if (normalizeProductName(createProductUiState.name) != normalized) {
                return@launch
            }

            if (existeNombreDuplicadoCompose(createProductUiState.name)) {
                createProductUiState = createProductUiState.copy(
                    errors = createProductUiState.errors + (
                            "name" to "Ya existe un producto con este nombre."
                            )
                )
            }
        }
    }


    private fun programarValidacionCategoriaCompose(category: String) {
        createProductCategoryValidationJob?.cancel()

        val value = category.trim()

        createProductUiState = createProductUiState.copy(
            errors = createProductUiState.errors - "categoryExistsInfo"
        )

        if (value.isBlank()) return

        createProductCategoryValidationJob = lifecycleScope.launch {
            delay(450)

            if (createProductUiState.category.trim() != value) return@launch

            val existe = createProductCategoryOptions.any {
                it.equals(value, ignoreCase = true)
            }

            createProductUiState = createProductUiState.copy(
                errors = createProductUiState.errors + (
                        "categoryExistsInfo" to if (existe) {
                            "Categoría existente"
                        } else {
                            "Categoría nueva"
                        }
                        )
            )
        }
    }

    private fun obtenerLotesExistentesParaCrearProducto() {
        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_PRODUCTOS)
            .get()
            .addOnSuccessListener { snapshot ->
                val lotes = mutableListOf<String>()
                snapshot.children.forEach { productoSnapshot ->
                    val producto = productoSnapshot.toMoldeProductos() ?: return@forEach
                    producto.lotes.values.forEach { lote ->
                        val numero = lote.numero.trim()
                        if (numero.isNotBlank()) {
                            lotes.add(numero.uppercase(Locale.getDefault()))
                        }
                    }
                }

                createProductExistingLots.clear()
                createProductExistingLots.addAll(lotes.distinct().sorted())
            }
            .addOnFailureListener {
                createProductExistingLots.clear()
            }
    }



    private fun avanzarPasoCreateProduct() {
        val estadoActual = createProductUiState
        if (!validarPasoCreateProduct(estadoActual.currentStep)) return

        val siguientePaso = when (estadoActual.currentStep) {
                CreateProductStep.PRODUCTO -> CreateProductStep.LOTE_INICIAL
                CreateProductStep.LOTE_INICIAL -> CreateProductStep.PRESENTACIONES
                CreateProductStep.PRESENTACIONES -> CreateProductStep.RESUMEN
                CreateProductStep.RESUMEN -> CreateProductStep.RESUMEN
            }
        createProductUiState = if (
            estadoActual.currentStep == CreateProductStep.LOTE_INICIAL &&
            siguientePaso == CreateProductStep.PRESENTACIONES
        ) {
            // Al llegar a "Presentaciones", priorizamos las que sugirió la IA
            // (jerárquicas: caja → blister → tableta, etc.). Si la IA no
            // devolvió nada (sin internet / falló), caemos al fallback
            // hardcoded que solo arma una entrada según el modo de ingreso.
            val sugerenciasIA = categorySuggestionViewModel.state.value
                .suggestion?.presentacionesSugeridas.orEmpty()
            val nuevasPresentaciones = if (sugerenciasIA.isNotEmpty()) {
                mapearPresentacionesIA(sugerenciasIA)
            } else {
                presentacionesDesdeIngreso(estadoActual)
            }

            estadoActual.copy(
                currentStep = siguientePaso,
                presentations = nuevasPresentaciones,
                mainPresentationId = nuevasPresentaciones.firstOrNull()?.id.orEmpty()
            )
        } else {
            estadoActual.copy(
                currentStep = siguientePaso
            )
        }
        if (estadoActual.currentStep == CreateProductStep.PRODUCTO &&
            siguientePaso == CreateProductStep.LOTE_INICIAL
        ) {
            // Fix (M5): solo solicita la referencia si cambió la combinación
            // de nombre + categoría. Antes, ir y volver del paso LOTE_INICIAL
            // gatillaba una llamada de red por cada avance.
            val claveActual = "${estadoActual.name.trim().lowercase(Locale.getDefault())}|" +
                estadoActual.category.trim().lowercase(Locale.getDefault())
            if (claveActual != ultimaReferenciaSolicitada) {
                ultimaReferenciaSolicitada = claveActual
                productReferenceViewModel.startProductReferenceSearch(
                    productName = estadoActual.name,
                    category = estadoActual.category
                )
            }
        }

        if (siguientePaso == CreateProductStep.LOTE_INICIAL &&
            createProductExistingLots.isEmpty()
        ) {
            obtenerLotesExistentesParaCrearProducto()
        }
    }

    private fun retrocederPasoCreateProduct() {
        createProductUiState = createProductUiState.copy(
            currentStep = when (createProductUiState.currentStep) {
                CreateProductStep.PRODUCTO -> CreateProductStep.PRODUCTO
                CreateProductStep.LOTE_INICIAL -> CreateProductStep.PRODUCTO
                CreateProductStep.PRESENTACIONES -> CreateProductStep.LOTE_INICIAL
                CreateProductStep.RESUMEN -> CreateProductStep.PRESENTACIONES
            }
        )
    }

    private fun reiniciarFlujoComposeCrearProducto() {
        createProductLoading = false
        createProductSuccessSummary = null
        createProductShowReference = false
        createProductReferenceDismissed = false
        ultimoProductoCreadoCompose = null
        // Fix (M5): liberar la clave cacheada para que el siguiente producto
        // dispare una búsqueda fresca aunque coincida en nombre/categoría.
        ultimaReferenciaSolicitada = null
        productReferenceViewModel.reset()
        // Reinicia también el ciclo de sugerencias IA: limpia el set de
        // rechazos y cualquier sugerencia visible para el próximo producto.
        categorySuggestionViewModel.reset()
        labelScannerViewModel.reset()
        limpiarFormularioCompose()
    }

    private fun limpiarFormularioCompose() {
        val savedAnterior = createProductUiState.savedPresentationOptions
        createProductUiState = construirEstadoInicialCreateProduct().copy(
            savedPresentationOptions = savedAnterior
        )
        listaPresentaciones.clear()
        presentacionPrincipalElegidaManualmente = false
        // Fix (M1): los reseteos de variables heredadas se eliminaron junto
        // con sus declaraciones; ya no quedan campos huérfanos del flujo XML.
    }





    private fun actualizarCategoriasComposeDesdeLista() {
        val nuevasCategorias = listaCategoria.map { it.nombre.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        createProductCategoryOptions.clear()
        createProductCategoryOptions.addAll(nuevasCategorias)
    }








    private fun sugerirPresentacionesIniciales(
        controlType: CreateProductControlType?,
        receivedPresentation: String
    ): List<CreateProductPresentation> {
        val recibida = receivedPresentation.trim()

        val options = if (recibida.isNotBlank()) {
            mutableListOf(recibida)
        } else {
            // Sin sugerencias hardcoded: el usuario crea sus presentaciones manualmente.
            mutableListOf<String>()
        }

        return options.distinctBy { it.lowercase(Locale.getDefault()) }.map { option ->
            val isBaseSimple = option.equals("Unidad", ignoreCase = true) ||
                    option.equals("g", ignoreCase = true) ||
                    option.equals("mL", ignoreCase = true)

            // Intenta extraer un número de la cadena para la equivalencia (ej. "100 mL" -> 100)
            val autoEquivalence = if (!isBaseSimple && recibida.isBlank()) {
                Regex("\\d+").find(option)?.value?.toIntOrNull()?.toString() ?: ""
            } else {
                if (isBaseSimple) "1" else ""
            }

            CreateProductPresentation(
                id = option.lowercase(Locale.getDefault()).replace(" ", "_"),
                name = option,
                equivalenceText = autoEquivalence,
                salePriceText = ""
            )
        }
    }


    // Fix (M2): se eliminaron `inferirPresentacionLiquida`,
    // `inferirPresentacionPeso` e `inferirPresentacionUnidad`. Eran
    // helpers heredados del flujo XML antiguo y no se invocaban en
    // ningún punto del flujo Compose actual.

    // Fix (M2/M3): `equivalenciaPorDefecto` se eliminó. Su único caller
    // era `normalizarPresentacionesSegunLote`, también removida. Existe
    // una variante equivalente (`defaultEquivalenceForPresentation`)
    // dentro de `CreateProductScreen.kt` para la lógica de UI.


    // Fix (M3): `normalizarPresentacionesSegunLote` se eliminó. Dependía de
    // `state.receivedPresentation`, un campo del flujo viejo que nunca se
    // setea en el flujo Compose, así que su llamado siempre devolvía la
    // lista sin cambios.

    /**
     * Convierte las sugerencias jerárquicas de Gemini al modelo de UI.
     * Cada presentación queda marcada con `isAiSuggested = true` para que
     * la UI le agregue un ✨; ese flag se vuelve false cuando el usuario
     * la edita (entonces deja de "pertenecer" a la IA).
     */
    private fun mapearPresentacionesIA(
        sugerencias: List<com.app.administradorfarmadon.ActivityInventario.reference.PresentacionSugerida>
    ): List<CreateProductPresentation> {
        return sugerencias.map { sug ->
            val nombreLimpio = sug.nombre.trim()
            val idBase = nombreLimpio.lowercase(Locale.getDefault()).replace(" ", "_")
            CreateProductPresentation(
                id = idBase + "_" + UUID.randomUUID().toString().take(4),
                name = nombreLimpio,
                equivalenceText = sug.equivalenciaUnidadBase.toString(),
                salePriceText = "",
                isAiSuggested = true
            )
        }
    }

    private fun presentacionesDesdeIngreso(state: CreateProductState): List<CreateProductPresentation> {
        // Si ya hay presentaciones, no sobrescribir, solo actualizar la presentación de compra si existe
        if (state.presentations.isNotEmpty()) {
            // Buscar si ya existe una presentación que represente la unidad de compra (ej. "Unidad", "Caja")
            val existingCompra = when (state.stockEntryMode) {
                CreateProductStockEntryMode.UNIDAD -> state.presentations.find {
                    it.name.equals("Unidad", ignoreCase = true)
                }
                CreateProductStockEntryMode.CAJA -> state.presentations.find {
                    it.name.equals("Caja", ignoreCase = true)
                }
                CreateProductStockEntryMode.CAJA_CON_PAQUETES -> state.presentations.find {
                    it.name.equals("Caja", ignoreCase = true)
                }
                null -> null
            }

            // Si existe la presentación de compra, actualizar su equivalencia y precio (si es necesario)
            return if (existingCompra != null) {
                state.presentations.map { presentation ->
                    if (presentation.id == existingCompra.id) {
                        val newEquivalence = when (state.stockEntryMode) {
                            CreateProductStockEntryMode.UNIDAD -> "1"
                            CreateProductStockEntryMode.CAJA -> state.unitsPerBoxText
                            CreateProductStockEntryMode.CAJA_CON_PAQUETES -> {
                                val total = parseComposeNumber(state.packagesPerBoxText) *
                                        parseComposeNumber(state.unitsPerPackageText)
                                if (total > 0) total.toInt().toString() else presentation.equivalenceText
                            }
                            null -> presentation.equivalenceText
                        }
                        presentation.copy(
                            equivalenceText = newEquivalence.takeIf { it.isNotBlank() } ?: presentation.equivalenceText,
                            salePriceText = presentation.salePriceText.takeIf { it.isNotBlank() }
                                ?: state.presentations.firstOrNull()?.salePriceText.orEmpty()
                        )
                    } else {
                        presentation
                    }
                }
            } else {
                // Si no existe, agregamos la presentación de compra al principio de la lista
                val nuevaPresentacion = when (state.stockEntryMode) {
                    CreateProductStockEntryMode.UNIDAD -> CreateProductPresentation(
                        id = "unidad",
                        name = "Unidad",
                        equivalenceText = "1",
                        salePriceText = state.presentations.firstOrNull()?.salePriceText.orEmpty()
                    )
                    CreateProductStockEntryMode.CAJA -> CreateProductPresentation(
                        id = "caja",
                        name = "Caja",
                        equivalenceText = state.unitsPerBoxText,
                        salePriceText = state.presentations.firstOrNull()?.salePriceText.orEmpty()
                    )
                    CreateProductStockEntryMode.CAJA_CON_PAQUETES -> {
                        val total = parseComposeNumber(state.packagesPerBoxText) *
                                parseComposeNumber(state.unitsPerPackageText)
                        CreateProductPresentation(
                            id = "caja",
                            name = "Caja",
                            equivalenceText = if (total > 0) total.toInt().toString() else "",
                            salePriceText = state.presentations.firstOrNull()?.salePriceText.orEmpty()
                        )
                    }
                    null -> null
                }
                if (nuevaPresentacion != null) listOf(nuevaPresentacion) + state.presentations
                else state.presentations
            }
        }

        // Si no hay presentaciones, generar la lista inicial según el modo de ingreso (comportamiento anterior)
        return when (state.stockEntryMode) {
            CreateProductStockEntryMode.UNIDAD -> listOf(
                CreateProductPresentation("unidad", "Unidad", "1", "")
            )
            CreateProductStockEntryMode.CAJA -> listOf(
                CreateProductPresentation("caja", "Caja", state.unitsPerBoxText, "")
            )
            CreateProductStockEntryMode.CAJA_CON_PAQUETES -> {
                val total = parseComposeNumber(state.packagesPerBoxText) *
                        parseComposeNumber(state.unitsPerPackageText)
                listOf(
                    CreateProductPresentation(
                        "caja", "Caja",
                        if (total > 0) total.toInt().toString() else "",
                        ""
                    )
                )
            }
            null -> state.presentations
        }
    }

    /**
     * Fix (F1): chequeo puro (no muta `errors`) que decide si el botón
     * "Siguiente" / "Guardar" debe estar habilitado. Se usa solo para
     * activar/desactivar el botón en la UI; el control de errores y
     * mensajes lo sigue haciendo `validarPasoCreateProduct` al pulsar.
     *
     * Para el paso actual revisa los requisitos mínimos:
     *  - PRODUCTO: nombre + categoria + tipo de control y sin error de duplicado.
     *  - LOTE_INICIAL: lote único, vencimiento válido y no vencido ni fuera
     *    de rango, modo de ingreso configurado con cantidades > 0,
     *    aviso mínimo > 0 y costo de compra > 0.
     *  - PRESENTACIONES: al menos una presentación, todas con equivalencia
     *    y precio > 0, principal seleccionada si hay más de una.
     *  - RESUMEN: los tres anteriores en conjunto.
     */
    private fun puedeAvanzarPasoCreateProduct(
        estado: CreateProductState,
        lotesExistentes: Set<String>
    ): Boolean {
        return when (estado.currentStep) {
            CreateProductStep.PRODUCTO -> puedeAvanzarPasoProducto(estado)
            CreateProductStep.LOTE_INICIAL -> puedeAvanzarPasoLote(estado, lotesExistentes)
            CreateProductStep.PRESENTACIONES -> puedeAvanzarPasoPresentaciones(estado)
            CreateProductStep.RESUMEN ->
                puedeAvanzarPasoProducto(estado) &&
                puedeAvanzarPasoLote(estado, lotesExistentes) &&
                puedeAvanzarPasoPresentaciones(estado)
        }
    }

    private fun puedeAvanzarPasoProducto(estado: CreateProductState): Boolean {
        if (estado.name.trim().isBlank()) return false
        if (estado.category.trim().isBlank()) return false
        if (estado.controlType == null) return false
        if (!estado.errors["name"].isNullOrBlank()) return false
        return true
    }

    private fun puedeAvanzarPasoLote(
        estado: CreateProductState,
        lotesExistentes: Set<String>
    ): Boolean {
        val lote = estado.lotNumber.trim().uppercase(Locale.getDefault())
        if (lote.isBlank()) return false
        if (lotesExistentes.contains(lote)) return false

        if (!esVencimientoValido(estado.expirationDate)) return false
        if (estaVencido(estado.expirationDate)) return false
        if (!esVencimientoDentroDeRango(estado.expirationDate)) return false

        val modo = estado.stockEntryMode ?: return false
        if (!estado.stockEntryConfigured) return false
        val recibido = when (modo) {
            CreateProductStockEntryMode.UNIDAD ->
                parseComposeNumber(estado.receivedUnitsText)
            CreateProductStockEntryMode.CAJA ->
                parseComposeNumber(estado.boxesReceivedText) *
                    parseComposeNumber(estado.unitsPerBoxText)
            CreateProductStockEntryMode.CAJA_CON_PAQUETES ->
                parseComposeNumber(estado.boxesReceivedText) *
                    parseComposeNumber(estado.packagesPerBoxText) *
                    parseComposeNumber(estado.unitsPerPackageText)
        }
        if (recibido <= 0.0) return false

        // La validación ahora usa minimumStockUnits (el valor físico calculado)
        // en lugar de minimumStockText (que ya no se usa en el stepper moderno).
        if (estado.minimumStockUnits <= 0) return false
        
        // El cálculo del stock base para la alerta se hace internamente
        // al guardar, aquí solo validamos que la opción elegida sea positiva.

        val costo = estado.purchaseCost
            .replace("Bs", "", ignoreCase = true)
            .trim()
            .replace(",", ".")
            .toDoubleOrNull()
        if (costo == null || costo <= 0.0) return false

        return true
    }

    private fun puedeAvanzarPasoPresentaciones(estado: CreateProductState): Boolean {
        if (estado.presentations.isEmpty()) return false
        estado.presentations.forEach { presentation ->
            val equivalencia = presentation.equivalenceText
                .trim()
                .replace(",", ".")
                .toDoubleOrNull()
            if (equivalencia == null || equivalencia <= 0.0) return false

            val precio = presentation.salePriceText
                .replace("Bs", "", ignoreCase = true)
                .trim()
                .replace(",", ".")
                .toDoubleOrNull()
            if (precio == null || precio <= 0.0) return false
        }
        if (estado.presentations.size > 1 && estado.mainPresentationId.isBlank()) return false
        return true
    }

    private fun validarPasoCreateProduct(step: CreateProductStep): Boolean {
        val estado = createProductUiState
        val errors = estado.errors.toMutableMap()

        when (step) {
            CreateProductStep.PRODUCTO -> {
                if (estado.name.trim().isBlank()) {
                    errors["name"] = "Ingresa el nombre del producto"
                } else if (existeNombreDuplicadoCompose(estado.name)) {
                    errors["name"] = "Ya existe un producto con este nombre."
                } else {
                    errors.remove("name")
                }

                if (estado.category.trim().isBlank()) {
                    errors["category"] = "Ingresa o selecciona una categoria"
                } else {
                    errors.remove("category")
                }

                if (estado.controlType == null) {
                    errors["controlType"] = "Selecciona el tipo de control"
                } else {
                    errors.remove("controlType")
                }
            }

            CreateProductStep.LOTE_INICIAL -> {
                if (estado.lotNumber.trim().isBlank()) {
                    errors["lotNumber"] = "Ingresa el numero de lote"
                } else if (
                    createProductExistingLots.contains(
                        estado.lotNumber.trim().uppercase(Locale.getDefault())
                    )
                ) {
                    errors["lotNumber"] = "Ese lote ya existe"
                } else {
                    errors.remove("lotNumber")
                }

                // Fix (F3): antes la validación dejaba pasar años fuera de
                // rango (ej. "07/99" → 2099). `estaVencido` retornaba false
                // para esos años, así que el paso avanzaba con vencimientos
                // absurdos. Ahora se rechaza explícitamente con
                // `esVencimientoDentroDeRango`.
                if (!esVencimientoValido(estado.expirationDate)) {
                    errors["expirationDate"] = "Usa el formato MM/AA"
                } else if (estaVencido(estado.expirationDate)) {
                    errors["expirationDate"] = "La fecha ingresada ya vencio"
                } else if (!esVencimientoDentroDeRango(estado.expirationDate)) {
                    errors["expirationDate"] =
                        "El año no puede superar ${getMaxAllowedYear() % 100} (máx. ${getMaxAllowedYear()})"
                } else {
                    errors.remove("expirationDate")
                }

                if (estado.stockEntryMode == null || !estado.stockEntryConfigured) {
                    errors["stockEntryMode"] = "Selecciona como recibiste el producto"
                } else {
                    errors.remove("stockEntryMode")
                }

                when (estado.stockEntryMode) {
                    null -> Unit
                    CreateProductStockEntryMode.UNIDAD -> {
                        val quantity = parseComposeNumber(estado.receivedUnitsText)

                        if (quantity <= 0.0) {
                            errors["receivedUnits"] = "Ingresa la cantidad recibida"
                        } else {
                            errors.remove("receivedUnits")
                        }

                        errors.remove("boxesReceived")
                        errors.remove("unitsPerBox")
                        errors.remove("packagesPerBox")
                        errors.remove("unitsPerPackage")
                    }

                    CreateProductStockEntryMode.CAJA -> {
                        val boxes = parseComposeNumber(estado.boxesReceivedText)
                        val unitsPerBox = parseComposeNumber(estado.unitsPerBoxText)

                        if (boxes <= 0.0) {
                            errors["boxesReceived"] = "Ingresa cuantas cajas recibiste"
                        } else {
                            errors.remove("boxesReceived")
                        }

                        if (unitsPerBox <= 0.0) {
                            errors["unitsPerBox"] = "Ingresa cuanto trae cada caja"
                        } else {
                            errors.remove("unitsPerBox")
                        }

                        errors.remove("receivedUnits")
                        errors.remove("packagesPerBox")
                        errors.remove("unitsPerPackage")
                    }

                    CreateProductStockEntryMode.CAJA_CON_PAQUETES -> {
                        val boxes = parseComposeNumber(estado.boxesReceivedText)
                        val packagesPerBox = parseComposeNumber(estado.packagesPerBoxText)
                        val unitsPerPackage = parseComposeNumber(estado.unitsPerPackageText)

                        if (boxes <= 0.0) {
                            errors["boxesReceived"] = "Ingresa cuantas cajas recibiste"
                        } else {
                            errors.remove("boxesReceived")
                        }

                        if (packagesPerBox <= 0.0) {
                            errors["packagesPerBox"] = "Ingresa cuantos paquetes trae cada caja"
                        } else {
                            errors.remove("packagesPerBox")
                        }

                        if (unitsPerPackage <= 0.0) {
                            errors["unitsPerPackage"] = "Ingresa cuanto trae cada paquete"
                        } else {
                            errors.remove("unitsPerPackage")
                        }

                        errors.remove("receivedUnits")
                        errors.remove("unitsPerBox")
                    }
                }

                val minimumStockBase = calcularStockMinimoDesdeCompose(estado)
                // Total recibido en unidades base (g, mL o unidades).
                val recibidoEnBase = when (estado.stockEntryMode) {
                    CreateProductStockEntryMode.UNIDAD ->
                        parseComposeNumber(estado.receivedUnitsText)
                    CreateProductStockEntryMode.CAJA ->
                        parseComposeNumber(estado.boxesReceivedText) *
                            parseComposeNumber(estado.unitsPerBoxText)
                    CreateProductStockEntryMode.CAJA_CON_PAQUETES ->
                        parseComposeNumber(estado.boxesReceivedText) *
                            parseComposeNumber(estado.packagesPerBoxText) *
                            parseComposeNumber(estado.unitsPerPackageText)
                    null -> 0.0
                }

                // La validación ahora usa minimumStockUnits en lugar del campo de texto antiguo.
                if (estado.minimumStockUnits <= 0) {
                    errors["minimumStock"] = "Selecciona una cantidad de alerta"
                } else if (estado.stockEntryMode != null && minimumStockBase > recibidoEnBase) {
                    // Aunque el stepper pre-calcula opciones válidas, mantenemos esta
                    // seguridad lógica por si hay cambios de stock recibidos después.
                    errors["minimumStock"] = "La alerta no puede superar el stock recibido"
                } else {
                    errors.remove("minimumStock")
                }

                val purchaseCost = estado.purchaseCost
                    .replace("Bs", "", ignoreCase = true)
                    .trim()
                    .replace(",", ".")
                    .toDoubleOrNull()

                if (purchaseCost == null || purchaseCost <= 0.0) {
                    errors["purchaseCost"] = "Ingresa el costo de compra"
                } else {
                    errors.remove("purchaseCost")
                }

                errors.remove("receivedPresentation")
                errors.remove("receivedQuantity")
            }

            CreateProductStep.PRESENTACIONES -> {
                if (estado.presentations.isEmpty()) {
                    errors["presentations"] = "Agrega al menos una presentacion"
                } else {
                    errors.remove("presentations")
                }

                estado.presentations.forEach { presentation ->
                    val equivalence = presentation.equivalenceText
                        .trim()
                        .replace(",", ".")
                        .toDoubleOrNull()

                    if (equivalence == null || equivalence <= 0.0) {
                        errors["equivalence_${presentation.id}"] = "Equivalencia invalida"
                    } else {
                        errors.remove("equivalence_${presentation.id}")
                    }

                    val price = presentation.salePriceText
                        .replace("Bs", "", ignoreCase = true)
                        .trim()
                        .replace(",", ".")
                        .toDoubleOrNull()

                    if (price == null || price <= 0.0) {
                        errors["price_${presentation.id}"] = "Precio requerido"
                    } else {
                        errors.remove("price_${presentation.id}")
                    }
                }

                if (estado.presentations.size > 1 && estado.mainPresentationId.isBlank()) {
                    errors["mainPresentation"] = "Selecciona la presentacion principal"
                } else {
                    errors.remove("mainPresentation")
                }
            }

            CreateProductStep.RESUMEN -> {
                return validarPasoCreateProduct(CreateProductStep.PRODUCTO) &&
                        validarPasoCreateProduct(CreateProductStep.LOTE_INICIAL) &&
                        validarPasoCreateProduct(CreateProductStep.PRESENTACIONES)
            }
        }

        createProductUiState = estado.copy(errors = errors)

        return errors.none { entry ->
            when (step) {
                CreateProductStep.PRODUCTO ->
                    entry.key in setOf("name", "category", "controlType")

                CreateProductStep.LOTE_INICIAL ->
                    entry.key in setOf(
                        "lotNumber",
                        "expirationDate",
                        "stockEntryMode",
                        "receivedUnits",
                        "boxesReceived",
                        "unitsPerBox",
                        "packagesPerBox",
                        "unitsPerPackage",
                        "minimumStock",
                        "purchaseCost"
                    )

                CreateProductStep.PRESENTACIONES ->
                    entry.key == "presentations" ||
                            entry.key == "mainPresentation" ||
                            entry.key.startsWith("price_") ||
                            entry.key.startsWith("equivalence_")

                CreateProductStep.RESUMEN -> false
            }
        }
    }

    private fun esVencimientoValido(value: String): Boolean {
        val cleaned = value.trim()
        if (!cleaned.matches(Regex("\\d{2}/\\d{2}"))) return false
        val parts = cleaned.split("/")
        val month = parts.getOrNull(0)?.toIntOrNull() ?: return false
        return month in 1..12
    }

    private fun estaVencido(value: String): Boolean {
        if (!esVencimientoValido(value)) return false
        val parts = value.trim().split("/")
        val month = parts.getOrNull(0)?.toIntOrNull() ?: return false
        val year = parts.getOrNull(1)?.toIntOrNull() ?: return false
        val fullYear = 2000 + year
        // Fix (F3): el corto-circuito anterior `if (fullYear > maxAllowed) return false`
        // ocultaba años futuros lejanos como si fueran "no vencidos". Ese chequeo
        // se movió a `esVencimientoDentroDeRango`, que sí es consultado por la
        // validación del paso. Aquí `estaVencido` se mantiene fiel a su nombre.
        val expirationDate = runCatching {
            YearMonth.of(fullYear, month).atEndOfMonth()
        }.getOrNull() ?: return false
        return expirationDate.isBefore(LocalDate.now())
    }

    /**
     * Fix (F3): valida que el año del vencimiento no exceda el límite
     * permitido (año actual + 10). Antes este chequeo vivía dentro de
     * `estaVencido` retornando `false`, lo que dejaba pasar fechas como
     * "07/99" en el paso LOTE_INICIAL. Ahora es un chequeo explícito.
     */
    private fun esVencimientoDentroDeRango(value: String): Boolean {
        if (!esVencimientoValido(value)) return false
        val year = value.trim().split("/").getOrNull(1)?.toIntOrNull() ?: return false
        val fullYear = 2000 + year
        return fullYear <= getMaxAllowedYear()
    }

    private fun getMaxAllowedYear(): Int {
        return LocalDate.now().year + 10
    }

    private fun guardarProductoDesdeCompose() {
        if (!validarPasoCreateProduct(CreateProductStep.RESUMEN)) return

        if (existeNombreDuplicadoCompose(createProductUiState.name)) {
            createProductUiState = createProductUiState.copy(
                currentStep = CreateProductStep.PRODUCTO,
                errors = createProductUiState.errors + ("name" to "Ya existe un producto con este nombre.")
            )
            return
        }

        val producto = construirProductoDesdeCompose(createProductUiState)

        listaPresentaciones.clear()
        listaPresentaciones.addAll(producto.presentaciones)

        ultimoProductoCreadoCompose = producto
        createProductLoading = true
        createProductShowReference = false

        // Fix duplicados: chequeo final contra Firebase para cubrir el caso
        // en que la caché local `createProductExistingNormalizedNames` esté
        // vacía o desactualizada (p. ej. otro usuario creó el producto en
        // paralelo). Si existe, se cancela el guardado.
        verificarNombreDuplicadoEnFirebase(
            nombre = producto.nombre,
            onExiste = {
                createProductLoading = false
                createProductUiState = createProductUiState.copy(
                    currentStep = CreateProductStep.PRODUCTO,
                    errors = createProductUiState.errors + (
                        "name" to "Ya existe un producto con este nombre."
                    )
                )
            },
            onContinuar = {
                verificarProductoComposeAntesDeGuardar(producto)
            }
        )
    }

    /**
     * Fix duplicados: consulta `NombresProductos/{normalizedKey}` antes de
     * persistir el producto. Si la entrada existe, llama a `onExiste`; si
     * no, llama a `onContinuar`. En caso de fallo de red se asume que no
     * existe para no bloquear al usuario y se delega la deduplicación al
     * chequeo por `indice` que ya hace `verificarProductoComposeAntesDeGuardar`.
     */
    private fun verificarNombreDuplicadoEnFirebase(
        nombre: String,
        onExiste: () -> Unit,
        onContinuar: () -> Unit
    ) {
        val normalized = normalizeProductName(nombre)
        val key = buildNormalizedNameKey(normalized)
        if (normalized.isBlank() || key.isBlank()) {
            onContinuar()
            return
        }
        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_NOMBRES)
            .child(key)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    if (!createProductExistingNormalizedNames.contains(normalized)) {
                        createProductExistingNormalizedNames.add(normalized)
                    }
                    onExiste()
                } else {
                    onContinuar()
                }
            }
            .addOnFailureListener {
                onContinuar()
            }
    }

    private fun verificarProductoComposeAntesDeGuardar(producto: MoldeProductos) {
        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_PRODUCTOS)
            .child(producto.indice)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    createProductLoading = false

                    MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.titulo_verificacion_producto))
                        .setMessage("Este producto ya existe. Se generara un codigo nuevo para no sobrescribirlo.")
                        .setPositiveButton(getString(R.string.boton_continuar)) { _, _ ->
                            createProductLoading = true

                            val productoConIndiceUnico = producto.copy(
                                indice = generarIndiceProductoUnico(producto.indice)
                            )

                            ultimoProductoCreadoCompose = productoConIndiceUnico
                            guardarProductoFinalCompose(productoConIndiceUnico)
                        }
                        .setNegativeButton(getString(R.string.boton_cancelar)) { _, _ ->
                            createProductLoading = false
                        }
                        .show()
                } else {
                    guardarProductoFinalCompose(producto)
                }
            }
            .addOnFailureListener { e ->
                mostrarErrorGuardado(e.message)
            }
    }

    private fun guardarProductoFinalCompose(producto: MoldeProductos) {
        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_PRODUCTOS)
            .child(producto.indice)
            .setValue(producto)
            .addOnSuccessListener {
                createProductLoading = false
                ultimoProductoCreadoCompose = producto
                createProductSuccessSummary = construirResumenExitoCompose(producto)
                // Fix duplicados: tras guardar, persistir el nombre normalizado
                // en `NombresProductos` y agregarlo al set en memoria para que
                // futuras creaciones (esta sesión y la próxima) lo detecten
                // como duplicado. Antes solo se escribía en `Productos` y el
                // catálogo de nombres quedaba desactualizado.
                registrarNombreProductoPersistido(producto)
            }
            .addOnFailureListener { e ->
                mostrarErrorGuardado(e.message)
            }
    }

    /**
     * Fix duplicados: persiste el nombre del producto recién creado en
     * `NombresProductos/{normalizedKey}` y mantiene en memoria el set
     * `createProductExistingNormalizedNames` para que el chequeo local
     * detecte el duplicado al instante. Se ignora el resultado de la
     * escritura porque la prevención principal corre antes (consulta
     * directa en `verificarProductoComposeAntesDeGuardar`).
     */
    private fun registrarNombreProductoPersistido(producto: MoldeProductos) {
        val normalized = normalizeProductName(producto.nombre)
        if (normalized.isBlank()) return
        if (!createProductExistingNormalizedNames.contains(normalized)) {
            createProductExistingNormalizedNames.add(normalized)
        }
        val key = buildNormalizedNameKey(normalized).ifBlank { return }
        val registro = NombreProductos(
            id = key,
            nombre = producto.nombre,
            indice = producto.indice,
            normalizedName = normalized
        )
        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_NOMBRES)
            .child(key)
            .setValue(registro)
    }

    private fun generarIndiceProductoUnico(indiceBase: String): String {
        val baseNormalizada = sanitizarNombreArchivo(indiceBase).ifBlank { "producto" }
        val sufijo = UUID.randomUUID().toString().take(8).lowercase(Locale.getDefault())
        return "${baseNormalizada}_$sufijo"
    }

    private fun sanitizarNombreArchivo(nombre: String): String {
        return ProductUtils.sanitizarTexto(nombre)
    }

    /**
     * Fix (F2): serializa cantidades respetando si son enteras o decimales.
     * Antes se hacía `.toString()` sobre un `Int`, perdiendo los decimales.
     * Ahora, si la parte fraccionaria es 0, se serializa como entero
     * ("500" en vez de "500.0") para mantener compatibilidad con productos
     * antiguos; si es decimal real, se conserva ("500.5").
     */
    private fun formatearCantidadCompose(value: Double): String {
        return if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
    }

    private fun parseComposeNumber(value: String): Double {
        return value
            .trim()
            .replace("Bs", "", ignoreCase = true)
            .replace(",", ".")
            .toDoubleOrNull()
            ?: 0.0
    }

    /**
     * Fix (F2): antes devolvía `Int` y se truncaban los decimales para PESO
     * y LIQUIDO (ej. recibir "5000.5 mL" se guardaba como 5000 mL).
     * Ahora devuelve `Double` y `construirProductoDesdeCompose` se encarga
     * de formatearlo como texto sin perder precisión.
     */
    private fun calcularStockInicialDesdeCompose(state: CreateProductState): Double {
        val total = when (state.stockEntryMode) {
            null -> 0.0
            CreateProductStockEntryMode.UNIDAD -> {
                parseComposeNumber(state.receivedUnitsText)
            }

            CreateProductStockEntryMode.CAJA -> {
                parseComposeNumber(state.boxesReceivedText) *
                        parseComposeNumber(state.unitsPerBoxText)
            }

            CreateProductStockEntryMode.CAJA_CON_PAQUETES -> {
                parseComposeNumber(state.boxesReceivedText) *
                        parseComposeNumber(state.packagesPerBoxText) *
                        parseComposeNumber(state.unitsPerPackageText)
            }
        }

        return total.coerceAtLeast(0.0)
    }

    private fun calcularUnidadesPorPresentacionCompraDesdeCompose(state: CreateProductState): Int {
        val unidadesPorPresentacion = when (state.stockEntryMode) {
            null -> 1.0
            CreateProductStockEntryMode.UNIDAD -> {
                1.0
            }

            CreateProductStockEntryMode.CAJA -> {
                parseComposeNumber(state.unitsPerBoxText)
            }

            CreateProductStockEntryMode.CAJA_CON_PAQUETES -> {
                parseComposeNumber(state.packagesPerBoxText) *
                        parseComposeNumber(state.unitsPerPackageText)
            }
        }

        return unidadesPorPresentacion.toInt().coerceAtLeast(1)
    }

    /**
     * Calcula la cantidad base (mL, g o unidades) para el stock mínimo.
     * Ahora utiliza `minimumStockUnits` como fuente de verdad.
     */
    private fun calcularStockMinimoDesdeCompose(state: CreateProductState): Double {
        val unidadesFisicas = state.minimumStockUnits.coerceAtLeast(0)
        
        // Determinamos cuántas unidades base hay por cada unidad física.
        val factorBase = when (state.stockControlMode ?: getEffectiveStockControlMode(state)) {
            StockControlMode.INDIVISIBLE -> {
                // Si es indivisible (por envase), la unidad física es el frasco/caja.
                // Obtenemos el contenido de 1 envase según el modo de entrada.
                when (state.stockEntryMode) {
                    CreateProductStockEntryMode.UNIDAD -> 1.0 // Recibido como frascos sueltos
                    CreateProductStockEntryMode.CAJA -> parseComposeNumber(state.unitsPerBoxText)
                    CreateProductStockEntryMode.CAJA_CON_PAQUETES -> 
                        parseComposeNumber(state.packagesPerBoxText) * parseComposeNumber(state.unitsPerPackageText)
                    null -> 1.0
                }
            }
            StockControlMode.DIVISIBLE -> 1.0 // Ya es unidad base
        }

        return unidadesFisicas * factorBase
    }

    private fun construirProductoDesdeCompose(state: CreateProductState): MoldeProductos {
        val controlType = state.controlType ?: CreateProductControlType.UNIDAD

        val unidadBase = when (controlType) {
            CreateProductControlType.UNIDAD -> "Unidad"
            CreateProductControlType.PESO -> "g"
            CreateProductControlType.LIQUIDO -> "mL"
        }

        val unidadStockMinimo = when (controlType) {
            CreateProductControlType.UNIDAD -> "unidades"
            CreateProductControlType.PESO -> "g"
            CreateProductControlType.LIQUIDO -> "mL"
        }

        val presentaciones = state.presentations.map { presentation ->
            PresentacionProducto(
                nombre = presentation.name.trim(),
                cantidad = presentation.equivalenceText
                    .trim()
                    .replace(",", ".")
                    .toDoubleOrNull()
                    ?.toInt()
                    ?.coerceAtLeast(1)
                    ?: 1,
                precioventa = presentation.salePriceText
                    .replace("Bs", "", ignoreCase = true)
                    .trim()
                    .replace(",", ".")
                    .toDoubleOrNull()
                    ?: 0.0
            )
        }.toMutableList()

        // Fix (F2): `stockDisponible` y `stockMinimo` ahora son `Double` para
        // preservar decimales en PESO/LIQUIDO. Se serializan con
        // `formatearCantidadCompose` que omite el ".0" cuando el valor es
        // entero, manteniendo compatibilidad con productos existentes.
        val stockDisponible = calcularStockInicialDesdeCompose(state)
        val stockMinimo = calcularStockMinimoDesdeCompose(state)
        val unidadesPorPresentacionCompra =
            calcularUnidadesPorPresentacionCompraDesdeCompose(state)

        val mainPresentation = when {
            presentaciones.isEmpty() -> ""
            presentaciones.size == 1 -> presentaciones.first().nombre
            else -> {
                val mainFromState = state.presentations.firstOrNull {
                    it.id == state.mainPresentationId
                }?.name

                mainFromState
                    ?: presentaciones.firstOrNull()?.nombre
                    ?: ""
            }
        }

        val precioCompra = state.purchaseCost
            .replace("Bs", "", ignoreCase = true)
            .trim()
            .replace(",", ".")

        val vencimientoLimpio = state.expirationDate
            .trim()
            .replace("/", "_")

        val indiceBase =
            "${state.name.trim()}_${state.category.trim()}_$vencimientoLimpio"

        val indiceSanitizado = sanitizarNombreArchivo(indiceBase)

        val numeroLote = state.lotNumber.trim()
        val claveLote = sanitizarClaveLote(numeroLote)

        val primerLote = LoteProducto(
            numero = numeroLote,
            vencimiento = state.expirationDate.trim(),
            cantidad = stockDisponible,
            fecha = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(java.util.Date()),
            costoUltimoIngreso = precioCompra.toDoubleOrNull() ?: 0.0
        )

        return MoldeProductos(
            nombre = state.name.trim(),
            codigo = "",
            vencimiento = state.expirationDate.trim(),
            categoria = state.category.trim(),
            preciodecompra = precioCompra,
            cantidadinicial = formatearCantidadCompose(stockDisponible),
            stockminimo = formatearCantidadCompose(stockMinimo),
            stockMinimoContenedores = state.minimumStockUnits,
            unidadStockMinimo = unidadStockMinimo,
            unidadbase = unidadBase,
            tipoBaseInventario = PresentacionRules.tipoBaseInventarioDesdeUnidadBase(unidadBase),
            presentacionprincipal = mainPresentation,
            requierereceta = state.requiresPrescription,
            estadodelproducto = state.active,
            indice = indiceSanitizado,
            tienePresentaciones = presentaciones.isNotEmpty(),
            presentaciones = presentaciones,
            unidadesPorPresentacionCompra = unidadesPorPresentacionCompra,
            basePorUnidad = when (controlType) {
                CreateProductControlType.UNIDAD -> 0.0
                CreateProductControlType.PESO -> 1.0
                CreateProductControlType.LIQUIDO -> 1.0
            },
            referenceKeywords = state.selectedKeywords.toList(), // Guardar solo las seleccionadas
            imagenUrl = "",
            lotes = mapOf(claveLote to primerLote)
        )
    }

    private fun sanitizarClaveLote(numero: String): String {
        return ProductUtils.sanitizarTexto(numero).ifBlank { "L001" }
    }

    private fun construirResumenExitoCompose(producto: MoldeProductos): ProductCreatedSummary {
        val precioPrincipal = producto.presentaciones.firstOrNull { presentacion ->
            presentacion.nombre.equals(producto.presentacionprincipal, ignoreCase = true)
        }?.precioventa ?: producto.presentaciones.firstOrNull()?.precioventa ?: 0.0
        val stockTexto = when {
            producto.cantidadinicial.isBlank() -> "0"
            else -> "${producto.cantidadinicial} ${unidadResumenSegunTipo(producto.tipoBaseInventario)}"
        }
        val loteInicial = producto.lotes.values.firstOrNull()

        return ProductCreatedSummary(
            indice = producto.indice,
            name = producto.nombre,
            category = producto.categoria,
            mainPrice = MonedaHelper.formatearSimple(precioPrincipal),
            stockAvailable = stockTexto,
            lotNumber = loteInicial?.numero.orEmpty().ifBlank { "Sin lote" },
            expirationDate = loteInicial?.vencimiento.orEmpty().ifBlank { producto.vencimiento }
        )
    }

    private fun unidadResumenSegunTipo(tipoBaseInventario: String): String {
        return when (tipoBaseInventario.trim().lowercase(Locale.getDefault())) {
            "peso" -> "g"
            "liquido" -> "mL"
            else -> "unidades"
        }
    }

    private fun abrirProductoRecienCreado() {
        val indice = createProductSuccessSummary?.indice.orEmpty()
        if (indice.isBlank()) return
        startActivity(
            android.content.Intent(this, EditarProductodelInventario::class.java).apply {
                putExtra("indice", indice)
            }
        )
    }

    private fun guardarReferenciaSugeridaEnProducto(reference: ProductReference) {
        val producto = ultimoProductoCreadoCompose ?: return
        val indice = producto.indice.ifBlank { createProductSuccessSummary?.indice.orEmpty() }
        if (indice.isBlank()) return

        val updates = hashMapOf<String, Any>(
            "referenceCommonUse" to reference.commonUse.orEmpty(),
            "referenceUseCases" to reference.useCases,
            "referenceHowToUse" to reference.howToUse.orEmpty(),
            "referenceNotRecommendedFor" to reference.notRecommendedFor,
            "referenceKeywords" to reference.searchKeywords,
            "referenceWarnings" to reference.warnings,
            "referenceSourceName" to reference.sourceName,
            "referenceSourceUrl" to reference.sourceUrl.orEmpty(),
            "referenceRxcui" to reference.rxcui.orEmpty(),
            "referenceNdc" to reference.ndc.orEmpty(),
            "referenceMatchedName" to reference.matchedName.orEmpty(),
            "referenceConfidence" to reference.confidence,
            "referenceLanguage" to reference.language
        )

        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_PRODUCTOS)
            .child(indice)
            .updateChildren(updates)
            .addOnSuccessListener {
                ultimoProductoCreadoCompose = producto.copy(
                    referenceCommonUse = reference.commonUse.orEmpty(),
                    referenceUseCases = reference.useCases,
                    referenceHowToUse = reference.howToUse.orEmpty(),
                    referenceNotRecommendedFor = reference.notRecommendedFor,
                    referenceKeywords = reference.searchKeywords,
                    referenceWarnings = reference.warnings,
                    referenceSourceName = reference.sourceName,
                    referenceSourceUrl = reference.sourceUrl.orEmpty(),
                    referenceRxcui = reference.rxcui.orEmpty(),
                    referenceNdc = reference.ndc.orEmpty(),
                    referenceMatchedName = reference.matchedName.orEmpty(),
                    referenceConfidence = reference.confidence,
                    referenceLanguage = reference.language
                )
                productReferenceViewModel.setEditedReference(reference)
                Toast.makeText(
                    this,
                    "Referencia guardada correctamente",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    error.message ?: "No se pudo guardar la referencia",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun suggestUniqueLotNumberFromActivity(
        productName: String,
        currentLot: String
    ): String {
        val prefix = productName.trim()
            .firstOrNull { it.isLetterOrDigit() }
            ?.uppercaseChar()
            ?.toString()
            ?: currentLot.trim().firstOrNull()?.uppercaseChar()?.toString()
            ?: "L"

        val suffixDigits = currentLot.trim().takeLastWhile { it.isDigit() }
        val width = suffixDigits.length.coerceAtLeast(3)
        var nextNumber = suffixDigits.toIntOrNull()?.plus(1) ?: 1
        var candidate: String

        do {
            candidate = prefix + nextNumber.toString().padStart(width, '0')
            nextNumber++
        } while (createProductExistingLots.contains(candidate.uppercase(Locale.getDefault())))

        return candidate
    }

    private fun aplicarStatusBarBlanca() {
        if (statusBarColorOriginal == null) {
            statusBarColorOriginal = window.statusBarColor
        }

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        if (statusBarLightOriginal == null) {
            statusBarLightOriginal = insetsController.isAppearanceLightStatusBars
        }

        window.statusBarColor = Color.WHITE
        insetsController.isAppearanceLightStatusBars = true
    }

    private fun restaurarStatusBarOriginal() {
        statusBarColorOriginal?.let { color ->
            window.statusBarColor = color
        }

        statusBarLightOriginal?.let { lightStatusBar ->
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
                lightStatusBar
        }
    }




    private fun obtenerCategoriasDeProductos() {
        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_CATEGORIAS)
            .get()
            .addOnSuccessListener { snapshot ->
                listaCategoria.clear()

                snapshot.children.forEach { data ->
                    val categoria = data.getValue(CategoriaProductos::class.java)
                    if (categoria != null) {
                        listaCategoria.add(categoria)
                    }
                }

                actualizarCategoriasComposeDesdeLista()
            }
    }

    // Fix (M1): se eliminó la escritura redundante en `listaNombreAutoComplete`
    // (campo eliminado). El único consumidor real es
    // `createProductExistingNormalizedNames`, usado por la validación de
    // nombre duplicado.
    private fun obtenerNombresDeProductos() {
        FirebaseDatabase.getInstance()
            .getReference(PATH_INVENTARIO)
            .child(PATH_NOMBRES)
            .get()
            .addOnSuccessListener { snapshot ->
                createProductExistingNormalizedNames.clear()

                snapshot.children.forEach { data ->
                    val nombreProducto = data.getValue(NombreProductos::class.java)
                    if (nombreProducto != null) {
                        val normalized = nombreProducto.normalizedName
                            ?.takeIf { it.isNotBlank() }
                            ?: normalizeProductName(nombreProducto.nombre)

                        if (normalized.isNotBlank()) {
                            createProductExistingNormalizedNames.add(normalized)
                        }
                    }
                }
            }
            .addOnFailureListener {
                createProductExistingNormalizedNames.clear()
            }
    }



    // ---------------------------------------------------------------------------------------------
    // SUGERENCIAS GLOBALES
    // ---------------------------------------------------------------------------------------------







    private fun aplicarDestelloRojoDesvanecido(view: View) {
        val width = view.width.takeIf { it > 0 } ?: view.measuredWidth
        val height = view.height.takeIf { it > 0 } ?: view.measuredHeight

        if (width <= 0 || height <= 0) {
            view.post { aplicarDestelloRojoDesvanecido(view) }
            return
        }

        val overlay = ColorDrawable(0x66D93025.toInt()).apply {
            alpha = 0
            setBounds(0, 0, width, height)
        }

        view.overlay.add(overlay)

        ValueAnimator.ofInt(110, 0).apply {
            duration = 650L
            addUpdateListener { animator ->
                overlay.alpha = animator.animatedValue as Int
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.overlay.remove(overlay)
                }

                override fun onAnimationCancel(animation: Animator) {
                    view.overlay.remove(overlay)
                }
            })
            start()
        }
    }


    private fun mostrarErrorGuardado(mensaje: String?) {
        createProductLoading = false

        Toast.makeText(
            this,
            mensaje ?: getString(R.string.error_generico_guardar),
            Toast.LENGTH_LONG
        ).show()
    }







}

data class CategoriaProductos(val id: String="",val nombre: String="",var indice: String? = null,var color: String? = null)

data class NombreProductos(
    val id: String = "",
    val nombre: String = "",
    var indice: String? = null,
    val normalizedName: String? = null
)
