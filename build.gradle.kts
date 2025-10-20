import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import java.util.Properties

// Load version from version.properties
val versionPropsFile = file("version.properties")
val versionProps = Properties().apply {
    if (versionPropsFile.exists()) {
        load(versionPropsFile.inputStream())
    }
}

// Make version info available to all subprojects
extra["versionMajor"] = versionProps.getProperty("VERSION_MAJOR", "1").toInt()
extra["versionMinor"] = versionProps.getProperty("VERSION_MINOR", "0").toInt()
extra["versionPatch"] = versionProps.getProperty("VERSION_PATCH", "0").toInt()
extra["versionCode"] = versionProps.getProperty("VERSION_CODE", "1").toInt()
extra["versionName"] = "${extra["versionMajor"]}.${extra["versionMinor"]}.${extra["versionPatch"]}"

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.detekt) // Applied to root to create aggregated detekt task for CI/CD

    id("zencastr.scaffolding.feature")
}

// Configure Detekt for root project (aggregates all subproject results)
detekt {
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
    parallel = true
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    configure<DetektExtension> {
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        allRules = false
        parallel = true
    }
}
