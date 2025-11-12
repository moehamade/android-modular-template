plugins {
    id("zencastr.android.feature")
}

android {
    namespace = "com.acksession.feature.settings"
}

dependencies {
    implementation(project(":feature:settings:api"))

    // Core dependencies for settings functionality
    implementation(project(":core:datastore:preferences"))
    implementation(project(":core:network"))

    // Add feature-specific dependencies here
}
