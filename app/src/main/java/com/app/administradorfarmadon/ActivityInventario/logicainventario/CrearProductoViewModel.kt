package com.app.administradorfarmadon.ActivityInventario.logicainventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.Proveedor
import com.app.administradorfarmadon.ActivityInventario.CategoriaProductos
import com.app.administradorfarmadon.ActivityInventario.ui.CategoryMarginRule
import com.app.administradorfarmadon.ActivityInventario.ui.CreateProductState
import com.app.administradorfarmadon.ClasesDatabase.DbPaths
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.toMoldeProductos
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Transaction
import com.google.firebase.database.MutableData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * V29.0: ViewModel centralizado para la creación de productos.
 * Gestiona toda la comunicación con Firebase (Red/DB).
 */
class CrearProductoViewModel : ViewModel() {

    private val db = FirebaseDatabase.getInstance()
    
    sealed class SaveResult {
        object Idle : SaveResult()
        object Loading : SaveResult()
        object Success : SaveResult()
        data class Error(val message: String) : SaveResult()
        data class Conflict(val message: String) : SaveResult()
    }

    private val _saveStatus = MutableStateFlow<SaveResult>(SaveResult.Idle)
    val saveStatus: StateFlow<SaveResult> = _saveStatus.asStateFlow()

    // Estados para datos maestros
    private val _suppliers = MutableStateFlow<List<Proveedor>>(emptyList())
    val suppliers: StateFlow<List<Proveedor>> = _suppliers.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _marginRules = MutableStateFlow<List<CategoryMarginRule>>(emptyList())
    val marginRules: StateFlow<List<CategoryMarginRule>> = _marginRules.asStateFlow()

    // Estado para guardado de proveedor
    private val _isSavingSupplier = MutableStateFlow(false)
    val isSavingSupplier: StateFlow<Boolean> = _isSavingSupplier.asStateFlow()

    private val _supplierSaveSuccess = MutableStateFlow(false)
    val supplierSaveSuccess: StateFlow<Boolean> = _supplierSaveSuccess.asStateFlow()

    private var supplierResetJob: Job? = null

    init {
        fetchInitialData()
    }

    private fun fetchInitialData() {
        obtenerProveedores()
        obtenerCategorias()
        obtenerReglasDeMargen()
    }

    fun obtenerProveedores() {
        db.getReference(DbPaths.INVENTARIO_PROVEEDORES)
            .get()
            .addOnSuccessListener { snapshot ->
                val lista = mutableListOf<Proveedor>()
                snapshot.children.forEach { child ->
                    child.getValue(Proveedor::class.java)?.let { lista.add(it) }
                }
                _suppliers.value = lista
            }
    }

    fun obtenerCategorias() {
        db.getReference("Inventario/CategoriasInventario")
            .get()
            .addOnSuccessListener { snapshot ->
                val lista = mutableListOf<String>()
                snapshot.children.forEach { data ->
                    data.getValue(CategoriaProductos::class.java)?.let { 
                        if (it.nombre.isNotBlank()) lista.add(it.nombre.trim())
                    }
                }
                _categories.value = lista.distinct().sorted()
            }
    }

    fun obtenerReglasDeMargen() {
        db.getReference("Inventario/ConfiguracionMargenes")
            .get()
            .addOnSuccessListener { snapshot ->
                val reglas = mutableListOf<CategoryMarginRule>()
                snapshot.children.forEach { data ->
                    data.getValue(CategoryMarginRule::class.java)?.let { reglas.add(it) }
                }
                _marginRules.value = reglas
            }
    }

