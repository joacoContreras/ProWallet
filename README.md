# ProWallet

Aplicación Android de gestión de gastos personales desarrollada como proyecto universitario para la materia de Desarrollo de Aplicaciones Móviles — **UNDEF**.

## Integrantes

| Nombre | GitHub |
|---|---|
| Joaquin Contreras | [@joacoContreras](https://github.com/joacoContreras) |
| Martin Gonzalez | [@mgonzalez309-dev](https://github.com/mgonzalez309-dev) |

---

## Funcionalidades principales

- **Registro e inicio de sesión** con persistencia de sesión entre reinicios (DataStore) y contraseñas hasheadas con PBKDF2
- **Registro de compras** con tienda, fecha, hora, categoría y lista de productos — guardadas en Room
- **Historial completo** de compras con total acumulado
- **Dashboard** con gasto mensual, presupuesto restante y barra de progreso en tiempo real
- **Comparación de precios** en el detalle de cada compra, consultando precios de referencia desde **Precios Claros** (API oficial del gobierno argentino) según la ubicación GPS del dispositivo — muestra rango mínimo–máximo por producto e indica si el precio pagado fue bueno, justo o alto
- **Estadísticas mensuales**: gasto total, ticket promedio, gráfico de tendencia de 6 meses y productos más comprados
- **Top tiendas**: ranking de comercios por gasto total
- **Ubicación de compra** guardada automáticamente al registrar una compra (GPS vía FusedLocationProviderClient)
- **Compartir compra** vía Intent nativo de Android (WhatsApp, Gmail, etc.)
- **Gestión de categorías** con AlertDialog de CRUD (agregar, editar, eliminar)
- **Presupuesto mensual** configurable, persistido en DataStore
- **Recuperación de contraseña** con flujo de verificación de código de 6 dígitos
- **Soporte multiidioma** — Español e Inglés

---

## Descargar APK

**[⬇ Descargar ProWallet v1.0.0](https://github.com/joacoContreras/ProWallet/releases/tag/v1.0.0)**

> Requiere Android 8.0+ (API 26). Activar *"Instalar desde fuentes desconocidas"* antes de instalar.

---

## Capturas de pantalla

> **Nota:** Las capturas se agregarán antes de la entrega final. A continuación se muestra la estructura de cada flujo principal.

### Flujo de autenticación
<!-- Reemplazar con capturas reales: Splash → Login → Registro → Home -->
| Splash | Login | Registro | Home |
|---|---|---|---|
| _próximamente_ | _próximamente_ | _próximamente_ | _próximamente_ |

### Dashboard y compras
<!-- Reemplazar con capturas reales: Home → Nueva Compra → Detalle → Historial -->
| Dashboard | Nueva Compra | Detalle | Historial |
|---|---|---|---|
| _próximamente_ | _próximamente_ | _próximamente_ | _próximamente_ |

### Estadísticas y perfil
<!-- Reemplazar con capturas reales: Analíticas → Top Tiendas → Perfil → Configuración -->
| Analíticas | Top Tiendas | Perfil | Configuración |
|---|---|---|---|
| _próximamente_ | _próximamente_ | _próximamente_ | _próximamente_ |

---

## Stack tecnológico

- **Kotlin** + **Jetpack Compose** (Material 3)
- **Navigation Compose** — navegación declarativa entre 25 pantallas
- **ViewModel** + **StateFlow** — arquitectura MVVM, un ViewModel por pantalla
- **Room** — base de datos local (usuarios, compras, productos, categorías)
- **DataStore Preferences** — persistencia de sesión entre reinicios
- **Coroutines** + **Flow** — operaciones asíncronas con `viewModelScope`
- **Retrofit** + **Gson** — dos clientes independientes: npoint.io (catálogo) y Precios Claros / CloudFront (precios de referencia por ubicación)
- **FusedLocationProviderClient** — ubicación GPS con puente `suspendCancellableCoroutine` para coroutines
- **compileSdk 35 / minSdk 26** (Android 8.0+)

---

## Arquitectura

```
com.undef.prowallet
├── data/
│   ├── dao/                        ← UserDao, PurchaseDao, ProductDao, PurchasedItemDao, CategoryDao
│   ├── remote/
│   │   ├── ProductDto.kt               ← DTOs: npoint.io + PreciosClarosProductDto / PreciosClarosResponse
│   │   ├── ProductApiService.kt        ← interfaz Retrofit (npoint.io)
│   │   ├── RetrofitClient.kt           ← cliente npoint.io (singleton)
│   │   ├── PreciosClarosApiService.kt  ← interfaz Retrofit (Precios Claros)
│   │   └── PreciosClarosClient.kt      ← cliente Precios Claros (singleton, base URL CloudFront)
│   ├── AppRepository.kt            ← fuente de verdad: Room + Retrofit
│   ├── ProWalletDatabase.kt        ← Room database singleton
│   ├── UserEntity.kt / PurchaseEntity.kt / ProductEntity.kt
│   ├── PurchasedItemEntity.kt / CategoryEntity.kt
│   ├── PurchaseWithItems.kt / PurchasedItemWithProduct.kt / StoreTotal.kt
├── domain/
│   └── models.kt                   ← User, Product, Purchase
├── ui/
│   ├── components/                 ← PrimaryButton, CustomTextField, TopBar, etc.
│   ├── navigation/                 ← NavGraph + sealed class Screen
│   ├── screens/                    ← una pantalla por archivo (25 pantallas)
│   └── theme/                      ← Color.kt · Type.kt · Theme.kt
├── util/
│   ├── LocaleHelper.kt             ← i18n (ES / EN)
│   ├── LocationHelper.kt           ← FusedLocationProviderClient → suspendCancellableCoroutine → Pair<Double,Double>?
│   ├── SessionManager.kt          ← DataStore: sesión persistida entre reinicios
│   └── DateUtils.kt               ← extensiones de Purchase: isCurrentMonth(), isInMonth()
└── viewmodel/
    ├── AuthViewModel.kt            ← Room + SHA-256 + DataStore
    ├── HomeViewModel.kt            ← AppRepository + presupuesto mensual (DataStore)
    ├── PurchaseViewModel.kt        ← formulario de compra + persistencia Room
    ├── PurchaseDetailViewModel.kt  ← detalle + comparación de precios Precios Claros (ubicación + async/awaitAll)
    ├── AnalyticsViewModel.kt
    ├── HistoryViewModel.kt
    └── TopStoresViewModel.kt
```

---

## Pantallas

| Pantalla | Ruta |
|---|---|
| Splash | `splash` |
| Login | `login` |
| Registro | `register` |
| Éxito registro | `register_success` |
| Home (Dashboard) | `home` |
| Nueva compra | `new_purchase` |
| Éxito compra | `purchase_success` |
| Detalle compra | `purchase_detail/{purchaseId}` |
| Historial | `history` |
| Estadísticas | `analytics` |
| Inflación personal | `personal_inflation` |
| Top tiendas | `top_stores` |
| Detalle tienda | `store_detail/{storeName}` |
| Perfil | `profile` |
| Configuración | `settings` |
| Gestión de cuentas | `manage_accounts` |
| Presupuesto mensual | `monthly_setup` |
| Ahorro automático | `auto_savings` |
| Chat IA | `chat_ai` |
| Notificaciones | `notifications` |
| Soporte | `contact_support` |
| Recuperar contraseña | `forgot_password` |
| Verificar código | `verify_code` |
| Nueva contraseña | `update_password` |
| Contraseña actualizada | `update_password_success` |

---

## Sistema de colores

| Token | Hex |
|---|---|
| Primary | `#A8DADC` |
| Secondary | `#457B9D` |
| Tertiary | `#F1FAEE` |
| Neutral | `#757777` |

---

## Cómo abrir el proyecto

1. Clonar el repositorio
2. Abrir **Android Studio** → `Open` → seleccionar la carpeta raíz
3. Esperar el sync de Gradle (requiere internet)
4. Correr en emulador o dispositivo con Android 8.0+ (API 26)

---

## Internacionalización

La app soporta **Español** e **Inglés**. El idioma se puede cambiar desde Configuración → Idioma. Todos los textos visibles están en `res/values/strings.xml` y `res/values-es/strings.xml`.
