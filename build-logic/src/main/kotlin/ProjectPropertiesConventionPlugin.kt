import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import java.util.Properties
import javax.inject.Inject

abstract class ProjectProperties @Inject constructor(project: Project) {

    private val templateProperties = Properties().apply {
        val propertiesFile = project.rootProject.file("template.properties")
        if (propertiesFile.exists()) {
            propertiesFile.inputStream().use { load(it) }
        } else {
            project.logger.warn("template.properties file not found!")
        }
    }

    private fun getRequiredProperty(key: String): String {
        return templateProperties.getProperty(key)
            ?: throw IllegalStateException("Required property '$key' not found in template.properties")
    }

    val projectName = getRequiredProperty("project.name")
    val projectNameLowercase = projectName.lowercase()
    val appDisplayName = getRequiredProperty("app.display.name")
    val appPackageName = getRequiredProperty("package.app")

    // Auto-derive base package from app package (remove last segment)
    // E.g., "com.example.myapp" -> "com.example"
    val corePackagePrefix = appPackageName.substringBeforeLast('.')
    val pluginIdPrefix = getRequiredProperty("plugin.id.prefix")
}

@Suppress("unused")
class ProjectPropertiesConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.rootProject.extensions.create<ProjectProperties>("projectProperties", target.rootProject)
    }
}
