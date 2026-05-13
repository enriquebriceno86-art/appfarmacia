package com.app.administradorfarmadon.ActivityFragmentos.Fragmentos

import com.app.administradorfarmadon.AdaptersCaja.ResumenPagoTurnoItem
import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.MutableData
import com.google.firebase.database.ServerValue
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

data class ResultadoCierreTurno(
    val efectivoEsperado: Double,
    val totalVentas: Double,
    val totalEgresos: Double,
    val diferencia: Double,
    val estadoCuadre: String,
    val montoSobrante: Double,
    val montoFaltante: Double,
    val requiereObservacion: Boolean,
    val umbralAlerta: Double
)

data class DevolucionCierreLinea(
    val producto: String,
    val cantidad: Int,
    val presentacion: String,
    val monto: Double,
    val motivo: String,
    val tipoResolucion: String = "",
    val productoSustituto: String = "",
    val montoCobradoCliente: Double = 0.0,
    val montoDevueltoCliente: Double = 0.0,
    val medioPagoDiferencia: String = "",
    val montoPagadoDiferencia: Double = 0.0
)

object CajaTurnoHelper {

    fun mutableMapFromValue(value: Any?): MutableMap<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        (value as? Map<*, *>)?.forEach { (key, itemValue) ->
            if (key is String) {
                result[key] = itemValue
            }
        }
        return result
    }

    /**
     * Filtra solo los métodos de pago (Tarjetas, Transferencias, etc)
     * ignorando residuos menores a 1 centavo.
     */
    fun construirSoloMetodosPago(snapshot: DataSnapshot): List<ResumenPagoTurnoItem> {
        val items = mutableListOf<ResumenPagoTurnoItem>()
        for (child in snapshot.child("resumenMetodos").children) {
            val monto = child.child("monto").getValue(String::class.java)?.toDoubleOrNull() ?: 0.0

            // FILTRO CRÍTICO: Si el monto es 0.0, no lo agregamos a la lista visual del cierre
            if (kotlin.math.abs(monto) >= 0.01) {
                val titulo = child.child("titulo").getValue(String::class.java).orEmpty()
                items.add(ResumenPagoTurnoItem(titulo, monto))
            }
        }
        return items
    }

    /**
     * Construye el resumen completo: Apertura + Ventas - Egresos.
     */
    fun construirItemsResumenTurno(snapshot: DataSnapshot): List<ResumenPagoTurnoItem> {
        val items = mutableListOf<ResumenPagoTurnoItem>()

        // 1. Apertura
        val montoApertura = snapshot.child("montoApertura").getValue(String::class.java)?.toDoubleOrNull() ?: 0.0
        if (montoApertura >= 0.01) {
            items.add(ResumenPagoTurnoItem("Apertura en efectivo", montoApertura))
        }

        // 2. Ventas por método
        items.addAll(construirSoloMetodosPago(snapshot))

        // 3. Egresos (Salidas de dinero)
        for (child in snapshot.child("egresos").children) {
            val motivo = child.child("motivo").getValue(String::class.java).orEmpty()
            val monto = child.child("monto").getValue(String::class.java)?.toDoubleOrNull() ?: 0.0

            // Filtro de centavo para egresos
            if (kotlin.math.abs(monto) >= 0.01) {
                val titulo = if (motivo.isNotBlank()) "Egreso: $motivo" else "Egreso de caja"
                items.add(ResumenPagoTurnoItem(titulo, -monto))
            }
        }

        return items
    }

    fun mutableMapFromData(data: MutableData): MutableMap<String, Any?> {
        return mutableMapFromValue(data.value)
    }

    fun normalizarClaveResumen(titulo: String): String {
        return titulo.trim()
            .lowercase(Locale.getDefault())
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .ifBlank { "metodo" }
    }

    fun obtenerDetallePagoParaTurno(
        metodoPago: String,
        totalPagar: Double,
        detalleMetodos: Map<String, Double>
    ): Map<String, Double> {
        // Si ya viene el detalle (mixto), lo usamos
        if (detalleMetodos.isNotEmpty()) return detalleMetodos

        // Limpiamos el texto (ej: quita el "Ref: 123" si existe)
        val titulo = metodoPago.substringBefore(" (Ref:").trim()

        // Si el total es 0, nos aseguramos de que el mapa guarde el valor exacto 0.0
        return mapOf(titulo.ifBlank { "Promoción" } to totalPagar)
    }



    fun calcularResultadoCierre(
        snapshot: DataSnapshot,
        efectivoReal: Double,
        porcentajeAlertaDescuadre: Double,
        umbralMinimoAlertaDescuadre: Double
    ): ResultadoCierreTurno {
        val efectivoEsperado = snapshot.child("efectivoEsperado").getValue(String::class.java)
            ?.toDoubleOrNull() ?: 0.0
        val totalVentas = snapshot.child("totalVentas").getValue(String::class.java)
            ?.toDoubleOrNull() ?: 0.0
        val totalEgresos = snapshot.child("totalEgresos").getValue(String::class.java)
            ?.toDoubleOrNull() ?: 0.0
        val diferencia = efectivoReal - efectivoEsperado
        val estadoCuadre = when {
            abs(diferencia) < 0.01 -> "exacto"
            diferencia > 0 -> "sobrante"
            else -> "faltante"
        }
        val montoSobrante = if (diferencia > 0) diferencia else 0.0
        val montoFaltante = if (diferencia < 0) abs(diferencia) else 0.0
        val requiereObservacion = estadoCuadre != "exacto"
        val umbralAlerta = max(
            umbralMinimoAlertaDescuadre,
            efectivoEsperado * porcentajeAlertaDescuadre
        )

        return ResultadoCierreTurno(
            efectivoEsperado = efectivoEsperado,
            totalVentas = totalVentas,
            totalEgresos = totalEgresos,
            diferencia = diferencia,
            estadoCuadre = estadoCuadre,
            montoSobrante = montoSobrante,
            montoFaltante = montoFaltante,
            requiereObservacion = requiereObservacion,
            umbralAlerta = umbralAlerta
        )
    }

    fun aplicarCierreTurno(
        turnoActual: MutableMap<String, Any?>,
        fechaCierre: String,
        horaCierre: String,
        idUsuarioCierre: String,
        nombreUsuarioCierre: String,
        efectivoReal: Double,
        diferencia: Double,
        estadoCuadre: String,
        montoSobrante: Double,
        montoFaltante: Double,
        observacionCierre: String,
        observacionCierreManual: String,
        totalGeneralTurno: Double
    ): MutableMap<String, Any?> {
        turnoActual["estado"] = "cerrada"
        
        // UI Fields
        turnoActual["fechaCierreLocal"] = fechaCierre
        turnoActual["horaCierreLocal"] = horaCierre
        
        // Accounting Fields
        turnoActual["timestampCierreServidor"] = ServerValue.TIMESTAMP
        turnoActual["ultimaActualizacionServidor"] = ServerValue.TIMESTAMP
        
        turnoActual["idUsuarioCierre"] = idUsuarioCierre
        turnoActual["nombreUsuarioCierre"] = nombreUsuarioCierre
        turnoActual["efectivoReal"] = String.format(Locale.US, "%.2f", efectivoReal)
        turnoActual["diferenciaEfectivo"] = String.format(Locale.US, "%.2f", diferencia)
        turnoActual["estadoCuadre"] = estadoCuadre
        turnoActual["montoSobrante"] = String.format(Locale.US, "%.2f", montoSobrante)
        turnoActual["montoFaltante"] = String.format(Locale.US, "%.2f", montoFaltante)
        turnoActual["observacionCierre"] = observacionCierre
        turnoActual["observacionCierreManual"] = observacionCierreManual
        turnoActual["tieneObservacion"] = observacionCierreManual.isNotBlank()
        turnoActual["totalGeneralTurno"] = String.format(Locale.US, "%.2f", totalGeneralTurno)
        
        // Reemplazamos campos antiguos
        turnoActual.remove("horaCierre")
        turnoActual.remove("timestampCierre")
        turnoActual.remove("ultimaActualizacion")
        turnoActual.remove("fecha")

        return turnoActual
    }

    fun construirTextoObservacionDevolucion(linea: DevolucionCierreLinea): String {
        val motivo = linea.motivo.trim().ifBlank { "sin motivo especificado" }
        val descripcionBase = buildString {
            append("Se devolvio ")
            append(linea.producto.ifBlank { "un producto" })
            append(", cantidad ")
            append(formatearCantidadPresentacionHumana(linea.cantidad, linea.presentacion))
            if (linea.monto > 0.0) {
                append(", por ")
                append(MonedaHelper.formatear(linea.monto))
            }
            append(", motivo: ")
            append(motivo)
        }

        val resolucionExtra = buildString {
            when (linea.tipoResolucion.trim().lowercase(Locale.getDefault())) {
                "cambio_producto" -> {
                    if (linea.productoSustituto.isNotBlank()) {
                        append(". Cambio por ")
                        append(linea.productoSustituto)
                    } else {
                        append(". Cambio por otro producto")
                    }
                    when {
                        linea.montoCobradoCliente > 0.0 -> {
                            append(". Se cobro diferencia de ")
                            append(
                                MonedaHelper.formatear(
                                    linea.montoPagadoDiferencia.takeIf { it > 0.0 }
                                        ?: linea.montoCobradoCliente
                                )
                            )
                            if (linea.medioPagoDiferencia.isNotBlank()) {
                                append(", medio de pago: ")
                                append(linea.medioPagoDiferencia)
                            }
                        }
                        linea.montoDevueltoCliente > 0.0 -> {
                            append(". Diferencia devuelta: ")
                            append(MonedaHelper.formatear(linea.montoDevueltoCliente))
                        }
                    }
                }
                "dinero_devuelto" -> append(". Resolucion: devolver dinero")
            }
        }

        return (descripcionBase + resolucionExtra).trim().let {
            if (it.endsWith(".")) it else "$it."
        }
    }

    fun combinarObservacionCierre(
        observacionManual: String,
        devoluciones: List<DevolucionCierreLinea>
    ): String {
        val manual = observacionManual.trim()
        val textoDevoluciones = devoluciones
            .map { construirTextoObservacionDevolucion(it) }
            .distinct()
            .takeIf { it.isNotEmpty() }
            ?.joinToString(separator = "\n") { "• $it" }
            ?.let { "Devoluciones del turno:\n$it" }
            .orEmpty()

        return when {
            manual.isBlank() -> textoDevoluciones
            textoDevoluciones.isBlank() -> manual
            else -> "$manual\n\n$textoDevoluciones"
        }.trim()
    }

    private fun formatearCantidadPresentacionHumana(cantidad: Int, presentacion: String): String {
        val base = presentacion.substringBefore("(").trim().lowercase(Locale.getDefault())
        val singular = when {
            "bl" in base || "blíster" in base || "blister" in base -> "blíster"
            "caj" in base -> "caja"
            "uni" in base -> "unidad"
            base.isBlank() -> "unidad"
            else -> base
        }
        val textoPresentacion = if (cantidad == 1) {
            singular
        } else {
            when (singular) {
                "unidad" -> "unidades"
                "caja" -> "cajas"
                "blíster" -> "blísteres"
                else -> "${singular}s"
            }
        }
        return "$cantidad $textoPresentacion"
    }
}
