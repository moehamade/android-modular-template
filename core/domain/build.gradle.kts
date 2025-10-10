plugins {
    id("zencastr.android.library")
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.domain"
}

dependencies {
    implementation(libs.androidx.core.ktx)
}