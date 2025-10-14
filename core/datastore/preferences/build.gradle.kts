plugins {
    id("zencastr.android.library")
    id("zencastr.android.hilt")
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.datastore.preferences"
}

dependencies {
    // Android core (internal use)
    implementation(libs.androidx.core.ktx)

    // DataStore - internal implementation
    implementation(libs.datastore.preferences)

    // Coroutines - exposed in AuthPreferencesDataSource public API (returns Flow)
    api(libs.kotlinx.coroutines.core)

    implementation(libs.androidx.security.crypto)
}
