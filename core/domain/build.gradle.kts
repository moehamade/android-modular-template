plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.hilt)
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.domain"
}

dependencies {
    // Android (internal use only)
    implementation(libs.androidx.core.ktx)

    // Coroutines - exposed in public API (repository interfaces return Flow)
    api(libs.kotlinx.coroutines.core)

    // Common - for dispatcher qualifiers in use cases
    implementation(project(":core:common"))

    // Note: kotlin-stdlib is included by default in Kotlin projects
}