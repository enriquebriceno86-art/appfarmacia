package com.app.administradorfarmadon.ClasesDatabase

import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.CatalogoPresentaciones
import com.google.firebase.database.FirebaseDatabase

/**
 * Configuracion de presentaciones a nivel TIENDA:
 * - habilitar/deshabilitar tipos (Caja, Blister, etc.)
 * - alias (ej: "Blister" -> "Tira")
 *
 * Importante:
 * - El catalogo base (tipos y compatibilidades por unidad base) es FIJO en codigo.
 * - Aqui solo se personaliza visualmente y se filtra.
 */
object PresentacionesTiendaConfigManager {

    private const val PATH_CONFIG = "ConfiguracionTienda"
    private const val PATH_CATALOGOS = "catalogos"
    private const val PATH_PRESENTACIONES = "presentaciones"

    data class PresentacionConfig(
        var habilitado: Boolean = true,
        var alias: String = ""
    )

    data class OpcionPresentacionUi(
        val nombreCanonical: String,
        val nombreVisible: String
    )

    @Volatile
    private var cache: Map<String, PresentacionConfig> = emptyMap()

    @Volatile
    private var cargando: Boolean = false

    // Callbacks pendientes cuando alguien pide precargar y el cache aun no está listo.
    // Se ejecutan en el hilo donde Firebase llama el listener (normalmente main).
    private val callbacksPendientes = mutableListOf<() -> Unit>()

    fun invalidarCache() {
        cache = emptyMap()
    }

    private fun normalizar(input: String): String {
        return PresentacionHelper.normalizarClave(input)
    }

    private fun keyCanonical(nombre: String): String {
        // Las claves en cache se guardan normalizadas para tolerar espacios/capitalizacion
        return normalizar(nombre)
    }

    fun precargar(onComplete: (() -> Unit)? = null) {
        if (cache.isNotEmpty()) {
            onComplete?.invoke()
            return
        }

        if (onComplete != null) {
            synchronized(callbacksPendientes) {
                callbacksPendientes.add(onComplete)
            }
        }

        if (cargando) return
        cargando = true

        FirebaseDatabase.getInstance()
            .getReference(PATH_CONFIG)
            .child(PATH_CATALOGOS)
            .child(PATH_PRESENTACIONES)
            .get()
            .addOnSuccessListener { snapshot ->
                val mapa = linkedMapOf<String, PresentacionConfig>()
                snapshot.children.forEach { child ->
                    val nombre = child.key?.trim().orEmpty()
                    if (nombre.isBlank()) return@forEach

                    val habilitado = child.child("habilitado").getValue(Boolean::class.java) ?: true
                    val alias = child.child("alias").getValue(String::class.java).orEmpty()
                    mapa[keyCanonical(nombre)] = PresentacionConfig(
                        habilitado = habilitado,
                        alias = alias
                    )
                }
                cache = mapa
                cargando = false

                val callbacks = synchronized(callbacksPendientes) {
                    val copia = callbacksPendientes.toList()
                    callbacksPendientes.clear()
                    copia
                }
                callbacks.forEach { it.invoke() }
            }
            .addOnFailureListener {
                // Si falla, seguimos con defaults (todo habilitado, sin alias).
                cargando = false

                val callbacks = synchronized(callbacksPendientes) {
                    val copia = callbacksPendientes.toList()
                    callbacksPendientes.clear()
                    copia
                }
                callbacks.forEach { it.invoke() }
            }
    }

    fun obtenerConfig(nombreCanonical: String): PresentacionConfig? {
        return cache[keyCanonical(nombreCanonical)]
    }

    fun estaHabilitado(nombreCanonical: String): Boolean {
        return obtenerConfig(nombreCanonical)?.habilitado ?: true
    }

    fun nombreVisible(nombreCanonical: String): String {
        val alias = obtenerConfig(nombreCanonical)?.alias?.trim().orEmpty()
        return if (alias.isBlank()) nombreCanonical else alias
    }

    fun opcionesParaUnidadBase(unidadBase: String): List<OpcionPresentacionUi> {
        val base = CatalogoPresentaciones.opcionesParaUnidadBase(unidadBase)
        return base
            .filter { estaHabilitado(it) }
            .map { canonical ->
                OpcionPresentacionUi(
                    nombreCanonical = canonical,
                    nombreVisible = nombreVisible(canonical)
                )
            }
    }

    fun canonicalDesdeVisible(
        unidadBase: String,
        visible: String
    ): String? {
        val clave = normalizar(visible)
        return opcionesParaUnidadBase(unidadBase)
            .firstOrNull { normalizar(it.nombreVisible) == clave }
            ?.nombreCanonical
    }

    /**
     * Guarda la configuracion completa de alias/habilitados.
     * Se espera que las llaves sean los nombres CANONICOS (ej: "Caja", "Blister").
     */
    fun guardarConfiguracion(
        configPorCanonical: Map<String, PresentacionConfig>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val ref = FirebaseDatabase.getInstance()
            .getReference(PATH_CONFIG)
            .child(PATH_CATALOGOS)
            .child(PATH_PRESENTACIONES)

        // Guardamos como { "Caja": {habilitado:true, alias:""}, ... }
        val payload = linkedMapOf<String, Any?>()
        configPorCanonical.forEach { (canonical, cfg) ->
            val nombre = canonical.trim()
            if (nombre.isBlank()) return@forEach

            payload[nombre] = mapOf(
                "habilitado" to cfg.habilitado,
                "alias" to cfg.alias.trim()
            )
        }

        ref.setValue(payload)
            .addOnSuccessListener {
                // Actualizar cache local (normalizado)
                val nuevo = linkedMapOf<String, PresentacionConfig>()
                configPorCanonical.forEach { (canonical, cfg) ->
                    nuevo[keyCanonical(canonical)] = cfg
                }
                cache = nuevo
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error al guardar configuracion")
            }
    }
}
