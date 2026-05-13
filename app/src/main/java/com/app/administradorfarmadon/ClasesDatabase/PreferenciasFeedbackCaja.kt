package com.app.administradorfarmadon.ClasesDatabase

import android.content.Context

object PreferenciasFeedbackCaja {
    private const val PREFS = "feedback_caja"
    private const val KEY_POS_ACTIVO = "feedback_pos_activo"
    private const val KEY_ACTIVO = "feedback_agregar_activo"
    private const val KEY_SONIDO_ACTIVO = "feedback_agregar_sonido_activo"
    private const val KEY_HAPTICA_ACTIVA = "feedback_agregar_haptica_activa"

    fun estaActivo(context: Context): Boolean {
        val prefs = obtenerPrefs(context)
        return when {
            prefs.contains(KEY_POS_ACTIVO) -> prefs.getBoolean(KEY_POS_ACTIVO, true)
            prefs.contains(KEY_ACTIVO) -> prefs.getBoolean(KEY_ACTIVO, true)
            else -> estaSonidoActivoInterno(prefs) || estaHapticaActivaInterno(prefs)
        }
    }

    fun setActivo(context: Context, activo: Boolean) {
        obtenerPrefs(context)
            .edit()
            .putBoolean(KEY_POS_ACTIVO, activo)
            .putBoolean(KEY_HAPTICA_ACTIVA, activo)
            .putBoolean(KEY_SONIDO_ACTIVO, activo)
            .apply()
    }

    fun setSonidoActivo(context: Context, activo: Boolean) {
        obtenerPrefs(context)
            .edit()
            .putBoolean(KEY_SONIDO_ACTIVO, activo)
            .apply()
    }

    fun setHapticaActiva(context: Context, activa: Boolean) {
        obtenerPrefs(context)
            .edit()
            .putBoolean(KEY_HAPTICA_ACTIVA, activa)
            .apply()
    }

    fun estaSonidoActivo(context: Context): Boolean {
        val prefs = obtenerPrefs(context)
        return estaActivo(context) && estaSonidoActivoInterno(prefs)
    }

    fun estaHapticaActiva(context: Context): Boolean {
        val prefs = obtenerPrefs(context)
        return estaActivo(context) && estaHapticaActivaInterno(prefs)
    }

    private fun obtenerPrefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun estaSonidoActivoInterno(prefs: android.content.SharedPreferences): Boolean {
        return when {
            prefs.contains(KEY_SONIDO_ACTIVO) -> prefs.getBoolean(KEY_SONIDO_ACTIVO, true)
            prefs.contains(KEY_ACTIVO) -> prefs.getBoolean(KEY_ACTIVO, true)
            else -> true
        }
    }

    private fun estaHapticaActivaInterno(prefs: android.content.SharedPreferences): Boolean {
        return when {
            prefs.contains(KEY_HAPTICA_ACTIVA) -> prefs.getBoolean(KEY_HAPTICA_ACTIVA, true)
            prefs.contains(KEY_ACTIVO) -> prefs.getBoolean(KEY_ACTIVO, true)
            else -> true
        }
    }
}
