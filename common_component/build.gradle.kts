import setup.moduleSetup

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

moduleSetup()

android {
    buildTypes {
        all {
            buildConfigField("Boolean", "IS_DEBUG_MODE", "${Config.isDebug}")
        }
    }

    sourceSets {
        named("main").configure {
            jniLibs.srcDir("libs")
        }
    }
}

kapt {
    arguments {
        arg("AROUTER_MODULE_NAME", name)
    }
}

dependencies {
    debugImplementation(Dependencies.Square.leakcanary)

    api(project(":data_component"))
    api(project(":repository:seven_zip"))
    api(project(":repository:immersion_bar"))
    api(project(":repository:thunder"))

    api(files("libs/sardine-1.0.1.jar"))
    api(files("libs/simple-xml-2.7.1.jar"))
    implementation(files("libs/mmkv-annotation.jar"))

    api(Dependencies.Kotlin.stdlib_jdk7)
    api(Dependencies.Kotlin.coroutines_core)
    api(Dependencies.Kotlin.coroutines_android)

    api(Dependencies.AndroidX.core)
    api(Dependencies.AndroidX.lifecycle_viewmodel)
    api(Dependencies.AndroidX.lifecycle_runtime)
    api(Dependencies.AndroidX.room_ktx)
    api(Dependencies.AndroidX.constraintlayout)
    api(Dependencies.AndroidX.recyclerview)
    api(Dependencies.AndroidX.swiperefreshlayout)
    api(Dependencies.AndroidX.appcompat)
    api(Dependencies.AndroidX.multidex)
    api(Dependencies.AndroidX.palette)
    api(Dependencies.AndroidX.paging)
    api(Dependencies.AndroidX.startup)
    api(Dependencies.AndroidX.preference)

    api(Dependencies.Google.material)
    api(Dependencies.Apache.commons_net)

    api(Dependencies.Tencent.mmkv)
    api(Dependencies.Tencent.bugly)
    api(Dependencies.Tencent.bugly_native)

    api(Dependencies.Square.retrofit)
    implementation(Dependencies.Square.retrofit_moshi)

    api(Dependencies.Github.glide)
    api(Dependencies.Github.nano_http)
    api(Dependencies.Github.smbj)
    api(Dependencies.Github.dcerpc)

    kapt(files("libs/mmkv-compiler.jar"))
    kapt(Dependencies.AndroidX.room_compiler)
    kapt(Dependencies.Alibaba.arouter_compiler)
}
