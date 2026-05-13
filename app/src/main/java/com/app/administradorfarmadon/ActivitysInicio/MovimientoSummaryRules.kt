package com.app.administradorfarmadon.ActivitysInicio

import com.app.administradorfarmadon.ActivitysInicio.reciclermovimientos.MovimientoAnalisisItem

object MovimientoSummaryRules {

    private const val RANGO_HOY = "HOY"
    private const val RANGO_AYER = "AYER"
    private const val RANGO_SEMANA = "SEMANA"

    fun construirResumenMovimientos(
        lista: List<MovimientoAnalisisItem>,
        rangoActual: String
    ): AnalisisMovimientosResumen {
        val secciones = calcularSecciones(lista)
        return AnalisisMovimientosResumen(
            secciones = secciones,
            rangoInicialSugerido = resolverRangoInicialSugerido(
                secciones = secciones,
                rangoActual = rangoActual
            ),
            listaVacia = lista.isEmpty()
        )
    }

    fun calcularSecciones(lista: List<MovimientoAnalisisItem>): AnalisisMovimientosSecciones {
        var posicionHoy = -1
        var posicionAyer = -1
        var posicionSemana = -1

        lista.forEachIndexed { index, movimiento ->
            when (movimiento.fecha.resolverCategoriaFecha()) {
                RANGO_HOY -> if (posicionHoy == -1) posicionHoy = index
                RANGO_AYER -> if (posicionAyer == -1) posicionAyer = index
                else -> if (posicionSemana == -1) posicionSemana = index
            }
        }

        return AnalisisMovimientosSecciones(
            posicionHoy = posicionHoy,
            posicionAyer = posicionAyer,
            posicionSemana = posicionSemana
        )
    }

    fun primerRangoDisponible(secciones: AnalisisMovimientosSecciones): String? {
        return when {
            secciones.posicionHoy >= 0 -> RANGO_HOY
            secciones.posicionAyer >= 0 -> RANGO_AYER
            secciones.posicionSemana >= 0 -> RANGO_SEMANA
            else -> null
        }
    }

    fun obtenerPosicionPorRango(
        rango: String,
        secciones: AnalisisMovimientosSecciones
    ): Int {
        return when (rango) {
            RANGO_HOY -> secciones.posicionHoy
            RANGO_AYER -> secciones.posicionAyer
            else -> secciones.posicionSemana
        }
    }

    fun tieneDatosParaRango(
        rango: String,
        secciones: AnalisisMovimientosSecciones
    ): Boolean {
        return obtenerPosicionPorRango(rango, secciones) >= 0
    }

    fun resolverCategoriaFecha(
        fechaFirebase: String,
        hoy: String,
        ayer: String
    ): String {
        return when (fechaFirebase) {
            hoy -> RANGO_HOY
            ayer -> RANGO_AYER
            else -> RANGO_SEMANA
        }
    }

    fun resolverRangoInicialSugerido(
        secciones: AnalisisMovimientosSecciones,
        rangoActual: String
    ): String? {
        return if (tieneDatosParaRango(rangoActual, secciones)) {
            rangoActual
        } else {
            primerRangoDisponible(secciones) ?: RANGO_HOY
        }
    }

    private fun String.resolverCategoriaFecha(): String {
        return when (this) {
            RANGO_HOY, RANGO_AYER, RANGO_SEMANA -> this
            else -> RANGO_SEMANA
        }
    }
}
