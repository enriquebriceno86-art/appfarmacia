package com.app.administradorfarmadon.ActivitysPerfilItem

object HorarioAuditRules {

    private const val VALOR_VACIO = "(vacio)"
    private const val VALOR_ELIMINADO = "(eliminado)"

    fun construirCambiosAntesDespuesHorario(
        anterior: HorarioTienda?,
        actual: HorarioTienda
    ): Map<String, Map<String, Any>> {
        val mapaActual = toAuditoriaMap(actual)
        val cambios = linkedMapOf<String, Map<String, Any>>()

        if (anterior == null) {
            mapaActual.forEach { (campo, valor) ->
                cambios[campo] = mapOf(
                    "antes" to VALOR_VACIO,
                    "despues" to valorAuditoriaHorario(valor)
                )
            }
            return cambios
        }

        val mapaAnterior = toAuditoriaMap(anterior)
        mapaActual.forEach { (campo, valorActual) ->
            val valorAnterior = mapaAnterior[campo]
            if (valorAnterior == valorActual) return@forEach
            cambios[campo] = mapOf(
                "antes" to valorAuditoriaHorario(valorAnterior),
                "despues" to valorAuditoriaHorario(valorActual)
            )
        }
        return cambios
    }

    fun construirCambiosEliminacionHorario(
        anterior: HorarioTienda
    ): Map<String, Map<String, Any>> {
        val cambios = linkedMapOf<String, Map<String, Any>>()
        toAuditoriaMap(anterior).forEach { (campo, valor) ->
            cambios[campo] = mapOf(
                "antes" to valorAuditoriaHorario(valor),
                "despues" to VALOR_ELIMINADO
            )
        }
        return cambios
    }

    private fun toAuditoriaMap(horario: HorarioTienda): Map<String, Any?> {
        return linkedMapOf(
            "dia" to horario.dia,
            "horaApertura" to horario.horaApertura,
            "horaCierre" to horario.horaCierre,
            "cerrado" to horario.cerrado,
            "veinticuatroHoras" to horario.veinticuatroHoras,
            "cerradoPorNombre" to horario.cerradoPorNombre,
            "cerradoEn" to horario.cerradoEn
        )
    }

    private fun valorAuditoriaHorario(valor: Any?): Any {
        return when (valor) {
            null -> VALOR_VACIO
            is String -> valor.ifBlank { VALOR_VACIO }
            is Boolean, is Number -> valor
            else -> valor.toString()
        }
    }
}
