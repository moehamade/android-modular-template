# ================================================================================================
# :core:datastore:proto Module - ProGuard Rules
# ================================================================================================
# This module will provide Proto DataStore for type-safe structured data storage.
# These rules ensure Protobuf generated classes work correctly after R8 optimization.

# ================================================================================================
# Proto DataStore (androidx.datastore:datastore)
# ================================================================================================
# Proto DataStore uses Protocol Buffers for structured data

# Keep all generated protobuf classes
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }

# Keep protobuf message fields
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}

# Keep DataStore serializers
-keepclassmembers class * extends androidx.datastore.core.Serializer {
    <init>(...);
    public ** deserialize(...);
    public ** serialize(...);
}

# Project-specific: Keep proto-generated classes (update namespace when proto files are added)
# Example: -keep class com.example.datastore.proto.UserPreferences { *; }

# ================================================================================================
# Protobuf Lite (com.google.protobuf:protobuf-javalite)
# ================================================================================================
# Protobuf uses reflection for message instantiation

-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <init>();
    *** newBuilder();
    *** parseFrom(...);
    *** getDefaultInstance();
}

# Keep Protobuf enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Suppress warnings for optional protobuf features
-dontwarn com.google.protobuf.**
-dontwarn javax.annotation.**

# ================================================================================================
# Kotlin Coroutines (for DataStore operations)
# ================================================================================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep volatile fields in coroutines (used for atomic operations)
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

-dontwarn kotlinx.coroutines.flow.**
