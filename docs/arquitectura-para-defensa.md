# Explicación de arquitectura — ProWallet
## Material de apoyo para la defensa oral

---

## 1. Visión general: ¿Qué es MVVM?

MVVM son las siglas de **Model – View – ViewModel**.

```
┌─────────────────────────────────────────────────────────┐
│  VIEW  (Jetpack Compose)                                │
│  Composable observa el UiState                          │
│  NO tiene lógica de negocio                             │
│  NO accede a la base de datos ni a la red               │
└──────────────────────┬──────────────────────────────────┘
                       │  collectAsStateWithLifecycle()
                       ▼
┌─────────────────────────────────────────────────────────┐
│  VIEWMODEL  (AndroidViewModel)                          │
│  Contiene el UiState como StateFlow                     │
│  Lanza coroutinas con viewModelScope                    │
│  Sobrevive a rotación de pantalla                       │
│  NO accede a la base de datos directamente              │
└──────────────────────┬──────────────────────────────────┘
                       │  suspend fun / Flow
                       ▼
┌─────────────────────────────────────────────────────────┐
│  REPOSITORY  (AppRepository)                            │
│  Única fuente de verdad                                 │
│  Decide si usar Room o Retrofit                         │
│  La UI nunca sabe de dónde vienen los datos             │
└──────────┬─────────────────────────┬───────────────────┘
           │                         │
           ▼                         ▼
┌────────────────────┐   ┌────────────────────────────────┐
│  ROOM  (SQLite)    │   │  RETROFIT  (Red)               │
│  Base de datos     │   │  Solo cuando Room está vacío   │
│  local — persiste  │   │  Resultado se guarda en Room   │
│  entre reinicios   │   │  La UI nunca lo llama directo  │
│  Emite Flow<T>     │   │                                │
└────────────────────┘   └────────────────────────────────┘
```

**Regla de oro:** La UI solo observa el ViewModel. El ViewModel solo habla con el Repository. El Repository decide entre Room y Retrofit. Room siempre gana — es la fuente de verdad.

---

## 2. Flujo concreto: guardar una compra

Este es el flujo más importante para mostrar en la defensa.

```
1. Usuario toca "Guardar compra"
   └── NewPurchaseScreen.kt
         └── Button(onClick = { viewModel.savePurchase() })

2. PurchaseViewModel.savePurchase()
   └── Valida que storeName no esté vacío
   └── Calcula totalAmount = products.sumOf { it.price }
   └── _uiState.value = copy(isSaving = true)
   └── viewModelScope.launch {
         try {
           val id = repository.savePurchase(purchase)
           _uiState.value = copy(savedSuccess = true, savedPurchaseId = id)
         } catch (e: Exception) {
           _uiState.value = copy(saveError = true)
         } finally {
           _uiState.value = copy(isSaving = false)
         }
       }

3. AppRepository.savePurchase(purchase)
   └── db.withTransaction {              ← atomicidad garantizada
         purchaseDao.insert(PurchaseEntity(...))
         purchase.products.forEach { product ->
           productDao.insert(ProductEntity(...))    ← upsert por UNIQUE code
           purchasedItemDao.insert(PurchasedItemEntity(...))
         }
         return purchaseId
       }

4. Room persiste la compra en SQLite

5. purchaseDao.getAllPurchasesWithItems() emite nuevo valor
   └── purchasesFlow en AppRepository actualizado
         └── HomeViewModel.purchasesFlow.collect { ... }
               └── _uiState.value = copy(recentPurchases = ..., totalMonthlySpend = ...)
                     └── HomeScreen.collectAsState() → UI actualizada automáticamente
```

---

## 3. Flujo concreto: Room como Single Source of Truth con Retrofit

Este flujo responde directamente a la crítica del profesor.

```
1. HomeViewModel.init
   └── viewModelScope.launch {
         repository.seedDefaultCategories()
         repository.refreshApiProductsIfEmpty()  ← punto clave
         repository.purchasesFlow.collect { ... }
       }

2. AppRepository.refreshApiProductsIfEmpty()
   └── val count = productDao.getProductCount()
   └── if (count > 0) return  ← Room ya tiene datos, no hay llamada de red
   └── // Primera vez: Room vacío
   └── try {
         val dtos = RetrofitClient.productApiService.getProducts().productos
         productDao.insertAll(entities)  ← Room persiste los datos de la API
       } catch (_: Exception) {
         // Sin red: la tabla queda vacía, el Flow emite lista vacía — no crashea
       }

3. productDao.getAllProducts()  ← Flow reactivo
   └── Emite automáticamente la lista nueva
         └── Cualquier Composable que observe apiProductsFlow se actualiza solo

SEGUNDA VEZ (sin red):
   └── productDao.getProductCount() > 0 → retorna inmediatamente
   └── Room sirve los datos del disco
   └── La app funciona offline
```

