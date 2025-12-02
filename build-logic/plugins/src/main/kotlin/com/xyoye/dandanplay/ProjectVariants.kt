package com.xyoye.dandanplay

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.impl.VariantOutputImpl

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2025/12/1
 *    desc  : 项目变体配置
 */

internal fun ApplicationAndroidComponentsExtension.configureVariantName() {
    onVariants { variant ->
        variant.outputs.filterIsInstance<VariantOutputImpl>().onEach { output ->
            val versionName = output.versionName.get()

            val newFileName = "dandanplay_v${versionName}_${output.baseName}.apk"
            output.outputFileName.set(newFileName)
        }
    }
}