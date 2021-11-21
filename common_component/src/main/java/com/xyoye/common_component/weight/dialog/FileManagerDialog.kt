package com.xyoye.common_component.weight.dialog

import android.os.Environment
import com.xyoye.common_component.R
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.adapter.initData
import com.xyoye.common_component.config.AppConfig
import com.xyoye.common_component.databinding.DialogFileManagerBinding
import com.xyoye.common_component.databinding.ItemFileManagerBinding
import com.xyoye.common_component.databinding.ItemFileManagerPathBinding
import com.xyoye.common_component.extension.*
import com.xyoye.common_component.utils.*
import com.xyoye.common_component.utils.view.FilePathItemDecoration
import com.xyoye.data_component.bean.FileManagerBean
import com.xyoye.data_component.bean.FilePathBean
import com.xyoye.data_component.enums.FileManagerAction
import java.io.File

/**
 * Created by xyoye on 2020/11/26.
 */

class FileManagerDialog : BaseBottomDialog<DialogFileManagerBinding> {
    private lateinit var action: FileManagerAction
    private var defaultFolderPath: String? = null
    private lateinit var listener: (resultPath: String) -> Unit

    constructor() : super()

    constructor(
        action: FileManagerAction,
        defaultFolderPath: String? = null,
        listener: (resultPath: String) -> Unit
    ) : super(true) {
        this.action = action
        this.defaultFolderPath = defaultFolderPath
        this.listener = listener
    }

    private lateinit var binding: DialogFileManagerBinding

    private val mRootPath = Environment.getExternalStorageDirectory().absolutePath
    private var currentDirPath = mRootPath

    private val mPathData = arrayListOf<FilePathBean>()

    override fun getChildLayoutId() = R.layout.dialog_file_manager

    override fun initView(binding: DialogFileManagerBinding) {
        this.binding = binding

        rootViewBinding.containerFl.apply {
            setPadding(paddingLeft, 0, paddingRight, paddingBottom)
        }

        defaultFolderPath = defaultFolderPath ?: AppConfig.getCachePath()!!

        setTitle(
            when (action) {
                FileManagerAction.ACTION_SELECT_DANMU -> "选择本地弹幕文件"
                FileManagerAction.ACTION_SELECT_SUBTITLE -> "选择本地字幕文件"
                FileManagerAction.ACTION_SELECT_VIDEO -> "选择视频文件"
                FileManagerAction.ACTION_SELECT_DIRECTORY -> "选择文件夹"
                FileManagerAction.ACTION_SELECT_TORRENT -> "选择种子文件"
            }
        )

        setPositiveVisible(action == FileManagerAction.ACTION_SELECT_DIRECTORY)

        setNegativeListener { dismiss() }

        setPositiveListener {
            dismiss()
            AppConfig.putLastOpenFolder(currentDirPath)
            listener.invoke(currentDirPath)
        }

        binding.rootPathTv.setOnClickListener {
            openTargetDirectory(mRootPath)
        }

        initTag()

        initRv()

        openTargetDirectory(defaultFolderPath!!)
    }

    private fun initTag() {
        val commonlyFolder1 = AppConfig.getCommonlyFolder1()
        if (commonlyFolder1?.isNotEmpty() == true) {
            val folder1Drawable = R.drawable.ic_tag.toResDrawable()
            folder1Drawable?.setTint(R.color.colorAccent.toResColor())
            addLeftAction(folder1Drawable, 8).setOnClickListener {
                openTargetDirectory(commonlyFolder1)
            }
        }

        val commonlyFolder2 = AppConfig.getCommonlyFolder2()
        if (commonlyFolder2?.isNotEmpty() == true) {
            val folder2Drawable = R.drawable.ic_tag.toResDrawable()
            folder2Drawable?.setTint(R.color.orange.toResColor())
            addLeftAction(folder2Drawable, 8).setOnClickListener {
                openTargetDirectory(commonlyFolder2)
            }
        }

        val defaultDrawable = R.drawable.ic_tag.toResDrawable()
        defaultDrawable?.setTint(R.color.gray.toResColor())
        addRightAction(defaultDrawable, 8, "本次打开目录").setOnClickListener {
            openTargetDirectory(defaultFolderPath!!)
        }

        val lastOpenFolderPath = AppConfig.getLastOpenFolder()
        if (AppConfig.isLastOpenFolderEnable() && lastOpenFolderPath?.isNotEmpty() == true) {
            val lastOpenDrawable = R.drawable.ic_tag.toResDrawable()
            lastOpenDrawable?.setTint(R.color.black.toResColor())
            addRightAction(lastOpenDrawable, 8, "上次打开目录").setOnClickListener {
                openTargetDirectory(lastOpenFolderPath)
            }
        }
    }

    private fun initRv() {
        binding.pathRv.apply {
            layoutManager = horizontal()

            adapter = buildAdapter<FilePathBean> {
                initData(mPathData)
                addItem<FilePathBean, ItemFileManagerPathBinding>(R.layout.item_file_manager_path) {
                    initView { data, _, _ ->
                        itemBinding.apply {
                            dirNameTv.text = data.name
                            dirNameTv.setTextColorRes(
                                if (data.isOpened) R.color.text_black else R.color.text_gray
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

        binding.fileRv.apply {
            layoutManager = vertical()

            adapter = buildAdapter<FileManagerBean> {
                addItem<FileManagerBean, ItemFileManagerBinding>(R.layout.item_file_manager) {
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
                                        dismiss()
                                        AppConfig.putLastOpenFolder(currentDirPath)
                                        listener.invoke(data.filePath)
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
        binding.pathRv.adapter?.notifyDataSetChanged()
        binding.pathRv.scrollToPosition(mPathData.size - 1)

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
        binding.pathRv.adapter?.notifyDataSetChanged()
        binding.pathRv.scrollToPosition(mPathData.size - 1)

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
        return when (action) {
            FileManagerAction.ACTION_SELECT_VIDEO -> isVideoFile(filePath)

            FileManagerAction.ACTION_SELECT_DANMU -> isDanmuFile(filePath)

            FileManagerAction.ACTION_SELECT_SUBTITLE -> isSubtitleFile(filePath)

            FileManagerAction.ACTION_SELECT_TORRENT -> isTorrentFile(filePath)

            else -> false
        }
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
        binding.rootPathTv.setTextColorRes(
            if (isSelected) R.color.text_black else R.color.text_gray
        )
    }

    private fun getFileCover(): Int {
        return when (action) {
            FileManagerAction.ACTION_SELECT_VIDEO -> R.drawable.ic_file_video

            FileManagerAction.ACTION_SELECT_DANMU -> R.drawable.ic_file_xml

            FileManagerAction.ACTION_SELECT_SUBTITLE -> R.drawable.ic_file_subtitle

            FileManagerAction.ACTION_SELECT_TORRENT -> R.drawable.ic_file_torrent

            else -> R.drawable.ic_file_unknow
        }
    }

    private fun setFileData(fileList: MutableList<FileManagerBean>) {
        binding.fileRv.setData(
            fileList.filterHideFile { it.fileName }
        )
    }
}