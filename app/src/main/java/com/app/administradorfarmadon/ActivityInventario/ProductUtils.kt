package com.app.administradorfarmadon.ActivityInventario

import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.LoteProducto
import com.app.administradorfarmadon.ActivityInventario.ClasesProductos.MoldeProductos
import com.app.administradorfarmadon.ClasesDatabase.MonedaHelper
import java.text.Normalizer
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Objeto de utilidad para centralizar la lógica de negocio, validaciones y 
 * procesamiento de datos relacionados con los productos del inventario.
 */
object ProductUtils {

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

    fun normalizarVencimiento(vencimiento: String): String {
        return vencimiento.trim().replace("_", "/")
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
            val fechaVencimiento = YearMonth.of(anio, mes).atEndOfMonth()
            fechaVencimiento.isBefore(LocalDate.now())
        } catch (e: Exception) {
            false
        }
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
}
