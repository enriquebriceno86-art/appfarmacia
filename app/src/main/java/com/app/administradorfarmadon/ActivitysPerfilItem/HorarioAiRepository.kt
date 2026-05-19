package com.app.administradorfarmadon.ActivitysPerfilItem

import com.app.administradorfarmadon.ClasesDatabase.obtenerDoubleFlexible
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HorarioAiRepository {

    private val db = FirebaseDatabase.getInstance()

    suspend fun getStoreConfig(): StoreConfig? {
        return try {
            val snapshot = db.getReference("ConfiguracionTienda").child("datosGenerales").get().await()
            if (snapshot.exists()) {
                // Mapeo manual simple para evitar dependencias de extensión no visibles
                StoreConfig(
                    tipoNegocio = snapshot.child("tipoNegocio").getValue(String::class.java) ?: "FARMACIA",
                    pais = snapshot.child("pais").getValue(String::class.java) ?: "",
                    ciudad = snapshot.child("ciudad").getValue(String::class.java) ?: ""
                )
            } else null
        } catch (e: Exception) { null }
    }

    suspend fun getVentasRecientes(dias: Int = 7): List<VentaSimplificada> {
        val lista = mutableListOf<VentaSimplificada>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()

        try {
            for (i in 0 until dias) {
                val fecha = sdf.format(cal.time)
                val snapshot = db.getReference("Ventas").child(fecha).get().await()
                
                if (snapshot.exists()) {
                    snapshot.children.forEach { ventaSnap ->
                        val total = ventaSnap.child("total").obtenerDoubleFlexible() ?: 0.0
                        val hora = ventaSnap.child("hora").getValue(String::class.java) 
                            ?: ventaSnap.child("infoVenta").child("hora").getValue(String::class.java)
                            ?: ""
                        
                        if (hora.isNotBlank()) {
                            lista.add(VentaSimplificada(fecha, hora, total))
                        }
                    }
                }
                cal.add(Calendar.DAY_OF_YEAR, -1)
            }
        } catch (e: Exception) { }
        
        return lista
    }
}
