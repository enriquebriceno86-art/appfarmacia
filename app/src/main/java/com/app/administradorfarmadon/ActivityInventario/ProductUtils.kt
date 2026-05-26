package com.app.administradorfarmadon.ActivityInventario

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteProducto
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos
import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import com.app.administradorfarmadon.ClasesDatabase.PresentacionHelper
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.Normalizer
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.regex.Pattern

/**
 * Objeto de utilidad para centralizar la lógica de negocio, validaciones y 
 * procesamiento de datos relacionados con los productos del inventario.
 */
object ProductUtils {


    data class LoteOcrResult(
        val lote: String?,
        val vencimiento: String?,
        val confianzaLote: Int,
        val confianzaVencimiento: Int,
        val textoOcr: String
    )

    private const val UMBRAL_SUGERENCIA_CATEGORIA = 0.58
    enum class MotivoIndisponibilidadVenta {
        SIN_STOCK_REAL,
        SIN_LOTES_REGISTRADOS,
        LOTES_BLOQUEADOS,
        LOTES_VENCIDOS,
        LOTES_BLOQUEADOS_O_VENCIDOS
    }

    data class EstadoDisponibilidadVenta(
        val stockFisico: Int,
        val stockVendible: Int,
        val tieneLotesRegistrados: Boolean,
        val lotesBloqueados: Int,
        val lotesVencidos: Int,
        val motivoIndisponibilidad: MotivoIndisponibilidadVenta?
    )

    private data class TextoNormalizadoConMapa(
        val texto: String,
        val mapaIndicesOriginales: IntArray
    )

    /**
     * V30.6: Intenta normalizar formatos complejos (DD/MM/AA, MM/AA, etc.) a MM/AA.
     * Corregido para priorizar el Mes y el Año cuando hay 3 segmentos (Día/Mes/Año).
     */
    fun normalizarVencimiento(vencimiento: String): String {
        val raw = vencimiento
            .trim()
            .uppercase(Locale.ROOT)
            .replace(Regex("^[A-Z.\\s]+[:#.-]?"), "")
            .replace(Regex("[^A-Z0-9]"), "/")
        if (raw.isBlank()) return ""

        // 1. Dividir por segmentos (separados por /, -, ., espacio)
        val parts = raw.split("/").filter { it.isNotBlank() }

        return when (parts.size) {
            3 -> {
                // Caso: 12/01/25 o 12/01/2025 (Día/Mes/Año)
                // En farmacia, el formato estándar es Día/Mes/Año. Tomamos la parte 2 y 3.
                val mes = parts[1].filter { it.isDigit() }.padStart(2, '0')
                val anio = parts[2].filter { it.isDigit() }.takeLast(2)

                if (mes.toIntOrNull() in 1..12) "$mes/$anio" else ""
            }
            2 -> {
                // Caso: 01/25 o ENE/2025
                val mesPart = parts[0].filter { it.isDigit() }
                val anioPart = parts[1].filter { it.isDigit() }

                if (mesPart.isNotEmpty() && anioPart.length >= 2) {
                    val mm = mesPart.padStart(2, '0')
                    val aa = anioPart.takeLast(2)
                    if (mm.toIntOrNull() in 1..12) "$mm/$aa" else ""
                } else ""
            }
            else -> {
                // Caer en la lógica de puros dígitos si no hay separadores claros (ej: 012025)
                val clean = raw.filter { it.isDigit() }
                when (clean.length) {
                    4 -> { // MMAA
                        val mm = clean.substring(0, 2)
                        val aa = clean.substring(2, 4)
                        if (mm.toIntOrNull() in 1..12) "$mm/$aa" else ""
                    }
                    6 -> { // DDMMYY o MMYYYY
                        // Intentamos detectar si termina en un año probable (24, 25, 26, 27...)
                        val last2 = clean.takeLast(2).toIntOrNull() ?: 0
                        if (last2 in 24..40) { // DD/MM/YY
                            val mm = clean.substring(2, 4)
                            val aa = clean.substring(4, 6)
                            if (mm.toIntOrNull() in 1..12) "$mm/$aa" else ""
                        } else { // MM/YYYY
                            val mm = clean.substring(0, 2)
                            val aa = clean.takeLast(2)
                            if (mm.toIntOrNull() in 1..12) "$mm/$aa" else ""
                        }
                    }
                    8 -> { // DDMMYYYY
                        val mm = clean.substring(2, 4)
                        val aa = clean.takeLast(2)
                        if (mm.toIntOrNull() in 1..12) "$mm/$aa" else ""
                    }
                    else -> ""
                }
            }
        }
    }



