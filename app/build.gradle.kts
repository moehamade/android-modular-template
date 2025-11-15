plugins {
    alias(libs.plugins.convention.android.application)
    alias(libs.plugins.convention.android.compose)
    alias(libs.plugins.convention.android.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf)
}

val projectProperties: ProjectProperties by rootProject.extensions

android {
    namespace = projectProperties.appPackageName.also {
        logger.lifecycle("Namespace: $it (from ${rootProject.file("template.properties")})")
    }

    flavorDimensions += "environment"

    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String", "ENVIRONMENT", "\"development\"")
            buildConfigField("String", "API_BASE_URL", "\"https://dev-api.example.com/\"") // TODO: Replace with your dev API URL
        }

        create("prod") {
            dimension = "environment"
            buildConfigField("String", "ENVIRONMENT", "\"production\"")
            buildConfigField("String", "API_BASE_URL", "\"https://api.example.com/\"") // TODO: Replace with your prod API URL
        }
    }

    defaultConfig {
        applicationId = projectProperties.appPackageName
        versionCode = rootProject.extra["versionCode"] as Int
        versionName = rootProject.extra["versionName"] as String

        resValue("string", "app_name", projectProperties.appDisplayName)

        manifestPlaceholders["appScheme"] = projectProperties.projectNameLowercase
        manifestPlaceholders["appHost"] = "${projectProperties.projectNameLowercase}.com"
        manifestPlaceholders["appTheme"] = "Theme.${projectProperties.projectName}"
    }

    // Signing configuration for release builds
    // TODO: Add keystore.properties file with:
    //   storeFile=path/to/keystore.jks
    //   storePassword=your_store_password
    //   keyAlias=your_key_alias
    //   keyPassword=your_key_password
    signingConfigs {
        create("release") {
            // TODO: Uncomment when ready for release
            // val keystorePropertiesFile = rootProject.file("keystore.properties")
            // if (keystorePropertiesFile.exists()) {
            //     val keystoreProperties = java.util.Properties()
            //     keystoreProperties.load(java.io.FileInputStream(keystorePropertiesFile))
            //     storeFile = file(keystoreProperties["storeFile"] as String)
            //     storePassword = keystoreProperties["storePassword"] as String
            //     keyAlias = keystoreProperties["keyAlias"] as String
            //     keyPassword = keystoreProperties["keyPassword"] as String
            // }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // TODO: Uncomment when signing is configured
            // signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // Core modules
    implementation(project(":core:common")) // Infrastructure (dispatchers, scopes, qualifiers)
    implementation(project(":core:ui"))
    implementation(project(":core:navigation"))
    implementation(project(":core:network")) // Provides Retrofit configuration
    implementation(project(":core:data")) // Already includes domain
    implementation(project(":core:datastore:preferences"))
    implementation(project(":core:analytics"))
    implementation(project(":core:notifications"))
    implementation(project(":core:remoteconfig"))

    // Feature modules
    implementation(project(":feature:recording"))
    implementation(project(":feature:recording:api"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:settings:api"))

    // Firebase (using BOM for version management)
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    // Navigation3 (using bundle)
    implementation(libs.bundles.navigation3)

    // Lifecycle (using bundle)
    implementation(libs.bundles.lifecycle)

    // Debug tools
    debugImplementation(libs.leakcanary)
    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.no.op)
}
