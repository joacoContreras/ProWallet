# Comparación de precios via networking — ProWallet

Documento técnico que explica el flujo completo de la funcionalidad que consulta precios de referencia desde una API externa, los persiste en una base de datos local (Room) bajo el patrón *Single Source of Truth*, y los compara contra los precios registrados en una compra.

---

## 1. Arquitectura de Integración (Room + Retrofit)

Para evitar tener Room y Retrofit como "islas separadas", la aplicación implementa el patrón **Single Source of Truth (SSOT)**. La UI nunca consume los datos de red de forma directa. En su lugar:
1. La UI llama al ViewModel para solicitar datos.
2. El ViewModel consulta al **Repository**.
3. El **Repository** consulta primero a **Room** (Base de datos local) para ver si hay una caché válida.
4. Si la caché existe y es válida (no ha expirado), se retorna directamente desde **Room**.
5. Si no hay caché o está vencida, el **Repository** consulta a la API externa mediante **Retrofit**, guarda la respuesta de forma local en **Room** y la retorna.

---

## 2. Diagrama del flujo completo de comparación

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
│    [3] supervisorScope {                                        │
│          por cada producto → async {                            │
│             repository.searchProductPrices(lat, lng, nombre)    │
│          }                                                      │
│          └─ awaitAll()  ←── en paralelo, fallos aislados       │
│        }                                                        │
│         └─ scorer selecciona el mejor resultado                 │
│              └─ uiState.apiPriceMap = { "leche" → (min, max) }  │
│                   └─ UI agrega badges de comparación            │
│  }                                                              │
└──────────────────────┬──────────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────────┐
│                        AppRepository                            │
│                                                                 │
│  searchProductPrices(lat, lng, query):                          │
│                                                                 │
│  1. Consultar Room (preciosClarosProductDao.getProductsByQuery) │
│  2. ¿Caché válida (< 24 horas)?                                 │
│       ├─ SÍ → Retorna caché local de Room                       │
│       └─ NO → Consulta Retrofit (PreciosClarosClient)           │
│                 │                                               │
│                 ├─ Éxito → Guarda en Room + retorna             │
│                 └─ Fallo → Retorna caché vieja (resiliencia)    │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. Persistencia de Caché (Room)

### 3.1 Entidad de Caché ([PreciosClarosProductEntity.kt](file:///C:/Users/cjoaq/AndroidStudioProjects/MyApplication/app/src/main/java/com/undef/prowallet/data/PreciosClarosProductEntity.kt))
Esta tabla almacena los resultados de búsqueda de la API de Precios Claros para consultas específicas, asociando cada producto con la búsqueda origen (`query`) y un `timestamp`.

```kotlin
@Entity(tableName = "precios_claros_cache")
data class PreciosClarosProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "query") val query: String,
    @ColumnInfo(name = "api_product_id") val apiProductId: String?,
    @ColumnInfo(name = "nombre") val nombre: String?,
    @ColumnInfo(name = "marca") val marca: String?,
    @ColumnInfo(name = "presentacion") val presentacion: String?,
    @ColumnInfo(name = "precio_min") val precioMin: Double?,
    @ColumnInfo(name = "precio_max") val precioMax: Double?,
    @ColumnInfo(name = "sucursales_disponibles") val sucursalesDisponibles: Int?,
    @ColumnInfo(name = "timestamp") val timestamp: Long
)
```

### 3.2 DAO de Caché ([PreciosClarosProductDao.kt](file:///C:/Users/cjoaq/AndroidStudioProjects/MyApplication/app/src/main/java/com/undef/prowallet/data/dao/PreciosClarosProductDao.kt))
Define los accesos para la caché.

```kotlin
@Dao
interface PreciosClarosProductDao {
    @Query("SELECT * FROM precios_claros_cache WHERE `query` = :query")
    suspend fun getProductsByQuery(query: String): List<PreciosClarosProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<PreciosClarosProductEntity>)

    @Query("DELETE FROM precios_claros_cache WHERE `query` = :query")
    suspend fun deleteByQuery(query: String)
}
```

### 3.3 Configuración de la base de datos y migración ([ProWalletDatabase.kt](file:///C:/Users/cjoaq/AndroidStudioProjects/MyApplication/app/src/main/java/com/undef/prowallet/data/ProWalletDatabase.kt))
La base de datos se actualizó a la versión `8` e incorporó `PreciosClarosProductEntity`. Se registró la migración `MIGRATION_7_8` para crear la tabla de caché de forma segura en producción y desarrollo:

