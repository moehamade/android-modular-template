plugins {
    id("zencastr.android.library")
    id("zencastr.android.hilt")
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.domain"
}

dependencies {
    // Android (internal use only)
    implementation(libs.androidx.core.ktx)

    // Coroutines - exposed in public API (repository interfaces return Flow)
    api(libs.kotlinx.coroutines.core)

    // Note: kotlin-stdlib is included by default in Kotlin projects
}