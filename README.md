# SUPER AHORRO

Aplicación Android de gestión de gastos personales. Primera entrega visual con datos mockeados y navegación completa.

## Stack tecnológico

- **Kotlin** + **Jetpack Compose**
- **Navigation Compose** — navegación declarativa entre pantallas
- **ViewModel** + **StateFlow** — arquitectura MVVM
- **Material 3** — sistema de diseño

## Arquitectura

```
com.undef.prowallet
├── data/           # MockRepository con datos de prueba
├── domain/         # Modelos (User, Purchase, Product)
├── ui/
│   ├── components/ # Componentes reutilizables (PrimaryButton, CustomTextField, etc.)
│   ├── navigation/ # NavGraph + rutas tipadas
│   ├── screens/    # 11 pantallas completas
│   └── theme/      # Color.kt · Type.kt · Theme.kt
└── viewmodel/      # AuthViewModel · HomeViewModel · PurchaseViewModel
```

## Pantallas

| Pantalla | Ruta |
|---|---|
| Splash | `splash` |
| Login | `login` |
| Registro | `register` |
| Éxito registro | `register_success` |
| Home (Dashboard) | `home` |
| Nueva compra | `new_purchase` |
| Detalle compra | `purchase_detail/{id}` |
| Historial | `history` |
| Estadísticas | `analytics` |
| Perfil | `profile` |
| Configuración | `settings` |

## Sistema de colores

| Token | Hex |
|---|---|
| Primary | `#A8DADC` |
| Secondary | `#457B9D` |
| Tertiary | `#F1FAEE` |
| Neutral | `#757777` |

## Cómo abrir el proyecto

1. Clonar / descomprimir el proyecto
2. Abrir **Android Studio** → `Open` → seleccionar la carpeta `ProWallet/`
3. Esperar sync de Gradle (requiere internet para descargar dependencias)
4. Correr en emulador o dispositivo físico con Android 8.0+ (API 26)

## Preparado para (próximas entregas)

- **Room** — persistencia local (estructura de paquete `data/` lista)
- **DataStore** — preferencias de usuario
- **Retrofit** — networking con backend real

## Notas

- Fuente **Plus Jakarta Sans**: actualmente usa `FontFamily.SansSerif` del sistema. Para activar la fuente original, descargar los `.ttf` de Google Fonts, colocarlos en `app/src/main/res/font/` y actualizar `Type.kt`.
- Todos los datos son mock — sin persistencia ni backend en esta entrega.
