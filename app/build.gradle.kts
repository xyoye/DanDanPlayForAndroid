import setup.applicationSetup

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

applicationSetup()

kapt {
    arguments {
        arg("AROUTER_MODULE_NAME", project.name)
    }
}

dependencies {
    implementation(project(":common_component"))
    implementation(project(":download_component"))
    implementation(project(":player_component"))
    implementation(project(":anime_component"))
    implementation(project(":user_component"))
    implementation(project(":local_component"))
    implementation(project(":stream_component"))

    kapt(Dependencies.Alibaba.arouter_compiler)
}
android {
    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}
