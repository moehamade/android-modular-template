plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.hilt)
    alias(libs.plugins.convention.android.network)
}

val projectProperties: ProjectProperties by rootProject.extensions

android {
    namespace = "${projectProperties.corePackagePrefix}.network"

    defaultConfig {
        // Allow BuildConfig to be configured from app module via DI
        buildFeatures {
            buildConfig = true
        }
    }
}

dependencies {
    implementation(project(":core:datastore:preferences"))

    // Network convention plugin provides:
    // - Retrofit, OkHttp, Kotlinx Serialization, Timber
}
