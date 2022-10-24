package com.xyoye.common_component.extension

import com.xyoye.common_component.utils.EntropyUtils
import java.io.File

/**
 * Created by xyoye on 2021/3/20.
 */

/**
 * 文件是否无效
 */
fun File?.isInvalid() = this == null || !exists() || length() == 0L

/**
 * 文件是否有效
 */
fun File?.isValid() = this != null && exists() && length() != 0L

/**
 * 文件的MD5值
 */
fun File?.md5() = if (this == null) null else EntropyUtils.file2Md5(this)