plugins {
    id("zencastr.android.feature")
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.feature.recording"
}

dependencies {
    // Permission handling
    implementation(libs.accompanist.permissions)
    // CameraX dependencies
    implementation(libs.bundles.camerax)
}
