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
import com.xyoye.common_component.extension.*
import com.xyoye.common_component.utils.*
import com.xyoye.common_component.utils.comparator.FileNameComparator
import com.xyoye.common_component.utils.view.FilePathItemDecoration
import com.xyoye.common_component.utils.view.ItemDecorationOrientation
import com.xyoye.data_component.bean.FileManagerBean
import com.xyoye.data_component.bean.FilePathBean
import com.xyoye.data_component.enums.SettingViewType
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
    private var mSettingViewType = SettingViewType.LOAD_DANMU_SOURCE

    private val mPathData = mutableListOf<FilePathBean>()
    private val mCommonDirectoryData = mutableListOf<FilePathBean>()
    private val mFileData = mutableListOf<FileManagerBean>()

    init {
        initView()

        initRv()
    }

    override fun getLayoutId() = R.layout.layout_switch_source

    override fun getSettingViewType() = SettingViewType.SWITCH_SOURCE

    override fun getGravity() = Gravity.START

    override fun onViewShow() {
        val isSwitchSubtitle = mSettingViewType == SettingViewType.LOAD_SUBTITLE_SOURCE
        viewBinding.titleTv.text = if (isSwitchSubtitle)
            R.string.text_select_subtitle.toResString()
        else
            R.string.select_local_danmu.toResString()
        openDirectory(getDefaultOpenDirectory())

        var lastOpenFolderPath = if (isSwitchSubtitle) {
            AppConfig.getLastOpenSubtitleFolder()
        } else {
            AppConfig.getLastOpenDanmakuFolder()
        }
        if (lastOpenFolderPath == null) {
            lastOpenFolderPath = AppConfig.getLastOpenFolder()
        }

        mCommonDirectoryData.clear()
        mCommonDirectoryData.addAll(getCommonDirectoryList(lastOpenFolderPath))
        viewBinding.rvCommonFolder.setData(mCommonDirectoryData)

        viewBinding.removeTv.isVisible = isSwitchSubtitle.not()
                && mControlWrapper.getDanmuUrl().isNullOrEmpty().not()
        viewBinding.tvSearchNetworkDanmu.isVisible = isSwitchSubtitle.not()
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
            if (viewBinding.removeTv.isVisible) {
                viewBinding.removeTv.requestFocus()
            } else if (viewBinding.tvSearchNetworkDanmu.isVisible) {
                viewBinding.tvSearchNetworkDanmu.requestFocus()
            } else {
                viewBinding.rvCommonFolder.requestIndexChildFocus(0)
            }
        }
        return true
    }

    fun setSwitchType(settingViewType: SettingViewType) {
        this.mSettingViewType = settingViewType
    }

    private fun initView() {
        viewBinding.removeTv.setOnClickListener {
            viewBinding.removeTv.isVisible = false
            mControlWrapper.onDanmuSourceChanged("")
        }

        viewBinding.tvSearchNetworkDanmu.setOnClickListener {
            mControlWrapper.showSettingView(SettingViewType.SEARCH_DANMU)
            onSettingVisibilityChanged(false)
        }
    }

    private fun initRv() {
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
                            fileIv.setImageResource(
                                when {
                                    data.isDirectory -> R.drawable.ic_folder
                                    mSettingViewType == SettingViewType.LOAD_SUBTITLE_SOURCE -> R.drawable.ic_file_subtitle
                                    else -> R.drawable.ic_file_xml
                                }
                            )
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

    /**
     * 选中目标文件
     */
    private fun openFile(data: FileManagerBean) {
        onSettingVisibilityChanged(false)

        File(data.filePath).parentFile?.absolutePath?.let {
            AppConfig.putLastOpenFolder(it)
            if (mSettingViewType == SettingViewType.LOAD_SUBTITLE_SOURCE) {
                AppConfig.putLastOpenSubtitleFolder(it)
            } else {
                AppConfig.putLastOpenDanmakuFolder(it)
            }
        }

        if (mSettingViewType == SettingViewType.LOAD_SUBTITLE_SOURCE) {
            mControlWrapper.addSubtitleStream(data.filePath)
            mControlWrapper.onSubtitleSourceUpdate(data.filePath)
        } else {
            mControlWrapper.onDanmuSourceChanged(data.filePath)
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
        val isSwitchSubtitle = mSettingViewType == SettingViewType.LOAD_SUBTITLE_SOURCE
        return (isSwitchSubtitle && isSubtitleFile(filePath))
                || (!isSwitchSubtitle && isDanmuFile(filePath))
    }

    /**
     * 获取常用目录列表
     */
    private fun getCommonDirectoryList(lastOpenFolderPath: String?): MutableList<FilePathBean> {
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
        val targetFilePath = if (mSettingViewType == SettingViewType.LOAD_SUBTITLE_SOURCE) {
            AppConfig.getCachePath()
        } else {
            mControlWrapper.getDanmuUrl()
        }

        if (targetFilePath.isNullOrEmpty()) {
            return AppConfig.getCachePath()!!
        }

        val file = File(targetFilePath)
        return if (file.isDirectory) {
            file.absolutePath
        } else {
            file.parentFile?.absolutePath ?: AppConfig.getCachePath()!!
        }
    }

    /**
     * 根据KeyCode目标焦点ItemBinding
     */
    private fun handleKeyCode(keyCode: Int): Boolean {
        //垂直方向上的事件处理
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            return handleKeyCodeVertical(keyCode)
        }

        //移除弹幕按钮的焦点处理
        if (viewBinding.removeTv.hasFocus()) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_RIGHT -> viewBinding.tvSearchNetworkDanmu.requestFocus()
            }
            return true
        }

        //网络弹幕按钮的焦点处理
        if (viewBinding.tvSearchNetworkDanmu.hasFocus()) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    if (viewBinding.removeTv.isVisible)
                        viewBinding.removeTv.requestFocus()
                    else
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
        if (handleKeyCodeInFileRv(keyCode)) {
            return true
        }
        return false
    }

    private fun handleKeyCodeVertical(keyCode: Int): Boolean {
        val isKeyUp = when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> true
            KeyEvent.KEYCODE_DPAD_DOWN -> false
            else -> return false
        }

        if (viewBinding.removeTv.hasFocus() || viewBinding.tvSearchNetworkDanmu.hasFocus()) {
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
                if (viewBinding.removeTv.isVisible) {
                    viewBinding.removeTv.requestFocus()
                } else if (viewBinding.tvSearchNetworkDanmu.isVisible) {
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
                } else if (viewBinding.removeTv.isVisible) {
                    viewBinding.removeTv.requestFocus()
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

            if (viewBinding.removeTv.isVisible) {
                viewBinding.removeTv.requestFocus()
            } else if (viewBinding.tvSearchNetworkDanmu.isVisible) {
                viewBinding.tvSearchNetworkDanmu.requestFocus()
            } else {
                viewBinding.rvCommonFolder.requestIndexChildFocus(0)
            }
            return true
        }

        return false
    }
}