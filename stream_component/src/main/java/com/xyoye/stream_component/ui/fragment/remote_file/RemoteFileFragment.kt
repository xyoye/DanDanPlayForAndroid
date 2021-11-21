package com.xyoye.stream_component.ui.fragment.remote_file

import android.os.Bundle
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.adapter.initData
import com.xyoye.common_component.base.BaseFragment
import com.xyoye.common_component.extension.setAutoSizeText
import com.xyoye.common_component.extension.setGlideImage
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.utils.RemoteHelper
import com.xyoye.common_component.utils.formatDuration
import com.xyoye.common_component.utils.isFileExist
import com.xyoye.data_component.data.remote.RemoteVideoData
import com.xyoye.stream_component.BR
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.FragmentRemoteFileBinding
import com.xyoye.stream_component.databinding.ItemRemoteFolderBinding
import com.xyoye.stream_component.databinding.ItemRemoteVideoBinding
import com.xyoye.stream_component.ui.activities.remote_file.RemoteFileActivity

class RemoteFileFragment : BaseFragment<RemoteFileFragmentViewModel, FragmentRemoteFileBinding>() {

    companion object {
        private const val FILE_DATA = "file_data"

        fun newInstance(fileList: MutableList<RemoteVideoData>): RemoteFileFragment {
            val fragment = RemoteFileFragment()
            val bundle = Bundle()

            bundle.putParcelableArrayList(FILE_DATA, ArrayList(fileList))
            fragment.arguments = bundle
            return fragment
        }
    }

    private var mFileData = mutableListOf<RemoteVideoData>()

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            RemoteFileFragmentViewModel::class.java
        )

    override fun getLayoutId() = R.layout.fragment_remote_file

    override fun initView() {
        var fileData = arguments?.getParcelableArrayList<RemoteVideoData>(FILE_DATA)
        if (fileData == null) {
            fileData = arrayListOf()
        }
        arguments?.clear()

        mFileData.addAll(fileData)
        initRv()
    }
    private fun initRv() {

        dataBinding.mediaRv.apply {

            layoutManager = vertical()

            adapter = buildAdapter<RemoteVideoData> {
                initData(mFileData)

                addItem<RemoteVideoData, ItemRemoteFolderBinding>(R.layout.item_remote_folder) {
                    checkType { data, _ -> data.isFolder }
                    initView { data, _, _ ->
                        itemBinding.apply {
                            folderTv.setAutoSizeText(data.Name)
//                            folderTv.setTextColorRes(
//                                if (data.isLastPlay) R.color.text_theme else R.color.text_black
//                            )

                            val fileCount = "${data.childData.size}视频"
                            fileCountTv.text = fileCount
                            itemLayout.setOnClickListener {
                                if (mAttachActivity is RemoteFileActivity) {
                                    (mAttachActivity as RemoteFileActivity).listFolder(
                                        data.Name,
                                        data.absolutePath,
                                        data.childData
                                    )
                                }
                            }
                        }
                    }
                }

                addItem<RemoteVideoData, ItemRemoteVideoBinding>(R.layout.item_remote_video) {
                    checkType { data, _ -> data.isFolder.not() }
                    initView { data, _, _ ->
                        itemBinding.run {
                            titleTv.setAutoSizeText(data.getEpisodeName())
                            durationTv.isGone = data.Duration == null
                            if (data.Duration != null) {
                                durationTv.text = formatDuration(data.Duration!! * 1000)
                            }
                            val coverUrl = RemoteHelper.getInstance().buildImageUrl(data.Id)
                            coverIv.setGlideImage(coverUrl, 5)

                            danmuTipsTv.isVisible = isFileExist(data.danmuPath)
                            subtitleTipsTv.isVisible = isFileExist(data.subtitlePath)

                            itemLayout.setOnClickListener {
                                if (mAttachActivity is RemoteFileActivity) {
                                    (mAttachActivity as RemoteFileActivity).openVideo(data, mFileData)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}