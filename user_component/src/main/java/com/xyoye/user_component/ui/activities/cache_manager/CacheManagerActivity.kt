package com.xyoye.user_component.ui.activities.cache_manager

import com.alibaba.android.arouter.facade.annotation.Route
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.utils.formatFileSize
import com.xyoye.common_component.weight.dialog.CommonDialog
import com.xyoye.data_component.bean.CacheBean
import com.xyoye.data_component.enums.CacheType
import com.xyoye.user_component.BR
import com.xyoye.user_component.R
import com.xyoye.user_component.databinding.ActivityCacheManagerBinding
import com.xyoye.user_component.databinding.ItemCacheTypeBinding

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

        dataBinding.appCacheLl.setOnClickListener {
            considerClearSystemCache()
        }

        dataBinding.rvCache.apply {
            layoutManager = vertical()
            adapter = buildAdapter<CacheBean> {
                addItem<CacheBean, ItemCacheTypeBinding>(R.layout.item_cache_type) {
                    initView { data, _, _ ->
                        var cacheTypeName = data.cacheType?.displayName ?: "其它文件"
                        if (data.fileCount > 0) {
                            cacheTypeName += "（${data.fileCount}）"
                        }
                        var cacheDirName = ""
                        if (data.cacheType != null) {
                            cacheDirName = "文件夹名称：${data.cacheType!!.dirName}"
                        }

                        itemBinding.cacheTypeNameTv.text = cacheTypeName
                        itemBinding.cacheDirNameTv.text = cacheDirName
                        itemBinding.cacheSizeTv.text = formatFileSize(data.totalSize)

                        itemBinding.root.setOnClickListener {
                            considerClearCache(data.cacheType)
                        }
                    }
                }
            }
        }

        viewModel.cacheDirsLiveData.observe(this) {
            dataBinding.rvCache.setData(it)
        }

        viewModel.refreshCache()
    }

    private fun considerClearSystemCache() {
        CommonDialog.Builder().run {
            tips = "清除系统缓存"
            content = "系统缓存包括图片缓存、日志缓存，清除后重新加载图片会消耗流量，确认清除？"
            addPositive { dialog ->
                dialog.dismiss()
                viewModel.clearAppCache()
            }
            addNegative { dialog -> dialog.dismiss() }
            build()
        }.show(this)
    }

    private fun considerClearCache(cacheType: CacheType?) {
        val title = if (cacheType == CacheType.PLAY_CACHE) {
            "清除${cacheType.displayName}"
        } else {
            "清除${cacheType?.displayName ?: "其它文件"}缓存"
        }
        val message = cacheType?.clearTips ?: "确认清除其它缓存？"
        val delay = cacheType == CacheType.DANMU_CACHE || cacheType == CacheType.SUBTITLE_CACHE

        CommonDialog.Builder().run {
            tips = title
            content = message
            delayConfirm = delay
            addPositive {
                it.dismiss()
                viewModel.clearCacheByType(cacheType)
            }
            addNegative { it.dismiss() }
            build()
        }.show(this)
    }
}