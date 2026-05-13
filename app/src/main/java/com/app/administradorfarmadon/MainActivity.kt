package com.app.administradorfarmadon

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.app.administradorfarmadon.ActivityFragmentos.ActivityFragmentos
import com.app.administradorfarmadon.ClasesDatabase.AccesoFieldRules
import com.app.administradorfarmadon.ClasesDatabase.SessionManager
import com.app.administradorfarmadon.ClasesDatabase.SesionUnicaManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.app.administradorfarmadon.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private data class EstadoAccesoLogin(
        val activo: Boolean,
        val bloqueadoPorNombre: String = "",
        val bloqueadoEn: Long = 0L
    )

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private var rolElegido = ""
    private val database = FirebaseDatabase.getInstance().reference
    private var mantenerSplashVisible = true
    private var loginUiInicializada = false

    private fun aplicarCierreDropdownAlRecibirFoco(autoComplete: AutoCompleteTextView) {
        autoComplete.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                autoComplete.dismissDropDown()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { mantenerSplashVisible }
        super.onCreate(savedInstanceState)

        // 2. Verificar sesión activa
        if (intentarAutoIngresoSeguro()) return

        inicializarPantallaLoginSiHaceFalta()
        configurarSugerenciasUsuario()
        mantenerSplashVisible = false
    }

    private fun configurarSugerenciasUsuario() {
        val usuariosGuardados = obtenerUsuariosRecordados()
        if (usuariosGuardados.isNotEmpty()) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, usuariosGuardados)
            (binding.etUsuario as? AutoCompleteTextView)?.setAdapter(adapter)
            
            // Mostrar sugerencias al hacer click si está vacío
            binding.etUsuario.setOnClickListener {
                if (binding.etUsuario.text.isNullOrEmpty()) {
                    (binding.etUsuario as? AutoCompleteTextView)?.showDropDown()
                }
            }
        }
    }

    private fun obtenerUsuariosRecordados(): List<String> {
        val prefs = getSharedPreferences("login_prefs", MODE_PRIVATE)
        val set = prefs.getStringSet("usuarios_recordados", emptySet()) ?: emptySet()
        return set.toList().sorted()
    }

    private fun guardarUsuarioRecordado(usuario: String) {
        val prefs = getSharedPreferences("login_prefs", MODE_PRIVATE)
        val set = prefs.getStringSet("usuarios_recordados", emptySet())?.toMutableSet() ?: mutableSetOf()
        if (set.add(usuario.lowercase().trim())) {
            prefs.edit().putStringSet("usuarios_recordados", set).apply()
            configurarSugerenciasUsuario() // Actualizar lista en vivo
        }
    }

    private fun inicializarPantallaLoginSiHaceFalta() {
        if (loginUiInicializada) return

        _binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        configurarVentana()
        configurarLogin()
        configurarRegistro()
        configurarBackNavigation()
        loginUiInicializada = true
    }

    private fun configurarVentana() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Forzar iconos oscuros en la barra de estado para fondo claro
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController?.isAppearanceLightStatusBars = true
    }

    private fun configurarBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    binding.constraintcrearusuario.isVisible -> {
                        mostrarLogin()
                        limpiarCamposRegistro()
                    }
                    binding.accesorestringidologin.isVisible -> {
                        binding.accesorestringidologin.visibility = View.GONE
                        binding.scrollLogin.visibility = View.VISIBLE
                    }
                    else -> finish()
                }
            }
        })
    }

    private fun configurarLogin() {
        binding.etUsuario.addTextChangedListener { validarCamposLogin() }
        binding.etContrasena.addTextChangedListener { validarCamposLogin() }

        // Mantenemos lógica de scroll automático al enfocar contraseña
        binding.etContrasena.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                binding.etContrasena.post {
                    binding.scrollLogin.smoothScrollTo(0, v.bottom + 100)
                }
            }
        }

        binding.btnIngresar.setOnClickListener {
            val usuario = binding.etUsuario.text.toString().trim()
            val contrasena = binding.etContrasena.text.toString().trim()
            verificarCredenciales(usuario, contrasena)
        }

        configurarTextoRegistrarse()
        
        // Botón Volver en acceso restringido
        binding.materialButton2.setOnClickListener { 
            binding.accesorestringidologin.visibility = View.GONE
            binding.scrollLogin.visibility = View.VISIBLE
        }
    }

    private fun configurarRegistro() {
        val cargos = listOf("Administrador", "Supervisor", "Caja", "Repartidor")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cargos)
        binding.dropRoll.setAdapter(adapter)
        aplicarCierreDropdownAlRecibirFoco(binding.dropRoll)

        binding.dropRoll.setOnItemClickListener { _, _, position, _ ->
            val seleccion = cargos[position]
            if (seleccion == "Repartidor") {
                mostrarToast("Este puesto aún no está disponible, elige otro")
                binding.dropRoll.setText("", false)
                rolElegido = ""
            } else {
                rolElegido = seleccion
            }
            validarCamposRegistro()
        }

        binding.editText.addTextChangedListener { validarCamposRegistro() }
        binding.editTextcontrasena.addTextChangedListener { validarCamposRegistro() }
        binding.editTextdocumento.addTextChangedListener { validarCamposRegistro() }

        binding.atrasregistro.setOnClickListener { 
            mostrarLogin()
            limpiarCamposRegistro()
        }
        
        binding.materialButton.setOnClickListener { registrarSolicitud() }
    }

    // --- LÓGICA DE BOTONES ---

    private fun validarCamposLogin() {
        val activar = binding.etUsuario.text.toString().trim().isNotEmpty() &&
                binding.etContrasena.text.toString().trim().isNotEmpty()
        cambiarEstadoBoton(binding.btnIngresar, activar)
    }

    private fun validarCamposRegistro() {
        val activar = binding.editText.text.toString().trim().isNotEmpty() &&
                binding.editTextcontrasena.text.toString().trim().isNotEmpty() &&
                binding.editTextdocumento.text.toString().trim().isNotEmpty() &&
                rolElegido.isNotEmpty()
        cambiarEstadoBoton(binding.materialButton, activar)
    }

    private fun cambiarEstadoBoton(boton: com.google.android.material.button.MaterialButton, activar: Boolean) {
        boton.isEnabled = activar
        val color = if (activar) "#36B37E" else "#BDBDBD"
        boton.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color))
    }

    // --- FIREBASE LOGIN ---

    private fun verificarCredenciales(usuarioInput: String, contrasenaInput: String) {
        mostrarProgreso(true)

        val usuarioBuscado = usuarioInput.trim().lowercase()
        val contrasenaBuscada = contrasenaInput.trim()

        database.child("trabajadores")
            .get()
            .addOnSuccessListener { snapshot ->
                for (trabajador in snapshot.children) {
                    val usuarioDb = trabajador.child("usuario").value
                        ?.toString()
                        .orEmpty()
                        .trim()
                        .lowercase()

                    val passDb = trabajador.child("contrasena").value
                        ?.toString()
                        .orEmpty()
                        .trim()

                    if (usuarioDb == usuarioBuscado) {
                        if (passDb == contrasenaBuscada) {
                            loginExitoso(trabajador)
                            return@addOnSuccessListener
                        } else {
                            mostrarProgreso(false)
                            mostrarToast("Credenciales incorrectas")
                            return@addOnSuccessListener
                        }
                    }
                }

                verificarSolicitudPendiente(usuarioBuscado, contrasenaBuscada)
            }
            .addOnFailureListener { error ->
                mostrarProgreso(false)
                mostrarToast(error.message ?: "No se pudo verificar el acceso")
            }
    }

    private fun loginExitoso(snapshot: DataSnapshot) {
        val acceso = AccesoFieldRules.parseAccesoDesdeSnapshot(
            snapshot = snapshot,
            defaultValue = false
        )
        if (acceso) {
            val user = UserTrabajador(
                usuario = snapshot.child("usuario").value?.toString() ?: "",
                contrasena = "",
                acceso = true,
                id = snapshot.child("id").value?.toString() ?: "",
                rol = snapshot.child("rol").value?.toString() ?: ""
            )
            // Antes de abrir la app, verificamos si hay una sesión activa en otro dispositivo
            guardarUsuarioRecordado(user.usuario)
            SesionUnicaManager.obtenerSesionActivaUnaVez(user.id) { info ->
                mostrarProgreso(false)
                if (info != null && !SesionUnicaManager.sesionEstaExpirada(info)) {
                    // Hay una sesión activa — avisar antes de patearla
                    val dispositivo = info.dispositivo.ifBlank { "otro dispositivo" }
                    val hora = SesionUnicaManager.formatearHoraIngreso(info)
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Sesión activa en otro dispositivo")
                        .setMessage(
                            "Tu cuenta está abierta en $dispositivo desde $hora.\n\n" +
                            "Si ingresás desde acá, esa sesión se cerrará automáticamente."
                        )
                        .setCancelable(false)
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Ingresar de todas formas") { _, _ ->
                            asegurarSesionYEntrar(user)
                        }
                        .show()
                } else {
                    // Sin sesión activa — entramos directo
                    asegurarSesionYEntrar(user)
                }
            }
        } else {
            mostrarProgreso(false)
            mostrarAccesoRestringidoLogin(
                mensaje = construirMensajeAccesoBloqueado(
                    bloqueadoPorNombre = snapshot.child("estadoAccesoActualizadoPorNombre")
                        .getValue(String::class.java)
                        .orEmpty(),
                    bloqueadoEn = snapshot.child("estadoAccesoActualizadoEnServidor")
                        .getValue(Long::class.java)
                        ?: snapshot.child("estadoAccesoActualizadoEn").getValue(Long::class.java)
                        ?: 0L
                )
            )
        }
    }

    private fun asegurarSesionYEntrar(user: UserTrabajador) {
        mostrarProgreso(true)
        SesionUnicaManager.reclamarSesionActiva(
            context = this,
            idUsuario = user.id,
            nombreUsuario = user.usuario,
            rolUsuario = user.rol
        ) { exito ->
            if (isFinishing || isDestroyed) return@reclamarSesionActiva
            mostrarProgreso(false)
            if (!exito) {
                mostrarDialogo(
                    "No se pudo asegurar la sesion",
                    "Intenta nuevamente. La app solo puede continuar cuando este dispositivo confirma la sesion activa."
                )
                return@reclamarSesionActiva
            }

            SessionManager.guardarSesion(this, user.id, user.usuario, user.rol)
            abrirAppConSesionValidada()
        }
    }

    private fun abrirAppConSesionValidada() {
        mantenerSplashVisible = false
        val intent = Intent(this, ActivityFragmentos::class.java).apply {
            putExtra("esLoginFresco", false)
        }
        startActivity(intent)
        finish()
    }

    private fun verificarSolicitudPendiente(usuario: String, contrasena: String) {
        database.child("solicitudregistrotrabajador")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mostrarProgreso(false)

                    for (solicitud in snapshot.children) {
                        val usuarioDb = solicitud.child("usuario").value
                            ?.toString()
                            .orEmpty()
                            .trim()
                            .lowercase()

                        val passDb = solicitud.child("contrasena").value
                            ?.toString()
                            .orEmpty()
                            .trim()

                        if (usuarioDb == usuario) {
                            if (passDb == contrasena) {
                                mostrarToast("Tu solicitud está pendiente de aprobación")
                            } else {
                                mostrarToast("Credenciales incorrectas")
                            }
                            return
                        }
                    }

                    mostrarToast("Usuario no registrado")
                }

                override fun onCancelled(error: DatabaseError) {
                    mostrarProgreso(false)
                    mostrarToast(error.message)
                }
            })
    }

    // --- FIREBASE REGISTRO ---

    private fun registrarSolicitud() {
        val user = binding.editText.text.toString().trim().lowercase()
        val pass = binding.editTextcontrasena.text.toString().trim()
        val doc = binding.editTextdocumento.text.toString().trim()

        mostrarProgreso(true)

        // Verificación de duplicados en trabajadores y solicitudes
        database.child("trabajadores").orderByChild("documento").equalTo(doc)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(s1: DataSnapshot) {
                    if (s1.exists()) {
                        errorRegistro("Ya existe un usuario con ese número de documento")
                    } else {
                        database.child("solicitudregistrotrabajador").orderByChild("documento").equalTo(doc)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(s2: DataSnapshot) {
                                    if (s2.exists()) {
                                        errorRegistro("Ya existe una solicitud con ese número de documento")
                                    } else {
                                        verificarNombreUsuarioUnico(user, pass, doc)
                                    }
                                }
                                override fun onCancelled(e: DatabaseError) { errorRegistro(e.message) }
                            })
                    }
                }
                override fun onCancelled(e: DatabaseError) { errorRegistro(e.message) }
            })
    }

    private fun verificarNombreUsuarioUnico(user: String, pass: String, doc: String) {
        database.child("trabajadores").orderByChild("usuario").equalTo(user)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(s1: DataSnapshot) {
                    if (s1.exists()) {
                        errorRegistro("Ese nombre de usuario ya existe. Crea otro nombre similar")
                    } else {
                        database.child("solicitudregistrotrabajador").orderByChild("usuario").equalTo(user)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(s2: DataSnapshot) {
                                    if (s2.exists()) {
                                        errorRegistro("Ese nombre de usuario ya fue solicitado. Crea otro nombre similar")
                                    } else {
                                        guardarSolicitudFinal(user, pass, doc)
                                    }
                                }
                                override fun onCancelled(e: DatabaseError) { errorRegistro(e.message) }
                            })
                    }
                }
                override fun onCancelled(e: DatabaseError) { errorRegistro(e.message) }
            })
    }

    private fun guardarSolicitudFinal(user: String, pass: String, doc: String) {
        val ref = database.child("solicitudregistrotrabajador")
        val key = ref.push().key
        if (key == null) {
            errorRegistro("Error al generar identificador. Intenta nuevamente.")
            return
        }

        val solicitud = hashMapOf(
            "id" to key,
            "usuario" to user,
            "contrasena" to pass,
            "documento" to doc,
            "rol" to rolElegido,
            "acceso" to false
        )

        ref.child(key).setValue(solicitud).addOnCompleteListener { task ->
            mostrarProgreso(false)
            if (task.isSuccessful) {
                mostrarDialogo("Solicitud enviada", "Tu cuenta será activada por un administrador")
                mostrarLogin()
                limpiarCamposRegistro()
            } else {
                mostrarToast("Error: ${task.exception?.message}")
            }
        }
    }

    // --- UTILIDADES ---

    private fun mostrarProgreso(mostrar: Boolean) {
        val view = _binding?.progreso ?: return // Seguridad: si no hay binding, no hacemos nada
        
        if (mostrar) {
            view.apply {
                alpha = 0f
                visibility = View.VISIBLE
                animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setListener(null)
            }
        } else {
            view.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction { 
                    if (_binding != null) view.visibility = View.GONE 
                }
        }
        
        // Bloquear botones mientras carga
        _binding?.btnIngresar?.isEnabled = !mostrar
        _binding?.materialButton?.isEnabled = !mostrar
    }

    private fun errorRegistro(m: String?) {
        mostrarProgreso(false)
        mostrarDialogo("Atención", m ?: "Ocurrió un error inesperado")
    }

    private fun mostrarLogin() {
        binding.scrollLogin.visibility = View.VISIBLE
        binding.constraintcrearusuario.visibility = View.GONE
    }

    private fun mostrarRegistro() {
        binding.scrollLogin.visibility = View.GONE
        binding.constraintcrearusuario.visibility = View.VISIBLE
    }

    private fun limpiarCamposRegistro() {
        binding.editText.text?.clear()
        binding.editTextcontrasena.text?.clear()
        binding.editTextdocumento.text?.clear()
        binding.dropRoll.setText("", false)
        rolElegido = ""
        validarCamposRegistro()
    }

    private fun mostrarDialogo(titulo: String, mensaje: String) {
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("Aceptar", null)
            .show()
    }

    private fun mostrarToast(m: String) = Toast.makeText(this, m, Toast.LENGTH_SHORT).show()

    private fun guardarCredencialesPreference(user: UserTrabajador) {
        // Usamos el SessionManager para centralizar la persistencia
        SessionManager.guardarSesion(this, user.id, user.usuario, user.rol)

        abrirAppConSesionValidada()
    }

    private fun intentarAutoIngresoSeguro(): Boolean {
        SessionManager.cargarSesion(this)
        if (!SessionManager.isLoggedIn) return false

        val idUsuario = SessionManager.idCajera.trim()
        val nombreUsuario = SessionManager.nombreCajera.trim()
        val rolUsuario = SessionManager.rol.trim()
        if (idUsuario.isBlank() || nombreUsuario.isBlank()) {
            resolverAutoIngresoInvalido()
            return false
        }

        verificarEstadoAccesoUsuarioUnaVez(idUsuario) accesoCheck@{ estadoAcceso ->
            if (isFinishing || isDestroyed) return@accesoCheck
            when {
                estadoAcceso == null -> {
                    resolverAutoIngresoInvalido()
                }
                !estadoAcceso.activo -> {
                    resolverAutoIngresoBloqueado(estadoAcceso)
                }
                else -> SesionUnicaManager.validarORecuperarSesionLocalUnaVez(
                    context = this,
                    idUsuario = idUsuario,
                    nombreUsuario = nombreUsuario,
                    rolUsuario = rolUsuario
                ) { exito ->
                    if (isFinishing || isDestroyed) return@validarORecuperarSesionLocalUnaVez
                    if (exito) {
                        abrirAppConSesionValidada()
                    } else {
                        resolverAutoIngresoInvalido()
                    }
                }
            }
        }
        return true
    }

    private fun verificarEstadoAccesoUsuarioUnaVez(
        idUsuario: String,
        onResult: (EstadoAccesoLogin?) -> Unit
    ) {
        database.child("trabajadores").child(idUsuario)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    onResult(null)
                    return@addOnSuccessListener
                }

                val activo = AccesoFieldRules.parseAccesoDesdeSnapshot(
                    snapshot = snapshot,
                    defaultValue = false
                )
                val bloqueadoPorNombre = snapshot.child("estadoAccesoActualizadoPorNombre")
                    .getValue(String::class.java)
                    .orEmpty()
                val bloqueadoEn = snapshot.child("estadoAccesoActualizadoEnServidor")
                    .getValue(Long::class.java)
                    ?: snapshot.child("estadoAccesoActualizadoEn").getValue(Long::class.java)
                    ?: 0L

                onResult(
                    EstadoAccesoLogin(
                        activo = activo,
                        bloqueadoPorNombre = bloqueadoPorNombre,
                        bloqueadoEn = bloqueadoEn
                    )
                )
            }
            .addOnFailureListener {
                mantenerSplashVisible = false // Liberar splash en caso de error de red
                onResult(null)
            }
    }

    private fun resolverAutoIngresoBloqueado(estadoAcceso: EstadoAccesoLogin) {
        SessionManager.limpiarSesion(this)
        inicializarPantallaLoginSiHaceFalta()
        mantenerSplashVisible = false
        mostrarAccesoRestringidoLogin(
            mensaje = construirMensajeAccesoBloqueado(
                bloqueadoPorNombre = estadoAcceso.bloqueadoPorNombre,
                bloqueadoEn = estadoAcceso.bloqueadoEn
            )
        )
    }

    private fun mostrarAccesoRestringidoLogin(mensaje: String? = null) {
        inicializarPantallaLoginSiHaceFalta()
        binding.scrollLogin.visibility = View.GONE
        binding.constraintcrearusuario.visibility = View.GONE
        binding.accesorestringidologin.visibility = View.VISIBLE
        if (!mensaje.isNullOrBlank()) {
            mostrarToast(mensaje)
        }
    }

    private fun construirMensajeAccesoBloqueado(
        bloqueadoPorNombre: String,
        bloqueadoEn: Long
    ): String {
        val quien = bloqueadoPorNombre.trim().ifBlank { "Administrador" }
        return if (bloqueadoEn > 0L) {
            "Tu acceso fue desactivado por $quien. Vuelve al login para continuar."
        } else {
            "Tu acceso fue desactivado por $quien. No puedes entrar a la app por ahora."
        }
    }

    private fun resolverAutoIngresoInvalido() {
        SessionManager.limpiarSesion(this)
        inicializarPantallaLoginSiHaceFalta()
        mantenerSplashVisible = false
        mostrarToast("Por seguridad debes iniciar sesion nuevamente")
    }

    private fun configurarTextoRegistrarse() {
        val fullText = "Aun no tienes una cuenta? Registrate"
        val word = "Registrate"
        val spannable = SpannableString(fullText)
        val click = object : ClickableSpan() {
            override fun onClick(v: View) = mostrarRegistro()
            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = false
                ds.color = Color.parseColor("#36B37E")
                ds.isFakeBoldText = true
            }
        }
        val start = fullText.indexOf(word)
        if (start != -1) {
            spannable.setSpan(click, start, start + word.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.registrasetxt.apply {
            text = spannable
            movementMethod = LinkMovementMethod.getInstance()
            highlightColor = Color.TRANSPARENT
        }
    }

    override fun onDestroy() {
        SesionUnicaManager.detenerHeartbeat()
        super.onDestroy()
        _binding = null
    }
}

data class UserTrabajador(var usuario: String, val contrasena: String, var acceso: Boolean, val id: String, val rol: String)
