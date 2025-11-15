import org.gradle.api.JavaVersion

/**
 * Android build configuration constants.
 * Only SDK and build tool versions remain here.
 */
object AndroidConfig {
    const val COMPILE_SDK = 36
    const val MIN_SDK = 30
    const val TARGET_SDK = 36
    val JAVA_VERSION = JavaVersion.VERSION_11
    const val JVM_TARGET = "11"
}
