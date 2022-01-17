package com.xyoye.stream_component.ui.activities.web_dav_file

import android.view.KeyEvent
import androidx.core.view.isVisible
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.databinding.ItemFileManagerPathBinding
import com.xyoye.common_component.extension.*
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.utils.view.FilePathItemDecoration
import com.xyoye.common_component.weight.BottomActionDialog
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.FilePathBean
import com.xyoye.data_component.bean.SheetActionBean
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.SheetActionType
import com.xyoye.sardine.DavResource
import com.xyoye.stream_component.BR
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.ActivityWebDavFileBinding
import com.xyoye.stream_component.databinding.ItemStorageFolderV2Binding
import com.xyoye.stream_component.databinding.ItemStorageVideoBinding

@Route(path = RouteTable.Stream.WebDavFile)
class WebDavFileActivity : BaseActivity<WebDavFileViewModel, ActivityWebDavFileBinding>() {

    @Autowired
    @JvmField
    var webDavData: MediaLibraryEntity? = null

    companion object {
        private const val ACTION_UNBIND_DANMU = 1
        private const val ACTION_UNBIND_SUBTITLE = 2
    }

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            WebDavFileViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_web_dav_file

    override fun initView() {
        ARouter.getInstance().inject(this)

        if (webDavData == null) {
            ToastCenter.showError("媒体库数据错误，请重试")
            title = "WebDav媒体库"
            return
        }
        title = webDavData!!.displayName

        initRv()

        viewModel.pathLiveData.observe(this) {
            dataBinding.pathRv.setData(it)
        }
        viewModel.fileLiveData.observe(this) {
            dataBinding.fileRv.setData(it)
        }
        viewModel.playLiveData.observe(this) {
            ARouter.getInstance()
                .build(RouteTable.Player.Player)
                .navigation()
        }
        viewModel.listStorageRoot(webDavData!!)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDirectory()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (viewModel.openParentDirectory()) {
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun initRv() {
        dataBinding.pathRv.apply {
            layoutManager = horizontal()

            adapter = buildAdapter {
                addItem<FilePathBean, ItemFileManagerPathBinding>(R.layout.item_file_manager_path) {
                    initView { data, _, _ ->
                        itemBinding.apply {
                            dirNameTv.text = data.name
                            dirNameTv.setTextColorRes(
                                if (data.isOpened) R.color.text_black else R.color.text_gray
                            )
                            dirNameTv.setOnClickListener {
                                viewModel.openDirectory(data.path)
                            }
                        }
                    }
                }
            }

            val dividerSize = dp2px(16)
            val divider = R.drawable.ic_file_manager_arrow.toResDrawable()
            if (divider != null) {
                addItemDecoration(FilePathItemDecoration(divider, dividerSize))
            }
        }

        dataBinding.fileRv.apply {
            layoutManager = vertical()

            adapter = buildAdapter {

                addItem<WebDavFileBean, ItemStorageVideoBinding>(R.layout.item_storage_video) {
                    initView { data, _, _ ->
                        val bean = data as WebDavFileBean
                        itemBinding.coverIv.setVideoCover(bean.uniqueKey)
                        itemBinding.titleTv.text = bean.davSource.name
                        itemBinding.danmuTipsTv.isVisible = bean.danmuPath?.isNotEmpty() ?: false
                        itemBinding.subtitleTipsTv.isVisible =
                            bean.subtitlePath?.isNotEmpty() ?: false

                        itemBinding.itemLayout.setOnClickListener {
                            viewModel.playItem(bean.davSource)
                        }
                        itemBinding.itemLayout.setOnLongClickListener {
                            showVideoManagerDialog(data)
                        }
                    }
                }

                addItem<DavResource, ItemStorageFolderV2Binding>(R.layout.item_storage_folder_v2) {
                    initView { data, _, _ ->
                        val davResource = data as DavResource
                        itemBinding.folderTv.text = davResource.name
                        itemBinding.fileCountTv.text = "目录"
                        itemBinding.itemLayout.setOnClickListener {
                            viewModel.openDirectory(davResource.path)
                        }
                    }
                }
            }
        }
    }

    private fun showVideoManagerDialog(bean: WebDavFileBean): Boolean {
        val actionList = mutableListOf<SheetActionBean>()

        if (bean.uniqueKey.isNullOrEmpty()) {
            return false
        }

        if (!bean.danmuPath.isNullOrEmpty()) {
            actionList.add(
                SheetActionBean(
                    ACTION_UNBIND_DANMU,
                    "移除弹幕绑定",
                    R.drawable.ic_unbind_danmu
                )
            )
        }
        if (!bean.subtitlePath.isNullOrEmpty()) {
            actionList.add(
                SheetActionBean(
                    ACTION_UNBIND_SUBTITLE,
                    "移除字幕绑定",
                    R.drawable.ic_unbind_subtitle
                )
            )
        }
        if (actionList.isEmpty())
            return false

        BottomActionDialog(actionList, SheetActionType.VERTICAL) {
            when (it) {
                ACTION_UNBIND_DANMU -> viewModel.unbindDanmu(bean.uniqueKey)
                ACTION_UNBIND_SUBTITLE -> viewModel.unbindSubtitle(bean.uniqueKey)
            }
            return@BottomActionDialog true
        }.show(this)

        return true
    }
}