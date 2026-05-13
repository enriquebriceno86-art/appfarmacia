package com.app.administradorfarmadon.ActivityFragmentos.Fragmentos

import com.app.administradorfarmadon.ActivityFragmentos.showElegantemente
import com.app.administradorfarmadon.ActivityFragmentos.hideElegantemente
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import com.app.administradorfarmadon.ClasesDatabase.CalculadoraVentasHelper
import com.app.administradorfarmadon.ClasesDatabase.AccesoFieldRules
import com.app.administradorfarmadon.ClasesDatabase.FechaHoraServidorHelper
import com.app.administradorfarmadon.ClasesDatabase.MovimientoInventarioLogger
import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import com.app.administradorfarmadon.ClasesDatabase.CierreTurnoPdfGenerator
import com.app.administradorfarmadon.ClasesDatabase.PresentacionHelper
import com.app.administradorfarmadon.ClasesDatabase.SessionManager
import com.app.administradorfarmadon.ClasesDatabase.MovimientoLogger
import com.app.administradorfarmadon.ClasesDatabase.SesionUnicaManager
import com.app.administradorfarmadon.ClasesDatabase.PresentacionesTiendaConfigManager
import com.app.administradorfarmadon.ClasesDatabase.obtenerDoubleFlexible
import com.app.administradorfarmadon.ClasesDatabase.obtenerTexto
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.widget.NestedScrollView
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.ActivityFragmentos.Reciclers.AdapterCategorias
import com.app.administradorfarmadon.ActivityFragmentos.Reciclers.AdapterMetodosPagoCobro
import com.app.administradorfarmadon.ActivityFragmentos.Reciclers.AdapterMetodosPagoFlujo
import com.app.administradorfarmadon.ActivityFragmentos.Reciclers.AdapterPagoMixto
import com.app.administradorfarmadon.ActivityFragmentos.Reciclers.AdapterProductosCatalogo
import com.app.administradorfarmadon.ActivityFragmentos.Reciclers.AdapterRecyclerProductosCaja
import com.app.administradorfarmadon.ActivityFragmentos.Reciclers.AdapterResultadosBusquedaCaja
import com.app.administradorfarmadon.ActivityFragmentos.Reciclers.CategoriaProductos
import com.app.administradorfarmadon.ActivityFragmentos.Reciclers.ItemPagoMixtoUi
import com.app.administradorfarmadon.ActivityFragmentos.ViewModels.CajaFeedbackViewModel
import com.app.administradorfarmadon.ActivityInventario.ProductUtils
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteConsumoResolucion
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteConsumoRules
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteProducto
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.PresentacionProducto
import com.app.administradorfarmadon.ActivityInventario.domain.PresentacionRules
import com.app.administradorfarmadon.ActivitysCaja.ActivitySeleccionarProductoSustitucion
import com.app.administradorfarmadon.ActivitysCaja.CajaEstadoUiBuilder
import com.app.administradorfarmadon.ActivitysCaja.CajaEnUsoSyncManager
import com.app.administradorfarmadon.ActivitysCaja.CajaOverlayCoordinator
import com.app.administradorfarmadon.ActivitysCaja.CajaOverlayUiBuilder
import com.app.administradorfarmadon.ActivitysCaja.CajaService
import com.app.administradorfarmadon.ActivitysCaja.CajaControlAsistidoManager
import com.app.administradorfarmadon.ActivitysCaja.CajaSupervisionManager
import com.app.administradorfarmadon.ActivitysCaja.CajaTurnoRules
import com.app.administradorfarmadon.ActivitysCaja.CajaVerificacionTurnoMapper
import com.app.administradorfarmadon.ActivitysCaja.CajaVerificacionUiRules
import com.app.administradorfarmadon.ActivitysCaja.ClasesCaja.InfoVenta
import com.app.administradorfarmadon.ActivitysCaja.LoteConsumoDetalleCaja
import com.app.administradorfarmadon.ActivitysCaja.ProductoCaja
import com.app.administradorfarmadon.ActivityFragmentos.ActivityFragmentos
import com.app.administradorfarmadon.ActivitysPerfilItem.MetodoPagoConfig
import com.app.administradorfarmadon.AdaptersCaja.*
import com.app.administradorfarmadon.MainActivity
import com.app.administradorfarmadon.R
import com.app.administradorfarmadon.databinding.BottomSheetConfirmarDevolucionBinding
import com.app.administradorfarmadon.databinding.BottomSheetVentasDevolucionBinding
import com.app.administradorfarmadon.databinding.FragmentFragmentoCajaBinding
import com.app.administradorfarmadon.databinding.ItemProductoDevolucionBinding
import com.app.administradorfarmadon.databinding.ItemVentaDevolucionBinding
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import androidx.recyclerview.widget.ItemTouchHelper
import com.app.administradorfarmadon.databinding.DialogResumenTurnoMejoradoBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max



class FragmentoCaja : Fragment()
{
    companion object {
        private const val MENU_CERRAR_TURNO_ID = 9101
        private const val MENU_REGISTRAR_EGRESO_ID = 9102
        private const val MENU_CAMBIAR_CAJA_ID = 9103
        private const val MENU_RESUMEN_TURNO_ID = 9104
        private const val MENU_DEVOLUCION_ID = 9105
        private const val MENU_PAUSAR_VENTA_ID = 9106
        private const val MENU_VER_EN_ESPERA_ID = 9107
        private const val PORCENTAJE_ALERTA_DESCUADRE = 0.10
        private const val UMBRAL_MINIMO_ALERTA_DESCUADRE = 1.0
        private const val UMBRAL_STOCK_CRITICO_CIERRE = 5
        private const val UMBRAL_CONFIRMACION_MONTO_APERTURA = 10000.0
        private const val UMBRAL_CONFIRMACION_MONTO_CIERRE = 10000.0
        private const val ALERTA_VENTAS_EN_ESPERA = 3
        private const val MAX_VENTAS_EN_ESPERA = 5
        private const val BLOQUEO_VENTA_EN_ESPERA_MS = 45_000L
        private const val DEBOUNCE_VERIFICACION_TURNO_MS = 350L
        private const val DEBOUNCE_BUSQUEDA_PRODUCTOS_MS = 140L
        private const val TIMEOUT_ACTUALIZANDO_CAJA_MS = 20_000L
        private const val TIMEOUT_VENTA_EN_PROCESO_MS = 60_000L
        private const val TIMEOUT_OPERACION_IDEMPOTENTE_VENTA_MS = 120_000L
        private const val TIMEOUT_PRE_REGISTRO_REVERSION_MS = 120_000L
        private const val TIMEOUT_LOADING_INICIAL_CAJA_MS = 5_000L
        private const val TIMEOUT_VALIDACION_TIEMPO_MS = 10_000L
        private const val CLOUDINARY_CLOUD_NAME = "dluvatyh7"
        private const val CLOUDINARY_UPLOAD_PRESET = "comprobantes"
    }

    private var watcherDocumentoCliente: TextWatcher? = null
    private var watcherDocumentoClienteInline: TextWatcher? = null

    private var tokenOverlayActual = 0L
    private var tokenVentaActual = 0L

    // Modelos y Adaptadores extraídos a AdaptersCaja/ para reducir complejidad

    // Tablet listeners
    private var inventarioProductosRefListener: DatabaseReference? = null
    private var inventarioProductosValueListener: ValueEventListener? = null
    private var _binding: FragmentFragmentoCajaBinding? = null
    private val binding get() = _binding!!
    private var dialogoProgresoVenta: AlertDialog? = null
    private var dialogoCobroActual: BottomSheetDialog? = null
    private var dialogoDevolucionActual: BottomSheetDialog? = null
    private var dialogoResolucionStockActual: AlertDialog? = null
    private var dialogoRegistrarEgresoActual: AlertDialog? = null
    private var dialogoOpcionesComprobanteEgresoActual: AlertDialog? = null
    private var dialogoConfirmacionEgresoActual: AlertDialog? = null
    private var dialogoVentasEnEsperaActual: BottomSheetDialog? = null
    private var dialogoOpcionesVentaEnEsperaActual: AlertDialog? = null
    private var dialogoNotaVentaEnEsperaActual: AlertDialog? = null
    private var dialogoSelectorCajerasActual: BottomSheetDialog? = null
    private var dialogoCambioPresentacionActual: BottomSheetDialog? = null
    private var dialogoDescuentoActual: AlertDialog? = null
    private var dialogoSalidaSupervisionActual: AlertDialog? = null
    private var popupMenuCajaActivo: PopupMenu? = null
    private var snackbarEliminarProductoActivo: Snackbar? = null
    private var onProductoSustitutoSeleccionadoDevolucion: ((String) -> Unit)? = null

    private lateinit var adapter: AdapterRecyclerProductosCaja
    private val feedbackVm: CajaFeedbackViewModel by activityViewModels()
    private val listaProductosCaja = mutableListOf<ProductoCaja>()
    
    // HUMANO: Caché de stock y precios en tiempo real para validaciones instantáneas
    private val cacheStockInventario = mutableMapOf<String, Int>()
    private val cachePreciosInventario = mutableMapOf<String, Map<String, Double>>()
    private val listenersStockProductos = mutableMapOf<String, ValueEventListener>()

    private data class CacheTrazabilidad(val itemId: String, val cantidad: Int, val resultado: String?)
    private val cacheTrazabilidadDevolucion = mutableMapOf<String, CacheTrazabilidad>()
    // Tablet
    private var adapterCategorias: AdapterCategorias? = null
    private var adapterProductos: AdapterProductosCatalogo? = null
    private val listaProductosTablet = mutableListOf<MoldeProductos>()
    private val listaOriginalProductosTablet = mutableListOf<MoldeProductos>()
    private var categoriaSeleccionadaActualTablet = "Todos"

    // Búsqueda inline (móvil): chips + resultados dentro del Fragment de Caja
    private var adapterChipsInline: AdapterCategorias? = null
    private var adapterResultadosInline: AdapterResultadosBusquedaCaja? = null
    private val listaResultadosInline = mutableListOf<MoldeProductos>()
    private val listaCategoriasInline = mutableListOf("Todos")
    private var categoriaSeleccionadaInline = "Todos"
    private var modoBusquedaActivo = false
    private var categoriasInlineExpandidas = false
    private var busquedaInlineConfigurada = false

    private var popupResultadosBusqueda: PopupWindow? = null
    private var rvResultadosDropdown: RecyclerView? = null
    private var tvSinResultadosDropdown: View? = null

    private var connectionRef: DatabaseReference? = null
    private var connectionListener: ValueEventListener? = null
    private var internetEstabaConectado = true
    private var internetRecienRecuperado = false

    private var cajaRefListener: DatabaseReference? = null
    private var cajaValueListener: ValueEventListener? = null
    private var inventarioCajaRefListener: DatabaseReference? = null
    private var inventarioCajaValueListener: ValueEventListener? = null
    private var turnoCajaRefListener: DatabaseReference? = null
    private var turnoCajaValueListener: ValueEventListener? = null

    private var ventaEnProceso = false
    private var actualizandoCaja = false
    private var operacionVentaEnEsperaEnCurso = false
    private var dialogoProgresoVentaEnEspera: AlertDialog? = null
    private var soyYoElQueCobra = false

    private var modoCobroTablet = false
    private var revirtiendoCheckoutPorCajaVacia = false

    private val database by lazy {
        FirebaseDatabase.getInstance()
    }

    private var estadoCobroListener: ValueEventListener? = null
    private var estadoCobroRefGlobal: DatabaseReference? = null
    private var ventasEnEsperaListener: ValueEventListener? = null
    private var ventasEnEsperaRef: DatabaseReference? = null
    private var cajaEnUsoSyncRefListener: DatabaseReference? = null
    private var cajaEnUsoSyncValueListener: ValueEventListener? = null

    // #9: Listener en tiempo real para total vendido del turno activo
    private var totalTurnoRef: DatabaseReference? = null
    private var totalTurnoListener: ValueEventListener? = null

    private var tipoComprobanteSeleccionado = "BOLETA"

    private data class SolicitudMetodosPagoActivos(
        val onSuccess: (List<MetodoPagoConfig>) -> Unit,
        val onError: (String) -> Unit
    )

    private var nombreClienteVenta = ""
    private var documentoClienteVenta = ""
    private var razonSocialClienteVenta = ""
    private var correoClienteVenta = ""
    private var formularioFacturaExpandido = false
    private var sincronizandoSwitchFactura = false
    private var pasoCobroActual = 1
    private var metodoCobroSeleccionado: MetodoPagoConfig? = null
    private var metodosPagoCobroActivos: List<MetodoPagoConfig> = emptyList()
    private var cacheMetodosPagoActivos: List<MetodoPagoConfig>? = null
    private var cargandoMetodosPagoActivos = false
    private val solicitudesMetodosPagoPendientes = mutableListOf<SolicitudMetodosPagoActivos>()
    private var totalCobroActual = 0.0
    private var vistaDetalleCobroInline: View? = null

    private var handlerBusquedaDocumento = Handler(Looper.getMainLooper())
    private var runnableBusquedaDocumento: Runnable? = null
    private var runnableBusquedaProductosTablet: Runnable? = null
    private var runnableBusquedaProductosInline: Runnable? = null
    private var ultimoDocumentoBuscado = ""
    private val handlerSeguridadOperaciones = Handler(Looper.getMainLooper())
    private var runnableResetActualizandoCaja: Runnable? = null
    private var runnableResetVentaEnProceso: Runnable? = null
    private var runnableResetLoadingInicialCaja: Runnable? = null
    private var runnableVerificarTurno: Runnable? = null

    private var runnableTimeoutValidacion: Runnable? = null
    private var runnableTimeoutLoadingGenerico: Runnable? = null
    private var runnableTimeoutCierreTurno: Runnable? = null
    private val handlerTimeoutValidacion = Handler(Looper.getMainLooper())

    // Manejo seguro del teclado en la pantalla de cobro
    private val handlerMontoRecibido = Handler(Looper.getMainLooper())
    private var runnableMontoRecibido: Runnable? = null


    private var clienteAutocompletadoActivo = false
    private var idTurnoActivo = ""
    private var fechaTurnoActivo = ""
    private var montoAperturaTurno = 0.0
    private var turnoAbierto = false
    private var cantidadVentasEnEspera = 0
    private var numeroVentasTurnoActual = 0L
    private var cerrandoTurno = false
    private var turnoPendienteDiaAnterior = false
    private var cantidadTurnosPendientes = 0
    private var fechaTurnoPendienteAvisada = ""
    private var verificacionTurnoDisponible = false
    private var llaveResguardoTurnoActiva = ""
    private var verificacionTurnoEnCurso = false
    private var verificacionTurnoPendiente = false
    private var reconciliacionPreRegistroEnCurso = false
    private var claveReconciliacionPreRegistro = ""
    private var limpiezaResiduoTurnoAnteriorEnCurso = false
    private var supervisorEnMiCajaId = ""
    private var supervisorEnMiCajaNombre = ""
    private var editorControlCajaId = ""
    private var editorControlCajaNombre = ""
    private var modoRescateCajaBloqueadaActivo = false
    private var idCajaRescateBloqueada = ""
    private var nombreCajaRescateBloqueada = ""
    private var nombreCuentaRescateBloqueada = ""
    private var ultimoAvisoModoRescateMs = 0L
    private var supervisionPresenciaInicializada = false
    private var ultimaClavePresenciaSupervision = ""
    private var runnableOcultarSplashPresenciaSupervision: Runnable? = null
    private var comprobanteEgresoUri: Uri? = null
    private var comprobanteEgresoCameraUri: Uri? = null
    private var timeChangeReceiver: BroadcastReceiver? = null
    private var comprobanteEgresoPreview: ImageView? = null
    private var comprobanteEgresoHint: TextView? = null
    private var comprobanteEgresoHintContainer: View? = null
    private var comprobanteEgresoQuitar: View? = null
    private var tokenSubidaComprobanteEgreso = 0L
    private var dialogErrorHoraServidor: AlertDialog? = null
    private var ultimaFechaHoraCajaValidada: FechaHoraServidorHelper.FechaHoraOficial? = null
    private var loadingInicialCajaActivo = false
    private var loadingOperacionCajaActivo = false
    private var cargaInicialCajaCompletada = false
    private var turnoInicialCajaListo = false
    private var productosInicialCajaListos = false
    private var cierreForzadoPendienteActivo = false
    private var validandoStockFinalEnCurso = false
    private var relojDesincronizado = false

    private data class ClienteFormRefs(
        val btnBoleta: MaterialButton?,
        val btnFactura: MaterialButton?,
        val inputDocumento: TextInputLayout?,
        val etDocumento: TextInputEditText?,
        val inputNombrePrincipal: TextInputLayout?,
        val etNombrePrincipal: TextInputEditText?,
        val inputRazonSocial: TextInputLayout?,
        val etRazonSocial: TextInputEditText?,
        val inputCorreo: TextInputLayout?,
        val etCorreo: TextInputEditText?
    )

    private enum class TipoConflictoStockFinal {
        AJUSTAR_CANTIDAD,
        CAMBIAR_PRESENTACION,
        ELIMINAR_PRODUCTO
    }

    private data class ConflictoStockFinal(
        val producto: ProductoCaja,
        val tipo: TipoConflictoStockFinal,
        val stockDisponibleParaEsteItem: Int,
        val cantidadSugerida: Int = 0,
        val opcionPresentacionSugerida: OpcionCorreccionPresentacionCaja? = null
    )

    private enum class TipoPresenciaSupervisionUi {
        SUPERVISOR_EN_CAJA_AJENA,
        SUPERVISOR_EN_MI_CAJA,
        RESCATE_CAJA_BLOQUEADA
    }

    private data class PresenciaSupervisionUiModel(
        val tipo: TipoPresenciaSupervisionUi,
        val clave: String,
        val tituloBanner: String,
        val detalleBanner: String,
        val fondoBanner: String,
        val textoBanner: String,
        val tituloSplash: String,
        val detalleSplash: String,
        val fondoSplash: String,
        val fondoBadgeSplash: String,
        val fondoIconoSplash: String,
        val mostrarBotonCerrar: Boolean
    )

    private data class EstadoControlAsistidoUiModel(
        val editorId: String,
        val editorNombre: String,
        val tieneControlEsteUsuario: Boolean,
        val bloqueadoPorOtroUsuario: Boolean,
        val puedeTomarControl: Boolean,
        val textoAccionBanner: String?,
        val tituloOverlay: String?,
        val detalleOverlay: String?
    )

    private data class CajaSelectorUiModel(
        val id: String,
        val nombre: String,
        val rol: String,
        val accesoActivo: Boolean,
        val tieneTurnoAbierto: Boolean,
        val esCajaPropia: Boolean,
        val supervisorActivoId: String = "",
        val supervisorActivoNombre: String = "",
        val supervisionOcupadaPorOtro: Boolean = false
    ) {
        val permiteModoRescate: Boolean
            get() = !accesoActivo && tieneTurnoAbierto

        val supervisorActivoVisible: String
            get() = supervisorActivoNombre.ifBlank { "otro supervisor" }
    }

    // Variables de sesión global
    private var idCajera: String = ""
    private var nombreCajera: String = ""

    private val cameraComprobanteEgresoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && comprobanteEgresoCameraUri != null) {
                comprobanteEgresoUri = comprobanteEgresoCameraUri
                actualizarPreviewComprobanteEgreso()
            }
        }

    private val galleryComprobanteEgresoLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                comprobanteEgresoUri = it
                actualizarPreviewComprobanteEgreso()
            }
        }

    private val cameraPermissionComprobanteEgresoLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                abrirCamaraParaComprobanteEgreso()
            } else {
                mostrarToastSeguro("Se necesita permiso de cámara para tomar la foto del comprobante")
            }
        }

    private val seleccionarProductoSustitutoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != android.app.Activity.RESULT_OK) return@registerForActivityResult
            val indiceSeleccionado = result.data
                ?.getStringExtra(ActivitySeleccionarProductoSustitucion.EXTRA_PRODUCTO_INDICE_RESULTADO)
                .orEmpty()
                .trim()
            if (indiceSeleccionado.isBlank()) return@registerForActivityResult
            onProductoSustitutoSeleccionadoDevolucion?.invoke(indiceSeleccionado)
        }

    override fun onResume() {
        super.onResume()
        if (isHidden) return
        limpiarTodosLosListeners()

        cargarDatosSesion()
        iniciarSincronizacionCajaEnUsoCuenta()

        // 🚨 ELIMINAMOS verificarTurnoActivo() de aquí para evitar el "choque de trenes" al iniciar

        escucharEstadoTurnoCaja() // Esta función dispara la validación limpiamente
        escucharProductosCaja()
        escucharEstadoCobro()
        escucharVentasEnEspera()
        escucharSupervisionEnMiCaja()
        escucharControlAsistidoEnCajaActual()
        publicarEstadoSupervisionActual()
        escucharConexionFirebase()

        if (esTablet()) {
            cargarCategoriasTablet()
            obtenerProductosTablet()
        } else {
            iniciarListenerProductosInventarioInline()
            cargarCategoriasBusquedaInline()
            if (modoBusquedaActivo) {
                aplicarLayoutInicialMobile()
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            // #10: Ocultar overlays inmediatamente al salir para evitar parpadeos visuales
            // durante las animaciones de transición entre fragmentos.
            _binding?.layoutAperturaCaja?.visibility = View.GONE
            _binding?.layoutCierreCajaTablet?.visibility = View.GONE
            _binding?.layoutSkeletonCajaInicial?.visibility = View.GONE
            _binding?.layoutLoadingOverlayCaja?.visibility = View.GONE

            salirModoBusqueda()
            restaurarContextoLocalCajaAlSalir()
            detenerSincronizacionCajaEnUsoCuenta()
            detenerListenersVisibles()
        } else if (isResumed) {
            limpiarTodosLosListeners()
            cargarDatosSesion()
            iniciarSincronizacionCajaEnUsoCuenta()
            // #12: Eliminamos la llamada directa a verificarTurnoActivo() aquí
            // porque escucharEstadoTurnoCaja() ya la dispara internamente al iniciar,
            // evitando el doble parpadeo de carga.
            escucharEstadoTurnoCaja()
            escucharProductosCaja()
            escucharEstadoCobro()
            escucharVentasEnEspera()
            escucharSupervisionEnMiCaja()
            escucharControlAsistidoEnCajaActual()
            publicarEstadoSupervisionActual()
            if (esTablet()) {
                cargarCategoriasTablet()
                obtenerProductosTablet()
            } else {
                iniciarListenerProductosInventarioInline()
                cargarCategoriasBusquedaInline()
                if (modoBusquedaActivo) {
                    aplicarLayoutInicialMobile()
                }
            }
        }
    }

    private fun limpiarTodosLosListeners() {
        estadoCobroListener?.let { listener ->
            estadoCobroRefGlobal?.removeEventListener(listener)
        }
        estadoCobroListener = null
        estadoCobroRefGlobal = null

        cajaValueListener?.let { listener ->
            cajaRefListener?.removeEventListener(listener)
        }
        cajaValueListener = null
        cajaRefListener = null

        inventarioProductosValueListener?.let { listener ->
            inventarioProductosRefListener?.removeEventListener(listener)
        }
        inventarioProductosValueListener = null
        inventarioProductosRefListener = null

        inventarioCajaValueListener?.let { listener ->
            inventarioCajaRefListener?.removeEventListener(listener)
        }
        inventarioCajaValueListener = null
        inventarioCajaRefListener = null

        turnoCajaValueListener?.let { listener ->
            turnoCajaRefListener?.removeEventListener(listener)
        }
        turnoCajaValueListener = null
        turnoCajaRefListener = null

        ventasEnEsperaListener?.let { listener ->
            ventasEnEsperaRef?.removeEventListener(listener)
        }
        ventasEnEsperaListener = null
        ventasEnEsperaRef = null

        popupMenuCajaActivo?.dismiss()
        popupMenuCajaActivo = null

        CajaSupervisionManager.detenerEscuchaSupervision()
        CajaControlAsistidoManager.detenerEscuchaControl()
    }

    private fun detenerListenersVisibles() {
        limpiarTodosLosListeners()
    }

    private fun actualizarEstadoActualizandoCaja(activo: Boolean) {
        runnableResetActualizandoCaja?.let { handlerSeguridadOperaciones.removeCallbacks(it) }
        runnableResetActualizandoCaja = null
        actualizandoCaja = activo

        if (activo) {
            val runnable = Runnable {
                if (!actualizandoCaja) return@Runnable
                actualizandoCaja = false
                Log.w("CajaOperacion", "Se libero actualizandoCaja por timeout de seguridad")
                if (isAdded) {
                    mostrarToastSeguro("La operacion de caja tardó demasiado. Revisa e inténtalo nuevamente.")
                }
            }
            runnableResetActualizandoCaja = runnable
            handlerSeguridadOperaciones.postDelayed(runnable, TIMEOUT_ACTUALIZANDO_CAJA_MS)
        }
    }

    private fun actualizarEstadoVentaEnProceso(activo: Boolean, liberarBloqueo: Boolean = false) {
        runnableResetVentaEnProceso?.let { handlerSeguridadOperaciones.removeCallbacks(it) }
        runnableResetVentaEnProceso = null

        if (activo) {
            // 1. Generamos un identificador único para ESTA solicitud de cobro exacta
            val miToken = ++tokenVentaActual
            ventaEnProceso = true
            actualizarIndicadorVentaEnProcesoUi(true)

            val runnable = Runnable {
                // El Watchdog de 60s SÓLO se ejecutará si el token no ha cambiado de forma lícita
                if (ventaEnProceso && tokenVentaActual == miToken) {
                    Log.w("CajaOperacion", "Se libero ventaEnProceso por timeout de seguridad (Token: $miToken)")

                    val cerrarEstado = {
                        actualizarEstadoVentaEnProceso(false)
                        if (isAdded) {
                            mostrarToastSeguro("La venta tardó demasiado en responder. Verifica el estado antes de reintentar.")
                        }
                    }

                    if (liberarBloqueo) {
                        liberarCajaSiEstabaBloqueada {
                            cerrarEstado()
                        }
                    } else {
                        cerrarEstado()
                    }
                }
            }

            runnableResetVentaEnProceso = runnable
            handlerSeguridadOperaciones.postDelayed(runnable, TIMEOUT_VENTA_EN_PROCESO_MS)
        } else {
            // 2. Al apagar el loading, incrementamos el token para neutralizar runnables viejos
            tokenVentaActual++
            ventaEnProceso = false

            // 3. Centralización Crítica: Apagamos el interruptor aquí para evitar fugas lógicas
            soyYoElQueCobra = false

            actualizarIndicadorVentaEnProcesoUi(false)
        }
    }

    private fun actualizarIndicadorVentaEnProcesoUi(activo: Boolean) {
        val b = _binding ?: return
        if (activo) {
            b.buttoncobrar.isEnabled = false
            b.buttoncobrar.alpha = 0.55f
            b.buttoncobrar.text = "Procesando venta..."
            b.tvTituloLoadingCaja.text = "Procesando venta"
            b.tvMensajeLoadingCaja.text =
                "Validando la venta y evitando cobros duplicados. No cierres esta pantalla."
            return
        }

        ocultarLoadingOverlayCaja()
        actualizarTotales()
    }

    private fun configurarToolbarCaja() {
        obtenerBotonMenuCaja()?.setOnClickListener { anchor ->
            mostrarMenuAccionesCaja(anchor)
        }
        obtenerBotonMasAccionesTablet()?.setOnClickListener { anchor ->
            mostrarMenuMasAccionesTablet(anchor)
        }
        obtenerBotonCambiarCajaTablet()?.setOnClickListener {
            mostrarSelectorDeCajeras()
        }
        obtenerBotonRegistrarEgresoTablet()?.setOnClickListener {
            mostrarDialogoRegistrarEgreso()
        }
        obtenerBotonCerrarTurnoTablet()?.setOnClickListener {
            intentarMostrarCierreCaja()
        }
        obtenerBotonResumenTurnoTablet()?.setOnClickListener {
            mostrarDialogoResumenTurno()
        }
        obtenerBotonDevolucionTablet()?.setOnClickListener {
            mostrarBottomSheetDevolucion()
        }
        val toolbar = _binding?.toolbar
            ?: run {
                actualizarMenuToolbarCaja()
                return
            }
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                MENU_CAMBIAR_CAJA_ID -> {
                    mostrarSelectorDeCajeras()
                    true
                }
                MENU_RESUMEN_TURNO_ID -> {
                    mostrarDialogoResumenTurno()
                    true
                }
                MENU_REGISTRAR_EGRESO_ID -> {
                    mostrarDialogoRegistrarEgreso()
                    true
                }
                MENU_DEVOLUCION_ID -> {
                    mostrarBottomSheetDevolucion()
                    true
                }
                MENU_CERRAR_TURNO_ID -> {
                    intentarMostrarCierreCaja()
                    true
                }
                else -> false
            }
        }
        actualizarMenuToolbarCaja()
    }

    private fun actualizarMenuToolbarCaja() {
        // 1. SOLUCOIN: Captura segura e inmutable del binding al inicio
        val b = _binding ?: return

        val botonMenu = b.btnMenuCaja // Acceso directo y protegido
        val estadoTablet = b.root.findViewById<TextView>(R.id.tvEstadoCajaTablet)
        val btnMasAccionesTablet = obtenerBotonMasAccionesTablet()
        val btnCerrarTurnoTablet = obtenerBotonCerrarTurnoTablet()
        val btnResumenTurnoTablet = obtenerBotonResumenTurnoTablet()
        val toolbar = b.toolbar

        // 2. OPTIMIZACIÓN: Removemos '?.' donde ya garantizamos la existencia de la vista
        toolbar?.menu?.clear()
        actualizarNombreCajaToolbar()

        val estadoUi = CajaEstadoUiBuilder.construirEstadoUi(
            usuarioPuedeCambiarCaja = usuarioPuedeCambiarCaja(),
            verificacionTurnoDisponible = verificacionTurnoDisponible,
            verificacionTurnoEnCurso = verificacionTurnoEnCurso,
            turnoAbierto = turnoAbierto,
            turnoPendienteDiaAnterior = turnoPendienteDiaAnterior,
            fechaTurnoActivo = fechaTurnoActivo
        )
        val toolbarScene = CajaOverlayCoordinator.coordinarToolbar(
            estadoUi = estadoUi,
            esTablet = esTablet()
        )

        botonMenu?.visibility = if (toolbarScene.mostrarBotonMenuMovil) View.VISIBLE else View.GONE
        btnCerrarTurnoTablet?.visibility = if (toolbarScene.mostrarCerrarTurnoTablet) View.VISIBLE else View.GONE
        btnResumenTurnoTablet?.visibility = if (toolbarScene.mostrarResumenTurnoTablet) View.VISIBLE else View.GONE

        val mostrarMasAccionesTablet =
            toolbarScene.mostrarCambiarCajaTablet ||
                    toolbarScene.mostrarRegistrarEgresoTablet ||
                    toolbarScene.mostrarDevolucionTablet
        btnMasAccionesTablet?.visibility = if (mostrarMasAccionesTablet) View.VISIBLE else View.GONE

        toolbar?.subtitle = estadoUi.textoEstado
        toolbar?.setSubtitleTextColor(Color.parseColor(estadoUi.colorEstadoHex))
        estadoTablet?.text = estadoUi.textoEstado
        estadoTablet?.setTextColor(Color.parseColor(estadoUi.colorEstadoHex))
        estadoTablet?.background = estadoUi.backgroundEstadoResId?.let { backgroundResId ->
            context?.let { ContextCompat.getDrawable(it, backgroundResId) }
        }

        val bloqueadoPorOtro = editorControlCajaId.isNotBlank() && editorControlCajaId != idCajera
        val bloqueadoPorRescate = estaEnModoRescateCajaBloqueada()
        botonMenu?.isEnabled = !bloqueadoPorOtro && !bloqueadoPorRescate
        botonMenu?.alpha = if (botonMenu?.isEnabled == true) 1f else 0.45f
        btnMasAccionesTablet?.isEnabled = !bloqueadoPorOtro && !bloqueadoPorRescate
        btnMasAccionesTablet?.alpha = if (btnMasAccionesTablet?.isEnabled == true) 1f else 0.45f
        btnCerrarTurnoTablet?.isEnabled = !bloqueadoPorOtro && (!bloqueadoPorRescate || turnoAbierto)
        btnCerrarTurnoTablet?.alpha = if (btnCerrarTurnoTablet?.isEnabled == true) 1f else 0.45f
    }

    private fun obtenerBotonMenuCaja(): ImageButton? {
        return _binding?.btnMenuCaja
    }

    private fun obtenerBotonMasAccionesTablet(): View? {
        return buscarVistaPorNombre("btnMasAccionesTablet")
    }

    private fun actualizarNombreCajaToolbar() {
        val tvNombreMovil = buscarVistaPorNombre("tvNombreCajaToolbar") as? TextView
        val tvResponsableTablet = buscarVistaPorNombre("tvNombreCajaHeaderTablet") as? TextView
        val tvCajaTablet = buscarVistaPorNombre("tvResponsableCajaHeaderTablet") as? TextView

        val responsableCrudo = SessionManager.nombreCajera.ifBlank { nombreCajera }.trim()
        val nombreCajaEnUso = obtenerNombreCajaOperativa().trim()
        val responsableEnUso = responsableCrudo
            .takeIf {
                it.isNotBlank() &&
                    !it.equals(nombreCajaEnUso, ignoreCase = true)
            }
        val nombreCajaVisible = nombreCajaEnUso
            .takeIf { it.isNotBlank() && !it.equals(responsableEnUso, ignoreCase = true) }
            ?: "Caja activa"

        tvNombreMovil?.text = nombreCajaVisible
        tvNombreMovil?.visibility = if (esTablet()) View.GONE else View.VISIBLE
        tvCajaTablet?.text = nombreCajaVisible
        tvCajaTablet?.visibility = View.VISIBLE

        if (responsableEnUso.isNullOrBlank()) {
            tvResponsableTablet?.visibility = View.GONE
        } else {
            tvResponsableTablet?.text = "Usuario: $responsableEnUso"
            tvResponsableTablet?.visibility = View.VISIBLE
        }
    }

    private fun buscarVistaPorNombre(vararg nombres: String): View? {
        val root = _binding?.root ?: return null
        val resources = root.resources
        val packageName = root.context.packageName
        for (nombre in nombres) {
            val id = resources.getIdentifier(nombre, "id", packageName)
            if (id != 0) {
                root.findViewById<View>(id)?.let { return it }
            }
        }
        return null
    }

    private fun obtenerBotonCambiarCajaTablet(): View? {
        return buscarVistaPorNombre("btnCambiarCajaTablet", "tvCambiarCajaTablet")
    }

    private fun obtenerBotonRegistrarEgresoTablet(): View? {
        return buscarVistaPorNombre("btnRegistrarEgresoTablet", "tvRegistrarEgresoTablet")
    }

    private fun obtenerBotonCerrarTurnoTablet(): View? {
        return buscarVistaPorNombre("btnCerrarTurnoAccionTablet", "tvCerrarTurnoTablet")
    }

    private fun obtenerBotonResumenTurnoTablet(): View? {
        return buscarVistaPorNombre("btnResumenTurnoAccionTablet", "tvResumenTurnoTablet")
    }

    private fun obtenerBotonDevolucionTablet(): View? {
        return buscarVistaPorNombre("btnDevolucionAccionTablet", "tvDevolucionTablet")
    }

    private fun mostrarMenuAccionesCaja(anchor: View) {
        popupMenuCajaActivo?.dismiss()


        // SOLUCIÓN: Inyectamos el estilo gris redondeado al contexto del Popup
        val contextoEstilado = androidx.appcompat.view.ContextThemeWrapper(requireContext(), R.style.PopupGrisRedondeado)
        val popup = PopupMenu(contextoEstilado, anchor)

        popupMenuCajaActivo = popup
        val hayProductos = listaProductosCaja.isNotEmpty()
        val hayVentasEnEspera = cantidadVentasEnEspera > 0
        val bloqueado = operacionVentaEnEsperaEnCurso

        if (usuarioPuedeCambiarCaja()) {
            popup.menu.add(0, MENU_CAMBIAR_CAJA_ID, 0, "Cambiar caja")
        }

        if (turnoAbierto) {
            popup.menu.add(0, MENU_RESUMEN_TURNO_ID, 1, "Ver resumen del turno")
            if (!turnoPendienteDiaAnterior) {
                popup.menu.add(0, MENU_REGISTRAR_EGRESO_ID, 2, "Registrar egreso")
                popup.menu.add(0, MENU_DEVOLUCION_ID, 3, "Registrar devoluci\u00f3n")
            }
            if (hayProductos) {
                popup.menu.add(0, MENU_PAUSAR_VENTA_ID, 4, "Pausar venta").isEnabled = !bloqueado
            }
            popup.menu.add(
                0,
                MENU_VER_EN_ESPERA_ID,
                5,
                if (hayVentasEnEspera) "Ver en espera ($cantidadVentasEnEspera)" else "Ver en espera"
            ).isEnabled = hayVentasEnEspera && !bloqueado
            popup.menu.add(0, MENU_CERRAR_TURNO_ID, 6, "Cerrar turno")
        }

        if (popup.menu.size() == 0) return

        popup.setOnDismissListener {
            if (popupMenuCajaActivo === popup) {
                popupMenuCajaActivo = null
            }
        }
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                MENU_CAMBIAR_CAJA_ID -> {
                    mostrarSelectorDeCajeras()
                    true
                }
                MENU_RESUMEN_TURNO_ID -> {
                    mostrarDialogoResumenTurno()
                    true
                }
                MENU_REGISTRAR_EGRESO_ID -> {
                    mostrarDialogoRegistrarEgreso()
                    true
                }
                MENU_DEVOLUCION_ID -> {
                    mostrarBottomSheetDevolucion()
                    true
                }
                MENU_PAUSAR_VENTA_ID -> {
                    pausarVentaActual()
                    true
                }
                MENU_VER_EN_ESPERA_ID -> {
                    mostrarVentasEnEspera()
                    true
                }
                MENU_CERRAR_TURNO_ID -> {
                    intentarMostrarCierreCaja()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun mostrarMenuMasAccionesTablet(anchor: View) {

        popupMenuCajaActivo?.dismiss()

        val contextoEstilado = androidx.appcompat.view.ContextThemeWrapper(requireContext(), R.style.PopupGrisRedondeado)
        val popup = PopupMenu(contextoEstilado, anchor)

        popupMenuCajaActivo = popup
        val hayProductos = listaProductosCaja.isNotEmpty()
        val hayVentasEnEspera = cantidadVentasEnEspera > 0
        val bloqueado = operacionVentaEnEsperaEnCurso

        if (usuarioPuedeCambiarCaja()) {
            popup.menu.add(0, MENU_CAMBIAR_CAJA_ID, 0, "Cambiar caja")
        }



        if (turnoAbierto && !turnoPendienteDiaAnterior) {
            popup.menu.add(0, MENU_REGISTRAR_EGRESO_ID, 1, "Egresos")
            popup.menu.add(0, MENU_DEVOLUCION_ID, 2, "Devoluci\u00f3n")
        }
        if (hayProductos) {
            popup.menu.add(0, MENU_PAUSAR_VENTA_ID, 3, "Pausar venta").isEnabled = !bloqueado
        }
        popup.menu.add(
            0,
            MENU_VER_EN_ESPERA_ID,
            4,
            if (hayVentasEnEspera) "Ver en espera ($cantidadVentasEnEspera)" else "Ver en espera"
        ).isEnabled = hayVentasEnEspera && !bloqueado

        if (popup.menu.size() == 0) return

        popup.setOnDismissListener {
            if (popupMenuCajaActivo === popup) {
                popupMenuCajaActivo = null
            }
        }
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                MENU_CAMBIAR_CAJA_ID -> {
                    mostrarSelectorDeCajeras()
                    true
                }
                MENU_REGISTRAR_EGRESO_ID -> {
                    mostrarDialogoRegistrarEgreso()
                    true
                }
                MENU_DEVOLUCION_ID -> {
                    mostrarBottomSheetDevolucion()
                    true
                }
                MENU_PAUSAR_VENTA_ID -> {
                    pausarVentaActual()
                    true
                }
                MENU_VER_EN_ESPERA_ID -> {
                    mostrarVentasEnEspera()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun usuarioPuedeCambiarCaja(): Boolean {
        return SessionManager.rol.equals("administrador", ignoreCase = true) ||
            SessionManager.rol.equals("supervisor", ignoreCase = true)
    }

    private fun configurarLayoutTurnos() {
        obtenerInputMontoApertura()?.doAfterTextChanged {
            obtenerLayoutMontoApertura()?.error = null
        }

        obtenerBotonConfirmarApertura()?.setOnClickListener {
            confirmarAperturaTurno()
        }

        obtenerInputEfectivoRealCierre()?.doAfterTextChanged {
            obtenerLayoutMontoCierre()?.error = null
            obtenerLayoutObservacionCierre()?.error = null
            
            val montoReal = it?.toString()?.toDoubleSeguro(0.0) ?: 0.0
            
            val esperado = obtenerTextTotalEsperadoCierre()?.text?.toString()
                    ?.replace(SessionManager.monedaSimbolo, "", ignoreCase = true)
                    ?.replace("$", "")
                    ?.replace(",", "")
                    ?.trim()
                    ?.toDoubleOrNull() ?: 0.0
                    
            actualizarEstadoObservacionCierre(esperado, montoReal)
        }

        obtenerInputObservacionCierre()?.doAfterTextChanged {
            obtenerLayoutObservacionCierre()?.error = null
        }

        obtenerInputObservacionCierre()?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                desplazarCierreHastaObservacion()
            }
        }

        obtenerBotonCancelarCierre()?.setOnClickListener {
            if (cierreObligatorioActivo()) {
                val mensaje = if (rescateRequiereCierreObligatorio()) {
                    "Debes cerrar el turno rescatado para continuar"
                } else {
                    "Debes completar el cierre pendiente para continuar"
                }
                mostrarToastSeguro(mensaje)
                return@setOnClickListener
            }
            ocultarLayoutCierreCaja()
        }

        obtenerBotonFinalizarCierre()?.setOnClickListener {
            confirmarCierreTurno()
        }

        obtenerRecyclerResumenCierre()?.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun obtenerInputMontoApertura(): TextInputEditText? {
        val b = _binding ?: return null
        return if (esTablet()) {
            b.root.findViewById(R.id.etMontoAperturaTablet)
        } else {
            b.root.findViewById(R.id.etMontoAperturaMovil)
        }
    }

    private fun obtenerBotonConfirmarApertura(): MaterialButton? {
        val b = _binding ?: return null
        return if (esTablet()) {
            b.root.findViewById(R.id.btnConfirmarAperturaTablet)
        } else {
            b.root.findViewById(R.id.btnConfirmarAperturaMovil)
        }
    }

    private fun obtenerLayoutMontoApertura(): TextInputLayout? {
        val input = obtenerInputMontoApertura() ?: return null
        return input.parent?.parent as? TextInputLayout
    }

    private fun obtenerTituloApertura(): TextView? {
        val b = _binding ?: return null
        return if (esTablet()) {
            b.root.findViewById(R.id.tvTituloAperturaTablet)
        } else {
            b.root.findViewById(R.id.tvTituloAperturaMovil)
        }
    }



    private fun obtenerLayoutCierreActual(): View? {
        val b = _binding ?: return null
        return if (esTablet()) {
            b.root.findViewById(R.id.layoutCierreCajaTablet)
        } else {
            b.root.findViewById(R.id.layoutCierreCajaMovil)
        }
    }

    private fun obtenerRecyclerResumenCierre(): RecyclerView? {
        val b = _binding ?: return null
        return if (esTablet()) {
            b.root.findViewById(R.id.rvDetalleMetodosCierreTablet)
        } else {
            b.root.findViewById(R.id.rvDetalleMetodosCierreMovil)
        }
    }

    private fun obtenerTextTotalEsperadoCierre(): TextView? {
        val b = _binding ?: return null
        return if (esTablet()) {
            b.root.findViewById(R.id.tvTotalGeneralEsperadoTablet)
        } else {
            b.root.findViewById(R.id.tvTotalGeneralEsperadoMovil)
        }
    }

    private fun obtenerInputEfectivoRealCierre(): TextInputEditText? {
        val b = _binding ?: return null
        return if (esTablet()) {
            b.root.findViewById(R.id.etEfectivoRealTablet)
        } else {
            b.root.findViewById(R.id.etEfectivoRealMovil)
        }
    }

    private fun actualizarModoCierreForzado() {
        val botonCancelar = obtenerBotonCancelarCierre()
        botonCancelar?.visibility = if (cierreObligatorioActivo()) View.GONE else View.VISIBLE
        obtenerBotonFinalizarCierre()?.text = if (rescateRequiereCierreObligatorio()) {
            "Cerrar turno rescatado"
        } else if (cierreForzadoPendienteActivo) {
            "Cerrar turno pendiente"
        } else if (esTablet()) {
            "Cerrar Turno y Guardar Reporte"
        } else {
            "Cerrar Caja"
        }
    }

    private fun rescateRequiereCierreObligatorio(): Boolean {
        return estaEnModoRescateCajaBloqueada() && turnoAbierto && idTurnoActivo.isNotBlank()
    }

    private fun cierreObligatorioActivo(): Boolean {
        return cierreForzadoPendienteActivo || rescateRequiereCierreObligatorio()
    }

    private fun actualizarDisponibilidadBuscadorPorRescate() {
        val bindingActual = _binding ?: return
        val inputBusqueda = bindingActual.buscar ?: return
        val bloqueoRescate = rescateRequiereCierreObligatorio()
        inputBusqueda.apply {
            if (bloqueoRescate) {
                clearFocus()
            }
            isEnabled = !bloqueoRescate
            isFocusable = !bloqueoRescate
            isFocusableInTouchMode = !bloqueoRescate
            isCursorVisible = !bloqueoRescate
        }
        if (bloqueoRescate) {
            ocultarTeclado()
        }
        bindingActual.layoutBuscadorInline?.alpha = if (bloqueoRescate) 0.55f else 1f
    }

    private fun obtenerLayoutMontoCierre(): TextInputLayout? {
        val input = obtenerInputEfectivoRealCierre() ?: return null
        return input.parent?.parent as? TextInputLayout
    }

    private fun obtenerInputObservacionCierre(): TextInputEditText? {
        val b = _binding ?: return null
        return if (esTablet()) {
            b.root.findViewById(R.id.etObservacionCierreTablet)
        } else {
            b.root.findViewById(R.id.etObservacionCierreMovil)
        }
    }

    private fun obtenerTextDiferenciaCierre(): TextView? {
        val b = _binding ?: return null
        return if (esTablet()) {
            b.root.findViewById(R.id.tvDiferenciaCierreTablet)
        } else {
            b.root.findViewById(R.id.tvDiferenciaCierreMovil)
        }
    }

    private fun obtenerTvEsperadoInlineCierre(): TextView? {
        val b = _binding ?: return null
        return if (esTablet()) b.root.findViewById(R.id.tvEsperadoInlineCierreTablet)
        else b.root.findViewById(R.id.tvEsperadoInlineCierreMovil)
    }

    private fun obtenerTvIngresadoInlineCierre(): TextView? {
        val b = _binding ?: return null
        return if (esTablet()) b.root.findViewById(R.id.tvIngresadoInlineCierreTablet)
        else b.root.findViewById(R.id.tvIngresadoInlineCierreMovil)
    }

    private fun obtenerCuadreBadgeCierre(): TextView? {
        val b = _binding ?: return null
        return if (esTablet()) b.root.findViewById(R.id.tvCuadreBadgeCierreTablet)
        else b.root.findViewById(R.id.tvCuadreBadgeCierreMovil)
    }

    private fun obtenerLayoutObservacionCierre(): TextInputLayout? {
        val b = _binding ?: return null
        return if (esTablet()) {
            b.root.findViewById(R.id.inputLayoutObservacionCierreTablet)
        } else {
            b.root.findViewById(R.id.inputLayoutObservacionCierreMovil)
        }
    }

    private fun obtenerScrollCierreActual(): View? {
        val b = _binding ?: return null
        return if (esTablet()) {
            b.root.findViewById<NestedScrollView>(R.id.scrollCierreCajaTablet)
        } else {
            b.root.findViewById<NestedScrollView>(R.id.scrollCierreCajaMovil)
        }
    }

    private fun desplazarCierreHastaObservacion() {
        val objetivo = obtenerLayoutObservacionCierre() ?: return
        val scroll = obtenerScrollCierreActual() ?: return
        objetivo.post {
            when (scroll) {
                is NestedScrollView -> scroll.smoothScrollTo(0, objetivo.top.coerceAtLeast(0))
            }
        }
    }

    private fun actualizarEstadoObservacionCierre(efectivoEsperado: Double, efectivoReal: Double?) {
        val layout = obtenerLayoutObservacionCierre() ?: return
        val input = obtenerInputObservacionCierre() ?: return
        val tvDiferencia = obtenerTextDiferenciaCierre()
        val estabaVisible = layout.visibility == View.VISIBLE

        val real = efectivoReal ?: 0.0
        val diferencia = if (efectivoReal == null) 0.0 else real - efectivoEsperado
        val requiereObservacion = kotlin.math.abs(diferencia) >= 0.01

        val colorNeutral  = Color.parseColor("#8F9BB3")
        val colorSobrante = Color.parseColor("#137333")
        val colorFaltante = Color.parseColor("#D93025")

        val textoDiferencia = when {
            kotlin.math.abs(diferencia) < 0.01 -> "Diferencia: ${MonedaHelper.formatear(0.0)}"
            else -> "Diferencia: ${MonedaHelper.formatearConSigno(diferencia)}"
        }

        tvDiferencia?.text = textoDiferencia
        tvDiferencia?.setTextColor(
            when {
                kotlin.math.abs(diferencia) < 0.01 -> colorNeutral
                diferencia > 0 -> colorSobrante
                else -> colorFaltante
            }
        )

        // -- Cuadre en vivo: Esperado / Ingresado / Badge --
        obtenerTvEsperadoInlineCierre()?.apply {
            text = MonedaHelper.formatear(efectivoEsperado)
            // Rojo cuando el saldo esperado es negativo para visibilidad inmediata
            setTextColor(
                if (efectivoEsperado < 0.0) Color.parseColor("#DC2626")
                else Color.parseColor("#111111")
            )
        }
        obtenerTvIngresadoInlineCierre()?.text = MonedaHelper.formatear(real)

        obtenerCuadreBadgeCierre()?.let { badge ->
            when {
                kotlin.math.abs(diferencia) < 0.01 -> {
                    badge.text = "? Cuadrado"
                    badge.setTextColor(Color.parseColor("#15803D"))
                    badge.backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor("#DCFCE7"))
                }
                diferencia > 0 -> {
                    badge.text = "\u2191 Sobrante"
                    badge.setTextColor(Color.parseColor("#92400E"))
                    badge.backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor("#FEF3C7"))
                }
                else -> {
                    badge.text = "\u2193 Faltante"
                    badge.setTextColor(Color.parseColor("#991B1B"))
                    badge.backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor("#FEE2E2"))
                }
            }
        }

        layout.visibility = if (requiereObservacion) View.VISIBLE else View.GONE
        if (requiereObservacion && !estabaVisible) {
            desplazarCierreHastaObservacion()
        } else if (!requiereObservacion) {
            layout.error = null
            input.setText("")
        }
    }

    private fun obtenerBotonFinalizarCierre(): MaterialButton? {
        val b = _binding ?: return null
        return if (esTablet()) {
            b.root.findViewById(R.id.btnFinalizarTurnoTablet)
        } else {
            b.root.findViewById(R.id.btnFinalizarTurnoMovil)
        }
    }

    private fun obtenerBotonCancelarCierre(): MaterialButton? {
        val b = _binding ?: return null
        return if (esTablet()) {
            b.root.findViewById(R.id.btnCancelarCierreTablet)
        } else {
            b.root.findViewById(R.id.btnCancelarCierreMovil)
        }
    }

    private fun mostrarDialogoResumenTurno() {
        if (!turnoAbierto || idTurnoActivo.isBlank()) {
            mostrarToastSeguro("No hay un turno abierto")
            return
        }
        if (!isAdded || _binding == null) return

        val ref = getTurnoActivoRef() ?: return

        // NUEVO: Bloquear UI mientras carga
        mostrarLoadingOverlayCaja("Cargando...", "Obteniendo resumen del turno")

        val dialogBinding = DialogResumenTurnoMejoradoBinding.inflate(layoutInflater)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Cerrar", null)
            .create()

        ref.get()
            .addOnSuccessListener { snapshot ->
                ocultarLoadingOverlayCaja() // NUEVO: Liberar UI
                if (!isAdded || _binding == null || !snapshot.exists()) {
                    dialog.show()
                    return@addOnSuccessListener
                }

                // Datos básicos
                val totalVentas = snapshot.child("totalVentas").getValue(String::class.java)?.toDoubleOrNull() ?: 0.0
                val numeroVentas = snapshot.child("numeroVentas").value?.toString()?.toLongOrNull() ?: 0L
                val totalEgresos = snapshot.child("totalEgresos").getValue(String::class.java)?.toDoubleOrNull() ?: 0.0
                val montoApertura = snapshot.child("montoApertura").value?.toString()?.toDoubleOrNull() ?: 0.0
                val ventasEfectivo = snapshot.child("ventasEfectivo").value?.toString()?.toDoubleOrNull() ?: 0.0
                val cajaFinalTeorica = montoApertura + ventasEfectivo - totalEgresos
                val horaApertura = snapshot.child("horaAperturaLocal").getValue(String::class.java)
                    ?: snapshot.child("horaApertura").getValue(String::class.java)
                    ?: ""
                val ultimaVenta = snapshot.child("ultimaVenta").getValue(String::class.java).orEmpty()

                // Header
                val fechaTexto = fechaTurnoActivo.ifBlank { obtenerFechaActual() }
                val aperturaTexto = if (horaApertura.isNotBlank()) "Abierto desde las $horaApertura" else "Turno abierto"
                dialogBinding.tvFechaTurnoResumen.text = "$fechaTexto · $aperturaTexto"

                // Tarjetas superiores
                dialogBinding.tvTotalVentasResumen.text = MonedaHelper.formatear(totalVentas)
                dialogBinding.tvNumeroVentasResumen.text = numeroVentas.toString()
                dialogBinding.tvEgresosCuadreResumen.text = MonedaHelper.formatear(totalEgresos)
                dialogBinding.tvEfectivoEsperadoResumen.text = MonedaHelper.formatear(cajaFinalTeorica)
                dialogBinding.tvEfectivoEsperadoResumen.setTextColor(
                    if (cajaFinalTeorica < 0.0) Color.parseColor("#DC2626") else Color.parseColor("#111111")
                )

                // Cuadre de caja
                dialogBinding.tvMontoAperturaResumen.text = MonedaHelper.formatear(montoApertura)
                dialogBinding.tvVentasEfectivoResumen.text = MonedaHelper.formatear(ventasEfectivo)
                dialogBinding.tvEgresosCuadreResumen.text = MonedaHelper.formatear(totalEgresos)
                dialogBinding.tvCajaFinalTeoricaResumen.text = MonedaHelper.formatear(cajaFinalTeorica)
                dialogBinding.tvCajaFinalTeoricaResumen.setTextColor(
                    if (cajaFinalTeorica >= 0) Color.parseColor("#166534") else Color.parseColor("#DC2626")
                )

                // Footer
        dialogBinding.tvHoraAperturaResumen.text = horaApertura.ifBlank { "\u2014" }
                dialogBinding.tvUltimaVentaResumen.text = ultimaVenta.ifBlank { "Sin ventas aún" }

                // Desglose por método de pago (excluyendo apertura)
                val items = CajaTurnoHelper.construirItemsResumenTurno(snapshot)
                    .filter { !it.titulo.contains("Apertura", ignoreCase = true) }
                if (items.isEmpty() || (numeroVentas == 0L && totalVentas <= 0.009 && totalEgresos <= 0.009)) {
                    dialogBinding.tvResumenSinVentas.visibility = View.VISIBLE
                    dialogBinding.recyclerResumenMetodos.visibility = View.GONE
                } else {
                    dialogBinding.tvResumenSinVentas.visibility = View.GONE
                    dialogBinding.recyclerResumenMetodos.visibility = View.VISIBLE
                    dialogBinding.recyclerResumenMetodos.layoutManager = LinearLayoutManager(requireContext())
                    dialogBinding.recyclerResumenMetodos.adapter = AdapterResumenPagoCierre(items, totalVentas)
                }

                // Top 5 productos más vendidos
                val idCajaTop = obtenerIdCajaOperativa()
                val fechaTop = fechaTurnoActivo.ifBlank { obtenerFechaActual() }
                database.reference
                    .child("VentasPorCajera").child(idCajaTop).child(fechaTop)
                    .get()
                    .addOnSuccessListener { ventasSnap ->
                        if (!isAdded || !dialog.isShowing) return@addOnSuccessListener
                        val conteo = mutableMapOf<String, Pair<String, Int>>()
                        for (ventaChild in ventasSnap.children) {
                            for (prodChild in ventaChild.child("productos").children) {
                                val nombre = prodChild.child("nombre").getValue(String::class.java).orEmpty().ifBlank { "Producto" }
                                val cantidad = prodChild.child("cantidad").getValue(String::class.java)?.toIntOrNull() ?: 0
                                val clave = prodChild.child("indiceProducto").getValue(String::class.java).orEmpty().ifBlank { nombre }
                                val prev = conteo[clave]
                                conteo[clave] = nombre to ((prev?.second ?: 0) + cantidad)
                            }
                        }
                        val top5 = conteo.values.sortedByDescending { it.second }.take(5)
                        if (top5.isNotEmpty()) {
                            dialogBinding.tvTopProductosTitulo.visibility = View.VISIBLE
                            dialogBinding.layoutTopProductos.visibility = View.VISIBLE
                            dialogBinding.layoutTopProductos.removeAllViews()
                            top5.forEachIndexed { i, (nombre, cant) ->
                                val tv = TextView(requireContext()).apply {
                    text = "${i + 1}. $nombre \u2014 $cant ${if (cant == 1) "unidad" else "unidades"}"
                                    textSize = 12f
                                    setTextColor(Color.parseColor("#374151"))
                                    setPadding(0, 8, 0, 8)
                                }
                                dialogBinding.layoutTopProductos.addView(tv)
                            }
                        }
                    }

                dialog.show()

                // Ajuste de tamaño para tablet
                if (esTablet()) {
                    val displayMetrics = resources.displayMetrics
                    val width = (displayMetrics.widthPixels * 0.7).toInt()
                    val height = (displayMetrics.heightPixels * 0.7).toInt()
                    dialog.window?.setLayout(width, height)
                    dialog.window?.setGravity(Gravity.CENTER)
                }
            }
            .addOnFailureListener {
                ocultarLoadingOverlayCaja() // NUEVO: Liberar UI
                if (isAdded) {
                    mostrarToastSeguro("No se pudo cargar el resumen del turno")
                }
            }
    }
    // -------------------------------------------------------------------------------------
    // DEVOLUCIÓN DE VENTAS
    // -------------------------------------------------------------------------------------

    data class VentaDevolucionUi(
        val idVenta: String,
        val fecha: String,
        val hora: String,
        val total: Double,
        val metodoPago: String,
        val tipoComprobante: String,
        val nombreCliente: String,
        val cantidadProductos: Int,
        val estadoDevolucion: String = "vigente",
        val itemsPendientesDevolucion: Int = 0
    )

    data class EstadoItemDevolucionUi(
        val itemVentaId: String,
        val cantidadVendida: Int,
        val cantidadDevuelta: Int,
        val estado: String,
        val tipoResolucion: String,
        val resuelto: Boolean,
        val subtotalDevuelto: Double,
        val lotesDevueltosDetalle: Map<String, LoteConsumoDetalleCaja> = emptyMap()
    )

    data class EstadoVentaDevolucionUi(
        val estadoGeneral: String,
        val textoVisible: String,
        val itemsPendientes: Int,
        val itemsDevueltos: Int,
        val esTotal: Boolean
    )

    data class ItemDevolucionUi(
        val itemVentaId: String,
        val indiceProducto: String,
        val nombre: String,
        val presentacion: String,
        val cantidadVendida: Int,
        val unidadesPorPresentacion: Int,
        val subtotalOriginal: Double,
        val productoVenta: ProductoCaja,
        val cantidadYaDevuelta: Int = 0,
        val cantidadDisponibleDevolucion: Int = 0,
        val lotesDevueltosPrevios: Map<String, LoteConsumoDetalleCaja> = emptyMap(),
        val estadoDevolucion: String = "normal",
        val tipoResolucionActual: String = "",
        val resuelto: Boolean = false,
        var cantidadADevolver: Int = 0,
        var seleccionado: Boolean = true
    )

    private data class ProductoSustitucionUi(
        val productoInventario: MoldeProductos,
        val presentacion: PresentacionProducto,
        val cantidadPresentaciones: Int,
        val productoCaja: ProductoCaja,
        val subtotal: Double,
        val stockVendibleDisponible: Int
    )

    private data class AjusteFinancieroDevolucion(
        val totalDevueltoProducto: Double,
        val totalSustitucion: Double,
        val diferenciaCliente: Double,
        val montoDevueltoCliente: Double,
        val montoCobradoCliente: Double,
        val deltaVentasTurno: Double
    )

    private data class PagoDiferenciaDevolucion(
        val metodoPago: String,
        val montoPagado: Double,
        val detalleMetodos: Map<String, Double>
    )

    private data class ResultadoRestauracionDevolucionUi(
        val item: ItemDevolucionUi,
        val stockFinal: Int,
        val loteRepuestoExacto: Boolean,
        val detalleDevolucionLote: String
    )

    private val motivosBloqueoDevolucion = listOf(
        "roto",
        "rota",
        "mal estado",
        "en mal estado",
        "vencido",
        "vencida",
        "caducado",
        "caducada",
        "danado",
        "danada",
        "daniado",
        "daniada",
        "averiado",
        "averiada",
        "deteriorado",
        "deteriorada",
        "defectuoso",
        "defectuosa",
        "contaminado",
        "contaminada",
        "mal sellado",
        "mal sellada",
        "inservible",
        "inutilizable",
        "abierto",
        "abierta",
        "quebrado",
        "quebrada"
    )

    private fun resolverEnteroFlexible(raw: Any?): Int {
        return when (raw) {
            is Long -> raw.toInt()
            is Int -> raw
            is Double -> raw.toInt()
            is String -> raw.trim().toIntOrNull() ?: 0
            else -> 0
        }
    }

    private fun resolverDoubleFlexible(raw: Any?): Double {
        return when (raw) {
            is Long -> raw.toDouble()
            is Int -> raw.toDouble()
            is Double -> raw
            is String -> raw.trim().toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
    }

    private fun normalizarTextoLibre(texto: String): String {
        return Normalizer.normalize(texto.trim().lowercase(Locale.getDefault()), Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
    }

    private fun motivoImplicaBloqueoVenta(motivo: String): Boolean {
        val motivoNormalizado = normalizarTextoLibre(motivo)
        if (motivoNormalizado.isBlank()) return false
        return motivosBloqueoDevolucion.any { palabra ->
            motivoNormalizado.contains(normalizarTextoLibre(palabra))
        }
    }

    private fun limpiarSeleccionManualSiCoincideConLoteBloqueado(
        currentData: MutableData,
        loteNode: MutableData
    ) {
        val loteSeleccionadoActual = currentData
            .child("loteConsumoSeleccionado")
            .value
            ?.toString()
            .orEmpty()
            .trim()
        val seleccionManualActual = currentData
            .child("loteConsumoSeleccionManual")
            .value
            .toBooleanSeguro(false)
        if (!seleccionManualActual || loteSeleccionadoActual.isBlank()) return

        val claveLote = loteNode.key.orEmpty().trim()
        val numeroLote = loteNode.child("numero").value?.toString().orEmpty().trim()
        val coincideSeleccion = loteSeleccionadoActual.equals(claveLote, ignoreCase = true) ||
            (numeroLote.isNotBlank() && loteSeleccionadoActual.equals(numeroLote, ignoreCase = true))
        if (coincideSeleccion) {
            currentData.child("loteConsumoSeleccionado").value = ""
            currentData.child("loteConsumoSeleccionManual").value = false
        }
    }

    private fun motivoBloqueoRegistrado(motivo: String): String {
        return motivo.trim().ifBlank { "Devolucion bloqueada para venta" }
    }

    private fun destinoStockDevolucion(motivoBloqueaVenta: Boolean): String {
        return if (motivoBloqueaVenta) "bloqueado" else "vendible"
    }

    private fun construirMapaDetalleLotes(
        detalles: Collection<LoteConsumoDetalleCaja>
    ): Map<String, Map<String, Any>> {
        val resultado = linkedMapOf<String, Map<String, Any>>()
        detalles.filter { it.clave.isNotBlank() && it.cantidad > 0 }.forEach { detalle ->
            resultado[detalle.clave] = linkedMapOf(
                "clave" to detalle.clave,
                "numero" to detalle.numero,
                "vencimiento" to detalle.vencimiento,
                "cantidad" to detalle.cantidad
            )
        }
        return resultado
    }

    private fun leerDetalleLotesDesdeSnapshot(snapshot: DataSnapshot): Map<String, LoteConsumoDetalleCaja> {
        if (!snapshot.exists()) return emptyMap()
        return snapshot.children.mapNotNull { detalleSnapshot ->
            val detalle = detalleSnapshot.getValue(LoteConsumoDetalleCaja::class.java)
            val clave = detalle?.clave?.trim().orEmpty().ifBlank { detalleSnapshot.key.orEmpty().trim() }
            if (detalle != null && clave.isNotBlank() && detalle.cantidad > 0) {
                clave to detalle.copy(clave = clave)
            } else {
                null
            }
        }.toMap(LinkedHashMap())
    }

    private fun inferirLotesDevueltosLegacy(
        producto: ProductoCaja,
        unidadesDevueltas: Int
    ): Map<String, LoteConsumoDetalleCaja> {
        if (unidadesDevueltas <= 0) return emptyMap()
        return detallesLotesParaMovimiento(producto, unidadesDevueltas)
            .filter { it.clave.isNotBlank() && it.cantidad > 0 }
            .associateByTo(LinkedHashMap()) { it.clave }
    }

    private fun acumularDetalleLotesDevueltos(
        previos: Map<String, LoteConsumoDetalleCaja>,
        actuales: List<LoteConsumoDetalleCaja>
    ): Map<String, LoteConsumoDetalleCaja> {
        val acumulado = LinkedHashMap<String, LoteConsumoDetalleCaja>()
        previos.values.forEach { detalle ->
            if (detalle.clave.isNotBlank() && detalle.cantidad > 0) {
                acumulado[detalle.clave] = detalle.copy(clave = detalle.clave)
            }
        }
        actuales.forEach { detalle ->
            if (detalle.clave.isBlank() || detalle.cantidad <= 0) return@forEach
            val previo = acumulado[detalle.clave]
            acumulado[detalle.clave] = if (previo != null) {
                previo.copy(cantidad = previo.cantidad + detalle.cantidad)
            } else {
                detalle.copy(clave = detalle.clave)
            }
        }
        return acumulado
    }

    private fun construirTramosDevolucionDesdeVenta(
        productoVenta: ProductoCaja,
        unidadesADevolver: Int,
        lotesDevueltosPrevios: Map<String, LoteConsumoDetalleCaja>
    ): List<LoteConsumoDetalleCaja> {
        if (unidadesADevolver <= 0) return emptyList()
        val cantidadVendida = productoVenta.cantidad.toIntOrNull() ?: 0
        val unidadesPorPresentacion = productoVenta.unidadesPorPresentacion.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val unidadesVendidas = cantidadVendida * unidadesPorPresentacion
        if (unidadesVendidas <= 0) return emptyList()

        val trazasOriginales = detallesLotesParaMovimiento(productoVenta, unidadesVendidas)
            .filter { it.cantidad > 0 }
        if (trazasOriginales.isEmpty()) return emptyList()

        var restante = unidadesADevolver
        val resultado = mutableListOf<LoteConsumoDetalleCaja>()
        trazasOriginales.forEach { original ->
            if (restante <= 0) return@forEach
            val devueltoPrevio = lotesDevueltosPrevios[original.clave]?.cantidad ?: 0
            val disponibleEnTraza = (original.cantidad - devueltoPrevio).coerceAtLeast(0)
            if (disponibleEnTraza <= 0) return@forEach

            val tramo = minOf(disponibleEnTraza, restante)
            if (tramo > 0) {
                resultado += original.copy(cantidad = tramo)
                restante -= tramo
            }
        }
        return if (restante <= 0) resultado else emptyList()
    }

    private fun construirProductoVentaParaEventoDevolucion(
        item: ItemDevolucionUi,
        tramosDevolucion: List<LoteConsumoDetalleCaja>
    ): ProductoCaja {
        val primerTramo = tramosDevolucion.firstOrNull()
        return item.productoVenta.copy(
            lotesConsumidosDetalle = tramosDevolucion.associateByTo(LinkedHashMap()) { it.clave },
            loteNumero = primerTramo?.numero.orEmpty(),
            loteClaveConsumida = primerTramo?.clave.orEmpty(),
            loteVencimiento = primerTramo?.vencimiento.orEmpty()
        )
    }

    private fun formatearCantidadPresentacionDevolucion(
        cantidad: Int,
        presentacion: String
    ): String {
        val cantidadSegura = cantidad.coerceAtLeast(0)
        val presentacionLimpia = presentacion.trim().ifBlank { "presentaciones" }
        return "$cantidadSegura $presentacionLimpia"
    }

    private fun construirTramosDevolucionPreview(
        item: ItemDevolucionUi,
        cantidadPresentaciones: Int
    ): List<LoteConsumoDetalleCaja> {
        val cantidadSegura = cantidadPresentaciones.coerceAtLeast(0)
        if (cantidadSegura <= 0) return emptyList()
        val unidadesADevolver = cantidadSegura * item.unidadesPorPresentacion.coerceAtLeast(1)
        return construirTramosDevolucionDesdeVenta(
            productoVenta = item.productoVenta,
            unidadesADevolver = unidadesADevolver,
            lotesDevueltosPrevios = item.lotesDevueltosPrevios
        )
    }

    private fun construirDetalleLotesPreviewDevolucion(
        item: ItemDevolucionUi,
        cantidadPresentaciones: Int
    ): String {
        val tramos = construirTramosDevolucionPreview(item, cantidadPresentaciones)
        if (tramos.isEmpty()) return ""
        return tramos.joinToString(", ") { detalle ->
            val numeroVisible = detalle.numero.trim().ifBlank { detalle.clave.trim() }
            "$numeroVisible: ${detalle.cantidad}"
        }
    }

    private fun construirHelperCantidadDevolucion(
        item: ItemDevolucionUi,
        cantidadPresentaciones: Int
    ): String {
        val nombrePresentacion = item.presentacion.ifBlank { "presentacion" }
        val detalleEquivalencia = if (item.unidadesPorPresentacion.coerceAtLeast(1) > 1) {
            "1 ${nombrePresentacion.lowercase(Locale.getDefault())} = ${item.unidadesPorPresentacion.coerceAtLeast(1)} unidades"
        } else {
            ""
        }
        val detalleLotes = construirDetalleLotesPreviewDevolucion(item, cantidadPresentaciones)
        return listOfNotNull(
            detalleEquivalencia.ifBlank { null },
            detalleLotes.takeIf { it.isNotBlank() }?.let { "Regresa a lotes: $it" }
        ).joinToString("\n")
    }

    private fun validarTrazabilidadExactaDevolucion(item: ItemDevolucionUi): String? {
        if (!item.seleccionado || item.resuelto) return null
        if (item.cantidadADevolver <= 0) {
            return "Ingresa una cantidad valida en ${item.presentacion}"
        }
        if (item.cantidadADevolver > item.cantidadDisponibleDevolucion) {
            return "Solo puedes devolver ${
                formatearCantidadPresentacionDevolucion(
                    item.cantidadDisponibleDevolucion,
                    item.presentacion
                )
            }"
        }
        val tramos = construirTramosDevolucionPreview(item, item.cantidadADevolver)
        val unidadesEsperadas = item.cantidadADevolver * item.unidadesPorPresentacion.coerceAtLeast(1)
        return when {
            tramos.isEmpty() -> "No se pudo reconstruir el lote exacto de origen para ${item.nombre}"
            tramos.sumOf { it.cantidad } < unidadesEsperadas ->
                "La devolucion de ${item.nombre} no puede volver exactamente a sus lotes de origen"
            else -> null
        }
    }

    private fun validarExistenciaLotesOrigenDevolucion(
        items: List<ItemDevolucionUi>,
        index: Int = 0,
        onResult: (String?) -> Unit
    ) {
        if (index >= items.size) {
            onResult(null)
            return
        }

        val item = items[index]
        val tramos = construirTramosDevolucionPreview(item, item.cantidadADevolver)
        if (tramos.isEmpty()) {
            onResult("No se pudo reconstruir la devolucion exacta por lotes para ${item.nombre}")
            return
        }

        database.reference
            .child("Inventario")
            .child("Productos")
            .child(item.indiceProducto)
            .child("lotes")
            .get()
            .addOnSuccessListener { lotesSnapshot ->
                if (!lotesSnapshot.exists()) {
                    onResult("El producto ${item.nombre} no tiene lotes registrados para devolverlo con trazabilidad exacta")
                    return@addOnSuccessListener
                }

                val faltantes = tramos.filter { detalle ->
                    lotesSnapshot.children.none { loteSnapshot ->
                        loteSnapshot.key.orEmpty().trim().equals(detalle.clave.trim(), ignoreCase = true) ||
                            loteSnapshot.child("numero").value
                                ?.toString()
                                .orEmpty()
                                .trim()
                                .equals(detalle.numero.trim(), ignoreCase = true)
                    }
                }

                if (faltantes.isNotEmpty()) {
                    val lotesTexto = faltantes.joinToString(", ") { detalle ->
                        detalle.numero.trim().ifBlank { detalle.clave.trim() }
                    }
                    onResult("No se encontraron los lotes de origen $lotesTexto para devolver ${item.nombre} con exactitud")
                    return@addOnSuccessListener
                }

                validarExistenciaLotesOrigenDevolucion(items, index + 1, onResult)
            }
            .addOnFailureListener { error ->
                onResult("No se pudo validar el lote de origen de ${item.nombre}: ${error.message}")
            }
    }

    private fun ajustarInventarioDevolucionItemExacta(
        item: ItemDevolucionUi,
        motivoDevolucion: String,
        motivoBloqueaVenta: Boolean,
        revertir: Boolean = false,
        onSuccess: (ResultadoRestauracionDevolucionUi) -> Unit,
        onError: (String) -> Unit
    ) {
        val productoVenta = item.productoVenta
        val unidadesAAjustar = item.cantidadADevolver * item.unidadesPorPresentacion.coerceAtLeast(1)
        if (unidadesAAjustar <= 0) {
            onError("La cantidad a devolver de ${item.nombre} no es válida")
            return
        }

        val productoRef = database.reference
            .child("Inventario")
            .child("Productos")
            .child(item.indiceProducto)

        var loteRepuestoExacto = productoVenta.loteNumero.trim().isBlank()
        var detalleDevolucionLote = ""
        var stockFinalResultado = 0
        val timestampBloqueo = System.currentTimeMillis()

        productoRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                fun resolverEntero(raw: Any?): Int {
                    return when (raw) {
                        is Long -> raw.toInt()
                        is Int -> raw
                        is Double -> raw.toInt()
                        is String -> raw.trim().toIntOrNull() ?: 0
                        else -> 0
                    }
                }

                fun resolverDouble(raw: Any?): Double {
                    return when (raw) {
                        is Long -> raw.toDouble()
                        is Int -> raw.toDouble()
                        is Double -> raw
                        is String -> raw.trim().toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }
                }

                val stockNode = currentData.child("cantidadinicial")
                val stockActual = resolverEntero(stockNode.value)
                val deltaStock = if (motivoBloqueaVenta) 0 else unidadesAAjustar

                if (revertir && deltaStock > 0 && stockActual < deltaStock) {
                    detalleDevolucionLote =
                        "No se pudo revertir la restauración de ${item.nombre} porque el stock ya cambió"
                    return Transaction.abort()
                }

                val detallesLotesConsumidos = detallesLotesParaMovimiento(productoVenta, unidadesAAjustar)
                    .filter { it.cantidad > 0 }
                val lotesNode = currentData.child("lotes")

                if (detallesLotesConsumidos.isNotEmpty()) {
                    val lotesEncontrados = mutableListOf<Pair<MutableData, LoteConsumoDetalleCaja>>()
                    val lotesNoEncontrados = mutableListOf<String>()

                    detallesLotesConsumidos.forEach { detalle ->
                        val loteAjuste = lotesNode.children.firstOrNull { loteChild ->
                            loteChild.key?.equals(detalle.clave, ignoreCase = true) == true
                        } ?: lotesNode.children.firstOrNull { loteChild ->
                            val numero = loteChild.child("numero").value?.toString().orEmpty().trim()
                            numero.equals(detalle.numero, ignoreCase = true)
                        }

                        if (loteAjuste != null) {
                            lotesEncontrados += loteAjuste to detalle
                        } else {
                            lotesNoEncontrados += detalle.numero.ifBlank { detalle.clave }
                        }
                    }

                    loteRepuestoExacto = lotesNoEncontrados.isEmpty()
                    if (lotesNoEncontrados.isNotEmpty()) {
                        detalleDevolucionLote = if (revertir) {
                            "No se encontraron los lotes ${lotesNoEncontrados.joinToString(", ")} para revertir una devolución total incompleta"
                        } else {
                            "No se encontraron los lotes ${lotesNoEncontrados.joinToString(", ")} para devolver ${item.nombre} con exactitud"
                        }
                        return Transaction.abort()
                    }

                    val campoCantidad = if (motivoBloqueaVenta) "cantidadBloqueada" else "cantidad"
                    lotesEncontrados.forEach { (loteAjuste, detalle) ->
                        val cantidadNode = loteAjuste.child(campoCantidad)
                        val cantidadActual = resolverDouble(cantidadNode.value)
                        val cantidadNueva = if (revertir) {
                            cantidadActual - detalle.cantidad
                        } else {
                            cantidadActual + detalle.cantidad
                        }

                        if (cantidadNueva < 0.0) {
                            detalleDevolucionLote = if (revertir) {
                                "No se pudo revertir la devolución total de ${item.nombre} en el lote ${detalle.numero.ifBlank { detalle.clave }}"
                            } else {
                                "No se pudo devolver ${item.nombre} al lote ${detalle.numero.ifBlank { detalle.clave }}"
                            }
                            return Transaction.abort()
                        }

                        cantidadNode.value = cantidadNueva

                        if (motivoBloqueaVenta) {
                            if (revertir) {
                                if (cantidadNueva <= 0.0) {
                                    loteAjuste.child("motivoBloqueo").value = ""
                                    loteAjuste.child("timestampUltimoBloqueo").value = 0L
                                }
                            } else {
                                loteAjuste.child("motivoBloqueo").value =
                                    motivoBloqueoRegistrado(motivoDevolucion)
                                loteAjuste.child("timestampUltimoBloqueo").value = timestampBloqueo
                                limpiarSeleccionManualSiCoincideConLoteBloqueado(currentData, loteAjuste)
                            }
                        } else if (!revertir) {
                            val estadoNode = loteAjuste.child("estado")
                            val estadoActual = estadoNode.value?.toString().orEmpty()
                            if (estadoActual.equals("agotado", ignoreCase = true)) {
                                estadoNode.value = "activo"
                            }
                        }
                    }
                } else {
                    val loteNumero = productoVenta.loteNumero.trim()
                    val loteClaveConsumida = productoVenta.loteClaveConsumida.trim()
                    if (loteNumero.isNotBlank() || loteClaveConsumida.isNotBlank()) {
                        val loteAjuste = if (loteClaveConsumida.isNotBlank()) {
                            lotesNode.children.firstOrNull { loteChild ->
                                loteChild.key?.equals(loteClaveConsumida, ignoreCase = true) == true
                            }
                        } else {
                            null
                        } ?: lotesNode.children.firstOrNull { loteChild ->
                            val numero = loteChild.child("numero").value?.toString().orEmpty().trim()
                            loteChild.key?.equals(loteNumero, ignoreCase = true) == true ||
                                numero.equals(loteNumero, ignoreCase = true)
                        }

                        if (loteAjuste == null) {
                            loteRepuestoExacto = false
                            detalleDevolucionLote = if (revertir) {
                                "No se encontró el lote consumido ${productoVenta.loteNumero.ifBlank { productoVenta.loteClaveConsumida }} para revertir una devolución total incompleta"
                            } else {
                                "No se encontró el lote consumido ${productoVenta.loteNumero.ifBlank { productoVenta.loteClaveConsumida }} para devolver ${item.nombre}"
                            }
                            return Transaction.abort()
                        }

                        val campoCantidad = if (motivoBloqueaVenta) "cantidadBloqueada" else "cantidad"
                        val cantidadNode = loteAjuste.child(campoCantidad)
                        val cantidadActual = resolverDouble(cantidadNode.value)
                        val cantidadNueva = if (revertir) {
                            cantidadActual - unidadesAAjustar
                        } else {
                            cantidadActual + unidadesAAjustar
                        }

                        if (cantidadNueva < 0.0) {
                            detalleDevolucionLote = if (revertir) {
                                "No se pudo revertir la devolución total de ${item.nombre} en su lote de origen"
                            } else {
                                "No se pudo devolver ${item.nombre} a su lote de origen"
                            }
                            return Transaction.abort()
                        }

                        cantidadNode.value = cantidadNueva
                        loteRepuestoExacto = true

                        if (motivoBloqueaVenta) {
                            if (revertir) {
                                if (cantidadNueva <= 0.0) {
                                    loteAjuste.child("motivoBloqueo").value = ""
                                    loteAjuste.child("timestampUltimoBloqueo").value = 0L
                                }
                            } else {
                                loteAjuste.child("motivoBloqueo").value =
                                    motivoBloqueoRegistrado(motivoDevolucion)
                                loteAjuste.child("timestampUltimoBloqueo").value = timestampBloqueo
                                limpiarSeleccionManualSiCoincideConLoteBloqueado(currentData, loteAjuste)
                            }
                        }
                    }
                }

                if (deltaStock > 0) {
                    val stockNuevo = if (revertir) stockActual - deltaStock else stockActual + deltaStock
                    if (stockNuevo < 0) {
                        detalleDevolucionLote =
                            "No se pudo ajustar el stock global de ${item.nombre} durante la devolución total"
                        return Transaction.abort()
                    }
                    stockNode.value = stockNuevo.toString()
                    stockFinalResultado = stockNuevo
                } else {
                    stockFinalResultado = stockActual
                }

                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    onError("Error al ajustar la devolución de ${item.nombre}: ${error.message}")
                    return
                }

                if (!committed) {
                    onError(
                        detalleDevolucionLote.ifBlank {
                            if (revertir) {
                                "No se pudo revertir la devolución total de ${item.nombre}"
                            } else {
                                "No se pudo restaurar ${item.nombre} en su lote exacto de origen"
                            }
                        }
                    )
                    return
                }

                val stockFinal = currentData?.child("cantidadinicial")?.value?.toString()?.toIntOrNull()
                    ?: stockFinalResultado
                onSuccess(
                    ResultadoRestauracionDevolucionUi(
                        item = item,
                        stockFinal = stockFinal,
                        loteRepuestoExacto = loteRepuestoExacto,
                        detalleDevolucionLote = detalleDevolucionLote
                    )
                )
            }
        })
    }

    private fun procesarRestauracionDevolucionTotalSegura(
        items: List<ItemDevolucionUi>,
        index: Int,
        motivoDevolucion: String,
        motivoBloqueaVenta: Boolean,
        resultados: MutableList<ResultadoRestauracionDevolucionUi>,
        onSuccess: (List<ResultadoRestauracionDevolucionUi>) -> Unit,
        onError: (String, List<ResultadoRestauracionDevolucionUi>) -> Unit
    ) {
        if (index >= items.size) {
            onSuccess(resultados.toList())
            return
        }

        val item = items[index]
        ajustarInventarioDevolucionItemExacta(
            item = item,
            motivoDevolucion = motivoDevolucion,
            motivoBloqueaVenta = motivoBloqueaVenta,
            revertir = false,
            onSuccess = { resultado ->
                resultados += resultado
                procesarRestauracionDevolucionTotalSegura(
                    items = items,
                    index = index + 1,
                    motivoDevolucion = motivoDevolucion,
                    motivoBloqueaVenta = motivoBloqueaVenta,
                    resultados = resultados,
                    onSuccess = onSuccess,
                    onError = onError
                )
            },
            onError = { mensaje ->
                onError(mensaje, resultados.toList())
            }
        )
    }

    private fun revertirRestauracionDevolucionTotalSegura(
        resultados: List<ResultadoRestauracionDevolucionUi>,
        index: Int = 0,
        motivoDevolucion: String,
        motivoBloqueaVenta: Boolean,
        onComplete: () -> Unit
    ) {
        if (index >= resultados.size) {
            onComplete()
            return
        }

        val resultado = resultados[index]
        ajustarInventarioDevolucionItemExacta(
            item = resultado.item,
            motivoDevolucion = motivoDevolucion,
            motivoBloqueaVenta = motivoBloqueaVenta,
            revertir = true,
            onSuccess = {
                revertirRestauracionDevolucionTotalSegura(
                    resultados = resultados,
                    index = index + 1,
                    motivoDevolucion = motivoDevolucion,
                    motivoBloqueaVenta = motivoBloqueaVenta,
                    onComplete = onComplete
                )
            },
            onError = {
                revertirRestauracionDevolucionTotalSegura(
                    resultados = resultados,
                    index = index + 1,
                    motivoDevolucion = motivoDevolucion,
                    motivoBloqueaVenta = motivoBloqueaVenta,
                    onComplete = onComplete
                )
            }
        )
    }

    private fun registrarResultadosRestauracionDevolucion(
        resultados: List<ResultadoRestauracionDevolucionUi>,
        idVenta: String,
        idDevolucion: String,
        motivoDevolucion: String,
        motivoBloqueaVenta: Boolean
    ) {
        resultados.forEach { resultado ->
            val item = resultado.item
            val producto = item.productoVenta
            val unidades = item.cantidadADevolver * item.unidadesPorPresentacion.coerceAtLeast(1)
            if (unidades <= 0) return@forEach

            if (resultado.loteRepuestoExacto &&
                (producto.lotesConsumidosDetalle.isNotEmpty() || producto.loteNumero.trim().isNotBlank())
            ) {
                if (motivoBloqueaVenta) {
                    registrarMovimientoBloqueoDevolucionEnLote(
                        productoCaja = producto,
                        cantidadUnidades = unidades,
                        idVenta = idVenta,
                        motivoDevolucion = motivoDevolucion
                    )
                } else {
                    registrarMovimientoDevolucionEnLote(
                        productoCaja = producto,
                        cantidadUnidades = unidades,
                        idVenta = idVenta
                    )
                }
            }

            registrarMovimientosInventarioPorDevolucion(
                producto = producto,
                cantidadRepuesta = if (motivoBloqueaVenta) 0 else unidades,
                cantidadBloqueada = if (motivoBloqueaVenta) unidades else 0,
                stockAntes = if (motivoBloqueaVenta) {
                    resultado.stockFinal
                } else {
                    (resultado.stockFinal - unidades).coerceAtLeast(0)
                },
                idVenta = idVenta,
                idDevolucion = idDevolucion,
                motivoDevolucion = motivoDevolucion,
                loteRepuestoExacto = resultado.loteRepuestoExacto,
                detalleDevolucionLote = resultado.detalleDevolucionLote,
                bloqueoVenta = motivoBloqueaVenta
            )
        }
    }

    private fun calcularAjusteFinancieroDevolucion(
        totalDevueltoProducto: Double,
        sustitucion: ProductoSustitucionUi?,
        tipoResolucion: String
    ): AjusteFinancieroDevolucion {
        val totalSustitucion = if (normalizarTipoResolucionDevolucion(tipoResolucion) == "cambio_producto") {
            sustitucion?.subtotal ?: 0.0
        } else {
            0.0
        }
        val diferenciaCliente = totalSustitucion - totalDevueltoProducto
        val montoDevueltoCliente = if (normalizarTipoResolucionDevolucion(tipoResolucion) == "cambio_producto") {
            (-diferenciaCliente).coerceAtLeast(0.0)
        } else {
            totalDevueltoProducto.coerceAtLeast(0.0)
        }
        val montoCobradoCliente = if (normalizarTipoResolucionDevolucion(tipoResolucion) == "cambio_producto") {
            diferenciaCliente.coerceAtLeast(0.0)
        } else {
            0.0
        }
        val deltaVentasTurno = if (normalizarTipoResolucionDevolucion(tipoResolucion) == "cambio_producto") {
            diferenciaCliente
        } else {
            -totalDevueltoProducto
        }

        return AjusteFinancieroDevolucion(
            totalDevueltoProducto = totalDevueltoProducto,
            totalSustitucion = totalSustitucion,
            diferenciaCliente = diferenciaCliente,
            montoDevueltoCliente = montoDevueltoCliente,
            montoCobradoCliente = montoCobradoCliente,
            deltaVentasTurno = deltaVentasTurno
        )
    }

    private fun construirTextoResumenDiferencia(ajuste: AjusteFinancieroDevolucion): String {
        return when {
            ajuste.totalSustitucion <= 0.0 -> "Se devolverá ${MonedaHelper.formatear(ajuste.montoDevueltoCliente)} al cliente."
            ajuste.montoCobradoCliente > 0.0 ->
                "El cliente debe pagar ${MonedaHelper.formatear(ajuste.montoCobradoCliente)} de diferencia."
            ajuste.montoDevueltoCliente > 0.0 ->
                "Debes devolver ${MonedaHelper.formatear(ajuste.montoDevueltoCliente)} al cliente."
            else -> "El cambio queda sin diferencia económica."
        }
    }

    private fun normalizarTipoResolucionDevolucion(tipo: String): String {
        val tipoNormalizado = normalizarTextoLibre(tipo)
        return when {
            tipoNormalizado.isBlank() -> ""
            tipoNormalizado == "cambio_producto" -> "cambio_producto"
            tipoNormalizado.contains("cambio") && tipoNormalizado.contains("producto") -> "cambio_producto"
            tipoNormalizado.contains("cambiar") && tipoNormalizado.contains("producto") -> "cambio_producto"
            tipoNormalizado == "dinero_devuelto" -> "dinero_devuelto"
            tipoNormalizado.contains("devolver") && tipoNormalizado.contains("dinero") -> "dinero_devuelto"
            tipoNormalizado.contains("dinero") && tipoNormalizado.contains("devuelto") -> "dinero_devuelto"
            else -> ""
        }
    }

    private fun textoTipoResolucionDevolucion(tipo: String): String {
        return when (normalizarTipoResolucionDevolucion(tipo)) {
            "cambio_producto" -> "Devuelto por cambio"
            "dinero_devuelto" -> "Devuelto con dinero"
            else -> "Devuelto"
        }
    }

    private fun construirItemVentaIdDevolucion(
        prodSnap: DataSnapshot,
        producto: ProductoCaja
    ): String {
        return prodSnap.key.orEmpty().trim().ifBlank {
            producto.id.trim().ifBlank {
                "${producto.indiceProducto.trim()}_${producto.presentacion.trim()}"
            }
        }
    }

    private fun leerEstadosDevolucionPorItem(
        devolucionSnap: DataSnapshot,
        productosSnap: DataSnapshot
    ): Map<String, EstadoItemDevolucionUi> {
        val productosPorItemId = productosSnap.children.mapNotNull { prodSnap ->
            val producto = prodSnap.getValue(ProductoCaja::class.java) ?: return@mapNotNull null
            val itemId = construirItemVentaIdDevolucion(prodSnap, producto)
            itemId.takeIf { it.isNotBlank() }?.let { it to producto }
        }.toMap()

        val itemsSnapshot = devolucionSnap.child("items")
        if (itemsSnapshot.exists()) {
            return itemsSnapshot.children.mapNotNull { itemSnap ->
                val itemId = itemSnap.key.orEmpty().trim()
                if (itemId.isBlank()) return@mapNotNull null
                val cantidadVendida = resolverEnteroFlexible(itemSnap.child("cantidadVendida").value)
                val cantidadDevuelta = resolverEnteroFlexible(itemSnap.child("cantidadDevuelta").value)
                val estado = itemSnap.child("estado").value?.toString().orEmpty().trim()
                val tipoResolucion = itemSnap.child("tipoResolucion").value?.toString().orEmpty().trim()
                    .ifBlank { itemSnap.child("tipoResolucionUltima").value?.toString().orEmpty().trim() }
                val subtotalDevuelto = resolverDoubleFlexible(
                    itemSnap.child("subtotalDevueltoAcumulado").value
                ).takeIf { it > 0.0 } ?: resolverDoubleFlexible(itemSnap.child("subtotalDevuelto").value)
                val lotesDevueltosDetalle = leerDetalleLotesDesdeSnapshot(
                    itemSnap.child("lotesDevueltosDetalle")
                ).ifEmpty {
                    val cantidadDevueltaPresentaciones = resolverEnteroFlexible(itemSnap.child("cantidadDevuelta").value)
                    productosPorItemId[itemId]?.let { producto ->
                        // Obtenemos el factor de conversión (ej. 10 unidades por blister)
                        val unidadesPorPresentacion = producto.unidadesPorPresentacion.toIntOrNull()?.coerceAtLeast(1) ?: 1

                        inferirLotesDevueltosLegacy(
                            producto = producto,
                            unidadesDevueltas = cantidadDevueltaPresentaciones * unidadesPorPresentacion // FIX: Ahora envía unidades reales
                        )
                    }.orEmpty()
                }
                itemId to EstadoItemDevolucionUi(
                    itemVentaId = itemId,
                    cantidadVendida = cantidadVendida,
                    cantidadDevuelta = cantidadDevuelta,
                    estado = estado.ifBlank {
                        if (cantidadDevuelta in 1 until cantidadVendida) "parcialmente_devuelto"
                        else if (cantidadVendida > 0 && cantidadDevuelta >= cantidadVendida) "devuelto"
                        else "normal"
                    },
                    tipoResolucion = tipoResolucion,
                    resuelto = itemSnap.child("resuelto").getValue(Boolean::class.java)
                        ?: (cantidadVendida > 0 && cantidadDevuelta >= cantidadVendida),
                    subtotalDevuelto = subtotalDevuelto,
                    lotesDevueltosDetalle = lotesDevueltosDetalle
                )
            }.toMap(LinkedHashMap())
        }

        val estadoLegacy = devolucionSnap.child("estado").value?.toString().orEmpty().trim()
        if (!estadoLegacy.equals("devuelta", ignoreCase = true)) return emptyMap()

        val tipoResolucionLegacy = normalizarTipoResolucionDevolucion(
            devolucionSnap.child("tipoResolucion").value?.toString().orEmpty()
        )

        return productosSnap.children.mapNotNull { prodSnap ->
            val producto = prodSnap.getValue(ProductoCaja::class.java) ?: return@mapNotNull null
            val itemId = construirItemVentaIdDevolucion(prodSnap, producto)
            val cantidadVendida = producto.cantidad.toIntOrNull() ?: 0
            if (itemId.isBlank() || cantidadVendida <= 0) return@mapNotNull null
            itemId to EstadoItemDevolucionUi(
                itemVentaId = itemId,
                cantidadVendida = cantidadVendida,
                cantidadDevuelta = cantidadVendida,
                estado = "devuelto",
                tipoResolucion = tipoResolucionLegacy,
                resuelto = true,
                subtotalDevuelto = producto.total.toDoubleOrNull() ?: 0.0,
                lotesDevueltosDetalle = inferirLotesDevueltosLegacy(
                    producto = producto,
                    unidadesDevueltas = cantidadVendida *
                        (producto.unidadesPorPresentacion.toIntOrNull()?.coerceAtLeast(1) ?: 1)
                )
            )
        }.toMap(LinkedHashMap())
    }

    private fun resolverEstadoVentaDevolucion(
        productosSnap: DataSnapshot,
        estadosPorItem: Map<String, EstadoItemDevolucionUi>
    ): EstadoVentaDevolucionUi {
        var totalItems = 0
        var itemsDevueltos = 0
        var itemsPendientes = 0

        productosSnap.children.forEach { prodSnap ->
            val producto = prodSnap.getValue(ProductoCaja::class.java) ?: return@forEach
            val itemId = construirItemVentaIdDevolucion(prodSnap, producto)
            val cantidadVendida = producto.cantidad.toIntOrNull() ?: 0
            if (itemId.isBlank() || cantidadVendida <= 0) return@forEach
            totalItems += 1
            val estado = estadosPorItem[itemId]
            val cantidadDevuelta = estado?.cantidadDevuelta ?: 0
            if (cantidadDevuelta > 0) itemsDevueltos += 1
            if (cantidadDevuelta < cantidadVendida) itemsPendientes += 1
        }

        if (itemsDevueltos == 0 || totalItems == 0) {
            return EstadoVentaDevolucionUi(
                estadoGeneral = "vigente",
                textoVisible = "Vigente",
                itemsPendientes = totalItems,
                itemsDevueltos = 0,
                esTotal = false
            )
        }

        if (itemsPendientes <= 0) {
            return EstadoVentaDevolucionUi(
                estadoGeneral = "devuelta_total",
                textoVisible = "Devuelta total",
                itemsPendientes = 0,
                itemsDevueltos = itemsDevueltos,
                esTotal = true
            )
        }

        return EstadoVentaDevolucionUi(
            estadoGeneral = "devolucion_parcial",
            textoVisible = "Devolucion parcial",
            itemsPendientes = itemsPendientes,
            itemsDevueltos = itemsDevueltos,
            esTotal = false
        )
    }

    private fun mostrarBottomSheetDevolucion() {
        if (estaCajaEnModoSoloVista(notificar = true)) return
        if (!turnoAbierto || idTurnoActivo.isBlank()) {
            mostrarToastSeguro("No hay un turno abierto")
            return
        }
        if (!isAdded || _binding == null) return
        // CORRECCIÓN: verificarHoraAntesDeOperar para que coincida con tu función original
        if (!verificarHoraAntesDeOperar("procesar devoluciones de productos")) return

        dialogoDevolucionActual?.dismiss()

        val idCajaOperativa = obtenerIdCajaOperativa()
        val fechaBusqueda = fechaTurnoActivo.ifBlank { obtenerFechaActual() }

        mostrarLoadingOverlayCaja("Cargando...", "Buscando ventas disponibles para devolver")

        database.reference
            .child("VentasPorCajera")
            .child(idCajaOperativa)
            .child(fechaBusqueda)
            .get()
            .addOnSuccessListener { snapshot ->
                ocultarLoadingOverlayCaja()
                if (!isAdded || _binding == null) return@addOnSuccessListener

                val sheetBinding = BottomSheetVentasDevolucionBinding.inflate(layoutInflater)
                val bottomSheet = BottomSheetDialog(requireContext())
                bottomSheet.setContentView(sheetBinding.root)

                bottomSheet.setOnDismissListener {
                    if (dialogoDevolucionActual === bottomSheet) {
                        dialogoDevolucionActual = null
                    }
                }
                dialogoDevolucionActual = bottomSheet
                sheetBinding.progressDevolucion.visibility = View.GONE

                val ventasMasterList = mutableListOf<VentaDevolucionUi>()
                snapshot.children.forEach { ventaSnap ->
                    val idTurnoVenta = ventaSnap.child("infoVenta").child("idTurno").getValue(String::class.java).orEmpty()
                    if (idTurnoVenta != idTurnoActivo) return@forEach

                    val estado = ventaSnap.child("infoVenta").child("estado").getValue(String::class.java).orEmpty()
                    if (estado != "finalizada") return@forEach

                    val productosSnap = ventaSnap.child("productos")
                    val cantProd = productosSnap.childrenCount.toInt()
                    val estadoDevolucion = resolverEstadoVentaDevolucion(
                        productosSnap = productosSnap,
                        estadosPorItem = leerEstadosDevolucionPorItem(ventaSnap.child("devolucion"), productosSnap)
                    )

                    ventasMasterList.add(
                        VentaDevolucionUi(
                            idVenta = ventaSnap.key.orEmpty(),
                            fecha = ventaSnap.child("infoVenta").child("fecha").getValue(String::class.java).orEmpty(),
                            hora = ventaSnap.child("infoVenta").child("hora").getValue(String::class.java).orEmpty(),
                            total = ventaSnap.child("infoVenta").child("total").getValue(String::class.java)?.toDoubleOrNull() ?: 0.0,
                            metodoPago = ventaSnap.child("infoVenta").child("metodoPago").getValue(String::class.java).orEmpty(),
                            tipoComprobante = ventaSnap.child("infoVenta").child("tipoComprobante").getValue(String::class.java).orEmpty(),
                            nombreCliente = ventaSnap.child("comprobante").child("nombreCliente").getValue(String::class.java).orEmpty(),
                            cantidadProductos = cantProd,
                            estadoDevolucion = estadoDevolucion.estadoGeneral,
                            itemsPendientesDevolucion = estadoDevolucion.itemsPendientes
                        )
                    )
                }

                ventasMasterList.sortByDescending { it.hora }

                // FUNCIÓN LOCAL: Renderiza de forma reactiva la lista que le pasemos
                fun pintarVentasEnLayout(listaAFiltrar: List<VentaDevolucionUi>) {
                    val container = sheetBinding.containerVentasDevolucion
                    container.removeAllViews() // Limpiar la lista previa de la pantalla

                    if (listaAFiltrar.isEmpty()) {
                        sheetBinding.layoutSinVentasDevolucion.visibility = View.VISIBLE
                        sheetBinding.scrollVentasDevolucion.visibility = View.GONE
                        return
                    }

                    sheetBinding.layoutSinVentasDevolucion.visibility = View.GONE
                    sheetBinding.scrollVentasDevolucion.visibility = View.VISIBLE

                    listaAFiltrar.forEach { venta ->
                        val rowBinding = ItemVentaDevolucionBinding.inflate(layoutInflater, container, false)
                        rowBinding.tvHoraVentaDev.text = venta.hora.ifBlank { "—" }
                        rowBinding.tvTotalVentaDev.text = MonedaHelper.formatear(venta.total)
                        rowBinding.tvMetodoPagoVentaDev.text = venta.metodoPago.substringBefore(" (").substringBefore(":").trim().ifBlank { "—" }

                        rowBinding.tvClienteVentaDev.text = buildString {
                            append(venta.tipoComprobante.ifBlank { "Boleta simple" })
                            if (venta.nombreCliente.isNotBlank()) append(" · ${venta.nombreCliente}")
                            append(" · ${venta.cantidadProductos} prod.")
                        }

                        // Gestión de insignias de devolución (Devuelta total / Parcial)
                        val estadoVisible = when (venta.estadoDevolucion) {
                            "devolucion_parcial" -> "Devolución parcial"
                            "devuelta_total" -> "Devuelta total"
                            else -> ""
                        }

                        if (estadoVisible.isNotBlank()) {
                            rowBinding.tvBadgeDevuelta.visibility = View.VISIBLE
                            rowBinding.tvBadgeDevuelta.text = estadoVisible
                            val esTotal = venta.estadoDevolucion == "devuelta_total"
                            rowBinding.tvBadgeDevuelta.setTextColor(Color.parseColor(if (esTotal) "#DC2626" else "#9A6400"))
                            rowBinding.tvBadgeDevuelta.background = android.graphics.drawable.GradientDrawable().apply {
                                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                                cornerRadius = 50 * resources.displayMetrics.density
                                setColor(Color.parseColor(if (esTotal) "#FEE2E2" else "#FEF3C7"))
                            }
                            if (esTotal) {
                                rowBinding.root.alpha = 0.6f
                                rowBinding.root.isClickable = false
                            }
                        }

                        if (venta.estadoDevolucion != "devuelta_total") {
                            rowBinding.root.setOnClickListener {
                                bottomSheet.dismiss()
                                cargarProductosYMostrarDialogoDevolucion(venta, idCajaOperativa, fechaBusqueda)
                            }
                        }
                        container.addView(rowBinding.root)
                    }
                }

                // Carga inicial completa de la lista
                pintarVentasEnLayout(ventasMasterList)

                // LISTENER REACTIVO: Filtra la lista original basándose en lo que escribe el cajero
                sheetBinding.etBuscarDevolucion?.doAfterTextChanged { editable ->
                    val query = editable?.toString()?.trim()?.lowercase(Locale.getDefault()).orEmpty()

                    if (query.isEmpty()) {
                        pintarVentasEnLayout(ventasMasterList)
                    } else {
                        val ventasFiltradas = ventasMasterList.filter { venta ->
                            val coincideId = venta.idVenta.lowercase(Locale.getDefault()).contains(query)
                            val coincideMonto = venta.total.toString().contains(query)
                            val coincideCliente = venta.nombreCliente.lowercase(Locale.getDefault()).contains(query)

                            coincideId || coincideMonto || coincideCliente
                        }
                        pintarVentasEnLayout(ventasFiltradas)
                    }
                }

                sheetBinding.tvSubtituloDevolucion.text = "Turno del $fechaBusqueda"
                bottomSheet.show()

                bottomSheet.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.let { sheet ->
                    BottomSheetBehavior.from(sheet).apply {
                        state = BottomSheetBehavior.STATE_EXPANDED
                        skipCollapsed = true
                    }
                }
            }
            .addOnFailureListener {
                ocultarLoadingOverlayCaja()
                if (isAdded) mostrarToastSeguro("No se pudieron cargar las ventas")
            }
    }

    private fun cargarProductosYMostrarDialogoDevolucion(
        venta: VentaDevolucionUi,
        idCajaOperativa: String,
        fechaBusqueda: String
    ) {
        database.reference
            .child("VentasPorCajera")
            .child(idCajaOperativa)
            .child(fechaBusqueda)
            .child(venta.idVenta)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!isAdded || _binding == null) return@addOnSuccessListener

                val productosSnap = snapshot.child("productos")
                val estadosPorItem = leerEstadosDevolucionPorItem(
                    devolucionSnap = snapshot.child("devolucion"),
                    productosSnap = productosSnap
                )
                val items = mutableListOf<ItemDevolucionUi>()
                productosSnap.children.forEach { prodSnap ->
                    val prod = prodSnap.getValue(ProductoCaja::class.java) ?: return@forEach
                    val cantidad = prod.cantidad.toIntOrNull() ?: 0
                    if (cantidad <= 0) return@forEach
                    val unidades = prod.unidadesPorPresentacion.toIntOrNull()?.coerceAtLeast(1) ?: 1
                    val subtotal = prod.total.toDoubleOrNull() ?: 0.0
                    val itemVentaId = construirItemVentaIdDevolucion(prodSnap, prod)
                    val estadoPrevio = estadosPorItem[itemVentaId]
                    val cantidadYaDevuelta = estadoPrevio?.cantidadDevuelta ?: 0
                    val cantidadDisponible = (cantidad - cantidadYaDevuelta).coerceAtLeast(0)
                    val lotesConsumidosNormalizados = prod.lotesConsumidosDetalle
                        .mapNotNull { (clave, detalle) ->
                            val claveNormalizada = detalle.clave.trim().ifBlank { clave.trim() }
                            if (claveNormalizada.isBlank() || detalle.cantidad <= 0) {
                                null
                            } else {
                                claveNormalizada to detalle.copy(clave = claveNormalizada)
                            }
                        }
                        .toMap(LinkedHashMap())
                    val productoVenta = prod.copy(
                        lotesConsumidosDetalle = lotesConsumidosNormalizados
                    )
                    items.add(
                        ItemDevolucionUi(
                            itemVentaId = itemVentaId,
                            indiceProducto = prod.indiceProducto.trim(),
                            nombre = prod.nombre.orEmpty().ifBlank { "Producto" },
                            presentacion = prod.presentacion.ifBlank { "unidades" },
                            cantidadVendida = cantidad,
                            unidadesPorPresentacion = unidades,
                            subtotalOriginal = subtotal,
                            productoVenta = productoVenta,
                            cantidadYaDevuelta = cantidadYaDevuelta,
                            cantidadDisponibleDevolucion = cantidadDisponible,
                            lotesDevueltosPrevios = estadoPrevio?.lotesDevueltosDetalle.orEmpty(),
                            estadoDevolucion = estadoPrevio?.estado.orEmpty(),
                            tipoResolucionActual = estadoPrevio?.tipoResolucion.orEmpty(),
                            resuelto = cantidadDisponible <= 0,
                            cantidadADevolver = cantidadDisponible,
                            seleccionado = cantidadDisponible > 0
                        )
                    )
                }

                if (items.isEmpty()) {
                    mostrarToastSeguro("Esta venta no tiene productos registrados")
                    return@addOnSuccessListener
                }

                if (items.none { it.cantidadDisponibleDevolucion > 0 }) {
                    mostrarToastSeguro("Todos los productos de esta venta ya fueron resueltos")
                    return@addOnSuccessListener
                }

                mostrarDialogoConfirmarDevolucionFlexible(venta, items, idCajaOperativa, fechaBusqueda)
            }
            .addOnFailureListener {
                if (isAdded) mostrarToastSeguro("No se pudieron cargar los productos")
            }
    }




    private fun mostrarDialogoConfirmarDevolucionFlexible(venta: VentaDevolucionUi, items: List<ItemDevolucionUi>, idCajaOperativa: String, fechaBusqueda: String) {
        dialogoDevolucionActual?.dismiss()
        val dialogBinding = BottomSheetConfirmarDevolucionBinding.inflate(layoutInflater)

        // Inicialización ÚNICA y segura del diálogo al principio de la función
        val bottomSheet = BottomSheetDialog(requireContext()).apply {
            setContentView(dialogBinding.root)
            setOnDismissListener {
                if (dialogoDevolucionActual === this) {
                    dialogoDevolucionActual = null
                }
                // Cancelar procesos y liberar referencias para evitar fugas de memoria
                cacheTrazabilidadDevolucion.clear()
                onProductoSustitutoSeleccionadoDevolucion = null
                handlerBusquedaDocumento.removeCallbacksAndMessages(null)
            }
        }
        dialogoDevolucionActual = bottomSheet

        val horaTexto = venta.hora.ifBlank { "sin hora" }
        dialogBinding.tvInfoVentaDevolucion.text = "Venta - $horaTexto"
        dialogBinding.tvTotalOriginalMontoDev.text = MonedaHelper.formatear(venta.total)
        dialogBinding.tvAvisoStockDevolucion.text =
            "El stock de los productos seleccionados se restaurara automaticamente al inventario."

        val opcionesResolucion = arrayOf("Devolver dinero", "Cambiar por otro producto")
        dialogBinding.actvTipoResolucionDevolucion.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, opcionesResolucion)
        )
        dialogBinding.actvTipoResolucionDevolucion.setText(opcionesResolucion.first(), false)

        dialogBinding.actvTipoResolucionDevolucion.keyListener = null // Bloquea el teclado
        dialogBinding.actvTipoResolucionDevolucion.isCursorVisible = false // Oculta el cursor de texto

        var tipoResolucionSeleccionado = normalizarTipoResolucionDevolucion(opcionesResolucion.first())
        dialogBinding.actvTipoResolucionDevolucion.setOnClickListener {
            dialogBinding.actvTipoResolucionDevolucion.showDropDown()
        }

        val filasPorItem = linkedMapOf<String, ItemProductoDevolucionBinding>()
        val inventarioSustitucion = mutableListOf<MoldeProductos>()
        var productoSustitutoSeleccionado: MoldeProductos? = null
        var presentacionSustitutaSeleccionada: PresentacionProducto? = null
        var sustitucionActual: ProductoSustitucionUi? = null
        val metodosPagoDiferenciaDisponibles = mutableListOf<MetodoPagoConfig>()
        var metodoPagoDiferenciaSeleccionado: MetodoPagoConfig? = null
        var montoDiferenciaEsperado = 0.0
        var ultimoTipoResolucionUi = tipoResolucionSeleccionado

        fun totalSeleccionadoActual(): Double {
            return items.filter { it.seleccionado }.sumOf { subtotalDevolucionItem(it) }
        }

        fun metodosPagoFallbackDiferencia(): List<MetodoPagoConfig> {
            return listOf(
                MetodoPagoConfig(titulo = "Efectivo", categoria = "efectivo", activo = true, permiteVuelto = true),
                MetodoPagoConfig(titulo = "Transferencia", categoria = "transferencia_bancaria", activo = true),
                MetodoPagoConfig(titulo = "Pago movil", categoria = "billetera_digital", activo = true),
                MetodoPagoConfig(titulo = "Tarjeta", categoria = "tarjeta", activo = true),
                MetodoPagoConfig(titulo = "Otro", categoria = "otro", activo = true)
            )
        }

        fun configurarMetodosPagoDiferencia(metodos: List<MetodoPagoConfig>) {
            metodosPagoDiferenciaDisponibles.clear()
            metodosPagoDiferenciaDisponibles.addAll(
                metodos
                    .filter { it.activo && !it.esMixtoLike() }
                    .sortedBy { it.orden }
                    .ifEmpty { metodosPagoFallbackDiferencia() }
            )

            val titulos = metodosPagoDiferenciaDisponibles.map {
                it.titulo.trim().ifBlank { it.categoria.ifBlank { "Otro" } }
            }
            dialogBinding.actvMetodoPagoDiferenciaDevolucion.setAdapter(
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, titulos)
            )

            val textoActual = dialogBinding.actvMetodoPagoDiferenciaDevolucion.text?.toString()?.trim().orEmpty()
            metodoPagoDiferenciaSeleccionado = metodosPagoDiferenciaDisponibles.firstOrNull {
                it.titulo.trim().equals(textoActual, ignoreCase = true)
            }
        }

        fun resolverMetodoPagoDiferenciaSeleccionado(): MetodoPagoConfig? {
            val actual = metodoPagoDiferenciaSeleccionado
            if (actual != null) return actual
            val texto = dialogBinding.actvMetodoPagoDiferenciaDevolucion.text?.toString()?.trim().orEmpty()
            return metodosPagoDiferenciaDisponibles.firstOrNull {
                it.titulo.trim().equals(texto, ignoreCase = true)
            }
        }

        fun limpiarErroresPagoDiferencia() {
            dialogBinding.layoutMetodoPagoDiferenciaDevolucion.error = null
            dialogBinding.layoutMontoPagoDiferenciaDevolucion.error = null
        }

        fun actualizarBloquePagoDiferencia(ajuste: AjusteFinancieroDevolucion?) {
            val mostrarBloque = tipoResolucionSeleccionado == "cambio_producto" &&
                    ajuste != null &&
                    ajuste.montoCobradoCliente > 0.0

            dialogBinding.layoutPagoDiferenciaDevolucion.visibility =
                if (mostrarBloque) View.VISIBLE else View.GONE

            if (!mostrarBloque) {
                limpiarErroresPagoDiferencia()
                montoDiferenciaEsperado = 0.0
                metodoPagoDiferenciaSeleccionado = null
                dialogBinding.actvMetodoPagoDiferenciaDevolucion.setText("", false)
                dialogBinding.etMontoPagoDiferenciaDevolucion.setText("")
                dialogBinding.tvAyudaPagoDiferenciaDevolucion.text =
                    "Selecciona el medio de pago y confirma el monto exacto de la diferencia."
                return
            }

            montoDiferenciaEsperado = ajuste!!.montoCobradoCliente
            if (metodosPagoDiferenciaDisponibles.isEmpty()) {
                configurarMetodosPagoDiferencia(emptyList())
            }
            val montoActual = dialogBinding.etMontoPagoDiferenciaDevolucion.text
                ?.toString()
                ?.trim()
                ?.replace(',', '.')
                ?.toDoubleOrNull()
            if (montoActual == null || kotlin.math.abs(montoActual - ajuste.montoCobradoCliente) > 0.01) {
                dialogBinding.etMontoPagoDiferenciaDevolucion.setText(
                    String.format(Locale.US, "%.2f", ajuste.montoCobradoCliente)
                )
            }

            val metodo = resolverMetodoPagoDiferenciaSeleccionado()
            if (metodo == null && metodosPagoDiferenciaDisponibles.size == 1) {
                metodoPagoDiferenciaSeleccionado = metodosPagoDiferenciaDisponibles.first()
                val tituloUnico = metodoPagoDiferenciaSeleccionado?.titulo?.trim()
                    ?.ifBlank { metodoPagoDiferenciaSeleccionado?.categoria?.ifBlank { "Otro" } ?: "Otro" }
                    ?: ""
                dialogBinding.actvMetodoPagoDiferenciaDevolucion.setText(tituloUnico, false)
            }
            dialogBinding.tvAyudaPagoDiferenciaDevolucion.text = if (metodo != null) {
                "Se cobrara ${MonedaHelper.formatear(ajuste.montoCobradoCliente)} por ${metodo.titulo.ifBlank { metodo.categoria }}."
            } else {
                "Selecciona como pagara el cliente ${MonedaHelper.formatear(ajuste.montoCobradoCliente)} de diferencia."
            }
            limpiarErroresPagoDiferencia()
        }

        fun actualizarResumenSeleccionDevolucion() {
            val seleccionados = items.filter { it.seleccionado && it.cantidadADevolver > 0 }
            dialogBinding.tvDetalleResumenDevolucion.text = when {
                seleccionados.isEmpty() -> {
                    "Selecciona productos y cantidades para calcular el total."
                }
                seleccionados.size == 1 -> {
                    val item = seleccionados.first()
                    "1 producto seleccionado. ${formatearCantidadPresentacionDevolucion(item.cantidadADevolver, item.presentacion)} a devolver."
                }
                else -> {
                    val totalPresentaciones = seleccionados.sumOf { it.cantidadADevolver.coerceAtLeast(0) }
                    "${seleccionados.size} productos seleccionados. Total de presentaciones: $totalPresentaciones."
                }
            }
        }

        fun actualizarResumenFinanciero() {
            val total = totalSeleccionadoActual()
            dialogBinding.tvTotalADevolver.text = MonedaHelper.formatear(total)
            actualizarResumenSeleccionDevolucion()

            if (total <= 0.0) {
                dialogBinding.cardResumenDevolucion.visibility = View.VISIBLE
                dialogBinding.cardResumenDiferenciaDevolucion.visibility = View.GONE
                actualizarBloquePagoDiferencia(null)
                dialogBinding.tvResumenResolucionDevolucion.text = ""
                dialogBinding.tvMontoProductoDevueltoDevolucion.text = ""
                dialogBinding.tvMontoProductoSustitutoDevolucion.text = ""
                dialogBinding.tvLabelDiferenciaDevolucion.text = ""
                dialogBinding.tvValorDiferenciaDevolucion.text = ""
                dialogBinding.tvDiferenciaDevolucion.text = ""
                return
            }

            dialogBinding.cardResumenDiferenciaDevolucion.visibility = View.VISIBLE
            dialogBinding.tvMontoProductoDevueltoDevolucion.text = MonedaHelper.formatear(total)
            if (tipoResolucionSeleccionado == "cambio_producto") {
                dialogBinding.cardResumenDevolucion.visibility = View.VISIBLE
                dialogBinding.tvResumenResolucionDevolucion.text = "Resumen economico"
                dialogBinding.layoutMontoProductoDevueltoDevolucion.visibility = View.VISIBLE
                dialogBinding.layoutMontoProductoSustitutoDevolucion.visibility = View.VISIBLE
                dialogBinding.viewSeparadorDiferenciaDevolucion.visibility = View.VISIBLE
                dialogBinding.layoutValorDiferenciaDevolucion.visibility = View.VISIBLE
                val sustitucion = sustitucionActual
                if (sustitucion == null) {
                    actualizarBloquePagoDiferencia(null)
                    dialogBinding.tvMontoProductoSustitutoDevolucion.text = "-"
                    dialogBinding.tvLabelDiferenciaDevolucion.text = "Diferencia"
                    dialogBinding.tvValorDiferenciaDevolucion.text = "-"
                    dialogBinding.tvDiferenciaDevolucion.text =
                        "Selecciona el producto sustituto, la presentacion y la cantidad para calcular la diferencia del cambio."
                    return
                }

                val ajuste = calcularAjusteFinancieroDevolucion(total, sustitucion, tipoResolucionSeleccionado)
                actualizarBloquePagoDiferencia(ajuste)
                dialogBinding.tvMontoProductoSustitutoDevolucion.text =
                    MonedaHelper.formatear(ajuste.totalSustitucion)
                when {
                    ajuste.montoCobradoCliente > 0.0 -> {
                        dialogBinding.tvLabelDiferenciaDevolucion.text = "Diferencia a cobrar"
                        dialogBinding.tvValorDiferenciaDevolucion.text =
                            MonedaHelper.formatear(ajuste.montoCobradoCliente)
                        dialogBinding.tvDiferenciaDevolucion.text =
                            "El cliente debe pagar este adicional para completar el cambio."
                    }
                    ajuste.montoDevueltoCliente > 0.0 -> {
                        dialogBinding.tvLabelDiferenciaDevolucion.text = "Diferencia a devolver"
                        dialogBinding.tvValorDiferenciaDevolucion.text =
                            MonedaHelper.formatear(ajuste.montoDevueltoCliente)
                        dialogBinding.tvDiferenciaDevolucion.text =
                            "Debes devolver esta diferencia al cliente."
                    }
                    else -> {
                        dialogBinding.tvLabelDiferenciaDevolucion.text = "Diferencia"
                        dialogBinding.tvValorDiferenciaDevolucion.text = MonedaHelper.formatear(0.0)
                        dialogBinding.tvDiferenciaDevolucion.text =
                            "El cambio queda equilibrado, sin diferencia economica."
                    }
                }
            } else {
                dialogBinding.cardResumenDevolucion.visibility = View.GONE
                actualizarBloquePagoDiferencia(null)
                dialogBinding.tvResumenResolucionDevolucion.text = "Resultado"
                dialogBinding.layoutMontoProductoDevueltoDevolucion.visibility = View.GONE
                dialogBinding.layoutMontoProductoSustitutoDevolucion.visibility = View.GONE
                dialogBinding.viewSeparadorDiferenciaDevolucion.visibility = View.GONE
                dialogBinding.layoutValorDiferenciaDevolucion.visibility = View.GONE
                dialogBinding.tvMontoProductoSustitutoDevolucion.text = ""
                dialogBinding.tvLabelDiferenciaDevolucion.text = ""
                dialogBinding.tvValorDiferenciaDevolucion.text = ""
                dialogBinding.tvDiferenciaDevolucion.text =
                    "Se devolveran ${MonedaHelper.formatear(total)} al cliente."
            }
        }

        fun actualizarAvisoStockPorMotivo() {
            val motivo = dialogBinding.etMotivoDevolucion.text?.toString()?.trim().orEmpty()
            dialogBinding.tvAvisoStockDevolucion.text = if (motivoImplicaBloqueoVenta(motivo)) {
                "El motivo indica mal estado. Esta devolucion quedara bloqueada para venta y no regresara al stock vendible."
            } else {
                "El stock de los productos seleccionados se restaurara automaticamente al inventario."
            }
        }

        fun limpiarErroresSustitucion() {
            dialogBinding.layoutPresentacionSustitucionDevolucion.error = null
            dialogBinding.layoutCantidadSustitucionDevolucion.error = null
        }

        fun actualizarResumenProductoSustituto() {
            val producto = productoSustitutoSeleccionado
            if (producto == null) {
                dialogBinding.etProductoSustitucionDevolucion.setText("")
                dialogBinding.tvResumenProductoSustitucionDevolucion.text =
                    "Elige el producto que entregaras al cliente como sustitucion."
                dialogBinding.btnSeleccionarProductoSustitucionDevolucion.text =
                    "Seleccionar producto sustituto"
                return
            }

            dialogBinding.etProductoSustitucionDevolucion.setText(producto.nombre.trim())
            dialogBinding.tvResumenProductoSustitucionDevolucion.text = buildString {
                append(producto.categoria.ifBlank { "Sin categoria" })
                append(" - Stock vendible: ${ProductUtils.stockVendibleProducto(producto).toInt()} ")
                append(producto.unidadbase.ifBlank { "unidades" })
            }
            dialogBinding.btnSeleccionarProductoSustitucionDevolucion.text =
                "Cambiar producto sustituto"
        }

        fun cargarInventarioSustitucion(onLoaded: (() -> Unit)? = null) {
            if (inventarioSustitucion.isNotEmpty()) {
                onLoaded?.invoke()
                return
            }
            database.reference.child("Inventario").child("Productos").get()
                .addOnSuccessListener { snapshot ->
                    val productos = snapshot.children.mapNotNull { child ->
                        child.toMoldeProductoSeguro()
                    }.filter { producto ->
                        producto.indice.trim().isNotBlank() &&
                                ProductUtils.stockVendibleProducto(producto).toInt() > 0
                    }.sortedBy { it.nombre.trim().lowercase(Locale.getDefault()) }

                    inventarioSustitucion.clear()
                    inventarioSustitucion.addAll(productos)
                    onLoaded?.invoke()
                }
                .addOnFailureListener {
                    onLoaded?.invoke()
                }
        }

        fun actualizarPresentacionesSustitucion(producto: MoldeProductos?) {
            val presentaciones = producto?.let { obtenerPresentacionesDisponiblesParaSustitucion(it) }.orEmpty()
            val nombres = presentaciones.map { presentacion ->
                PresentacionHelper.resumenPresentacionUi(
                    PresentacionesTiendaConfigManager.nombreVisible(
                        presentacion.nombre.trim().ifBlank {
                            producto?.presentacionprincipal?.ifBlank { "Unidad" } ?: "Unidad"
                        }
                    ),
                    presentacion.cantidad.coerceAtLeast(1),
                    producto?.unidadbase?.ifBlank { "unidades" } ?: "unidades"
                )
            }
            dialogBinding.actvPresentacionSustitucionDevolucion.setAdapter(
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, nombres)
            )
            presentacionSustitutaSeleccionada = presentaciones.firstOrNull()
            if (presentaciones.isNotEmpty()) {
                dialogBinding.actvPresentacionSustitucionDevolucion.setText(nombres.first(), false)
            } else {
                dialogBinding.actvPresentacionSustitucionDevolucion.setText("", false)
            }
        }

        fun aplicarProductoSustitutoSeleccionado(producto: MoldeProductos?) {
            productoSustitutoSeleccionado = producto
            actualizarPresentacionesSustitucion(producto)
            actualizarResumenProductoSustituto()
        }

        fun buscarProductoSustitutoPorIndice(
            indiceProducto: String,
            onResultado: (MoldeProductos?) -> Unit
        ) {
            val indiceNormalizado = indiceProducto.trim()
            val local = inventarioSustitucion.firstOrNull { it.indice.trim() == indiceNormalizado }
            if (local != null) {
                onResultado(local)
                return
            }

            database.reference.child("Inventario").child("Productos").child(indiceNormalizado).get()
                .addOnSuccessListener { snapshot ->
                    onResultado(snapshot.toMoldeProductoSeguro())
                }
                .addOnFailureListener {
                    onResultado(null)
                }
        }

        fun abrirSelectorProductoSustituto() {
            cargarInventarioSustitucion {
                val intent = Intent(requireContext(), ActivitySeleccionarProductoSustitucion::class.java).apply {
                    putExtra(
                        ActivitySeleccionarProductoSustitucion.EXTRA_PRODUCTO_INDICE_SELECCIONADO,
                        productoSustitutoSeleccionado?.indice?.trim().orEmpty()
                    )
                }
                seleccionarProductoSustitutoLauncher.launch(intent)
            }
        }

        fun resolverSustitucionActual(): ProductoSustitucionUi? {
            val producto = productoSustitutoSeleccionado ?: return null
            val presentacion = presentacionSustitutaSeleccionada ?: return null
            val cantidad = dialogBinding.etCantidadSustitucionDevolucion.text
                ?.toString()
                ?.trim()
                ?.toIntOrNull()
                ?: return null
            if (cantidad <= 0) return null

            val productoCaja = construirProductoCajaSustitucion(producto, presentacion, cantidad)
            val precio = productoCaja.precioUnitario.toDoubleOrNull() ?: return null
            if (precio <= 0.0) return null

            val unidadesRequeridas =
                cantidad * (productoCaja.unidadesPorPresentacion.toIntOrNull()?.coerceAtLeast(1) ?: 1)
            val resolucion = LoteConsumoRules.resolver(producto, unidadesRequeridas)
            return if (ventaPuedeConsumirLotes(resolucion)) {
                ProductoSustitucionUi(
                    productoInventario = producto,
                    presentacion = presentacion,
                    cantidadPresentaciones = cantidad,
                    productoCaja = productoCaja,
                    subtotal = productoCaja.total.toDoubleOrNull() ?: 0.0,
                    stockVendibleDisponible = ProductUtils.stockVendibleProducto(producto).toInt()
                )
            } else {
                null
            }
        }

        fun actualizarAyudaSustitucion() {
            val tipoResolucion = tipoResolucionSeleccionado
            dialogBinding.layoutSustitucionDevolucion.visibility =
                if (tipoResolucion == "cambio_producto") View.VISIBLE else View.GONE
            limpiarErroresSustitucion()
            actualizarResumenProductoSustituto()

            if (tipoResolucion != "cambio_producto") {
                sustitucionActual = null
                actualizarResumenFinanciero()
                return
            }

            val producto = productoSustitutoSeleccionado
            val presentacion = presentacionSustitutaSeleccionada
            val cantidad = dialogBinding.etCantidadSustitucionDevolucion.text
                ?.toString()
                ?.trim()
                ?.toIntOrNull() ?: 0

            if (producto == null) {
                dialogBinding.tvAyudaSustitucionDevolucion.text =
                    "Toca \"Seleccionar producto sustituto\" para buscar el reemplazo."
                sustitucionActual = null
                actualizarResumenFinanciero()
                return
            }

            if (presentacion == null) {
                dialogBinding.tvAyudaSustitucionDevolucion.text =
                    "Selecciona la presentacion del producto sustituto."
                sustitucionActual = null
                actualizarResumenFinanciero()
                return
            }

            val unidadesPorPresentacion = presentacion.cantidad.coerceAtLeast(1)
            val stockVendible = ProductUtils.stockVendibleProducto(producto).toInt()
            val maximoPorStockVendible = stockVendibleEnPresentaciones(producto, presentacion)

            if (cantidad <= 0) {
                dialogBinding.tvAyudaSustitucionDevolucion.text =
                    "Ingresa cuantas presentaciones entregaras en el cambio."
                sustitucionActual = null
                actualizarResumenFinanciero()
                return
            }

            if (cantidad > maximoPorStockVendible) {
                dialogBinding.layoutCantidadSustitucionDevolucion.error =
                    "Solo hay $maximoPorStockVendible presentaciones vendibles"
                dialogBinding.tvAyudaSustitucionDevolucion.text =
                    "Stock vendible insuficiente para esta sustitucion."
                sustitucionActual = null
                actualizarResumenFinanciero()
                return
            }

            val candidato = construirProductoCajaSustitucion(producto, presentacion, cantidad)
            val resolucion = LoteConsumoRules.resolver(producto, cantidad * unidadesPorPresentacion)
            if (!ventaPuedeConsumirLotes(resolucion)) {
                dialogBinding.tvAyudaSustitucionDevolucion.text =
                    construirMensajeVentaSinLoteValido(
                        nombreProducto = producto.nombre,
                        resolucion = resolucion,
                        unidadesSolicitadas = cantidad * unidadesPorPresentacion,
                        stockRegistrado = producto.cantidadinicial.toDoubleOrNull() ?: 0.0,
                        lotes = producto.lotes
                    )
                sustitucionActual = null
                actualizarResumenFinanciero()
                return
            }

            val subtotal = candidato.total.toDoubleOrNull() ?: 0.0
            dialogBinding.tvAyudaSustitucionDevolucion.text = buildString {
                append("Entregaras ${cantidad} ${candidato.presentacion.lowercase(Locale.getDefault())}.")
                append(" Stock vendible: $maximoPorStockVendible presentaciones")
                if (stockVendible > 0) {
                    append(" ($stockVendible ${producto.unidadbase.ifBlank { "unidades" }})")
                }
            }
            sustitucionActual = ProductoSustitucionUi(
                productoInventario = producto,
                presentacion = presentacion,
                cantidadPresentaciones = cantidad,
                productoCaja = candidato,
                subtotal = subtotal,
                stockVendibleDisponible = stockVendible
            )
            actualizarResumenFinanciero()
        }

        fun enfocarBloqueSustitucion() {
            dialogBinding.nestedScrollDevolucion.post {
                if (dialogBinding.layoutSustitucionDevolucion.visibility != View.VISIBLE) return@post
                dialogBinding.nestedScrollDevolucion.scrollTo(
                    0,
                    dialogBinding.layoutSustitucionDevolucion.top.coerceAtLeast(0)
                )
                when {
                    productoSustitutoSeleccionado == null -> dialogBinding.btnSeleccionarProductoSustitucionDevolucion.requestFocus()
                    presentacionSustitutaSeleccionada == null -> {
                        dialogBinding.actvPresentacionSustitucionDevolucion.requestFocus()
                        dialogBinding.actvPresentacionSustitucionDevolucion.showDropDown()
                    }
                    else -> dialogBinding.etCantidadSustitucionDevolucion.requestFocus()
                }
            }
        }

        fun enfocarErrorSustitucion(
            campo: View,
            abrirAccion: (() -> Unit)? = null
        ) {
            dialogBinding.nestedScrollDevolucion.post {
                dialogBinding.nestedScrollDevolucion.scrollTo(
                    0,
                    dialogBinding.layoutSustitucionDevolucion.top.coerceAtLeast(0)
                )
                campo.requestFocus()
                abrirAccion?.invoke()
            }
        }

        fun aplicarTipoResolucionSeleccionado(
            textoSeleccionado: String,
            forzarEnfoqueSustitucion: Boolean = false
        ) {
            val nuevoTipo = normalizarTipoResolucionDevolucion(textoSeleccionado)
            tipoResolucionSeleccionado = nuevoTipo
            dialogBinding.layoutTipoResolucionDevolucion.error = null
            if (nuevoTipo == "cambio_producto") {
                cargarInventarioSustitucion()
            }
            actualizarAyudaSustitucion()
            if (
                nuevoTipo == "cambio_producto" &&
                (forzarEnfoqueSustitucion || ultimoTipoResolucionUi != "cambio_producto")
            ) {
                enfocarBloqueSustitucion()
            }
            ultimoTipoResolucionUi = nuevoTipo
        }

        val container = dialogBinding.containerProductosDevolucion
        items.forEach { item ->
            val rowBinding = ItemProductoDevolucionBinding.inflate(layoutInflater, container, false)
            filasPorItem[item.itemVentaId] = rowBinding
            rowBinding.tvNombreProductoDev.text = item.nombre
            rowBinding.tvDetalleProductoDev.text =
                "Vendido: ${formatearCantidadPresentacionDevolucion(item.cantidadVendida, item.presentacion)}"
            rowBinding.tvEstadoProductoDev.visibility = View.VISIBLE
            rowBinding.tvEstadoProductoDev.text = when {
                item.resuelto -> {
                    "Resuelto: ${textoTipoResolucionDevolucion(item.tipoResolucionActual)}"
                }
                item.cantidadYaDevuelta > 0 -> {
                    "Maximo a devolver: ${formatearCantidadPresentacionDevolucion(item.cantidadDisponibleDevolucion, item.presentacion)}"
                }
                else -> {
                    "Maximo a devolver: ${formatearCantidadPresentacionDevolucion(item.cantidadDisponibleDevolucion, item.presentacion)}"
                }
            }

            if (item.resuelto) {
                rowBinding.checkboxProductoDev.isChecked = false
                rowBinding.checkboxProductoDev.isEnabled = false
                rowBinding.layoutCantidadProductoDev.visibility = View.GONE
                item.seleccionado = false
                item.cantidadADevolver = 0
            } else {
                rowBinding.layoutCantidadProductoDev.visibility = View.VISIBLE
                rowBinding.tvLabelCantidadDev.text = "Cantidad a devolver"
                val cantidadInicial = item.cantidadADevolver
                    .coerceAtLeast(1)
                    .coerceAtMost(item.cantidadDisponibleDevolucion.coerceAtLeast(1))
                item.cantidadADevolver = cantidadInicial
                rowBinding.etCantidadProductoDev.setText(cantidadInicial.toString())
            }

            fun actualizarHelperCantidad(cantidadPresentaciones: Int) {
                val helper = construirHelperCantidadDevolucion(item, cantidadPresentaciones)
                rowBinding.tvEquivalenciaDev.text = helper
                rowBinding.tvEquivalenciaDev.visibility = if (helper.isNotBlank()) View.VISIBLE else View.GONE
            }

            fun actualizarSubtotal() {
                rowBinding.tvSubtotalProductoDev.text =
                    MonedaHelper.formatear(subtotalDevolucionItem(item))
            }

            fun aplicarEstadoSeleccion(isChecked: Boolean) {
                item.seleccionado = isChecked && !item.resuelto
                val alpha = if (item.seleccionado || item.resuelto) 1f else 0.45f
                rowBinding.tvNombreProductoDev.alpha = alpha
                rowBinding.tvDetalleProductoDev.alpha = alpha
                rowBinding.tvEstadoProductoDev.alpha = alpha
                rowBinding.tvSubtotalProductoDev.alpha = alpha
                rowBinding.layoutCantidadProductoDev.isEnabled = item.seleccionado
                rowBinding.etCantidadProductoDev.isEnabled = item.seleccionado
                if (!item.seleccionado) {
                    rowBinding.tvErrorCantidadDev.visibility = View.GONE
                    actualizarHelperCantidad(item.cantidadDisponibleDevolucion)
                }
                actualizarSubtotal()
                actualizarResumenFinanciero()
            }

            if (!item.resuelto) {
                rowBinding.btnRestarCantidadDev.setOnClickListener {
                    if (!item.seleccionado) return@setOnClickListener
                    val actual = rowBinding.etCantidadProductoDev.text?.toString()?.trim()?.toIntOrNull()
                        ?: item.cantidadADevolver.coerceAtLeast(1)
                    val nuevo = (actual - 1).coerceAtLeast(1)
                    if (nuevo != actual) {
                        rowBinding.etCantidadProductoDev.setText(nuevo.toString())
                    }
                }
                rowBinding.btnSumarCantidadDev.setOnClickListener {
                    if (!item.seleccionado) return@setOnClickListener
                    val actual = rowBinding.etCantidadProductoDev.text?.toString()?.trim()?.toIntOrNull()
                        ?: item.cantidadADevolver.coerceAtLeast(1)
                    val maximo = item.cantidadDisponibleDevolucion.coerceAtLeast(1)
                    val nuevo = (actual + 1).coerceAtMost(maximo)
                    if (nuevo != actual) {
                        rowBinding.etCantidadProductoDev.setText(nuevo.toString())
                    }
                }
            }

            if (!item.resuelto) {
                rowBinding.etCantidadProductoDev.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                    override fun afterTextChanged(s: Editable?) {
                        if (!item.seleccionado) {
                            rowBinding.tvErrorCantidadDev.visibility = View.GONE
                            return
                        }
                        val cantidadTexto = s?.toString()?.trim().orEmpty()
                        val cantidadNueva = cantidadTexto.toIntOrNull()
                        when {
                            cantidadTexto.isBlank() -> {
                                item.cantidadADevolver = 0
                                rowBinding.tvErrorCantidadDev.text = "Ingresa una cantidad"
                                rowBinding.tvErrorCantidadDev.visibility = View.VISIBLE
                            }
                            cantidadNueva == null || cantidadNueva <= 0 -> {
                                item.cantidadADevolver = 0
                                rowBinding.tvErrorCantidadDev.text = "Ingresa una cantidad valida"
                                rowBinding.tvErrorCantidadDev.visibility = View.VISIBLE
                            }
                            cantidadNueva > item.cantidadDisponibleDevolucion -> {
                                item.cantidadADevolver = cantidadNueva
                                rowBinding.tvErrorCantidadDev.text =
                                    "Solo puedes devolver ${formatearCantidadPresentacionDevolucion(item.cantidadDisponibleDevolucion, item.presentacion)}"
                                rowBinding.tvErrorCantidadDev.visibility = View.VISIBLE
                            }
                            else -> {
                                item.cantidadADevolver = cantidadNueva!!
                                val errorTrazabilidad = validarTrazabilidadExactaDevolucionCached(item)
                                rowBinding.tvErrorCantidadDev.text = errorTrazabilidad
                                rowBinding.tvErrorCantidadDev.visibility = if (errorTrazabilidad != null) View.VISIBLE else View.GONE
                            }
                        }
                        actualizarHelperCantidad(
                            if (cantidadNueva != null && cantidadNueva > 0) cantidadNueva else item.cantidadDisponibleDevolucion
                        )
                        actualizarSubtotal()
                        actualizarResumenFinanciero()
                    }
                })
            }

            rowBinding.checkboxProductoDev.isChecked = item.seleccionado
            rowBinding.checkboxProductoDev.setOnCheckedChangeListener { _, isChecked ->
                aplicarEstadoSeleccion(isChecked)
            }
            actualizarHelperCantidad(item.cantidadADevolver.coerceAtLeast(1))
            aplicarEstadoSeleccion(rowBinding.checkboxProductoDev.isChecked)
            container.addView(rowBinding.root)
        }

        onProductoSustitutoSeleccionadoDevolucion = productoSustitutoCallback@{ indiceSeleccionado ->
            if (bottomSheet?.isShowing != true) return@productoSustitutoCallback
            buscarProductoSustitutoPorIndice(indiceSeleccionado) { producto ->
                if (!isAdded || _binding == null) return@buscarProductoSustitutoPorIndice
                val productoValido = producto?.takeIf {
                    ProductUtils.stockVendibleProducto(it).toInt() > 0
                }
                if (productoValido == null) {
                    mostrarToastSeguro("El producto seleccionado ya no tiene stock vendible disponible")
                    return@buscarProductoSustitutoPorIndice
                }
                inventarioSustitucion.removeAll { it.indice.trim() == productoValido.indice.trim() }
                inventarioSustitucion.add(productoValido)
                aplicarProductoSustitutoSeleccionado(productoValido)
                actualizarAyudaSustitucion()
                enfocarBloqueSustitucion()
            }
        }

        dialogBinding.actvTipoResolucionDevolucion.setOnItemClickListener { _, _, position, _ ->
            val textoSeleccionado = opcionesResolucion.getOrNull(position)
                ?: dialogBinding.actvTipoResolucionDevolucion.text?.toString().orEmpty()
            if (textoSeleccionado.isNotBlank()) {
                dialogBinding.actvTipoResolucionDevolucion.setText(textoSeleccionado, false)
            }
            aplicarTipoResolucionSeleccionado(
                textoSeleccionado = textoSeleccionado,
                forzarEnfoqueSustitucion = true
            )
        }
        dialogBinding.actvTipoResolucionDevolucion.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                aplicarTipoResolucionSeleccionado(
                    textoSeleccionado = s?.toString().orEmpty(),
                    forzarEnfoqueSustitucion = false
                )
            }
        })
        dialogBinding.actvTipoResolucionDevolucion.setOnDismissListener {
            aplicarTipoResolucionSeleccionado(
                textoSeleccionado = dialogBinding.actvTipoResolucionDevolucion.text?.toString().orEmpty(),
                forzarEnfoqueSustitucion = tipoResolucionSeleccionado == "cambio_producto"
            )
        }
        dialogBinding.btnSeleccionarProductoSustitucionDevolucion.setOnClickListener {
            abrirSelectorProductoSustituto()
        }
        dialogBinding.etProductoSustitucionDevolucion.setOnClickListener {
            abrirSelectorProductoSustituto()
        }
        dialogBinding.actvPresentacionSustitucionDevolucion.setOnClickListener {
            dialogBinding.actvPresentacionSustitucionDevolucion.showDropDown()
        }
        dialogBinding.actvMetodoPagoDiferenciaDevolucion.setOnClickListener {
            dialogBinding.actvMetodoPagoDiferenciaDevolucion.showDropDown()
        }
        dialogBinding.actvPresentacionSustitucionDevolucion.setOnItemClickListener { _, _, position, _ ->
            val presentaciones = productoSustitutoSeleccionado?.let {
                obtenerPresentacionesDisponiblesParaSustitucion(it)
            }.orEmpty()
            presentacionSustitutaSeleccionada = presentaciones.getOrNull(position)
            dialogBinding.layoutPresentacionSustitucionDevolucion.error = null
            actualizarAyudaSustitucion()
        }
        dialogBinding.actvMetodoPagoDiferenciaDevolucion.setOnItemClickListener { _, _, position, _ ->
            metodoPagoDiferenciaSeleccionado = metodosPagoDiferenciaDisponibles.getOrNull(position)
            dialogBinding.layoutMetodoPagoDiferenciaDevolucion.error = null
            actualizarBloquePagoDiferencia(
                if (tipoResolucionSeleccionado == "cambio_producto") {
                    calcularAjusteFinancieroDevolucion(
                        totalDevueltoProducto = totalSeleccionadoActual(),
                        sustitucion = sustitucionActual,
                        tipoResolucion = tipoResolucionSeleccionado
                    )
                } else {
                    null
                }
            )
        }
        dialogBinding.actvMetodoPagoDiferenciaDevolucion.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                val texto = s?.toString()?.trim().orEmpty()
                metodoPagoDiferenciaSeleccionado = metodosPagoDiferenciaDisponibles.firstOrNull {
                    it.titulo.trim().equals(texto, ignoreCase = true)
                }
                dialogBinding.layoutMetodoPagoDiferenciaDevolucion.error = null
            }
        })
        dialogBinding.etCantidadSustitucionDevolucion.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                dialogBinding.layoutCantidadSustitucionDevolucion.error = null
                actualizarAyudaSustitucion()
            }
        })
        dialogBinding.etMontoPagoDiferenciaDevolucion.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                dialogBinding.layoutMontoPagoDiferenciaDevolucion.error = null
            }
        })
        dialogBinding.etMotivoDevolucion.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                dialogBinding.layoutMotivoDevolucion.error = null
                actualizarAvisoStockPorMotivo()
            }
        })

        obtenerMetodosPagoActivos(
            onSuccess = { metodos ->
                if (!isAdded || _binding == null) return@obtenerMetodosPagoActivos
                configurarMetodosPagoDiferencia(metodos)
            },
            onError = {
                if (!isAdded || _binding == null) return@obtenerMetodosPagoActivos
                configurarMetodosPagoDiferencia(emptyList())
            }
        )
        cargarInventarioSustitucion()
        actualizarAvisoStockPorMotivo()
        aplicarTipoResolucionSeleccionado(
            textoSeleccionado = dialogBinding.actvTipoResolucionDevolucion.text?.toString().orEmpty(),
            forzarEnfoqueSustitucion = false
        )
        actualizarResumenFinanciero()

        dialogBinding.btnCancelarDevolucionSheet.setOnClickListener {
            bottomSheet.dismiss()
        }

        dialogBinding.btnConfirmarDevolucionSheet.setOnClickListener {
            val seleccionados = items.filter { it.seleccionado }
            if (seleccionados.isEmpty()) {
                dialogBinding.layoutMotivoDevolucion.error = "Selecciona al menos un producto"
                return@setOnClickListener
            }
            dialogBinding.layoutMotivoDevolucion.error = null

            val tipoResolucionTexto = dialogBinding.actvTipoResolucionDevolucion.text
                ?.toString()?.trim().orEmpty()
            val tipoResolucion = tipoResolucionSeleccionado.ifBlank {
                normalizarTipoResolucionDevolucion(tipoResolucionTexto)
            }
            if (tipoResolucion.isBlank()) {
                dialogBinding.layoutTipoResolucionDevolucion.error =
                    "Selecciona como se resolvio la devolucion"
                dialogBinding.actvTipoResolucionDevolucion.requestFocus()
                dialogBinding.actvTipoResolucionDevolucion.showDropDown()
                return@setOnClickListener
            }
            dialogBinding.layoutTipoResolucionDevolucion.error = null

            val itemInvalido = seleccionados.firstOrNull {
                it.cantidadADevolver <= 0 || it.cantidadADevolver > it.cantidadDisponibleDevolucion
            }
            if (itemInvalido != null) {
                val fila = filasPorItem[itemInvalido.itemVentaId]
                fila?.tvErrorCantidadDev?.text = "Revisa la cantidad a devolver"
                fila?.tvErrorCantidadDev?.visibility = View.VISIBLE
                fila?.etCantidadProductoDev?.requestFocus()
                mostrarToastSeguro("Revisa la cantidad a devolver de ${itemInvalido.nombre}")
                return@setOnClickListener
            }

            val itemSinTrazabilidad = seleccionados.firstOrNull {
                validarTrazabilidadExactaDevolucion(it) != null
            }
            if (itemSinTrazabilidad != null) {
                val mensajeError = validarTrazabilidadExactaDevolucion(itemSinTrazabilidad)
                    ?: "No se pudo reconstruir la trazabilidad exacta de ${itemSinTrazabilidad.nombre}"
                val fila = filasPorItem[itemSinTrazabilidad.itemVentaId]
                fila?.tvErrorCantidadDev?.text = mensajeError
                fila?.tvErrorCantidadDev?.visibility = View.VISIBLE
                fila?.etCantidadProductoDev?.requestFocus()
                mostrarToastSeguro(mensajeError)
                return@setOnClickListener
            }

            val motivo = dialogBinding.etMotivoDevolucion.text?.toString()?.trim().orEmpty()
            val sustitucion = if (tipoResolucion == "cambio_producto") {
                val producto = productoSustitutoSeleccionado
                if (producto == null) {
                    mostrarToastSeguro("Selecciona el producto sustituto")
                    enfocarErrorSustitucion(
                        campo = dialogBinding.btnSeleccionarProductoSustitucionDevolucion
                    )
                    return@setOnClickListener
                }
                if (presentacionSustitutaSeleccionada == null) {
                    dialogBinding.layoutPresentacionSustitucionDevolucion.error =
                        "Selecciona la presentacion"
                    enfocarErrorSustitucion(
                        campo = dialogBinding.actvPresentacionSustitucionDevolucion,
                        abrirAccion = { dialogBinding.actvPresentacionSustitucionDevolucion.showDropDown() }
                    )
                    return@setOnClickListener
                }
                val cantidad = dialogBinding.etCantidadSustitucionDevolucion.text
                    ?.toString()
                    ?.trim()
                    ?.toIntOrNull()
                if (cantidad == null || cantidad <= 0) {
                    dialogBinding.layoutCantidadSustitucionDevolucion.error =
                        "Ingresa una cantidad valida"
                    enfocarErrorSustitucion(
                        campo = dialogBinding.etCantidadSustitucionDevolucion
                    )
                    return@setOnClickListener
                }
                val candidato = resolverSustitucionActual()
                if (candidato == null) {
                    dialogBinding.layoutCantidadSustitucionDevolucion.error =
                        "La sustitucion no tiene stock vendible suficiente"
                    enfocarErrorSustitucion(
                        campo = dialogBinding.etCantidadSustitucionDevolucion
                    )
                    return@setOnClickListener
                }
                candidato
            } else {
                null
            }

            val totalDevolucion = seleccionados.sumOf { subtotalDevolucionItem(it) }
            val ajustePago = calcularAjusteFinancieroDevolucion(
                totalDevueltoProducto = totalDevolucion,
                sustitucion = sustitucion,
                tipoResolucion = tipoResolucion
            )
            val pagoDiferencia = if (
                tipoResolucion == "cambio_producto" &&
                ajustePago.montoCobradoCliente > 0.0
            ) {
                val metodo = resolverMetodoPagoDiferenciaSeleccionado()
                if (metodo == null) {
                    dialogBinding.layoutMetodoPagoDiferenciaDevolucion.error =
                        "Selecciona el medio de pago"
                    dialogBinding.nestedScrollDevolucion.post {
                        dialogBinding.nestedScrollDevolucion.scrollTo(
                            0,
                            dialogBinding.layoutPagoDiferenciaDevolucion.top.coerceAtLeast(0)
                        )
                        dialogBinding.actvMetodoPagoDiferenciaDevolucion.requestFocus()
                        dialogBinding.actvMetodoPagoDiferenciaDevolucion.showDropDown()
                    }
                    return@setOnClickListener
                }
                val montoPagado = dialogBinding.etMontoPagoDiferenciaDevolucion.text
                    ?.toString()
                    ?.trim()
                    ?.replace(',', '.')
                    ?.toDoubleOrNull()
                if (montoPagado == null || montoPagado <= 0.0) {
                    dialogBinding.layoutMontoPagoDiferenciaDevolucion.error =
                        "Ingresa el monto pagado"
                    dialogBinding.nestedScrollDevolucion.post {
                        dialogBinding.nestedScrollDevolucion.scrollTo(
                            0,
                            dialogBinding.layoutPagoDiferenciaDevolucion.top.coerceAtLeast(0)
                        )
                        dialogBinding.etMontoPagoDiferenciaDevolucion.requestFocus()
                    }
                    return@setOnClickListener
                }
                if (kotlin.math.abs(montoPagado - ajustePago.montoCobradoCliente) > 0.01) {
                    dialogBinding.layoutMontoPagoDiferenciaDevolucion.error =
                        "Registra ${MonedaHelper.formatear(ajustePago.montoCobradoCliente)}"
                    dialogBinding.nestedScrollDevolucion.post {
                        dialogBinding.nestedScrollDevolucion.scrollTo(
                            0,
                            dialogBinding.layoutPagoDiferenciaDevolucion.top.coerceAtLeast(0)
                        )
                        dialogBinding.etMontoPagoDiferenciaDevolucion.requestFocus()
                    }
                    return@setOnClickListener
                }
                PagoDiferenciaDevolucion(
                    metodoPago = metodo.titulo.trim().ifBlank { metodo.categoria.ifBlank { "Otro" } },
                    montoPagado = ajustePago.montoCobradoCliente,
                    detalleMetodos = mapOf(
                        metodo.titulo.trim().ifBlank { metodo.categoria.ifBlank { "Otro" } } to ajustePago.montoCobradoCliente
                    )
                )
            } else {
                null
            }

            bottomSheet.dismiss()
            procesarDevolucionFlexible(
                idVenta = venta.idVenta,
                fechaVenta = fechaBusqueda,
                totalDevolucion = totalDevolucion,
                itemsADevolver = seleccionados,
                motivo = motivo,
                idCajaOperativa = idCajaOperativa,
                metodoPagoOriginal = venta.metodoPago,
                tipoResolucion = tipoResolucion,
                sustitucion = sustitucion,
                pagoDiferencia = pagoDiferencia
            )
        }

        bottomSheet.show()
        bottomSheet.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)?.let { sheet ->
            BottomSheetBehavior.from(sheet).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
            }
        }
    }


    private fun subtotalDevolucionItem(item: ItemDevolucionUi): Double {
        if (item.cantidadVendida <= 0 || item.cantidadADevolver <= 0) return 0.0

        // Caso devolución total exacta del ítem: usar el subtotal original directamente
        // para evitar perder centavos por divisiones de decimales infinitos.
        if (item.cantidadADevolver == item.cantidadVendida && item.cantidadYaDevuelta == 0) {
            return item.subtotalOriginal
        }

        val precioPorPresentacion = item.subtotalOriginal / item.cantidadVendida.toDouble()
        val subtotalBruto = precioPorPresentacion * item.cantidadADevolver
        return CalculadoraVentasHelper.redondear(subtotalBruto)
    }

    private fun obtenerPresentacionesDisponiblesParaSustitucion(
        producto: MoldeProductos
    ): List<PresentacionProducto> {
        if (producto.tienePresentaciones && producto.presentaciones.isNotEmpty()) {
            return producto.presentaciones
        }
        return listOf(
            PresentacionProducto(
                nombre = producto.presentacionprincipal.ifBlank { "Unidad" },
                cantidad = 1,
                precioventa = producto.preciodecompra.toDoubleOrNull() ?: 0.0
            )
        )
    }

    private fun construirProductoCajaSustitucion(
        producto: MoldeProductos,
        presentacion: PresentacionProducto,
        cantidadPresentaciones: Int
    ): ProductoCaja {
        val unidadesPorPresentacion = presentacion.cantidad.coerceAtLeast(1)
        val precioUnitario = presentacion.precioventa
        val subtotal = precioUnitario * cantidadPresentaciones.coerceAtLeast(0)
        val nombrePresentacion = PresentacionHelper.resumenPresentacionUi(
            PresentacionesTiendaConfigManager.nombreVisible(
                presentacion.nombre.trim().ifBlank { producto.presentacionprincipal.ifBlank { "Unidad" } }
            ),
            unidadesPorPresentacion,
            producto.unidadbase.ifBlank { "unidades" }
        )

        return ProductoCaja(
            id = "cambio_${producto.indice.trim()}_${PresentacionHelper.claveFirebaseSegura(nombrePresentacion)}_${UUID.randomUUID().toString().take(6)}",
            indiceProducto = producto.indice.trim(),
            nombre = producto.nombre,
            categoria = producto.categoria,
            presentacion = nombrePresentacion,
            cantidad = cantidadPresentaciones.coerceAtLeast(0).toString(),
            unidadesPorPresentacion = unidadesPorPresentacion.toString(),
            precioUnitario = String.format(Locale.US, "%.2f", precioUnitario),
            total = String.format(Locale.US, "%.2f", subtotal)
        )
    }

    private fun stockVendibleEnPresentaciones(
        producto: MoldeProductos,
        presentacion: PresentacionProducto
    ): Int {
        val unidadesPorPresentacion = presentacion.cantidad.coerceAtLeast(1)
        val stockVendible = ProductUtils.stockVendibleProducto(producto).toInt()
        return if (unidadesPorPresentacion > 0) {
            stockVendible / unidadesPorPresentacion
        } else {
            0
        }
    }

    private fun clavePresentacionSustitucion(
        producto: MoldeProductos,
        presentacion: PresentacionProducto
    ): String {
        val nombreVisible = PresentacionesTiendaConfigManager.nombreVisible(
            presentacion.nombre.trim().ifBlank {
                producto.presentacionprincipal.ifBlank { "Unidad" }
            }
        )
        return "${normalizarTextoLibre(nombreVisible)}|${presentacion.cantidad.coerceAtLeast(1)}"
    }

    private fun encontrarPresentacionSustitucionActual(
        producto: MoldeProductos,
        presentacionBuscada: PresentacionProducto
    ): PresentacionProducto? {
        val claveBuscada = clavePresentacionSustitucion(producto, presentacionBuscada)
        return obtenerPresentacionesDisponiblesParaSustitucion(producto).firstOrNull { presentacion ->
            clavePresentacionSustitucion(producto, presentacion) == claveBuscada
        }
    }




    private fun construirMapaEstadoItemDevolucion(
        item: ItemDevolucionUi,
        cantidadDevueltaAcumulada: Int,
        subtotalDevueltoAcumulado: Double,
        lotesDevueltosDetalle: Map<String, LoteConsumoDetalleCaja>,
        tipoResolucion: String,
        motivo: String,
        motivoBloqueaVenta: Boolean = false,
        timestampDev: Long
    ): Map<String, Any> {
        val cantidadDisponible = (item.cantidadVendida - cantidadDevueltaAcumulada).coerceAtLeast(0)
        val estado = when {
            cantidadDevueltaAcumulada <= 0 -> "normal"
            cantidadDevueltaAcumulada < item.cantidadVendida -> "parcialmente_devuelto"
            else -> "devuelto"
        }
        return linkedMapOf(
            "itemVentaId" to item.itemVentaId,
            "indiceProducto" to item.indiceProducto,
            "nombre" to item.nombre,
            "presentacion" to item.presentacion,
            "cantidadVendida" to item.cantidadVendida,
            "unidadesPorPresentacion" to item.unidadesPorPresentacion,
            "cantidadDevuelta" to cantidadDevueltaAcumulada,
            "cantidadDisponible" to cantidadDisponible,
            "estado" to estado,
            "resuelto" to (cantidadDisponible == 0),
            "tipoResolucion" to tipoResolucion,
            "tipoResolucionUltima" to tipoResolucion,
            "motivoUltimaDevolucion" to motivo.ifBlank { "Sin motivo" },
            "motivoBloqueaVentaUltimo" to motivoBloqueaVenta,
            "destinoStockUltimo" to destinoStockDevolucion(motivoBloqueaVenta),
            "motivoBloqueoUltimo" to if (motivoBloqueaVenta) {
                motivoBloqueoRegistrado(motivo)
            } else {
                ""
            },
            "subtotalOriginal" to String.format(Locale.US, "%.2f", item.subtotalOriginal),
            "subtotalDevueltoAcumulado" to String.format(Locale.US, "%.2f", subtotalDevueltoAcumulado),
            "lotesDevueltosDetalle" to construirMapaDetalleLotes(lotesDevueltosDetalle.values),
            "timestampUltimaDevolucion" to timestampDev
        )
    }

    private fun construirDetalleProductoDevolucionEvento(
        item: ItemDevolucionUi,
        tipoResolucion: String,
        motivoBloqueaVenta: Boolean,
        motivoDevolucion: String
    ): Map<String, Any> {
        val subtotalDevuelto = subtotalDevolucionItem(item)
        val unidadesDevueltas = item.cantidadADevolver * item.unidadesPorPresentacion
        return linkedMapOf(
            "itemVentaId" to item.itemVentaId,
            "indiceProducto" to item.indiceProducto,
            "nombre" to item.nombre,
            "presentacion" to item.presentacion,
            "cantidadVendida" to item.cantidadVendida,
            "cantidadDevueltaAhora" to item.cantidadADevolver,
            "cantidadDevueltaAcumulada" to (item.cantidadYaDevuelta + item.cantidadADevolver),
            "unidadesPorPresentacion" to item.unidadesPorPresentacion,
            "unidadesDevueltas" to unidadesDevueltas,
            "cantidadDisponiblePendiente" to
                (item.cantidadDisponibleDevolucion - item.cantidadADevolver).coerceAtLeast(0),
            "subtotalDevuelto" to String.format(Locale.US, "%.2f", subtotalDevuelto),
            "tipoResolucion" to tipoResolucion,
            "detalleLotesTexto" to construirDetalleLotesTexto(item.productoVenta, unidadesDevueltas),
            "lotesDevueltosAhoraDetalle" to construirMapaDetalleLotes(
                item.productoVenta.lotesConsumidosDetalle.values
            ),
            "bloqueadoParaVenta" to motivoBloqueaVenta,
            "destinoStock" to destinoStockDevolucion(motivoBloqueaVenta),
            "motivoBloqueoAplicado" to if (motivoBloqueaVenta) {
                motivoBloqueoRegistrado(motivoDevolucion)
            } else {
                ""
            }
        )
    }



    private fun construirMapaAjusteFinancieroDevolucion(
        ajuste: AjusteFinancieroDevolucion
    ): Map<String, Any> {
        return linkedMapOf(
            "totalDevueltoProducto" to String.format(Locale.US, "%.2f", ajuste.totalDevueltoProducto),
            "totalSustitucion" to String.format(Locale.US, "%.2f", ajuste.totalSustitucion),
            "diferenciaCliente" to String.format(Locale.US, "%.2f", ajuste.diferenciaCliente),
            "montoDevueltoCliente" to String.format(Locale.US, "%.2f", ajuste.montoDevueltoCliente),
            "montoCobradoCliente" to String.format(Locale.US, "%.2f", ajuste.montoCobradoCliente),
            "deltaVentasTurno" to String.format(Locale.US, "%.2f", ajuste.deltaVentasTurno)
        )
    }


    private fun procesarDevolucionFlexible(
        idVenta: String,
        fechaVenta: String,
        totalDevolucion: Double,
        itemsADevolver: List<ItemDevolucionUi>,
        motivo: String,
        idCajaOperativa: String,
        metodoPagoOriginal: String,
        tipoResolucion: String,
        sustitucion: ProductoSustitucionUi? = null,
        pagoDiferencia: PagoDiferenciaDevolucion? = null
    ) {
        if (!isAdded || _binding == null) return

        val tipoResolucionNormalizado = normalizarTipoResolucionDevolucion(tipoResolucion)
        if (tipoResolucionNormalizado.isBlank()) {
            mostrarToastSeguro("Selecciona una resolución válida para la devolución")
            return
        }

        val ajustePrevio = calcularAjusteFinancieroDevolucion(
            totalDevueltoProducto = totalDevolucion,
            sustitucion = sustitucion,
            tipoResolucion = tipoResolucionNormalizado
        )
        val pagoDiferenciaValidado = if (
            tipoResolucionNormalizado == "cambio_producto" &&
            ajustePrevio.montoCobradoCliente > 0.0
        ) {
            val pago = pagoDiferencia
            when {
                pago == null || pago.metodoPago.trim().isBlank() -> {
                    mostrarToastSeguro("Selecciona como se pagara la diferencia del cambio")
                    return
                }
                kotlin.math.abs(pago.montoPagado - ajustePrevio.montoCobradoCliente) > 0.01 -> {
                    mostrarToastSeguro("Registra el monto exacto de la diferencia a cobrar")
                    return
                }
                else -> pago.copy(
                    metodoPago = pago.metodoPago.trim(),
                    montoPagado = ajustePrevio.montoCobradoCliente,
                    detalleMetodos = if (pago.detalleMetodos.isNotEmpty()) {
                        pago.detalleMetodos
                    } else {
                        mapOf(pago.metodoPago.trim() to ajustePrevio.montoCobradoCliente)
                    }
                )
            }
        } else {
            null
        }

        // Captura de fecha actual para corregir reportes (Bug 4)
        val fechaHoyReal = obtenerFechaActual()
        val timestampDev = System.currentTimeMillis()
        val idDevolucion = "dev_${timestampDev}_${UUID.randomUUID().toString().take(6)}"
        val horaDevolucion = obtenerHoraActual()
        val motivoBloqueaVenta = motivoImplicaBloqueoVenta(motivo)
        val valorProductosFmt = String.format(Locale.US, "%.2f", totalDevolucion)

        mostrarLoadingOverlayCaja("Procesando devolucion", "Restaurando stock y actualizando turno...")

        database.reference.child("Ventas").child(fechaVenta).child(idVenta).get()
            .addOnSuccessListener { ventaSnapshot ->
                if (_binding == null || !isAdded) return@addOnSuccessListener
                val productosSnap = ventaSnapshot.child("productos")
                if (!productosSnap.exists()) {
                    ocultarLoadingOverlayCaja()
                    mostrarToastSeguro("No se encontraron productos en la venta")
                    return@addOnSuccessListener
                }

                // Captura de detalle de métodos original para ventas mixtas (Bug 3)
                val detallePagoOriginal = ventaSnapshot.child("detallePago").children.mapNotNull {
                    val titulo = it.child("titulo").value?.toString() ?: it.key ?: ""
                    val monto = it.child("monto").value?.toString()?.toDoubleOrNull() ?: 0.0
                    if (titulo.isNotBlank()) titulo to monto else null
                }.toMap()

                val estadosExistentes = leerEstadosDevolucionPorItem(
                    devolucionSnap = ventaSnapshot.child("devolucion"),
                    productosSnap = productosSnap
                )
                val seleccionadosPorId = itemsADevolver.associateBy { it.itemVentaId }
                val itemsActualizados = linkedMapOf<String, Map<String, Any>>()
                val estadosFinales = linkedMapOf<String, EstadoItemDevolucionUi>()
                val detalleEvento = mutableListOf<Map<String, Any>>()
                val itemsProcesados = mutableListOf<ItemDevolucionUi>()

                productosSnap.children.forEach { prodSnap ->
                    val producto = prodSnap.getValue(ProductoCaja::class.java) ?: return@forEach
                    val itemVentaId = construirItemVentaIdDevolucion(prodSnap, producto)
                    if (itemVentaId.isBlank()) return@forEach

                    val itemSeleccionado = seleccionadosPorId[itemVentaId]
                    val estadoPrevio = estadosExistentes[itemVentaId]
                    val cantidadVendida = producto.cantidad.toIntOrNull() ?: 0
                    val cantidadDevueltaPrevia = estadoPrevio?.cantidadDevuelta ?: 0
                    val subtotalDevueltoPrevio = estadoPrevio?.subtotalDevuelto ?: 0.0

                    if (itemSeleccionado != null) {
                        val disponibleActual = (cantidadVendida - cantidadDevueltaPrevia).coerceAtLeast(0)
                        if (itemSeleccionado.cantidadADevolver <= 0 ||
                            itemSeleccionado.cantidadADevolver > disponibleActual
                        ) {
                            ocultarLoadingOverlayCaja()
                            mostrarToastSeguro("La devolucion de ${itemSeleccionado.nombre} ya no es valida.")
                            return@addOnSuccessListener
                        }

                        val unidadesADevolver = itemSeleccionado.cantidadADevolver * itemSeleccionado.unidadesPorPresentacion
                        val tramosDevolucion = construirTramosDevolucionDesdeVenta(
                            productoVenta = producto,
                            unidadesADevolver = unidadesADevolver,
                            lotesDevueltosPrevios = estadoPrevio?.lotesDevueltosDetalle.orEmpty()
                        )
                        if (tramosDevolucion.sumOf { it.cantidad } < unidadesADevolver) {
                            ocultarLoadingOverlayCaja()
                            mostrarToastSeguro("No se pudo reconstruir la trazabilidad exacta de lotes.")
                            return@addOnSuccessListener
                        }

                        val productoEvento = construirProductoVentaParaEventoDevolucion(
                            item = itemSeleccionado.copy(productoVenta = producto),
                            tramosDevolucion = tramosDevolucion
                        )
                        val itemProcesado = itemSeleccionado.copy(productoVenta = productoEvento)
                        val cantidadDevueltaNueva = cantidadDevueltaPrevia + itemSeleccionado.cantidadADevolver
                        val subtotalDevueltoNuevo = subtotalDevueltoPrevio + subtotalDevolucionItem(itemSeleccionado)
                        val lotesDevueltosAcumulados = acumularDetalleLotesDevueltos(
                            previos = estadoPrevio?.lotesDevueltosDetalle.orEmpty(),
                            actuales = tramosDevolucion
                        )
                        itemsActualizados[itemVentaId] = construirMapaEstadoItemDevolucion(
                            item = itemProcesado,
                            cantidadDevueltaAcumulada = cantidadDevueltaNueva,
                            subtotalDevueltoAcumulado = subtotalDevueltoNuevo,
                            lotesDevueltosDetalle = lotesDevueltosAcumulados,
                            tipoResolucion = tipoResolucionNormalizado,
                            motivo = motivo,
                            motivoBloqueaVenta = motivoBloqueaVenta,
                            timestampDev = timestampDev
                        )
                        estadosFinales[itemVentaId] = EstadoItemDevolucionUi(
                            itemVentaId = itemVentaId,
                            cantidadVendida = cantidadVendida,
                            cantidadDevuelta = cantidadDevueltaNueva,
                            estado = if (cantidadDevueltaNueva < cantidadVendida) "parcialmente_devuelto" else "devuelto",
                            tipoResolucion = tipoResolucionNormalizado,
                            resuelto = cantidadDevueltaNueva >= cantidadVendida,
                            subtotalDevuelto = subtotalDevueltoNuevo,
                            lotesDevueltosDetalle = lotesDevueltosAcumulados
                        )
                        itemsProcesados += itemProcesado
                        detalleEvento += construirDetalleProductoDevolucionEvento(
                            itemProcesado,
                            tipoResolucionNormalizado,
                            motivoBloqueaVenta,
                            motivo
                        )
                    } else if (estadoPrevio != null && estadoPrevio.cantidadDevuelta > 0) {
                        val itemPrevioSnapshot = ventaSnapshot.child("devolucion").child("items").child(itemVentaId)
                        val mapaPrevio = itemPrevioSnapshot.value as? Map<*, *>
                        if (mapaPrevio != null) {
                            itemsActualizados[itemVentaId] = mapaPrevio.entries.associate { it.key.toString() to (it.value ?: "") }
                        }
                        estadosFinales[itemVentaId] = estadoPrevio
                    }
                }

                val itemsConDevolucion = itemsProcesados.filter { it.indiceProducto.isNotBlank() && it.cantidadADevolver > 0 }
                val estadoVentaObjetivo = resolverEstadoVentaDevolucion(productosSnap, estadosFinales)

                fun continuarPersistenciaDevolucion(
                    resultadosRestauracionTotal: List<ResultadoRestauracionDevolucionUi> = emptyList(),
                    sustitucionConfirmada: ProductoSustitucionUi? = sustitucion,
                    stocksOriginalesSustitucionPreaplicados: MutableMap<String, String> = mutableMapOf(),
                    sustitucionYaDescontada: Boolean = false
                ) {
                    val ajusteFinanciero = calcularAjusteFinancieroDevolucion(totalDevolucion, sustitucionConfirmada, tipoResolucionNormalizado)
                    val montoDevueltoClienteFmt = String.format(Locale.US, "%.2f", ajusteFinanciero.montoDevueltoCliente)
                    val montoCobradoClienteFmt = String.format(Locale.US, "%.2f", ajusteFinanciero.montoCobradoCliente)
                    val deltaVentasFmt = String.format(Locale.US, "%.2f", ajusteFinanciero.deltaVentasTurno)

                    val eventoActual = linkedMapOf<String, Any>(
                        "idDevolucion" to idDevolucion,
                        "estadoPosterior" to estadoVentaObjetivo.estadoGeneral,
                        "tipoResolucion" to tipoResolucionNormalizado,
                        "montoDevuelto" to montoDevueltoClienteFmt,
                        "valorProductosDevueltos" to valorProductosFmt,
                        "montoCobradoCliente" to montoCobradoClienteFmt,
                        "deltaVentasTurno" to deltaVentasFmt,
                        "timestampDevolucionServidor" to ServerValue.TIMESTAMP,
                        "fechaDevolucionLocal" to fechaHoyReal, // Corregido a hoy
                        "fechaVentaOriginal" to fechaVenta,
                        "horaDevolucionLocal" to horaDevolucion,
                        "fecha" to fechaHoyReal,                // Corregido a hoy
                        "hora" to horaDevolucion,
                        "motivo" to motivo.ifBlank { "Sin motivo" },
                        "motivoBloqueaVenta" to motivoBloqueaVenta,
                        "idTurno" to idTurnoActivo,
                        "idUsuario" to idCajera,
                        "nombreUsuario" to nombreCajera,
                        "detalleProductos" to detalleEvento,
                        "ajusteFinanciero" to construirMapaAjusteFinancieroDevolucion(ajusteFinanciero)
                    )

                    val rutasDevolucion = listOf("Ventas/$fechaVenta/$idVenta/devolucion", "VentasPorCajera/$idCajaOperativa/$fechaVenta/$idVenta/devolucion")
                    val updates = hashMapOf<String, Any>()
                    rutasDevolucion.forEach { ruta ->
                        updates["$ruta/idDevolucion"] = idDevolucion
                        updates["$ruta/estado"] = estadoVentaObjetivo.estadoGeneral
                        updates["$ruta/fechaDevolucionLocal"] = fechaHoyReal // Corregido a hoy
                        updates["$ruta/fecha"] = fechaHoyReal                // Corregido a hoy
                        updates["$ruta/fechaUltimaDevolucion"] = fechaHoyReal // Corregido a hoy
                        updates["$ruta/items"] = itemsActualizados
                        updates["$ruta/eventos/$idDevolucion"] = eventoActual
                    }

                    val continuarPersistencia: (ProductoSustitucionUi?, MutableMap<String, String>) -> Unit = { sustitucionAplicada, _ ->
                        // Helper para escritura única y manejo de éxito (Bug 1)
                        fun escribirYFinalizar(onRollback: (onDone: () -> Unit) -> Unit) {
                            database.reference.updateChildren(updates)
                                .addOnSuccessListener {
                                    if (resultadosRestauracionTotal.isNotEmpty()) {
                                        registrarResultadosRestauracionDevolucion(resultadosRestauracionTotal, idVenta, idDevolucion, motivo, motivoBloqueaVenta)
                                    }
                                    // Actualización de turno pasando el detallePagoOriginal (Bug 3)
                                    actualizarTurnoPostDevolucion(
                                        deltaVentasNeto = ajusteFinanciero.deltaVentasTurno,
                                        montoDevueltoCliente = ajusteFinanciero.montoDevueltoCliente,
                                        metodoPagoOriginal = metodoPagoOriginal,
                                        detalleMetodosVentaOriginal = detallePagoOriginal,
                                        pagoDiferencia = pagoDiferenciaValidado,
                                        decrementarNumeroVentas = (estadoVentaObjetivo.esTotal && tipoResolucionNormalizado != "cambio_producto"),
                                        onComplete = {
                                            // CORRECCIÓN PROTECTORA: Si el fragmento murió en el transcurso de la operación,
                                            // salimos en silencio para evitar que esTablet() rompa la app.
                                            if (_binding == null || !isAdded) return@actualizarTurnoPostDevolucion

                                            ocultarLoadingOverlayCaja()
                                            dialogoDevolucionActual?.dismiss()
                                            mostrarToastSeguro("Devolución registrada correctamente")
                                            if (esTablet()) obtenerProductosTablet()
                                        }
                                    )
                                }
                                .addOnFailureListener { e ->
                                    onRollback { ocultarLoadingOverlayCaja(); mostrarToastSeguro("Error al guardar: ${e.message}. Stock revertido.") }
                                }
                        }

                        if (resultadosRestauracionTotal.isNotEmpty()) {
                            escribirYFinalizar { onDone -> revertirRestauracionDevolucionTotalSegura(resultadosRestauracionTotal.asReversed(), 0, motivo, motivoBloqueaVenta, onDone) }
                        } else {
                            procesarRestauracionDevolucionRecursiva(itemsConDevolucion, 0, idVenta, idDevolucion, motivo, motivoBloqueaVenta, {
                                escribirYFinalizar { onDone -> revertirRestauracionDevolucionParcialSegura(itemsConDevolucion, 0, motivo, motivoBloqueaVenta, mutableListOf(), { onDone() }) }
                            }, { msg -> mostrarToastSeguro(msg); ocultarLoadingOverlayCaja() })
                        }
                    }

                    if (sustitucionConfirmada != null) {
                        if (sustitucionYaDescontada) {
                            continuarPersistencia(sustitucionConfirmada, stocksOriginalesSustitucionPreaplicados)
                        } else {
                            val stocksOriginalesSustitucion = mutableMapOf<String, String>()
                            descontarStockDeProducto(sustitucionConfirmada.productoCaja, stocksOriginalesSustitucion, {
                                continuarPersistencia(sustitucionConfirmada, stocksOriginalesSustitucion)
                            }, { msg -> mostrarToastSeguro(msg); ocultarLoadingOverlayCaja() })
                        }
                    } else {
                        continuarPersistencia(null, mutableMapOf())
                    }
                }

                validarExistenciaLotesOrigenDevolucion(itemsConDevolucion) { error ->
                    if (error != null) { ocultarLoadingOverlayCaja(); mostrarToastSeguro(error) }
                    else if (tipoResolucionNormalizado == "cambio_producto" || estadoVentaObjetivo.esTotal) {
                        procesarRestauracionDevolucionTotalSegura(itemsConDevolucion, 0, motivo, motivoBloqueaVenta, mutableListOf(), { resultados ->
                            continuarPersistenciaDevolucion(resultados)
                        }, { msg, prev -> revertirRestauracionDevolucionTotalSegura(prev.asReversed(), 0, motivo, motivoBloqueaVenta, { mostrarToastSeguro(msg); ocultarLoadingOverlayCaja() }) })
                    } else { continuarPersistenciaDevolucion() }
                }
            }
            .addOnFailureListener { e -> ocultarLoadingOverlayCaja(); mostrarToastSeguro("Error al obtener venta: ${e.message}") }
    }


    private fun procesarRestauracionDevolucionRecursiva(
        items: List<ItemDevolucionUi>,
        index: Int,
        idVenta: String,
        idDevolucion: String,
        motivoDevolucion: String,
        motivoBloqueaVenta: Boolean = false,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (index >= items.size) {
            onComplete()
            return
        }

        val item = items[index]
        item.productoVenta
        val unidadesARestaurar = item.cantidadADevolver * item.unidadesPorPresentacion
        if (unidadesARestaurar <= 0) {
            procesarRestauracionDevolucionRecursiva(
                items = items,
                index = index + 1,
                idVenta = idVenta,
                idDevolucion = idDevolucion,
                motivoDevolucion = motivoDevolucion,
                motivoBloqueaVenta = motivoBloqueaVenta,
                onComplete = onComplete,
                onError = onError
            )
            return
        }

        ajustarInventarioDevolucionItemExacta(
            item = item,
            motivoDevolucion = motivoDevolucion,
            motivoBloqueaVenta = motivoBloqueaVenta,
            onSuccess = { resultado ->
                registrarResultadosRestauracionDevolucion(
                    resultados = listOf(resultado),
                    idVenta = idVenta,
                    idDevolucion = idDevolucion,
                    motivoDevolucion = motivoDevolucion,
                    motivoBloqueaVenta = motivoBloqueaVenta
                )
                procesarRestauracionDevolucionRecursiva(
                    items = items,
                    index = index + 1,
                    idVenta = idVenta,
                    idDevolucion = idDevolucion,
                    motivoDevolucion = motivoDevolucion,
                    motivoBloqueaVenta = motivoBloqueaVenta,
                    onComplete = onComplete,
                    onError = onError
                )
            },
            onError = onError
        )
        return

/*
        val productoRef = database.reference
            .child("Inventario")
            .child("Productos")
            .child(item.indiceProducto)

        var loteRepuestoExacto = productoVenta.loteNumero.trim().isBlank()
        var detalleDevolucionLote = ""
        val timestampBloqueo = System.currentTimeMillis()

        productoRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val stockNode = currentData.child("cantidadinicial")
                val stockActual = when (val raw = stockNode.value) {
                    is Long -> raw.toInt()
                    is Int -> raw
                    is Double -> raw.toInt()
                    is String -> raw.trim().toIntOrNull() ?: 0
                    null -> 0
                    else -> 0
                }
                if (!motivoBloqueaVenta) {
                    stockNode.value = (stockActual + unidadesARestaurar).toString()
                }

                val lotesNode = currentData.child("lotes")
                val detallesLotesConsumidos = detallesLotesParaMovimiento(productoVenta, unidadesARestaurar)
                    .filter { it.cantidad > 0 }

                if (detallesLotesConsumidos.isNotEmpty()) {
                    val lotesNoEncontrados = mutableListOf<String>()
                    detallesLotesConsumidos.forEach { detalle ->
                        val loteAReponer = lotesNode.children.firstOrNull { loteChild ->
                            loteChild.key?.equals(detalle.clave, ignoreCase = true) == true
                        } ?: lotesNode.children.firstOrNull { loteChild ->
                            val numero = loteChild.child("numero").value?.toString().orEmpty().trim()
                            numero.equals(detalle.numero, ignoreCase = true)
                        }

                        if (loteAReponer != null) {
                            val campoCantidad = if (motivoBloqueaVenta) "cantidadBloqueada" else "cantidad"
                            val cantidadNode = loteAReponer.child(campoCantidad)
                            val cantidadActual = when (val raw = cantidadNode.value) {
                                is Long -> raw.toDouble()
                                is Int -> raw.toDouble()
                                is Double -> raw
                                is String -> raw.trim().toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                            cantidadNode.value = cantidadActual + detalle.cantidad

                            if (motivoBloqueaVenta) {
                                loteAReponer.child("motivoBloqueo").value =
                                    motivoBloqueoRegistrado(motivoDevolucion)
                                loteAReponer.child("timestampUltimoBloqueo").value = timestampBloqueo
                                limpiarSeleccionManualSiCoincideConLoteBloqueado(currentData, loteAReponer)
                            } else {
                                val estadoNode = loteAReponer.child("estado")
                                val estadoActual = estadoNode.value?.toString().orEmpty()
                                if (estadoActual.equals("agotado", ignoreCase = true)) {
                                    estadoNode.value = "activo"
                                }
                            }
                        } else {
                            lotesNoEncontrados += detalle.numero.ifBlank { detalle.clave }
                        }
                    }

                    loteRepuestoExacto = lotesNoEncontrados.isEmpty()
                    if (lotesNoEncontrados.isNotEmpty()) {
                        detalleDevolucionLote =
                            "No se encontraron los lotes ${lotesNoEncontrados.joinToString(", ")} para reponer la devolución de forma exacta. Solo se restauró stock global."
                    }
                } else {
                    val loteNumero = productoVenta.loteNumero.trim()
                    val loteClaveConsumida = productoVenta.loteClaveConsumida.trim()
                    if (loteNumero.isNotBlank() || loteClaveConsumida.isNotBlank()) {
                        val loteAReponer = if (loteClaveConsumida.isNotBlank()) {
                            lotesNode.children.firstOrNull { loteChild ->
                                loteChild.key?.equals(loteClaveConsumida, ignoreCase = true) == true
                            }
                        } else {
                            null
                        } ?: lotesNode.children.firstOrNull { loteChild ->
                            val numero = loteChild.child("numero").value?.toString().orEmpty().trim()
                            loteChild.key?.equals(loteNumero, ignoreCase = true) == true ||
                                numero.equals(loteNumero, ignoreCase = true)
                        }

                        if (loteAReponer != null) {
                            val campoCantidad = if (motivoBloqueaVenta) "cantidadBloqueada" else "cantidad"
                            val cantidadNode = loteAReponer.child(campoCantidad)
                            val cantidadActual = when (val raw = cantidadNode.value) {
                                is Long -> raw.toDouble()
                                is Int -> raw.toDouble()
                                is Double -> raw
                                is String -> raw.trim().toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                            cantidadNode.value = cantidadActual + unidadesARestaurar

                            if (motivoBloqueaVenta) {
                                loteAReponer.child("motivoBloqueo").value =
                                    motivoBloqueoRegistrado(motivoDevolucion)
                                loteAReponer.child("timestampUltimoBloqueo").value = timestampBloqueo
                                limpiarSeleccionManualSiCoincideConLoteBloqueado(currentData, loteAReponer)
                            } else {
                                val estadoNode = loteAReponer.child("estado")
                                val estadoActual = estadoNode.value?.toString().orEmpty()
                                if (estadoActual.equals("agotado", ignoreCase = true)) {
                                    estadoNode.value = "activo"
                                }
                            }
                            loteRepuestoExacto = true
                        } else {
                            loteRepuestoExacto = false
                            detalleDevolucionLote = if (motivoBloqueaVenta) {
                                "No se encontro el lote consumido ${productoVenta.loteNumero.ifBlank { productoVenta.loteClaveConsumida }} para bloquearlo. La devolucion requiere revision."
                            } else {
                                "No se encontro el lote consumido ${productoVenta.loteNumero.ifBlank { productoVenta.loteClaveConsumida }} para reponerlo. Solo se restauro stock global."
                            }
                        }
                    }
                }

                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error == null && committed) {
                    val stockFinal = currentData?.child("cantidadinicial")?.value?.toString()?.toIntOrNull() ?: 0
                    if (loteRepuestoExacto &&
                        (productoVenta.lotesConsumidosDetalle.isNotEmpty() || productoVenta.loteNumero.trim().isNotBlank())
                    ) {
                        if (motivoBloqueaVenta) {
                            registrarMovimientoBloqueoDevolucionEnLote(
                                productoCaja = productoVenta,
                                cantidadUnidades = unidadesARestaurar,
                                idVenta = idVenta,
                                motivoDevolucion = motivoDevolucion
                            )
                        } else {
                            registrarMovimientoDevolucionEnLote(
                                productoCaja = productoVenta,
                                cantidadUnidades = unidadesARestaurar,
                                idVenta = idVenta
                            )
                        }
                    }

                    registrarMovimientosInventarioPorDevolucion(
                        producto = productoVenta,
                        cantidadRepuesta = if (motivoBloqueaVenta) 0 else unidadesARestaurar,
                        cantidadBloqueada = if (motivoBloqueaVenta) unidadesARestaurar else 0,
                        stockAntes = if (motivoBloqueaVenta) {
                            stockFinal
                        } else {
                            (stockFinal - unidadesARestaurar).coerceAtLeast(0)
                        },
                        idVenta = idVenta,
                        idDevolucion = idDevolucion,
                        motivoDevolucion = motivoDevolucion,
                        loteRepuestoExacto = loteRepuestoExacto,
                        detalleDevolucionLote = detalleDevolucionLote,
                        bloqueoVenta = motivoBloqueaVenta
                    )
                }

                procesarRestauracionDevolucionRecursiva(
                    items = items,
                    index = index + 1,
                    idVenta = idVenta,
                    idDevolucion = idDevolucion,
                    motivoDevolucion = motivoDevolucion,
                    motivoBloqueaVenta = motivoBloqueaVenta,
                    onComplete = onComplete,
                    onError = onError
                )
            }
        })
*/
    }

    private fun revertirRestauracionDevolucionParcialSegura(
        items: List<ItemDevolucionUi>,
        index: Int = 0,
        motivoDevolucion: String,
        motivoBloqueaVenta: Boolean,
        fallosAcumulados: MutableList<Pair<ItemDevolucionUi, String>> = mutableListOf(),
        onComplete: (fallos: List<Pair<ItemDevolucionUi, String>>) -> Unit
    ) {
        if (index >= items.size) {
            if (fallosAcumulados.isNotEmpty()) {
                // Registro técnico en auditoría
                MovimientoLogger.registrar(
                    tipo = "error_reversion_devolucion",
                    modulo = "caja",
                    titulo = "Reversión incompleta tras error de red",
                    descripcion = "No se pudieron revertir: " +
                            fallosAcumulados.joinToString("; ") { "${it.first.nombre}: ${it.second}" },
                    idUsuario = idCajera,
                    nombreUsuario = nombreCajera,
                    extra = mapOf(
                        "idTurno" to idTurnoActivo,
                        "itemsAfectados" to fallosAcumulados.map { it.first.itemVentaId }
                    )
                )

                // FIX: Notificación visual para el cajero
                mostrarToastSeguro("¡Atención! ${fallosAcumulados.size} producto(s) no pudieron devolverse al stock. Revisa el historial.")
            }
            onComplete(fallosAcumulados.toList())
            return
        }

        val item = items[index]
        ajustarInventarioDevolucionItemExacta(
            item = item,
            motivoDevolucion = motivoDevolucion,
            motivoBloqueaVenta = motivoBloqueaVenta,
            revertir = true,
            onSuccess = {
                revertirRestauracionDevolucionParcialSegura(
                    items, index + 1, motivoDevolucion, motivoBloqueaVenta,
                    fallosAcumulados, onComplete
                )
            },
            onError = { mensaje ->
                fallosAcumulados.add(item to mensaje)
                revertirRestauracionDevolucionParcialSegura(
                    items, index + 1, motivoDevolucion, motivoBloqueaVenta,
                    fallosAcumulados, onComplete
                )
            }
        )
    }

    private fun actualizarTurnoPostDevolucion(
        deltaVentasNeto: Double,
        montoDevueltoCliente: Double,
        metodoPagoOriginal: String = "",
        detalleMetodosVentaOriginal: Map<String, Double> = emptyMap(),
        pagoDiferencia: PagoDiferenciaDevolucion? = null,
        decrementarNumeroVentas: Boolean = false,
        onComplete: () -> Unit
    ) {
        val ref = getTurnoActivoRef() ?: run { onComplete(); return }

        val usarPagoDiferencia = deltaVentasNeto > 0.0 &&
                pagoDiferencia != null &&
                pagoDiferencia.metodoPago.isNotBlank()

        val metodoTexto = if (usarPagoDiferencia) {
            pagoDiferencia?.metodoPago.orEmpty().trim()
        } else {
            metodoPagoOriginal.substringBefore(" (Ref:").trim()
        }

        val deltaMetodo = if (usarPagoDiferencia) {
            pagoDiferencia?.montoPagado ?: deltaVentasNeto
        } else {
            deltaVentasNeto
        }

        // Clave normalizada en resumenMetodos
        val metodoNormKey = CajaTurnoHelper.normalizarClaveResumen(
            if (metodoTexto.startsWith("Mixto:", ignoreCase = true)) "Pago mixto" else metodoTexto
        )

        ref.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val map = CajaTurnoHelper.mutableMapFromData(currentData)

                val totalVentasPrevio = map["totalVentas"]?.toString()?.toDoubleOrNull() ?: 0.0
                map["totalVentas"] = String.format(
                    Locale.US, "%.2f",
                    (totalVentasPrevio + deltaVentasNeto).coerceAtLeast(0.0)
                )

                if (decrementarNumeroVentas) {
                    val numVentasPrevio = map["numeroVentas"]?.toString()?.toIntOrNull() ?: 0
                    map["numeroVentas"] = (numVentasPrevio - 1).coerceAtLeast(0).toString()
                }

                if (usarPagoDiferencia && deltaMetodo > 0.0) {
                    map["ultimoMetodoPago"] = metodoTexto.ifBlank { "Otro" }
                    map["ultimaVenta"] = obtenerHoraActual()
                    map["ultimoMontoRecibido"] = String.format(Locale.US, "%.2f", deltaMetodo)
                }

                // 1. Manejo de Diferencias Cobradas (Solo si delta > 0)
                if (usarPagoDiferencia && deltaMetodo > 0.0) {
                    val detalleDiferencia = pagoDiferencia?.detalleMetodos
                        ?.filterValues { kotlin.math.abs(it) > 0.009 }
                        ?.takeIf { it.isNotEmpty() }
                        ?: mapOf(metodoTexto.ifBlank { "Otro" } to deltaMetodo)

                    val resumenDiferencias = CajaTurnoHelper.mutableMapFromValue(map["resumenDiferenciasCobradas"])
                    val totalDiferenciasPrevio = map["totalDiferenciasCobradas"]?.toString()?.toDoubleOrNull() ?: 0.0

                    detalleDiferencia.forEach { (titulo, montoMetodo) ->
                        val key = CajaTurnoHelper.normalizarClaveResumen(titulo)
                        val dataMetodo = CajaTurnoHelper.mutableMapFromValue(resumenDiferencias[key])
                        val montoPrevioMetodo = dataMetodo["monto"]?.toString()?.toDoubleOrNull() ?: 0.0
                        dataMetodo["titulo"] = titulo.ifBlank { "Otro" }
                        dataMetodo["monto"] = String.format(Locale.US, "%.2f", (montoPrevioMetodo + montoMetodo).coerceAtLeast(0.0))
                        resumenDiferencias[key] = dataMetodo
                    }
                    map["resumenDiferenciasCobradas"] = resumenDiferencias
                    map["totalDiferenciasCobradas"] = String.format(Locale.US, "%.2f", (totalDiferenciasPrevio + deltaMetodo).coerceAtLeast(0.0))
                }

                val totalDevPrevio = map["totalDevoluciones"]?.toString()?.toDoubleOrNull() ?: 0.0
                map["totalDevoluciones"] = String.format(
                    Locale.US, "%.2f",
                    (totalDevPrevio + montoDevueltoCliente).coerceAtLeast(0.0)
                )

                // 2. DETERMINAR DESGLOSE PARA resumenMetodos (Soporte Mixto)
                val esVentaMixta = metodoPagoOriginal.startsWith("Mixto:", ignoreCase = true)
                val detalleParaTurno: Map<String, Double> = when {
                    usarPagoDiferencia -> {
                        pagoDiferencia?.detalleMetodos
                            ?.filterValues { kotlin.math.abs(it) > 0.009 }
                            ?.takeIf { it.isNotEmpty() }
                            ?: mapOf(metodoTexto.ifBlank { "Otro" } to deltaMetodo)
                    }
                    esVentaMixta && detalleMetodosVentaOriginal.isNotEmpty() -> {
                        val totalOriginal = detalleMetodosVentaOriginal.values.sum().takeIf { it > 0.009 } ?: 1.0
                        detalleMetodosVentaOriginal.mapValues { (_, monto) ->
                            deltaVentasNeto * (monto / totalOriginal)
                        }
                    }
                    else -> mapOf(metodoTexto.ifBlank { "Otro" } to deltaMetodo)
                }

                // 3. Aplicar desglose a resumenMetodos
                val resumen = CajaTurnoHelper.mutableMapFromValue(map["resumenMetodos"])
                detalleParaTurno.forEach { (titulo, montoMetodo) ->
                    if (kotlin.math.abs(montoMetodo) < 0.009) return@forEach
                    val key = CajaTurnoHelper.normalizarClaveResumen(titulo)
                    val dataMetodo = CajaTurnoHelper.mutableMapFromValue(resumen[key])
                    val montoPrevioMetodo = dataMetodo["monto"]?.toString()?.toDoubleOrNull() ?: 0.0
                    dataMetodo["monto"] = String.format(Locale.US, "%.2f", (montoPrevioMetodo + montoMetodo).coerceAtLeast(0.0))
                    if (dataMetodo["titulo"]?.toString().isNullOrBlank()) {
                        dataMetodo["titulo"] = titulo.ifBlank { "Otro" }
                    }
                    resumen[key] = dataMetodo
                }
                map["resumenMetodos"] = resumen

                // 4. Actualizar ventasEfectivo y efectivoEsperado usando el desglose
                val apertura = map["montoApertura"]?.toString()?.toDoubleOrNull() ?: 0.0
                val totalEgresos = map["totalEgresos"]?.toString()?.toDoubleOrNull() ?: 0.0
                val ventasEfectivoPrevio = map["ventasEfectivo"]?.toString()?.toDoubleOrNull() ?: 0.0
                val devolucionesEfectivoPrevio = map["devolucionesEfectivo"]?.toString()?.toDoubleOrNull() ?: 0.0

                val efectivoNeto = detalleParaTurno
                    .filterKeys { it.contains("efectivo", ignoreCase = true) }
                    .values
                    .sum()

                val nuevoVentasEfectivo = (ventasEfectivoPrevio + efectivoNeto).coerceAtLeast(0.0)
                if (kotlin.math.abs(efectivoNeto) > 0.009) {
                    map["ventasEfectivo"] = String.format(Locale.US, "%.2f", nuevoVentasEfectivo)
                }

                val montoDevueltoEfectivo = if (!usarPagoDiferencia && efectivoNeto < 0.0) {
                    kotlin.math.abs(efectivoNeto)
                } else 0.0

                val nuevasDevolucionesEfectivo = (devolucionesEfectivoPrevio + montoDevueltoEfectivo).coerceAtLeast(0.0)
                map["devolucionesEfectivo"] = String.format(Locale.US, "%.2f", nuevasDevolucionesEfectivo)

                // 5. Recalcular efectivoEsperado
                map["efectivoEsperado"] = String.format(
                    Locale.US, "%.2f",
                    apertura + nuevoVentasEfectivo - totalEgresos
                )

                map["ultimaActualizacion"] = System.currentTimeMillis()
                currentData.value = map
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                onComplete()
            }
        })
    }

    // -------------------------------------------------------------------------------------

    private fun mostrarDialogoRegistrarEgreso() {
        if (estaCajaEnModoSoloVista(notificar = true)) return
        if (!puedeOperarTurnoActual()) return

// CANDADO HORARIO: Bloquea egresos si la hora está mal
        if (!verificarHoraAntesDeOperar("registrar egresos de caja")) return

        if (!turnoAbierto || idTurnoActivo.isBlank()) {
            mostrarToastSeguro("No hay un turno abierto para registrar egresos")
            return
        }

        dialogoRegistrarEgresoActual?.dismiss()
        comprobanteEgresoUri = null
        comprobanteEgresoCameraUri = null
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_registrar_egreso, null, false)

        val layoutMonto = dialogView.findViewById<TextInputLayout>(R.id.inputMontoEgresoDialog)
        val inputMonto = dialogView.findViewById<TextInputEditText>(R.id.etMontoEgresoDialog)
        val layoutMotivo = dialogView.findViewById<TextInputLayout>(R.id.inputMotivoEgresoDialog)
        val inputMotivo = dialogView.findViewById<TextInputEditText>(R.id.etMotivoEgresoDialog)
        val cardAdjuntar = dialogView.findViewById<MaterialCardView>(R.id.cardAdjuntarComprobanteEgreso)
        val layoutHintComprobante = dialogView.findViewById<View>(R.id.layoutHintComprobanteEgresoDialog)
        val tvComprobanteHint = dialogView.findViewById<TextView>(R.id.tvHintComprobanteEgresoDialog)
        val imageComprobante = dialogView.findViewById<ImageView>(R.id.imgPreviewComprobanteEgresoDialog)
        val btnGuardar = dialogView.findViewById<MaterialButton>(R.id.btnGuardarEgresoDialog)
        val btnCancelar = dialogView.findViewById<MaterialButton>(R.id.btnCancelarEgresoDialog)
        val btnQuitarComprobante = dialogView.findViewById<TextView>(R.id.btnQuitarComprobanteEgresoDialog)
        val tvEfectivoDisponible = dialogView.findViewById<TextView>(R.id.tvEfectivoDisponibleEgreso)

        // Texto temporal
        tvEfectivoDisponible?.text = "Efectivo disponible: cargando..."

        comprobanteEgresoPreview = imageComprobante
        comprobanteEgresoHint = tvComprobanteHint
        comprobanteEgresoHintContainer = layoutHintComprobante
        comprobanteEgresoQuitar = btnQuitarComprobante
        actualizarPreviewComprobanteEgreso()
        configurarOcultarTecladoEnDialogo(dialogView, inputMonto, inputMotivo)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            dialog.window?.setDimAmount(0.85f)
            if (esTablet()) {
                val width = (resources.displayMetrics.widthPixels * 0.38f).toInt()
                dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
        }

        dialog.setOnDismissListener {
            if (dialogoRegistrarEgresoActual === dialog) {
                dialogoRegistrarEgresoActual = null
            }

            tokenSubidaComprobanteEgreso++
            comprobanteEgresoPreview = null
            comprobanteEgresoHint = null
            comprobanteEgresoHintContainer = null
            comprobanteEgresoQuitar = null
        }

        // Cargar efectivo disponible
        getTurnoActivoRef()?.get()
            ?.addOnSuccessListener { snapshot ->
                val efectivo = snapshot.child("efectivoEsperado").getValue(String::class.java)?.toDoubleOrNull() ?: 0.0
                tvEfectivoDisponible?.text = "Efectivo disponible: ${MonedaHelper.formatear(efectivo)}"
            }
            ?.addOnFailureListener {
                tvEfectivoDisponible?.text = "Efectivo disponible: error"
            }

        fun actualizarEstadoBotonGuardar() {
            val montoValido = inputMonto.text?.toString().toDoubleSeguro(null)?.let { it > 0.0 } ?: false
            val motivoValido = inputMotivo.text?.toString()?.trim().orEmpty().isNotBlank()
            btnGuardar.isEnabled = montoValido && motivoValido
            btnGuardar.alpha = if (btnGuardar.isEnabled) 1f else 0.5f
        }

        inputMonto.doAfterTextChanged {
            layoutMonto.error = null
            actualizarEstadoBotonGuardar()
        }

        inputMotivo.doAfterTextChanged {
            layoutMotivo.error = null
            actualizarEstadoBotonGuardar()
        }

        cardAdjuntar.setOnClickListener { mostrarOpcionesComprobanteEgreso() }
        btnQuitarComprobante.setOnClickListener {
            comprobanteEgresoUri = null
            comprobanteEgresoCameraUri = null
            actualizarPreviewComprobanteEgreso()
        }
        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnGuardar.setOnClickListener {
            layoutMonto.error = null
            layoutMotivo.error = null

            val monto = inputMonto.text?.toString().toDoubleSeguro(null)
            val motivo = inputMotivo.text?.toString()?.trim().orEmpty()

            if (monto == null) {
                layoutMonto.error = "Ingresa un monto válido"
                inputMonto.requestFocus()
                return@setOnClickListener
            }
            if (monto <= 0.0) {
                layoutMonto.error = "El monto debe ser mayor a 0"
                inputMonto.requestFocus()
                return@setOnClickListener
            }
            if (motivo.isBlank()) {
                layoutMotivo.error = "Ingresa el motivo del egreso"
                inputMotivo.requestFocus()
                return@setOnClickListener
            }

            // Validar contra efectivo disponible si se pudo cargar
            val textoEfectivo = tvEfectivoDisponible?.text?.toString()
            if (textoEfectivo != null && textoEfectivo != "Efectivo disponible: cargando..." && textoEfectivo != "Efectivo disponible: error") {
                val efectivoDisponible = textoEfectivo.replace("Efectivo disponible: ", "").let {
                    MonedaHelper.parsear(it) // necesitas una función que convierta "Bs 1.234,56" a Double
                }
                if (efectivoDisponible != null && monto > efectivoDisponible) {
                    dialogoConfirmacionEgresoActual?.dismiss()
                    val dialogoConfirmacion = MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Egreso mayor al efectivo disponible")
                        .setMessage("El monto del egreso (${MonedaHelper.formatear(monto)}) supera el efectivo disponible (${MonedaHelper.formatear(efectivoDisponible)}). ¿Deseas continuar de todas formas?")
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Continuar") { _, _ ->
                            procesarEgreso(monto, motivo, dialog, btnGuardar, btnCancelar)
                        }
                        .create()
                    dialogoConfirmacion.setOnDismissListener {
                        if (dialogoConfirmacionEgresoActual === dialogoConfirmacion) {
                            dialogoConfirmacionEgresoActual = null
                        }
                    }
                    dialogoConfirmacionEgresoActual = dialogoConfirmacion
                    dialogoConfirmacion.show()
                    return@setOnClickListener
                }
            }

            procesarEgreso(monto, motivo, dialog, btnGuardar, btnCancelar)
        }

        actualizarEstadoBotonGuardar()
        dialogoRegistrarEgresoActual = dialog
        dialog.show()
    }

    private fun procesarEgreso(
        monto: Double,
        motivo: String,
        dialog: AlertDialog,
        btnGuardar: MaterialButton,
        btnCancelar: MaterialButton
    ) {
        fun restaurarBotones() {
            btnCancelar.isEnabled = true
            btnGuardar.isEnabled = true
            btnGuardar.text = "Guardar egreso"
            btnGuardar.alpha = 1f
        }

        fun guardarEgreso(comprobanteUrl: String?) {
            registrarEgresoCaja(
                monto = monto,
                motivo = motivo,
                comprobanteUrl = comprobanteUrl,
                onSuccess = {
                    comprobanteEgresoUri = null
                    comprobanteEgresoCameraUri = null
                    actualizarPreviewComprobanteEgreso()
                    dialog.dismiss()
                },
                onFailure = { mensajeError ->
                    restaurarBotones()
                    mostrarToastSeguro(mensajeError)
                }
            )
        }

        val fotoSeleccionada = comprobanteEgresoUri
        if (fotoSeleccionada != null) {
            val tokenSubidaActual = ++tokenSubidaComprobanteEgreso
            btnGuardar.isEnabled = false
            btnGuardar.text = "Subiendo comprobante..."
            btnCancelar.isEnabled = false

            subirImagenComprobanteEgreso(
                uriLocal = fotoSeleccionada,
                tokenSubida = tokenSubidaActual,
                nombreBase = "egreso_${obtenerIdCajaOperativa()}_${System.currentTimeMillis()}",
                onSuccess = { url ->
                    btnGuardar.text = "Guardando egreso..."
                    guardarEgreso(url)
                },
                onFailure = { error ->
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("No se pudo subir el comprobante")
                        .setMessage("¿Quieres registrar el egreso sin comprobante?\n\nDetalle: $error")
                        .setNegativeButton("Cancelar") { _, _ -> restaurarBotones() }
                        .setPositiveButton("Guardar igual") { _, _ ->
                            btnGuardar.isEnabled = false
                            btnGuardar.text = "Guardando egreso..."
                            btnCancelar.isEnabled = false
                            guardarEgreso(null)
                        }
                        .show()
                }
            )
        } else {
            btnGuardar.isEnabled = false
            btnGuardar.text = "Guardando egreso..."
            btnCancelar.isEnabled = false
            guardarEgreso(null)
        }
    }

    private fun registrarEgresoCaja(monto: Double, motivo: String, comprobanteUrl: String?, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        obtenerMomentoOficialCaja(
            onSuccess = { momento ->
                registrarEgresoCajaConMomento(
                    monto = monto,
                    motivo = motivo,
                    comprobanteUrl = comprobanteUrl,
                    momento = momento,
                    onSuccess = onSuccess,
                    onFailure = onFailure
                )
            },
            onError = {
                onFailure(FechaHoraServidorHelper.MENSAJE_ERROR_FECHA_HORA)
            }
        )
    }

    private fun registrarEgresoCajaConMomento(
        monto: Double,
        motivo: String,
        comprobanteUrl: String?,
        momento: FechaHoraServidorHelper.FechaHoraOficial,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val turnoRef = getTurnoActivoRef()
        if (turnoRef == null) {
            onFailure("No hay turno activo para registrar el egreso")
            return
        }

        val idEgreso = turnoRef.child("egresos").push().key
        if (idEgreso.isNullOrBlank()) {
            onFailure("No se pudo generar el egreso")
            return
        }

        turnoRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val turnoActual = CajaTurnoHelper.mutableMapFromData(currentData)

                val estadoActual = turnoActual["estado"]?.toString().orEmpty()
                if (!estadoActual.equals("abierta", ignoreCase = true)) {
                    return Transaction.abort()
                }

                val totalEgresosPrevio = currentData.child("totalEgresos").value?.toString()?.toDoubleOrNull() ?: 0.0
                val nuevoTotalEgresos = totalEgresosPrevio + monto
                turnoActual["totalEgresos"] = String.format(Locale.US, "%.2f", nuevoTotalEgresos)

                val ventasEfectivo = currentData.child("ventasEfectivo").value.toString().toDoubleOrNull() ?: 0.0
                val apertura = currentData.child("montoApertura").value.toString().toDoubleOrNull() ?: 0.0
                turnoActual["efectivoEsperado"] =
                    String.format(Locale.US, "%.2f", apertura + ventasEfectivo - nuevoTotalEgresos)
                turnoActual["ultimaActualizacion"] = momento.timestampServidorMs
                turnoActual["ultimoEgresoHora"] = momento.horaTexto

                val egresosActuales = CajaTurnoHelper.mutableMapFromValue(turnoActual["egresos"])

                val tsLocal = momento.timestampServidorMs
                val horaActual = momento.horaTexto
                egresosActuales[idEgreso] = mapOf(
                    "idEgreso" to idEgreso,
                    "monto" to String.format(Locale.US, "%.2f", monto),
                    "motivo" to motivo,
                    
                    // UI Fields
                    "horaEgresoLocal" to horaActual,
                    "fechaEgresoLocal" to momento.fechaFirebase,

                    // Accounting Fields
                    "timestampEgresoServidor" to ServerValue.TIMESTAMP,

                    // Legacy
                    "hora" to horaActual,
                    "timestamp" to tsLocal,
                    "timestampServidor" to ServerValue.TIMESTAMP,

                    "idUsuario" to idCajera,
                    "nombreUsuario" to nombreCajera,
                    "tieneComprobante" to !comprobanteUrl.isNullOrBlank(),
                    "comprobanteUrl" to comprobanteUrl.orEmpty()
                )
                turnoActual["egresos"] = egresosActuales

                currentData.value = turnoActual
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    onFailure("No se pudo registrar el egreso: ${error.message}")
                    return
                }

                if (!committed) {
                    onFailure("No se pudo registrar el egreso en el turno actual")
                    return
                }

                // Actualizar el timestampServidor real ahora que la transacción se confirmó
                turnoRef.child("egresos").child(idEgreso)
                    .child("timestampServidor").setValue(ServerValue.TIMESTAMP)
                sincronizarCamposVisiblesDesdeTimestampServidor(
                    ref = turnoRef.child("egresos").child(idEgreso),
                    campoTimestampServidor = "timestampServidor",
                    rutasFecha = listOf("fechaEgresoLocal"),
                    rutasHora = listOf("horaEgresoLocal", "hora")
                )

                MovimientoLogger.registrar(
                    tipo = "egreso_caja",
                    modulo = "caja",
                    titulo = "Egreso registrado",
                    descripcion = "Egreso por ${MonedaHelper.formatear(monto)} - $motivo",
                    idUsuario = idCajera,
                    nombreUsuario = nombreCajera,
                    monto = monto,
                    referenciaId = idEgreso,
                    idCaja = obtenerIdCajaOperativa(),
                    nombreCaja = obtenerNombreCajaOperativa(),
                    extra = mapOf(
                        "motivo" to motivo,
                        "idTurno" to idTurnoActivo,
                        "tieneComprobante" to !comprobanteUrl.isNullOrBlank(),
                        "comprobanteUrl" to comprobanteUrl.orEmpty()
                    ),
                    database = database
                )

                mostrarToastSeguro("Egreso registrado correctamente")
                onSuccess()
            }
        })
    }

    private fun mostrarOpcionesComprobanteEgreso() {
        if (estaCajaEnModoSoloVista(notificar = true)) return
        dialogoOpcionesComprobanteEgresoActual?.dismiss()
        val opciones = arrayOf("Tomar foto", "Elegir de galería")
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Adjuntar comprobante")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> cameraPermissionComprobanteEgresoLauncher.launch(Manifest.permission.CAMERA)
                    1 -> galleryComprobanteEgresoLauncher.launch("image/*")
                }
            }
            .create()
        dialog.setOnDismissListener {
            if (dialogoOpcionesComprobanteEgresoActual === dialog) {
                dialogoOpcionesComprobanteEgresoActual = null
            }
        }
        dialogoOpcionesComprobanteEgresoActual = dialog
        dialog.show()
    }

    private fun abrirCamaraParaComprobanteEgreso() {
        try {
            val picturesDir = requireContext().getExternalFilesDir(null) ?: requireContext().filesDir
            val photoFile = File.createTempFile(
                "egreso_${System.currentTimeMillis()}",
                ".jpg",
                picturesDir
            )

            comprobanteEgresoCameraUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                photoFile
            )

            cameraComprobanteEgresoLauncher.launch(comprobanteEgresoCameraUri)
        } catch (e: Exception) {
            mostrarToastSeguro("No se pudo abrir la cámara: ${e.message}")
        }
    }

    private fun actualizarPreviewComprobanteEgreso() {
        val uri = comprobanteEgresoUri
        val preview = comprobanteEgresoPreview
        val hint = comprobanteEgresoHint
        val hintContainer = comprobanteEgresoHintContainer
        val btnQuitar = comprobanteEgresoQuitar

        if (preview == null || hint == null || hintContainer == null || btnQuitar == null || !isAdded) return

        if (uri == null) {
            preview.setImageDrawable(null)
            preview.visibility = View.GONE
            hint.visibility = View.VISIBLE
            hintContainer.visibility = View.VISIBLE
            btnQuitar.visibility = View.GONE
            return
        }

        Glide.with(this)
            .load(uri)
            .centerCrop()
            .into(preview)

        preview.visibility = View.VISIBLE
        hint.visibility = View.GONE
        hintContainer.visibility = View.GONE
        btnQuitar.visibility = View.VISIBLE
    }

    private fun subirImagenComprobanteEgreso(
        uriLocal: Uri,
        tokenSubida: Long,
        nombreBase: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val ctx = context ?: return

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // CANDADO 1: Si el usuario cerró el fragmento antes de empezar, frenamos aquí
                ensureActive()

                val archivoComprimido = comprimirImagenComprobante(
                    ctx = ctx,
                    uriLocal = uriLocal,
                    nombreBase = nombreBase
                )

                var secureUrlFinal = ""
                var ultimoError = "No se pudo subir el comprobante"

                repeat(3) { intento ->
                    // CANDADO 2: Si la corutina fue cancelada en el intento anterior, no lances el siguiente viaje de red
                    ensureActive()

                    try {
                        val boundary = "Boundary-${UUID.randomUUID()}"
                        val endpoint = URL("https://api.cloudinary.com/v1_1/$CLOUDINARY_CLOUD_NAME/image/upload")
                        val connection = (endpoint.openConnection() as HttpURLConnection).apply {
                            requestMethod = "POST"
                            doInput = true
                            doOutput = true
                            useCaches = false
                            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                        }

                        // ... (Todo tu bloque de escritura DataOutputStream se queda igual) ...

                        val responseCode = connection.responseCode
                        val responseBody = try {
                            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
                            stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                        } finally {
                            connection.disconnect()
                        }

                        if (responseCode !in 200..299) {
                            throw Exception(responseBody.ifBlank { "Cloudinary no aceptó la imagen" })
                        }

                        val secureUrl = JSONObject(responseBody).optString("secure_url")
                        if (secureUrl.isBlank()) {
                            throw Exception("Cloudinary no devolvió la URL")
                        }

                        secureUrlFinal = secureUrl
                        return@repeat
                    } catch (e: Exception) {
                        // Tu filtro actual deja pasar CancellationException, lo cual es perfecto para ensureActive()
                        if (e is kotlinx.coroutines.CancellationException) throw e

                        ultimoError = e.message ?: "No se pudo subir el comprobante"
                        if (intento < 2) {
                            delay(900)
                        }
                    }
                }

                if (secureUrlFinal.isBlank()) throw Exception(ultimoError)

                // CANDADO 3: Verificación final justo antes de saltar al hilo principal
                ensureActive()

                withContext(Dispatchers.Main) {
                    if (viewLifecycleOwner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) {
                        ejecutarSiSubidaComprobanteSigueActiva(tokenSubida) {
                            onSuccess(secureUrlFinal)
                        }
                    }
                }

            } catch (e: kotlinx.coroutines.CancellationException) {
                // La corutina muere pacíficamente sin lanzar fallos a la UI
                throw e
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (viewLifecycleOwner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) {
                        ejecutarSiSubidaComprobanteSigueActiva(tokenSubida) {
                            onFailure(e.message ?: "No se pudo subir el comprobante")
                        }
                    }
                }
            } finally {
                try {
                    val archivoCmp = File(ctx.cacheDir, "${sanitizarNombreArchivo(nombreBase)}_cmp.jpg")
                    if (archivoCmp.exists()) archivoCmp.delete()
                } catch (_: Exception) {}
            }
        }
    }

    private fun comprimirImagenComprobante(ctx: Context, uriLocal: Uri, nombreBase: String): File {
        val maxDimension = 1600
        val maxBytes = 500 * 1024

        val boundsOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        ctx.contentResolver.openInputStream(uriLocal)?.use { input ->
            BitmapFactory.decodeStream(input, null, boundsOptions)
        } ?: throw Exception("No se pudo leer la imagen del comprobante")

        val sampleSize = calcularSampleSize(
            width = boundsOptions.outWidth,
            height = boundsOptions.outHeight,
            reqWidth = maxDimension,
            reqHeight = maxDimension
        )

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val bitmap = ctx.contentResolver.openInputStream(uriLocal)?.use { input ->
            BitmapFactory.decodeStream(input, null, decodeOptions)
        } ?: throw Exception("No se pudo procesar la imagen del comprobante")

        val compressedBytes = ByteArrayOutputStream()
        var quality = 82
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, compressedBytes)

        while (compressedBytes.size() > maxBytes && quality > 45) {
            compressedBytes.reset()
            quality -= 7
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, compressedBytes)
        }

        bitmap.recycle()

        val tempFile = File.createTempFile(
            "${sanitizarNombreArchivo(nombreBase)}_cmp",
            ".jpg",
            ctx.cacheDir
        )

        FileOutputStream(tempFile).use { output ->
            output.write(compressedBytes.toByteArray())
            output.flush()
        }

        return tempFile
    }

    private fun calcularSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            var halfHeight = height / 2
            var halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight &&
                (halfWidth / inSampleSize) >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }

    private fun ejecutarSiSubidaComprobanteSigueActiva(tokenSubida: Long, accion: () -> Unit) {
        if (!isAdded) return
        if (_binding == null) return
        if (tokenSubida != tokenSubidaComprobanteEgreso) return
        accion()
    }

    private fun sanitizarNombreArchivo(nombre: String): String {
        return nombre
            .trim()
            .lowercase(Locale.getDefault())
            .replace("/", "_")
            .replace("\\", "_")
            .replace(".", "_")
            .replace("#", "_")
            .replace(Regex("[()\\{\\}\\[\\]]"), "_")
            .replace(" ", "_")
    }

    private fun getVentasEnEsperaRef(): DatabaseReference {
        return database.reference
            .child("VentasEnEspera")
            .child(obtenerIdCajaOperativa())
    }

    private fun configurarAccionesVentaEspera() {
        val b = _binding ?: return

        b.btnPausarVenta?.setOnClickListener {
            pausarVentaActual()
        }

        b.btnVentasEnEspera?.setOnClickListener {
            mostrarVentasEnEspera()
        }

        actualizarAccionesVentaEsperaUi()
    }

    private fun escucharVentasEnEspera() {
        ventasEnEsperaListener?.let { listener ->
            ventasEnEsperaRef?.removeEventListener(listener)
        }

        ventasEnEsperaRef = getVentasEnEsperaRef()
        ventasEnEsperaListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cantidadVentasEnEspera = snapshot.childrenCount.toInt()
                if (!isAdded || _binding == null) return
                actualizarAccionesVentaEsperaUi()
                mostrarEstadoVacio()
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded || _binding == null) return
                cantidadVentasEnEspera = 0
                actualizarAccionesVentaEsperaUi()
            }
        }

        ventasEnEsperaRef?.addValueEventListener(ventasEnEsperaListener!!)
    }

    private fun actualizarAccionesVentaEsperaUi() {
        val b = _binding ?: return
        val hayProductos = listaProductosCaja.isNotEmpty()
        val hayVentasEnEspera = cantidadVentasEnEspera > 0
        val limiteAlcanzado = cantidadVentasEnEspera >= MAX_VENTAS_EN_ESPERA
        val bloqueado = operacionVentaEnEsperaEnCurso
        val bloqueadoPorRescate = estaEnModoRescateCajaBloqueada()
        
        // Mantener visible pero deshabilitar si no hay productos o hay rescate
        b.btnPausarVenta?.visibility = View.VISIBLE
        b.btnPausarVenta?.isEnabled = hayProductos && !bloqueado && !bloqueadoPorRescate && !limiteAlcanzado
        b.btnPausarVenta?.alpha = if (b.btnPausarVenta.isEnabled) 1f else 0.5f
        
        b.btnPausarVenta?.text = when {
            limiteAlcanzado -> "Límite en espera"
            else -> "Pausar venta"
        }

        b.btnVentasEnEspera?.text = if (hayVentasEnEspera) {
            "En espera ($cantidadVentasEnEspera)"
        } else {
            "Ver en espera"
        }
        
        b.btnVentasEnEspera?.isEnabled = hayVentasEnEspera && !bloqueado && !bloqueadoPorRescate
        b.btnVentasEnEspera?.alpha = if (b.btnVentasEnEspera.isEnabled) 1f else 0.5f
    }

    private fun actualizarAyudaCajaVaciaUi() {
        val b = _binding ?: return
        if (b.layoutComprobante.visibility == View.VISIBLE || b.layoutAperturaCaja.visibility == View.VISIBLE) {
            b.tvAyudaCajaVacia.visibility = View.GONE
            return
        }

        // En modo busqueda no mostramos el texto de ayuda - el cajero esta enfocado en buscar
        if (modoBusquedaActivo) {
            b.tvAyudaCajaVacia.visibility = View.GONE
            return
        }

        actualizarEstadoVacioCajaUi()
        b.tvAyudaCajaVacia.visibility = View.GONE
    }

    private fun actualizarEstadoVacioCajaUi() {
        val root = _binding?.root ?: return
        val titulo = root.findViewById<TextView>(R.id.tvEstadoVacioCajaTitulo) ?: return
        val detalle = root.findViewById<TextView>(R.id.tvEstadoVacioCajaDetalle) ?: return
        val mostrarTitulo = numeroVentasTurnoActual <= 0L
        titulo.text = "Inicia tu primera venta del dia"
        titulo.visibility = if (mostrarTitulo) View.VISIBLE else View.GONE
        detalle.visibility = View.GONE
    }

    private fun iniciarOperacionVentaEnEspera(titulo: String = "Procesando venta", mensaje: String = "Espera un momento..."): Boolean {
        if (operacionVentaEnEsperaEnCurso) {
            mostrarToastSeguro("Espera a que termine la operacion actual")
            return false
        }
        operacionVentaEnEsperaEnCurso = true
        actualizarAccionesVentaEsperaUi()
        mostrarDialogoProgresoVentaEnEspera(titulo, mensaje)
        return true
    }

    private fun finalizarOperacionVentaEnEspera() {
        operacionVentaEnEsperaEnCurso = false
        ocultarDialogoProgresoVentaEnEspera()
        if (!isAdded || _binding == null) return
        actualizarAccionesVentaEsperaUi()
    }




    private fun mostrarLoadingOverlayCaja(titulo: String, mensaje: String) {
        val b = _binding ?: return
        loadingOperacionCajaActivo = true
        b.layoutLoadingOverlayCaja.showElegantemente(180L)
        b.tvTituloLoadingCaja.text = titulo
        b.tvMensajeLoadingCaja.text = mensaje

        // SOLUCOIN: Generamos un identificador único para ESTA solicitud de carga exacta
        val miToken = ++tokenOverlayActual

        runnableTimeoutLoadingGenerico?.let { handlerSeguridadOperaciones.removeCallbacks(it) }
        runnableTimeoutLoadingGenerico = Runnable {
            // El Watchdog SÓLO actuará si el token no ha cambiado (nadie más renovó el loading)
            if (loadingOperacionCajaActivo && tokenOverlayActual == miToken) {
                ocultarLoadingOverlayCaja()
                operacionVentaEnEsperaEnCurso = false
                validandoStockFinalEnCurso = false
                actualizarAccionesVentaEsperaUi()

                mostrarToastSeguro("La operación está tardando demasiado. Revisa tu conexión a internet.")
            }
        }
        handlerSeguridadOperaciones.postDelayed(runnableTimeoutLoadingGenerico!!, 45_000L)
    }

    private fun ocultarLoadingOverlayCaja() {
        runnableTimeoutLoadingGenerico?.let { handlerSeguridadOperaciones.removeCallbacks(it) }
        runnableTimeoutLoadingGenerico = null

        // Al cerrar, invalidamos el token actual incrementándolo de nuevo
        tokenOverlayActual++

        loadingOperacionCajaActivo = false
        validandoStockFinalEnCurso = false

        _binding?.layoutLoadingOverlayCaja?.hideElegantemente(180L)
    }

    private fun mostrarLoadingInicialCaja() {
        val b = _binding ?: return
        if (cargaInicialCajaCompletada) return
        loadingInicialCajaActivo = true
        turnoInicialCajaListo = false
        productosInicialCajaListos = false
        runnableResetLoadingInicialCaja?.let { handlerSeguridadOperaciones.removeCallbacks(it) }
        val runnable = Runnable {
            if (!loadingInicialCajaActivo || cargaInicialCajaCompletada) return@Runnable
            Log.w("CajaOperacion", "Timeout de carga inicial: se libera solo carga de productos, esperando verificación de turno")
            productosInicialCajaListos = true
            evaluarOcultamientoLoadingInicialCaja()
        }
        runnableResetLoadingInicialCaja = runnable
        handlerSeguridadOperaciones.postDelayed(runnable, TIMEOUT_LOADING_INICIAL_CAJA_MS)
        b.layoutSkeletonCajaInicial.showElegantemente(140L)
    }

    private fun marcarTurnoInicialCajaListo() {
        turnoInicialCajaListo = true
        evaluarOcultamientoLoadingInicialCaja()
    }

    private fun abortarCobroPorCambioDeCarrito(mensaje: String) {
        val b = _binding ?: return
        val checkoutVisible = b.layoutCobroPanel.visibility == View.VISIBLE || b.layoutComprobante.visibility == View.VISIBLE

        if (checkoutVisible) {
            liberarCajaSiEstabaBloqueada()
            reiniciarFlujoCobroInline()
            if (esTablet()) aplicarLayoutInicialTablet() else ocultarLayoutComprobanteMobile()

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("⚠️ Carrito actualizado")
                .setMessage(mensaje)
                .setPositiveButton("Revisar carrito", null)
                .setCancelable(false)
                .show()
        }
    }

    private fun marcarProductosInicialCajaListos() {
        productosInicialCajaListos = true
        evaluarOcultamientoLoadingInicialCaja()
    }

    private fun evaluarOcultamientoLoadingInicialCaja() {
        if (cargaInicialCajaCompletada) return
        if (!turnoInicialCajaListo || !productosInicialCajaListos) return

        cargaInicialCajaCompletada = true
        loadingInicialCajaActivo = false
        runnableResetLoadingInicialCaja?.let { handlerSeguridadOperaciones.removeCallbacks(it) }
        runnableResetLoadingInicialCaja = null

        // #11: Pequeño delay de cortesía (60ms) para asegurar que el contenido 
        // recién cargado haya pasado por un ciclo de layout antes de retirar el skeleton.
        handlerSeguridadOperaciones.postDelayed({
            _binding?.layoutSkeletonCajaInicial?.hideElegantemente(180L)
        }, 60L)
    }

    private fun mostrarDialogoProgresoVentaEnEspera(titulo: String, mensaje: String) {
        mostrarLoadingOverlayCaja(titulo, mensaje)
    }

    private fun validarTrazabilidadExactaDevolucionCached(item: ItemDevolucionUi): String? {
        val cached = cacheTrazabilidadDevolucion[item.itemVentaId]
        if (cached != null && cached.cantidad == item.cantidadADevolver) {
            return cached.resultado
        }
        val resultado = validarTrazabilidadExactaDevolucion(item)
        cacheTrazabilidadDevolucion[item.itemVentaId] = CacheTrazabilidad(item.itemVentaId, item.cantidadADevolver, resultado)
        return resultado
    }

    private fun ocultarDialogoProgresoVentaEnEspera() {
        ocultarLoadingOverlayCaja()
    }

    private fun pausarVentaActual() {
        if (!puedeOperarTurnoActual()) return

        if (ventaEnProceso || actualizandoCaja || operacionVentaEnEsperaEnCurso) {
            mostrarToastSeguro("Espera a que termine la operacion actual")
            return
        }

        if (!turnoAbierto || idTurnoActivo.isBlank()) {
            if (!isHidden) {
                mostrarToastSeguro("Primero debes abrir un turno de caja")
                binding.layoutAperturaCaja.visibility = View.VISIBLE
            }
            return
        }

        if (listaProductosCaja.isEmpty()) {
            mostrarToastSeguro("No hay productos para pausar")
            return
        }

        if (_binding?.layoutComprobante?.visibility == View.VISIBLE) {
            mostrarToastSeguro("Termina o cancela el cobro antes de pausar la venta")
            return
        }

        if (cantidadVentasEnEspera >= MAX_VENTAS_EN_ESPERA) {
            mostrarDialogoLimiteVentasEnEspera()
            return
        }

        solicitarNotaVentaEnEspera { nota ->
            guardarVentaEnEspera(nota)
        }
    }

    private fun solicitarNotaVentaEnEspera(titulo: String = "Pausar venta", mensaje: String = "Agrega una nota r\u00e1pida para identificar mejor esta venta en espera (opcional)", onGuardar: (String) -> Unit) {
        if (estaCajaEnModoSoloVista(notificar = true)) return
        dialogoNotaVentaEnEsperaActual?.dismiss()
        val view = layoutInflater.inflate(R.layout.dialog_nota_venta_espera, null)
        val tvTitulo = view.findViewById<TextView>(R.id.tvTituloNotaVentaEspera)
        val tvMensaje = view.findViewById<TextView>(R.id.tvMensajeNotaVentaEspera)
        val input = view.findViewById<TextInputEditText>(R.id.inputNotaVentaEspera)
        val btnCancelar = view.findViewById<MaterialButton>(R.id.btnCancelarNotaVentaEspera)
        val btnGuardar = view.findViewById<MaterialButton>(R.id.btnGuardarNotaVentaEspera)

        tvTitulo.text = titulo
        tvMensaje.text = mensaje
        configurarOcultarTecladoEnDialogo(view, input)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.white)
        dialog.setOnDismissListener {
            if (dialogoNotaVentaEnEsperaActual === dialog) {
                dialogoNotaVentaEnEsperaActual = null
            }
        }
        btnCancelar.setOnClickListener { dialog.dismiss() }
        btnGuardar.setOnClickListener {
            onGuardar(input.text?.toString()?.trim().orEmpty())
            dialog.dismiss()
        }
        dialogoNotaVentaEnEsperaActual = dialog
        dialog.show()
    }

    private fun guardarVentaEnEspera(notaRapida: String, mostrarMensajeExito: Boolean = true, reiniciarLayout: Boolean = true, onSuccess: (() -> Unit)? = null) {
        if (bloquearOperacionPorRescateCajaBloqueada()) return
        if (cantidadVentasEnEspera >= MAX_VENTAS_EN_ESPERA) {
            mostrarDialogoLimiteVentasEnEspera()
            return
        }
        if (!iniciarOperacionVentaEnEspera()) return

        val idCajaOperativa = obtenerIdCajaOperativa()
        val nombreCajaOperativa = obtenerNombreCajaOperativa()
        val esperaRef = getVentasEnEsperaRef().push()
        val idEspera = esperaRef.key

        if (idEspera.isNullOrBlank()) {
            finalizarOperacionVentaEnEspera()
            mostrarToastSeguro("No se pudo pausar la venta")
            return
        }

        val total = obtenerTotalVenta()
        val productosMap = hashMapOf<String, Any>()
        listaProductosCaja.forEach { producto ->
            productosMap[producto.id] = producto
        }

        val tituloVentaEnEspera = notaRapida.ifBlank { obtenerTituloVentaEnEsperaPorDefecto() }

        val datos = hashMapOf<String, Any>(
            "idEspera" to idEspera,
            "titulo" to tituloVentaEnEspera,
            "notaRapida" to notaRapida,
            "fecha" to obtenerFechaActual(),
            "hora" to obtenerHoraActual(),
            "timestamp" to System.currentTimeMillis(),
            "estado" to "en_espera",
            "idCajaOperativa" to idCajaOperativa,
            "nombreCajaOperativa" to nombreCajaOperativa,
            "idUsuario" to idCajera,
            "nombreUsuario" to nombreCajera,
            "idTurno" to idTurnoActivo,
            "total" to String.format(Locale.US, "%.2f", total),
            "cantidadProductos" to listaProductosCaja.size,
            "productos" to productosMap
        )

        val updates = hashMapOf<String, Any?>(
            "VentasEnEspera/$idCajaOperativa/$idEspera" to datos,
            "CajasIndividuales/$idCajaOperativa/Productos" to null
        )

        database.reference.updateChildren(updates)
            .addOnSuccessListener {
                normalizarTitulosVentasEnEspera(idCajaOperativa) { totalActual ->
                    registrarMovimientoVentaEnEspera(
                        tipo = "venta_en_espera_guardada",
                        titulo = "Venta enviada a espera",
                        descripcion = "Se guardó \"$tituloVentaEnEspera\" en ventas en espera.",
                        referenciaId = idEspera,
                        monto = total,
                        extra = mapOf(
                            "tituloVenta" to tituloVentaEnEspera,
                            "cantidadProductos" to listaProductosCaja.size,
                            "totalVentasEnEspera" to totalActual
                        )
                    )
                    if (mostrarMensajeExito) {
                        mostrarToastSeguro("Venta enviada a espera")
                    }
                    if (reiniciarLayout) {
                        if (esTablet()) {
                            aplicarLayoutInicialTablet()
                        } else {
                            aplicarLayoutInicialMobile()
                        }
                    }
                    if (totalActual >= ALERTA_VENTAS_EN_ESPERA && totalActual < MAX_VENTAS_EN_ESPERA) {
                        mostrarDialogoAdvertenciaVentasEnEspera(totalActual)
                    }
                    finalizarOperacionVentaEnEspera()
                    onSuccess?.invoke()
                }
            }
            .addOnFailureListener { e ->
                finalizarOperacionVentaEnEspera()
                mostrarToastSeguro("No se pudo pausar la venta: ${e.message}")
            }
    }

    private fun construirDatosVentaEnEspera(idEspera: String, notaRapida: String, productos: List<ProductoCaja>): HashMap<String, Any> {
        val idCajaOperativa = obtenerIdCajaOperativa()
        val nombreCajaOperativa = obtenerNombreCajaOperativa()
        val productosMap = hashMapOf<String, Any>()
        productos.forEach { producto ->
            productosMap[producto.id] = producto
        }

        val total = productos.sumOf { producto ->
            val cantidad = producto.cantidad.toDoubleOrNull() ?: 0.0
            val precio = producto.precioUnitario.toDoubleOrNull() ?: 0.0
            cantidad * precio
        }

        val tituloVentaEnEspera = notaRapida.ifBlank { obtenerTituloVentaEnEsperaPorDefecto() }

        return hashMapOf(
            "idEspera" to idEspera,
            "titulo" to tituloVentaEnEspera,
            "notaRapida" to notaRapida,
            "fecha" to obtenerFechaActual(),
            "hora" to obtenerHoraActual(),
            "timestamp" to System.currentTimeMillis(),
            "timestampServidor" to ServerValue.TIMESTAMP,
            "estado" to "en_espera",
            "idCajaOperativa" to idCajaOperativa,
            "nombreCajaOperativa" to nombreCajaOperativa,
            "idUsuario" to idCajera,
            "nombreUsuario" to nombreCajera,
            "idTurno" to idTurnoActivo,
            "total" to String.format(Locale.US, "%.2f", total),
            "cantidadProductos" to productos.size,
            "productos" to productosMap
        )
    }

    private fun obtenerTituloVentaEnEsperaPorDefecto(): String {
        val siguienteNumero = (cantidadVentasEnEspera + 1).coerceAtLeast(1)
        return "Cliente $siguienteNumero"
    }

    private fun registrarMovimientoVentaEnEspera(
        tipo: String,
        titulo: String,
        descripcion: String,
        referenciaId: String,
        monto: Double? = null,
        extra: Map<String, Any?> = emptyMap()
    ) {
        val idCajaOperativa = obtenerIdCajaOperativa()
        val nombreCajaOperativa = obtenerNombreCajaOperativa()

        MovimientoLogger.registrar(
            tipo = tipo,
            modulo = "caja",
            titulo = titulo,
            descripcion = descripcion,
            idUsuario = idCajera,
            nombreUsuario = nombreCajera,
            monto = monto,
            referenciaId = referenciaId,
            idCaja = idCajaOperativa,
            nombreCaja = nombreCajaOperativa,
            extra = extra,
            database = database
        )
    }

    private fun normalizarTitulosVentasEnEspera(
        idCajaOperativa: String,
        onComplete: (Int) -> Unit = {}
    ) {
        val ref = database.reference.child("VentasEnEspera").child(idCajaOperativa)
        ref.get()
            .addOnSuccessListener { snapshot ->
                val updates = hashMapOf<String, Any?>()
                var contadorClientes = 1

                snapshot.children
                    .sortedBy { it.key.orEmpty() }
                    .forEach { child ->
                        val idVenta = child.key.orEmpty()
                        if (idVenta.isBlank()) return@forEach
                        updates["VentasEnEspera/$idCajaOperativa/$idVenta/titulo"] = "Cliente $contadorClientes"
                        contadorClientes++
                    }

                if (updates.isEmpty()) {
                    onComplete(snapshot.childrenCount.toInt())
                    return@addOnSuccessListener
                }

                database.reference.updateChildren(updates)
                    .addOnSuccessListener { onComplete(snapshot.childrenCount.toInt()) }
                    .addOnFailureListener { onComplete(snapshot.childrenCount.toInt()) }
            }
            .addOnFailureListener {
                onComplete(cantidadVentasEnEspera)
            }
    }

    private fun mostrarDialogoAdvertenciaVentasEnEspera(totalVentas: Int) {
        if (!isAdded) return

        mostrarDialogoAvisoVentasEnEspera(
            titulo = "Atencion con las ventas en espera",
            mensaje = "Ya tienes $totalVentas ventas en espera. Lo recomendable es retomarlas o cerrarlas pronto para no perder el orden de caja ni dejar ventas guardadas por demasiado tiempo."
        )
    }

    private fun mostrarDialogoLimiteVentasEnEspera() {
        if (!isAdded) return

        mostrarDialogoAvisoVentasEnEspera(
            titulo = "Limite de ventas en espera",
            mensaje = "Ya llegaste al maximo de $MAX_VENTAS_EN_ESPERA ventas en espera. Recupera o elimina una antes de guardar otra para mantener la caja ordenada."
        )
    }

    private fun mostrarDialogoAvisoVentasEnEspera(titulo: String, mensaje: String) {
        if (!isAdded) return

        val view = layoutInflater.inflate(R.layout.dialog_aviso_ventas_espera, null)
        val tvTitulo = view.findViewById<TextView>(R.id.tvTituloAvisoVentasEspera)
        val tvMensaje = view.findViewById<TextView>(R.id.tvMensajeAvisoVentasEspera)
        val btnEntendido = view.findViewById<MaterialButton>(R.id.btnEntendidoAvisoVentasEspera)

        tvTitulo.text = titulo
        tvMensaje.text = mensaje

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.white)
        btnEntendido.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun tomarControlVentaEnEspera(
        idEspera: String,
        accion: String,
        onTomada: (DatabaseReference) -> Unit
    ) {
        val esperaRef = getVentasEnEsperaRef().child(idEspera)
        val ahora = System.currentTimeMillis()

        esperaRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val rawMap = currentData.value as? Map<*, *> ?: return Transaction.abort()
                val estadoOperacion = rawMap["estadoOperacion"]?.toString().orEmpty()
                val bloqueadoPor = rawMap["bloqueadoPor"]?.toString().orEmpty()
                val timestampBloqueo = when (val raw = rawMap["timestampBloqueo"]) {
                    is Long -> raw
                    is Int -> raw.toLong()
                    is Double -> raw.toLong()
                    is String -> raw.toLongOrNull() ?: 0L
                    else -> 0L
                }

                val bloqueoSigueVigente =
                    estadoOperacion.equals("procesando", ignoreCase = true) &&
                        bloqueadoPor.isNotBlank() &&
                        (ahora - timestampBloqueo) < BLOQUEO_VENTA_EN_ESPERA_MS

                if (bloqueoSigueVigente && bloqueadoPor != idCajera) {
                    return Transaction.abort()
                }

                val nuevoMapa = rawMap.entries.associate { entry ->
                    entry.key.toString() to entry.value
                }.toMutableMap()

                nuevoMapa["estadoOperacion"] = "procesando"
                nuevoMapa["accionOperacion"] = accion
                nuevoMapa["bloqueadoPor"] = idCajera
                nuevoMapa["nombreBloqueadoPor"] = nombreCajera
                nuevoMapa["timestampBloqueo"] = ahora
                currentData.value = nuevoMapa
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    finalizarOperacionVentaEnEspera()
                    mostrarToastSeguro("No se pudo preparar la venta en espera: ${error.message}")
                    return
                }

                if (!committed) {
                    finalizarOperacionVentaEnEspera()
                    mostrarToastSeguro("Esta venta en espera está siendo usada en otro dispositivo")
                    return
                }

                onTomada(esperaRef)
            }
        })
    }

    private fun liberarControlVentaEnEspera(
        esperaRef: DatabaseReference,
        onComplete: (() -> Unit)? = null
    ) {
        val updates = hashMapOf<String, Any?>(
            "estadoOperacion" to null,
            "accionOperacion" to null,
            "bloqueadoPor" to null,
            "nombreBloqueadoPor" to null,
            "timestampBloqueo" to null
        )
        esperaRef.updateChildren(updates).addOnCompleteListener { onComplete?.invoke() }
    }

    private fun mostrarVentasEnEspera() {
        if (ventaEnProceso || actualizandoCaja || operacionVentaEnEsperaEnCurso) {
            mostrarToastSeguro("Espera a que termine la operacion actual")
            return
        }

        // NUEVO: Bloquear UI mientras carga
        mostrarLoadingOverlayCaja("Cargando...", "Buscando ventas en espera")

        getVentasEnEsperaRef()
            .get()
            .addOnSuccessListener { snapshot ->
                ocultarLoadingOverlayCaja() // NUEVO: Liberar UI
                if (!isAdded || _binding == null) return@addOnSuccessListener

                val ventas = snapshot.children.mapNotNull { child ->
                    val id = child.key.orEmpty()
                    if (id.isBlank()) return@mapNotNull null

                    VentaEnEsperaUi(
                        id = id,
                        titulo = child.child("titulo").getValue(String::class.java).orEmpty()
                            .ifBlank { "Venta en espera" },
                        total = child.child("total").getValue(String::class.java)?.toDoubleOrNull() ?: 0.0,
                        cantidadProductos = child.child("cantidadProductos").getValue(Int::class.java)
                            ?: child.child("productos").childrenCount.toInt(),
                        hora = child.child("hora").getValue(String::class.java).orEmpty(),
                        nombreUsuario = child.child("nombreUsuario").getValue(String::class.java).orEmpty(),
                        timestamp = child.child("timestamp").getValue(Long::class.java)
                            ?: child.child("timestamp").value?.toString()?.toLongOrNull()
                            ?: 0L
                    )
                }.sortedBy { it.id }

                if (ventas.isEmpty()) {
                    mostrarToastSeguro("No hay ventas en espera")
                    return@addOnSuccessListener
                }

                mostrarBottomSheetVentasEnEspera(ventas)
            }
            .addOnFailureListener { e ->
                ocultarLoadingOverlayCaja() // NUEVO: Liberar UI
                mostrarToastSeguro("No se pudieron cargar las ventas en espera: ${e.message}")
            }
    }

    private fun mostrarBottomSheetVentasEnEspera(ventas: List<VentaEnEsperaUi>) {
        if (estaCajaEnModoSoloVista(notificar = true)) return
        if (!isAdded) return

        dialogoVentasEnEsperaActual?.dismiss()
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_ventas_en_espera, null)
        dialog.setContentView(view)
        dialog.setOnDismissListener {
            if (dialogoVentasEnEsperaActual === dialog) {
                dialogoVentasEnEsperaActual = null
            }
        }
        dialogoVentasEnEsperaActual = dialog

        val recycler = view.findViewById<RecyclerView>(R.id.rvVentasEnEsperaDialog)
        val tvCantidad = view.findViewById<TextView>(R.id.tvCantidadVentasEnEsperaDialog)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotalVentasEnEsperaDialog)
        val btnCerrar = view.findViewById<MaterialButton>(R.id.btnCerrarVentasEnEsperaDialog)

        val totalPendiente = ventas.sumOf { it.total }

        tvCantidad.text = if (ventas.size == 1) {
            "1 venta guardada"
        } else {
            "${ventas.size} ventas guardadas"
        }
        tvTotal.text = MonedaHelper.formatear(totalPendiente)

        val alturaItem = (124 * resources.displayMetrics.density).toInt()
        val alturaDeseada = ventas.size * alturaItem
        val alturaMaxima = (resources.displayMetrics.heightPixels * 0.42f).toInt()
        val alturaFinal = minOf(alturaDeseada, alturaMaxima)

        recycler.layoutParams = recycler.layoutParams.apply {
            height = alturaFinal
        }
        recycler.isNestedScrollingEnabled = alturaDeseada > alturaMaxima
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = AdapterVentasEnEspera(
            items = ventas,
            onSeleccionar = { venta ->
                dialog.dismiss()
                reanudarVentaEnEspera(venta.id)
            },
            onEliminar = { venta ->
                eliminarVentaEnEspera(venta.id) {
                    dialog.dismiss()
                    mostrarVentasEnEspera()
                }
            }
        )

        btnCerrar.setOnClickListener { dialog.dismiss() }

        dialog.show()
        val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }
    }

    private fun reanudarVentaEnEspera(idEspera: String) {
        if (_binding?.layoutComprobante?.visibility == View.VISIBLE) {
            mostrarToastSeguro("Sal del cobro antes de recuperar una venta")
            return
        }

        obtenerProductosCajaActual(
            onSuccess = { productosActuales ->
                if (productosActuales.isNotEmpty()) {
                    mostrarDialogoOpcionesVentaEnEspera(
                        idEspera = idEspera,
                        productosActuales = productosActuales
                    )
                } else {
                    cargarVentaEnEspera(idEspera)
                }
            },
            onError = {
                mostrarToastSeguro("No se pudo verificar la caja actual")
            }
        )
    }

    private fun mostrarDialogoOpcionesVentaEnEspera(
        idEspera: String,
        productosActuales: List<ProductoCaja>
    ) {
        if (estaCajaEnModoSoloVista(notificar = true)) return
        dialogoOpcionesVentaEnEsperaActual?.dismiss()
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("¿Cómo quieres abrir esta venta?")
            .setMessage("Ya tienes productos en tu caja actual. Puedes reemplazar tu venta actual o fusionarla con la venta guardada.")
            .setNegativeButton("Cancelar", null)
            .setNeutralButton("Fusionar con mi caja") { _, _ ->
                fusionarVentaEnEsperaConCajaActual(
                    idEspera = idEspera,
                    productosActuales = productosActuales
                )
            }
            .setPositiveButton("Abrir reemplazando") { _, _ ->
                solicitarNotaVentaEnEspera(
                    titulo = "Cambiar a otra venta",
                    mensaje = "Estas eligiendo una venta guardada, pero en tu caja actual ya tienes productos agregados. Escribe una nota para recordar mejor esa venta cuando vuelva a quedar en espera."
                ) { nota ->
                    intercambiarVentaActualConVentaEnEspera(
                        idEspera = idEspera,
                        notaRapida = nota,
                        productosActuales = productosActuales
                    )
                }
            }
            .create()
        dialog.setOnDismissListener {
            if (dialogoOpcionesVentaEnEsperaActual === dialog) {
                dialogoOpcionesVentaEnEsperaActual = null
            }
        }
        dialogoOpcionesVentaEnEsperaActual = dialog
        dialog.show()
    }

    private fun eliminarVentaEnEspera(idEspera: String, onSuccess: () -> Unit) {
        if (!iniciarOperacionVentaEnEspera()) return
        val idCajaOperativa = obtenerIdCajaOperativa()
        tomarControlVentaEnEspera(idEspera, "eliminar") { esperaRef ->
            esperaRef.removeValue()
                .addOnSuccessListener {
                    normalizarTitulosVentasEnEspera(idCajaOperativa) {
                        registrarMovimientoVentaEnEspera(
                            tipo = "venta_en_espera_eliminada",
                            titulo = "Venta en espera eliminada",
                            descripcion = "Se elimino una venta guardada de la lista de espera.",
                            referenciaId = idEspera
                        )
                        finalizarOperacionVentaEnEspera()
                        mostrarToastSeguro("Venta en espera eliminada")
                        onSuccess()
                    }
                }
                .addOnFailureListener { e ->
                    liberarControlVentaEnEspera(esperaRef) {
                        finalizarOperacionVentaEnEspera()
                        mostrarToastSeguro("No se pudo eliminar la venta en espera: ${e.message}")
                    }
                }
        }
    }

    private fun intercambiarVentaActualConVentaEnEspera(
        idEspera: String,
        notaRapida: String,
        productosActuales: List<ProductoCaja>
    ) {
        if (!iniciarOperacionVentaEnEspera(
                titulo = "Cambiando de venta",
                mensaje = "Tu venta actual pasara a espera y se abrira la venta que elegiste."
            )
        ) return
        val idCajaOperativa = obtenerIdCajaOperativa()
        tomarControlVentaEnEspera(idEspera, "intercambiar") { esperaRef ->
            esperaRef.get()
                .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    finalizarOperacionVentaEnEspera()
                    mostrarToastSeguro("La venta en espera ya no está disponible")
                    return@addOnSuccessListener
                }

                val productosSnapshot = snapshot.child("productos")
                if (!productosSnapshot.exists()) {
                    finalizarOperacionVentaEnEspera()
                    mostrarToastSeguro("La venta en espera no tiene productos")
                    esperaRef.removeValue()
                    return@addOnSuccessListener
                }

                val idTurnoVenta = snapshot.child("idTurno").getValue(String::class.java).orEmpty()
                if (idTurnoVenta.isNotBlank() && idTurnoVenta != idTurnoActivo) {
                    finalizarOperacionVentaEnEspera()
                    mostrarToastSeguro("Esta venta en espera pertenece a otro turno y no puede recuperarse aquí")
                    return@addOnSuccessListener
                }

                val productosSeleccionadosMap = hashMapOf<String, Any>()
                for (productoSnap in productosSnapshot.children) {
                    val producto = productoSnap.getValue(ProductoCaja::class.java) ?: continue
                    productosSeleccionadosMap[producto.id] = producto
                }

                if (productosSeleccionadosMap.isEmpty()) {
                    finalizarOperacionVentaEnEspera()
                    mostrarToastSeguro("La venta en espera no tiene productos validos")
                    esperaRef.removeValue()
                    return@addOnSuccessListener
                }

                val nuevoIdEspera = getVentasEnEsperaRef().push().key
                if (nuevoIdEspera.isNullOrBlank()) {
                    finalizarOperacionVentaEnEspera()
                    mostrarToastSeguro("No se pudo guardar la venta actual en espera")
                    return@addOnSuccessListener
                }

                val ventaActualEnEspera = construirDatosVentaEnEspera(
                    idEspera = nuevoIdEspera,
                    notaRapida = notaRapida,
                    productos = productosActuales
                )

                val updates = hashMapOf<String, Any?>(
                    "CajasIndividuales/$idCajaOperativa/Productos" to productosSeleccionadosMap,
                    "VentasEnEspera/$idCajaOperativa/$idEspera" to null,
                    "VentasEnEspera/$idCajaOperativa/$nuevoIdEspera" to ventaActualEnEspera
                )

                database.reference
                    .updateChildren(updates)
                    .addOnSuccessListener {
                        normalizarTitulosVentasEnEspera(idCajaOperativa) {
                            registrarMovimientoVentaEnEspera(
                                tipo = "venta_en_espera_intercambiada",
                                titulo = "Venta intercambiada",
                                descripcion = "Se abrió una venta guardada y la venta actual pasó a espera.",
                                referenciaId = idEspera,
                                extra = mapOf(
                                    "nuevaVentaEnEsperaId" to nuevoIdEspera,
                                    "cantidadProductosActuales" to productosActuales.size
                                )
                            )
                            if (esTablet()) {
                                aplicarLayoutInicialTablet()
                            } else {
                                aplicarLayoutInicialMobile()
                            }
                            finalizarOperacionVentaEnEspera()
                        }
                    }
                    .addOnFailureListener { e ->
                        liberarControlVentaEnEspera(esperaRef) {
                            finalizarOperacionVentaEnEspera()
                            mostrarToastSeguro("No se pudo cambiar de venta: ${e.message}")
                        }
                    }
            }
            .addOnFailureListener { e ->
                liberarControlVentaEnEspera(esperaRef) {
                    finalizarOperacionVentaEnEspera()
                    mostrarToastSeguro("No se pudo leer la venta en espera: ${e.message}")
                }
            }
        }
    }

    private fun fusionarVentaEnEsperaConCajaActual(
        idEspera: String,
        productosActuales: List<ProductoCaja>
    ) {
        if (!iniciarOperacionVentaEnEspera(
                titulo = "Fusionando ventas",
                mensaje = "Estamos uniendo tu caja actual con la venta guardada y validando el stock."
            )
        ) return

        tomarControlVentaEnEspera(idEspera, "fusionar") { esperaRef ->
            esperaRef.get()
                .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    finalizarOperacionVentaEnEspera()
                    mostrarToastSeguro("La venta en espera ya no esta disponible")
                    return@addOnSuccessListener
                }

                val productosSnapshot = snapshot.child("productos")
                if (!productosSnapshot.exists()) {
                    finalizarOperacionVentaEnEspera()
                    mostrarToastSeguro("La venta en espera no tiene productos")
                    esperaRef.removeValue()
                    return@addOnSuccessListener
                }

                val idTurnoVenta = snapshot.child("idTurno").getValue(String::class.java).orEmpty()
                if (idTurnoVenta.isNotBlank() && idTurnoVenta != idTurnoActivo) {
                    finalizarOperacionVentaEnEspera()
                    mostrarToastSeguro("Esta venta en espera pertenece a otro turno y no puede fusionarse aqui")
                    return@addOnSuccessListener
                }

                val productosEspera = mutableListOf<ProductoCaja>()
                for (productoSnap in productosSnapshot.children) {
                    val producto = productoSnap.getValue(ProductoCaja::class.java) ?: continue
                    productosEspera.add(producto)
                }

                if (productosEspera.isEmpty()) {
                    finalizarOperacionVentaEnEspera()
                    mostrarToastSeguro("La venta en espera no tiene productos validos")
                    esperaRef.removeValue()
                    return@addOnSuccessListener
                }

                val productosFusionados = fusionarProductosCaja(productosActuales, productosEspera)
                validarFusionVentaEnEspera(productosFusionados) { valido, mensaje ->
                    if (!valido) {
                        finalizarOperacionVentaEnEspera()
                        mostrarToastSeguro(mensaje.ifBlank { "No se pudo fusionar por stock insuficiente" })
                        return@validarFusionVentaEnEspera
                    }

                    val idCajaOperativa = obtenerIdCajaOperativa()
                    val productosFusionadosMap = hashMapOf<String, Any>()
                    productosFusionados.forEach { producto ->
                        productosFusionadosMap[producto.id] = producto
                    }

                    val updates = hashMapOf<String, Any?>(
                        "CajasIndividuales/$idCajaOperativa/Productos" to productosFusionadosMap,
                        "VentasEnEspera/$idCajaOperativa/$idEspera" to null
                    )

                    database.reference
                        .updateChildren(updates)
                        .addOnSuccessListener {
                            registrarMovimientoVentaEnEspera(
                                tipo = "venta_en_espera_fusionada",
                                titulo = "Venta fusionada",
                                descripcion = "Se fusiono una venta guardada con la caja actual.",
                                referenciaId = idEspera,
                                monto = snapshot.child("total").getValue(String::class.java)?.toDoubleOrNull(),
                                extra = mapOf(
                                    "cantidadProductosFusionados" to productosFusionados.size,
                                    "productosCajaActual" to productosActuales.size,
                                    "productosVentaEnEspera" to productosEspera.size
                                )
                            )
                            mostrarToastSeguro("Venta fusionada con tu caja actual")
                            finalizarOperacionVentaEnEspera()
                            if (esTablet()) {
                                aplicarLayoutInicialTablet()
                            } else {
                                aplicarLayoutInicialMobile()
                            }
                        }
                        .addOnFailureListener { e ->
                            liberarControlVentaEnEspera(esperaRef) {
                                finalizarOperacionVentaEnEspera()
                                mostrarToastSeguro("No se pudo fusionar la venta: ${e.message}")
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                liberarControlVentaEnEspera(esperaRef) {
                    finalizarOperacionVentaEnEspera()
                    mostrarToastSeguro("No se pudo leer la venta en espera: ${e.message}")
                }
            }
        }
    }

    private fun fusionarProductosCaja(
        productosActuales: List<ProductoCaja>,
        productosEspera: List<ProductoCaja>
    ): List<ProductoCaja> {
        val mapaFusion = linkedMapOf<String, ProductoCaja>()

        fun agregarProducto(producto: ProductoCaja) {
            val existente = mapaFusion[producto.id]
            if (existente == null) {
                mapaFusion[producto.id] = producto.copy()
                return
            }

            val cantidadExistente = existente.cantidad.toIntSeguro(0)
            val cantidadNueva = producto.cantidad.toIntSeguro(0)
            val precio = existente.precioUnitario.toDoubleSeguro(0.0)
            val cantidadFinal = cantidadExistente + cantidadNueva

            mapaFusion[producto.id] = existente.copy(
                cantidad = cantidadFinal.toString(),
                total = String.format(Locale.US, "%.2f", cantidadFinal.toDouble() * (precio ?: 0.0))
            )
        }

        productosActuales.forEach(::agregarProducto)
        productosEspera.forEach(::agregarProducto)
        return mapaFusion.values.toList()
    }

    private fun validarFusionVentaEnEspera(
        productosFusionados: List<ProductoCaja>,
        onResult: (Boolean, String) -> Unit
    ) {
        val requeridasPorIndice = productosFusionados
            .groupBy { it.indiceProducto.trim() }
            .mapValues { (_, items) ->
                items.sumOf { item ->
                    val cantidad = item.cantidad.toIntSeguro(0)
                    val unidades = item.unidadesPorPresentacion.toIntSeguro(1)
                    cantidad * unidades
                }
            }

        database.getReference("Inventario")
            .child("Productos")
            .get()
            .addOnSuccessListener { snapshot ->
                val productoInsuficiente = requeridasPorIndice.entries.firstOrNull { (indice, requeridas) ->
                    val stock = snapshot.child(indice).child("cantidadinicial").value?.toString().toIntSeguro(0)
                    stock < requeridas
                }

                if (productoInsuficiente != null) {
                    val indice = productoInsuficiente.key
                    val requeridas = productoInsuficiente.value
                    val stock = snapshot.child(indice).child("cantidadinicial").value?.toString().toIntSeguro(0)
                    val nombre = productosFusionados.firstOrNull { it.indiceProducto.trim() == indice }?.nombre ?: "este producto"
                    onResult(false, "No se puede fusionar $nombre. Se necesitan $requeridas unidades y solo hay $stock disponibles.")
                } else {
                    onResult(true, "")
                }
            }
            .addOnFailureListener { e ->
                onResult(false, "No se pudo validar el stock para fusionar: ${e.message}")
            }
    }

    private fun cargarVentaEnEspera(idEspera: String) {
        if (!iniciarOperacionVentaEnEspera()) return
        tomarControlVentaEnEspera(idEspera, "recuperar") { esperaRef ->
            esperaRef.get()
                .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    finalizarOperacionVentaEnEspera()
                    mostrarToastSeguro("La venta en espera ya no esta disponible")
                    return@addOnSuccessListener
                }

                val productosSnapshot = snapshot.child("productos")
                if (!productosSnapshot.exists()) {
                    finalizarOperacionVentaEnEspera()
                    mostrarToastSeguro("La venta en espera no tiene productos")
                    esperaRef.removeValue()
                    return@addOnSuccessListener
                }

                val idTurnoVenta = snapshot.child("idTurno").getValue(String::class.java).orEmpty()
                if (idTurnoVenta.isNotBlank() && idTurnoVenta != idTurnoActivo) {
                    finalizarOperacionVentaEnEspera()
                    mostrarToastSeguro("Esta venta en espera pertenece a otro turno y no puede recuperarse aqui")
                    return@addOnSuccessListener
                }

                val productosMap = hashMapOf<String, Any>()
                for (productoSnap in productosSnapshot.children) {
                    val producto = productoSnap.getValue(ProductoCaja::class.java) ?: continue
                    productosMap[producto.id] = producto
                }

                if (productosMap.isEmpty()) {
                    finalizarOperacionVentaEnEspera()
                    mostrarToastSeguro("La venta en espera no tiene productos validos")
                    esperaRef.removeValue()
                    return@addOnSuccessListener
                }

                val idCajaOperativa = obtenerIdCajaOperativa()
                val updates = hashMapOf<String, Any?>(
                    "CajasIndividuales/$idCajaOperativa/Productos" to productosMap,
                    "VentasEnEspera/$idCajaOperativa/$idEspera" to null
                )

                database.reference
                    .updateChildren(updates)
                    .addOnSuccessListener {
                    registrarMovimientoVentaEnEspera(
                        tipo = "venta_en_espera_recuperada",
                        titulo = "Venta recuperada desde espera",
                        descripcion = "Se cargo una venta guardada nuevamente en la caja.",
                        referenciaId = idEspera,
                        monto = snapshot.child("total").getValue(String::class.java)?.toDoubleOrNull(),
                        extra = mapOf(
                            "cantidadProductos" to productosMap.size
                        )
                    )
                    val mensaje = if (listaProductosCaja.isNotEmpty()) {
                        "Tu venta actual se guardo en espera y se abrio la seleccionada"
                    } else {
                        "Venta cargada desde espera"
                    }
                    mostrarToastSeguro(mensaje)
                        finalizarOperacionVentaEnEspera()
                        if (esTablet()) {
                            aplicarLayoutInicialTablet()
                        } else {
                            aplicarLayoutInicialMobile()
                        }
                    }
                    .addOnFailureListener { e ->
                        liberarControlVentaEnEspera(esperaRef) {
                            finalizarOperacionVentaEnEspera()
                            mostrarToastSeguro("No se pudo recuperar la venta: ${e.message}")
                        }
                    }
            }
            .addOnFailureListener { e ->
                liberarControlVentaEnEspera(esperaRef) {
                    finalizarOperacionVentaEnEspera()
                    mostrarToastSeguro("No se pudo leer la venta en espera: ${e.message}")
                }
            }
        }
    }

    private fun getCorteCajaFechaRef(fecha: String = fechaTurnoActivo.ifBlank { obtenerFechaActual() }): DatabaseReference {
        return getCorteCajaCajeraRef()
            .child(fecha)
    }

    private fun getCorteCajaCajeraRef(): DatabaseReference {
        val idCajaOperativa = obtenerIdCajaOperativa()
        return database.reference
            .child("CorteCaja")
            .child("Cajeras")
            .child(idCajaOperativa)
    }

    private fun getTurnosRef(): DatabaseReference {
        return getCorteCajaFechaRef().child("turnos")
    }

    private fun getTurnoActivoRef(): DatabaseReference? {
        if (idTurnoActivo.isBlank()) return null
        return getTurnosRef().child(idTurnoActivo)
    }

    private fun activarResguardoDesconexionTurno() {
        val turnoRef = getTurnoActivoRef() ?: return
        val llaveActual = "$fechaTurnoActivo|$idTurnoActivo"
        if (llaveActual.isBlank() || llaveResguardoTurnoActiva == llaveActual) return

        llaveResguardoTurnoActiva = llaveActual
        turnoRef.child("sesionActiva").onDisconnect().setValue(false)
        turnoRef.child("desconexionInesperada").onDisconnect().setValue(true)
        turnoRef.child("timestampDesconexion").onDisconnect().setValue(ServerValue.TIMESTAMP)

        turnoRef.child("sesionActiva").setValue(true)
        turnoRef.child("desconexionInesperada").setValue(false)
        turnoRef.child("ultimaConexion").setValue(ServerValue.TIMESTAMP)
    }

    private fun cancelarResguardoDesconexionTurno() {
        val turnoRef = getTurnoActivoRef() ?: return
        turnoRef.child("sesionActiva").onDisconnect().cancel()
        turnoRef.child("desconexionInesperada").onDisconnect().cancel()
        turnoRef.child("timestampDesconexion").onDisconnect().cancel()
        llaveResguardoTurnoActiva = ""
    }

    private fun obtenerTurnoAbiertoDesdeFecha(snapshotFecha: DataSnapshot): TurnoDetectado? {
        val fecha = snapshotFecha.key.orEmpty()
        if (fecha.isBlank()) return null

        val idTurno = snapshotFecha.child("turnoActivoId").getValue(String::class.java).orEmpty()
        if (idTurno.isBlank()) return null

        val turnoSnapshot = snapshotFecha.child("turnos").child(idTurno)
        val estado = turnoSnapshot.child("estado").getValue(String::class.java).orEmpty()
        if (!estado.equals("abierta", ignoreCase = true)) return null

        val montoApertura = turnoSnapshot.child("montoApertura")
            .getValue(String::class.java)
            ?.toDoubleOrNull() ?: 0.0

        return TurnoDetectado(
            fecha = fecha,
            idTurno = idTurno,
            montoApertura = montoApertura
        )
    }

    private fun obtenerTurnoAbiertoDesdePunteroGlobal(snapshotCaja: DataSnapshot): TurnoDetectado? {
        val fecha = snapshotCaja.child("turnoActivoGlobal/fecha").getValue(String::class.java).orEmpty()
        val idTurno = snapshotCaja.child("turnoActivoGlobal/idTurno").getValue(String::class.java).orEmpty()
        if (fecha.isBlank() || idTurno.isBlank()) return null

        val turnoSnapshot = snapshotCaja.child(fecha).child("turnos").child(idTurno)
        val estado = turnoSnapshot.child("estado").getValue(String::class.java).orEmpty()
        if (!estado.equals("abierta", ignoreCase = true)) return null

        val montoApertura = turnoSnapshot.child("montoApertura")
            .getValue(String::class.java)
            ?.toDoubleOrNull() ?: 0.0

        return TurnoDetectado(
            fecha = fecha,
            idTurno = idTurno,
            montoApertura = montoApertura
        )
    }

    private fun obtenerTurnosAbiertosAnteriores(
        snapshotCaja: DataSnapshot,
        fechaActual: String
    ): List<TurnoDetectado> {
        return snapshotCaja.children
            .filter {
                val key = it.key.orEmpty()
                key.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) && key < fechaActual
            }
            .sortedBy { it.key.orEmpty() }
            .mapNotNull { obtenerTurnoAbiertoDesdeFecha(it) }
    }

    private fun confirmarMontoInusualmenteAlto(
        titulo: String,
        mensaje: String,
        onConfirmado: () -> Unit
    ) {
        if (!isAdded) return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(titulo)
            .setMessage(mensaje)
            .setCancelable(false)
            .setNegativeButton("Revisar", null)
            .setPositiveButton("Continuar") { _, _ ->
                // Post para ejecutar después de que el diálogo se cierre y la UI quede estable.
                Handler(Looper.getMainLooper()).post { onConfirmado() }
            }
            .show()
    }

    private fun mostrarAvisoTurnoPendiente(fechaTurno: String) {
        if (!isAdded || fechaTurnoPendienteAvisada == fechaTurno) return
        fechaTurnoPendienteAvisada = fechaTurno
        formatearFechaTurnoPendienteCorta(fechaTurno)

        val dialogView = layoutInflater.inflate(R.layout.dialog_turno_pendiente_caja, null)
        val tvTitulo = dialogView.findViewById<TextView>(R.id.tvTituloTurnoPendiente)
        val tvSubtitulo = dialogView.findViewById<TextView>(R.id.tvSubtituloTurnoPendiente)
        val tvMensaje = dialogView.findViewById<TextView>(R.id.tvMensajeTurnoPendiente)
        val tvDetalle = dialogView.findViewById<TextView>(R.id.tvDetalleTurnoPendiente)

        if (cantidadTurnosPendientes > 1) {
            tvTitulo?.text = "Turnos pendientes detectados"
            tvSubtitulo?.text = "Debes cerrarlos uno por uno antes de seguir"
            tvMensaje?.text =
                "Se detectaron $cantidadTurnosPendientes turnos pendientes. Empezaremos por el día $fechaTurno para que cada cierre genere su reporte por separado."
            tvDetalle?.text =
                "Mientras existan turnos pendientes, la caja quedará bloqueada para ventas, egresos y nuevas operaciones."
        } else {
            tvTitulo?.text = "Turno pendiente detectado"
            tvSubtitulo?.text = "Debes cerrarlo antes de seguir operando"
            tvMensaje?.text =
                "Esta caja tiene un turno abierto del día $fechaTurno. Primero debes cerrar ese turno para recién continuar con la operación de caja."
            tvDetalle?.text =
                "El cierre se hará por separado para mantener ordenado el reporte de cada día."
        }

        tvMensaje?.text = if (cantidadTurnosPendientes > 1) {
            "Se detectaron $cantidadTurnosPendientes turnos pendientes. Empezaremos por el día ${formatearFechaTurnoPendienteCorta(fechaTurno)} para que cada cierre genere su reporte por separado."
        } else {
            "Esta caja tiene un turno abierto del día ${formatearFechaTurnoPendienteCorta(fechaTurno)}. Primero debes cerrar ese turno para recién continuar con la operación de caja."
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCanceledOnTouchOutside(false)
        dialogView.findViewById<MaterialButton>(R.id.btnEntendidoTurnoPendiente)?.setOnClickListener {
            dialog.dismiss()
            intentarMostrarCierreCaja()
        }
        dialog.show()
    }

    private fun formatearFechaTurnoPendienteCorta(fechaTurno: String): String {
        val texto = fechaTurno.trim()
        if (texto.isBlank()) return texto

        val partes = texto.split("-", "/")
        if (partes.size < 2) return texto

        return if (partes.firstOrNull()?.length == 4 && partes.size >= 3) {
            val mes = partes[1].padStart(2, '0')
            val dia = partes[2].padStart(2, '0')
            "$dia/$mes"
        } else {
            val dia = partes[0].padStart(2, '0')
            val mes = partes[1].padStart(2, '0')
            "$dia/$mes"
        }
    }

    private fun puedeOperarTurnoActual(): Boolean {
        if (bloquearOperacionPorRescateCajaBloqueada()) {
            return false
        }

        // CANDADO HORARIO: Evita que agreguen/quiten productos si modifican el reloj
        if (!verificarHoraAntesDeOperar("modificar el carrito de compras")) {
            return false
        }
        if (!CajaTurnoRules.puedeOperarTurnoActual(
                verificacionTurnoDisponible = verificacionTurnoDisponible,
                turnoPendienteDiaAnterior = turnoPendienteDiaAnterior
            )
        ) {
            if (!verificacionTurnoDisponible) {
                mostrarToastSeguro("No se pudo verificar el estado del turno. Revisa tu conexión e inténtalo de nuevo.")
                return false
            }
            mostrarAvisoTurnoPendiente(fechaTurnoActivo)
            intentarMostrarCierreCaja()
            return false
        }

        return true
    }

    private fun solicitarVerificacionTurno(delayMs: Long = 0L) {
        if (cerrandoTurno) {
            verificacionTurnoPendiente = true
            return
        }
        runnableVerificarTurno?.let { handlerSeguridadOperaciones.removeCallbacks(it) }
        val runnable = Runnable { verificarTurnoActivo() }
        runnableVerificarTurno = runnable
        if (delayMs > 0L) {
            handlerSeguridadOperaciones.postDelayed(runnable, delayMs)
        } else {
            handlerSeguridadOperaciones.post(runnable)
        }
    }

    private fun finalizarVerificacionTurno(exitoso: Boolean = true) {
        verificacionTurnoEnCurso = false
        if (verificacionTurnoPendiente) {
            verificacionTurnoPendiente = false
            solicitarVerificacionTurno(DEBOUNCE_VERIFICACION_TURNO_MS)
        }
    }

    private fun prepararVerificacionTurnoSinOverlay(mostrarEstadoVerificando: Boolean = true) {
        _binding ?: return
        if (!mostrarEstadoVerificando) return

        verificacionTurnoDisponible = false
        aplicarCoordinacionVisual(CajaOverlayCoordinator.coordinarPreparacionVerificacion())
        val overlayUi = CajaOverlayUiBuilder.construirOverlayParaPrepararVerificacion(esTablet())
        aplicarConfiguracionOverlayApertura(overlayUi)
        val toolbar = _binding?.toolbar
        val estadoTablet = _binding?.root?.findViewById<TextView>(R.id.tvEstadoCajaTablet)
        val estadoUi = CajaEstadoUiBuilder.construirEstadoUi(
            usuarioPuedeCambiarCaja = usuarioPuedeCambiarCaja(),
            verificacionTurnoDisponible = verificacionTurnoDisponible,
            verificacionTurnoEnCurso = true,
            turnoAbierto = turnoAbierto,
            turnoPendienteDiaAnterior = turnoPendienteDiaAnterior,
            fechaTurnoActivo = fechaTurnoActivo
        )
        toolbar?.subtitle = estadoUi.textoEstado
        toolbar?.setSubtitleTextColor(Color.parseColor(estadoUi.colorEstadoHex))
        estadoTablet?.text = estadoUi.textoEstado
        estadoTablet?.setTextColor(Color.parseColor(estadoUi.colorEstadoHex))
    }

    private fun aplicarConfiguracionOverlayApertura(config: CajaOverlayUiBuilder.CajaOverlayUiModel) {
        val b = _binding ?: return
        obtenerTituloApertura()?.text = config.titulo

        // #14: Actualizamos el mensaje de ayuda
        if (esTablet()) {
            b.root.findViewById<TextView>(R.id.tvMensajeAperturaTablet)?.text = config.mensaje
        } else {
            b.root.findViewById<TextView>(R.id.tvMensajeAperturaMovil)?.text = config.mensaje
        }

        obtenerLayoutMontoApertura()?.visibility = if (config.mostrarInputMonto) View.VISIBLE else View.GONE
        obtenerBotonConfirmarApertura()?.apply {
            text = config.textoBoton
            isEnabled = true
            alpha = 1f
        }

        val etMonto = obtenerInputMontoApertura()
        etMonto?.setText(config.montoPrellenado.orEmpty())

        // #15: Lógica de Sugerencia Pro (Monto de cierre anterior)
        configurarChipSugerenciaApertura(config.montoSugerido)
    }

    private fun configurarChipSugerenciaApertura(monto: Double?) {
        val b = _binding ?: return
        val cardSugerencia = if (esTablet()) {
            b.root.findViewById<View>(R.id.cardSugerenciaAperturaTablet)
        } else {
            b.root.findViewById<View>(R.id.cardSugerenciaAperturaMovil)
        }

        val tvSugerencia = if (esTablet()) {
            b.root.findViewById<TextView>(R.id.tvSugerenciaAperturaTablet)
        } else {
            b.root.findViewById<TextView>(R.id.tvSugerenciaAperturaMovil)
        }

        if (monto != null && monto > 0.009) {
            val montoFormateado = MonedaHelper.formatear(monto)
            tvSugerencia?.text = "¿Usar saldo anterior? ($montoFormateado)"
            cardSugerencia?.showElegantemente()
            cardSugerencia?.setOnClickListener {
                val etMonto = obtenerInputMontoApertura()
                val valorSimple = String.format(Locale.US, "%.2f", monto)
                etMonto?.setText(valorSimple)
                etMonto?.setSelection(valorSimple.length)
                // Efecto visual de confirmación
                cardSugerencia.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                    cardSugerencia.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }.start()
            }
        } else {
            cardSugerencia?.hideElegantemente()
        }
    }

    private fun aplicarCoordinacionVisual(
        scene: CajaOverlayCoordinator.CajaPanelScene
    ) {
        if (isHidden) return

        scene.mostrarApertura?.let { mostrar ->
            val b = _binding ?: return@let
            if (mostrar) {
                if (b.layoutAperturaCaja.visibility != View.VISIBLE) {
                    b.layoutAperturaCaja.showElegantemente()
                    toggleEfectoVidrioFondo(true)

                    // Ocultar buscador en ambos dispositivos
                    if (esTablet()) b.buscadorLayout?.visibility = View.GONE else b.layoutBuscadorInline?.visibility = View.GONE

                    val card = if (esTablet()) b.root.findViewById<View>(R.id.cardContenedorAperturaTablet)
                    else b.root.findViewById<View>(R.id.cardContenedorAperturaMovil)

                    card?.let {
                        it.scaleX = 0.88f
                        it.scaleY = 0.88f
                        it.alpha = 0f
                        it.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .setDuration(500)
                            .setInterpolator(android.view.animation.OvershootInterpolator(1.2f))
                            .start()
                    }
                }
            } else {
                if (b.layoutAperturaCaja.visibility == View.VISIBLE) {
                    b.layoutAperturaCaja.hideElegantemente()
                    toggleEfectoVidrioFondo(false)

                    b.appBarLayout?.visibility = View.VISIBLE
                    if (esTablet()) b.buscadorLayout?.visibility = View.VISIBLE else b.layoutBuscadorInline?.visibility = View.VISIBLE
                    b.cardResumenCaja.visibility = View.VISIBLE
                }
            }
        }

        scene.nuevoCierreForzadoPendienteActivo?.let { nuevoValor ->
            cierreForzadoPendienteActivo = nuevoValor
            actualizarModoCierreForzado()
        }

        scene.mostrarCierre?.let { mostrar ->
            val b = _binding ?: return@let
            val layoutCierre = obtenerLayoutCierreActual()
            if (mostrar) {
                layoutCierre?.showElegantemente()
                toggleEfectoVidrioFondo(true)

                // Ocultar buscador
                if (esTablet()) b.buscadorLayout?.visibility = View.GONE else b.layoutBuscadorInline?.visibility = View.GONE
            } else {
                layoutCierre?.hideElegantemente()
                toggleEfectoVidrioFondo(false)

                if (turnoAbierto) {
                    b.appBarLayout?.visibility = View.VISIBLE
                    if (esTablet()) b.buscadorLayout?.visibility = View.VISIBLE else b.layoutBuscadorInline?.visibility = View.VISIBLE
                    b.cardResumenCaja.visibility = View.VISIBLE
                }
            }
        }

        if (scene.limpiarObservacionCierre) {
            obtenerLayoutObservacionCierre()?.apply {
                visibility = View.GONE
                error = null
            }
            obtenerInputObservacionCierre()?.setText("")
        }
    }

    private fun toggleEfectoVidrioFondo(activar: Boolean) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) return
        val b = _binding ?: return
        val effect = if (activar) {
            android.graphics.RenderEffect.createBlurEffect(18f, 18f, android.graphics.Shader.TileMode.CLAMP)
        } else {
            null
        }

        val root = b.root as? ViewGroup ?: return
        for (i in 0 until root.childCount) {
            val child = root.getChildAt(i)
            val id = child.id
            // Excluimos los overlays para que se mantengan nítidos sobre el fondo borroso
            if (id != R.id.layoutAperturaCaja &&
                id != R.id.layoutCierreCajaMovil &&
                id != R.id.layoutCierreCajaTablet &&
                id != R.id.layoutLoadingOverlayCaja &&
                id != R.id.layoutVentaExito &&
                id != R.id.layoutBloqueoCobro &&
                id != R.id.overlayControlAsistidoCaja &&
                id != R.id.layoutSkeletonCajaInicial &&
                id != R.id.scrimBusqueda
            ) {
                child.setRenderEffect(effect)
            }
        }
    }


    private fun aplicarResultadoVerificacionTurno(
        resultado: CajaVerificacionTurnoMapper.CajaVerificacionTurnoModel
    ) {
        fechaTurnoActivo = resultado.fechaTurnoActivo
        idTurnoActivo = resultado.idTurnoActivo
        montoAperturaTurno = resultado.montoAperturaTurno
        turnoAbierto = resultado.turnoAbierto
        verificacionTurnoDisponible = resultado.verificacionTurnoDisponible
        cantidadTurnosPendientes = resultado.cantidadTurnosPendientes
        turnoPendienteDiaAnterior = resultado.turnoPendienteDiaAnterior
    }

    private fun aplicarResultadoConTurno(
        resultado: CajaVerificacionTurnoMapper.CajaVerificacionTurnoModel
    ) {
        val b = _binding ?: return
        activarResguardoDesconexionTurno()
        escucharTotalTurnoEnTiempoReal() // #9: contador en vivo
        b.layoutAperturaCaja.hideElegantemente()

        // Mostramos el buscador solo cuando ya hay un turno abierto
        if (esTablet()) {
            b.buscadorLayout?.visibility = View.VISIBLE
        } else {
            b.layoutBuscadorInline?.visibility = View.VISIBLE
        }

        reconciliarPreRegistrosPendientesSiCorresponde()

        if (CajaVerificacionUiRules.debeMostrarAvisoTurnoPendiente(resultado)) {
            mostrarAvisoTurnoPendiente(resultado.fechaAvisoTurnoPendiente.orEmpty())
        } else if (CajaVerificacionUiRules.debeLimpiarAvisoPendiente(resultado)) {
            fechaTurnoPendienteAvisada = ""
        }

        limpiarResiduoCajaDeTurnoAnteriorSiCorresponde()
    }

    private fun aplicarResultadoSinTurno(
        resultado: CajaVerificacionTurnoMapper.CajaVerificacionTurnoModel
    ) {
        val b = _binding ?: return
        if (isHidden) return
        
        // Hacemos que el buscador desaparezca si no hay turno abierto
        if (esTablet()) {
            b.buscadorLayout?.visibility = View.GONE
        } else {
            b.layoutBuscadorInline?.visibility = View.GONE
        }

        if (estaEnModoRescateCajaBloqueada()) {
            ocultarLayoutCierreCajaInterno()
            volverAMiCajaDesdeSupervision("El turno rescatado ya no esta abierto. Volviste a tu caja.")
            return
        }
        fechaTurnoPendienteAvisada = ""
        claveReconciliacionPreRegistro = ""
        llaveResguardoTurnoActiva = ""
        if (CajaVerificacionUiRules.debeMostrarOverlayApertura(resultado)) {
            val overlayUi = CajaOverlayUiBuilder.construirOverlayParaResultadoSinTurno(
                esTablet = esTablet(),
                resultado = resultado
            )
            _binding?.layoutAperturaCaja?.showElegantemente()
            aplicarConfiguracionOverlayApertura(overlayUi)
        }
        ocultarLayoutCierreCajaInterno()
    }

    private fun procesarVerificacionTurnoExitosa(resultado: CajaVerificacionTurnoMapper.CajaVerificacionTurnoModel) {
        ocultarDialogoErrorHoraServidor()
        _binding?.bannerDesincronizacionTiempo?.visibility = View.GONE

        aplicarResultadoVerificacionTurno(resultado)

        if (CajaVerificacionUiRules.debeProcesarComoTurnoActivo(resultado)) {
            aplicarResultadoConTurno(resultado)
        } else {
            aplicarResultadoSinTurno(resultado)
        }

        finalizarVerificacionTurno(exitoso = true)
        actualizarMenuToolbarCaja()
        marcarTurnoInicialCajaListo()
    }

    private fun procesarVerificacionTurnoFallida(detalleError: String, mostrarDialogo: Boolean) {
        if (!isAdded || _binding == null || isHidden) {
            finalizarVerificacionTurno(exitoso = false)
            return
        }

        dialogErrorHoraServidor?.dismiss()
        verificacionTurnoDisponible = false

        if (turnoAbierto && idTurnoActivo.isNotBlank()) {
            _binding?.layoutAperturaCaja?.hideElegantemente()
        } else {
            _binding?.layoutAperturaCaja?.showElegantemente()
            val overlayUi = CajaOverlayUiBuilder.construirOverlayParaFalloVerificacion()
            aplicarConfiguracionOverlayApertura(overlayUi)
        }

        cantidadTurnosPendientes = 0
        llaveResguardoTurnoActiva = ""

        if (detalleError.contains("reloj", ignoreCase = true) ||
            detalleError.contains("Zona horaria", ignoreCase = true)
        ) {
            _binding?.bannerDesincronizacionTiempo?.showElegantemente()
        }

        finalizarVerificacionTurno(exitoso = false)
        actualizarMenuToolbarCaja()

        if (mostrarDialogo && isAdded) {
            mostrarDialogoErrorHoraServidor(detalleError)
        }

        marcarTurnoInicialCajaListo()
    }

    private fun mostrarDialogoErrorHoraServidor(detalleError: String) {
        if (!isAdded || _binding == null) return
        if (dialogErrorHoraServidor?.isShowing == true) return

        val mensaje = buildString {
            if (
                detalleError.contains("Zona horaria", ignoreCase = true) ||
                detalleError.contains("reloj", ignoreCase = true)
            ) {
                append(detalleError)
            } else {
                append("No se pudo sincronizar la hora con el servidor.\n\n")
                append("Detalle técnico:\n")
                append(detalleError.ifBlank { "Sin detalle disponible." })
            }
            append("\n\nSin hora oficial no se puede operar la caja de forma segura.")
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Verificación de Tiempo")
            .setMessage(mensaje)
            .setCancelable(false)
            .setNegativeButton("Cerrar", null)
            .setPositiveButton("Reintentar", null)
            .create()

        dialog.setOnDismissListener {
            if (dialogErrorHoraServidor === dialog) {
                dialogErrorHoraServidor = null
            }
        }

        dialog.setCanceledOnTouchOutside(false)
        dialogErrorHoraServidor = dialog
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            ocultarDialogoErrorHoraServidor()
            verificarTurnoActivo()
        }
    }

    private fun ocultarDialogoErrorHoraServidor() {
        dialogErrorHoraServidor?.dismiss()
        dialogErrorHoraServidor = null
        _binding?.bannerDesincronizacionTiempo?.visibility = View.GONE

        // Si el turno ya está abierto, aseguramos que el layout de apertura desaparezca
        if (turnoAbierto && idTurnoActivo.isNotBlank()) {
            _binding?.layoutAperturaCaja?.visibility = View.GONE
        }

        // 🚨 ELIMINAMOS EL BLOQUE 'else' QUE CAUSABA EL BUCLE INFINITO
        // Ya no pedimos verificaciones frescas desde aquí para no ahogar el procesador.
    }
    private fun mostrarDialogoFechaHoraDispositivoInvalida() {
        if (!isAdded || _binding == null) return
        if (dialogErrorHoraServidor?.isShowing == true) return

        dialogErrorHoraServidor = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Fecha y hora incorrectas")
            .setMessage(FechaHoraServidorHelper.MENSAJE_ERROR_FECHA_HORA)
            .setCancelable(false)
            .setNegativeButton("Cerrar", null)
            .setPositiveButton("Reintentar") { _, _ ->
                verificarTurnoActivo()
            }
            .create()

        dialogErrorHoraServidor?.setCanceledOnTouchOutside(false)
        dialogErrorHoraServidor?.show()
    }

    private fun guardarMomentoCajaValidado(
        momento: FechaHoraServidorHelper.FechaHoraOficial
    ): FechaHoraServidorHelper.FechaHoraOficial {
        ultimaFechaHoraCajaValidada = momento
        return momento
    }

    private fun validarMomentoCriticoCaja(onValidado: (FechaHoraServidorHelper.FechaHoraOficial) -> Unit) {
        var operacionFinalizada = false

        // Configurar el Timeout (Circuit Breaker)
        runnableTimeoutValidacion?.let { handlerTimeoutValidacion.removeCallbacks(it) }
        runnableTimeoutValidacion = Runnable {
            if (!operacionFinalizada) {
                operacionFinalizada = true
                manejarFalloValidacionTiempo("El servidor de tiempo no respondió dentro del límite de 10 segundos.")
            }
        }
        handlerTimeoutValidacion.postDelayed(runnableTimeoutValidacion!!, TIMEOUT_VALIDACION_TIEMPO_MS)

        // Llamada al Helper de Firebase
        FechaHoraServidorHelper.validarMomentoActual(
            database = database,
            onSuccess = { resultado ->
                if (!operacionFinalizada) {
                    operacionFinalizada = true
                    handlerTimeoutValidacion.removeCallbacks(runnableTimeoutValidacion!!)
                    onValidado(guardarMomentoCajaValidado(resultado.fechaHoraOficial))
                }
            },
            onError = { error ->
                if (!operacionFinalizada) {
                    operacionFinalizada = true
                    handlerTimeoutValidacion.removeCallbacks(runnableTimeoutValidacion!!)
                    manejarFalloValidacionTiempo(error)
                }
            }
        )
    }

    private fun manejarFalloValidacionTiempo(detalleError: String) {
        if (!isAdded) return

        ocultarLoadingOverlayCaja()
        ocultarDialogoProgresoVenta()
        soyYoElQueCobra = false
        liberarCajaSiEstabaBloqueada() // Muy importante para no dejar la caja "en uso"

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Fallo de Conexión")
            .setMessage("$detalleError\n\nPor seguridad contable, la operación fue cancelada. Revisa tu internet e inténtalo de nuevo.")
            .setPositiveButton("Entendido", null)
            .show()
    }

    private fun obtenerMomentoOficialCaja(
        onSuccess: (FechaHoraServidorHelper.FechaHoraOficial) -> Unit,
        onError: (String) -> Unit
    ) {
        FechaHoraServidorHelper.obtenerMomentoActual(
            database = database,
            onSuccess = { momento ->
                onSuccess(guardarMomentoCajaValidado(momento))
            },
            onError = onError
        )
    }

    private fun sincronizarCamposVisiblesDesdeTimestampServidor(
        ref: DatabaseReference,
        campoTimestampServidor: String,
        rutasFecha: List<String>,
        rutasHora: List<String>,
        onComplete: (() -> Unit)? = null
    ) {
        ref.child(campoTimestampServidor)
            .get()
            .addOnSuccessListener { snapshot ->
                val timestampServidor = snapshot.getValue(Long::class.java)
                if (timestampServidor == null || timestampServidor <= 0L) {
                    onComplete?.invoke()
                    return@addOnSuccessListener
                }

                val fechaOficial = FechaHoraServidorHelper.formatearFechaFirebase(timestampServidor)
                val horaOficial = FechaHoraServidorHelper.formatearHora(timestampServidor)
                val updates = hashMapOf<String, Any>()
                rutasFecha.forEach { ruta -> updates[ruta] = fechaOficial }
                rutasHora.forEach { ruta -> updates[ruta] = horaOficial }

                if (updates.isEmpty()) {
                    onComplete?.invoke()
                    return@addOnSuccessListener
                }

                ref.updateChildren(updates)
                    .addOnCompleteListener { onComplete?.invoke() }
            }
            .addOnFailureListener {
                onComplete?.invoke()
            }
    }

    private fun verificarTurnoActivo() {
        // 1. Control de estado para evitar colisiones
        if (cerrandoTurno || verificacionTurnoEnCurso) {
            verificacionTurnoPendiente = true
            return
        }

        // 2. Referencia segura al binding (Blindaje NPE) - CORREGIDO: Línea agregada
        val b = _binding ?: return

        runnableVerificarTurno?.let { handlerSeguridadOperaciones.removeCallbacks(it) }
        runnableVerificarTurno = null

        dialogErrorHoraServidor?.dismiss()
        dialogErrorHoraServidor = null

        verificacionTurnoEnCurso = true
        val verificacionSilenciosa = turnoAbierto && idTurnoActivo.isNotBlank()
        prepararVerificacionTurnoSinOverlay(mostrarEstadoVerificando = !verificacionSilenciosa)

        // 3. Helper local para apagar el Watchdog y limpiar estado (Evita fugas de memoria)
        fun limpiarYFinalizar(exitoso: Boolean) {
            runnableTimeoutValidacion?.let { handlerTimeoutValidacion.removeCallbacks(it) }
            finalizarVerificacionTurno(exitoso)
        }

        // Watchdog de Seguridad (10s)
        runnableTimeoutValidacion?.let { handlerTimeoutValidacion.removeCallbacks(it) }
        runnableTimeoutValidacion = Runnable {
            if (verificacionTurnoEnCurso) {
                procesarVerificacionTurnoFallida(
                    detalleError = "El servidor tardó demasiado. Revisa tu conexión a internet.",
                    mostrarDialogo = true
                )
            }
        }
        handlerTimeoutValidacion.postDelayed(runnableTimeoutValidacion!!, 10_000L)

        FechaHoraServidorHelper.obtenerMomentoActual(
            database = database,
            onSuccess = { momento ->
                // Verificación post-red
                if (_binding == null || !isAdded) {
                    limpiarYFinalizar(false)
                    return@obtenerMomentoActual
                }

                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val desincronizado = verificarDesincronizacionReloj(momento)

                        withContext(Dispatchers.Main) {
                            val bMain = _binding ?: run {
                                limpiarYFinalizar(false)
                                return@withContext
                            }

                            relojDesincronizado = desincronizado
                            bMain.bannerDesincronizacionTiempo.visibility = if (desincronizado) View.VISIBLE else View.GONE

                            val fechaActual = momento.fechaFirebase
                            getCorteCajaCajeraRef().get()
                                .addOnSuccessListener { snapshot ->
                                    // ÉXITO: Firebase respondió
                                    runnableTimeoutValidacion?.let { handlerTimeoutValidacion.removeCallbacks(it) }

                                    if (_binding == null || !isAdded) {
                                        finalizarVerificacionTurno(false)
                                        return@addOnSuccessListener
                                    }

                                    val estadoTurno = CajaService.resolverEstadoTurno(snapshot, fechaActual)
                                    val saldoSugerido = if (estadoTurno.turnoDetectado == null) {
                                        CajaService.obtenerUltimoSaldoCierre(snapshot)
                                    } else 0.0

                                    val resultado = CajaVerificacionTurnoMapper.desdeEstadoTurno(
                                        estadoTurno = estadoTurno,
                                        fechaActual = fechaActual,
                                        saldoSugeridoApertura = saldoSugerido
                                    )

                                    procesarVerificacionTurnoExitosa(resultado)
                                    if (desincronizado) bMain.bannerDesincronizacionTiempo.visibility = View.VISIBLE
                                }
                                .addOnFailureListener { e ->
                                    // FALLO DE RED/FIREBASE
                                    limpiarYFinalizar(false)
                                    procesarVerificacionTurnoFallida(
                                        detalleError = "No se pudo leer el estado de caja: ${e.message}",
                                        mostrarDialogo = true
                                    )
                                }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            limpiarYFinalizar(false)
                            procesarVerificacionTurnoFallida("Error interno: ${e.message}", true)
                        }
                    }
                }
            },
            onError = { detalleError ->
                // FALLO DE TIEMPO
                limpiarYFinalizar(false)
                procesarVerificacionTurnoFallida(detalleError, true)
            }
        )
    }

    private suspend fun verificarDesincronizacionReloj(momento: FechaHoraServidorHelper.FechaHoraOficial): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://www.google.com")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "HEAD"
                connection.connectTimeout = 3000
                connection.readTimeout = 3000
                connection.useCaches = false
                val dateHeader = connection.getHeaderField("Date")
                connection.disconnect()
                if (!dateHeader.isNullOrBlank()) {
                    val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
                    val serverTimeMs = sdf.parse(dateHeader)?.time ?: System.currentTimeMillis()
                    val localTimeMs = System.currentTimeMillis()
                    val diferenciaMs = kotlin.math.abs(localTimeMs - serverTimeMs)
                    diferenciaMs > 5 * 60 * 1000
                } else {
                    val serverTimeMs = momento.timestampServidorMs
                    kotlin.math.abs(System.currentTimeMillis() - serverTimeMs) > 5 * 60 * 1000
                }
            } catch (e: Exception) {
                val serverTimeMs = momento.timestampServidorMs
                kotlin.math.abs(System.currentTimeMillis() - serverTimeMs) > 5 * 60 * 1000
            }
        }
    }

    private fun confirmarAperturaTurno() {
        if (bloquearOperacionPorRescateCajaBloqueada()) return
        if (turnoPendienteDiaAnterior) {
            mostrarAvisoTurnoPendiente(
                fechaTurnoActivo.ifBlank { fechaTurnoPendienteAvisada }
            )
            return
        }

        if (!verificacionTurnoDisponible) {
            verificarTurnoActivo()
            return
        }

        val inputMonto = obtenerInputMontoApertura() ?: return
        val textoMonto = inputMonto.text?.toString()
        val monto = textoMonto.toDoubleSeguro(null)

        if (monto == null || monto < 0.0) {
            obtenerLayoutMontoApertura()?.error = "Ingresa un monto válido"
            inputMonto.requestFocus()
            return
        }

        if (monto > UMBRAL_CONFIRMACION_MONTO_APERTURA) {
            confirmarMontoInusualmenteAlto(
                titulo = "Confirma la apertura",
                mensaje = "El monto de apertura ${MonedaHelper.formatear(monto)} es inusualmente alto. Verifica que no sea un error de digitación antes de continuar."
            ) {
                // Validamos tiempo real antes de proceder
                validarMomentoCriticoCaja { momento ->
                    confirmarAperturaTurnoConMontoValidado(monto, momento)
                }
            }
            return
        }

        // Validamos tiempo real antes de proceder
        validarMomentoCriticoCaja { momento ->
            confirmarAperturaTurnoConMontoValidado(monto, momento)
        }
    }

    private fun confirmarAperturaTurnoConMonto(monto: Double) {
        // Redirigimos al flujo validado
        validarMomentoCriticoCaja { momento ->
            confirmarAperturaTurnoConMontoValidado(monto, momento)
        }
    }

    private fun confirmarAperturaTurnoConMontoValidado(
        monto: Double,
        momento: FechaHoraServidorHelper.FechaHoraOficial
    ) {
        val turnosRef = getTurnosRef()
        val nuevoTurnoId = turnosRef.push().key
        if (nuevoTurnoId.isNullOrBlank()) {
            mostrarToastSeguro("No se pudo generar el turno")
            return
        }

        val idCajaOperativa = obtenerIdCajaOperativa()
        val nombreCajaOperativa = obtenerNombreCajaOperativa()
        val fechaActual = momento.fechaFirebase
        val horaActual = momento.horaTexto

        val fechaRef = database.reference
            .child("CorteCaja")
            .child("Cajeras")
            .child(idCajaOperativa)
            .child(fechaActual)

        fechaRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val fechaActualMap = CajaTurnoHelper.mutableMapFromData(currentData)
                if (!CajaService.puedeAbrirTurno(fechaActualMap)) {
                    return Transaction.abort()
                }

                currentData.value = CajaService.aplicarAperturaTurnoEnFecha(
                    fechaActualMap = fechaActualMap,
                    nuevoTurnoId = nuevoTurnoId,
                    fechaActual = fechaActual,
                    horaActual = horaActual,
                    monto = monto,
                    idCajaOperativa = idCajaOperativa,
                    nombreCajaOperativa = nombreCajaOperativa,
                    idUsuarioApertura = idCajera,
                    nombreUsuarioApertura = nombreCajera
                )
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    mostrarToastSeguro("No se pudo abrir el turno: ${error.message}")
                    return
                }

                if (!committed) {
                    mostrarToastSeguro("Ya existe un turno abierto en esta caja")
                    verificarTurnoActivo()
                    return
                }

                if (!isAdded || _binding == null) return
                idTurnoActivo = nuevoTurnoId
                fechaTurnoActivo = fechaActual
                montoAperturaTurno = monto
                turnoAbierto = true
                verificacionTurnoDisponible = true
                turnoPendienteDiaAnterior = false
                cantidadTurnosPendientes = 0
                fechaTurnoPendienteAvisada = ""
                val punteroGlobal = CajaService.crearPunteroGlobalApertura(
                    nuevoTurnoId = nuevoTurnoId,
                    fechaActual = fechaActual,
                    horaActual = horaActual,
                    idUsuarioApertura = idCajera,
                    nombreUsuarioApertura = nombreCajera
                )

                getCorteCajaCajeraRef()
                    .child("turnoActivoGlobal")
                    .setValue(punteroGlobal)
                    .addOnCompleteListener {
                        sincronizarCamposVisiblesDesdeTimestampServidor(
                            ref = fechaRef.child("turnos").child(nuevoTurnoId),
                            campoTimestampServidor = "timestampAperturaServidor",
                            rutasFecha = listOf("fechaAperturaLocal"),
                            rutasHora = listOf("horaAperturaLocal")
                        )
                        sincronizarCamposVisiblesDesdeTimestampServidor(
                            ref = getCorteCajaCajeraRef().child("turnoActivoGlobal"),
                            campoTimestampServidor = "timestampAperturaServidor",
                            rutasFecha = listOf("fechaAperturaLocal"),
                            rutasHora = listOf("horaAperturaLocal")
                        )

                        MovimientoLogger.registrar(
                            tipo = "apertura_turno",
                            modulo = "caja",
                            titulo = "Turno abierto",
                            descripcion = "Apertura con ${MonedaHelper.formatear(monto)}",
                            idUsuario = idCajera,
                            nombreUsuario = nombreCajera,
                            monto = monto,
                            referenciaId = nuevoTurnoId,
                            idCaja = idCajaOperativa,
                            nombreCaja = nombreCajaOperativa,
                            extra = mapOf("fechaTurno" to fechaActual),
                            database = database
                        )

                        activarResguardoDesconexionTurno()
                        binding.layoutAperturaCaja.hideElegantemente()
                        // Limpiamos el monto de apertura para que no quede "pegado" si luego
                        // se vuelve a mostrar el overlay (por cambio de caja, cierre, etc.).
                        obtenerLayoutMontoApertura()?.error = null
                        obtenerInputMontoApertura()?.setText("")
                        ocultarTeclado()
                        actualizarMenuToolbarCaja()
                        mostrarToastSeguro("Turno abierto correctamente")
                    }
            }
        })
    }

    private fun intentarMostrarCierreCaja() {
        if (!turnoAbierto || idTurnoActivo.isBlank()) {
            mostrarToastSeguro("No hay un turno abierto")
            return
        }

        if (!verificarHoraAntesDeOperar("cerrar el turno actual")) return

        if (ventaEnProceso || actualizandoCaja || reconciliacionPreRegistroEnCurso) {
            val mensaje = when {
                reconciliacionPreRegistroEnCurso -> "El sistema está restaurando stock de una operación previa. Espera unos segundos."
                else -> "Espera a que termine la operación actual"
            }
            mostrarToastSeguro(mensaje)
            return
        }

        if (turnoPendienteDiaAnterior && listaProductosCaja.isNotEmpty()) {
            limpiarResiduoCajaDeTurnoAnteriorSiCorresponde()
            mostrarToastSeguro("Se limpiaron los productos residuales del turno anterior")
            return
        }

        if (listaProductosCaja.isNotEmpty()) {
            if (estaEnModoRescateCajaBloqueada()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Carrito pendiente")
                    .setMessage("Esta caja bloqueada aun tiene ${listaProductosCaja.size} producto(s) en el carrito. Revisa la caja antes de cerrar.")
                    .setPositiveButton("Entendido", null)
                    .show()
                return
            }
            
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Carrito con productos")
                .setMessage("No puedes cerrar el turno si hay productos en el carrito. ¿Qué deseas hacer?")
                .setNeutralButton("Cancelar", null)
                .setNegativeButton("Vaciar Carrito") { _, _ ->
                    vaciarCarritoCompleto()
                }
                .setPositiveButton("Ver Carrito") { _, _ ->
                    irAListaCarritoCaja()
                }
                .show()
            return
        }

        if (_binding?.layoutComprobante?.visibility == View.VISIBLE) {
            mostrarToastSeguro("Termina o cancela el cobro antes de cerrar")
            return
        }

        if (cantidadVentasEnEspera > 0) {
            if (estaEnModoRescateCajaBloqueada()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Ventas en espera pendientes")
                    .setMessage("Esta caja bloqueada todavia tiene $cantidadVentasEnEspera ventas en espera. El modo rescate no permite seguir operando sobre ellas, asi que primero deben revisarse antes del cierre.")
                    .setPositiveButton("Entendido", null)
                    .show()
                return
            }
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Ventas en espera pendientes")
                .setMessage("Aún tienes $cantidadVentasEnEspera ventas en espera. Debes anularlas o procesarlas antes de cerrar el turno.")
                .setPositiveButton("Entendido", null)
                .show()
            return
        }

        mostrarLayoutCierreCaja()
    }

    private fun vaciarCarritoCompleto() {
        if (bloquearOperacionPorRescateCajaBloqueada()) return
        actualizarEstadoActualizandoCaja(true)
        getCajaRef().child("Productos").removeValue()
            .addOnCompleteListener { actualizarEstadoActualizandoCaja(false) }
    }

    private fun irAListaCarritoCaja() {
        _binding?.recyclerView2?.smoothScrollToPosition(0)
        ocultarLayoutCierreCaja()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun limpiarResiduoCajaDeTurnoAnteriorSiCorresponde() {
        if (!turnoPendienteDiaAnterior) return
        if (listaProductosCaja.isEmpty()) return
        if (limpiezaResiduoTurnoAnteriorEnCurso) return

        limpiezaResiduoTurnoAnteriorEnCurso = true
        getCajaRef()
            .child("Productos")
            .removeValue()
            .addOnCompleteListener { task ->
                limpiezaResiduoTurnoAnteriorEnCurso = false
                if (!isAdded || _binding == null) return@addOnCompleteListener

                if (task.isSuccessful) {
                    listaProductosCaja.clear()
                    refrescarCarritoCompletoUi()
                    actualizarTotales()
                    mostrarEstadoVacio()
                    if (_binding?.layoutComprobante?.visibility == View.VISIBLE) {
                        salirDeCheckoutPorCajaVacia()
                    }
                } else {
                    mostrarToastSeguro(
                        "No se pudo limpiar la caja residual del turno anterior"
                    )
                }
            }
    }

    private fun mostrarLayoutCierreCaja() {
        obtenerLayoutCierreActual() ?: return
        val recycler = obtenerRecyclerResumenCierre() ?: return
        val tvTotal = obtenerTextTotalEsperadoCierre() ?: return
        val tvApertura = obtenerAperturaCierreTextView()  // Helper para tablet/movil

        aplicarCoordinacionVisual(
            CajaOverlayCoordinator.coordinarPreparacionCierre(
                turnoPendienteDiaAnterior = turnoPendienteDiaAnterior
            )
        )

        getTurnoActivoRef()
            ?.get()
            ?.addOnSuccessListener { snapshot ->
                if (!isAdded || _binding == null) return@addOnSuccessListener

                // Mostrar la apertura en su campo dedicado
                val montoApertura = snapshot.child("montoApertura").value?.toString()?.toDoubleOrNull() ?: 0.0
                tvApertura?.text = MonedaHelper.formatear(montoApertura)

                // Solo métodos de pago (sin apertura ni egresos)
                val items = CajaTurnoHelper.construirSoloMetodosPago(snapshot)
                val totalVentas = snapshot.child("totalVentas").getValue(String::class.java)?.toDoubleOrNull() ?: 0.0
                recycler.adapter = AdapterResumenPagoCierre(items, totalVentas)

                val totalEsperado = snapshot.child("efectivoEsperado").getValue(String::class.java)?.toDoubleOrNull() ?: 0.0
                tvTotal.text = MonedaHelper.formatear(totalEsperado)
                tvTotal.setTextColor(
                    if (totalEsperado < 0.0) Color.parseColor("#DC2626")
                    else Color.parseColor("#111111")
                )

                val prefillCierre = totalEsperado.coerceAtLeast(0.0)
                obtenerInputEfectivoRealCierre()?.setText(String.format(Locale.US, "%.2f", prefillCierre))
                actualizarEstadoObservacionCierre(totalEsperado, prefillCierre)
                actualizarModoCierreForzado()
                aplicarCoordinacionVisual(CajaOverlayCoordinator.coordinarMostrarCierre())
            }
            ?.addOnFailureListener { e ->
                mostrarToastSeguro("No se pudo cargar el cierre: ${e.message}")
            }
    }

    private fun obtenerAperturaCierreTextView(): TextView? {
        val b = _binding ?: return null
        return if (esTablet()) {
            b.root.findViewById(R.id.tvAperturaCierreTablet)
        } else {
            b.root.findViewById(R.id.tvAperturaCierreMovil)
        }
    }

    private fun ocultarLayoutCierreCaja() {
        if (cierreObligatorioActivo()) return

        // 1. SOLUCIÓN: Apagamos el efecto blur/ofuscado del fondo de inmediato
        toggleEfectoVidrioFondo(false)

        obtenerLayoutCierreActual()?.visibility = View.GONE
        obtenerLayoutObservacionCierre()?.apply {
            visibility = View.GONE
            error = null
        }
        obtenerInputObservacionCierre()?.setText("")

        // 2. RESTAURACIÓN: Devolvemos la visibilidad a los paneles de la caja tapados
        val b = _binding ?: return
        if (turnoAbierto) {
            b.appBarLayout?.visibility = View.VISIBLE
            if (esTablet()) b.buscadorLayout?.visibility = View.VISIBLE else b.layoutBuscadorInline?.visibility = View.VISIBLE
            b.cardResumenCaja.visibility = View.VISIBLE
        }
    }

    private fun ocultarLayoutCierreCajaInterno() {
        aplicarCoordinacionVisual(CajaOverlayCoordinator.coordinarOcultarCierreInterno())
    }

    private fun confirmarCierreTurno() {
        if (cerrandoTurno) {
            mostrarToastSeguro("El turno ya se está cerrando")
            return
        }

        val inputEfectivo = obtenerInputEfectivoRealCierre() ?: return
        val texto = inputEfectivo.text?.toString()
        val efectivoReal = texto.toDoubleSeguro(null)

        if (cantidadVentasEnEspera > 0) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Ventas en espera pendientes")
                .setMessage("Aún tienes $cantidadVentasEnEspera ventas en espera. Debes anularlas o procesarlas antes de cerrar el turno.")
                .setPositiveButton("Entendido", null)
                .show()
            return
        }

        if (efectivoReal == null || efectivoReal < 0.0) {
            obtenerLayoutMontoCierre()?.error = "Ingresa un monto válido"
            inputEfectivo.requestFocus()
            return
        }

        if (efectivoReal > UMBRAL_CONFIRMACION_MONTO_CIERRE) {
            confirmarMontoInusualmenteAlto(
                titulo = "Confirma el cierre",
                mensaje = "El efectivo real ingresado ${MonedaHelper.formatear(efectivoReal)} es inusualmente alto. Verifica que no sea un error de digitación antes de continuar."
            ) {
                validarMomentoCriticoCaja { confirmarCierreTurnoConMonto(efectivoReal) }
            }
            return
        }

        validarMomentoCriticoCaja { confirmarCierreTurnoConMonto(efectivoReal) }
    }

    private fun confirmarCierreTurnoConMonto(efectivoReal: Double) {
        val inputObservacion = obtenerInputObservacionCierre()
        val layoutObservacion = obtenerLayoutObservacionCierre()

        val turnoRef = getTurnoActivoRef()
        if (turnoRef == null) {
            mostrarToastSeguro("No hay un turno activo")
            return
        }

        turnoRef.get()
            .addOnSuccessListener { snapshot ->
                val resultadoCierre = CajaTurnoHelper.calcularResultadoCierre(
                    snapshot = snapshot,
                    efectivoReal = efectivoReal,
                    porcentajeAlertaDescuadre = PORCENTAJE_ALERTA_DESCUADRE,
                    umbralMinimoAlertaDescuadre = UMBRAL_MINIMO_ALERTA_DESCUADRE
                )
                val observacionCierre = inputObservacion?.text?.toString()?.trim().orEmpty()
                val fechaActual = fechaTurnoActivo.ifBlank { obtenerFechaActual() }
                val idCajaOperativa = obtenerIdCajaOperativa()

                actualizarEstadoObservacionCierre(resultadoCierre.efectivoEsperado, efectivoReal)

                if (resultadoCierre.requiereObservacion && observacionCierre.isBlank()) {
                    layoutObservacion?.error = "Describe el motivo del descuadre"
                    inputObservacion?.requestFocus()
                    return@addOnSuccessListener
                }

                fun resolverObservacionYGuardar() {
                    resolverObservacionCierreConDevoluciones(
                        idCajaOperativa = idCajaOperativa,
                        fechaTurno = fechaActual,
                        idTurno = idTurnoActivo,
                        observacionManual = observacionCierre
                    ) { observacionFinal ->
                        guardarCierreTurno(
                            turnoRef = turnoRef,
                            idCajaOperativa = idCajaOperativa,
                            fechaActual = fechaActual,
                            efectivoReal = efectivoReal,
                            diferencia = resultadoCierre.diferencia,
                            estadoCuadre = resultadoCierre.estadoCuadre,
                            montoSobrante = resultadoCierre.montoSobrante,
                            montoFaltante = resultadoCierre.montoFaltante,
                            observacionCierre = observacionFinal,
                            observacionCierreManual = observacionCierre,
                            totalVentas = resultadoCierre.totalVentas,
                            totalEgresos = resultadoCierre.totalEgresos
                        )
                    }
                }

                if (kotlin.math.abs(resultadoCierre.diferencia) >= resultadoCierre.umbralAlerta) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Verifica el cierre")
                        .setMessage(
                            "Existe una diferencia considerable de ${
                                MonedaHelper.formatear(kotlin.math.abs(resultadoCierre.diferencia))
                            }. Verifica el efectivo físico una vez más antes de cerrar."
                        )
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Cerrar de todos modos") { _, _ ->
                            resolverObservacionYGuardar()
                        }
                        .show()
                    return@addOnSuccessListener
                }

                resolverObservacionYGuardar()
            }
            .addOnFailureListener { e ->
                mostrarToastSeguro("No se pudo leer el turno actual: ${e.message}")
            }
    }

    private fun resolverObservacionCierreConDevoluciones(
        idCajaOperativa: String,
        fechaTurno: String,
        idTurno: String,
        observacionManual: String,
        onReady: (String) -> Unit
    ) {
        if (idCajaOperativa.isBlank() || fechaTurno.isBlank() || idTurno.isBlank()) {
            onReady(observacionManual)
            return
        }

        database.reference
            .child("VentasPorCajera")
            .child(idCajaOperativa)
            .child(fechaTurno)
            .get()
            .addOnSuccessListener { snapshot ->
                val lineas = mutableListOf<DevolucionCierreLinea>()

                snapshot.children.forEach { ventaSnap ->
                    val devolucionSnap = ventaSnap.child("devolucion")
                    if (!devolucionSnap.exists()) return@forEach

                    val eventosSnap = devolucionSnap.child("eventos")
                    if (eventosSnap.exists()) {
                        eventosSnap.children.forEach { eventoSnap ->
                            val idTurnoEvento = eventoSnap.child("idTurno").obtenerTexto()
                                .ifBlank { devolucionSnap.child("idTurno").obtenerTexto() }
                            if (idTurnoEvento.isNotBlank() && idTurnoEvento != idTurno) return@forEach
                            lineas += construirLineasObservacionDevolucion(
                                detalleSnap = eventoSnap.child("detalleProductos"),
                                motivo = eventoSnap.child("motivo").obtenerTexto()
                                    .ifBlank { devolucionSnap.child("motivo").obtenerTexto() },
                                tipoResolucion = eventoSnap.child("tipoResolucion").obtenerTexto()
                                    .ifBlank { devolucionSnap.child("tipoResolucion").obtenerTexto() },
                                sustitucionNombre = eventoSnap.child("sustitucion").child("nombre").obtenerTexto(),
                                montoCobradoCliente = eventoSnap.child("ajusteFinanciero")
                                    .child("montoCobradoCliente")
                                    .obtenerDoubleFlexible()
                                    ?: eventoSnap.child("montoCobradoCliente").obtenerDoubleFlexible()
                                    ?: 0.0,
                                montoDevueltoCliente = eventoSnap.child("ajusteFinanciero")
                                    .child("montoDevueltoCliente")
                                    .obtenerDoubleFlexible()
                                    ?: eventoSnap.child("montoDevuelto").obtenerDoubleFlexible()
                                    ?: 0.0,
                                medioPagoDiferencia = eventoSnap.child("pagoDiferencia")
                                    .child("metodoPago")
                                    .obtenerTexto(),
                                montoPagadoDiferencia = eventoSnap.child("pagoDiferencia")
                                    .child("montoPagado")
                                    .obtenerDoubleFlexible()
                                    ?: 0.0,
                                montoFallback = eventoSnap.child("montoDevuelto").obtenerDoubleFlexible()
                                    ?: eventoSnap.child("valorProductosDevueltos").obtenerDoubleFlexible()
                                    ?: devolucionSnap.child("montoDevuelto").obtenerDoubleFlexible()
                                    ?: 0.0
                            )
                        }
                    } else {
                        val idTurnoVenta = ventaSnap.child("idTurno").obtenerTexto()
                            .ifBlank { ventaSnap.child("infoVenta").child("idTurno").obtenerTexto() }
                            .ifBlank { devolucionSnap.child("idTurno").obtenerTexto() }
                        if (idTurnoVenta.isNotBlank() && idTurnoVenta != idTurno) return@forEach

                        lineas += construirLineasObservacionDevolucion(
                            detalleSnap = devolucionSnap.child("detalleProductos"),
                            motivo = devolucionSnap.child("motivo").obtenerTexto(),
                            tipoResolucion = devolucionSnap.child("tipoResolucion").obtenerTexto(),
                            sustitucionNombre = devolucionSnap.child("sustitucionUltima").child("nombre").obtenerTexto(),
                            montoCobradoCliente = devolucionSnap.child("montoCobradoClienteUltimo").obtenerDoubleFlexible()
                                ?: devolucionSnap.child("montoCobradoClienteAcumulado").obtenerDoubleFlexible()
                                ?: 0.0,
                            montoDevueltoCliente = devolucionSnap.child("montoDevueltoUltimo").obtenerDoubleFlexible()
                                ?: devolucionSnap.child("montoDevuelto").obtenerDoubleFlexible()
                                ?: 0.0,
                            medioPagoDiferencia = devolucionSnap.child("metodoPagoDiferenciaUltimo").obtenerTexto()
                                .ifBlank {
                                    devolucionSnap.child("pagoDiferenciaUltimo")
                                        .child("metodoPago")
                                        .obtenerTexto()
                                },
                            montoPagadoDiferencia = devolucionSnap.child("montoPagoDiferenciaUltimo").obtenerDoubleFlexible()
                                ?: devolucionSnap.child("pagoDiferenciaUltimo")
                                    .child("montoPagado")
                                    .obtenerDoubleFlexible()
                                ?: 0.0,
                            montoFallback = devolucionSnap.child("montoDevuelto").obtenerDoubleFlexible() ?: 0.0
                        )
                    }
                }

                onReady(CajaTurnoHelper.combinarObservacionCierre(observacionManual, lineas))
            }
            .addOnFailureListener {
                onReady(observacionManual)
            }
    }

    private fun construirLineasObservacionDevolucion(
        detalleSnap: DataSnapshot,
        motivo: String,
        tipoResolucion: String,
        sustitucionNombre: String,
        montoCobradoCliente: Double,
        montoDevueltoCliente: Double,
        medioPagoDiferencia: String,
        montoPagadoDiferencia: Double,
        montoFallback: Double
    ): List<DevolucionCierreLinea> {
        if (!detalleSnap.exists()) return emptyList()

        val detalles = detalleSnap.children.toList()
        val montoUnitarioFallback = if (detalles.size == 1) {
            montoFallback
        } else {
            0.0
        }

        return detalles.mapNotNull { itemSnap ->
            val producto = itemSnap.child("nombre").obtenerTexto().ifBlank { "Producto" }
            val cantidad = itemSnap.child("cantidadDevueltaAhora").value?.toString()?.toIntOrNull()
                ?: itemSnap.child("cantidadDevuelta").value?.toString()?.toIntOrNull()
                ?: itemSnap.child("cantidadVendida").value?.toString()?.toIntOrNull()
                ?: 0
            if (cantidad <= 0) return@mapNotNull null

            val montoItem = itemSnap.child("subtotalDevuelto").obtenerDoubleFlexible()
                ?: montoUnitarioFallback

            DevolucionCierreLinea(
                producto = producto,
                cantidad = cantidad,
                presentacion = itemSnap.child("presentacion").obtenerTexto(),
                monto = montoItem,
                motivo = motivo.ifBlank { "Sin motivo" },
                tipoResolucion = tipoResolucion,
                productoSustituto = sustitucionNombre,
                montoCobradoCliente = montoCobradoCliente,
                medioPagoDiferencia = medioPagoDiferencia,
                montoPagadoDiferencia = montoPagadoDiferencia,
                montoDevueltoCliente = if (normalizarTipoResolucionDevolucion(tipoResolucion) == "cambio_producto") {
                    montoDevueltoCliente
                } else {
                    0.0
                }
            )
        }
    }

    private fun guardarCierreTurno(turnoRef: DatabaseReference, idCajaOperativa: String, fechaActual: String, efectivoReal: Double, diferencia: Double, estadoCuadre: String, montoSobrante: Double, montoFaltante: Double, observacionCierre: String, observacionCierreManual: String, totalVentas: Double, totalEgresos: Double) {
        validarMomentoCriticoCaja { momento ->
            guardarCierreTurnoValidado(
                turnoRef = turnoRef,
                idCajaOperativa = idCajaOperativa,
                fechaActual = fechaActual,
                momento = momento,
                efectivoReal = efectivoReal,
                diferencia = diferencia,
                estadoCuadre = estadoCuadre,
                montoSobrante = montoSobrante,
                montoFaltante = montoFaltante,
                observacionCierre = observacionCierre,
                observacionCierreManual = observacionCierreManual,
                totalVentas = totalVentas,
                totalEgresos = totalEgresos
            )
        }
    }

    private fun guardarCierreTurnoValidado(
        turnoRef: DatabaseReference,
        idCajaOperativa: String,
        fechaActual: String,
        momento: FechaHoraServidorHelper.FechaHoraOficial,
        efectivoReal: Double,
        diferencia: Double,
        estadoCuadre: String,
        montoSobrante: Double,
        montoFaltante: Double,
        observacionCierre: String,
        observacionCierreManual: String,
        totalVentas: Double,
        totalEgresos: Double
    ) {
        cerrandoTurno = true
        runnableVerificarTurno?.let { handlerSeguridadOperaciones.removeCallbacks(it) }
        runnableVerificarTurno = null
        obtenerBotonFinalizarCierre()?.isEnabled = false

        // --- Watchdog para Cierre de Turno (60 segundos) ---
        runnableTimeoutCierreTurno?.let { handlerSeguridadOperaciones.removeCallbacks(it) }
        runnableTimeoutCierreTurno = Runnable {
            if (cerrandoTurno) {
                cerrandoTurno = false
                obtenerBotonFinalizarCierre()?.isEnabled = true
                ocultarLoadingOverlayCaja() // SOLUCIÓN: Quitamos la ofuscación si el servidor expira
                mostrarToastSeguro("El cierre de turno no respondió a tiempo. Verifica tu red e intenta de nuevo.")
            }
        }
        handlerSeguridadOperaciones.postDelayed(runnableTimeoutCierreTurno!!, 60_000L)
        // -----------------------------------------------------------

        val fechaTurnoCerrado = fechaActual
        val eraTurnoPendiente = fechaTurnoCerrado != momento.fechaFirebase
        val idTurnoCerrado = idTurnoActivo
        val montoTurnoCerrado = montoAperturaTurno

        turnoRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val turnoActual = CajaTurnoHelper.mutableMapFromData(currentData)
                if (!CajaService.puedeCerrarTurno(turnoActual)) {
                    return Transaction.abort()
                }

                currentData.value = CajaService.aplicarCierreTurno(
                    turnoActual = turnoActual,
                    fechaActual = fechaActual,
                    horaCierre = momento.horaTexto,
                    idUsuarioCierre = idCajera,
                    nombreUsuarioCierre = nombreCajera,
                    efectivoReal = efectivoReal,
                    diferencia = diferencia,
                    estadoCuadre = estadoCuadre,
                    montoSobrante = montoSobrante,
                    montoFaltante = montoFaltante,
                    observacionCierre = observacionCierre,
                    observacionCierreManual = observacionCierreManual,
                    totalGeneralTurno = montoAperturaTurno + totalVentas - totalEgresos
                )
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                runnableTimeoutCierreTurno?.let { handlerSeguridadOperaciones.removeCallbacks(it) }

                // Si el fragmento se cerró mientras Firebase respondía, frenamos para evitar NPE
                val b = _binding ?: return

                if (error != null) {
                    cerrandoTurno = false
                    verificacionTurnoPendiente = false
                    obtenerBotonFinalizarCierre()?.isEnabled = true
                    ocultarLoadingOverlayCaja() // SOLUCIÓN: Quitamos la ofuscación en error de red
                    mostrarToastSeguro("No se pudo cerrar el turno: ${error.message}")
                    return
                }

                if (!committed) {
                    cerrandoTurno = false
                    verificacionTurnoPendiente = false
                    obtenerBotonFinalizarCierre()?.isEnabled = true
                    ocultarLoadingOverlayCaja() // SOLUCIÓN: Quitamos la ofuscación si la transacción aborta
                    mostrarToastSeguro("El turno ya fue cerrado o cambió su estado")
                    return
                }

                sincronizarCamposVisiblesDesdeTimestampServidor(
                    ref = turnoRef,
                    campoTimestampServidor = "timestampCierreServidor",
                    rutasFecha = listOf("fechaCierreLocal"),
                    rutasHora = listOf("horaCierreLocal")
                )
                cancelarResguardoDesconexionTurno()

                limpiarEstadoTurnoActivoTrasCierre(
                    idCajaOperativa = idCajaOperativa,
                    fechaActual = fechaActual,
                    idTurnoCerrado = idTurnoCerrado,
                    onSuccess = {
                        val bSuccess = _binding ?: return@limpiarEstadoTurnoActivoTrasCierre

                        MovimientoLogger.registrar(
                            tipo = "cierre_turno",
                            modulo = "caja",
                            titulo = "Turno cerrado",
                            descripcion = CajaService.mensajeResumenCierre(
                                estadoCuadre = estadoCuadre,
                                montoSobrante = montoSobrante,
                                montoFaltante = montoFaltante
                            ),
                            idUsuario = idCajera,
                            nombreUsuario = nombreCajera,
                            monto = montoTurnoCerrado + totalVentas - totalEgresos,
                            referenciaId = idTurnoCerrado,
                            idCaja = idCajaOperativa,
                            nombreCaja = obtenerNombreCajaOperativa(),
                            extra = mapOf(
                                "fechaTurno" to fechaTurnoCerrado,
                                "efectivoReal" to efectivoReal,
                                "diferencia" to diferencia,
                                "estadoCuadre" to estadoCuadre,
                                "totalVentas" to totalVentas,
                                "totalEgresos" to totalEgresos,
                                "observacion" to observacionCierre
                            ),
                            database = database
                        )

                        adjuntarStockCriticoAlTurno(turnoRef)
                        cerrandoTurno = false
                        verificacionTurnoPendiente = false
                        obtenerBotonFinalizarCierre()?.isEnabled = true

                        turnoAbierto = false
                        idTurnoActivo = ""
                        fechaTurnoActivo = momento.fechaFirebase
                        turnoPendienteDiaAnterior = false
                        cantidadTurnosPendientes = 0
                        fechaTurnoPendienteAvisada = ""
                        montoAperturaTurno = 0.0
                        ocultarLayoutCierreCajaInterno()
                        actualizarMenuToolbarCaja()

                        val mensajeCierre = CajaService.mensajeResumenCierre(
                            estadoCuadre = estadoCuadre,
                            montoSobrante = montoSobrante,
                            montoFaltante = montoFaltante
                        )

                        if (estaEnModoRescateCajaBloqueada()) {
                            volverAMiCajaDesdeSupervision("Turno rescatado cerrado. Volviste a tu caja.")
                        } else if (eraTurnoPendiente) {
                            manejarPostCierreTurnoPendiente(fechaTurnoCerrado, mensajeCierre)
                        } else {
                            bSuccess.layoutAperturaCaja.visibility = View.VISIBLE // Blindaje contra NPE
                            mostrarDialogoSeguridadPostCierre(
                                mensajeCierre     = mensajeCierre,
                                montoApertura     = montoTurnoCerrado,
                                totalVentas       = totalVentas,
                                totalEgresos      = totalEgresos,
                                efectivoEsperado  = montoTurnoCerrado + totalVentas - totalEgresos,
                                efectivoReal      = efectivoReal,
                                estadoCuadre      = estadoCuadre,
                                diferencia        = diferencia,
                                observacionCierre = observacionCierre,
                                idTurnoCerrado    = idTurnoCerrado,
                                fechaTurnoCerrado = fechaTurnoCerrado,
                                idCajaOperativa   = idCajaOperativa
                            )
                        }
                    },
                    onError = { mensajeError ->
                        cerrandoTurno = false
                        verificacionTurnoPendiente = false
                        obtenerBotonFinalizarCierre()?.isEnabled = true
                        ocultarLoadingOverlayCaja() // SOLUCIÓN: Quitamos la ofuscación en caso de fallo interno
                        mostrarToastSeguro(mensajeError)
                        verificarTurnoActivo()
                    }
                )
            }
        })
    }

    private fun limpiarEstadoTurnoActivoTrasCierre(
        idCajaOperativa: String,
        fechaActual: String,
        idTurnoCerrado: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val rootRef = database.reference
        val cajaRef = rootRef.child("CorteCaja").child("Cajeras").child(idCajaOperativa)
        val fechaRef = cajaRef.child(fechaActual)
        val updates = hashMapOf<String, Any>(
            "CorteCaja/Cajeras/$idCajaOperativa/$fechaActual/turnos/$idTurnoCerrado/timestampCierreServidor" to ServerValue.TIMESTAMP,
            "CorteCaja/Cajeras/$idCajaOperativa/$fechaActual/turnos/$idTurnoCerrado/ultimaActualizacionServidor" to ServerValue.TIMESTAMP
        )

        rootRef.updateChildren(updates)
            .addOnFailureListener { error ->
                onError("El turno se cerró, pero no se pudieron sellar sus metadatos: ${error.message}")
            }
            .addOnSuccessListener {
                fechaRef.child("turnoActivoId").removeValue()
                    .addOnFailureListener { error ->
                        onError("El turno se cerró, pero no se pudo liberar el turno activo: ${error.message}")
                    }
                    .addOnSuccessListener {
                        cajaRef.child("turnoActivoGlobal").removeValue()
                            .addOnFailureListener { error ->
                                onError("El turno se cerró, pero no se pudo limpiar el puntero global: ${error.message}")
                            }
                            .addOnSuccessListener {
                                fechaRef.child("turnoActivoId").get()
                                    .addOnFailureListener { error ->
                                        onError("El turno se cerró, pero no se pudo verificar el estado final: ${error.message}")
                                    }
                                    .addOnSuccessListener { turnoActivoSnapshot ->
                                        cajaRef.child("turnoActivoGlobal").get()
                                            .addOnFailureListener { error ->
                                                onError("El turno se cerró, pero no se pudo verificar el puntero global: ${error.message}")
                                            }
                                            .addOnSuccessListener { turnoGlobalSnapshot ->
                                                if (turnoActivoSnapshot.exists() || turnoGlobalSnapshot.exists()) {
                                                    onError("El turno se cerró, pero quedó un estado activo colgado. Reintenta una vez.")
                                                } else {
                                                    onSuccess()
                                                }
                                            }
                                    }
                            }
                    }
            }
    }

    private fun manejarPostCierreTurnoPendiente(fechaTurnoCerrado: String, mensajeCierre: String) {
        if (!isAdded) return

        val fechaActual = obtenerFechaActual()
        getCorteCajaCajeraRef()
            .get()
            .addOnSuccessListener { snapshot ->
                if (!isAdded) return@addOnSuccessListener
                val pendientesRestantes = CajaService.obtenerTurnosAbiertosAnteriores(snapshot, fechaActual)
                verificarTurnoActivo()

                val mensaje = if (pendientesRestantes.isNotEmpty()) {
                    "$mensajeCierre\n\nSe cerró el turno del día $fechaTurnoCerrado. Aún quedan ${pendientesRestantes.size} turnos pendientes. Debes continuar con el cierre del día ${pendientesRestantes.first().fecha}."
                } else {
                    "$mensajeCierre\n\nSe cerró el turno pendiente del día $fechaTurnoCerrado. Ya no quedan días pendientes y la caja puede continuar con normalidad."
                }

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Cierre pendiente completado")
                    .setMessage(mensaje)
                    .setPositiveButton("Entendido", null)
                    .show()
            }
            .addOnFailureListener {
                verificarTurnoActivo()
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Cierre pendiente completado")
                    .setMessage("$mensajeCierre\n\nSe cerró el turno pendiente del día $fechaTurnoCerrado. Revisa nuevamente la caja para continuar.")
                    .setPositiveButton("Entendido", null)
                    .show()
            }
    }

    private fun mostrarDialogoSeguridadPostCierre(
        mensajeCierre: String,
        montoApertura: Double,
        totalVentas: Double,
        totalEgresos: Double,
        efectivoEsperado: Double,
        efectivoReal: Double,
        estadoCuadre: String = "cuadrado",
        diferencia: Double = 0.0,
        observacionCierre: String = "",
        idTurnoCerrado: String = "",
        fechaTurnoCerrado: String = "",
        idCajaOperativa: String = ""
    ) {
        if (!isAdded) return

        val detalleCierre = buildString {
            appendLine(mensajeCierre)
            appendLine()
            appendLine("Resumen del turno:")
            appendLine("Apertura: ${MonedaHelper.formatear(montoApertura)}")
            appendLine("Ventas del turno: ${MonedaHelper.formatear(totalVentas)}")
            if (totalEgresos > 0.0) appendLine("Egresos registrados: ${MonedaHelper.formatear(totalEgresos)}")
            appendLine("Efectivo esperado: ${MonedaHelper.formatear(efectivoEsperado)}")
            appendLine("Efectivo contado: ${MonedaHelper.formatear(efectivoReal)}")
            appendLine()
            append("Por seguridad, esta sesión se cerrará ahora. El próximo turno debe iniciar con credenciales propias para mantener la trazabilidad de caja.")
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Turno cerrado correctamente")
            .setMessage(detalleCierre)
            .setCancelable(false)
            .setNeutralButton("Compartir PDF") { _, _ ->
                compartirPdfCierre(
                    montoApertura = montoApertura,
                    totalVentas = totalVentas,
                    totalEgresos = totalEgresos,
                    efectivoEsperado = efectivoEsperado,
                    efectivoReal = efectivoReal,
                    estadoCuadre = estadoCuadre,
                    diferencia = diferencia,
                    observacionCierre = observacionCierre,
                    idTurnoCerrado = idTurnoCerrado,
                    fechaTurnoCerrado = fechaTurnoCerrado,
                    idCajaOperativa = idCajaOperativa
                )
                cerrarSesionYCargarLogin()
            }
            .setPositiveButton("Continuar") { _, _ ->
                cerrarSesionYCargarLogin()
            }
            .show()
    }

    private fun compartirPdfCierre(
        montoApertura: Double,
        totalVentas: Double,
        totalEgresos: Double,
        efectivoEsperado: Double,
        efectivoReal: Double,
        estadoCuadre: String,
        diferencia: Double,
        observacionCierre: String,
        idTurnoCerrado: String,
        fechaTurnoCerrado: String,
        idCajaOperativa: String
    ) {
        val ctx = context ?: return
        val turnoRef = database.reference
            .child("CorteCaja/Cajeras/$idCajaOperativa/$fechaTurnoCerrado/turnos/$idTurnoCerrado")

        turnoRef.get().addOnSuccessListener { snapshot ->
            if (!isAdded) return@addOnSuccessListener

            // Leer campos adicionales del turno
            val horaApertura     = snapshot.child("horaAperturaLocal").value?.toString()
                ?: snapshot.child("horaApertura").value?.toString()
                ?: ""
            val horaCierre       = snapshot.child("horaCierre").value?.toString().orEmpty()
            val ventasEfectivo   = snapshot.child("ventasEfectivo").value?.toString()?.toDoubleOrNull() ?: 0.0
            val totalDevoluciones= snapshot.child("totalDevoluciones").value?.toString()?.toDoubleOrNull() ?: 0.0
        val nombreCaja       = obtenerNombreCajaOperativa().ifBlank { "Caja" }

            // Desglose por método de pago
            val resumenMetodos = mutableListOf<Pair<String, Double>>()
            val resumenSnap = snapshot.child("resumenMetodos")
            for (metodoSnap in resumenSnap.children) {
                val nombre = metodoSnap.child("titulo").value?.toString()
                    ?: metodoSnap.child("nombre").value?.toString()
                    ?: metodoSnap.key.orEmpty()
                val monto  = metodoSnap.child("monto").value?.toString()?.toDoubleOrNull() ?: 0.0
                if (monto > 0.01) resumenMetodos.add(Pair(nombre, monto))
            }

            val resumenDiferenciasCobradas = mutableListOf<Pair<String, Double>>()
            val resumenDiferenciasSnap = snapshot.child("resumenDiferenciasCobradas")
            for (metodoSnap in resumenDiferenciasSnap.children) {
                val nombre = metodoSnap.child("titulo").value?.toString()
                    ?: metodoSnap.key.orEmpty()
                val monto = metodoSnap.child("monto").value?.toString()?.toDoubleOrNull() ?: 0.0
                if (monto > 0.01) resumenDiferenciasCobradas.add(Pair(nombre, monto))
            }
            val totalDiferenciasCobradas = snapshot.child("totalDiferenciasCobradas")
                .value?.toString()?.toDoubleOrNull()
                ?: resumenDiferenciasCobradas.sumOf { it.second }

            val datos = CierreTurnoPdfGenerator.DatosCierre(
                nombreCaja        = nombreCaja,
                nombreCajero      = nombreCajera,
                fecha             = fechaTurnoCerrado,
                horaApertura      = horaApertura,
                horaCierre        = horaCierre,
                montoApertura     = montoApertura,
                totalVentas       = totalVentas,
                ventasEfectivo    = ventasEfectivo,
                totalEgresos      = totalEgresos,
                totalDevoluciones = totalDevoluciones,
                efectivoEsperado  = efectivoEsperado,
                efectivoReal      = efectivoReal,
                diferencia        = diferencia,
                estadoCuadre      = estadoCuadre,
                observacion       = observacionCierre,
                resumenMetodos    = resumenMetodos,
                totalDiferenciasCobradas = totalDiferenciasCobradas,
                resumenDiferenciasCobradas = resumenDiferenciasCobradas
            )

            try {
                val file = CierreTurnoPdfGenerator.generar(ctx, datos)
                val uri  = FileProvider.getUriForFile(
                    ctx, "${ctx.packageName}.provider", file
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        "Cierre de turno · $nombreCaja · $fechaTurnoCerrado")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(intent, "Compartir reporte PDF"))
            } catch (e: Exception) {
                mostrarToastSeguro("No se pudo generar el PDF: ${e.message}")
            }
        }.addOnFailureListener {
            mostrarToastSeguro("No se pudo leer el turno para el PDF")
        }
    }

    private fun cerrarSesionYCargarLogin() {
        val ctx = context ?: return
        val idUsuario = SessionManager.idCajera
        if (idUsuario.isNotBlank()) {
            SesionUnicaManager.cerrarSesionActual(ctx, idUsuario)
        }
        SessionManager.limpiarSesion(ctx)

        val intent = Intent(ctx, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        activity?.finish()
    }

    private fun adjuntarStockCriticoAlTurno(turnoRef: DatabaseReference) {
        database.getReference("Inventario")
            .child("Productos")
            .get()
            .addOnSuccessListener { snapshot ->
                val productosCriticos = hashMapOf<String, Any>()
                var cantidadCriticos = 0
                var cantidadAgotados = 0

                for (child in snapshot.children) {
                    val producto = child.toMoldeProductoSeguro() ?: continue
                    val stockActual = producto.cantidadinicial.toIntOrNull() ?: continue

                    if (stockActual < UMBRAL_STOCK_CRITICO_CIERRE) {
                        val clave = child.key ?: producto.indice.ifBlank { UUID.randomUUID().toString() }
                        productosCriticos[clave] = mapOf(
                            "indiceProducto" to (producto.indice.ifBlank { clave }),
                            "nombre" to producto.nombre,
                            "categoria" to producto.categoria,
                            "stockActual" to stockActual.toString(),
                            "unidadBase" to producto.unidadbase,
                            "agotado" to (stockActual <= 0)
                        )
                        cantidadCriticos++
                        if (stockActual <= 0) {
                            cantidadAgotados++
                        }
                    }
                }

                val updates = hashMapOf<String, Any>(
                    "stockCriticoCierre/umbral" to UMBRAL_STOCK_CRITICO_CIERRE,
                    "stockCriticoCierre/cantidadProductos" to cantidadCriticos,
                    "stockCriticoCierre/cantidadAgotados" to cantidadAgotados,
                    "stockCriticoCierre/timestamp" to System.currentTimeMillis()
                )

                if (productosCriticos.isNotEmpty()) {
                    updates["stockCriticoCierre/productos"] = productosCriticos
                } else {
                    updates["stockCriticoCierre/productos"] = emptyMap<String, Any>()
                }

                turnoRef.updateChildren(updates)
            }
            .addOnFailureListener { e ->
                turnoRef.child("stockCriticoCierre")
                    .child("error")
                    .setValue(e.message ?: "No se pudo consultar inventario al cierre")
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFragmentoCajaBinding.inflate(inflater, container, false)
        feedbackVm.precalentar()
        mostrarLoadingInicialCaja()

        cargarDatosSesion()
        configurarRecyclerCaja()
        configurarToolbarCaja()
        configurarLayoutTurnos()
        configurarAccionesVentaEspera()
        configurarBotonCobrar()
        configurarComprobante()
        precargarMetodosPagoActivos()

        irListaProductos()
        prepararVerificacionTurnoSinOverlay(mostrarEstadoVerificando = false)

        // 2. Aplicar estado inicial según dispositivo
        if (esTablet()) {
            configurarModoTablet()
        } else {
            aplicarLayoutInicialMobile()
        }

        configurarOcultarTecladoAlTocarFuera()

        // 3. Manejar botón ATRÁS del sistema para no salir de la app accidentalmente
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!manejarRetrocesoInterno()) {
                    // Si no hay nada interno que manejar, dejamos que la actividad lo maneje
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })

        return binding.root
    }

    fun manejarRetrocesoInterno(): Boolean {
        val b = _binding ?: return false
        val dialogoCobroVisible = dialogoCobroActual?.isShowing == true
        val flujoCobroVisible = b.layoutCobroPanel.visibility == View.VISIBLE
        val checkoutVisible = b.layoutComprobante.visibility == View.VISIBLE
        val exitoVisible = b.layoutVentaExito?.visibility == View.VISIBLE
        val aperturaVisible = b.layoutAperturaCaja.visibility == View.VISIBLE
        val cierreVisible = obtenerLayoutCierreActual()?.visibility == View.VISIBLE

        return when {
            cierreVisible -> {
                if (cierreObligatorioActivo()) {
                    val mensaje = if (rescateRequiereCierreObligatorio()) {
                        "Debes cerrar el turno rescatado para continuar"
                    } else {
                        "Debes completar el cierre pendiente para continuar"
                    }
                    mostrarToastSeguro(mensaje)
                } else {
                    ocultarLayoutCierreCaja()
                }
                true
            }

            dialogoCobroVisible -> {
                dialogoCobroActual?.dismiss()
                true
            }

            flujoCobroVisible && pasoCobroActual == 3 -> {
                if (metodosPagoCobroActivos.size == 1) {
                    liberarCajaSiEstabaBloqueada()
                    reiniciarFlujoCobroInline()
                } else {
                    irAPaso(2)
                }
                true
            }

            flujoCobroVisible && pasoCobroActual == 2 -> {
                liberarCajaSiEstabaBloqueada()
                reiniciarFlujoCobroInline()
                true
            }

            aperturaVisible -> {
                mostrarToastSeguro("Debes abrir la caja para continuar")
                true
            }

            checkoutVisible -> {
                liberarCajaSiEstabaBloqueada()
                if (esTablet()) aplicarLayoutInicialTablet() else ocultarLayoutComprobanteMobile()
                true
            }

            exitoVisible -> {
                b.layoutVentaExito?.visibility = View.GONE
                if (esTablet()) aplicarLayoutInicialTablet() else aplicarLayoutInicialMobile()
                true
            }

            modoBusquedaActivo -> {
                val viewRaiz = b.root
                val insets = ViewCompat.getRootWindowInsets(viewRaiz)
                val tecladoVisible = insets?.isVisible(WindowInsetsCompat.Type.ime()) == true

                if (tecladoVisible) {
                    // Si el teclado está abierto, lo ocultamos y quitamos el foco
                    ocultarTecladoYLiberarFocoBusqueda()
                } else {
                    // Si el teclado ya está oculto (por gesto o por el toque anterior), salimos
                    salirModoBusqueda()
                }
                true
            }

            else -> false
        }
    }

    private fun configurarOcultarTecladoAlTocarFuera() {
        binding.root.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // Durante la búsqueda, el Scrim (fondo) ya se encarga del cierre.
                // Quitamos la lógica de este listener para evitar cierres accidentales al tocar productos.
                if (!modoBusquedaActivo) {
                    val view = activity?.currentFocus
                    if (view is EditText) {
                        val outRect = android.graphics.Rect()
                        view.getGlobalVisibleRect(outRect)
                        if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                            view.clearFocus()
                            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                            imm?.hideSoftInputFromWindow(view.windowToken, 0)
                        }
                    }
                }
            }
            false
        }
    }

    private fun ocultarTecladoYLiberarFocoBusqueda() {
        val b = _binding ?: return
        val buscadorMobile = b.buscar
        val buscadorTablet = b.root.findViewById<TextInputEditText>(R.id.buscarProductos)
        val vistaConFoco = activity?.currentFocus ?: buscadorMobile ?: buscadorTablet ?: b.root

        buscadorMobile?.clearFocus()
        buscadorTablet?.clearFocus()
        vistaConFoco.clearFocus()

        val destinoFoco = b.root.findViewById<View>(R.id.recyclerResultadosInline)
            ?: b.root.findViewById<View>(R.id.recyclerProductos)
            ?: b.recyclerView2
        destinoFoco.requestFocus()

        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(vistaConFoco.windowToken, 0)
    }

    override fun onDestroyView() {
        if (soyYoElQueCobra) {
            liberarCajaSiEstabaBloqueada()
            soyYoElQueCobra = false
        }

        salirModoBusqueda()
        restaurarContextoLocalCajaAlSalir()
        detenerSincronizacionCajaEnUsoCuenta()
        dialogoRegistrarEgresoActual?.dismiss()
        dialogoRegistrarEgresoActual = null
        dialogoOpcionesComprobanteEgresoActual?.dismiss()
        dialogoOpcionesComprobanteEgresoActual = null
        dialogoConfirmacionEgresoActual?.dismiss()
        dialogoConfirmacionEgresoActual = null
        dialogoVentasEnEsperaActual?.dismiss()
        dialogoVentasEnEsperaActual = null
        dialogoOpcionesVentaEnEsperaActual?.dismiss()
        dialogoOpcionesVentaEnEsperaActual = null
        dialogoNotaVentaEnEsperaActual?.dismiss()
        dialogoNotaVentaEnEsperaActual = null
        dialogoSelectorCajerasActual?.dismiss()
        dialogoSelectorCajerasActual = null
        dialogoCambioPresentacionActual?.dismiss()
        dialogoCambioPresentacionActual = null
        dialogoDescuentoActual?.dismiss()
        dialogoDescuentoActual = null
        dialogoSalidaSupervisionActual?.dismiss()
        dialogoSalidaSupervisionActual = null
        dialogoCobroActual?.dismiss()
        dialogoCobroActual = null
        dialogoDevolucionActual?.dismiss()
        dialogoDevolucionActual = null
        dialogoResolucionStockActual?.dismiss()
        dialogoResolucionStockActual = null
        onProductoSustitutoSeleccionadoDevolucion = null
        dialogErrorHoraServidor?.dismiss()
        dialogErrorHoraServidor = null
        tokenSubidaComprobanteEgreso++
        comprobanteEgresoUri = null
        comprobanteEgresoCameraUri = null
        comprobanteEgresoPreview = null
        comprobanteEgresoHint = null
        comprobanteEgresoHintContainer = null
        comprobanteEgresoQuitar = null
        detenerRadaresStock()
        connectionListener?.let { connectionRef?.removeEventListener(it) }
        connectionListener = null
        connectionRef = null
        // #9: cleanup listener total turno
        totalTurnoListener?.let { totalTurnoRef?.removeEventListener(it) }
        totalTurnoRef = null
        totalTurnoListener = null
        limpiarTodosLosListeners()

        adapterCategorias = null
        adapterProductos = null
        adapterChipsInline = null
        adapterResultadosInline = null
        listaResultadosInline.clear()
        modoBusquedaActivo = false
        busquedaInlineConfigurada = false
        ocultarDialogoProgresoVenta()
        ocultarDialogoProgresoVentaEnEspera()
        ocultarLoadingOverlayCaja()
        dialogoProgresoVenta = null
        dialogoProgresoVentaEnEspera = null

        runnableBusquedaDocumento?.let { handlerBusquedaDocumento.removeCallbacks(it) }
        runnableBusquedaDocumento = null
        runnableBusquedaProductosTablet?.let { handlerSeguridadOperaciones.removeCallbacks(it) }
        runnableBusquedaProductosTablet = null
        runnableBusquedaProductosInline?.let { handlerSeguridadOperaciones.removeCallbacks(it) }
        runnableBusquedaProductosInline = null
        ultimoDocumentoBuscado = ""
        runnableResetActualizandoCaja?.let { handlerSeguridadOperaciones.removeCallbacks(it) }
        runnableResetActualizandoCaja = null
        runnableResetVentaEnProceso?.let { handlerSeguridadOperaciones.removeCallbacks(it) }
        runnableResetVentaEnProceso = null
        runnableResetLoadingInicialCaja?.let { handlerSeguridadOperaciones.removeCallbacks(it) }
        runnableResetLoadingInicialCaja = null
        runnableVerificarTurno?.let { handlerSeguridadOperaciones.removeCallbacks(it) }
        runnableVerificarTurno = null
        runnableOcultarSplashPresenciaSupervision?.let { handlerSeguridadOperaciones.removeCallbacks(it) }
        runnableOcultarSplashPresenciaSupervision = null
        runnableTimeoutLoadingGenerico?.let { handlerSeguridadOperaciones.removeCallbacks(it) }
        runnableTimeoutLoadingGenerico = null
        runnableTimeoutCierreTurno?.let { handlerSeguridadOperaciones.removeCallbacks(it) }
        runnableTimeoutCierreTurno = null

        handlerTimeoutValidacion.removeCallbacksAndMessages(null)
        handlerMontoRecibido.removeCallbacksAndMessages(null)

        // FIX: Cancela todas las tareas de fondo de este handler (incluyendo "grace_period")
        handlerSeguridadOperaciones.removeCallbacksAndMessages(null)

        actualizandoCaja = false
        ventaEnProceso = false
        loadingInicialCajaActivo = false
        verificacionTurnoEnCurso = false
        verificacionTurnoPendiente = false
        supervisionPresenciaInicializada = false
        ultimaClavePresenciaSupervision = ""

        timeChangeReceiver?.let {
            try {
                requireContext().unregisterReceiver(it)
            } catch (e: Exception) {
                Log.e("CajaReloj", "Error al desregistrar receiver: ${e.message}")
            }
        }
        timeChangeReceiver = null

        activity?.findViewById<ViewGroup>(android.R.id.content)?.let { decor ->
            val snapshotsPendientes = mutableListOf<View>()
            for (i in 0 until decor.childCount) {
                val hijo = decor.getChildAt(i)
                if (hijo.tag == "snapshot_vuelo_caja") {
                    snapshotsPendientes.add(hijo)
                }
            }
            // Los removemos de forma segura fuera del bucle de inspección
            snapshotsPendientes.forEach { vista ->
                runCatching { decor.removeView(vista) }
            }
        }

        _binding = null

        super.onDestroyView()
    }

    private fun esTablet(): Boolean {
        return resources.configuration.smallestScreenWidthDp >= 720
    }

    private fun actualizarProductosFiltradosTablet(productos: List<MoldeProductos>) {
        listaProductosTablet.clear()
        listaProductosTablet.addAll(productos)
        adapterProductos?.actualizarProductos(listaProductosTablet.toList())
    }

    private fun actualizarResultadosBusquedaInline(productos: List<MoldeProductos>) {
        listaResultadosInline.clear()
        listaResultadosInline.addAll(productos)
        val query = _binding?.buscar?.text?.toString().orEmpty()
        adapterResultadosInline?.actualizarProductos(listaResultadosInline.toList(), query)
    }

    private fun programarFiltroTablet(textoBusqueda: String) {
        runnableBusquedaProductosTablet?.let { handlerSeguridadOperaciones.removeCallbacks(it) }
        val runnable = Runnable {
            if (!isAdded || _binding == null) return@Runnable
            aplicarFiltrosTablet(textoBusqueda)
        }
        runnableBusquedaProductosTablet = runnable
        handlerSeguridadOperaciones.postDelayed(runnable, DEBOUNCE_BUSQUEDA_PRODUCTOS_MS)
    }

    private fun programarFiltroBusquedaInline(textoBusqueda: String) {
        runnableBusquedaProductosInline?.let { handlerSeguridadOperaciones.removeCallbacks(it) }
        val runnable = Runnable {
            val b = _binding ?: return@Runnable // CRÍTICO: Evita NPE tras el delay de 250ms
            aplicarFiltrosBusquedaInline(textoBusqueda)
        }
        runnableBusquedaProductosInline = runnable
        handlerSeguridadOperaciones.postDelayed(runnable, 250L)
    }

    private fun construirUnidadesTotalesPorProductoEnCaja(): Map<String, Int> {
        return listaProductosCaja
            .groupBy { it.indiceProducto.trim() }
            .mapValues { (_, productos) ->
                productos.sumOf { item ->
                    val cantidad = item.cantidad.toIntSeguro(0)
                    val unidades = item.unidadesPorPresentacion.toIntSeguro(1)
                    cantidad * unidades
                }
            }
    }

    private fun sincronizarUnidadesTotalesCarrito() {
        if (!::adapter.isInitialized) return
        adapter.actualizarUnidadesTotalesPorProducto(construirUnidadesTotalesPorProductoEnCaja())
    }

    private fun refrescarCarritoCompletoUi() {
        if (!::adapter.isInitialized) return
        sincronizarUnidadesTotalesCarrito()
        adapter.notifyDataSetChanged()
    }

    private fun refrescarItemsCarrito(indices: Collection<Int>) {
        if (!::adapter.isInitialized) return
        sincronizarUnidadesTotalesCarrito()
        val posicionesValidas = indices
            .distinct()
            .filter { it in listaProductosCaja.indices }

        if (posicionesValidas.isEmpty()) {
            adapter.notifyDataSetChanged()
            return
        }

        posicionesValidas.forEach { pos -> adapter.notifyItemChanged(pos) }
    }

    private fun configurarModoTablet() {
        if (!isAdded || _binding == null) return
        configurarSeccionProductosTablet() // Exclusivo de tablet (solo UI)
        configurarComprobante()           // Lógica unificada
    }


    private fun aplicarLayoutInicialTablet() {
        val currentBinding = _binding ?: return
        modoCobroTablet = false
        aplicarRestriccionesInicialesTablet()

        // En Tablet, el estado inicial depende de si hay productos
        val hayProductos = listaProductosCaja.isNotEmpty()
        val hayVentasEnEspera = cantidadVentasEnEspera > 0
        val mostrarPanelCaja = hayProductos || hayVentasEnEspera

        currentBinding.layoutProductos?.visibility = View.VISIBLE
        // Modo POS 2-paneles con tabs: arrancamos en tab Carrito (layoutCaja visible).
        currentBinding.layoutCaja?.visibility = View.VISIBLE
        currentBinding.layoutComprobante.visibility = View.GONE
        currentBinding.layoutAperturaCaja.visibility = View.GONE
        currentBinding.layoutVentaExito?.visibility = View.GONE

        val recyclerProductos = currentBinding.root.findViewById<RecyclerView>(R.id.recyclerProductos)
        (recyclerProductos?.layoutManager as? GridLayoutManager)?.spanCount = 4
        configurarTabsPanelDerechoTablet()

        // Aseguramos que los elementos internos de la caja tengan visibilidad correcta
        currentBinding.recyclerView2.visibility = if (hayProductos) View.VISIBLE else View.GONE
        currentBinding.cardResumenCaja.visibility = if (mostrarPanelCaja) View.VISIBLE else View.GONE
        // Cobro rapido: boton verde "Cobrar Bs X.XX" abre directo el pago (boleta sin datos).
        currentBinding.buttoncobrar.visibility = if (hayProductos) View.VISIBLE else View.GONE
        adapterProductos?.notifyDataSetChanged()
        actualizarAccionesVentaEsperaUi()
        actualizarAyudaCajaVaciaUi()
        limpiarFormularioComprobanteGlobal()
        actualizarSeparadoresTablet()
        actualizarStepperTablet()
        // Volver al tab Carrito al refrescar/cancelar
        currentBinding.root.findViewById<com.google.android.material.tabs.TabLayout>(
            R.id.tabLayoutPanelDerechoTablet
        )?.getTabAt(0)?.select()
    }

    private fun mostrarModoCobroTablet() {
        val currentBinding = _binding ?: return
        modoCobroTablet = true
        currentBinding.layoutProductos?.visibility = View.VISIBLE
        // Cambio directo: oculta carrito y muestra comprobante en el panel derecho.
        cambiarTabPanelDerechoTablet(mostrarCobro = true)
        seleccionarTabCobroTablet()

        actualizarAccionesVentaEsperaUi()
        actualizarAyudaCajaVaciaUi()
        actualizarSeparadoresTablet()
        actualizarStepperTablet()
    }

    private fun actualizarStepperTablet() {
        if (!esTablet()) return
        val b = _binding ?: return
        // Diseno POS sin stepper: los 3 paneles siempre visibles, los steppers ocultos.
        b.root.findViewById<View>(R.id.layoutStepperProductosTablet)?.visibility = View.GONE
    }

    private fun configurarTabsPanelDerechoTablet() {
        if (!esTablet()) return
        val b = _binding ?: return
        val tabs = b.root.findViewById<com.google.android.material.tabs.TabLayout>(R.id.tabLayoutPanelDerechoTablet)
            ?: return
        if (tabs.tag == "configurado") return
        tabs.tag = "configurado"
        tabs.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                val mostrarCobro = tab.position == 1
                cambiarTabPanelDerechoTablet(mostrarCobro)
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab) = Unit
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab) = Unit
        })
    }

    private fun cambiarTabPanelDerechoTablet(mostrarCobro: Boolean) {
        val b = _binding ?: return
        if (mostrarCobro) {
            b.layoutCaja?.visibility = View.GONE
            b.layoutComprobante.visibility = View.VISIBLE
        } else {
            b.layoutCaja?.visibility = View.VISIBLE
            b.layoutComprobante.visibility = View.GONE
        }
    }

    private fun seleccionarTabCobroTablet() {
        val tabs = _binding?.root?.findViewById<com.google.android.material.tabs.TabLayout>(
            R.id.tabLayoutPanelDerechoTablet
        ) ?: return
        tabs.getTabAt(1)?.select()
    }

    private fun aplicarRestriccionesInicialesTablet() {
        if (!esTablet()) return
        val root = _binding?.root as? ConstraintLayout ?: return
        val set = ConstraintSet()
        set.clone(root)

        set.clear(R.id.layoutProductos, ConstraintSet.END)
        set.clear(R.id.layoutCaja, ConstraintSet.START)
        set.clear(R.id.layoutCaja, ConstraintSet.END)
        set.clear(R.id.layoutComprobante, ConstraintSet.START)
        set.clear(R.id.layoutComprobante, ConstraintSet.END)

        // Modo POS 2-paneles: Productos (70%) | Panel derecho con tabs (30%)
        set.connect(R.id.layoutProductos, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        set.connect(R.id.layoutProductos, ConstraintSet.END, R.id.guidePanelDerechoTablet, ConstraintSet.START)
        set.connect(R.id.layoutCaja, ConstraintSet.START, R.id.guidePanelDerechoTablet, ConstraintSet.START)
        set.connect(R.id.layoutCaja, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        set.connect(R.id.layoutComprobante, ConstraintSet.START, R.id.guidePanelDerechoTablet, ConstraintSet.START)
        set.connect(R.id.layoutComprobante, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        set.applyTo(root)
    }

    private fun aplicarRestriccionesCheckoutTablet() {
        // Modo POS 3-paneles: usa siempre la misma distribucion (Productos | Caja | Comprobante).
        aplicarRestriccionesInicialesTablet()
    }

    private fun actualizarSeparadoresTablet() {
        if (!esTablet()) return
        val layoutProductos = buscarVistaPorNombre("layoutProductos")
        val layoutCaja = buscarVistaPorNombre("layoutCaja")
        val layoutComprobante = buscarVistaPorNombre("layoutComprobante")
        val dividerProductosCaja = buscarVistaPorNombre("dividerProductosCajaTablet")
        val dividerCajaComprobante = buscarVistaPorNombre("dividerCajaComprobanteTablet")

        val mostrarProductosCaja =
            layoutProductos?.visibility == View.VISIBLE && layoutCaja?.visibility == View.VISIBLE
        val mostrarCajaComprobante =
            layoutCaja?.visibility == View.VISIBLE && layoutComprobante?.visibility == View.VISIBLE

        dividerProductosCaja?.visibility = if (mostrarProductosCaja) View.VISIBLE else View.GONE
        dividerCajaComprobante?.visibility = if (mostrarCajaComprobante) View.VISIBLE else View.GONE
    }


    private fun puedoInteractuarConCarrito(): Boolean {
        return !actualizandoCaja && !ventaEnProceso && !loadingOperacionCajaActivo
    }

    private fun configurarRecyclerCaja() {
        adapter = AdapterRecyclerProductosCaja(
            listaProductosCaja = listaProductosCaja,
            cacheStockInventario = cacheStockInventario,
            onSumarClick = { producto, view ->
                if (puedoInteractuarConCarrito()) {
                    feedbackVm.notificarProductoAgregado(view)
                    sumarProducto(producto)
                }
            },
            onRestarClick = { producto, view ->
                if (puedoInteractuarConCarrito()) {
                    feedbackVm.notificarProductoRestado(view)
                    restarProducto(producto)
                }
            },
            onEliminarClick = { producto, view ->
                if (puedoInteractuarConCarrito()) {
                    feedbackVm.notificarAccionDestructiva(view)
                    eliminarProducto(producto)
                }
            },
            onProductoClick = { _ ->
                // Tap en la card del item del carrito desactivado
            },
            onDescuentoClick = { producto ->
                if (puedoInteractuarConCarrito()) {
                    mostrarDialogoDescuento(producto)
                }
            }
        )

        binding.recyclerView2.layoutManager = LinearLayoutManager(requireActivity())
        binding.recyclerView2.adapter = adapter
        sincronizarUnidadesTotalesCarrito()

        // Registrar escucha de cambios en el reloj del sistema
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_CHANGED)      // Se dispara al cambiar la hora manualmente
            addAction(Intent.ACTION_TIMEZONE_CHANGED)  // Se dispara al cambiar la zona horaria
        }

        timeChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // Ejecutamos una verificación inmediata
                verificarTurnoActivo()
            }
        }
        requireContext().registerReceiver(timeChangeReceiver, intentFilter)

        // -- Fix #5: Swipe izquierdo para eliminar producto del carrito -----------
        val swipeToDelete = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.bindingAdapterPosition
                val b = _binding ?: return
                if (pos < 0 || pos >= listaProductosCaja.size || !puedoInteractuarConCarrito()) {
                    if (pos >= 0) adapter.notifyItemChanged(pos)
                    return
                }
                val eliminado = listaProductosCaja[pos]
                feedbackVm.notificarAccionDestructiva(viewHolder.itemView)
                eliminarProducto(eliminado)
                snackbarEliminarProductoActivo?.dismiss()
                snackbarEliminarProductoActivo = Snackbar.make(
                    b.root,
                    "\"${eliminado.nombre.orEmpty()}\" eliminado del carrito",
                    Snackbar.LENGTH_LONG
                ).setAction("Deshacer") {
                    if (isAdded) restaurarProductoCaja(eliminado)
                }
                snackbarEliminarProductoActivo?.show()
            }

            override fun onChildDraw(c: android.graphics.Canvas, rv: RecyclerView, vh: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isActive: Boolean) {
                val itemView = vh.itemView
                if (dX < 0) {
                    val colorFondo = Color.parseColor("#FF5252") // Rojo Material vibrante
                    val bg = ColorDrawable(colorFondo)
                    bg.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                    bg.draw(c)

                    // Dibujar icono de papelera
                    val icon = ContextCompat.getDrawable(rv.context, R.drawable.deleteiconitem)
                    icon?.let {
                        val iconMargin = (itemView.height - it.intrinsicHeight) / 2
                        val iconTop = itemView.top + (itemView.height - it.intrinsicHeight) / 2
                        val iconBottom = iconTop + it.intrinsicHeight
                        val iconLeft = itemView.right - iconMargin - it.intrinsicWidth
                        val iconRight = itemView.right - iconMargin
                        it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        it.setTint(Color.WHITE)
                        it.draw(c)
                    }
                }
                super.onChildDraw(c, rv, vh, dX, dY, actionState, isActive)
            }
        }
        ItemTouchHelper(swipeToDelete).attachToRecyclerView(binding.recyclerView2)
    }

    /** Restaura un ProductoCaja previamente eliminado (usado para Undo del swipe). */
    private fun restaurarProductoCaja(producto: ProductoCaja) {
        if (!isAdded || producto.id.isBlank()) return
        if (bloquearOperacionPorRescateCajaBloqueada()) return
        getCajaRef().child("Productos").child(producto.id).setValue(producto)
    }

    private fun configurarSeccionProductosTablet() {
        val currentBinding = _binding ?: return

        val recyclerCategorias = currentBinding.root.findViewById<RecyclerView>(R.id.recyclerCategorias) ?: return
        val recyclerProductos = currentBinding.root.findViewById<RecyclerView>(R.id.recyclerProductos) ?: return
        val buscarProductos = currentBinding.root.findViewById<TextInputEditText>(R.id.buscarProductos) ?: return
        val btnFiltro = currentBinding.root.findViewById<View>(R.id.btnFiltro) ?: return

        recyclerCategorias.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        recyclerProductos.layoutManager = GridLayoutManager(requireContext(), 4)


        adapterCategorias = AdapterCategorias(
            listaCategorias = listOf("Todos"),
            onCategoriaClick = { categoriaSeleccionada ->
                if (!isAdded || _binding == null) return@AdapterCategorias

                categoriaSeleccionadaActualTablet = categoriaSeleccionada
                aplicarFiltrosTablet(buscarProductos.text?.toString().orEmpty())
            }
        )

        adapterProductos = AdapterProductosCatalogo(
            onProductoClick = { producto, anchor ->
                if (!isAdded || _binding == null || ventaEnProceso) return@AdapterProductosCatalogo
                if (manejarClickProductoTablet(producto, anchor)) {
                    feedbackVm.notificarProductoAgregado(anchor)
                    animarVueloAlCarrito(anchor)
                }
            }
        )

        recyclerCategorias.adapter = adapterCategorias
        recyclerProductos.adapter = adapterProductos

        buscarProductos.doAfterTextChanged { editable ->
            if (!isAdded || _binding == null) return@doAfterTextChanged
            programarFiltroTablet(editable?.toString().orEmpty())
        }

        btnFiltro.setOnClickListener {
            if (!isAdded || _binding == null) return@setOnClickListener
            mostrarToastSeguro("Aquí luego puedes abrir filtros")
        }

    }

    private fun cargarCategoriasTablet() {
        database.getReference("Inventario")
            .child("CategoriasInventario")
            .get()
            .addOnSuccessListener { snapshot ->
                if (!isAdded || _binding == null) return@addOnSuccessListener

                val categorias = mutableListOf<String>()

                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        val categoria = child.getValue(CategoriaProductos::class.java)
                        val nombreCategoria = categoria?.nombre?.trim().orEmpty()

                        if (nombreCategoria.isNotEmpty() && !categorias.contains(nombreCategoria)) {
                            categorias.add(nombreCategoria)
                        }
                    }
                }

                val currentBinding = _binding ?: return@addOnSuccessListener
                val buscarProductos = currentBinding.root.findViewById<TextInputEditText>(R.id.buscarProductos)
                val recyclerCategorias = currentBinding.root.findViewById<RecyclerView>(R.id.recyclerCategorias)

                adapterCategorias = AdapterCategorias(
                    listaCategorias = categorias,
                    onCategoriaClick = { categoriaSeleccionada ->
                        if (!isAdded || _binding == null) return@AdapterCategorias

                        categoriaSeleccionadaActualTablet = categoriaSeleccionada
                        aplicarFiltrosTablet(buscarProductos.text?.toString().orEmpty())
                    }
                )

                recyclerCategorias.adapter = adapterCategorias
            }
            .addOnFailureListener { e ->
                if (!isAdded || _binding == null) return@addOnFailureListener
                mostrarToastSeguro("Error al cargar categorías: ${e.message}")
            }
    }

    private fun obtenerProductosTablet() {
        inventarioProductosValueListener?.let { listener ->
            inventarioProductosRefListener?.removeEventListener(listener)
        }

        inventarioProductosRefListener = database.getReference("Inventario").child("Productos")

        inventarioProductosValueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || _binding == null) return

                listaOriginalProductosTablet.clear()
                listaProductosTablet.clear()

                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        val producto = child.toMoldeProductoSeguro()
                        if (producto != null) {
                            if (producto.indice.isBlank()) {
                                producto.indice = child.key ?: ""
                            }
                            listaOriginalProductosTablet.add(producto)
                        }
                    }
                }

                val currentBinding = _binding ?: return
                val buscarProductos =
                    currentBinding.root.findViewById<TextInputEditText>(R.id.buscarProductos)

                aplicarFiltrosTablet(
                    buscarProductos.text?.toString().orEmpty()
                )
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded || _binding == null) return
                mostrarToastSeguro("Error al cargar productos: ${error.message}")
            }
        }

        val listener = inventarioProductosValueListener ?: return
        inventarioProductosRefListener?.addValueEventListener(listener)
    }


    private fun aplicarFiltrosTablet(textoBusqueda: String) {
        if (!isAdded || _binding == null) return

        val tokens = normalizarTextoLibre(textoBusqueda).split(" ").filter { it.isNotBlank() }
        val hayBusqueda = tokens.isNotEmpty()
        val productosFiltrados = mutableListOf<MoldeProductos>()

        for (producto in listaOriginalProductosTablet) {
            val sinFiltroCategoria = categoriaSeleccionadaActualTablet.isBlank() ||
                    categoriaSeleccionadaActualTablet == "Todos"
            val coincideCategoria = sinFiltroCategoria ||
                    producto.categoria.equals(categoriaSeleccionadaActualTablet, ignoreCase = true)

            if (!coincideCategoria) continue

            // Producto agotado: solo aparece si el usuario lo busca explicitamente.
            val stockTotal = producto.cantidadinicial.trim().toIntOrNull() ?: 0
            val agotado = stockTotal <= 0
            if (agotado && !hayBusqueda) continue

            val nombreNorm = normalizarTextoLibre(producto.nombre)
            val catNorm = normalizarTextoLibre(producto.categoria)
            val codNorm = normalizarTextoLibre(producto.codigo)
            val presentacionesNorm = producto.presentaciones.map { normalizarTextoLibre(it.nombre) }

            val coincideBusqueda = !hayBusqueda || tokens.all { token ->
                nombreNorm.contains(token) ||
                catNorm.contains(token) ||
                codNorm.contains(token) ||
                presentacionesNorm.any { it.contains(token) }
            }

            if (coincideBusqueda) {
                productosFiltrados.add(producto)
            }
        }

        actualizarProductosFiltradosTablet(productosFiltrados)
    }

    private fun manejarClickProductoTablet(producto: MoldeProductos, anchor: View? = null): Boolean {
        if (!puedeOperarTurnoActual()) return false
        ocultarTecladoYLiberarFocoBusqueda()
        // Selector inline (popup anclado a la tarjeta del producto) en movil y tablet.
        // Si no hay anchor (caso defensivo), agregamos la presentacion principal directo.
        if (debeMostrarSelectorPresentacion(producto) && anchor != null) {
            mostrarPopupPresentacionesTablet(producto, anchor)
            return false
        } else {
            agregarProductoDirectoTablet(producto)
            return true
        }
    }

    private fun mostrarPopupPresentacionesTablet(producto: MoldeProductos, anchor: View) {
        if (!puedeOperarTurnoActual()) return
        val context = context ?: return

        val popupView = layoutInflater.inflate(R.layout.popup_presentaciones_caja, null)
        val tvNombre = popupView.findViewById<TextView>(R.id.tvPopupPresentacionesNombre)
        val container = popupView.findViewById<LinearLayout>(R.id.llPopupPresentacionesItems)
        tvNombre.text = producto.nombre.ifBlank { "Producto" }

        val popup = android.widget.PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popup.elevation = 14f
        popup.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        popup.isOutsideTouchable = true
        popup.isFocusable = true

        val unidadBase = producto.unidadbase.ifEmpty { "unidades" }
        val stockTotal = producto.cantidadinicial.toIntOrNull() ?: 0
        // Stock REAL disponible = stock total menos lo que ya esta en el carrito.
        // Sin restar el carrito, una presentacion que "tecnicamente alcanza por stock total"
        // se mostraria como disponible aunque al tap fallaria la validacion de agregar.
        val claveInventarioProducto = producto.indice.trim()
        val unidadesYaEnCarrito = listaProductosCaja
            .filter { it.indiceProducto.trim() == claveInventarioProducto }
            .sumOf { (it.cantidad.toIntOrNull() ?: 0) * (it.unidadesPorPresentacion.toIntOrNull() ?: 1) }
        val stockRestanteReal = (stockTotal - unidadesYaEnCarrito).coerceAtLeast(0)

        val rojoError = android.graphics.Color.parseColor("#DC2626")
        val rojoFondoSuave = android.graphics.Color.parseColor("#1FDC2626")
        val grisRowDisabled = android.graphics.Color.parseColor("#94A3B8")

        producto.presentaciones.forEach { presentacion ->
            val row = layoutInflater.inflate(R.layout.popup_presentaciones_caja_row, container, false)
            val tvNombreRow = row.findViewById<TextView>(R.id.tvPopupRowNombre)
            val tvDetalleRow = row.findViewById<TextView>(R.id.tvPopupRowDetalle)
            val tvPrecioRow = row.findViewById<TextView>(R.id.tvPopupRowPrecio)

            val cantidadUnid = if (presentacion.cantidad <= 0) 1 else presentacion.cantidad
            val agotada = stockRestanteReal < cantidadUnid
            val nombreVisible = PresentacionesTiendaConfigManager
                .nombreVisible(presentacion.nombre.ifBlank { "Unidad" })

            tvNombreRow.text = PresentacionHelper.capitalizarNombre(nombreVisible)
            tvDetalleRow.text = when {
                stockRestanteReal <= 0 -> "Sin stock disponible"
                agotada -> "No alcanza · solo $stockRestanteReal $unidadBase libres"
                else -> "$cantidadUnid $unidadBase"
            }
            tvPrecioRow.text = MonedaHelper.formatear(presentacion.precioventa)

            if (agotada) {
                // Visual rojo: nombre + detalle en rojo, precio gris (no resaltar lo que no se vende),
                // fondo rojo muy suave para destacar.
                row.setBackgroundColor(rojoFondoSuave)
                tvNombreRow.setTextColor(rojoError)
                tvDetalleRow.setTextColor(rojoError)
                tvPrecioRow.setTextColor(grisRowDisabled)
                row.setOnClickListener {
                    // Tap permitido pero sin agregar: feedback de error claro al cajero.
                    feedbackVm.notificarError(anchor)
                    mostrarToastSeguro(
                        if (stockRestanteReal <= 0)
                            "Ya no hay stock libre de este producto"
                        else
                            "Esta presentación necesita $cantidadUnid $unidadBase y solo quedan $stockRestanteReal libres"
                    )
                }
            } else {
                row.setOnClickListener {
                    // Cerramos el popup primero (la view se detached). El feedback decide segun
                    // el resultado real de agregar: exito -> ding + haptica + vuelo; rechazo -> error.
                    popup.dismiss()
                    agregarProductoDirectoTablet(producto, presentacion) { agregado ->
                        if (agregado) {
                            feedbackVm.notificarProductoAgregado(anchor)
                            animarVueloAlCarrito(anchor)
                        } else {
                            feedbackVm.notificarError(anchor)
                        }
                    }
                }
            }
            container.addView(row)
        }

        // Mostrar anclado a la tarjeta del producto
        val location = IntArray(2)
        anchor.getLocationInWindow(location)
        popup.showAsDropDown(anchor, 0, 8)
    }

    private fun debeMostrarSelectorPresentacion(producto: MoldeProductos): Boolean {
        return producto.tienePresentaciones && producto.presentaciones.size > 1
    }

    private fun cargarDatosSesion() {
        if (!isAdded) return
        SessionManager.cargarSesion(requireContext())
        SessionManager.asegurarCajaPropiaSiCorresponde(requireContext())
        this.idCajera = SessionManager.idCajera
        this.nombreCajera = SessionManager.nombreCajera

        actualizarInterfazSupervision()
    }

    private fun obtenerIdCajaOperativa(): String = SessionManager.idCajeraEnUso.ifBlank { idCajera }

    private fun obtenerNombreCajaOperativa(): String = SessionManager.nombreCajeraEnUso.ifBlank { nombreCajera }

    private fun estaEnModoSupervision(): Boolean {
        val idCajaEnUso = obtenerIdCajaOperativa()
        return idCajaEnUso.isNotBlank() && idCajaEnUso != idCajera
    }

    private fun estaEnModoRescateCajaBloqueada(): Boolean {
        if (!modoRescateCajaBloqueadaActivo) return false
        val idCajaEnUso = obtenerIdCajaOperativa().trim()
        return idCajaRescateBloqueada.isNotBlank() && idCajaEnUso == idCajaRescateBloqueada
    }

    private fun activarModoRescateCajaBloqueada(
        idCaja: String,
        nombreCaja: String,
        nombreCuentaBloqueada: String
    ) {
        modoRescateCajaBloqueadaActivo = true
        idCajaRescateBloqueada = idCaja.trim()
        nombreCajaRescateBloqueada = nombreCaja.trim()
        nombreCuentaRescateBloqueada = nombreCuentaBloqueada.trim()
        ultimoAvisoModoRescateMs = 0L
        cerrarInteraccionesSensiblesPorControlAsistido()
        _binding?.layoutComprobante?.visibility = View.GONE
    }

    private fun limpiarModoRescateCajaBloqueada() {
        modoRescateCajaBloqueadaActivo = false
        idCajaRescateBloqueada = ""
        nombreCajaRescateBloqueada = ""
        nombreCuentaRescateBloqueada = ""
        ultimoAvisoModoRescateMs = 0L
    }

    private fun mostrarAvisoModoRescateCajaBloqueada() {
        val ahora = System.currentTimeMillis()
        if (ahora - ultimoAvisoModoRescateMs < 1200L) return
        ultimoAvisoModoRescateMs = ahora
        mostrarToastSeguro("Esta cuenta esta bloqueada. Solo puedes cerrar el turno abierto.")
    }

    private fun bloquearOperacionPorRescateCajaBloqueada(notificar: Boolean = true): Boolean {
        if (!estaEnModoRescateCajaBloqueada()) return false
        if (notificar) mostrarAvisoModoRescateCajaBloqueada()
        return true
    }

    private fun volverAMiCajaDesdeSupervision(mensaje: String? = null) {
        limpiarSupervisionPublicada()
        limpiarModoRescateCajaBloqueada()
        aplicarCajaOperativaEnSesion(idCajera, nombreCajera)
        publicarCajaOperativaActual()
        publicarEstadoSupervisionActual()
        actualizarInterfazSupervision()
        escucharProductosCaja()
        escucharEstadoCobro()
        escucharVentasEnEspera()
        escucharSupervisionEnMiCaja()
        verificarTurnoActivo()
        mensaje?.takeIf { it.isNotBlank() }?.let {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun aplicarCajaOperativaEnSesion(idCaja: String, nombreCaja: String) {
        if (!isAdded) return
        val idCajaActual = obtenerIdCajaOperativa()
        if (idCajaActual.isNotBlank() && idCajaActual != idCaja) {
            liberarControlAsistidoSiLoTengo(idCajaActual)
        }
        if (idCaja.trim() != idCajaRescateBloqueada) {
            limpiarModoRescateCajaBloqueada()
        }
        SessionManager.cambiarCajaDestino(idCaja, nombreCaja)
        SessionManager.asegurarCajaPropiaSiCorresponde(requireContext())
        SessionManager.persistirCajaEnUso(requireContext())
    }

    private fun publicarCajaOperativaActual() {
        if (idCajera.isBlank()) return
        CajaEnUsoSyncManager.publicarCajaEnUso(
            idUsuario = idCajera,
            idCaja = obtenerIdCajaOperativa(),
            nombreCaja = obtenerNombreCajaOperativa()
        )
    }

    private fun restaurarContextoLocalCajaAlSalir() {
        if (!estaEnModoSupervision()) return

        liberarControlAsistidoSiLoTengo(obtenerIdCajaOperativa())
        limpiarSupervisionPublicada()
        limpiarModoRescateCajaBloqueada()
        SessionManager.restaurarCajaPropia()
        publicarCajaOperativaActual()
    }

    fun prepararSalidaDeSupervisionSiHaceFalta(
        saliendoDeLaApp: Boolean = false,
        onSalidaConfirmada: () -> Unit
    ): Boolean {
        if (rescateRequiereCierreObligatorio()) {
            mostrarToastSeguro("Debes cerrar el turno rescatado para continuar")
            return true
        }

        if (!estaEnModoSupervision()) return false
        if (!isAdded) return true

        mostrarDialogoSalidaSupervision(
            saliendoDeLaApp = saliendoDeLaApp,
            onSalidaConfirmada = onSalidaConfirmada
        )
        return true
    }

    private fun mostrarDialogoSalidaSupervision(
        saliendoDeLaApp: Boolean,
        onSalidaConfirmada: () -> Unit
    ) {
        val dialogoExistente = dialogoSalidaSupervisionActual
        if (dialogoExistente?.isShowing == true) {
            dialogoExistente.window?.decorView?.announceForAccessibility("La supervision sigue activa en esta caja")
            return
        }

        if (!isAdded) return

        val dialogView = layoutInflater.inflate(R.layout.dialog_salir_supervision, null, false)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val nombreCajaObjetivo = obtenerNombreCajaOperativa().ifBlank { "esta caja" }
        val tieneControlActivo = editorControlCajaId.trim() == idCajera
        val badge = dialogView.findViewById<TextView>(R.id.tvBadgeSalirSupervision)
        val titulo = dialogView.findViewById<TextView>(R.id.tvTituloSalirSupervision)
        val detalle = dialogView.findViewById<TextView>(R.id.tvDetalleSalirSupervision)
        val nota = dialogView.findViewById<TextView>(R.id.tvNotaSalirSupervision)
        val error = dialogView.findViewById<TextView>(R.id.tvErrorSalirSupervision)
        val btnQuedarme = dialogView.findViewById<MaterialButton>(R.id.btnSeguirSupervision)
        val btnSalir = dialogView.findViewById<MaterialButton>(R.id.btnSalirSupervision)

        badge.text = if (estaEnModoRescateCajaBloqueada()) "MODO RESCATE" else "SUPERVISION ACTIVA"
        titulo.text = if (saliendoDeLaApp) {
            "Si sales ahora, cerrarás esta supervision"
        } else {
            "Si cambias de pantalla, dejarás esta caja"
        }
        detalle.text = when {
            tieneControlActivo -> "Tienes el control activo en $nombreCajaObjetivo. Antes de salir vamos a devolverlo y a cerrar la supervision para no dejar bloqueada a la cajera."
            estaEnModoRescateCajaBloqueada() -> "Antes de salir liberaremos el modo de rescate de $nombreCajaObjetivo y volverás a tu contexto normal."
            else -> "Antes de salir liberaremos la supervision de $nombreCajaObjetivo para que la caja siga operando sin arrastrar bloqueos ni ediciones pendientes."
        }
        nota.text = if (saliendoDeLaApp) {
            "La app solo se cerrará cuando esta caja quede liberada correctamente."
        } else {
            "También cerraremos cobro, paneles y overlays de esta caja para que no se arrastren a otra pantalla."
        }

        btnQuedarme.setOnClickListener { dialog.dismiss() }
        btnSalir.setOnClickListener {
            btnQuedarme.isEnabled = false
            btnSalir.isEnabled = false
            btnSalir.text = "Liberando..."
            error.visibility = View.GONE
            error.text = ""

            liberarSupervisionAntesDeSalir(
                onSuccess = {
                    dialog.dismiss()
                    onSalidaConfirmada()
                },
                onError = { mensaje ->
                    btnQuedarme.isEnabled = true
                    btnSalir.isEnabled = true
                    btnSalir.text = "Salir y liberar"
                    error.text = mensaje
                    error.visibility = View.VISIBLE
                }
            )
        }

        dialog.setOnDismissListener {
            if (dialogoSalidaSupervisionActual === dialog) {
                dialogoSalidaSupervisionActual = null
            }
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogoSalidaSupervisionActual = dialog
        dialog.show()
    }

    private fun liberarSupervisionAntesDeSalir(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val idCajaObjetivo = obtenerIdCajaOperativa().trim()
        if (idCajaObjetivo.isBlank()) {
            onError("No se pudo identificar la caja supervisada.")
            return
        }

        val nombreCajaObjetivo = obtenerNombreCajaOperativa().ifBlank { idCajaObjetivo }

        fun finalizarSalidaLocal() {
            editorControlCajaId = ""
            editorControlCajaNombre = ""
            cerrarInteraccionesSensiblesPorControlAsistido()
            detenerSincronizacionCajaEnUsoCuenta()
            limpiarModoRescateCajaBloqueada()
            SessionManager.restaurarCajaPropia()
            publicarCajaOperativaActual()
            publicarEstadoSupervisionActual()
            actualizarInterfazSupervision()
            onSuccess()
        }

        fun liberarSupervisionPublicada() {
            CajaSupervisionManager.limpiarSupervisionPublicada(
                database = database,
                onFinSupervision = { idCajaLiberada, nombreCajaLiberada ->
                    registrarMovimientoSupervision(
                        tipo = "supervision_caja_finalizada",
                        titulo = "Supervisión operativa finalizada",
                        descripcion = "$nombreCajera salió de la caja de $nombreCajaLiberada",
                        idCajaObjetivo = idCajaLiberada,
                        nombreCajaObjetivo = nombreCajaLiberada
                    )
                    Log.d(
                        "CajaSupervision",
                        "Supervision limpiada supervisor=$idCajera cajaSupervisada=$idCajaLiberada"
                    )
                },
                onError = { error ->
                    Log.e(
                        "CajaSupervision",
                        "No se pudo limpiar supervision al salir supervisor=$idCajera cajaSupervisada=$idCajaObjetivo",
                        error
                    )
                    onError("No pudimos liberar la supervision de $nombreCajaObjetivo. Intenta nuevamente.")
                },
                onEstadoActualizado = { _, _ ->
                    if (isAdded && _binding != null) {
                        actualizarInterfazSupervision()
                    }
                },
                onComplete = { finalizarSalidaLocal() }
            )
        }

        if (editorControlCajaId.trim() == idCajera) {
            CajaControlAsistidoManager.soltarControl(
                database = database,
                idCaja = idCajaObjetivo,
                idEditor = idCajera
            ) { success ->
                if (!success) {
                    onError("No pudimos devolver el control de $nombreCajaObjetivo. Intenta nuevamente.")
                    return@soltarControl
                }
                editorControlCajaId = ""
                editorControlCajaNombre = ""
                liberarSupervisionPublicada()
            }
            return
        }

        liberarSupervisionPublicada()
    }

    private fun detenerSincronizacionCajaEnUsoCuenta() {
        CajaEnUsoSyncManager.dejarDeEscuchar(cajaEnUsoSyncRefListener, cajaEnUsoSyncValueListener)
        cajaEnUsoSyncRefListener = null
        cajaEnUsoSyncValueListener = null
    }

    private fun iniciarSincronizacionCajaEnUsoCuenta() {
        if (!isAdded || idCajera.isBlank()) return

        detenerSincronizacionCajaEnUsoCuenta()

        val sync = CajaEnUsoSyncManager.escucharCajaEnUso(
            idUsuario = idCajera,
            onChanged = { remoto ->
                if (!isAdded) return@escucharCajaEnUso

                val puedeCambiarCaja = SessionManager.puedeCambiarCaja()
                val idLocal = obtenerIdCajaOperativa()
                val nombreLocal = obtenerNombreCajaOperativa()

                val estadoObjetivo = when {
                    remoto == null -> CajaEnUsoSyncManager.EstadoCajaEnUso(
                        idCaja = if (puedeCambiarCaja) idLocal else idCajera,
                        nombreCaja = if (puedeCambiarCaja) nombreLocal else nombreCajera
                    )
                    !puedeCambiarCaja -> CajaEnUsoSyncManager.EstadoCajaEnUso(
                        idCaja = idCajera,
                        nombreCaja = nombreCajera
                    )
                    else -> CajaEnUsoSyncManager.EstadoCajaEnUso(
                        idCaja = remoto.idCaja.ifBlank { idCajera },
                        nombreCaja = remoto.nombreCaja.ifBlank { nombreCajera }
                    )
                }

                val hayCambio = estadoObjetivo.idCaja != idLocal || estadoObjetivo.nombreCaja != nombreLocal
                if (!hayCambio) return@escucharCajaEnUso

                aplicarCajaOperativaEnSesion(estadoObjetivo.idCaja, estadoObjetivo.nombreCaja)

                if (_binding == null) return@escucharCajaEnUso

                actualizarInterfazSupervision()
                limpiarTodosLosListeners()
                verificarTurnoActivo()
                escucharEstadoTurnoCaja()
                escucharProductosCaja()
                escucharEstadoCobro()
                escucharVentasEnEspera()
                escucharSupervisionEnMiCaja()
                publicarEstadoSupervisionActual()
                if (esTablet()) {
                    cargarCategoriasTablet()
                    obtenerProductosTablet()
                }
            },
            onError = {
                if (!isAdded) return@escucharCajaEnUso
                SessionManager.asegurarCajaPropiaSiCorresponde(requireContext())
            }
        )

        cajaEnUsoSyncRefListener = sync?.first
        cajaEnUsoSyncValueListener = sync?.second
    }

    private fun actualizarInterfazSupervision() {
        val b = _binding ?: return
        val card = b.cardAvisoSupervision
        val tvTitulo = b.tvMensajeSupervision
        val tvDetalle = b.tvNombreCajaSupervisada
        val btnCerrar = b.btnCerrarSupervision
        val btnAccion = b.btnAccionControlSupervision
        val controlUi = construirEstadoControlAsistidoUiModel()
        val modelo = construirPresenciaSupervisionUiModel()
        val modoRescateActivo = estaEnModoRescateCajaBloqueada()

        if (modelo == null) {
            animarBannerSupervision(card, mostrar = false)
            btnCerrar.setOnClickListener(null)
            btnAccion.visibility = View.GONE
            btnAccion.setOnClickListener(null)
            ocultarSplashPresenciaSupervision(animado = false)
            registrarEstadoPresenciaSupervision(modelo = null)
            actualizarOverlayControlAsistidoUi(controlUi)
            actualizarDisponibilidadBuscadorPorRescate()
            actualizarModoCierreForzado()
            actualizarMenuToolbarCaja()
            return
        }

        var fondoBanner = modelo.fondoBanner
        var textoBanner = modelo.textoBanner
        var tituloBanner = modelo.tituloBanner
        var detalleBanner = modelo.detalleBanner
        val rescateObligatorio = rescateRequiereCierreObligatorio()
        val mostrarBotonCerrar = modelo.mostrarBotonCerrar &&
                !controlUi.tieneControlEsteUsuario &&
                !rescateObligatorio

        when {
            controlUi.tieneControlEsteUsuario -> {
                fondoBanner = "#DCFCE7"
                textoBanner = "#166534"
                tituloBanner = "Control activo en ${obtenerNombreCajaOperativa()}"
                detalleBanner = "Solo tu puedes editar por ahora. Devuelve el control cuando termines para evitar cruces."
            }
            controlUi.bloqueadoPorOtroUsuario && estaEnModoSupervision() -> {
                fondoBanner = "#EEF2FF"
                textoBanner = "#1D4ED8"
                tituloBanner = "${controlUi.editorNombre} ya tiene el control"
                detalleBanner = "Puedes seguir viendo esta caja en vivo, pero la edicion esta reservada para ${controlUi.editorNombre}."
            }
            controlUi.bloqueadoPorOtroUsuario -> {
                fondoBanner = "#EEF2FF"
                textoBanner = "#1D4ED8"
                tituloBanner = "${controlUi.editorNombre} esta editando esta caja"
                detalleBanner = "Tu pantalla quedo en modo visual para evitar cobros, borrados o cambios cruzados."
            }
        }

        if (modoRescateActivo) {
            val nombreObjetivo = nombreCuentaRescateBloqueada.ifBlank { obtenerNombreCajaOperativa() }
            fondoBanner = "#FEF3C7"
            textoBanner = "#92400E"
            tituloBanner = "Modo rescate en ${obtenerNombreCajaOperativa()}"
            detalleBanner = "$nombreObjetivo tiene la cuenta bloqueada. Solo puedes cerrar el turno abierto y luego volveremos a tu caja."
        }

        card.setCardBackgroundColor(Color.parseColor(fondoBanner))
        tvTitulo.setTextColor(Color.parseColor(textoBanner))
        tvDetalle.setTextColor(Color.parseColor(textoBanner))
        tvTitulo.text = tituloBanner
        tvDetalle.text = detalleBanner
        btnCerrar.setColorFilter(Color.parseColor(textoBanner))
        btnCerrar.visibility = if (mostrarBotonCerrar) View.VISIBLE else View.GONE
        actualizarDisponibilidadBuscadorPorRescate()
        actualizarModoCierreForzado()

        val textoAccion = when {
            rescateObligatorio -> "Cerrar turno"
            modoRescateActivo -> "Volver a mi caja"
            else -> controlUi.textoAccionBanner
        }
        if (textoAccion.isNullOrBlank()) {
            btnAccion.visibility = View.GONE
            btnAccion.setOnClickListener(null)
        } else {
            btnAccion.visibility = View.VISIBLE
            btnAccion.text = textoAccion
            btnAccion.setTextColor(Color.parseColor(textoBanner))
            btnAccion.strokeColor = ColorStateList.valueOf(Color.parseColor(textoBanner))
            btnAccion.setOnClickListener {
                when (textoAccion) {
                    "Cerrar turno" -> intentarMostrarCierreCaja()
                    "Volver a mi caja" -> volverAMiCajaDesdeSupervision("Has vuelto a tu caja")
                    "Tomar control" -> tomarControlAsistidoCaja()
                    "Devolver control" -> soltarControlAsistidoCaja(notificar = true)
                }
            }
        }

        if (mostrarBotonCerrar) {
            btnCerrar.setOnClickListener {
                volverAMiCajaDesdeSupervision("Has vuelto a tu caja")
            }
        } else {
            btnCerrar.setOnClickListener(null)
        }

        animarBannerSupervision(card, mostrar = true)
        actualizarOverlayControlAsistidoUi(controlUi)
        registrarEstadoPresenciaSupervision(modelo)
        actualizarMenuToolbarCaja()
    }

    private fun construirEstadoControlAsistidoUiModel(): EstadoControlAsistidoUiModel {
        val editorId = editorControlCajaId.trim()
        val editorNombre = editorControlCajaNombre.trim().ifBlank { "Soporte" }
        val tieneControlEsteUsuario = editorId.isNotBlank() && editorId == idCajera
        val bloqueadoPorOtroUsuario = editorId.isNotBlank() && editorId != idCajera
        val puedeTomarControl = estaEnModoSupervision() && editorId.isBlank()
        val textoAccionBanner = when {
            tieneControlEsteUsuario -> "Devolver control"
            puedeTomarControl -> "Tomar control"
            else -> null
        }
        val tituloOverlay = when {
            !bloqueadoPorOtroUsuario -> null
            estaEnModoSupervision() -> "$editorNombre ya esta editando esta caja"
            else -> "$editorNombre esta asistiendo tu caja"
        }
        val detalleOverlay = when {
            !bloqueadoPorOtroUsuario -> null
            estaEnModoSupervision() ->
                "Sigue viendo todo en vivo, pero la edicion quedo reservada para $editorNombre hasta que devuelva el control."
            else ->
                "Tu pantalla queda en modo visual para evitar cobros, borrados o cambios cruzados mientras $editorNombre edita."
        }

        return EstadoControlAsistidoUiModel(
            editorId = editorId,
            editorNombre = editorNombre,
            tieneControlEsteUsuario = tieneControlEsteUsuario,
            bloqueadoPorOtroUsuario = bloqueadoPorOtroUsuario,
            puedeTomarControl = puedeTomarControl,
            textoAccionBanner = textoAccionBanner,
            tituloOverlay = tituloOverlay,
            detalleOverlay = detalleOverlay
        )
    }

    private fun estaCajaEnModoSoloVista(notificar: Boolean = false): Boolean {
        val editorId = editorControlCajaId.trim()
        if (editorId.isBlank() || editorId == idCajera) return false

        if (notificar && isAdded) {
            val editorNombre = editorControlCajaNombre.trim().ifBlank { "Soporte" }
            mostrarToastSeguro("Esta caja esta en modo visual mientras $editorNombre edita")
        }
        return true
    }

    private fun actualizarOverlayControlAsistidoUi(controlUi: EstadoControlAsistidoUiModel) {
        val b = _binding ?: return
        val overlay = b.overlayControlAsistidoCaja
        val card = b.cardControlAsistidoCaja
        val tvBadge = b.tvBadgeControlAsistidoCaja
        val tvTitulo = b.tvTituloControlAsistidoCaja
        val tvDetalle = b.tvDetalleControlAsistidoCaja
        val mostrarOverlay = controlUi.bloqueadoPorOtroUsuario

        if (mostrarOverlay) {
            tvBadge.text = if (estaEnModoSupervision()) "OBSERVANDO EN VIVO" else "SOLO VISTA"
            tvTitulo.text = controlUi.tituloOverlay
            tvDetalle.text = controlUi.detalleOverlay
            overlay.bringToFront()
            b.cardAvisoSupervision.bringToFront()
            b.cardSplashPresenciaCaja.bringToFront()
            cerrarInteraccionesSensiblesPorControlAsistido()
        }

        animarOverlayControlAsistido(overlay, card, mostrarOverlay)
    }

    private fun animarOverlayControlAsistido(overlay: View, card: View, mostrar: Boolean) {
        val yaVisible = overlay.visibility == View.VISIBLE
        overlay.animate().cancel()
        card.animate().cancel()

        if (mostrar) {
            if (!yaVisible) {
                overlay.alpha = 0f
                card.alpha = 0f
                card.translationY = dpToPx(18f)
                card.scaleX = 0.97f
                card.scaleY = 0.97f
                overlay.visibility = View.VISIBLE
                overlay.animate()
                    .alpha(1f)
                    .setDuration(180L)
                    .start()
                card.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(220L)
                    .start()
            }
            return
        }

        if (!yaVisible) return
        overlay.animate()
            .alpha(0f)
            .setDuration(150L)
            .withEndAction {
                val bindingActual = _binding ?: return@withEndAction
                if (bindingActual.overlayControlAsistidoCaja == overlay) {
                    overlay.visibility = View.GONE
                    overlay.alpha = 1f
                    card.alpha = 1f
                    card.translationY = 0f
                    card.scaleX = 1f
                    card.scaleY = 1f
                }
            }
            .start()
    }

    private fun cerrarInteraccionesSensiblesPorControlAsistido() {
        ocultarTeclado()
        _binding?.buscar?.clearFocus()
        cerrarPanelesEditablesPorControlAsistido()
        snackbarEliminarProductoActivo?.dismiss()
        snackbarEliminarProductoActivo = null
        popupMenuCajaActivo?.dismiss()
        popupMenuCajaActivo = null
        dialogoRegistrarEgresoActual?.dismiss()
        dialogoRegistrarEgresoActual = null
        dialogoOpcionesComprobanteEgresoActual?.dismiss()
        dialogoOpcionesComprobanteEgresoActual = null
        dialogoConfirmacionEgresoActual?.dismiss()
        dialogoConfirmacionEgresoActual = null
        dialogoVentasEnEsperaActual?.dismiss()
        dialogoVentasEnEsperaActual = null
        dialogoOpcionesVentaEnEsperaActual?.dismiss()
        dialogoOpcionesVentaEnEsperaActual = null
        dialogoNotaVentaEnEsperaActual?.dismiss()
        dialogoNotaVentaEnEsperaActual = null
        dialogoSelectorCajerasActual?.dismiss()
        dialogoSelectorCajerasActual = null
        dialogoCambioPresentacionActual?.dismiss()
        dialogoCambioPresentacionActual = null
        dialogoDescuentoActual?.dismiss()
        dialogoDescuentoActual = null
        dialogoCobroActual?.dismiss()
        dialogoCobroActual = null
        dialogoDevolucionActual?.dismiss()
        dialogoDevolucionActual = null
        dialogoResolucionStockActual?.dismiss()
        dialogoResolucionStockActual = null
    }

    private fun cerrarPanelesEditablesPorControlAsistido() {
        val bindingActual = _binding ?: return
        val rescateMantieneCierre = rescateRequiereCierreObligatorio()

        if (!rescateMantieneCierre && obtenerLayoutCierreActual()?.visibility == View.VISIBLE) {
            ocultarLayoutCierreCajaInterno()
            obtenerLayoutMontoCierre()?.error = null
            obtenerLayoutObservacionCierre()?.apply {
                visibility = View.GONE
                error = null
            }
            obtenerInputObservacionCierre()?.setText("")
        }

        val checkoutVisible =
            bindingActual.layoutComprobante.visibility == View.VISIBLE ||
                bindingActual.layoutCobroPanel.visibility == View.VISIBLE
        if (!checkoutVisible) return

        reiniciarFlujoCobroInline()
        if (esTablet()) {
            aplicarLayoutInicialTablet()
        } else {
            ocultarLayoutComprobanteMobile()
        }
    }

    private fun construirPresenciaSupervisionUiModel(): PresenciaSupervisionUiModel? {
        val idEnUso = obtenerIdCajaOperativa()
        val nombreEnUso = obtenerNombreCajaOperativa().ifBlank { "esta caja" }
        val nombreSupervisorVisible = supervisorEnMiCajaNombre.ifBlank { "Alguien de soporte" }

        return when {
            estaEnModoRescateCajaBloqueada() -> {
                val nombreObjetivo = nombreCuentaRescateBloqueada.ifBlank { nombreEnUso }
                PresenciaSupervisionUiModel(
                    tipo = TipoPresenciaSupervisionUi.RESCATE_CAJA_BLOQUEADA,
                    clave = "rescate:$idEnUso:$nombreObjetivo",
                    tituloBanner = "Modo rescate en $nombreEnUso",
                    detalleBanner = "$nombreObjetivo tiene la cuenta bloqueada. Solo puedes cerrar el turno abierto y volver a tu caja.",
                    fondoBanner = "#FEF3C7",
                    textoBanner = "#92400E",
                    tituloSplash = "Entraste en modo rescate",
                    detalleSplash = "Esta caja quedo limitada a cierre de turno por cuenta bloqueada.",
                    fondoSplash = "#7C3F00",
                    fondoBadgeSplash = "#9A5100",
                    fondoIconoSplash = "#D97706",
                    mostrarBotonCerrar = true
                )
            }
            idEnUso.isNotBlank() && idEnUso != idCajera -> {
                PresenciaSupervisionUiModel(
                    tipo = TipoPresenciaSupervisionUi.SUPERVISOR_EN_CAJA_AJENA,
                    clave = "supervisor:$idEnUso:$nombreEnUso",
                    tituloBanner = "Asistencia en vivo en $nombreEnUso",
                    detalleBanner = "Tus cambios se veran en esta caja al instante. Evita editar al mismo tiempo con la cajera.",
                    fondoBanner = "#E7F8F3",
                    textoBanner = "#0B5D4B",
                    tituloSplash = "Entraste a la caja de $nombreEnUso",
                    detalleSplash = "Tu presencia ya es visible en vivo para esta caja.",
                    fondoSplash = "#0B5D4B",
                    fondoBadgeSplash = "#12715C",
                    fondoIconoSplash = "#22A07B",
                    mostrarBotonCerrar = true
                )
            }
            supervisorEnMiCajaId.isNotBlank() && supervisorEnMiCajaId != idCajera -> {
                PresenciaSupervisionUiModel(
                    tipo = TipoPresenciaSupervisionUi.SUPERVISOR_EN_MI_CAJA,
                    clave = "cajera:$supervisorEnMiCajaId:$nombreSupervisorVisible",
                    tituloBanner = "$nombreSupervisorVisible entro a tu caja",
                    detalleBanner = "Ahora ambos ven la misma venta en vivo. Avancen un paso a la vez para evitar cruces.",
                    fondoBanner = "#EAF2FF",
                    textoBanner = "#184A8C",
                    tituloSplash = "$nombreSupervisorVisible ya esta contigo",
                    detalleSplash = "Esta caja acaba de recibir compania en tiempo real.",
                    fondoSplash = "#183B6B",
                    fondoBadgeSplash = "#24589E",
                    fondoIconoSplash = "#2E79D2",
                    mostrarBotonCerrar = false
                )
            }
            else -> null
        }
    }

    private fun registrarEstadoPresenciaSupervision(modelo: PresenciaSupervisionUiModel?) {
        val claveActual = modelo?.clave.orEmpty()
        val debeMostrarSplash = when {
            !supervisionPresenciaInicializada -> {
                supervisionPresenciaInicializada = true
                modelo != null
            }
            modelo == null -> false
            claveActual == ultimaClavePresenciaSupervision -> false
            else -> true
        }

        ultimaClavePresenciaSupervision = claveActual

        if (debeMostrarSplash && modelo != null) {
            mostrarSplashPresenciaSupervision(modelo)
        }
    }

    private fun animarBannerSupervision(card: View, mostrar: Boolean) {
        val yaVisible = card.visibility == View.VISIBLE
        card.animate().cancel()

        if (mostrar) {
            if (!yaVisible) {
                card.alpha = 0f
                card.translationY = -dpToPx(10f)
                card.visibility = View.VISIBLE
                card.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(220L)
                    .start()
            }
            return
        }

        if (!yaVisible) return
        card.animate()
            .alpha(0f)
            .translationY(-dpToPx(8f))
            .setDuration(160L)
            .withEndAction {
                val bindingActual = _binding ?: return@withEndAction
                if (bindingActual.cardAvisoSupervision == card) {
                    card.visibility = View.GONE
                    card.alpha = 1f
                    card.translationY = 0f
                }
            }
            .start()
    }

    private fun mostrarSplashPresenciaSupervision(modelo: PresenciaSupervisionUiModel) {
        val b = _binding ?: return
        val card = b.cardSplashPresenciaCaja
        val cardIcono = b.cardSplashPresenciaIcono
        val cardBadge = b.cardSplashPresenciaBadge
        val tvBadge = b.tvSplashPresenciaBadge
        val tvTitulo = b.tvSplashPresenciaTitulo
        val tvDetalle = b.tvSplashPresenciaDetalle

        runnableOcultarSplashPresenciaSupervision?.let { handlerSeguridadOperaciones.removeCallbacks(it) }
        runnableOcultarSplashPresenciaSupervision = null

        card.setCardBackgroundColor(Color.parseColor(modelo.fondoSplash))
        cardIcono.setCardBackgroundColor(Color.parseColor(modelo.fondoIconoSplash))
        cardBadge.setCardBackgroundColor(Color.parseColor(modelo.fondoBadgeSplash))
        tvBadge.text = when (modelo.tipo) {
            TipoPresenciaSupervisionUi.SUPERVISOR_EN_CAJA_AJENA -> "ASISTENCIA EN VIVO"
            TipoPresenciaSupervisionUi.SUPERVISOR_EN_MI_CAJA -> "CAJA COMPARTIDA"
            TipoPresenciaSupervisionUi.RESCATE_CAJA_BLOQUEADA -> "MODO RESCATE"
        }
        tvTitulo.text = modelo.tituloSplash
        tvDetalle.text = modelo.detalleSplash

        card.bringToFront()
        card.animate().cancel()
        card.visibility = View.VISIBLE
        card.alpha = 0f
        card.translationY = -dpToPx(18f)
        card.scaleX = 0.97f
        card.scaleY = 0.97f

        val animadorEntrada = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(card, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(card, View.TRANSLATION_Y, -dpToPx(18f), 0f),
                ObjectAnimator.ofFloat(card, View.SCALE_X, 0.97f, 1f),
                ObjectAnimator.ofFloat(card, View.SCALE_Y, 0.97f, 1f)
            )
            duration = 260L
        }
        val animadorPulso = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(cardIcono, View.SCALE_X, 1f, 1.08f, 1f),
                ObjectAnimator.ofFloat(cardIcono, View.SCALE_Y, 1f, 1.08f, 1f)
            )
            duration = 720L
            startDelay = 60L
        }
        animadorEntrada.start()
        animadorPulso.start()

        val runnableOcultar = Runnable {
            ocultarSplashPresenciaSupervision(animado = true)
        }
        runnableOcultarSplashPresenciaSupervision = runnableOcultar
        handlerSeguridadOperaciones.postDelayed(runnableOcultar, 2600L)
    }

    private fun ocultarSplashPresenciaSupervision(animado: Boolean) {
        runnableOcultarSplashPresenciaSupervision?.let { handlerSeguridadOperaciones.removeCallbacks(it) }
        runnableOcultarSplashPresenciaSupervision = null

        val card = _binding?.cardSplashPresenciaCaja ?: return
        card.animate().cancel()

        if (!animado || card.visibility != View.VISIBLE) {
            card.visibility = View.GONE
            card.alpha = 0f
            card.translationY = 0f
            card.scaleX = 1f
            card.scaleY = 1f
            return
        }

        val animadorSalida = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(card, View.ALPHA, card.alpha, 0f),
                ObjectAnimator.ofFloat(card, View.TRANSLATION_Y, card.translationY, -dpToPx(12f)),
                ObjectAnimator.ofFloat(card, View.SCALE_X, card.scaleX, 0.985f),
                ObjectAnimator.ofFloat(card, View.SCALE_Y, card.scaleY, 0.985f)
            )
            duration = 180L
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    val bindingActual = _binding ?: return
                    if (bindingActual.cardSplashPresenciaCaja == card) {
                        card.visibility = View.GONE
                        card.alpha = 0f
                        card.translationY = 0f
                        card.scaleX = 1f
                        card.scaleY = 1f
                    }
                }
            })
        }
        animadorSalida.start()
    }

    private fun dpToPx(valueDp: Float): Float {
        return valueDp * resources.displayMetrics.density
    }

    private fun registrarMovimientoSupervision(tipo: String, titulo: String, descripcion: String, idCajaObjetivo: String, nombreCajaObjetivo: String) {
        MovimientoLogger.registrar(
            tipo = tipo,
            modulo = "caja",
            titulo = titulo,
            descripcion = descripcion,
            idUsuario = idCajera,
            nombreUsuario = nombreCajera,
            referenciaId = idCajaObjetivo,
            idCaja = idCajaObjetivo,
            nombreCaja = nombreCajaObjetivo,
            extra = mapOf(
                "esSupervision" to true,
                "idSupervisor" to idCajera,
                "nombreSupervisor" to nombreCajera,
                "idCajaSupervisada" to idCajaObjetivo,
                "nombreCajaSupervisada" to nombreCajaObjetivo
            )
        )
    }

    private fun registrarMovimientoCambioCaja(idCajaAnterior: String, nombreCajaAnterior: String, idCajaNueva: String, nombreCajaNueva: String) {
        MovimientoLogger.registrar(
            tipo = "cambio_caja",
            modulo = "caja",
            titulo = "Caja operativa cambiada",
            descripcion = "Se cambio de $nombreCajaAnterior a $nombreCajaNueva",
            idUsuario = idCajera,
            nombreUsuario = nombreCajera,
            referenciaId = idCajaNueva,
            idCaja = idCajaNueva,
            nombreCaja = nombreCajaNueva,
            extra = mapOf(
                "idCajaAnterior" to idCajaAnterior,
                "nombreCajaAnterior" to nombreCajaAnterior,
                "idCajaNueva" to idCajaNueva,
                "nombreCajaNueva" to nombreCajaNueva
            ),
            database = database
        )
    }

    private fun registrarMovimientoCambioPresentacionCaja(productoAnterior: ProductoCaja, productoActualizado: ProductoCaja, opcion: OpcionCorreccionPresentacionCaja) {
        val idCajaOperativa = obtenerIdCajaOperativa()
        val nombreCajaOperativa = obtenerNombreCajaOperativa()

        MovimientoLogger.registrar(
            tipo = "cambio_presentacion_caja",
            modulo = "caja",
            titulo = "Presentaci\u00f3n corregida en caja",
            descripcion = "Se cambi\u00f3 ${productoAnterior.nombre} a ${opcion.nombreUi} para continuar la venta",
            idUsuario = idCajera,
            nombreUsuario = nombreCajera,
            referenciaId = productoAnterior.id,
            idCaja = idCajaOperativa,
            nombreCaja = nombreCajaOperativa,
            extra = mapOf(
                "idProductoCaja" to productoAnterior.id,
                "indiceProducto" to productoAnterior.indiceProducto,
                "nombreProducto" to productoAnterior.nombre,
                "presentacionAnterior" to productoAnterior.presentacion,
                "presentacionNueva" to productoActualizado.presentacion,
                "cantidadAnterior" to productoAnterior.cantidad,
                "cantidadNueva" to productoActualizado.cantidad,
                "unidadesPorPresentacionAnterior" to productoAnterior.unidadesPorPresentacion,
                "unidadesPorPresentacionNueva" to productoActualizado.unidadesPorPresentacion,
                "precioUnitarioAnterior" to productoAnterior.precioUnitario,
                "precioUnitarioNuevo" to productoActualizado.precioUnitario,
                "totalAnterior" to productoAnterior.total,
                "totalNuevo" to productoActualizado.total
            ),
            database = database
        )
    }

    private fun escucharSupervisionEnMiCaja() {
        if (idCajera.isBlank()) return

        CajaSupervisionManager.escucharSupervisionCaja(
            database = database,
            idCaja = idCajera,
            onChanged = { idSupervisor, nombreSupervisor ->
                supervisorEnMiCajaId = idSupervisor
                supervisorEnMiCajaNombre = nombreSupervisor
                Log.d(
                    "CajaSupervision",
                    "Escuchando supervision caja=$idCajera supervisorId=$supervisorEnMiCajaId supervisorNombre=$supervisorEnMiCajaNombre"
                )
                if (isAdded && _binding != null) {
                    actualizarInterfazSupervision()
                }
            },
            onError = { error ->
                Log.w(
                    "CajaSupervision",
                    "Error escuchando supervision caja=$idCajera: ${error.message}"
                )
            }
        )
    }

    private fun escucharControlAsistidoEnCajaActual() {
        val idCajaObjetivo = obtenerIdCajaOperativa()
        if (idCajaObjetivo.isBlank()) return

        CajaControlAsistidoManager.escucharControlCaja(
            database = database,
            idCaja = idCajaObjetivo,
            onChanged = { estado ->
                editorControlCajaId = estado?.idEditor.orEmpty()
                editorControlCajaNombre = estado?.nombreEditor.orEmpty()
                if (isAdded && _binding != null) {
                    actualizarInterfazSupervision()
                }
            },
            onError = { error ->
                Log.w(
                    "CajaControlAsistido",
                    "Error escuchando control asistido caja=$idCajaObjetivo: ${error.message}"
                )
            }
        )
    }

    private fun tomarControlAsistidoCaja() {
        if (!estaEnModoSupervision()) return

        val idCajaObjetivo = obtenerIdCajaOperativa()
        val nombreCajaObjetivo = obtenerNombreCajaOperativa()
        if (idCajaObjetivo.isBlank()) return

        CajaControlAsistidoManager.tomarControl(
            database = database,
            idCaja = idCajaObjetivo,
            nombreCaja = nombreCajaObjetivo,
            idEditor = idCajera,
            nombreEditor = nombreCajera
        ) { success, holderName ->
            if (!isAdded) return@tomarControl
            if (success) {
                editorControlCajaId = idCajera
                editorControlCajaNombre = nombreCajera
                actualizarInterfazSupervision()
                mostrarToastSeguro("Ahora tienes el control de ${nombreCajaObjetivo.ifBlank { "esta caja" }}")
                return@tomarControl
            }

            val nombreOcupado = holderName?.ifBlank { null }
            val mensaje = if (nombreOcupado != null) {
                "$nombreOcupado ya tiene el control de esta caja."
            } else {
                "No se pudo tomar el control de esta caja."
            }
            mostrarToastSeguro(mensaje)
        }
    }

    private fun soltarControlAsistidoCaja(notificar: Boolean = false) {
        val idCajaObjetivo = obtenerIdCajaOperativa()
        if (idCajaObjetivo.isBlank() || editorControlCajaId != idCajera) return

        CajaControlAsistidoManager.soltarControl(
            database = database,
            idCaja = idCajaObjetivo,
            idEditor = idCajera
        ) { success ->
            if (!success || !isAdded) return@soltarControl
            editorControlCajaId = ""
            editorControlCajaNombre = ""
            actualizarInterfazSupervision()
            if (notificar) {
                mostrarToastSeguro("Devolviste el control de esta caja")
            }
        }
    }

    private fun liberarControlAsistidoSiLoTengo(idCajaObjetivo: String) {
        if (idCajaObjetivo.isBlank() || editorControlCajaId != idCajera) return

        CajaControlAsistidoManager.soltarControl(
            database = database,
            idCaja = idCajaObjetivo,
            idEditor = idCajera
        )
    }

    private fun publicarEstadoSupervisionActual() {
        if (idCajera.isBlank()) return

        val idEnUso = obtenerIdCajaOperativa()
        val nombreEnUso = obtenerNombreCajaOperativa()
        CajaSupervisionManager.sincronizarSupervisionActual(
            database = database,
            idSupervisor = idCajera,
            nombreSupervisor = nombreCajera,
            idCajaPropia = idCajera,
            idCajaEnUso = idEnUso,
            nombreCajaEnUso = nombreEnUso,
            onInicioSupervision = { idCajaObjetivo, nombreCajaObjetivo ->
                registrarMovimientoSupervision(
                    tipo = "supervision_caja_iniciada",
                    titulo = "Supervisi\u00f3n operativa iniciada",
                    descripcion = "$nombreCajera ingres\u00f3 a la caja de $nombreCajaObjetivo",
                    idCajaObjetivo = idCajaObjetivo,
                    nombreCajaObjetivo = nombreCajaObjetivo
                )
                Log.d(
                    "CajaSupervision",
                    "Supervision publicada supervisor=$idCajera cajaSupervisada=$idCajaObjetivo"
                )
            },
            onFinSupervision = { idCajaObjetivo, nombreCajaObjetivo ->
                registrarMovimientoSupervision(
                    tipo = "supervision_caja_finalizada",
                    titulo = "Supervisi\u00f3n operativa finalizada",
                    descripcion = "$nombreCajera sali\u00f3 de la caja de $nombreCajaObjetivo",
                    idCajaObjetivo = idCajaObjetivo,
                    nombreCajaObjetivo = nombreCajaObjetivo
                )
                Log.d(
                    "CajaSupervision",
                    "Supervision limpiada supervisor=$idCajera cajaSupervisada=$idCajaObjetivo"
                )
            },
            onError = { error ->
                if (error is CajaSupervisionManager.CajaSupervisionOcupadaException) {
                    if (estaEnModoSupervision()) {
                        volverAMiCajaDesdeSupervision()
                    }
                    mostrarDialogoAccesoCajaRestringido(
                        titulo = "Supervisión en curso",
                        mensaje = "${error.nombreSupervisorActual.ifBlank { "Otro supervisor" }} ya está supervisando esta caja. Espera a que termine para entrar."
                    )
                    return@sincronizarSupervisionActual
                }
                Log.e(
                    "CajaSupervision",
                    "No se pudo sincronizar supervision supervisor=$idCajera cajaSupervisada=$idEnUso",
                    error
                )
                if (isAdded) {
                    mostrarToastSeguro("No se pudo actualizar la supervisión de esta caja")
                }
            },
            onEstadoActualizado = { _, _ ->
                if (isAdded && _binding != null) {
                    actualizarInterfazSupervision()
                }
            }
        )
    }

    private fun limpiarSupervisionPublicada() {
        CajaSupervisionManager.limpiarSupervisionPublicada(
            database = database,
            onFinSupervision = { idCajaObjetivo, nombreCajaObjetivo ->
                registrarMovimientoSupervision(
                    tipo = "supervision_caja_finalizada",
                    titulo = "Supervisi\u00f3n operativa finalizada",
                    descripcion = "$nombreCajera sali\u00f3 de la caja de $nombreCajaObjetivo",
                    idCajaObjetivo = idCajaObjetivo,
                    nombreCajaObjetivo = nombreCajaObjetivo
                )
                Log.d(
                    "CajaSupervision",
                    "Supervision limpiada supervisor=$idCajera cajaSupervisada=$idCajaObjetivo"
                )
            },
            onError = { error ->
                Log.e(
                    "CajaSupervision",
                    "No se pudo limpiar supervision supervisor=$idCajera cajaPublicada=${CajaSupervisionManager.cajaPublicadaId}",
                    error
                )
            },
            onEstadoActualizado = { _, _ ->
                if (isAdded && _binding != null) {
                    actualizarInterfazSupervision()
                }
            }
        )
    }

    private fun cambiarCajaOperativaDesdeSelector(
        idCajaDestino: String,
        nombreCajaDestino: String,
        activarRescate: Boolean = false,
        nombreCuentaBloqueada: String = ""
    ) {
        val idCajaAnterior = obtenerIdCajaOperativa()
        val nombreCajaAnterior = obtenerNombreCajaOperativa()

        aplicarCajaOperativaEnSesion(idCajaDestino, nombreCajaDestino)
        if (activarRescate) {
            activarModoRescateCajaBloqueada(
                idCaja = idCajaDestino,
                nombreCaja = nombreCajaDestino,
                nombreCuentaBloqueada = nombreCuentaBloqueada.ifBlank { nombreCajaDestino }
            )
        } else {
            limpiarModoRescateCajaBloqueada()
        }
        editorControlCajaId = ""
        editorControlCajaNombre = ""
        limpiarTodosLosListeners()
        publicarCajaOperativaActual()
        registrarMovimientoCambioCaja(
            idCajaAnterior = idCajaAnterior,
            nombreCajaAnterior = nombreCajaAnterior,
            idCajaNueva = idCajaDestino,
            nombreCajaNueva = nombreCajaDestino
        )
        escucharEstadoTurnoCaja()
        escucharProductosCaja()
        escucharEstadoCobro()
        escucharVentasEnEspera()
        escucharSupervisionEnMiCaja()
        escucharControlAsistidoEnCajaActual()
        publicarEstadoSupervisionActual()
        actualizarInterfazSupervision()
        verificarTurnoActivo()
    }

    private fun mostrarSelectorDeCajeras() {
        if (estaCajaEnModoSoloVista(notificar = true)) return
        // CANDADO HORARIO: Bloquea cambio de caja si la hora está mal
        if (!verificarHoraAntesDeOperar("cambiar a otra caja")) return
        dialogoSelectorCajerasActual?.dismiss()
        val dialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottomsheet_selector_cajeras, null)
        dialog.setContentView(bottomSheetView)
        dialog.setOnDismissListener {
            if (dialogoSelectorCajerasActual === dialog) {
                dialogoSelectorCajerasActual = null
            }
        }

        val recyclerCajeras = bottomSheetView.findViewById<RecyclerView>(R.id.recyclerCajerasSelector)
        val progress = bottomSheetView.findViewById<ProgressBar>(R.id.progressSelector)
        val esSupervisor = SessionManager.rol.equals("supervisor", ignoreCase = true)

        recyclerCajeras.layoutManager = LinearLayoutManager(requireContext())

        CajaService.obtenerFechaActualServidor(
            onSuccess = { fechaActual ->
                FirebaseDatabase.getInstance().getReference("trabajadores").get()
                    .addOnSuccessListener { trabajadoresSnapshot ->
                        database.reference.child("CorteCaja").child("Cajeras").get()
                            .addOnSuccessListener { cajasSnapshot ->
                                progress.visibility = View.GONE
                                if (!trabajadoresSnapshot.exists()) return@addOnSuccessListener

                                val listaCajeras = mutableListOf<CajaSelectorUiModel>()
                                listaCajeras.add(
                                    CajaSelectorUiModel(
                                        id = idCajera,
                                        nombre = "$nombreCajera (Mi Caja)",
                                        rol = "Personal",
                                        accesoActivo = true,
                                        tieneTurnoAbierto = turnoAbierto,
                                        esCajaPropia = true
                                    )
                                )

                                for (uSnap in trabajadoresSnapshot.children) {
                                    val uid = uSnap.child("id").value?.toString().orEmpty().trim()
                                    val nombre = uSnap.child("usuario").value?.toString().orEmpty().trim()
                                    val rol = uSnap.child("rol").value?.toString()?.trim().orEmpty().ifBlank { "Cajera" }
                                    if (uid.isBlank() || uid == "null" || uid == idCajera) continue

                                    val accesoActivo = AccesoFieldRules.parseAccesoDesdeSnapshot(
                                        uSnap,
                                        defaultValue = false
                                    )
                                    val supervisionSnapshot = cajasSnapshot
                                        .child(uid)
                                        .child("supervisionActiva")
                                    val supervisorActivoId = supervisionSnapshot
                                        .child("idSupervisor")
                                        .value
                                        ?.toString()
                                        .orEmpty()
                                        .trim()
                                    val supervisorActivoNombre = supervisionSnapshot
                                        .child("nombreSupervisor")
                                        .value
                                        ?.toString()
                                        .orEmpty()
                                        .trim()
                                    val tieneTurnoAbierto = if (accesoActivo) {
                                        false
                                    } else {
                                        CajaService.resolverEstadoTurno(
                                            cajasSnapshot.child(uid),
                                            fechaActual
                                        ).turnoAbierto
                                    }

                                    listaCajeras.add(
                                        CajaSelectorUiModel(
                                            id = uid,
                                            nombre = nombre,
                                            rol = rol,
                                            accesoActivo = accesoActivo,
                                            tieneTurnoAbierto = tieneTurnoAbierto,
                                            esCajaPropia = false,
                                            supervisorActivoId = supervisorActivoId,
                                            supervisorActivoNombre = supervisorActivoNombre,
                                            supervisionOcupadaPorOtro = supervisorActivoId.isNotBlank() && supervisorActivoId != idCajera
                                        )
                                    )
                                }

                                val adapterCajeras = object : RecyclerView.Adapter<CajeraViewHolder>() {
                                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CajeraViewHolder {
                                        val view = layoutInflater.inflate(R.layout.item_selector_cajera, parent, false)
                                        return CajeraViewHolder(view)
                                    }

                                    override fun onBindViewHolder(holder: CajeraViewHolder, position: Int) {
                                        val item = listaCajeras[position]
                                        holder.tvNombre.text = item.nombre
                                        holder.tvRol.text = item.rol
                                        when {
                                            item.supervisionOcupadaPorOtro -> {
                                                holder.tvEstado.text = "Supervisada por ${item.supervisorActivoVisible}"
                                                holder.tvEstado.visibility = View.VISIBLE
                                            }
                                            item.permiteModoRescate -> {
                                                holder.tvEstado.text = "Cuenta bloqueada · solo cierre de turno"
                                                holder.tvEstado.visibility = View.VISIBLE
                                            }
                                            !item.accesoActivo -> {
                                                holder.tvEstado.text = "Cuenta bloqueada"
                                                holder.tvEstado.visibility = View.VISIBLE
                                            }
                                            else -> holder.tvEstado.visibility = View.GONE
                                        }

                                        if (item.id == obtenerIdCajaOperativa()) {
                                            holder.itemView.setBackgroundColor(Color.parseColor("#E8F5E9"))
                                            holder.imgCheck.visibility = View.VISIBLE
                                        } else {
                                            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
                                            holder.imgCheck.visibility = View.GONE
                                        }

                                        holder.itemView.alpha = if ((!item.accesoActivo && !item.permiteModoRescate) || item.supervisionOcupadaPorOtro) 0.78f else 1f
                                        holder.itemView.setOnClickListener {
                                            if (esSupervisor && item.rol.equals("administrador", ignoreCase = true)) {
                                                mostrarDialogoAccesoCajaRestringido()
                                                return@setOnClickListener
                                            }
                                            if (item.supervisionOcupadaPorOtro) {
                                                mostrarDialogoAccesoCajaRestringido(
                                                    titulo = "Supervisión en curso",
                                                    mensaje = "${item.supervisorActivoVisible} ya está supervisando esta caja. Espera a que termine para entrar."
                                                )
                                                return@setOnClickListener
                                            }
                                            if (!item.accesoActivo && !item.permiteModoRescate) {
                                                mostrarDialogoAccesoCajaRestringido(
                                                    titulo = "Cuenta bloqueada",
                                                    mensaje = "Esta cuenta esta bloqueada y no tiene un turno abierto por rescatar. No puedes supervisarla ni abrir su caja."
                                                )
                                                return@setOnClickListener
                                            }

                                            val nombreCajaNueva = item.nombre.substringBefore(" (").ifBlank { item.nombre }
                                            dialog.dismiss()
                                            cambiarCajaOperativaDesdeSelector(
                                                idCajaDestino = item.id,
                                                nombreCajaDestino = nombreCajaNueva,
                                                activarRescate = item.permiteModoRescate,
                                                nombreCuentaBloqueada = nombreCajaNueva
                                            )
                                            val mensaje = if (item.permiteModoRescate) {
                                                "Entraste en modo rescate para cerrar el turno de ${obtenerNombreCajaOperativa()}"
                                            } else {
                                                "Ahora operando en caja de: ${obtenerNombreCajaOperativa()}"
                                            }
                                            mostrarToastSeguro(mensaje)
                                        }
                                    }

                                    override fun getItemCount() = listaCajeras.size
                                }

                                recyclerCajeras.adapter = adapterCajeras
                            }
                            .addOnFailureListener {
                                progress.visibility = View.GONE
                                mostrarDialogoAccesoCajaRestringido(
                                    titulo = "No se pudo cargar las cajas",
                                    mensaje = "No pudimos verificar si alguna cuenta bloqueada tiene un turno abierto. Intenta nuevamente."
                                )
                            }
                    }
                    .addOnFailureListener {
                        progress.visibility = View.GONE
                        mostrarDialogoAccesoCajaRestringido(
                            titulo = "No se pudo cargar el personal",
                            mensaje = "No pudimos verificar las cajas disponibles en este momento. Intenta nuevamente."
                        )
                    }
            },
            onError = {
                progress.visibility = View.GONE
                mostrarDialogoAccesoCajaRestringido(
                    titulo = "No se pudo validar la caja",
                    mensaje = "No se pudo confirmar el estado actual de las cajas. Intenta nuevamente."
                )
            }
        )

        dialog.setOnShowListener {
            val bs = dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bs?.let { sheet ->
                val behavior = BottomSheetBehavior.from(sheet)
                behavior.skipCollapsed = true
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        dialogoSelectorCajerasActual = dialog
        dialog.show()
    }

    private fun mostrarDialogoAccesoCajaRestringido(
        titulo: String = "Acceso restringido",
        mensaje: String = "Tu rol de supervisor no tiene acceso a cajas de rango administrador."
    ) {
        if (!isAdded) return

        val dialogView = layoutInflater.inflate(R.layout.dialog_acceso_caja_restringido, null)
        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialogView.findViewById<TextView>(R.id.tvTituloAccesoCaja)?.text = titulo
        dialogView.findViewById<TextView>(R.id.tvMensajeAccesoCaja)?.text = mensaje
        dialogView.findViewById<TextView>(R.id.tvBotonEntendidoAccesoCaja)?.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
    }

    class CajeraViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreCajeraSelector)
        val tvRol: TextView = view.findViewById(R.id.tvRolCajeraSelector)
        val tvEstado: TextView = view.findViewById(R.id.tvEstadoCajeraSelector)
        val imgCheck: ImageView = view.findViewById(R.id.imgCheckCajera)
    }


    private fun getCajaRef(): DatabaseReference {
        // PRIORIDAD: Usar el ID en uso del SessionManager (Soporta Supervisión)
        val idAConsultar = obtenerIdCajaOperativa().ifEmpty {
            // Fallback a SharedPreferences si el SessionManager está vacío
            val prefs = requireActivity().getSharedPreferences("usuario", Context.MODE_PRIVATE)
            prefs.getString("id", "general") ?: "general"
        }
        return database.getReference("CajasIndividuales").child(idAConsultar)
    }

    private fun escucharProductosCaja() {
        // Remover listener anterior si existe
        cajaValueListener?.let { listener ->
            cajaRefListener?.removeEventListener(listener)
        }

        cajaRefListener = getCajaRef().child("Productos")

        cajaValueListener = object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                listaProductosCaja.clear()

                for (item in snapshot.children) {
                    val producto = item.getValue(ProductoCaja::class.java)
                    if (producto != null) {
                        listaProductosCaja.add(producto)
                        // RADAR DE STOCK: Activamos vigilancia en tiempo real para este producto
                        activarRadarStock(producto.indiceProducto)
                    }
                }

                if (!isAdded || _binding == null) return

                escucharInventarioProductosEnCaja()
                verificarDisponibilidadProductosEnCaja()
                marcarProductosInicialCajaListos()
            }

            override fun onCancelled(error: DatabaseError) {
                mostrarToastSeguro("Error al cargar productos")
                marcarProductosInicialCajaListos()
            }
        }

        cajaRefListener?.addValueEventListener(cajaValueListener!!)
    }

    private fun escucharEstadoTurnoCaja() {
        turnoCajaValueListener?.let { turnoCajaRefListener?.removeEventListener(it) }

        turnoCajaRefListener = getCorteCajaCajeraRef()
        turnoCajaValueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val b = _binding ?: return
                if (!isAdded) return

                // 1. Cancelar cualquier verificación programada previa (Reinicio del Debounce)
                runnableVerificarTurno?.let { handlerSeguridadOperaciones.removeCallbacks(it) }

                // 2. Determinar el retraso según el estado de la red
                // Si internetRecienRecuperado es true, damos 1500ms para que el offset de Firebase se estabilice
                val debounceMs = if (internetRecienRecuperado) 1500L else DEBOUNCE_VERIFICACION_TURNO_MS

                // 3. Programar la nueva verificación
                val runnable = Runnable {
                    if (_binding != null && isAdded) {
                        verificarTurnoActivo()
                    }
                }
                runnableVerificarTurno = runnable
                handlerSeguridadOperaciones.postDelayed(runnable, debounceMs)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CajaTurno", "Error en listener de turno: ${error.message}")
            }
        }

        turnoCajaRefListener?.addValueEventListener(turnoCajaValueListener!!)
    }

    // -- Fix #9: Total del turno en tiempo real -----------------------------------
    private fun escucharTotalTurnoEnTiempoReal() {
        totalTurnoListener?.let { totalTurnoRef?.removeEventListener(it) }
        totalTurnoRef = null
        totalTurnoListener = null
        val turnoRef = getTurnoActivoRef() ?: run {
            numeroVentasTurnoActual = 0L
            actualizarAyudaCajaVaciaUi()
            return
        }
        totalTurnoRef = turnoRef
        totalTurnoListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val b = _binding ?: return
                if (!isAdded) return
                val num = snapshot.child("numeroVentas").value?.toString()?.toLongOrNull() ?: 0L
                numeroVentasTurnoActual = num
                actualizarAyudaCajaVaciaUi()
                b.tvResumenTurnoRapido.visibility = View.GONE
            }
            override fun onCancelled(error: DatabaseError) {
                numeroVentasTurnoActual = 0L
                actualizarAyudaCajaVaciaUi()
            }
        }
        turnoRef.addValueEventListener(totalTurnoListener!!)
    }

    private fun escucharInventarioProductosEnCaja() {
        inventarioCajaValueListener?.let { listener ->
            inventarioCajaRefListener?.removeEventListener(listener)
        }
        inventarioCajaValueListener = null
        inventarioCajaRefListener = null

        if (listaProductosCaja.isEmpty()) return

        inventarioCajaRefListener = database.getReference("Inventario").child("Productos")
        inventarioCajaValueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || _binding == null || listaProductosCaja.isEmpty()) return
                verificarDisponibilidadProductosEnCaja(mostrarAvisoCambioExterno = true)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }

        inventarioCajaRefListener?.addValueEventListener(inventarioCajaValueListener!!)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun verificarDisponibilidadProductosEnCaja(mostrarAvisoCambioExterno: Boolean = false) {
        if (!isAdded || _binding == null) return

        if (listaProductosCaja.isEmpty()) {
            refrescarCarritoCompletoUi()
            actualizarTotales()
            mostrarEstadoVacio()
            return
        }

        val estadoPrevio = listaProductosCaja.associate { producto ->
            producto.id to producto.agotado
        }

        var huboCambioDePrecio = false
        var huboCambiosStock = false

        val requeridasPorIndice = listaProductosCaja
            .groupBy { it.indiceProducto.trim() }
            .mapValues { (_, productos) ->
                productos.sumOf { item ->
                    val cantidad = item.cantidad.toIntSeguro(0)
                    val unidades = item.unidadesPorPresentacion.toIntSeguro(1)
                    cantidad * unidades
                }
            }

        // 1. MEJORA: Lista para rastrear las posiciones (índices) modificadas
        val indicesCambiados = mutableListOf<Int>()

        // Cambiamos a forEachIndexed para conocer la posición exacta de cada producto
        listaProductosCaja.forEachIndexed { index, producto ->
            val clave = producto.indiceProducto.trim()
            val stockDisponible = cacheStockInventario[clave] ?: producto.stockDisponibleActual.toIntOrNull() ?: 0
            val unidadesRequeridasTotal = requeridasPorIndice[clave] ?: 0

            var itemModificado = false

            // -- Sincronización de Precio --
            val preciosProducto = cachePreciosInventario[clave]
            val precioActualizado = preciosProducto?.get(producto.presentacion)
            if (precioActualizado != null) {
                val precioLocal = producto.precioUnitario.toDoubleOrNull() ?: 0.0
                if (Math.abs(precioLocal - precioActualizado) > 0.001) {
                    producto.precioUnitario = String.format(Locale.US, "%.2f", precioActualizado)
                    val cant = producto.cantidad.toIntOrNull() ?: 0
                    val nuevoTotal = CalculadoraVentasHelper.calcularTotalConDescuento(
                        cant, precioActualizado, producto.descuento, producto.tipoDescuento
                    )
                    producto.total = String.format(Locale.US, "%.2f", nuevoTotal)

                    getCajaRef().child("Productos").child(producto.id).updateChildren(
                        mapOf(
                            "precioUnitario" to producto.precioUnitario,
                            "total" to producto.total
                        )
                    )
                    huboCambioDePrecio = true
                    itemModificado = true
                }
            }

            if (producto.stockDisponibleActual != stockDisponible.toString()) {
                producto.stockDisponibleActual = stockDisponible.toString()
                itemModificado = true
            }

            val unidadesPorPresentacion = producto.unidadesPorPresentacion.toIntSeguro(1)
            val unidadesSolicitadasItem = producto.cantidad.toIntSeguro(0) * unidadesPorPresentacion

            val sinStockTotal = stockDisponible <= 0
            val presentacionNoAlcanza = stockDisponible in 1 until unidadesPorPresentacion
            val cantidadExcedida = stockDisponible in unidadesPorPresentacion until unidadesSolicitadasItem

            val estabaAgotado = producto.agotado
            producto.agotado = stockDisponible < unidadesRequeridasTotal || sinStockTotal

            if (estabaAgotado != producto.agotado) {
                huboCambiosStock = true
                itemModificado = true
            }

            val nuevoMensaje = when {
                !producto.agotado -> ""
                sinStockTotal -> "Sin stock"
                presentacionNoAlcanza -> "Esta presentación ya no alcanza"
                cantidadExcedida -> "Usa menos cantidad u otra presentación"
                else -> "Stock total insuficiente ($stockDisponible disponibles)"
            }

            if (producto.mensajeStockActual != nuevoMensaje) {
                producto.mensajeStockActual = nuevoMensaje
                itemModificado = true
            }

            // Si el ítem sufrió algún cambio visual, guardamos su índice
            if (itemModificado) {
                indicesCambiados.add(index)
            }
        }

        // 2. SOLUCIÓN AL PARPADEO: Reemplazamos refrescarCarritoCompletoUi() por refresco inteligente
        if (indicesCambiados.isNotEmpty()) {
            // Si hay ítems específicos modificados, refrescamos solo esas posiciones
            refrescarItemsCarrito(indicesCambiados)
        } else if (!mostrarAvisoCambioExterno) {
            // Si es una carga inicial completa del carrito (no un cambio externo de stock),
            // usamos notifyItemRangeChanged. Es infinitamente más suave que notifyDataSetChanged()
            sincronizarUnidadesTotalesCarrito()
            adapter.notifyItemRangeChanged(0, listaProductosCaja.size)
        }

        actualizarTotales()

        // 3. Notificaciones proactivas ante cambios externos...
        if (mostrarAvisoCambioExterno && !actualizandoCaja && !ventaEnProceso) {
            if (huboCambioDePrecio) {
                feedbackVm.notificarError(_binding?.root)
                val mensajePrecio = "Algunos precios de tu carrito fueron actualizados desde la base de datos central."
                abortarCobroPorCambioDeCarrito(mensajePrecio)
                if (_binding?.layoutCobroPanel?.visibility != View.VISIBLE) {
                    mostrarToastSeguro("¡Atención! $mensajePrecio")
                }
            }

            val productosAfectados = listaProductosCaja.filter { producto ->
                producto.agotado && estadoPrevio[producto.id] != true
            }

            if (productosAfectados.isNotEmpty()) {
                val primerAfectado = productosAfectados.first()
                feedbackVm.notificarError(_binding?.root)
                val posicion = listaProductosCaja.indexOf(primerAfectado)
                if (posicion != -1) {
                    _binding?.recyclerView2?.post {
                        _binding?.recyclerView2?.smoothScrollToPosition(posicion)
                    }
                }

                val mensajeStock = "El producto '${primerAfectado.nombre}' acaba de quedarse sin stock suficiente debido a una venta en otra caja."
                abortarCobroPorCambioDeCarrito(mensajeStock)

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("⚠️ Alerta de Inventario")
                    .setMessage("$mensajeStock\n\nPor favor, ajusta la cantidad o retíralo del carrito para poder continuar.")
                    .setPositiveButton("Entendido") { dialog, _ -> dialog.dismiss() }
                    .setCancelable(false)
                    .show()
            }
        }
    }

    private fun obtenerProductosAgotadosEnCaja(): List<ProductoCaja> {
        val unidadesTotalesPorProducto = construirUnidadesTotalesPorProductoEnCaja()
        return listaProductosCaja.filter { producto ->
            // Agotado por flag directo de Firebase
            if (producto.agotado) return@filter true
            // Validación en tiempo real con el Radar: total en carrito supera stock real
            val clave = producto.indiceProducto.trim()
            val stockReal = cacheStockInventario[clave]
                ?: producto.stockDisponibleActual.toIntOrNull()
                ?: return@filter false
            val unidadesTotalesEnCarrito = unidadesTotalesPorProducto[clave] ?: 0
            unidadesTotalesEnCarrito > stockReal
        }
    }

    private fun hayProductosAgotadosEnCaja(): Boolean {
        return obtenerProductosAgotadosEnCaja().isNotEmpty()
    }

    private fun validarStockFinalAntesDeContinuar(continuarOverlay: Boolean = false, onStockOk: (List<ProductoCaja>) -> Unit) {
        if (validandoStockFinalEnCurso) return
        validandoStockFinalEnCurso = true
        mostrarDialogoProgresoVenta(
            titulo = "Verificando stock",
            mensaje = "Revisando disponibilidad actual antes de continuar..."
        )

        obtenerProductosCajaActual(
            onSuccess = { productosCajaActuales ->
                if (!isAdded || _binding == null) {
                    validandoStockFinalEnCurso = false
                    ocultarDialogoProgresoVenta()
                    return@obtenerProductosCajaActual
                }

                if (productosCajaActuales.isEmpty()) {
                    validandoStockFinalEnCurso = false
                    ocultarDialogoProgresoVenta()
                    onStockOk(emptyList())
                    return@obtenerProductosCajaActual
                }

                // SOLO UNA LLAMADA a get()
                database.getReference("Inventario")
                    .child("Productos")
                    .get()
                    .addOnSuccessListener { inventarioSnapshot ->
                        validandoStockFinalEnCurso = false

                        if (!isAdded || _binding == null) return@addOnSuccessListener

                        actualizarCacheStockDesdeValidacionFinal(
                            productosCaja = productosCajaActuales,
                            inventarioSnapshot = inventarioSnapshot
                        )

                        val conflicto = construirPrimerConflictoStockFinal(
                            productosCaja = productosCajaActuales,
                            inventarioSnapshot = inventarioSnapshot
                        )

                        if (conflicto == null) {
                            if (continuarOverlay) {
                                // ✅ Usar actualizarLoadingOverlayCaja (solo cambia el texto)
                                actualizarLoadingOverlayCaja(
                                    titulo = "Procesando venta",
                                    mensaje = "Validando la venta y evitando cobros duplicados..."
                                )
                            } else {
                                ocultarDialogoProgresoVenta()
                            }
                            onStockOk(productosCajaActuales)
                            return@addOnSuccessListener
                        }

                        // Si hay conflicto, ocultamos el overlay y mostramos resolución
                        ocultarDialogoProgresoVenta()
                        feedbackVm.notificarError(_binding?.buttoncobrar ?: _binding?.root)
                        mostrarOverlayResolucionStockFinal(
                            conflicto = conflicto,
                            onResolved = {
                                validarStockFinalAntesDeContinuar(continuarOverlay = false, onStockOk)
                            },
                            onReview = {
                                soyYoElQueCobra = false
                                verificarDisponibilidadProductosEnCaja()
                                liberarCajaSiEstabaBloqueada()
                            }
                        )
                    }
                    .addOnFailureListener { e ->
                        validandoStockFinalEnCurso = false
                        ocultarDialogoProgresoVenta()
                        feedbackVm.notificarError(_binding?.buttoncobrar ?: _binding?.root)
                        mostrarToastSeguro(
                            e.message ?: "No se pudo verificar el stock actual. Intenta nuevamente."
                        )
                        soyYoElQueCobra = false
                        liberarCajaSiEstabaBloqueada()
                    }
            },
            onError = { mensaje ->
                validandoStockFinalEnCurso = false
                ocultarDialogoProgresoVenta()
                feedbackVm.notificarError(_binding?.buttoncobrar ?: _binding?.root)
                mostrarToastSeguro(mensaje)
                soyYoElQueCobra = false
                liberarCajaSiEstabaBloqueada()
            }
        )
    }

    private fun actualizarLoadingOverlayCaja(titulo: String, mensaje: String) {
        val b = _binding ?: return
        b.tvTituloLoadingCaja.text = titulo
        b.tvMensajeLoadingCaja.text = mensaje
    }

    private fun actualizarCacheStockDesdeValidacionFinal(
        productosCaja: List<ProductoCaja>,
        inventarioSnapshot: DataSnapshot
    ) {
        productosCaja
            .map { it.indiceProducto.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .forEach { indice ->
                cacheStockInventario[indice] =
                    obtenerStockDisponibleSnapshot(inventarioSnapshot.child(indice))
            }
    }

    private fun construirPrimerConflictoStockFinal(
        productosCaja: List<ProductoCaja>,
        inventarioSnapshot: DataSnapshot
    ): ConflictoStockFinal? {
        for (producto in productosCaja) {
            val indiceProducto = producto.indiceProducto.trim()
            if (indiceProducto.isBlank()) {
                return ConflictoStockFinal(
                    producto = producto,
                    tipo = TipoConflictoStockFinal.ELIMINAR_PRODUCTO,
                    stockDisponibleParaEsteItem = 0
                )
            }

            val snapshotProducto = inventarioSnapshot.child(indiceProducto)
            val productoInventario = snapshotProducto.toMoldeProductoSeguro()
            val stockTotal = obtenerStockDisponibleSnapshot(snapshotProducto)
            val unidadesPorPresentacionActual =
                producto.unidadesPorPresentacion.toIntSeguro(1).coerceAtLeast(1)
            val cantidadActual = producto.cantidad.toIntSeguro(0).coerceAtLeast(0)
            val unidadesSolicitadas = cantidadActual * unidadesPorPresentacionActual

            val unidadesOtrosItems = productosCaja
                .asSequence()
                .filter { it.indiceProducto.trim() == indiceProducto && it.id != producto.id }
                .sumOf { item ->
                    item.cantidad.toIntSeguro(0) *
                        item.unidadesPorPresentacion.toIntSeguro(1).coerceAtLeast(1)
                }

            val stockDisponibleParaEsteItem = (stockTotal - unidadesOtrosItems).coerceAtLeast(0)
            if (stockTotal > 0 && stockDisponibleParaEsteItem >= unidadesSolicitadas) continue

            val cantidadSugerida = if (stockDisponibleParaEsteItem > 0) {
                (stockDisponibleParaEsteItem / unidadesPorPresentacionActual).coerceAtMost(cantidadActual)
            } else {
                0
            }

            if (cantidadSugerida in 1 until cantidadActual) {
                return ConflictoStockFinal(
                    producto = producto,
                    tipo = TipoConflictoStockFinal.AJUSTAR_CANTIDAD,
                    stockDisponibleParaEsteItem = stockDisponibleParaEsteItem,
                    cantidadSugerida = cantidadSugerida
                )
            }

            val opcionSugerida = productoInventario
                ?.let { inventario ->
                    construirOpcionesCorreccionPresentacion(
                        productoCaja = producto,
                        productoInventario = inventario,
                        stockDisponibleParaEsteItem = stockDisponibleParaEsteItem
                    ).firstOrNull { opcion ->
                        normalizarNombrePresentacion(opcion.nombrePresentacionGuardada) !=
                            normalizarNombrePresentacion(producto.presentacion)
                    }
                }

            if (opcionSugerida != null) {
                return ConflictoStockFinal(
                    producto = producto,
                    tipo = TipoConflictoStockFinal.CAMBIAR_PRESENTACION,
                    stockDisponibleParaEsteItem = stockDisponibleParaEsteItem,
                    opcionPresentacionSugerida = opcionSugerida
                )
            }

            return ConflictoStockFinal(
                producto = producto,
                tipo = TipoConflictoStockFinal.ELIMINAR_PRODUCTO,
                stockDisponibleParaEsteItem = stockDisponibleParaEsteItem
            )
        }

        return null
    }

    private fun obtenerStockDisponibleSnapshot(snapshot: DataSnapshot): Int {
        return when (val raw = snapshot.child("cantidadinicial").value) {
            is Long -> raw.toInt()
            is Int -> raw
            is Double -> raw.toInt()
            is String -> raw.trim().toIntOrNull() ?: 0
            else -> 0
        }
    }

    private fun obtenerPresentacionActualProductoUi(producto: ProductoCaja): String {
        return PresentacionHelper.formatearPresentacionGuardadaParaUi(producto.presentacion)
            .ifBlank { producto.presentacion.trim() }
    }

    private fun mostrarOverlayResolucionStockFinal(conflicto: ConflictoStockFinal, onResolved: () -> Unit, onReview: () -> Unit) {
        val ctx = context ?: run {
            soyYoElQueCobra = false
            onReview()
            return
        }

        dialogoResolucionStockActual?.dismiss()
        val view = layoutInflater.inflate(R.layout.dialog_resolucion_stock_caja, null)
        val tvBadge = view.findViewById<TextView>(R.id.tvBadgeResolucionStock)
        val tvProductoAnimacion = view.findViewById<TextView>(R.id.tvProductoAnimacionResolucionStock)
        val imgPapelera = view.findViewById<ImageView>(R.id.imgPapeleraResolucionStock)
        val tvPresentacionActual = view.findViewById<TextView>(R.id.tvPresentacionActualResolucionStock)
        val tvTitulo = view.findViewById<TextView>(R.id.tvTituloResolucionStock)
        val tvMensaje = view.findViewById<TextView>(R.id.tvMensajeResolucionStock)
        val tvSugerencia = view.findViewById<TextView>(R.id.tvSugerenciaResolucionStock)
        val tvDetalle = view.findViewById<TextView>(R.id.tvDetalleResolucionStock)
        val btnPrincipal = view.findViewById<MaterialButton>(R.id.btnResolverStockPrincipal)
        val btnRevisar = view.findViewById<MaterialButton>(R.id.btnResolverStockSecundario)

        val producto = conflicto.producto
        val nombreProducto = producto.nombre.orEmpty().ifBlank { "Producto" }
        val presentacionActual = obtenerPresentacionActualProductoUi(producto)
        val cantidadActual = producto.cantidad.toIntSeguro(0).coerceAtLeast(0)

        tvProductoAnimacion.text = nombreProducto
        tvPresentacionActual.text = if (presentacionActual.isNotBlank()) {
            "Actual: $presentacionActual"
        } else {
            ""
        }
        tvPresentacionActual.visibility =
            if (tvPresentacionActual.text.isNullOrBlank()) View.GONE else View.VISIBLE

        when (conflicto.tipo) {
            TipoConflictoStockFinal.AJUSTAR_CANTIDAD -> {
                tvBadge.text = "Cantidad disponible"
                tvBadge.background = ContextCompat.getDrawable(ctx, R.drawable.bg_pill_warning)
                tvBadge.setTextColor(Color.parseColor("#B54708"))
                imgPapelera.visibility = View.GONE

                tvTitulo.text = "Solo quedan ${conflicto.cantidadSugerida} disponibles"
                tvMensaje.text =
                    "Tenias $cantidadActual en el carrito. Esta cantidad si esta disponible para continuar sin errores."
                tvSugerencia.visibility = View.VISIBLE
                tvSugerencia.background = ContextCompat.getDrawable(ctx, R.drawable.bg_pill_green_soft)
                tvSugerencia.setTextColor(Color.parseColor("#047857"))
                tvSugerencia.text = "Ajustaremos a ${conflicto.cantidadSugerida}"
                tvDetalle.visibility = View.VISIBLE
                tvDetalle.text = "Quedaran ${conflicto.cantidadSugerida} unidades de esta seleccion."
                btnPrincipal.text = "Ajustar a ${conflicto.cantidadSugerida}"
                btnPrincipal.setOnClickListener {
                    btnPrincipal.isEnabled = false
                    btnRevisar.isEnabled = false
                    ajustarCantidadProductoEnCaja(producto, conflicto.cantidadSugerida) { exito ->
                        dialogoResolucionStockActual?.dismiss()
                        if (exito) {
                            onResolved()
                        } else {
                            onReview()
                        }
                    }
                }
            }

            TipoConflictoStockFinal.CAMBIAR_PRESENTACION -> {
                val sugerida = conflicto.opcionPresentacionSugerida ?: run {
                    onReview()
                    return
                }

                tvBadge.text = "Cambio sugerido"
                tvBadge.background = ContextCompat.getDrawable(ctx, R.drawable.bg_pill_warning)
                tvBadge.setTextColor(Color.parseColor("#B54708"))
                imgPapelera.visibility = View.GONE

                tvTitulo.text = "Ya no queda esta presentacion"
                tvMensaje.text =
                    "La presentacion actual ya no alcanza con el stock real. Te sugiero esta opcion para seguir sin errores."
                tvSugerencia.visibility = View.VISIBLE
                tvSugerencia.background = ContextCompat.getDrawable(ctx, R.drawable.bg_pill_green_soft)
                tvSugerencia.setTextColor(Color.parseColor("#047857"))
                tvSugerencia.text = "Te sugiero: ${sugerida.nombreUi}"
                tvDetalle.visibility = View.VISIBLE
                tvDetalle.text = sugerida.detalleUi
                btnPrincipal.text = "Cambiar a ${sugerida.nombreUi}"
                btnPrincipal.setOnClickListener {
                    btnPrincipal.isEnabled = false
                    btnRevisar.isEnabled = false
                    actualizarPresentacionProductoEnCaja(producto, sugerida) { exito, productoActualizado ->
                        if (exito && productoActualizado != null) {
                            registrarMovimientoCambioPresentacionCaja(
                                productoAnterior = producto,
                                productoActualizado = productoActualizado,
                                opcion = sugerida
                            )
                        }
                        dialogoResolucionStockActual?.dismiss()
                        if (exito) {
                            onResolved()
                        } else {
                            onReview()
                        }
                    }
                }
            }

            TipoConflictoStockFinal.ELIMINAR_PRODUCTO -> {
                tvBadge.text = "Producto agotado"
                tvBadge.background = ContextCompat.getDrawable(ctx, R.drawable.bg_pill_error)
                tvBadge.setTextColor(Color.parseColor("#B42318"))
                imgPapelera.visibility = View.VISIBLE

                tvTitulo.text = "Producto ya no disponible"
                tvMensaje.text =
                    "Ya no queda disponible. Presiona confirmar para quitarlo del carrito y continuar sin errores."
                tvSugerencia.visibility = View.GONE
                tvDetalle.visibility = View.VISIBLE
                tvDetalle.text = "El producto se quitara con seguridad de la caja."
                btnPrincipal.text = "Confirmar y quitar"
                btnPrincipal.setOnClickListener {
                    btnPrincipal.isEnabled = false
                    btnRevisar.isEnabled = false
                    feedbackVm.notificarAccionDestructiva(btnPrincipal)
                    animarProductoHaciaPapelera(
                        chipProducto = tvProductoAnimacion,
                        papelera = imgPapelera
                    ) {
                        eliminarProducto(producto) { exito ->
                            dialogoResolucionStockActual?.dismiss()
                            if (exito) {
                                onResolved()
                            } else {
                                onReview()
                            }
                        }
                    }
                }
            }
        }

        val dialog = MaterialAlertDialogBuilder(ctx)
            .setView(view)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnDismissListener {
            if (dialogoResolucionStockActual === dialog) {
                dialogoResolucionStockActual = null
            }
            validandoStockFinalEnCurso = false
        }

        btnRevisar.setOnClickListener {
            dialog.dismiss()
            onReview()
        }

        dialogoResolucionStockActual = dialog
        dialog.show()
    }

    private fun animarProductoHaciaPapelera(
        chipProducto: TextView,
        papelera: ImageView,
        onEnd: () -> Unit
    ) {
        chipProducto.animate().cancel()
        papelera.animate().cancel()
        chipProducto.alpha = 1f
        chipProducto.scaleX = 1f
        chipProducto.scaleY = 1f
        chipProducto.translationX = 0f
        chipProducto.translationY = 0f
        papelera.alpha = 1f
        papelera.scaleX = 1f
        papelera.scaleY = 1f

        chipProducto.post {
            val chipLoc = IntArray(2)
            val papeleraLoc = IntArray(2)
            chipProducto.getLocationOnScreen(chipLoc)
            papelera.getLocationOnScreen(papeleraLoc)

            val chipCentroX = chipLoc[0] + (chipProducto.width / 2f)
            val chipCentroY = chipLoc[1] + (chipProducto.height / 2f)
            val papeleraCentroX = papeleraLoc[0] + (papelera.width / 2f)
            val papeleraCentroY = papeleraLoc[1] + (papelera.height / 2f)

            val deltaX = papeleraCentroX - chipCentroX
            val deltaY = papeleraCentroY - chipCentroY

            AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(chipProducto, View.TRANSLATION_X, 0f, deltaX),
                    ObjectAnimator.ofFloat(chipProducto, View.TRANSLATION_Y, 0f, deltaY),
                    ObjectAnimator.ofFloat(chipProducto, View.SCALE_X, 1f, 0.34f),
                    ObjectAnimator.ofFloat(chipProducto, View.SCALE_Y, 1f, 0.34f),
                    ObjectAnimator.ofFloat(chipProducto, View.ALPHA, 1f, 0.08f)
                )
                duration = 340L
                interpolator = android.view.animation.AccelerateDecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        chipProducto.visibility = View.INVISIBLE
                        papelera.animate()
                            .scaleX(1.14f)
                            .scaleY(1.14f)
                            .setDuration(120L)
                            .withEndAction {
                                papelera.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(160L)
                                    .setInterpolator(android.view.animation.OvershootInterpolator(1.7f))
                                    .withEndAction(onEnd)
                                    .start()
                            }
                            .start()
                    }
                })
            }.start()
        }
    }

    private fun mostrarMensajeProductosAgotadosEnCaja() {
        val primerProducto = obtenerProductosAgotadosEnCaja().firstOrNull() ?: return
        val stockDisponible = primerProducto.stockDisponibleActual.toIntOrNull() ?: 0
        feedbackVm.notificarError(_binding?.root)
        mostrarToastSeguro(
            "No puedes continuar. ${primerProducto.nombre} tiene un problema de stock. Quedan $stockDisponible unidades disponibles"
        )
    }

    private fun mostrarBottomSheetCorreccionPresentacion(producto: ProductoCaja) {
        if (estaCajaEnModoSoloVista(notificar = true)) return
        if (bloquearOperacionPorRescateCajaBloqueada()) return
        val indiceProducto = producto.indiceProducto.trim()
        if (indiceProducto.isBlank()) {
            mostrarToastSeguro("No se pudo revisar este producto")
            return
        }

        dialogoCambioPresentacionActual?.dismiss()
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_cambiar_presentacion_caja, null)
        dialog.setContentView(view)
        dialog.setOnDismissListener {
            if (dialogoCambioPresentacionActual === dialog) {
                dialogoCambioPresentacionActual = null
            }
        }

        val tvTitulo = view.findViewById<TextView>(R.id.tvTituloCambiarPresentacionCaja)
        val tvMensaje = view.findViewById<TextView>(R.id.tvMensajeCambiarPresentacionCaja)
        val layoutOpciones = view.findViewById<LinearLayout>(R.id.layoutOpcionesCambiarPresentacionCaja)
        val btnCerrar = view.findViewById<MaterialButton>(R.id.btnCerrarCambiarPresentacionCaja)

        val nombreProducto = producto.nombre.orEmpty().ifBlank { "este producto" }
        val presentacionActual = PresentacionHelper.formatearPresentacionGuardadaParaUi(producto.presentacion)
            .ifBlank { producto.presentacion }
        tvTitulo.text = "Cambiar presentación"
        tvMensaje.text = buildString {
            append("Producto: $nombreProducto")
            if (presentacionActual.isNotBlank()) append("\nActual: $presentacionActual")
            append("\n\nElige la presentación que quieres usar:")
        }
        btnCerrar.setOnClickListener { dialog.dismiss() }

        val cajaRef = getCajaRef().child("Productos")
        val inventarioRef = database.getReference("Inventario").child("Productos").child(indiceProducto)

        cajaRef.get().addOnSuccessListener { cajaSnapshot ->
            inventarioRef.get().addOnSuccessListener { inventarioSnapshot ->
                if (estaCajaEnModoSoloVista()) {
                    dialog.dismiss()
                    return@addOnSuccessListener
                }
                val productoInventario = inventarioSnapshot.toMoldeProductoSeguro()
                if (productoInventario == null) {
                    dialog.dismiss()
                    mostrarToastSeguro("Ya no se encontro el producto en inventario")
                    return@addOnSuccessListener
                }

                val stockTotal = productoInventario.cantidadinicial.toIntSeguro(0)
                var unidadesOtrosItems = 0

                for (child in cajaSnapshot.children) {
                    val itemCaja = child.getValue(ProductoCaja::class.java) ?: continue
                    if (itemCaja.indiceProducto.trim() != indiceProducto) continue
                    if (itemCaja.id == producto.id) continue

                    val cantidad = itemCaja.cantidad.toIntSeguro(0)
                    val unidades = itemCaja.unidadesPorPresentacion.toIntSeguro(1)
                    unidadesOtrosItems += cantidad * unidades
                }

                val stockDisponibleParaEsteItem = (stockTotal - unidadesOtrosItems).coerceAtLeast(0)
                val opciones = construirOpcionesCorreccionPresentacion(
                    productoCaja = producto,
                    productoInventario = productoInventario,
                    stockDisponibleParaEsteItem = stockDisponibleParaEsteItem
                )

                layoutOpciones.removeAllViews()

                if (opciones.isEmpty()) {
                    dialog.dismiss()
                    mostrarDialogoProductoAgotadoSinOpciones(producto)
                    return@addOnSuccessListener
                }

                opciones.forEach { opcion ->
                    val itemView = layoutInflater.inflate(R.layout.item_opcion_presentacion_caja, layoutOpciones, false)
                    val card = itemView.findViewById<MaterialCardView>(R.id.cardOpcionPresentacionCaja)
                    val tvNombre = itemView.findViewById<TextView>(R.id.tvNombreOpcionPresentacionCaja)
                    val tvDetalle = itemView.findViewById<TextView>(R.id.tvDetalleOpcionPresentacionCaja)
                    val btnUsar = itemView.findViewById<MaterialButton>(R.id.btnUsarOpcionPresentacionCaja)

                    tvNombre.text = opcion.nombreUi
                    tvDetalle.text = opcion.detalleUi

                    val aplicar = {
                        aplicarCorreccionPresentacionCaja(producto, opcion, dialog)
                    }

                    card.setOnClickListener { aplicar() }
                    btnUsar.setOnClickListener { aplicar() }
                    layoutOpciones.addView(itemView)
                }

                dialog.setOnShowListener {
                    val sheet = dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                    sheet?.let { bottomSheet ->
                        BottomSheetBehavior.from(bottomSheet).apply {
                            skipCollapsed = true
                            state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }
                }

                dialogoCambioPresentacionActual = dialog
                dialog.show()
            }.addOnFailureListener {
                dialog.dismiss()
                mostrarToastSeguro("No se pudo revisar el inventario actual")
            }
        }.addOnFailureListener {
            dialog.dismiss()
            mostrarToastSeguro("No se pudo revisar tu caja actual")
        }
    }

    private fun construirOpcionesCorreccionPresentacion(productoCaja: ProductoCaja, productoInventario: MoldeProductos, stockDisponibleParaEsteItem: Int): List<OpcionCorreccionPresentacionCaja> {
        if (stockDisponibleParaEsteItem <= 0) return emptyList()

        val cantidadActual = productoCaja.cantidad.toIntSeguro(0)
        val unidadBase = productoInventario.unidadbase.ifBlank { "unidades" }
        val nombreActualNormalizado = normalizarNombrePresentacion(productoCaja.presentacion)
        val opciones = mutableListOf<OpcionCorreccionPresentacionCaja>()
        val llavesVistas = mutableSetOf<String>()

        val presentacionesDisponibles = if (productoInventario.tienePresentaciones && productoInventario.presentaciones.isNotEmpty()) {
            productoInventario.presentaciones.toList()
        } else {
            listOf(
                PresentacionProducto(
                    nombre = productoInventario.presentacionprincipal.ifBlank { unidadBase },
                    cantidad = 1,
                    precioventa = productoInventario.preciodecompra.toDoubleSeguro(0.0) ?: 0.0
                )
            )
        }

        presentacionesDisponibles.forEach { presentacion ->
            val unidades = if (presentacion.cantidad <= 0) 1 else presentacion.cantidad
            val maxCantidad = stockDisponibleParaEsteItem / unidades
            if (maxCantidad <= 0) return@forEach

            val cantidadSugerida = when {
                cantidadActual <= 0 -> 1
                maxCantidad >= cantidadActual -> cantidadActual
                else -> maxCantidad
            }
            if (cantidadSugerida <= 0) return@forEach

            val nombreUi = presentacion.nombre.trim()
                .ifBlank { unidadBase }
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            val nombreGuardado = construirNombrePresentacionGuardada(nombreUi, unidades, unidadBase)
            val llave = "${normalizarNombrePresentacion(nombreGuardado)}_$cantidadSugerida"
            if (!llavesVistas.add(llave)) return@forEach

            val detalleBase = if (normalizarNombrePresentacion(nombreGuardado) == nombreActualNormalizado) {
                "Misma presentacion con hasta $cantidadSugerida disponible${if (cantidadSugerida > 1) "s" else ""}"
            } else {
                "Hasta $cantidadSugerida disponible${if (cantidadSugerida > 1) "s" else ""} en esta presentacion"
            }
            val detallePrecio = " · ${MonedaHelper.formatear(presentacion.precioventa)}"
            val detalleUnidades = " · ${unidades} $unidadBase"

            opciones.add(
                OpcionCorreccionPresentacionCaja(
                    nombreUi = nombreUi,
                    detalleUi = detalleBase + detalleUnidades + detallePrecio,
                    nombrePresentacionGuardada = nombreGuardado,
                    unidadesPorPresentacion = unidades,
                    precioUnitario = presentacion.precioventa,
                    cantidadSugerida = cantidadSugerida
                )
            )
        }

        return opciones.sortedWith(
            compareByDescending<OpcionCorreccionPresentacionCaja> {
                normalizarNombrePresentacion(it.nombrePresentacionGuardada) == nombreActualNormalizado
            }.thenByDescending { it.unidadesPorPresentacion }
        )
    }

    private fun actualizarPresentacionProductoEnCaja(
        producto: ProductoCaja,
        opcion: OpcionCorreccionPresentacionCaja,
        onComplete: (Boolean, ProductoCaja?) -> Unit
    ) {
        if (bloquearOperacionPorRescateCajaBloqueada()) {
            onComplete(false, null)
            return
        }
        actualizarEstadoActualizandoCaja(true)
        val itemCajaRef = getCajaRef().child("Productos").child(producto.id)

        itemCajaRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val productoActual = currentData.getValue(ProductoCaja::class.java)
                    ?: return Transaction.abort()

                // Preservar descuento existente al cambiar presentación
                val totalConDescuento = CalculadoraVentasHelper.calcularTotalConDescuento(
                    opcion.cantidadSugerida,
                    opcion.precioUnitario,
                    productoActual.descuento,
                    productoActual.tipoDescuento
                )
                val productoActualizado = productoActual.copy(
                    cantidad = opcion.cantidadSugerida.toString(),
                    presentacion = opcion.nombrePresentacionGuardada,
                    unidadesPorPresentacion = opcion.unidadesPorPresentacion.toString(),
                    precioUnitario = String.format(Locale.US, "%.2f", opcion.precioUnitario),
                    total = String.format(Locale.US, "%.2f", totalConDescuento)
                )

                currentData.value = productoActualizado
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                actualizarEstadoActualizandoCaja(false)
                onComplete(
                    error == null && committed,
                    currentData?.getValue(ProductoCaja::class.java)
                )
            }
        })
    }

    private fun aplicarCorreccionPresentacionCaja(
        producto: ProductoCaja,
        opcion: OpcionCorreccionPresentacionCaja,
        dialog: BottomSheetDialog
    ) {
        actualizarPresentacionProductoEnCaja(producto, opcion) { exito, productoActualizado ->
            if (!exito || productoActualizado == null) {
                mostrarToastSeguro("No se pudo corregir la presentación en caja")
                return@actualizarPresentacionProductoEnCaja
            }

            registrarMovimientoCambioPresentacionCaja(
                productoAnterior = producto,
                productoActualizado = productoActualizado,
                opcion = opcion
            )
            dialog.dismiss()
            mostrarToastSeguro("Cambiamos a ${opcion.nombreUi} para que puedas continuar")
        }
    }

    private fun ajustarCantidadProductoEnCaja(
        producto: ProductoCaja,
        nuevaCantidad: Int,
        onComplete: (Boolean) -> Unit
    ) {
        if (bloquearOperacionPorRescateCajaBloqueada()) {
            onComplete(false)
            return
        }
        if (producto.id.isBlank() || nuevaCantidad <= 0) {
            onComplete(false)
            return
        }

        actualizarEstadoActualizandoCaja(true)
        val itemCajaRef = getCajaRef().child("Productos").child(producto.id)

        itemCajaRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val productoActual = currentData.getValue(ProductoCaja::class.java)
                    ?: return Transaction.abort()

                val precioUnitario = productoActual.precioUnitario.toDoubleOrNull() ?: 0.0
                val nuevoTotal = CalculadoraVentasHelper.calcularTotalConDescuento(
                    nuevaCantidad,
                    precioUnitario,
                    productoActual.descuento,
                    productoActual.tipoDescuento
                )

                productoActual.cantidad = nuevaCantidad.toString()
                productoActual.total = String.format(Locale.US, "%.2f", nuevoTotal)
                currentData.value = productoActual
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                actualizarEstadoActualizandoCaja(false)
                if (error != null || !committed) {
                    mostrarToastSeguro("No se pudo ajustar la cantidad del producto")
                    onComplete(false)
                    return
                }
                onComplete(true)
            }
        })
    }

    private fun mostrarDialogoProductoAgotadoSinOpciones(producto: ProductoCaja) {
        val view = layoutInflater.inflate(R.layout.dialog_producto_agotado_caja, null)
        val tvTitulo = view.findViewById<TextView>(R.id.tvTituloProductoAgotadoCaja)
        val tvMensaje = view.findViewById<TextView>(R.id.tvMensajeProductoAgotadoCaja)
        val btnEliminar = view.findViewById<MaterialButton>(R.id.btnEliminarProductoAgotadoCaja)
        val btnCancelar = view.findViewById<MaterialButton>(R.id.btnCancelarProductoAgotadoCaja)

        tvTitulo.text = producto.nombre.orEmpty().ifBlank { "Producto sin stock" }
        tvMensaje.text = "Este producto ya no tiene una presentaci\u00f3n disponible para continuar. Lo mejor es quitarlo de la caja para seguir con la venta sin errores."

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        btnEliminar.setOnClickListener {
            dialog.dismiss()
            feedbackVm.notificarAccionDestructiva(btnEliminar)
            eliminarProducto(producto)
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun construirNombrePresentacionGuardada(nombrePresentacion: String, unidadesPorPresentacion: Int, unidadBase: String): String {
        return PresentacionHelper.resumenPresentacionUi(
            nombrePresentacion,
            unidadesPorPresentacion,
            unidadBase
        )
    }

    private fun normalizarNombrePresentacion(texto: String): String {
        return PresentacionHelper.normalizarNombreBase(texto)
    }

    private fun escucharEstadoCobro() {
        estadoCobroListener?.let { listener ->
            estadoCobroRefGlobal?.removeEventListener(listener)
        }

        estadoCobroRefGlobal = database.getReference("CajasIndividuales")
            .child(obtenerIdCajaOperativa())
            .child("estadoCobro")

        estadoCobroListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || _binding == null) return

                // Obtenemos quién bloqueó la caja (guardaremos el ID del cajero)
                val idBloqueador = snapshot.getValue(String::class.java)
                val miId = idCajera // ID del usuario actual

                // Si hay alguien bloqueando y NO soy yo, bloqueamos la pantalla
                if (!idBloqueador.isNullOrEmpty() && idBloqueador != miId) {
                    _binding?.layoutBloqueoCobro?.showElegantemente()
                } else {
                    _binding?.layoutBloqueoCobro?.hideElegantemente()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        estadoCobroRefGlobal?.addValueEventListener(estadoCobroListener!!)
    }

    private fun sumarProducto(producto: ProductoCaja) {
        if (bloquearOperacionPorRescateCajaBloqueada()) return

        val error = validarProductoCajaBasico(producto)
        if (error != null) {
            val b = _binding ?: return
            feedbackVm.notificarError(b.root)
            mostrarToastSeguro(error)
            return
        }

        val claveInventario = producto.indiceProducto.trim()
        val unidadesPorPresentacion = producto.unidadesPorPresentacion.toIntSeguro(0)

        actualizarEstadoActualizandoCaja(true)
        val itemCajaRef = getCajaRef().child("Productos").child(producto.id)

        // HUMANO: Variable local para capturar el error exacto dentro de la transacción
        var mensajeErrorTransaccion: String? = null

        itemCajaRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val productoActual = currentData.getValue(ProductoCaja::class.java)
                    ?: return Transaction.abort()

                // 1. Validar contra el stock real del Radar (caché sincronizada)
                val stockReal = cacheStockInventario[claveInventario] ?: 0

                val unidadesEnCarrito = listaProductosCaja
                    .filter { it.indiceProducto.trim() == claveInventario && it.id != producto.id }
                    .sumOf { it.cantidad.toIntSeguro(0) * it.unidadesPorPresentacion.toIntSeguro(1) }

                val cantidadActual = productoActual.cantidad.toIntOrNull() ?: 0
                val nuevasUnidadesRequeridas = unidadesEnCarrito + ((cantidadActual + 1) * unidadesPorPresentacion)

                if (nuevasUnidadesRequeridas > stockReal) {
                    mensajeErrorTransaccion = "¡No hay suficiente stock! Quedan $stockReal unidades en total."
                    return Transaction.abort()
                }

                // 2. Proceder con el incremento si hay disponibilidad
                val precioUnitario = productoActual.precioUnitario.toDoubleOrNull() ?: 0.0
                val nuevaCantidad = cantidadActual + 1
                val nuevoTotal = CalculadoraVentasHelper.calcularTotalConDescuento(
                    nuevaCantidad, precioUnitario,
                    productoActual.descuento, productoActual.tipoDescuento
                )

                productoActual.cantidad = nuevaCantidad.toString()
                productoActual.total = String.format(Locale.US, "%.2f", nuevoTotal)

                currentData.value = productoActual
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                actualizarEstadoActualizandoCaja(false)

                val b = _binding ?: return

                if (error != null) {
                    feedbackVm.notificarError(b.root)
                    mostrarToastSeguro("Error al actualizar: ${error.message}")
                } else if (!committed) {
                    feedbackVm.notificarError(b.root)
                    mostrarToastSeguro(mensajeErrorTransaccion ?: "Stock insuficiente. No se pudo sumar.")
                } else {
                    // Éxito confirmado por el servidor
                    feedbackVm.notificarProductoAgregado(b.root)
                }
            }
        })
    }


    private fun restarProducto(producto: ProductoCaja) {
        if (bloquearOperacionPorRescateCajaBloqueada()) return

        if (producto.id.isBlank()) {
            mostrarToastSeguro("Producto inválido en caja")
            return
        }

        actualizarEstadoActualizandoCaja(true)
        val itemCajaRef = getCajaRef().child("Productos").child(producto.id)

        itemCajaRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val productoActual = currentData.getValue(ProductoCaja::class.java)
                    ?: return Transaction.abort()

                val cantidadActual = productoActual.cantidad.toIntOrNull() ?: 0
                val precioUnitario = productoActual.precioUnitario.toDoubleOrNull() ?: 0.0

                if (cantidadActual <= 0) {
                    return Transaction.abort()
                }

                if (cantidadActual == 1) {
                    currentData.value = null
                    return Transaction.success(currentData)
                }

                val nuevaCantidad = cantidadActual - 1
                val nuevoTotal = CalculadoraVentasHelper.calcularTotalConDescuento(
                    nuevaCantidad, precioUnitario,
                    productoActual.descuento, productoActual.tipoDescuento
                )

                productoActual.cantidad = nuevaCantidad.toString()
                productoActual.total = String.format(Locale.US, "%.2f", nuevoTotal)

                currentData.value = productoActual
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                actualizarEstadoActualizandoCaja(false)

                // Blindaje de binding: si el usuario cerró la pantalla antes de la respuesta de red, salimos sin crash
                val b = _binding ?: return

                if (error != null) {
                    feedbackVm.notificarError(b.root)
                    mostrarToastSeguro("Error al actualizar: ${error.message}")
                } else if (!committed) {
                    feedbackVm.notificarError(b.root)
                    mostrarToastSeguro("No se pudo actualizar el producto")
                } else {
                    // El feedback de éxito se ejecuta solo si Firebase confirmó la operación
                    feedbackVm.notificarProductoRestado(b.root)
                }
            }
        })
    }

    // -------------------------------------------------------------------------
    //  Helpers: conexión
    // -------------------------------------------------------------------------

    private fun mostrarDialogoDescuento(producto: ProductoCaja) {
        if (estaCajaEnModoSoloVista(notificar = true)) return
        if (bloquearOperacionPorRescateCajaBloqueada()) return

        // Bloqueo por stock: evitar descuentos en productos en conflicto
        if (producto.agotado) {
            mostrarToastSeguro("No puedes aplicar descuentos a un producto agotado o en conflicto.")
            feedbackVm.notificarError(_binding?.root)
            return
        }

        if (!isAdded || _binding == null) return
        val ctx = requireContext()

        val dialogView = LayoutInflater.from(ctx).inflate(R.layout.dialog_descuento_item, null, false)
        val tvNombreDescuento = dialogView.findViewById<TextView>(R.id.tvNombreDescuento)
        val tvPreviewDescuento = dialogView.findViewById<TextView>(R.id.tvPreviewDescuento)
        val etDescuento = dialogView.findViewById<TextInputEditText>(R.id.etValorDescuento)
        val btnPct = dialogView.findViewById<MaterialButton>(R.id.btnTipoPct)
        val btnMonto = dialogView.findViewById<MaterialButton>(R.id.btnTipoMonto)
        val btnAplicar = dialogView.findViewById<MaterialButton>(R.id.btnAplicarDescuento)
        val btnQuitar = dialogView.findViewById<MaterialButton>(R.id.btnQuitarDescuento)

        val precio = producto.precioUnitario.toDoubleOrNull() ?: 0.0
        val cantidad = producto.cantidad.toIntOrNull() ?: 1
        var tipoActual = if (producto.tipoDescuento == "monto") "monto" else "pct"

        tvNombreDescuento.text = producto.nombre?.trim().orEmpty().ifEmpty { "Producto" }

        fun actualizarTipo(tipo: String) {
            tipoActual = tipo
            // Usar atributos del tema en lugar de recursos internos de Material3
            // (m3_sys_color_primary no es estable entre versiones)
            val tv = android.util.TypedValue()
            ctx.theme.resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, tv, true)
            val activeColor = tv.data
            ctx.theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceVariant, tv, true)
            val inactiveColor = tv.data
            if (tipo == "pct") {
                btnPct.backgroundTintList = ColorStateList.valueOf(activeColor)
                btnMonto.backgroundTintList = ColorStateList.valueOf(inactiveColor)
                etDescuento.hint = "Ej: 10 (para 10%)"
            } else {
                btnMonto.backgroundTintList = ColorStateList.valueOf(activeColor)
                btnPct.backgroundTintList = ColorStateList.valueOf(inactiveColor)
                etDescuento.hint = "Monto a descontar por unidad"
            }
        }

        fun actualizarPreview() {
            val valorStr = etDescuento.text?.toString().orEmpty()
            val valor = valorStr.toDoubleOrNull() ?: 0.0
            val total = CalculadoraVentasHelper.calcularTotalConDescuento(cantidad, precio, valor, tipoActual)
            val bruto = cantidad * precio
            val ahorrado = bruto - total
            tvPreviewDescuento.text = if (valor > 0.0) {
                "Subtotal: ${MonedaHelper.formatear(total)}  (Ahorro: ${MonedaHelper.formatear(ahorrado)})"
            } else {
                "Subtotal: ${MonedaHelper.formatear(bruto)}"
            }
        }

        // Cargar valores actuales del producto
        actualizarTipo(tipoActual)
        if (producto.descuento > 0.0) {
            etDescuento.setText(
                if (tipoActual == "pct") producto.descuento.toInt().toString()
                else String.format(Locale.US, "%.2f", producto.descuento)
            )
        }
        actualizarPreview()

        etDescuento.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { actualizarPreview() }
        })

        btnPct.setOnClickListener { actualizarTipo("pct"); actualizarPreview() }
        btnMonto.setOnClickListener { actualizarTipo("monto"); actualizarPreview() }

        dialogoDescuentoActual?.dismiss()
        val dialog = MaterialAlertDialogBuilder(ctx)
            .setView(dialogView)
            .create()
        dialog.setOnDismissListener {
            if (dialogoDescuentoActual === dialog) {
                dialogoDescuentoActual = null
            }
        }

        btnAplicar.setOnClickListener {
            val valor = etDescuento.text?.toString()?.toDoubleOrNull() ?: 0.0

            // Usar el validador estricto del Helper
            val errorValidacion = CalculadoraVentasHelper.validarDescuentoPermitido(precio, valor, tipoActual)
            if (errorValidacion != null) {
                mostrarToastSeguro(errorValidacion)
                etDescuento.error = errorValidacion
                return@setOnClickListener
            }

            aplicarDescuentoProducto(producto, valor, tipoActual)
            dialog.dismiss()
        }

        btnQuitar.setOnClickListener {
            aplicarDescuentoProducto(producto, 0.0, "")
            dialog.dismiss()
        }

        dialogoDescuentoActual = dialog
        dialog.show()
    }

    private fun aplicarDescuentoProducto(producto: ProductoCaja, descuento: Double, tipo: String) {
        if (bloquearOperacionPorRescateCajaBloqueada()) return
        if (producto.id.isBlank()) return
        val cantidad = producto.cantidad.toIntOrNull() ?: 1
        val precio = producto.precioUnitario.toDoubleOrNull() ?: 0.0
        val nuevoTotal = CalculadoraVentasHelper.calcularTotalConDescuento(cantidad, precio, descuento, tipo)

        val updates = mapOf(
            "descuento" to descuento,
            "tipoDescuento" to tipo,
            "total" to String.format(Locale.US, "%.2f", nuevoTotal)
        )
        getCajaRef().child("Productos").child(producto.id).updateChildren(updates)
            .addOnFailureListener { mostrarToastSeguro("No se pudo guardar el descuento") }
    }

    private fun escucharConexionFirebase() {
        if (!isAdded || _binding == null) return
        connectionListener?.let { connectionRef?.removeEventListener(it) }

        val ref = FirebaseDatabase.getInstance().getReference(".info/connected")
        connectionRef = ref
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || _binding == null) return
                val conectado = snapshot.getValue(Boolean::class.java) ?: true
                val btnCobrar = _binding?.buttoncobrar ?: return
                
                if (conectado) {
                    // DETECCION DE RECOVERY: Si antes estabamos offline y ahora conectamos
                    if (!internetEstabaConectado) {
                        internetRecienRecuperado = true
                        // Damos 3 segundos de "ventana de gracia" para que Firebase sincronice su reloj interno
                        handlerSeguridadOperaciones.removeCallbacksAndMessages("grace_period")
                        handlerSeguridadOperaciones.postDelayed({ 
                            internetRecienRecuperado = false 
                        }, "grace_period", 3000L)
                    }
                    internetEstabaConectado = true

                    if (turnoAbierto) {
                        btnCobrar.isEnabled = listaProductosCaja.isNotEmpty()
                        btnCobrar.alpha = if (btnCobrar.isEnabled) 1f else 0.55f
                    }
                } else {
                    internetEstabaConectado = false
                    btnCobrar.isEnabled = false
                    btnCobrar.alpha = 0.55f
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        connectionListener = listener
        ref.addValueEventListener(listener)
    }

    private fun eliminarProducto(producto: ProductoCaja, onComplete: ((Boolean) -> Unit)? = null) {
        if (bloquearOperacionPorRescateCajaBloqueada()) {
            onComplete?.invoke(false)
            return
        }
        if (producto.id.isBlank()) {
            mostrarToastSeguro("Producto inválido en caja")
            onComplete?.invoke(false)
            return
        }

        actualizarEstadoActualizandoCaja(true)

        val itemCajaRef = getCajaRef()
            .child("Productos")
            .child(producto.id)

        itemCajaRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                currentData.getValue(ProductoCaja::class.java)
                    ?: return Transaction.abort()

                currentData.value = null
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                actualizarEstadoActualizandoCaja(false)

                if (error != null) {
                    mostrarToastSeguro("No se pudo eliminar el producto: ${error.message}")
                    onComplete?.invoke(false)
                } else if (!committed) {
                    mostrarToastSeguro("El producto ya no estaba disponible en la caja")
                    onComplete?.invoke(false)
                } else {
                    onComplete?.invoke(true)
                }
            }
        })
    }

    private fun mostrarMensajeStockInsuficiente(stockDisponible: Int, unidadesYaEnCaja: Int, producto: ProductoCaja) {
        val unidadesPorPresentacion = producto.unidadesPorPresentacion.toIntSeguro(1)
        val restantesUnidades = (stockDisponible - unidadesYaEnCaja).coerceAtLeast(0)
        val maximoPresentacionesRestantes = if (unidadesPorPresentacion > 0) {
            restantesUnidades / unidadesPorPresentacion
        } else {
            0
        }

        if (maximoPresentacionesRestantes <= 0) {
            val mensaje = if (stockDisponible <= 0) {
                "Este producto ya no tiene stock en inventario"
            } else {
                "Ya tienes todo el stock disponible de este producto en tu caja"
            }
            mostrarToastSeguro(mensaje)
        } else {
            mostrarToastSeguro("Solo puedes agregar $maximoPresentacionesRestantes ${producto.presentacion}")
        }
    }

    private var ultimoTotalVentaAnimado = 0.0
    private var ultimaCantidadItemsAnimada = 0

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun actualizarTotales() {
        val b = _binding ?: return

        var total = 0.0
        var bruto = 0.0

        // Un solo viaje para calcular bruto y total de forma eficiente
        listaProductosCaja.forEach { prod ->
            val cant = prod.cantidad.toIntOrNull() ?: 0
            val precio = prod.precioUnitario.toDoubleOrNull() ?: 0.0

            bruto += cant * precio
            total += (prod.total.toDoubleSeguro(0.0) ?: 0.0)
        }

        bruto = CalculadoraVentasHelper.redondear(bruto)
        total = CalculadoraVentasHelper.redondear(total)

        val descuentoTotal = CalculadoraVentasHelper.redondear(bruto - total).coerceAtLeast(0.0)

        // Toda la lógica de descuentos agrupada limpiamente sin pisarse
        if (descuentoTotal > 0.01) {
            b.labelSubtotal?.visibility = View.VISIBLE
            b.textsubtotal?.visibility = View.VISIBLE
            b.labelSubtotal?.text = "Descuentos"
            b.textsubtotal?.text = "-${MonedaHelper.formatear(descuentoTotal)}"
            b.textsubtotal?.setTextColor(Color.parseColor("#B42318"))
        } else {
            b.labelSubtotal?.visibility = View.GONE
            b.textsubtotal?.visibility = View.GONE
        }

        b.labelBruto?.visibility = View.VISIBLE
        b.textbruto?.visibility = View.VISIBLE
        b.textbruto?.text = MonedaHelper.formatear(bruto)

        val totalItems = listaProductosCaja.sumOf { it.cantidad.toIntOrNull() ?: 0 }.coerceAtLeast(0)

        b.labelTotal?.text = when (totalItems) {
            1 -> "Total a cobrar (1 prod.)"
            else -> "Total a cobrar ($totalItems prods.)"
        }

        // Rolling Numbers Animación
        animarMontoTotal(total)

        // 1. MEJORA DE UX: Cambiamos el texto del botón contextualmente si hay problemas de stock
        val hayAgotados = hayProductosAgotadosEnCaja()
        b.buttoncobrar.text = if (hayAgotados) "Revisar stock agotado" else "Continuar al pago"

        // Bounce micro-interacción
        if (total != ultimoTotalVentaAnimado || totalItems != ultimaCantidadItemsAnimada) {
            ultimoTotalVentaAnimado = total
            ultimaCantidadItemsAnimada = totalItems
            if (totalItems > 0 && !modoBusquedaActivo) {
                animarBounceCarrito(b.cardResumenCaja)
            }
        }

        // 2. SOLUCIÓN AL BLOQUEO: Añadimos '!hayAgotados' a la condición de activación
        val puedeContinuar = listaProductosCaja.isNotEmpty() && total >= 0.0 && !hayAgotados
        b.buttoncobrar.isEnabled = puedeContinuar
        b.buttoncobrar.alpha = if (puedeContinuar) 1f else 0.55f

        if (estaEnModoRescateCajaBloqueada()) {
            b.buttoncobrar.visibility = View.GONE
            b.buttoncobrar.isEnabled = false
            b.buttoncobrar.alpha = 0.55f
        }

        actualizarAccionesVentaEsperaUi()
        mostrarEstadoVacio()
    }

    /**
     * MEJORA PREMIUM #1: Contador de Dinero Animado.
     * Interpola el valor del total para que los numeros "rueden" fluidamente.
     */
    private fun animarMontoTotal(nuevoTotal: Double) {
        val b = _binding ?: return
        val montoAnterior = ultimoTotalVentaAnimado
        
        // Si el cambio es insignificante, no animamos para evitar parpadeos
        if (kotlin.math.abs(nuevoTotal - montoAnterior) < 0.001) {
            val texto = MonedaHelper.formatear(nuevoTotal)
            b.texttotal.text = texto
            b.tvResumenCobroTotal.text = texto
            return
        }

        val animator = android.animation.ValueAnimator.ofFloat(montoAnterior.toFloat(), nuevoTotal.toFloat())
        animator.duration = 450L // Duracion premium
        animator.interpolator = android.view.animation.DecelerateInterpolator(1.5f)
        
        animator.addUpdateListener { animation ->
            val bindingActual = _binding ?: return@addUpdateListener
            val valorActual = (animation.animatedValue as Float).toDouble()
            val textoFormateado = MonedaHelper.formatear(valorActual)
            
            bindingActual.texttotal.text = textoFormateado
            bindingActual.tvResumenCobroTotal.text = textoFormateado
            
            // Si el total esta visible en el paso de cobro, tambien lo actualizamos
            if (bindingActual.layoutCobroPanel.visibility == View.VISIBLE) {
                bindingActual.tvTotalPasoCobroMetodo.text = textoFormateado
            }
        }
        
        animator.start()
    }

    private fun mostrarEstadoVacio() {
        val b = _binding ?: return
        val hayProductos = listaProductosCaja.isNotEmpty()
        val hayVentasEnEspera = cantidadVentasEnEspera > 0
        val mostrarPanelResumen = hayProductos || hayVentasEnEspera

        // Tablet/Movil: si estamos en checkout y la caja queda vacía, regresamos al catálogo automáticamente
        if (!hayProductos && b.layoutComprobante.visibility == View.VISIBLE) {
            salirDeCheckoutPorCajaVacia()
            return
        }

        if (esTablet()) {
            val recyclerProductos = b.root.findViewById<RecyclerView>(R.id.recyclerProductos)
            val gridLayoutManager = recyclerProductos?.layoutManager as? GridLayoutManager

            if (!mostrarPanelResumen) {
                b.layoutCaja?.visibility = View.GONE
                gridLayoutManager?.spanCount = 4

                b.recyclerView2.visibility = View.GONE
                b.cardResumenCaja.visibility = View.GONE
                b.buttoncobrar.visibility = View.GONE
            } else {
                b.layoutCaja?.visibility = View.VISIBLE
                gridLayoutManager?.spanCount = 4

                b.recyclerView2.visibility = if (hayProductos) View.VISIBLE else View.GONE
                b.cardResumenCaja.visibility = View.VISIBLE
                b.buttoncobrar.visibility = if (hayProductos) View.VISIBLE else View.GONE
            }
            adapterProductos?.notifyDataSetChanged()
        } else {
            // === CAMBIO DE CONTROL: Detectamos si la pantalla de cobros o comprobante está activa ===
            val enFlujoCobro = b.layoutCobroPanel.visibility == View.VISIBLE || b.layoutComprobante.visibility == View.VISIBLE

            // El carrito y totales SOLO se muestran si no estamos buscando Y tampoco estamos cobrando
            val mostrarCarrito = hayProductos && !modoBusquedaActivo && !enFlujoCobro

            b.headerProductos.visibility = if (mostrarCarrito) View.VISIBLE else View.GONE
            b.recyclerView2.visibility = if (mostrarCarrito) View.VISIBLE else View.GONE

            // Sincronizar visibilidad del panel de resumen con animación
            animarPanelResumen(mostrarCarrito)

            // Si se está buscando o cobrando, limpiamos forzosamente los dibujos e ilustraciones del fondo
            if (modoBusquedaActivo || enFlujoCobro) {
                b.root.findViewById<View>(R.id.layoutEstadoVacioCaja)?.visibility = View.GONE
                b.tvAyudaCajaVacia.visibility = View.GONE
            } else {
                b.root.findViewById<View>(R.id.layoutEstadoVacioCaja)?.visibility =
                    if (hayProductos) View.GONE else View.VISIBLE
            }
        }

        if (estaEnModoRescateCajaBloqueada()) {
            b.buttoncobrar.visibility = View.GONE
        }
        actualizarAccionesVentaEsperaUi()
        actualizarAyudaCajaVaciaUi()
        actualizarFormularioClienteInline()
    }

    private fun animarPanelResumen(mostrar: Boolean) {
        val b = _binding ?: return
        val card = b.cardResumenCaja
        
        // Evitar reiniciar la animación si ya estamos en el estado objetivo
        val estaVisible = card.visibility == View.VISIBLE && card.alpha > 0.1f
        if (mostrar == estaVisible) return

        card.animate().cancel() // Limpiar animaciones pendientes

        if (mostrar) {
            // Estado inicial antes de la animación de entrada
            if (card.visibility != View.VISIBLE) {
                card.visibility = View.VISIBLE
                card.alpha = 0f
                card.translationY = dpToPx(32f) 
            }
            
            card.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(350L)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        } else {
            // Animación de salida
            card.animate()
                .alpha(0f)
                .translationY(dpToPx(32f))
                .setDuration(300L)
                .setInterpolator(android.view.animation.AccelerateInterpolator())
                .withEndAction {
                    // Solo ocultamos si al terminar la animación seguimos queriendo ocultarlo
                    if (card.alpha < 0.1f) card.visibility = View.GONE
                }
                .start()
        }
    }

    private fun salirDeCheckoutPorCajaVacia() {
        val b = _binding ?: return
        if (revirtiendoCheckoutPorCajaVacia) return
        if (listaProductosCaja.isNotEmpty()) return
        if (b.layoutComprobante.visibility != View.VISIBLE) return

        revirtiendoCheckoutPorCajaVacia = true
        liberarCajaSiEstabaBloqueada()

        mostrarLoadingOverlayCaja(
            titulo = "Actualizando vista",
            mensaje = "La caja quedó vacía. Volviendo al catálogo..."
        )

        b.root.postDelayed({
            if (_binding == null) {
                revirtiendoCheckoutPorCajaVacia = false
                return@postDelayed
            }

            if (esTablet()) {
                aplicarLayoutInicialTablet()
            } else {
                ocultarLayoutComprobanteMobile()
            }

            ocultarLoadingOverlayCaja()
            revirtiendoCheckoutPorCajaVacia = false
        }, 260L)
    }

    private fun irListaProductos() {
        // La búsqueda de productos ahora es INLINE dentro del Fragment de Caja.
        // No se abre ninguna Activity externa para buscar/agregar productos.
        configurarBusquedaInlineCaja()
    }

    private fun configurarBusquedaInlineCaja() {
        if (busquedaInlineConfigurada) return
        val b = _binding ?: return
        val context = context ?: return
        val buscar = b.buscar ?: return

        val recyclerChips = b.root.findViewById<RecyclerView>(R.id.recyclerCategoriasInline) ?: return
        val recyclerResultados = b.root.findViewById<RecyclerView>(R.id.recyclerResultadosInline) ?: return

        recyclerChips.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerResultados.layoutManager = LinearLayoutManager(context)

        adapterChipsInline = AdapterCategorias(
            listaCategorias = listaCategoriasInline.toList(),
            onCategoriaClick = { categoria ->
                if (!isAdded || _binding == null) return@AdapterCategorias
                categoriaSeleccionadaInline = categoria
                aplicarFiltrosBusquedaInline(_binding?.buscar?.text?.toString().orEmpty())
            }
        )
        recyclerChips.adapter = adapterChipsInline

        adapterResultadosInline = AdapterResultadosBusquedaCaja(
            onAgregarPresentacion = { producto, presentacion, anchor ->
                if (!isAdded || _binding == null || ventaEnProceso) return@AdapterResultadosBusquedaCaja
                if (!puedeOperarTurnoActual()) return@AdapterResultadosBusquedaCaja
                agregarProductoDirectoTablet(producto, presentacion) { exito ->
                    if (exito) {
                        feedbackVm.notificarProductoAgregado(anchor)
                        animarVueloAlCarrito(anchor)
                        
                        // --- DETALLE AVANZADO: Retener teclado para multiventas ---
                        _binding?.buscar?.requestFocus()
                        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                        imm?.showSoftInput(_binding?.buscar, InputMethodManager.SHOW_IMPLICIT)
                    }
                }
            }
        )
        recyclerResultados.adapter = adapterResultadosInline
        b.root.findViewById<View>(R.id.btnCategoriasBusquedaInline)?.setOnClickListener {
            categoriasInlineExpandidas = !categoriasInlineExpandidas
            actualizarVisibilidadCategoriasInline()
        }


        buscar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (!modoBusquedaActivo) entrarModoBusqueda()
            } else {
                val texto = buscar.text?.toString().orEmpty().trim()
                if (texto.isBlank()) {
                    buscar.post {
                        salirModoBusqueda()
                    }
                }
            }
        }

        buscar.setOnClickListener {
            if (!modoBusquedaActivo) entrarModoBusqueda()
        }

        buscar.doAfterTextChanged { editable ->
            if (!isAdded || _binding == null) return@doAfterTextChanged
            val texto = editable?.toString().orEmpty()

            if (texto.isNotBlank()) {
                if (!modoBusquedaActivo) entrarModoBusqueda()
                programarFiltroBusquedaInline(texto)
            } else {
                runnableBusquedaProductosInline?.let {
                    handlerSeguridadOperaciones.removeCallbacks(it)
                }
                runnableBusquedaProductosInline = null

                ocultarDropdownResultados()
                actualizarResultadosBusquedaInline(emptyList())
                actualizarVisibilidadResultadosInline()

                if (modoBusquedaActivo) {
                    buscar.post {
                        salirModoBusqueda()
                    }
                }
            }
        }

        buscar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                ocultarTecladoBuscador()
                true
            } else false
        }

        b.root.findViewById<View>(R.id.scrimBusqueda)?.setOnClickListener {
            salirModoBusqueda()
        }

        // --- DETECTAR CIERRE DEL TECLADO POR GESTO/BOTÓN ATRÁS ---
        ViewCompat.setOnApplyWindowInsetsListener(b.root) { _, insets ->
            val tecladoVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            if (!tecladoVisible && modoBusquedaActivo && b.buscar?.hasFocus() == true) {
                if (b.buscar?.text?.isEmpty() == true) {
                    salirModoBusqueda(ocultarTecladoTambien = false)
                }
            }
            insets
        }

        cargarCategoriasBusquedaInline()
        iniciarListenerProductosInventarioInline()
        busquedaInlineConfigurada = true
    }

    private fun ocultarTecladoBuscador() {
        val view = activity?.currentFocus ?: _binding?.buscar ?: return
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun cargarCategoriasBusquedaInline() {
        database.getReference("Inventario")
            .child("CategoriasInventario")
            .get()
            .addOnSuccessListener { snapshot ->
                if (!isAdded || _binding == null) return@addOnSuccessListener
                val nuevasCategorias = mutableListOf("Todos", "Frecuentes")
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        val categoria = child.getValue(CategoriaProductos::class.java)
                        val nombre = categoria?.nombre?.trim().orEmpty()
                        if (nombre.isNotEmpty() && !nuevasCategorias.contains(nombre)) {
                            nuevasCategorias.add(nombre)
                        }
                    }
                }
                
                listaCategoriasInline.clear()
                listaCategoriasInline.addAll(nuevasCategorias)
                
                // Actualizamos el adaptador existente o lo creamos si es la primera vez
                if (adapterChipsInline == null) {
                    val recyclerChips = _binding?.root?.findViewById<RecyclerView>(R.id.recyclerCategoriasInline)
                    adapterChipsInline = AdapterCategorias(
                        listaCategorias = listaCategoriasInline,
                        onCategoriaClick = { categoria ->
                            if (!isAdded || _binding == null) return@AdapterCategorias
                            categoriaSeleccionadaInline = categoria
                            aplicarFiltrosBusquedaInline(_binding?.buscar?.text?.toString().orEmpty())
                        }
                    )
                    recyclerChips?.adapter = adapterChipsInline
                } else {
                    adapterChipsInline?.actualizarLista(listaCategoriasInline)
                }
            }
    }

    private fun iniciarListenerProductosInventarioInline() {
        if (inventarioProductosValueListener != null) {
            // Listener compartido (tablet también lo usa). Solo aseguramos que la lista actual
            // dispare un refresh inmediato del adapter inline para que aparezcan productos al primer foco.
            aplicarFiltrosBusquedaInline(_binding?.buscar?.text?.toString().orEmpty())
            return
        }

        inventarioProductosRefListener = database.getReference("Inventario").child("Productos")
        inventarioProductosValueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || _binding == null) return
                listaOriginalProductosTablet.clear()
                listaProductosTablet.clear()
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        val producto = child.toMoldeProductoSeguro() ?: continue
                        if (producto.indice.isBlank()) {
                            producto.indice = child.key.orEmpty()
                        }
                        listaOriginalProductosTablet.add(producto)
                    }
                }
                aplicarFiltrosBusquedaInline(_binding?.buscar?.text?.toString().orEmpty())
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded || _binding == null) return
                mostrarToastSeguro("Error al cargar productos: ${error.message}")
            }
        }
        inventarioProductosRefListener?.addValueEventListener(inventarioProductosValueListener!!)
    }

    private fun aplicarFiltrosBusquedaInline(textoBusqueda: String) {
        if (!isAdded || _binding == null) return
        val tokens = normalizarTextoLibre(textoBusqueda).split(" ").filter { it.isNotBlank() }

        if (tokens.isEmpty()) {
            actualizarResultadosBusquedaInline(emptyList())
            ocultarDropdownResultados()
            actualizarVisibilidadResultadosInline()
            return
        }

        val resultadosFiltrados = mutableListOf<MoldeProductos>()
        for (producto in listaOriginalProductosTablet) {
            val nombreNorm = normalizarTextoLibre(producto.nombre)
            val catNorm = normalizarTextoLibre(producto.categoria)
            val codNorm = normalizarTextoLibre(producto.codigo)
            val presentacionesNorm = producto.presentaciones.map { normalizarTextoLibre(it.nombre) }

            val coincideBusqueda = tokens.all { token ->
                nombreNorm.contains(token) ||
                catNorm.contains(token) ||
                codNorm.contains(token) ||
                presentacionesNorm.any { it.contains(token) }
            }

            val coincideCategoria = when (categoriaSeleccionadaInline) {
                "Todos", "Frecuentes" -> true
                else -> producto.categoria.equals(categoriaSeleccionadaInline, ignoreCase = true)
            }

            if (coincideBusqueda && coincideCategoria) {
                resultadosFiltrados.add(producto)
            }
        }

        // Actualizamos el adaptador con los resultados filtrados
        actualizarResultadosBusquedaInline(resultadosFiltrados)
        actualizarResultadosDropdown(resultadosFiltrados)
        actualizarVisibilidadResultadosInline()

        // MANEJO DE ESTADO VACIO
        _binding?.layoutSinResultadosBusqueda?.visibility =
            if (resultadosFiltrados.isEmpty() && textoBusqueda.isNotBlank()) View.VISIBLE else View.GONE
    }

    private fun actualizarVisibilidadResultadosInline() {
        val b = _binding ?: return
        // Forzamos que todo lo que no sea el popup esté oculto en el fondo para evitar duplicados
        b.root.findViewById<View>(R.id.recyclerResultadosInline)?.visibility = View.GONE
        b.root.findViewById<View>(R.id.layoutSinResultadosBusqueda)?.visibility = View.GONE
    }

    private fun actualizarCabeceraBusquedaInline() {
        val b = _binding ?: return
        val contenedor = b.root.findViewById<View>(R.id.layoutBusquedaActivaInline) ?: return

        // Siempre oculto para que no se duplique con el Popup flotante
        contenedor.visibility = View.GONE
    }

    private fun actualizarVisibilidadCategoriasInline() {
        val b = _binding ?: return
        val botonCategorias = b.root.findViewById<MaterialButton>(R.id.btnCategoriasBusquedaInline) ?: return
        val recyclerCategorias = b.root.findViewById<RecyclerView>(R.id.recyclerCategoriasInline) ?: return

        if (!modoBusquedaActivo) {
            botonCategorias.visibility = View.GONE
            recyclerCategorias.visibility = View.GONE
            botonCategorias.alpha = 1f
            return
        }

        botonCategorias.visibility = View.GONE // Forzamos GONE para eliminar el icono de categorias del buscador
        recyclerCategorias.visibility = if (categoriasInlineExpandidas) View.VISIBLE else View.GONE
        botonCategorias.alpha = if (categoriasInlineExpandidas || categoriaSeleccionadaInline != "Todos") 1f else 0.85f
    }

    private fun actualizarEstiloModoBusqueda(activo: Boolean) {
        val b = _binding ?: return
        if (activo) {
            // b.root.setBackgroundColor(Color.parseColor("#F1FAF4"))
            b.buscador?.boxBackgroundColor = Color.WHITE
            b.buscador?.boxStrokeColor = Color.parseColor("#22A861")
        } else {
            b.root.setBackgroundColor(Color.parseColor("#F8FAFB"))
            b.buscador?.boxBackgroundColor = Color.WHITE
            b.buscador?.boxStrokeColor = Color.parseColor("#E8EAF0")
        }
    }

    private fun entrarModoBusqueda() {
        if (bloquearOperacionPorRescateCajaBloqueada()) return // <--- AGREGAR ESTO
        if (modoBusquedaActivo) return
        modoBusquedaActivo = true
        actualizarEstiloModoBusqueda(true)
        
        val b = _binding ?: return
        
        // 1. Ocultar navegación y resumen primero para asentar el layout
        (activity as? ActivityFragmentos)?.setBottomNavigationVisible(false)
        actualizarTotales()
        
        b.scrimBusqueda?.visibility = View.VISIBLE
        b.scrimBusqueda?.bringToFront()
        b.layoutBuscadorInline?.bringToFront()
        
        // 2. Esperar un instante a que el layout se estabilice antes de mostrar teclado
        b.buscar?.postDelayed({
            if (!isAdded || _binding == null) return@postDelayed
            b.buscar?.requestFocus()
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(b.buscar, InputMethodManager.SHOW_IMPLICIT)
        }, 120L)
    }

    private fun salirModoBusqueda(ocultarTecladoTambien: Boolean = true) {
        val b = _binding ?: return

        if (!modoBusquedaActivo && b.scrimBusqueda?.visibility != View.VISIBLE) {
            return
        }

        modoBusquedaActivo = false

        runnableBusquedaProductosInline?.let {
            handlerSeguridadOperaciones.removeCallbacks(it)
        }
        runnableBusquedaProductosInline = null

        if (b.buscar?.text?.isNotEmpty() == true) {
            b.buscar?.setText("")
        }

        b.buscar?.clearFocus()
        b.scrimBusqueda?.visibility = View.GONE

        ocultarDropdownResultados()
        actualizarResultadosBusquedaInline(emptyList())
        actualizarVisibilidadResultadosInline()
        actualizarVisibilidadCategoriasInline()
        actualizarEstiloModoBusqueda(false)

        if (ocultarTecladoTambien) {
            ocultarTeclado(b.buscar)
        }

        (activity as? ActivityFragmentos)?.setBottomNavigationVisible(true)

        val hayProductos = listaProductosCaja.isNotEmpty()

        b.headerProductos.visibility = if (hayProductos) View.VISIBLE else View.GONE
        b.recyclerView2.visibility = if (hayProductos) View.VISIBLE else View.GONE
        b.buttoncobrar.visibility = if (hayProductos) View.VISIBLE else View.GONE

        b.cardResumenCaja.animate().cancel()
        b.cardResumenCaja.alpha = 1f
        b.cardResumenCaja.translationY = 0f
        b.cardResumenCaja.visibility = if (hayProductos) View.VISIBLE else View.GONE

        b.root.findViewById<View>(R.id.layoutEstadoVacioCaja)?.visibility =
            if (hayProductos) View.GONE else View.VISIBLE

        actualizarTotales()
    }

    private fun ocultarDropdownResultados() {
        popupResultadosBusqueda?.dismiss()
        popupResultadosBusqueda = null
        rvResultadosDropdown = null
        tvSinResultadosDropdown = null
    }

    private fun actualizarResultadosDropdown(listaFiltrada: List<MoldeProductos>) {
        if (!isAdded || _binding == null) return

        val b = _binding!!
        val anchor = b.buscador ?: return

        // --- CÁLCULO DE ALTURA DINÁMICA HASTA EL TECLADO ---
        val rectPantalla = android.graphics.Rect()
        anchor.getWindowVisibleDisplayFrame(rectPantalla)
        
        val localizacionAnchor = IntArray(2)
        anchor.getLocationInWindow(localizacionAnchor)
        val baseBuscadorY = localizacionAnchor[1] + anchor.height
        
        // Espacio disponible entre el buscador y el teclado (o fondo de pantalla)
        val espacioDisponible = (rectPantalla.bottom - baseBuscadorY) - (8 * resources.displayMetrics.density).toInt()
        val alturaMaxDeseada = (450 * resources.displayMetrics.density).toInt()
        val alturaFinal = if (espacioDisponible > 100) espacioDisponible.coerceAtMost(alturaMaxDeseada) else 100
        // --------------------------------------------------

        if (popupResultadosBusqueda == null) {
            val view = layoutInflater.inflate(R.layout.dropdown_resultados_busqueda, null)
            rvResultadosDropdown = view.findViewById(R.id.rvResultadosDropdown)
            tvSinResultadosDropdown = view.findViewById(R.id.tvSinResultadosDropdown)

            rvResultadosDropdown?.layoutManager = LinearLayoutManager(requireContext())
            rvResultadosDropdown?.adapter = adapterResultadosInline

            popupResultadosBusqueda = PopupWindow(
                view,
                anchor.width,
                alturaFinal,
                false
            ).apply {
                isOutsideTouchable = true
                isFocusable = false        
                elevation = 60f // Superar elevación del buscador (50dp) y scrim para asegurar detección de toques
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        }

        popupResultadosBusqueda?.width = anchor.width
        popupResultadosBusqueda?.height = alturaFinal // Actualizar altura en cada filtrado por si el teclado se movió

        if (listaFiltrada.isEmpty()) {
            rvResultadosDropdown?.visibility = View.GONE
            tvSinResultadosDropdown?.visibility = View.VISIBLE
        } else {
            rvResultadosDropdown?.visibility = View.VISIBLE
            tvSinResultadosDropdown?.visibility = View.GONE
        }

        if (popupResultadosBusqueda?.isShowing == false && isAdded) {
            popupResultadosBusqueda?.showAsDropDown(anchor, 0, 4)
        }
    }

    private fun configurarBotonCobrar() {
        val b = _binding ?: return

        b.buttoncobrar.setOnClickListener {
            val bClick = _binding ?: return@setOnClickListener

            if (!puedeOperarTurnoActual()) return@setOnClickListener
            if (ventaEnProceso || actualizandoCaja) return@setOnClickListener

            if (!turnoAbierto || idTurnoActivo.isBlank()) {
                if (!isHidden) {
                    feedbackVm.notificarError(bClick.buttoncobrar)
                    mostrarToastSeguro("Primero debes abrir un turno de caja")
                    bClick.layoutAperturaCaja.visibility = View.VISIBLE
                }
                return@setOnClickListener
            }

            if (listaProductosCaja.isEmpty()) {
                feedbackVm.notificarError(bClick.buttoncobrar)
                mostrarToastSeguro("No hay productos en la caja")
                return@setOnClickListener
            }

            val total = obtenerTotalVenta()
            if (total <= 0.0) {
                feedbackVm.notificarError(bClick.buttoncobrar)
                mostrarToastSeguro("Total inválido")
                return@setOnClickListener
            }

            // 1. SOLUCIÓN: Usamos tu método centralizado para congelar el botón y mostrar "Procesando venta..."
            actualizarEstadoVentaEnProceso(true, liberarBloqueo = true)

            val estadoCobroRef = database.getReference("CajasIndividuales")
                .child(obtenerIdCajaOperativa())
                .child("estadoCobro")

            estadoCobroRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val bloqueadoPor = currentData.value as? String
                    if (!bloqueadoPor.isNullOrEmpty() && bloqueadoPor != idCajera) {
                        return Transaction.abort()
                    }
                    currentData.value = idCajera
                    return Transaction.success(currentData)
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                    val bComplete = _binding ?: return

                    // 2. SOLUCIÓN: Si falla, usamos el método centralizado en FALSE para revivir el botón al instante
                    if (error != null || !committed) {
                        actualizarEstadoVentaEnProceso(false)

                        feedbackVm.notificarError(bComplete.root)
                        val msg = error?.message ?: "La caja ya está siendo cobrada por otra persona en este momento."
                        mostrarToastSeguro(msg)
                        return
                    }

                    // Flujo de éxito normal
                    estadoCobroRef.onDisconnect().removeValue()
                    soyYoElQueCobra = true

                    validarStockFinalAntesDeContinuar { productosValidados ->
                        if (productosValidados.isNotEmpty()) {
                            confirmarComprobanteDesdeUi()
                        } else {
                            // 3. SOLUCIÓN: Si la validación de stock falla o se cancela, también revivimos el botón
                            actualizarEstadoVentaEnProceso(false)
                            liberarCajaSiEstabaBloqueada()
                        }
                    }
                }
            })
        }

        b.btnNecesitoFactura.let { configurarSwitchComprobante(it) }

        b.btnVolverPasoCobroMetodo.setOnClickListener {
            liberarCajaSiEstabaBloqueada()
            reiniciarFlujoCobroInline()
        }

        b.btnVolverPasoCobroDetalle.setOnClickListener {
            if (metodosPagoCobroActivos.size == 1) {
                liberarCajaSiEstabaBloqueada()
                reiniciarFlujoCobroInline()
            } else {
                irAPaso(2)
            }
        }
    }

    private fun iniciarFlujoCobroInline(totalPagar: Double) {
        val b = _binding ?: return
        totalCobroActual = totalPagar
        actualizarResumenPasoCobro(totalPagar)
        b.recyclerPasoCobroMetodo.layoutManager = LinearLayoutManager(requireContext())

        obtenerMetodosPagoActivos(
            onSuccess = { metodos ->
                if (!isAdded || _binding == null) return@obtenerMetodosPagoActivos

                val ordenados = metodos
                    .sortedWith(compareBy<MetodoPagoConfig>({ if (it.esMixtoLike()) 1 else 0 }, { it.orden }))

                metodosPagoCobroActivos = ordenados
                precargarQrsMetodos(ordenados)

                if (ordenados.size == 1) {
                    mostrarPasoDetalleCobroInline(ordenados.first(), totalPagar, forzarRecrear = true)
                } else {
                    val adapter = AdapterMetodosPagoFlujo(ordenados) { metodo ->
                        mostrarPasoDetalleCobroInline(metodo, totalPagar, forzarRecrear = true)
                    }
                    b.recyclerPasoCobroMetodo.adapter = adapter
                    metodoCobroSeleccionado = null
                    vistaDetalleCobroInline = null
                    b.contenedorPasoCobroDetalle.removeAllViews()
                    irAPaso(2)
                }
            },
            onError = { mensaje ->
                if (!isAdded || _binding == null) return@obtenerMetodosPagoActivos
                liberarCajaSiEstabaBloqueada()
                mostrarToastSeguro(mensaje)
                reiniciarFlujoCobroInline()
            }
        )
    }

    private fun actualizarResumenPasoCobro(totalPagar: Double = totalCobroActual) {
        val b = _binding ?: return
        b.tvTotalPasoCobroMetodo.text = MonedaHelper.formatear(totalPagar)
    }

    private fun reiniciarFlujoCobroInline() {
        val b = _binding ?: return
        handlerMontoRecibido.removeCallbacksAndMessages(null)
        pasoCobroActual = 1
        metodoCobroSeleccionado = null
        totalCobroActual = 0.0
        metodosPagoCobroActivos = emptyList()
        vistaDetalleCobroInline = null
        b.contenedorPasoCobroDetalle.removeAllViews()
        b.recyclerPasoCobroMetodo.adapter = null
        irAPaso(1)
    }

    private fun irAPaso(numero: Int) {
        val b = _binding ?: return
        pasoCobroActual = numero

        val mostrarFlujo = numero != 1
        b.layoutCobroPanel.visibility = if (mostrarFlujo) View.VISIBLE else View.GONE
        b.layoutPasoCobroMetodo.visibility = if (numero == 2) View.VISIBLE else View.GONE
        b.layoutPasoCobroDetalle.visibility = if (numero == 3) View.VISIBLE else View.GONE

        if (esTablet()) {
            b.layoutCaja?.visibility = if (mostrarFlujo) View.GONE else View.VISIBLE
            if (mostrarFlujo) {
                b.layoutComprobante.visibility = View.GONE
            }
        } else {
            // Control de visibilidad del buscador superior de fondo
            val visibilidadFondo = if (mostrarFlujo) View.GONE else View.VISIBLE
            b.buscador?.visibility = visibilidadFondo
            b.layoutBuscadorInline?.visibility = visibilidadFondo

            // === CORRECCIÓN CRÍTICA: Al volver al paso 1, sólo mostramos el carrito si tiene productos ===
            val mostrarComponentesCarrito = if (mostrarFlujo) {
                View.GONE
            } else {
                if (listaProductosCaja.isNotEmpty()) View.VISIBLE else View.GONE
            }

            b.cardResumenCaja.visibility = mostrarComponentesCarrito
            b.recyclerView2.visibility = mostrarComponentesCarrito
            b.headerProductos.visibility = mostrarComponentesCarrito

            // Control del dibujo/ilustración de caja vacía
            if (!mostrarFlujo) {
                b.root.findViewById<View>(R.id.layoutEstadoVacioCaja)?.visibility =
                    if (listaProductosCaja.isEmpty()) View.VISIBLE else View.GONE
            } else {
                b.root.findViewById<View>(R.id.layoutEstadoVacioCaja)?.visibility = View.GONE
            }
        }
    }

    private fun esMismoMetodoCobro(a: MetodoPagoConfig?, b: MetodoPagoConfig): Boolean {
        if (a == null) return false
        return a.categoria == b.categoria &&
            a.titulo == b.titulo &&
            a.orden == b.orden
    }

    @SuppressLint("SetTextI18n")
    private fun mostrarPasoDetalleCobroInline(
        metodo: MetodoPagoConfig,
        totalPagar: Double,
        forzarRecrear: Boolean = false
    ) {
        val b = _binding ?: return
        totalCobroActual = totalPagar
        actualizarResumenPasoCobro(totalPagar)

        val debeRecrear = forzarRecrear ||
            vistaDetalleCobroInline == null ||
            !esMismoMetodoCobro(metodoCobroSeleccionado, metodo)

        metodoCobroSeleccionado = metodo

        if (!debeRecrear) {
            irAPaso(3)
            return
        }

        b.contenedorPasoCobroDetalle.removeAllViews()
        val view = layoutInflater.inflate(R.layout.bottomsheet_tipo_pago, b.contenedorPasoCobroDetalle, false)
        vistaDetalleCobroInline = view
        b.contenedorPasoCobroDetalle.addView(view)

        var confirmando = false
        val operationIdCobro = generarOperationIdVenta()

        val viewHandleCobro = view.findViewById<View>(R.id.viewHandleCobro)
        val layoutStepperCobro = view.findViewById<View>(R.id.layoutStepperCobro)
        val tvTituloCobro = view.findViewById<TextView>(R.id.tvTituloCobro)
        val tvSubtituloCobro = view.findViewById<TextView>(R.id.tvSubtituloCobro)
        val tvTotalPagar = view.findViewById<TextView>(R.id.tvTotalPagar)
        val tvLabelMetodosPago = view.findViewById<TextView>(R.id.tvLabelMetodosPago)
        val recyclerMetodosPago = view.findViewById<RecyclerView>(R.id.recyclerMetodosPago)

        val tvDetalleTitulo = view.findViewById<TextView>(R.id.tvDetalleTitulo)
        val tvDescripcionMetodo = view.findViewById<TextView>(R.id.tvDescripcionMetodo)
        val cardDetalleMetodo = view.findViewById<View>(R.id.cardDetalleMetodo)
        val layoutDetalleEfectivo = view.findViewById<View>(R.id.layoutDetalleEfectivo)
        val layoutDetalleTransferencia = view.findViewById<View>(R.id.layoutDetalleTransferencia)
        val layoutDetalleBilletera = view.findViewById<View>(R.id.layoutDetalleBilletera)
        val tvDetalleGenerico = view.findViewById<TextView>(R.id.tvDetalleGenerico)

        val edtMontoRecibido = view.findViewById<TextInputEditText>(R.id.edtMontoRecibido)
        val layoutMontoRecibido = view.findViewById<TextInputLayout>(R.id.layoutMontoRecibido)
        val txtResultadoLabel = view.findViewById<TextView>(R.id.txtResultadoLabel)
        val tvResultadoPago = view.findViewById<TextView>(R.id.tvResultadoPago)

        val tvBanco = view.findViewById<TextView>(R.id.tvBanco)
        val tvTipoCuenta = view.findViewById<TextView>(R.id.tvTipoCuenta)
        val tvNumeroCuenta = view.findViewById<TextView>(R.id.tvNumeroCuenta)
        val tvTitularBanco = view.findViewById<TextView>(R.id.tvTitularBanco)
        val tvDocumentoBanco = view.findViewById<TextView>(R.id.tvDocumentoBanco)
        val edtReferenciaTransferencia = view.findViewById<TextInputEditText>(R.id.edtReferenciaTransferencia)
        val layoutReferenciaTransferencia = view.findViewById<TextInputLayout>(R.id.layoutReferenciaTransferencia)

        val tvTitularBilletera = view.findViewById<TextView>(R.id.tvTitularBilletera)
        val tvTelefonoBilletera = view.findViewById<TextView>(R.id.tvTelefonoBilletera)
        val tvAliasBilletera = view.findViewById<TextView>(R.id.tvAliasBilletera)
        val tvInstruccionesBilletera = view.findViewById<TextView>(R.id.tvInstruccionesBilletera)
        val cardQrBilletera = view.findViewById<View>(R.id.cardQrBilletera)
        val imgQrBilletera = view.findViewById<ImageView>(R.id.imgQrBilletera)

        val cardPagoMixto = view.findViewById<View>(R.id.cardPagoMixto)
        val recyclerPagoMixto = view.findViewById<RecyclerView>(R.id.recyclerPagoMixto)
        val tvTotalMixto = view.findViewById<TextView>(R.id.tvTotalMixto)
        val tvDescripcionMixto = view.findViewById<TextView>(R.id.tvDescripcionMixto)

        val btnCancelarCobro = view.findViewById<MaterialButton>(R.id.btnCancelarCobro)
        val btnConfirmarVenta = view.findViewById<MaterialButton>(R.id.btnConfirmarVenta)

        viewHandleCobro.visibility = View.GONE
        layoutStepperCobro.visibility = View.GONE
        tvLabelMetodosPago.visibility = View.GONE
        recyclerMetodosPago.visibility = View.GONE

        configurarOcultarTecladoEnDialogo(view, edtMontoRecibido, edtReferenciaTransferencia)

        tvTituloCobro.text = metodo.titulo.ifBlank { metodo.categoria.replaceFirstChar { it.uppercase() } }
        tvSubtituloCobro.text = "Completa los datos del cobro para confirmar la venta"
        tvTotalPagar.text = MonedaHelper.formatear(totalPagar)
        tvResultadoPago.text = MonedaHelper.formatear(0.0)
        btnConfirmarVenta.text = "Cobrar ${MonedaHelper.formatear(totalPagar)}"
        btnCancelarCobro.text = "Volver"

        val itemsMixto = mutableListOf<ItemPagoMixtoUi>()
        lateinit var adapterMixto: AdapterPagoMixto
        var metodoSeleccionado: MetodoPagoConfig? = metodo
        val debounceMontoRecibidoMs = 280L

        fun actualizarTotalesMixto() {
            val totalMixto = calcularTotalMixto(itemsMixto)
            val restante = totalPagar - totalMixto
            val totalTexto = MonedaHelper.formatear(totalPagar)
            val asignadoTexto = MonedaHelper.formatear(totalMixto)
            val restanteTexto = MonedaHelper.formatear(kotlin.math.abs(restante))

            if (itemsMixto.isEmpty()) {
                tvTotalMixto.text = "No hay métodos disponibles"
                tvTotalMixto.setTextColor(Color.GRAY)
            } else {
                when {
                    kotlin.math.abs(restante) < 0.01 -> {
                        tvTotalMixto.text = "¡Monto completo! ($totalTexto)"
                        tvTotalMixto.setTextColor(Color.parseColor("#2E7D32"))
                    }
                    restante > 0 -> {
                        tvTotalMixto.text = "Asignado: $asignadoTexto | Falta: $restanteTexto"
                        tvTotalMixto.setTextColor(Color.parseColor("#111111"))
                    }
                    else -> {
                        tvTotalMixto.text = "Asignado: $asignadoTexto | Excedido: $restanteTexto"
                        tvTotalMixto.setTextColor(Color.RED)
                    }
                }
            }
        }

        fun actualizarEstadoBotonConfirmar() {
            val metodoActual = metodoSeleccionado
            if (metodoActual == null || confirmando) {
                btnConfirmarVenta.isEnabled = false
                btnConfirmarVenta.alpha = 0.6f
                return
            }

            val habilitado = if (metodoActual.esMixtoLike()) {
                if (itemsMixto.isEmpty()) {
                    false
                } else {
                    val totalMixto = calcularTotalMixto(itemsMixto)
                    kotlin.math.abs(totalMixto - totalPagar) < 0.01
                }
            } else if (metodoActual.esEfectivoLike()) {
                val textoMonto = edtMontoRecibido.text?.toString()?.trim().orEmpty()
                if (textoMonto.isEmpty()) {
                    true
                } else {
                    val montoRecibido = textoMonto.toDoubleOrNull() ?: 0.0
                    montoRecibido >= totalPagar - 0.01
                }
            } else if (metodoActual.esTransferenciaLike() && metodoActual.permiteReferencia) {
                edtReferenciaTransferencia.text?.isNotBlank() == true
            } else {
                true
            }

            btnConfirmarVenta.isEnabled = habilitado
            btnConfirmarVenta.alpha = if (habilitado) 1f else 0.6f
        }

        fun limpiarDetalles() {
            cardDetalleMetodo.visibility = View.VISIBLE
            layoutDetalleEfectivo.visibility = View.GONE
            layoutDetalleTransferencia.visibility = View.GONE
            layoutDetalleBilletera.visibility = View.GONE
            tvDetalleGenerico.visibility = View.GONE
            cardPagoMixto.visibility = View.GONE

            layoutMontoRecibido.error = null
            layoutReferenciaTransferencia.error = null

            edtMontoRecibido.setText("")
            edtReferenciaTransferencia.setText("")

            txtResultadoLabel.text = "Resultado"
            tvResultadoPago.text = MonedaHelper.formatear(0.0)
            cardQrBilletera.visibility = View.GONE
        }

        fun calcularVueltoUI() {
            val metodoActual = metodoSeleccionado ?: return
            val textoMonto = edtMontoRecibido.text?.toString()?.trim().orEmpty()

            if (!metodoActual.esEfectivoLike()) {
                txtResultadoLabel.text = "Pago"
                tvResultadoPago.text = MonedaHelper.formatear(totalPagar)
                tvResultadoPago.setTextColor(Color.parseColor("#111111"))
                return
            }

            txtResultadoLabel.text = "Vuelto"

            if (textoMonto.isEmpty()) {
                tvResultadoPago.text = MonedaHelper.formatear(0.0)
                tvResultadoPago.setTextColor(Color.parseColor("#111111"))
                return
            }

            val montoRecibido = textoMonto.toDoubleOrNull()
            if (montoRecibido == null) {
                tvResultadoPago.text = MonedaHelper.formatear(0.0)
                tvResultadoPago.setTextColor(Color.parseColor("#111111"))
                return
            }

            val vuelto = montoRecibido - totalPagar
            when {
                vuelto > 0.009 -> {
                    txtResultadoLabel.text = "Vuelto"
                    tvResultadoPago.text = MonedaHelper.formatear(vuelto)
                    tvResultadoPago.setTextColor(Color.parseColor("#111111"))
                }
                kotlin.math.abs(vuelto) < 0.01 -> {
                    txtResultadoLabel.text = "Resultado"
                    tvResultadoPago.text = "Pago exacto"
                    tvResultadoPago.setTextColor(Color.parseColor("#15803D"))
                }
                else -> {
                    txtResultadoLabel.text = "Falta"
                    tvResultadoPago.text = MonedaHelper.formatear(kotlin.math.abs(vuelto))
                    tvResultadoPago.setTextColor(Color.parseColor("#B3261E"))
                }
            }
        }

        fun programarActualizacionMontoRecibido() {
            runnableMontoRecibido?.let(handlerMontoRecibido::removeCallbacks)
            val tarea = Runnable {
                calcularVueltoUI()
                actualizarEstadoBotonConfirmar()
            }
            runnableMontoRecibido = tarea
            handlerMontoRecibido.postDelayed(tarea, debounceMontoRecibidoMs)
        }

        edtMontoRecibido.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                layoutMontoRecibido.error = null
                programarActualizacionMontoRecibido()
            }
        })

        edtReferenciaTransferencia.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                layoutReferenciaTransferencia.error = null
                actualizarEstadoBotonConfirmar()
            }
        })

        fun mostrarDetalleMetodo(metodoDetalle: MetodoPagoConfig) {
            metodoSeleccionado = metodoDetalle
            limpiarDetalles()
            actualizarEstadoBotonConfirmar()

            tvDetalleTitulo.text = "Detalle del pago"
            tvDetalleTitulo.visibility =
                if (metodoDetalle.esEfectivoLike()) View.GONE else View.VISIBLE

            if (metodoDetalle.esMixtoLike()) {
                cardDetalleMetodo.visibility = View.GONE
                cardPagoMixto.visibility = View.VISIBLE
                actualizarTotalesMixto()
            }

            if (metodoDetalle.descripcion.isNotBlank()) {
                tvDescripcionMetodo.visibility = View.VISIBLE
                tvDescripcionMetodo.text = metodoDetalle.descripcion
            } else {
                tvDescripcionMetodo.visibility = View.GONE
            }

            when {
                metodoDetalle.esEfectivoLike() -> {
                    layoutDetalleEfectivo.visibility = View.VISIBLE
                    txtResultadoLabel.text = "Vuelto"
                    calcularVueltoUI()
                }

                metodoDetalle.esTransferenciaLike() -> {
                    layoutDetalleTransferencia.visibility = View.VISIBLE
                    tvBanco.text = "Banco: ${metodoDetalle.banco.ifBlank { "-" }}"
                    tvTipoCuenta.text = "Tipo de cuenta: ${metodoDetalle.tipoCuenta.ifBlank { "-" }}"
                    tvNumeroCuenta.text = "Número: ${metodoDetalle.numeroCuenta.ifBlank { "-" }}"
                    tvTitularBanco.text = "Titular: ${metodoDetalle.titularBanco.ifBlank { "-" }}"
                    tvDocumentoBanco.text = "Documento: ${metodoDetalle.documentoBanco.ifBlank { "-" }}"
                    layoutReferenciaTransferencia.visibility =
                        if (metodoDetalle.permiteReferencia) View.VISIBLE else View.GONE
                    txtResultadoLabel.text = "Pago"
                    tvResultadoPago.text = MonedaHelper.formatear(totalPagar)
                }

                metodoDetalle.esBilleteraLike() -> {
                    layoutDetalleBilletera.visibility = View.VISIBLE
                    tvTitularBilletera.text = "Titular: ${metodoDetalle.titularBilletera.ifBlank { "-" }}"
                    tvTelefonoBilletera.text = "Teléfono: ${metodoDetalle.telefonoBilletera.ifBlank { "-" }}"
                    tvAliasBilletera.text = "Alias: ${metodoDetalle.aliasBilletera.ifBlank { "-" }}"
                    tvInstruccionesBilletera.text = "Instrucciones: ${metodoDetalle.instrucciones.ifBlank { "-" }}"

                    if (metodoDetalle.usaQR && metodoDetalle.qrUrl.isNotBlank()) {
                        cardQrBilletera.visibility = View.VISIBLE
                        Glide.with(view.context)
                            .load(metodoDetalle.qrUrl)
                            .placeholder(R.drawable.baseline_sync_24)
                            .error(R.drawable.close_24)
                            .into(imgQrBilletera)
                    } else {
                        cardQrBilletera.visibility = View.GONE
                    }

                    txtResultadoLabel.text = "Pago"
                    tvResultadoPago.text = MonedaHelper.formatear(totalPagar)
                }

                metodoDetalle.esMixtoLike() -> {
                    cardPagoMixto.visibility = View.VISIBLE
                    txtResultadoLabel.text = "Pago"
                    tvResultadoPago.text = MonedaHelper.formatear(totalPagar)
                    tvDescripcionMixto.text =
                        "Selecciona uno o más métodos, marca sus montos y la suma debe ser igual al total."
                    actualizarTotalesMixto()
                }

                else -> {
                    tvDetalleGenerico.visibility = View.VISIBLE
                    txtResultadoLabel.text = "Pago"
                    tvResultadoPago.text = MonedaHelper.formatear(totalPagar)
                }
            }

            actualizarEstadoBotonConfirmar()
        }

        recyclerPagoMixto.layoutManager = LinearLayoutManager(requireContext())
        val metodosParaMixto = metodosPagoCobroActivos.filter {
            !it.esMixtoLike() && it.disponibleMixto && it.activo
        }
        itemsMixto.clear()
        itemsMixto.addAll(metodosParaMixto.map { ItemPagoMixtoUi(it) })
        adapterMixto = AdapterPagoMixto(itemsMixto) {
            actualizarTotalesMixto()
            actualizarEstadoBotonConfirmar()
        }
        recyclerPagoMixto.adapter = adapterMixto

        edtMontoRecibido.doAfterTextChanged {
            val metodoActual = metodoSeleccionado ?: return@doAfterTextChanged
            if (!metodoActual.esEfectivoLike()) return@doAfterTextChanged

            val textoMonto = it?.toString()?.trim().orEmpty()
            val montoRecibido = textoMonto.toDoubleOrNull()

            layoutMontoRecibido.error = when {
                textoMonto.isEmpty() -> null
                montoRecibido == null -> "Ingresa un monto válido"
                montoRecibido < totalPagar -> "Monto insuficiente"
                else -> null
            }

            calcularVueltoUI()
        }

        btnCancelarCobro.setOnClickListener {
            if (confirmando) return@setOnClickListener
            if (metodosPagoCobroActivos.size == 1) {
                liberarCajaSiEstabaBloqueada()
                reiniciarFlujoCobroInline()
            } else {
                irAPaso(2)
            }
        }

        btnConfirmarVenta.setOnClickListener {
            if (confirmando || ventaEnProceso) return@setOnClickListener

            val metodoActual = metodoSeleccionado
            if (metodoActual == null) {
                mostrarToastSeguro("Selecciona un método de pago")
                return@setOnClickListener
            }

            confirmando = true
            btnConfirmarVenta.isEnabled = false
            btnConfirmarVenta.alpha = 0.5f
            btnCancelarCobro.isEnabled = false

            val metodoPagoFinal: String
            val montoRecibidoFinal: Double
            val detallePagoTurnoFinal: Map<String, Double>

            fun abortarConfirmacion(error: String?, layout: TextInputLayout? = null) {
                confirmando = false
                btnConfirmarVenta.isEnabled = true
                btnConfirmarVenta.alpha = 1f
                btnCancelarCobro.isEnabled = true
                layout?.error = error
                if (error != null && layout == null) mostrarToastSeguro(error)
            }

            when {
                metodoActual.esTransferenciaLike() -> {
                    val ref = edtReferenciaTransferencia.text?.toString()?.trim().orEmpty()
                    if (metodoActual.permiteReferencia && ref.isEmpty()) {
                        abortarConfirmacion("Ingresa la referencia", layoutReferenciaTransferencia)
                        return@setOnClickListener
                    }
                    metodoPagoFinal =
                        if (ref.isNotEmpty()) "${metodoActual.titulo} (Ref: $ref)" else metodoActual.titulo
                    montoRecibidoFinal = totalPagar
                    detallePagoTurnoFinal =
                        mapOf(metodoActual.titulo.ifBlank { "Transferencia" } to totalPagar)
                }

                metodoActual.esEfectivoLike() -> {
                    val textoMonto = edtMontoRecibido.text?.toString()?.trim().orEmpty()
                    val montoRec = textoMonto.toDoubleOrNull() ?: 0.0

                    if (textoMonto.isNotEmpty() && montoRec < totalPagar - 0.01) {
                        abortarConfirmacion("Monto insuficiente", layoutMontoRecibido)
                        return@setOnClickListener
                    }

                    metodoPagoFinal = metodoActual.titulo.ifBlank { "Efectivo" }
                    montoRecibidoFinal = if (montoRec > 0) montoRec else totalPagar
                    detallePagoTurnoFinal = mapOf(metodoPagoFinal to totalPagar)
                }

                metodoActual.esMixtoLike() -> {
                    val seleccionados = itemsMixto.filter { it.seleccionado }
                    if (seleccionados.isEmpty()) {
                        abortarConfirmacion("Selecciona al menos un método para el pago mixto")
                        return@setOnClickListener
                    }

                    val tieneMontoInvalido = seleccionados.any { (it.monto.toDoubleOrNull() ?: 0.0) <= 0.0 }
                    if (tieneMontoInvalido) {
                        abortarConfirmacion("Ingresa montos válidos en el pago mixto")
                        return@setOnClickListener
                    }

                    val totalMixto = calcularTotalMixto(itemsMixto)
                    if (kotlin.math.abs(totalMixto - totalPagar) >= 0.01) {
                        abortarConfirmacion(
                            "La suma del pago mixto debe ser igual al total (${MonedaHelper.formatear(totalPagar)})"
                        )
                        return@setOnClickListener
                    }

                    metodoPagoFinal = "Mixto: " + seleccionados.joinToString(" + ") { item ->
                        "${item.metodo.titulo} ${MonedaHelper.formatear(item.monto.toDoubleOrNull() ?: 0.0)}"
                    }
                    montoRecibidoFinal = totalPagar
                    detallePagoTurnoFinal = seleccionados.associate { item ->
                        val monto = item.monto.toDoubleOrNull() ?: 0.0
                        item.metodo.titulo.ifBlank { item.metodo.categoria } to monto
                    }
                }

                else -> {
                    metodoPagoFinal = metodoActual.titulo.ifBlank { metodoActual.categoria }
                    montoRecibidoFinal = totalPagar
                    detallePagoTurnoFinal = mapOf(metodoPagoFinal to totalPagar)
                }
            }

            confirmarVenta(
                totalPagar = totalPagar,
                metodoPago = metodoPagoFinal,
                montoRecibido = montoRecibidoFinal,
                detalleMetodosTurno = detallePagoTurnoFinal,
                operationId = operationIdCobro,
                dialog = null,
                onFinish = {
                    ocultarDialogoProgresoVenta()
                    confirmando = false
                    btnConfirmarVenta.isEnabled = true
                    btnConfirmarVenta.alpha = 1f
                    btnCancelarCobro.isEnabled = true
                    actualizarEstadoBotonConfirmar()
                }
            )
            mostrarDialogoProgresoVenta()
        }

        mostrarDetalleMetodo(metodo)
        irAPaso(3)
    }

    private fun precargarMetodosPagoActivos() {
        if (cacheMetodosPagoActivos != null || cargandoMetodosPagoActivos) return
        obtenerMetodosPagoActivos(onSuccess = {}, onError = {})
    }

    private fun resolverSolicitudesMetodosPagoActivos(
        metodos: List<MetodoPagoConfig>? = null,
        error: String? = null
    ) {
        val pendientes = solicitudesMetodosPagoPendientes.toList()
        solicitudesMetodosPagoPendientes.clear()

        when {
            metodos != null && metodos.isNotEmpty() -> pendientes.forEach { it.onSuccess(metodos) }
            else -> {
                val mensaje = error ?: "No hay tipos de pago activos"
                pendientes.forEach { it.onError(mensaje) }
            }
        }
    }

    private fun obtenerMetodosPagoActivos(onSuccess: (List<MetodoPagoConfig>) -> Unit, onError: (String) -> Unit) {
        cacheMetodosPagoActivos?.let { cache ->
            if (cache.isEmpty()) {
                onError("No hay tipos de pago activos")
            } else {
                onSuccess(cache)
            }
            return
        }

        solicitudesMetodosPagoPendientes.add(
            SolicitudMetodosPagoActivos(
                onSuccess = onSuccess,
                onError = onError
            )
        )

        if (cargandoMetodosPagoActivos) return

        cargandoMetodosPagoActivos = true
        database.getReference("ConfiguracionTienda")
            .child("metodosPago")
            .get()
            .addOnSuccessListener { snapshot ->
                val lista = mutableListOf<MetodoPagoConfig>()

                for (child in snapshot.children) {
                    val metodo = child.getValue(MetodoPagoConfig::class.java) ?: continue
                    if (metodo.activo) {
                        lista.add(metodo)
                    }
                }

                cargandoMetodosPagoActivos = false
                cacheMetodosPagoActivos = lista.sortedBy { it.orden }
                resolverSolicitudesMetodosPagoActivos(metodos = cacheMetodosPagoActivos)
            }
            .addOnFailureListener { e ->
                cargandoMetodosPagoActivos = false
                resolverSolicitudesMetodosPagoActivos(
                    error = e.message ?: "No se pudieron cargar los tipos de pago"
                )
            }
    }

    private fun MetodoPagoConfig.esEfectivoLike(): Boolean {
        return categoria == "efectivo" || permiteVuelto || solicitaMontoRecibido || calculaVuelto
    }

    private fun MetodoPagoConfig.esTransferenciaLike(): Boolean {
        return categoria == "transferencia_bancaria"
    }

    private fun MetodoPagoConfig.esBilleteraLike(): Boolean {
        return categoria == "billetera_digital"
    }

    private fun MetodoPagoConfig.esMixtoLike(): Boolean {
        return categoria == "mixto"
    }

    private fun calcularTotalMixto(items: List<ItemPagoMixtoUi>): Double {
        var total = 0.0
        items.filter { it.seleccionado }.forEach { item ->
            total = CalculadoraVentasHelper.redondear(total + (item.monto.trim().toDoubleOrNull() ?: 0.0))
        }
        return total
    }

    private fun obtenerTotalVenta(): Double {
        var total = 0.0
        listaProductosCaja.forEach { prod ->
            total = CalculadoraVentasHelper.redondear(total + (prod.total.toDoubleSeguro(0.0) ?: 0.0))
        }
        return total
    }

        private fun agregarProductoDirectoTablet(
        producto: MoldeProductos,
        presentacionElegida: PresentacionProducto? = null,
        onResultado: ((Boolean) -> Unit)? = null
    ) {
        if (!puedeOperarTurnoActual() || actualizandoCaja || ventaEnProceso) {
            onResultado?.invoke(false)
            return
        }

        val unidadesYaEnCaja = obtenerUnidadesYaEnCaja(producto.indice)
        val presentacionSeleccionada = presentacionElegida ?: obtenerPresentacionUnicaOPrincipal(producto)

        if (presentacionSeleccionada == null) {
            mostrarToastSeguro("El producto no tiene presentación configurada")
            onResultado?.invoke(false)
            return
        }

        val stockInventarioUnidades = cacheStockInventario[producto.indice] 
            ?: producto.cantidadinicial.toIntOrNull() 
            ?: 0
        
        val unidadesPorPresentacion = if (presentacionSeleccionada.cantidad <= 0) 1 else presentacionSeleccionada.cantidad
        val stockRestanteUnidades = (stockInventarioUnidades - unidadesYaEnCaja).coerceAtLeast(0)

        if (stockRestanteUnidades < unidadesPorPresentacion) {
            val unidadBase = producto.unidadbase.ifBlank { "unidades" }
            val mensaje = when {
                stockInventarioUnidades <= 0 -> "Este producto ya no tiene stock en inventario"
                stockRestanteUnidades <= 0 -> "Ya tienes todo el stock disponible de este producto en tu caja"
                else -> "Esta presentación ya no alcanza. Solo quedan $stockRestanteUnidades $unidadBase libres."
            }
            mostrarToastSeguro(mensaje)
            onResultado?.invoke(false)
            return
        }

        val precio = presentacionSeleccionada.precioventa
        if (precio <= 0.0) {
            mostrarToastSeguro("La presentación no tiene precio válido")
            onResultado?.invoke(false)
            return
        }

        val unidadBase = producto.unidadbase.ifBlank { "unidades" }
        val nombrePresentacionBase = presentacionSeleccionada.nombre.ifBlank {
            producto.presentacionprincipal.ifBlank { "Unidad" }
        }

        val nombrePresentacionCompleto = PresentacionHelper.resumenPresentacionUi(
            PresentacionesTiendaConfigManager.nombreVisible(nombrePresentacionBase),
            unidadesPorPresentacion,
            unidadBase
        )

        val resolucionLote = LoteConsumoRules.resolver(producto, unidadesPorPresentacion)
        if (!ventaPuedeConsumirLotes(resolucionLote)) {
            mostrarToastSeguro(
                construirMensajeVentaSinLoteValido(
                    nombreProducto = producto.nombre,
                    resolucion = resolucionLote,
                    unidadesSolicitadas = unidadesPorPresentacion,
                    stockRegistrado = producto.cantidadinicial.toDoubleOrNull() ?: 0.0,
                    lotes = producto.lotes
                )
            )
            onResultado?.invoke(false)
            return
        }

        agregarProductosFirebaseACaja(
            cantidadPresentaciones = 1,
            unidadesPorPresentacion = unidadesPorPresentacion,
            nombre = producto.nombre,
            total = String.format(Locale.US, "%.2f", precio),
            categoria = producto.categoria,
            indice = producto.indice,
            precioUnitarioProductos = precio.toString(),
            nombrePresentacion = nombrePresentacionCompleto,
            dialog = null,
            loteActual = resolucionLote.loteActual,
            loteFefo = resolucionLote.loteRecomendadoFefo,
            loteSeleccionManual = resolucionLote.usaSeleccionManual
        )
        onResultado?.invoke(true)
    }

    private fun obtenerPresentacionUnicaOPrincipal(producto: MoldeProductos): PresentacionProducto? {
        if (producto.tienePresentaciones && producto.presentaciones.isNotEmpty()) {
            val principal = producto.presentacionprincipal.trim()

            return producto.presentaciones.firstOrNull {
                PresentacionHelper.sonNombresEquivalentes(it.nombre, principal)
            } ?: producto.presentaciones.firstOrNull()
        }

        return PresentacionProducto(
            nombre = producto.presentacionprincipal.ifEmpty { "Unidad" },
            cantidad = 1,
            precioventa = producto.preciodecompra.toDoubleOrNull() ?: 0.0
        )
    }

    private fun obtenerUnidadesYaEnCaja(indiceProducto: String): Int {
        var totalUnidadesEnCaja = 0
        listaProductosCaja.forEach { productoCaja ->
            if (productoCaja.indiceProducto == indiceProducto) {
                val cantidadPresentaciones = productoCaja.cantidad.toIntOrNull() ?: 0
                val unidadesPorPresentacion = productoCaja.unidadesPorPresentacion.toIntOrNull() ?: 1
                totalUnidadesEnCaja += cantidadPresentaciones * unidadesPorPresentacion
            }
        }
        return totalUnidadesEnCaja
    }

    private fun agregarProductosFirebaseACaja(
        cantidadPresentaciones: Int,
        unidadesPorPresentacion: Int,
        nombre: String,
        total: String,
        categoria: String,
        indice: String,
        precioUnitarioProductos: String,
        nombrePresentacion: String,
        dialog: BottomSheetDialog?,
        loteActual: Pair<String, LoteProducto>? = null,
        loteFefo: Pair<String, LoteProducto>? = null,
        loteSeleccionManual: Boolean = false
    ) {
        if (bloquearOperacionPorRescateCajaBloqueada()) return
        val presentacionKey = PresentacionHelper.claveFirebaseSegura(nombrePresentacion)
        val loteNumeroActual = loteActual?.second?.numero?.ifBlank { loteActual.first }.orEmpty()
        val loteNumeroManual = if (loteSeleccionManual) loteNumeroActual else ""
        val loteVencimientoManual = if (loteSeleccionManual) loteActual?.second?.vencimiento.orEmpty() else ""
        val loteReferenciaFefo = loteFefo ?: if (!loteSeleccionManual) loteActual else null
        val loteKey = loteNumeroManual
            .takeIf { it.isNotBlank() }
            ?.let { PresentacionHelper.claveFirebaseSegura(it) }
            .orEmpty()

        val claveProductoCaja = if (loteKey.isNotBlank()) {
            "${indice}_${presentacionKey}_$loteKey"
        } else {
            "${indice}_$presentacionKey"
        }

        val reference = getCajaRef()
            .child("Productos")
            .child(claveProductoCaja)

        val precioUnitario = precioUnitarioProductos.toDoubleOrNull() ?: 0.0

        reference.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val productoActual = currentData.getValue(ProductoCaja::class.java)

                val cantidadActual = productoActual?.cantidad?.toIntOrNull() ?: 0
                val cantidadFinal = cantidadActual + cantidadPresentaciones
                val nuevoTotal = cantidadFinal * precioUnitario

                currentData.value = ProductoCaja(
                    id = claveProductoCaja,
                    indiceProducto = indice,
                    nombre = nombre,
                    categoria = categoria,
                    presentacion = nombrePresentacion,
                    cantidad = cantidadFinal.toString(),
                    unidadesPorPresentacion = unidadesPorPresentacion.toString(),
                    precioUnitario = precioUnitarioProductos,
                    total = String.format(Locale.US, "%.2f", nuevoTotal),
                    loteNumero = loteNumeroManual,
                    loteVencimiento = loteVencimientoManual,
                    loteNumeroFefo = loteReferenciaFefo?.second?.numero?.ifBlank { loteReferenciaFefo.first }.orEmpty(),
                    loteVencimientoFefo = loteReferenciaFefo?.second?.vencimiento.orEmpty(),
                    loteSeleccionManual = loteSeleccionManual
                )

                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null || !committed) {
                    mostrarToastSeguro("Error al agregar producto a la caja")
                    return
                }

                val productoActual = currentData?.getValue(ProductoCaja::class.java)
                val cantidadFinal = productoActual?.cantidad?.toIntOrNull() ?: cantidadPresentaciones

                if (cantidadFinal > cantidadPresentaciones) {
                    mostrarToastSeguro("Cantidad actualizada en caja")
                } else {
                    mostrarToastSeguro("Producto agregado a la caja")
                }

                dialog?.dismiss()
            }
        })
    }


    private fun liberarCajaSiEstabaBloqueada(onComplete: (() -> Unit)? = null) {
        val idCajaOperativa = obtenerIdCajaOperativa()
        val ref = database.getReference("CajasIndividuales")
            .child(idCajaOperativa)
            .child("estadoCobro")

        // SOLUCIÓN: Cancelamos inmediatamente el listener en el servidor para que no quede colgado
        ref.onDisconnect().cancel()

        ref.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val bloqueador = currentData.value as? String
                if (bloqueador == idCajera) {
                    currentData.value = null
                }
                return Transaction.success(currentData)
            }
            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                onComplete?.invoke()
            }
        })
    }

    private fun generarOperationIdVenta(): String {
        val pushKey = database.reference.push().key
        return if (!pushKey.isNullOrBlank()) {
            "opventa_$pushKey"
        } else {
            "opventa_${System.currentTimeMillis()}_${UUID.randomUUID().toString().replace("-", "")}"
        }
    }

    private fun obtenerOperacionVentaRef(fechaVenta: String, operationId: String): DatabaseReference {
        val idCajaOperativa = obtenerIdCajaOperativa()
        return database.reference
            .child("OperacionesCaja")
            .child(idCajaOperativa)
            .child("ventas")
            .child(fechaVenta)
            .child(operationId)
    }

    private fun bloquearOperacionVenta(operationId: String, fechaVenta: String, totalPagar: Double, metodoPago: String, onPermitida: (yaFinalizada: Boolean) -> Unit, onError: (String) -> Unit) {
        val operationIdLimpio = operationId.trim()
        if (operationIdLimpio.isBlank()) {
            onError("No se pudo asegurar la operación de venta")
            return
        }

        val opRef = obtenerOperacionVentaRef(fechaVenta, operationIdLimpio)
        var estadoBloqueante = ""

        opRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val ahora = System.currentTimeMillis()
                val mapaRaw = currentData.value as? Map<*, *>
                val mapaActual = mapaRaw
                    ?.entries
                    ?.associate { (k, v) -> k.toString() to v }
                    ?.toMutableMap()
                    ?: mutableMapOf()

                val estadoActual = mapaActual["estado"]?.toString().orEmpty().lowercase(Locale.getDefault())
                val actualizadoTs = when (val raw = mapaActual["actualizadoTs"]) {
                    is Long -> raw
                    is Number -> raw.toLong()
                    is String -> raw.toLongOrNull() ?: 0L
                    else -> 0L
                }
                val sigueVigente = (ahora - actualizadoTs) <= TIMEOUT_OPERACION_IDEMPOTENTE_VENTA_MS

                when {
                    mapaActual.isEmpty() -> {
                        // Primera ejecución de la operación.
                    }
                    estadoActual == "finalizada" -> {
                        estadoBloqueante = "finalizada"
                        return Transaction.abort()
                    }
                    estadoActual == "procesando" && sigueVigente -> {
                        estadoBloqueante = "procesando"
                        return Transaction.abort()
                    }
                }

                val intentosPrevios = when (val raw = mapaActual["intentos"]) {
                    is Long -> raw.toInt()
                    is Number -> raw.toInt()
                    is String -> raw.toIntOrNull() ?: 0
                    else -> 0
                }

                mapaActual["operationId"] = operationIdLimpio
                mapaActual["idVenta"] = operationIdLimpio
                mapaActual["idTurno"] = idTurnoActivo
                mapaActual["estado"] = "procesando"
                mapaActual["metodoPago"] = metodoPago
                mapaActual["totalEsperado"] = String.format(Locale.US, "%.2f", totalPagar)
                mapaActual["idUsuario"] = idCajera
                mapaActual["nombreUsuario"] = nombreCajera
                mapaActual["actualizadoTs"] = ahora
                mapaActual["actualizadoTsServidor"] = ServerValue.TIMESTAMP
                if (!mapaActual.containsKey("inicioTs")) {
                    mapaActual["inicioTs"] = ahora
                    mapaActual["inicioTsServidor"] = ServerValue.TIMESTAMP
                }
                mapaActual["intentos"] = intentosPrevios + 1

                currentData.value = mapaActual
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    onError("No se pudo asegurar la venta: ${error.message}")
                    return
                }

                if (committed) {
                    onPermitida(false)
                    return
                }

                if (estadoBloqueante == "finalizada") {
                    onPermitida(true)
                    return
                }

                onError("Esta venta ya se está procesando. Espera un momento.")
            }
        })
    }

    private fun actualizarEstadoOperacionVenta(operationId: String, fechaVenta: String, estado: String, detalle: String = "", idVenta: String = operationId) {
        val operationIdLimpio = operationId.trim()
        if (operationIdLimpio.isBlank()) return

        val updates = hashMapOf<String, Any?>(
            "estado" to estado,
            "detalle" to detalle,
            "idVenta" to idVenta,
            "idTurno" to idTurnoActivo,
            "actualizadoTs" to ServerValue.TIMESTAMP,
            "actualizadoTsServidor" to ServerValue.TIMESTAMP,
            "ultimaActualizacionServidor" to ServerValue.TIMESTAMP
        )

        if (estado.equals("finalizada", ignoreCase = true)) {
            updates["finalizadaTs"] = ServerValue.TIMESTAMP
            updates["finalizadaTsServidor"] = ServerValue.TIMESTAMP
        }
        if (estado.equals("fallida", ignoreCase = true)) {
            updates["fallidaTs"] = ServerValue.TIMESTAMP
            updates["fallidaTsServidor"] = ServerValue.TIMESTAMP
        }

        obtenerOperacionVentaRef(fechaVenta, operationIdLimpio).updateChildren(updates)
    }

    private fun obtenerClaveStockControlProducto(productoCaja: ProductoCaja, index: Int = 0): String {
        val base = productoCaja.id.trim().ifBlank {
            "${productoCaja.indiceProducto.trim()}_${productoCaja.presentacion.trim()}_${index}"
        }

        return base
            .replace(".", "_")
            .replace("#", "_")
            .replace("\$", "_")
            .replace("[", "_")
            .replace("]", "_")
            .replace("/", "_")
            .ifBlank { "item_$index" }
    }

    private fun Any?.toLongSeguro(default: Long = 0L): Long {
        return when (this) {
            is Long -> this
            is Int -> this.toLong()
            is Double -> this.toLong()
            is Float -> this.toLong()
            is String -> this.trim().toLongOrNull() ?: default
            else -> default
        }
    }

    private fun Any?.toBooleanSeguro(default: Boolean = false): Boolean {
        return when (this) {
            is Boolean -> this
            is Number -> this.toInt() != 0
            is String -> {
                when (this.trim().lowercase(Locale.getDefault())) {
                    "1", "true", "si", "sí" -> true
                    "0", "false", "no" -> false
                    else -> default
                }
            }
            else -> default
        }
    }

    private fun reconciliarPreRegistrosPendientesSiCorresponde() {
        if (!turnoAbierto || idTurnoActivo.isBlank() || fechaTurnoActivo.isBlank()) return
        if (reconciliacionPreRegistroEnCurso) return

        val idCajaOperativa = obtenerIdCajaOperativa()
        if (idCajaOperativa.isBlank()) return

        val claveActual = "$idCajaOperativa|$fechaTurnoActivo|$idTurnoActivo"
        if (claveReconciliacionPreRegistro == claveActual) return

        reconciliacionPreRegistroEnCurso = true
        val ahora = System.currentTimeMillis()

        database.reference
            .child("VentasPorCajera")
            .child(idCajaOperativa)
            .child(fechaTurnoActivo)
            .get()
            .addOnSuccessListener { fechaSnapshot ->
                val pendientes = mutableListOf<VentaPendienteReversion>()

                fechaSnapshot.children.forEach { ventaSnapshot ->
                    val idVenta = ventaSnapshot.key.orEmpty().trim()
                    if (idVenta.isBlank()) return@forEach

                    val idTurnoVenta = ventaSnapshot.child("infoVenta").child("idTurno").value?.toString().orEmpty()
                    if (idTurnoVenta != idTurnoActivo) return@forEach

                    val estadoPre = ventaSnapshot
                        .child("preRegistro")
                        .child("estado")
                        .value
                        ?.toString()
                        .orEmpty()
                        .lowercase(Locale.getDefault())

                    if (estadoPre == "finalizada") return@forEach

                    val tsPre = ventaSnapshot.child("preRegistro").child("ultimaActualizacion").value.toLongSeguro(
                        ventaSnapshot.child("preRegistro").child("timestamp").value.toLongSeguro(
                            ventaSnapshot.child("infoVenta").child("timestamp").value.toLongSeguro(0L)
                        )
                    )

                    val esReciente = tsPre > 0L && (ahora - tsPre) < TIMEOUT_PRE_REGISTRO_REVERSION_MS
                    if (esReciente) return@forEach

                    // HUMANO: Verificamos si la venta realmente necesita una devolución de stock
                    val reversionPendiente =
                        ventaSnapshot.child("preRegistro").child("reversionPendiente").value.toBooleanSeguro()
                    val fase = ventaSnapshot.child("preRegistro").child("fase").value?.toString().orEmpty()

                    // Solo actuamos si se marcó como pendiente o si el cobro se inició pero nunca terminó
                    if (!reversionPendiente && fase != "cobro_iniciado") return@forEach

                    val productos = ventaSnapshot
                        .child("productos")
                        .children
                        .mapNotNull { it.getValue(ProductoCaja::class.java) }

                    if (productos.isEmpty()) return@forEach

                    val operationId = ventaSnapshot.child("preRegistro").child("operationId").value?.toString()
                        .orEmpty()
                        .ifBlank {
                            ventaSnapshot.child("infoVenta").child("operationId").value?.toString().orEmpty()
                        }

                    // HUMANO: Avisamos en el historial que encontramos un error y lo estamos arreglando
                    MovimientoLogger.registrar(
                        tipo = "reconciliacion_automatica",
                        modulo = "caja",
                        titulo = "Recuperación de venta inconclusa",
                        descripcion = "Se detectó la venta #$idVenta sin finalizar (Fase: $fase). Iniciando restauración de stock automática para proteger el inventario.",
                        idUsuario = idCajera,
                        nombreUsuario = nombreCajera,
                        referenciaId = idVenta,
                        idCaja = idCajaOperativa
                    )

                    pendientes += VentaPendienteReversion(
                        idVenta = idVenta,
                        operationId = operationId,
                        productos = productos
                    )
                }

                if (pendientes.isEmpty()) {
                    claveReconciliacionPreRegistro = claveActual
                    reconciliacionPreRegistroEnCurso = false
                    return@addOnSuccessListener
                }

                procesarReversionesPendientesRecursivo(
                    pendientes = pendientes,
                    index = 0,
                    fechaVenta = fechaTurnoActivo,
                    onComplete = {
                        claveReconciliacionPreRegistro = claveActual
                        reconciliacionPreRegistroEnCurso = false
                    }
                )
            }
            .addOnFailureListener { e ->
                Log.w("CajaReversion", "No se pudo verificar ventas pendientes: ${e.message}")
                reconciliacionPreRegistroEnCurso = false
            }
    }

    private fun procesarReversionesPendientesRecursivo(
        pendientes: List<VentaPendienteReversion>,
        index: Int,
        fechaVenta: String,
        onComplete: () -> Unit
    ) {
        if (index >= pendientes.size) {
            onComplete()
            return
        }

        val pendiente = pendientes[index]
        marcarPreRegistroVentaFallida(
            idVenta = pendiente.idVenta,
            fechaVenta = fechaVenta,
            motivo = "Reversión automática por venta incompleta"
        )

        restaurarStocks(
            productosCaja = pendiente.productos,
            lastIndex = pendiente.productos.size - 1,
            stocksOriginales = mutableMapOf(),
            fechaVenta = fechaVenta,
            idVenta = pendiente.idVenta
        ) {
            if (pendiente.operationId.isNotBlank()) {
                actualizarEstadoOperacionVenta(
                    operationId = pendiente.operationId,
                    fechaVenta = fechaVenta,
                    estado = "fallida",
                    detalle = "Stock revertido automáticamente por contingencia",
                    idVenta = pendiente.idVenta
                )
            }

            procesarReversionesPendientesRecursivo(
                pendientes = pendientes,
                index = index + 1,
                fechaVenta = fechaVenta,
                onComplete = onComplete
            )
        }
    }

    private fun confirmarVenta(
        totalPagar: Double,
        metodoPago: String,
        montoRecibido: Double,
        detalleMetodosTurno: Map<String, Double>,
        operationId: String,
        dialog: BottomSheetDialog?,
        onFinish: () -> Unit
    ) {
        validarMomentoCriticoCaja { momento ->
            confirmarVentaValidada(
                totalPagar = totalPagar,
                metodoPago = metodoPago,
                montoRecibido = montoRecibido,
                detalleMetodosTurno = detalleMetodosTurno,
                operationId = operationId,
                dialog = dialog,
                momento = momento,
                onFinish = onFinish
            )
        }
    }

    private fun confirmarVentaValidada(
        totalPagar: Double,
        metodoPago: String,
        montoRecibido: Double,
        detalleMetodosTurno: Map<String, Double>,
        operationId: String,
        dialog: BottomSheetDialog?,
        momento: FechaHoraServidorHelper.FechaHoraOficial,
        onFinish: () -> Unit
    ) {
        if (ventaEnProceso) {
            onFinish()
            return
        }

        actualizarEstadoVentaEnProceso(true, liberarBloqueo = true)
        soyYoElQueCobra = true // Reafirmamos que somos nosotros por seguridad

        obtenerProductosCajaActual(
            onSuccess = { productosCaja ->
                if (productosCaja.isEmpty()) {
                    soyYoElQueCobra = false
                    liberarCajaSiEstabaBloqueada {
                        actualizarEstadoVentaEnProceso(false)
                        ocultarLoadingOverlayCaja()
                        ocultarDialogoProgresoVenta()
                        onFinish()
                        mostrarToastSeguro("No hay productos en la caja")
                    }
                    return@obtenerProductosCajaActual
                }

                val productoInvalido = productosCaja.firstOrNull { validarProductoCajaBasico(it) != null }
                if (productoInvalido != null) {
                    soyYoElQueCobra = false
                    liberarCajaSiEstabaBloqueada {
                        actualizarEstadoVentaEnProceso(false)
                        ocultarLoadingOverlayCaja()
                        ocultarDialogoProgresoVenta()
                        onFinish()
                        mostrarToastSeguro(validarProductoCajaBasico(productoInvalido) ?: "Producto inválido")
                    }
                    return@obtenerProductosCajaActual
                }

                val stocksOriginales = mutableMapOf<String, String>()
                val fechaVenta = momento.fechaFirebase
                val horaVenta = momento.horaTexto

                bloquearOperacionVenta(
                    operationId = operationId,
                    fechaVenta = fechaVenta,
                    totalPagar = totalPagar,
                    metodoPago = metodoPago,
                    onPermitida = { yaFinalizada ->
                        if (yaFinalizada) {
                            soyYoElQueCobra = false
                            liberarCajaSiEstabaBloqueada {
                                actualizarEstadoVentaEnProceso(false)
                                ocultarLoadingOverlayCaja()
                                ocultarDialogoProgresoVenta()
                                if (dialog?.isShowing == true) dialog.dismiss()
                                onFinish()
                                mostrarDialogoVentaYaRegistrada(operationId)
                            }
                            return@bloquearOperacionVenta
                        }

                        crearPreRegistroVenta(
                            operationId = operationId,
                            productosCaja = productosCaja,
                            totalPagar = totalPagar,
                            metodoPago = metodoPago,
                            fechaVenta = fechaVenta,
                            horaVenta = horaVenta,
                            timestampVentaMs = momento.timestampServidorMs,
                            onSuccess = { idVenta ->
                                procesarVentaProductos(
                                    productosCaja = productosCaja,
                                    index = 0,
                                    stocksOriginales = stocksOriginales,
                                    idVenta = idVenta,
                                    fechaVenta = fechaVenta,
                                    onSuccess = {
                                        guardarVentaFinal(
                                            operationId = operationId,
                                            idVenta = idVenta,
                                            fechaActual = fechaVenta,
                                            horaActual = horaVenta,
                                            timestampVentaMs = momento.timestampServidorMs,
                                            productosCaja = productosCaja,
                                            totalPagar = totalPagar,
                                            metodoPago = metodoPago,
                                            montoRecibido = montoRecibido,
                                            detalleMetodosTurno = detalleMetodosTurno,
                                            dialog = dialog,
                                            stocksOriginales = stocksOriginales,
                                            onFinish = {
                                                soyYoElQueCobra = false
                                                actualizarEstadoVentaEnProceso(false)
                                                ocultarLoadingOverlayCaja()
                                                ocultarDialogoProgresoVenta()
                                                onFinish()
                                            }
                                        )
                                    },
                                    onError = { mensaje ->
                                        marcarPreRegistroVentaFallida(idVenta = idVenta, fechaVenta = fechaVenta, motivo = mensaje)
                                        actualizarEstadoOperacionVenta(operationId = operationId, fechaVenta = fechaVenta, estado = "fallida", detalle = mensaje, idVenta = idVenta)

                                        soyYoElQueCobra = false
                                        liberarCajaSiEstabaBloqueada {
                                            actualizarEstadoVentaEnProceso(false)
                                            ocultarLoadingOverlayCaja()
                                            ocultarDialogoProgresoVenta()
                                            onFinish()
                                            mostrarToastSeguro(mensaje)
                                        }
                                    }
                                )
                            },
                            onError = { mensaje ->
                                actualizarEstadoOperacionVenta(operationId = operationId, fechaVenta = fechaVenta, estado = "fallida", detalle = mensaje)

                                soyYoElQueCobra = false
                                liberarCajaSiEstabaBloqueada {
                                    actualizarEstadoVentaEnProceso(false)
                                    ocultarLoadingOverlayCaja()
                                    ocultarDialogoProgresoVenta()
                                    onFinish()
                                    mostrarToastSeguro(mensaje)
                                }
                            }
                        )
                    },
                    onError = { mensaje ->
                        soyYoElQueCobra = false
                        liberarCajaSiEstabaBloqueada {
                            actualizarEstadoVentaEnProceso(false)
                            ocultarLoadingOverlayCaja()
                            ocultarDialogoProgresoVenta()
                            onFinish()
                            mostrarToastSeguro(mensaje)
                        }
                    }
                )
            },
            onError = { mensaje ->
                soyYoElQueCobra = false
                liberarCajaSiEstabaBloqueada {
                    actualizarEstadoVentaEnProceso(false)
                    ocultarLoadingOverlayCaja()
                    ocultarDialogoProgresoVenta()
                    onFinish()
                    mostrarToastSeguro(mensaje)
                }
            }
        )
    }

    private fun obtenerProductosCajaActual(onSuccess: (List<ProductoCaja>) -> Unit, onError: (String) -> Unit) {
        getCajaRef()
            .child("Productos")
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    onSuccess(emptyList())
                    return@addOnSuccessListener
                }

                val productos = mutableListOf<ProductoCaja>()

                for (child in snapshot.children) {
                    val producto = child.getValue(ProductoCaja::class.java)
                    if (producto != null) {
                        productos.add(producto)
                    }
                }

                onSuccess(productos)
            }
            .addOnFailureListener { e ->
                onError("Error al leer la caja actual: ${e.message}")
            }
    }

    private fun crearPreRegistroVenta(
        operationId: String,
        productosCaja: List<ProductoCaja>,
        totalPagar: Double,
        metodoPago: String,
        fechaVenta: String,
        horaVenta: String,
        timestampVentaMs: Long,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val rootRef = database.reference
        val idVenta = operationId.trim()
        if (idVenta.isBlank()) {
            onError("No se pudo generar la venta")
            return
        }

        val esSupervision = estaEnModoSupervision()
        val idCajaOperativa = obtenerIdCajaOperativa()
        val nombreCajaOperativa = obtenerNombreCajaOperativa()

        val infoVenta = hashMapOf<String, Any>(
            "id" to idVenta,
            "operationId" to operationId,
            "timestamp" to timestampVentaMs,
            "timestampServidor" to ServerValue.TIMESTAMP,
            "fecha" to fechaVenta,
            "hora" to horaVenta,
            "estado" to "procesando",
            "fase" to "descontando_stock",
            "total" to String.format(Locale.US, "%.2f", totalPagar),
            "metodoPago" to metodoPago,
            "tipoComprobante" to tipoComprobanteSeleccionado,
            "idCajaOperativa" to idCajaOperativa,
            "nombreCajaOperativa" to nombreCajaOperativa,
            "idTurno" to idTurnoActivo,
            "idUsuarioQueCobro" to idCajera,
            "nombreUsuarioQueCobro" to nombreCajera,
            "esSupervision" to esSupervision,
            "idSupervisor" to if (esSupervision) idCajera else "",
            "nombreSupervisor" to if (esSupervision) nombreCajera else ""
        )

        val pathGlobal = "Ventas/$fechaVenta/$idVenta"
        val pathIndividual = "VentasPorCajera/$idCajaOperativa/$fechaVenta/$idVenta"
        val updates = hashMapOf<String, Any>(
            "$pathGlobal/infoVenta" to infoVenta,
            "$pathGlobal/total" to String.format(Locale.US, "%.2f", totalPagar),
            "$pathGlobal/preRegistro/estado" to "procesando",
            "$pathGlobal/preRegistro/fase" to "descontando_stock",
            "$pathGlobal/preRegistro/timestamp" to timestampVentaMs,
            "$pathGlobal/preRegistro/timestampServidor" to ServerValue.TIMESTAMP,
            "$pathIndividual/infoVenta" to infoVenta,
            "$pathIndividual/total" to String.format(Locale.US, "%.2f", totalPagar),
            "$pathIndividual/preRegistro/estado" to "procesando",
            "$pathIndividual/preRegistro/fase" to "descontando_stock",
            "$pathIndividual/preRegistro/timestamp" to timestampVentaMs
            ,
            "$pathIndividual/preRegistro/timestampServidor" to ServerValue.TIMESTAMP,
            "$pathGlobal/preRegistro/operationId" to operationId,
            "$pathIndividual/preRegistro/operationId" to operationId
        )

        productosCaja.forEachIndexed { index, producto ->
            val claveControl = obtenerClaveStockControlProducto(producto, index)
            val cantidadPresentaciones = producto.cantidad.toIntSeguro(0)
            val unidadesPorPresentacion = producto.unidadesPorPresentacion.toIntSeguro(1).coerceAtLeast(1)
            val unidadesVendidas = (cantidadPresentaciones * unidadesPorPresentacion).coerceAtLeast(0)

            updates["$pathGlobal/productos/${producto.id}"] = producto
            updates["$pathIndividual/productos/${producto.id}"] = producto

            updates["$pathGlobal/preRegistro/stockControl/$claveControl/indiceProducto"] = producto.indiceProducto.trim()
            updates["$pathGlobal/preRegistro/stockControl/$claveControl/nombreProducto"] = producto.nombre.orEmpty()
            updates["$pathGlobal/preRegistro/stockControl/$claveControl/presentacion"] = producto.presentacion.orEmpty()
            updates["$pathGlobal/preRegistro/stockControl/$claveControl/cantidadPresentaciones"] = cantidadPresentaciones
            updates["$pathGlobal/preRegistro/stockControl/$claveControl/unidadesPorPresentacion"] = unidadesPorPresentacion
            updates["$pathGlobal/preRegistro/stockControl/$claveControl/unidadesVendidas"] = unidadesVendidas
            updates["$pathGlobal/preRegistro/stockControl/$claveControl/descontado"] = false
            updates["$pathGlobal/preRegistro/stockControl/$claveControl/revertido"] = false

            updates["$pathIndividual/preRegistro/stockControl/$claveControl/indiceProducto"] = producto.indiceProducto.trim()
            updates["$pathIndividual/preRegistro/stockControl/$claveControl/nombreProducto"] = producto.nombre.orEmpty()
            updates["$pathIndividual/preRegistro/stockControl/$claveControl/presentacion"] = producto.presentacion.orEmpty()
            updates["$pathIndividual/preRegistro/stockControl/$claveControl/cantidadPresentaciones"] = cantidadPresentaciones
            updates["$pathIndividual/preRegistro/stockControl/$claveControl/unidadesPorPresentacion"] = unidadesPorPresentacion
            updates["$pathIndividual/preRegistro/stockControl/$claveControl/unidadesVendidas"] = unidadesVendidas
            updates["$pathIndividual/preRegistro/stockControl/$claveControl/descontado"] = false
            updates["$pathIndividual/preRegistro/stockControl/$claveControl/revertido"] = false
        }

        rootRef.updateChildren(updates)
            .addOnSuccessListener { onSuccess(idVenta) }
            .addOnFailureListener { e ->
                onError("No se pudo preparar la venta: ${e.message}")
            }
    }

    private fun marcarPreRegistroVentaFallida(
        idVenta: String,
        fechaVenta: String,
        motivo: String
    ) {
        val idCajaOperativa = obtenerIdCajaOperativa()
        val updates = hashMapOf<String, Any>(
            "Ventas/$fechaVenta/$idVenta/infoVenta/estado" to "fallida",
            "Ventas/$fechaVenta/$idVenta/preRegistro/estado" to "fallida",
            "Ventas/$fechaVenta/$idVenta/preRegistro/fase" to "reversion_stock",
            "Ventas/$fechaVenta/$idVenta/preRegistro/reversionPendiente" to true,
            "Ventas/$fechaVenta/$idVenta/preRegistro/motivo" to motivo,
            "Ventas/$fechaVenta/$idVenta/preRegistro/ultimaActualizacion" to System.currentTimeMillis(),
            "Ventas/$fechaVenta/$idVenta/preRegistro/ultimaActualizacionServidor" to ServerValue.TIMESTAMP,
            "VentasPorCajera/$idCajaOperativa/$fechaVenta/$idVenta/infoVenta/estado" to "fallida",
            "VentasPorCajera/$idCajaOperativa/$fechaVenta/$idVenta/preRegistro/estado" to "fallida",
            "VentasPorCajera/$idCajaOperativa/$fechaVenta/$idVenta/preRegistro/fase" to "reversion_stock",
            "VentasPorCajera/$idCajaOperativa/$fechaVenta/$idVenta/preRegistro/reversionPendiente" to true,
            "VentasPorCajera/$idCajaOperativa/$fechaVenta/$idVenta/preRegistro/motivo" to motivo,
            "VentasPorCajera/$idCajaOperativa/$fechaVenta/$idVenta/preRegistro/ultimaActualizacion" to System.currentTimeMillis(),
            "VentasPorCajera/$idCajaOperativa/$fechaVenta/$idVenta/preRegistro/ultimaActualizacionServidor" to ServerValue.TIMESTAMP
        )

        database.reference.updateChildren(updates)
    }

    private fun marcarStockControlDescontado(
        fechaVenta: String,
        idVenta: String,
        productoCaja: ProductoCaja,
        indexProducto: Int,
        stocksOriginales: Map<String, String>,
        onComplete: () -> Unit
    ) {
        val idCajaOperativa = obtenerIdCajaOperativa()
        val claveControl = obtenerClaveStockControlProducto(productoCaja, indexProducto)
        val claveProducto = productoCaja.indiceProducto.trim()
        val stockAntes = stocksOriginales[claveProducto].toIntSeguro(-1)
        val cantidadPresentaciones = productoCaja.cantidad.toIntSeguro(0)
        val unidadesPorPresentacion = productoCaja.unidadesPorPresentacion.toIntSeguro(1).coerceAtLeast(1)
        val unidadesVendidas = (cantidadPresentaciones * unidadesPorPresentacion).coerceAtLeast(0)
        val stockDespues = if (stockAntes >= 0) (stockAntes - unidadesVendidas).coerceAtLeast(0) else -1

        val pathGlobal = "Ventas/$fechaVenta/$idVenta/preRegistro/stockControl/$claveControl"
        val pathIndividual =
            "VentasPorCajera/$idCajaOperativa/$fechaVenta/$idVenta/preRegistro/stockControl/$claveControl"
        val pathProductoGlobal = "Ventas/$fechaVenta/$idVenta/preRegistro/productos/${productoCaja.id}"
        val pathProductoIndividual =
            "VentasPorCajera/$idCajaOperativa/$fechaVenta/$idVenta/preRegistro/productos/${productoCaja.id}"
        val loteConsumido = productoCaja.loteNumero.trim()
        val loteConsumidoClave = productoCaja.loteClaveConsumida.trim().ifBlank {
            if (loteConsumido.isNotBlank()) sanitizarClaveLoteCaja(loteConsumido) else ""
        }
        val loteConsumidoVencimiento = productoCaja.loteVencimiento.trim()
        val loteFefo = productoCaja.loteNumeroFefo.trim()
        val loteFefoVencimiento = productoCaja.loteVencimientoFefo.trim()

        val updates = hashMapOf<String, Any>(
            "$pathGlobal/descontado" to true,
            "$pathGlobal/revertido" to false,
            "$pathGlobal/stockAntes" to stockAntes,
            "$pathGlobal/stockDespues" to stockDespues,
            "$pathGlobal/loteNumeroConsumido" to loteConsumido,
            "$pathGlobal/loteClaveConsumida" to loteConsumidoClave,
            "$pathGlobal/loteVencimientoConsumido" to loteConsumidoVencimiento,
            "$pathGlobal/loteNumeroFefo" to loteFefo,
            "$pathGlobal/loteVencimientoFefo" to loteFefoVencimiento,
            "$pathGlobal/loteSeleccionManual" to productoCaja.loteSeleccionManual,
            "$pathGlobal/ultimaActualizacion" to System.currentTimeMillis(),
            "$pathGlobal/ultimaActualizacionServidor" to ServerValue.TIMESTAMP,
            "$pathIndividual/descontado" to true,
            "$pathIndividual/revertido" to false,
            "$pathIndividual/stockAntes" to stockAntes,
            "$pathIndividual/stockDespues" to stockDespues,
            "$pathIndividual/loteNumeroConsumido" to loteConsumido,
            "$pathIndividual/loteClaveConsumida" to loteConsumidoClave,
            "$pathIndividual/loteVencimientoConsumido" to loteConsumidoVencimiento,
            "$pathIndividual/loteNumeroFefo" to loteFefo,
            "$pathIndividual/loteVencimientoFefo" to loteFefoVencimiento,
            "$pathIndividual/loteSeleccionManual" to productoCaja.loteSeleccionManual,
            "$pathProductoGlobal/loteNumero" to loteConsumido,
            "$pathProductoGlobal/loteClaveConsumida" to loteConsumidoClave,
            "$pathProductoGlobal/loteVencimiento" to loteConsumidoVencimiento,
            "$pathProductoGlobal/loteNumeroFefo" to loteFefo,
            "$pathProductoGlobal/loteVencimientoFefo" to loteFefoVencimiento,
            "$pathProductoGlobal/loteSeleccionManual" to productoCaja.loteSeleccionManual,
            "$pathProductoIndividual/loteNumero" to loteConsumido,
            "$pathProductoIndividual/loteClaveConsumida" to loteConsumidoClave,
            "$pathProductoIndividual/loteVencimiento" to loteConsumidoVencimiento,
            "$pathProductoIndividual/loteNumeroFefo" to loteFefo,
            "$pathProductoIndividual/loteVencimientoFefo" to loteFefoVencimiento,
            "$pathProductoIndividual/loteSeleccionManual" to productoCaja.loteSeleccionManual,
            "$pathIndividual/ultimaActualizacion" to System.currentTimeMillis(),
            "$pathIndividual/ultimaActualizacionServidor" to ServerValue.TIMESTAMP
        )

        productoCaja.lotesConsumidosDetalle.values.forEach { detalle ->
            if (detalle.clave.isBlank()) return@forEach
            val pathDetalleGlobal = "$pathGlobal/lotesConsumidos/${detalle.clave}"
            val pathDetalleIndividual = "$pathIndividual/lotesConsumidos/${detalle.clave}"
            updates["$pathDetalleGlobal/clave"] = detalle.clave
            updates["$pathDetalleGlobal/numero"] = detalle.numero
            updates["$pathDetalleGlobal/vencimiento"] = detalle.vencimiento
            updates["$pathDetalleGlobal/cantidad"] = detalle.cantidad
            updates["$pathDetalleIndividual/clave"] = detalle.clave
            updates["$pathDetalleIndividual/numero"] = detalle.numero
            updates["$pathDetalleIndividual/vencimiento"] = detalle.vencimiento
            updates["$pathDetalleIndividual/cantidad"] = detalle.cantidad
        }

        database.reference.updateChildren(updates)
            .addOnSuccessListener {
                onComplete()
            }
            .addOnFailureListener { e ->
                // El stock ya fue descontado del inventario; si falla el registro de control
                // igualmente continuamos para no bloquear la venta, pero lo registramos.
                Log.e("StockControl", "Error al registrar control de stock: ${e.message}")
                onComplete()
            }
    }

    private fun ventaPuedeConsumirLotes(resolucion: LoteConsumoResolucion): Boolean {
        return resolucion.loteActual != null &&
            resolucion.tramosConsumo.isNotEmpty() &&
            resolucion.loteActualTieneStockSuficiente
    }

    private fun construirMensajeVentaSinLoteValido(
        nombreProducto: String?,
        resolucion: LoteConsumoResolucion,
        unidadesSolicitadas: Int,
        stockRegistrado: Double,
        lotes: Map<String, LoteProducto>
    ): String {
        return ProductUtils.construirMensajeVentaNoDisponible(
            nombreProducto = nombreProducto,
            cantidadSolicitada = unidadesSolicitadas,
            stockRegistrado = stockRegistrado,
            lotes = lotes,
            stockVendibleDisponible = resolucion.stockFefoDisponible
        )
    }

    private fun procesarVentaProductos(
        productosCaja: List<ProductoCaja>,
        index: Int,
        stocksOriginales: MutableMap<String, String>,
        idVenta: String,
        fechaVenta: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (index >= productosCaja.size) {
            onSuccess()
            return
        }

        val producto = productosCaja[index]

        descontarStockDeProducto(
            productoCaja = producto,
            stocksOriginales = stocksOriginales,
            onSuccess = {
                marcarStockControlDescontado(
                    fechaVenta = fechaVenta,
                    idVenta = idVenta,
                    productoCaja = producto,
                    indexProducto = index,
                    stocksOriginales = stocksOriginales
                ) {
                    procesarVentaProductos(
                        productosCaja = productosCaja,
                        index = index + 1,
                        stocksOriginales = stocksOriginales,
                        idVenta = idVenta,
                        fechaVenta = fechaVenta,
                        onSuccess = onSuccess,
                        onError = onError
                    )
                }
            },
            onError = { mensaje ->
                restaurarStocks(
                    productosCaja = productosCaja,
                    lastIndex = index - 1,
                    stocksOriginales = stocksOriginales,
                    fechaVenta = fechaVenta,
                    idVenta = idVenta
                ) {
                    onError(mensaje)
                }
            }
        )
    }

    private fun descontarStockDeProducto(productoCaja: ProductoCaja, stocksOriginales: MutableMap<String, String>, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val error = validarProductoCajaBasico(productoCaja)
        if (error != null) {
            onError(error)
            return
        }

        val claveProducto = productoCaja.indiceProducto.trim()
        val cantidadPresentaciones = productoCaja.cantidad.toIntSeguro(0)
        val unidadesPorPresentacion = productoCaja.unidadesPorPresentacion.toIntSeguro(1)
        val unidadesVendidas = cantidadPresentaciones * unidadesPorPresentacion

        val productoRef = database.getReference("Inventario")
            .child("Productos")
            .child(claveProducto)

        productoRef.get()
            .addOnSuccessListener { productoSnapshot ->
                if (!productoSnapshot.exists()) {
                    onError("Producto no encontrado en inventario: ${productoCaja.nombre}")
                    return@addOnSuccessListener
                }

                val stockOriginal = productoSnapshot.child("cantidadinicial").value?.toString()
                val stockOriginalInt = stockOriginal.toIntSeguro(-1)

                if (stockOriginal.isNullOrBlank() || stockOriginalInt < 0) {
                    onError("El producto ${productoCaja.nombre} no tiene stock válido en inventario")
                    return@addOnSuccessListener
                }

                if (!stocksOriginales.containsKey(claveProducto)) {
                    stocksOriginales[claveProducto] = stockOriginal
                }

                var motivoAbortar: String? = null
                var loteConsumidoNumero = productoCaja.loteNumero.trim()
                var loteConsumidoClave = productoCaja.loteClaveConsumida.trim()
                var loteConsumidoVencimiento = productoCaja.loteVencimiento.trim()
                productoCaja.lotesConsumidosDetalle = emptyMap()

                productoRef.runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        fun resolverEntero(raw: Any?): Int? {
                            return when (raw) {
                                is Long -> raw.toInt()
                                is Int -> raw
                                is Double -> raw.toInt()
                                is String -> raw.trim().toIntOrNull()
                                null -> null
                                else -> null
                            }
                        }

                        val stockNode = currentData.child("cantidadinicial")
                        val stockActual = resolverEntero(stockNode.value ?: stockOriginal)
                            ?: return Transaction.abort()

                        if (stockActual < 0) {
                            motivoAbortar = "El producto ${productoCaja.nombre} no tiene stock valido en inventario"
                            return Transaction.abort()
                        }

                        if (unidadesVendidas > stockActual) {
                            motivoAbortar = "Stock insuficiente para ${productoCaja.nombre}. Solo quedan $stockActual unidades disponibles"
                            return Transaction.abort()
                        }

                        val lotesNode = currentData.child("lotes")
                        val lotes = lotesNode.children.mapNotNull { loteChild ->
                            val lote = loteChild.getValue(LoteProducto::class.java)
                            val clave = loteChild.key.orEmpty()
                            if (lote != null && clave.isNotBlank()) clave to lote else null
                        }.toMap()
                        if (lotes.isEmpty()) {
                            motivoAbortar = ProductUtils.construirMensajeVentaNoDisponible(
                                nombreProducto = productoCaja.nombre,
                                cantidadSolicitada = unidadesVendidas,
                                stockRegistrado = stockActual.toDouble(),
                                lotes = emptyMap()
                            )
                            return Transaction.abort()
                        }

                        if (lotes.isEmpty()) {
                            motivoAbortar =
                                "El producto ${productoCaja.nombre} no tiene lotes válidos configurados para vender."
                            return Transaction.abort()
                        }

                        val loteSeleccionadoActual = currentData
                            .child("loteConsumoSeleccionado")
                            .value
                            ?.toString()
                            .orEmpty()
                            .trim()
                        val seleccionManualActual = currentData
                            .child("loteConsumoSeleccionManual")
                            .value
                            .toBooleanSeguro(false) &&
                            loteSeleccionadoActual.isNotBlank()
                        val resolucion = LoteConsumoRules.resolver(
                            lotes = lotes,
                            loteSeleccionado = loteSeleccionadoActual,
                            seleccionManual = seleccionManualActual,
                            unidadesRequeridas = unidadesVendidas
                        )

                        if (!ventaPuedeConsumirLotes(resolucion)) {
                            motivoAbortar = construirMensajeVentaSinLoteValido(
                                nombreProducto = productoCaja.nombre,
                                resolucion = resolucion,
                                unidadesSolicitadas = unidadesVendidas,
                                stockRegistrado = stockActual.toDouble(),
                                lotes = lotes
                            )
                            return Transaction.abort()
                        }

                        val tramosConsumo = resolucion.tramosConsumo.map { tramo ->
                            LoteConsumoDetalleCaja(
                                clave = tramo.clave,
                                numero = tramo.lote.numero.ifBlank { tramo.clave },
                                vencimiento = tramo.lote.vencimiento,
                                cantidad = tramo.cantidad
                            )
                        }

                        tramosConsumo.forEach { tramo ->
                            val stockLoteActual = lotes[tramo.clave]?.cantidad ?: 0.0
                            val loteNode = lotesNode.child(tramo.clave).child("cantidad")
                            loteNode.value = (stockLoteActual - tramo.cantidad)
                        }

                        val claveLoteManual = resolucion.loteSeleccionManual?.first.orEmpty()
                        val cantidadConsumidaLoteManual = if (claveLoteManual.isNotBlank()) {
                            tramosConsumo.firstOrNull { it.clave.equals(claveLoteManual, ignoreCase = true) }
                                ?.cantidad ?: 0
                        } else {
                            0
                        }
                        val stockRestanteLoteManual = if (claveLoteManual.isNotBlank()) {
                            ((lotes[claveLoteManual]?.cantidad ?: 0.0) - cantidadConsumidaLoteManual)
                                .coerceAtLeast(0.0)
                        } else {
                            0.0
                        }
                        val debeLiberarSeleccionManual = seleccionManualActual && (
                            resolucion.seleccionManualInvalida ||
                                claveLoteManual.isBlank() ||
                                stockRestanteLoteManual <= 0.0
                            )
                        if (debeLiberarSeleccionManual) {
                            currentData.child("loteConsumoSeleccionado").value = ""
                            currentData.child("loteConsumoSeleccionManual").value = false
                        }

                        val primerTramo = tramosConsumo.firstOrNull()
                        loteConsumidoNumero = primerTramo?.numero.orEmpty()
                        loteConsumidoClave = primerTramo?.clave.orEmpty()
                        loteConsumidoVencimiento = primerTramo?.vencimiento.orEmpty()
                        productoCaja.lotesConsumidosDetalle = tramosConsumo.associateBy { it.clave }
                        productoCaja.loteNumeroFefo = resolucion.loteRecomendadoFefo?.second?.numero
                            ?.ifBlank { resolucion.loteRecomendadoFefo.first }
                            .orEmpty()
                        productoCaja.loteVencimientoFefo = resolucion.loteRecomendadoFefo?.second?.vencimiento.orEmpty()
                        productoCaja.loteSeleccionManual = resolucion.usaSeleccionManual

                        stockNode.value = (stockActual - unidadesVendidas).toString()
                        return Transaction.success(currentData)
                    }

                    override fun onComplete(
                        error: DatabaseError?,
                        committed: Boolean,
                        currentData: DataSnapshot?
                    ) {
                        if (error != null) {
                            onError("Error al validar stock de ${productoCaja.nombre}: ${error.message}")
                            return
                        }

                        if (!committed) {
                            onError(motivoAbortar ?: "No se pudo descontar el stock de ${productoCaja.nombre}")
                            return
                        }

                        if (loteConsumidoNumero.isNotBlank()) {
                            productoCaja.loteNumero = loteConsumidoNumero
                            productoCaja.loteClaveConsumida = loteConsumidoClave
                            productoCaja.loteVencimiento = loteConsumidoVencimiento
                            registrarMovimientoVentaEnLote(
                                productoCaja = productoCaja,
                                cantidadUnidades = unidadesVendidas,
                                idVenta = ""
                            )
                        }

                        onSuccess()
                    }
                })
            }
            .addOnFailureListener { e ->
                onError("Error leyendo inventario: ${e.message}")
            }
    }

    // Nota: Antes llevábamos un acumulado duplicado en el nodo "ReportesVentas/{fecha}/{metodo}".
    // Ese nodo no era consumido por ninguna pantalla y duplicaba datos que ya existen en:
    // - el turno (resumenMetodos / totalVentas / ventasEfectivo)
    // - ventas individuales + movimientos (auditoría)
    // Por eso se eliminó para mantener la base más simple y evitar inconsistencias.

    private fun guardarVentaFinal(
        operationId: String,
        idVenta: String,
        fechaActual: String,
        horaActual: String,
        timestampVentaMs: Long,
        productosCaja: List<ProductoCaja>,
        totalPagar: Double,
        metodoPago: String,
        montoRecibido: Double,
        detalleMetodosTurno: Map<String, Double>,
        dialog: BottomSheetDialog?,
        stocksOriginales: MutableMap<String, String>,
        onFinish: () -> Unit
    ) {
        val rootRef = database.reference
        val cajaRef = getCajaRef().child("Productos")

        val vuelto = if (metodoPago.lowercase(Locale.getDefault()).contains("efectivo")) {
            (montoRecibido - totalPagar).coerceAtLeast(0.0)
        } else {
            0.0
        }

        val tipoComprobanteFinal = if (tipoComprobanteSeleccionado == "FACTURA") {
            "Factura"
        } else {
            if (nombreClienteVenta.isBlank() && documentoClienteVenta.isBlank()) {
                "Boleta simple"
            } else {
                "Boleta"
            }
        }

        val esSupervision = estaEnModoSupervision()
        val idCajaOperativa = obtenerIdCajaOperativa()
        val nombreCajaOperativa = obtenerNombreCajaOperativa()
        val detallePagoFinal = CajaTurnoHelper.obtenerDetallePagoParaTurno(
            metodoPago = metodoPago,
            totalPagar = totalPagar,
            detalleMetodos = detalleMetodosTurno
        )

        val infoVenta = hashMapOf<String, Any>(
            "id" to idVenta,
            "operationId" to operationId,
            "timestamp" to timestampVentaMs,
            "timestampServidor" to ServerValue.TIMESTAMP,

            // #5: Nuevo estándar contable y auditoría (Fase 3)
            "timestampVentaServidor" to ServerValue.TIMESTAMP,
            "ultimaActualizacionServidor" to ServerValue.TIMESTAMP,
            // OJO: 'creadoServidor' NO se sobrescribe aquí, ya existe desde crearPreRegistroVenta

            // #5: Campos UI (separados de contabilidad)
            "fechaVentaLocal" to fechaActual,
            "horaVentaLocal" to horaActual,

            "fecha" to fechaActual,
            "hora" to horaActual,
            "estado" to "finalizada",
            "fase" to "completada",

            "total" to String.format(Locale.US, "%.2f", totalPagar),
            "montoRecibido" to String.format(Locale.US, "%.2f", montoRecibido),
            "vuelto" to String.format(Locale.US, "%.2f", vuelto),
            "metodoPago" to metodoPago,
            "tipoComprobante" to tipoComprobanteFinal,

            "idCajaOperativa" to idCajaOperativa,
            "nombreCajaOperativa" to nombreCajaOperativa,
            "idTurno" to idTurnoActivo,

            "idUsuarioQueCobro" to idCajera,
            "nombreUsuarioQueCobro" to nombreCajera,

            "esSupervision" to esSupervision,
            "idSupervisor" to if (esSupervision) idCajera else "",
            "nombreSupervisor" to if (esSupervision) nombreCajera else ""
        )

        val pathGlobal = "Ventas/$fechaActual/$idVenta"
        val pathIndividual = "VentasPorCajera/$idCajaOperativa/$fechaActual/$idVenta"
        val pathAcumuladoCajera = "CorteCaja/Cajeras/$idCajaOperativa/$fechaActual"

        val updates = hashMapOf<String, Any>(
            "$pathGlobal/infoVenta" to infoVenta,
            "$pathGlobal/comprobante/tipo" to tipoComprobanteFinal,
            "$pathGlobal/comprobante/nombreCliente" to nombreClienteVenta,
            "$pathGlobal/comprobante/documentoCliente" to documentoClienteVenta,
            "$pathGlobal/comprobante/razonSocial" to razonSocialClienteVenta,
            "$pathGlobal/comprobante/correoCliente" to correoClienteVenta,
            "$pathGlobal/preRegistro/estado" to "finalizada",
            "$pathGlobal/preRegistro/fase" to "completada",
            "$pathGlobal/preRegistro/ultimaActualizacion" to timestampVentaMs,
            "$pathGlobal/preRegistro/ultimaActualizacionServidor" to ServerValue.TIMESTAMP,

            "$pathIndividual/infoVenta" to infoVenta,
            "$pathIndividual/total" to String.format(Locale.US, "%.2f", totalPagar),
            "$pathIndividual/comprobante/tipo" to tipoComprobanteFinal,
            "$pathIndividual/comprobante/nombreCliente" to nombreClienteVenta,
            "$pathIndividual/comprobante/documentoCliente" to documentoClienteVenta,
            "$pathIndividual/comprobante/razonSocial" to razonSocialClienteVenta,
            "$pathIndividual/comprobante/correoCliente" to correoClienteVenta,
            "$pathIndividual/preRegistro/estado" to "finalizada",
            "$pathIndividual/preRegistro/fase" to "completada",
            "$pathIndividual/preRegistro/ultimaActualizacion" to timestampVentaMs,
            "$pathIndividual/preRegistro/ultimaActualizacionServidor" to ServerValue.TIMESTAMP,

            "$pathAcumuladoCajera/nombre" to obtenerNombreCajaOperativa(),
            "$pathAcumuladoCajera/ultimaVenta" to horaActual
        )

        for (producto in productosCaja) {
            updates["$pathGlobal/productos/${producto.id}"] = producto
            updates["$pathIndividual/productos/${producto.id}"] = producto
        }

        detallePagoFinal.forEach { (titulo, monto) ->
            val claveDetalle = CajaTurnoHelper.normalizarClaveResumen(titulo)
            updates["$pathGlobal/detallePago/$claveDetalle/titulo"] = titulo
            updates["$pathGlobal/detallePago/$claveDetalle/monto"] =
                String.format(Locale.US, "%.2f", monto)
            updates["$pathIndividual/detallePago/$claveDetalle/titulo"] = titulo
            updates["$pathIndividual/detallePago/$claveDetalle/monto"] =
                String.format(Locale.US, "%.2f", monto)
        }

        rootRef.updateChildren(updates)
            .addOnSuccessListener {
                sincronizarCamposVisiblesDesdeTimestampServidor(
                    ref = database.getReference(pathGlobal).child("infoVenta"),
                    campoTimestampServidor = "timestampVentaServidor",
                    rutasFecha = listOf("fechaVentaLocal", "fecha"),
                    rutasHora = listOf("horaVentaLocal", "hora")
                )
                sincronizarCamposVisiblesDesdeTimestampServidor(
                    ref = database.getReference(pathIndividual).child("infoVenta"),
                    campoTimestampServidor = "timestampVentaServidor",
                    rutasFecha = listOf("fechaVentaLocal", "fecha"),
                    rutasHora = listOf("horaVentaLocal", "hora")
                )

                if (tipoComprobanteFinal != "Boleta simple") {
                    guardarUltimosDatosClienteComprobante()
                    guardarClienteComprobantePorDocumento()
                }

                // La venta ya quedó persistida en Firebase en este punto.
                // Los movimientos deben registrarse aquí para no perder trazabilidad
                // si luego falla el reporte por tipo de pago o el acumulado del turno.
                registrarMovimientosInventarioPorVenta(
                    productosCaja = productosCaja,
                    stocksOriginales = stocksOriginales,
                    idVenta = idVenta
                )

                MovimientoLogger.registrar(
                    tipo = "venta_realizada",
                    modulo = "caja",
                    titulo = "Venta registrada",
                    descripcion = "Venta #$idVenta por ${MonedaHelper.formatear(totalPagar)}",
                    idUsuario = idCajera,
                    nombreUsuario = nombreCajera,
                    monto = totalPagar,
                    referenciaId = idVenta,
                    idCaja = idCajaOperativa,
                    nombreCaja = nombreCajaOperativa,
                    extra = mapOf(
                        "idTurno" to idTurnoActivo,
                        "metodoPago" to metodoPago,
                        "tipoComprobante" to tipoComprobanteFinal,
                        "cantidadProductos" to productosCaja.size,
                        "esSupervision" to esSupervision,
                        "operationId" to operationId
                    ),
                    database = database
                )

                actualizarEstadoOperacionVenta(
                    operationId = operationId,
                    fechaVenta = fechaActual,
                    estado = "finalizada",
                    detalle = "Venta finalizada correctamente",
                    idVenta = idVenta
                )

                actualizarAcumuladoCajera(
                    idCajera = idCajaOperativa,
                    fecha = fechaActual,
                    monto = totalPagar,
                    montoRecibido = montoRecibido,
                    metodoPago = metodoPago,
                    detalleMetodosTurno = detallePagoFinal,
                    onSuccess = {
                        limpiarCajaActual(dialog, cajaRef) {
                            liberarCajaSiEstabaBloqueada {
                                onFinish()
                            }
                        }
                    },
                    onError = { mensaje ->
                        limpiarCajaActual(dialog, cajaRef) {
                            liberarCajaSiEstabaBloqueada {
                                onFinish()
                                mostrarToastSeguro("Venta guardada, pero falló el corte: $mensaje")
                            }
                        }
                    }
                )
            }
            .addOnFailureListener { e ->
                Log.e("VentaCaja", "Error al guardar venta final #$idVenta", e)
                marcarPreRegistroVentaFallida(
                    idVenta = idVenta,
                    fechaVenta = fechaActual,
                    motivo = e.message ?: "Error al guardar la venta final"
                )
                actualizarEstadoOperacionVenta(
                    operationId = operationId,
                    fechaVenta = fechaActual,
                    estado = "fallida",
                    detalle = e.message ?: "Error al guardar la venta final",
                    idVenta = idVenta
                )
                restaurarStocks(
                    productosCaja = productosCaja,
                    lastIndex = productosCaja.size - 1,
                    stocksOriginales = stocksOriginales,
                    fechaVenta = fechaActual,
                    idVenta = idVenta
                ) {
                    onFinish()
                    mostrarToastSeguro("Error al guardar la venta: ${e.message}")
                }
            }
    }




    private fun actualizarAcumuladoCajera(
        idCajera: String,
        fecha: String,
        monto: Double,
        montoRecibido: Double,
        metodoPago: String,
        detalleMetodosTurno: Map<String, Double>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (idTurnoActivo.isBlank()) {
            onError("No hay turno activo para registrar la venta")
            return
        }

        val ref = database.reference.child("CorteCaja/Cajeras/$idCajera/$fecha/turnos/$idTurnoActivo")

        ref.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentData = CajaTurnoHelper.mutableMapFromData(mutableData)

                val totalPrevio = mutableData.child("totalVentas").value?.toString()?.toDoubleOrNull() ?: 0.0
                currentData["totalVentas"] = String.format(Locale.US, "%.2f", totalPrevio + monto)

                val ventasPrevias = mutableData.child("numeroVentas").value?.toString()?.toLongOrNull() ?: 0L
                currentData["numeroVentas"] = ventasPrevias + 1
                currentData["ultimoMetodoPago"] = metodoPago
                currentData["ultimaVenta"] = obtenerHoraActual()
                currentData["ultimaActualizacion"] = System.currentTimeMillis()

                val resumenActual = CajaTurnoHelper.mutableMapFromValue(currentData["resumenMetodos"])

                detalleMetodosTurno.forEach { (titulo, montoMetodo) ->
                    val key = CajaTurnoHelper.normalizarClaveResumen(titulo)
                    val nodeMetodo = mutableData.child("resumenMetodos").child(key)
                    val montoPrevio = nodeMetodo.child("monto").value?.toString()?.toDoubleOrNull() ?: 0.0
                    
                    val dataMetodo = CajaTurnoHelper.mutableMapFromValue(resumenActual[key])
                    dataMetodo["titulo"] = titulo
                    dataMetodo["monto"] = String.format(Locale.US, "%.2f", montoPrevio + montoMetodo)
                    resumenActual[key] = dataMetodo
                }
                currentData["resumenMetodos"] = resumenActual

                val efectivoPrevio = mutableData.child("ventasEfectivo").value.toString().toDoubleOrNull() ?: 0.0
                val efectivoVenta = detalleMetodosTurno
                    .filterKeys { it.contains("efectivo", ignoreCase = true) }
                    .values
                    .sum()
                val nuevoEfectivo = efectivoPrevio + efectivoVenta
                currentData["ventasEfectivo"] = String.format(Locale.US, "%.2f", nuevoEfectivo)

                val apertura = mutableData.child("montoApertura").value.toString().toDoubleOrNull() ?: 0.0
                val totalEgresos = mutableData.child("totalEgresos").value.toString().toDoubleOrNull() ?: 0.0
                currentData["efectivoEsperado"] = String.format(Locale.US, "%.2f", apertura + nuevoEfectivo - totalEgresos)
                currentData["ultimoMontoRecibido"] = String.format(Locale.US, "%.2f", montoRecibido)

                mutableData.value = currentData
                return Transaction.success(mutableData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (error != null) {
                    Log.e("CorteCaja", "Error actualizando acumulado: ${error.message}")
                    onError(error.message)
                    return
                }

                if (!committed) {
                    onError("No se pudo actualizar el turno actual")
                    return
                }

                onSuccess()
            }
        })
    }

    private fun limpiarCajaActual(dialog: BottomSheetDialog?, cajaRef: DatabaseReference, onFinish: () -> Unit) {
        cajaRef.removeValue()
            .addOnSuccessListener {
                if (dialog?.isShowing == true) {
                    dialog.dismiss()
                }
                onFinish()
                mostrarAnimacionVentaExitosa()
            }
            .addOnFailureListener { e ->
                if (dialog?.isShowing == true) {
                    dialog.dismiss()
                }
                onFinish()
                mostrarToastSeguro("Venta guardada, pero no se pudo limpiar la caja: ${e.message}")
            }
    }

    private fun restaurarStocks(
        productosCaja: List<ProductoCaja>,
        lastIndex: Int,
        stocksOriginales: MutableMap<String, String>,
        fechaVenta: String? = null,
        idVenta: String? = null,
        onFinish: (() -> Unit)? = null
    ) {
        if (lastIndex < 0 || productosCaja.isEmpty()) {
            onFinish?.invoke()
            return
        }

        val limite = minOf(lastIndex, productosCaja.lastIndex)
        if (limite < 0) {
            onFinish?.invoke()
            return
        }

        val usarControlPersistido = !fechaVenta.isNullOrBlank() && !idVenta.isNullOrBlank()
        val productosObjetivo = mutableListOf<Pair<Int, ProductoCaja>>()

        for (i in 0..limite) {
            val producto = productosCaja[i]
            val claveProducto = producto.indiceProducto.trim()
            if (claveProducto.isBlank()) continue

            if (usarControlPersistido || stocksOriginales.containsKey(claveProducto)) {
                productosObjetivo += i to producto
            }
        }

        if (productosObjetivo.isEmpty()) {
            onFinish?.invoke()
            return
        }

        restaurarStockControlRecursivo(
            productosObjetivo = productosObjetivo,
            index = 0,
            stocksOriginales = stocksOriginales,
            fechaVenta = fechaVenta,
            idVenta = idVenta
        ) {
            if (!fechaVenta.isNullOrBlank() && !idVenta.isNullOrBlank()) {
                marcarPreRegistroReversionCompletada(
                    fechaVenta = fechaVenta,
                    idVenta = idVenta,
                    onComplete = { onFinish?.invoke() }
                )
            } else {
                onFinish?.invoke()
            }
        }
    }

    private fun restaurarStockControlRecursivo(
        productosObjetivo: List<Pair<Int, ProductoCaja>>,
        index: Int,
        stocksOriginales: Map<String, String>,
        fechaVenta: String?,
        idVenta: String?,
        onComplete: () -> Unit
    ) {
        if (index >= productosObjetivo.size) {
            onComplete()
            return
        }

        val (indexProducto, producto) = productosObjetivo[index]
        intentarRestaurarStockProducto(
            productoCaja = producto,
            indexProducto = indexProducto,
            stocksOriginales = stocksOriginales,
            fechaVenta = fechaVenta,
            idVenta = idVenta
        ) {
            restaurarStockControlRecursivo(
                productosObjetivo = productosObjetivo,
                index = index + 1,
                stocksOriginales = stocksOriginales,
                fechaVenta = fechaVenta,
                idVenta = idVenta,
                onComplete = onComplete
            )
        }
    }

    private fun intentarRestaurarStockProducto(
        productoCaja: ProductoCaja,
        indexProducto: Int,
        stocksOriginales: Map<String, String>,
        fechaVenta: String?,
        idVenta: String?,
        onComplete: () -> Unit
    ) {
        val claveProducto = productoCaja.indiceProducto.trim()
        if (claveProducto.isBlank()) {
            onComplete()
            return
        }

        val cantidadPresentaciones = productoCaja.cantidad.toIntSeguro(0)
        val unidadesPorPresentacion = productoCaja.unidadesPorPresentacion.toIntSeguro(1).coerceAtLeast(1)
        val unidadesAReponer = (cantidadPresentaciones * unidadesPorPresentacion).coerceAtLeast(0)
        val tieneEvidenciaLocalDescuento = stocksOriginales.containsKey(claveProducto)
        if (unidadesAReponer <= 0) {
            onComplete()
            return
        }

        var loteRepuestoExacto = productoCaja.loteNumero.trim().isBlank()
        var detalleReversionLote = ""

        val ejecutarReversion = {
            val productoRef = database.reference.child("Inventario").child("Productos").child(claveProducto)
            productoRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val stockNode = currentData.child("cantidadinicial")
                    val stockActual = when (val raw = stockNode.value) {
                        is Long -> raw.toInt()
                        is Int -> raw
                        is Double -> raw.toInt()
                        is String -> raw.trim().toIntOrNull() ?: 0
                        else -> 0
                    }

                    stockNode.value = (stockActual + unidadesAReponer).toString()

                    val detallesLotesConsumidos = productoCaja.lotesConsumidosDetalle.values
                        .filter { it.clave.isNotBlank() && it.cantidad > 0 }
                    val loteNumero = productoCaja.loteNumero.trim()
                    val loteClaveConsumida = productoCaja.loteClaveConsumida.trim()
                    if (detallesLotesConsumidos.isNotEmpty()) {
                        val lotesNode = currentData.child("lotes")
                        val lotesNoEncontrados = mutableListOf<String>()

                        detallesLotesConsumidos.forEach { detalle ->
                            val loteAReponer = lotesNode.children.firstOrNull { loteChild ->
                                loteChild.key?.equals(detalle.clave, ignoreCase = true) == true
                            }
                            if (loteAReponer != null) {
                                loteAReponer.child("cantidad").let { cantidadNode ->
                                    val cantidadActual = when (val raw = cantidadNode.value) {
                                        is Long -> raw.toDouble()
                                        is Int -> raw.toDouble()
                                        is Double -> raw
                                        is String -> raw.trim().toDoubleOrNull() ?: 0.0
                                        else -> 0.0
                                    }
                                    cantidadNode.value = cantidadActual + detalle.cantidad
                                }
                            } else {
                                lotesNoEncontrados += detalle.numero.ifBlank { detalle.clave }
                            }
                        }

                        loteRepuestoExacto = lotesNoEncontrados.isEmpty()
                        if (lotesNoEncontrados.isNotEmpty()) {
                            detalleReversionLote =
                                "No se encontraron los lotes consumidos ${lotesNoEncontrados.joinToString(", ")} para reponerlos de forma exacta. Solo se devolvio stock global."
                        }
                    } else if (loteNumero.isNotBlank() || loteClaveConsumida.isNotBlank()) {
                        val lotesNode = currentData.child("lotes")
                        val loteAReponer = if (loteClaveConsumida.isNotBlank()) {
                            lotesNode.children.firstOrNull { loteChild ->
                                loteChild.key?.equals(loteClaveConsumida, ignoreCase = true) == true
                            }
                        } else {
                            null
                        } ?: lotesNode.children.firstOrNull { loteChild ->
                            val lote = loteChild.getValue(LoteProducto::class.java)
                            loteChild.key?.equals(loteNumero, ignoreCase = true) == true ||
                                lote?.numero?.trim()?.equals(loteNumero, ignoreCase = true) == true
                        }
                        if (loteAReponer != null) {
                            loteAReponer.child("cantidad").let { cantidadNode ->
                                val cantidadActual = when (val raw = cantidadNode.value) {
                                    is Long -> raw.toDouble()
                                    is Int -> raw.toDouble()
                                    is Double -> raw
                                    is String -> raw.trim().toDoubleOrNull() ?: 0.0
                                    else -> 0.0
                                }
                                cantidadNode.value = cantidadActual + unidadesAReponer
                            }
                            loteRepuestoExacto = true
                        } else {
                            loteRepuestoExacto = false
                            detalleReversionLote =
                                "No se encontro el lote consumido ${productoCaja.loteNumero.ifBlank { productoCaja.loteClaveConsumida }} para reponerlo de forma exacta. Solo se devolvio stock global."
                        }
                    }

                    return Transaction.success(currentData)
                }

                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    if (error != null || !committed) {
                        onComplete()
                        return
                    }

                    // HUMANO: Registramos en el historial del producto por qué "apareció" stock de nuevo
                    val stockFinal = currentData?.child("cantidadinicial")?.value?.toString()?.toIntOrNull() ?: 0
                    if (loteRepuestoExacto && productoCaja.loteNumero.isNotBlank()) {
                        registrarMovimientoReversionEnLote(
                            productoCaja = productoCaja,
                            cantidadUnidades = unidadesAReponer,
                            idVenta = idVenta ?: "desconocida"
                        )
                    }

                    registrarMovimientosInventarioPorReversion(
                        producto = productoCaja,
                        cantidadRepuesta = unidadesAReponer,
                        stockAntes = stockFinal - unidadesAReponer,
                        idVenta = idVenta ?: "desconocida",
                        loteRepuestoExacto = loteRepuestoExacto,
                        detalleReversionLote = detalleReversionLote
                    )

                    if (!fechaVenta.isNullOrBlank() && !idVenta.isNullOrBlank()) {
                        marcarStockControlRevertido(
                            fechaVenta = fechaVenta,
                            idVenta = idVenta,
                            productoCaja = productoCaja,
                            indexProducto = indexProducto,
                            onComplete = onComplete
                        )
                    } else {
                        onComplete()
                    }
                }
            })
        }

        if (fechaVenta.isNullOrBlank() || idVenta.isNullOrBlank()) {
            ejecutarReversion()
            return
        }

        val claveControl = obtenerClaveStockControlProducto(productoCaja, indexProducto)
        database.reference
            .child("Ventas")
            .child(fechaVenta)
            .child(idVenta)
            .child("preRegistro")
            .child("stockControl")
            .child(claveControl)
            .get()
            .addOnSuccessListener { controlSnapshot ->
                val descontado = controlSnapshot.child("descontado").value.toBooleanSeguro(false)
                val revertido = controlSnapshot.child("revertido").value.toBooleanSeguro(false)
                val stockAntesControl = controlSnapshot.child("stockAntes").value.toLongSeguro(-1L)
                val stockDespuesControl = controlSnapshot.child("stockDespues").value.toLongSeguro(-1L)
                val hayEvidenciaPersistidaDescuento = stockAntesControl >= 0L || stockDespuesControl >= 0L
                val loteConsumidoPersistido = controlSnapshot.child("loteNumeroConsumido").value?.toString().orEmpty().trim()
                val loteClaveConsumidaPersistida = controlSnapshot.child("loteClaveConsumida").value?.toString().orEmpty().trim()
                val loteVencimientoPersistido = controlSnapshot.child("loteVencimientoConsumido").value?.toString().orEmpty().trim()
                val detallesLotesConsumidosPersistidos = controlSnapshot
                    .child("lotesConsumidos")
                    .children
                    .mapNotNull { detalleSnapshot ->
                        val detalle = detalleSnapshot.getValue(LoteConsumoDetalleCaja::class.java)
                        val clave = detalle?.clave?.trim().orEmpty().ifBlank { detalleSnapshot.key.orEmpty() }
                        if (detalle != null && clave.isNotBlank() && detalle.cantidad > 0) {
                            detalle.copy(clave = clave)
                        } else {
                            null
                        }
                    }
                val loteFefoPersistido = controlSnapshot.child("loteNumeroFefo").value?.toString().orEmpty().trim()
                val loteFefoVencimientoPersistido = controlSnapshot.child("loteVencimientoFefo").value?.toString().orEmpty().trim()
                val seleccionManualPersistida = controlSnapshot.child("loteSeleccionManual").value.toBooleanSeguro(productoCaja.loteSeleccionManual)

                if (loteConsumidoPersistido.isNotBlank()) {
                    productoCaja.loteNumero = loteConsumidoPersistido
                    productoCaja.loteVencimiento = loteVencimientoPersistido
                }
                if (loteClaveConsumidaPersistida.isNotBlank()) {
                    productoCaja.loteClaveConsumida = loteClaveConsumidaPersistida
                }
                if (detallesLotesConsumidosPersistidos.isNotEmpty()) {
                    productoCaja.lotesConsumidosDetalle =
                        detallesLotesConsumidosPersistidos.associateBy { it.clave }
                }
                if (loteFefoPersistido.isNotBlank()) {
                    productoCaja.loteNumeroFefo = loteFefoPersistido
                    productoCaja.loteVencimientoFefo = loteFefoVencimientoPersistido
                }
                productoCaja.loteSeleccionManual = seleccionManualPersistida

                if (revertido) {
                    onComplete()
                    return@addOnSuccessListener
                }

                val debeRevertir = descontado || hayEvidenciaPersistidaDescuento || tieneEvidenciaLocalDescuento
                if (!debeRevertir) {
                    onComplete()
                    return@addOnSuccessListener
                }
                ejecutarReversion()
            }
            .addOnFailureListener {
                onComplete()
            }
    }

    private fun marcarStockControlRevertido(
        fechaVenta: String,
        idVenta: String,
        productoCaja: ProductoCaja,
        indexProducto: Int,
        onComplete: () -> Unit
    ) {
        val idCajaOperativa = obtenerIdCajaOperativa()
        val claveControl = obtenerClaveStockControlProducto(productoCaja, indexProducto)
        val pathGlobal = "Ventas/$fechaVenta/$idVenta/preRegistro/stockControl/$claveControl"
        val pathIndividual =
            "VentasPorCajera/$idCajaOperativa/$fechaVenta/$idVenta/preRegistro/stockControl/$claveControl"

        val updates = hashMapOf<String, Any>(
            "$pathGlobal/revertido" to true,
            "$pathGlobal/descontado" to false,
            "$pathGlobal/reversionTs" to System.currentTimeMillis(),
            "$pathGlobal/reversionTsServidor" to ServerValue.TIMESTAMP,
            "$pathIndividual/revertido" to true,
            "$pathIndividual/descontado" to false,
            "$pathIndividual/reversionTs" to System.currentTimeMillis(),
            "$pathIndividual/reversionTsServidor" to ServerValue.TIMESTAMP
        )

        database.reference.updateChildren(updates)
            .addOnCompleteListener { onComplete() }
    }

    private fun marcarPreRegistroReversionCompletada(
        fechaVenta: String,
        idVenta: String,
        onComplete: (() -> Unit)? = null
    ) {
        val idCajaOperativa = obtenerIdCajaOperativa()
        val updates = hashMapOf<String, Any>(
            "Ventas/$fechaVenta/$idVenta/preRegistro/fase" to "reversion_stock_completada",
            "Ventas/$fechaVenta/$idVenta/preRegistro/reversionPendiente" to false,
            "Ventas/$fechaVenta/$idVenta/preRegistro/ultimaActualizacion" to System.currentTimeMillis(),
            "Ventas/$fechaVenta/$idVenta/preRegistro/ultimaActualizacionServidor" to ServerValue.TIMESTAMP,
            "VentasPorCajera/$idCajaOperativa/$fechaVenta/$idVenta/preRegistro/fase" to "reversion_stock_completada",
            "VentasPorCajera/$idCajaOperativa/$fechaVenta/$idVenta/preRegistro/reversionPendiente" to false,
            "VentasPorCajera/$idCajaOperativa/$fechaVenta/$idVenta/preRegistro/ultimaActualizacion" to System.currentTimeMillis(),
            "VentasPorCajera/$idCajaOperativa/$fechaVenta/$idVenta/preRegistro/ultimaActualizacionServidor" to ServerValue.TIMESTAMP
        )

        database.reference.updateChildren(updates)
            .addOnCompleteListener {
                MovimientoLogger.registrar(
                    tipo = "reversion_stock",
                    modulo = "caja",
                    titulo = "Devoluci\u00f3n autom\u00e1tica de productos",
                    descripcion = "Venta fallida ($idVenta). El sistema devolvi\u00f3 los productos al inventario.",
                    idUsuario = idCajera,
                    nombreUsuario = nombreCajera,
                    referenciaId = idVenta,
                    idCaja = idCajaOperativa,
                    nombreCaja = obtenerNombreCajaOperativa()
                )
                onComplete?.invoke()
            }
    }

    private fun registrarMovimientosInventarioPorReversion(
        producto: ProductoCaja,
        cantidadRepuesta: Int,
        stockAntes: Int,
        idVenta: String,
        loteRepuestoExacto: Boolean = true,
        detalleReversionLote: String = ""
    ) {
        val indiceProducto = producto.indiceProducto.trim()
        if (indiceProducto.isBlank()) return

        val stockDespues = stockAntes + cantidadRepuesta
        val detalleLotesTexto = construirDetalleLotesTexto(producto, cantidadRepuesta)

        val extra = mutableMapOf<String, Any>(
            "nombreProducto" to producto.nombre.orEmpty(),
            "presentacion" to producto.presentacion,
            "loteRepuestoExacto" to loteRepuestoExacto,
            "idTurno" to idTurnoActivo
        ).apply {
            putAll(construirExtraMovimientoLote(producto, cantidadRepuesta))
            if (detalleLotesTexto.isNotBlank()) {
                this["detalleLotesTexto"] = detalleLotesTexto
            }
            if (detalleReversionLote.isNotBlank()) {
                this["detalleReversionLote"] = detalleReversionLote
                this["requiereRevisionLote"] = true
            }
        }

        // HUMANO: Creamos un registro que explica la entrada de stock al inventario
        MovimientoInventarioLogger.registrar(
            indiceProducto = indiceProducto,
            tipo = "stock_entrada_reversion",
            titulo = "Restauración por venta fallida",
            descripcion = buildString {
                append("Se devolvieron unidades de ${producto.nombre} por fallo en venta #$idVenta")
                if (detalleLotesTexto.isNotBlank()) {
                    append(". Lotes repuestos: ")
                    append(detalleLotesTexto)
                }
            },
            idUsuario = idCajera,
            nombreUsuario = nombreCajera,
            cantidad = cantidadRepuesta,
            stockAntes = stockAntes,
            stockDespues = stockDespues,
            motivo = "Venta cancelada automáticamente",
            referenciaId = idVenta,
            extra = extra,
            database = database
        )
    }

    private fun registrarMovimientosInventarioPorDevolucion(
        producto: ProductoCaja,
        cantidadRepuesta: Int,
        cantidadBloqueada: Int = 0,
        stockAntes: Int,
        idVenta: String,
        idDevolucion: String,
        motivoDevolucion: String,
        loteRepuestoExacto: Boolean = true,
        detalleDevolucionLote: String = "",
        bloqueoVenta: Boolean = false
    ) {
        val indiceProducto = producto.indiceProducto.trim()
        if (indiceProducto.isBlank()) return

        val cantidadMovimiento = if (bloqueoVenta) {
            cantidadBloqueada.coerceAtLeast(0)
        } else {
            cantidadRepuesta.coerceAtLeast(0)
        }
        val stockDespues = if (bloqueoVenta) stockAntes else stockAntes + cantidadRepuesta
        val detalleLotesTexto = construirDetalleLotesTexto(producto, cantidadMovimiento)

        val extra = mutableMapOf<String, Any>(
            "nombreProducto" to producto.nombre.orEmpty(),
            "presentacion" to producto.presentacion,
            "loteRepuestoExacto" to loteRepuestoExacto,
            "idTurno" to idTurnoActivo,
            "idDevolucion" to idDevolucion,
            "bloqueadoParaVenta" to bloqueoVenta,
            "destinoStock" to destinoStockDevolucion(bloqueoVenta)
        ).apply {
            putAll(construirExtraMovimientoLote(producto, cantidadMovimiento))
            if (detalleLotesTexto.isNotBlank()) {
                this["detalleLotesTexto"] = detalleLotesTexto
            }
            if (motivoDevolucion.isNotBlank()) {
                this["motivoDevolucion"] = motivoDevolucion
            }
            if (bloqueoVenta) {
                this["motivoBloqueoAplicado"] = motivoBloqueoRegistrado(motivoDevolucion)
            }
            if (cantidadBloqueada > 0) {
                this["cantidadBloqueada"] = cantidadBloqueada
            }
            if (cantidadRepuesta > 0) {
                this["cantidadRepuestaVendible"] = cantidadRepuesta
            }
            if (detalleDevolucionLote.isNotBlank()) {
                this["detalleDevolucionLote"] = detalleDevolucionLote
                this["requiereRevisionLote"] = true
            }
        }

        MovimientoInventarioLogger.registrar(
            indiceProducto = indiceProducto,
            tipo = if (bloqueoVenta) "stock_bloqueado_devolucion" else "stock_entrada_devolucion",
            titulo = if (bloqueoVenta) "Bloqueo por devolucion" else "Entrada por devolucion",
            descripcion = buildString {
                if (bloqueoVenta) {
                    append("Devolucion bloqueada para ${producto.nombre}")
                } else {
                    append("Devolucion de ${producto.nombre}")
                }
                if (detalleLotesTexto.isNotBlank()) {
                    append(if (bloqueoVenta) ". Lotes bloqueados: " else ". Lotes restituidos: ")
                    append(detalleLotesTexto)
                }
            },
            idUsuario = idCajera,
            nombreUsuario = nombreCajera,
            cantidad = cantidadMovimiento,
            stockAntes = stockAntes,
            stockDespues = stockDespues,
            motivo = motivoDevolucion.ifBlank {
                if (bloqueoVenta) motivoBloqueoRegistrado(motivoDevolucion) else "Devolucion de venta"
            },
            referenciaId = idVenta,
            extra = extra,
            database = database
        )
    }

    private fun registrarMovimientosInventarioPorVenta(
        productosCaja: List<ProductoCaja>,
        stocksOriginales: Map<String, String>,
        idVenta: String
    ) {
        productosCaja.forEach { producto ->
            val indiceProducto = producto.indiceProducto.trim()
            if (indiceProducto.isBlank()) return@forEach

            val stockAntes = stocksOriginales[indiceProducto]?.toIntOrNull() ?: return@forEach
            val cantidadPresentaciones = producto.cantidad.toIntOrNull() ?: return@forEach
            val unidadesPorPresentacion = producto.unidadesPorPresentacion.toIntOrNull() ?: 1
            val cantidadSalida = cantidadPresentaciones * unidadesPorPresentacion
            val stockDespues = (stockAntes - cantidadSalida).coerceAtLeast(0)
            val detalleLotesTexto = construirDetalleLotesTexto(producto)

            val extra = mutableMapOf<String, Any>(
                "nombreProducto" to producto.nombre.orEmpty(),
                "presentacion" to producto.presentacion,
                "cantidadPresentaciones" to cantidadPresentaciones,
                "unidadesPorPresentacion" to unidadesPorPresentacion,
                "idTurno" to idTurnoActivo
            ).apply {
                putAll(construirExtraMovimientoLote(producto))
                if (detalleLotesTexto.isNotBlank()) {
                    this["detalleLotesTexto"] = detalleLotesTexto
                }
            }

            MovimientoInventarioLogger.registrar(
                indiceProducto = indiceProducto,
                tipo = "stock_salida_venta",
                titulo = "Salida por venta",
                descripcion = buildString {
                    append("Venta #$idVenta de ${producto.nombre}")
                    if (detalleLotesTexto.isNotBlank()) {
                        append(". Lotes consumidos: ")
                        append(detalleLotesTexto)
                    }
                },
                idUsuario = idCajera,
                nombreUsuario = nombreCajera,
                cantidad = cantidadSalida,
                stockAntes = stockAntes,
                stockDespues = stockDespues,
                motivo = "Venta finalizada",
                referenciaId = idVenta,
                extra = extra,
                database = database
            )
        }
    }

    private fun construirExtraMovimientoLote(
        producto: ProductoCaja,
        cantidadUnidades: Int? = null
    ): Map<String, Any> {
        val loteConsumido = producto.loteNumero.trim()
        val loteClaveConsumida = producto.loteClaveConsumida.trim()
        val loteVencimientoConsumido = producto.loteVencimiento.trim()
        val loteFefo = producto.loteNumeroFefo.trim()
        val loteVencimientoFefo = producto.loteVencimientoFefo.trim()
        val cantidadBase = cantidadUnidades ?: (
            (producto.cantidad.toIntOrNull() ?: 0) *
                (producto.unidadesPorPresentacion.toIntOrNull() ?: 1)
            )
        val lotesConsumidosDetalle = detallesLotesParaMovimiento(producto, cantidadBase)
            .filter { it.clave.isNotBlank() && it.cantidad > 0 }
            .associate { detalle ->
                detalle.clave to mapOf(
                    "clave" to detalle.clave,
                    "numero" to detalle.numero,
                    "vencimiento" to detalle.vencimiento,
                    "cantidad" to detalle.cantidad
                )
            }
        val coincideConFefo = loteConsumido.isNotBlank() &&
            loteFefo.isNotBlank() &&
            loteConsumido.equals(loteFefo, ignoreCase = true)
        val consumoMixtoManualFefo =
            producto.loteSeleccionManual && lotesConsumidosDetalle.size > 1

        return mutableMapOf<String, Any>(
            "loteNumero" to loteConsumido,
            "loteClaveConsumida" to loteClaveConsumida,
            "loteVencimiento" to loteVencimientoConsumido,
            "loteNumeroConsumido" to loteConsumido,
            "loteVencimientoConsumido" to loteVencimientoConsumido,
            "loteNumeroFefo" to loteFefo,
            "loteVencimientoFefo" to loteVencimientoFefo,
            "loteSeleccionManual" to producto.loteSeleccionManual,
            "modoConsumoLote" to when {
                consumoMixtoManualFefo -> "manual_con_fallback_fefo"
                producto.loteSeleccionManual -> "manual"
                loteConsumido.isNotBlank() -> "fefo_automatico"
                else -> "sin_lote"
            },
            "loteCoincideConFefo" to coincideConFefo,
            "consumoMixtoManualFefo" to consumoMixtoManualFefo
        ).apply {
            if (lotesConsumidosDetalle.isNotEmpty()) {
                put("lotesConsumidosDetalle", lotesConsumidosDetalle)
            }
        }
    }

    private fun construirDetalleLotesTexto(
        producto: ProductoCaja,
        cantidadUnidades: Int? = null
    ): String {
        val cantidadBase = cantidadUnidades ?: (
            (producto.cantidad.toIntOrNull() ?: 0) *
                (producto.unidadesPorPresentacion.toIntOrNull() ?: 1)
            )
        val detalles = detallesLotesParaMovimiento(producto, cantidadBase)
            .filter { it.cantidad > 0 }
            .mapNotNull { detalle ->
                val numeroVisible = detalle.numero.trim().ifBlank { detalle.clave.trim() }
                numeroVisible.takeIf { it.isNotBlank() }?.let { "$it: ${detalle.cantidad}" }
            }

        if (detalles.isNotEmpty()) return detalles.joinToString(", ")

        val loteVisible = producto.loteNumero.trim()
        if (loteVisible.isBlank()) return ""

        return if (cantidadBase > 0) {
            "$loteVisible: $cantidadBase"
        } else {
            loteVisible
        }
    }

    private data class MovimientoLoteCaja(
        val tipo: String = "",
        val cantidad: Double = 0.0,
        val unidad: String = "",
        val motivo: String = "",
        val nombreUsuario: String = "",
        val fecha: String = "",
        val hora: String = "",
        val timestamp: Long = 0L
    )

    private fun registrarMovimientoVentaEnLote(
        productoCaja: ProductoCaja,
        cantidadUnidades: Int,
        idVenta: String
    ) {
        val detalles = detallesLotesParaMovimiento(productoCaja, cantidadUnidades)
        detalles.forEach { detalle ->
            registrarMovimientoEnLoteCaja(
                productoCaja = productoCaja,
                detalle = detalle,
                tipo = "salida",
                delta = -detalle.cantidad.toDouble(),
                motivo = if (idVenta.isNotBlank()) {
                    "Salida por venta #$idVenta"
                } else {
                    "Salida por venta"
                }
            )
        }
    }

    private fun registrarMovimientoReversionEnLote(
        productoCaja: ProductoCaja,
        cantidadUnidades: Int,
        idVenta: String
    ) {
        val detalles = detallesLotesParaMovimiento(productoCaja, cantidadUnidades)
        detalles.forEach { detalle ->
            registrarMovimientoEnLoteCaja(
                productoCaja = productoCaja,
                detalle = detalle,
                tipo = "entrada",
                delta = detalle.cantidad.toDouble(),
                motivo = "Reversion de venta #$idVenta"
            )
        }
    }

    private fun registrarMovimientoDevolucionEnLote(
        productoCaja: ProductoCaja,
        cantidadUnidades: Int,
        idVenta: String
    ) {
        val detalles = detallesLotesParaMovimiento(productoCaja, cantidadUnidades)
        detalles.forEach { detalle ->
            registrarMovimientoEnLoteCaja(
                productoCaja = productoCaja,
                detalle = detalle,
                tipo = "entrada",
                delta = detalle.cantidad.toDouble(),
                motivo = if (idVenta.isNotBlank()) {
                    "Devolución de venta #$idVenta"
                } else {
                    "Devolución de venta"
                }
            )
        }
    }

    private fun registrarMovimientoBloqueoDevolucionEnLote(
        productoCaja: ProductoCaja,
        cantidadUnidades: Int,
        idVenta: String,
        motivoDevolucion: String
    ) {
        val detalles = detallesLotesParaMovimiento(productoCaja, cantidadUnidades)
        detalles.forEach { detalle ->
            registrarMovimientoEnLoteCaja(
                productoCaja = productoCaja,
                detalle = detalle,
                tipo = "bloqueo",
                delta = detalle.cantidad.toDouble(),
                motivo = buildString {
                    append("Bloqueo por devolucion")
                    if (idVenta.isNotBlank()) append(" de venta #$idVenta")
                    if (motivoDevolucion.isNotBlank()) {
                        append(": ")
                        append(motivoDevolucion)
                    }
                }
            )
        }
    }

    private fun detallesLotesParaMovimiento(
        productoCaja: ProductoCaja,
        cantidadUnidades: Int
    ): List<LoteConsumoDetalleCaja> {
        val detalles = productoCaja.lotesConsumidosDetalle.values
            .filter { it.clave.isNotBlank() && it.cantidad > 0 }
            .toList()

        if (detalles.isNotEmpty()) {
            var pendiente = cantidadUnidades.coerceAtLeast(0)
            val resultado = mutableListOf<LoteConsumoDetalleCaja>()
            detalles.forEach { detalle ->
                if (pendiente <= 0) return@forEach
                val tramo = minOf(detalle.cantidad, pendiente)
                if (tramo > 0) {
                    resultado += detalle.copy(cantidad = tramo)
                    pendiente -= tramo
                }
            }
            return resultado
        }

        val numeroLote = productoCaja.loteNumero.trim()
        val claveLote = productoCaja.loteClaveConsumida.trim().ifBlank {
            if (numeroLote.isNotBlank()) sanitizarClaveLoteCaja(numeroLote) else ""
        }
        if (claveLote.isBlank()) return emptyList()

        return listOf(
            LoteConsumoDetalleCaja(
                clave = claveLote,
                numero = numeroLote.ifBlank { claveLote },
                vencimiento = productoCaja.loteVencimiento.trim(),
                cantidad = cantidadUnidades
            )
        )
    }

    private fun registrarMovimientoEnLoteCaja(
        productoCaja: ProductoCaja,
        detalle: LoteConsumoDetalleCaja,
        tipo: String,
        delta: Double,
        motivo: String
    ) {
        val indiceProducto = productoCaja.indiceProducto.trim()
        val claveLote = detalle.clave.trim()
        if (indiceProducto.isBlank() || claveLote.isBlank()) return

        val momento = FechaHoraServidorHelper.estimarMomentoActualDesdeCache()
        
        val movimiento = MovimientoLoteCaja(
            tipo = tipo,
            cantidad = delta,
            unidad = "unidades",
            motivo = motivo,
            nombreUsuario = nombreCajera.ifBlank { "Sistema" },
            fecha = momento.fechaFirebase,
            hora = momento.horaTexto,
            timestamp = momento.timestampServidorMs
        )

        database.reference
            .child("Inventario")
            .child("Productos")
            .child(indiceProducto)
            .child("lotes")
            .child(claveLote)
            .child("movimientos")
            .push()
            .setValue(movimiento)
    }

    private fun sanitizarClaveLoteCaja(numero: String): String {
        return numero.trim()
            .replace("[.#$\\[\\]/\\\\]".toRegex(), "-")
            .replace(" ", "_")
            .ifBlank { "L001" }
    }

    private fun validarProductoCajaBasico(producto: ProductoCaja): String? {
        if (producto.id.isBlank()) return "El producto en caja no tiene id válido"
        if (producto.indiceProducto.isBlank()) return "El producto ${producto.nombre} no tiene índice válido"

        val cantidad = producto.cantidad.toIntOrNull()
        if (cantidad == null || cantidad <= 0) return "Cantidad inválida para ${producto.nombre}"

        val unidades = producto.unidadesPorPresentacion.toIntOrNull()
        if (unidades == null || unidades <= 0) return "Presentación inválida para ${producto.nombre}"

        val precio = producto.precioUnitario.toDoubleOrNull()
        if (precio == null || precio < 0) return "Precio inválido para ${producto.nombre}"

        return null
    }

    private fun mostrarToastSeguro(mensaje: String) {
        val ctx = context ?: return
        Toast.makeText(ctx, mensaje, Toast.LENGTH_SHORT).show()
    }

    private fun mostrarDialogoProgresoVenta(
        titulo: String = "Procesando venta",
        mensaje: String = "Validando la venta y evitando cobros duplicados. No cierres esta pantalla."
    ) {
        mostrarLoadingOverlayCaja(titulo, mensaje)
    }

    private fun ocultarDialogoProgresoVenta() {
        ocultarLoadingOverlayCaja()
    }

    private fun mostrarDialogoVentaYaRegistrada(operationId: String) {
        if (!isAdded || context == null) return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Venta ya registrada")
            .setMessage(
                "Esta operación ya se había procesado.\n\n" +
                    "No se volvió a cobrar ni a descontar stock para evitar duplicados.\n\n" +
                    "ID de operación: $operationId"
            )
            .setPositiveButton("Entendido", null)
            .show()
    }

    private fun mensajeResumenCierre(
        estadoCuadre: String,
        montoSobrante: Double,
        montoFaltante: Double
    ): String {
        return when (estadoCuadre) {
            "sobrante" -> "Turno cerrado con sobrante de ${MonedaHelper.formatear(montoSobrante)}"
            "faltante" -> "Turno cerrado con faltante de ${MonedaHelper.formatear(montoFaltante)}"
            else -> "Turno cerrado correctamente"
        }
    }

    private fun String?.toIntSeguro(default: Int = 0): Int {
        return this?.trim()?.toIntOrNull() ?: default
    }

    private fun String?.toDoubleSeguro(default: Double? = 0.0): Double? {
        if (this == null) return default
        return try {
            val limpio = this.trim()
                .replace("$", "")
                .replace(" ", "")
                .replace(",", ".")
                .replace("[^0-9.-]".toRegex(), "")
            limpio.toDoubleOrNull() ?: default
        } catch (_: Exception) {
            default
        }
    }

    private fun DataSnapshot.toMoldeProductoSeguro(): MoldeProductos? {
        if (!exists()) return null
        return try {
            getValue(MoldeProductos::class.java)?.let { producto ->
                if (producto.tipoBaseInventario.isBlank()) {
                    producto.copy(
                        tipoBaseInventario = PresentacionRules.normalizarTipoBaseInventario(
                            producto.tipoBaseInventario,
                            producto.unidadbase
                        )
                    )
                } else {
                    producto
                }
            }
        } catch (_: DatabaseException) {
            MoldeProductos(
                nombre = child("nombre").value?.toString().orEmpty(),
                codigo = child("codigo").value?.toString().orEmpty(),
                vencimiento = child("vencimiento").value?.toString().orEmpty(),
                categoria = child("categoria").value?.toString().orEmpty(),
                preciodecompra = child("preciodecompra").value?.toString().orEmpty(),
                cantidadinicial = child("cantidadinicial").value?.toString().orEmpty(),
                stockminimo = child("stockminimo").value?.toString().orEmpty(),
                unidadbase = child("unidadbase").value?.toString().orEmpty(),
                tipoBaseInventario = PresentacionRules.normalizarTipoBaseInventario(
                    child("tipoBaseInventario").value?.toString(),
                    child("unidadbase").value?.toString().orEmpty()
                ),
                presentacionprincipal = child("presentacionprincipal").value?.toString().orEmpty(),
                requierereceta = child("requierereceta").getValue(Boolean::class.java) ?: false,
                estadodelproducto = child("estadodelproducto").getValue(Boolean::class.java) ?: true,
                indice = child("indice").value?.toString().orEmpty().ifBlank { key.orEmpty() },
                tienePresentaciones = child("tienePresentaciones").getValue(Boolean::class.java) ?: false,
                unidadesPorPresentacionCompra = child("unidadesPorPresentacionCompra").value?.toString()?.toIntOrNull() ?: 0,
                basePorUnidad = child("basePorUnidad").value?.toString()?.toDoubleOrNull() ?: 0.0,
                imagenUrl = child("imagenUrl").value?.toString().orEmpty(),
                loteConsumoSeleccionado = child("loteConsumoSeleccionado").value?.toString().orEmpty(),
                loteConsumoSeleccionManual = child("loteConsumoSeleccionManual").getValue(Boolean::class.java) ?: false,
                lotes = child("lotes").children.mapNotNull { loteSnapshot ->
                    val lote = loteSnapshot.getValue(LoteProducto::class.java)
                    val clave = loteSnapshot.key.orEmpty()
                    if (lote != null && clave.isNotBlank()) clave to lote else null
                }.toMap()
            )
        }
    }

    private fun obtenerFechaActual(): String {
        val cache = FechaHoraServidorHelper.estimarMomentoActualDesdeCache().fechaFirebase
        return cache.ifBlank {
            // Backup seguro: si no hay caché, usamos la fecha local pero informamos
            java.time.LocalDate.now(java.time.ZoneId.of("America/Lima")).toString()
        }
    }

    private fun obtenerHoraActual(): String {
        val cache = FechaHoraServidorHelper.estimarMomentoActualDesdeCache().horaTexto
        return cache.ifBlank {
            // Backup seguro: formato h:mm a
            java.time.LocalTime.now(java.time.ZoneId.of("America/Lima"))
                .format(java.time.format.DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.forLanguageTag("es-PE")))
                .lowercase()
        }
    }

    private fun obtenerTituloVentaExitosa(): TextView? {
        val b = _binding ?: return null
        // OJO: Aunque el ID dice "Apertura", en el XML está dentro de layoutVentaExito
        return if (esTablet()) {
            b.root.findViewById(R.id.tvTituloAperturaTablet)
        } else {
            b.root.findViewById(R.id.tvTituloAperturaMovil)
        }
    }



    private fun restablecerPantallaDespuesDeVenta() {
        val b = _binding ?: return

        // Restaurar StatusBar a blanco y reaparecer la barra de navegación
        configurarStatusBarInmersiva(false)
        (activity as? ActivityFragmentos)?.setBottomNavigationVisibility(true)

        // Resetear expansión inmersiva
        b.layoutVentaExito.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = 0
            bottomMargin = 0
        }

        b.layoutVentaExito.visibility = View.GONE
        b.layoutAperturaCaja.visibility = View.GONE
        ocultarLayoutCierreCajaInterno()
        reiniciarFlujoCobroInline()
        limpiarFormularioComprobanteGlobal()

        // === CORRECCIÓN: Usar los nombres de tus funciones reales ===
        if (esTablet()) {
            aplicarLayoutInicialTablet()
        } else {
            aplicarLayoutInicialMobile()
        }
    }

    /**
     * Microanimacion "vuela al carrito": usamos un snapshot chico del producto y lo llevamos
     * con una trayectoria curva hasta el badge/resumen del carrito. Cuando aterriza, el badge
     * hace un bounce corto para confirmar visualmente que la accion se registro.
     */


    private fun animarVueloAlCarrito(anchor: View?) {
        if (anchor == null) return
        val activity = activity ?: return
        val destino = obtenerDestinoAnimacionCarrito() ?: return
        val decor = activity.findViewById<ViewGroup>(android.R.id.content) ?: return

        val origenLoc = IntArray(2)
        anchor.getLocationInWindow(origenLoc)
        val destinoLoc = IntArray(2)
        destino.getLocationInWindow(destinoLoc)
        val decorLoc = IntArray(2)
        decor.getLocationInWindow(decorLoc)

        val tamanioInicial = (64 * resources.displayMetrics.density).toInt()
        val tamanioFinal = (26 * resources.displayMetrics.density).toInt()
        val origenX = origenLoc[0] + anchor.width / 2f - tamanioInicial / 2f - decorLoc[0]
        val origenY = origenLoc[1] + anchor.height / 2f - tamanioInicial / 2f - decorLoc[1]
        val destinoX = destinoLoc[0] + destino.width / 2f - tamanioFinal / 2f - decorLoc[0]
        val destinoY = destinoLoc[1] + destino.height / 2f - tamanioFinal / 2f - decorLoc[1]
        val puntoArcoY = minOf(origenY, destinoY) - (56 * resources.displayMetrics.density)

        val snapshot = crearSnapshotVueloProducto(anchor, tamanioInicial) ?: return

        // MEJORA: Asignamos un tag para poder identificarlo y barrerlo en onDestroyView si hace falta
        snapshot.tag = "snapshot_vuelo_caja"
        decor.addView(snapshot)

        snapshot.x = origenX
        snapshot.y = origenY
        snapshot.alpha = 0f
        snapshot.scaleX = 0.72f
        snapshot.scaleY = 0.72f

        // SOLUCIÓN: Cambiamos withEndAction por un Listener completo que capture la cancelación
        snapshot.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(90L)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (_binding == null) {
                        runCatching { decor.removeView(snapshot) }
                        return
                    }

                    val animacionVuelo = AnimatorSet().apply {
                        playTogether(
                            ObjectAnimator.ofFloat(snapshot, View.X, origenX, destinoX),
                            ObjectAnimator.ofFloat(snapshot, View.Y, origenY, puntoArcoY, destinoY),
                            ObjectAnimator.ofFloat(snapshot, View.SCALE_X, 1f, 0.42f),
                            ObjectAnimator.ofFloat(snapshot, View.SCALE_Y, 1f, 0.42f),
                            ObjectAnimator.ofFloat(snapshot, View.ALPHA, 1f, 0.96f, 0f)
                        )
                        duration = 320L
                        interpolator = android.view.animation.AccelerateDecelerateInterpolator()
                        addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(anim: Animator) {
                                runCatching { decor.removeView(snapshot) }
                                if (_binding != null) {
                                    animarBounceCarrito(destino)
                                }
                            }

                            override fun onAnimationCancel(anim: Animator) {
                                // Si se cancela a mitad de camino, removemos la vista inmediatamente
                                runCatching { decor.removeView(snapshot) }
                            }
                        })
                    }
                    animacionVuelo.start()
                }

                override fun onAnimationCancel(animation: Animator) {
                    runCatching { decor.removeView(snapshot) }
                }
            })
            .start()
    }

    private fun obtenerDestinoAnimacionCarrito(): View? {
        val b = _binding ?: return null
        return when {
            b.tvCarritoBadgeResumen.visibility == View.VISIBLE -> b.tvCarritoBadgeResumen
            b.cardResumenCaja.visibility == View.VISIBLE -> b.cardResumenCaja
            b.recyclerView2.visibility == View.VISIBLE -> b.recyclerView2
            else -> null
        }
    }

    private fun crearSnapshotVueloProducto(anchor: View, tamanio: Int): ImageView? {
        val ancho = max(anchor.width, 1)
        val alto = max(anchor.height, 1)
        val bitmap = runCatching {
            Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888).also { salida ->
                val canvas = Canvas(salida)
                anchor.draw(canvas)
            }
        }.getOrNull() ?: return null

        return ImageView(requireContext()).apply {
            background = ContextCompat.getDrawable(context, R.drawable.bg_burbuja_vuelo_carrito)
            setImageBitmap(bitmap)
            scaleType = ImageView.ScaleType.CENTER_CROP
            clipToOutline = true
            val padding = (4 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
            layoutParams = ViewGroup.LayoutParams(tamanio, tamanio)
            elevation = 30f
        }
    }

    private fun animarBounceCarrito(destino: View) {
        destino.animate().cancel()
        destino.scaleX = 1f
        destino.scaleY = 1f
        destino.animate()
            .scaleX(1.12f)
            .scaleY(1.12f)
            .setDuration(110L)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .withEndAction {
                if (_binding == null) return@withEndAction
                destino.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(170L)
                    .setInterpolator(android.view.animation.OvershootInterpolator(1.8f))
                    .start()
            }
            .start()
    }

    private fun prepararPantallaParaVentaExitosa() {
        val b = _binding ?: return
        val elevacionOverlay = 24f * resources.displayMetrics.density

        dialogoCobroActual?.dismiss()
        dialogoCobroActual = null

        loadingOperacionCajaActivo = false
        b.layoutLoadingOverlayCaja.animate().cancel()
        b.layoutLoadingOverlayCaja.clearAnimation()
        b.layoutLoadingOverlayCaja.alpha = 1f
        b.layoutLoadingOverlayCaja.visibility = View.GONE

        b.layoutCobroPanel.animate().cancel()
        b.layoutCobroPanel.clearAnimation()
        b.layoutCobroPanel.visibility = View.GONE
        b.layoutPasoCobroMetodo.visibility = View.GONE
        b.layoutPasoCobroDetalle.visibility = View.GONE

        b.layoutComprobante.animate().cancel()
        b.layoutComprobante.clearAnimation()
        b.layoutComprobante.visibility = View.GONE

        b.layoutVentaExito?.animate()?.cancel()
        b.layoutVentaExito?.clearAnimation()
        b.layoutVentaExito?.bringToFront()
        b.layoutVentaExito?.translationZ = elevacionOverlay
        b.layoutVentaExito?.let { ViewCompat.setElevation(it, elevacionOverlay) }
    }

    private fun configurarStatusBarInmersiva(activa: Boolean) {
        val window = activity?.window ?: return
        val decorView = window.decorView
        val controller = androidx.core.view.WindowInsetsControllerCompat(window, decorView)

        if (activa) {
            window.statusBarColor = Color.parseColor("#22C55E")
            controller.isAppearanceLightStatusBars = false // Iconos en blanco para contrastar
        } else {
            window.statusBarColor = Color.WHITE
            controller.isAppearanceLightStatusBars = true // Restaurar iconos oscuros
        }
    }

    private fun ejecutarEfectosVentaExitosa() {
        val b = _binding ?: return
        val ctx = context ?: return

        // 0. ACTIVAR MODO INMERSIVO (StatusBar verde y ocultar barra inferior sin tirones)
        (activity as? ActivityFragmentos)?.setBottomNavigationVisibility(false)
        configurarStatusBarInmersiva(true)

        // Expansión inmersiva real: usamos márgenes negativos para cubrir las áreas de las barras
        // sin mover el resto de los elementos del fragmento (cero saltos visuales).
        val insets = ViewCompat.getRootWindowInsets(b.root)
        val systemBars = insets?.getInsets(WindowInsetsCompat.Type.systemBars())
        val topI = systemBars?.top ?: 0
        val bottomI = systemBars?.bottom ?: 0

        b.layoutVentaExito?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = -topI
            bottomMargin = -bottomI
        }

        // 1. GRADIENTE VERDE PREMIUM
        val gradienteVerde = android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                Color.parseColor("#22C55E"), // Verde vibrante arriba
                Color.parseColor("#166534")  // Verde esmeralda oscuro abajo
            )
        )
        b.layoutVentaExito?.background = gradienteVerde
        b.layoutVentaExito?.visibility = View.VISIBLE

        // 2. ANIMACIÓN "PULSO DE RESPIRACIÓN" (Más lento y disfrutable)
        val animacionPulso = android.view.animation.AlphaAnimation(1.0f, 0.7f).apply {
            duration = 800 // 800ms por cada fase (más suave, no estresante)
            repeatMode = android.view.animation.Animation.REVERSE
            repeatCount = 3 // Pulsa un par de veces para llamar la atención
        }
        b.layoutVentaExito?.startAnimation(animacionPulso)

        // 3. REPRODUCIR EL SONIDO DE ÉXITO PREMIUM (Buena vibra armónica)
        // Dispara el nuevo arpegio sintetizado a través de tu controlador de sonido
        feedbackVm.notificarVentaExitosa(b.layoutVentaExito ?: b.root)

        // 4. EL CANDADO DE TIEMPO (Dejar disfrutar la venta y limpiar la pantalla sola)
        b.layoutVentaExito?.postDelayed({
            val bFinal = _binding ?: return@postDelayed

            // Desvanecemos la pantalla suavemente al salir
            val transicionSalida = android.view.animation.AlphaAnimation(1.0f, 0.0f).apply {
                duration = 400
            }
            bFinal.layoutVentaExito?.startAnimation(transicionSalida)
            bFinal.layoutVentaExito?.postDelayed({
                if (_binding != null) {
                    restablecerPantallaDespuesDeVenta()
                }
            }, 400)
        }, 3500) // 3.5 segundos de gloria en pantalla
    }

    private fun mostrarAnimacionVentaExitosa() {
        val b = _binding ?: return
        val titulo = obtenerTituloVentaExitosa()
        // El layout actual solo tiene imagen + titulo (sin subtitulo, sin boton). Card es
        // el primer hijo del overlay para animar pop con scale + alpha.
        val cardExito = (b.layoutVentaExito as? ViewGroup)?.getChildAt(0)

        prepararPantallaParaVentaExitosa()
        feedbackVm.notificarVentaExitosa(b.layoutVentaExito ?: b.root)

        // Aplicamos los efectos premium (gradiente, parpadeo, sonido)
        ejecutarEfectosVentaExitosa()

        titulo?.text = "¡Venta exitosa!"

        // Estado inicial: overlay invisible, card colapsada, imagen invisible y rotada.
        b.layoutVentaExito?.visibility = View.VISIBLE
        b.layoutVentaExito?.alpha = 0f
        cardExito?.scaleX = 0.6f
        cardExito?.scaleY = 0.6f
        cardExito?.alpha = 0f
        b.imgVentaExitosa?.scaleX = 0f
        b.imgVentaExitosa?.scaleY = 0f
        b.imgVentaExitosa?.rotation = -20f
        b.imgVentaExitosa?.alpha = 0f
        titulo?.alpha = 0f
        titulo?.translationY = 16f

        // 1) Overlay aparece rapido.
        b.layoutVentaExito?.animate()
            ?.alpha(1f)
            ?.setDuration(200)
            ?.start()

        // 2) Card hace pop con bounce.
        cardExito?.animate()
            ?.scaleX(1f)
            ?.scaleY(1f)
            ?.alpha(1f)
            ?.setDuration(380)
            ?.setInterpolator(android.view.animation.OvershootInterpolator(1.4f))
            ?.start()

        // 3) Imagen del check con bounce dramatico.
        b.imgVentaExitosa?.animate()
            ?.scaleX(1f)
            ?.scaleY(1f)
            ?.rotation(0f)
            ?.alpha(1f)
            ?.setStartDelay(180)
            ?.setDuration(520)
            ?.setInterpolator(android.view.animation.OvershootInterpolator(2.5f))
            ?.start()

        // 4) Titulo aparece con fade + slide-up.
        titulo?.animate()
            ?.alpha(1f)
            ?.translationY(0f)
            ?.setStartDelay(380)
            ?.setDuration(280)
            ?.setInterpolator(android.view.animation.DecelerateInterpolator())
            ?.start()
    }

    private fun precargarQrsMetodos(metodos: List<MetodoPagoConfig>) {
        val ctx = context ?: return

        metodos.forEach { metodo ->
            if (metodo.esBilleteraLike() && metodo.usaQR && metodo.qrUrl.isNotBlank()) {
                Glide.with(ctx)
                    .load(metodo.qrUrl)
                    .preload()
            }
        }
    }


    private fun guardarUltimosDatosClienteComprobante() {
        val datos = hashMapOf<String, Any>(
            "nombre" to nombreClienteVenta,
            "documento" to documentoClienteVenta,
            "razonSocial" to razonSocialClienteVenta,
            "correo" to correoClienteVenta,
            "tipoComprobante" to tipoComprobanteSeleccionado,
            "timestamp" to System.currentTimeMillis()
        )

        database.getReference("ConfiguracionTienda")
            .child("datosClienteComprobante")
            .setValue(datos)
    }



    private fun guardarClienteComprobantePorDocumento() {
        val documentoLimpio = documentoClienteVenta.trim()
        if (documentoLimpio.isBlank()) return

        val datos = hashMapOf<String, Any>(
            "documento" to documentoLimpio,
            "nombre" to nombreClienteVenta,
            "razonSocial" to razonSocialClienteVenta,
            "correo" to correoClienteVenta,
            "tipoComprobante" to tipoComprobanteSeleccionado,
            "timestamp" to System.currentTimeMillis()
        )

        database.getReference("ConfiguracionTienda")
            .child("clientesComprobante")
            .child(documentoLimpio)
            .setValue(datos)
    }



    private fun aplicarLayoutInicialMobile() {
        val b = _binding ?: return

        b.layoutComprobante.visibility = View.GONE
        b.layoutAperturaCaja.visibility = View.GONE
        b.layoutVentaExito?.visibility = View.GONE
        b.buscador?.visibility = View.VISIBLE

        // En modo busqueda: solo mostrar panel de resultados sin ocultar carrito
        if (modoBusquedaActivo) {
            b.root.findViewById<View>(R.id.layoutBusquedaActivaInline)?.visibility = View.VISIBLE
            b.root.findViewById<RecyclerView>(R.id.recyclerResultadosInline)?.visibility = View.VISIBLE
            actualizarCabeceraBusquedaInline()
            actualizarVisibilidadCategoriasInline()
            aplicarFiltrosBusquedaInline(b.buscar?.text?.toString().orEmpty())
            return
        }
    }

    private fun configurarSwitchComprobante(switch: SwitchMaterial) {
        switch.setOnCheckedChangeListener { _, isChecked ->
            if (sincronizandoSwitchFactura) return@setOnCheckedChangeListener

            if (!puedeOperarTurnoActual()) {
                actualizarEstadoSwitchFacturaSinFeedback(switch, formularioFacturaExpandido)
                return@setOnCheckedChangeListener
            }

            if (formularioFacturaExpandido == isChecked) return@setOnCheckedChangeListener

            formularioFacturaExpandido = isChecked
            feedbackVm.notificarSwitchComprobante(switch, activado = isChecked)
            actualizarFormularioClienteInline()

            // CAMBIO: Solo fuerza el foco si el switch se acaba de encender
            actualizarFocoFormularioClienteInline(forzarEnfoque = isChecked)

            actualizarUiTipoComprobante()
        }
    }

    // Fuente unica de verdad para la visibilidad de los switches "necesita factura":
    // depende solo de si hay productos en el carrito. Llamar despues de cualquier cambio
    // que muestre/oculte cardResumenCaja o que modifique listaProductosCaja.
    private fun actualizarFormularioClienteInline() {
        val b = _binding ?: return

        // CORRECCIÓN: Si estamos buscando productos, no mostramos el formulario
        // de cliente ni permitimos que se limpie el foco (lo que ocultaría el teclado).
        if (modoBusquedaActivo) {
            b.btnNecesitoFactura.visibility = View.GONE
            b.cardDatosClienteInline.visibility = View.GONE
            return
        }

        val hayProductos = listaProductosCaja.isNotEmpty()
        val visibilidad = if (hayProductos) View.VISIBLE else View.GONE

        b.btnNecesitoFactura.visibility = visibilidad
        actualizarEstadoSwitchFacturaSinFeedback(b.btnNecesitoFactura, formularioFacturaExpandido)

        // El card inline se expande tanto en movil como en tablet — es el mini-formulario.
        b.cardDatosClienteInline.visibility =
            if (formularioFacturaExpandido && hayProductos) View.VISIBLE else View.GONE

        if (!formularioFacturaExpandido || !hayProductos) {
            limpiarFocoFormularioClienteInline()
        }
    }

    private fun actualizarEstadoSwitchFacturaSinFeedback(switch: SwitchMaterial, checked: Boolean) {
        if (switch.isChecked == checked) return
        sincronizandoSwitchFactura = true
        try {
            switch.isChecked = checked
        } finally {
            sincronizandoSwitchFactura = false
        }
    }

    private fun actualizarFocoFormularioClienteInline(forzarEnfoque: Boolean = false) {
        val b = _binding ?: return
        if (!formularioFacturaExpandido || listaProductosCaja.isEmpty()) {
            limpiarFocoFormularioClienteInline()
            return
        }

        // Si no se requiere forzar el enfoque de forma explícita, salimos para no interrumpir
        if (!forzarEnfoque) return

        val documentoInline = b.etDocumentoClienteInline ?: return
        documentoInline.post {
            if (!isAdded || !documentoInline.isShown) return@post

            // Salvaguarda: Si el cajero ya está escribiendo en Nombre, Razón Social o Correo, NO le quitamos el foco
            val algunCampoTieneFoco = b.etNombreClienteInline?.hasFocus() == true ||
                    b.etRazonSocialInline?.hasFocus() == true ||
                    b.etCorreoClienteInline?.hasFocus() == true
            if (algunCampoTieneFoco) return@post

            desplazarFormularioClienteInline(documentoInline)
            val restaurarSoftInput = documentoInline.showSoftInputOnFocus
            documentoInline.showSoftInputOnFocus = false
            documentoInline.requestFocus()
            documentoInline.setSelection(documentoInline.text?.length ?: 0)
            documentoInline.showSoftInputOnFocus = restaurarSoftInput
        }
    }

    private fun limpiarFocoFormularioClienteInline() {
        val b = _binding ?: return
        if (modoBusquedaActivo) return // No quitar foco si el usuario está buscando

        val campos = arrayOf(
            b.etDocumentoClienteInline,
            b.etNombreClienteInline,
            b.etRazonSocialInline,
            b.etCorreoClienteInline
        )
        val campoActivo = campos.firstOrNull { it?.hasFocus() == true }
        val destinoFoco = if (b.buttoncobrar.visibility == View.VISIBLE) {
            b.buttoncobrar
        } else {
            b.btnNecesitoFactura
        }
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        campos.forEach { it?.clearFocus() }
        destinoFoco.requestFocus()
        campoActivo?.let { imm?.hideSoftInputFromWindow(it.windowToken, 0) }
        imm?.restartInput(destinoFoco)
        ocultarTeclado(campoActivo ?: destinoFoco)
        b.root.post {
            destinoFoco.requestFocus()
            imm?.restartInput(destinoFoco)
            ocultarTeclado(destinoFoco)
        }
        b.root.postDelayed({
            destinoFoco.requestFocus()
            imm?.restartInput(destinoFoco)
            ocultarTeclado(destinoFoco)
        }, 120)
        campoActivo?.post {
            campoActivo.clearFocus()
        }
    }

    private fun desplazarFormularioClienteInline(objetivo: View) {
        var actual = objetivo.parent
        while (actual is View) {
            if (actual is NestedScrollView) {
                actual.smoothScrollTo(0, objetivo.top.coerceAtLeast(0))
                return
            }
            actual = actual.parent
        }
    }

    private fun limpiarDatosClienteSeleccionados() {
        nombreClienteVenta = ""
        documentoClienteVenta = ""
        razonSocialClienteVenta = ""
        correoClienteVenta = ""
    }

    private fun mostrarLayoutComprobanteMobile() {
        val b = _binding ?: return

        b.layoutComprobante.visibility = View.VISIBLE

        b.buscador?.visibility = View.GONE
        b.headerProductos.visibility = View.GONE
        b.recyclerView2.visibility = View.GONE
        b.cardResumenCaja.visibility = View.GONE
        b.buttoncobrar.visibility = View.GONE
        b.root.findViewById<View>(R.id.recyclerCategoriasInline)?.visibility = View.GONE
        b.root.findViewById<View>(R.id.recyclerResultadosInline)?.visibility = View.GONE
        b.root.findViewById<View>(R.id.layoutSinResultadosBusqueda)?.visibility = View.GONE
        b.root.findViewById<View>(R.id.btnCategoriasBusquedaInline)?.visibility = View.GONE
        b.root.findViewById<View>(R.id.layoutEstadoVacioCaja)?.visibility = View.GONE
        actualizarAccionesVentaEsperaUi()
        actualizarAyudaCajaVaciaUi()

    }

    private fun ocultarLayoutComprobanteMobile() {
        val b = _binding ?: return

        b.layoutComprobante.visibility = View.GONE
        b.buscador?.visibility = View.VISIBLE
        // Si seguíamos en modo búsqueda, salir limpiamente
        if (modoBusquedaActivo) {
            salirModoBusqueda()
            return
        }

        val hayProductos = listaProductosCaja.isNotEmpty()

        b.headerProductos.visibility = if (hayProductos) View.VISIBLE else View.GONE
        b.recyclerView2.visibility = if (hayProductos) View.VISIBLE else View.GONE
        b.cardResumenCaja.visibility = View.VISIBLE
        b.buttoncobrar.visibility = if (hayProductos) View.VISIBLE else View.GONE
        b.root.findViewById<View>(R.id.layoutEstadoVacioCaja)?.visibility =
            if (hayProductos) View.GONE else View.VISIBLE

        actualizarAccionesVentaEsperaUi()
        actualizarAyudaCajaVaciaUi()
        limpiarFormularioComprobanteGlobal()
    }



    private fun configurarAutocompletadoDocumentoGlobal() {
        val b = _binding ?: return

        // FIX: Removemos los watchers anteriores si es que ya existen para evitar acumulaciones
        watcherDocumentoCliente?.let { b.etDocumentoCliente.removeTextChangedListener(it) }
        watcherDocumentoClienteInline?.let { b.etDocumentoClienteInline?.removeTextChangedListener(it) }

        // ASIGNAR GUARDANDO LA REFERENCIA
        watcherDocumentoCliente = b.etDocumentoCliente.doAfterTextChanged { editable ->
            val documento = editable?.toString()?.trim().orEmpty()

            // Si el campo se vacía, limpiamos todo al instante
            if (documento.isEmpty()) {
                handlerBusquedaDocumento.removeCallbacksAndMessages(null)
                limpiarSoloCamposAutocompletados()
                ultimoDocumentoBuscado = ""
                clienteAutocompletadoActivo = false
                b.inputLayoutDocumentoCliente.endIconMode = TextInputLayout.END_ICON_NONE
                return@doAfterTextChanged
            }

            runnableBusquedaDocumento?.let { handlerBusquedaDocumento.removeCallbacks(it) }

            runnableBusquedaDocumento = Runnable {
                if (!isAdded || _binding == null) return@Runnable

                if (documento.length == 8 || documento.length == 11) {
                    if (documento != ultimoDocumentoBuscado) {
                        // Feedback visual: Cargando (??)
                        b.inputLayoutDocumentoCliente.setEndIconDrawable(R.drawable.baseline_sync_24)
                        b.inputLayoutDocumentoCliente.endIconMode = TextInputLayout.END_ICON_CUSTOM

                        ultimoDocumentoBuscado = documento
                        buscarClientePorDocumento(documento)
                    }
                } else {
                    // Si el documento deja de ser válido, limpiamos por seguridad
                    if (clienteAutocompletadoActivo) {
                        limpiarSoloCamposAutocompletados()
                        clienteAutocompletadoActivo = false
                        ultimoDocumentoBuscado = ""
                    }
                }
            }
            handlerBusquedaDocumento.postDelayed(runnableBusquedaDocumento!!, 500)
        }

        // ASIGNAR GUARDANDO LA REFERENCIA
        watcherDocumentoClienteInline = b.etDocumentoClienteInline?.doAfterTextChanged { editable ->
            val documento = editable?.toString()?.trim().orEmpty()

            if (documento.isEmpty()) {
                handlerBusquedaDocumento.removeCallbacksAndMessages(null)
                limpiarSoloCamposAutocompletados()
                ultimoDocumentoBuscado = ""
                clienteAutocompletadoActivo = false
                b.inputLayoutDocumentoClienteInline?.endIconMode = TextInputLayout.END_ICON_NONE
                ocultarChipClienteEncontradoInline()
                return@doAfterTextChanged
            }

            // Auto-deteccion del tipo segun longitud (8 = boleta, 11 = factura).
            // El cajero solo escribe el documento; tipo y datos se autocargan.
            val tipoAutoDetectado = when (documento.length) {
                8 -> "BOLETA"
                11 -> "FACTURA"
                else -> null
            }
            if (tipoAutoDetectado != null && tipoAutoDetectado != tipoComprobanteSeleccionado) {
                tipoComprobanteSeleccionado = tipoAutoDetectado
                actualizarUiTipoComprobante()
            }

            runnableBusquedaDocumento?.let { handlerBusquedaDocumento.removeCallbacks(it) }

            runnableBusquedaDocumento = Runnable {
                if (!isAdded || _binding == null) return@Runnable

                if (documento.length == 8 || documento.length == 11) {
                    if (documento != ultimoDocumentoBuscado) {
                        b.inputLayoutDocumentoClienteInline?.setEndIconDrawable(R.drawable.baseline_sync_24)
                        b.inputLayoutDocumentoClienteInline?.endIconMode = TextInputLayout.END_ICON_CUSTOM

                        ultimoDocumentoBuscado = documento
                        buscarClientePorDocumento(documento)
                    }
                } else {
                    if (clienteAutocompletadoActivo) {
                        limpiarSoloCamposAutocompletados()
                        clienteAutocompletadoActivo = false
                        ultimoDocumentoBuscado = ""
                    }
                    ocultarChipClienteEncontradoInline()
                }
            }
            handlerBusquedaDocumento.postDelayed(runnableBusquedaDocumento!!, 500)
        }
    }



    private fun buscarClientePorDocumento(documento: String) {
        _binding ?: return
        val form = obtenerFormularioClienteActual()

        database.getReference("ConfiguracionTienda")
            .child("clientesComprobante")
            .child(documento)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!isAdded || _binding == null) return@addOnSuccessListener

                // QUITAR "CARGANDO" (Volver al icono normal o limpiar)
                form?.inputDocumento?.setEndIconDrawable(R.drawable.searchicon)

                if (snapshot.exists()) {
                    val nombre = snapshot.child("nombre").getValue(String::class.java).orEmpty()
                    val doc = snapshot.child("documento").getValue(String::class.java).orEmpty()
                    val razon = snapshot.child("razonSocial").getValue(String::class.java).orEmpty()
                    val correo = snapshot.child("correo").getValue(String::class.java).orEmpty()

                    rellenarCamposCliente(nombre, doc, razon, correo)
                } else {
                    clienteAutocompletadoActivo = false
                    limpiarSoloCamposAutocompletados()
                    ocultarChipClienteEncontradoInline()
                }
            }
            .addOnFailureListener {
                form?.inputDocumento?.setEndIconDrawable(R.drawable.searchicon)
                ocultarChipClienteEncontradoInline()
            }
    }




    private fun obtenerFormularioClienteActual(): ClienteFormRefs? {
        val b = _binding ?: return null
        return if (b.layoutComprobante.visibility == View.VISIBLE) {
            ClienteFormRefs(
                btnBoleta = b.btnBoleta,
                btnFactura = b.btnFactura,
                inputDocumento = b.inputLayoutDocumentoCliente,
                etDocumento = b.etDocumentoCliente,
                inputNombrePrincipal = b.inputLayoutNombreCliente,
                etNombrePrincipal = b.etNombreCliente,
                inputRazonSocial = b.inputLayoutRazonSocial,
                etRazonSocial = b.etRazonSocial,
                inputCorreo = b.inputLayoutCorreo,
                etCorreo = b.etCorreoCliente
            )
        } else {
            ClienteFormRefs(
                btnBoleta = b.btnBoletaInline,
                btnFactura = b.btnFacturaInline,
                inputDocumento = b.inputLayoutDocumentoClienteInline,
                etDocumento = b.etDocumentoClienteInline,
                inputNombrePrincipal = b.inputLayoutNombreClienteInline,
                etNombrePrincipal = b.etNombreClienteInline,
                inputRazonSocial = b.inputLayoutRazonSocialInline,
                etRazonSocial = b.etRazonSocialInline,
                inputCorreo = b.inputLayoutCorreoInline,
                etCorreo = b.etCorreoClienteInline
            )
        }
    }

    private fun cambiarTipoComprobante(tipo: String) {
        if (tipoComprobanteSeleccionado == tipo) return
        tipoComprobanteSeleccionado = tipo
        limpiarCamposNoCompatiblesPorTipoComprobante()
        actualizarUiTipoComprobante()

        if (tipo == "FACTURA") {
            formularioFacturaExpandido = true
            actualizarFormularioClienteInline()

            // CAMBIO: Solo enfocamos el documento si el cajero no estaba interactuando ya con el formulario
            val b = _binding
            val yaTieneFoco = b?.etDocumentoClienteInline?.hasFocus() == true ||
                    b?.etNombreClienteInline?.hasFocus() == true ||
                    b?.etRazonSocialInline?.hasFocus() == true

            actualizarFocoFormularioClienteInline(forzarEnfoque = !yaTieneFoco)
        }
    }

    private fun configurarComprobante() {
        val root = _binding?.root ?: return

        // Usamos una lista de botones para aplicar el mismo listener a todos los que existan (Mobile/Tablet/Inline)
        val idsBoleta = listOf(R.id.btnBoleta, R.id.btnBoletaInline)
        val idsFactura = listOf(R.id.btnFactura, R.id.btnFacturaInline)

        idsBoleta.forEach { id ->
            root.findViewById<MaterialButton>(id)?.setOnClickListener { cambiarTipoComprobante("BOLETA") }
        }

        idsFactura.forEach { id ->
            root.findViewById<MaterialButton>(id)?.setOnClickListener { cambiarTipoComprobante("FACTURA") }
        }

        // 2. Autocompletado y Limpieza de Errores al escribir
        configurarAutocompletadoDocumentoGlobal()

        val campos = listOf(
            R.id.etNombreCliente to R.id.inputLayoutNombreCliente,
            R.id.etDocumentoCliente to R.id.inputLayoutDocumentoCliente,
            R.id.etRazonSocial to R.id.inputLayoutRazonSocial,
            R.id.etCorreoCliente to R.id.inputLayoutCorreo,
            R.id.etNombreClienteInline to R.id.inputLayoutNombreClienteInline,
            R.id.etDocumentoClienteInline to R.id.inputLayoutDocumentoClienteInline
        )
        campos.forEach { (etId, layoutId) ->
            root.findViewById<TextInputEditText>(etId)?.doAfterTextChanged {
                root.findViewById<TextInputLayout>(layoutId)?.error = null
            }
        }

        // 3. Acciones de botones
        root.findViewById<MaterialButton>(R.id.btnEmitirComprobante)?.setOnClickListener {
            confirmarComprobanteDesdeUi() // Asegúrate de tener esta función para procesar la venta
        }

        root.findViewById<View>(R.id.btnCancelarVenta)?.setOnClickListener {
            feedbackVm.notificarAccionDestructiva(it)
            liberarCajaSiEstabaBloqueada()
            if (esTablet()) {
                aplicarLayoutInicialTablet()
            } else {
                ocultarLayoutComprobanteMobile()
            }
        }



        actualizarUiTipoComprobante()
        actualizarFormularioClienteInline()
    }



    private fun actualizarUiTipoComprobante() {
        val b = _binding ?: return

        val esFactura = tipoComprobanteSeleccionado == "FACTURA"

        // Visibilidad de campos (panel comprobante full screen movil)
        b.inputLayoutNombreCliente.visibility = if (esFactura) View.GONE else View.VISIBLE
        b.inputLayoutRazonSocial.visibility = if (esFactura) View.VISIBLE else View.GONE

        // Mini-form inline en tablet (sw720dp): los campos nombre/razon social estan siempre OCULTOS.
        // Solo se muestra DNI/RUC y el chip de "Cliente encontrado"; el resto se autocompleta invisible.
        // En movil (layout/) seguimos cambiando el hint del unico campo Nombre/Razon social visible.
        if (b.inputLayoutRazonSocialInline != null) {
            b.inputLayoutNombreClienteInline?.visibility = View.GONE
            b.inputLayoutRazonSocialInline?.visibility = View.GONE
        } else {
            b.inputLayoutNombreClienteInline?.hint = if (esFactura) "Razón social" else "Nombre completo"
        }
        b.tvDatosClienteInlineTitulo?.text = if (esFactura) "DATOS DE FACTURA" else "DATOS DEL CLIENTE"

        val colorSeleccionado = ColorStateList.valueOf(Color.parseColor("#111111"))
        val colorNoSeleccionado = ColorStateList.valueOf(Color.TRANSPARENT)
        val colorTextoSeleccionado = Color.WHITE
        val colorTextoNoSeleccionado = Color.parseColor("#5F6368")

        if (esFactura) {
            b.btnFactura.backgroundTintList = colorSeleccionado
            b.btnFactura.setTextColor(colorTextoSeleccionado)
            b.btnBoleta.backgroundTintList = colorNoSeleccionado
            b.btnBoleta.setTextColor(colorTextoNoSeleccionado)
            b.btnFacturaInline?.backgroundTintList = colorSeleccionado
            b.btnFacturaInline?.setTextColor(colorTextoSeleccionado)
            b.btnBoletaInline?.backgroundTintList = colorNoSeleccionado
            b.btnBoletaInline?.setTextColor(colorTextoNoSeleccionado)
        } else {
            b.btnBoleta.backgroundTintList = colorSeleccionado
            b.btnBoleta.setTextColor(colorTextoSeleccionado)
            b.btnFactura.backgroundTintList = colorNoSeleccionado
            b.btnFactura.setTextColor(colorTextoNoSeleccionado)
            b.btnBoletaInline?.backgroundTintList = colorSeleccionado
            b.btnBoletaInline?.setTextColor(colorTextoSeleccionado)
            b.btnFacturaInline?.backgroundTintList = colorNoSeleccionado
            b.btnFacturaInline?.setTextColor(colorTextoNoSeleccionado)
        }
    }

    private fun limpiarCamposNoCompatiblesPorTipoComprobante() {
        val b = _binding ?: return

        if (tipoComprobanteSeleccionado == "FACTURA") {
            b.etNombreCliente.setText("")
            nombreClienteVenta = ""
            b.inputLayoutNombreCliente.error = null
        } else {
            b.etRazonSocial.setText("")
            razonSocialClienteVenta = ""
            b.inputLayoutRazonSocial.error = null
        }
        b.inputLayoutNombreClienteInline?.error = null
    }


        fun validarFormularioComprobante(): Boolean {
        val form = obtenerFormularioClienteActual() ?: return false
        val doc = form.etDocumento?.text.toString().trim()
        val nombrePrincipal = form.etNombrePrincipal?.text.toString().trim()
        val razon = form.etRazonSocial?.text.toString().trim()
        val nombreORazon = if (tipoComprobanteSeleccionado == "FACTURA") {
            razon.ifBlank { nombrePrincipal }
        } else {
            nombrePrincipal
        }
        var valido = true
        if (tipoComprobanteSeleccionado == "FACTURA") {
            if (doc.length != 11 || (!doc.startsWith("10") && !doc.startsWith("20"))) {
                form.inputDocumento?.error = "RUC inv\u00e1lido (11 d\u00edgitos, inicia con 10 o 20)"
                valido = false
            }
            if (nombreORazon.isEmpty()) {
                val layoutError = form.inputRazonSocial ?: form.inputNombrePrincipal
                layoutError?.error = "Raz\u00f3n social obligatoria"
                valido = false
            }
        } else {
            val tieneDoc = doc.isNotEmpty()
            val tieneNombre = nombreORazon.isNotEmpty()
            if (tieneDoc || tieneNombre) {
                if (!tieneDoc) {
                    form.inputDocumento?.error = "Ingrese el DNI o RUC"
                    valido = false
                } else if (doc.length != 8 && doc.length != 11) {
                    form.inputDocumento?.error = "Documento debe tener 8 u 11 d\u00edgitos"
                    valido = false
                }
                if (!tieneNombre) {
                    form.inputNombrePrincipal?.error = "Ingrese el nombre del cliente"
                    valido = false
                }
            }
        }
        if (!valido) {
            val anchorError = form.inputDocumento ?: form.inputNombrePrincipal ?: form.inputRazonSocial ?: _binding?.root
            anchorError?.let { feedbackVm.notificarError(it) }
        }
        if (valido) {
            documentoClienteVenta = doc
            if (tipoComprobanteSeleccionado == "FACTURA") {
                nombreClienteVenta = ""
                razonSocialClienteVenta = nombreORazon
            } else {
                nombreClienteVenta = nombreORazon
                razonSocialClienteVenta = ""
            }
            correoClienteVenta = ""
        }
        return valido
    }

    private fun confirmarComprobanteDesdeUi() {
        if (!puedeOperarTurnoActual()) return

        // === SOLUCIÓN CRÍTICA: Si soy yo el que cobra, ignoramos el bloqueo para poder pasar ===
        if ((ventaEnProceso && !soyYoElQueCobra) || actualizandoCaja || validandoStockFinalEnCurso) return

        if (!turnoAbierto || idTurnoActivo.isBlank()) {
            feedbackVm.notificarError(_binding?.buttoncobrar ?: _binding?.root)
            mostrarToastSeguro("Primero debes abrir un turno de caja")
            _binding?.layoutAperturaCaja?.visibility = View.VISIBLE
            return
        }
        if (listaProductosCaja.isEmpty()) {
            feedbackVm.notificarError(_binding?.buttoncobrar ?: _binding?.root)
            mostrarToastSeguro("No hay productos en la caja")
            return
        }

        val usaBoletaSimpleInline =
            _binding?.layoutComprobante?.visibility != View.VISIBLE && !formularioFacturaExpandido
        if (usaBoletaSimpleInline) {
            tipoComprobanteSeleccionado = "BOLETA"
            limpiarDatosClienteSeleccionados()
        } else if (!validarFormularioComprobante()) {
            actualizarEstadoVentaEnProceso(false)
            liberarCajaSiEstabaBloqueada()
            return
        }

        var total = 0.0
        listaProductosCaja.forEach { prod ->
            total = CalculadoraVentasHelper.redondear(total + (prod.total.toDoubleSeguro(0.0) ?: 0.0))
        }

        when {
            total < 0.0 -> {
                feedbackVm.notificarError(_binding?.buttoncobrar ?: _binding?.root)
                mostrarToastSeguro("Error: El total no puede ser negativo")
                actualizarEstadoVentaEnProceso(false)
                liberarCajaSiEstabaBloqueada()
            }
            total == 0.0 -> {
                val operationId = generarOperationIdVenta()

                mostrarDialogoProgresoVenta(
                    titulo = "Venta de Promoción",
                    mensaje = "Registrando cortesía y actualizando inventario..."
                )

                confirmarVenta(
                    totalPagar = 0.0,
                    metodoPago = "Promoción",
                    montoRecibido = 0.0,
                    detalleMetodosTurno = mapOf("Promoción" to 0.0),
                    operationId = operationId,
                    dialog = null,
                    onFinish = {
                        ocultarDialogoProgresoVenta()
                        soyYoElQueCobra = false
                        actualizarEstadoVentaEnProceso(false)
                        mostrarAnimacionVentaExitosa()
                    }
                )
            }
            else -> {
                // === SOLUCIÓN CRÍTICA 2: Apagamos la carga para que el flujo de pago sea interactivo ===
                actualizarEstadoVentaEnProceso(false)
                iniciarFlujoCobroInline(total)
            }
        }
    }

        fun rellenarCamposCliente(nombre: String, documento: String, razon: String, correo: String) {
        val form = obtenerFormularioClienteActual()
        val nombrePrincipal = if (tipoComprobanteSeleccionado == "FACTURA") {
            razon.ifBlank { nombre }
        } else {
            nombre.ifBlank { razon }
        }
        form?.etDocumento?.setText(documento)
        form?.etNombrePrincipal?.setText(nombrePrincipal)
        form?.etRazonSocial?.setText(razon)
        form?.etCorreo?.setText(correo)
        form?.inputDocumento?.error = null
        form?.inputNombrePrincipal?.error = null
        form?.inputRazonSocial?.error = null
        form?.inputCorreo?.error = null
        clienteAutocompletadoActivo = true
        mostrarChipClienteEncontradoInline(nombrePrincipal)
    }

    private fun mostrarChipClienteEncontradoInline(nombreMostrar: String) {
        val b = _binding ?: return
        val chip = b.cardClienteEncontradoInline ?: return
        val tv = b.tvClienteEncontradoInline ?: return
        val texto = nombreMostrar.trim()
        if (texto.isEmpty()) {
            chip.visibility = View.GONE
            return
        }
        tv.text = texto
        chip.visibility = View.VISIBLE
    }

    private fun ocultarChipClienteEncontradoInline() {
        _binding?.cardClienteEncontradoInline?.visibility = View.GONE
    }


        private fun limpiarSoloCamposAutocompletados() {
        val form = obtenerFormularioClienteActual()
        form?.etNombrePrincipal?.setText("")
        form?.etRazonSocial?.setText("")
        form?.etCorreo?.setText("")
        form?.inputNombrePrincipal?.error = null
        form?.inputRazonSocial?.error = null
        form?.inputCorreo?.error = null
    }
        private fun limpiarFormularioComprobanteGlobal() {
        val b = _binding ?: return
        listOf(
            b.etNombreCliente,
            b.etDocumentoCliente,
            b.etRazonSocial,
            b.etCorreoCliente,
            b.etNombreClienteInline,
            b.etDocumentoClienteInline,
            b.etRazonSocialInline,
            b.etCorreoClienteInline
        ).forEach { it?.setText("") }
        listOf(
            b.inputLayoutNombreCliente,
            b.inputLayoutDocumentoCliente,
            b.inputLayoutRazonSocial,
            b.inputLayoutCorreo,
            b.inputLayoutNombreClienteInline,
            b.inputLayoutDocumentoClienteInline,
            b.inputLayoutRazonSocialInline,
            b.inputLayoutCorreoInline
        ).forEach { it?.error = null }
        limpiarDatosClienteSeleccionados()
        ultimoDocumentoBuscado = ""
        clienteAutocompletadoActivo = false
        tipoComprobanteSeleccionado = "BOLETA"
        formularioFacturaExpandido = false
        runnableBusquedaDocumento?.let { handlerBusquedaDocumento.removeCallbacks(it) }
        actualizarUiTipoComprobante()
        actualizarFormularioClienteInline()
        ocultarChipClienteEncontradoInline()
        ocultarTeclado()
    }


    private fun ocultarTeclado(targetView: View? = null) {
        val view = targetView ?: activity?.currentFocus ?: _binding?.root ?: return
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
        ViewCompat.getWindowInsetsController(view)?.hide(WindowInsetsCompat.Type.ime())
    }

    private fun configurarOcultarTecladoEnDialogo(
        root: View,
        vararg inputs: EditText?
    ) {
        val campos = inputs.filterNotNull()
        if (campos.isEmpty()) return

        configurarOcultarTecladoAlTocarFuera(root, campos)
        configurarOcultarTecladoConEnter(*campos.toTypedArray())
    }

    private fun configurarOcultarTecladoAlTocarFuera(
        view: View,
        campos: List<EditText>
    ) {
        if (view is TextInputEditText || view is EditText) return

        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val algunCampoConFoco = campos.any { it.hasFocus() }
                if (algunCampoConFoco) {
                    campos.forEach { it.clearFocus() }
                    ocultarTeclado()
                }
            }
            false
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                configurarOcultarTecladoAlTocarFuera(view.getChildAt(i), campos)
            }
        }
    }

    private fun configurarOcultarTecladoConEnter(vararg campos: EditText) {
        campos.forEach { campo ->
            campo.setOnEditorActionListener { v, actionId, event ->
                val accionIme = actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_GO ||
                    actionId == EditorInfo.IME_ACTION_NEXT ||
                    actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_SEND
                val enterFisico = event?.keyCode == KeyEvent.KEYCODE_ENTER &&
                    event.action == KeyEvent.ACTION_DOWN

                if (accionIme || enterFisico) {
                    v.clearFocus()
                    ocultarTeclado()
                    true
                } else {
                    false
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // RADAR DE STOCK EN TIEMPO REAL
    // -------------------------------------------------------------------------

    /**
     * Activa un escucha en tiempo real para el stock de un producto específico.
     */
    private fun activarRadarStock(indiceProducto: String) {
        val clave = indiceProducto.trim()
        if (clave.isEmpty() || listenersStockProductos.containsKey(clave)) return

        val ref = database.getReference("Inventario/Productos").child(clave)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val stockReal = snapshot.child("cantidadinicial").value.toString().toIntOrNull() ?: 0
                val stockAnterior = cacheStockInventario[clave]

                // --- CORRECCIÓN: Radar de Precios Blindado ---
                val mapaPrecios = mutableMapOf<String, Double>()
                val unidadBase = snapshot.child("unidadbase").value?.toString()?.trim().orEmpty().ifBlank { "unidades" }

                // 1. Capturar el precio de la presentación principal (base)
                val nombrePrincipal = snapshot.child("presentacionprincipal").value?.toString()?.trim().orEmpty()
                val precioBase = snapshot.child("preciodecompra").value?.toString()?.toDoubleOrNull() ?: 0.0
                if (nombrePrincipal.isNotBlank() && precioBase > 0.0) {
                    val presentacionBaseFormateada = PresentacionHelper.resumenPresentacionUi(
                        PresentacionesTiendaConfigManager.nombreVisible(nombrePrincipal),
                        1, // La presentación principal asume 1 unidad
                        unidadBase
                    )
                    mapaPrecios[presentacionBaseFormateada] = precioBase
                }

                // 2. Capturar el array de presentaciones adicionales
                snapshot.child("presentaciones").children.forEach { presSnap ->
                    val nombre = presSnap.child("nombre").value?.toString()?.trim().orEmpty()
                    val cantidadUnidades = presSnap.child("cantidad").value?.toString()?.toIntOrNull() ?: 1
                    val precio = presSnap.child("precioventa").value?.toString()?.toDoubleOrNull() ?: 0.0

                    if (nombre.isNotBlank()) {
                        val presentacionFormateada = PresentacionHelper.resumenPresentacionUi(
                            PresentacionesTiendaConfigManager.nombreVisible(nombre),
                            cantidadUnidades,
                            unidadBase
                        )
                        mapaPrecios[presentacionFormateada] = precio
                    }
                }
                cachePreciosInventario[clave] = mapaPrecios
                // ----------------------------------------------

                // Solo procedemos si el stock en el servidor realmente cambió o si es la primera carga
                if (stockAnterior != stockReal || stockAnterior == null) {
                    cacheStockInventario[clave] = stockReal

                    if (isAdded && _binding != null) {
                        // Delegamos la actualización visual y lógica a la validación centralizada
                        verificarDisponibilidadProductosEnCaja(mostrarAvisoCambioExterno = stockAnterior != null)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }

        listenersStockProductos[clave] = listener
        ref.addValueEventListener(listener)
    }

    /**
     * Detiene todos los radares de stock para liberar memoria.
     */
    private fun detenerRadaresStock() {
        listenersStockProductos.forEach { (clave, listener) ->
            database.getReference("Inventario/Productos").child(clave).removeEventListener(listener)
        }
        listenersStockProductos.clear()
        cacheStockInventario.clear()
    }

    private fun verificarHoraAntesDeOperar(accion: String): Boolean {
        if (relojDesincronizado) {
            if (!isAdded || context == null) return false

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("⚠️ Operación Bloqueada")
                .setMessage("No puedes $accion porque la hora de tu dispositivo está desincronizada de la hora oficial de internet.\n\nPor favor, corrige tu reloj (activa la 'Hora automática') para continuar.")
                .setCancelable(false)
                .setNegativeButton("Cerrar", null)
                .setPositiveButton("Ir a Ajustes") { _, _ ->
                    try {
                        startActivity(Intent(android.provider.Settings.ACTION_DATE_SETTINGS))
                    } catch (e: Exception) {
                        mostrarToastSeguro("Abre los Ajustes de Android y corrige la hora.")
                    }
                }
                .show()
            return false
        }
        return true
    }
}


