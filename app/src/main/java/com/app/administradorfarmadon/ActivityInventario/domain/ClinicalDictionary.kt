package com.app.administradorfarmadon.ActivityInventario.domain

/**
 * Diccionario centralizado de sinónimos clínicos y populares (V4.9).
 * Compartido entre el motor de búsqueda y las reglas de UI para garantizar sincronía total.
 */
object ClinicalDictionary {
    
    val synonymGroups = listOf(
        setOf("amigdalitis", "garganta", "dolor de garganta", "infeccion de garganta", "garganta inflamada", "anginas", "amigdalas"),
        setOf("cefalea", "cabeza", "dolor de cabeza", "jaqueca", "migraña"),
        setOf("mialgia", "muscular", "dolor muscular", "cuerpo", "dolor de cuerpo", "espalda", "cuello", "articulaciones", "artritis", "reumatismo"),
        setOf("antipiretico", "fiebre", "calentura", "temperatura", "bajar fiebre", "pirético"),
        setOf("rinitis", "alergia", "estornudos", "congestion", "mocos", "picazon", "antihistaminico", "alergico", "ronchas"),
        setOf("antigripal", "gripe", "malestar", "resfriado", "catarro", "flu", "resfrio", "tos", "expectorante"),
        setOf("odontalgia", "muela", "dolor de muela", "diente", "dentadura", "encias"),
        setOf("analgesico", "dolor", "alivio", "calmante", "inflamacion", "antiinflamatorio"),
        setOf("gastritis", "estomago", "acidez", "ardor", "reflujo", "indigestion", "pesadez", "flatulencia", "gases"),
        setOf("diarrea", "estomago", "evacuacion", "laxante", "soltura", "estreñimiento", "constipacion")
    )

    /**
     * Busca sugerencias dinámicas basadas en una consulta parcial o compuesta (V4.10).
     * Soporta tokenización para sincronía con el motor de búsqueda.
     */
    fun findSuggestionsForQuery(query: String): List<String> {
        val q = query.lowercase().trim()
        if (q.length < 2) return emptyList()
        
        val tokens = q.split(" ").filter { it.length >= 3 }
        
        return synonymGroups.find { group ->
            // Un grupo se activa si el query completo coincide O si algún token relevante coincide
            group.any { it.contains(q) || q.contains(it) } || 
            tokens.any { token -> group.any { it.contains(token) || token.contains(it) } }
        }?.toList() ?: emptyList()
    }
}
