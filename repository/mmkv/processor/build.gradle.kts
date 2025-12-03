plugins {
    id("java-library")
    alias(kotlinx.plugins.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(project(":repository:mmkv:annotation"))
    implementation(kotlinx.symbolprocessing)
    implementation(libs.square.kotlinpoet)
    implementation(libs.square.kotlinpoet.ksp)
}
