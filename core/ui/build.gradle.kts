plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.compose)
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.ui"
}

dependencies {
    // Android core (internal use)
    implementation(libs.androidx.core.ktx)

    // Permissions (internal utility)
    implementation(libs.accompanist.permissions)

    // Compose - ALL exposed via api() because:
    // 1. UI components return Composables
    // 2. Consumers need direct access to Material3, BOM, etc.
    // 3. This is a shared UI library
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.material.icons.core)
}