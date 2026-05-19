package com.app.administradorfarmadon.ActivitysPerfilItem

data class AiSuggestion(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val horarioSugerido: List<HorarioTienda>,
    val motivo: String,
    val prioridad: Int = 0 // 0: Normal, 1: Recomendado
)

data class AiOpportunity(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val accion: String, // "EXTENDER", "RECORTAR", "ABRIR"
    val detalleTecnico: String, // "Ventas detectadas a las 9:30 PM"
    val cambiosSugeridos: List<HorarioTienda>,
    val diasAfectados: List<String> = emptyList()
)

data class AiAlert(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val nivel: AlertLevel = AlertLevel.INFO
)

enum class AlertLevel {
    INFO, WARNING, CRITICAL
}

data class AiAnalysisResult(
    val sugerenciaInicial: AiSuggestion? = null,
    val oportunidades: List<AiOpportunity> = emptyList(),
    val alertas: List<AiAlert> = emptyList()
)

data class VentaSimplificada(
    val fecha: String, // yyyy-MM-dd
    val hora: String, // h:mm a
    val total: Double
)
