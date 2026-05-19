package com.app.administradorfarmadon.ActivitysPerfilItem


data class CountryInfo(
    val name: String,
    val currencyCode: String,
    val currencySymbol: String,
    val fiscalDocLabel: String,
    val defaultTaxName: String,
    val defaultTaxRate: Double,
    val fiscalDocOptions: List<String>,
    val statesAndCities: Map<String, List<String>>
)

object CountryCatalog {
    val countries = listOf(
        CountryInfo(
            name = "Perú",
            currencyCode = "PEN",
            currencySymbol = "S/",
            fiscalDocLabel = "RUC",
            defaultTaxName = "IGV",
            defaultTaxRate = 18.0,
            fiscalDocOptions = listOf("RUC", "DNI", "Carné de extranjería"),
            statesAndCities = mapOf(
                "Amazonas" to listOf("Chachapoyas", "Bagua", "Bongará"),
                "Áncash" to listOf("Huaraz", "Chimbote", "Caraz", "Casma"),
                "Apurímac" to listOf("Abancay", "Andahuaylas"),
                "Arequipa" to listOf("Arequipa", "Camaná", "Mollendo", "Majes"),
                "Ayacucho" to listOf("Ayacucho", "Huanta"),
                "Cajamarca" to listOf("Cajamarca", "Jaén", "Cutervo"),
                "Cusco" to listOf("Cusco", "Sicuani", "Quillabamba"),
                "Huancavelica" to listOf("Huancavelica", "Acobamba"),
                "Huánuco" to listOf("Huánuco", "Tingo María"),
                "Ica" to listOf("Ica", "Chincha Alta", "Pisco", "Nazca"),
                "Junín" to listOf("Huancayo", "Tarma", "Jauja", "La Merced", "Satipo"),
                "La Libertad" to listOf("Trujillo", "Huamachuco", "Pacasmayo", "Chepén", "Virú"),
                "Lambayeque" to listOf("Chiclayo", "Lambayeque", "Ferreñafe"),
                "Lima" to listOf("Lima Metropolitana", "Huacho", "Cañete", "Huaral", "Barranca"),
                "Loreto" to listOf("Iquitos", "Yurimaguas", "Nauta"),
                "Madre de Dios" to listOf("Puerto Maldonado"),
                "Moquegua" to listOf("Moquegua", "Ilo"),
                "Pasco" to listOf("Cerro de Pasco", "Oxapampa"),
                "Piura" to listOf("Piura", "Sullana", "Talara", "Paita", "Chulucanas"),
                "Puno" to listOf("Puno", "Juliaca", "Azángaro"),
                "San Martín" to listOf("Moyobamba", "Tarapoto", "Juanjuí", "Rioja"),
                "Tacna" to listOf("Tacna"),
                "Tumbes" to listOf("Tumbes", "Zarumilla"),
                "Ucayali" to listOf("Pucallpa")
            )
        ),
        CountryInfo(
            name = "Venezuela",
            currencyCode = "VES",
            currencySymbol = "Bs.",
            fiscalDocLabel = "RIF",
            defaultTaxName = "IVA",
            defaultTaxRate = 16.0,
            fiscalDocOptions = listOf("RIF", "Cédula"),
            statesAndCities = mapOf(
                "Amazonas" to listOf("Puerto Ayacucho"),
                "Anzoátegui" to listOf("Barcelona", "Puerto La Cruz", "El Tigre", "Anaco"),
                "Apure" to listOf("San Fernando de Apure", "Guasdualito"),
                "Aragua" to listOf("Maracay", "Turmero", "La Victoria", "Cagua"),
                "Barinas" to listOf("Barinas"),
                "Bolívar" to listOf("Ciudad Guayana", "Ciudad Bolívar", "Upata"),
                "Carabobo" to listOf("Valencia", "Puerto Cabello", "Guacara", "Naguanagua"),
                "Cojedes" to listOf("San Carlos"),
                "Delta Amacuro" to listOf("Tucupita"),
                "Distrito Capital" to listOf("Caracas"),
                "Falcón" to listOf("Coro", "Punto Fijo"),
                "Guárico" to listOf("San Juan de los Morros", "Valle de la Pascua", "Calabozo"),
                "Lara" to listOf("Barquisimeto", "Cabudare", "Carora", "El Tocuyo"),
                "Mérida" to listOf("Mérida", "El Vigía"),
                "Miranda" to listOf("Los Teques", "Guarenas", "Guatire", "Charallave", "Ocumare del Tuy"),
                "Monagas" to listOf("Matarín"),
                "Nueva Esparta" to listOf("La Asunción", "Porlamar"),
                "Portuguesa" to listOf("Guanare", "Acarigua"),
                "Sucre" to listOf("Cumaná", "Carúpano"),
                "Táchira" to listOf("San Cristóbal", "Táriba", "Rubio"),
                "Trujillo" to listOf("Trujillo", "Valera"),
                "Vargas" to listOf("La Guaira", "Catia La Mar"),
                "Yaracuy" to listOf("San Felipe"),
                "Zulia" to listOf("Maracaibo", "Cabimas", "Ciudad Ojeda", "San Francisco")
            )
        ),
        CountryInfo(
            name = "Colombia",
            currencyCode = "COP",
            currencySymbol = "$",
            fiscalDocLabel = "NIT",
            defaultTaxName = "IVA",
            defaultTaxRate = 19.0,
            fiscalDocOptions = listOf("NIT", "CC"),
            statesAndCities = mapOf(
                "Amazonas" to listOf("Leticia"),
                "Antioquia" to listOf("Medellín", "Bello", "Itagüí", "Envigado", "Apartadó", "Rionegro"),
                "Atlántico" to listOf("Barranquilla", "Soledad", "Malambo"),
                "Bogotá D.C." to listOf("Bogotá"),
                "Bolívar" to listOf("Cartagena", "Magangué"),
                "Boyacá" to listOf("Tunja", "Duitama", "Sogamoso"),
                "Caldas" to listOf("Manizales"),
                "Caquetá" to listOf("Florencia"),
                "Casanare" to listOf("Yopal"),
                "Cauca" to listOf("Popayán"),
                "Cesar" to listOf("Valledupar"),
                "Chocó" to listOf("Quibdó"),
                "Córdoba" to listOf("Montería"),
                "Cundinamarca" to listOf("Soacha", "Girardot", "Zipaquirá"),
                "Huila" to listOf("Neiva"),
                "La Guajira" to listOf("Riohacha", "Maicao"),
                "Magdalena" to listOf("Santa Marta"),
                "Meta" to listOf("Villavicencio"),
                "Nariño" to listOf("Pasto", "Ipiales", "Tumaco"),
                "Norte de Santander" to listOf("Cúcuta", "Ocaña"),
                "Quindío" to listOf("Armenia"),
                "Risaralda" to listOf("Pereira", "Dosquebradas"),
                "Santander" to listOf("Bucaramanga", "Floridablanca", "Barrancabermeja"),
                "Sucre" to listOf("Sincelejo"),
                "Tolima" to listOf("Ibagué"),
                "Valle del Cauca" to listOf("Cali", "Palmira", "Buenaventura", "Tuluá", "Cartago")
            )
        ),
        CountryInfo(
            name = "Ecuador",
            currencyCode = "USD",
            currencySymbol = "$",
            fiscalDocLabel = "RUC",
            defaultTaxName = "IVA",
            defaultTaxRate = 15.0,
            fiscalDocOptions = listOf("RUC", "Cédula"),
            statesAndCities = mapOf(
                "Azuay" to listOf("Cuenca", "Gualaceo"),
                "Bolívar" to listOf("Guaranda"),
                "Cañar" to listOf("Azogues"),
                "Carchi" to listOf("Tulcán"),
                "Chimborazo" to listOf("Riobamba"),
                "Cotopaxi" to listOf("Latacunga"),
                "El Oro" to listOf("Machala", "Pasaje", "Santa Rosa"),
                "Esmeraldas" to listOf("Esmeraldas"),
                "Galápagos" to listOf("Puerto Baquerizo Moreno"),
                "Guayas" to listOf("Guayaquil", "Durán", "Samborondón", "Milagro", "Daule"),
                "Imbabura" to listOf("Ibarra", "Otavalo"),
                "Loja" to listOf("Loja"),
                "Los Ríos" to listOf("Babahoyo", "Quevedo"),
                "Manabí" to listOf("Portoviejo", "Manta", "Chone"),
                "Morona Santiago" to listOf("Macas"),
                "Napo" to listOf("Tena"),
                "Orellana" to listOf("Puerto Francisco de Orellana"),
                "Pastaza" to listOf("Puyo"),
                "Pichincha" to listOf("Quito", "Sangolquí", "Machachi"),
                "Santa Elena" to listOf("Santa Elena", "La Libertad"),
                "Santo Domingo" to listOf("Santo Domingo"),
                "Sucumbíos" to listOf("Nueva Loja"),
                "Tungurahua" to listOf("Ambato"),
                "Zamora Chinchipe" to listOf("Zamora")
            )
        )
    )

    fun getCountry(name: String): CountryInfo? = countries.find { it.name == name }
    fun countryNames(): List<String> = countries.map { it.name }
    fun getStates(countryName: String): List<String> = getCountry(countryName)?.statesAndCities?.keys?.sorted() ?: emptyList()
    fun getCities(countryName: String, stateName: String): List<String> = getCountry(countryName)?.statesAndCities?.get(stateName)?.sorted() ?: emptyList()
}
