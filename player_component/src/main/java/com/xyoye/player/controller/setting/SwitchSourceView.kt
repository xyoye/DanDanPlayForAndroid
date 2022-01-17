package com.xyoye.player.controller.setting

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.os.Environment
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.adapter.initData
import com.xyoye.common_component.config.AppConfig
import com.xyoye.common_component.databinding.ItemFileManagerPathBinding
import com.xyoye.common_component.extension.*
import com.xyoye.common_component.utils.*
import com.xyoye.common_component.utils.view.FilePathItemDecoration
import com.xyoye.data_component.bean.FileManagerBean
import com.xyoye.data_component.bean.FilePathBean
import com.xyoye.data_component.enums.PlayState
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player.wrapper.ControlWrapper
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.ItemFileManagerPlayerBinding
import com.xyoye.player_component.databinding.LayoutSwitchSourceBinding
import java.io.File

/**
 * Created by xyoye on 2021/2/15.
 */

@SuppressLint("ClickableViewAccessibility")
class SwitchSourceView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), InterSettingView {
    private val mHideTranslateX = -dp2px(300).toFloat()

    private val mRootPath = Environment.getExternalStorageDirectory().absolutePath
    private var currentDirPath = mRootPath
    private var isSwitchSubtitle = false

    private val mPathData = arrayListOf<FilePathBean>()

    private lateinit var mControlWrapper: ControlWrapper

    private val viewBinding = DataBindingUtil.inflate<LayoutSwitchSourceBinding>(
        LayoutInflater.from(context),
        R.layout.layout_switch_source,
        this,
        true
    )

    init {
        gravity = Gravity.START

        viewBinding.switchSourceLl.setOnTouchListener { _, _ -> return@setOnTouchListener true }

        viewBinding.rootPathTv.setOnClickListener {
            openTargetDirectory(mRootPath)
        }

        initRv()
    }

    override fun getSettingViewType() = SettingViewType.SWITCH_SOURCE

    override fun onSettingVisibilityChanged(isVisible: Boolean) {
        viewBinding.switchSourceTv.text = if (isSwitchSubtitle) "选择字幕" else "选择弹幕"
        val initialPath = PlayerInitializer.selectSourceDirectory ?: AppConfig.getCachePath()!!
        openTargetDirectory(initialPath)

        if (isVisible) {
            ViewCompat.animate(viewBinding.switchSourceLl)
                .translationX(0f)
                .setDuration(500)
                .start()
        } else {
            ViewCompat.animate(viewBinding.switchSourceLl)
                .translationX(mHideTranslateX)
                .setDuration(500)
                .start()
        }
    }

    override fun isSettingShowing() = viewBinding.switchSourceLl.translationX == 0f

    override fun attach(controlWrapper: ControlWrapper) {
        mControlWrapper = controlWrapper
    }

    override fun getView() = this

    override fun onVisibilityChanged(isVisible: Boolean) {

    }

    override fun onPlayStateChanged(playState: PlayState) {

    }

    override fun onProgressChanged(duration: Long, position: Long) {

    }

    override fun onLockStateChanged(isLocked: Boolean) {

    }

    override fun onVideoSizeChanged(videoSize: Point) {

    }

    fun setSwitchType(isSwitchSubtitle: Boolean) {
        this.isSwitchSubtitle = isSwitchSubtitle
    }

