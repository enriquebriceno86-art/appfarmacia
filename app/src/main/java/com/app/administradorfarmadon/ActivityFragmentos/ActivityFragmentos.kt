package com.app.administradorfarmadon.ActivityFragmentos

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import com.app.administradorfarmadon.ActivityFragmentos.Fragmentos.FragmentoInventario
import com.app.administradorfarmadon.ActivityFragmentos.Fragmentos.FragmentoPerfil
import com.app.administradorfarmadon.ActivityFragmentos.Fragmentos.FragmentoPrincipal
import com.app.administradorfarmadon.ActivityFragmentos.Fragmentos.FragmentosPedidos
import com.app.administradorfarmadon.ActivitysPerfilItem.DatosdelaTienda
import com.app.administradorfarmadon.ActivitysPerfilItem.HorariosActivity
import com.app.administradorfarmadon.ActivitysPerfilItem.ListaReportesCaja
import com.app.administradorfarmadon.ClasesDatabase.AccesoFieldRules
import com.app.administradorfarmadon.ClasesDatabase.MovimientoLogger
import com.app.administradorfarmadon.ClasesDatabase.SessionManager
import com.app.administradorfarmadon.ClasesDatabase.SesionUnicaManager
import com.app.administradorfarmadon.MainActivity
import com.app.administradorfarmadon.R
import com.app.administradorfarmadon.databinding.ActivityFragmentosBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.activity.viewModels
import com.app.administradorfarmadon.ActivityFragmentos.Fragmentos.FragmentoCaja
import com.app.administradorfarmadon.ActivityFragmentos.ViewModels.CajaFeedbackViewModel

class ActivityFragmentos : AppCompatActivity() {

    companion object {
        const val EXTRA_DESTINO_INICIAL = "extra_destino_inicial"
    }

    private var _binding: ActivityFragmentosBinding? = null
    private val binding get() = _binding!!
    private val database by lazy { FirebaseDatabase.getInstance() }
    private val ordenDiasSemana = listOf(
        "lunes",
        "martes",
        "miercoles",
        "jueves",
        "viernes",
        "sabado",
        "domingo"
    )

    private val fragmentoHome = FragmentoPrincipal()
    private var firebaseConnectionListener: ValueEventListener? = null
    private val fragmentoCaja = FragmentoCaja()
    private val fragmentoPedidos = FragmentosPedidos()
    private val fragmentoInventario = FragmentoInventario()
    private val fragmentoPerfil = FragmentoPerfil()

    private val feedbackVm: CajaFeedbackViewModel by viewModels()
    private var fragmentoActivo: Fragment = fragmentoHome
    private var bloqueadoPorConfigTienda = false
    private var bloqueadoPorHorario = false
    private var bloqueadoPorUsuarioInactivo = false
    private var verificacionConfigEnCurso = false
    private var sesionActivaRefListener: DatabaseReference? = null
    private var sesionActivaValueListener: ValueEventListener? = null
    private var usuarioEstadoRef: DatabaseReference? = null
    private var usuarioEstadoListener: ValueEventListener? = null
    private var horarioDiaRef: DatabaseReference? = null
    private var horarioDiaListener: ValueEventListener? = null
    private var diaHorarioEscuchado: String = ""
    private var cierreSesionForzadaMostrado = false
    private var sesionReemplazadaPendiente: SesionUnicaManager.SesionRemotaInfo? = null
    private var movimientoSesionReemplazadaRegistrado = false
    private var sinConexion = false
    private var firebaseOffline = false
    private var monitoreoConexionRegistrado = false
    private var sesionUnicaInicializada = false
    private var sesionUnicaEnProceso = false
    private var intentoSesionUnica = 0L
    private var estaAppEnPrimerPlano = false
    private var ultimoBackParaSalir = 0L
    private var navegacionManualOculta = false
    private val ventanaBackParaSalirMs = 2000L
    private val connectivityManager by lazy {
        getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            runOnUiThread { actualizarEstadoConexion() }
        }

        override fun onLost(network: Network) {
            runOnUiThread { actualizarEstadoConexion() }
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            runOnUiThread { actualizarEstadoConexion() }
        }

