package com.xyoye.stream_component.ui.fragment.remote_file

import android.os.Bundle
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.base.BaseFragment
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.services.ScreencastProvideService
import com.xyoye.common_component.weight.StorageAdapter
import com.xyoye.data_component.data.remote.RemoteVideoData
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.data_component.enums.MediaType
import com.xyoye.stream_component.BR
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.FragmentRemoteFileBinding
import com.xyoye.stream_component.ui.activities.remote_file.RemoteFileActivity

class RemoteFileFragment : BaseFragment<RemoteFileFragmentViewModel, FragmentRemoteFileBinding>() {

    companion object {
        private const val STORAGE_DATA = "storage_data"
        private const val FILE_DATA = "file_data"

        fun newInstance(remoteData: MediaLibraryEntity?, fileList: MutableList<RemoteVideoData>): RemoteFileFragment {
            val fragment = RemoteFileFragment()
            val bundle = Bundle()

            bundle.putParcelable(STORAGE_DATA, remoteData)
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
                castFile = { data, device ->
                    viewModel.castItem(data.uniqueKey ?: "", device)
                },
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
        viewModel.castLiveData.observe(this) {
            ARouter.getInstance()
                .navigation(ScreencastProvideService::class.java)
                .startService(mAttachActivity, it)
        }

        val fileData = arguments?.getParcelableArrayList<RemoteVideoData>(FILE_DATA)
        val storageData = arguments?.getParcelable<MediaLibraryEntity>(STORAGE_DATA)
        arguments?.clear()
        viewModel.initRemoteStorage(storageData)
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
            remoteVideoData.Name,
            path,
            remoteVideoData.childData
        )
    }
}