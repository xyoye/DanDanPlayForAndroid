package com.xyoye.user_component.ui.activities.cache_manager

import com.alibaba.android.arouter.facade.annotation.Route
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.weight.dialog.CommonDialog
import com.xyoye.data_component.enums.CacheType
import com.xyoye.user_component.BR
import com.xyoye.user_component.R
import com.xyoye.user_component.databinding.ActivityCacheManagerBinding

@Route(path = RouteTable.User.CacheManager)
class CacheManagerActivity : BaseActivity<CacheManagerViewModel, ActivityCacheManagerBinding>() {

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            CacheManagerViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_cache_manager

    override fun initView() {

        title = "缓存目录管理"

        viewModel.confirmCacheLiveData.observe(this) {
            when (it) {
                CacheType.SYSTEM_CACHE -> {
                    showConfirmDialog(
                        it,
                        "清除系统缓存",
                        "系统缓存包括图片缓存、日志缓存，清除后重新加载图片会消耗流量，确认清除？",
                        false
                    )
                }
                CacheType.DANMU_CACHE -> {
                    showConfirmDialog(
                        it,
                        "清除弹幕缓存",
                        "弹幕缓存包括所有匹配、绑定、下载的弹幕，清除后将移除绑定并需要重新下载弹幕，确认清除？",
                        true
                    )
                }
                CacheType.SUBTITLE_CACHE -> {
                    showConfirmDialog(
                        it,
                        "清除字幕缓存",
                        "字幕缓存包括所有匹配、绑定、下载的字幕，清除后将移除绑定并需要重新下载字幕，确认清除？",
                        true
                    )
                }
                CacheType.PLAY_CACHE -> {
                    showConfirmDialog(
                        it,
                        "清除播放缓存",
                        "播放缓存主要为播放网络视频的临时缓存，确认清除？",
                        false
                    )
                }
                CacheType.SCREEN_SHOT_CACHE -> {
                    showConfirmDialog(
                        it,
                        "清除截图缓存",
                        "截图缓存为视频截图缓存，确认清除？",
                        false
                    )
                }
                CacheType.OTHER_CACHE -> {
                    showConfirmDialog(
                        it,
                        "清除其它缓存",
                        "确认清除其它缓存？",
                        false
                    )
                }
                else -> {
                }
            }
        }
    }

    private fun showConfirmDialog(
        cacheType: CacheType,
        title: String,
        message: String,
        delay: Boolean
    ) {
        CommonDialog.Builder().run {
            tips = title
            content = message
            delayConfirm = delay
            addPositive {
                it.dismiss()
                viewModel.confirmClearCache(cacheType)
            }
            addNegative { it.dismiss() }
            build()
        }.show(this)
    }
}