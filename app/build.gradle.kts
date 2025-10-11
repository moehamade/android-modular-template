plugins {
    id("zencastr.android.application")
    id("zencastr.android.compose")
    id("zencastr.android.hilt")
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.zencastr"

    defaultConfig {
        applicationId = "com.acksession.zencastr"
    }
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":feature:recording"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
}