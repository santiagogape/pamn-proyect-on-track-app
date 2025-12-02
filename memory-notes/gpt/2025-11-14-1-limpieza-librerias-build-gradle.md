Excelente movimiento 💪 — ahora que tu app es **100 % Jetpack Compose**, puedes hacer una limpieza profunda de dependencias y features pensadas para XML.
Esto no solo **reduce el tiempo de compilación**, sino que también **minimiza el tamaño del build** y las tareas innecesarias de Android Gradle Plugin.

A continuación te muestro cómo dejarlo limpio, paso a paso.

---

## 🧹 1️⃣ Quitar lo que ya **no necesitas**

### ❌ Dependencias heredadas del sistema de vistas XML

Elimínalas por completo del bloque `dependencies`:

```kotlin
implementation(libs.androidx.appcompat)
implementation(libs.material)
implementation(libs.androidx.constraintlayout)
implementation(libs.androidx.lifecycle.livedata.ktx)
implementation(libs.androidx.lifecycle.viewmodel.ktx)
implementation(libs.androidx.navigation.fragment.ktx)
implementation(libs.androidx.navigation.ui.ktx)
implementation(libs.androidx.lifecycle.runtime.ktx)
implementation(libs.androidx.core.ktx)   // ⚠️ se mantiene SOLO si lo usas fuera de Compose
```

> ⚠️ `androidx.core:core-ktx` puedes dejarla si usas extensiones básicas de Android (por ejemplo `Context.getSystemService<T>()` o `toUri()`), pero **no es obligatoria** para Compose.

---

### ❌ Desactiva `viewBinding`

Ya no inflas layouts XML.

```kotlin
buildFeatures {
    compose = true
    // viewBinding = true   <-- ❌ elimínalo
}
```

---

## ✅ 2️⃣ Mantén solo lo esencial para Compose

Tu `dependencies` quedaría así de limpio y moderno:

```kotlin
dependencies {
    // --- Compose BOM ---
    implementation(platform(libs.androidx.compose.bom))

    // --- Core Compose ---
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    // --- Testing / Tooling ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
```

---

## ⚙️ 3️⃣ Build features y opciones actualizadas

Tu bloque `android {}` modernizado:

```kotlin
android {
    namespace = "com.example.on_track_app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.on_track_app"
        minSdk = 24
        targetSdk = 36
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
        // viewBinding eliminado
    }
}
```

---

## 🧩 4️⃣ Ajustes menores opcionales

* Si ya no usas `androidx.core.ktx`, puedes borrar también esa entrada del `[libraries]` en tu `libs.versions.toml`.
* Las versiones `navigationFragmentKtx`, `navigationUiKtx`, `lifecycleLivedataKtx`, `lifecycleViewmodelKtx`, `constraintlayout`, y `material` también puedes quitarlas de `[versions]` porque no se usan más.
* Deja solo lo relacionado con Compose, Lifecycle Compose y Navigation Compose.

---

## ✅ 5️⃣ Resultado final

Tu build ahora:

* No compila dependencias XML ni clases de compatibilidad innecesarias.
* Solo incluye Material 3, Navigation Compose y Lifecycle Compose.
* Reduce drásticamente el tiempo de sincronización y el tamaño del APK.

---

¿Quieres que te deje el fragmento exacto del `libs.versions.toml` **ya depurado** también (solo con las versiones que Compose realmente necesita)?
