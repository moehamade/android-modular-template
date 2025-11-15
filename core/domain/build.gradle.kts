plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.hilt)
}

val projectProperties: ProjectProperties by rootProject.extensions

android {
    namespace = "${projectProperties.corePackagePrefix}.domain"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    api(libs.kotlinx.coroutines.core)
    implementation(project(":core:common"))
}
