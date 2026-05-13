package com.app.administradorfarmadon.ActivitysCaja

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.toMoldeProductos
import com.app.administradorfarmadon.ActivityInventario.ProductUtils
import com.app.administradorfarmadon.ActivitysCaja.ReciclerCajas.AdapterProductosSustitucion
import com.app.administradorfarmadon.databinding.ActivitySeleccionarProductoSustitucionBinding
import com.google.firebase.database.FirebaseDatabase
import java.util.Locale

class ActivitySeleccionarProductoSustitucion : AppCompatActivity() {

    companion object {
        const val EXTRA_PRODUCTO_INDICE_SELECCIONADO = "extra_producto_indice_seleccionado"
        const val EXTRA_PRODUCTO_INDICE_RESULTADO = "extra_producto_indice_resultado"
    }

    private lateinit var binding: ActivitySeleccionarProductoSustitucionBinding
    private lateinit var adapter: AdapterProductosSustitucion

    private val database by lazy { FirebaseDatabase.getInstance() }
    private val productos = mutableListOf<MoldeProductos>()
    private var categoriaSeleccionada = "Todas"
    private var indiceSeleccionadoActual = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeleccionarProductoSustitucionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        indiceSeleccionadoActual = intent.getStringExtra(EXTRA_PRODUCTO_INDICE_SELECCIONADO).orEmpty().trim()

        configurarRecycler()
        configurarInsets()
        configurarBuscador()
        configurarTabs()
        configurarAcciones()
        cargarProductos()
    }

    private fun configurarInsets() {
        val paddingTopBase = binding.headerSustitucion.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(binding.headerSustitucion) { view, windowInsets ->
            val topInset = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.updatePadding(top = paddingTopBase + topInset)
            windowInsets
        }
        ViewCompat.requestApplyInsets(binding.root)
    }

    private fun configurarRecycler() {
        adapter = AdapterProductosSustitucion { producto ->
            setResult(
                RESULT_OK,
                Intent().putExtra(EXTRA_PRODUCTO_INDICE_RESULTADO, producto.indice.trim())
            )
            finish()
        }

        binding.recyclerProductosSustitucion.layoutManager = GridLayoutManager(
            this,
            if (resources.configuration.smallestScreenWidthDp >= 720) 4 else 1
        )
        binding.recyclerProductosSustitucion.adapter = adapter
    }

    private fun configurarBuscador() {
        binding.etBuscarSustitucion.doAfterTextChanged {
            aplicarFiltros()
        }
    }

    private fun configurarTabs() {
        binding.tabLayoutCategoriasSustitucion.addOnTabSelectedListener(
            object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                    categoriaSeleccionada = tab?.text?.toString().orEmpty().ifBlank { "Todas" }
                    aplicarFiltros()
                }

                override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) = Unit
                override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) = Unit
            }
        )
    }

    private fun configurarAcciones() {
        binding.layoutBusquedaSustitucion.setStartIconOnClickListener { finish() }
        binding.layoutBusquedaSustitucion.setEndIconOnClickListener {
            binding.etBuscarSustitucion.requestFocus()
            binding.etBuscarSustitucion.setSelection(binding.etBuscarSustitucion.text?.length ?: 0)
        }
    }

    private fun cargarProductos() {
        binding.progressSustitucion.visibility = android.view.View.VISIBLE
        binding.recyclerProductosSustitucion.visibility = android.view.View.GONE
        binding.layoutSinProductosSustitucion.visibility = android.view.View.GONE

        database.reference.child("Inventario").child("Productos").get()
            .addOnSuccessListener { snapshot ->
                productos.clear()
                productos.addAll(
                    snapshot.children.mapNotNull { it.toMoldeProductos() }
                        .filter { producto ->
                            producto.indice.trim().isNotBlank() &&
                                ProductUtils.stockVendibleProducto(producto).toInt() > 0
                        }
                        .sortedBy { it.nombre.trim().lowercase(Locale.getDefault()) }
                )
                actualizarTabsCategorias()
                aplicarFiltros()
            }
            .addOnFailureListener {
                productos.clear()
                actualizarTabsCategorias()
                aplicarFiltros()
            }
    }

    private fun actualizarTabsCategorias() {
        val categorias = mutableListOf("Todas")
        categorias.addAll(
            productos.map { it.categoria.ifBlank { "Sin categoría" } }
                .distinct()
                .sortedBy { it.lowercase(Locale.getDefault()) }
        )

        binding.tabLayoutCategoriasSustitucion.removeAllTabs()
        categorias.forEachIndexed { index, categoria ->
            binding.tabLayoutCategoriasSustitucion.addTab(
                binding.tabLayoutCategoriasSustitucion.newTab().setText(categoria),
                index == 0
            )
        }
        categoriaSeleccionada = "Todas"
    }

    private fun aplicarFiltros() {
        val query = normalizarTexto(binding.etBuscarSustitucion.text?.toString().orEmpty())
        val filtrados = productos.filter { producto ->
            val coincideCategoria = categoriaSeleccionada == "Todas" ||
                producto.categoria.ifBlank { "Sin categoría" } == categoriaSeleccionada
            val coincideTexto = query.isBlank() ||
                normalizarTexto(producto.nombre).contains(query) ||
                normalizarTexto(producto.categoria).contains(query)
            coincideCategoria && coincideTexto
        }

        binding.progressSustitucion.visibility = android.view.View.GONE
        binding.recyclerProductosSustitucion.visibility =
            if (filtrados.isNotEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        binding.layoutSinProductosSustitucion.visibility =
            if (filtrados.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        binding.tvMensajeVacioSustitucion.text = if (productos.isEmpty()) {
            "No hay productos con stock vendible para usar como sustitución."
        } else {
            "Prueba con otra búsqueda o categoría."
        }
        adapter.actualizarProductos(filtrados, indiceSeleccionadoActual)
    }

    private fun normalizarTexto(texto: String): String {
        return ProductUtils.normalizarTextoBusqueda(texto)
    }
}
