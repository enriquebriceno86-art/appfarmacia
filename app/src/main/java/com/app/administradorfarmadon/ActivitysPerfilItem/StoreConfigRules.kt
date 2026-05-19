package com.app.administradorfarmadon.ActivitysPerfilItem

object StoreConfigRules {

    fun validate(config: StoreConfig): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        errors.putAll(validateSection(config, StoreConfigSection.NEGOCIO))
        errors.putAll(validateSection(config, StoreConfigSection.MERCADO))
        errors.putAll(validateSection(config, StoreConfigSection.FISCAL))
        errors.putAll(validateSection(config, StoreConfigSection.OPERACION))
        return errors
    }

    fun validateSection(config: StoreConfig, section: StoreConfigSection): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        when (section) {
            StoreConfigSection.NEGOCIO -> {
                if (config.nombreComercial.isBlank()) errors["nombreComercial"] = "Obligatorio"
                if (config.razonSocial.isBlank()) errors["razonSocial"] = "Obligatorio"
            }

            StoreConfigSection.MERCADO -> {
                if (config.pais.isBlank()) errors["pais"] = "Obligatorio"
                if (config.estado.isBlank()) errors["estado"] = "Obligatorio"
                if (config.ciudad.isBlank()) errors["ciudad"] = "Obligatorio"
                if (config.direccion.isBlank()) errors["direccion"] = "Obligatorio"
                if (config.monedaCodigo.isBlank()) errors["monedaCodigo"] = "Obligatorio"
                if (config.monedaSimbolo.isBlank()) errors["monedaSimbolo"] = "Obligatorio"
            }

            StoreConfigSection.FISCAL -> {
                if (config.tipoDocumentoFiscal.isBlank()) errors["tipoDocumentoFiscal"] = "Obligatorio"
                if (config.nroDocumentoFiscal.isBlank()) errors["nroDocumentoFiscal"] = "Obligatorio"
                if (config.cobraImpuestos) {
                    if (config.nombreImpuesto.isBlank()) errors["nombreImpuesto"] = "Obligatorio"
                    if (config.porcentajeImpuesto < 0 || config.porcentajeImpuesto > 100) {
                        errors["porcentajeImpuesto"] = "Debe estar entre 0 y 100"
                    }
                }
            }

            StoreConfigSection.OPERACION -> {
                if (config.usarMargenMinimo && config.margenMinimoDefault < 0) {
                    errors["margenMinimoDefault"] = "Debe ser 0 o mayor"
                }
            }
        }
        return errors
    }
}
