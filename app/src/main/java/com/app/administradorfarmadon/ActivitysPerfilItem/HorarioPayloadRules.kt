package com.app.administradorfarmadon.ActivitysPerfilItem

data class HorarioGuardadoPreparado(
    val id: String,
    val horario: HorarioTienda
)

data class HorariosActualizacionPreparados(
    val updates: Map<String, Any>,
    val horarios: List<HorarioGuardadoPreparado>
)

object HorarioPayloadRules {

    fun prepararHorarioParaGuardado(horario: HorarioTienda): HorarioGuardadoPreparado {
        val id = horario.generarId()
        val horarioPreparado = horario.copy(id = id)
        return HorarioGuardadoPreparado(
            id = id,
            horario = horarioPreparado
        )
    }

    fun prepararHorariosParaActualizacion(horarios: List<HorarioTienda>): HorariosActualizacionPreparados {
        val horariosPreparados = horarios.map(::prepararHorarioParaGuardado)
        val updates = linkedMapOf<String, Any>()
        horariosPreparados.forEach { preparado ->
            updates[preparado.id] = preparado.horario
        }
        return HorariosActualizacionPreparados(
            updates = updates,
            horarios = horariosPreparados
        )
    }
}