    private fun initRv() {
        viewBinding.pathRv.apply {
            layoutManager = horizontal()

            adapter = buildAdapter {
                initData(mPathData)
                addItem<FilePathBean, ItemFileManagerPathBinding>(R.layout.item_file_manager_path) {
                    initView { data, _, _ ->
                        itemBinding.apply {
                            dirNameTv.text = data.name
                            dirNameTv.setTextColorRes(
                                if (data.isOpened) R.color.text_white_immutable else R.color.text_gray
                            )
                            dirNameTv.setOnClickListener {
                                openTargetDirectory(data.path)
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

        viewBinding.fileRv.apply {
            layoutManager = vertical()

            adapter = buildAdapter {
                addItem<FileManagerBean, ItemFileManagerPlayerBinding>(R.layout.item_file_manager_player) {
                    initView { data, _, _ ->
                        itemBinding.apply {
                            fileNameTv.text = data.fileName
                            fileIv.setImageResource(
                                if (data.isDirectory) R.drawable.ic_folder else getFileCover()
                            )
                            itemLayout.setOnClickListener {
                                when {
                                    data.isDirectory -> {
                                        openChildDirectory(data.fileName)
                                    }
                                    else -> {
                                        selectSourceFile(data)
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
     * 打开任意目标目录
     */
    private fun openTargetDirectory(dirPath: String) {
        val directory = File(dirPath)
        currentDirPath = directory.absolutePath

        setRootPathState(currentDirPath == mRootPath)

        val pathData = getPathData(currentDirPath).reversed()
        if (pathData.isNotEmpty()) {
            pathData.last().isOpened = true
        }
        mPathData.clear()
        mPathData.addAll(pathData)
        viewBinding.pathRv.adapter?.notifyDataSetChanged()
        viewBinding.pathRv.scrollToPosition(mPathData.size - 1)

        setFileData(getDirectoryChildData(directory))
    }

    /**
     * 打开当前目录下子目录
     */
    private fun openChildDirectory(dirName: String) {
        val directory = File(currentDirPath, dirName)
        currentDirPath = directory.absolutePath

        setRootPathState(false)

        if (mPathData.isNotEmpty()) {
            mPathData.last().isOpened = false
        }
        mPathData.add(
            FilePathBean(
                dirName,
                currentDirPath,
                true
            )
        )
        viewBinding.pathRv.adapter?.notifyDataSetChanged()
        viewBinding.pathRv.scrollToPosition(mPathData.size - 1)

        setFileData(getDirectoryChildData(directory))
    }

    /**
     * 获取目录下文件信息
     */
    private fun getDirectoryChildData(directory: File): MutableList<FileManagerBean> {
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

        fileManagerData.sortWith(FileComparator(
            value = { it.fileName },
            isDirectory = { it.isDirectory }
        ))
        return fileManagerData
    }

    /**
     * 是否为当前所可选中文件类型
     */
    private fun isTargetFile(filePath: String): Boolean {
        return (isSwitchSubtitle && isSubtitleFile(filePath))
                || (!isSwitchSubtitle && isDanmuFile(filePath))
    }

    /**
     * 根据原始路径获取切割路径信息集合
     */
    private fun getPathData(path: String): MutableList<FilePathBean> {
        var filePath = path
        val pathData = arrayListOf<FilePathBean>()
        if (filePath.isEmpty()) {
            return pathData
        }
        if (filePath.endsWith(File.separator) && filePath.length > 1) {
            filePath = filePath.substring(0, filePath.length - 1)
        }
        if (!filePath.contains(File.separator) || filePath == mRootPath) {
            return pathData
        }


        val lastSep = filePath.lastIndexOf(File.separator)
        val dirName = filePath.substring(lastSep + 1)
        val parentPath = filePath.substring(0, lastSep)

        pathData.add(
            FilePathBean(
                dirName,
                filePath
            )
        )
        pathData.addAll(getPathData(parentPath))

        return pathData
    }

    private fun setRootPathState(isSelected: Boolean) {
        viewBinding.rootPathTv.setTextColorRes(
            if (isSelected) R.color.text_white_immutable else R.color.text_gray
        )
    }

    private fun getFileCover(): Int {
        return if (isSwitchSubtitle) R.drawable.ic_file_subtitle else R.drawable.ic_file_xml
    }

    private fun setFileData(fileList: MutableList<FileManagerBean>) {
        viewBinding.fileRv.setData(
            fileList.filterHideFile { it.fileName }
        )
    }

    private fun selectSourceFile(data: FileManagerBean) {
        onSettingVisibilityChanged(false)
        if (isSwitchSubtitle) {
            mControlWrapper.setSubtitlePath(data.filePath, playWhenReady = true)
        } else {
            mControlWrapper.onDanmuSourceChanged(data.filePath)
        }
    }
}