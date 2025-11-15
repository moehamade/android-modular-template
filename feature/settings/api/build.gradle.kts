plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.acksession.feature.settings.api"
}

dependencies {
    api(project(":core:navigation"))
    api(libs.bundles.navigation3)
}
