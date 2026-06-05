# Comparación de precios via networking — ProWallet

Documento técnico que explica el flujo completo de la funcionalidad que consulta precios de referencia desde una API externa y los compara contra los precios registrados en una compra.

---

## 1. Las dos APIs del proyecto

El proyecto tiene **dos clientes HTTP independientes**, y es importante no confundirlos porque tienen roles distintos:

| | `RetrofitClient` | `PreciosClarosClient` |
|---|---|---|
| Archivo | `RetrofitClient.kt` | `PreciosClarosClient.kt` |
| URL base | `https://api.npoint.io/` | `https://d3e6htiiul5ek9.cloudfront.net/` |
| Endpoint | `GET /5ea678c319a23faa10ab` | `GET /prod/productos` |
| Tipo de datos | Catálogo estático (nombre, descripción, precio promedio) | Precios reales por ubicación GPS (precioMin, precioMax) |
| Repositorio | `AppRepository.getApiProducts()` | `AppRepository.searchProductPrices()` |
| ¿Dónde se usa actualmente? | Sin uso activo en pantallas | **Comparación de precios en `PurchaseDetailScreen`** |

La comparación visible al usuario utiliza exclusivamente **Precios Claros** (CloudFront). `getApiProducts()` existe en el repositorio pero no está siendo consumida por ninguna pantalla activa.

---

## 2. Diagrama del flujo completo

```
┌─────────────────────────────────────────────────────────────────┐
│                      PurchaseDetailScreen                       │
│                                                                 │
│  LaunchedEffect(purchaseId)                                     │
│      │                                                          │
│      ├─ ¿Tiene permiso ACCESS_FINE_LOCATION?                   │
│      │       ├─ Sí  → viewModel.loadPurchase(id)               │
│      │       └─ No  → solicita permiso → (respuesta del user)  │
│      │                     └─────────→ viewModel.loadPurchase(id)
└──────────────────────┬──────────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────────┐
│                  PurchaseDetailViewModel                        │
│                                                                 │
│  viewModelScope.launch {                                        │
│                                                                 │
│    [1] repository.getPurchaseById(id)  ←── Room (local)        │
│         └─ uiState.purchase = ...                              │
│              └─ UI muestra la compra inmediatamente            │
│                                                                 │
│    [2] LocationHelper.getLocation()    ←── GPS                 │
│         └─ si null → return (sin comparación)                  │
│                                                                 │
│    [3] por cada producto → async {                             │
│           repository.searchProductPrices(lat, lng, nombre)     │
│        }                                                        │
│         └─ awaitAll()  ←── todos en paralelo                   │
│              └─ scorer selecciona el mejor resultado           │
│                   └─ uiState.apiPriceMap = { "leche" → (min, max) }
│                        └─ UI agrega badges de comparación      │
│  }                                                              │
└──────────────────────┬──────────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────────┐
│                       AppRepository                             │
│                                                                 │
│  PreciosClarosClient.service                                    │
│    .getProductos(query, lat, lng, limit = 30)                   │
│                                                                 │
│  URL resultante:                                                │
│  cloudfront.net/prod/productos                                  │
│    ?string=leche+entera&lat=-34.6&lng=-58.4&limit=30            │
│                                                                 │
│  try { ... } catch { emptyList() }  ←── fallo silencioso       │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. Capa de red — los clientes Retrofit

### 3.1 `PreciosClarosClient.kt`

```kotlin
object PreciosClarosClient {

    private const val BASE_URL = "https://d3e6htiiul5ek9.cloudfront.net/"

    val service: PreciosClarosApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PreciosClarosApiService::class.java)
    }
}
```

Es un `object` (singleton): Retrofit y el cliente HTTP subyacente se crean una única vez en toda la vida de la app (`by lazy`).

### 3.2 `PreciosClarosApiService.kt`

```kotlin
interface PreciosClarosApiService {

    @GET("prod/productos")
    suspend fun getProductos(
        @Query("string") query: String,   // nombre del producto a buscar
        @Query("lat")    lat: Double,     // latitud del dispositivo
        @Query("lng")    lng: Double,     // longitud del dispositivo
        @Query("limit")  limit: Int       // máximo de resultados
    ): PreciosClarosResponse
}
```

Retrofit convierte automáticamente los parámetros en query strings de la URL:

```
GET https://d3e6htiiul5ek9.cloudfront.net/prod/productos
        ?string=leche+entera&lat=-34.603&lng=-58.381&limit=30
