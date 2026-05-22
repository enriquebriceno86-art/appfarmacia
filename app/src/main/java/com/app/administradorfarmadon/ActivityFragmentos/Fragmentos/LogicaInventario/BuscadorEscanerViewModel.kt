package com.app.administradorfarmadon.ActivityFragmentos.Fragmentos.logicainventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.toMoldeProductos
import com.app.administradorfarmadon.ClasesDatabase.DbPaths
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.app.administradorfarmadon.ActivityInventario.ProductUtils
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.stockFisicoBase
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.stockMinimoBase
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

enum class FiltroAlerta {
    NINGUNO, VENCIDOS, STOCK_BAJO, POR_VENCER, SIN_CODIGO
}

data class InventarioUiState(
    val productos: List<MoldeProductos> = emptyList(),
    val productosFiltrados: List<MoldeProductos> = emptyList(),
    val busquedaQuery: String = "",
    val estaCargando: Boolean = true,
    val estaEscaneando: Boolean = false,
    val mensajeError: String? = null,
    val productoNoEncontrado: Boolean = false,
    val ultimoCodigoEscaneado: String = "",
    val indiceResaltado: String? = null,
    val busquedaInteligenteActiva: Boolean = false,
    val terminosSeleccionados: List<String> = emptyList(),
    
    // Categorías
    val categorias: List<String> = emptyList(),
    val categoriaSeleccionada: String = "Todos",
    
    // Alertas
    val filtroAlertaActivo: FiltroAlerta = FiltroAlerta.NINGUNO,
    val conteoVencidos: Int = 0,
    val conteoStockBajo: Int = 0,
    val conteoPorVencer: Int = 0,
    val conteoSinCodigo: Int = 0
)

class BuscadorEscanerViewModel : ViewModel() {

    private val _state = MutableStateFlow(InventarioUiState())
    val state: StateFlow<InventarioUiState> = _state.asStateFlow()

    private val db = FirebaseDatabase.getInstance()

    init {
        escucharProductos()
    }

