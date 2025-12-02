import java.io.ByteArrayOutputStream

plugins {
    alias(dandanplay.plugins.library)
    alias(dandanplay.plugins.router)
}

android {
    namespace = "com.xyoye.user_component"

    defaultConfig {
        buildConfigField("String", "BUILD_COMMIT", "\"${currentCommit()}\"")
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