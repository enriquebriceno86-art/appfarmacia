package com.app.administradorfarmadon.ActivitysPerfilItem

import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat

object HorarioAiRules {

    fun generarSugerenciaInicial(config: StoreConfig): AiSuggestion {
        val horarios = mutableListOf<HorarioTienda>()
        val diasLaborales = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes")
        
        // Horario base sugerido para farmacias en LATAM
        diasLaborales.forEach { dia ->
            horarios.add(HorarioTienda(dia = dia, horaApertura = "08:00 AM", horaCierre = "09:00 PM"))
        }
        horarios.add(HorarioTienda(dia = "Sábado", horaApertura = "08:00 AM", horaCierre = "08:00 PM"))
        horarios.add(HorarioTienda(dia = "Domingo", cerrado = true))

        return AiSuggestion(
            id = "sugerencia_inicial_farmadon",
            titulo = "Sugerencia Inteligente",
            descripcion = "Basado en tu ubicación en ${config.ciudad}, ${config.pais} y el tipo de negocio (${config.tipoNegocio}).",
            horarioSugerido = horarios,
            motivo = "Horario estándar para farmacias en tu región que optimiza el flujo de clientes matutinos y nocturnos."
        )
    }

    fun analizarOportunidades(
        ventas: List<VentaSimplificada>,
        horariosActuales: List<HorarioTienda>
    ): List<AiOpportunity> {
        val oportunidades = mutableListOf<AiOpportunity>()
        if (ventas.isEmpty() || horariosActuales.isEmpty()) return oportunidades

        // Heurística 1: Ventas fuera de horario (Cierre)
        val ventasTardias = detectarVentasFueraDeHorarioCierre(ventas, horariosActuales)
        if (ventasTardias.isNotEmpty()) {
            val diasConVentasTardias = ventasTardias.map { obtenerNombreDia(it.fecha) }.distinct()
            oportunidades.add(
                AiOpportunity(
                    id = "extender_cierre",
                    titulo = "Oportunidad de Extensión",
                    descripcion = "Se detectó actividad relevante justo después de tu hora de cierre actual.",
                    accion = "EXTENDER",
                    detalleTecnico = "Se detectaron ventas registradas después del cierre en: ${diasConVentasTardias.joinToString(", ")}.",
                    cambiosSugeridos = generarCambioExtension(horariosActuales, diasConVentasTardias),
                    diasAfectados = diasConVentasTardias
                )
            )
        }

        // Heurística 2: Domingos cerrados con ventas
        val ventasDomingo = ventas.filter { 
            val cal = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            cal.time = sdf.parse(it.fecha) ?: java.util.Date()
            cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
        }
        val domingoCerrado = horariosActuales.find { it.dia == "Domingo" }?.cerrado ?: false
        if (ventasDomingo.isNotEmpty() && domingoCerrado) {
            oportunidades.add(
                AiOpportunity(
                    id = "abrir_domingos",
                    titulo = "Actividad en días cerrados",
                    descripcion = "Tu farmacia registró ventas el domingo, pero figura como cerrada.",
                    accion = "ABRIR",
                    detalleTecnico = "Se detectaron ${ventasDomingo.size} ventas el último domingo registrado.",
                    cambiosSugeridos = listOf(HorarioTienda(dia = "Domingo", horaApertura = "09:00 AM", horaCierre = "01:00 PM"))
                )
            )
        }

        return oportunidades
    }

    fun detectarAlertas(
        ventas: List<VentaSimplificada>,
        horarios: List<HorarioTienda>,
        excepciones: List<HorarioExcepcion>
    ): List<AiAlert> {
        val alertas = mutableListOf<AiAlert>()

        // Alerta 1: 24h sin actividad nocturna
        val tiene24h = horarios.any { it.veinticuatroHoras }
        if (tiene24h) {
            val ventasNocturnas = ventas.filter { 
                val hora24 = convertirA24h(it.hora)
                hora24 in 0..5 || hora24 >= 23
            }
            if (ventasNocturnas.isEmpty() && ventas.size > 20) {
                alertas.add(
                    AiAlert(
                        id = "alerta_24h_sin_ventas",
                        titulo = "Inconsistencia Operativa",
                        descripcion = "El negocio figura 24 horas, pero no se registra actividad nocturna significativa.",
                        nivel = AlertLevel.WARNING
                    )
                )
            }
        }

        // Alerta 2: Muchas excepciones
        if (excepciones.size >= 3) {
            alertas.add(
                AiAlert(
                    id = "alerta_excesivas_excepciones",
                    titulo = "Ajuste de Horario Base",
                    descripcion = "Tienes varias excepciones activas. Podrías considerar ajustar tu horario semanal permanente.",
                    nivel = AlertLevel.INFO
                )
            )
        }

        return alertas
    }

    private fun detectarVentasFueraDeHorarioCierre(
        ventas: List<VentaSimplificada>,
        horarios: List<HorarioTienda>
    ): List<VentaSimplificada> {
        val fueraDeHorario = mutableListOf<VentaSimplificada>()
        val sdf = SimpleDateFormat("h:mm a", Locale.US)
        
        ventas.forEach { venta ->
            val calVenta = Calendar.getInstance()
            calVenta.time = sdf.parse(venta.hora) ?: return@forEach
            
            val diaSemana = obtenerNombreDia(venta.fecha)
            val horarioDia = horarios.find { it.dia == diaSemana } ?: return@forEach
            
            if (!horarioDia.cerrado && !horarioDia.veinticuatroHoras) {
                val calCierre = Calendar.getInstance()
                calCierre.time = sdf.parse(horarioDia.horaCierre) ?: return@forEach
                
                // Si la venta es hasta 60 min después del cierre
                val diff = (calVenta.timeInMillis - calCierre.timeInMillis) / (1000 * 60)
                if (diff in 1..60) {
                    fueraDeHorario.add(venta)
                }
            }
        }
        return fueraDeHorario
    }

    private fun obtenerNombreDia(fecha: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        cal.time = sdf.parse(fecha) ?: return ""
        val localeEs = Locale.forLanguageTag("es")
        return cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, localeEs)
            ?.replaceFirstChar { it.uppercase() } ?: ""
    }

    private fun convertirA24h(horaStr: String): Int {
        return try {
            val sdf = SimpleDateFormat("h:mm a", Locale.US)
            val date = sdf.parse(horaStr) ?: return -1
            val cal = Calendar.getInstance()
            cal.time = date
            cal.get(Calendar.HOUR_OF_DAY)
        } catch (e: Exception) { -1 }
    }

    private fun generarCambioExtension(
        horarios: List<HorarioTienda>, 
        diasAfectados: List<String>
    ): List<HorarioTienda> {
        // Sugiere extender 1 hora el cierre SOLO de los días que tienen ventas tardías
        return horarios.filter { it.dia in diasAfectados && !it.cerrado && !it.veinticuatroHoras }.map {
            try {
                val sdf = SimpleDateFormat("h:mm a", Locale.US)
                val date = sdf.parse(it.horaCierre) ?: return@map it
                val cal = Calendar.getInstance()
                cal.time = date
                cal.add(Calendar.HOUR_OF_DAY, 1)
                it.copy(horaCierre = sdf.format(cal.time))
            } catch (e: Exception) { it }
        }
    }
}
