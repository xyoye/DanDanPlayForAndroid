package com.xyoye.dandanplay

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2025/12/1
 *    desc  : 项目扩展属性
 */
val Project.libs
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")