    private fun escucharProductos() {
        // V23.1: Optimización de Resumen y Categorías
        val refResumen = db.getReference("Inventario/Resumen")
        refResumen.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val vencidos = snapshot.child("conteoVencidos").getValue(Int::class.java) ?: 0
                val bajoStock = snapshot.child("conteoStockBajo").getValue(Int::class.java) ?: 0
                val porVencer = snapshot.child("conteoPorVencer").getValue(Int::class.java) ?: 0
                val sinCodigo = snapshot.child("conteoSinCodigo").getValue(Int::class.java) ?: 0

                _state.value = _state.value.copy(
                    conteoVencidos = vencidos,
                    conteoStockBajo = bajoStock,
                    conteoPorVencer = porVencer,
                    conteoSinCodigo = sinCodigo
                )
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Escuchar Categorías de forma independiente
        val refCats = db.getReference(DbPaths.INVENTARIO_CATEGORIAS)
        refCats.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cats = mutableListOf("Todos")
                snapshot.children.forEach { child ->
                    val nombre = child.child("nombre").getValue(String::class.java)
                    if (!nombre.isNullOrBlank()) cats.add(nombre)
                }
                _state.value = _state.value.copy(
                    categorias = cats.distinct().sorted()
                )
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // V23.1: Escuchar cambios en los últimos productos registrados (Sincronización en Tiempo Real)
        val refProd = db.getReference(DbPaths.INVENTARIO_PRODUCTOS).limitToLast(100)
        refProd.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewModelScope.launch {
                    val fullList = coroutineScope {
                        snapshot.children.map { child ->
                            async {
                                val prod = child.toMoldeProductos()
                                if (prod != null) {
                                    val lotesSnap = db.getReference(DbPaths.INVENTARIO_PRODUCTO_LOTES).child(prod.indice).get().await()
                                    val lotesMap = mutableMapOf<String, com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteProducto>()
                                    lotesSnap.children.forEach { lChild ->
                                        lChild.getValue(com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteProducto::class.java)?.let {
                                            lotesMap[lChild.key ?: ""] = it
                                        }
                                    }
                                    prod.lotes = lotesMap
                                    prod
                                } else null
                            }
                        }.awaitAll().filterNotNull()
                    }
                    
                    val sortedList = fullList.reversed()
                    _state.value = _state.value.copy(
                        productos = sortedList,
                        estaCargando = false
                    )
                    aplicarFiltro()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                _state.value = _state.value.copy(estaCargando = false)
            }
        })
    }

    fun refrescar() {
        // El listener ya está activo, pero esto asegura que forzamos una lectura inmediata si es necesario
        _state.value = _state.value.copy(estaCargando = true)
        viewModelScope.launch {
            try {
                val refProd = db.getReference(DbPaths.INVENTARIO_PRODUCTOS).limitToLast(100)
                val snapshot = refProd.get().await()
                
                val fullList = coroutineScope {
                    snapshot.children.map { child ->
                        async {
                            val prod = child.toMoldeProductos()
                            if (prod != null) {
                                val lotesSnap = db.getReference(DbPaths.INVENTARIO_PRODUCTO_LOTES).child(prod.indice).get().await()
                                val lotesMap = mutableMapOf<String, com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteProducto>()
                                lotesSnap.children.forEach { lChild ->
                                    lChild.getValue(com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteProducto::class.java)?.let {
                                        lotesMap[lChild.key ?: ""] = it
                                    }
                                }
                                prod.lotes = lotesMap
                                prod
                            } else null
                        }
                    }.awaitAll().filterNotNull()
                }
                
                _state.value = _state.value.copy(
                    productos = fullList.reversed(),
                    estaCargando = false
                )
                aplicarFiltro()
            } catch (e: Exception) {
                _state.value = _state.value.copy(estaCargando = false)
            }
        }
    }

    fun actualizarBusqueda(query: String) {
        _state.value = _state.value.copy(busquedaQuery = query, filtroAlertaActivo = FiltroAlerta.NINGUNO)
        if (!state.value.busquedaInteligenteActiva) {
            aplicarFiltro()
        }
    }

    fun toggleFiltroAlerta(filtro: FiltroAlerta) {
        val actual = _state.value.filtroAlertaActivo
        val nuevo = if (actual == filtro) FiltroAlerta.NINGUNO else filtro
        _state.value = _state.value.copy(filtroAlertaActivo = nuevo, busquedaQuery = "", terminosSeleccionados = emptyList())
        aplicarFiltro()
    }

    fun toggleBusquedaInteligente() {
        val nuevoEstado = !_state.value.busquedaInteligenteActiva
        _state.value = _state.value.copy(
            busquedaInteligenteActiva = nuevoEstado,
            busquedaQuery = "",
            terminosSeleccionados = emptyList(),
            filtroAlertaActivo = FiltroAlerta.NINGUNO,
            categoriaSeleccionada = "Todos"
        )
        aplicarFiltro()
    }

    fun seleccionarCategoria(cat: String) {
        _state.value = _state.value.copy(
            categoriaSeleccionada = cat,
            filtroAlertaActivo = FiltroAlerta.NINGUNO
        )
        aplicarFiltro()
    }

    fun agregarTermino(termino: String) {
        val limpio = termino.trim().lowercase()
        if (limpio.isBlank()) return
        val actual = _state.value.terminosSeleccionados
        if (limpio !in actual) {
            _state.value = _state.value.copy(
                terminosSeleccionados = actual + limpio,
                busquedaQuery = ""
            )
            aplicarFiltro()
        }
    }

    fun eliminarTermino(termino: String) {
        _state.value = _state.value.copy(
            terminosSeleccionados = _state.value.terminosSeleccionados - termino
        )
        aplicarFiltro()
    }

    private fun aplicarFiltro() {
        viewModelScope.launch {
            val query = _state.value.busquedaQuery
            val terminos = _state.value.terminosSeleccionados
            val inteligente = _state.value.busquedaInteligenteActiva
            val alerta = _state.value.filtroAlertaActivo
            val catSel = _state.value.categoriaSeleccionada

            var sourceList = _state.value.productos

            if (catSel != "Todos") {
                sourceList = sourceList.filter { it.categoria == catSel }
            }

            if (alerta != FiltroAlerta.NINGUNO) {
                val filtrados = when (alerta) {
                    FiltroAlerta.VENCIDOS -> sourceList.filter { ProductUtils.obtenerEstadoVencimiento(it) == "VENCIDO" }
                    FiltroAlerta.STOCK_BAJO -> sourceList.filter { 
                        val current = it.stockFisicoBase()
                        val min = it.stockMinimoBase()
                        current < min && min > 0 
                    }
                    FiltroAlerta.POR_VENCER -> sourceList.filter { ProductUtils.obtenerEstadoVencimiento(it) == "POR_VENCER" }
                    FiltroAlerta.SIN_CODIGO -> sourceList.filter { !it.tieneCodigoBarra && it.codigo.isBlank() }
                }
                _state.value = _state.value.copy(productosFiltrados = filtrados)
                return@launch
            }

            if (inteligente) {
                if (terminos.isEmpty()) {
                    _state.value = _state.value.copy(productosFiltrados = sourceList)
                    return@launch
                }
                _state.value = _state.value.copy(estaCargando = true)
                val resultados = BuscadorSearchRepository.buscarPorTerminos(terminos)
                _state.value = _state.value.copy(
                    productosFiltrados = resultados,
                    estaCargando = false
                )
            } else {
                val filtered = if (query.isBlank()) {
                    sourceList
                } else {
                    sourceList.filter {
                        it.nombre.contains(query, ignoreCase = true) ||
                        it.categoria.contains(query, ignoreCase = true) ||
                        it.codigo.contains(query, ignoreCase = true) ||
                        it.lotes.values.any { lote -> lote.numero.contains(query, ignoreCase = true) }
                    }
                }
                _state.value = _state.value.copy(productosFiltrados = filtered)
            }
        }
    }

    fun setEstaEscaneando(activo: Boolean) {
        _state.value = _state.value.copy(estaEscaneando = activo)
    }

    fun limpiarErrorEscaneo() {
        _state.value = _state.value.copy(productoNoEncontrado = false)
    }

    fun procesarCodigoEscaneado(codigo: String) {
        setEstaEscaneando(false)
        val query = codigo.trim()
        val all = _state.value.productos
        val foundProduct = all.find { it.codigo.equals(query, ignoreCase = true) }
        
        if (foundProduct != null) {
            _state.value = _state.value.copy(
                busquedaInteligenteActiva = false,
                terminosSeleccionados = emptyList()
            )
            actualizarBusqueda(query)
            viewModelScope.launch {
                _state.value = _state.value.copy(indiceResaltado = foundProduct.indice)
                delay(3000)
                _state.value = _state.value.copy(indiceResaltado = null)
            }
        } else {
            _state.value = _state.value.copy(
                productoNoEncontrado = true,
                ultimoCodigoEscaneado = query
            )
        }
    }
}
