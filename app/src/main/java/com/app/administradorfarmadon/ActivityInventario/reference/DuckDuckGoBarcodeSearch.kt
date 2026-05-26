package com.app.administradorfarmadon.ActivityInventario.reference

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URLEncoder

data class DuckBarcodeSearchItem(
    val title: String = "",
    val snippet: String = "",
    val link: String = ""
)

class DuckDuckGoBarcodeSearch {

    private val memoryCache = mutableMapOf<String, List<DuckBarcodeSearchItem>>()

    suspend fun searchBarcode(
        barcode: String
    ): List<DuckBarcodeSearchItem> = withContext(Dispatchers.IO) {
        val cleanBarcode = barcode.trim()
        if (cleanBarcode.isBlank()) return@withContext emptyList()

        memoryCache[cleanBarcode]?.let {
            Log.d("BarcodeDuckSearch", "Caché de memoria hit: $cleanBarcode")
            return@withContext it
        }

        // Búsqueda neutral para evitar sesgos y ahorrar tiempo
        val queries = listOf(
            cleanBarcode,
            "\"$cleanBarcode\""
        )

        val results = mutableListOf<DuckBarcodeSearchItem>()

        for (query in queries) {
            val found = searchQuery(query, cleanBarcode)
            val cleanFound = found.filterNot { isGarbageResult(it) }

            Log.d("BarcodeDuckSearch", "Query: $query | Encontrados: ${cleanFound.size}")

            results.addAll(cleanFound)
            if (results.isNotEmpty()) break
        }

        // Reducimos a 4 resultados para que Gemini sea más rápido
        val finalResults = results
            .distinctBy { it.link.ifBlank { it.title } }
            .take(4)

        memoryCache[cleanBarcode] = finalResults
        return@withContext finalResults
    }

    private fun searchQuery(
        query: String,
        barcode: String
    ): List<DuckBarcodeSearchItem> {
        return try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "https://html.duckduckgo.com/html/?q=$encodedQuery"

            val document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36")
                .timeout(5_000) 
                .get()

            document.select(".result")
                .mapNotNull { result ->
                    val titleElement = result.select(".result__a").first() ?: result.select(".result__title a").first()
                    val title = titleElement?.text()?.trim().orEmpty()
                    val link = titleElement?.attr("href")?.trim().orEmpty()
                    val snippet = result.select(".result__snippet").text().trim()

                    if (title.isBlank() && snippet.isBlank()) return@mapNotNull null

                    DuckBarcodeSearchItem(title, snippet, link)
                }
                .filter { item ->
                    val text = "${item.title} ${item.snippet} ${item.link}".lowercase()
                    text.contains(barcode) || text.contains("sku") || text.contains("ean") || text.contains("producto")
                }
        } catch (e: Exception) {
            Log.e("BarcodeDuckSearch", "Error: ${e.message}")
            emptyList()
        }
    }

    private fun isGarbageResult(item: DuckBarcodeSearchItem): Boolean {
        val text = "${item.title} ${item.snippet} ${item.link}".lowercase()
        return listOf(
            "barcode generator", "barcode lookup", "barcode reader", "qr code", 
            "free barcode", "upc database", "ean database", "generador de código", 
            "lector de código", "barcode-list", "barcodelookup"
        ).any { text.contains(it) }
    }
}
