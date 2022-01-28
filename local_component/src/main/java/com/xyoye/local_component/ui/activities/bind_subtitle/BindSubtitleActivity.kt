package com.xyoye.local_component.ui.activities.bind_subtitle

import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.adapter.addEmptyView
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.adapter.paging.BasePagingAdapter
import com.xyoye.common_component.adapter.paging.PagingFooterAdapter
import com.xyoye.common_component.adapter.paging.addItem
import com.xyoye.common_component.adapter.paging.buildPagingAdapter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.CommonDialog
import com.xyoye.common_component.weight.dialog.CommonEditDialog
import com.xyoye.common_component.weight.dialog.FileManagerDialog
import com.xyoye.data_component.bean.EditBean
import com.xyoye.data_component.data.SubtitleMatchData
import com.xyoye.data_component.data.SubtitleSourceBean
import com.xyoye.data_component.enums.FileManagerAction
import com.xyoye.local_component.BR
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.ActivityBindSubtitleBinding
import com.xyoye.local_component.databinding.ItemSubtitleSearchSourceBinding
import com.xyoye.local_component.databinding.ItemSubtitleSourceBinding
import com.xyoye.local_component.ui.dialog.SubtitleDetailDialog
import com.xyoye.local_component.ui.dialog.SubtitleFileListDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Route(path = RouteTable.Local.BindSubtitle)
class BindSubtitleActivity : BaseActivity<BindSubtitleViewModel, ActivityBindSubtitleBinding>() {

    @JvmField
    @Autowired
    var videoPath: String? = null

    private lateinit var subtitleSearchAdapter: BasePagingAdapter<SubtitleSourceBean>

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            BindSubtitleViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_bind_subtitle

    override fun initView() {
        ARouter.getInstance().inject(this)

        title = "选绑字幕"

        initObserver()

        dataBinding.refreshLayout.apply {
            setOnRefreshListener {
                subtitleSearchAdapter.refresh()
            }
            isEnabled = false
        }

        dataBinding.subtitleRv.apply {
            layoutManager = vertical()

            adapter = buildAdapter {
                addEmptyView(R.layout.layout_empty)

                addItem<SubtitleMatchData, ItemSubtitleSourceBinding>(R.layout.item_subtitle_source) {
                    initView { data, _, _ ->
                        itemBinding.apply {
                            subtitleTitleTv.text = data.name
                            subtitleOriginTv.text = data.origin
                            val rateText = "${data.rate}星"
                            subtitleRateTv.text = rateText
                            itemLayout.setOnClickListener {
                                viewModel.downloadSubtitle(videoPath!!, data.name, data.url)
                            }
                        }
                    }
                }
            }
        }

        subtitleSearchAdapter = buildPagingAdapter {

            addItem<SubtitleSourceBean, ItemSubtitleSearchSourceBinding>(R.layout.item_subtitle_search_source) {
                initView { data, position, _ ->
                    itemBinding.apply {
                        val language = "语言: ${data.language}"

                        positionTv.text = (position + 1).toString()
                        subtitleNameTv.text = data.name
                        subtitleDescribeTv.text = language
                        itemLayout.setOnClickListener {
                            viewModel.getSearchSubDetail(data.id)
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            subtitleSearchAdapter.loadStateFlow.collectLatest {
                dataBinding.refreshLayout.isRefreshing = it.refresh is LoadState.Loading
            }
        }

        videoPath?.let {
            viewModel.matchSubtitleSource(it)
        }
    }

    private fun initObserver() {
        viewModel.sourceLiveData.observe(this) {
            dataBinding.subtitleRv.setData(it)
        }

        viewModel.bindResultLiveData.observe(this) {
            if (it) {
                ToastCenter.showSuccess("绑定字幕成功！")
                finish()
            } else {
                ToastCenter.showError("保存字幕失败！")
            }
        }

        viewModel.searchSubtitleLiveData.observe(this) {
            subtitleSearchAdapter.submitPagingData(lifecycle, it)
        }

        viewModel.searchSubDetailLiveData.observe(this) {
            SubtitleDetailDialog(it,
                downloadOne = {
                    SubtitleFileListDialog(it.filelist!!) { fileName, url ->
                        viewModel.downloadSubtitle(videoPath!!, fileName, url)
                    }.show(this)
                },
                downloadZip = { fileName, url ->
                    viewModel.downloadAndUnzipFile(fileName, url)
                }
            ).show(this)
        }

        viewModel.unzipResultLiveData.observe(this) {
            FileManagerDialog(
                FileManagerAction.ACTION_SELECT_SUBTITLE,
                it
            ) { resultPath ->
                viewModel.bindLocalSubtitle(videoPath!!, resultPath)
                ToastCenter.showSuccess("绑定字幕成功！")
                finish()
            }.show(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bind_subtitle, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_search_subtitle -> {
                val shooterSecret = SubtitleConfig.getShooterSecret()
                if (shooterSecret.isNullOrEmpty()) {
                    showSecretDialog()
                } else {
                    showSearchDialog()
                }
            }
            R.id.item_local_subtitle -> {
                showFileManagerDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 密钥输入弹窗
     */
    private fun showSecretDialog() {
        CommonDialog.Builder().run {
            content = "密钥为空无法搜索\n\n请到 个人中心->射手(伪)字幕下载 中设置API密钥"
            addPositive("前往设置") {
                it.dismiss()
                ARouter.getInstance()
                    .build(RouteTable.Local.ShooterSubtitle)
                    .navigation()
            }
            addNegative()
            build()
        }.show(this)
    }

    /**
     * 字幕搜索弹窗
     */
    private fun showSearchDialog() {
        val editBean = EditBean(
            "搜索字幕",
            "视频名称不能为空",
            "视频名"
        )

        CommonEditDialog(editBean) {
            if (dataBinding.subtitleRv.adapter !is ConcatAdapter) {
                dataBinding.subtitleRv.adapter = subtitleSearchAdapter.withLoadStateFooter(
                    PagingFooterAdapter { subtitleSearchAdapter.retry() }
                )
                dataBinding.refreshLayout.isEnabled = true
            }
            viewModel.searchSubtitle(it)
        }.show(this)
    }

    /**
     * 本地文件弹窗
     */
    private fun showFileManagerDialog() {
        FileManagerDialog(
            FileManagerAction.ACTION_SELECT_SUBTITLE
        ) {
            viewModel.bindLocalSubtitle(videoPath!!, it)
            ToastCenter.showSuccess("绑定字幕成功！")
            finish()
        }.show(this)
    }
}