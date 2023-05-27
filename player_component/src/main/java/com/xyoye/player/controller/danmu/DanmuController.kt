package com.xyoye.player.controller.danmu

import android.content.Context
import androidx.lifecycle.LiveData
import com.xyoye.data_component.enums.DanmakuLanguage
import com.xyoye.data_component.bean.SendDanmuBean
import com.xyoye.data_component.entity.DanmuBlockEntity
import com.xyoye.data_component.enums.PlayerType
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player.wrapper.InterDanmuController

/**
 * Created by xyoye on 2021/4/14.
 */

class DanmuController(context: Context) : InterDanmuController {

    private val danmuView = DanmuView(context)

    override fun getDanmuUrl(): String? {
        return danmuView.mUrl
    }

    override fun updateDanmuSize() {
        danmuView.updateDanmuSize()
    }

    override fun updateDanmuSpeed() {
        danmuView.updateDanmuSpeed()
    }

    override fun updateDanmuAlpha() {
        danmuView.updateDanmuAlpha()
    }

    override fun updateDanmuStoke() {
        danmuView.updateDanmuStoke()
    }

    override fun updateDanmuOffsetTime() {
        danmuView.updateOffsetTime()
    }

    override fun updateMobileDanmuState() {
        danmuView.updateMobileDanmuState()
    }

    override fun updateTopDanmuState() {
        danmuView.updateTopDanmuState()
    }

    override fun updateBottomDanmuState() {
        danmuView.updateBottomDanmuState()
    }

    override fun updateMaxLine() {
        danmuView.updateMaxLine()
    }

    override fun updateMaxScreenNum() {
        danmuView.updateMaxScreenNum()
    }

    override fun toggleDanmuVisible() {
        danmuView.toggleVis()
    }

    override fun onDanmuSourceChanged(filePath: String, episodeId: Int) {
        danmuView.release()
        danmuView.loadDanmu(filePath)
    }

    override fun allowSendDanmu(): Boolean {
        return danmuView.allowSendDanmu()
    }

    override fun addDanmuToView(danmuBean: SendDanmuBean) {
        danmuView.addDanmuToView(danmuBean)
    }

    override fun addBlackList(isRegex: Boolean, vararg keyword: String) {
        danmuView.addBlackList(isRegex, *keyword)
    }

    override fun removeBlackList(isRegex: Boolean, keyword: String) {
        danmuView.removeBlackList(isRegex, keyword)
    }

    override fun setSpeed(speed: Float) {
        //IJK内核倍速无法按预期加速，导致弹幕倍速会出现偏移，因此禁用
        //倍速小于1的情况下，弹幕没有按预期减速，因此禁用
        if (PlayerInitializer.playerType != PlayerType.TYPE_IJK_PLAYER
            && speed >= 1f
        ) {
            danmuView.setSpeed(speed)
        }
    }

    override fun seekTo(timeMs: Long, isPlaying: Boolean){
        danmuView.seekTo(timeMs, isPlaying)
    }

    override fun setLanguage(language: DanmakuLanguage) {
        danmuView.setLanguage(language)
    }

    override fun danmuRelease() {
        danmuView.release()
    }

    fun setCloudBlockLiveData(cloudBlockLiveData: LiveData<MutableList<DanmuBlockEntity>>?) {
        danmuView.setCloudBlockLiveData(cloudBlockLiveData)
    }

    fun getView() = danmuView
}