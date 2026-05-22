package com.app.administradorfarmadon.ClasesDatabase

import com.app.administradorfarmadon.ActivityInventario.ui.SavedPresentation
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Gestiona las presentaciones de venta personalizadas y recurrentes de la tienda.
 * Permite guardar "Caja x 30", "Saco 5kg", etc., para que aparezcan como chips
 * sugeridos en futuros productos del mismo tipo.
 */
object PresentacionesTiendaConfigManager {

    private const val PATH_ROOT = "ConfiguracionTienda/PresentacionesRecientes"
    
    private val _presentaciones = MutableStateFlow<List<SavedPresentation>>(emptyList())
    val presentaciones: StateFlow<List<SavedPresentation>> = _presentaciones

    private var initialized = false

    fun precargar() {
        if (initialized) return
        initialized = true

        val ref = FirebaseDatabase.getInstance().getReference(PATH_ROOT)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<SavedPresentation>()
                snapshot.children.forEach { typeSnapshot ->
                    typeSnapshot.children.forEach { item ->
                        item.getValue(SavedPresentation::class.java)?.let {
                            list.add(it)
                        }
                    }
                }
                _presentaciones.value = list.reversed() // Más recientes primero
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun guardarPresentacion(presentation: SavedPresentation) {
        val name = presentation.name.trim()
        val equivalence = presentation.equivalenceBase.trim()
        if (name.isBlank() || equivalence.isBlank()) return
        
        val type = presentation.controlType.ifBlank { "UNIDAD" }
        
        // V21.1: Generar una clave única determinística basada en nombre y equivalencia
        // Esto evita duplicados a nivel de base de datos sin importar la caché local.
        val sanitizedName = com.app.administradorfarmadon.ActivityInventario.ProductUtils.sanitizarTexto(name)
        val sanitizedEquiv = equivalence.replace(".", "_")
        val uniqueKey = "${sanitizedName}_$sanitizedEquiv"

        val db = FirebaseDatabase.getInstance().getReference(PATH_ROOT).child(type)
        db.child(uniqueKey).setValue(presentation.copy(id = uniqueKey))
    }

    /**
     * Devuelve el nombre visible de una presentación dado su ID canónico.
     */
    fun nombreVisible(canonicalId: String): String {
        if (canonicalId.isBlank()) return ""
        return _presentaciones.value.find { it.id == canonicalId }?.name ?: ""
    }

    /**
     * Resuelve el ID canónico de una presentación dado su nombre visible.
     */
    fun canonicalDesdeVisible(controlType: String, nombreVisible: String): String {
        if (nombreVisible.isBlank()) return ""
        return _presentaciones.value.find { 
            it.controlType == controlType && it.name.equals(nombreVisible, true) 
        }?.id ?: ""
    }

    /**
     * Devuelve las presentaciones guardadas filtradas por tipo de control.
     */
    fun opcionesParaUnidadBase(controlType: String): List<SavedPresentation> {
        return _presentaciones.value.filter { it.controlType == controlType }
    }
}
