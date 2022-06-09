package com.xyoye.player.controller.setting

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Environment
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.config.AppConfig
import com.xyoye.common_component.databinding.ItemFileManagerPathBinding
import com.xyoye.common_component.extension.*
import com.xyoye.common_component.utils.*
import com.xyoye.common_component.utils.view.FilePathItemDecoration
import com.xyoye.data_component.bean.FileManagerBean
import com.xyoye.data_component.bean.FilePathBean
import com.xyoye.data_component.enums.SettingViewType
import com.xyoye.player.info.PlayerInitializer
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
) : BaseSettingView<LayoutSwitchSourceBinding>(context, attrs, defStyleAttr) {

    private val mRootPath = Environment.getExternalStorageDirectory().absolutePath
    private var currentDirPath = mRootPath
    private var isSwitchSubtitle = false

    private val mPathData = arrayListOf<FilePathBean>()

    init {
        viewBinding.switchSourceLl.setOnTouchListener { _, _ -> return@setOnTouchListener true }

        viewBinding.rootPathTv.setOnClickListener {
            openTargetDirectory(mRootPath)
        }

        initRv()

        initTag()
    }

    override fun getLayoutId() = R.layout.layout_switch_source

    override fun getSettingViewType() = SettingViewType.SWITCH_SOURCE

    override fun getGravity() = Gravity.START

    override fun onSettingVisibilityChanged(isVisible: Boolean) {
        super.onSettingVisibilityChanged(isVisible)
        viewBinding.switchSourceTv.text = if (isSwitchSubtitle) "选择字幕" else "选择弹幕"
        openTargetDirectory(AppConfig.getCachePath()!!)
    }

    fun setSwitchType(isSwitchSubtitle: Boolean) {
        this.isSwitchSubtitle = isSwitchSubtitle
    }

    private fun initRv() {
        viewBinding.pathRv.apply {
            layoutManager = horizontal()

            adapter = buildAdapter {
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

            setData(mPathData)
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
        viewBinding.pathRv.setData(mPathData)
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
        viewBinding.pathRv.setData(mPathData)
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
            fileList.filterHiddenFile { it.fileName }
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

    /**
     * 初始化快速打开目录标签
     */
    private fun initTag() {
        //视频所在目录
        val videoFolder = PlayerInitializer.selectSourceDirectory
        if (videoFolder?.isNotEmpty() == true) {
            val videoFolderDrawable = com.xyoye.common_component.R.drawable.ic_tag.toResDrawable()
            videoFolderDrawable?.setTint(com.xyoye.common_component.R.color.white.toResColor())
            addAction(videoFolderDrawable, 8).setOnClickListener {
                openTargetDirectory(videoFolder)
            }
        }

        //常用目录1
        val commonlyFolder1 = AppConfig.getCommonlyFolder1()
        if (commonlyFolder1?.isNotEmpty() == true) {
            val folder1Drawable = com.xyoye.common_component.R.drawable.ic_tag.toResDrawable()
            folder1Drawable?.setTint(com.xyoye.common_component.R.color.colorAccent.toResColor())
            addAction(folder1Drawable, 8).setOnClickListener {
                openTargetDirectory(commonlyFolder1)
            }
        }

        //常用目录2
        val commonlyFolder2 = AppConfig.getCommonlyFolder2()
        if (commonlyFolder2?.isNotEmpty() == true) {
            val folder2Drawable = com.xyoye.common_component.R.drawable.ic_tag.toResDrawable()
            folder2Drawable?.setTint(com.xyoye.common_component.R.color.orange.toResColor())
            addAction(folder2Drawable, 8).setOnClickListener {
                openTargetDirectory(commonlyFolder2)
            }
        }

        //上次打开目录
        val lastOpenFolderPath = AppConfig.getLastOpenFolder()
        if (AppConfig.isLastOpenFolderEnable() && lastOpenFolderPath?.isNotEmpty() == true) {
            val lastOpenDrawable = com.xyoye.common_component.R.drawable.ic_tag.toResDrawable()
            lastOpenDrawable?.setTint(com.xyoye.common_component.R.color.cyan.toResColor())
            addAction(lastOpenDrawable, 8, "上次打开目录").setOnClickListener {
                openTargetDirectory(lastOpenFolderPath)
            }
        }

        //本次打开目录
        val defaultDrawable = com.xyoye.common_component.R.drawable.ic_tag.toResDrawable()
        defaultDrawable?.setTint(com.xyoye.common_component.R.color.gray.toResColor())
        addAction(defaultDrawable, 8, "本次打开目录").setOnClickListener {
            openTargetDirectory(AppConfig.getCachePath()!!)
        }
    }

    private fun addAction(
        drawable: Drawable?,
        paddingDp: Int = 6,
        description: String = ""
    ): View {
        val actionView = createActionView(drawable, description, paddingDp)
        viewBinding.actionContainer.addView(actionView, actionLayoutParams())
        return actionView
    }

    private fun actionLayoutParams(): LinearLayout.LayoutParams {
        val size = dp2px(36)
        return LinearLayout.LayoutParams(size, size)
    }

    private fun createActionView(drawable: Drawable?, description: String, paddingDp: Int): View {
        val padding = dp2px(paddingDp)
        val actionView = AppCompatImageView(context)
        actionView.setImageDrawable(drawable)
        actionView.setPadding(padding, padding, padding, padding)
        actionView.background = rippleDrawable()
        actionView.contentDescription = description
        return actionView
    }
}