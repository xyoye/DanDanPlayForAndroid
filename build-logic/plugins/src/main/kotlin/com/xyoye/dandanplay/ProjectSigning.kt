package com.xyoye.dandanplay

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Project
import java.io.File
import java.io.FileInputStream
import java.util.Properties

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2025/12/1
 *    desc  : 项目签名配置
 */

private val Project.keysStoreProperties
    get(): Properties? = readProperties("gradle/assemble/keystore.properties")

private val Project.debugKeystoreProperties
    get(): Properties = readProperties("gradle/assemble/debug.properties")
        ?: throw IllegalStateException("debug keystore properties not found")

private val Project.singingKeyFile
    get(): File = File(rootDir, "gradle/assemble/dandanplay.jks")

internal fun ApplicationExtension.configureSigning(
    project: Project
) {
    signingConfigs {
        getByName("debug") {
            val properties = project.keysStoreProperties ?: project.debugKeystoreProperties
            storeFile = File(project.rootDir, properties["KEY_LOCATION"].toString())
            storePassword = properties["KEYSTORE_PASS"].toString()
            keyAlias = properties["ALIAS_NAME"].toString()
            keyPassword = properties["ALIAS_PASS"].toString()

            enableV1Signing = true
            enableV2Signing = true
        }

        create("release") {
            storeFile = project.singingKeyFile
            storePassword = System.getenv("KEYSTORE_PASS")
            keyAlias = System.getenv("ALIAS_NAME")
            keyPassword = System.getenv("ALIAS_PASS")

            enableV1Signing = true
            enableV2Signing = true
        }
    }
}

/**
 * 从配置文件中读取配置信息
 */
private fun Project.readProperties(relativePath: String): Properties? {
    val propertiesFile = File(rootDir, relativePath)
    if (propertiesFile.exists()) {
        val properties = Properties()
        properties.load(FileInputStream(propertiesFile))
        return properties
    }

    return null
}