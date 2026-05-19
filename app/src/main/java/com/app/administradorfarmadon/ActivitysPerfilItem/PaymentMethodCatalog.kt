package com.app.administradorfarmadon.ActivitysPerfilItem

data class PaymentMethodSuggestion(
    val title: String,
    val category: String,
    val description: String,
    val usaQr: Boolean = false,
    val permiteReferencia: Boolean = false,
    val permiteVuelto: Boolean = false,
    val solicitaMontoRecibido: Boolean = false,
    val calculaVuelto: Boolean = false,
    val disponibleMixto: Boolean = true
)

object PaymentMethodCatalog {

    fun suggestionsForCountry(country: String): List<PaymentMethodSuggestion> {
        return when (normalize(country)) {
            "peru" -> listOf(
                PaymentMethodSuggestion(
                    title = "Efectivo",
                    category = "efectivo",
                    description = "Cobro directo en caja con vuelto y monto recibido.",
                    permiteVuelto = true,
                    solicitaMontoRecibido = true,
                    calculaVuelto = true
                ),
                PaymentMethodSuggestion(
                    title = "Yape",
                    category = "billetera_digital",
                    description = "Billetera digital popular en Peru.",
                    usaQr = true,
                    permiteReferencia = true
                ),
                PaymentMethodSuggestion(
                    title = "Plin",
                    category = "billetera_digital",
                    description = "Cobro por billetera digital muy usado en mostrador.",
                    usaQr = true,
                    permiteReferencia = true
                ),
                PaymentMethodSuggestion(
                    title = "Transferencia bancaria",
                    category = "transferencia_bancaria",
                    description = "Transferencias a cuentas bancarias de la farmacia.",
                    usaQr = false,
                    permiteReferencia = true
                ),
                PaymentMethodSuggestion(
                    title = "Tarjeta",
                    category = "tarjeta",
                    description = "Cobro con POS o terminal.",
                    permiteReferencia = true
                )
            )

            "bolivia" -> listOf(
                PaymentMethodSuggestion(
                    title = "Efectivo",
                    category = "efectivo",
                    description = "Cobro directo con vuelto en caja.",
                    permiteVuelto = true,
                    solicitaMontoRecibido = true,
                    calculaVuelto = true
                ),
                PaymentMethodSuggestion(
                    title = "QR",
                    category = "billetera_digital",
                    description = "Cobros por QR interoperable frecuentes en Bolivia.",
                    usaQr = true,
                    permiteReferencia = true
                ),
                PaymentMethodSuggestion(
                    title = "Transferencia bancaria",
                    category = "transferencia_bancaria",
                    description = "Transferencias entre bancos y comprobante.",
                    permiteReferencia = true
                ),
                PaymentMethodSuggestion(
                    title = "Tigo Money",
                    category = "billetera_digital",
                    description = "Billetera movil usada en el mercado boliviano.",
                    usaQr = false,
                    permiteReferencia = true
                ),
                PaymentMethodSuggestion(
                    title = "Tarjeta",
                    category = "tarjeta",
                    description = "Cobro con terminal de tarjeta.",
                    permiteReferencia = true
                )
            )

            "venezuela" -> listOf(
                PaymentMethodSuggestion(
                    title = "Efectivo",
                    category = "efectivo",
                    description = "Cobro de mostrador con vuelto.",
                    permiteVuelto = true,
                    solicitaMontoRecibido = true,
                    calculaVuelto = true
                ),
                PaymentMethodSuggestion(
                    title = "Pago movil",
                    category = "billetera_digital",
                    description = "Metodo muy comun para cobro rapido en Venezuela.",
                    permiteReferencia = true
                ),
                PaymentMethodSuggestion(
                    title = "Transferencia bancaria",
                    category = "transferencia_bancaria",
                    description = "Transferencia entre bancos con referencia.",
                    permiteReferencia = true
                ),
                PaymentMethodSuggestion(
                    title = "Zelle",
                    category = "billetera_digital",
                    description = "Cobro alternativo usado en algunos negocios.",
                    permiteReferencia = true
                ),
                PaymentMethodSuggestion(
                    title = "Tarjeta",
                    category = "tarjeta",
                    description = "Cobro con punto de venta.",
                    permiteReferencia = true
                )
            )

            "colombia" -> listOf(
                PaymentMethodSuggestion(
                    title = "Efectivo",
                    category = "efectivo",
                    description = "Cobro directo con vuelto.",
                    permiteVuelto = true,
                    solicitaMontoRecibido = true,
                    calculaVuelto = true
                ),
                PaymentMethodSuggestion(
                    title = "Nequi",
                    category = "billetera_digital",
                    description = "Billetera digital comun en Colombia.",
                    usaQr = true,
                    permiteReferencia = true
                ),
                PaymentMethodSuggestion(
                    title = "Daviplata",
                    category = "billetera_digital",
                    description = "Cobro por billetera digital de uso frecuente.",
                    usaQr = true,
                    permiteReferencia = true
                ),
                PaymentMethodSuggestion(
                    title = "Transferencia bancaria",
                    category = "transferencia_bancaria",
                    description = "Transferencias y comprobantes.",
                    permiteReferencia = true
                ),
                PaymentMethodSuggestion(
                    title = "Tarjeta",
                    category = "tarjeta",
                    description = "Cobro con tarjeta.",
                    permiteReferencia = true
                )
            )

            else -> listOf(
                PaymentMethodSuggestion(
                    title = "Efectivo",
                    category = "efectivo",
                    description = "Cobro directo con vuelto.",
                    permiteVuelto = true,
                    solicitaMontoRecibido = true,
                    calculaVuelto = true
                ),
                PaymentMethodSuggestion(
                    title = "Transferencia bancaria",
                    category = "transferencia_bancaria",
                    description = "Transferencias bancarias del negocio.",
                    permiteReferencia = true
                ),
                PaymentMethodSuggestion(
                    title = "Tarjeta",
                    category = "tarjeta",
                    description = "Cobro con tarjeta o POS.",
                    permiteReferencia = true
                )
            )
        }
    }

    private fun normalize(value: String): String {
        return value.lowercase()
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .trim()
    }
}
