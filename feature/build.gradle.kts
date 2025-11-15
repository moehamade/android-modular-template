plugins {
    alias(libs.plugins.convention.android.library)
}

val projectProperties : ProjectProperties by rootProject.extensions

android {
    namespace = "${projectProperties.corePackagePrefix}feature"
}

dependencies {
    implementation(libs.androidx.core.ktx)
}
