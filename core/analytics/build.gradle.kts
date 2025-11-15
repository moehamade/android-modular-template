plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.hilt)
}

val projectProperties: ProjectProperties by rootProject.extensions

android {
    namespace = "${projectProperties.corePackagePrefix}.analytics"
}

dependencies {
    // Firebase BOM (Bill of Materials) - manages versions
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.perf)

    // Timber for logging
    implementation(libs.timber)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
}
