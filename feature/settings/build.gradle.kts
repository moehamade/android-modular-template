plugins {
    alias(libs.plugins.convention.android.feature)
}

val projectProperties: ProjectProperties by rootProject.extensions

android {
    namespace = "${projectProperties.corePackagePrefix}.feature.settings"
}

dependencies {
    implementation(project(":feature:settings:api"))
    implementation(project(":core:datastore:preferences"))
    implementation(project(":core:network"))
}
