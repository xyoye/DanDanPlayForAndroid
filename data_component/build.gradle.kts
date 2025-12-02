plugins {
    alias(dandanplay.plugins.library)
    alias(dandanplay.plugins.router)
    id("kotlin-parcelize")
}

android {
    namespace = "com.xyoye.data_component"
}

dependencies {
    implementation(androidx.core)
    implementation(androidx.room.runtime)

    api(libs.alibaba.arouter.api)
    api(libs.square.moshi)

    kapt(libs.square.moshi.codegen)
}
