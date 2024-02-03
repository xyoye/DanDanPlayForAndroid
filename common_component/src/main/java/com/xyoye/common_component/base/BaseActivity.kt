package com.xyoye.common_component.base

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.gyf.immersionbar.ImmersionBar
import com.xyoye.common_component.R
import com.xyoye.common_component.extension.isNightMode
import com.xyoye.data_component.helper.Loading

/**
 * Created by xyoye on 2020/4/13.
 */

abstract class BaseActivity<VM : BaseViewModel, V : ViewDataBinding> : BaseAppCompatActivity<V>() {

    private val viewModelInit: ViewModelInit<VM> by lazy {
        initViewModel()
    }

    protected val viewModel: VM by lazy {
        ViewModelProvider(
            viewModelStore,
            ViewModelProvider.AndroidViewModelFactory(application)
        ).get(viewModelInit.clazz)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataBinding.setVariable(viewModelInit.variableId, viewModel)

        observeLoadingDialog()
    }

    override fun initStatusBar() {
        ImmersionBar.with(this)
            .fitsSystemWindows(true)
            .statusBarDarkFont(isNightMode().not())
            .statusBarColor(R.color.status_bar_color)
            .navigationBarDarkIcon(isNightMode().not())
            .navigationBarColor(R.color.status_bar_color)
            .init()
    }

    open fun observeLoadingDialog() {
        viewModel.loadingObserver.observe(this) {
            when (it.first) {
                Loading.SHOW_LOADING -> showLoading()
                Loading.SHOW_LOADING_MSG -> showLoading(it.second!!)
                else -> hideLoading()
            }
        }
    }

    fun getOwnerViewModel(): BaseViewModel = viewModel

    abstract fun initViewModel(): ViewModelInit<VM>

    data class ViewModelInit<VM>(val variableId: Int, val clazz: Class<VM>)
}