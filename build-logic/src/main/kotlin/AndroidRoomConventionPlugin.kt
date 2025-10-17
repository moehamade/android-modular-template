import androidx.room.gradle.RoomExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Convention plugin for Room database
 * Applies androidx.room plugin, KSP, and adds required Room dependencies
 *
 * Follows official Room documentation:
 * https://developer.android.com/training/data-storage/room/migrating-db-versions
 */
class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("androidx.room")
                apply("com.google.devtools.ksp")
            }

            extensions.configure<RoomExtension> {
                schemaDirectory("$projectDir/schemas")
            }

            dependencies {
                val libs = libraries
                add("implementation", libs.findBundle("room").get())
                add("ksp", libs.findLibrary("room.compiler").get())
            }
        }
    }
}
