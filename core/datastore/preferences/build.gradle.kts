plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.hilt)
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.datastore.preferences"
}

dependencies {
    // Android core (internal use)
    implementation(libs.androidx.core.ktx)

    // DataStore - internal implementation
    implementation(libs.datastore.preferences)

    // Coroutines - exposed in TinkAuthStorage public API (returns Flow)
    api(libs.kotlinx.coroutines.core)

    // Tink for encryption
    implementation(libs.tink.android)

    // Common - for @ApplicationScopeIO annotation and dispatcher qualifiers
    implementation(project(":core:common"))
}
