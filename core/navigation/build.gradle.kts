plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.navigation"
}

dependencies {
    // Navigation3 - exposed in public API (navigation components used by consumers)
    api(libs.bundles.navigation3)
}
