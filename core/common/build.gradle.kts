plugins {
    id("zencastr.android.library")
    id("zencastr.android.hilt")
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.common"
}

dependencies {
    // Coroutines - exposed in public API
    api(libs.kotlinx.coroutines.core)
}
