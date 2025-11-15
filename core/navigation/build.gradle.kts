plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

val projectProperties: ProjectProperties by rootProject.extensions

android {
    namespace = "${projectProperties.corePackagePrefix}.navigation"
}

dependencies {
    // Navigation3 - exposed in public API (navigation components used by consumers)
    api(libs.bundles.navigation3)
}
