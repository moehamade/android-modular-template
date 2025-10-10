plugins {
    id("zencastr.android.library")
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.data"
}

dependencies {
    implementation(libs.androidx.core.ktx)
}