import setup.moduleSetup

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

moduleSetup()

android {
    sourceSets {
        getByName("main") {
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

    implementation(project(":common_component"))
    implementation(project(":repository:panel_switch"))
    implementation(project(":repository:danmaku"))

    implementation(Dependencies.Github.keyboard_panel)

    // TODO 暂时移除，编译出64位后再考虑重新添加
    //implementation "com.github.ctiao:ndkbitmap-armv7a:0.9.21"

    implementation(Dependencies.Google.exoplayer)
    implementation(Dependencies.Google.exoplayer_core)
    implementation(Dependencies.Google.exoplayer_dash)
    implementation(Dependencies.Google.exoplayer_hls)
    implementation(Dependencies.Google.exoplayer_smoothstraming)
    implementation(Dependencies.Google.exoplayer_rtmp)

    implementation(Dependencies.VLC.vlc)

    kapt(Dependencies.Alibaba.arouter_compiler)
}
