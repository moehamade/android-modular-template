plugins {
    id("zencastr.android.library")
    id("zencastr.android.hilt")
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.remoteconfig"
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config)
    
    implementation(libs.timber)
    implementation(libs.kotlinx.coroutines.android)
}
