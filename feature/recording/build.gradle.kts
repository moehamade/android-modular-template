plugins {
    id("zencastr.android.feature")
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.feature.recording"
}

dependencies {
    // Permissions module
    implementation(project(":core:permissions"))

    // CameraX dependencies
    implementation(libs.bundles.camerax)
}
