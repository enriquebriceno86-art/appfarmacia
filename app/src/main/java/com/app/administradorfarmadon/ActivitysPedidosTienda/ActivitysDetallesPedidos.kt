package com.app.administradorfarmadon.ActivitysPedidosTienda

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.administradorfarmadon.ActivitysPedidosTienda.Clases.AdapterProductosPedidoDetalle
import com.app.administradorfarmadon.ActivitysPedidosTienda.Clases.PedidoInformacion
import com.app.administradorfarmadon.ActivitysPedidosTienda.Clases.PedidoProductoDetalle
import com.app.administradorfarmadon.databinding.ActivityActivitysDetallesPedidosBinding
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivitysDetallesPedidos : AppCompatActivity() {

    private lateinit var binding: ActivityActivitysDetallesPedidosBinding
    private lateinit var adapterProductos: AdapterProductosPedidoDetalle
    private val listaProductos = mutableListOf<PedidoProductoDetalle>()
    private var idPedido: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityActivitysDetallesPedidosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        idPedido = intent.getStringExtra("idPedido").orEmpty()
        if (idPedido.isBlank()) {
            Toast.makeText(this, "No se pudo abrir el pedido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.toolbarDetallePedido.setNavigationOnClickListener { finish() }
        adapterProductos = AdapterProductosPedidoDetalle(listaProductos)
        binding.rvProductosPedidoDetalle.layoutManager = LinearLayoutManager(this)
        binding.rvProductosPedidoDetalle.adapter = adapterProductos

        cargarInformacionPedido()
        cargarProductosPedido()
    }

    private fun cargarInformacionPedido() {
        FirebaseDatabase.getInstance().reference
            .child("PedidosRecibidos")
            .child(idPedido)
            .child("informacion")
            .get()
            .addOnSuccessListener { snapshot ->
                val pedido = snapshot.getValue(PedidoInformacion::class.java)
                if (pedido != null) {
                    renderizarInformacionPedido(pedido)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "No se pudo cargar la información del pedido", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarProductosPedido() {
        FirebaseDatabase.getInstance().reference
            .child("PedidosRecibidos")
            .child(idPedido)
            .child("productos")
            .get()
            .addOnSuccessListener { snapshot ->
                val productos = snapshot.children.mapNotNull { it.getValue(PedidoProductoDetalle::class.java) }
                adapterProductos.actualizar(productos)
                binding.tvProductosVaciosPedidoDetalle.isVisible = productos.isEmpty()
            }
            .addOnFailureListener {
                Toast.makeText(this, "No se pudieron cargar los productos del pedido", Toast.LENGTH_SHORT).show()
            }
    }

    private fun renderizarInformacionPedido(pedido: PedidoInformacion) {
        binding.toolbarDetallePedido.title = "Pedido #${pedido.idPedido.takeLast(5)}"
        binding.tvClientePedidoDetalle.text = pedido.nombreCliente.ifBlank { "Cliente no informado" }
        binding.tvEstadoPedidoDetalle.text = pedido.estado.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }
        binding.tvEstadoPedidoDetalle.setTextColor(colorEstado(pedido.estado))
        binding.tvFechaPedidoDetalle.text = formatearFechaHora(pedido.fechaHora)
        binding.tvDireccionPedidoDetalle.text = pedido.direccion.ifBlank { "Dirección no informada" }
        binding.tvPagoPedidoDetalle.text = pedido.tipoPago.ifBlank { "Método de pago no informado" }
        binding.tvTotalPedidoDetalle.text = pedido.total.ifBlank { "Bs 0.00" }
        binding.tvTelefonoPedidoDetalle.text = pedido.telefonoCliente.ifBlank { "No informado" }

        val comentario = pedido.comentario.trim()
        binding.layoutComentarioPedidoDetalle.isVisible = comentario.isNotBlank()
        binding.tvComentarioPedidoDetalle.text = comentario
    }

    private fun colorEstado(estado: String): Int {
        return when (estado.trim().lowercase()) {
            "pendiente" -> Color.parseColor("#C2410C")
            "aceptado", "preparando" -> Color.parseColor("#1F8B62")
            "encamino", "en camino" -> Color.parseColor("#2563EB")
            "entregado", "completado" -> Color.parseColor("#6D4C41")
            else -> Color.parseColor("#475467")
        }
    }

    private fun formatearFechaHora(timestamp: Long): String {
        if (timestamp <= 0L) return "Fecha no informada"
        val formato = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault())
        return formato.format(Date(timestamp))
    }
}
