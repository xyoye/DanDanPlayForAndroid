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

    kapt(Dependencies.Alibaba.arouter_compiler)
}
android {
    namespace = "com.xyoye.download_component"
}
