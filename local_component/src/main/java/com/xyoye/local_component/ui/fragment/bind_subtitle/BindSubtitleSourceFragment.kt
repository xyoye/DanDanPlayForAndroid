package com.xyoye.local_component.ui.fragment.bind_subtitle

import com.xyoye.common_component.base.BaseFragment
import com.xyoye.local_component.BR
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.FragmentBindSubtitleSourceBinding
import com.xyoye.local_component.listener.ExtraSourceListener


/**
 * Created by xyoye on 2022/1/25
 */
class BindSubtitleSourceFragment :
    BaseFragment<BindSubtitleSourceFragmentViewModel, FragmentBindSubtitleSourceBinding>(),
    ExtraSourceListener {

    companion object {
        fun newInstance(videoPath: String?): BindSubtitleSourceFragment {
            return BindSubtitleSourceFragment()
        }
    }

    override fun initViewModel() = ViewModelInit(
        BR.viewModel,
        BindSubtitleSourceFragmentViewModel::class.java
    )

    override fun getLayoutId() = R.layout.fragment_bind_subtitle_source

    override fun initView() {

    }

    override fun search(searchText: String) {

    }

    override fun unbindDanmu() {

    }

    override fun unbindSubtitle() {
        viewModel.unbindSubtitle()
    }
}