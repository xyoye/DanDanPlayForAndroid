package com.xyoye.user_component.ui.activities.register

import android.app.Activity
import android.content.Intent
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.utils.showKeyboard
import com.xyoye.user_component.BR
import com.xyoye.user_component.R
import com.xyoye.user_component.databinding.ActivityRegisterBinding

@Route(path = RouteTable.User.UserRegister)
class RegisterActivity : BaseActivity<RegisterViewModel, ActivityRegisterBinding>() {

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            RegisterViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_register

    override fun initView() {
        title = ""

        dataBinding.apply {
            //帐号
            userAccountEt.postDelayed({
                showKeyboard(userAccountEt)
            }, 200)

            userAccountEt.addTextChangedListener {
                userAccountLayout.error = ""
            }
            userPasswordEt.addTextChangedListener {
                userAccountLayout.error = ""
            }
            userEmailEt.addTextChangedListener {
                userAccountLayout.error = ""
            }
            userScreenNameEt.addTextChangedListener {
                userAccountLayout.error = ""
            }
        }

        viewModel.accountErrorLiveData.observe(this, Observer {
            dataBinding.userAccountLayout.error = it
        })
        viewModel.passwordErrorLiveData.observe(this, Observer {
            dataBinding.userPasswordLayout.error = it
        })
        viewModel.emailErrorLiveData.observe(this, Observer {
            dataBinding.userEmailLayout.error = it
        })
        viewModel.screenNameErrorLiveData.observe(this, Observer {
            dataBinding.userScreenNameLayout.error = it
        })
        viewModel.registerLiveData.observe(this, Observer {
            finish()
        })
    }
}