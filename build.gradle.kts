buildscript {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.0.4")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.20")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven("https://developer.huawei.com/repo/")
    }
}

tasks {
    val clean by registering(Delete::class) {
        delete(buildDir)
    }
}