package com.app.administradorfarmadon.ActivitysCaja

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import com.app.administradorfarmadon.ClasesDatabase.obtenerDoubleFlexible
import com.app.administradorfarmadon.ClasesDatabase.obtenerTexto
import com.app.administradorfarmadon.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import java.util.Locale

class DetalleVentaActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FECHA_VENTA = "extra_fecha_venta"
        const val EXTRA_ID_VENTA = "extra_id_venta"
    }

    private data class ResumenCambioVentaDetalle(
        val lineas: List<String> = emptyList()
    )

    private lateinit var rvProductos: RecyclerView
    private lateinit var rvCambios: RecyclerView
    private lateinit var tvVentaNumero: TextView
    private lateinit var tvVentaFechaHora: TextView
    private lateinit var tvVentaMetodoPago: TextView
    private lateinit var tvVentaEstado: TextView
    private lateinit var tvVentaTipoComprobante: TextView
    private lateinit var tvVentaMontoRecibido: TextView
    private lateinit var tvVentaVuelto: TextView
    private lateinit var tvVentaTotal: TextView
    private lateinit var tvSinCambios: TextView
    private lateinit var productosAdapter: ProductoDetalleAdapter
    private lateinit var cambiosAdapter: CambioVentaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_venta)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        inicializarVistas()
        configurarToolbar()
        configurarListas()
        cargarVenta()
    }

    private fun inicializarVistas() {
        rvProductos = findViewById(R.id.rvProductosDetalleVenta)
        rvCambios = findViewById(R.id.rvCambiosVenta)
        tvVentaNumero = findViewById(R.id.tvVentaNumeroDetalle)
        tvVentaFechaHora = findViewById(R.id.tvVentaFechaHoraDetalle)
        tvVentaMetodoPago = findViewById(R.id.tvVentaMetodoPagoDetalle)
        tvVentaEstado = findViewById(R.id.tvVentaEstadoDetalle)
        tvVentaTipoComprobante = findViewById(R.id.tvVentaTipoComprobanteDetalle)
        tvVentaMontoRecibido = findViewById(R.id.tvVentaMontoRecibidoDetalle)
        tvVentaVuelto = findViewById(R.id.tvVentaVueltoDetalle)
        tvVentaTotal = findViewById(R.id.tvVentaTotalDetalle)
        tvSinCambios = findViewById(R.id.tvSinCambiosVenta)
    }

    private fun configurarToolbar() {
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarDetalleVenta).setNavigationOnClickListener {
            finish()
        }
    }

    private fun configurarListas() {
        productosAdapter = ProductoDetalleAdapter()
        cambiosAdapter = CambioVentaAdapter()
        rvProductos.layoutManager = LinearLayoutManager(this)
        rvProductos.adapter = productosAdapter
        rvCambios.layoutManager = LinearLayoutManager(this)
        rvCambios.adapter = cambiosAdapter
    }

    private fun cargarVenta() {
        val fecha = intent.getStringExtra(EXTRA_FECHA_VENTA).orEmpty()
        val idVenta = intent.getStringExtra(EXTRA_ID_VENTA).orEmpty()
        if (fecha.isBlank() || idVenta.isBlank()) {
            Toast.makeText(this, "No se pudo abrir el detalle de la venta", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        FirebaseDatabase.getInstance().reference
            .child("Ventas")
            .child(fecha)
            .child(idVenta)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    Toast.makeText(this, "No se encontró la venta", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                val infoVentaOriginal = snapshot.child("infoVenta").getValue(InfoVenta::class.java)
                if (infoVentaOriginal == null) {
                    Toast.makeText(this, "La venta no tiene información válida", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                val infoVenta = HistorialVentasDetailRules.construirInfoVentaDetalle(
                    VentaDetalleRemoteData(
                        idVentaFallback = snapshot.key ?: idVenta,
                        infoVentaOriginal = infoVentaOriginal,
                        timestampVentaServidor = snapshot.child("infoVenta").child("timestampVentaServidor").getValue(Long::class.java)
                            ?: snapshot.child("infoVenta").child("timestampServidor").getValue(Long::class.java)
                            ?: 0L
                    )
                )
                val productos = HistorialVentasDetailRules.construirListaProductosDetalle(
                    snapshot.child("productos").children.map { it.getValue(ProductoCaja::class.java) }
                )
                val resumenCambio = construirResumenCambioVenta(snapshot)

                renderizarInfo(infoVenta)
                productosAdapter.actualizar(productos)
                cambiosAdapter.actualizar(resumenCambio.lineas)
                val hayCambios = resumenCambio.lineas.isNotEmpty()
                rvCambios.visibility = if (hayCambios) View.VISIBLE else View.GONE
                tvSinCambios.visibility = if (hayCambios) View.GONE else View.VISIBLE
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, error.message ?: "Error al cargar la venta", Toast.LENGTH_LONG).show()
                finish()
            }
    }

    private fun renderizarInfo(infoVenta: InfoVenta) {
        val tipoComprobante = infoVenta.tipoComprobante.ifBlank { "Sin comprobante" }
        val totalVenta = infoVenta.total.toDoubleOrNull() ?: 0.0
        val montoRecibido = infoVenta.montoRecibido.toDoubleOrNull() ?: 0.0
        val vuelto = infoVenta.vuelto.toDoubleOrNull() ?: 0.0

        tvVentaNumero.text = "Venta #${infoVenta.id.takeLast(8)}"
        tvVentaFechaHora.text = "${infoVenta.fecha} • ${infoVenta.hora}"
        tvVentaMetodoPago.text = infoVenta.metodoPago.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
        tvVentaEstado.text = infoVenta.estado.ifBlank { "Sin estado" }
        tvVentaTipoComprobante.text = tipoComprobante.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
        tvVentaMontoRecibido.text = MonedaHelper.formatear(montoRecibido)
        tvVentaVuelto.text = MonedaHelper.formatear(vuelto)
        tvVentaTotal.text = MonedaHelper.formatear(totalVenta)
    }

    private fun construirResumenCambioVenta(snapshot: DataSnapshot): ResumenCambioVentaDetalle {
        val devolucionSnap = snapshot.child("devolucion")
        if (!devolucionSnap.exists()) return ResumenCambioVentaDetalle()

        val eventos = if (devolucionSnap.child("eventos").exists()) {
            devolucionSnap.child("eventos").children.toList()
        } else {
            listOf(devolucionSnap)
        }

        val lineas = eventos.mapNotNull { eventoSnap ->
            val tipoResolucion = eventoSnap.child("tipoResolucion").obtenerTexto()
                .ifBlank { devolucionSnap.child("tipoResolucion").obtenerTexto() }
                .trim()
                .lowercase(Locale.getDefault())
            if (tipoResolucion != "cambio_producto" && tipoResolucion != "dinero_devuelto") return@mapNotNull null

            val sustituto = eventoSnap.child("sustitucion").child("nombre").obtenerTexto()
                .ifBlank { devolucionSnap.child("sustitucionUltima").child("nombre").obtenerTexto() }
            val medioPago = eventoSnap.child("pagoDiferencia").child("metodoPago").obtenerTexto()
                .ifBlank { devolucionSnap.child("metodoPagoDiferenciaUltimo").obtenerTexto() }
            val montoCobrado = eventoSnap.child("ajusteFinanciero")
                .child("montoCobradoCliente")
                .obtenerDoubleFlexible()
                ?: eventoSnap.child("montoCobradoCliente").obtenerDoubleFlexible()
                ?: 0.0
            val montoDevuelto = eventoSnap.child("ajusteFinanciero")
                .child("montoDevueltoCliente")
                .obtenerDoubleFlexible()
                ?: eventoSnap.child("montoDevueltoCliente").obtenerDoubleFlexible()
                ?: eventoSnap.child("montoDevuelto").obtenerDoubleFlexible()
                ?: 0.0
            val motivo = eventoSnap.child("motivo").obtenerTexto()
                .ifBlank { devolucionSnap.child("motivo").obtenerTexto() }

            buildString {
                append(
                    if (tipoResolucion == "cambio_producto") "Cambio de producto"
                    else "Devolución con dinero"
                )
                if (sustituto.isNotBlank()) {
                    append(": ")
                    append(sustituto)
                }
                if (montoCobrado > 0.0) {
                    append(". Diferencia cobrada: ")
                    append(MonedaHelper.formatear(montoCobrado))
                }
                if (montoDevuelto > 0.0) {
                    append(". Monto devuelto: ")
                    append(MonedaHelper.formatear(montoDevuelto))
                }
                if (medioPago.isNotBlank()) {
                    append(". Pago: ")
                    append(medioPago)
                }
                if (motivo.isNotBlank()) {
                    append(". Motivo: ")
                    append(motivo)
                }
            }
        }.distinct()

        return ResumenCambioVentaDetalle(lineas)
    }
}

private class ProductoDetalleAdapter : RecyclerView.Adapter<ProductoDetalleAdapter.ProductoDetalleViewHolder>() {
    private val items = mutableListOf<ProductoCaja>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoDetalleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto_detalle_venta, parent, false)
        return ProductoDetalleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoDetalleViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun actualizar(nuevos: List<ProductoCaja>) {
        items.clear()
        items.addAll(nuevos)
        notifyDataSetChanged()
    }

    class ProductoDetalleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombreProductoDetalleVenta)
        private val tvPresentacion: TextView = itemView.findViewById(R.id.tvPresentacionProductoDetalleVenta)
        private val tvCantidad: TextView = itemView.findViewById(R.id.tvCantidadProductoDetalleVenta)
        private val tvPrecio: TextView = itemView.findViewById(R.id.tvPrecioProductoDetalleVenta)
        private val tvSubtotal: TextView = itemView.findViewById(R.id.tvSubtotalProductoDetalleVenta)

        fun bind(item: ProductoCaja) {
            tvNombre.text = item.nombre.orEmpty().ifBlank { "Producto" }
            tvPresentacion.text = item.presentacion.ifBlank { "Sin presentación" }
            tvCantidad.text = "Cant. ${item.cantidad.ifBlank { "0" }}"
            tvPrecio.text = MonedaHelper.formatear(item.precioUnitario.toDoubleOrNull() ?: 0.0)
            tvSubtotal.text = MonedaHelper.formatear(item.total.toDoubleOrNull() ?: 0.0)
        }
    }
}

private class CambioVentaAdapter : RecyclerView.Adapter<CambioVentaAdapter.CambioVentaViewHolder>() {
    private val items = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CambioVentaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return CambioVentaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CambioVentaViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun actualizar(nuevos: List<String>) {
        items.clear()
        items.addAll(nuevos)
        notifyDataSetChanged()
    }

    class CambioVentaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTexto: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(texto: String) {
            tvTexto.text = texto
        }
    }
}
