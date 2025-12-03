package com.xyoye.dandanplay

import com.android.build.api.dsl.ApplicationBaseFlavor
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryBaseFlavor
import org.gradle.api.JavaVersion

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2025/12/1
 *    desc  : 项目编译配置
 */

internal fun CommonExtension<*, *, *, *, *>.configureCompile() {
    compileSdk = BuildVersion.COMPILE

    defaultConfig {
        minSdk = BuildVersion.MIN

        val extension = this
        if (extension is ApplicationBaseFlavor) {
            extension.targetSdk = BuildVersion.TARGET
        } else if (extension is LibraryBaseFlavor) {
            lint.targetSdk = BuildVersion.TARGET
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}