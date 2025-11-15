plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.kotlin.serialization)
}

val projectProperties: ProjectProperties by rootProject.extensions

android {
    namespace = "${projectProperties.corePackagePrefix}.feature.recording.api"
}

dependencies {
    // Navigation for NavKey and Navigator
    api(project(":core:navigation"))

    // Navigation3 dependencies for @Serializable and NavKey
    api(libs.bundles.navigation3)
}
