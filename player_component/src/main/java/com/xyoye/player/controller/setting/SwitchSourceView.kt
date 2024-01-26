package com.xyoye.player.controller.setting

import android.content.Context
import android.os.Environment
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import androidx.annotation.Dimension
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.adapter.setupVerticalAnimation
import com.xyoye.common_component.config.AppConfig
import com.xyoye.common_component.extension.filterHiddenFile
import com.xyoye.common_component.extension.findIndexOnLeft
import com.xyoye.common_component.extension.findIndexOnRight
import com.xyoye.common_component.extension.horizontal
import com.xyoye.common_component.extension.isValid
import com.xyoye.common_component.extension.nextItemIndex
import com.xyoye.common_component.extension.previousItemIndex
import com.xyoye.common_component.extension.requestIndexChildFocus
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.setTextColorRes
import com.xyoye.common_component.extension.toResDrawable
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.utils.PathHelper
import com.xyoye.common_component.utils.comparator.FileNameComparator
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.utils.getFileName
import com.xyoye.common_component.utils.getFolderName
import com.xyoye.common_component.utils.isAudioFile
import com.xyoye.common_component.utils.isDanmuFile
import com.xyoye.common_component.utils.isSubtitleFile
import com.xyoye.common_component.utils.view.FilePathItemDecoration
import com.xyoye.common_component.utils.view.ItemDecorationOrientation
import com.xyoye.data_component.bean.FileManagerBean
import com.xyoye.data_component.bean.FilePathBean
import com.xyoye.data_component.bean.LocalDanmuBean
import com.xyoye.data_component.bean.VideoTrackBean
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.data_component.enums.TrackType
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.ItemFileManagerFolderBinding
import com.xyoye.player_component.databinding.ItemFileManagerPlayerBinding
import com.xyoye.player_component.databinding.LayoutSwitchSourceBinding
import java.io.File

/**
 * Created by xyoye on 2021/2/15.
 */

class SwitchSourceView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseSettingView<LayoutSwitchSourceBinding>(context, attrs, defStyleAttr) {

    private val mRootPath = Environment.getExternalStorageDirectory().absolutePath

    private val mPathData = mutableListOf<FilePathBean>()
    private val mCommonDirectoryData = mutableListOf<FilePathBean>()
    private val mFileData = mutableListOf<FileManagerBean>()

    private var mTrackType = TrackType.DANMU

    // 标题
    private val title
        get() = when (mTrackType) {
            TrackType.AUDIO -> "选择音轨文件"
            TrackType.DANMU -> "选择弹幕轨文件"
            TrackType.SUBTITLE -> "选择字幕轨文件"
        }

    // 是否显示搜索网络弹幕按钮
    private val sourceSearchAble get() = mTrackType == TrackType.DANMU

    // 文件图标
    private val sourceFileIcon
        get() = when (mTrackType) {
            TrackType.AUDIO -> R.drawable.ic_file_audio
            TrackType.DANMU -> R.drawable.ic_file_xml
            TrackType.SUBTITLE -> R.drawable.ic_file_subtitle
        }

    init {
        initView()

        initListener()
    }

    override fun getLayoutId() = R.layout.layout_switch_source

    override fun getSettingViewType() = SettingViewType.SWITCH_SOURCE

    override fun getGravity() = Gravity.START

    override fun onViewShow() {
        viewBinding.titleTv.text = title
        openDirectory(getDefaultOpenDirectory())

        mCommonDirectoryData.clear()
        mCommonDirectoryData.addAll(getCommonDirectoryList())
        viewBinding.rvCommonFolder.setData(mCommonDirectoryData)

        viewBinding.tvSearchNetworkDanmu.isVisible = sourceSearchAble
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (isSettingShowing().not()) {
            return false
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            return true
        }

        //KeyCode对应的View
        val handled = handleKeyCode(keyCode)
        if (handled) {
            return true
        }

        if (mFileData.size > 0) {
            viewBinding.rvFile.requestIndexChildFocus(0)
        } else {
            if (viewBinding.tvSearchNetworkDanmu.isVisible) {
                viewBinding.tvSearchNetworkDanmu.requestFocus()
            } else {
                viewBinding.rvCommonFolder.requestIndexChildFocus(0)
            }
        }
        return true
    }

    fun setTrackType(trackType: TrackType) {
        this.mTrackType = trackType
    }

