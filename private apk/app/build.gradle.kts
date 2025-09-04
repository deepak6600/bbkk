plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")

}

android {
    namespace = "com.safe.setting.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.safe.setting.app"
        minSdk = 23
        targetSdk = 36
        versionCode = 3
        versionName = "2.20002"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    // 'packagingOptions' has been renamed to 'packaging' in newer AGP versions.
    packaging {
        // RxJava 3 doesn't need this exclusion anymore, but keeping it in case of other conflicts.
        resources.excludes.add("META-INF/rxjava.properties")
    }
}

// Set the JVM toolchain for Kotlin, which is the modern way to set the JVM target.
kotlin {
    jvmToolchain(17)
}

dependencies {
    // Core Android libraries
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")

    // Android Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.3")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.3")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.2.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.firebase:firebase-analytics")


    // --- Cloudinary की नई  यहाँ जोड़नी है ---
    implementation("com.cloudinary:cloudinary-android:3.1.1") // <-- यह नई लाइन




    // Glide Image Loading
    implementation("com.github.bumptech.glide:glide:5.0.4")
    ksp("com.github.bumptech.glide:compiler:5.0.4")


    // Dagger 2
    implementation("com.google.dagger:dagger:2.57.1")
    ksp("com.google.dagger:dagger-compiler:2.57.1")

    // RxJava3 & RxBinding
    implementation("com.jakewharton.rxbinding4:rxbinding:4.0.0")
    implementation("io.reactivex.rxjava3:rxkotlin:3.0.1")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}

