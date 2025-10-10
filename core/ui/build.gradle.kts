plugins {
    id("zencastr.android.library")
    id("zencastr.android.compose")
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.ui"
}

dependencies {
    implementation(libs.androidx.core.ktx)
}