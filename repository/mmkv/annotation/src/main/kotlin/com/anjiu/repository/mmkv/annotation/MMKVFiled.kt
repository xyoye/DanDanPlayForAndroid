package com.anjiu.repository.mmkv.annotation


/**
 *    author: xyoye1997@outlook.com
 *    time  : 2024/4/2
 *    desc  :
 */

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class MMKVFiled(
    val key: String = "",
    val commit: Boolean = false
)
