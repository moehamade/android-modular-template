plugins {
    id("zencastr.android.feature")
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.feature.recording"
}

dependencies {
    // Recording API for navigation routes
    api(project(":feature:recording:api"))

    // Profile API for navigation (routes only, no UI dependencies)
    implementation(project(":feature:profile:api"))

    // Settings API for navigation (routes only, no UI dependencies)
    implementation(project(":feature:settings:api"))

    // Permission handling
    implementation(libs.accompanist.permissions)
    // CameraX dependencies
    implementation(libs.bundles.camerax)
}
