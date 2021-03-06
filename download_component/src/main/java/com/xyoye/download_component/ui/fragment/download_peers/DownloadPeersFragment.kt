package com.xyoye.download_component.ui.fragment.download_peers

import android.os.Bundle
import com.xyoye.common_component.base.BaseFragment

import com.xyoye.download_component.BR
import com.xyoye.download_component.R
import com.xyoye.download_component.databinding.FragmentDownloadPeersBinding
import com.xyoye.download_component.ui.fragment.download_info.DownloadInfoFragment

class DownloadPeersFragment :
    BaseFragment<DownloadPeersFragmentViewModel, FragmentDownloadPeersBinding>() {

    companion object {
        fun newInstance(infoHash: String): DownloadPeersFragment {
            val peersFragment = DownloadPeersFragment()
            val bundle = Bundle()
            bundle.putString("info_hash", infoHash)
            peersFragment.arguments = bundle
            return peersFragment
        }
    }

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            DownloadPeersFragmentViewModel::class.java
        )

    override fun getLayoutId() = R.layout.fragment_download_peers

    override fun initView() {

    }
}