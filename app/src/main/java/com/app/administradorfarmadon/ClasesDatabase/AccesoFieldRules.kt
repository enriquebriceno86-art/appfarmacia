package com.app.administradorfarmadon.ClasesDatabase

import com.google.firebase.database.DataSnapshot

object AccesoFieldRules {

    fun parseAcceso(value: Any?, defaultValue: Boolean = false): Boolean {
        val valor = value ?: return defaultValue
        return when (valor) {
            is Boolean -> valor
            is Number -> valor.toInt() != 0
            is String -> {
                val normalizado = valor.trim()
                normalizado.equals("true", ignoreCase = true) || normalizado == "1"
            }
            else -> defaultValue
        }
    }

    fun parseAccesoDesdeSnapshot(
        snapshot: DataSnapshot,
        childKey: String = "acceso",
        defaultValue: Boolean = false
    ): Boolean {
        return parseAcceso(
            value = snapshot.child(childKey).value,
            defaultValue = defaultValue
        )
    }
}
