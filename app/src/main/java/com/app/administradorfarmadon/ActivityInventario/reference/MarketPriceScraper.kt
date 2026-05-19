package com.app.administradorfarmadon.ActivityInventario.reference

import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

/**
 * Encargado de consultar fuentes web publicas para obtener precios de mercado.
 * Nota: esta implementacion es liviana y busca extraer datos basicos de HTML/Scripts.
 */
object MarketPriceScraper {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    /**
     * Realiza busqueda en fuentes peruanas.
     */
    suspend fun searchPeru(query: String): List<MarketSourceItem> {
        val results = mutableListOf<MarketSourceItem>()

        runCatching { fetchInkafarma(query)?.let(results::addAll) }
        runCatching { fetchMifarma(query)?.let(results::addAll) }

        return results
    }

    private fun fetchInkafarma(query: String): List<MarketSourceItem>? {
        val url = buildSearchUrl(
            baseUrl = "https://inkafarma.pe/buscador",
            queryParam = "keyword",
            query = query
        )
        return scrapeGeneric(url, "Inkafarma", query)
    }

    private fun fetchMifarma(query: String): List<MarketSourceItem>? {
        val url = buildSearchUrl(
            baseUrl = "https://www.mifarma.com.pe/buscador",
            queryParam = "keyword",
            query = query
        )
        return scrapeGeneric(url, "Mifarma", query)
    }

    private fun buildSearchUrl(baseUrl: String, queryParam: String, query: String): String {
        val encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8.toString())
        return "$baseUrl?$queryParam=$encoded"
    }

    /**
     * Intenta extraer informacion de una pagina de busqueda generica.
     * Dado que estas webs suelen ser SPAs, buscaremos bloques de datos en JSON-LD o scripts.
     */
    private fun scrapeGeneric(url: String, storeName: String, query: String): List<MarketSourceItem>? {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .build()

        val response = runCatching { client.newCall(request).execute() }.getOrNull()
        if (response == null || !response.isSuccessful) return null

        val html = response.body?.string() ?: return null
        val doc = Jsoup.parse(html)
        val items = mutableListOf<MarketSourceItem>()

        val scripts = doc.select("script[type=application/ld+json]")
        for (script in scripts) {
            val content = script.data()
            if (content.contains("\"@type\":\"Product\"") || content.contains("\"ItemList\"")) {
                extractItemsFromJson(content, storeName, url, items, query)
            }
        }

        if (items.isEmpty()) {
            val queryWords = tokenizeQuery(query)
            val productNodes = doc.select(".product-item, .vtex-product-summary-2-x-container")
            for (node in productNodes) {
                val name = node.select(".product-name, .vtex-product-summary-2-x-productBrand").text()
                val priceText = node.select(".price, .vtex-product-price-1-x-currencyInteger").text()
                val price = priceText.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
                if (name.isNotBlank() && price > 0) {
                    val confidence = calculateNameConfidence(name, queryWords)
                    if (confidence > 30) {
                        items.add(MarketSourceItem(storeName, name, "", price, url, confidence))
                    }
                }
            }
        }

        return items.take(10)
    }

    private fun extractItemsFromJson(
        json: String,
        storeName: String,
        baseUrl: String,
        list: MutableList<MarketSourceItem>,
        query: String
    ) {
        val queryWords = tokenizeQuery(query)

        fun extractPriceFromOffers(offers: Any?): Double {
            return when (offers) {
                is org.json.JSONObject -> offers.optDouble("price", 0.0)
                is org.json.JSONArray -> {
                    var price = 0.0
                    for (i in 0 until offers.length()) {
                        val candidate = offers.optJSONObject(i)?.optDouble("price", 0.0) ?: 0.0
                        if (candidate > 0.0) {
                            price = candidate
                            break
                        }
                    }
                    price
                }
                else -> 0.0
            }
        }

        fun visit(element: Any?) {
            when (element) {
                is org.json.JSONObject -> {
                    val type = element.optString("@type", "")
                    val name = element.optString("name", "")
                    var price = element.optDouble("price", 0.0)

                    if (price <= 0.0 && element.has("offers")) {
                        price = extractPriceFromOffers(element.opt("offers"))
                    }

                    val looksLikeProduct =
                        type.equals("Product", ignoreCase = true) ||
                            (name.isNotBlank() && price > 0.0)

                    if (looksLikeProduct && name.isNotBlank() && price > 0.0) {
                        val confidence = calculateNameConfidence(name, queryWords)
                        if (confidence > 30) {
                            list.add(MarketSourceItem(storeName, name, "", price, baseUrl, confidence))
                        }
                    }

                    val keys = element.keys()
                    while (keys.hasNext()) {
                        visit(element.opt(keys.next()))
                    }
                }

                is org.json.JSONArray -> {
                    for (i in 0 until element.length()) {
                        visit(element.opt(i))
                    }
                }
            }
        }

        runCatching {
            val trimmed = json.trim()
            when {
                trimmed.startsWith("{") -> visit(org.json.JSONObject(trimmed))
                trimmed.startsWith("[") -> visit(org.json.JSONArray(trimmed))
            }
        }
    }

    private fun tokenizeQuery(query: String): Set<String> {
        return query.lowercase()
            .split(Regex("\\s+"))
            .map { it.trim() }
            .filter { it.length > 2 }
            .toSet()
    }

    private fun calculateNameConfidence(name: String, queryWords: Set<String>): Int {
        if (queryWords.isEmpty()) return 100
        val nameWords = name.lowercase().split(Regex("\\s+")).map { it.trim() }.toSet()
        val matches = queryWords.count { nameWords.contains(it) }
        return matches * 100 / queryWords.size
    }
}
