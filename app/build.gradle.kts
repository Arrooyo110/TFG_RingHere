plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.0.21-1.0.27"
}

android {
    namespace = "com.cdm.tfg_ringhere"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.cdm.tfg_ringhere"
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
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // --- LIBRERÍAS DE ROOM (BASE DE DATOS) ---
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version") // Para usar Corrutinas y Flow con Room
    ksp("androidx.room:room-compiler:$room_version")

    // --- LIBRERÍA DE CORRUTINAS (Para tareas en segundo plano) ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    // --- LIBRERÍA PARA USAR VIEWMODEL EN COMPOSE ---
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    // Retrofit para peticiones de red
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Convertidor de JSON (Gson)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // Navegación en Jetpack Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")
    // Iconos extendidos de Material Design
    implementation("androidx.compose.material:material-icons-extended")
    // Google Maps para Jetpack Compose
    implementation("com.google.maps.android:maps-compose:4.3.3")
    // Librería oficial de Google Play Services para Mapas
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    // Librería oficial de Google para ubicación y Geofencing
    implementation("com.google.android.gms:play-services-location:21.1.0")
}