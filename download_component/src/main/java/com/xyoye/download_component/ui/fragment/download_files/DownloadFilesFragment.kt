package com.xyoye.download_component.ui.fragment.download_files

import android.os.Bundle
import com.xyoye.common_component.base.BaseFragment
import com.xyoye.download_component.BR
import com.xyoye.download_component.R
import com.xyoye.download_component.databinding.FragmentDownloadFilesBinding

class DownloadFilesFragment :
    BaseFragment<DownloadFilesFragmentViewModel, FragmentDownloadFilesBinding>() {

    companion object {
        fun newInstance(infoHash: String): DownloadFilesFragment {
            val filesFragment = DownloadFilesFragment()
            val bundle = Bundle()
            bundle.putString("info_hash", infoHash)
            filesFragment.arguments = bundle
            return filesFragment
        }
    }

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            DownloadFilesFragmentViewModel::class.java
        )

    override fun getLayoutId() = R.layout.fragment_download_files

    override fun initView() {

    }
}