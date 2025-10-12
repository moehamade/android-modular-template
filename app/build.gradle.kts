plugins {
    id("zencastr.android.application")
    id("zencastr.android.compose")
    id("zencastr.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.zencastr"

    defaultConfig {
        applicationId = "com.acksession.zencastr"
    }
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:navigation"))
    implementation(project(":feature:recording"))
    implementation(project(":feature:profile"))

    // Navigation3 dependencies
    implementation(libs.bundles.navigation3)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
}