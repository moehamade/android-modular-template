import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Convention plugin for feature modules
 * Applies common configurations for feature modules including:
 * - Android library plugin
 * - Jetpack Compose
 * - Hilt dependency injection
 * - Common dependencies (core modules)
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("zencastr.android.library")
                apply("zencastr.android.compose")
                apply("zencastr.android.hilt")
            }

            extensions.configure<LibraryExtension> {
                defaultConfig {
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }
            }

            dependencies {
                val libs = libraries

                // Core module dependencies
                add("implementation", project.dependencies.project(mapOf("path" to ":core:ui")))
                add("implementation", project.dependencies.project(mapOf("path" to ":core:domain")))
                add("implementation", project.dependencies.project(mapOf("path" to ":core:data")))

                // Lifecycle dependencies
                add("implementation", libs.findBundle("lifecycle").get())

                // Hilt navigation compose
                add("implementation", libs.findLibrary("hilt.navigation.compose").get())

                // Coroutines
                add("implementation", libs.findLibrary("kotlinx.coroutines.android").get())
            }
        }
    }
}
