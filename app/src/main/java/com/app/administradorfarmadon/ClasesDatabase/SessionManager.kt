package com.app.administradorfarmadon.ClasesDatabase

import android.content.Context
import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Gestor de sesión centralizado con persistencia automática mediante delegados de Kotlin.
 * Al modificar cualquier propiedad, se guarda automáticamente en SharedPreferences.
 */
object SessionManager {
    private const val PREF_NAME = "usuario"
    
    private var prefs: SharedPreferences? = null

    // Delegados para persistencia automática
    var idCajera: String by StringPreference("id", "")
    var nombreCajera: String by StringPreference("usuario", "")
    var rol: String by StringPreference("rol", "Cajera")
    var idCajeraEnUso: String by StringPreference("id_caja_en_uso", "")
    var nombreCajeraEnUso: String by StringPreference("nombre_caja_en_uso", "")
    var appSessionId: String by StringPreference("app_session_id", "")
    var monedaCodigo: String by StringPreference("moneda_codigo", "PEN")
    var monedaSimbolo: String by StringPreference("moneda_simbolo", "S/")
    var paisOperacion: String by StringPreference("pais_operacion", "Perú") // V5.3: País explícito para IA
    var isLoggedIn: Boolean by BooleanPreference("login", false)

    /**
     * Inicializa el gestor con el contexto de la aplicación.
     * Debe llamarse una sola vez en AppFarmadon.
     */
    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
        normalizarCajaEnUsoSegunRol()
    }

    /**
     * Mantenemos por compatibilidad, pero redirige a init.
     */
    fun cargarSesion(context: Context) {
        init(context)
    }

    fun puedeCambiarCaja(): Boolean {
        return SessionRules.puedeCambiarCaja(rol)
    }

    private fun normalizarCajaEnUsoSegunRol() {
        val resultado = SessionRules.resolverCajaEnUso(
            idCajera = idCajera,
            nombreCajera = nombreCajera,
            rol = rol,
            idCajaEnUsoActual = idCajeraEnUso,
            nombreCajaEnUsoActual = nombreCajeraEnUso
        )
        idCajeraEnUso = resultado.idCajaEnUso
        nombreCajeraEnUso = resultado.nombreCajaEnUso
    }

    fun guardarSesion(context: Context, id: String, nombre: String, rolUsuario: String = "Cajera") {
        cargarSesion(context)
        idCajera = id
        nombreCajera = nombre
        rol = rolUsuario
        isLoggedIn = true
        normalizarCajaEnUsoSegunRol()
    }

    fun cambiarCajaDestino(id: String, nombre: String) {
        val resultado = SessionRules.resolverCambioCajaDestino(
            puedeCambiarCaja = puedeCambiarCaja(),
            idCajera = idCajera,
            nombreCajera = nombreCajera,
            idDestino = id,
            nombreDestino = nombre
        )
        idCajeraEnUso = resultado.idCajaEnUso
        nombreCajeraEnUso = resultado.nombreCajaEnUso
    }

    fun restaurarCajaPropia() {
        idCajeraEnUso = idCajera.trim()
        nombreCajeraEnUso = nombreCajera.trim()
    }

    fun guardarSessionIdLocal(context: Context, sessionId: String) {
        cargarSesion(context)
        appSessionId = sessionId.trim()
    }

    fun guardarMonedaConfigurada(context: Context, codigo: String, simbolo: String, pais: String = "Perú") {
        cargarSesion(context)
        val resultado = SessionRules.resolverMoneda(codigo, simbolo)
        monedaCodigo = resultado.codigo
        monedaSimbolo = resultado.simbolo
        paisOperacion = pais // V5.4: Persistir país real de la tienda
    }

    fun obtenerSessionIdLocal(context: Context): String {
        cargarSesion(context)
        return appSessionId
    }

    /**
     * Estas funciones se mantienen por compatibilidad con el código anterior,
     * pero ya no son necesarias porque el guardado es automático.
     */
    fun asegurarCajaPropiaSiCorresponde(context: Context? = null) {
        context?.let { cargarSesion(it) }
        normalizarCajaEnUsoSegunRol()
    }

    fun persistirCajaEnUso(context: Context) {
        // No hace nada porque el delegado ya persiste automáticamente al asignar
    }

    fun limpiarSesion(context: Context) {
        cargarSesion(context)
        prefs?.edit()?.clear()?.apply()
        
        // Reset de variables en memoria
        idCajera = ""
        nombreCajera = ""
        rol = ""
        idCajeraEnUso = ""
        nombreCajeraEnUso = ""
        appSessionId = ""
        monedaCodigo = "PEN"
        monedaSimbolo = "S/"
        paisOperacion = "Perú"
        isLoggedIn = false
    }

    // --- Implementación de Delegados ---

    private class StringPreference(val key: String, val defaultValue: String) : ReadWriteProperty<Any?, String> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): String {
            return prefs?.getString(key, defaultValue) ?: defaultValue
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
            prefs?.edit()?.putString(key, value)?.apply()
        }
    }

    private class BooleanPreference(val key: String, val defaultValue: Boolean) : ReadWriteProperty<Any?, Boolean> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
            return prefs?.getBoolean(key, defaultValue) ?: defaultValue
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
            prefs?.edit()?.putBoolean(key, value)?.apply()
        }
    }
}
