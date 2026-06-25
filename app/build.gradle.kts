plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.undef.prowallet"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.undef.prowallet"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        // lintDebug crashea por una incompatibilidad de versión de kotlinx-metadata-jvm:
        // el Kotlin compiler genera metadata 2.1.0 pero la lib que usan los detectores de
        // Compose lint para leer @Composable solo soporta hasta 2.0.0 ("Provided Metadata
        // instance has version 2.1.0, while maximum supported version is 2.0.0"). Es un bug
        // de toolchain, no del código de la app, y afecta a distintos detectores
        // (ComposableStateFlowValueDetector, ComposableCoroutineCreationDetector, etc.)
        // según qué archivo se analice. Deshabilitar un check puntual no soluciona la causa
        // raíz, solo evita que ESE detector específico crashee. Con estos dos deshabilitados
        // `lintDebug` corre limpio sobre el código actual del proyecto.
        disable += "StateFlowValueCalledInComposition"
        disable += "CoroutineCreationDuringComposition"
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.coil.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.androidx.biometric)
    implementation(libs.play.services.location)
    implementation(libs.mlkit.text.recognition)
}
