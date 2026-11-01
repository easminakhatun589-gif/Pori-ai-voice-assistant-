import java.time.Duration

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.gms.google-services")
}

fun readDotEnvValue(key: String): String? {
    val candidates = listOf(rootProject.file(".env"), rootProject.file("../.env"), project.file(".env"))
    for (file in candidates) {
        if (file.exists()) {
            file.readLines().forEach { rawLine ->
                val line = rawLine.trim()
                if (line.isNotEmpty() && !line.startsWith("#") && line.contains("=")) {
                    val idx = line.indexOf('=')
                    val k = line.substring(0, idx).trim()
                    val v = line.substring(idx + 1).trim().removeSurrounding("\"").removeSurrounding("'")
                    if (
                        k == key &&
                        v.isNotBlank() &&
                        !v.startsWith("YOUR_") &&
                        v != "GEMINI_API_KEY_DEFAULT_VALUE" &&
                        v != "MY_GEMINI_API_KEY" &&
                        v != "PLACEHOLDER"
                    ) {
                        return v
                    }
                }
            }
        }
    }
    return null
}

fun configuredValue(name: String): String? =
    providers.gradleProperty(name).orNull
        ?: providers.environmentVariable(name).orNull
        ?: readDotEnvValue(name)

fun buildConfigString(value: String): String = "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

val releaseStorePath = configuredValue("JARVIS_RELEASE_STORE_FILE")
val releaseStorePassword = configuredValue("JARVIS_RELEASE_STORE_PASSWORD")
val releaseKeyAlias = configuredValue("JARVIS_RELEASE_KEY_ALIAS")
val releaseKeyPassword = configuredValue("JARVIS_RELEASE_KEY_PASSWORD")
val releaseCertSha256 = configuredValue("JARVIS_RELEASE_CERT_SHA256")?.replace(":", "")?.uppercase().orEmpty()
val developerName = configuredValue("JARVIS_DEVELOPER_NAME")?.ifBlank { null } ?: "MD Aslam"
val copyrightOwner = configuredValue("JARVIS_COPYRIGHT_OWNER")?.ifBlank { null } ?: "MD Aslam"
val verificationEndpoint = configuredValue("JARVIS_VERIFICATION_ENDPOINT").orEmpty()
val googleMapsApiKey = configuredValue("GOOGLE_MAPS_API_KEY").orEmpty()
val geminiApiKey = configuredValue("GEMINI_API_KEY").orEmpty()
val tavilyApiKey = configuredValue("TAVILY_API_KEY").orEmpty()
val tavilyProxyUrl = configuredValue("TAVILY_PROXY_URL").orEmpty()
val haviApiKey = configuredValue("HAVI_API_KEY").orEmpty()
val haviBaseUrl = configuredValue("HAVI_BASE_URL").orEmpty()
val releaseSigningReady = listOf(releaseStorePath, releaseStorePassword, releaseKeyAlias, releaseKeyPassword).all { !it.isNullOrBlank() }

android {
    namespace = "com.jarvis.assistant"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.jarvis.assistant"
        minSdk = 26
        targetSdk = 34
        versionCode = 143
        versionName = "1.8.7"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "DEVELOPER_NAME", buildConfigString(developerName.ifBlank { "Unverified developer" }))
        buildConfigField("String", "COPYRIGHT_OWNER", buildConfigString(copyrightOwner.ifBlank { "Unconfigured" }))
        buildConfigField("String", "PINNED_RELEASE_CERT_SHA256", buildConfigString(releaseCertSha256))
        buildConfigField("String", "VERIFICATION_ENDPOINT", buildConfigString(verificationEndpoint))
        buildConfigField("String", "GOOGLE_MAPS_API_KEY", buildConfigString(googleMapsApiKey))
        buildConfigField("String", "GEMINI_API_KEY", buildConfigString(geminiApiKey))
        buildConfigField("String", "TAVILY_API_KEY", buildConfigString(tavilyApiKey))
        buildConfigField("String", "TAVILY_PROXY_URL", buildConfigString(tavilyProxyUrl))
        buildConfigField("String", "HAVI_API_KEY", buildConfigString(haviApiKey))
        buildConfigField("String", "HAVI_BASE_URL", buildConfigString(haviBaseUrl))
    }

    signingConfigs {
        create("release") {
            if (releaseSigningReady) {
                storeFile = file(releaseStorePath!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf("-Xskip-metadata-version-check")
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    lint {
        // AndroidX Lifecycle 2.8.6's NullSafeMutableLiveData detector crashes under this project's Kotlin 2.0 UAST.
        // Keep all other lint checks enabled while that upstream detector is incompatible.
        disable += "NullSafeMutableLiveData"
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true
    }
}

tasks.withType<Test> {
    failFast = true
    timeout.set(Duration.ofMinutes(5))
    maxHeapSize = "2048m"
    forkEvery = 4
    testLogging {
        events("passed", "failed", "skipped")
    }
}

tasks.register("verifyReleaseSecurityInputs") {
    doLast {
        check(releaseSigningReady) {
            "Release signing is not configured. Set JARVIS_RELEASE_STORE_FILE, JARVIS_RELEASE_STORE_PASSWORD, JARVIS_RELEASE_KEY_ALIAS, and JARVIS_RELEASE_KEY_PASSWORD in the local environment or Gradle user properties."
        }
        check(releaseCertSha256.matches(Regex("[A-F0-9]{64}"))) {
            "Set JARVIS_RELEASE_CERT_SHA256 to the SHA-256 fingerprint of the release signing certificate."
        }
        check(developerName.isNotBlank() && copyrightOwner.isNotBlank()) {
            "Set JARVIS_DEVELOPER_NAME and JARVIS_COPYRIGHT_OWNER to the verified publisher identity."
        }
    }
}

tasks.matching { it.name == "packageRelease" }.configureEach {
    dependsOn("verifyReleaseSecurityInputs")
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.webkit:webkit:1.12.0")
    implementation("net.java.dev.jna:jna:5.13.0@aar")
    implementation("com.alphacephei:vosk-android:0.3.75@aar")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Security Crypto for EncryptedSharedPreferences
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // CameraX for Camera Analysis Screen
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")
    implementation("com.google.guava:guava:32.1.3-android")

    // ML Kit On-Device Text Recognition & Barcode Scanning
    implementation("com.google.mlkit:text-recognition:16.0.1")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    // WorkManager for Reliable Local Scheduling
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // Google Play Services Location (FusedLocationProviderClient for Part A Maps & Routes)
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Firebase (Firestore, Auth, AI Logic, Remote Config, Cloud Storage, Messaging, App Check) & Jetpack Credential Manager
    implementation(platform("com.google.firebase:firebase-bom:34.19.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-ai")
    implementation("com.google.firebase:firebase-config")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    implementation("com.google.firebase:firebase-appcheck-recaptcha")
    implementation("com.google.firebase:firebase-appcheck-debug")
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.16.1")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("com.google.truth:truth:1.4.4")
    testImplementation("org.jetbrains.kotlin:kotlin-test:2.0.20")
}


