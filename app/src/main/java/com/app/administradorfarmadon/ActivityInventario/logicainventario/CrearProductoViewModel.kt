package com.app.administradorfarmadon.ActivityInventario.logicainventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos
import com.app.administradorfarmadon.ActivityInventario.ui.CreateProductState
import com.app.administradorfarmadon.ClasesDatabase.DbPaths
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Transaction
import com.google.firebase.database.MutableData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * V28.0: ViewModel centralizado para la creación de productos.
 * Implementa reserva atómica para evitar colisiones entre múltiples usuarios.
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

    /**
     * Intenta guardar el producto usando un protocolo de "Reserva Atómica"
     * para evitar la Race Condition de nombres duplicados simultáneos.
     */
    fun guardarProductoBlindado(
        producto: MoldeProductos,
        nameKey: String,
        updates: Map<String, Any>
    ) {
        viewModelScope.launch {
            _saveStatus.value = SaveResult.Loading

            // 1. PROTOCOLO DE RESERVA ATÓMICA
            // Intentamos escribir en el índice de nombres con una transacción.
            // Si el nodo ya tiene datos, la transacción falla y detenemos todo.
            val indexRef = db.getReference(DbPaths.INVENTARIO_NOMBRES).child(nameKey)

            indexRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    if (currentData.value != null) {
                        // YA EXISTE: Alguien escribió aquí milisegundos antes que nosotros.
                        return Transaction.abort()
                    }
                    // ESTÁ VACÍO: "Reservamos" el nombre poniendo el ID de nuestro producto.
                    currentData.value = producto.indice
                    return Transaction.success(currentData)
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                    if (committed) {
                        // 2. RESERVA EXITOSA: Ahora sí disparamos la escritura masiva (updateChildren).
                        // Como ya reservamos el nombre, nadie más puede sobreescribir nuestro índice.
                        db.getReference().updateChildren(updates)
                            .addOnSuccessListener {
                                _saveStatus.value = SaveResult.Success
                            }
                            .addOnFailureListener {
                                // Si falla el guardado masivo, liberamos el nombre para no dejar basura
                                indexRef.removeValue()
                                _saveStatus.value = SaveResult.Error("Error al guardar datos: ${it.message}")
                            }
                    } else {
                        // 3. CONFLICTO DETECTADO
                        val errorMsg = if (error != null) "Error de red: ${error.message}" 
                                      else "Este producto acaba de ser registrado por otro administrador."
                        _saveStatus.value = SaveResult.Conflict(errorMsg)
                    }
                }
            })
        }
    }

    fun resetStatus() {
        _saveStatus.value = SaveResult.Idle
    }
}
