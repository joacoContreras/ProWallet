# Propuesta de Arquitectura: Sincronización en la Nube para ProWallet

Este documento presenta una propuesta técnica detallada para cumplir con el requerimiento de **Sincronización en la Nube** en **ProWallet**, adaptándose al stack tecnológico actual del proyecto (Room, Retrofit, DataStore y Jetpack Compose).

---

## 1. Estado Actual de Sincronización en ProWallet

Al analizar la base de código actual, encontramos las siguientes características relevantes:

*   **Persistencia Local**: La aplicación utiliza Room como su base de datos local principal con entidades para: `users`, `purchases`, `products`, `purchased_items`, `categories`, `accounts` y `fixed_expenses`.
*   **Sesiones de Usuario**: Gestionadas mediante DataStore (`SessionManager.kt`), que persiste el estado de logueo y el email del usuario actual, pero **la base de datos de compras (`purchases`) no cuenta actualmente con un identificador de usuario (`user_id`)**.
*   **Intento de Sincronización Existente**: En `AppRepository.kt` (línea 91), la función `guardarCompra` intenta hacer un POST remoto al guardar una compra:
    ```kotlin
    suspend fun guardarCompra(compra: Purchase): Int {
        try {
            RetrofitClient.apiService.crearCompra(compra)
        } catch (e: Exception) {
            // Ignora errores de red para soporte offline-first
        }
        return savePurchase(compra)
    }
    ```
    *   **Limitaciones Actuales**:
        1.  **Falta de Resiliencia**: Si el dispositivo está sin conexión, la excepción se ignora y la compra **nunca** se vuelve a reintentar subir cuando se restablece la red.
        2.  **Sincronización de una Vía**: No hay recuperación (pull) de compras creadas en otros dispositivos o reinstalaciones.
        3.  **Falta de Soporte Multi-Usuario**: Las compras guardadas localmente no están enlazadas a un `user_id`, lo que imposibilita segmentar los datos en el servidor por usuario.
        4.  **Otras Entidades sin Sincronizar**: Elementos clave como las cuentas (`accounts`), las categorías personalizadas (`categories`) y los gastos fijos (`fixed_expenses`) carecen de mecanismos de sincronización.

---

## 2. Estrategias de Sincronización Propuestas

Podemos cumplir con este requerimiento de dos formas principales:

### Estrategia A: Sincronización Asíncrona (Offline-First) mediante Room + WorkManager + Retrofit (Recomendada)

Consiste en mantener **Room** como la única fuente de verdad local (Single Source of Truth) y añadir una capa de sincronización en segundo plano coordinada por **WorkManager** de Android.

```
+------------------------------------+
|         UI (Jetpack Compose)       |
+------------------+-----------------+
                   | (Lectura/Escritura inmediata)
                   v
+------------------+-----------------+
|          Base de Datos Room        | <---+
|    (Con metadatos de sincronización)|     |
+------------------+-----------------+     | (Actualiza local y
                   |                       |  limpia flags sucios)
                   | (Dispara al cambiar)  |
                   v                       |
+------------------+-----------------+     |
|   WorkManager (SyncWorker)         |-----+
+------------------+-----------------+
                   | (Llamadas API en Batch)
                   v
+------------------+-----------------+
|      Retrofit / API Service        |
+------------------+-----------------+
                   |
                   v
+------------------+-----------------+
|     Servidor Cloud / Backend       |
+------------------------------------+
```

*   **Cómo funciona**:
    1.  Cada entidad local incluye metadatos: `is_dirty` (indica si cambió localmente y no se ha subido), `is_deleted` (eliminación lógica) y `last_updated` (marca de tiempo).
    2.  Al modificar datos, la app actualiza Room de inmediato (respuesta instantánea en la UI) y marca el registro como `is_dirty = true`.
    3.  Se encola una tarea en `WorkManager` con restricciones de red (`NetworkType.CONNECTED`).
    4.  El `Worker` ejecuta en segundo plano un proceso de sincronización (Push/Pull) contra un Backend personalizado, enviando cambios locales y descargando cambios remotos desde el último sync.
*   **Pros**:
    *   **Control total** del flujo de sincronización y del backend.
    *   **Excelente rendimiento y consumo de batería** al usar las APIs de optimización del sistema operativo (WorkManager).
    *   No rompe la arquitectura MVVM ni los flujos reactivos de Room que ya posee el proyecto.
*   **Cons**:
    *   Requiere diseñar y programar la lógica de resolución de conflictos (ej. Last-Write-Wins en base a marcas de tiempo) y la lógica de negocio en el Backend.

---

### Estrategia B: Sincronización Serverless (ej. Firebase Firestore / Supabase)

Consiste en delegar la base de datos y la sincronización a una plataforma Serverless (como Firebase Firestore o el SDK de Supabase).

*   **Cómo funciona**:
    *   Se reemplaza Room (o se crea un puente) para utilizar las librerías oficiales de Firebase Firestore/Supabase.
    *   Estas plataformas gestionan de forma nativa la caché offline y sincronizan automáticamente las colecciones en tiempo real al detectar conexión de red.
*   **Pros**:
    *   **Desarrollo rápido**: No requiere programar ni hostear un backend personalizado para la sincronización.
    *   **Resolución nativa**: El SDK resuelve la mayoría de los problemas de desconexión y sincronización automáticamente.
*   **Cons**:
    *   **Dependencia tecnológica (Vendor Lock-in)**: Acopla fuertemente el proyecto a la infraestructura de Firebase/Supabase.
    *   **Rediseño de Consultas**: Room permite realizar operaciones complejas de base de datos relacional y transacciones SQL complejas nativas de SQLite; portar todo a una base de datos NoSQL como Firestore requiere repensar las relaciones complejas de `PurchaseWithItems`.

