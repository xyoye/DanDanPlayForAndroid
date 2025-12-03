plugins {
    alias(dandanplay.plugins.library)
    alias(dandanplay.plugins.router)
    alias(kotlinx.plugins.ksp)
    alias(kotlinx.plugins.parcelize)
}

android {
    namespace = "com.xyoye.data_component"
}

dependencies {
    implementation(androidx.core)
    implementation(androidx.room.runtime)

    api(libs.square.moshi)

    ksp(libs.square.moshi.codegen)
}
