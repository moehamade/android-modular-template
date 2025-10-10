plugins {
    id("zencastr.android.library")
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}feature"
}

dependencies {
    implementation(libs.androidx.core.ktx)
}