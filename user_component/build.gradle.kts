import java.io.ByteArrayOutputStream

plugins {
    alias(dandanplay.plugins.library)
    alias(dandanplay.plugins.router)
    alias(kotlinx.plugins.kapt)
}

android {
    namespace = "com.xyoye.user_component"

    defaultConfig {
        buildConfigField("String", "BUILD_COMMIT", "\"${currentCommit()}\"")

        buildFeatures {
            buildConfig = true
        }
    }
}

dependencies {
    implementation(project(":common_component"))
}

fun Project.currentCommit(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine = "git log --pretty=format:%h -1".split(" ")
        standardOutput = stdout
    }
    return stdout.toString()
}