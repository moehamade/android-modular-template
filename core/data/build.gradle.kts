plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.hilt)
    alias(libs.plugins.convention.android.room)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.data"
}

dependencies {
    // Android core (internal use)
    implementation(libs.androidx.core.ktx)

    // Coroutines - use Android version in data layer
    implementation(libs.kotlinx.coroutines.android)

    // Domain layer - EXPOSED to consumers via api()
    api(project(":core:domain"))

    // Network layer - provides Retrofit + interceptors (NO circular dependency!)
    implementation(project(":core:network"))

    // DataStore - internal implementation detail
    implementation(project(":core:datastore:preferences"))

    // Network dependencies for DTOs and API services
    implementation(libs.retrofit)
    implementation(libs.kotlinx.serialization.json)
}