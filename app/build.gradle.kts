import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
}

val versionPropsFile = rootProject.file("version.properties")
val versionProps = Properties().apply {
    if (versionPropsFile.exists()) {
        FileInputStream(versionPropsFile).use { load(it) }
    } else {
        setProperty("BUILD_NUMBER", "1")
        setProperty("VERSION_NAME", "1.2.0")
        setProperty("COPYRIGHT_NOTICE", "Copyright of https://cdrivex4.github.io/ 2026.")
        FileOutputStream(versionPropsFile).use { store(it, "BrizSey Version Properties") }
    }
}

val currentBuildNumber = (versionProps.getProperty("BUILD_NUMBER") ?: "1").toInt()
val currentVersionName = versionProps.getProperty("VERSION_NAME") ?: "1.2.0"
val copyrightNotice = versionProps.getProperty("COPYRIGHT_NOTICE") ?: "Copyright of https://cdrivex4.github.io/ 2026."

android {
    namespace = "sc.meteo.seymeteo"
    compileSdk = 35

    defaultConfig {
        applicationId = "sc.meteo.seymeteo"
        minSdk = 26
        targetSdk = 35
        versionCode = currentBuildNumber
        versionName = currentVersionName

        buildConfigField("int", "BUILD_NUMBER", "$currentBuildNumber")
        buildConfigField("String", "COPYRIGHT_NOTICE", "\"$copyrightNotice\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

tasks.register("incrementBuildNumber") {
    doLast {
        val props = Properties()
        if (versionPropsFile.exists()) {
            FileInputStream(versionPropsFile).use { props.load(it) }
        }
        val cur = (props.getProperty("BUILD_NUMBER") ?: "0").toInt()
        val next = cur + 1
        props.setProperty("BUILD_NUMBER", next.toString())
        FileOutputStream(versionPropsFile).use { props.store(it, "BrizSey Version Properties") }
        println("BrizSey: Incremented build number from $cur to $next")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose & Material 3
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Networking & Serialization
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)

    // Image loading
    implementation(libs.coil.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Room (offline-first persistence)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore Preferences (settings & favourites metadata)
    implementation(libs.androidx.datastore.preferences)

    // WorkManager (background sync & alert polling)
    implementation(libs.androidx.work.runtime.ktx)

    // Location (GPS auto-detect)
    implementation(libs.play.services.location)
    implementation(libs.accompanist.permissions)

    // Glance (home screen widget)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
