package setup.utils

import Dependencies
import Versions
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.ApkVariantOutput
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.fileTree
import org.gradle.kotlin.dsl.getByName
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import java.io.ByteArrayOutputStream

fun BaseExtension.setupKotlinOptions() {
    val extensions = (this as ExtensionAware).extensions
    val kotlinOptions = extensions.getByName<KotlinJvmOptions>("kotlinOptions")
    kotlinOptions.apply {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

fun Project.setupDefaultDependencies() {
    dependencies.apply {
        add("implementation", fileTree("include" to listOf("*.jar"), "dir" to "libs"))

        add("testImplementation", Dependencies.Junit.junit)
        add("androidTestImplementation", Dependencies.AndroidX.junit_ext)
        add("androidTestImplementation", Dependencies.AndroidX.espresso)
    }
}

fun Project.currentCommit(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine = "git log --pretty=format:%h -1".split(" ")
        standardOutput = stdout
    }
    return stdout.toString()
}

fun AppExtension.setupSignConfigs(project: Project) = apply {
    signingConfigs {
        named("debug") {
            SignConfig.debug(project, this)
        }

        create("release") {
            SignConfig.release(project, this)
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.findByName(this.name)
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        getByName("release") {
            signingConfig = signingConfigs.findByName(this.name)
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        create("beta") {
            initWith(getByName("release"))
        }
    }
}

fun AppExtension.setupOutputApk() = apply {
    applicationVariants.all {
        outputs.filter { it is ApkVariantOutput }
            .map { it as ApkVariantOutput }
            .onEach {
                it.outputFileName = "dandanplay_v${Versions.versionName}_${it.name}.apk"
            }
    }
}