plugins {
    id("zencastr.android.library")
    id("zencastr.android.compose")
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.ui"
}

dependencies {
    implementation(libs.androidx.core.ktx)

    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.material.icons.core)
}