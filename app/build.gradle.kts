plugins {
    id("zencastr.android.application")
    id("zencastr.android.compose")
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.zencastr"

    defaultConfig {
        applicationId = "com.acksession.zencastr"
    }
}

dependencies {
    implementation(project(":core:ui"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
}