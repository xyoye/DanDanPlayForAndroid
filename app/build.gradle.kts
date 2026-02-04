plugins {
    id("org.jetbrains.kotlin.kapt")
    id("dandanplay.android.application")
    id("dandanplay.android.router")
}

android {
    namespace = "com.xyoye.dandanplay"

    defaultConfig {
        applicationId = "com.xyoye.dandanplay"
        versionCode = 61
        versionName = "4.2.0"
    }
}

dependencies {
    implementation(project(":common_component"))
    implementation(project(":player_component"))
    implementation(project(":anime_component"))
    implementation(project(":user_component"))
    implementation(project(":local_component"))
    implementation(project(":storage_component"))
}
