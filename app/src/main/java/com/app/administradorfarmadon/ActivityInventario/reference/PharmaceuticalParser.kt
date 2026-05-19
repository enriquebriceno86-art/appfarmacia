package com.app.administradorfarmadon.ActivityInventario.reference

import java.util.Locale

/**
 * Utilidad para interpretar entradas "sucias" o comprimidas de productos farmacéuticos.
 * (V3.25)
 */
object PharmaceuticalParser {

    data class ParsedProduct(
        val original: String,
        val baseName: String,
        val strength: String,
        val form: String,
        val fullNormalized: String
    )

    /**
     * Toma una cadena como "omeprazol20mgcaps" y devuelve una estructura desglosada.
     */
    fun parse(input: String): ParsedProduct {
        if (input.isBlank()) return ParsedProduct("", "", "", "", "")

        // 1. Normalización semántica (V3.24) incorporada
        val normalized = normalizeProductName(input)
        
        // 2. Extracción de Fuerza/Concentración
        // Busca patrones como "500 mg", "250 mg / 5 ml", "1 g", "0.05 %"
        // Refinado V3.29: Asegurar que coincida con el patrón completo
        val strengthRegex = Regex("(\\d+(\\.\\d+)?\\s*(mg|g|ml|mcg|ui|%|u|kg)(\\s*/\\s*\\d+\\s*(ml|g|mg))?)", RegexOption.IGNORE_CASE)
        val strengthMatch = strengthRegex.find(normalized)
        val strength = strengthMatch?.value?.trim() ?: ""

        // 3. Extracción de Forma Farmacéutica (Refinado V3.29)
        // Usamos Regex con límites de palabra (\b) O al final de la cadena ($) 
        // para evitar que "amp" rompa "ampicilina".
        val formKeywords = listOf(
            "capsulas", "caps", "tabletas", "tabs", "pastillas", "comprimidos", 
            "suspension", "susp", "jarabe", "syr", "ampolla", "amp", "vial", 
            "crema", "pomada", "unguento", "spray", "gotas", "sachet", "sobre", "gel"
        )
        
        // El regex busca la forma si está: 
        // a) Sola tras un espacio/número: (?<=[0-9\s])form\b
        // b) Al final de la cadena pegada a algo: (?<=[a-z0-9])form$
        // c) Como palabra independiente: \bform\b
        val formPattern = "(\\b(${formKeywords.joinToString("|")})\\b|(?<=[0-9\\s])(${formKeywords.joinToString("|")})\\b|(?<=[a-z0-9])(${formKeywords.joinToString("|")})$)"
        val formRegex = Regex(formPattern, RegexOption.IGNORE_CASE)
        val formMatch = formRegex.find(normalized)
        val detectedForm = formMatch?.value?.trim() ?: ""

        // 4. Nombre Base (lo que queda tras extraer fuerza y forma)
        // Refinado V3.30: Limpieza por coordenadas exactas para evitar corromper nombres 
        // que contienen la misma subcadena (ej: "Gelocatil 500 gel")
        var baseNameBuilder = StringBuilder(normalized)
        
        // Removemos de atrás hacia adelante para no invalidar los índices del primer match
        val matchesToRemove = listOfNotNull(strengthMatch, formMatch)
            .sortedByDescending { it.range.first }

        for (match in matchesToRemove) {
            baseNameBuilder.replace(match.range.first, match.range.last + 1, "")
        }
        
        var baseName = baseNameBuilder.toString().replace(Regex("\\s+"), " ").trim()
        
        // Si el baseName quedó vacío (ej: solo escribió "500mg"), devolvemos el original normalizado
        val finalBaseName = baseName.ifBlank { normalized }.replaceFirstChar { it.uppercase() }

        return ParsedProduct(
            original = input,
            baseName = finalBaseName,
            strength = strength.lowercase(),
            form = detectedForm.lowercase(),
            fullNormalized = "$finalBaseName ${strength.lowercase()} ${detectedForm.lowercase()}".trim().replace(Regex("\\s+"), " ")
        )
    }
}
