package com.xyoye.local_component.ui.activities.local_media

import android.text.TextUtils
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.adapter.addEmptyView
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.*
import com.xyoye.common_component.source.factory.LocalSourceFactory
import com.xyoye.common_component.utils.*
import com.xyoye.common_component.weight.BottomActionDialog
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.FolderBean
import com.xyoye.data_component.bean.SheetActionBean
import com.xyoye.data_component.entity.VideoEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.data_component.enums.SheetActionType
import com.xyoye.local_component.BR
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.ActivityLocalMediaBinding
import com.xyoye.local_component.databinding.ItemMediaFolderBinding
import com.xyoye.local_component.databinding.ItemMediaVideoBinding
import java.io.File

@Route(path = RouteTable.Local.LocalMediaStorage)
class LocalMediaActivity : BaseActivity<LocalMediaViewModel, ActivityLocalMediaBinding>() {

    private var mSearchView: SearchView? = null
    private var mSearchEt: SearchView.SearchAutoComplete? = null

    companion object {
        private const val ACTION_BIND_DANMU_AUTO = 1
        private const val ACTION_BIND_DANMU_MANUAL = 2
        private const val ACTION_BIND_SUBTITLE = 3
        private const val ACTION_UNBIND_DANMU = 4
        private const val ACTION_UNBIND_SUBTITLE = 5
    }

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            LocalMediaViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_local_media

