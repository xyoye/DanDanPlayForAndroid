import setup.moduleSetup

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
}

moduleSetup()

kapt {
    arguments {
        arg("AROUTER_MODULE_NAME", name)
    }
}

dependencies {
    implementation(Dependencies.Kotlin.stdlib_jdk7)

    implementation(Dependencies.AndroidX.core)
    implementation(Dependencies.AndroidX.room)

    api(Dependencies.Alibaba.arouter_api)
    api(Dependencies.Square.moshi)

    kapt(Dependencies.Square.moshi_codegen)
    kapt(Dependencies.Alibaba.arouter_compiler)
}
android {
    namespace = "com.xyoye.data_component"
}
