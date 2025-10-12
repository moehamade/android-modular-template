plugins {
    id("zencastr.android.feature")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.feature.profile"
}

dependencies {
    // No additional dependencies needed for this test feature
}
