package com.app.administradorfarmadon.ActivityInventario.reference

/**
 * Convierte las respuestas crudas de DeepSeek al dominio de la aplicación.
 */
object DeepSeekMapper {

    fun toCategorySuggestion(
        productName: String,
        answer: DeepSeekCategoryAnswer
    ): CategorySuggestion {
        val tipo = when (answer.tipoControl.uppercase()) {
            "UNIDAD" -> TipoControlDetectado.UNIDAD
            "LIQUIDO", "LÍQUIDO" -> TipoControlDetectado.LIQUIDO
            "PESO" -> TipoControlDetectado.PESO
            else -> TipoControlDetectado.DESCONOCIDO
        }

        val normalizedConfidence = if (answer.confianza <= 1.0) {
            (answer.confianza * 100).toInt()
        } else {
            answer.confianza.toInt()
        }.coerceIn(0, 100)

        val tipoConsulta = when (answer.tipoConsulta.uppercase()) {
            "GENERAL" -> TipoConsultaDetectada.GENERAL
            "AMBIGUO" -> TipoConsultaDetectada.AMBIGUO
            "INSUFICIENTE" -> TipoConsultaDetectada.INSUFICIENTE
            else -> TipoConsultaDetectada.ESPECIFICO
        }

        return CategorySuggestion(
            productName = productName,
            categoria = answer.categoria.trim(),
            emoji = inferEmoji(tipo),
            tipoControl = tipo,
            razonTipo = answer.razon.trim(),
            requiereReceta = answer.requiereReceta,
            razonReceta = if (answer.requiereReceta) answer.razon.trim() else "",
            modoIngreso = ModoIngresoDetectado.DESCONOCIDO,
            presentacionesSugeridas = emptyList(),
            keywords = answer.keywords.distinct(),
            existeEnLista = false,
            confianza = normalizedConfidence,
            razon = answer.descripcionBreveUso.ifBlank { answer.razon },
            isRefined = true,
            tipoConsulta = tipoConsulta,
            nombreCorregido = answer.nombreCorregido,
            sugerenciaUsuario = answer.sugerenciaUsuario,
            sugerenciasBusqueda = answer.sugerenciasBusqueda
        )
    }

    private fun inferEmoji(tipo: TipoControlDetectado): String {
        return when (tipo) {
            TipoControlDetectado.LIQUIDO -> "🧪"
            TipoControlDetectado.PESO -> "⚖️"
            TipoControlDetectado.UNIDAD -> "💊"
            TipoControlDetectado.DESCONOCIDO -> "💊"
        }
    }
}
