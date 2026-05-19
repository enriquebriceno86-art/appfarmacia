package com.app.administradorfarmadon.ActivityFragmentos.Fragmentos

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.administradorfarmadon.ActivitysPerfilItem.ConfiguracionesControlActivity
import com.app.administradorfarmadon.ActivitysPerfilItem.DatosdelaTienda
import com.app.administradorfarmadon.ActivitysPerfilItem.HorariosActivity
import com.app.administradorfarmadon.ActivitysPerfilItem.ListaReportesCaja
import com.app.administradorfarmadon.ActivitysPerfilItem.TiposPagoActivity
import com.app.administradorfarmadon.ActivitysUsuarios.UserActivity
import com.app.administradorfarmadon.ClasesDatabase.SessionManager
import com.app.administradorfarmadon.ClasesDatabase.SesionUnicaManager
import com.app.administradorfarmadon.databinding.FragmentFragmentoPerfilBinding

class FragmentoPerfil : Fragment() {

    private var _binding: FragmentFragmentoPerfilBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFragmentoPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenu()


        binding.salirClick.setOnClickListener {
            salir()
        }

        actualizarInfoUsuario()
        configurarAppBar()
    }

    private fun configurarAppBar() {
        // Forzamos el color blanco y quitamos elevación al scrollear (Material 3)
        binding.appBarLayout.setBackgroundColor(resources.getColor(android.R.color.white, null))
        binding.collapsingToolbar.setCollapsedTitleTextColor(resources.getColor(com.app.administradorfarmadon.R.color.text_primary, null))
        binding.collapsingToolbar.setExpandedTitleColor(resources.getColor(com.app.administradorfarmadon.R.color.text_primary, null))
    }

    private fun setupMenu() {
        // Establecimiento
        binding.tiendaClick.tvTitle.text = "Información de la tienda"
        binding.tiendaClick.ivIcon.setImageResource(com.app.administradorfarmadon.R.drawable.tiendaicon)
        binding.tiendaClick.root.setOnClickListener { abrirActivity(DatosdelaTienda::class.java) }

        binding.horariosClick.tvTitle.text = "Horarios de atención"
        binding.horariosClick.ivIcon.setImageResource(com.app.administradorfarmadon.R.drawable.horariostiendaicon)
        binding.horariosClick.root.setOnClickListener { abrirActivity(HorariosActivity::class.java) }

        binding.tiposPagoClick.tvTitle.text = "Tipos de pago"
        binding.tiposPagoClick.ivIcon.setImageResource(com.app.administradorfarmadon.R.drawable.iconotarjetadepago)
        binding.tiposPagoClick.root.setOnClickListener { abrirActivity(TiposPagoActivity::class.java) }

        // Administración
        binding.usuariosClick.tvTitle.text = "Gestión de usuarios"
        binding.usuariosClick.ivIcon.setImageResource(com.app.administradorfarmadon.R.drawable.usuarioicon)
        binding.usuariosClick.root.setOnClickListener { abrirActivity(UserActivity::class.java) }

        binding.reportesClick.tvTitle.text = "Historial y reportes"
        binding.reportesClick.ivIcon.setImageResource(com.app.administradorfarmadon.R.drawable.reportesicon)
        binding.reportesClick.root.setOnClickListener { abrirActivity(ListaReportesCaja::class.java) }

        // Sistema
        binding.configuracionesClick.tvTitle.text = "Configuraciones"
        binding.configuracionesClick.ivIcon.setImageResource(com.app.administradorfarmadon.R.drawable.configuracionicon)
        binding.configuracionesClick.root.setOnClickListener { 
            abrirActivity(ConfiguracionesControlActivity::class.java)
        }
    }

    private fun actualizarInfoUsuario() {
        val nombre = SessionManager.nombreCajera.ifBlank { "Administrador" }
        binding.tvNombreUsuario.text = nombre
        binding.tvRolUsuario.text = SessionManager.rol.ifBlank { "Gesti\u00f3n Total" }
        
        // Iniciales
        val iniciales = nombre.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.take(1).uppercase() }
        binding.tvIniciales.text = iniciales.ifBlank { "AD" }
    }

    private fun salir() {
        if (_binding == null) return
        binding.salirClick.isEnabled = false

        binding.salirClick.animate()
            .alpha(0f)
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(300L)
            .withEndAction {
                if (!isAdded) return@withEndAction

                val ctx = requireContext()
                val idUsuario = SessionManager.idCajera.trim()

                SesionUnicaManager.detenerHeartbeat()
                if (idUsuario.isNotBlank()) {
                    SesionUnicaManager.cerrarSesionActual(ctx, idUsuario)
                }

                SessionManager.limpiarSesion(ctx)

                val intent = Intent(ctx, com.app.administradorfarmadon.MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
            .start()
    }



    private fun abrirActivity(activity: Class<*>) {
        startActivity(Intent(requireContext(), activity))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