    fun guardarProveedor(nombre: String, idFiscal: String, onConflict: (String) -> Unit) {
        // V3.1: Cancelamos reset pendiente
        supplierResetJob?.cancel()

        // 1. VALIDACIÓN DE DUPLICADOS (Nombre o ID Fiscal)
        val nombreLimpio = nombre.trim()
        val idLimpio = idFiscal.trim()

        val existe = _suppliers.value.any {
            it.nombre.equals(nombreLimpio, ignoreCase = true) ||
                    (idLimpio.isNotBlank() && it.idFiscal == idLimpio)
        }

        if (existe) {
            onConflict("Ya existe un proveedor con ese nombre o identificación fiscal.")
            return
        }

        // 2. PROCESO DE GUARDADO
        viewModelScope.launch {
            _isSavingSupplier.value = true
            _supplierSaveSuccess.value = false

            val ref = db.getReference(DbPaths.INVENTARIO_PROVEEDORES)
            val id = ref.push().key ?: UUID.randomUUID().toString()

            val datosProveedor = mutableMapOf<String, Any>()
            datosProveedor["id"] = id
            datosProveedor["nombre"] = nombreLimpio
            if (idLimpio.isNotBlank()) datosProveedor["idFiscal"] = idLimpio

            ref.child(id).setValue(datosProveedor)
                .addOnSuccessListener {
                    val nuevo = Proveedor(id = id, nombre = nombreLimpio, idFiscal = idLimpio)
                    _suppliers.value = _suppliers.value + nuevo
                    _supplierSaveSuccess.value = true
                    _isSavingSupplier.value = false

                    // Temporizador para limpiar estados de éxito
                    supplierResetJob = viewModelScope.launch {
                        delay(3000)
                        resetSupplierStatus()
                    }
                }
                .addOnFailureListener {
                    _isSavingSupplier.value = false
                }
        }
    }

    /**
     * Intenta guardar el producto usando un protocolo de "Reserva Atómica"
     */
    fun guardarProductoBlindado(
        producto: MoldeProductos,
        nameKey: String,
        updates: Map<String, Any>
    ) {
        supplierResetJob?.cancel()
        viewModelScope.launch {
            _saveStatus.value = SaveResult.Loading

            val indexRef = db.getReference("Inventario/NombresProductos").child(nameKey)

            indexRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    if (currentData.value != null) {
                        return Transaction.abort()
                    }
                    currentData.value = producto.indice
                    return Transaction.success(currentData)
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                    if (committed) {
                        db.getReference().updateChildren(updates)
                            .addOnSuccessListener {
                                _saveStatus.value = SaveResult.Success
                            }
                            .addOnFailureListener {
                                indexRef.removeValue()
                                _saveStatus.value = SaveResult.Error("Error al guardar datos: ${it.message}")
                            }
                    } else {
                        val errorMsg = if (error != null) "Error de red: ${error.message}" 
                                      else "Este producto acaba de ser registrado por otro administrador."
                        _saveStatus.value = SaveResult.Conflict(errorMsg)
                    }
                }
            })
        }
    }

    fun resetStatus() {
        supplierResetJob?.cancel()
        _saveStatus.value = SaveResult.Idle
    }

    fun resetSupplierStatus() {
        supplierResetJob?.cancel()
        _supplierSaveSuccess.value = false
        _isSavingSupplier.value = false
    }

    // --- VALIDACIONES REMOTAS ---

    fun verificarCodigoBarras(barcode: String, onResult: (DataSnapshot?) -> Unit) {
        supplierResetJob?.cancel()
        db.getReference("Inventario/CodigosProductos").child(barcode).get()
            .addOnSuccessListener { onResult(it) }
            .addOnFailureListener { onResult(null) }
    }

    fun buscarProductoPorId(productId: String, onResult: (MoldeProductos?) -> Unit) {
        supplierResetJob?.cancel()
        db.getReference("Inventario/Productos").child(productId).get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.toMoldeProductos())
            }
            .addOnFailureListener { onResult(null) }
    }

    fun verificarNombreDuplicado(nameKey: String, onResult: (DataSnapshot?) -> Unit) {
        supplierResetJob?.cancel()
        db.getReference("Inventario/NombresProductos").child(nameKey).get()
            .addOnSuccessListener { onResult(it) }
            .addOnFailureListener { onResult(null) }
    }

    fun verificarLoteRemoto(keyLote: String, onResult: (DataSnapshot?) -> Unit) {
        supplierResetJob?.cancel()
        db.getReference(DbPaths.INVENTARIO_LOTES_POR_NUMERO).child(keyLote).get()
            .addOnSuccessListener { onResult(it) }
            .addOnFailureListener { onResult(null) }
    }
}
