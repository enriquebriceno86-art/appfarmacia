package com.app.administradorfarmadon.ActivityFragmentos.Fragmentos

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.administradorfarmadon.ActivitysPedidosTienda.ActivitysDetallesPedidos
import com.app.administradorfarmadon.ActivitysPedidosTienda.Clases.AdapterPedidosNuevos
import com.app.administradorfarmadon.ActivitysPedidosTienda.Clases.PedidoInformacion
import com.app.administradorfarmadon.R
import com.app.administradorfarmadon.databinding.FragmentFragmentosPedidosBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class FragmentosPedidos : Fragment() {

    private var _binding: FragmentFragmentosPedidosBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PedidosViewModel by activityViewModels()

    private val listaPedidos = ArrayList<PedidoInformacion>()
    private val listaPedidosFiltrados = ArrayList<PedidoInformacion>()

    private lateinit var adapterPedidosNuevos: AdapterPedidosNuevos
    private var pedidosRef: DatabaseReference? = null
    private var pedidosListener: ValueEventListener? = null
    private var datosActivos = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFragmentosPedidosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        adapterPedidosNuevos = AdapterPedidosNuevos(arrayListOf()) { pedido ->
            val intent = Intent(requireContext(), ActivitysDetallesPedidos::class.java)
            intent.putExtra("idPedido", pedido.idPedido)
            startActivity(intent)
        }

        binding.rvpedidos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvpedidos.adapter = adapterPedidosNuevos

        clickchip()
        seleccionarFiltro(viewModel.filtroActual)
    }

    override fun onResume() {
        super.onResume()
        activarDatosSiVisible()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            desactivarDatos()
        } else {
            activarDatosSiVisible()
        }
    }

    private fun obtenerPedidos() {
        pedidosRef = FirebaseDatabase.getInstance()
            .reference
            .child("PedidosRecibidos")

        pedidosListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (_binding == null) return
                    listaPedidos.clear()

                    for (pedidoSnap in snapshot.children) {
                        val pedido = pedidoSnap.child("informacion")
                            .getValue(PedidoInformacion::class.java)

                        if (pedido != null) {
                            listaPedidos.add(pedido)
                        }
                    }

                    filtrarPedidos(viewModel.filtroActual)
                }

                override fun onCancelled(error: DatabaseError) {
                    if (_binding == null) return
                    Toast.makeText(requireContext(), "Error al leer pedidos", Toast.LENGTH_SHORT).show()
                }
            }

        pedidosRef?.addValueEventListener(pedidosListener!!)
    }

    private fun activarDatosSiVisible() {
        if (!isAdded || isHidden || datosActivos) return
        datosActivos = true
        obtenerPedidos()
    }

    private fun desactivarDatos() {
        pedidosListener?.let { pedidosRef?.removeEventListener(it) }
        pedidosListener = null
        pedidosRef = null
        datosActivos = false
    }

    private fun clickchip() {
        binding.cardPorAtender.setOnClickListener {
            seleccionarFiltro("por_atender")
        }

        binding.cardEnCamino.setOnClickListener {
            seleccionarFiltro("en_camino")
        }

        binding.cardCompletados.setOnClickListener {
            seleccionarFiltro("completados")
        }
    }

    private fun seleccionarFiltro(filtro: String) {
        if (_binding == null) return
        binding.cardPorAtender.setCardBackgroundColor(Color.parseColor("#F3F3F3"))
        binding.cardEnCamino.setCardBackgroundColor(Color.parseColor("#F3F3F3"))
        binding.cardCompletados.setCardBackgroundColor(Color.parseColor("#F3F3F3"))

        binding.txtchipporatender.setTextColor(Color.parseColor("#BABABA"))
        binding.txtchipencamino.setTextColor(Color.parseColor("#BABABA"))
        binding.txtchipcompletado.setTextColor(Color.parseColor("#BABABA"))

        when (filtro) {
            "por_atender" -> {
                binding.cardPorAtender.setCardBackgroundColor(Color.parseColor("#F28297"))
                binding.txtchipporatender.setTextColor(Color.WHITE)
            }

            "en_camino" -> {
                binding.cardEnCamino.setCardBackgroundColor(Color.parseColor("#73A5FB"))
                binding.txtchipencamino.setTextColor(Color.WHITE)
            }

            "completados" -> {
                binding.cardCompletados.setCardBackgroundColor(Color.parseColor("#57F4A0"))
                binding.txtchipcompletado.setTextColor(Color.WHITE)
            }
        }

        viewModel.filtroActual = filtro
        filtrarPedidos(filtro)
    }

    private fun filtrarPedidos(filtro: String) {
        if (_binding == null) return
        listaPedidosFiltrados.clear()

        for (pedido in listaPedidos) {
            val estado = pedido.estado.trim().lowercase()

            when (filtro) {
                "por_atender" -> {
                    if (
                        estado == "pendiente" ||
                        estado == "aceptado" ||
                        estado == "preparando"
                    ) {
                        listaPedidosFiltrados.add(pedido)
                    }
                }

                "en_camino" -> {
                    if (
                        estado == "encamino" ||
                        estado == "en camino"
                    ) {
                        listaPedidosFiltrados.add(pedido)
                    }
                }

                "completados" -> {
                    if (
                        estado == "entregado" ||
                        estado == "completado"
                    ) {
                        listaPedidosFiltrados.add(pedido)
                    }
                }
            }
        }

        listaPedidosFiltrados.sortByDescending { it.fechaHora }

        adapterPedidosNuevos.actualizarLista(listaPedidosFiltrados)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}


class PedidosViewModel : ViewModel() {
    var filtroActual: String = "por_atender"
}
