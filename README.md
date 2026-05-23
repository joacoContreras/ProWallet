# ProWallet

Aplicación Android de gestión de gastos personales desarrollada como proyecto universitario para la materia de Desarrollo de Aplicaciones Móviles — **UNDEF**.

## Integrantes

| Nombre | GitHub |
|---|---|
| Joaquin Contreras | [@joacoContreras](https://github.com/joacoContreras) |
| Martin Gonzalez | — |

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
- **Navigation Compose** — navegación declarativa entre 24 pantallas
- **ViewModel** + **StateFlow** — arquitectura MVVM, un ViewModel por pantalla
- **Coroutines** — operaciones asíncronas con `viewModelScope`
- **compileSdk 35 / minSdk 26** (Android 8.0+)

---

## Arquitectura

```
com.undef.prowallet
├── data/
│   ├── MockRepository.kt       ← datos de prueba (compras, usuario, budget)
│   └── PurchaseRepository.kt   ← fuente de verdad compartida (StateFlow)
├── domain/
│   └── models.kt               ← User, Product, Purchase
├── ui/
│   ├── components/             ← PrimaryButton, CustomTextField, TopBar, etc.
│   ├── navigation/             ← NavGraph + sealed class Screen
│   ├── screens/                ← una pantalla por archivo (24 pantallas)
│   └── theme/                  ← Color.kt · Type.kt · Theme.kt
├── util/
│   └── LocaleHelper.kt         ← i18n (ES / EN)
└── viewmodel/
    ├── AuthViewModel.kt
    ├── HomeViewModel.kt
    ├── PurchaseViewModel.kt
    ├── AnalyticsViewModel.kt
    ├── HistoryViewModel.kt
    ├── TopStoresViewModel.kt
    └── PurchaseDetailViewModel.kt
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
