package com.xyoye.local_component.ui.activities.play_history

import android.view.Menu
import android.view.MenuItem
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.adapter.BaseViewHolderCreator
import com.xyoye.common_component.adapter.addEmptyView
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.databinding.ItemStorageVideoBinding
import com.xyoye.common_component.extension.*
import com.xyoye.common_component.utils.FastClickFilter
import com.xyoye.common_component.utils.PlayHistoryUtils
import com.xyoye.common_component.utils.formatDuration
import com.xyoye.common_component.weight.BottomActionDialog
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.SheetActionBean
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.local_component.BR
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.ActivityPlayHistoryBinding
import com.xyoye.local_component.ui.dialog.MagnetPlayDialog
import com.xyoye.local_component.ui.dialog.StreamLinkDialog
import com.xyoye.local_component.ui.weight.PlayHistoryMenus
import java.io.File

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

        viewModel.updatePlayHistory(mediaType)
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
            dataBinding.playHistoryRv.setData(it)
        }
        viewModel.playLiveData.observe(this) {
            ARouter.getInstance()
                .build(RouteTable.Player.Player)
                .navigation()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        mMenus = PlayHistoryMenus.inflater(this, menu)
        mMenus.onClearHistory { viewModel.clearHistory(mediaType) }
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

            adapter = buildAdapter {
                addEmptyView(R.layout.layout_empty) {
                    initEmptyView {
                        itemBinding.emptyTv.text = "暂无播放记录"
                    }
                }

                addItem(R.layout.item_storage_video) {
                    initView(historyItem())
                }
            }
        }
    }

    private fun BaseViewHolderCreator<ItemStorageVideoBinding>.historyItem() =
        { data: PlayHistoryEntity ->
            itemBinding.coverIv.loadImageByKey(data.uniqueKey)

            itemBinding.durationTv.text =
                getProgress(data.videoPosition, data.videoDuration)
            itemBinding.durationTv.isVisible = data.videoDuration > 0

            val isInvalid = isHistoryInvalid(data)
            val titleTextColor = if (isInvalid)
                R.color.text_gray
            else
                R.color.text_black

            itemBinding.titleTv.setTextColor(titleTextColor.toResColor())
            itemBinding.titleTv.text = data.videoName

            itemBinding.mediaTypeTv.isVisible = true
            itemBinding.mediaTypeTv.text = data.mediaType.storageName

            itemBinding.lastPlayTimeTv.isVisible = true
            itemBinding.lastPlayTimeTv.text = PlayHistoryUtils.formatPlayTime(data.playTime)

            itemBinding.danmuTipsTv.isGone = data.danmuPath.isNullOrEmpty()
            itemBinding.subtitleTipsTv.isGone = data.subtitlePath.isNullOrEmpty()

            itemBinding.itemLayout.setOnClickListener {
                //防止快速点击
                if (FastClickFilter.isNeedFilter())
                    return@setOnClickListener

                if (isInvalid) {
                    ToastCenter.showError("记录已失效，无法播放")
                    return@setOnClickListener
                }
                viewModel.openHistory(data)
            }

            itemBinding.moreActionIv.setOnClickListener {
                showEditDialog(data)
            }
            itemBinding.itemLayout.setOnLongClickListener {
                showEditDialog(data)
                return@setOnLongClickListener true
            }
        }

    private fun isHistoryInvalid(entity: PlayHistoryEntity): Boolean {
        return when (entity.mediaType) {
            MediaType.MAGNET_LINK -> {
                val torrentPath = entity.torrentPath
                //磁链种子文件丢失
                if (torrentPath.isNullOrEmpty() || entity.torrentIndex == -1) {
                    return true
                }
                val torrentFile = File(torrentPath)
                return !torrentFile.exists()
            }
            else -> entity.storageId == null
        }
    }

    private fun getProgress(position: Long, duration: Long): String {
        return if (position > 0 && duration > 0) {
            "${formatDuration(position)}/${formatDuration(duration)}"
        } else if (duration > 0) {
            formatDuration(duration)
        } else {
            ""
        }
    }

    private fun showStreamDialog() {
        StreamLinkDialog(this) { link, header ->
            viewModel.openStreamLink(link, header)
        }.show()
    }

    private fun showMagnetDialog() {
        MagnetPlayDialog(this).show()
    }

    private fun showEditDialog(history: PlayHistoryEntity) {
        val actions = mutableListOf<SheetActionBean>()
        if (history.danmuPath.isNullOrEmpty().not()) {
            actions.add(EditHistory.REMOVE_DANMU.toAction())
        }
        if (history.subtitlePath.isNullOrEmpty().not()) {
            actions.add(EditHistory.REMOVE_SUBTITLE.toAction())
        }
        actions.add(EditHistory.COPY_URL.toAction())
        actions.add(EditHistory.DELETE_HISTORY.toAction())
        BottomActionDialog(this, actions) {
            when (it.actionId) {
                EditHistory.REMOVE_DANMU -> viewModel.unbindDanmu(history)
                EditHistory.REMOVE_SUBTITLE -> viewModel.unbindSubtitle(history)
                EditHistory.DELETE_HISTORY -> viewModel.removeHistory(history)
                EditHistory.COPY_URL -> {
                    history.url.addToClipboard()
                    ToastCenter.showSuccess("链接已复制！")
                }
            }
            return@BottomActionDialog true
        }.show()
    }

    private enum class EditHistory(val title: String, val icon: Int) {
        REMOVE_DANMU("移除弹幕绑定", R.drawable.ic_unbind_danmu),
        REMOVE_SUBTITLE("移除字幕绑定", R.drawable.ic_unbind_subtitle),
        COPY_URL("复制播放链接", R.drawable.ic_copy_url),
        DELETE_HISTORY("删除播放记录", R.drawable.ic_delete_history);

        fun toAction() = SheetActionBean(this, title, icon)
    }
}