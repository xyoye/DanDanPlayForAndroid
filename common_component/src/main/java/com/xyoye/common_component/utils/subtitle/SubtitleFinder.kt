package com.xyoye.common_component.utils.subtitle

import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.utils.getFileExtension
import com.xyoye.common_component.utils.supportSubtitleExtension

/**
 * Created by xyoye on 2023/5/10
 *
 * 根据字幕优先级获取最合适字幕
 */

object SubtitleFinder {
    /**
     * 获取优先字幕
     */
    fun <T> preferred(subtitles: List<T>, videoName: String, subtitleName: (T) -> String): T? {
        //筛选出当前视频的候选字幕
        val sourceNames = subtitles
            .mapIndexed { index, t -> index to subtitleName(t) }
            .filter { isCandidateSubtitle(videoName, it.second) }

        //无候选字幕
        if (sourceNames.isEmpty()) {
            return null
        }
        //只有一个候选字幕
        if (sourceNames.size == 1) {
            return subtitles[sourceNames.first().first]
        }
        //多个候选字幕，根据优先级筛选
        getPriorities().forEach { priority ->
            val preferred = sourceNames.firstOrNull { isPreferred(priority, it.second) }
            if (preferred != null) {
                return subtitles[preferred.first]
            }
        }
        //无匹配优先级，取第一个
        return subtitles[sourceNames.first().first]
    }

    /**
     * 获取优先级规则
     */
    private fun getPriorities(): List<String> {
        return SubtitleConfig.getSubtitlePriority()
            ?.split(",")
            ?: emptyList()
    }

    /**
     * 判断是否是优先字幕
     */
    private fun isPreferred(priority: String, subtitleName: String): Boolean {
        val pointStart = subtitleName.indexOf(".")
        val pointEnd = subtitleName.lastIndexOf(".")
        if (pointEnd <= pointStart) {
            return false
        }
        val compareText = subtitleName.substring(pointStart, pointEnd)
        return compareText.contains(priority)
    }

    /**
     * 判断是否是候选字幕
     */
    private fun isCandidateSubtitle(videoName: String, subtitleName: String): Boolean {
        //获取点之前的视频名
        var compareVideoName = videoName
        val pointIndex = videoName.indexOf(".")
        if (pointIndex != -1) {
            compareVideoName = videoName.substring(0, pointIndex)
        }

        //字幕文件名以视频名为开头
        if (subtitleName.startsWith(compareVideoName).not()) {
            return false
        }

        val extension = getFileExtension(subtitleName)
        //支持的字幕格式
        return supportSubtitleExtension.any {
            it.equals(extension, true)
        }
    }
}