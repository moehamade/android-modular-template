# ================================================================================================
# :core:datastore:preferences Module - ProGuard Rules
# ================================================================================================
# This module provides encrypted token storage using EncryptedSharedPreferences.
# These rules ensure encryption (Tink) and secure storage work correctly after R8 optimization.

# ================================================================================================
# AndroidX Security Crypto (EncryptedSharedPreferences)
# ================================================================================================
# Keep encryption classes - critical for token storage

-keep class androidx.security.crypto.** { *; }
-keep class com.google.crypto.tink.** { *; }

# Tink has optional dependencies that are not included - suppress warnings
-dontwarn com.google.crypto.tink.proto.**
-dontwarn com.google.api.client.http.GenericUrl
-dontwarn com.google.api.client.http.HttpHeaders
-dontwarn com.google.api.client.http.HttpRequest
-dontwarn com.google.api.client.http.HttpRequestFactory
-dontwarn com.google.api.client.http.HttpResponse
-dontwarn com.google.api.client.http.HttpTransport
-dontwarn com.google.api.client.http.javanet.NetHttpTransport
-dontwarn com.google.api.client.http.javanet.NetHttpTransport$Builder
-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.CheckReturnValue
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn com.google.errorprone.annotations.InlineMe
-dontwarn com.google.errorprone.annotations.RestrictedApi
-dontwarn org.joda.time.Instant

# Project-specific: Keep encrypted storage classes
-keep class com.acksession.datastore.preferences.EncryptedAuthStorage { *; }
-keep class com.acksession.datastore.preferences.AuthPreferencesDataSource { *; }

# ================================================================================================
# AndroidX DataStore Preferences 1.1.7
# ================================================================================================
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}
-keep class androidx.datastore.preferences.** { *; }
