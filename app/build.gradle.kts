plugins {
    id("zencastr.android.application")
    id("zencastr.android.compose")
    id("zencastr.android.hilt")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf)
}

android {
    namespace = "${AndroidConfig.NAMESPACE_PREFIX}.zencastr"

    flavorDimensions += "environment"

    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String", "API_BASE_URL", "\"https://dev-api.zencastr.com/\"")
            buildConfigField("String", "ENVIRONMENT", "\"development\"")
        }

        create("prod") {
            dimension = "environment"
            buildConfigField("String", "API_BASE_URL", "\"https://api.zencastr.com/\"")
            buildConfigField("String", "ENVIRONMENT", "\"production\"")
        }
    }

    defaultConfig {
        applicationId = "com.acksession.zencastr"
        versionCode = rootProject.extra["versionCode"] as Int
        versionName = rootProject.extra["versionName"] as String
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