```kotlin
private val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS precios_claros_cache (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `query` TEXT NOT NULL,
                api_product_id TEXT,
                nombre TEXT,
                marca TEXT,
                presentacion TEXT,
                precio_min REAL,
                precio_max REAL,
                sucursales_disponibles INTEGER,
                timestamp INTEGER NOT NULL
            )
        """.trimIndent())
    }
}
```

---

## 4. Capa de Red — `PreciosClarosClient`

### 4.1 Cliente y API Service ([PreciosClarosClient.kt](file:///C:/Users/cjoaq/AndroidStudioProjects/MyApplication/app/src/main/java/com/undef/prowallet/data/remote/PreciosClarosClient.kt))
Se expone un cliente Retrofit usando `by lazy` para garantizar un único HttpClient (Singleton) y un consumo optimizado.

```kotlin
interface PreciosClarosApiService {
    @GET("prod/productos")
    suspend fun getProductos(
        @Query("string") query: String,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("limit") limit: Int
    ): PreciosClarosResponse
}
```

### 4.2 DTO y Tolerancia a Nulos ([ProductDto.kt](file:///C:/Users/cjoaq/AndroidStudioProjects/MyApplication/app/src/main/java/com/undef/prowallet/data/remote/ProductDto.kt))
Los campos del DTO `PreciosClarosProductDto` se marcan como anulables (`Double?`, `Int?`, `String?`) para mitigar excepciones de unboxing por Gson si los datos en el JSON faltan o son corruptos.

```kotlin
data class PreciosClarosProductDto(
    @SerializedName("id") val id: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("marca") val marca: String?,
    @SerializedName("presentacion") val presentacion: String?,
    @SerializedName("precioMin") val precioMin: Double?,
    @SerializedName("precioMax") val precioMax: Double?,
    @SerializedName("cantSucursalesDisponible") val sucursalesDisponibles: Int?
)
```

---

## 5. Implementación del repositorio ([AppRepository.kt](file:///C:/Users/cjoaq/AndroidStudioProjects/MyApplication/app/src/main/java/com/undef/prowallet/data/AppRepository.kt))

La lógica centralizada en `searchProductPrices` implementa la lógica de expiración (24 horas) y la resiliencia en modo offline:

```kotlin
suspend fun searchProductPrices(lat: Double, lng: Double, query: String): List<PreciosClarosProductDto> {
    val normalizedQuery = query.trim().lowercase(Locale.ROOT)
    
    // 1. Consultar Room primero (cache-first)
    val cached = preciosClarosProductDao.getProductsByQuery(normalizedQuery)
    val isExpired = cached.isNotEmpty() && (System.currentTimeMillis() - cached.first().timestamp > 24 * 60 * 60 * 1000)

    if (cached.isNotEmpty() && !isExpired) {
        return cached.map {
            PreciosClarosProductDto(
                id = it.apiProductId,
                nombre = it.nombre,
                marca = it.marca,
                presentacion = it.presentacion,
                precioMin = it.precioMin,
                precioMax = it.precioMax,
                sucursalesDisponibles = it.sucursalesDisponibles
            )
        }
    }

    // 2. Si no hay cache o está vencido, llamar a Retrofit
    try {
        val dtos = PreciosClarosClient.service.getProductos(query = query, lat = lat, lng = lng, limit = 30).productos ?: emptyList()

        // 3. Guardar la respuesta en Room (SSOT)
        preciosClarosProductDao.deleteByQuery(normalizedQuery)
        if (dtos.isNotEmpty()) {
            val entities = dtos.map { dto ->
                PreciosClarosProductEntity(
                    query = normalizedQuery,
                    apiProductId = dto.id,
                    nombre = dto.nombre,
                    marca = dto.marca,
                    presentacion = dto.presentacion,
                    precioMin = dto.precioMin,
                    precioMax = dto.precioMax,
                    sucursales_disponibles = dto.sucursalesDisponibles,
                    timestamp = System.currentTimeMillis()
                )
            }
            preciosClarosProductDao.insertAll(entities)
        }
        return dtos
    } catch (e: Exception) {
        // Modo resiliente offline: si la API falla, devolver lo que tengamos en cache
        if (cached.isNotEmpty()) {
            return cached.map {
                PreciosClarosProductDto(
                    id = it.apiProductId,
                    nombre = it.nombre,
                    marca = it.marca,
                    presentacion = it.presentacion,
                    precioMin = it.precioMin,
                    precioMax = it.precioMax,
                    sucursalesDisponibles = it.sucursalesDisponibles
                )
            }
        }
        return emptyList()
    }
}
```

---

## 6. Lógica de Negocio y Scoring en el ViewModel

