package setup.utils

import com.android.build.gradle.internal.dsl.SigningConfig
import org.gradle.api.Project
import java.io.File
import java.io.FileInputStream
import java.util.*

object SignConfig {

    fun debug(project: Project, config: SigningConfig) {
        val properties = loadProperties(project) ?: return
        config.apply {
            storeFile(project.getAssembleFile(properties["KEY_LOCATION"].toString()))
            storePassword(properties["KEYSTORE_PASS"].toString())
            keyAlias(properties["ALIAS_NAME"].toString())
            keyPassword(properties["ALIAS_PASS"].toString())

            enableV1Signing = true
        }
    }

    fun release(project: Project, config: SigningConfig) {
        val keystoreFile = project.getAssembleFile("dandanplay.jks")
        config.apply {
            storeFile = keystoreFile
            storePassword(System.getenv("KEYSTORE_PASS"))
            keyAlias(System.getenv("ALIAS_NAME"))
            keyPassword(System.getenv("ALIAS_PASS"))
        }
    }

    private fun loadProperties(project: Project): Properties? {
        var propertiesFile = project.getAssembleFile("keystore.properties")
        if (propertiesFile.exists().not()) {
            propertiesFile = project.getAssembleFile("debug.properties")
        }
        if (propertiesFile.exists()) {
            val properties = Properties()
            properties.load(FileInputStream(propertiesFile))
            return properties
        }
        return null
    }

    private fun Project.getAssembleFile(fileName: String): File {
        return File(rootDir,"gradle/assemble/$fileName")
    }
}