package com.app.administradorfarmadon.ActivitysUsuarios

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.ClasesDatabase.MovimientoLogger
import com.app.administradorfarmadon.ClasesDatabase.SessionManager
import com.app.administradorfarmadon.ClasesDatabase.Trabajadores
import com.app.administradorfarmadon.R
import com.app.administradorfarmadon.databinding.ActivityUserBinding
import com.google.firebase.database.ServerValue
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class UserActivity : AppCompatActivity() {

    var activitypage=1

    private lateinit var binding: ActivityUserBinding

    private lateinit var adapterUsuarios: AdapterUsuarioRecycler
    private lateinit var adapterSolicitudes: AdapterSolicitudesRecycler

    private val listaUsuarios = mutableListOf<Trabajadores>()
    private val listaSolicitudes = mutableListOf<SolicitudTrabajador>()

    private val database by lazy { FirebaseDatabase.getInstance() }
    private val trabajadoresRef by lazy { database.getReference("trabajadores") }
    private val solicitudesRef by lazy { database.getReference("solicitudregistrotrabajador") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        SessionManager.cargarSesion(this)

        configurarInsets()
        configurarUI()
        configurarRecyclers()
        configurarBackNavigation()
        cargarUsuarios()
        cargarSolicitudes()
    }

    private fun configurarBackNavigation() {
        onBackPressedDispatcher.addCallback(this) {
            if (UsuarioUiRules.debeCerrarActividad(activitypage)) {
                finish()
            } else {
                activitypage = 1
                mostrarPantallaSolicitudes(false)
            }
        }
    }

    private fun configurarUI() {
        configurarBotonAtras()
        configurarBuscador()
        configurarCambioDePantallas()
    }

    private fun configurarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }

    private fun configurarRecyclers() {
        adapterUsuarios = AdapterUsuarioRecycler(listaUsuarios) { usuario ->
            cambiarEstadoUsuario(usuario)
        }

        adapterSolicitudes = AdapterSolicitudesRecycler(
            listaSolicitudes,
            onAprobar = { solicitud -> confirmarAprobacion(solicitud) },
            onRechazar = { solicitud -> confirmarRechazo(solicitud) }
        )

        val isTablet = resources.configuration.smallestScreenWidthDp >= 720

        binding.reciclerlistausuario.apply {
            layoutManager = if (isTablet) GridLayoutManager(this@UserActivity, 2) else LinearLayoutManager(this@UserActivity)
            adapter = adapterUsuarios
        }

        binding.recyclerSolicitudes.apply {
            layoutManager = if (isTablet) GridLayoutManager(this@UserActivity, 2) else LinearLayoutManager(this@UserActivity)
            adapter = adapterSolicitudes
        }

        activarSwipeUsuarios()
        configurarSearchView()
    }

    private fun cambiarEstadoUsuario(usuario: Trabajadores) {
        val decision = UsuarioAccessRules.decidirCambioAcceso(
            usuarioObjetivo = usuario,
            contexto = UsuarioAccessContext(
                idUsuarioActual = SessionManager.idCajera,
                rolUsuarioActual = SessionManager.rol,
                nombreOperador = SessionManager.nombreCajera.trim().ifBlank { "Administrador" }
            )
        )
        if (!decision.permitido) {
            mostrarToast(decision.mensajeError.orEmpty())
            return
        }
        val nombreOperador = SessionManager.nombreCajera.trim().ifBlank { "Administrador" }
        val idOperador = SessionManager.idCajera.trim()
        val timestampActualizacion = System.currentTimeMillis()
        val payload = UsuarioPayloadRules.construirPayloadCambioAcceso(
            nuevoEstado = decision.nuevoEstado,
            idOperador = idOperador,
            nombreOperador = nombreOperador,
            timestampActualizacion = timestampActualizacion,
            motivoCambio = decision.motivoCambio
        )
        trabajadoresRef.child(usuario.id)
            .updateChildren(payload.updates)
            .addOnSuccessListener {
                mostrarToast(
                    if (decision.nuevoEstado == "false") {
                        "Usuario bloqueado"
                    } else {
                        "Usuario activado"
                    }
                )
                MovimientoLogger.registrarConSesion(
                    context = this,
                    tipo = "usuario_estado_acceso_actualizado",
                    modulo = "usuarios",
                    titulo = if (decision.nuevoEstado == "false") "Usuario bloqueado" else "Usuario activado",
                    descripcion = decision.descripcionCambio,
                    referenciaId = usuario.id,
                    extra = mapOf(
                        "accesoAnterior" to decision.accesoActual.toString(),
                        "accesoNuevo" to decision.nuevoEstado,
                        "usuarioObjetivoId" to usuario.id,
                        "usuarioObjetivoNombre" to usuario.usuario,
                        "motivo" to decision.motivoCambio
                    )
                )
            }
            .addOnFailureListener {
                mostrarToast("Error al actualizar")
            }
    }

    private fun aprobarSolicitud(solicitud: SolicitudTrabajador) {
        val decision = UsuarioAccessRules.decidirAprobacionSolicitud(solicitud)
        if (!decision.permitido) {
            mostrarToast(decision.mensajeError.orEmpty())
            return
        }
        val payload = UsuarioPayloadRules.construirPayloadAprobacionSolicitud(solicitud)
        database.reference.updateChildren(payload.updates)
            .addOnSuccessListener {
                mostrarToast("Solicitud aceptada")
            }
            .addOnFailureListener {
                mostrarToast("Error al aprobar solicitud")
            }
    }

    private fun confirmarAprobacion(solicitud: SolicitudTrabajador) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Aceptar solicitud")
            .setMessage("¿Deseas aceptar a ${solicitud.usuario}?")
            .setPositiveButton("Aceptar") { _, _ ->
                aprobarSolicitud(solicitud)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarRechazo(solicitud: SolicitudTrabajador) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Rechazar solicitud")
            .setMessage("¿Deseas rechazar a ${solicitud.usuario}?")
            .setPositiveButton("Rechazar") { _, _ ->
                rechazarSolicitud(solicitud)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun rechazarSolicitud(solicitud: SolicitudTrabajador) {
        val decision = UsuarioAccessRules.decidirRechazoSolicitud(solicitud)
        if (!decision.permitido) {
            mostrarToast(decision.mensajeError.orEmpty())
            return
        }
        val payload = UsuarioPayloadRules.construirPayloadRechazoSolicitud(solicitud)
        solicitudesRef.child(payload.id).removeValue()
            .addOnSuccessListener {
                mostrarToast("Solicitud rechazada")
            }
            .addOnFailureListener {
                mostrarToast("Error al rechazar")
            }
    }

    private fun cargarUsuarios() {
        trabajadoresRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val usuarios = snapshot.children.mapNotNull { trabajadorSnapshot ->
                    construirTrabajadorSeguro(trabajadorSnapshot)
                }
                aplicarUsuariosCargados(usuarios)
            }

            override fun onCancelled(error: DatabaseError) {
                mostrarToast("Error al cargar usuarios")
            }
        })
    }

    private fun construirTrabajadorSeguro(snapshot: DataSnapshot): Trabajadores? {
        val trabajadorSeguro = UsuarioRemoteRules.construirTrabajadorRemoto(
            snapshotId = snapshot.child("id").value?.toString().orEmpty().ifBlank { snapshot.key.orEmpty() },
            usuario = snapshot.child("usuario").value?.toString().orEmpty(),
            rol = snapshot.child("rol").value?.toString().orEmpty(),
            contrasena = snapshot.child("contrasena").value?.toString().orEmpty(),
            acceso = snapshot.child("acceso").value,
            documento = snapshot.child("documento").value?.toString().orEmpty()
        ) ?: return null
        return Trabajadores(
            id = trabajadorSeguro.id,
            usuario = trabajadorSeguro.usuario,
            rol = trabajadorSeguro.rol,
            contrasena = trabajadorSeguro.contrasena,
            acceso = trabajadorSeguro.accesoNormalizado,
            documento = trabajadorSeguro.documento
        )
    }

    private fun normalizarAcceso(valor: Any?): String {
        return UsuarioAccessRules.normalizarAcceso(valor)
    }

    private fun cargarSolicitudes() {
        solicitudesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val solicitudes = snapshot.children.mapNotNull { child ->
                    construirSolicitudSegura(child)
                }
                aplicarSolicitudesCargadas(solicitudes)
            }

            override fun onCancelled(error: DatabaseError) {
                binding.materialButton4.visibility = View.GONE
                mostrarToast("Error al verificar solicitudes")
            }
        })
    }

    private fun construirSolicitudSegura(snapshot: DataSnapshot): SolicitudTrabajador? {
        val solicitudSegura = UsuarioRemoteRules.construirSolicitudRemota(
            snapshotId = snapshot.key.orEmpty(),
            usuario = snapshot.child("usuario").value?.toString().orEmpty(),
            rol = snapshot.child("rol").value?.toString().orEmpty(),
            contrasena = snapshot.child("contrasena").value?.toString().orEmpty(),
            documento = snapshot.child("documento").value?.toString().orEmpty(),
            estado = snapshot.child("estado").value?.toString().orEmpty().ifBlank { "Pendiente" }
        ) ?: return null

        return SolicitudTrabajador(
            id = solicitudSegura.id,
            usuario = solicitudSegura.usuario,
            rol = solicitudSegura.rol,
            documento = solicitudSegura.documento,
            contrasena = solicitudSegura.contrasena,
            estado = solicitudSegura.estado
        )
    }

    private fun aplicarUsuariosCargados(usuarios: List<Trabajadores>) {
        listaUsuarios.clear()
        listaUsuarios.addAll(usuarios)
        adapterUsuarios.filtrarBusqueda(binding.searchview.query?.toString().orEmpty())
    }

    private fun aplicarSolicitudesCargadas(solicitudes: List<SolicitudTrabajador>) {
        listaSolicitudes.clear()
        listaSolicitudes.addAll(solicitudes)
        adapterSolicitudes.notifyDataSetChanged()

        val estadoUi = UsuarioRemoteRules.resolverEstadoSolicitudes(
            cantidadSolicitudes = listaSolicitudes.size,
            activityPage = activitypage
        )

        if (estadoUi.debeCerrarPantallaSolicitudes) {
            activitypage = 1
            mostrarPantallaSolicitudes(false)
            mostrarToast("Ya no hay solicitudes pendientes")
        } else {
            binding.materialButton4.visibility =
                if (estadoUi.mostrarBotonSolicitudes) View.VISIBLE else View.GONE
        }
    }

    private fun activarSwipeUsuarios() {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val usuario = adapterUsuarios.getUsuario(position)

                if (usuario.id.isNotEmpty()) {
                    trabajadoresRef.child(usuario.id).removeValue()
                    mostrarToast("Usuario eliminado")
                } else {
                    adapterUsuarios.notifyItemChanged(position)
                    mostrarToast("No se pudo eliminar el usuario")
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val paint = Paint().apply { color = Color.RED }

                c.drawRect(
                    itemView.left.toFloat(),
                    itemView.top.toFloat(),
                    itemView.left + dX,
                    itemView.bottom.toFloat(),
                    paint
                )

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }

        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.reciclerlistausuario)
    }

    private fun configurarBuscador() {
        binding.imageViewsearch.setOnClickListener { cambiarEstadoBuscador(true) }
        binding.btnCerrarSearch.setOnClickListener { cambiarEstadoBuscador(false) }
    }

    private fun cambiarEstadoBuscador(mostrar: Boolean) {
        val estadoUi = UsuarioUiRules.construirEstadoBuscador(mostrar)
        binding.materialCardView3.visibility = if (estadoUi.mostrarBuscador) View.VISIBLE else View.GONE
        binding.imageViewsearch.visibility = if (estadoUi.mostrarIconoBusqueda) View.VISIBLE else View.GONE

        if (mostrar) {
            binding.searchview.isIconified = false
            binding.searchview.requestFocus()
            mostrarTeclado()
        } else {
            binding.searchview.setQuery("", false)
            binding.searchview.clearFocus()
            adapterUsuarios.filtrarBusqueda("")
            ocultarTeclado()
        }
    }

    private fun configurarSearchView() {
        binding.searchview.setOnQueryTextListener(
            object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextChange(texto: String?) = filtrarUsuarios(texto)
                override fun onQueryTextSubmit(texto: String?) = filtrarUsuarios(texto)
            }
        )
    }

    private fun filtrarUsuarios(texto: String?): Boolean {
        adapterUsuarios.filtrarBusqueda(texto.orEmpty())
        return true
    }

    private fun configurarCambioDePantallas() {
        binding.materialButton4.setOnClickListener {
            activitypage = 2
            mostrarPantallaSolicitudes(true)
        }

        binding.btnBackSolicitudes.setOnClickListener {
            activitypage = 1
            mostrarPantallaSolicitudes(false)
        }
    }

    private fun mostrarPantallaSolicitudes(mostrarSolicitudes: Boolean) {
        if (mostrarSolicitudes) {
            cambiarEstadoBuscador(false)
        }

        val estadoUi = UsuarioUiRules.construirEstadoPantallaSolicitudes(
            mostrarSolicitudes = mostrarSolicitudes,
            haySolicitudes = listaSolicitudes.isNotEmpty()
        )

        binding.constraintsolicitudes.visibility =
            if (estadoUi.mostrarSolicitudes) View.VISIBLE else View.GONE

        binding.constraintlistausuarios.visibility =
            if (estadoUi.mostrarListaUsuarios) View.VISIBLE else View.GONE

        binding.materialButton4.visibility =
            if (estadoUi.mostrarBotonSolicitudes) View.VISIBLE else View.GONE
    }

    private fun configurarBotonAtras() {
        binding.imageViewatrasfinish.setOnClickListener { finish() }
    }

    private fun mostrarTeclado() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.searchview, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun ocultarTeclado() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchview.windowToken, 0)
    }

    private fun mostrarToast(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }


    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {

        val view = currentFocus

        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()
        }

        return super.dispatchTouchEvent(ev)
    }


}



data class SolicitudTrabajador(
    val id: String = "",
    val usuario: String = "",
    val rol: String = "",
    val documento: String = "",
    val contrasena: String = "",
    val estado: String = "Pendiente"
)

