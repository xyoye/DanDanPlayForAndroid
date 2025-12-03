plugins {
    alias(dandanplay.plugins.library)
    alias(dandanplay.plugins.router)
    alias(kotlinx.plugins.kapt)
}

android {
    namespace = "com.xyoye.anime_component"
}

dependencies {
    implementation(project(":common_component"))

    implementation(libs.github.banner)
}