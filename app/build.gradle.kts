import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("com.google.dagger.hilt.android")
    kotlin("plugin.serialization") version "2.0.0"
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.urvoices"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.urvoices"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        var algoliaProperties = Properties()
        file("../local.properties").inputStream().use { algoliaProperties.load(it) }
        buildConfigField("String", "ALGOLIA_APPLICATION_ID", "\"${algoliaProperties.getProperty("ALGOLIA_APPLICATION_ID")}\"")
        buildConfigField("String", "ALGOLIA_SEARCH_API_KEY", "\"${algoliaProperties.getProperty("ALGOLIA_SEARCH_API_KEY")}\"")
        buildConfigField("String", "ALGOLIA_INDEX_NAME", "\"${algoliaProperties.getProperty("ALGOLIA_INDEX_NAME")}\"")
    }

    kapt {
        arguments {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.functions)
    implementation(libs.firebase.dataconnect)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //room
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-paging:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")



    //material3
    val material3_version = "1.3.0"
    implementation("androidx.compose.material3:material3:$material3_version")
    implementation("androidx.compose.foundation:foundation-layout-android:1.6.8@aar")

    //DaggerHilt
    val daggerVersion = "2.48"
    val hiltVersion = "1.2.0"
    implementation("com.google.dagger:hilt-android:$daggerVersion")
    kapt("com.google.dagger:hilt-android-compiler:$daggerVersion")
    implementation("androidx.hilt:hilt-work:$hiltVersion")
    kapt("androidx.hilt:hilt-compiler:$hiltVersion")
    implementation("androidx.hilt:hilt-navigation-compose:$hiltVersion")

    //navigation
    val nav_version = "2.8.0"
    implementation("androidx.navigation:navigation-compose:$nav_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

    //retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    //Compose Gallery Picker Libs
    implementation("io.github.mr0xf00:easycrop:0.1.1")

    //Gson
    implementation("com.google.code.gson:gson:2.11.0")

    //Media
    implementation("androidx.media3:media3-exoplayer:1.4.0")
    implementation("androidx.media3:media3-exoplayer-dash:1.4.0")
    implementation("androidx.media3:media3-ui:1.4.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    //Waveform
    implementation("com.github.lincollincol:amplituda:2.2.2")
    implementation("com.github.lincollincol:compose-audiowaveform:1.1.1")

    //
    implementation("com.github.wseemann:FFmpegMediaMetadataRetriever-core:1.0.19")
    implementation("com.github.wseemann:FFmpegMediaMetadataRetriever-native:1.0.19")

    //FireBase
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))
    implementation("com.google.firebase:firebase-functions")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation("com.facebook.android:facebook-login:latest.release")
    implementation(libs.firebase.storage)
    implementation("com.google.firebase:firebase-dynamic-links")

    //Aglolia
    implementation("com.algolia:instantsearch-android:3.+")
    implementation("com.algolia:instantsearch-compose:3.+")
    implementation("com.algolia:instantsearch-android-paging3:3.+")

    //LoadItem Paging3 Lib
    val paging_version = "3.3.2"
    implementation("androidx.paging:paging-runtime:$paging_version")
    implementation("androidx.paging:paging-compose:3.3.2")

    //DataStore
    implementation(libs.androidx.datastore.core.android)
    implementation(libs.androidx.datastore.preferences.core.jvm)
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    //CoroutineCore
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")


    //Mockito
    dependencies {
        testImplementation("org.mockito:mockito-core:3.+")
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.2")
        testImplementation("io.mockk:mockk:1.12.0")
    }
    // Coil
    implementation("io.coil-kt:coil:2.7.0")
    implementation("io.coil-kt:coil-compose:2.7.0")

    //androidx.work
    val work_version = "2.7.1"
    implementation("androidx.work:work-runtime-ktx:$work_version")
}

kapt {
    correctErrorTypes = true
}