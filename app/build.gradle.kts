import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // Firebase services
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.android.doctorcube"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.android.doctorcube"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Load API key from local.properties securely
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }

        // Define YouTube API Key as BuildConfig variable
        buildConfigField(
            "String", "YOUTUBE_API_KEY",
            "\"${localProperties["YOUTUBE_API_KEY"] ?: ""}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = true // Enable to shrink & obfuscate code
            isShrinkResources = true // Removes unused resources

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

    sourceSets {
        getByName("main") {
            assets {
                srcDirs("src\\main\\assets", "src\\main\\assets")
            }
        }
    }

    buildFeatures {
        buildConfig = true // Enable BuildConfig generation
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase dependencies (ensure safe versions)
    implementation(platform("com.google.firebase:firebase-bom:33.11.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")

    implementation(libs.recyclerview)
    implementation(libs.firebase.firestore)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // YouTube Player
    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")

    implementation("androidx.webkit:webkit:1.13.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation(libs.android.pdf.viewer)
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.9")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.9")

    // Secure storage & encryption
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Secure networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.android.volley:volley:1.2.1")

    // Excel support
    implementation("org.apache.poi:poi:5.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.5")

    // UI Components
    implementation("com.gauravk.bubblenavigation:bubblenavigation:1.0.7")
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Background task security
    implementation("androidx.work:work-runtime:2.10.0")

    // Secure database handling
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // Animations
    implementation("com.airbnb.android:lottie:6.6.0")
}

// Apply Google Services plugin
apply(plugin = "com.google.gms.google-services")
