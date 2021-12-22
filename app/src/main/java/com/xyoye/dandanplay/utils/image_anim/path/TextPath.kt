package com.xyoye.dandanplay.utils.image_anim.path

import android.graphics.Path
import java.util.*

/**
 * Created by xyoye on 2019/9/15.
 */
class TextPath constructor(str: String, scale: Float = 1f, textIntervalPx: Float = 14f) {
    var width = 0f
    var height = 0f
    val path: Path = Path()

    init {
        val pathData = initPathData(str, scale, textIntervalPx)
        for (i in pathData.indices) {
            path.moveTo(pathData[i][0], pathData[i][1])
            path.lineTo(pathData[i][2], pathData[i][3])
        }
    }

    /**
     * 初始化path
     *
     * @param str            文字，范围参照PathUtil
     * @param scale          缩放倍数
     * @param textIntervalPx 文字间距 px
     */
    private fun initPathData(
        str: String,
        scale: Float,
        textIntervalPx: Float
    ): ArrayList<FloatArray> {
        val pathData = ArrayList<FloatArray>()
        //上下左右偏移5px
        val padding = 5
        var offsetForWidth = padding.toFloat()
        for (element in str) {
            val pos = element.code
            val key = TextPathUtils.pointList.indexOfKey(pos)
            if (key == -1) {
                continue
            }
            val points = TextPathUtils.pointList[pos]
            val pointCount = points!!.size / 4
            //当前字最左点
            var minX = 0f
            //当前字最右点
            var maxX = 0f
            //对Path进行缩放，同时获取整体宽高
            for (j in 0 until pointCount) {
                val line = FloatArray(4)
                for (k in 0..3) {
                    val l = points[j * 4 + k]
                    // x
                    if (k % 2 == 0) {
                        line[k] = offsetForWidth + l * scale
                        if (minX == 0f || minX > line[k]) {
                            minX = line[k]
                        }
                        if (maxX == 0f || maxX < line[k]) {
                            maxX = line[k]
                        }
                    } else {
                        line[k] = l * scale + padding
                        if (height == 0f || line[k] > height) {
                            height = line[k]
                        }
                    }
                }
                pathData.add(line)
            }
            val textSize = maxX - minX
            offsetForWidth += textSize + textIntervalPx
        }
        width = offsetForWidth - textIntervalPx + padding
        height += padding.toFloat()
        return pathData
    }
}