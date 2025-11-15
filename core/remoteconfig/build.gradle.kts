plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.hilt)
}

val projectProperties: ProjectProperties by rootProject.extensions

android {
    namespace = "${projectProperties.corePackagePrefix}.remoteconfig"
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config)

    implementation(libs.timber)
    implementation(libs.kotlinx.coroutines.android)
}
