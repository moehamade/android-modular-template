# ================================================================================================
# :core:network Module - ProGuard Rules
# ================================================================================================
# This module provides Retrofit, OkHttp, and Kotlinx Serialization for network operations.
# These rules ensure DTOs, API services, and network clients work correctly after R8 optimization.

# ================================================================================================
# Kotlinx Serialization (CRITICAL - DTOs use @Serializable)
# ================================================================================================
# R8 conditional rules - only keeps what's actually serializable

-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

-if @kotlinx.serialization.Serializable class ** {
    static **$Companion Companion;
}
-keepclassmembers class <2>$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep serializer for classes that have @Serializable
-if @kotlinx.serialization.Serializable class **
-keep,allowshrinking,allowobfuscation class <1>$$serializer {
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep serializers for sealed and enum classes
-if @kotlinx.serialization.Serializable class ** {
    ** $serializer;
}
-keepclassmembers class <1> {
    ** $serializer;
}

# Project-specific: Keep all DTOs (they use @Serializable)
-keep,allowobfuscation,allowshrinking @kotlinx.serialization.Serializable class com.example.data.remote.dto.** { *; }

# Kotlinx Serialization Core
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-dontwarn kotlinx.serialization.internal.ClassValueReferences

# ================================================================================================
# Retrofit 3.x with Kotlinx Serialization
# ================================================================================================
# Retrofit 3.0.0+ uses Kotlinx Serialization by default

# Keep service interfaces
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>
-keep,allowobfuscation,allowshrinking class <1>

# Keep annotation default values for Retrofit annotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep generic signatures for Retrofit (needed for Kotlin suspend functions)
-keepattributes Signature, InnerClasses, EnclosingMethod

# Keep Retrofit-internal OptionalConverterFactory for Java 8+ Optional support
-dontwarn retrofit2.Platform$Java8

# ================================================================================================
# OkHttp 5.x
# ================================================================================================
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Keep OkHttp's public suffix database for proper domain validation
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Platform used only on JVM and when Conscrypt dependency is available
-dontwarn org.codehaus.mojo.animal_sniffer.*

# JSR 305 annotations (used by OkHttp and others)
-dontwarn javax.annotation.**

# ================================================================================================
# Kotlin Coroutines (for suspend functions in API services)
# ================================================================================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep volatile fields in coroutines (used for atomic operations)
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

-dontwarn kotlinx.coroutines.flow.**
