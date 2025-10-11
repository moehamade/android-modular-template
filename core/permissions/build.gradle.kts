plugins {
    id("zencastr.android.library")
    id("zencastr.android.compose")
    id("zencastr.android.hilt")
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.permissions"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.accompanist.permissions)
    implementation(libs.kotlinx.coroutines.core)

    // Lifecycle and ViewModel
    implementation(libs.bundles.lifecycle)
    implementation(libs.hilt.navigation.compose)

    // DataStore for persistent permission state
    implementation(project(":core:datastore:preferences"))
}