    private fun initView() {
        viewBinding.rvCommonFolder.apply {
            layoutManager = horizontal()

            adapter = buildAdapter {
                addItem<FilePathBean, ItemFileManagerFolderBinding>(R.layout.item_file_manager_folder) {
                    initView { data, _, _ ->
                        itemBinding.apply {
                            tvName.background =
                                R.drawable.background_player_setting_text.toResDrawable()
                            tvName.text = data.name
                            tvName.setOnClickListener {
                                openDirectory(data.path)
                            }
                        }
                    }
                }
            }

            addItemDecoration(
                ItemDecorationOrientation(
                    dp2px(5),
                    0,
                    RecyclerView.HORIZONTAL
                )
            )
        }

        viewBinding.rvPath.apply {
            layoutManager = horizontal()

            adapter = buildAdapter {
                addItem<FilePathBean, ItemFileManagerFolderBinding>(R.layout.item_file_manager_folder) {
                    initView { data, _, _ ->
                        itemBinding.apply {
                            tvName.background =
                                R.drawable.background_player_setting_text_transparent.toResDrawable()
                            tvName.text = data.name
                            tvName.setTextSize(Dimension.SP, 14f)
                            tvName.setTextColorRes(
                                if (data.isOpened) R.color.text_white_immutable else R.color.text_gray
                            )
                            tvName.setOnClickListener {
                                openDirectory(data.path)
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

            itemAnimator = null

            setData(mPathData)
        }

        viewBinding.rvFile.apply {
            layoutManager = vertical()

            adapter = buildAdapter {
                setupVerticalAnimation()

                addItem<FileManagerBean, ItemFileManagerPlayerBinding>(R.layout.item_file_manager_player) {
                    initView { data, _, _ ->
                        itemBinding.apply {
                            fileNameTv.text = data.fileName
                            fileIv.setImageResource(if (data.isDirectory) R.drawable.ic_folder else sourceFileIcon)
                            itemLayout.setOnClickListener {
                                when {
                                    data.isDirectory -> {
                                        openDirectory(data.filePath)
                                    }

                                    else -> {
                                        openFile(data)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initListener() {
        viewBinding.tvSearchNetworkDanmu.setOnClickListener {
            mControlWrapper.showSettingView(SettingViewType.SEARCH_DANMU)
            onSettingVisibilityChanged(false)
        }
    }

    /**
     * 选中目标文件
     */
    private fun openFile(data: FileManagerBean) {
        onSettingVisibilityChanged(false)

        File(data.filePath).parentFile?.absolutePath?.let {
            AppConfig.putLastOpenFolder(it)
        }

        when (mTrackType) {
            TrackType.AUDIO -> {
                mControlWrapper.addTrack(VideoTrackBean.audio(data.filePath))
            }

            TrackType.DANMU -> {
                mControlWrapper.addTrack(VideoTrackBean.danmu(LocalDanmuBean(data.filePath)))
            }

            TrackType.SUBTITLE -> {
                mControlWrapper.addTrack(VideoTrackBean.subtitle(data.filePath))
            }
        }
    }

    /**
     * 打开文件夹
     */
    private fun openDirectory(directoryPath: String) {
        val directory = File(directoryPath)
        val pathData = getPathData(directory.absolutePath)
        if (pathData.isNotEmpty()) {
            pathData.last().isOpened = true
        }
        mPathData.clear()
        mPathData.addAll(pathData)
        viewBinding.rvPath.setData(mPathData)
        viewBinding.rvPath.scrollToPosition(mPathData.size - 1)

        mFileData.clear()
        mFileData.addAll(getDirectoryChildData(directory))
        viewBinding.rvFile.setData(mFileData)
    }

    /**
     * 获取文件夹名称列表
     */
    private fun getPathData(directoryPath: String): List<FilePathBean> {
        if (directoryPath.isEmpty()) {
            return emptyList()
        }
        val pathData = mutableListOf<FilePathBean>()
        var directoryFile: File? = File(directoryPath)
        while (directoryFile != null) {
            if (directoryFile.absolutePath == mRootPath) {
                pathData.add(FilePathBean("根目录", directoryFile.absolutePath))
                break
            }
            pathData.add(FilePathBean(directoryFile.name, directoryFile.absolutePath))
            directoryFile = directoryFile.parentFile
        }
        return pathData.reversed()
    }

    /**
     * 获取文件夹内文件信息
     */
    private fun getDirectoryChildData(directory: File): List<FileManagerBean> {
        val fileManagerData = mutableListOf<FileManagerBean>()
        if (!directory.exists() || !directory.isDirectory)
            return fileManagerData
        val childFiles = directory.listFiles() ?: return fileManagerData

        for (childFile in childFiles) {
            when {
                childFile.isDirectory -> {
                    fileManagerData.add(
                        FileManagerBean(
                            childFile.absolutePath,
                            getFolderName(childFile.absolutePath),
                            isDirectory = true
                        )
                    )
                }

                isTargetFile(childFile.absolutePath) -> {
                    fileManagerData.add(
                        FileManagerBean(
                            childFile.absolutePath,
                            getFileName(childFile.absolutePath),
                            isDirectory = false
                        )
                    )
                }
            }
        }

        return fileManagerData
            .asSequence()
            .filterHiddenFile { it.fileName }
            .sortedWith(FileNameComparator(
                getName = { it.fileName },
                isDirectory = { it.isDirectory }
            ))
    }

    /**
     * 是否为目标文件类型
     */
    private fun isTargetFile(filePath: String): Boolean {
        return when (mTrackType) {
            TrackType.AUDIO -> isAudioFile(filePath)
            TrackType.DANMU -> isDanmuFile(filePath)
            TrackType.SUBTITLE -> isSubtitleFile(filePath)
        }
    }

    /**
     * 获取常用目录列表
     */
    private fun getCommonDirectoryList(): MutableList<FilePathBean> {
        val commonDirectoryList = mutableListOf<FilePathBean>()

        //常用目录1
        val commonlyFolder1 = AppConfig.getCommonlyFolder1()
        if (commonlyFolder1?.isNotEmpty() == true) {
            commonDirectoryList.add(FilePathBean("常用目录1", commonlyFolder1))
        }

        //常用目录2
        val commonlyFolder2 = AppConfig.getCommonlyFolder2()
        if (commonlyFolder2?.isNotEmpty() == true) {
            commonDirectoryList.add(FilePathBean("常用目录1", commonlyFolder2))
        }

        //本次打开目录
        commonDirectoryList.add(FilePathBean("默认目录", getDefaultOpenDirectory()))

        //上次打开目录
        val lastOpenFolderPath = AppConfig.getLastOpenFolder()
        if (AppConfig.isLastOpenFolderEnable() && lastOpenFolderPath?.isNotEmpty() == true) {
            commonDirectoryList.add(FilePathBean("上次使用", lastOpenFolderPath))
        }

        //视频所在目录
        val videoFolder = PlayerInitializer.selectSourceDirectory
        if (videoFolder?.isNotEmpty() == true) {
            commonDirectoryList.add(FilePathBean("视频目录", videoFolder))
        }

        return commonDirectoryList
    }

    /**
     * 获取默认打开的目录
     * 优先：当前已加载文件所在目录
     * 否则：默认缓存目录
     */
    private fun getDefaultOpenDirectory(): String {
        val addedSourcePath = when (mTrackType) {
            TrackType.AUDIO -> mControlWrapper.getVideoSource().getAudioPath()
            TrackType.DANMU -> mControlWrapper.getVideoSource().getDanmu()?.danmuPath
            TrackType.SUBTITLE -> mControlWrapper.getVideoSource().getSubtitlePath()
        }
        if (addedSourcePath.isNullOrEmpty()) {
            return PathHelper.getCachePath()
        }

        val parentDirectory = File(addedSourcePath).parentFile
        if (parentDirectory != null && parentDirectory.isValid()) {
            return parentDirectory.absolutePath
        }

        return PathHelper.getCachePath()
    }

    /**
     * 根据KeyCode目标焦点ItemBinding
     */
    private fun handleKeyCode(keyCode: Int): Boolean {
        //垂直方向上的事件处理
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            return handleKeyCodeVertical(keyCode)
        }

        //网络弹幕按钮的焦点处理
        if (viewBinding.tvSearchNetworkDanmu.hasFocus()) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    viewBinding.tvSearchNetworkDanmu.requestFocus()
                }
            }
            return true
        }

        if (handleKeyCodeInCommonRv(keyCode)) {
            return true
        }
        if (handleKeyCodeInPathRv(keyCode)) {
            return true
        }
        return handleKeyCodeInFileRv(keyCode)
    }

    private fun handleKeyCodeVertical(keyCode: Int): Boolean {
        val isKeyUp = when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> true
            KeyEvent.KEYCODE_DPAD_DOWN -> false
            else -> return false
        }

        if (viewBinding.tvSearchNetworkDanmu.hasFocus()) {
            if (isKeyUp) {
                if (mFileData.size > 0) {
                    viewBinding.rvFile.requestIndexChildFocus(mFileData.size - 1)
                } else if (mPathData.size > 0) {
                    viewBinding.rvPath.requestIndexChildFocus(mPathData.size - 1)
                } else {
                    viewBinding.rvCommonFolder.requestIndexChildFocus(0)
                }
            } else {
                viewBinding.rvCommonFolder.requestIndexChildFocus(0)
            }
            return true
        }

        if (viewBinding.rvCommonFolder.focusedChild != null) {
            if (isKeyUp) {
                if (viewBinding.tvSearchNetworkDanmu.isVisible) {
                    viewBinding.tvSearchNetworkDanmu.requestFocus()
                } else if (mFileData.size > 0) {
                    viewBinding.rvFile.requestIndexChildFocus(mFileData.size - 1)
                }
            } else {
                if (mPathData.size > 0) {
                    viewBinding.rvPath.requestIndexChildFocus(mPathData.size - 1)
                } else if (mFileData.size > 0) {
                    viewBinding.rvFile.requestIndexChildFocus(0)
                }
            }
            return true
        }

        if (viewBinding.rvPath.focusedChild != null) {
            if (isKeyUp) {
                viewBinding.rvCommonFolder.requestIndexChildFocus(0)
            } else {
                if (mFileData.size > 0) {
                    viewBinding.rvFile.requestIndexChildFocus(0)
                } else if (viewBinding.tvSearchNetworkDanmu.isVisible) {
                    viewBinding.tvSearchNetworkDanmu.requestFocus()
                }
            }
            return true
        }

        if (viewBinding.rvFile.focusedChild != null) {
            return handleKeyCodeInFileRv(keyCode)
        }

        return false
    }

    private fun handleKeyCodeInCommonRv(keyCode: Int): Boolean {
        //已获取的焦点View
        val focusedChild = viewBinding.rvCommonFolder.focusedChild
            ?: return false
        val focusedChildIndex = viewBinding.rvCommonFolder.getChildAdapterPosition(focusedChild)
        //已获取的焦点View的位置
        if (focusedChildIndex == -1) {
            return false
        }

        //向左的点击事件
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            val targetIndex =
                mCommonDirectoryData.previousItemIndex<FilePathBean>(focusedChildIndex)
            viewBinding.rvCommonFolder.requestIndexChildFocus(targetIndex)
            return true
        }

        //向右的点击事件
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            val targetIndex = mCommonDirectoryData.nextItemIndex<FilePathBean>(focusedChildIndex)
            viewBinding.rvCommonFolder.requestIndexChildFocus(targetIndex)
            return true
        }
        return false
    }

    private fun handleKeyCodeInPathRv(keyCode: Int): Boolean {
        //已获取的焦点View
        val focusedChild = viewBinding.rvPath.focusedChild
            ?: return false

        val focusedChildIndex = viewBinding.rvPath.getChildAdapterPosition(focusedChild)
        //已获取的焦点View的位置
        if (focusedChildIndex == -1) {
            return false
        }

        //向左的点击事件
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            val targetIndex = mPathData.previousItemIndex<FilePathBean>(focusedChildIndex)
            viewBinding.rvPath.requestIndexChildFocus(targetIndex)
            return true
        }

        //向右的点击事件
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            val targetIndex = mPathData.nextItemIndex<FilePathBean>(focusedChildIndex)
            viewBinding.rvPath.requestIndexChildFocus(targetIndex)
            return true
        }
        return false
    }

    private fun handleKeyCodeInFileRv(keyCode: Int): Boolean {
        //已获取的焦点View
        val focusedChild = viewBinding.rvFile.focusedChild
            ?: return false
        val focusedChildIndex = viewBinding.rvFile.getChildAdapterPosition(focusedChild)
        //已获取的焦点View的位置
        if (focusedChildIndex == -1) {
            return false
        }

        //向左或向右的点击事件
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            focusedChild.requestFocus()
            return true
        }

        //向上的事件
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            val index = mFileData.findIndexOnLeft(focusedChildIndex) { true }
            if (index != -1) {
                viewBinding.rvFile.requestIndexChildFocus(index)
                return true
            }

            if (mPathData.size > 0) {
                viewBinding.rvPath.requestIndexChildFocus(mPathData.size - 1)
            } else {
                viewBinding.rvCommonFolder.requestIndexChildFocus(0)
            }
            return true
        }

        //向下的事件
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            val index = mFileData.findIndexOnRight(focusedChildIndex) { true }
            if (index != -1) {
                viewBinding.rvFile.requestIndexChildFocus(index)
                return true
            }

            if (viewBinding.tvSearchNetworkDanmu.isVisible) {
                viewBinding.tvSearchNetworkDanmu.requestFocus()
            } else {
                viewBinding.rvCommonFolder.requestIndexChildFocus(0)
            }
            return true
        }

        return false
    }
}