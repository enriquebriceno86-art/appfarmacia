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
    val horasError: String? = null,
    val aviso: String? = null
)

enum class EstadoTipo { ABIERTO, CIERRA_PRONTO, CERRADO, PROXIMA_APERTURA }

data class EstadoHorarioActual(
    val tipo: EstadoTipo,
    val mensaje: String,
    val minutosRestantes: Long = 0
)

data class HorarioUiState(
    val horaAperturaHabilitada: Boolean,
    val horaCierreHabilitada: Boolean,
    val textoHoraApertura: String,
    val textoHoraCierre: String,
    val mostrarAplicarLaborales: Boolean
)
