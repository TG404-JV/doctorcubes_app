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

}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth") // Ensure this is added
    implementation("com.google.firebase:firebase-database")
    implementation(libs.recyclerview) // Ensure this is added

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("com.pierfrancescosoffritti.androidyoutubeplayer:core:11.1.0")
        implementation ("androidx.webkit:webkit:1.8.0")
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation (libs.android.pdf.viewer)
    implementation ("androidx.navigation:navigation-fragment-ktx:2.7.4")
    implementation ("androidx.navigation:navigation-ui-ktx:2.7.4")
    implementation ("androidx.security:security-crypto:1.1.0-alpha06")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation ("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0") // Modern YouTube Player
    implementation ("com.squareup.picasso:picasso:2.8")
    implementation ("com.android.volley:volley:1.2.1")

    implementation ("org.apache.poi:poi:5.2.5")
    implementation ("org.apache.poi:poi-ooxml:5.2.5")







}

// Apply the Google Services plugin at the end
apply(plugin = "com.google.gms.google-services")
