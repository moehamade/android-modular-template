plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.hilt)
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.notifications"
}

dependencies {
    // Firebase BOM
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    // Core modules
    implementation(project(":core:common"))
    implementation(project(":core:analytics"))

    // Timber for logging
    implementation(libs.timber)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // WorkManager for background tasks
    implementation(libs.work.runtime.ktx)
}
