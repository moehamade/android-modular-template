plugins {
    id("zencastr.android.feature")
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.feature.recording"
}

dependencies {
    // Profile API for navigation (routes only, no UI dependencies)
    implementation(project(":feature:profile:api"))

    // Permission handling
    implementation(libs.accompanist.permissions)
    // CameraX dependencies
    implementation(libs.bundles.camerax)
}
