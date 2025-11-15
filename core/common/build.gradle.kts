plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.hilt)
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.common"
}

dependencies {
    // Coroutines - exposed in public API
    api(libs.kotlinx.coroutines.core)
}
