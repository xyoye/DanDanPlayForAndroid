package com.xyoye.player.wrapper

import com.xyoye.data_component.bean.SendDanmuBean
import com.xyoye.data_component.enums.DanmakuLanguage

/**
 * Created by xyoye on 2021/4/14.
 */

interface InterDanmuController : InterVideoTrack {

    /**
     * 更新弹幕文字大小
     */
    fun updateDanmuSize()

    /**
     * 更新弹幕速度
     */
    fun updateDanmuSpeed()

    /**
     * 更新弹幕文字透明度
     */
    fun updateDanmuAlpha()

    /**
     * 更新弹幕文字描边宽度
     */
    fun updateDanmuStoke()

    /**
     * 更新弹幕偏移时间
     */
    fun updateDanmuOffsetTime()

    /**
     * 更新滚动弹幕状态
     */
    fun updateMobileDanmuState()

    /**
     * 更新顶部弹幕状态
     */
    fun updateTopDanmuState()

    /**
     * 更新底部弹幕状态
     */
    fun updateBottomDanmuState()

    /**
     * 更新弹幕最大行数限制
     */
    fun updateMaxLine()

    /**
     * 更新屏幕最大弹幕数量限制
     */
    fun updateMaxScreenNum()

    /**
     * 改变弹幕的显示状态
     */
    fun toggleDanmuVisible()

    /**
     * 是否允许发送弹幕
     */
    fun allowSendDanmu(): Boolean

    /**
     * 添加弹幕到弹幕视图
     */
    fun addDanmuToView(danmuBean: SendDanmuBean)

    /**
     * 添加弹幕屏蔽
     */
    fun addBlackList(isRegex: Boolean, vararg keyword: String)

    /**
     * 移除弹幕屏蔽
     */
    fun removeBlackList(isRegex: Boolean, keyword: String)

    /**
     * 设置弹幕速度
     */
    fun setSpeed(speed: Float)

    /**
     * 弹幕进度跳转
     */
    fun seekTo(timeMs: Long, isPlaying: Boolean)

    /**
     * 弹幕简繁
     */
    fun setLanguage(language: DanmakuLanguage)

    fun danmuRelease()
}