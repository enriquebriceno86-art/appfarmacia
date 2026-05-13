package com.app.administradorfarmadon.ClasesDatabase

object SessionRules {

    data class CajaEnUsoResultado(
        val idCajaEnUso: String,
        val nombreCajaEnUso: String
    )

    data class MonedaResultado(
        val codigo: String,
        val simbolo: String
    )

    fun puedeCambiarCaja(rol: String): Boolean {
        return rol.equals("administrador", ignoreCase = true) ||
            rol.equals("supervisor", ignoreCase = true)
    }

    fun resolverCajaEnUso(
        idCajera: String,
        nombreCajera: String,
        rol: String,
        idCajaEnUsoActual: String,
        nombreCajaEnUsoActual: String
    ): CajaEnUsoResultado {
        val idCajeraLimpio = limpiarTexto(idCajera)
        val nombreCajeraLimpio = limpiarTexto(nombreCajera)
        val idCajaActualLimpio = limpiarTexto(idCajaEnUsoActual)
        val nombreCajaActualLimpio = limpiarTexto(nombreCajaEnUsoActual)

        if (idCajeraLimpio.isBlank()) {
            return CajaEnUsoResultado(
                idCajaEnUso = idCajaActualLimpio,
                nombreCajaEnUso = nombreCajaActualLimpio
            )
        }

        return if (!puedeCambiarCaja(rol)) {
            CajaEnUsoResultado(
                idCajaEnUso = idCajeraLimpio,
                nombreCajaEnUso = nombreCajeraLimpio
            )
        } else {
            CajaEnUsoResultado(
                idCajaEnUso = idCajaActualLimpio.ifBlank { idCajeraLimpio },
                nombreCajaEnUso = nombreCajaActualLimpio.ifBlank { nombreCajeraLimpio }
            )
        }
    }

    fun resolverCambioCajaDestino(
        puedeCambiarCaja: Boolean,
        idCajera: String,
        nombreCajera: String,
        idDestino: String,
        nombreDestino: String
    ): CajaEnUsoResultado {
        if (!puedeCambiarCaja) {
            return CajaEnUsoResultado(
                idCajaEnUso = limpiarTexto(idCajera),
                nombreCajaEnUso = limpiarTexto(nombreCajera)
            )
        }

        val idDestinoLimpio = limpiarTexto(idDestino)
        return if (idDestinoLimpio.isBlank()) {
            CajaEnUsoResultado(
                idCajaEnUso = limpiarTexto(idCajera),
                nombreCajaEnUso = limpiarTexto(nombreCajera)
            )
        } else {
            CajaEnUsoResultado(
                idCajaEnUso = idDestinoLimpio,
                nombreCajaEnUso = limpiarTexto(nombreDestino).ifBlank { limpiarTexto(nombreCajera) }
            )
        }
    }

    fun resolverMoneda(
        codigo: String,
        simbolo: String
    ): MonedaResultado {
        return MonedaResultado(
            codigo = limpiarTexto(codigo).ifBlank { "PEN" },
            simbolo = limpiarTexto(simbolo).ifBlank { "S/" }
        )
    }

    fun limpiarTexto(valor: String): String = valor.trim()
}
