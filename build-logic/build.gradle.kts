plugins {
    `kotlin-dsl`
}

group = "com.acksession.zencastr.buildlogic"

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
            id = "zencastr.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "zencastr.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "zencastr.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "zencastr.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
        register("androidRoom") {
            id = "zencastr.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("androidFeature") {
            id = "zencastr.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidNetwork") {
            id = "zencastr.android.network"
            implementationClass = "AndroidNetworkConventionPlugin"
        }
        register("featureScaffolding") {
            id = "zencastr.scaffolding.feature"
            implementationClass = "FeatureScaffoldingConventionPlugin"
        }
    }
}
