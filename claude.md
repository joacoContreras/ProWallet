# ProWallet — Android App

Aplicación Android de gestión de gastos personales, desarrollada como proyecto universitario para la materia de desarrollo móvil (UNDEF).

## Estado actual: rama `2nd_delivery`

La **1ra entrega** está completa: UI, navegación y datos mockeados.  
La **2da entrega** requiere reemplazar los mocks por funcionalidad real.

## Uso de git
No hacer commits propios. Dejar que el usuario haga los commits.

### Requisitos de la 2da entrega
- Persistencia local de sesión o preferencias (DataStore)
- Base de datos local para compras y productos (Room)
- Operaciones con corrutinas
- Networking (Retrofit o similar)
- Menús y diálogos
- Carga real de datos
- Al menos una funcionalidad con Intents (ej: compartir compra)

## Stack tecnológico

- **Kotlin** + **Jetpack Compose** (Material 3)
- **Navigation Compose** — navegación declarativa
- **ViewModel** + **StateFlow** — patrón MVVM
- **compileSdk 35 / minSdk 26** (Android 8.0+)
- **Coil** — carga de imágenes
- Fuente prevista: Plus Jakarta Sans (actualmente usa `FontFamily.SansSerif`)

### Dependencias aún NO agregadas (necesarias para 2da entrega)
- Room (persistencia local)
- DataStore Preferences (sesión de usuario)
- Retrofit + Gson/Moshi (networking)
- Coroutines (ya disponible via lifecycle, pero sin uso real aún)

## Arquitectura

```
com.undef.prowallet
├── data/
│   └── MockRepository.kt       ← todos los datos son mock; reemplazar con Room + Retrofit
├── domain/
│   └── models.kt               ← User, Product, Purchase
├── ui/
│   ├── components/
│   │   └── Components.kt       ← PrimaryButton, CustomTextField y otros reutilizables
│   ├── navigation/
│   │   └── NavGraph.kt         ← sealed class Screen + AppNavGraph
│   ├── screens/                ← una pantalla por archivo (ver lista abajo)
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── util/
│   └── LocaleHelper.kt         ← internacionalización (API 33+ y AppCompat fallback)
└── viewmodel/
    ├── AuthViewModel.kt
    ├── HomeViewModel.kt
    └── PurchaseViewModel.kt
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
| PurchaseDetailScreen | `purchase_detail/{purchaseId}` | Detalle de una compra |
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
| VerifyCodeScreen | `verify_code` | Verificar código |
| UpdatePasswordScreen | `update_password` | Nueva contraseña |
| UpdatePasswordSuccessScreen | `update_password_success` | Confirmación cambio contraseña |

## Modelos de dominio (`domain/models.kt`)

```kotlin
data class User(id, fullName, email, avatarUrl)
data class Product(id, code, name, description, price: Double)
data class Purchase(id, storeName, date, time, totalAmount: Double, category, products, ticketImageUri)
```

## ViewModels

### AuthViewModel
- Estado: `AuthUiState` (isLoading, isLoggedIn, user, error, registrationSuccess, resetEmailSent, codeVerified, passwordUpdated)
- Todas las operaciones son mock (siempre exitosas). Necesita: Room para user local, DataStore para sesión persistente.

### HomeViewModel
- Estado: `HomeUiState` (userName, totalMonthlySpend, monthlyBudget, remaining, recentPurchases, allPurchases, topStores, monthlyTrend, analytics)
- Carga datos desde `MockRepository`. Necesita: Room DAO para leer/escribir compras reales.
- `deletePurchase(id)` opera solo en memoria (sin persistencia).

### PurchaseViewModel
- Estado: `PurchaseUiState` (campos del formulario de compra + lista de productos)
- `savePurchase()` solo pone `savedSuccess = true`, no persiste nada. Necesita: Room para guardar compras.
- `getPurchaseById(id)` busca en `MockRepository.mockPurchases` (estático).

## MockRepository (a reemplazar)
- `currentUser` — usuario hardcodeado
- `mockPurchases` — 6 compras de ejemplo
- `mockProducts` — 5 productos de ejemplo
- `monthlyBudget`, `totalMonthlySpend`, `topStores`, `monthlyTrend` — datos estáticos

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
- Readme: El enunciado pide explícitamente: "GIF o capturas de la app" y "APK compilada". 
  El README está bien estructurado pero no tiene capturas, ni GIF, ni link al APK, ni mención de los integrantes del grupo. 
  Linkear APK desde el README. Falta material visual (el profesor también insistió mucho en esto en clase).