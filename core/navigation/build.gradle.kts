plugins {
    id("zencastr.android.library")
    id("zencastr.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.navigation"
}

dependencies {
    // Navigation3 dependencies
    implementation(libs.bundles.navigation3)
}
