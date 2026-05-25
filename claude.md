# ProWallet — Android App

Aplicación Android de gestión de gastos personales, desarrollada como proyecto universitario para la materia de desarrollo móvil (UNDEF).

## Estado actual: rama `2nd_delivery`

La **1ra entrega** está completa: UI, navegación y datos mockeados.  
La **2da entrega** está prácticamente completa — Room + DataStore + Retrofit operativos, CRUD de categorías, comparación de precios API, filtrado mensual centralizado, hashing PBKDF2.

## Uso de git
No hacer commits propios. Dejar que el usuario haga los commits.

### Requisitos de la 2da entrega
- ✅ Persistencia local de sesión o preferencias (DataStore)
- ✅ Base de datos local para compras y productos (Room)
- ✅ Operaciones con corrutinas
- ✅ Networking (Retrofit + Gson)
- ✅ Menús y diálogos (AlertDialogs de CRUD de categorías, time picker, date picker)
- ✅ Carga real de datos
- ⬜ Al menos una funcionalidad con Intents (ej: compartir compra)

## Stack tecnológico

- **Kotlin** + **Jetpack Compose** (Material 3)
- **Navigation Compose** — navegación declarativa
- **ViewModel** + **StateFlow** — patrón MVVM
- **Room** — base de datos local (v4): usuarios, compras, productos, categorías
- **DataStore Preferences** — sesión persistida entre reinicios + presupuesto mensual
- **Retrofit** + **Gson** — consumo de API REST (precios de referencia de productos)
- **Coroutines** + **Flow** — operaciones asíncronas con `viewModelScope`
- **compileSdk 35 / minSdk 26** (Android 8.0+)
- **Coil** — carga de imágenes
- **Plus Jakarta Sans** — fuente tipográfica

## Arquitectura

```
com.undef.prowallet
├── data/
│   ├── dao/
│   │   ├── UserDao.kt
│   │   ├── PurchaseDao.kt
│   │   ├── ProductDao.kt
│   │   ├── PurchasedItemDao.kt
│   │   └── CategoryDao.kt
│   ├── remote/
│   │   ├── ProductDto.kt           ← DTOs (ProductDto, ProductsResponse)
│   │   ├── ProductApiService.kt    ← interfaz Retrofit (@GET npoint.io)
│   │   └── RetrofitClient.kt       ← singleton Retrofit con GsonConverterFactory
│   ├── AppRepository.kt            ← fuente de verdad: Room + Retrofit
│   ├── ProWalletDatabase.kt        ← Room singleton, version=4, fallbackToDestructiveMigration
│   ├── UserEntity.kt
│   ├── PurchaseEntity.kt
│   ├── ProductEntity.kt            ← UNIQUE index en code
│   ├── PurchasedItemEntity.kt      ← PK autoincremental
│   ├── CategoryEntity.kt           ← UNIQUE index en name
│   ├── PurchaseWithItems.kt
│   ├── PurchasedItemWithProduct.kt
│   └── StoreTotal.kt
├── domain/
│   └── models.kt                   ← User, Product, Purchase
├── ui/
│   ├── components/
│   │   └── Components.kt           ← PrimaryButton, CustomTextField, TopBar, ProductItem, etc.
│   ├── navigation/
│   │   └── NavGraph.kt             ← sealed class Screen + AppNavGraph
│   ├── screens/                    ← una pantalla por archivo (25 pantallas)
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── util/
│   ├── LocaleHelper.kt             ← i18n: API 33+ LocaleManager, fallback AppCompatDelegate
│   ├── SessionManager.kt          ← DataStore: isLoggedIn, email, monthlyBudget
│   └── DateUtils.kt               ← extensiones de Purchase: parsedCalendar(), isInMonth(), isCurrentMonth()
└── viewmodel/
    ├── AuthViewModel.kt
    ├── HomeViewModel.kt
    ├── PurchaseViewModel.kt
    ├── PurchaseDetailViewModel.kt
    ├── AnalyticsViewModel.kt
    ├── HistoryViewModel.kt
    └── TopStoresViewModel.kt
```

## Pantallas y rutas

| Archivo | Ruta | Descripción |
|---|---|---|
| SplashScreen | `splash` | Pantalla de inicio |
| LoginScreen | `login` | Login con email/password |
| RegisterScreen | `register` | Registro de usuario |
| RegisterSuccessScreen | `register_success` | Confirmación de registro |
| HomeScreen | `home` | Dashboard principal |
| NewPurchaseScreen | `new_purchase` | Formulario de nueva compra |
| PurchaseSuccessScreen | `purchase_success` | Confirmación de compra guardada |
| PurchaseDetailScreen | `purchase_detail/{purchaseId}` | Detalle + comparación de precios API |
| HistoryScreen | `history` | Historial de compras |
| AnalyticsScreen | `analytics` | Estadísticas y gráficos |
| PersonalInflationScreen | `personal_inflation` | Inflación personal |
| TopStoresScreen | `top_stores` | Tiendas más frecuentes |
| StoreDetailScreen | `store_detail/{storeName}` | Detalle de tienda |
| ProfileScreen | `profile` | Perfil de usuario |
| SettingsScreen | `settings` | Configuración |
| ManageAccountsScreen | `manage_accounts` | Gestión de cuentas |
| MonthlySetupScreen | `monthly_setup` | Presupuesto mensual |
| AutoSavingsScreen | `auto_savings` | Ahorro automático |
| ChatAiScreen | `chat_ai` | Chat con IA |
| NotificationsScreen | `notifications` | Notificaciones |
| ContactSupportScreen | `contact_support` | Soporte |
| ForgotPasswordScreen | `forgot_password` | Recuperar contraseña |
| VerifyCodeScreen | `verify_code` | Verificar código (6 dígitos) |
| UpdatePasswordScreen | `update_password` | Nueva contraseña |
| UpdatePasswordSuccessScreen | `update_password_success` | Confirmación cambio contraseña |