```

### 3.3 DTOs — los datos que llegan de la API

```kotlin
// El producto que devuelve Precios Claros
data class PreciosClarosProductDto(
    @SerializedName("id")                    val id: String,
    @SerializedName("nombre")                val nombre: String,
    @SerializedName("marca")                 val marca: String,
    @SerializedName("presentacion")          val presentacion: String,
    @SerializedName("precioMin")             val precioMin: Double,  // ← precio mínimo en sucursales cercanas
    @SerializedName("precioMax")             val precioMax: Double,  // ← precio máximo
    @SerializedName("cantSucursalesDisponible") val sucursalesDisponibles: Int
)

// El envelope que envuelve la lista
data class PreciosClarosResponse(
    @SerializedName("productos") val productos: List<PreciosClarosProductDto>
)
```

`@SerializedName` le indica a Gson qué campo del JSON mapea a cada propiedad Kotlin. El JSON que llega de la API usa `snake_case` o nombres en español; los DTOs los traducen al modelo del proyecto.

---

## 4. Capa de repositorio — `AppRepository`

```kotlin
suspend fun searchProductPrices(
    lat: Double,
    lng: Double,
    query: String
): List<PreciosClarosProductDto> =
    try {
        PreciosClarosClient.service
            .getProductos(query = query, lat = lat, lng = lng, limit = 30)
            .productos
    } catch (e: Exception) {
        emptyList()
    }
```

Tres decisiones de diseño importantes aquí:

1. **`suspend fun`**: la llamada HTTP se suspende (no bloquea) en la coroutine que la llama.
2. **`try/catch` con `emptyList()`**: si la API falla por cualquier motivo (sin internet, timeout, 500, etc.), devuelve una lista vacía en lugar de propagar la excepción. La UI simplemente no muestra badges de comparación — no crashea.
3. **`limit = 30`**: se piden hasta 30 candidatos para darle al sistema de scoring (sección 5) suficiente pool donde elegir el resultado más relevante.

---

## 5. Capa de ViewModel — coordinación, paralelismo y scoring

```kotlin
fun loadPurchase(id: String) {
    // ...
    viewModelScope.launch {

        // ── Paso 1: cargar la compra desde Room ──────────────────────────
        val purchase = repository.getPurchaseById(numericId)
        _uiState.value = _uiState.value.copy(isLoading = false, purchase = purchase)

        if (purchase == null) return@launch

        // ── Paso 2: obtener ubicación GPS ─────────────────────────────────
        val coords = LocationHelper(getApplication()).getLocation()
        if (coords == null) return@launch
        val (lat, lng) = coords

        // ── Paso 3: consultar la API para cada producto en paralelo ───────
        val deferreds = purchase.products.map { product ->
            async {
                product.name to repository.searchProductPrices(lat, lng, product.name)
            }
        }

        val apiPriceMap = deferreds.awaitAll()
            .mapNotNull { (name, results) ->
                val queryWords = name.toQueryWords()
                if (queryWords.isEmpty()) return@mapNotNull null

                val firstQueryWord = queryWords.first()
                val maxSucursales = results.maxOfOrNull { it.sucursalesDisponibles } ?: 0

                val best = results
                    .filter { it.precioMin > 0.0 }
                    .map { dto -> dto to scoreDto(dto, queryWords, firstQueryWord, maxSucursales) }
                    .filter { (_, score) -> score >= 0.0 }
                    .maxByOrNull { (_, score) -> score }
                    ?.first
                    ?: return@mapNotNull null

                name.trim().lowercase(Locale.ROOT) to ApiPriceResult(best.precioMin, best.precioMax)
            }
            .toMap()

        _uiState.value = _uiState.value.copy(apiPriceMap = apiPriceMap)
    }
}
```

### ¿Por qué `async` + `awaitAll` en lugar de un loop secuencial?

Si una compra tiene 3 productos y cada llamada HTTP tarda ~800ms, la diferencia es notable:

```
Loop secuencial:   producto1 → producto2 → producto3  =  ~2400ms
async + awaitAll:  producto1 ┐
                   producto2 ┼─ en paralelo ──────────  =  ~800ms
                   producto3 ┘
