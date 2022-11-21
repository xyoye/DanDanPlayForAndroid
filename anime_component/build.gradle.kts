import setup.moduleSetup

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

moduleSetup()

kapt {
    arguments {
        arg("AROUTER_MODULE_NAME", name)
    }
}

dependencies {
    implementation(project(":common_component"))

    implementation(Dependencies.Kotlin.lib)
    implementation(Dependencies.Github.banner)

    kapt(Dependencies.Alibaba.arouter_compiler)
}
android {
    namespace = "com.xyoye.anime_component"
}
