plugins {
    alias(dandanplay.plugins.library)
    alias(dandanplay.plugins.router)
    alias(kotlinx.plugins.ksp)
    alias(kotlinx.plugins.kapt)
    alias(kotlinx.plugins.parcelize)
}

android {
    namespace = "com.xyoye.common_component"

    buildFeatures {
        buildConfig = true
    }

    sourceSets {
        named("main").configure {
            jniLibs.srcDir("libs")
        }
    }
}

dependencies {
    api(project(":data_component"))
    api(project(":repository:seven_zip"))
    api(project(":repository:immersion_bar"))
    api(project(":repository:thunder"))
    implementation(project(":repository:mmkv:annotation"))

    api(files("libs/sardine-1.0.2.jar"))
    api(files("libs/simple-xml-2.7.1.jar"))

    api(androidx.bundles.lifecycle)
    api(androidx.bundles.room)
    api(kotlinx.bundles.coroutines)
    api(libs.bundles.coil)
    api(libs.bundles.retrofit)

    api(androidx.core)
    api(androidx.constraintlayout)
    api(androidx.recyclerview)
    api(androidx.swiperefreshlayout)
    api(androidx.appcompat)
    api(androidx.multidex)
    api(androidx.palette)
    api(androidx.paging)
    api(androidx.startup)
    api(androidx.preference)
    api(androidx.activity.ktx)

    api(libs.alicloud.update)
    api(libs.alicloud.feedback)
    implementation(libs.alicloud.analysis)
    api(libs.apache.commons.net)
    api(libs.github.nanohttpd)
    api(libs.github.smbj)
    api(libs.github.dcerpc)
    api(libs.github.router)
    api(libs.google.material)
    api(libs.tencent.mmkv)
    implementation(libs.tencent.bugly)
    implementation(kotlin("reflect"))

    ksp(androidx.room.compiler)
    ksp(project(":repository:mmkv:processor"))

    debugImplementation(libs.square.leakcanary)
}
