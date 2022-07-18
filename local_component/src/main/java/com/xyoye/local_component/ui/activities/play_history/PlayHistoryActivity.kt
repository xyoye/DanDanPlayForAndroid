package com.xyoye.local_component.ui.activities.play_history

import android.view.Menu
import android.view.MenuItem
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.adapter.addEmptyView
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.databinding.ItemStorageVideoBinding
import com.xyoye.common_component.extension.*
import com.xyoye.common_component.utils.*
import com.xyoye.common_component.weight.BottomActionDialog
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.CommonDialog
import com.xyoye.common_component.weight.dialog.FileManagerDialog
import com.xyoye.data_component.bean.SheetActionBean
import com.xyoye.data_component.entity.PlayHistoryEntity
import com.xyoye.data_component.enums.FileManagerAction
import com.xyoye.data_component.enums.MediaType
import com.xyoye.local_component.BR
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.ActivityPlayHistoryBinding
import com.xyoye.local_component.ui.dialog.MagnetPlayDialog
import com.xyoye.local_component.ui.dialog.StreamLinkDialog
import java.io.File
import java.util.*

@Route(path = RouteTable.Local.PlayHistory)
class PlayHistoryActivity : BaseActivity<PlayHistoryViewModel, ActivityPlayHistoryBinding>() {

    @Autowired
    @JvmField
    var typeValue: String = MediaType.LOCAL_STORAGE.value

    private lateinit var mediaType: MediaType
    private lateinit var mTitleText: String
    private val mHistoryList = mutableListOf<PlayHistoryEntity>()

    companion object {
        private const val ACTION_UNBIND_DANMU = 1
        private const val ACTION_UNBIND_SUBTITLE = 2
        private const val ACTION_COPY_URL = 3
        private const val ACTION_DELETE_HISTORY = 4
    }

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            PlayHistoryViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_play_history

    override fun initView() {
        ARouter.getInstance().inject(this)

        mediaType = MediaType.fromValue(typeValue)

        mTitleText = when (mediaType) {
            MediaType.MAGNET_LINK -> "磁链播放"
            MediaType.STREAM_LINK -> "串流播放"
            else -> "播放历史"
        }
        title = mTitleText

        initRv()

        //添加播放事件
        dataBinding.addLinkBt.setOnClickListener {
            if (mediaType == MediaType.STREAM_LINK) {
                showStreamDialog()
            } else if (mediaType == MediaType.MAGNET_LINK) {
                showMagnetDialog()
            }
        }

        observerPlay()

        //初始化数据源
        viewModel.initHistoryType(mediaType)
        viewModel.showAddButton.set(mediaType == MediaType.MAGNET_LINK || mediaType == MediaType.STREAM_LINK)
        viewModel.playHistoryLiveData.observe(this) {
            mHistoryList.clear()
            mHistoryList.addAll(it)
            dataBinding.playHistoryRv.setData(mHistoryList)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.let {
            menuInflater.inflate(R.menu.menu_history, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.clear_history_item) {
            CommonDialog.Builder().run {
                tips = "清空播放记录"
                content = "清空播放记录，将同时移除弹幕和字幕绑定记录，确定清空?"
                addNegative()
                addPositive {
                    it.dismiss()
                    viewModel.clearHistory(mediaType)
                }
                build()
            }.show(this)
        }
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

                addItem<PlayHistoryEntity, ItemStorageVideoBinding>(R.layout.item_storage_video) {
                    initView { data, _, _ ->
                        var placeholder: String? = null
                        if (data.mediaType == MediaType.LOCAL_STORAGE) {
                            placeholder = data.url
                        }
                        itemBinding.coverIv.setVideoCover(data.uniqueKey, placeholder)
                        itemBinding.durationTv.text =
                            getProgress(data.videoPosition, data.videoDuration)
                        itemBinding.durationTv.isVisible = data.videoDuration > 0

                        val isInvalid = isHistoryInvalid(data)
                        val titleTextColor = if (isInvalid)
                            R.color.text_gray.toResColor()
                        else
                            R.color.text_black.toResColor()
                        itemBinding.titleTv.setTextColor(titleTextColor)
                        itemBinding.titleTv.text = data.videoName

                        itemBinding.mediaTypeTv.isVisible = true
                        itemBinding.mediaTypeTv.text = data.mediaType.storageName

                        itemBinding.lastPlayTimeTv.isVisible = true
                        itemBinding.lastPlayTimeTv.text = PlayHistoryUtils.formatPlayTime(data.playTime)

                        itemBinding.danmuTipsTv.isGone = data.danmuPath.isNullOrEmpty()
                        itemBinding.subtitleTipsTv.isGone = data.subtitlePath.isNullOrEmpty()
                        itemBinding.moreActionIv.isVisible = true

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
                }
            }
        }
    }

    private fun observerPlay() {
        viewModel.playLiveData.observe(this) {
            ARouter.getInstance()
                .build(RouteTable.Player.Player)
                .navigation()
        }
    }

    private fun showStreamDialog() {
        StreamLinkDialog { link, header ->
            viewModel.openStreamLink(link, header)
        }.show(this)
    }

    private fun showMagnetDialog() {
        MagnetPlayDialog(magnetCallback = {
            //磁链选择播放
            ARouter.getInstance()
                .build(RouteTable.Download.PlaySelection)
                .withString("magnetLink", it)
                .navigation()
        }, torrentCallback = {
            //选择本地种子文件
            FileManagerDialog(FileManagerAction.ACTION_SELECT_TORRENT) {
                //磁链选择播放
                ARouter.getInstance()
                    .build(RouteTable.Download.PlaySelection)
                    .withString("torrentPath", it)
                    .navigation()
            }.show(this)
        }).show(this)
    }

    private fun showEditDialog(history: PlayHistoryEntity) {
        val actionList = mutableListOf<SheetActionBean>()
        if (history.danmuPath.isNullOrEmpty().not()) {
            actionList.add(
                SheetActionBean(
                    ACTION_UNBIND_DANMU,
                    "移除弹幕绑定",
                    R.drawable.ic_unbind_danmu
                )
            )
        }
        if (history.subtitlePath.isNullOrEmpty().not()) {
            actionList.add(
                SheetActionBean(
                    ACTION_UNBIND_SUBTITLE,
                    "移除字幕绑定",
                    R.drawable.ic_unbind_subtitle
                )
            )
        }
        actionList.add(
            SheetActionBean(
                ACTION_COPY_URL,
                "复制播放链接",
                R.drawable.ic_copy_url
            )
        )
        actionList.add(
            SheetActionBean(
                ACTION_DELETE_HISTORY,
                "删除播放记录",
                R.drawable.ic_delete_history
            )
        )
        BottomActionDialog(actionList) {
            when (it) {
                ACTION_UNBIND_DANMU -> viewModel.unbindDanmu(history)
                ACTION_UNBIND_SUBTITLE -> viewModel.unbindSubtitle(history)
                ACTION_DELETE_HISTORY -> viewModel.removeHistory(history)
                ACTION_COPY_URL -> {
                    history.url.addToClipboard()
                    ToastCenter.showSuccess("链接已复制！")
                }
            }
            return@BottomActionDialog true
        }.show(this)
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
            MediaType.SMB_SERVER,
            MediaType.FTP_SERVER -> true
            else -> false
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
}