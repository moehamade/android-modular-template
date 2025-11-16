plugins {
    `kotlin-dsl`
}

group = "com.example.myapp.buildlogic"

val pluginIdPrefix = "convention"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "$pluginIdPrefix.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "$pluginIdPrefix.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "$pluginIdPrefix.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "$pluginIdPrefix.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
        register("androidRoom") {
            id = "$pluginIdPrefix.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("androidFeature") {
            id = "$pluginIdPrefix.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidNetwork") {
            id = "$pluginIdPrefix.android.network"
            implementationClass = "AndroidNetworkConventionPlugin"
        }
        register("featureScaffolding") {
            id = "$pluginIdPrefix.scaffolding.feature"
            implementationClass = "FeatureScaffoldingConventionPlugin"
        }
        register("projectProperties") {
            id = "$pluginIdPrefix.project.properties"
            implementationClass = "ProjectPropertiesConventionPlugin"
        }

    }
}
