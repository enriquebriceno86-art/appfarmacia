package com.app.administradorfarmadon.ActivityInventario.bulk

import android.content.Context
import android.net.Uri
import com.app.administradorfarmadon.ActivityInventario.ProductUtils
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Motor simple para leer y parsear archivos CSV.
 * Detecta automáticamente delimitadores (coma o punto y coma) y mapea a borradores.
 */
object BulkImportCsvParser {

    /**
     * Procesa un URI de un archivo CSV y devuelve una lista de borradores.
     * Soporta saltos de línea dentro de celdas entrecomilladas.
     */
    fun parse(context: Context, uri: Uri): List<ImportDraftProduct> {
        val drafts = mutableListOf<ImportDraftProduct>()
        
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                
                // 1. Leer todas las líneas procesando el estado inQuotes para celdas multilinea
                val fullLines = mutableListOf<List<String>>()
                var currentFields = mutableListOf<String>()
                var currentField = StringBuilder()
                var inQuotes = false
                var wasQuotedInField = false
                var charInt: Int
                var delimiter: String? = null

                while (reader.read().also { charInt = it } != -1) {
                    val c = charInt.toChar()

                    when {
                        c == '\"' -> {
                            wasQuotedInField = true
                            // Revisar comilla escapada ""
                            reader.mark(1)
                            val nextInt = reader.read()
                            if (inQuotes && nextInt != -1 && nextInt.toChar() == '\"') {
                                currentField.append('\"')
                            } else {
                                inQuotes = !inQuotes
                                if (nextInt != -1) reader.reset()
                            }
                        }
                        (delimiter == null && (c == ',' || c == ';')) && !inQuotes -> {
                            delimiter = c.toString()
                            val value = if (wasQuotedInField) currentField.toString() else currentField.toString().trim()
                            currentFields.add(value)
                            currentField.setLength(0)
                            wasQuotedInField = false
                        }
                        delimiter != null && c.toString() == delimiter && !inQuotes -> {
                            val value = if (wasQuotedInField) currentField.toString() else currentField.toString().trim()
                            currentFields.add(value)
                            currentField.setLength(0)
                            wasQuotedInField = false
                        }
                        (c == '\n' || c == '\r') && !inQuotes -> {
                            if (c == '\r') {
                                reader.mark(1)
                                if (reader.read().toChar() != '\n') reader.reset()
                            }
                            val value = if (wasQuotedInField) currentField.toString() else currentField.toString().trim()
                            currentFields.add(value)
                            if (currentFields.any { it.isNotEmpty() }) {
                                fullLines.add(currentFields.toList())
                            }
                            currentFields.clear()
                            currentField.setLength(0)
                            wasQuotedInField = false
                        }
                        else -> {
                            currentField.append(c)
                        }
                    }
                }
                // Añadir última línea si quedó algo pendiente
                if (currentField.isNotEmpty() || currentFields.isNotEmpty()) {
                    val value = if (wasQuotedInField) currentField.toString() else currentField.toString().trim()
                    currentFields.add(value)
                    fullLines.add(currentFields)
                }

                if (fullLines.isEmpty()) return emptyList()

                // 2. Procesar encabezados
                val headerRow = fullLines.first()
                val indexMap = mapHeaders(headerRow.map { it.lowercase() })

                // 3. Procesar datos
                fullLines.drop(1).forEach { columns ->
                    val draft = mapToDraft(columns, indexMap)
                    drafts.add(draft)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return drafts
    }

    // Eliminamos parseCsvLine ya que el procesamiento ahora es por caracter en parse()

    private fun mapHeaders(headers: List<String>): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        headers.forEachIndexed { index, header ->
            when {
                header.contains("nombre") || header.contains("producto") || header.contains("descripcion") -> 
                    map["name"] = index
                header.contains("stock") || header.contains("cantidad") || header.contains("existencia") -> 
                    map["stock"] = index
                header.contains("costo") || header.contains("compra") -> 
                    map["cost"] = index
                header.contains("precio") || header.contains("venta") -> 
                    map["price"] = index
                header.contains("lote") -> 
                    map["lot"] = index
                header.contains("vence") || header.contains("vencimiento") || header.contains("expira") -> 
                    map["expiration"] = index
                header.contains("categoria") -> 
                    map["category"] = index
            }
        }
        return map
    }

    private fun mapToDraft(columns: List<String>, indexMap: Map<String, Int>): ImportDraftProduct {
        val rawName = getColumn(columns, indexMap["name"])
        val rawStock = getColumn(columns, indexMap["stock"])
        val rawCost = getColumn(columns, indexMap["cost"])
        val rawPrice = getColumn(columns, indexMap["price"])
        val rawLot = getColumn(columns, indexMap["lot"])
        val rawExpiration = getColumn(columns, indexMap["expiration"])
        val rawCategory = getColumn(columns, indexMap["category"])

        // Validación inicial rápida
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        if (rawName.isBlank()) errors.add("Falta el nombre del producto")
        
        val costValue = rawCost.replace(",", ".").replace("$", "").trim().toDoubleOrNull() ?: 0.0
        if (costValue <= 0) errors.add("Costo de compra inválido")

        val stockValue = rawStock.replace(",", ".").trim().toDoubleOrNull() ?: 0.0
        
        val normalizedExpiration = ProductUtils.normalizarVencimiento(rawExpiration)
        if (rawExpiration.isNotBlank() && normalizedExpiration.isBlank()) {
            warnings.add("Formato de vencimiento no reconocido ($rawExpiration). Se usará vacío.")
        }

        return ImportDraftProduct(
            rawName = rawName,
            rawStock = rawStock,
            rawPurchaseCost = rawCost,
            rawSalePrice = rawPrice,
            rawLotNumber = rawLot,
            rawExpirationDate = rawExpiration,
            rawCategory = rawCategory,
            
            name = rawName,
            initialStock = stockValue,
            purchaseCost = costValue,
            lotNumber = rawLot.uppercase(),
            expirationDate = normalizedExpiration,
            category = rawCategory.ifBlank { "Sin categoría" },
            
            errors = errors,
            warnings = warnings,
            validationState = when {
                errors.isNotEmpty() -> ImportValidationState.ERROR
                warnings.isNotEmpty() -> ImportValidationState.WARNING
                else -> ImportValidationState.READY
            }
        )
    }

    private fun getColumn(columns: List<String>, index: Int?): String {
        if (index == null || index >= columns.size) return ""
        return columns[index]
    }
}
