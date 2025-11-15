plugins {
    alias(libs.plugins.convention.android.feature)
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.feature.profile"
}

dependencies {
    // Profile API for routes
    api(project(":feature:profile:api"))
}
