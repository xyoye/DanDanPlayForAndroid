package com.xyoye.common_component.base

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.xyoye.common_component.R
import java.lang.ref.WeakReference

/**
 * Created by xyoye on 2020/7/7.
 */

abstract class BaseAppCompatActivity<V : ViewDataBinding> : AppCompatActivity() {

    protected lateinit var dataBinding: V

    protected var loadingReference : WeakReference<BaseLoadingDialog>? = null

    protected var mToolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initStatusBar()

        initDataBinding()

        initToolbar()

        initView()
    }

    protected fun initDataBinding() {
        dataBinding = DataBindingUtil.setContentView(this, getLayoutId())
        dataBinding.lifecycleOwner = this@BaseAppCompatActivity
    }

    private fun initToolbar() {
        mToolbar = findViewById(R.id.toolbar)
        mToolbar?.apply {
            this@BaseAppCompatActivity.setSupportActionBar(this)
        }

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            this@BaseAppCompatActivity.setSupportActionBar(mToolbar)
        }

        mToolbar?.setNavigationOnClickListener { finish() }
    }

    open fun showLoading(msg: String = "") {
        hideLoading()
        loadingReference = WeakReference(BaseLoadingDialog(this, msg))
        loadingReference!!.get()?.show()
    }

    open fun hideLoading() {
        loadingReference?.get()?.dismiss()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onDestroy() {
        hideLoading()
        loadingReference = null
        super.onDestroy()
    }

    abstract fun initStatusBar()

    abstract fun getLayoutId(): Int

    abstract fun initView()
}