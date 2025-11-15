plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.kotlin.serialization)
}

val projectProperties: ProjectProperties by rootProject.extensions

android {
    namespace = "${projectProperties.corePackagePrefix}.feature.settings.api"
}

dependencies {
    api(project(":core:navigation"))
    api(libs.bundles.navigation3)
}
