package com.app.administradorfarmadon.ActivitysPerfilItem

import android.net.Uri
import com.app.administradorfarmadon.ClasesDatabase.MovimientoLogger
import com.app.administradorfarmadon.ClasesDatabase.SessionManager
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repositorio centralizado para la gestión de métodos de pago.
 * Unifica la lógica de validación, subida de archivos y persistencia
 * para evitar discrepancias entre los flujos Compose y Legacy.
 */
object PaymentMethodsRepository {
    private val database = FirebaseDatabase.getInstance()
    private val metodosRef = database.getReference("ConfiguracionTienda").child("metodosPago")
    private val storage = FirebaseStorage.getInstance()

    suspend fun getMethods(): List<MetodoPagoConfig> {
        val snapshot = metodosRef.get().await()
        return snapshot.children.mapNotNull { child ->
            child.getValue(MetodoPagoConfig::class.java)?.let { normalizeMethod(it) }
        }.sortedWith(compareBy<MetodoPagoConfig>({ it.orden }, { it.titulo.lowercase() }))
    }

    /**
     * Normaliza categorías legacy a la nueva convención de forma silenciosa.
     */
    private fun normalizeMethod(method: MetodoPagoConfig): MetodoPagoConfig {
        val normalizedCategory = when (method.categoria.lowercase().trim()) {
            "transferencia" -> "transferencia_bancaria"
            "billetera", "qr" -> "billetera_digital"
            else -> method.categoria
        }
        return if (normalizedCategory != method.categoria) {
            method.copy(categoria = normalizedCategory)
        } else {
            method
        }
    }

    suspend fun saveMethod(
        method: MetodoPagoConfig,
        localQrUri: Uri? = null,
        existingMethods: List<MetodoPagoConfig>? = null
    ): Result<Unit> = runCatching {
        val methodToSave = normalizeMethod(method)
        val isNew = methodToSave.id.isBlank()
        val id = methodToSave.id.ifBlank { metodosRef.push().key ?: throw Exception("No se pudo generar ID") }
        
        // 1. Validar que no se quede sin métodos activos
        if (!methodToSave.activo) {
            val methods = existingMethods ?: getMethods()
            val otherActive = methods.count { it.activo && it.id != id }
            if (otherActive == 0) {
                throw Exception("Caja debe tener al menos una forma de cobro activa. No puedes desactivar el único método restante.")
            }
        }

        // 2. Subir QR si es una URI local
        val finalQrUrl = if (localQrUri != null && localQrUri.toString().startsWith("content://")) {
            uploadQrImage(localQrUri, id)
        } else {
            methodToSave.qrUrl
        }

        val finalMethod = methodToSave.copy(id = id, qrUrl = finalQrUrl)

        // 3. Guardar en Firebase Realtime Database
        metodosRef.child(id).setValue(toFirebaseMap(finalMethod)).await()

        // 4. Registrar movimiento en el log de auditoría
        MovimientoLogger.registrar(
            tipo = if (isNew) "metodo_pago_creado" else "metodo_pago_editado",
            modulo = "tipos_pago",
            titulo = if (isNew) "Método de pago creado: ${finalMethod.titulo}" else "Método de pago actualizado: ${finalMethod.titulo}",
            descripcion = "Se guardó el método de pago '${finalMethod.titulo}' bajo la categoría ${finalMethod.categoria}.",
            idUsuario = SessionManager.idCajera,
            nombreUsuario = SessionManager.nombreCajera,
            referenciaId = id,
            extra = mapOf(
                "seccion" to "metodosPago",
                "metodoId" to id,
                "categoria" to finalMethod.categoria,
                "activo" to finalMethod.activo
            )
        )
    }

    suspend fun deleteMethod(methodId: String, existingMethods: List<MetodoPagoConfig>? = null): Result<Unit> = runCatching {
        if (methodId.isBlank()) return@runCatching

        val methods = existingMethods ?: getMethods()
        val remainingActive = methods.count { it.activo && it.id != methodId }
        
        if (remainingActive == 0) {
            throw Exception("No puedes eliminar el último método de pago activo. Caja quedaría sin formas de cobro.")
        }

        metodosRef.child(methodId).removeValue().await()
        
        MovimientoLogger.registrar(
            tipo = "metodo_pago_eliminado",
            modulo = "tipos_pago",
            titulo = "Método de pago eliminado",
            descripcion = "Se eliminó un método de pago de la configuración de la tienda.",
            idUsuario = SessionManager.idCajera,
            nombreUsuario = SessionManager.nombreCajera,
            referenciaId = methodId
        )
    }

    private suspend fun uploadQrImage(uri: Uri, methodId: String): String = withContext(Dispatchers.IO) {
        val storageRef = storage.reference.child("configuracion/metodos_pago/$methodId/qr.jpg")
        storageRef.putFile(uri).await()
        storageRef.downloadUrl.await().toString()
    }

    /**
     * Mapeo centralizado a Firebase para asegurar consistencia de llaves.
     */
    private fun toFirebaseMap(method: MetodoPagoConfig): Map<String, Any> {
        val map = linkedMapOf<String, Any>(
            "id" to method.id.trim(),
            "titulo" to method.titulo.trim(),
            "categoria" to method.categoria.trim(),
            "activo" to method.activo,
            "permiteVuelto" to method.permiteVuelto,
            "solicitaMontoRecibido" to method.solicitaMontoRecibido,
            "calculaVuelto" to method.calculaVuelto,
            "permiteReferencia" to method.permiteReferencia,
            "usaQR" to method.usaQR,
            "disponibleMixto" to method.disponibleMixto,
            "orden" to method.orden,
            "preload" to method.preload
        )

        fun putIfNotBlank(key: String, value: String) {
            if (value.isNotBlank()) map[key] = value.trim()
        }

        putIfNotBlank("banco", method.banco)
        putIfNotBlank("tipoCuenta", method.tipoCuenta)
        putIfNotBlank("numeroCuenta", method.numeroCuenta)
        putIfNotBlank("titularBanco", method.titularBanco)
        putIfNotBlank("documentoBanco", method.documentoBanco)
        putIfNotBlank("telefonoBilletera", method.telefonoBilletera)
        putIfNotBlank("titularBilletera", method.titularBilletera)
        putIfNotBlank("aliasBilletera", method.aliasBilletera)
        putIfNotBlank("qrUrl", method.qrUrl)
        putIfNotBlank("instrucciones", method.instrucciones)
        putIfNotBlank("descripcion", method.descripcion)
        putIfNotBlank("placeholder", method.placeholder)
        
        return map
    }
}
