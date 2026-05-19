package com.app.administradorfarmadon.ActivitysPerfilItem

/**
 * Resultado de la validación de un método de pago.
 */
data class PaymentValidationResult(
    val isValid: Boolean,
    val fieldErrors: Map<String, String> = emptyMap(),
    val globalWarnings: List<String> = emptyList()
)

/**
 * Motor de reglas para validar la configuración de métodos de pago
 * según su categoría.
 */
object PaymentMethodValidationRules {

    fun validate(form: PaymentFormState): PaymentValidationResult {
        val errors = mutableMapOf<String, String>()
        val warnings = mutableListOf<String>()

        // 1. Validación común: Nombre/Título
        if (form.title.isBlank()) {
            errors["title"] = "El nombre del método es obligatorio."
        }

        // 2. Validaciones por categoría
        when (form.category) {
            "efectivo" -> {
                if (form.permiteVuelto && !form.solicitaMontoRecibido) {
                    warnings.add("Recomendación: Si permites vuelto, deberías activar 'Solicita monto recibido' para un mejor control en caja.")
                }
            }

            "transferencia_bancaria" -> {
                if (form.banco.isBlank()) errors["banco"] = "Indica el nombre del banco."
                if (form.numeroCuenta.isBlank()) errors["numeroCuenta"] = "El número de cuenta es necesario para que el cliente pueda pagar."
                if (form.titularBanco.isBlank()) errors["titularBanco"] = "Indica el nombre del titular de la cuenta."
            }

            "billetera_digital" -> {
                if (form.telefonoBilletera.isBlank() && form.aliasBilletera.isBlank()) {
                    errors["telefonoBilletera"] = "Debes ingresar al menos el teléfono o el alias para identificar la billetera."
                    errors["aliasBilletera"] = "Falta un identificador (Teléfono o Alias)."
                }
                
                if (form.usaQr && form.qrUrl.isBlank()) {
                    errors["qrUrl"] = "Has marcado que usa QR, pero no has cargado ninguna imagen aún."
                }
            }
            
            "tarjeta" -> {
                // Solo validación de nombre (ya realizada arriba)
            }
            
            "otro" -> {
                // Solo validación de nombre (ya realizada arriba)
            }
        }

        return PaymentValidationResult(
            isValid = errors.isEmpty(),
            fieldErrors = errors,
            globalWarnings = warnings
        )
    }
}
