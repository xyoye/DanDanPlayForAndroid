package com.xyoye.user_component.ui.activities.login

import android.view.View
import androidx.core.widget.addTextChangedListener
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.utils.SecurityHelper
import com.xyoye.common_component.utils.showKeyboard
import com.xyoye.common_component.weight.BottomActionDialog
import com.xyoye.common_component.weight.dialog.CommonDialog
import com.xyoye.data_component.bean.SheetActionBean
import com.xyoye.user_component.BR
import com.xyoye.user_component.R
import com.xyoye.user_component.databinding.ActivityLoginBinding

@Route(path = RouteTable.User.UserLogin)
class LoginActivity : BaseActivity<LoginViewModel, ActivityLoginBinding>() {

    @Autowired
    @JvmField
    var userAccount: String? = null

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            LoginViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_login

    override fun initView() {
        ARouter.getInstance().inject(this)

        title = ""

        val isOfficialApplication = SecurityHelper.getInstance().isOfficialApplication!!
        if (!isOfficialApplication) {
            showLimitDialog()
            return
        }

        if (!userAccount.isNullOrEmpty()) {
            viewModel.accountField.set(userAccount)
            showKeyboardWithView(dataBinding.userPasswordEt)
        } else {
            showKeyboardWithView(dataBinding.userAccountEt)
        }

        dataBinding.apply {
            userAccountEt.addTextChangedListener {
                userAccountLayout.error = ""
            }
            userPasswordEt.addTextChangedListener {
                userAccountLayout.error = ""
            }

            registerTv.setOnClickListener {
                ARouter.getInstance()
                    .build(RouteTable.User.UserRegister)
                    .navigation()
            }

            forgotTv.setOnClickListener {
                BottomActionDialog(
                    this@LoginActivity,
                    arrayListOf(
                        SheetActionBean(1, "重置密码", R.drawable.ic_forgot_password),
                        SheetActionBean(2, "找回帐号", R.drawable.ic_forgot_account)
                    )
                ) {
                    ARouter.getInstance()
                        .build(RouteTable.User.UserForgot)
                        .withBoolean("isForgotPassword", it == 1)
                        .navigation()
                    return@BottomActionDialog true
                }.show()
            }
        }

        viewModel.accountErrorLiveData.observe(this) {
            dataBinding.userAccountLayout.error = it
        }
        viewModel.passwordErrorLiveData.observe(this) {
            dataBinding.userPasswordLayout.error = it
        }
        viewModel.loginLiveData.observe(this) {
            finish()
        }
    }

    private fun showKeyboardWithView(view: View) {
        view.postDelayed({
            showKeyboard(view)
        }, 200)
    }

    private fun showLimitDialog() {
        CommonDialog.Builder(this).apply {
            content = "当前应用为非官方版本\n\n无法使用帐号相关功能"
            cancelable = false
            touchCancelable = false
            addPositive {
                it.dismiss()
                finish()
            }
        }.build().show()
    }
}