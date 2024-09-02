package com.xyoye.common_component.config

import com.xyoye.common_component.base.app.BaseApplication

/**
 * Created by xyoye on 2020/7/28.
 */
object DefaultConfig {

    // 默认缓存路径
    val DEFAULT_CACHE_PATH: String by lazy {
        val context = BaseApplication.getAppContext()
        val externalFilesDir = context.getExternalFilesDir(null)
        val externalCacheDir = context.externalCacheDir
        return@lazy (when {
            externalFilesDir != null -> {
                externalFilesDir.absolutePath
            }
            externalCacheDir != null -> {
                externalCacheDir.absolutePath
            }
            else -> {
                context.filesDir.absolutePath
            }
        })
    }

    // 默认Jsoup的User-Agent
    const val DEFAULT_JSOUP_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"
}
