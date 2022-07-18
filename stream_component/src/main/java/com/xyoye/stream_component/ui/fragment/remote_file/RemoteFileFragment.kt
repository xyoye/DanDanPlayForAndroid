package com.xyoye.stream_component.ui.fragment.remote_file

import android.os.Bundle
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.base.BaseFragment
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.weight.StorageAdapter
import com.xyoye.data_component.data.remote.RemoteVideoData
import com.xyoye.data_component.enums.MediaType
import com.xyoye.stream_component.BR
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.FragmentRemoteFileBinding
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

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            RemoteFileFragmentViewModel::class.java
        )

    override fun getLayoutId() = R.layout.fragment_remote_file

    override fun initView() {
        dataBinding.mediaRv.apply {

            layoutManager = vertical()

            adapter = StorageAdapter.newInstance(
                mAttachActivity as RemoteFileActivity,
                MediaType.REMOTE_STORAGE,
                refreshDirectory = { viewModel.refreshDirectoryWithHistory() },
                openFile = { viewModel.playItem(it.uniqueKey ?: "") },
                openDirectory = { openDirectory(it.filePath) }
            )
        }

        viewModel.fileLiveData.observe(this) {
            dataBinding.mediaRv.setData(it)
        }

        viewModel.playLiveData.observe(this) {
            ARouter.getInstance()
                .build(RouteTable.Player.Player)
                .navigation()
        }

        val fileData = arguments?.getParcelableArrayList<RemoteVideoData>(FILE_DATA)
        arguments?.clear()
        viewModel.initDirectoryFiles(fileData)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDirectoryWithHistory()
    }

    private fun openDirectory(path: String) {
        val remoteVideoData = viewModel.curDirectoryFiles.find {
            it.absolutePath == path
        } ?: return

        (mAttachActivity as RemoteFileActivity).listFolder(
            remoteVideoData.displayName,
            path,
            remoteVideoData.childData
        )
    }
}