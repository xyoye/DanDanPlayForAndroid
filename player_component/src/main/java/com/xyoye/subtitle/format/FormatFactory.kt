package com.xyoye.subtitle.format

import com.xyoye.common_component.utils.getFileExtension
import java.util.*

/**
 * Created by xyoye on 2018/9/20.
 */
object FormatFactory {
    fun findFormat(path: String): TimedTextFileFormat? {
        return when (getFileExtension(path).uppercase(Locale.ROOT)) {
            "ASS" -> FormatASS()
            "SCC" -> FormatSCC()
            "SRT" -> FormatSRT()
            "STL" -> FormatSTL()
            "XML" -> FormatTTML()
            else -> null
        }
    }
}