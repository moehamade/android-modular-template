# ================================================================================================
# :core:data Module - ProGuard Rules
# ================================================================================================
# This module provides Room database, repositories, and data sources.
# These rules ensure database entities, DAOs, and Room-generated code work after R8 optimization.

# ================================================================================================
# Room Database 2.8+
# ================================================================================================
# R8 handles most Room code via annotations, but keep entities and DAOs explicit

-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Project-specific: Keep entities and DAOs
-keep class com.example.data.local.entity.** { *; }
-keep class com.example.data.local.dao.** { *; }
-keep class com.example.data.local.AppDatabase { *; }

# ================================================================================================
# Kotlin Coroutines (for Flow in repositories)
# ================================================================================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep volatile fields in coroutines (used for atomic operations)
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

-dontwarn kotlinx.coroutines.flow.**
