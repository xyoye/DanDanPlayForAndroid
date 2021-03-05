package com.xyoye.common_component.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.xyoye.data_component.helper.Loading

/**
 * Created by xyoye on 2020/7/10.
 */

abstract class BaseFragment<VM : BaseViewModel, V : ViewDataBinding> : BaseAppFragment<V>() {

    private val viewModelInit: ViewModelInit<VM> by lazy {
        initViewModel()
    }

    protected val viewModel: VM by lazy {
        ViewModelProvider(
            viewModelStore,
            ViewModelProvider.AndroidViewModelFactory(mAttachActivity.application)
        ).get(viewModelInit.clazz)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mView = super.onCreateView(inflater, container, savedInstanceState)

        dataBinding.setVariable(viewModelInit.variableId, viewModel)

        observeLoadingDialog()

        return mView
    }

    open fun observeLoadingDialog() {
        viewModel.loadingObserver.observe(mAttachActivity, {
            when(it.first){
                Loading.SHOW_LOADING -> showLoading()
                Loading.SHOW_LOADING_MSG -> showLoading(it.second!!)
                else -> hideLoading()
            }
        })
    }

    abstract fun initViewModel(): ViewModelInit<VM>

    data class ViewModelInit<VM>(val variableId: Int, val clazz: Class<VM>)
}