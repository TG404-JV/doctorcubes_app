# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Optimization
# Optimization settings
-optimizationpasses 5
-dontpreverify
-allowaccessmodification
-dontobfuscate

# Keep essential classes
-keep class * implements android.os.Parcelable { *; }
-keep class **.R$* { *; }
-keep public class * extends android.app.Application { *; }

# Keep annotations
-keepattributes *Annotation*

# Keep Serializable classes
-keep class * implements java.io.Serializable { *; }

# Keep Firebase and Google Play services
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep Retrofit & Gson models
-keep class com.google.gson.** { *; }
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }

# Keep Room Database models
-keep class androidx.room.** { *; }

# Keep WorkManager
-keep class androidx.work.** { *; }

# Keep Dagger/Hilt dependency injection
-keep class dagger.** { *; }
-keep class hilt_** { *; }

# Keep coroutines and threading classes
-keep class kotlinx.coroutines.** { *; }

# Remove all log messages in release build
-assumenosideeffects class android.util.Log { *; }
# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
