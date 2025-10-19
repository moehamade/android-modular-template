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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Zencastr"
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
