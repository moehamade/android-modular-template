plugins {
    alias(libs.plugins.convention.android.feature)
}

val projectProperties: ProjectProperties by rootProject.extensions

android {
    namespace = "${projectProperties.corePackagePrefix}.feature.profile"
}

dependencies {
    // Profile API for routes
    api(project(":feature:profile:api"))
}