    override fun initView() {
        ARouter.getInstance().inject(this)

        title = "本地媒体库"

        initRv()

        initListener()

        dataBinding.refreshLayout.setColorSchemeResources(R.color.text_theme)
        dataBinding.refreshLayout.isRefreshing = true
        viewModel.listRoot(true)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK && interceptBack())
            true
        else
            super.onKeyDown(keyCode, event)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_local_media, menu)

        mSearchView = menu.findItem(R.id.item_search_file)?.actionView as SearchView
        mSearchView?.apply {
            onActionViewExpanded()
            isIconified = true
            imeOptions = EditorInfo.IME_ACTION_SEARCH

            findViewById<View>(R.id.search_plate).background = null
            findViewById<View>(R.id.submit_area).background = null

            mSearchEt = findViewById(R.id.search_src_text)
            mSearchEt?.textSize = 16f
        }

        initSearchListener()

        return super.onCreateOptionsMenu(menu)
    }

    private fun initRv() {

        dataBinding.mediaRv.apply {
            layoutManager = vertical()

            adapter = buildAdapter<Any> {
                addEmptyView(R.layout.layout_empty) {
                    initEmptyView {
                        itemBinding.emptyTv.text = "找不到相关视频"
                    }
                }

                addItem<Any, ItemMediaFolderBinding>(R.layout.item_media_folder) {
                    checkType { data, _ -> data is FolderBean }
                    initView { data, _, _ ->
                        data as FolderBean
                        itemBinding.apply {
                            val folderName = getFolderName(data.folderPath)
                            folderTv.text = folderName
                            folderTv.setTextColorRes(
                                if (data.isLastPlay) R.color.text_theme else R.color.text_black
                            )

                            val fileCount = "${data.fileCount}视频"
                            fileCountTv.text = fileCount
                            itemLayout.setOnClickListener {
                                mSearchView?.clearFocus()
                                viewModel.listFolder(folderName, data.folderPath)
                            }
                        }
                    }
                }

                addItem<Any, ItemMediaVideoBinding>(R.layout.item_media_video) {
                    checkType { data, _ -> data is VideoEntity }
                    initView { data, position, _ ->
                        data as VideoEntity
                        itemBinding.run {
                            titleTv.setTextColorRes(
                                if (data.isLastPlay) R.color.text_theme else R.color.text_black
                            )
                            titleTv.text = getFileNameNoExtension(data.filePath)
                            durationTv.text = formatDuration(data.videoDuration)
                            setVideoCover(coverIv, data)
                            danmuTipsTv.isVisible = isFileExist(data.danmuPath)
                            subtitleTipsTv.isVisible = isFileExist(data.subtitlePath)

                            itemLayout.setOnClickListener {
                                mSearchView?.clearFocus()
                                viewModel.playItem(position)
                            }
                            moreActionIv.setOnClickListener {
                                mSearchView?.clearFocus()
                                showVideoManagerDialog(data)
                            }
                            itemLayout.setOnLongClickListener {
                                mSearchView?.clearFocus()
                                showVideoManagerDialog(data)
                                true
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initListener() {
        dataBinding.fastPlayBt.setOnClickListener {
            viewModel.fastPlay()
        }

        dataBinding.refreshLayout.setOnRefreshListener {
            viewModel.listRoot(true)
        }

        mToolbar?.setNavigationOnClickListener {
            if (interceptBack().not()) finish()
        }

        viewModel.fileLiveData.observe(this) {
            updateExtViewVisible(viewModel.inSearchState.get().not())
            dataBinding.mediaRv.setData(it)
        }

        viewModel.folderLiveData.observe(this) {
            updateExtViewVisible(true)
            dataBinding.mediaRv.setData(it)
        }

        viewModel.refreshEnableLiveData.observe(this) {
            dataBinding.refreshLayout.isEnabled = it
        }

        viewModel.refreshLiveData.observe(this) { isSuccess ->
            if (dataBinding.refreshLayout.isRefreshing) {
                dataBinding.refreshLayout.isRefreshing = false
            }
            if (!isSuccess) {
                ToastCenter.showError("未找到视频文件")
            }
        }

        //更新界面中的最近播放
        viewModel.lastPlayHistory.observe(this) {
            if (it?.mediaType == MediaType.LOCAL_STORAGE) {
                viewModel.updateLastPlay(it.url)
            }
        }

        viewModel.playLiveData.observe(this) {
            ARouter.getInstance()
                .build(RouteTable.Player.Player)
                .navigation()
        }
    }

    private fun initSearchListener() {
        mSearchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                mSearchView?.clearFocus()
                return false
            }

            override fun onQueryTextChange(keyword: String): Boolean {
                if (TextUtils.isEmpty(keyword)) {
                    viewModel.exitSearchVideo()
                } else {
                    viewModel.searchVideo(keyword)
                }
                return false
            }
        })
    }

    private fun showVideoManagerDialog(data: VideoEntity) {
        val actionList = mutableListOf(
            SheetActionBean(
                ACTION_BIND_DANMU_AUTO,
                "自动匹配弹幕",
                R.drawable.ic_bind_danmu_auto
            ),
            SheetActionBean(
                ACTION_BIND_DANMU_MANUAL,
                "手动查找弹幕",
                R.drawable.ic_bind_danmu_manual
            ),
            SheetActionBean(
                ACTION_BIND_SUBTITLE,
                "手动查找字幕",
                R.drawable.ic_bind_subtitle
            )
        )

        if (!data.danmuPath.isNullOrEmpty()) {
            actionList.add(
                SheetActionBean(
                    ACTION_UNBIND_DANMU,
                    "移除弹幕绑定",
                    R.drawable.ic_unbind_danmu
                )
            )
        }

        if (!data.subtitlePath.isNullOrEmpty()) {
            actionList.add(
                SheetActionBean(
                    ACTION_UNBIND_SUBTITLE,
                    "移除字幕绑定",
                    R.drawable.ic_unbind_subtitle
                )
            )
        }

        BottomActionDialog(actionList, SheetActionType.VERTICAL) {
            when (it) {
                ACTION_BIND_DANMU_AUTO -> viewModel.matchDanmu(data.filePath)
                ACTION_UNBIND_DANMU -> viewModel.removeDanmu(data.filePath)
                ACTION_UNBIND_SUBTITLE -> viewModel.removeSubtitle(data.filePath)
                ACTION_BIND_DANMU_MANUAL -> {
                    ARouter.getInstance()
                        .build(RouteTable.Local.BindDanmu)
                        .withString("videoName", getFileName(data.filePath))
                        .withString("videoPath", data.filePath)
                        .navigation()
                }
                ACTION_BIND_SUBTITLE -> {
                    ARouter.getInstance()
                        .build(RouteTable.Local.BindSubtitle)
                        .withString("videoPath", data.filePath)
                        .navigation()
                }
            }
            return@BottomActionDialog true
        }.show(this)
    }

    private fun updateExtViewVisible(visible: Boolean) {
        dataBinding.pathLl.isVisible = visible
        dataBinding.fastPlayBt.isVisible = visible
    }

    private fun interceptBack(): Boolean {
        if (mSearchEt?.isShown == true) {
            mSearchEt?.setText("")
            mSearchView?.isIconified = true
            return true
        }

        if (!viewModel.inRootFolder.get()) {
            viewModel.listRoot()
            return true
        }

        return false
    }

    private fun setVideoCover(imageView: ImageView, data: VideoEntity) {
        val uniqueKey = LocalSourceFactory.generateUniqueKey(data)
        val coverFile = File(PathHelper.getVideoCoverDirectory(), uniqueKey)
        if (coverFile.exists()) {
            imageView.setGlideImage(coverFile.absolutePath, 5, isCache = false)
        } else {
            if (data.fileId != 0L) {
                val videoUri = IOUtils.getVideoUri(data.fileId)
                imageView.setGlideImage(videoUri, 5)
            }
        }
    }
}