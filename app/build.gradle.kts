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

        // API Base URL configuration
        // TODO: Replace with your actual API URL
        buildConfigField("String", "API_BASE_URL", "\"https://api.zencastr.com/\"")
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // Core modules
    implementation(project(":core:ui"))
    implementation(project(":core:navigation"))
    implementation(project(":core:network")) // Provides Retrofit configuration
    implementation(project(":core:data")) // Already includes domain
    implementation(project(":core:datastore:preferences"))

    // Feature modules
    implementation(project(":feature:recording"))
    implementation(project(":feature:profile"))

    // Navigation3 (using bundle)
    implementation(libs.bundles.navigation3)

    // Lifecycle (using bundle)
    implementation(libs.bundles.lifecycle)
}