package com.xyoye.download_component.ui.fragment.download_tracker

import android.os.Bundle
import com.xyoye.common_component.base.BaseFragment
import com.xyoye.download_component.BR
import com.xyoye.download_component.R
import com.xyoye.download_component.databinding.FragmentDownloadTrackerBinding

class DownloadTrackerFragment :
    BaseFragment<DownloadTrackerFragmentViewModel, FragmentDownloadTrackerBinding>() {

    companion object {
        fun newInstance(infoHash: String): DownloadTrackerFragment {
            val trackerFragment = DownloadTrackerFragment()
            val bundle = Bundle()
            bundle.putString("info_hash", infoHash)
            trackerFragment.arguments = bundle
            return trackerFragment
        }
    }

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            DownloadTrackerFragmentViewModel::class.java
        )

    override fun getLayoutId() = R.layout.fragment_download_tracker

    override fun initView() {

    }
}