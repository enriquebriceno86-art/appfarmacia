package com.app.administradorfarmadon.ActivitysPerfilItem

data class HorarioTienda(
    var id: String = "",
    var dia: String = "",
    var horaApertura: String = "",
    var horaCierre: String = "",
    var cerrado: Boolean = false,
    var veinticuatroHoras: Boolean = false,
    var cerradoPorId: String = "",
    var cerradoPorNombre: String = "",
    var cerradoEn: Long = 0L
) {
    fun generarId(): String {
        return HorarioRules.normalizarDia(dia)
    }
}
