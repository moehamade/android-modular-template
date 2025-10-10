import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Convention plugin for Android application modules
 * Applies common configurations for app modules
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<ApplicationExtension> {
                configureAndroidCommon(this@with)

                defaultConfig {
                    targetSdk = AndroidConfig.TARGET_SDK
                    versionCode = AndroidConfig.VERSION_CODE
                    versionName = AndroidConfig.VERSION_NAME
                }
            }

            configureKotlin()
            configureTestDependencies()
        }
    }
}