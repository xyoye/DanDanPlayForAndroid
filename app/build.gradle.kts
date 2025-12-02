plugins {
    alias(dandanplay.plugins.application)
    alias(dandanplay.plugins.router)
}

android {
    namespace = "com.xyoye.dandanplay"

    defaultConfig {
        applicationId = "com.xyoye.dandanplay"
        versionCode = 59
        versionName = "4.1.2"
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
