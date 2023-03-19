import setup.applicationSetup

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

applicationSetup()

android {
    namespace = "com.xyoye.dandanplay"
    compileSdk = Versions.compileSdkVersion
    defaultConfig {
        applicationId = Versions.applicationId
        minSdk = Versions.minSdkVersion
        targetSdk = Versions.targetSdkVersion
        targetSdk = Versions.targetSdkVersion
        versionCode = Versions.versionCode
        versionName = Versions.versionName
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

//        ndk {
//            abiFilters.add("armeabi-v7a")
//            abiFilters.add("arm64-v8a")
//        }
    }

    flavorDimensions.add("dandanplay")

    productFlavors {
        create("arm64") {
            dimension = "dandanplay"
            ndk {
                abiFilters.clear()
                abiFilters.add("arm64-v8a")
            }
        }
        create("armeabi") {
            dimension = "dandanplay"
            ndk {
                abiFilters.clear()
                abiFilters.add("armeabi-v7a")
            }
        }
    }
}

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