    /**
     * V17.49: Codifica un número de lote de forma reversible y segura para Firebase Keys.
     * Usa Base64 URL-Safe sin padding para evitar colisiones y caracteres prohibidos (#, ., $, etc).
     */
    fun encodeLotKey(lotNumber: String): String {
        val clean = lotNumber.trim().uppercase()
        if (clean.isBlank()) return "EMPTY_LOT"
        return Base64.encodeToString(clean.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    fun estaLoteBloqueadoParaVenta(lote: LoteProducto): Boolean {
        return lote.cantidadBloqueada > 0.0 || lote.motivoBloqueo.trim().isNotBlank()
    }

    fun cantidadVendibleLote(lote: LoteProducto): Double {
        return if (estaLoteBloqueadoParaVenta(lote)) 0.0 else lote.cantidad.coerceAtLeast(0.0)
    }

    fun cantidadFisicaLote(lote: LoteProducto): Double {
        return lote.cantidad.coerceAtLeast(0.0) + lote.cantidadBloqueada.coerceAtLeast(0.0)
    }

    fun stockVendibleProducto(producto: MoldeProductos): Double {
        if (producto.lotes.isEmpty()) return producto.cantidadinicial.toDoubleOrNull() ?: 0.0
        return producto.lotes.values.sumOf { lote ->
            if (esLoteValidoParaConsumo(lote)) cantidadVendibleLote(lote) else 0.0
        }
    }

    fun stockFisicoProducto(producto: MoldeProductos): Double {
        if (producto.lotes.isEmpty()) return producto.cantidadinicial.toDoubleOrNull() ?: 0.0
        return producto.lotes.values.sumOf { cantidadFisicaLote(it) }
    }

    fun evaluarDisponibilidadVenta(producto: MoldeProductos): EstadoDisponibilidadVenta {
        val stockRegistrado = producto.cantidadinicial.toDoubleOrNull() ?: 0.0
        return evaluarDisponibilidadVenta(stockRegistrado, producto.lotes)
    }

    fun evaluarDisponibilidadVenta(
        stockRegistrado: Double,
        lotes: Map<String, LoteProducto>
    ): EstadoDisponibilidadVenta {
        if (lotes.isEmpty()) {
            val stock = stockRegistrado.coerceAtLeast(0.0).toInt()
            val motivo = if (stock > 0) {
                MotivoIndisponibilidadVenta.SIN_LOTES_REGISTRADOS
            } else {
                MotivoIndisponibilidadVenta.SIN_STOCK_REAL
            }
            return EstadoDisponibilidadVenta(
                stockFisico = stock,
                stockVendible = stock,
                tieneLotesRegistrados = false,
                lotesBloqueados = 0,
                lotesVencidos = 0,
                motivoIndisponibilidad = motivo
            )
        }

        val stockFisico = lotes.values.sumOf { cantidadFisicaLote(it) }.toInt()
        val stockVendible = lotes.values.sumOf { lote ->
            if (esLoteValidoParaConsumo(lote)) cantidadVendibleLote(lote) else 0.0
        }.toInt()
        val lotesBloqueados = lotes.values.count { estaLoteBloqueadoParaVenta(it) }
        val lotesVencidos = lotes.values.count { lote ->
            cantidadFisicaLote(lote) > 0.0 && lote.vencimiento.isNotBlank() && estaVencido(lote.vencimiento)
        }

        val motivo = when {
            stockFisico <= 0 -> MotivoIndisponibilidadVenta.SIN_STOCK_REAL
            stockVendible > 0 -> null
            lotesBloqueados > 0 && lotesVencidos > 0 ->
                MotivoIndisponibilidadVenta.LOTES_BLOQUEADOS_O_VENCIDOS
            lotesBloqueados > 0 -> MotivoIndisponibilidadVenta.LOTES_BLOQUEADOS
            lotesVencidos > 0 -> MotivoIndisponibilidadVenta.LOTES_VENCIDOS
            else -> MotivoIndisponibilidadVenta.SIN_STOCK_REAL
        }

        return EstadoDisponibilidadVenta(
            stockFisico = stockFisico,
            stockVendible = stockVendible,
            tieneLotesRegistrados = true,
            lotesBloqueados = lotesBloqueados,
            lotesVencidos = lotesVencidos,
            motivoIndisponibilidad = motivo
        )
    }

    fun construirMensajeVentaNoDisponible(
        nombreProducto: String?,
        cantidadSolicitada: Int,
        stockRegistrado: Double,
        lotes: Map<String, LoteProducto>,
        stockVendibleDisponible: Int? = null
    ): String {
        val nombreUi = nombreProducto?.trim().orEmpty().ifBlank { "este producto" }
        val disponibilidad = evaluarDisponibilidadVenta(stockRegistrado, lotes)
        val stockVendible = stockVendibleDisponible ?: disponibilidad.stockVendible

        if (disponibilidad.motivoIndisponibilidad == MotivoIndisponibilidadVenta.SIN_LOTES_REGISTRADOS) {
            return "El producto $nombreUi no tiene lotes registrados para venta. Revisa el inventario antes de venderlo."
        }

        if (stockVendible > 0 && cantidadSolicitada > stockVendible) {
            return "El stock vendible disponible de $nombreUi no alcanza para esa cantidad. Solo hay $stockVendible unidades vendibles."
        }

        return when (disponibilidad.motivoIndisponibilidad) {
            MotivoIndisponibilidadVenta.SIN_LOTES_REGISTRADOS ->
                "El producto $nombreUi no tiene lotes registrados para venta. Revisa el inventario antes de venderlo."
            MotivoIndisponibilidadVenta.LOTES_BLOQUEADOS ->
                "El producto $nombreUi no está disponible para venta porque sus lotes están bloqueados. Stock físico: ${disponibilidad.stockFisico}."
            MotivoIndisponibilidadVenta.LOTES_VENCIDOS ->
                "El producto $nombreUi no está disponible para venta porque sus lotes están vencidos. Stock físico: ${disponibilidad.stockFisico}."
            MotivoIndisponibilidadVenta.LOTES_BLOQUEADOS_O_VENCIDOS ->
                "El producto $nombreUi no está disponible para venta porque sus lotes están bloqueados o vencidos. Stock físico: ${disponibilidad.stockFisico}."
            else ->
                "El producto $nombreUi no tiene stock real disponible para vender."
        }
    }

    fun esLoteValidoParaConsumo(lote: LoteProducto): Boolean {
        if (cantidadVendibleLote(lote) <= 0.0) return false

        val vencimientoNormalizado = normalizarVencimiento(lote.vencimiento)
        return vencimientoNormalizado.isBlank() || !estaVencido(vencimientoNormalizado)
    }

    fun obtenerLotesRegistrados(producto: MoldeProductos): List<LoteProducto> {
        return producto.lotes.values.filter { it.numero.trim().isNotBlank() }
    }

    fun cantidadLotesRegistrados(producto: MoldeProductos): Int {
        return obtenerLotesRegistrados(producto).size
    }

    fun tieneMultiplesLotesRegistrados(producto: MoldeProductos): Boolean {
        return cantidadLotesRegistrados(producto) > 1
    }

    fun obtenerVencimientoGeneralVisible(producto: MoldeProductos): String {
        val lotes = obtenerLotesRegistrados(producto)
        val vencimiento = when {
            lotes.size > 1 -> ""
            lotes.size == 1 -> lotes.first().vencimiento.ifBlank { producto.vencimiento }
            else -> producto.vencimiento
        }
        return normalizarVencimiento(vencimiento)
    }

    fun obtenerVencimientosParaEvaluacion(producto: MoldeProductos): List<String> {
        val lotes = obtenerLotesRegistrados(producto)
        if (lotes.size > 1) {
            val lotesConVencimiento = lotes.filter { it.vencimiento.isNotBlank() }
            val candidatos = lotesConVencimiento.filter { it.cantidad > 0 }.ifEmpty { lotesConVencimiento }
            return candidatos.map { normalizarVencimiento(it.vencimiento) }
        }

        val vencimientoGeneral = obtenerVencimientoGeneralVisible(producto)
        return if (vencimientoGeneral.isNotBlank()) listOf(vencimientoGeneral) else emptyList()
    }

    fun obtenerLoteConVencimientoMasProximo(producto: MoldeProductos): LoteProducto? {
        val lotesConVencimiento = obtenerLotesRegistrados(producto)
            .filter { it.vencimiento.isNotBlank() }
        if (lotesConVencimiento.isEmpty()) return null

        val candidatos = lotesConVencimiento.filter { esLoteValidoParaConsumo(it) }
            .ifEmpty { lotesConVencimiento.filter { cantidadVendibleLote(it) > 0 } }
            .ifEmpty { lotesConVencimiento }
        return candidatos.minByOrNull { diasHastaVencerLote(it.vencimiento) ?: Int.MAX_VALUE }
    }

    fun obtenerEstadoVencimiento(producto: MoldeProductos): String? {
        val diasDisponibles = obtenerVencimientosParaEvaluacion(producto)
            .mapNotNull { diasHastaVencerLote(it) }
        if (diasDisponibles.isEmpty()) return null

        return when {
            diasDisponibles.any { it < 0 } -> "VENCIDO"
            diasDisponibles.any { it <= 30 } -> "POR_VENCER"
            else -> "OK"
        }
    }

    /**
     * Formatea un valor numérico como moneda usando MonedaHelper.
     */
    fun formatearPrecio(precio: Any?): String {
        return MonedaHelper.formatear(precio)
    }

    /**
     * Calcula los días restantes hasta el vencimiento de un lote.
     * Formato esperado: MM/AA (ej. 02/26)
     * 
     * @param vencimiento String en formato MM/AA
     * @return Días restantes (positivo = vigente, negativo = vencido) o null si el formato es inválido.
     */
    fun diasHastaVencerLote(vencimiento: String): Int? {
        if (vencimiento.isBlank()) return null
        return try {
            val partes = normalizarVencimiento(vencimiento).split("/")
            if (partes.size != 2) return null

            val mes = partes[0].toInt()
            val anioCorto = partes[1].toInt()
            val anio = 2000 + anioCorto

            // Obtener el último día de ese mes/año usando java.time (API moderna)
            val fechaVencimiento = YearMonth.of(anio, mes).atEndOfMonth()
            val hoy = LocalDate.now()

            // Calcular diferencia exacta en días
            ChronoUnit.DAYS.between(hoy, fechaVencimiento).toInt()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calcula la diferencia en meses entre hoy y la fecha de vencimiento.
     * Útil para alertas de proximidad (ej. vence en menos de 3 meses).
     */
    fun mesesHastaVencer(vencimiento: String): Long? {
        if (vencimiento.length != 5) return null
        return try {
            val partes = normalizarVencimiento(vencimiento).split("/")
            val mes = partes[0].toInt()
            val anio = 2000 + partes[1].toInt()

            val fechaVencimiento = YearMonth.of(anio, mes).atEndOfMonth()
            val hoy = LocalDate.now()

            ChronoUnit.MONTHS.between(
                YearMonth.from(hoy),
                YearMonth.from(fechaVencimiento)
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Determina si una fecha MM/AA ya ha pasado respecto al día de hoy.
     */
    fun estaVencido(vencimiento: String): Boolean {
        val partes = normalizarVencimiento(vencimiento).split("/")
        if (partes.size != 2) return false
        return try {
            val mes = partes[0].toInt()
            val anio = 2000 + partes[1].toInt()
            val expiryYearMonth = YearMonth.of(anio, mes)
            val currentYearMonth = YearMonth.now()
            expiryYearMonth.isBefore(currentYearMonth)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Normalización exacta que usa CrearProducto para el campo 'normalizedName'.
     * Mantiene compatibilidad con el contrato de datos del sistema.
     */
    fun normalizarNombreContrato(name: String): String {
        return name
            .trim()
            .lowercase(Locale.getDefault())
            .replace(Regex("\\s+"), " ")
    }

    /**
     * Generación de clave exacta que usa CrearProducto para el nodo NombresProductos.
     */
    fun generarKeyNombreContrato(normalized: String): String {
        val sinAcentos = Normalizer
            .normalize(normalized, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")

        return sinAcentos
            .trim()
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9_-]"), "_")
            .replace(Regex("_+"), "_")
            .trim('_', '-')
            .ifBlank { "ref_${System.currentTimeMillis()}" }
            .take(120)
    }

    /**
     * Sanitiza un nombre para ser usado como ID de búsqueda o nombre de archivo.
     * Elimina acentos, caracteres especiales y espacios.
     */
    fun sanitizarTexto(texto: String): String {
        val sinAcentos = Normalizer
            .normalize(texto, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")

        val limpio = sinAcentos
            .trim()
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9_-]"), "_")
            .replace(Regex("_+"), "_")
            .trim('_', '-')

        return limpio
            .ifBlank { "ref_${System.currentTimeMillis()}" }
            .take(120)
    }

    fun normalizarTextoBusqueda(texto: String): String {
        val sinAcentos = Normalizer
            .normalize(texto, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")

        return sinAcentos
            .trim()
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun normalizarClaveCategoria(texto: String): String {
        return normalizarTextoBusqueda(texto)
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { simplificarPluralCategoria(it) }
            .trim()
    }

    fun resolverCategoriaExistente(entrada: String, categorias: List<String>): String? {
        val claveEntrada = normalizarClaveCategoria(entrada)
        if (claveEntrada.isBlank()) return null

        return categorias
            .map { it.trim() }
            .firstOrNull { categoria ->
                categoria.isNotBlank() && normalizarClaveCategoria(categoria) == claveEntrada
            }
    }

    fun sugerirCategoriasSimilares(
        consulta: String,
        categorias: List<String>,
        limite: Int = 6
    ): List<String> {
        val consultaNormalizada = normalizarTextoBusqueda(consulta)
        val consultaClave = normalizarClaveCategoria(consulta)
        val categoriasCanonicas = linkedMapOf<String, String>()

        categorias.forEach { categoria ->
            val nombre = categoria.trim()
            if (nombre.isBlank()) return@forEach
            val clave = normalizarClaveCategoria(nombre)
            if (clave.isBlank()) return@forEach
            categoriasCanonicas.putIfAbsent(clave, nombre)
        }

        if (categoriasCanonicas.isEmpty()) return emptyList()

        if (consultaNormalizada.isBlank()) {
            return categoriasCanonicas.values
                .sortedBy { normalizarTextoBusqueda(it) }
                .take(limite)
        }

        val longitudConsulta = consultaClave.replace(" ", "").length
        if (longitudConsulta < 3) return emptyList()

        return categoriasCanonicas.values
            .mapNotNull { categoria ->
                val categoriaNormalizada = normalizarTextoBusqueda(categoria)
                val categoriaClave = normalizarClaveCategoria(categoria)
                val puntaje = puntuarCoincidenciaCategoria(
                    consultaNormalizada = consultaNormalizada,
                    consultaClave = consultaClave,
                    categoriaNormalizada = categoriaNormalizada,
                    categoriaClave = categoriaClave
                )

                if (puntaje > 0) puntaje to categoria else null
            }
            .sortedWith(
                compareByDescending<Pair<Int, String>> { it.first }
                    .thenBy { normalizarTextoBusqueda(it.second).length }
                    .thenBy { normalizarTextoBusqueda(it.second) }
            )
            .map { it.second }
            .take(limite)
    }

    fun encontrarRangoSugerenciaCategoria(textoOriginal: String, consulta: String): IntRange? {
        val texto = textoOriginal.trim()
        val consultaNormalizada = normalizarTextoBusqueda(consulta)
        if (texto.isBlank() || consultaNormalizada.length < 3) return null

        encontrarSubcadenaNormalizada(texto, consultaNormalizada)?.let { return it }

        val tokensConsulta = normalizarClaveCategoria(consulta)
            .split(" ")
            .filter { it.length >= 3 }

        tokensConsulta.forEach { token ->
            encontrarSubcadenaNormalizada(texto, token)?.let { return it }
        }

        val tokensObjetivo = tokensConsulta.ifEmpty { listOf(consultaNormalizada) }
        val regexPalabra = Regex("[\\p{L}0-9][\\p{L}\\p{M}0-9]*")
        var mejorRango: IntRange? = null
        var mejorSimilitud = 0.0

        regexPalabra.findAll(texto).forEach { coincidencia ->
            val palabraNormalizada = normalizarClaveCategoria(coincidencia.value)
            if (palabraNormalizada.isBlank()) return@forEach

            tokensObjetivo.forEach { tokenConsulta ->
                val similitud = calcularSimilitudNormalizadaInterna(
                    palabraNormalizada,
                    simplificarPluralCategoria(tokenConsulta)
                )
                if (similitud >= 0.72 && similitud > mejorSimilitud) {
                    mejorSimilitud = similitud
                    mejorRango = coincidencia.range
                }
            }
        }

        return mejorRango
    }

    /**
     * Calcula la distancia de Levenshtein entre dos cadenas para medir su similitud.
     */
    fun calcularDistanciaLevenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j

        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[a.length][b.length]
    }

    /**
     * Determina si dos nombres son similares basándose en un umbral (0.0 a 1.0).
     */
    fun sonSimilares(a: String, b: String, umbral: Double = 0.7): Boolean {
        val aNorm = a.lowercase().trim()
        val bNorm = b.lowercase().trim()
        if (aNorm == bNorm) return true
        
        val distancia = calcularDistanciaLevenshtein(aNorm, bNorm)
        val maxLen = maxOf(aNorm.length, bNorm.length)
        if (maxLen == 0) return true
        
        val similitud = 1.0 - (distancia.toDouble() / maxLen)
        return similitud >= umbral
    }

    private fun simplificarPluralCategoria(token: String): String {
        val limpio = token.trim()
        return when {
            limpio.length <= 4 -> limpio
            limpio.endsWith("is") -> limpio
            limpio.endsWith("s") -> limpio.dropLast(1)
            else -> limpio
        }
    }

    private fun calcularSimilitudNormalizada(a: String, b: String): Double {
        return calcularSimilitudNormalizadaInterna(a, b)
    }

    private fun calcularSimilitudNormalizadaInterna(a: String, b: String): Double {
        if (a.isBlank() || b.isBlank()) return 0.0
        val distancia = calcularDistanciaLevenshtein(a, b)
        val maxLen = maxOf(a.length, b.length)
        if (maxLen == 0) return 1.0
        return 1.0 - (distancia.toDouble() / maxLen)
    }

    private fun encontrarSubcadenaNormalizada(
        textoOriginal: String,
        consultaNormalizada: String
    ): IntRange? {
        if (consultaNormalizada.isBlank()) return null

        val textoMapeado = normalizarTextoConMapa(textoOriginal)
        val indiceInicio = textoMapeado.texto.indexOf(consultaNormalizada)
        if (indiceInicio < 0) return null

        val indiceFin = indiceInicio + consultaNormalizada.length - 1
        if (indiceFin >= textoMapeado.mapaIndicesOriginales.size) return null

        val inicioOriginal = textoMapeado.mapaIndicesOriginales[indiceInicio]
        val finOriginal = textoMapeado.mapaIndicesOriginales[indiceFin]
        return inicioOriginal..finOriginal
    }

    private fun normalizarTextoConMapa(texto: String): TextoNormalizadoConMapa {
        val normalizado = StringBuilder()
        val mapa = mutableListOf<Int>()
        var ultimoFueEspacio = true

        texto.forEachIndexed { indiceOriginal, charOriginal ->
            val descompuesto = Normalizer.normalize(charOriginal.toString(), Normalizer.Form.NFD)
            val sinAcentos = descompuesto.replace(Regex("\\p{M}+"), "")

            sinAcentos.forEach { charNormalizado ->
                val caracter = charNormalizado.lowercaseChar()
                when {
                    caracter.isLetterOrDigit() -> {
                        normalizado.append(caracter)
                        mapa.add(indiceOriginal)
                        ultimoFueEspacio = false
                    }

                    !ultimoFueEspacio -> {
                        normalizado.append(' ')
                        mapa.add(indiceOriginal)
                        ultimoFueEspacio = true
                    }
                }
            }
        }

        if (normalizado.isNotEmpty() && normalizado.last() == ' ') {
            normalizado.deleteCharAt(normalizado.length - 1)
            mapa.removeAt(mapa.lastIndex)
        }

        return TextoNormalizadoConMapa(
            texto = normalizado.toString(),
            mapaIndicesOriginales = mapa.toIntArray()
        )
    }

    private fun puntuarCoincidenciaCategoria(
        consultaNormalizada: String,
        consultaClave: String,
        categoriaNormalizada: String,
        categoriaClave: String
    ): Int {
        if (consultaClave.isBlank()) return 100
        if (categoriaClave == consultaClave) return 1100
        if (categoriaNormalizada == consultaNormalizada) return 1080

        if (categoriaClave.startsWith(consultaClave) || categoriaNormalizada.startsWith(consultaNormalizada)) {
            return 950 - (categoriaClave.length - consultaClave.length).coerceAtLeast(0)
        }

        val tokensCategoria = categoriaClave.split(" ").filter { it.isNotBlank() }
        val tokensConsulta = consultaClave.split(" ").filter { it.isNotBlank() }
        if (tokensConsulta.isNotEmpty() && tokensConsulta.all { tokenConsulta ->
                tokensCategoria.any { it.startsWith(tokenConsulta) }
            }
        ) {
            return 880
        }

        val indiceContenido = when {
            categoriaClave.contains(consultaClave) -> categoriaClave.indexOf(consultaClave)
            categoriaNormalizada.contains(consultaNormalizada) -> categoriaNormalizada.indexOf(consultaNormalizada)
            else -> -1
        }
        if (indiceContenido >= 0) {
            return 780 - indiceContenido
        }

        if (consultaClave.length < 3) return 0

        val similitudCategoria = calcularSimilitudNormalizada(categoriaClave, consultaClave)
        if (similitudCategoria >= UMBRAL_SUGERENCIA_CATEGORIA) {
            return (600 + similitudCategoria * 100).toInt()
        }

        val mejorSimilitudToken = tokensCategoria
            .map { calcularSimilitudNormalizada(it, consultaClave) }
            .maxOrNull()
            ?: 0.0

        if (mejorSimilitudToken >= 0.72) {
            return (560 + mejorSimilitudToken * 100).toInt()
        }

        return 0
    }

    fun formatearStockVisible(producto: MoldeProductos): String {
        val info = obtenerInfoStockEstructurada(producto)
        return if (info.detalle.isNotBlank()) {
            "${info.principal} de ${info.detalle}"
        } else {
            info.principal
        }
    }

    data class InfoStockEstructurada(
        val principal: String,
        val detalle: String
    )

    fun obtenerInfoStockEstructurada(producto: MoldeProductos): InfoStockEstructurada {
        val stockTotal = producto.cantidadinicial.trim().toIntOrNull()?.coerceAtLeast(0) ?: 0
        val unidadBase = producto.unidadbase.trim().ifBlank { "unidades" }
        val equivalenciaPrincipal = obtenerEquivalenciaPresentacionPrincipal(producto)
        val nombrePresentacion = producto.presentacionprincipal.trim()

        // Caso base: unidad simple o sin equivalencia util para representar el stock por contenedores
        if (
            equivalenciaPrincipal <= 1 ||
            nombrePresentacion.isBlank() ||
            !esUnidadContable(unidadBase)
        ) {
            return InfoStockEstructurada(
                principal = "$stockTotal ${formatearUnidadCantidad(unidadBase, stockTotal)}",
                detalle = ""
            )
        }

        val presentacionesCompletas = stockTotal / equivalenciaPrincipal
        val unidadesSueltas = stockTotal % equivalenciaPrincipal

        if (presentacionesCompletas <= 0 && stockTotal > 0) {
            return InfoStockEstructurada(
                principal = "$stockTotal ${formatearUnidadCantidad(unidadBase, stockTotal)}",
                detalle = ""
            )
        }

        val nombrePresentacionUi = nombrePresentacion.lowercase(Locale.getDefault())
        val principal = formatearCantidadTexto(
            cantidad = presentacionesCompletas,
            singular = nombrePresentacionUi,
            plural = pluralizarTexto(nombrePresentacionUi)
        )

        val detalle = if (unidadesSueltas > 0) {
            "$unidadesSueltas ${formatearUnidadCantidad(unidadBase, unidadesSueltas)}"
        } else {
            ""
        }

        return InfoStockEstructurada(principal, detalle)
    }

    private fun obtenerEquivalenciaPresentacionPrincipal(producto: MoldeProductos): Int {
        val principal = producto.presentacionprincipal.trim()
        if (principal.isBlank()) return producto.unidadesPorPresentacionCompra.coerceAtLeast(0)

        val presentacionPrincipal = producto.presentaciones.firstOrNull { presentacion ->
            PresentacionHelper.sonNombresEquivalentes(presentacion.nombre, principal)
        }

        return presentacionPrincipal?.cantidad?.takeIf { it > 0 }
            ?: producto.unidadesPorPresentacionCompra.coerceAtLeast(0)
    }

    private fun esUnidadContable(unidadBase: String): Boolean {
        return when (unidadBase.trim().lowercase(Locale.getDefault())) {
            "", "unidad", "unidades", "und", "uds" -> true
            else -> false
        }
    }

    private fun formatearUnidadCantidad(unidadBase: String, cantidad: Int): String {
        return when (unidadBase.trim().lowercase(Locale.getDefault())) {
            "", "unidad", "unidades", "und", "uds" -> if (cantidad == 1) "unidad" else "unidades"
            else -> unidadBase
        }
    }

    private fun formatearCantidadTexto(cantidad: Int, singular: String, plural: String): String {
        return if (cantidad == 1) "$cantidad $singular" else "$cantidad $plural"
    }

    private fun pluralizarTexto(texto: String): String {
        val limpio = texto.trim()
        if (limpio.isBlank()) return ""
        return when {
            limpio.endsWith("s", ignoreCase = true) -> limpio
            limpio.endsWith("z", ignoreCase = true) -> limpio.dropLast(1) + "ces"
            else -> "${limpio}s"
        }
    }

    /**
     * V29.0: Utilidades de conversión de imagen para auditoría visual.
     */
    fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        if (angle == 0f) return source
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // Redimensionar para ahorrar espacio en Realtime Database (Nivel Auditoría)
        val scaled = if (bitmap.width > 800 || bitmap.height > 800) {
            val scale = 800f / Math.max(bitmap.width, bitmap.height)
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
        } else bitmap
        
        scaled.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    fun base64ToBitmap(base64: String?): Bitmap? {
        if (base64 == null) return null
        return try {
            val decodedString = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * V30.8: Abre un archivo Base64 usando el visor nativo del dispositivo.
     * Crea un archivo temporal seguro y lanza un Intent con el MIME type adecuado.
     */
    fun openBase64FileNatively(context: Context, base64: String, fileName: String = "factura_respaldo") {
        try {
            val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
            
            // Determinar extensión y MIME type
            // Truco: ML Kit/Cámara siempre guardan JPEG, el selector de archivos puede traer PDF
            val isPdf = base64.take(10).contains("JVBER") // Header típico de PDF en Base64
            val extension = if (isPdf) ".pdf" else ".jpg"
            val mimeType = if (isPdf) "application/pdf" else "image/jpeg"

            // Crear archivo temporal en el cache de la app
            val tempFile = File(context.cacheDir, "$fileName$extension")
            FileOutputStream(tempFile).use { it.write(decodedBytes) }

            // Generar URI segura vía FileProvider
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider", // V30.8: Corregido para coincidir con AndroidManifest.xml
                tempFile
            )

            // Lanzar Intent de visualización
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            context.startActivity(Intent.createChooser(intent, "Abrir con..."))
        } catch (e: Exception) {
            android.util.Log.e("ProductUtils", "Error abriendo archivo nativo", e)
        }
    }

    private fun fixCommonOcrLotErrors(input: String): String {
        return input
            .replace("O", "0")
            .replace("Q", "0")
            .replace("I", "1")
            .replace("L", "1")
            .replace("Z", "2")
            .replace("S", "5")
            .replace("B", "8")
            .replace("G", "6")
    }

    /**
     * V30.0: Lógica de extracción de Lote y Vencimiento movida desde LiveOcrScannerOverlay
     * para permitir uso compartido con reconocimiento local (ML Kit).
     */
    fun extractLoteAndVenc(text: String): Pair<String?, String?> {
        val lines = text.lines()
            .map { normalizeOcrLine(it) }
            .filter { it.isNotBlank() }

        var lote: String? = null
        var venc: String? = null

        val lotPrefixes = listOf(
            "LOTE",
            "LOT",
            "LT",
            "L:",
            "L.",
            "BATCH",
            "B/",
            "BN",
            "SERIE",
            "SER"
        )

        val expPrefixes = listOf(
            "EXP",
            "VENC",
            "VENCE",
            "VTO",
            "CAD",
            "FV",
            "F.V",
            "F.V.",
            "FC",
            "F.C",
            "PV",
            "P.V",
            "P.V.",
            "BB",
            "BBD",
            "BEST BY"
        )

        fun cleanLotCandidate(raw: String): String? {
            val tokens = raw
                .replace(Regex("[^A-Z0-9]+"), " ")
                .trim()
                .split(Regex("\\s+"))
                .filter { it.isNotBlank() }
                .filterNot { it in lotPrefixes }
                .filterNot { it in expPrefixes }
                .filterNot { normalizarVencimiento(it).isNotBlank() }

            if (tokens.isEmpty()) return null

            val candidate = fixCommonOcrLotErrors(
                tokens
                    .take(2)
                    .joinToString("")
                    .replace(Regex("[^A-Z0-9]"), "")
            )

            return candidate.takeIf { isValidLotCandidate(it) }
        }

        val lotPrefixRegex = lotPrefixes
            .sortedByDescending { it.length }
            .joinToString("|") { Pattern.quote(it) }

        val expPrefixRegex = expPrefixes
            .sortedByDescending { it.length }
            .joinToString("|") { Pattern.quote(it) }

        val lotPattern = Pattern.compile(
            "(?:^|\\b)(?:$lotPrefixRegex)\\s*[:#.-]?\\s*([A-Z0-9][A-Z0-9\\s\\-]{2,30})"
        )

        val datePattern =
            "(\\d{1,2}[/\\-\\s]\\d{1,2}[/\\-\\s]\\d{2,4}|" +
                    "\\d{1,2}[/\\-\\s]\\d{2,4}|" +
                    "\\d{6,8}|" +
                    "\\d{4}[/\\-\\s]\\d{2})"

        val expPattern = Pattern.compile(
            "(?:^|\\b)(?:$expPrefixRegex)\\s*[:#.-]?\\s*$datePattern"
        )

        for (line in lines) {
            if (lote == null) {
                val matcher = lotPattern.matcher(line)
                if (matcher.find()) {
                    lote = cleanLotCandidate(matcher.group(1).orEmpty())
                }
            }

            if (venc == null) {
                val matcher = expPattern.matcher(line)
                if (matcher.find()) {
                    val normalized = normalizarVencimiento(matcher.group(1).orEmpty())
                    if (normalized.isNotBlank()) {
                        venc = normalized
                    }
                }
            }
        }

        // Fallback: si no encontró lote con prefijo,
        // busca una línea que parezca lote y evita fabricación/vencimiento.
        if (lote == null) {
            for (line in lines) {
                val isExpirationLine = expPrefixes.any { prefix ->
                    line.startsWith(prefix) ||
                            line.contains("$prefix:") ||
                            line.contains("$prefix ") ||
                            line.contains("$prefix.")
                }

                val isManufactureLine =
                    line.startsWith("FP") ||
                            line.startsWith("FAB") ||
                            line.startsWith("MFG") ||
                            line.startsWith("MFD") ||
                            line.startsWith("ELAB") ||
                            line.contains("FABRIC")

                if (!isExpirationLine && !isManufactureLine) {
                    val candidate = cleanLotCandidate(line)
                    if (!candidate.isNullOrBlank()) {
                        lote = candidate
                        break
                    }
                }
            }
        }

        // Fallback: buscar fecha aunque no venga con prefijo EXP/VENC/PV.
        if (venc == null) {
            val looseDatePattern = Pattern.compile(
                "(\\d{1,2}[/\\-\\.\\s]\\d{1,2}[/\\-\\.\\s]\\d{2,4}|" +
                        "\\d{1,2}[/\\-\\.\\s]\\d{2,4})"
            )

            for (line in lines) {
                val isLotLine = lotPrefixes.any { prefix ->
                    line.startsWith(prefix) ||
                            line.contains("$prefix:") ||
                            line.contains("$prefix ") ||
                            line.contains("$prefix.")
                }

                val isManufactureLine =
                    line.startsWith("FP") ||
                            line.startsWith("FAB") ||
                            line.startsWith("MFG") ||
                            line.startsWith("MFD") ||
                            line.startsWith("ELAB") ||
                            line.contains("FABRIC")

                if (!isLotLine && !isManufactureLine) {
                    val matcher = looseDatePattern.matcher(line)

                    if (matcher.find()) {
                        val normalized = normalizarVencimiento(matcher.group(1).orEmpty())

                        if (normalized.isNotBlank()) {
                            venc = normalized
                            break
                        }
                    }
                }
            }
        }

        return Pair(lote, venc)
    }

    private fun normalizeOcrLine(line: String): String {
        return line.uppercase(Locale.ROOT)
            .replace("L T", "LT")
            .replace("L0T", "LOT")
            .replace("L0TE", "LOTE")
            .replace("IOTE", "LOTE")
            .replace("VENC.", "VENC")
            .replace("EXP.", "EXP")
            .replace("F V", "FV")
            .replace("P V", "PV")
            .replace(":", ":")
            .trim()
    }

    private fun isValidLotCandidate(candidate: String): Boolean {
        val clean = candidate
            .replace(Regex("[^A-Z0-9]"), "")
            .trim()

        if (clean.length < 4) return false
        if (clean.length > 30) return false

        // Debe tener al menos un número.
        if (!clean.any { it.isDigit() }) return false

        // No debe ser una fecha pura.
        if (normalizarVencimiento(clean).isNotBlank()) return false

        return true
    }

    private fun isValidMonthDate(date: String?): Boolean {
        if (date == null) return false
        val digits = date.filter { it.isDigit() }
        if (digits.length < 4) return false

        val parts = date.split(Regex("[/\\-\\s]+")).filter { it.isNotBlank() }

        return when {
            parts.size >= 3 -> {
                val d0 = parts[0].toIntOrNull() ?: 0
                val d1 = parts[1].toIntOrNull() ?: 0
                (d0 in 1..12) || (d1 in 1..12)
            }
            parts.size == 2 -> {
                val monthPart = if (parts[0].length == 4) parts[1] else parts[0]
                val month = monthPart.toIntOrNull() ?: 0
                month in 1..12
            }
            else -> {
                val month = digits.take(2).toIntOrNull() ?: 0
                month in 1..12
            }
        }
    }

    private fun normalizeDetectedDate(raw: String?): String? {
        if (raw == null) return null

        val clean = raw.uppercase()
            .replace(Regex("[/\\-\\s]+"), "/")
            .trim('/')

        val parts = clean.split("/").filter { it.isNotBlank() }

        if (parts.size >= 3) {
            val d0 = parts[0].toIntOrNull() ?: 0
            val d1 = parts[1].toIntOrNull() ?: 0
            val year = parts.last().takeLast(2)

            return when {
                d1 in 1..12 -> parts[1].padStart(2, '0') + "/" + year
                d0 in 1..12 -> parts[0].padStart(2, '0') + "/" + year
                else -> null
            }
        }

        if (!isValidMonthDate(clean)) return null
        val digits = clean.filter { it.isDigit() }
        return when {
            clean.matches(Regex("\\d{4}/\\d{2}")) -> clean.takeLast(2) + "/" + clean.substring(2, 4)
            clean.matches(Regex("\\d{2}/\\d{4}")) -> clean.take(2) + "/" + clean.takeLast(2)
            clean.matches(Regex("\\d{2}/\\d{2}")) -> clean
            digits.length == 4 -> {
                val m = digits.take(2).toIntOrNull() ?: 0
                if (m in 1..12) digits.take(2) + "/" + digits.takeLast(2) else null
            }
            else -> null
        }
    }


    fun extractLoteAndVencSmart(text: String): LoteOcrResult {
        val (lote, vencimiento) = extractLoteAndVenc(text)

        val confianzaLote = when {
            lote.isNullOrBlank() -> 0
            lote.length >= 6 && lote.any { it.isDigit() } -> 90
            lote.length >= 4 && lote.any { it.isDigit() } -> 75
            else -> 45
        }

        val confianzaVencimiento = when {
            vencimiento.isNullOrBlank() -> 0
            vencimiento.matches(Regex("\\d{2}/\\d{2}")) -> 90
            else -> 55
        }

        return LoteOcrResult(
            lote = lote,
            vencimiento = vencimiento,
            confianzaLote = confianzaLote,
            confianzaVencimiento = confianzaVencimiento,
            textoOcr = text
        )
    }

}
