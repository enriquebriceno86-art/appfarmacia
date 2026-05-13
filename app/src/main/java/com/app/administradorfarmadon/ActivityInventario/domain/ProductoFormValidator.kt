package com.app.administradorfarmadon.ActivityInventario.domain

import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.PresentacionProducto

/**
 * Validador puro del formulario de producto.
 *
 * Mantiene las reglas fuera del Activity para que Crear/Editar
 * solo traduzcan el resultado a errores visuales.
 */
object ProductoFormValidator {

    enum class Campo {
        NOMBRE,
        VENCIMIENTO,
        CATEGORIA,
        COSTO_COMPRA,
        UNIDAD_BASE,
        CANTIDAD,
        STOCK_MINIMO,
        CANTIDAD_PRESENTACIONES,
        UNIDADES_POR_PRESENTACION,
        STOCK_MINIMO_PRESENTACION,
        PRESENTACION_PRINCIPAL
    }

    sealed interface Resultado {
        data object Valido : Resultado
        data class CampoVacio(val campo: Campo) : Resultado
        data class CampoInvalido(val campo: Campo, val motivo: MotivoCampoInvalido) : Resultado
        data object ListaPresentacionesVacia : Resultado
        data object PresentacionPrincipalNoSeleccionada : Resultado
        data object PresentacionPrincipalDebeElegirseManualmente : Resultado
        data object PresentacionPrincipalNoExiste : Resultado
        data class PresentacionSuperaContenedor(
            val presentacion: PresentacionProducto,
            val maximo: Int
        ) : Resultado
        data class PresentacionSuperaStockTotal(
            val presentacion: PresentacionProducto,
            val stockTotal: Int
        ) : Resultado
        data class PresentacionesSinPrecio(
            val presentaciones: List<PresentacionProducto>
        ) : Resultado
    }

    enum class MotivoCampoInvalido {
        SOLO_TEXTO_SIN_NUMEROS,
        DEBE_SER_MAYOR_A_CERO,
        DEBE_SER_MAYOR_O_IGUAL_A_CERO,
        TOTAL_CALCULADO_INVALIDO
    }

    fun validarCamposBasicos(data: ProductoFormData): Resultado {
        if (data.nombre.isBlank()) return Resultado.CampoVacio(Campo.NOMBRE)
        if (data.requiereVencimientoGeneral && data.vencimiento.isBlank()) {
            return Resultado.CampoVacio(Campo.VENCIMIENTO)
        }
        if (data.categoria.isBlank()) return Resultado.CampoVacio(Campo.CATEGORIA)
        if (data.requiereCostoCompraBase && data.costoCompraTexto.isBlank()) {
            return Resultado.CampoVacio(Campo.COSTO_COMPRA)
        }
        if (data.unidadBase.isBlank()) return Resultado.CampoVacio(Campo.UNIDAD_BASE)

        if (data.unidadBase.any { it.isDigit() }) {
            return Resultado.CampoInvalido(
                campo = Campo.UNIDAD_BASE,
                motivo = MotivoCampoInvalido.SOLO_TEXTO_SIN_NUMEROS
            )
        }

        if (data.requiereCostoCompraBase) {
            val costoCompra = data.costoCompra
            if (costoCompra == null || costoCompra <= 0.0) {
                return Resultado.CampoInvalido(
                    campo = Campo.COSTO_COMPRA,
                    motivo = MotivoCampoInvalido.DEBE_SER_MAYOR_A_CERO
                )
            }
        }

        return when {
            data.registroPorUnidades -> validarRegistroPorUnidades(data)
            data.registroPorPaquetes -> validarRegistroPorPaquetes(data)
            else -> Resultado.Valido
        }
    }

