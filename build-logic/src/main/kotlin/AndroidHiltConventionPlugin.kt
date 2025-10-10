import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Convention plugin for Hilt dependency injection
 * Applies Hilt and adds required dependencies
 */
class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.dagger.hilt.android")
                apply("com.google.devtools.ksp")
            }

            dependencies {
                add("implementation", libraries.findLibrary("hilt.android").get())
                add("ksp", libraries.findLibrary("hilt.compiler").get())
            }
        }
    }
}