```

`async { }` lanza una coroutine que corre concurrentemente. `awaitAll()` suspende hasta que **todas** terminen, y devuelve sus resultados en orden.

### El sistema de scoring

La API de Precios Claros busca por **substring**, lo que significa que "leche" devuelve alfajores de dulce de leche antes que leche entera. Para elegir el resultado más relevante del pool de 30 candidatos, el ViewModel aplica un scorer compuesto.

#### Normalización del nombre (`toQueryWords`)

```kotlin
private val SPANISH_STOP_WORDS = setOf(
    "con", "del", "los", "las", "una", "uno", "por", "sin",
    "sobre", "para", "como", "cada", "pero", "mas", "sus", "que"
)

private fun String.toQueryWords(): List<String> =
    trim().lowercase(Locale.ROOT)
        .split("\\s+".toRegex())
        .filter { it.length >= 3 && it !in SPANISH_STOP_WORDS }
```

El nombre del producto se convierte en una lista de palabras significativas: minúsculas, sin palabras de menos de 3 caracteres y sin artículos/preposiciones del español. Así `"arroz con leche"` produce `["arroz", "leche"]` — "con" no participa en el match.

#### Función de scoring (`scoreDto`)

```kotlin
private fun scoreDto(
    dto: PreciosClarosProductDto,
    queryWords: List<String>,
    firstQueryWord: String,
    maxSucursales: Int
): Double {
    val dtoWords = dto.nombre.lowercase(Locale.ROOT).split("\\s+".toRegex())
    val matched = queryWords.count { qw -> dtoWords.any { dw -> dw.startsWith(qw) } }
    val matchRatio = matched.toDouble() / queryWords.size
    if (matchRatio < MIN_MATCH_RATIO) return -1.0   // descarta el candidato
    val normSuc = if (maxSucursales > 0) dto.sucursalesDisponibles.toDouble() / maxSucursales else 0.0
    val firstBonus = if ((dtoWords.firstOrNull() ?: "").startsWith(firstQueryWord)) BONUS_FIRST else 0.0
    return (matchRatio * WEIGHT_MATCH) + (normSuc * WEIGHT_SUC) + firstBonus
}
```

#### Fórmula del score

```
score = (matchRatio × 0.70) + (normSuc × 0.15) + firstBonus(0.15 ó 0.0)
```

| Componente | Peso | Qué mide |
|---|---|---|
| `matchRatio` | 70% | Fracción de palabras del query presentes en el nombre del DTO |
| `normSuc` | 15% | Popularidad relativa: `sucursalesDTO / maxSucursalesDelPool` |
| `firstBonus` | 15% | Bonus si la primera palabra del DTO coincide con la primera del query |

Un DTO solo es candidato si `matchRatio >= 0.50`. Si ningún resultado del pool supera ese umbral, no se muestra badge — mejor no mostrar nada que mostrar un precio incorrecto.

#### Ejemplo con "leche entera"

| Producto de la API | matchRatio | normSuc | firstBonus | score |
|---|---|---|---|---|
| Leche Entera La Serenísima 1L | 2/2 = 1.0 | 28/28 = 1.0 | +0.15 | **1.0** ✅ elegido |
| Leche en Polvo Entera Día 400g | 2/2 = 1.0 | 23/28 = 0.82 | +0.15 | 0.97 |
| Casanto Leche Entera 1L | 2/2 = 1.0 | 27/28 = 0.96 | 0 (empieza con "casanto") | 0.84 |
| Alfajor Dulce de Leche | 1/2 = 0.5 | — | — | **-1.0** ❌ descartado |

### La clave del mapa: nombre en minúsculas

```kotlin
name.trim().lowercase(Locale.ROOT) to ApiPriceResult(best.precioMin, best.precioMax)
```

La clave del mapa resultante usa el nombre original del producto (no el nombre del DTO ganador), normalizado a minúsculas con `Locale.ROOT`. La pantalla busca con exactamente esa misma clave (`product.name.trim().lowercase(Locale.ROOT)`), por lo que ambos lados siempre coinciden.

### El modelo intermedio `ApiPriceResult`

```kotlin
data class ApiPriceResult(val precioMin: Double, val precioMax: Double)
```

Es intencional que sea más pequeño que el DTO. La UI solo necesita los dos precios de referencia — no la marca, presentación ni cantidad de sucursales.

---

## 6. Capa de UI — mostrar los badges

### 6.1 Permiso de ubicación antes de cargar

```kotlin
LaunchedEffect(purchaseId) {
    val granted = ContextCompat.checkSelfPermission(
        context, android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (granted) {
        viewModel.loadPurchase(purchaseId)
    } else {
        locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }
}
```

`LaunchedEffect(purchaseId)` garantiza que esto corre una sola vez por `purchaseId`. El launcher siempre llama `loadPurchase()` independientemente de si el usuario acepta o rechaza el permiso — si no se otorga, `LocationHelper.getLocation()` devolverá `null` y la comparación simplemente no aparece.

### 6.2 Renderizado de cada producto con su badge

```kotlin
p.products.forEachIndexed { idx, product ->
    ProductItem(product = product)

    // Busca el resultado de la API por nombre (en minúsculas)
    val apiResult = state.apiPriceMap[product.name.trim().lowercase(Locale.ROOT)]
    if (apiResult != null) {
        PriceComparisonBadge(paidPrice = product.price, apiResult = apiResult)
    }

    if (idx < p.products.lastIndex) {
        HorizontalDivider(...)
    }
}
```

Si `apiPriceMap` todavía está vacío (la API no respondió aún), `apiResult` es `null` y el badge simplemente no se muestra. Cuando la coroutine termina y el estado se actualiza con `apiPriceMap`, Compose recompone automáticamente y los badges aparecen — sin ningún código adicional de sincronización.

### 6.3 Lógica del badge `PriceComparisonBadge`

```kotlin
@Composable
private fun PriceComparisonBadge(paidPrice: Double, apiResult: ApiPriceResult) {
    if (apiResult.precioMin <= 0.0) return   // dato inválido → no mostrar

    val ratio = paidPrice / apiResult.precioMin

    val (icon, label, color) = when {
        ratio < 0.90 -> Triple(Icons.Default.CheckCircle, "Buen precio",  verde)
        ratio > 1.10 -> Triple(Icons.Default.Warning,     "Precio alto",  rojo)
        else         -> Triple(Icons.Default.Info,         "Precio justo", gris)
    }

    val rangeText = "Ref: $${precioMin} – $${precioMax}"

    Row(...) {
        Icon(icon, ...)
        Text(label, ...)
        Text(rangeText, ...)
    }
}
```

#### Tabla de decisión del badge

| Condición | Significado | Badge |
|---|---|---|
| `precioPagado / precioMin < 0.90` | Pagaste menos del 90% del precio mínimo de referencia | ✅ Buen precio (verde) |
| `precioPagado / precioMin > 1.10` | Pagaste más del 110% del precio mínimo | ⚠️ Precio alto (rojo) |
| Entre 0.90 y 1.10 | Dentro del margen del ±10% | ℹ️ Precio justo (gris) |

El margen del **±10%** actúa como zona de tolerancia para no penalizar diferencias pequeñas de precio (variaciones de presentación, descuentos, etc.).

El badge también muestra el rango `precioMin – precioMax` como texto de referencia, para que el usuario sepa contra qué se comparó.

---

## 7. Casos de falla y su manejo

| Situación | Comportamiento |
|---|---|
| Sin permiso de ubicación | No se llama a la API; la compra se muestra sin badges |
| GPS apagado / sin señal | `LocationHelper` devuelve `null`; `return@launch` silencioso |
| Sin internet | `catch (e: Exception) { emptyList() }` en el repositorio; sin badges |
| API devuelve lista vacía | Sin candidatos para el scorer; el producto se excluye del mapa |
| Ningún candidato supera `matchRatio >= 0.50` | El scorer descarta todos; sin badge — evita mostrar precios incorrectos |
| `precioMin <= 0` en el resultado ganador | Filtrado antes del scoring (`filter { it.precioMin > 0.0 }`) y guard en `PriceComparisonBadge` |
| Query compuesta solo de stop words | `toQueryWords()` devuelve lista vacía; `return@mapNotNull null` |
| `purchaseId` no numérico | Guard al inicio de `loadPurchase`; estado con `isLoading = false` |

---

## 8. Secuencia temporal en la UI

```
t=0ms    Usuario abre PurchaseDetailScreen
          └─ CircularProgressIndicator visible

t=~50ms  Room devuelve la compra
          └─ Compra visible, lista de productos sin badges

t=~200ms LocationHelper obtiene coordenadas GPS

t=~200ms Lanza async por cada producto (en paralelo)
          └─ cada coroutine consulta limit=30 resultados de la API

t=~800ms awaitAll() completa
          └─ scorer elige el mejor candidato por producto
               └─ todos los badges aparecen simultáneamente
```

La UI es **progresiva**: la compra aparece rápidamente (Room es local), y los badges se agregan cuando la API responde — sin bloquear ni mostrar pantalla en blanco.