    fun validarPresentaciones(data: ProductoFormData): Resultado {
        if (!data.usaPresentaciones) return Resultado.Valido

        if (data.listaPresentaciones.isEmpty()) {
            return Resultado.ListaPresentacionesVacia
        }

        if (data.presentacionPrincipal.isBlank()) {
            return Resultado.PresentacionPrincipalNoSeleccionada
        }

        if (data.listaPresentaciones.size > 1 && !data.presentacionPrincipalElegidaManualmente) {
            return Resultado.PresentacionPrincipalDebeElegirseManualmente
        }

        if (!PresentacionRules.existePresentacionPrincipal(data.presentacionPrincipal, data.listaPresentaciones)) {
            return Resultado.PresentacionPrincipalNoExiste
        }

        PresentacionRules.primeraPresentacionQueSuperaContenedor(
            listaPresentaciones = data.listaPresentaciones,
            maxUnidadesPorContenedor = data.maxUnidadesPorContenedor
        )?.let { invalida ->
            return Resultado.PresentacionSuperaContenedor(
                presentacion = invalida,
                maximo = data.maxUnidadesPorContenedor ?: 0
            )
        }

        PresentacionRules.primeraPresentacionQueSuperaStockTotal(
            listaPresentaciones = data.listaPresentaciones,
            stockTotalEnUnidadBase = data.stockTotalEnUnidadBase
        )?.let { invalida ->
            return Resultado.PresentacionSuperaStockTotal(
                presentacion = invalida,
                stockTotal = data.stockTotalEnUnidadBase
            )
        }

        if (data.validarPrecioPresentaciones) {
            val sinPrecio = PresentacionRules.presentacionesSinPrecio(data.listaPresentaciones)
            if (sinPrecio.isNotEmpty()) {
                return Resultado.PresentacionesSinPrecio(sinPrecio)
            }
        }

        return Resultado.Valido
    }

    private fun validarRegistroPorUnidades(data: ProductoFormData): Resultado {
        if (data.cantidadTexto.isBlank()) return Resultado.CampoVacio(Campo.CANTIDAD)
        if (data.stockMinimoTexto.isBlank()) return Resultado.CampoVacio(Campo.STOCK_MINIMO)

        val cantidad = data.cantidad
        if (cantidad == null || cantidad <= 0) {
            return Resultado.CampoInvalido(
                campo = Campo.CANTIDAD,
                motivo = MotivoCampoInvalido.DEBE_SER_MAYOR_A_CERO
            )
        }

        val stockMinimo = data.stockMinimo
        if (stockMinimo == null || stockMinimo < 0) {
            return Resultado.CampoInvalido(
                campo = Campo.STOCK_MINIMO,
                motivo = MotivoCampoInvalido.DEBE_SER_MAYOR_O_IGUAL_A_CERO
            )
        }

        return Resultado.Valido
    }

    private fun validarRegistroPorPaquetes(data: ProductoFormData): Resultado {
        if (data.cantidadPresentacionesTexto.isBlank()) {
            return Resultado.CampoVacio(Campo.CANTIDAD_PRESENTACIONES)
        }
        if (data.unidadesPorPresentacionTexto.isBlank()) {
            return Resultado.CampoVacio(Campo.UNIDADES_POR_PRESENTACION)
        }
        if (data.stockMinimoPresentacionTexto.isBlank()) {
            return Resultado.CampoVacio(Campo.STOCK_MINIMO_PRESENTACION)
        }

        val cantidadPresentaciones = data.cantidadPresentaciones
        if (cantidadPresentaciones == null || cantidadPresentaciones <= 0) {
            return Resultado.CampoInvalido(
                campo = Campo.CANTIDAD_PRESENTACIONES,
                motivo = MotivoCampoInvalido.DEBE_SER_MAYOR_A_CERO
            )
        }

        val unidadesPorPresentacion = data.unidadesPorPresentacion
        if (unidadesPorPresentacion == null || unidadesPorPresentacion <= 0) {
            return Resultado.CampoInvalido(
                campo = Campo.UNIDADES_POR_PRESENTACION,
                motivo = MotivoCampoInvalido.DEBE_SER_MAYOR_A_CERO
            )
        }

        val stockMinimoPresentacion = data.stockMinimoPresentacion
        if (stockMinimoPresentacion == null || stockMinimoPresentacion < 0) {
            return Resultado.CampoInvalido(
                campo = Campo.STOCK_MINIMO_PRESENTACION,
                motivo = MotivoCampoInvalido.DEBE_SER_MAYOR_O_IGUAL_A_CERO
            )
        }

        val total = cantidadPresentaciones * unidadesPorPresentacion
        if (total <= 0) {
            return Resultado.CampoInvalido(
                campo = Campo.CANTIDAD_PRESENTACIONES,
                motivo = MotivoCampoInvalido.TOTAL_CALCULADO_INVALIDO
            )
        }

        return Resultado.Valido
    }
}
