package com.app.administradorfarmadon.ActivitysPerfilItem

import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object HorarioRules {

    private val marcadoresMojibake = setOf(
        0x00C3,
        0x00C2,
        0x00E2,
        0x00C6,
        0x00A2,
        0x20AC,
        0x2122,
        0x0153,
        0x017E,
        0x0161,
        0x00C5,
        0xFFFD
    )

    private val ordenDias = listOf(
        "Lunes",
        "Martes",
        "Miércoles",
        "Jueves",
        "Viernes",
        "Sábado",
        "Domingo"
    )

    private val diasLaboralesNormalizados = setOf(
        "lunes",
        "martes",
        "miercoles",
        "jueves",
        "viernes"
    )

    fun validarFormulario(data: HorarioFormData): HorarioValidationResult {
        val diaNormalizado = data.dia.trim()
        if (diaNormalizado.isEmpty()) {
            return HorarioValidationResult(
                esValido = false,
                diaError = "Selecciona un día"
            )
        }

        if (!data.cerrado && !data.veinticuatroHoras) {
            if (data.horaApertura.isBlank() || data.horaCierre.isBlank()) {
                return HorarioValidationResult(
                    esValido = false,
                    horasError = "Selecciona las horas de apertura y cierre"
                )
            }

            try {
                val sdf = SimpleDateFormat("h:mm a", Locale.US)
                val apertura = sdf.parse(data.horaApertura)
                val cierre = sdf.parse(data.horaCierre)

                if (apertura != null && cierre != null) {
                    if (apertura == cierre) {
                        return HorarioValidationResult(
                            esValido = false,
                            horasError = "La apertura y el cierre no pueden ser a la misma hora. Si atiendes todo el día, usa la opción 'Atender 24 horas'."
                        )
                    }
                    
                    if (cierre.before(apertura)) {
                        return HorarioValidationResult(
                            esValido = true,
                            aviso = "Has configurado un turno nocturno que termina el día siguiente (${data.horaCierre})."
                        )
                    }
                    
                    val diff = cierre.time - apertura.time
                    val diffHours = diff / (1000 * 60 * 60)
                    if (diffHours < 1) {
                        return HorarioValidationResult(
                            esValido = true,
                            aviso = "El rango de atención es muy corto (menos de 1 hora). ¿Es correcto?"
                        )
                    }
                }
            } catch (e: Exception) {
                return HorarioValidationResult(
                    esValido = false,
                    horasError = "Formato de hora inválido"
                )
            }
        }

        return HorarioValidationResult(esValido = true)
    }

    fun construirEstadoBotones(
        horaAperturaSeleccionada: String,
        horaCierreSeleccionada: String,
        cerrado: Boolean,
        veinticuatroHoras: Boolean,
        dia: String,
        esEdicion: Boolean
    ): HorarioUiState {
        val mostrarAplicarLaborales = debeMostrarAplicarLaborales(
            dia = dia,
            esEdicion = esEdicion
        )

        return when {
            cerrado -> HorarioUiState(
                horaAperturaHabilitada = false,
                horaCierreHabilitada = false,
                textoHoraApertura = "Cerrado",
                textoHoraCierre = "Cerrado",
                mostrarAplicarLaborales = mostrarAplicarLaborales
            )

            veinticuatroHoras -> HorarioUiState(
                horaAperturaHabilitada = false,
                horaCierreHabilitada = false,
                textoHoraApertura = "24 horas",
                textoHoraCierre = "24 horas",
                mostrarAplicarLaborales = mostrarAplicarLaborales
            )

            else -> HorarioUiState(
                horaAperturaHabilitada = true,
                horaCierreHabilitada = true,
                textoHoraApertura = horaAperturaSeleccionada.ifEmpty { "Apertura" },
                textoHoraCierre = horaCierreSeleccionada.ifEmpty { "Cierre" },
                mostrarAplicarLaborales = mostrarAplicarLaborales
            )
        }
    }

    fun debeMostrarAplicarLaborales(
        dia: String,
        esEdicion: Boolean
    ): Boolean {
        return !esEdicion && normalizarDia(dia) in diasLaboralesNormalizados
    }

    fun normalizarDia(dia: String): String {
        val reparado = repararTextoPotencialmenteCorrupto(dia).lowercase()
        return Normalizer.normalize(reparado, Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
            .trim()
    }

    private fun repararTextoPotencialmenteCorrupto(texto: String): String {
        var actual = texto
        repeat(3) {
            val reparado = try {
                actual.toByteArray(Charsets.ISO_8859_1).toString(Charsets.UTF_8)
            } catch (_: Exception) {
                return@repeat
            }
            if (reparado == actual || contarMarcadoresMojibake(reparado) > contarMarcadoresMojibake(actual)) {
                return@repeat
            }
            actual = reparado
        }
        return actual
    }

    private fun contarMarcadoresMojibake(texto: String): Int {
        return texto.count { it.code in marcadoresMojibake }
    }

    fun ordenarSegunDia(horarios: List<HorarioTienda>): List<HorarioTienda> {
        val ordenNormalizado = ordenDias
            .mapIndexed { index, dia -> normalizarDia(dia) to index }
            .toMap()

        return horarios.sortedBy { horario ->
            ordenNormalizado[normalizarDia(horario.dia)] ?: Int.MAX_VALUE
        }
    }

    fun calcularEstadoActual(
        horarioSemanal: HorarioTienda?,
        excepcionHoy: HorarioExcepcion?,
        ahora: Calendar = Calendar.getInstance()
    ): EstadoHorarioActual {
        // La excepción siempre tiene prioridad sobre el horario semanal
        if (excepcionHoy != null) {
            return calcularEstadoGenerico(
                cerrado = excepcionHoy.cerrado,
                v24 = excepcionHoy.veinticuatroHoras,
                aperturaStr = excepcionHoy.horaApertura,
                cierreStr = excepcionHoy.horaCierre,
                prefijoMensaje = if (excepcionHoy.motivo.isNotBlank()) "${excepcionHoy.motivo}: " else "",
                ahora = ahora
            )
        }

        if (horarioSemanal == null || horarioSemanal.cerrado) {
            return EstadoHorarioActual(EstadoTipo.CERRADO, "Cerrado")
        }

        val localeEs = Locale.forLanguageTag("es")
        val diaActualStr = ahora.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, localeEs) ?: ""
        
        if (normalizarDia(horarioSemanal.dia) != normalizarDia(diaActualStr)) {
            return EstadoHorarioActual(EstadoTipo.CERRADO, "Cerrado")
        }

        return calcularEstadoGenerico(
            cerrado = horarioSemanal.cerrado,
            v24 = horarioSemanal.veinticuatroHoras,
            aperturaStr = horarioSemanal.horaApertura,
            cierreStr = horarioSemanal.horaCierre,
            ahora = ahora
        )
    }

    private fun calcularEstadoGenerico(
        cerrado: Boolean,
        v24: Boolean,
        aperturaStr: String,
        cierreStr: String,
        prefijoMensaje: String = "",
        ahora: Calendar
    ): EstadoHorarioActual {
        if (cerrado) return EstadoHorarioActual(EstadoTipo.CERRADO, "${prefijoMensaje}Cerrado")
        if (v24) return EstadoHorarioActual(EstadoTipo.ABIERTO, "${prefijoMensaje}Abierto 24h")
        
        if (aperturaStr.isBlank() || cierreStr.isBlank()) {
            return EstadoHorarioActual(EstadoTipo.CERRADO, "${prefijoMensaje}Horario no definido")
        }

        return try {
            val sdf = SimpleDateFormat("h:mm a", Locale.US)
            val horaActual = sdf.parse(sdf.format(ahora.time)) ?: return EstadoHorarioActual(EstadoTipo.CERRADO, "Cerrado")
            val apertura = sdf.parse(aperturaStr) ?: return EstadoHorarioActual(EstadoTipo.CERRADO, "Cerrado")
            val cierre = sdf.parse(cierreStr) ?: return EstadoHorarioActual(EstadoTipo.CERRADO, "Cerrado")

            val esNocturno = cierre.before(apertura)
            val estaAbierto = if (esNocturno) {
                horaActual.after(apertura) || horaActual.before(cierre)
            } else {
                horaActual.after(apertura) && horaActual.before(cierre)
            }

            if (estaAbierto) {
                val cierreRelativo = if (esNocturno && horaActual.after(apertura)) {
                    cierre.time + (24 * 60 * 60 * 1000)
                } else {
                    cierre.time
                }
                val diffMin = (cierreRelativo - horaActual.time) / (1000 * 60)
                if (diffMin in 1..60) {
                    EstadoHorarioActual(EstadoTipo.CIERRA_PRONTO, "${prefijoMensaje}Cierra en $diffMin min", diffMin)
                } else {
                    EstadoHorarioActual(EstadoTipo.ABIERTO, "${prefijoMensaje}Abierto ahora")
                }
            } else {
                if (horaActual.before(apertura)) {
                    val diffApertura = (apertura.time - horaActual.time) / (1000 * 60)
                    if (diffApertura in 1..60) {
                        EstadoHorarioActual(EstadoTipo.PROXIMA_APERTURA, "${prefijoMensaje}Abre en $diffApertura min", diffApertura)
                    } else EstadoHorarioActual(EstadoTipo.CERRADO, "${prefijoMensaje}Cerrado")
                } else EstadoHorarioActual(EstadoTipo.CERRADO, "${prefijoMensaje}Cerrado")
            }
        } catch (e: Exception) {
            EstadoHorarioActual(EstadoTipo.CERRADO, "Cerrado")
        }
    }

    fun construirHorarioBase(
        dia: String,
        apertura: String,
        cierre: String,
        cerrado: Boolean,
        veinticuatroHoras: Boolean
    ): HorarioTienda {
        return HorarioTienda(
            dia = dia,
            horaApertura = apertura,
            horaCierre = cierre,
            cerrado = cerrado,
            veinticuatroHoras = veinticuatroHoras
        )
    }

    fun formatearHora(hour: Int, minute: Int): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.US)
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        return sdf.format(cal.time)
    }
}
