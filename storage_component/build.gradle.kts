plugins {
    alias(dandanplay.plugins.library)
    alias(dandanplay.plugins.router)
}

android {
    namespace = "com.xyoye.storage_component"
}

dependencies {
    implementation(project(":common_component"))

    implementation(libs.huawei.scan)
}