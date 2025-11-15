plugins {
    alias(libs.plugins.convention.android.library)
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}feature"
}

dependencies {
    implementation(libs.androidx.core.ktx)
}