**Por qué importa:** Sin este patrón, cada pantalla tendría que manejar errores de red, estados de carga, y sincronización manual. Con Room como SSoT, toda esa complejidad queda en el Repository.

---

## 4. Flujo concreto: comparación de precios (Retrofit con location)

```
1. Usuario abre PurchaseDetailScreen(purchaseId = "42")

2. PurchaseDetailViewModel.loadPurchase("42")
   └── repository.getPurchaseById(42)  ← Room
   └── _uiState.value = copy(purchase = purchase)  ← UI muestra compra inmediatamente

3. LocationHelper.getLocation()
   └── suspendCancellableCoroutine { cont ->
         fusedLocationClient.getCurrentLocation { location ->
           cont.resume(Pair(location.latitude, location.longitude))
         }
       }

4. _uiState.value = copy(isLoadingPrices = true)

5. purchase.products.map { product ->
     async {  ← paralelismo: todas las búsquedas a la vez
       product.name to repository.searchProductPrices(lat, lng, product.name)
     }
   }.awaitAll()

6. Scoring por relevancia y popularidad de sucursales
   └── scoreDto(): matchRatio × 0.70 + normSuc × 0.15 + firstBonus × 0.15
   └── Selecciona el producto más representativo de la respuesta

7. _uiState.value = copy(apiPriceMap = result, isLoadingPrices = false)

8. PurchaseDetailScreen muestra PriceComparisonBadge:
   └── precio < API × 0.90  →  "Buen precio" (verde)
   └── precio < API × 1.10  →  "Precio justo" (amarillo)
   └── precio >= API × 1.10 →  "Precio alto"  (rojo)
```

---

## 5. Flujo concreto: Intent de compartir compra

```
1. Usuario toca el ícono Share en PurchaseDetailScreen

2. PurchaseDetailScreen.kt (línea ~128)
   └── val intent = Intent(Intent.ACTION_SEND).apply {
         type = "text/plain"
         putExtra(Intent.EXTRA_SUBJECT, shareSubject)
         putExtra(Intent.EXTRA_TEXT, shareBody)
       }
   └── context.startActivity(Intent.createChooser(intent, shareSubject))

3. Android muestra el chooser con todas las apps disponibles
   └── WhatsApp, Gmail, Telegram, etc.
   └── La app no necesita saber qué app elige el usuario

Por qué es un "Intent implícito":
   No especificamos qué app va a manejar el Intent.
   Android resuelve la app en tiempo de ejecución según el tipo MIME y el ACTION.
```

---

## 6. Autenticación: PBKDF2WithHmacSHA256

```
REGISTRO:
   password → SecureRandom.nextBytes(16) → salt
   PBEKeySpec(password, salt, 65_536, 256)
   SecretKeyFactory("PBKDF2WithHmacSHA256").generateSecret(spec).encoded → hash
   UserEntity.password = Base64(salt) + ":" + Base64(hash)

LOGIN:
   password ingresado + stored = "saltBase64:hashBase64"
   Separar salt del hash almacenado
   Re-derivar hash con el mismo salt y las mismas iteraciones
   MessageDigest.isEqual(expectedHash, actualHash)  ← comparación segura (tiempo constante)
```

**Por qué 65.536 iteraciones:** hace que cada intento de cracking tome ~100ms en hardware moderno. Un atacante con 10 millones de contraseñas por segundo tardaría años en un ataque de diccionario.

---

## 7. DataStore: persistencia de sesión y preferencias

DataStore reemplaza a SharedPreferences con soporte nativo de coroutinas y Flow.

```kotlin
// SessionManager.kt — lo que se persiste entre reinicios:
IS_LOGGED_IN    : Boolean   // ¿hay sesión activa?
EMAIL           : String    // email del usuario logueado
MONTHLY_BUDGET  : Double    // presupuesto mensual configurado
MONTHLY_INCOME  : Double    // ingreso mensual configurado
SAVINGS_%       : Float     // porcentaje de ahorro (0-100)
SAVINGS_METHOD  : String    // "Percentage" | "Fixed Amount"
SAVINGS_FREQ    : String    // "Monthly" | "Weekly" | "Yearly"
DARK_MODE       : Boolean   // tema oscuro
BIOMETRIC       : Boolean   // autenticación biométrica
```

