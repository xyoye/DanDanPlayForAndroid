plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("com.android.tools.build:gradle:7.2.2")
    implementation(kotlin("gradle-plugin", "1.7.10"))
}