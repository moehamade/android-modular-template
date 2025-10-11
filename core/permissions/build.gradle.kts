plugins {
    id("zencastr.android.library")
    id("zencastr.android.compose")
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.permissions"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.accompanist.permissions)
}
