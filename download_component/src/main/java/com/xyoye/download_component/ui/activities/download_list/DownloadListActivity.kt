package com.xyoye.download_component.ui.activities.download_list

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.utils.formatFileSize
import com.xyoye.common_component.weight.BottomActionDialog
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.CommonDialog
import com.xyoye.data_component.bean.SheetActionBean
import com.xyoye.data_component.enums.SheetActionType
import com.xyoye.download_component.BR
import com.xyoye.download_component.R
import com.xyoye.download_component.databinding.ActivityDownloadListBinding
import com.xyoye.download_component.databinding.ItemDownloadBinding
import com.xyoye.download_component.frostwire.download.TorrentDownloader
import com.xyoye.download_component.frostwire.utils.TransferState

@Route(path = RouteTable.Download.DownloadList)
class DownloadListActivity : BaseActivity<DownloadListViewModel, ActivityDownloadListBinding>() {
    companion object {
        private const val ACTION_COPY_MAGNET = 1
        private const val ACTION_DELETE_TASK = 2
        private const val ACTION_SHOW_DETAIL = 3
        private const val ACTION_CLEAR_FINISHED = 4
        private const val ACTION_START_SEED = 5
        private const val ACTION_STOP_SEED = 6
        private const val ACTION_PAUSE = 7
        private const val ACTION_RESUME = 8
        private const val ACTION_DELETE_ALL = 9
    }

    @Autowired
    @JvmField
    var torrentPath: String? = null

    @Autowired
    @JvmField
    var selection: ByteArray? = null

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            DownloadListViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_download_list

    override fun initView() {
        ARouter.getInstance().inject(this)

        title = "下载管理"

        initRv()

        viewModel.startTransfer()

        if (torrentPath != null) {
            viewModel.addDownload(torrentPath!!, selection)
        }

        viewModel.transferLiveData.observe(this) {
            dataBinding.downloadRv.setData(it)
        }
    }

    override fun onDestroy() {
        viewModel.stopTransfer()
        super.onDestroy()
    }

    private fun initRv() {
        dataBinding.downloadRv.apply {
            layoutManager = vertical()

            adapter = buildAdapter<TorrentDownloader> {
                addItem<TorrentDownloader, ItemDownloadBinding>(R.layout.item_download) {
                    initView { data, _, _ ->
                        val seedText = "播种: ${data.getConnectedSeeds()}/${data.getTotalSeeds()}"
                        val peerText = "节点: ${data.getConnectedPeers()}/${data.getTotalPeers()}"
                        val progressText =
                            "${formatFileSize(data.getBytesReceived())}/${formatFileSize(data.getSize())}"
                        val speedText = "${formatFileSize(data.getDownloadSpeed())}/s"
                        itemBinding.apply {
                            downloadTitleTv.text = data.getDisplayName()
                            downloadPb.progress = data.getProgress()
                            downloadStatusTv.text = getDisplayStatus(data.getState())
                            downloadSeedTv.text = seedText
                            downloadPeerTv.text = peerText
                            downloadSizeTv.text = progressText
                            downloadSpeedTv.text = speedText
                            downloadDetailIv.setOnClickListener {
                                showDownloaderDetail(data.getInfoHash())
                            }
                            downloadInfoCl.setOnClickListener {
                                BottomActionDialog(
                                    getActionData(data.getState()),
                                    SheetActionType.VERTICAL
                                ) {
                                    disposeAction(it, data)
                                    return@BottomActionDialog true
                                }.show(this@DownloadListActivity)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getDisplayStatus(state: TransferState): String {
        if (TransferState.isErrorState(state))
            return "错误"

        return when (state) {
            TransferState.CHECKING -> "检验中"
            TransferState.FINISHED -> "已完成"
            TransferState.DOWNLOADING_METADATA -> "下载元数据"
            TransferState.DOWNLOADING -> "下载中"
            TransferState.SEEDING -> "播种中"
            TransferState.PAUSED -> "已暂停"
            TransferState.STOPPED -> "已停止"
            else -> "未知"
        }
    }

    private fun getActionData(state: TransferState): MutableList<SheetActionBean> {
        val actionData = mutableListOf<SheetActionBean>()
        when (state) {
            TransferState.FINISHED -> {
                actionData.add(SheetActionBean(ACTION_CLEAR_FINISHED, "移除已完成任务"))
                actionData.add(SheetActionBean(ACTION_START_SEED, "开始播种"))
            }
            TransferState.DOWNLOADING_METADATA,
            TransferState.DOWNLOADING -> {
                actionData.add(SheetActionBean(ACTION_PAUSE, "暂停当前任务"))
            }
            TransferState.SEEDING -> {
                actionData.add(SheetActionBean(ACTION_STOP_SEED, "停止播种"))
            }
            TransferState.PAUSED -> {
                actionData.add(SheetActionBean(ACTION_RESUME, "恢复当前任务"))
            }
            else -> {
            }
        }
        actionData.add(SheetActionBean(ACTION_COPY_MAGNET, "复制磁链"))
        actionData.add(SheetActionBean(ACTION_DELETE_TASK, "删除任务"))
        actionData.add(SheetActionBean(ACTION_DELETE_ALL, "删除任务及文件"))
        actionData.add(SheetActionBean(ACTION_SHOW_DETAIL, "显示任务详情"))
        return actionData
    }

    private fun disposeAction(actionId: Int, downloader: TorrentDownloader) {
        when (actionId) {
            ACTION_RESUME, ACTION_START_SEED -> downloader.resume()
            ACTION_PAUSE, ACTION_STOP_SEED -> downloader.pause()
            ACTION_DELETE_ALL -> removeDownloadConfirm(downloader, true)
            ACTION_DELETE_TASK -> removeDownloadConfirm(downloader, false)
            ACTION_CLEAR_FINISHED -> viewModel.removeFinished()
            ACTION_COPY_MAGNET -> copyMagnetLink(downloader.getInfoHash())
            ACTION_SHOW_DETAIL -> showDownloaderDetail(downloader.getInfoHash())
        }
    }

    private fun removeDownloadConfirm(downloader: TorrentDownloader, deleteData: Boolean) {
        val messageContent = if (deleteData) "确认删除任务及已下载的文件？" else "确认删除任务？"
        CommonDialog.Builder().run {
            content = messageContent
            tips = "删除确认"
            addPositive {
                it.dismiss()
                viewModel.removeDownload(downloader, deleteData)
            }
            addNegative()
            build()
        }.show(this)
    }

    private fun copyMagnetLink(infoHash: String) {
        val magnetLink = "magnet:?xt=urn:btih:$infoHash"
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("data", magnetLink)
        clipboard.setPrimaryClip(clipData)
        ToastCenter.showSuccess("磁链已复制！")
    }

    private fun showDownloaderDetail(infoHash: String) {
        ARouter.getInstance()
            .build(RouteTable.Download.DownloadDetail)
            .withString("infoHash", infoHash)
            .navigation()
    }
}