package com.app.administradorfarmadon.ActivitysInicio

import java.util.Locale

object MovimientoPresentationRules {

    private const val TITULO_FALLBACK = "Movimiento registrado"
    private const val MENSAJE_ESTADO_VACIO = "Sin movimientos en los ultimos 7 dias"

    fun construirTituloVisible(data: MovimientoTituloData): String {
        val tipoNormalizado = data.tipo.lowercase(Locale.getDefault())
        if (!tipoNormalizado.startsWith("metodo_pago")) {
            return data.titulo.ifBlank { TITULO_FALLBACK }
        }

        val nombreMetodo = data.tituloMetodoMetadata
            .takeIf { it.isNotBlank() }
            ?: extraerTextoEntreComillasSimples(data.descripcion)

        if (nombreMetodo.isBlank()) {
            return data.titulo.ifBlank { TITULO_FALLBACK }
        }

        return when {
            tipoNormalizado.contains("creado_automatico") -> "Metodo de pago por defecto creado: $nombreMetodo"
            tipoNormalizado.contains("creado") -> "Metodo de pago creado: $nombreMetodo"
            tipoNormalizado.contains("actualizado") -> "Metodo de pago actualizado: $nombreMetodo"
            tipoNormalizado.contains("eliminado") -> "Metodo de pago eliminado: $nombreMetodo"
            else -> data.titulo.ifBlank { TITULO_FALLBACK }
        }
    }

    fun construirEstadoVacio(vacio: Boolean): AnalisisMovimientosEstadoVacio {
        return AnalisisMovimientosEstadoVacio(
            mostrarLista = !vacio,
            mostrarEstadoVacio = vacio,
            mensajeEstadoVacio = MENSAJE_ESTADO_VACIO
        )
    }

    private fun extraerTextoEntreComillasSimples(texto: String): String {
        val inicio = texto.indexOf('\'')
        if (inicio < 0) return ""
        val fin = texto.indexOf('\'', inicio + 1)
        if (fin <= inicio) return ""
        return texto.substring(inicio + 1, fin).trim()
    }
}
