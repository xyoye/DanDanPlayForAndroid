package com.xyoye.common_component.utils.danmu.helper

import com.xyoye.common_component.extension.checkDirectory
import com.xyoye.common_component.extension.isValid
import com.xyoye.common_component.extension.toText
import com.xyoye.common_component.utils.DiskUtils
import com.xyoye.common_component.utils.PathHelper
import java.io.File
import java.io.IOException
import java.util.Date

/**
 * Created by xyoye on 2023/12/28
 * 生成空的弹幕文件
 */

object DanmuFileCreator {

    /**
     * 创建弹幕文件，文件路径的组成为：默认弹幕文件夹 + 番剧名 + 剧集名
     */
    fun create(animeTitle: String, episodeTitle: String): File? {
        try {
            val originDirectoryName = animeTitle.ifEmpty { "未知" }
            val originFileName = episodeTitle.ifEmpty { "DANMU_${Date().toText("yyyy-MM-dd_HH-mm")}" }

            val directoryName = DiskUtils.buildValidFilename(originDirectoryName)
            val fileName = DiskUtils.buildValidFilename(originFileName)

            val animeDirectory = File(PathHelper.getDanmuDirectory(), directoryName)
            if (animeDirectory.checkDirectory().not()) {
                throw IOException("保存弹幕失败，创建父文件夹失败: $directoryName")
            }

            val danmuFile = File(animeDirectory, "$fileName.xml")
            if (danmuFile.isValid()) {
                danmuFile.delete()
            }
            danmuFile.createNewFile()
            return danmuFile
        } catch (e: Exception) {
            return null
        }
    }
}