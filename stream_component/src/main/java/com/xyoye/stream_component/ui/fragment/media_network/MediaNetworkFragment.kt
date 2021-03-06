package com.xyoye.stream_component.ui.fragment.media_network

import com.xyoye.common_component.base.BaseFragment

import com.xyoye.stream_component.BR
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.FragmentMediaNetworkBinding

class MediaNetworkFragment : BaseFragment<MainFragmentViewModel, FragmentMediaNetworkBinding>() {

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            MainFragmentViewModel::class.java
        )

    override fun getLayoutId() = R.layout.fragment_media_network

    override fun initView() {

    }
}