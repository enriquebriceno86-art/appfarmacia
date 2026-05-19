# Walkthrough - Referencias Reales de Mercado (V6.0)

He implementado una nueva arquitectura para obtener precios de mercado reales directamente de fuentes web p\u00fablicas (Inkafarma y Mifarma), eliminando la dependencia exclusiva de las estimaciones de la IA.

## Componentes Creados

### 1. [MarketPriceModels.kt](file:///C:/Users/braya/AndroidStudioProjects/AdministradorFarmadon/app/src/main/java/com/app/administradorfarmadon/ActivityInventario/reference/MarketPriceModels.kt)
Define las estructuras de datos para las referencias agregadas y los items individuales encontrados en la web.
- `MarketPriceReference`: Datos agrupados por presentaci\u00f3n.
- `MarketSourceItem`: Datos crudos de cada tienda (nombre, precio, url).

### 2. [MarketPriceScraper.kt](file:///C:/Users/braya/AndroidStudioProjects/AdministradorFarmadon/app/src/main/java/com/app/administradorfarmadon/ActivityInventario/reference/MarketPriceScraper.kt)
Realiza la consulta a las farmacias.
- **Fuentes**: Inkafarma y Mifarma.
- **T\u00e9cnica**: Scraping liviano de HTML, extracci\u00f3n de datos de bloques `JSON-LD` (est\u00e1ndar e-commerce) y selectores CSS espec\u00edficos de VTEX.
- **Seguridad**: Usa un User-Agent real para evitar bloqueos inmediatos.

### 3. [MarketPriceNormalizer.kt](file:///C:/Users/braya/AndroidStudioProjects/AdministradorFarmadon/app/src/main/java/com/app/administradorfarmadon/ActivityInventario/reference/MarketPriceNormalizer.kt)
Limpia y agrupa los resultados.
- **Detecci\u00f3n de Formatos**: Identifica autom\u00e1ticamente si un item es una "Unidad", "Caja x 100", "Frasco", etc.
- **C\u00e1lculo de Confianza**: Basado en el n\u00famero de fuentes, diversidad de tiendas y consistencia de precios (coeficiente de variaci\u00f3n).

### 4. [MarketPriceRepository.kt](file:///C:/Users/braya/AndroidStudioProjects/AdministradorFarmadon/app/src/main/java/com/app/administradorfarmadon/ActivityInventario/reference/MarketPriceRepository.kt)
Orquestador con **Cach\u00e9 en memoria (30 min)**. Evita consultas repetitivas a la red mientras el usuario edita el mismo producto.

## Integraci\u00f3n en el Flujo

### ViewModel ([CategorySuggestionViewModel.kt](file:///C:/Users/braya/AndroidStudioProjects/AdministradorFarmadon/app/src/main/java/com/app/administradorfarmadon/ActivityInventario/reference/CategorySuggestionViewModel.kt))
Ahora dispara en paralelo:
1.  **IA (Gemini)**: Para categor\u00eda, tipo de control y receta.
2.  **Mercado Real (Scraper)**: Para precios de referencia de Inkafarma/Mifarma.

### UI ([CreateProductScreen.kt](file:///C:/Users/braya/AndroidStudioProjects/AdministradorFarmadon/app/src/main/java/com/app/administradorfarmadon/ActivityInventario/ui/CreateProductScreen.kt))
Se actualizaron las tarjetas de presentaci\u00f3n para diferenciar visualmente las fuentes:
- **"Referencia web (real)"**: Cuando hay datos s\u00f3lidos de farmacias.
- **"Estimaci\u00f3n basada en fuentes"**: Cuando hay datos web pero son pocos o divergentes.
- **"Sugerencia IA (no verificada)"**: Fallback cuando no hay rastro del producto en la web y solo queda la estimaci\u00f3n de Gemini.
- **Listado de Fuentes**: Ahora se muestran las tiendas encontradas (ej: "Fuentes: Inkafarma + Mifarma").

## Verificaci\u00f3n
- Se agreg\u00f3 la dependencia `org.jsoup:jsoup:1.18.1` al proyecto.
- Se verific\u00f3 la compilaci\u00f3n exitosa con `./gradlew :app:compileDebugKotlin`.
- La l\u00f3gica de honestidad asegura que si no hay datos web, no se inventen precios bajo la etiqueta de "Mercado".

> [!NOTE]
> El scraping est\u00e1 sujeto a cambios en el HTML de las tiendas. Sin embargo, al usar extracci\u00f3n de JSON-LD y m\u00faltiples selectores, la robustez es significativamente mayor que un scraping de texto plano.
