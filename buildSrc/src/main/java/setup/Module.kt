package setup

import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByName
import setup.utils.setupDefaultDependencies
import setup.utils.setupKotlinOptions

@Suppress("UnstableApiUsage")
fun Project.moduleSetup() {
    extensions.getByName<LibraryExtension>("android").apply {
        compileSdk = Versions.compileSdkVersion
        defaultConfig {
            minSdk = Versions.minSdkVersion
            targetSdk = Versions.targetSdkVersion
        }

        buildTypes {
            getByName("release") {
                isMinifyEnabled = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }

            getByName("debug") {
                initWith(buildTypes.getByName("release"))
            }

            create("beta") {
                initWith(buildTypes.getByName("release"))
            }
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }

        buildFeatures {
            dataBinding = true
        }

        setupKotlinOptions()
    }

    setupDefaultDependencies()
}