### 6.1 Paralelismo y control de ráfagas (Rate Limiting) ([PurchaseDetailViewModel.kt](file:///C:/Users/cjoaq/AndroidStudioProjects/MyApplication/app/src/main/java/com/undef/prowallet/viewmodel/PurchaseDetailViewModel.kt))

#### Evitando bloqueos de red (Staggered Delay)
La API de Precios Claros opera detrás del CDN de CloudFront, el cual cuenta con políticas de seguridad estrictas contra ráfagas de peticiones de una misma IP (*burst/DDoS protection*). Si la app dispara 3 o más consultas HTTP concurrentemente en el mismo milisegundo, la CDN responde con errores `403 Forbidden` o `429 Too Many Requests` para los productos subsiguientes.

Para evadir este límite de forma transparente y eficiente, la aplicación intercala un **retraso escalonado (staggered delay)** de 400ms entre el lanzamiento de cada tarea asíncrona.

#### Implementación con `supervisorScope` y `delay`:
Utilizamos `supervisorScope` para que el fallo aislado de un producto no cancele las otras descargas ni tire la aplicación. Con `mapIndexed` y `delay`, escalonamos las tareas:

```kotlin
val apiPriceMap = supervisorScope {
    val deferreds = purchase.products.mapIndexed { index, product ->
        async {
            if (index > 0) {
                kotlinx.coroutines.delay(400L * index) // Espera 400ms por producto adicional
            }
            product.name to repository.searchProductPrices(lat, lng, product.name)
        }
    }
    deferreds.awaitAll()
}.mapNotNull { (name, results) ->
    // scoring de resultados y conversión a mapa
}
```
* El producto 0 se consulta en `t = 0 ms`.
* El producto 1 se consulta en `t = 400 ms`.
* El producto 2 se consulta en `t = 800 ms`.

Esto asegura que todas las peticiones se completen de manera óptima sin ser bloqueadas por el cortafuegos de la API.

### 6.2 El algoritmo de scoring
El ViewModel selecciona el producto más relevante entre los devueltos mediante una combinación de ratio de coincidencia, disponibilidad física y orden de las palabras.

```kotlin
private fun scoreDto(
    dto: PreciosClarosProductDto,
    queryWords: List<String>,
    firstQueryWord: String,
    maxSucursales: Int
): Double {
    val nombre = dto.nombre ?: return -1.0
    val dtoWords = nombre.lowercase(Locale.ROOT).split("\\s+".toRegex())
    val matched = queryWords.count { qw -> dtoWords.any { dw -> dw.startsWith(qw) } }
    val matchRatio = matched.toDouble() / queryWords.size
    if (matchRatio < MIN_MATCH_RATIO) return -1.0
    val sucursales = dto.sucursalesDisponibles ?: 0
    val normSuc = if (maxSucursales > 0) sucursales.toDouble() / maxSucursales else 0.0
    val firstBonus = if ((dtoWords.firstOrNull() ?: "").startsWith(firstQueryWord)) BONUS_FIRST else 0.0
    return (matchRatio * WEIGHT_MATCH) + (normSuc * WEIGHT_SUC) + firstBonus
}
```

#### ¿Importa el orden de las palabras del producto?

* **Para la coincidencia general (No importa el orden):**
  El cálculo de palabras coincidentes (`matchRatio`) cuenta la presencia de palabras significativas en cualquier orden. Buscar `"Arroz Gallo"` o `"Gallo Arroz"` produce el mismo índice de match general, ya que solo evalúa si las palabras están presentes.
* **Para el bono por primera palabra (Sí importa el orden):**
  Para aumentar la relevancia del desempate, el algoritmo otorga un beneficio extra de `0.15` (`firstBonus`) si la **primera palabra** del término de búsqueda coincide con la **primera palabra** de la descripción devuelta por la API. Esto hace que buscar `"Coca Cola"` priorice a `"Coca Cola 1.5L"` por sobre `"Cola Marca Coca 1.5L"`.

---

## 7. Persistencia al editar compras y productos

Para que la modificación del nombre de un producto en una compra ya registrada persista en la base de datos de forma correcta, las funciones `savePurchase` y `updatePurchase` de `AppRepository.kt` buscan el producto en Room por su código. Si el producto ya existe pero su nombre o descripción han cambiado en el formulario de la UI, se actualizan sus datos en la base de datos mediante:

```kotlin
if (existingProduct.name != product.name || existingProduct.description != product.description) {
    productDao.update(
        existingProduct.copy(
            name = product.name,
            description = product.description
        )
    )
}
```
Esto previene que la aplicación reuse el nombre viejo del producto guardado históricamente y persista los nuevos datos provistos por el usuario en tiempo real.
