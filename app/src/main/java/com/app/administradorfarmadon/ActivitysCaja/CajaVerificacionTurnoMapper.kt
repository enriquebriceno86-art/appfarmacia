package com.app.administradorfarmadon.ActivitysCaja

/**
 * HUMANO: Este mapper convierte el resultado "puro" de CajaService en un
 * modelo simple que FragmentoCaja puede aplicar a su estado local sin
 * volver a recalcular reglas repartidas en varios ifs.
 */
object CajaVerificacionTurnoMapper {

    data class CajaVerificacionTurnoModel(
        val hayTurnoActivo: Boolean,
        val turnoAbierto: Boolean,
        val fechaTurnoActivo: String,
        val idTurnoActivo: String,
        val montoAperturaTurno: Double,
        val verificacionTurnoDisponible: Boolean,
        val cantidadTurnosPendientes: Int,
        val turnoPendienteDiaAnterior: Boolean,
        val fechaAvisoTurnoPendiente: String?,
        val saldoSugeridoApertura: Double
    )

    fun desdeEstadoTurno(
        estadoTurno: CajaEstadoTurno,
        fechaActual: String,
        saldoSugeridoApertura: Double
    ): CajaVerificacionTurnoModel {
        val turnoDetectado = estadoTurno.turnoDetectado
        val fechaAvisoPendiente = estadoTurno.turnosPendientes.firstOrNull()?.fecha

        return if (turnoDetectado != null) {
            CajaVerificacionTurnoModel(
                hayTurnoActivo = true,
                turnoAbierto = true,
                fechaTurnoActivo = turnoDetectado.fecha,
                idTurnoActivo = turnoDetectado.idTurno,
                montoAperturaTurno = turnoDetectado.montoApertura,
                verificacionTurnoDisponible = estadoTurno.turnosPendientes.isEmpty(),
                cantidadTurnosPendientes = estadoTurno.turnosPendientes.size,
                turnoPendienteDiaAnterior = estadoTurno.turnosPendientes.isNotEmpty(),
                fechaAvisoTurnoPendiente = fechaAvisoPendiente,
                saldoSugeridoApertura = 0.0
            )
        } else {
            CajaVerificacionTurnoModel(
                hayTurnoActivo = false,
                turnoAbierto = false,
                fechaTurnoActivo = fechaActual,
                idTurnoActivo = "",
                montoAperturaTurno = 0.0,
                verificacionTurnoDisponible = true,
                cantidadTurnosPendientes = 0,
                turnoPendienteDiaAnterior = false,
                fechaAvisoTurnoPendiente = null,
                saldoSugeridoApertura = saldoSugeridoApertura
            )
        }
    }
}
