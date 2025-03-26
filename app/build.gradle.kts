import java.util.regex.Pattern.compile

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // Add this line for Firebase services
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
        sourceCompatibility = JavaVersion.VERSION_17 // Update to latest LTS version
        targetCompatibility = JavaVersion.VERSION_17
    }
    sourceSets {
        getByName("main") {
            assets {
                srcDirs("src\\main\\assets", "src\\main\\assets")
            }
        }
    }

}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:33.11.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth") // Ensure this is added
    implementation("com.google.firebase:firebase-database")
    implementation ("com.google.android.gms:play-services-auth:21.3.0")

    implementation(libs.recyclerview)
    implementation(libs.firebase.firestore) // Ensure this is added

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")
        implementation ("androidx.webkit:webkit:1.13.0")
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation (libs.android.pdf.viewer)
    implementation ("androidx.navigation:navigation-fragment-ktx:2.8.9")
    implementation ("androidx.navigation:navigation-ui-ktx:2.8.9")
    implementation ("androidx.security:security-crypto:1.1.0-alpha06")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    implementation ("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0") // Modern YouTube Player
    implementation ("com.squareup.picasso:picasso:2.71828")
    implementation ("com.android.volley:volley:1.2.1")

    implementation ("org.apache.poi:poi:5.2.5")
    implementation ("org.apache.poi:poi-ooxml:5.2.5")
    implementation ("com.gauravk.bubblenavigation:bubblenavigation:1.0.7")
    implementation ("de.hdodenhof:circleimageview:3.1.0")

    // Notification Tool
    implementation ("androidx.work:work-runtime:2.10.0")
    implementation ("androidx.room:room-runtime:2.6.1")
    annotationProcessor ("androidx.room:room-compiler:2.6.1")

    implementation ("com.airbnb.android:lottie:6.6.0")

}

// Apply the Google Services plugin at the end
apply(plugin = "com.google.gms.google-services")
