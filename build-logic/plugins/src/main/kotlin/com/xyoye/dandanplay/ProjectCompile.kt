package com.xyoye.dandanplay

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2025/12/1
 *    desc  : 项目编译配置
 */

internal fun ApplicationExtension.configureCompile() {
    compileSdk = BuildVersion.COMPILE

    defaultConfig {
        minSdk = BuildVersion.MIN
        targetSdk = BuildVersion.TARGET
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

internal fun LibraryExtension.configureCompile() {
    compileSdk = BuildVersion.COMPILE

    defaultConfig {
        minSdk = BuildVersion.MIN
    }

    lint {
        targetSdk = BuildVersion.TARGET
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