---

## 3. Plan de Implementación Detallado (Estrategia A - Recomendada)

Para implementar la **Estrategia A (Room + WorkManager + Retrofit)** en ProWallet, se deben seguir los siguientes pasos:

### Paso 1: Vincular Datos al Usuario
Debemos añadir un campo `user_id` en las tablas que requieran sincronización. Por ejemplo, en `PurchaseEntity`:
```kotlin
@Entity(
    tableName = "purchases",
    foreignKeys = [
        ForeignKey(entity = CategoryEntity::class, parentColumns = ["id"], childColumns = ["category_id"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["user_id"], onDelete = ForeignKey.CASCADE)
    ]
)
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "user_id") val userId: Int, // Enlace al usuario logueado
    @ColumnInfo(name = "category_id") val categoryId: Int?,
    // ... otros campos
)
```

### Paso 2: Agregar Campos de Metadatos de Sincronización
Modificaremos las entidades de Room (`purchases`, `accounts`, `fixed_expenses`, `categories`) agregando los siguientes atributos:
```kotlin
@ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
@ColumnInfo(name = "is_dirty") val isDirty: Boolean = false,
@ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
```
*   *Nota*: Para eliminaciones, en lugar de ejecutar un `DELETE` físico en Room, cambiaremos `is_deleted = true` e `is_dirty = true` para poder notificar al servidor de la eliminación. Tras sincronizar con éxito, el registro puede borrarse físicamente si se desea, o mantenerse para propósitos de histórico local.

### Paso 3: Crear el Contrato de la API de Sincronización (`SyncApiService`)
Definiremos un endpoint que reciba y devuelva las novedades (deltas) en una sola transacción o por lotes:
```kotlin
interface SyncApiService {
    @POST("sync")
    suspend fun syncData(
        @Header("Authorization") token: String,
        @Body syncRequest: SyncRequest
    ): Response<SyncResponse>
}

// Estructura de la petición
data class SyncRequest(
    val lastSyncTime: Long,
    val modifiedPurchases: List<PurchaseDto>,
    val deletedPurchaseIds: List<String>,
    // ... lo mismo para cuentas, categorías y gastos fijos
)

data class SyncResponse(
    val serverTime: Long,
    val remotePurchases: List<PurchaseDto>,
    val remoteDeletedPurchaseIds: List<String>
    // ...
)
```

### Paso 4: Implementar el SyncWorker con WorkManager
Crearemos un worker que gestione el flujo de sincronización asíncrona de manera segura y controlando la conectividad de internet:

```kotlin
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = AppRepository(applicationContext)
        val sessionManager = SessionManager(applicationContext)
        val userEmail = sessionManager.email.first() ?: return Result.failure()

        try {
            // 1. Obtener de Room los registros locales que tengan is_dirty = true
            val dirtyPurchases = repository.getDirtyPurchases()
            val deletedPurchaseIds = repository.getDeletedPurchaseIds()

            // 2. Obtener la marca de tiempo del último sync exitoso del DataStore
            val lastSyncTime = sessionManager.getLastSyncTime()

            // 3. Enviar a la API mediante Retrofit
            val response = RetrofitClient.syncApiService.syncData(
                token = "Bearer ...",
                syncRequest = SyncRequest(
                    lastSyncTime = lastSyncTime,
                    modifiedPurchases = dirtyPurchases,
                    deletedPurchaseIds = deletedPurchaseIds
                )
            )

            if (response.isSuccessful && response.body() != null) {
                val syncResponse = response.body()!!

                // 4. Integrar cambios remotos en Room y resolver conflictos (Last-Write-Wins)
                repository.applySyncResponse(syncResponse)

                // 5. Limpiar los flags is_dirty de los registros locales subidos
                repository.clearDirtyFlags(dirtyPurchases)

                // 6. Guardar la nueva marca de tiempo del último sync en DataStore
                sessionManager.saveLastSyncTime(syncResponse.serverTime)

                return Result.success()
            }
            return Result.retry()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}
```

### Paso 5: Programar el Disparador de Sincronización
Cada vez que el usuario realice una acción (crear compra, editar presupuesto, borrar cuenta), la app actualiza Room inmediatamente y luego le indica a WorkManager que encole el trabajo de sincronización:

```kotlin
fun triggerSync(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
        .setConstraints(constraints)
        .setBackoffCriteria(
            BackoffPolicy.EXPONENTIAL,
            WorkRequest.MIN_BACKOFF_MILLIS,
            TimeUnit.MILLISECONDS
        )
        .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
        "cloud_sync_work",
        ExistingWorkPolicy.REPLACE, // Evita colisiones si hay múltiples escrituras seguidas
        syncWorkRequest
    )
}
```

---

## 4. Próximos Pasos Recomendados

Para avanzar en la implementación, propongo:

1.  **Alineamiento del Diseño**: Decidir si utilizaremos la **Estrategia A (Room + WorkManager + backend personalizado)** o si prefieres migrar hacia **Estrategia B (Firebase / Supabase)**.
2.  **Modificaciones de la BD**: Si optamos por la Estrategia A, podemos proceder a actualizar la base de datos de Room introduciendo los campos de sincronización, la relación con el usuario (`userId`) y las consultas DAO para filtrar registros "sucios".
3.  **Implementación del Worker**: Configurar e integrar la dependencia de WorkManager en el archivo `app/build.gradle.kts` e implementar la lógica de sincronización asíncrona.
