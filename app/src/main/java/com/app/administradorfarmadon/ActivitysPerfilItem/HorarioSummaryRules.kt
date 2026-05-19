package com.app.administradorfarmadon.ActivitysPerfilItem

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class HorarioSemanalSummary(
    val diasConfigurados: Int,
    val diasCerrados: Int,
    val diasVeinticuatroHoras: Int,
    val horaMasTemprana: String?,
    val horaMasTardia: String?
)

object HorarioSummaryRules {

    fun calcularResumen(horarios: List<HorarioTienda>): HorarioSemanalSummary {
        val diasConfigurados = horarios.size
        val diasCerrados = horarios.count { it.cerrado }
        val diasVeinticuatroHoras = horarios.count { it.veinticuatroHoras }

        val horariosConHora = horarios.filter { !it.cerrado && !it.veinticuatroHoras }
        
        val sdf = SimpleDateFormat("h:mm a", Locale.US)
        
        val aperturasParsed = horariosConHora.mapNotNull { 
            runCatching { sdf.parse(it.horaApertura) }.getOrNull() 
        }
        
        val cierresParsed = horariosConHora.mapNotNull { horario ->
            val apertura = runCatching { sdf.parse(horario.horaApertura) }.getOrNull()
            val cierre = runCatching { sdf.parse(horario.horaCierre) }.getOrNull()
            
            if (apertura != null && cierre != null) {
                if (cierre.before(apertura)) {
                    // Si el cierre es numéricamente menor que la apertura (ej: 2 AM vs 10 PM),
                    // significa que cierra al día siguiente. Le sumamos 24h para que 
                    // maxByOrNull lo detecte correctamente como el punto más tardío.
                    val cal = Calendar.getInstance().apply {
                        time = cierre
                        add(Calendar.DAY_OF_YEAR, 1)
                    }
                    cal.time
                } else {
                    cierre
                }
            } else null
        }

        val horaMasTemprana = aperturasParsed.minByOrNull { it.time }?.let { sdf.format(it) }
        val horaMasTardia = cierresParsed.maxByOrNull { it.time }?.let { sdf.format(it) }

        return HorarioSemanalSummary(
            diasConfigurados = diasConfigurados,
            diasCerrados = diasCerrados,
            diasVeinticuatroHoras = diasVeinticuatroHoras,
            horaMasTemprana = horaMasTemprana,
            horaMasTardia = horaMasTardia
        )
    }
}
