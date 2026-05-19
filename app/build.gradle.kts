import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.compose.compiler)
}

// Lee las API Keys desde local.properties (no commiteado).
val props = Properties()
val file = rootProject.file("local.properties")
if (file.exists()) props.load(file.inputStream())

val geminiApiKey: String = (props.getProperty("GEMINI_API_KEY") ?: "").trim()
val deepseekApiKey: String = (props.getProperty("DEEPSEEK_API_KEY") ?: "").trim()

android {
    namespace = "com.app.administradorfarmadon"
    compileSdk = 36 // Simplificado para evitar el error de release(36)

    defaultConfig {
        applicationId = "com.app.administradorfarmadon"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
        buildConfigField("String", "DEEPSEEK_API_KEY", "\"$deepseekApiKey\"")
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
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        viewBinding=true
        compose = true
        buildConfig = true
    }

}

dependencies {
    implementation(libs.androidx.animation)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    // Importa el BoM primero (esto es obligatorio)
    implementation(platform(libs.firebase.bom))

    // Librerías base
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(platform("androidx.compose:compose-bom:2026.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Firebase (ahora tomarán la versión del BoM automáticamente)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.auth.ktx)

    // Scanner y Cámara
    implementation(libs.barcode.scanning)
    implementation(libs.text.recognition)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Otras librerías
    implementation("androidx.core:core-splashscreen:1.2.0")
    implementation("com.hbb20:ccp:2.7.3")
    implementation("com.github.bumptech.glide:glide:5.0.5")
    implementation("androidx.gridlayout:gridlayout:1.1.0")
    implementation("androidx.palette:palette:1.0.0")
    implementation("org.jsoup:jsoup:1.18.1")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation("androidx.compose.ui:ui-tooling")






}
