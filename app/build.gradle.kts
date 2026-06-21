plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.grupo6.subastar"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.grupo6.subastar"
        minSdk = 26
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

        getByName("debug") {
            // Versión Local: Apunta al localhost de tu PC a través del emulador
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/\"")
            buildConfigField("String", "WS_URL", "\"ws://10.0.2.2:8080/v1/subastar-ws/websocket\"")
        }

        create("production") {
            initWith(getByName("debug"))
            // Versión Nube: Apunta a tu servidor dedicado en Azure (Cambia la IP por la tuya)
            buildConfigField("String", "BASE_URL", "\"http://20.226.11.103/\"")
            buildConfigField("String", "WS_URL", "\"ws://20.226.11.103/v1/subastar-ws/websocket\"")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Seguridad para encriptar SharedPreferences
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // STOMP para WebSockets
    implementation("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")
    // RxJava para manejar la suscripción reactiva a los mensajes del socket
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation(platform("com.google.firebase:firebase-bom:34.15.0"))
}