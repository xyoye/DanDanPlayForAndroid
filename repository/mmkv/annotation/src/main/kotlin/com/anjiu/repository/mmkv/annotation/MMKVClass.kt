package com.anjiu.repository.mmkv.annotation


/**
 *    author: xyoye1997@outlook.com
 *    time  : 2024/4/2
 *    desc  :
 */

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class MMKVClass(
    val className: String,
    val customMMKV: Boolean = false
)