        override fun onUnavailable() {
            runOnUiThread { actualizarEstadoConexion() }
        }
    }

    private val abrirDatosTiendaLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            verificarConfiguracionTiendaYBloquearSiFalta()
        }

    private fun resolverAccesoUiModel(): AppAccesoUiBuilder.AppAccesoUiModel {
        return AppAccesoUiBuilder.construirEstadoAcceso(
            sinConexion = sinConexion,
            bloqueadoPorUsuarioInactivo = bloqueadoPorUsuarioInactivo,
            bloqueadoPorConfigTienda = bloqueadoPorConfigTienda,
            bloqueadoPorHorario = bloqueadoPorHorario
        )
    }

    private fun aplicarAccesoUiModel(uiModel: AppAccesoUiBuilder.AppAccesoUiModel) {
        val scene = AppBloqueoOverlayCoordinator.coordinarEscena(uiModel)
        aplicarUiBloqueos(scene)
        aplicarContenidoPrincipal(scene)
    }

    private fun reevaluarAccesoUi() {
        aplicarAccesoUiModel(resolverAccesoUiModel())
    }

    private fun aplicarUiBloqueos(
        scene: AppBloqueoOverlayCoordinator.AppBloqueoOverlayScene
    ) {
        val b = _binding ?: return
        
        val visibilidadActualSinConexion = b.layoutBloqueoSinConexion.visibility
        val nuevaVisibilidadSinConexion = if (scene.mostrarOverlaySinConexion) View.VISIBLE else View.GONE
        
        if (nuevaVisibilidadSinConexion == View.VISIBLE && visibilidadActualSinConexion != View.VISIBLE) {
            b.layoutBloqueoSinConexion.showElegantemente()
            val anim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.anim_bloqueo_entrada)
            b.cardBloqueoSinConexion?.startAnimation(anim)
        } else if (nuevaVisibilidadSinConexion == View.GONE && visibilidadActualSinConexion == View.VISIBLE) {
            // Animación de salida elegante
            val animSalida = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.anim_bloqueo_salida)
            b.cardBloqueoSinConexion?.startAnimation(animSalida)
            b.layoutBloqueoSinConexion.hideElegantemente()
        } else if (visibilidadActualSinConexion != nuevaVisibilidadSinConexion) {
            b.layoutBloqueoSinConexion.visibility = nuevaVisibilidadSinConexion
        }

        val vUsuarioInactivo = if (scene.mostrarOverlayUsuarioInactivo) View.VISIBLE else View.GONE
        if (b.layoutBloqueoUsuarioInactivo.visibility != vUsuarioInactivo) {
            if (vUsuarioInactivo == View.VISIBLE) {
                b.layoutBloqueoUsuarioInactivo.showElegantemente()
            } else {
                b.layoutBloqueoUsuarioInactivo.hideElegantemente()
            }
        }

        val vConfigTienda = if (scene.mostrarOverlayConfigTienda) View.VISIBLE else View.GONE
        if (b.layoutBloqueoConfigTienda.visibility != vConfigTienda) {
            if (vConfigTienda == View.VISIBLE) {
                b.layoutBloqueoConfigTienda.showElegantemente()
            } else {
                b.layoutBloqueoConfigTienda.hideElegantemente()
            }
        }

        val vHorario = if (scene.mostrarOverlayHorario) View.VISIBLE else View.GONE
        if (b.layoutBloqueoTiendaCerrada.visibility != vHorario) {
            if (vHorario == View.VISIBLE) {
                b.layoutBloqueoTiendaCerrada.showElegantemente()
            } else {
                b.layoutBloqueoTiendaCerrada.hideElegantemente()
            }
        }
    }

    private fun aplicarContenidoPrincipal(
        scene: AppBloqueoOverlayCoordinator.AppBloqueoOverlayScene
    ) {
        val b = _binding ?: return
        val visibilidadPrincipal = if (scene.mostrarContenidoPrincipal) View.VISIBLE else View.GONE
        
        if (b.fragmentocontainer.visibility != visibilidadPrincipal) {
            b.fragmentocontainer.visibility = visibilidadPrincipal
        }
        
        // Bloque de navegacion con "seguro" para la busqueda
        val visibilidadNav = if (scene.mostrarContenidoPrincipal && !navegacionManualOculta) {
            View.VISIBLE
        } else {
            View.GONE
        }
        
        if (b.botonnavigation?.visibility != visibilidadNav) {
            b.botonnavigation?.visibility = visibilidadNav
        }
        if (b.navigationRail?.visibility != visibilidadNav) {
            b.navigationRail?.visibility = visibilidadNav
        }
        val sidebar = b.root.findViewById<View>(R.id.sidebarTablet)
        if (sidebar?.visibility != visibilidadNav) {
            sidebar?.visibility = visibilidadNav
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashScreen = installSplashScreen()
        var keepSplash = true
        Handler(Looper.getMainLooper()).postDelayed({ keepSplash = false }, 1000)
        splashScreen.setKeepOnScreenCondition { keepSplash }

        enableEdgeToEdge()

        _binding = ActivityFragmentosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SessionManager.cargarSesion(this)

        val railChromeColor = Color.parseColor("#071F1C")
        window.statusBarColor = Color.WHITE
        window.navigationBarColor = railChromeColor
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = false
        }



        // Edge-to-edge: el sistema reporta los insets de status/nav bars y nosotros los aplicamos
        // como padding para que NINGUN layout tenga que poner padding manual para respetarlas.
        // - Top: siempre va al root (status bar).
        // - Bottom: si hay bottom nav visible (movil), lo recibe el bottom nav como padding interno;
        //   si no (tablet, IME abierto), lo recibe el root.
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val esTablet = resources.configuration.smallestScreenWidthDp >= 720
            val bottomNav = binding.botonnavigation
            val bottomScrim = binding.systemBarBottomScrim
            val bottomNavVisible = bottomNav != null && !esTablet && !imeVisible

            // Bottom inset = max(barra de navegacion, teclado). Si el teclado esta abierto,
            // su altura es mayor; le damos ese padding al root para que el form se "suba"
            // y el campo enfocado quede visible sobre el teclado.
            val bottomInset = if (imeVisible) ime.bottom else sb.bottom

            v.setPadding(sb.left, sb.top, sb.right, if (bottomNavVisible) 0 else bottomInset)

            if (bottomNav != null) {
                bottomNav.updatePadding(bottom = if (bottomNavVisible) sb.bottom else 0)
                if (!esTablet) {
                    bottomNav.visibility = if (imeVisible || navegacionManualOculta) View.GONE else View.VISIBLE
                }
            }
            bottomScrim.updateLayoutParams<ViewGroup.LayoutParams> {
                height = if (!bottomNavVisible && !imeVisible) sb.bottom else 0
            }
            bottomScrim.visibility =
                if (!bottomNavVisible && !imeVisible && sb.bottom > 0) View.VISIBLE else View.GONE
            insets
        }

        setupNavigationManualmente()
        configurarMenuSegunRol()
        navegarADestinoInicial()
        configurarBloqueoDatosTienda()
        configurarBloqueoSinConexion()
        configurarBloqueoTiendaCerrada()
        configurarBloqueoUsuarioInactivo()
        iniciarEscuchaEstadoAccesoUsuario()
        iniciarMonitoreoConexion()
        iniciarMonitoreoConexionUltraRapido()
        configurarBackNavigation()


        if (!sinConexion) {
            verificarConfiguracionTiendaYBloquearSiFalta()
            iniciarEscuchaHorarioDelDiaActual()
        }

    }



    override fun onResume() {
        super.onResume()
        estaAppEnPrimerPlano = true
        reevaluarAccesoGlobal()
        
        // Fase 3: Si regresamos y ya estamos desconectados, reforzamos visualmente el bloqueo
        if (sinConexion) {
            val b = _binding ?: return
            if (b.layoutBloqueoSinConexion.visibility == View.VISIBLE) {
                val anim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.anim_bloqueo_entrada)
                b.cardBloqueoSinConexion?.startAnimation(anim)
            }
        }

        sesionReemplazadaPendiente?.let {
            sesionReemplazadaPendiente = null
            mostrarDialogoSesionReemplazada(it)
        }
    }

    override fun onPause() {
        super.onPause()
        estaAppEnPrimerPlano = false
    }

    override fun onStart() {
        super.onStart()
        val idUsuario = SessionManager.idCajera.trim()
        if (idUsuario.isNotBlank() && !sinConexion) {
            val esLoginFresco = intent.getBooleanExtra("esLoginFresco", false)
            
            if (esLoginFresco && !sesionUnicaInicializada) {
                // CONSUMIMOS EL FLAG: Evita que al recrear la actividad (o volver a ella) 
                // se intente reclamar una sesión nueva y se auto-expulse.
                intent.putExtra("esLoginFresco", false)
                activarSesionUnicaEstrica()
            } else if (!sesionUnicaInicializada) {
                // Si no es login fresco, solo escuchamos por si alguien nos saca.
                activarSesionUnicaEstrica()
            }
        }
    }

    override fun onStop() {
        // Mantenemos el heartbeat incluso en background para evitar que otro dispositivo entre
        // El onDisconnect se encargará si la app se mata realmente.
        // SesionUnicaManager.detenerHeartbeat()
        super.onStop()
    }

    override fun onDestroy() {
        SesionUnicaManager.detenerHeartbeat()
        detenerEscuchaSesionActiva()
        detenerEscuchaEstadoAccesoUsuario()
        detenerEscuchaHorarioDelDiaActual()
        detenerMonitoreoConexion()
        _binding = null
        super.onDestroy()
    }

    private fun configurarBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (bloqueadoPorConfigTienda || bloqueadoPorHorario || bloqueadoPorUsuarioInactivo || sinConexion) {
                    finish()
                    return
                }

                if (fragmentoActivo == fragmentoCaja && fragmentoCaja.manejarRetrocesoInterno()) {
                    return
                }

                if (
                    fragmentoActivo == fragmentoCaja &&
                    fragmentoCaja.prepararSalidaDeSupervisionSiHaceFalta(
                        saliendoDeLaApp = true
                    ) {
                        finishAffinity()
                    }
                ) {
                    return
                }

                val ahora = System.currentTimeMillis()
                if (ahora - ultimoBackParaSalir <= ventanaBackParaSalirMs) {
                    finishAffinity()
                    return
                }

                ultimoBackParaSalir = ahora
                Toast.makeText(
                    this@ActivityFragmentos,
                    "Presiona otra vez para salir",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun configurarMenuSegunRol() {
        val prefs = getSharedPreferences("usuario", MODE_PRIVATE)
        val rol = prefs.getString("rol", "") ?: ""
        val esTablet = resources.configuration.smallestScreenWidthDp >= 720

        if (!esTablet) {
            binding.botonnavigation?.menu?.findItem(R.id.nav_pedidos)?.isVisible = false
            binding.botonnavigation?.menu?.findItem(R.id.nav_reportes)?.isVisible = false
        }

        if (rol == "Caja") {
            binding.botonnavigation?.menu?.findItem(R.id.nav_pedidos)?.isVisible = false
            binding.botonnavigation?.menu?.findItem(R.id.nav_productos)?.isVisible = false

            binding.navigationRail?.menu?.findItem(R.id.nav_pedidos)?.isVisible = false
            binding.navigationRail?.menu?.findItem(R.id.nav_productos)?.isVisible = false
        }
    }

    private fun setupNavigationManualmente() {
        val fm = supportFragmentManager

        fm.beginTransaction()
            .add(R.id.fragmentocontainer, fragmentoHome, "1")
            .add(R.id.fragmentocontainer, fragmentoCaja, "2")
            .hide(fragmentoCaja)
            .add(R.id.fragmentocontainer, fragmentoPedidos, "3")
            .hide(fragmentoPedidos)
            .add(R.id.fragmentocontainer, fragmentoInventario, "4")
            .hide(fragmentoInventario)
            .add(R.id.fragmentocontainer, fragmentoPerfil, "5")
            .hide(fragmentoPerfil)
            .commit()

        binding.botonnavigation?.setOnItemSelectedListener { item ->
            manejarSeleccionNavegacion(item.itemId)
        }

        binding.navigationRail?.setOnItemSelectedListener { item ->
            manejarSeleccionNavegacion(item.itemId)
        }
    }

    /**
     * Oculta o muestra la navegación inferior con una animación de deslizamiento.
     * Evita el "layout jumping" al no remover la vista abruptamente.
     */
    fun setBottomNavigationVisibility(visible: Boolean) {
        val nav = binding.botonnavigation ?: return
        val root = binding.main

        // Usamos TransitionManager para que el cambio de visibilidad sea fluido
        // y el contenedor de fragmentos se expanda/contraiga sin saltos bruscos.
        TransitionManager.beginDelayedTransition(root, Slide(Gravity.BOTTOM).setDuration(300))

        nav.visibility = if (visible) View.VISIBLE else View.GONE
        navegacionManualOculta = !visible // Sincroniza con el listener de insets para evitar conflictos
    }

    private fun manejarSeleccionNavegacion(itemId: Int): Boolean {
        if (itemId == R.id.nav_reportes) {
            return abrirReportesConSalidaSegura()
        }

        if (itemId == R.id.nav_cajaventas) {
            seleccionarFragmento(itemId)
            return true
        }

        if (
            fragmentoActivo == fragmentoCaja &&
            fragmentoCaja.prepararSalidaDeSupervisionSiHaceFalta {
                seleccionarFragmento(itemId)
                marcarDestinoNavegacion(itemId)
            }
        ) {
            sincronizarSeleccionNavegacionActual()
            return false
        }

        seleccionarFragmento(itemId)
        return true
    }

    private fun abrirReportesConSalidaSegura(): Boolean {
        if (
            fragmentoActivo == fragmentoCaja &&
            fragmentoCaja.prepararSalidaDeSupervisionSiHaceFalta {
                abrirReportes()
            }
        ) {
            sincronizarSeleccionNavegacionActual()
            return false
        }

        abrirReportes()
        sincronizarSeleccionNavegacionActual()
        return false
    }

    private fun abrirReportes() {
        val accesoUi = resolverAccesoUiModel()
        if (accesoUi.bloquearNavegacion) return
        startActivity(Intent(this, ListaReportesCaja::class.java))
    }

    private fun navegarADestinoInicial() {
        val destino = intent.getIntExtra(EXTRA_DESTINO_INICIAL, View.NO_ID)
        if (destino == View.NO_ID || destino == R.id.nav_reportes) return
        if (!puedeAbrirDestinoInicial(destino)) return

        seleccionarFragmento(destino)
        binding.botonnavigation?.selectedItemId = destino
        binding.navigationRail?.selectedItemId = destino
    }

    private fun puedeAbrirDestinoInicial(destino: Int): Boolean {
        val rol = getSharedPreferences("usuario", MODE_PRIVATE)
            .getString("rol", "")
            .orEmpty()

        if (rol != "Caja") return true

        return destino != R.id.nav_productos && destino != R.id.nav_pedidos
    }

    private fun configurarBloqueoDatosTienda() {
        binding.btnAbrirDatosTienda.setOnClickListener {
            abrirDatosTiendaLauncher.launch(Intent(this, DatosdelaTienda::class.java))
        }
    }

    private fun configurarBloqueoSinConexion() {
        binding.btnReintentarConexion.setOnClickListener {
            actualizarEstadoConexion(forzarToast = true)
        }
    }

    private fun configurarBloqueoTiendaCerrada() {
        val mostrarAccionHorarios = usuarioPuedeGestionarHorarios()
        binding.btnIrHorariosTiendaCerrada.visibility = if (mostrarAccionHorarios) {
            View.VISIBLE
        } else {
            View.GONE
        }

        binding.btnIrHorariosTiendaCerrada.setOnClickListener {
            startActivity(Intent(this, HorariosActivity::class.java))
        }
    }

    private fun configurarBloqueoUsuarioInactivo() {
        binding.btnIrLoginUsuarioInactivo.setOnClickListener {
            cerrarSesionLocalYIrLogin()
        }
    }

    private fun iniciarEscuchaEstadoAccesoUsuario() {
        val idUsuario = SessionManager.idCajera.trim()
        if (
            !AppAccesoReevaluationRules.debeIniciarEscuchaEstadoUsuario(
                idUsuario = idUsuario,
                usuarioEstadoRefKey = usuarioEstadoRef?.key,
                existeListener = usuarioEstadoListener != null
            )
        ) {
            return
        }

        detenerEscuchaEstadoAccesoUsuario()

        val ref = database.getReference("trabajadores").child(idUsuario)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return

                val accesoActivo = AccesoFieldRules.parseAccesoDesdeSnapshot(
                    snapshot = snapshot,
                    defaultValue = true
                )
                if (accesoActivo) {
                    aplicarEstadoUsuarioActivo()
                    return
                }

                val bloqueadoPor = snapshot.child("estadoAccesoActualizadoPorNombre")
                    .getValue(String::class.java)
                    .orEmpty()
                    .trim()
                    .ifBlank { "Administrador" }

                val bloqueadoEn = snapshot.child("estadoAccesoActualizadoEnServidor").parseLongSeguro()
                    .takeIf { it > 0L }
                    ?: snapshot.child("estadoAccesoActualizadoEn").parseLongSeguro()

                mostrarBloqueoUsuarioInactivo(
                    mostrar = true,
                    bloqueadoPorNombre = bloqueadoPor,
                    bloqueadoEn = bloqueadoEn
                )
            }

            override fun onCancelled(error: DatabaseError) {
                // Si falla la lectura, no alteramos el estado actual para evitar cortes falsos.
            }
        }

        usuarioEstadoRef = ref
        usuarioEstadoListener = listener
        ref.addValueEventListener(listener)
    }

    private fun aplicarEstadoUsuarioActivo() {
        bloqueadoPorUsuarioInactivo = false
        actualizarVisibilidadBloqueos()
    }

    private fun detenerEscuchaEstadoAccesoUsuario() {
        usuarioEstadoRef?.let { ref ->
            usuarioEstadoListener?.let { listener ->
                ref.removeEventListener(listener)
            }
        }
        usuarioEstadoRef = null
        usuarioEstadoListener = null
    }

    private fun mostrarBloqueoUsuarioInactivo(
        mostrar: Boolean,
        bloqueadoPorNombre: String = "",
        bloqueadoEn: Long = 0L
    ) {
        bloqueadoPorUsuarioInactivo = mostrar
        if (mostrar) {
            SesionUnicaManager.detenerHeartbeat()
            val b = _binding ?: return
            b.tvMensajeBloqueoUsuarioInactivo.text = construirMensajeBloqueoUsuarioInactivo(
                bloqueadoPorNombre = bloqueadoPorNombre,
                bloqueadoEn = bloqueadoEn
            )
        }
        actualizarVisibilidadBloqueos()
    }

    private fun construirMensajeBloqueoUsuarioInactivo(
        bloqueadoPorNombre: String,
        bloqueadoEn: Long
    ): String {
        val quien = bloqueadoPorNombre.trim().ifBlank { "Administrador" }
        val hora = if (bloqueadoEn > 0L) formatearHora(bloqueadoEn) else ""
        return if (hora.isBlank()) {
            "Tu acceso fue desactivado por $quien.\nPor seguridad debes volver al login."
        } else {
            "Tu acceso fue desactivado por $quien a las $hora.\nPor seguridad debes volver al login."
        }
    }

    private fun iniciarMonitoreoConexionUltraRapido() {
        val conRef = FirebaseDatabase.getInstance().getReference(".info/connected")
        firebaseConnectionListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val conectado = snapshot.getValue(Boolean::class.java) ?: true
                firebaseOffline = !conectado
                
                if (!conectado) {
                    // DISPARO DIRECTO: Solo hacemos ruido si el usuario esta mirando la app
                    if (!sinConexion && estaAppEnPrimerPlano) {
                        feedbackVm.notificarFalloInternet(null)
                    }
                    runOnUiThread { actualizarEstadoConexion() }
                } else {
                    // RECUPERACION: Solo hacemos ruido bonito si el usuario esta mirando la app
                    if (sinConexion) {
                        if (estaAppEnPrimerPlano) {
                            feedbackVm.notificarInternetRecuperado(null)
                        }
                        runOnUiThread { actualizarEstadoConexion() }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        conRef.addValueEventListener(firebaseConnectionListener!!)
    }

    private fun usuarioPuedeGestionarHorarios(): Boolean {
        val rolSesion = SessionManager.rol.trim()
        if (rolSesion.equals("administrador", ignoreCase = true)) return true
        if (rolSesion.equals("supervisor", ignoreCase = true)) return true

        val rolPrefs = getSharedPreferences("usuario", MODE_PRIVATE)
            .getString("rol", "")
            .orEmpty()
            .trim()
        return rolPrefs.equals("administrador", ignoreCase = true) ||
            rolPrefs.equals("supervisor", ignoreCase = true)
    }

    private fun iniciarMonitoreoConexion() {
        if (monitoreoConexionRegistrado) {
            actualizarEstadoConexion()
        }

        try {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } catch (_: Exception) {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, networkCallback)
        }
        monitoreoConexionRegistrado = true
        actualizarEstadoConexion()
    }

    private fun detenerMonitoreoConexion() {
        firebaseConnectionListener?.let {
            FirebaseDatabase.getInstance().getReference(".info/connected").removeEventListener(it)
        }
        if (!monitoreoConexionRegistrado) return
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {
        }
        monitoreoConexionRegistrado = false
    }

    private fun hayInternetDisponible(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        val tieneInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val validada = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        return tieneInternet && validada
    }

    private fun actualizarEstadoConexion(forzarToast: Boolean = false) {
        if (_binding == null) return
        val sinConexionCalculada = !hayInternetDisponible() || firebaseOffline
        val huboCambioConexion = sinConexion != sinConexionCalculada
        
        // DETECCION DE RECUPERACION: Si antes no habia y ahora si
        if (huboCambioConexion && !sinConexionCalculada && sinConexion) {
            feedbackVm.notificarInternetRecuperado(null)
        }

        sinConexion = sinConexionCalculada

        if (sinConexionCalculada && huboCambioConexion) {
            feedbackVm.notificarFalloInternet(null)
        }

        actualizarVisibilidadBloqueos()

        val plan = AppAccesoReevaluationRules.planParaCambioConexion(
            sinConexion = sinConexion,
            huboCambio = huboCambioConexion,
            forzarToast = forzarToast,
            sesionUnicaInicializada = sesionUnicaInicializada
        )

        if (sinConexion) {
            procesarReevaluacionSinConexion(plan)
            return
        }

        procesarReevaluacionConConexion(plan)
    }

    private fun mostrarBloqueoSinConexion(mostrar: Boolean) {
        sinConexion = mostrar
        actualizarVisibilidadBloqueos()
    }

    private fun actualizarVisibilidadBloqueos() {
        if (_binding == null) return
        reevaluarAccesoUi()
    }

    private fun aplicarVisibilidadContenidoPrincipal() {
        val scene = AppBloqueoOverlayCoordinator.coordinarEscena(resolverAccesoUiModel())
        aplicarContenidoPrincipal(scene)
    }

    private fun activarSesionUnicaEstrica() {
        if (sesionUnicaInicializada || sesionUnicaEnProceso || sinConexion) return
        sesionUnicaEnProceso = true
        val intentoActual = nuevoIntentoSesionUnica()
        SessionManager.cargarSesion(this)
        val idUsuario = SessionManager.idCajera.trim()
        if (idUsuario.isBlank()) {
            sesionUnicaEnProceso = false
            cerrarSesionLocalYIrLogin()
            return
        }

        val completarSesionUnica: (Boolean) -> Unit = fun(exito: Boolean) {
            if (intentoActual != intentoSesionUnica) return
            sesionUnicaEnProceso = false
            if (!exito) {
                if (sinConexion) return
                mostrarDialogoValidacionSesionFallida()
                return
            }
            if (sesionUnicaInicializada || cierreSesionForzadaMostrado) return
            sesionUnicaInicializada = true
            escucharSesionActivaEstrica(idUsuario)
            SesionUnicaManager.iniciarHeartbeat(
                context = this,
                idUsuario = idUsuario
            )
        }

        val sessionLocal = SesionUnicaManager.obtenerSessionIdLocal(this)
        if (sessionLocal.isBlank()) {
            SesionUnicaManager.reclamarSesionActiva(
                context = this,
                idUsuario = idUsuario,
                nombreUsuario = SessionManager.nombreCajera,
                rolUsuario = SessionManager.rol,
                onComplete = completarSesionUnica
            )
        } else {
            SesionUnicaManager.validarORecuperarSesionLocalUnaVez(
                context = this,
                idUsuario = idUsuario,
                nombreUsuario = SessionManager.nombreCajera,
                rolUsuario = SessionManager.rol,
                onComplete = completarSesionUnica
            )
        }
    }

    private fun escucharSesionActivaEstrica(idUsuario: String) {
        detenerEscuchaSesionActiva()
        val pair = SesionUnicaManager.escucharSesionActiva(
            idUsuario = idUsuario,
            onChanged = { info ->
                if (cierreSesionForzadaMostrado) return@escucharSesionActiva

                val sessionLocal = SesionUnicaManager.obtenerSessionIdLocal(this).trim()

                // Si la sesión remota no existe (info == null), no hacemos nada.
                // Dejamos que el Heartbeat o el flujo de onStart intenten reclamarla de nuevo
                // si es que somos el dispositivo legítimo.
                if (info == null) return@escucharSesionActiva

                val sessionRemota = info.sessionId.trim()
                
                // Si los IDs coinciden, todo está perfecto, somos nosotros.
                if (sessionLocal == sessionRemota) {
                    return@escucharSesionActiva
                }

                // UNICAMENTE expulsamos si hay una sesión activa con un ID DIFERENTE al nuestro.
                // Esto significa que alguien más entró en otro dispositivo.
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    mostrarDialogoSesionReemplazada(info)
                } else {
                    sesionReemplazadaPendiente = info
                }
            },
            onError = { mensaje ->
                Toast.makeText(
                    this,
                    "No se pudo validar la sesión: $mensaje",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        sesionActivaRefListener = pair?.first
        sesionActivaValueListener = pair?.second
    }

    private fun mostrarDialogoSesionReemplazada(info: SesionUnicaManager.SesionRemotaInfo) {
        if (cierreSesionForzadaMostrado || isFinishing || isDestroyed) return
        cierreSesionForzadaMostrado = true

        val usuario = info.usuario.ifBlank { SessionManager.nombreCajera.ifBlank { "No disponible" } }
        val dispositivo = info.dispositivo.ifBlank { "Dispositivo no identificado" }
        val horaIngreso = SesionUnicaManager.formatearHoraIngreso(info)
        registrarMovimientoSesionReemplazada(info, horaIngreso)

        val mensaje = buildString {
            appendLine("Tu cuenta fue abierta en otro dispositivo y esta sesión se cerrará por seguridad.")
            appendLine()
            appendLine("Usuario: $usuario")
            appendLine("Dispositivo: $dispositivo")
            append("Hora de ingreso: $horaIngreso")
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Sesión cerrada por seguridad")
            .setMessage(mensaje)
            .setCancelable(false)
            .setPositiveButton("Entendido") { _, _ ->
                cerrarSesionLocalYIrLogin()
            }
            .show()
    }

    private fun registrarMovimientoSesionReemplazada(
        info: SesionUnicaManager.SesionRemotaInfo,
        horaIngreso: String
    ) {
        if (movimientoSesionReemplazadaRegistrado) return
        movimientoSesionReemplazadaRegistrado = true

        val idUsuario = SessionManager.idCajera.trim()
        val nombreUsuario = SessionManager.nombreCajera.trim()
        val sessionLocal = SessionManager.obtenerSessionIdLocal(this).trim()
        val sessionRemota = info.sessionId.trim()

        if (idUsuario.isBlank()) return

        MovimientoLogger.registrar(
            tipo = "seguridad_sesion_reemplazada",
            modulo = "seguridad",
            titulo = "Sesion cerrada por inicio en otro dispositivo",
            descripcion = "La sesion local fue cerrada automaticamente porque la cuenta se abrio en otro dispositivo.",
            idUsuario = idUsuario,
            nombreUsuario = nombreUsuario.ifBlank { idUsuario },
            idCaja = SessionManager.idCajera,
            nombreCaja = SessionManager.nombreCajera,
            extra = mapOf(
                "motivo" to "sesion_reemplazada_por_otro_dispositivo",
                "usuarioRemoto" to info.usuario.ifBlank { nombreUsuario.ifBlank { idUsuario } },
                "rolRemoto" to info.rol,
                "dispositivoRemoto" to info.dispositivo,
                "horaIngresoRemoto" to horaIngreso,
                "sessionIdLocal" to sessionLocal,
                "sessionIdRemota" to sessionRemota
            )
        )
    }

    private fun mostrarDialogoValidacionSesionFallida() {
        if (cierreSesionForzadaMostrado || isFinishing || isDestroyed) return
        cierreSesionForzadaMostrado = true

        MaterialAlertDialogBuilder(this)
            .setTitle("No se pudo validar la sesión")
            .setMessage(
                "Se requiere conexión para validar sesión única por cuenta. " +
                    "Por seguridad volverás al login."
            )
            .setCancelable(false)
            .setPositiveButton("Entendido") { _, _ ->
                cerrarSesionLocalYIrLogin()
            }
            .show()
    }

    private fun detenerEscuchaSesionActiva() {
        SesionUnicaManager.dejarDeEscuchar(sesionActivaRefListener, sesionActivaValueListener)
        sesionActivaRefListener = null
        sesionActivaValueListener = null
    }

    private fun nuevoIntentoSesionUnica(): Long {
        intentoSesionUnica += 1L
        return intentoSesionUnica
    }

    private fun invalidarIntentosSesionUnica() {
        intentoSesionUnica += 1L
    }

    private fun cerrarSesionLocalYIrLogin() {
        SesionUnicaManager.detenerHeartbeat()
        detenerEscuchaSesionActiva()
        sesionUnicaEnProceso = false
        invalidarIntentosSesionUnica()
        SessionManager.limpiarSesion(this)

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        finish()
    }

        private fun verificarConfiguracionTiendaYBloquearSiFalta() {
        if (
            !AppAccesoReevaluationRules.debeVerificarConfiguracionTienda(
                sinConexion = sinConexion,
                verificacionConfigEnCurso = verificacionConfigEnCurso
            )
        ) return
        verificacionConfigEnCurso = true

        database.getReference("ConfiguracionTienda")
            .child("datosGenerales")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    sincronizarMonedaSesion(snapshot)
                    val tieneDatos = snapshot.tieneDatosGeneralesTienda()
                    aplicarResultadoConfiguracionTienda(tieneDatos)
                }

                override fun onCancelled(error: DatabaseError) {
                    aplicarErrorConfiguracionTienda()
                }
            })
    }

    private fun mostrarBloqueoConfiguracionTienda(mostrar: Boolean) {
        bloqueadoPorConfigTienda = mostrar
        actualizarVisibilidadBloqueos()
    }

    private fun iniciarEscuchaHorarioDelDiaActual() {
        val diaKey = obtenerClaveDiaActual()
        if (
            !AppAccesoReevaluationRules.debeIniciarEscuchaHorario(
                sinConexion = sinConexion,
                diaKey = diaKey,
                diaHorarioEscuchado = diaHorarioEscuchado,
                horarioDiaRefExiste = horarioDiaRef != null,
                horarioDiaListenerExiste = horarioDiaListener != null
            )
        ) {
            return
        }

        detenerEscuchaHorarioDelDiaActual()
        diaHorarioEscuchado = diaKey

        val ref = database.getReference("ConfiguracionTienda")
            .child("Horarios")
            .child(diaKey)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cerrado = snapshot.child("cerrado").getValue(Boolean::class.java) == true
                val cerradoPorNombre = snapshot.child("cerradoPorNombre")
                    .getValue(String::class.java)
                    .orEmpty()
                    .trim()
                val cerradoEn = snapshot.child("cerradoEn").parseLongSeguro()
                aplicarEstadoHorarioBloqueado(
                    cerradoHoy = cerrado,
                    cerradoPorNombre = cerradoPorNombre,
                    cerradoEn = cerradoEn
                )
            }

            override fun onCancelled(error: DatabaseError) {
                aplicarEstadoHorarioBloqueado(
                    cerradoHoy = false,
                    cerradoPorNombre = "",
                    cerradoEn = 0L
                )
            }
        }

        horarioDiaRef = ref
        horarioDiaListener = listener
        ref.addValueEventListener(listener)
    }

    private fun detenerEscuchaHorarioDelDiaActual() {
        horarioDiaRef?.let { ref ->
            horarioDiaListener?.let { listener ->
                ref.removeEventListener(listener)
            }
        }
        horarioDiaRef = null
        horarioDiaListener = null
        diaHorarioEscuchado = ""
    }

    private fun aplicarEstadoHorarioBloqueado(
        cerradoHoy: Boolean,
        cerradoPorNombre: String,
        cerradoEn: Long
    ) {
        bloqueadoPorHorario = cerradoHoy
        if (cerradoHoy) {
            aplicarResultadoHorarioBloqueado(
                cerradoPorNombre = cerradoPorNombre,
                cerradoEn = cerradoEn
            )
        } else {
            limpiarResultadoHorarioBloqueado()
        }
        actualizarVisibilidadBloqueos()
    }

    private fun aplicarResultadoHorarioBloqueado(
        cerradoPorNombre: String,
        cerradoEn: Long
    ) {
        val b = _binding ?: return
        val diaActual = obtenerNombreDiaActual()
        b.tvMensajeBloqueoTiendaCerrada.text = construirMensajeBloqueoHorario(
            diaActual = diaActual,
            cerradoPorNombre = cerradoPorNombre,
            cerradoEn = cerradoEn
        )
        b.tvProximaAperturaTiendaCerrada.visibility = View.VISIBLE
        b.tvProximaAperturaTiendaCerrada.text = "Buscando proxima apertura..."
        cargarProximaAperturaDesdeHorarios()
    }

    private fun limpiarResultadoHorarioBloqueado() {
        val b = _binding ?: return
        b.tvProximaAperturaTiendaCerrada.visibility = View.GONE
    }

    private fun aplicarResultadoConfiguracionTienda(
        tieneDatos: Boolean
    ) {
        verificacionConfigEnCurso = false
        bloqueadoPorConfigTienda = !tieneDatos
        actualizarVisibilidadBloqueos()
    }

    private fun aplicarErrorConfiguracionTienda() {
        verificacionConfigEnCurso = false
        bloqueadoPorConfigTienda = true
        actualizarVisibilidadBloqueos()
    }

    private fun reevaluarAccesoGlobal() {
        val plan = AppAccesoReevaluationRules.planParaForeground(
            sinConexion = sinConexion,
            bloqueadoPorHorario = bloqueadoPorHorario
        )

        if (plan.debeEscucharEstadoUsuario) {
            iniciarEscuchaEstadoAccesoUsuario()
        }
        if (plan.debeActualizarEstadoConexion) {
            actualizarEstadoConexion()
        }
        if (plan.debeCargarProximaApertura) {
            cargarProximaAperturaDesdeHorarios()
        }
    }

    private fun procesarReevaluacionSinConexion(
        plan: AppAccesoReevaluationRules.AppReevaluacionPlan
    ) {
        if (plan.debeDetenerEscuchaHorario) {
            detenerEscuchaHorarioDelDiaActual()
        }
        if (plan.debeCancelarSesionUnicaEnProceso) {
            sesionUnicaEnProceso = false
        }
        if (plan.debeInvalidarIntentosSesionUnica) {
            invalidarIntentosSesionUnica()
        }
        if (plan.debeMostrarToastSinConexion) {
            Toast.makeText(
                this,
                "Sin internet. Verifica tu conexi\u00F3n para continuar.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun procesarReevaluacionConConexion(
        plan: AppAccesoReevaluationRules.AppReevaluacionPlan
    ) {
        if (plan.debeIntentarReanudarSesionUnica) {
            reanudarSesionUnicaSiCorresponde()
        }

        if (plan.debeIniciarEscuchaHorario) {
            iniciarEscuchaHorarioDelDiaActual()
        }

        if (plan.debeVerificarConfigTienda) {
            verificarConfiguracionTiendaYBloquearSiFalta()
        }
    }

    private fun reanudarSesionUnicaSiCorresponde() {
        val sessionLocal = SesionUnicaManager.obtenerSessionIdLocal(this)
        val idUsuario = SessionManager.idCajera.trim()
        if (sessionLocal.isNotBlank() && idUsuario.isNotBlank()) {
            // Ya existe una sesion guardada localmente; no generamos una nueva.
            // Solo retomamos listener y heartbeat sin pisar el session ID remoto.
            sesionUnicaInicializada = true
            escucharSesionActivaEstrica(idUsuario)
            SesionUnicaManager.iniciarHeartbeat(context = this, idUsuario = idUsuario)
        } else {
            // No hay sesion local disponible, asi que reclamamos una nueva.
            activarSesionUnicaEstrica()
        }
    }

    private fun construirMensajeBloqueoHorario(
        diaActual: String,
        cerradoPorNombre: String,
        cerradoEn: Long
    ): String {
        if (cerradoPorNombre.isBlank()) {
            return "La tienda esta cerrada hoy ($diaActual).\nActiva el horario del dia para continuar."
        }

        val hora = if (cerradoEn > 0L) formatearHora(cerradoEn) else ""
        return if (hora.isBlank()) {
            "Han cerrado el acceso de la tienda para hoy ($diaActual).\nResponsable: $cerradoPorNombre."
        } else {
            "Han cerrado el acceso de la tienda para hoy ($diaActual).\nResponsable: $cerradoPorNombre a las $hora."
        }
    }

    private fun cargarProximaAperturaDesdeHorarios() {
        if (sinConexion || !bloqueadoPorHorario) return

        database.getReference("ConfiguracionTienda")
            .child("Horarios")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val b = _binding ?: return
                    if (!bloqueadoPorHorario) return
                    val horarios = mutableMapOf<String, HorarioDiaInfo>()
                    snapshot.children.forEach { item ->
                        val keyDia = normalizarDia(item.key.orEmpty())
                        if (keyDia.isBlank()) return@forEach

                        val info = HorarioDiaInfo(
                            cerrado = item.child("cerrado").getValue(Boolean::class.java) == true,
                            veinticuatroHoras = item.child("veinticuatroHoras").getValue(Boolean::class.java) == true,
                            horaApertura = (item.child("horaAperturaLocal").getValue(String::class.java)
                                ?: item.child("horaApertura").getValue(String::class.java)
                                ?: "").trim()
                        )
                        horarios[keyDia] = info
                    }

                    b.tvProximaAperturaTiendaCerrada.text =
                        calcularTextoProximaApertura(horarios)
                }

                override fun onCancelled(error: DatabaseError) {
                    val b = _binding ?: return
                    if (!bloqueadoPorHorario) return
                    b.tvProximaAperturaTiendaCerrada.text =
                        "No se pudo verificar la proxima apertura."
                }
            })
    }

    private fun calcularTextoProximaApertura(horarios: Map<String, HorarioDiaInfo>): String {
        if (horarios.isEmpty()) {
            return "No hay horarios configurados para calcular la apertura."
        }

        val claveHoy = obtenerClaveDiaActual()
        val indiceHoy = ordenDiasSemana.indexOf(claveHoy).takeIf { it >= 0 } ?: 0

        for (salto in 1..7) {
            val indice = (indiceHoy + salto) % ordenDiasSemana.size
            val claveDia = ordenDiasSemana[indice]
            val horario = horarios[claveDia] ?: continue
            if (horario.cerrado) continue

            val diaTexto = nombreDiaMostrable(claveDia)
            val prefijo = if (salto == 1) {
                "Manana ($diaTexto)"
            } else {
                diaTexto
            }

            return when {
                horario.veinticuatroHoras ->
                    "Proxima apertura: $prefijo, 24 horas."
                horario.horaApertura.isNotBlank() ->
                    "Proxima apertura: $prefijo a las ${horario.horaApertura}."
                else ->
                    "Proxima apertura: $prefijo."
            }
        }

        return "No hay dias abiertos configurados en Horarios."
    }

    private fun nombreDiaMostrable(claveDia: String): String {
        return when (claveDia) {
            "lunes" -> "Lunes"
            "martes" -> "Martes"
            "miercoles" -> "Miercoles"
            "jueves" -> "Jueves"
            "viernes" -> "Viernes"
            "sabado" -> "Sabado"
            "domingo" -> "Domingo"
            else -> claveDia.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        }
    }

    private fun obtenerClaveDiaActual(): String {
        val formato = SimpleDateFormat("EEEE", Locale.forLanguageTag("es-PE"))
        val nombreDia = formato.format(Calendar.getInstance().time)
        return normalizarDia(nombreDia)
    }

    private fun obtenerNombreDiaActual(): String {
        val localeEsPe = Locale.forLanguageTag("es-PE")
        val formato = SimpleDateFormat("EEEE", localeEsPe)
        val nombreDia = formato.format(Calendar.getInstance().time).trim()
        if (nombreDia.isBlank()) return "hoy"
        return nombreDia.replaceFirstChar { c ->
            if (c.isLowerCase()) c.titlecase(localeEsPe) else c.toString()
        }
    }

    private fun normalizarDia(valor: String): String {
        val texto = valor.trim().lowercase(Locale.ROOT)
        if (texto.isBlank()) return texto
        val normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD)
        return normalizado.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    }

    private fun DataSnapshot.parseLongSeguro(): Long {
        val valor = value ?: return 0L
        return when (valor) {
            is Long -> valor
            is Int -> valor.toLong()
            is Double -> valor.toLong()
            is String -> valor.toLongOrNull() ?: 0L
            else -> 0L
        }
    }

    private fun formatearHora(timestamp: Long): String {
        return try {
            val formato = SimpleDateFormat("h:mm a", Locale.forLanguageTag("es-PE"))
            formato.format(timestamp)
        } catch (_: Exception) {
            ""
        }
    }

    private data class HorarioDiaInfo(
        val cerrado: Boolean,
        val veinticuatroHoras: Boolean,
        val horaApertura: String
    )

    private fun DataSnapshot.tieneDatosGeneralesTienda(): Boolean {
        if (!exists()) return false

        val nombreTienda = child("nombreTienda").getValue(String::class.java).orEmpty().trim()
        val razonSocial = child("razonSocial").getValue(String::class.java).orEmpty().trim()
        val identificacionFiscal = child("identificacionFiscal").getValue(String::class.java).orEmpty().trim()
        val monedaCodigo = child("monedaCodigo").getValue(String::class.java).orEmpty().trim()
        val monedaSimbolo = child("monedaSimbolo").getValue(String::class.java).orEmpty().trim()

        return nombreTienda.isNotBlank() &&
            razonSocial.isNotBlank() &&
            identificacionFiscal.isNotBlank() &&
            monedaCodigo.isNotBlank() &&
            monedaSimbolo.isNotBlank()
    }

    private fun sincronizarMonedaSesion(snapshot: DataSnapshot) {
        if (!snapshot.exists()) return
        val monedaCodigo = snapshot.child("monedaCodigo").getValue(String::class.java).orEmpty().trim()
        val monedaSimbolo = snapshot.child("monedaSimbolo").getValue(String::class.java).orEmpty().trim()
        if (monedaCodigo.isBlank() || monedaSimbolo.isBlank()) return
        SessionManager.guardarMonedaConfigurada(this, monedaCodigo, monedaSimbolo)
    }

    fun setBottomNavigationVisible(visible: Boolean) {
        navegacionManualOculta = !visible
        val visibilidad = if (visible) View.VISIBLE else View.GONE
        val b = _binding ?: return
        b.botonnavigation?.visibility = visibilidad
        b.navigationRail?.visibility = visibilidad
        b.root.findViewById<View>(R.id.sidebarTablet)?.visibility = visibilidad
    }

    private fun seleccionarFragmento(id: Int) {
        val accesoUi = resolverAccesoUiModel()
        if (accesoUi.bloquearNavegacion) return

        val fragmentoDestino = when (id) {
            R.id.nav_home -> fragmentoHome
            R.id.nav_cajaventas -> fragmentoCaja
            R.id.nav_pedidos -> fragmentoPedidos
            R.id.nav_productos -> fragmentoInventario
            R.id.nav_perfil -> fragmentoPerfil
            else -> fragmentoHome
        }

        if (fragmentoActivo != fragmentoDestino) {
            supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .hide(fragmentoActivo)
                .show(fragmentoDestino)
                .commit()
            fragmentoActivo = fragmentoDestino
        }

    }

    private fun marcarDestinoNavegacion(itemId: Int) {
        binding.botonnavigation?.menu?.findItem(itemId)?.isChecked = true
        binding.navigationRail?.menu?.findItem(itemId)?.isChecked = true
    }

    private fun sincronizarSeleccionNavegacionActual() {
        marcarDestinoNavegacion(
            when (fragmentoActivo) {
                fragmentoCaja -> R.id.nav_cajaventas
                fragmentoPedidos -> R.id.nav_pedidos
                fragmentoInventario -> R.id.nav_productos
                fragmentoPerfil -> R.id.nav_perfil
                else -> R.id.nav_home
            }
        )
    }


}
