plugins {
    id("zencastr.android.feature")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.feature.recording"
}

dependencies {
    // Profile feature for navigation (routes only)
    implementation(project(":feature:profile"))

    // Permission handling
    implementation(libs.accompanist.permissions)
    // CameraX dependencies
    implementation(libs.bundles.camerax)
}
