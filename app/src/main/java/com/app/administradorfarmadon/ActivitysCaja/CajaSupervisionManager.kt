package com.app.administradorfarmadon.ActivitysCaja

import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.ServerValue
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener

object CajaSupervisionManager {

    data class EstadoSupervisionCaja(
        val idSupervisor: String,
        val nombreSupervisor: String
    )

    class CajaSupervisionOcupadaException(
        val nombreSupervisorActual: String
    ) : IllegalStateException(nombreSupervisorActual)

    private var supervisionCajaRef: DatabaseReference? = null
    private var supervisionCajaListener: ValueEventListener? = null
    private var cajaSupervisadaPublicadaId = ""
    private var cajaSupervisadaPublicadaNombre = ""
    private var tokenSincronizacion = 0L

    val cajaPublicadaId: String
        get() = cajaSupervisadaPublicadaId

    val cajaPublicadaNombre: String
        get() = cajaSupervisadaPublicadaNombre

    fun escucharSupervisionCaja(
        database: FirebaseDatabase,
        idCaja: String,
        onChanged: (String, String) -> Unit,
        onError: (DatabaseError) -> Unit = {}
    ) {
        detenerEscuchaSupervision()
        if (idCaja.isBlank()) return

        val ref = getSupervisionCajaRef(database, idCaja)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val idSupervisor = snapshot.child("idSupervisor").getValue(String::class.java).orEmpty()
                val nombreSupervisor = snapshot.child("nombreSupervisor").getValue(String::class.java).orEmpty()
                onChanged(idSupervisor, nombreSupervisor)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error)
            }
        }

        supervisionCajaRef = ref
        supervisionCajaListener = listener
        ref.addValueEventListener(listener)
    }

    fun detenerEscuchaSupervision() {
        supervisionCajaListener?.let { listener ->
            supervisionCajaRef?.removeEventListener(listener)
        }
        supervisionCajaListener = null
        supervisionCajaRef = null
    }

    fun sincronizarSupervisionActual(
        database: FirebaseDatabase,
        idSupervisor: String,
        nombreSupervisor: String,
        idCajaPropia: String,
        idCajaEnUso: String,
        nombreCajaEnUso: String,
        onInicioSupervision: (String, String) -> Unit = { _, _ -> },
        onFinSupervision: (String, String) -> Unit = { _, _ -> },
        onError: (Exception) -> Unit = {},
        onEstadoActualizado: (String, String) -> Unit = { _, _ -> }
    ) {
        val tokenActual = ++tokenSincronizacion
        val estaSupervisandoOtraCaja = idCajaEnUso.isNotBlank() && idCajaEnUso != idCajaPropia

        fun continuarSincronizacion() {
            if (tokenActual != tokenSincronizacion) return

            if (!estaSupervisandoOtraCaja) {
                onEstadoActualizado("", "")
                return
            }

            val supervisionYaPublicadaEnEsaCaja = cajaSupervisadaPublicadaId == idCajaEnUso
            if (supervisionYaPublicadaEnEsaCaja) {
                onEstadoActualizado(cajaSupervisadaPublicadaId, cajaSupervisadaPublicadaNombre)
                return
            }

            val supervisionData = hashMapOf<String, Any>(
                "idSupervisor" to idSupervisor,
                "nombreSupervisor" to nombreSupervisor,
                "idCajaSupervisada" to idCajaEnUso,
                "nombreCajaSupervisada" to nombreCajaEnUso,
                "timestamp" to ServerValue.TIMESTAMP
            )

            val ref = getSupervisionCajaRef(database, idCajaEnUso)
            ref.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val supervisorActualId = currentData.child("idSupervisor")
                        .value
                        ?.toString()
                        ?.trim()
                        .orEmpty()
                    if (supervisorActualId.isNotBlank() && supervisorActualId != idSupervisor) {
                        return Transaction.abort()
                    }

                    currentData.value = supervisionData
                    return Transaction.success(currentData)
                }

                override fun onComplete(
                    error: com.google.firebase.database.DatabaseError?,
                    committed: Boolean,
                    currentData: com.google.firebase.database.DataSnapshot?
                ) {
                    if (tokenActual != tokenSincronizacion) return

                    if (error != null) {
                        if (cajaSupervisadaPublicadaId == idCajaEnUso) {
                            cajaSupervisadaPublicadaId = ""
                            cajaSupervisadaPublicadaNombre = ""
                        }
                        onError(error.toException())
                        onEstadoActualizado("", "")
                        return
                    }

                    if (!committed) {
                        if (cajaSupervisadaPublicadaId == idCajaEnUso) {
                            cajaSupervisadaPublicadaId = ""
                            cajaSupervisadaPublicadaNombre = ""
                        }
                        val nombreSupervisorActual = currentData
                            ?.child("nombreSupervisor")
                            ?.getValue(String::class.java)
                            .orEmpty()
                        onError(CajaSupervisionOcupadaException(nombreSupervisorActual))
                        onEstadoActualizado("", "")
                        return
                    }

                    cajaSupervisadaPublicadaId = idCajaEnUso
                    cajaSupervisadaPublicadaNombre = nombreCajaEnUso
                    ref.onDisconnect().removeValue()
                    onInicioSupervision(idCajaEnUso, nombreCajaEnUso)
                    onEstadoActualizado(cajaSupervisadaPublicadaId, cajaSupervisadaPublicadaNombre)
                }
            })
        }

        if (cajaSupervisadaPublicadaId.isNotBlank() && cajaSupervisadaPublicadaId != idCajaEnUso) {
            limpiarSupervisionPublicada(
                database = database,
                onFinSupervision = onFinSupervision,
                onError = onError,
                onEstadoActualizado = onEstadoActualizado,
                onComplete = { continuarSincronizacion() }
            )
        } else if (!estaSupervisandoOtraCaja && cajaSupervisadaPublicadaId.isNotBlank()) {
            limpiarSupervisionPublicada(
                database = database,
                onFinSupervision = onFinSupervision,
                onError = onError,
                onEstadoActualizado = onEstadoActualizado
            )
        } else {
            continuarSincronizacion()
        }
    }

    fun limpiarSupervisionPublicada(
        database: FirebaseDatabase,
        onFinSupervision: (String, String) -> Unit = { _, _ -> },
        onError: (Exception) -> Unit = {},
        onEstadoActualizado: (String, String) -> Unit = { _, _ -> },
        onComplete: (() -> Unit)? = null
    ) {
        val idCaja = cajaSupervisadaPublicadaId
        val nombreCaja = cajaSupervisadaPublicadaNombre
        if (idCaja.isBlank()) {
            onEstadoActualizado("", "")
            onComplete?.invoke()
            return
        }

        val tokenActual = ++tokenSincronizacion
        val ref = getSupervisionCajaRef(database, idCaja)
        ref.onDisconnect().cancel()
        ref.removeValue()
            .addOnSuccessListener {
                if (tokenActual != tokenSincronizacion) return@addOnSuccessListener
                cajaSupervisadaPublicadaId = ""
                cajaSupervisadaPublicadaNombre = ""
                if (nombreCaja.isNotBlank()) {
                    onFinSupervision(idCaja, nombreCaja)
                }
                onEstadoActualizado("", "")
                onComplete?.invoke()
            }
            .addOnFailureListener { error ->
                if (tokenActual != tokenSincronizacion) return@addOnFailureListener
                onError(error)
                onComplete?.invoke()
            }
    }

    private fun getSupervisionCajaRef(database: FirebaseDatabase, idCaja: String): DatabaseReference {
        return database.reference
            .child("CorteCaja")
            .child("Cajeras")
            .child(idCaja)
            .child("supervisionActiva")
    }
}
