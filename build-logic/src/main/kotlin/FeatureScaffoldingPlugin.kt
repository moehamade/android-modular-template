import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import java.io.File
import java.util.Locale

// Plugin to register the task
class FeatureScaffoldingConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        if (project.parent == null) {
            project.tasks.register("createFeature", CreateFeatureTask::class.java) {
                // Connects the command line to the task
                if (project.hasProperty("featureName")) {
                    featureName.set(project.property("featureName") as String)
                }
            }
        }
    }
}

abstract class CreateFeatureTask : DefaultTask() {

    @get:Input
    abstract val featureName: Property<String>

    @TaskAction
    fun run() {
        val name = featureName.get().lowercase(Locale.ROOT).trim()
        if (name.isEmpty() || !name.matches(Regex("[a-z0-9]+"))) {
            error("Invalid feature name '$name'. Use lowercase alphanumeric characters only.")
        }

        val camelCaseName = name.replaceFirstChar { it.titlecase(Locale.ROOT) }
        val namespacePrefix = "com.acksession" // As defined in your project structure
        val rootDir = project.rootDir

        val featureModulePath = "feature/$name"
        val apiModulePath = "feature/$name/api"
        val featureDir = File(rootDir, featureModulePath)
        val apiDir = File(rootDir, apiModulePath)

        if (featureDir.exists() || apiDir.exists()) {
            error("A feature module named '$name' already exists.")
        }

        logger.quiet("Creating feature: '$name'...")

        // Create Dirs
        createSrcDirectories(featureDir, "$namespacePrefix.feature.$name")
        createSrcDirectories(apiDir, "$namespacePrefix.feature.$name.api")

        // Create Build Files
        createFeatureBuildGradle(featureDir, name, namespacePrefix)
        createApiBuildGradle(apiDir, name, namespacePrefix)

        // Create Manifests
        createAndroidManifest(featureDir)
        createAndroidManifest(apiDir)

        // Create NavKey
        createNavKeyFile(apiDir, name, camelCaseName, namespacePrefix)

        // Update Settings
        updateSettingsGradle(rootDir, name)

        logger.quiet("\n✅ Feature '$name' created successfully!")
        logger.quiet("➡️  Sync your project and start building!")
    }

    // --- Helper Functions ---

    private fun createSrcDirectories(moduleDir: File, fullNamespace: String) {
        val packagePath = fullNamespace.replace('.', '/')
        File(moduleDir, "src/main/java/$packagePath").mkdirs()
    }

    private fun createFeatureBuildGradle(moduleDir: File, name: String, namespacePrefix: String) {
        File(moduleDir, "build.gradle.kts").writeText("""
            plugins {
                id("zencastr.android.feature")
            }

            android {
                namespace = "$namespacePrefix.feature.$name"
            }

            dependencies {
                implementation(project(":feature:$name:api"))

                // Add feature-specific dependencies here
            }
        """.trimIndent())
    }

    private fun createApiBuildGradle(moduleDir: File, name: String, namespacePrefix: String) {
        File(moduleDir, "build.gradle.kts").writeText("""
            plugins {
                id("zencastr.android.library")
                alias(libs.plugins.kotlin.serialization)
            }

            android {
                namespace = "$namespacePrefix.feature.$name.api"
            }

            dependencies {
                api(project(":core:navigation"))
                api(libs.bundles.navigation3)
            }
        """.trimIndent())
    }

    private fun createAndroidManifest(moduleDir: File) {
        File(moduleDir, "src/main/AndroidManifest.xml").writeText("""
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android">
            </manifest>
        """.trimIndent())
    }

    private fun createNavKeyFile(apiDir: File, name: String, camelCaseName: String, namespacePrefix: String) {
        val packagePath = "$namespacePrefix.feature.$name.api".replace('.', '/')
        val content = """
            package $namespacePrefix.feature.$name.api

            import $namespacePrefix.navigation.NavKey
            import $namespacePrefix.navigation.Navigator
            import kotlinx.serialization.Serializable

            @Serializable
            sealed interface ${camelCaseName}Route : NavKey {
                @Serializable
                data object ${camelCaseName}Screen : ${camelCaseName}Route
            }

            fun Navigator.navigateTo$camelCaseName() {
                navigateTo(${camelCaseName}Route.${camelCaseName}Screen)
            }
        """.trimIndent()
        File(apiDir, "src/main/java/$packagePath/${camelCaseName}Route.kt").writeText(content)
    }

    private fun updateSettingsGradle(rootDir: File, name: String) {
        val settingsFile = File(rootDir, "settings.gradle.kts")
        settingsFile.appendText("""

            include(":feature:$name")
            include(":feature:$name:api")
        """.trimIndent())
    }
}
