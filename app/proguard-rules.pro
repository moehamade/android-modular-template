# ================================================================================================
# MyApp Android - App Module R8 Configuration
# ================================================================================================
# This file contains ONLY app-level ProGuard rules.
# Library-specific rules are distributed across modules in their consumer-rules.pro files:
#   - :core:network/consumer-rules.pro (Retrofit, OkHttp, Kotlinx Serialization)
#   - :core:data/consumer-rules.pro (Room database, entities, DAOs)
#   - :core:datastore:preferences/consumer-rules.pro (EncryptedSharedPreferences, Tink)
#   - :core:datastore:proto/consumer-rules.pro (Proto DataStore, Protobuf)
#   - :core:navigation/consumer-rules.pro (Navigation3, NavKey routes)
#   - :core:ui/consumer-rules.pro (Jetpack Compose, Coil)
#   - :feature:recording/consumer-rules.pro (Stream Video SDK, WebRTC, CameraX)

# Keep source file names and line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep annotations for reflection and runtime processing
-keepattributes *Annotation*, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keepattributes Signature

# ================================================================================================
# Hilt / Dagger 2.57+ (App-level DI)
# ================================================================================================
# R8 handles most of Hilt automatically via its built-in rules.
# Only keep what's necessary for runtime reflection.

-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Hilt-generated classes (R8 usually handles this, but being explicit)
-keep class **_HiltModules { *; }
-keep class **_HiltModules$** { *; }

# Keep injected constructors and fields
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
    @javax.inject.Inject <fields>;
}

# Keep ViewModels injected by Hilt
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ================================================================================================
# Timber Logging 5.0.1 (Used across all modules)
# ================================================================================================
-dontwarn org.jetbrains.annotations.**
-keep class timber.log.** { *; }

# ================================================================================================
# General Android Rules
# ================================================================================================

# Keep Parcelable implementations
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep enum values() and valueOf()
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep View constructors (used by XML inflation)
-keepclasseswithmembers class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# ================================================================================================
# R8 Full Mode Optimizations
# ================================================================================================
# https://r8.googlesource.com/r8/+/refs/heads/main/compatibility-faq.md

# Enable aggressive optimizations (R8 full mode)
-allowaccessmodification

# Don't use -repackageclasses as it can break reflection in some libraries
# -repackageclasses

# Additional optimization passes (R8 default is 5, max is 10)
# Uncomment if you want more aggressive optimization (longer build times)
# -optimizationpasses 7

# ================================================================================================
# Debugging (Remove in production)
# ================================================================================================
# Uncomment to print configuration and usage during build (helpful for debugging rules)
# -printconfiguration build/outputs/mapping/configuration.txt
# -printusage build/outputs/mapping/usage.txt

# ================================================================================================
# Firebase Services
# ================================================================================================

# Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# Firebase Analytics
-keep class com.google.android.gms.measurement.** { *; }
-keep class com.google.firebase.analytics.** { *; }

# Firebase Messaging (FCM)
-keep class com.google.firebase.messaging.** { *; }
-keep class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

# Firebase Remote Config
-keep class com.google.firebase.remoteconfig.** { *; }
-keep class com.google.firebase.abt.** { *; }

# Firebase Performance
-keep class com.google.firebase.perf.** { *; }

# Firebase Common
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ================================================================================================
# Debug Tools (Leakcanary, Chucker)
# ================================================================================================
# These are debug-only dependencies and won't be in release builds
# But adding rules just in case

-dontwarn com.squareup.leakcanary.**
-dontwarn com.chuckerteam.chucker.**
