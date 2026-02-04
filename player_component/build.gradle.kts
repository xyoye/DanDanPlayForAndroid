plugins {
    id("org.jetbrains.kotlin.kapt")
    id("dandanplay.android.library")
    id("dandanplay.android.router")
}

android {
    namespace = "com.xyoye.player_component"

    sourceSets {
        getByName("main") {
            jniLibs.srcDir("libs")
        }
    }
}

dependencies {
    implementation(project(":common_component"))
    implementation(project(":repository:panel_switch"))
    implementation(project(":repository:danmaku"))
    implementation(project(":repository:video_cache"))

    implementation(libs.bundles.exoplayer)
    implementation(libs.github.keyboardpanel)
    implementation(libs.videolan.vlc)

    // TODO 暂时移除，编译出64位后再考虑重新添加
    //implementation "com.github.ctiao:ndkbitmap-armv7a:0.9.21"
}
