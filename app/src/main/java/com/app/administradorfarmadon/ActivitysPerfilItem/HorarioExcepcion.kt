package com.app.administradorfarmadon.ActivitysPerfilItem

data class HorarioExcepcion(
    var id: String = "",
    var fecha: String = "", // Formato YYYY-MM-DD
    var motivo: String = "", // Ej: Feriado, Inventario, Mantenimiento
    var horaApertura: String = "",
    var horaCierre: String = "",
    var cerrado: Boolean = false,
    var veinticuatroHoras: Boolean = false,
    var creadoEn: Long = 0L
)
