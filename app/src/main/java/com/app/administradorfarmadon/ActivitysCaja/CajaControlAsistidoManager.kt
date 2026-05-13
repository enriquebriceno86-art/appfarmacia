package com.app.administradorfarmadon.ActivitysCaja

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.ServerValue
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener

object CajaControlAsistidoManager {

    data class EstadoControlCaja(
        val idEditor: String,
        val nombreEditor: String
    )

    private var controlCajaRef: DatabaseReference? = null
    private var controlCajaListener: ValueEventListener? = null

    fun escucharControlCaja(
        database: FirebaseDatabase,
        idCaja: String,
        onChanged: (EstadoControlCaja?) -> Unit,
        onError: (DatabaseError) -> Unit = {}
    ) {
        detenerEscuchaControl()
        if (idCaja.isBlank()) return

        val ref = getControlCajaRef(database, idCaja)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    onChanged(null)
                    return
                }

                val idEditor = snapshot.child("idEditor").getValue(String::class.java).orEmpty()
                val nombreEditor = snapshot.child("nombreEditor").getValue(String::class.java).orEmpty()
                if (idEditor.isBlank()) {
                    onChanged(null)
                    return
                }

                onChanged(
                    EstadoControlCaja(
                        idEditor = idEditor,
                        nombreEditor = nombreEditor
                    )
                )
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error)
            }
        }

        controlCajaRef = ref
        controlCajaListener = listener
        ref.addValueEventListener(listener)
    }

    fun detenerEscuchaControl() {
        controlCajaListener?.let { listener ->
            controlCajaRef?.removeEventListener(listener)
        }
        controlCajaListener = null
        controlCajaRef = null
    }

    fun tomarControl(
        database: FirebaseDatabase,
        idCaja: String,
        nombreCaja: String,
        idEditor: String,
        nombreEditor: String,
        onResult: (success: Boolean, holderName: String?) -> Unit
    ) {
        val caja = idCaja.trim()
        val editor = idEditor.trim()
        if (caja.isBlank() || editor.isBlank()) {
            onResult(false, null)
            return
        }

        val nombreVisibleEditor = nombreEditor.trim().ifBlank { "Soporte" }
        val ref = getControlCajaRef(database, caja)
        ref.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val actualId = currentData.child("idEditor").value?.toString()?.trim().orEmpty()
                if (actualId.isNotBlank() && actualId != editor) {
                    return Transaction.abort()
                }

                currentData.value = hashMapOf<String, Any>(
                    "idEditor" to editor,
                    "nombreEditor" to nombreVisibleEditor,
                    "idCajaControlada" to caja,
                    "nombreCajaControlada" to nombreCaja.trim().ifBlank { caja },
                    "timestamp" to ServerValue.TIMESTAMP
                )
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    onResult(false, null)
                    return
                }

                if (!committed) {
                    val holderName = currentData
                        ?.child("nombreEditor")
                        ?.getValue(String::class.java)
                        .orEmpty()
                        .ifBlank { null }
                    onResult(false, holderName)
                    return
                }

                ref.onDisconnect().removeValue()
                onResult(true, nombreVisibleEditor)
            }
        })
    }

    fun soltarControl(
        database: FirebaseDatabase,
        idCaja: String,
        idEditor: String,
        onResult: (success: Boolean) -> Unit = {}
    ) {
        val caja = idCaja.trim()
        val editor = idEditor.trim()
        if (caja.isBlank() || editor.isBlank()) {
            onResult(false)
            return
        }

        val ref = getControlCajaRef(database, caja)
        ref.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val actualId = currentData.child("idEditor").value?.toString()?.trim().orEmpty()
                if (actualId.isBlank()) {
                    currentData.value = null
                    return Transaction.success(currentData)
                }

                if (actualId != editor) {
                    return Transaction.abort()
                }

                currentData.value = null
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    onResult(false)
                    return
                }

                ref.onDisconnect().cancel()
                onResult(committed)
            }
        })
    }

    private fun getControlCajaRef(database: FirebaseDatabase, idCaja: String): DatabaseReference {
        return database.reference
            .child("CorteCaja")
            .child("Cajeras")
            .child(idCaja)
            .child("controlAsistido")
    }
}
