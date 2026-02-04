plugins {
    id("org.jetbrains.kotlin.kapt")
    id("dandanplay.android.library")
    id("dandanplay.android.router")
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

fun currentCommit(): String {
    return try {
        val output = ProcessBuilder("git", "log", "--pretty=format:%h", "-1")
            .redirectErrorStream(true)
            .start()
            .inputStream
            .bufferedReader()
            .readText()
            .trim()
        output.ifBlank { "unknown" }
    } catch (ex: Exception) {
        "unknown"
    }
}