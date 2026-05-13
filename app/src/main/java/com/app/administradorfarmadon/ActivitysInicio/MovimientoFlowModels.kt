package com.app.administradorfarmadon.ActivitysInicio

import com.app.administradorfarmadon.ActivitysInicio.reciclermovimientos.CambioAuditoriaItem

data class MovimientoRemoteData(
    val idMovimiento: String,
    val titulo: String,
    val descripcion: String,
    val detalleLotes: String,
    val tipo: String,
    val tituloMetodoMetadata: String,
    val modulo: String,
    val autor: String,
    val hora: String,
    val fechaNodo: String,
    val fechaMovimiento: String,
    val timestamp: Long,
    val timestampServidor: Long,
    val monto: Double?,
    val cambiosAntesDespues: Map<String, CambioAuditoriaItem>
)

data class CambioAuditoriaRemoteData(
    val campo: String,
    val antes: Any? = null,
    val despues: Any? = null
)

data class MovimientoRemoteParseado(
    val idMovimiento: String,
    val titulo: String,
    val descripcion: String,
    val detalleLotes: String,
    val tipo: String,
    val tituloMetodoMetadata: String,
    val modulo: String,
    val autor: String,
    val hora: String,
    val fecha: String,
    val timestamp: Long,
    val monto: Double?,
    val cambiosAntesDespues: Map<String, CambioAuditoriaItem>
)

data class MovimientoTituloData(
    val tipo: String,
    val titulo: String,
    val descripcion: String,
    val tituloMetodoMetadata: String
)

data class AnalisisMovimientosEstadoVacio(
    val mostrarLista: Boolean,
    val mostrarEstadoVacio: Boolean,
    val mensajeEstadoVacio: String
)

data class AnalisisMovimientosSecciones(
    val posicionHoy: Int,
    val posicionAyer: Int,
    val posicionSemana: Int
)

data class AnalisisMovimientosResumen(
    val secciones: AnalisisMovimientosSecciones,
    val rangoInicialSugerido: String?,
    val listaVacia: Boolean
)
