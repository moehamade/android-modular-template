plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.hilt)
    alias(libs.plugins.convention.android.network)
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.network"

    defaultConfig {
        // Allow BuildConfig to be configured from app module via DI
        buildFeatures {
            buildConfig = true
        }
    }
}

dependencies {
    // DataStore preferences - for token storage (NO circular dependency!)
    implementation(project(":core:datastore:preferences"))

    // Network convention plugin provides:
    // - Retrofit, OkHttp, Kotlinx Serialization, Timber
}
