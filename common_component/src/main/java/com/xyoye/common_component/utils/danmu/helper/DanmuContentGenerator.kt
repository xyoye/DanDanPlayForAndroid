package com.xyoye.common_component.utils.danmu.helper

import com.xyoye.data_component.data.DanmuContentData

/**
 * Created by xyoye on 2023/12/27
 * 弹幕内容生成器
 */

object DanmuContentGenerator {

    private const val LINE_SEPARATOR = "\n"

    /**
     * 生成弹幕文件Xml格式内容
     */
    fun generate(comments: List<DanmuContentData>): String? {
        val contents = comments.mapNotNull { generateXmlLine(it) }
        if (contents.isEmpty()) {
            return null
        }

        val xmlHeader = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
        val contentText = contents.joinToString(LINE_SEPARATOR)
        return "$xmlHeader<i>$LINE_SEPARATOR$contentText$LINE_SEPARATOR</i>"
    }

    /**
     * 将弹弹弹幕转换为xml格式
     */
    private fun generateXmlLine(content: DanmuContentData): String? {
        // 弹幕内容不能为空
        if (content.m.isEmpty())
            return null

        // 弹幕参数不能为空
        val params = content.p.split(",")
        if (params.isEmpty()) {
            return null
        }

        // 获取弹幕参数
        val time = params[0]
        val type = params.getOrNull(1) ?: "1"
        val textColor = params.getOrNull(2) ?: "16777215"
        val timestamp = params.getOrNull(3) ?: "0"

        return generateXmlLine(time, type, textColor, timestamp, content.m)
    }

    /**
     * 生成弹幕文件Xml格式一行内容
     * @param time 弹幕出现时间
     * @param type 弹幕类型
     * @param timestamp Unix时间戳
     * @param textColor 弹幕颜色
     * @param content 弹幕内容
     * @param textSize 弹幕字体大小
     * @param poolId 弹幕池ID
     * @param senderId 发送者ID
     * @param rowId 弹幕在弹幕数据库中rowID
     */
    private fun generateXmlLine(
        time: String,
        type: String,
        textColor: String,
        timestamp: String,
        content: String,
        textSize: String = "25",
        poolId: String = "0",
        senderId: String = "0",
        rowId: String = "0"
    ): String? {
        if (content.isEmpty())
            return null
        // 弹幕颜色异常时，设置为白色
        val correctedTextColor = correctTextColor(textColor)
        // 弹幕内容特殊字符转义
        val encodedContent = encodeContent(content)
        return "<d p=\"$time,$type,$textSize,$correctedTextColor,$timestamp,$poolId,$senderId,$rowId\">$encodedContent</d>"
    }

    /**
     * 修改弹幕颜色
     */
    private fun correctTextColor(textColor: String): String {
        return if (textColor.isEmpty() || "0" == textColor || "-1" == textColor) {
            "16777215"
        } else {
            textColor
        }
    }

    /**
     * 转义弹幕内容特殊字符
     */
    private fun encodeContent(content: String): String {
        var encoded = content
        if (encoded.contains("&")) {
            encoded = encoded.replace("&", "&amp;")
        }
        if (encoded.contains("\"")) {
            encoded = encoded.replace("\"", "&quot;")
        }
        if (encoded.contains(">")) {
            encoded = encoded.replace(">", "&gt;")
        }
        if (encoded.contains("<")) {
            encoded = encoded.replace("<", "&lt;")
        }
        return encoded
    }
}