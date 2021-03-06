package com.xyoye.download_component.ui.fragment.download_info

import android.os.Bundle
import com.xyoye.common_component.base.BaseFragment
import com.xyoye.download_component.BR
import com.xyoye.download_component.R
import com.xyoye.download_component.databinding.FragmentDownloadInfoBinding

class DownloadInfoFragment :
    BaseFragment<DownloadInfoFragmentViewModel, FragmentDownloadInfoBinding>() {

    companion object {
        fun newInstance(infoHash: String): DownloadInfoFragment {
            val infoFragment = DownloadInfoFragment()
            val bundle = Bundle()
            bundle.putString("info_hash", infoHash)
            infoFragment.arguments = bundle
            return infoFragment
        }
    }

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            DownloadInfoFragmentViewModel::class.java
        )

    override fun getLayoutId() = R.layout.fragment_download_info

    override fun initView() {

    }
}