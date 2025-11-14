import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

val localProperties = Properties().apply {
    load(rootProject.file("local.properties").inputStream())
}

val baseUrl = localProperties.getProperty("base_url") ?: ""
val oAuthClientId = localProperties.getProperty("oauth_client_id") ?: ""
val oAuthRedirectUri = localProperties.getProperty("redirect-uris.app") ?: ""
val ipAddress = localProperties.getProperty("ip_address") ?: ""
val wsHost = localProperties.getProperty("ws_host") ?: ""
val naverClientId = localProperties.getProperty("naver_client_id") ?: ""
val naverClientSecret = localProperties.getProperty("naver_client_secret") ?: ""

android {
    namespace = "com.imhungry.sillok"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.imhungry.sillok"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.imhungry.sillok.HiltTestRunner"

        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        buildConfigField("String", "OAUTH_CLIENT_ID", "\"$oAuthClientId\"")
        buildConfigField("String", "OAUTH_REDIRECT_URI", "\"$oAuthRedirectUri\"")
        buildConfigField("String", "IP_ADDRESS", "\"$ipAddress\"")
        buildConfigField("String", "WS_HOST", "\"$wsHost\"")
        buildConfigField("String", "NAVER_CLIENT_ID", "\"$naverClientId\"")
        buildConfigField("String", "NAVER_CLIENT_SECRET", "\"$naverClientSecret\"")
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

    packaging {
        resources {
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
            excludes += "/META-INF/NOTICE.md"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/NOTICE.txt"
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    
    // ViewModel & LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // Dependency Injection - Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    implementation(libs.androidx.foundation.layout)
    ksp("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // JavaPoet for KSP compatibility
    implementation("com.squareup:javapoet:1.13.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Retrofit for Network
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Coil for Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("io.coil-kt:coil-gif:2.5.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ndk")

    // Pager
    implementation("com.google.accompanist:accompanist-pager-indicators:0.28.0")
    implementation("com.google.accompanist:accompanist-pager:0.28.0")
    
    // Paging 3
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")
    implementation("androidx.paging:paging-compose:3.2.1")

    // Exoplayer
    implementation("androidx.media3:media3-exoplayer:1.2.0")
    implementation("androidx.media3:media3-ui:1.2.0")

    implementation("androidx.browser:browser:1.7.0")

    // SSE
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-sse:4.12.0")

    // Testing
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    
    // Hilt testing for unit tests
    testImplementation("com.google.dagger:hilt-android-testing:2.48")
    kspTest("com.google.dagger:hilt-android-compiler:2.48")
    
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    
    // AndroidX Test runner & rules
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    
    // Hilt testing for instrumented tests
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.48")
    kspAndroidTest("com.google.dagger:hilt-android-compiler:2.48")
    
    // Coroutines test utilities for instrumented tests
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    
    // MockK for instrumented tests
    androidTestImplementation("io.mockk:mockk-android:1.13.8")
    
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    
}