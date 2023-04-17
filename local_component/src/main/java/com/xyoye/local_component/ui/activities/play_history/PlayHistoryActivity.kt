package com.xyoye.local_component.ui.activities.play_history

import android.view.Menu
import android.view.MenuItem
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.adapter.BaseAdapter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.vertical
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.local_component.BR
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.ActivityPlayHistoryBinding
import com.xyoye.local_component.ui.dialog.MagnetPlayDialog
import com.xyoye.local_component.ui.dialog.StreamLinkDialog
import com.xyoye.local_component.ui.weight.PlayHistoryMenus
import com.xyoye.local_component.utils.HistoryDiffCallback

@Route(path = RouteTable.Local.PlayHistory)
class PlayHistoryActivity : BaseActivity<PlayHistoryViewModel, ActivityPlayHistoryBinding>() {

    @Autowired
    @JvmField
    var typeValue: String = MediaType.LOCAL_STORAGE.value

    private lateinit var mediaType: MediaType

    // 标题栏菜单管理器
    private lateinit var mMenus: PlayHistoryMenus

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            PlayHistoryViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_play_history

    override fun initView() {
        ARouter.getInstance().inject(this)

        mediaType = MediaType.fromValue(typeValue)
        viewModel.mediaType = mediaType

        title = when (mediaType) {
            MediaType.MAGNET_LINK -> "磁链播放"
            MediaType.STREAM_LINK -> "串流播放"
            else -> "播放历史"
        }
        dataBinding.addLinkBt.isVisible = mediaType == MediaType.MAGNET_LINK || mediaType == MediaType.STREAM_LINK

        initRv()

        initListener()
    }

    override fun onResume() {
        super.onResume()

        viewModel.updatePlayHistory()
    }

    private fun initListener() {
        dataBinding.addLinkBt.setOnClickListener {
            if (mediaType == MediaType.STREAM_LINK) {
                showStreamDialog()
            } else if (mediaType == MediaType.MAGNET_LINK) {
                showMagnetDialog()
            }
        }
        viewModel.historyLiveData.observe(this) {
            updateHistoryData(it)
        }
        viewModel.playLiveData.observe(this) {
            ARouter.getInstance()
                .build(RouteTable.Player.Player)
                .navigation()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        mMenus = PlayHistoryMenus.inflater(this, menu)
        mMenus.onClearHistory { viewModel.clearHistory() }
        mMenus.onSortTypeChanged { viewModel.changeSortOption(it) }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        mMenus.onOptionsItemSelected(item)
        return super.onOptionsItemSelected(item)
    }

    private fun initRv() {
        dataBinding.playHistoryRv.apply {
            layoutManager = vertical()

            adapter = PlayHistoryAdapter(
                this@PlayHistoryActivity,
                viewModel
            ).createAdapter()
        }
    }

    private fun updateHistoryData(newData: List<PlayHistoryEntity>) {
        //通过setData实现空布局加载和动画
        if (newData.isEmpty()) {
            dataBinding.playHistoryRv.setData(newData)
            return
        }

        val adapter = dataBinding.playHistoryRv.adapter as BaseAdapter
        val oldData = adapter.items
        val calculateResult = DiffUtil.calculateDiff(
            HistoryDiffCallback(oldData, newData)
        )
        oldData.clear()
        oldData.addAll(newData)
        adapter.resetAnimation()
        calculateResult.dispatchUpdatesTo(adapter)
    }

    private fun showStreamDialog() {
        StreamLinkDialog(this) { link, header ->
            viewModel.openStreamLink(link, header)
        }.show()
    }

    private fun showMagnetDialog() {
        MagnetPlayDialog(this).show()
    }
}