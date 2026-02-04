plugins {
    id("org.jetbrains.kotlin.kapt")
    id("dandanplay.android.library")
    id("dandanplay.android.router")
}

android {
    namespace = "com.xyoye.local_component"
}

dependencies {
    implementation(project(":common_component"))

    implementation(libs.github.jsoup)
}
