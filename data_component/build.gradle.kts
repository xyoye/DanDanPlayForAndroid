plugins {
    alias(dandanplay.plugins.library)
    alias(dandanplay.plugins.router)
    alias(kotlinx.plugins.parcelize)
    alias(kotlinx.plugins.serialization)
}

android {
    namespace = "com.xyoye.data_component"
}

dependencies {
    implementation(androidx.core)
    implementation(androidx.room.runtime)

    api(kotlinx.serialization.json)
}
