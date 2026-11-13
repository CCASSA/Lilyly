plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.lilyly.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.lilyly.app"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        minSdk = 26
        targetSdk = 36
        versionCode = 32
        versionName = "0.32.0"
        manifestPlaceholders["appLabel"] = "Lilyly"
    }

    // CI fingerprints this exact file. AGP otherwise resolves its debug key using SDK-specific paths.
    providers.gradleProperty("previewKeyFile").orNull?.let { path ->
        signingConfigs.getByName("debug").storeFile = file(path)
    }

    buildTypes {
        create("preview") {
            initWith(getByName("debug"))
            val signerId = providers.gradleProperty("previewSigner").orNull
            require(signerId == null || signerId.matches(Regex("[0-9a-f]{24}"))) { "Invalid preview signing fingerprint" }
            applicationIdSuffix = if (signerId == null) ".preview" else ".preview.k" + signerId
            versionNameSuffix = "-preview"
            manifestPlaceholders["appLabel"] = if (signerId == null) "Lilyly Garden Preview" else "Lilyly Garden · Moon"
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
