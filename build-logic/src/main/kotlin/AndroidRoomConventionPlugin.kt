import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Convention plugin for Room database
 * Applies KSP and adds required Room dependencies
 */
class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.devtools.ksp")
            }

            dependencies {
                val libs = libraries
                add("implementation", libs.findBundle("room").get())
                add("ksp", libs.findLibrary("room.compiler").get())
            }
        }
    }
}
