package com.app.administradorfarmadon.ActivitysInicio.reciclermovimientos

data class MovimientoAnalisisItem(
    val idMovimiento: String,
    val titulo: String,
    val descripcion: String,
    val detalleLotes: String,
    val tipo: String,
    val modulo: String,
    val monto: Double?,
    val autor: String,
    val hora: String,
    val fecha: String,
    val timestamp: Long,
    val cambiosAntesDespues: Map<String, CambioAuditoriaItem> = emptyMap()
)

data class CambioAuditoriaItem(
    val antes: Any? = null,
    val despues: Any? = null
)
