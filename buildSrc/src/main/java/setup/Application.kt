package setup

import Versions
import com.android.build.gradle.AppExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByName
import setup.utils.setupDefaultDependencies
import setup.utils.setupKotlinOptions
import setup.utils.setupOutputApk
import setup.utils.setupSignConfigs

@Suppress("UnstableApiUsage")
fun Project.applicationSetup() {
    extensions.getByName<AppExtension>("android").apply {
        compileSdkVersion(Versions.compileSdkVersion)
        defaultConfig {
            applicationId = Versions.applicationId
            minSdk = Versions.minSdkVersion
            targetSdk = Versions.targetSdkVersion
            targetSdk = Versions.targetSdkVersion
            versionCode = Versions.versionCode
            versionName = Versions.versionName
            multiDexEnabled = true
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

            ndk {
                abiFilters.add("armeabi-v7a")
                abiFilters.add("arm64-v8a")
            }
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }

        buildFeatures.apply {
            dataBinding.isEnabled = true
        }

        setupKotlinOptions()
        setupSignConfigs(this@applicationSetup)
        setupOutputApk()
    }

    setupDefaultDependencies()
}