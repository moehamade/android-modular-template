plugins {
    alias(libs.plugins.convention.android.feature)
}

val projectProperties: ProjectProperties by rootProject.extensions

android {
    namespace = "${projectProperties.corePackagePrefix}.feature.recording"
}

dependencies {
    implementation(project(":feature:recording:api"))
    implementation(project(":feature:profile:api"))
    implementation(project(":feature:settings:api"))
    implementation(libs.accompanist.permissions)
    implementation(libs.bundles.camerax)
}
