# Preserve essential Android classes
-keep class * implements android.os.Parcelable { *; }
-keep class **.R$* { *; }
-keep public class * extends android.app.Application { *; }
-keep public class * extends android.app.Activity { *; }
-keep public class * extends android.app.Fragment { *; }
-keep public class * extends androidx.fragment.app.Fragment { *; }
-keep public class * extends android.app.Service { *; }
-keep public class * extends android.content.BroadcastReceiver { *; }
-keep public class * extends android.content.ContentProvider { *; }

# Keep annotations
-keepattributes *Annotation*

# Keep Serializable classes
-keep class * implements java.io.Serializable { *; }

# Keep AndroidX libraries
-keep class androidx.security.crypto.** { *; }  # For EncryptedSharedPreferences
-keep class androidx.preference.** { *; }
-keep class androidx.core.content.** { *; }
-keep class androidx.appcompat.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class androidx.navigation.** { *; }  # For Navigation component
-keep class androidx.swiperefreshlayout.** { *; }  # For SwipeRefreshLayout
-keep class androidx.webkit.** { *; }  # For Webkit

# Keep Firebase and Google Play Services
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep Retrofit, Gson, OkHttp, and Volley
-keep class com.google.gson.** { *; }
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep class com.android.volley.** { *; }

# Keep Room Database
-keep class androidx.room.** { *; }

# Keep WorkManager
-keep class androidx.work.** { *; }

# Keep Glide and Picasso
-keep class com.bumptech.glide.** { *; }
-keep class com.squareup.picasso.** { *; }

# Keep YouTube Player
-keep class com.pierfrancescosoffritti.androidyoutubeplayer.** { *; }

# Keep Apache POI (Excel support)
-keep class org.apache.poi.** { *; }

# Keep UI libraries
-keep class com.gauravk.bubblenavigation.** { *; }  # Bubble Navigation
-keep class de.hdodenhof.circleimageview.** { *; }  # CircleImageView

# Keep Lottie animations
-keep class com.airbnb.lottie.** { *; }

# Keep WhatsApp-related classes for Intent handling
-keep class android.content.Intent { *; }
-keep class android.net.Uri { *; }
-keep class android.content.pm.PackageManager { *; }

# Keep your custom classes
-keep class com.android.doctorcube.** { *; }  # Matches your applicationId

# Remove all log messages in release build
-assumenosideeffects class android.util.Log {
    *;
}

# Suppress warnings for missing classes that might not be critical
-dontwarn com.google.**
-dontwarn androidx.**
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn com.android.volley.**
-dontwarn org.apache.poi.**
-dontwarn com.bumptech.glide.**
-dontwarn com.squareup.picasso.**
-dontwarn com.pierfrancescosoffritti.androidyoutubeplayer.**
-dontwarn com.gauravk.bubblenavigation.**
-dontwarn de.hdodenhof.circleimageview.**
-dontwarn com.airbnb.lottie.**

# Keep missing Microsoft schemas classes
-keep class com.microsoft.schemas.office.office.** { *; }
-keep class com.microsoft.schemas.office.powerpoint.** { *; }
-keep class com.microsoft.schemas.office.visio.x2012.main.** { *; }
-keep class com.microsoft.schemas.office.word.** { *; }
-keep class com.microsoft.schemas.office.x2006.digsig.** { *; }
-keep class com.microsoft.schemas.vml.** { *; }

# Keep AWT Shape

# Keep ETSI classes
-keep class org.etsi.uri.x01903.v13.** { *; }

# Keep OpenXML classes
-keep class org.openxmlformats.schemas.drawingml.x2006.chart.** { *; }
-keep class org.openxmlformats.schemas.drawingml.x2006.diagram.** { *; }
-keep class org.openxmlformats.schemas.drawingml.x2006.main.** { *; }
-keep class org.openxmlformats.schemas.presentationml.x2006.main.** { *; }

