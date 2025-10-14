import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Convention plugin for modules that need network dependencies (Retrofit, OkHttp).
 *
 * Usage:
 * ```
 * plugins {
 *     id("zencastr.android.library")
 *     id("zencastr.android.network")
 *     alias(libs.plugins.kotlin.serialization)
 * }
 * ```
 *
 * Note: The kotlin.serialization plugin must be applied manually in each module
 * because plugin classpath dependencies can't be easily shared from convention plugins.
 */
class AndroidNetworkConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            dependencies {
                val libs = libraries

                // Retrofit
                add("implementation", libs.findLibrary("retrofit").get())
                add("implementation", libs.findLibrary("retrofit.kotlinx.serialization").get())

                // OkHttp
                add("implementation", libs.findLibrary("okhttp").get())
                add("implementation", libs.findLibrary("okhttp.logging").get())

                // Kotlinx Serialization
                add("implementation", libs.findLibrary("kotlinx.serialization.json").get())
            }
        }
    }
}
