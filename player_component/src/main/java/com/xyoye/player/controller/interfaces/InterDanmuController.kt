package com.xyoye.player.controller.interfaces

import com.xyoye.data_component.bean.SendDanmuBean

/**
 * Created by xyoye on 2021/4/14.
 */

interface InterDanmuController {

    fun getDanmuUrl() : String?

    fun updateDanmuSize()

    fun updateDanmuSpeed()

    fun updateDanmuAlpha()

    fun updateDanmuStoke()

    fun updateOffsetTime()

    fun updateMobileDanmuState()

    fun updateTopDanmuState()

    fun updateBottomDanmuState()

    fun updateMaxLine()

    fun updateMaxScreenNum()

    fun toggleDanmuVisible()

    fun onDanmuSourceChanged(filePath: String)

    fun allowSendDanmu(): Boolean

    fun addDanmuToView(danmuBean: SendDanmuBean)

    fun addBlackList(isRegex: Boolean, vararg keyword: String)

    fun removeBlackList(isRegex: Boolean, keyword: String)

    fun setSpeed(speed: Float)
}