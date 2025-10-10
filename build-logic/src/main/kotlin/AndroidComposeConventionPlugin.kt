import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType

/**
 * Convention plugin for Jetpack Compose
 * Adds Compose dependencies and configuration
 * Must be applied after android.application or android.library plugin
 */
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            // Configure either ApplicationExtension or LibraryExtension
            val androidExtension = extensions.findByType<ApplicationExtension>()
                ?: extensions.findByType<LibraryExtension>()
                ?: error("Android application or library plugin must be applied before compose plugin")

            androidExtension.apply {
                buildFeatures {
                    compose = true
                }
            }

            dependencies {
                val libs = libraries
                val bom = libs.findLibrary("androidx.compose.bom").get()
                add("implementation", dependencies.platform(bom))
                add("implementation", libs.findBundle("compose").get())

                // Debug dependencies
                add("debugImplementation", libs.findLibrary("androidx.compose.ui.tooling").get())
                add("debugImplementation", libs.findLibrary("androidx.compose.ui.test.manifest").get())

                // Test dependencies
                add("androidTestImplementation", dependencies.platform(bom))
                add("androidTestImplementation", libs.findLibrary("androidx.compose.ui.test.junit4").get())
            }
        }
    }
}