package com.xyoye.local_component.ui.activities.bind_danmu

import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.adapter.addEmptyView
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.AppConfig
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.utils.FastClickFilter
import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.common_component.weight.dialog.FileManagerDialog
import com.xyoye.data_component.bean.DanmuSearchBean
import com.xyoye.data_component.data.DanmuMatchDetailData
import com.xyoye.data_component.enums.FileManagerAction
import com.xyoye.local_component.BR
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.ActivityBindDanmuBinding
import com.xyoye.local_component.databinding.ItemDanmuSourceBinding
import com.xyoye.local_component.ui.dialog.DanmuDownloadDialog
import com.xyoye.local_component.ui.dialog.DanmuSearchDialog
import com.xyoye.local_component.utils.getAnimeType

@Route(path = RouteTable.Local.BindDanmu)
class BindDanmuActivity : BaseActivity<BindDanmuViewModel, ActivityBindDanmuBinding>() {

    @JvmField
    @Autowired
    var videoPath: String? = null

    @JvmField
    @Autowired
    var searchKeyword: String? = null

    @JvmField
    @Autowired
    var videoName: String? = null

    private var danmuDownloadDialog: DanmuDownloadDialog? = null

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            BindDanmuViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_bind_danmu

    override fun initView() {
        ARouter.getInstance().inject(this)

        title = "选绑弹幕"

        dataBinding.danmuRv.apply {
            layoutManager = vertical()

            adapter = buildAdapter {
                addEmptyView(R.layout.layout_empty)

                addItem<DanmuMatchDetailData, ItemDanmuSourceBinding>(R.layout.item_danmu_source) {
                    initView { data, _, _ ->
                        itemBinding.apply {
                            episodeTitleTv.text = data.episodeTitle
                            videoTitleTv.text = data.animeTitle
                            videoTypeTv.text = getAnimeType(data.type)

                            itemLayout.setOnClickListener {
                                if (FastClickFilter.isNeedFilter()) {
                                    return@setOnClickListener
                                }
                                viewModel.getDanmuRelated(data)
                            }
                        }
                    }
                }
            }
        }

        viewModel.sourceLiveData.observe(this) {
            dataBinding.danmuRv.setData(it)
        }

        viewModel.relatedLiveData.observe(this) {

        }

        viewModel.bindSuccessLiveData.observe(this) {
            danmuDownloadDialog?.dismiss()
            val intent = Intent()
            intent.putExtra("danmu_path", it.first)
            intent.putExtra("episode_id", it.second)
            setResult(RESULT_OK, intent)
            finish()
        }

        if (videoPath != null) {
            viewModel.getDanmuSource(videoPath!!)
        } else {
            DanmuSearchDialog(searchKeyword, videoName) { animeName, episodeId ->
                viewModel.searchDanmuSource(animeName, episodeId)
            }.show(this)
        }


        val lastDanmuSearchJson = AppConfig.getLastSearchDanmuJson()
        if (lastDanmuSearchJson.isNullOrEmpty())
            return
        val lastSearchBean = JsonHelper
            .parseJson<DanmuSearchBean>(lastDanmuSearchJson) ?: return
        viewModel.searchDanmuSource(lastSearchBean.animeName, lastSearchBean.episodeId)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bind_danmu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_search_danmu -> {
                DanmuSearchDialog(searchKeyword, videoName) { animeName, episodeId ->
                    viewModel.searchDanmuSource(animeName, episodeId)
                }.show(this)
            }
            R.id.item_local_danmu -> {
                FileManagerDialog(
                    FileManagerAction.ACTION_SELECT_DANMU
                ) {
                    viewModel.bindLocalDanmu(videoPath, it)
                }.show(this)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}