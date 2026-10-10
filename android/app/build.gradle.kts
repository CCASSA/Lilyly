plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val spotifyClient = providers.gradleProperty("spotifyClientId").orElse("").get()
val spotifyHost = providers.gradleProperty("spotifyRedirectHost").orElse("unconfigured.invalid").get().ifBlank { "unconfigured.invalid" }
require(spotifyClient.isEmpty() || spotifyClient.matches(Regex("[a-fA-F0-9]{32}")))
require(spotifyHost.matches(Regex("[a-zA-Z0-9.-]+")))

android {
    namespace = "com.lilyly.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.lilyly.app"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        minSdk = 26
        targetSdk = 36
        versionCode = 13
        versionName = "0.13.0-music-pages"
        manifestPlaceholders["appLabel"] = "Lilyly"
        manifestPlaceholders["spotifyHost"] = spotifyHost
        manifestPlaceholders["spotifyPath"] = "/spotify/callback"
        buildConfigField("String", "SPOTIFY_CLIENT_ID", "\"$spotifyClient\"")
        buildConfigField("String", "SPOTIFY_REDIRECT_HOST", "\"$spotifyHost\"")
        buildConfigField("String", "SPOTIFY_REDIRECT_PATH", "\"/spotify/callback\"")
    }

    buildTypes {
        create("preview") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".preview"
            manifestPlaceholders["spotifyPath"] = "/spotify/preview/callback"
            buildConfigField("String", "SPOTIFY_REDIRECT_PATH", "\"/spotify/preview/callback\"")
            versionNameSuffix = "-preview"
            manifestPlaceholders["appLabel"] = "Lilyly Garden Preview"
            matchingFallbacks += listOf("debug")
        }
        release {
            isMinifyEnabled = false
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
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
    val composeBom = platform("androidx.compose:compose-bom:2025.08.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("io.coil-kt.coil3:coil-compose:3.2.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
