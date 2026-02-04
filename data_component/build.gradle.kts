plugins {
    id("org.jetbrains.kotlin.kapt")
    id("dandanplay.android.library")
    id("dandanplay.android.router")
    alias(kotlinx.plugins.parcelize)
    alias(kotlinx.plugins.serialization)
}

android {
    namespace = "com.xyoye.data_component"
}

dependencies {
    implementation(androidx.core)
    implementation(androidx.room.runtime)
    implementation(kotlin("parcelize-runtime"))

    api(kotlinx.serialization.json)
}
