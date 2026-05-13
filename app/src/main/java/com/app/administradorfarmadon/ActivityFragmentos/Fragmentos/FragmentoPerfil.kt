package com.app.administradorfarmadon.ActivityFragmentos.Fragmentos

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.administradorfarmadon.ActivitysPerfilItem.DatosdelaTienda
import com.app.administradorfarmadon.ActivitysPerfilItem.HorariosActivity
import com.app.administradorfarmadon.ActivitysPerfilItem.ListaReportesCaja
import com.app.administradorfarmadon.ActivitysPerfilItem.TiposPagoActivity
import com.app.administradorfarmadon.ActivitysUsuarios.UserActivity
import com.app.administradorfarmadon.ClasesDatabase.PreferenciasFeedbackCaja
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

        binding.usuariosClick.setOnClickListener {
            abrirActivity(UserActivity::class.java)
        }

        binding.horariosClick.setOnClickListener {
            abrirActivity(HorariosActivity::class.java)
        }

        binding.tiendaClick.setOnClickListener {
            abrirActivity(DatosdelaTienda::class.java)
        }

        binding.tiposPagoClick.setOnClickListener {
            abrirActivity(TiposPagoActivity::class.java)
        }

        binding.reportesClick.setOnClickListener {
            abrirActivity(ListaReportesCaja::class.java)
        }

        binding.switchFeedbackCaja.isChecked = PreferenciasFeedbackCaja.estaActivo(requireContext())
        binding.switchFeedbackCaja.setOnCheckedChangeListener { _, isChecked ->
            PreferenciasFeedbackCaja.setActivo(requireContext(), isChecked)
        }

        binding.salirClick.setOnClickListener {
            salir()
        }


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
