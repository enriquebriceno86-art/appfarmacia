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
                    horasError = "Selecciona las horas"
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
