pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
val templateProperties = java.util.Properties().apply {
    val propertiesFile = file("template.properties")
    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use { load(it) }
    }
}

rootProject.name = templateProperties.getProperty("project.name") ?: "android-modular-template"

include(":app")
include(":core:ui")
include(":core:common")
include(":core:data")
include(":core:domain")
include(":core:navigation")
include(":core:network")
include(":core:datastore:preferences")
include(":core:datastore:proto")
include(":feature")
include(":feature:recording")
include(":feature:recording:api")
include(":feature:profile")
include(":feature:profile:api")
include(":core:analytics")
include(":core:notifications")
include(":core:remoteconfig")
include(":feature:settings")
include(":feature:settings:api")
