# Google ML Kit ProGuard Rules
-keep class com.google.mlkit.** { *; }
-keep interface com.google.mlkit.** { *; }
-keep class com.google.android.gms.** { *; }
-keep interface com.google.android.gms.** { *; }

# Kotlin Coroutines
-keepclassmembers class * extends kotlinx.coroutines.internal.MainDispatcherFactory {
    public <init>();
}
-keep class kotlinx.coroutines.android.AndroidDispatcherFactory {
    public <init>();
}

# Coil Image Loader
-keep class coil.** { *; }

# App Components & Data Models
-keep class com.cardreminder.app.** { *; }
-keepclassmembers class com.cardreminder.app.** { *; }
