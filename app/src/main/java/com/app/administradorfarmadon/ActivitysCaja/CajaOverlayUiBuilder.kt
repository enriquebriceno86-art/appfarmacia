package com.app.administradorfarmadon.ActivitysCaja

import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import java.util.Locale

/**
 * HUMANO: Configuracion pura del overlay de apertura/verificacion.
 * El Fragment sigue aplicando la UI final; aqui solo armamos texto,
 * visibilidad del input y valor sugerido.
 */
object CajaOverlayUiBuilder {

    data class CajaOverlayUiModel(
        val titulo: String,
        val mensaje: String,
        val mostrarInputMonto: Boolean,
        val textoBoton: String,
        val montoSugerido: Double? = null,
        val montoPrellenado: String? = null
    )

    fun construirOverlayVerificando(
        esTablet: Boolean
    ): CajaOverlayUiModel {
        return CajaOverlayUiModel(
            titulo = "Apertura de Caja",
            mensaje = if (esTablet) {
                "Configura el saldo inicial para comenzar tu jornada."
            } else {
                "Monto inicial para ventas"
            },
            mostrarInputMonto = true,
            textoBoton = "Iniciar Turno",
            montoSugerido = null,
            montoPrellenado = null
        )
    }

    fun construirOverlayParaPrepararVerificacion(
        esTablet: Boolean
    ): CajaOverlayUiModel {
        return construirOverlayVerificando(esTablet)
    }

    fun construirOverlayAperturaDisponible(
        esTablet: Boolean,
        saldoSugerido: Double
    ): CajaOverlayUiModel {
        val mensaje = if (esTablet) {
            "Configura el saldo inicial para comenzar tu jornada. Hemos detectado un saldo remanente del turno previo."
        } else {
            "Configura el saldo inicial para comenzar a vender."
        }

        return CajaOverlayUiModel(
            titulo = "Apertura de Caja",
            mensaje = mensaje,
            mostrarInputMonto = true,
            textoBoton = "Iniciar Turno",
            montoSugerido = if (saldoSugerido > 0.009) saldoSugerido else null,
            montoPrellenado = null // Ahora el campo inicia vacío por seguridad UX
        )
    }

    fun construirOverlayParaAperturaDisponible(
        esTablet: Boolean,
        saldoSugerido: Double
    ): CajaOverlayUiModel {
        return construirOverlayAperturaDisponible(
            esTablet = esTablet,
            saldoSugerido = saldoSugerido
        )
    }

    fun construirOverlayParaResultadoSinTurno(
        esTablet: Boolean,
        resultado: CajaVerificacionTurnoMapper.CajaVerificacionTurnoModel
    ): CajaOverlayUiModel {
        return construirOverlayAperturaDisponible(
            esTablet = esTablet,
            saldoSugerido = resultado.saldoSugeridoApertura
        )
    }

    fun construirOverlaySinVerificacion(): CajaOverlayUiModel {
        return CajaOverlayUiModel(
            titulo = "No se pudo verificar la caja",
            mensaje = "Revisa tu conexión y vuelve a intentarlo antes de abrir o usar el turno.",
            mostrarInputMonto = false,
            textoBoton = "Reintentar",
            montoPrellenado = null
        )
    }

    fun construirOverlayParaFalloVerificacion(): CajaOverlayUiModel {
        return construirOverlaySinVerificacion()
    }
}
