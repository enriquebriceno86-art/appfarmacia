package com.app.administradorfarmadon.ActivitysPerfilItem

data class HorarioFormData(
    val dia: String,
    val horaApertura: String,
    val horaCierre: String,
    val cerrado: Boolean,
    val veinticuatroHoras: Boolean,
    val aplicarLaborales: Boolean,
    val esEdicion: Boolean
)

data class HorarioValidationResult(
    val esValido: Boolean,
    val diaError: String? = null,
    val horasError: String? = null
)

data class HorarioUiState(
    val horaAperturaHabilitada: Boolean,
    val horaCierreHabilitada: Boolean,
    val textoHoraApertura: String,
    val textoHoraCierre: String,
    val mostrarAplicarLaborales: Boolean
)
