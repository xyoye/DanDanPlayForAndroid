package com.xyoye.local_component.ui.activities.shooter_subtitle

import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.alibaba.android.arouter.facade.annotation.Route
import com.xyoye.common_component.adapter.paging.BasePagingAdapter
import com.xyoye.common_component.adapter.paging.PagingFooterAdapter
import com.xyoye.common_component.adapter.paging.addItem
import com.xyoye.common_component.adapter.paging.buildPagingAdapter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.extension.setAutoSizeText
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.CommonEditDialog
import com.xyoye.data_component.bean.EditBean
import com.xyoye.data_component.data.SubtitleSearchData
import com.xyoye.local_component.BR
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.ActivityShooterSubtitleBinding
import com.xyoye.local_component.databinding.ItemSubtitleSearchSourceBinding
import com.xyoye.local_component.ui.dialog.ShooterSecretDialog
import com.xyoye.local_component.ui.dialog.SubtitleDetailDialog
import com.xyoye.local_component.ui.dialog.SubtitleFileListDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Route(path = RouteTable.Local.ShooterSubtitle)
class ShooterSubtitleActivity :
    BaseActivity<ShooterSubtitleViewModel, ActivityShooterSubtitleBinding>() {

    private lateinit var subtitleSearchAdapter: BasePagingAdapter<SubtitleSearchData>

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            ShooterSubtitleViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_shooter_subtitle

    override fun initView() {

        title = "射手(伪)字幕下载"

        subtitleSearchAdapter = buildPagingAdapter {

            addItem<SubtitleSearchData, ItemSubtitleSearchSourceBinding>(R.layout.item_subtitle_search_source) {
                initView { data, position, _ ->
                    itemBinding.apply {
                        val type = "格式: ${data.type}"
                        val language = "语言: ${data.language}"

                        positionTv.text = (position + 1).toString()
                        subtitleNameTv.setAutoSizeText(data.name, 12, 14)
                        subtitleFormatTv.text = type
                        subtitleLanguageTv.text = language
                        subtitleTimeTv.text = data.time
                        itemLayout.setOnClickListener {
                            viewModel.getSearchSubDetail(data.id)
                        }
                    }
                }
            }
        }

        dataBinding.refreshLayout.setOnRefreshListener {
            subtitleSearchAdapter.refresh()
        }

        dataBinding.subtitleRv.apply {
            layoutManager = vertical()

            adapter = subtitleSearchAdapter.withLoadStateFooter(
                PagingFooterAdapter { subtitleSearchAdapter.retry() }
            )
        }

        lifecycleScope.launch {
            subtitleSearchAdapter.loadStateFlow.collectLatest {
                dataBinding.refreshLayout.isRefreshing = it.refresh is LoadState.Loading
            }
        }

        initObserver()

        val apiShooterSecret = SubtitleConfig.getShooterSecret()
        if (apiShooterSecret.isNullOrEmpty()) {
            showSecretDialog()
        } else {
            showSearchDialog()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_shooter_subtitle, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_shooter_setting -> {
                showSecretDialog()
            }
            R.id.item_search_subtitle -> {
                val apiShooterSecret = SubtitleConfig.getShooterSecret()
                if (apiShooterSecret.isNullOrEmpty()) {
                    ToastCenter.showError("请先设置API密钥")
                    return true
                }

                showSearchDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initObserver() {
        viewModel.searchSubtitleLiveData.observe(this) {
            subtitleSearchAdapter.submitPagingData(lifecycle, it)
        }

        viewModel.searchSubDetailLiveData.observe(this) {
            SubtitleDetailDialog(it,
                downloadOne = {
                    SubtitleFileListDialog(it.filelist!!) { fileName, url ->
                        viewModel.downloadSubtitle(fileName, url)
                    }.show(this)
                },
                downloadZip = { fileName, url ->
                    viewModel.downloadAndUnzipFile(fileName, url)
                }
            ).show(this)
        }
    }

    private fun showSecretDialog() {
        ShooterSecretDialog().show(this)
    }

    private fun showSearchDialog() {
        CommonEditDialog(
            EditBean(
                "搜索字幕",
                "视频名称不能为空",
                "视频名"
            )
        ) {
            viewModel.searchSubtitle(it)
        }.show(this)
    }
}