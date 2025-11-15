plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.hilt)
}

val projectProperties: ProjectProperties by rootProject.extensions

android {
    namespace = "${projectProperties.corePackagePrefix}.common"
}

dependencies {
    // Coroutines - exposed in public API
    api(libs.kotlinx.coroutines.core)
}