## Modelos de dominio (`domain/models.kt`)

```kotlin
data class User(id, fullName, email, avatarUrl)
data class Product(id, code, name, description, price: Double)
data class Purchase(id, storeName, date, time, totalAmount: Double, category, products, ticketImageUri, timestampMs: Long = 0L)
```

`timestampMs` viene directo de `PurchaseEntity.timestamp` (epoch ms). Se usa en `DateUtils` para filtrado mensual sin re-parsear el string de fecha.

## ViewModels

### AuthViewModel
- Estado: `AuthUiState` (isLoading, isLoggedIn, user, error, registrationSuccess, resetEmailSent, codeVerified, passwordUpdated)
- Registro e inicio de sesión persisten en Room (`UserDao`) con hash **PBKDF2WithHmacSHA256** + salt aleatorio
- `init` valida sesión contra Room al arrancar — si el usuario no existe en DB, limpia DataStore
- Sesión guardada en `SessionManager` (DataStore): `isLoggedIn`, `userEmail`
- `verifyCode()` valida 6 dígitos; `updatePassword()` tiene guard `codeVerified`

### HomeViewModel
- Estado: `HomeUiState` (userName, totalMonthlySpend, monthlyBudget, remaining, budgetProgress, budgetPercent, recentPurchases, …)
- Colecta `AppRepository.purchasesFlow` (Flow reactivo desde Room); filtra por mes actual via `isCurrentMonth()` (usa `timestampMs`)
- Presupuesto mensual persistido en `SessionManager.monthlyBudget` (DataStore)
- `budgetProgress` y `budgetPercent` son pre-calculados en el ViewModel (safe division)

### PurchaseViewModel
- Estado: `PurchaseUiState` (campos del formulario + `categories: List<CategoryEntity>` + lista de productos + isSaving + savedSuccess + saveError + validationError)
- `totalAmount` se calcula automáticamente como `products.sumOf { it.price }` — no hay input manual
- `init` colecta `repository.categoriesFlow` para mantener la lista de categorías actualizada en tiempo real
- `savePurchase()` valida `storeName` y emite `validationError = true` si está vacío; persiste en Room; try/catch/finally garantiza reset de `isSaving`
- `resetForm()` preserva `categories` (no resetea el Flow de Room); se llama via `LaunchedEffect(Unit)` en `NewPurchaseScreen` para garantizar formulario limpio en cada entrada
- CRUD de categorías: `addCategory()`, `deleteCategory()`, `updateCategoryName()` delegados a `AppRepository`

### PurchaseDetailViewModel
- Estado: `PurchaseDetailUiState` (isLoading, purchase, apiPriceMap)
- `loadPurchase()` lanza dos coroutines independientes: una para Room, otra para la API
- Si la API falla o devuelve `apiPrice <= 0`, `apiPriceMap` queda vacío y no se muestra badge de comparación

## AppRepository
- Única fuente de verdad para datos de compras/productos/categorías
- `purchasesFlow`: @Transaction query (getAllPurchasesWithItems) + 2 bulk maps — sin N+1; cada `Purchase` incluye `timestampMs` del DB
- `categoriesFlow`: Flow reactivo de `categoryDao.getAllCategories()` — alimenta el dropdown de `NewPurchaseScreen` en tiempo real
- `savePurchase()`: upsert de categoría + producto por nombre/código dentro de `db.withTransaction`; `totalAmount` viene ya calculado del ViewModel
- `addCategory(name)`: insert con `OnConflictStrategy.IGNORE`, devuelve `false` si ya existe
- `deleteCategory(id)` / `updateCategory(id, newName)`: delegados a `CategoryDao`
- `getApiProducts()`: llama a `RetrofitClient.productApiService.getProducts().productos`
- Categorías y productos tienen constraint UNIQUE (índice) + `OnConflictStrategy.IGNORE` + fallback lookup para manejar races
- `seedDefaultCategories()`: inserta Groceries, Transport, Dining, Coffee, Other si no existen

## Sistema de colores (Color.kt)

| Token | Hex |
|---|---|
| Primary | `#A8DADC` |
| Secondary | `#457B9D` |
| Tertiary | `#F1FAEE` |
| Neutral | `#757777` |

## Internacionalización
`LocaleHelper` maneja cambio de idioma: API 33+ usa `LocaleManager`, fallback usa `AppCompatDelegate`. Las strings deben ir en `res/values/strings.xml` y `res/values-es/strings.xml`.

## Normas del proyecto (académicas)
- Package: `com.undef.prowallet`
- Arquitectura MVVM obligatoria
- Jetpack Compose (no XML layouts)
- Corrutinas para operaciones asíncronas
- Versionado en GitHub (rama activa: `2nd_delivery`)
- El README necesita capturas/GIF reales y el link al APK actualizado antes de la entrega final
