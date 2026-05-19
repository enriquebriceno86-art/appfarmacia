package com.app.administradorfarmadon.ActivitysPerfilItem

data class HorarioTemplate(
    val nombre: String,
    val descripcion: String,
    val horarios: List<HorarioTienda>
)

object HorarioTemplateRules {

    private val diasSemana = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    private val laborales = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes")

    fun obtenerPlantillas(): List<HorarioTemplate> {
        return listOf(
            HorarioTemplate(
                nombre = "Lunes a Viernes",
                descripcion = "Horario de oficina (8 AM - 6 PM)",
                horarios = laborales.map { dia ->
                    HorarioTienda(dia = dia, horaApertura = "8:00 AM", horaCierre = "6:00 PM")
                }
            ),
            HorarioTemplate(
                nombre = "Todos los días",
                descripcion = "Lunes a Domingo (8 AM - 8 PM)",
                horarios = diasSemana.map { dia ->
                    HorarioTienda(dia = dia, horaApertura = "8:00 AM", horaCierre = "8:00 PM")
                }
            ),
            HorarioTemplate(
                nombre = "Farmacia 24h",
                descripcion = "Atención ininterrumpida todos los días",
                horarios = diasSemana.map { dia ->
                    HorarioTienda(dia = dia, veinticuatroHoras = true)
                }
            ),
            HorarioTemplate(
                nombre = "Cerrar Domingos",
                descripcion = "Establece el domingo como día no laborable",
                horarios = listOf(
                    HorarioTienda(dia = "Domingo", cerrado = true)
                )
            )
        )
    }
}