Cada preferencia es un `Flow<T>` → cualquier Composable puede observarla y reaccionar automáticamente cuando cambia.

---

## 8. Room: entidades y relaciones

```
purchases ──────────────────── purchase_items ──── products
   id (PK)                        id (PK, auto)       id (PK)
   store_name                     purchase_id (FK)     name
   amount                         product_id (FK)      code (UNIQUE)
   category_id (FK nullable)      quantity             description
   timestamp (epoch ms)           price
   latitude
   longitude
   ticket_image_path

categories                      users                  accounts
   id (PK)                        id (PK)                id (PK)
   name (UNIQUE)                  name                   name
                                  lastname               type
fixed_expenses                   email (UNIQUE)         last_four
   id (PK)                        password (PBKDF2)     is_primary
   name                           avatar_url
   amount
   category
   frequency
```

**Migrations:** 7 versiones progresivas. En DEBUG usa `fallbackToDestructiveMigration` para facilitar el desarrollo. En producción, todas las migrations están implementadas.

---

## 9. Preguntas del profesor y respuestas cortas

**¿Qué es MVVM?**
"Separación en tres capas: View (Compose, solo UI), ViewModel (lógica, StateFlow), Model (Room, Retrofit, DataStore). La View nunca toca datos directamente."

**¿Por qué ViewModel?**
"Sobrevive a la rotación de pantalla. Los Composables se destruyen y recrean al rotar — el ViewModel no. Sin ViewModel, perderíamos el formulario de nueva compra al rotar el teléfono."

**¿Qué es StateFlow?**
"Un Flow con estado: siempre tiene un valor actual, emite al cambiar, y los nuevos suscriptores reciben el último valor inmediatamente (replay = 1). Se consume en Compose con `collectAsStateWithLifecycle()`."

**¿Por qué Room debe ser Single Source of Truth?**
"Porque la UI solo observa un Flow de Room. Si usáramos Retrofit directamente, la UI tendría que manejar errores de red, estados offline y sincronización. Con Room, todo eso está en el Repository — la UI es simple."

**¿Diferencia entre Room y Retrofit?**
"Retrofit es un cliente HTTP que parsea JSON con Gson. Room es un ORM sobre SQLite local. Retrofit trae datos de internet, Room los persiste en el dispositivo. El Repository los conecta."

**¿Qué son las coroutinas?**
"`viewModelScope.launch` inicia una coroutina en el hilo principal. Las operaciones de I/O (Room, Retrofit) se ejecutan en `Dispatchers.IO` para no bloquear la UI. Si el ViewModel se destruye, la coroutina se cancela automáticamente."

**¿Qué es @Transaction en Room?**
"Atomicidad: todos los inserts de una compra (1 en purchases + N en purchase_items) se ejecutan como una unidad. Si falla cualquiera, se hace rollback total. Sin @Transaction, podrían quedar datos inconsistentes."

**¿Para qué sirve DataStore?**
"Reemplaza SharedPreferences con soporte de coroutinas y Flow. Guardamos sesión, presupuesto y preferencias. Cualquier pantalla puede observar esos valores reactivamente — si cambia el presupuesto, HomeScreen se actualiza solo."

**¿Qué es un Intent implícito?**
"Un Intent sin destinatario específico. Android lo resuelve en tiempo de ejecución buscando apps que puedan manejarlo. En nuestro caso, `ACTION_SEND` + `text/plain` hace que Android muestre el chooser con WhatsApp, Gmail, etc."

---

## 10. Archivos clave para abrir en la defensa (orden sugerido)

1. `data/AppRepository.kt` — `purchasesFlow`, `refreshApiProductsIfEmpty()`, `savePurchase()`
2. `viewmodel/HomeViewModel.kt` — `init` con las coroutinas y `collect`
3. `viewmodel/PurchaseViewModel.kt` — `savePurchase()` con manejo de estados
4. `data/dao/PurchaseDao.kt` — `@Transaction`, `getAllPurchasesWithItems()`
5. `data/ProWalletDatabase.kt` — version 7, 6 migrations progresivas
6. `ui/screens/PurchaseDetailScreen.kt:128` — Intent de compartir
7. `util/SessionManager.kt` — DataStore con Flows reactivos
8. `ui/navigation/NavGraph.kt` — sealed class Screen, popUpTo correcto
