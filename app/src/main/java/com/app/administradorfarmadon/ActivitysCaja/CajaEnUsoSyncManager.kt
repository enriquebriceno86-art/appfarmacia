package com.app.administradorfarmadon.ActivitysCaja

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener

object CajaEnUsoSyncManager {

    data class EstadoCajaEnUso(
        val idCaja: String,
        val nombreCaja: String
    )

    private fun refSesionCaja(idUsuario: String): DatabaseReference {
        return FirebaseDatabase.getInstance()
            .reference
            .child("SesionCaja")
            .child(idUsuario)
    }

    fun publicarCajaEnUso(
        idUsuario: String,
        idCaja: String,
        nombreCaja: String,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val usuario = idUsuario.trim()
        val caja = idCaja.trim()
        if (usuario.isBlank() || caja.isBlank()) {
            onComplete?.invoke(false)
            return
        }

        val nombre = nombreCaja.trim().ifBlank { caja }

        val data = hashMapOf<String, Any>(
            "idCajaEnUso" to caja,
            "nombreCajaEnUso" to nombre,
            "actualizadoEn" to ServerValue.TIMESTAMP
        )

        refSesionCaja(usuario)
            .updateChildren(data)
            .addOnCompleteListener { task ->
                onComplete?.invoke(task.isSuccessful)
            }
    }

    fun escucharCajaEnUso(
        idUsuario: String,
        onChanged: (EstadoCajaEnUso?) -> Unit,
        onError: ((String) -> Unit)? = null
    ): Pair<DatabaseReference, ValueEventListener>? {
        val usuario = idUsuario.trim()
        if (usuario.isBlank()) return null

        val reference = refSesionCaja(usuario)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    onChanged(null)
                    return
                }

                val idCaja = snapshot.child("idCajaEnUso").value?.toString()?.trim().orEmpty()
                val nombreCaja = snapshot.child("nombreCajaEnUso").value?.toString()?.trim().orEmpty()
                if (idCaja.isBlank()) {
                    onChanged(null)
                    return
                }

                onChanged(EstadoCajaEnUso(idCaja = idCaja, nombreCaja = nombreCaja))
            }

            override fun onCancelled(error: DatabaseError) {
                onError?.invoke(error.message)
            }
        }

        reference.addValueEventListener(listener)
        return reference to listener
    }

    fun dejarDeEscuchar(
        reference: DatabaseReference?,
        listener: ValueEventListener?
    ) {
        if (reference == null || listener == null) return
        reference.removeEventListener(listener)
    }
}
