package com.xyoye.user_component.ui.dialog

import android.app.Activity
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.DevelopConfig
import com.xyoye.common_component.extension.startUrlActivity
import com.xyoye.common_component.network.repository.UserRepository
import com.xyoye.common_component.utils.SupervisorScope
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.BaseBottomDialog
import com.xyoye.user_component.R
import com.xyoye.user_component.databinding.DialogDeveloperAuthenticateBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2025/1/22
 *    desc  :
 */

class DeveloperAuthenticateDialog(
    private val activity: Activity,
    private val onAuthenticate: () -> Unit
) : BaseBottomDialog<DialogDeveloperAuthenticateBinding>(activity) {

    private lateinit var binding: DialogDeveloperAuthenticateBinding

    override fun getChildLayoutId(): Int {
        return R.layout.dialog_developer_authenticate
    }

    override fun initView(binding: DialogDeveloperAuthenticateBinding) {
        this.binding = binding

        setTitle("开发者认证")

        binding.inputAppId.setText(DevelopConfig.getAppId())
        binding.inputAppSecret.setText(DevelopConfig.getAppSecret())

        setNegativeText("忽略")
        setNegativeListener {
            dismiss()
        }

        setPositiveListener {
            checkAuthenticate()
        }

        binding.tvReadDocument.setOnClickListener {
            activity.startUrlActivity("https://doc.dandanplay.com/open/")
        }
    }

    /**
     * 开发者认证
     */
    private fun checkAuthenticate() {
        val appId = binding.inputAppId.text.toString()
        val appSecret = binding.inputAppSecret.text.toString()

        if (appId.isEmpty() || appSecret.isEmpty()) {
            ToastCenter.showWarning("请输入AppId和AppSecret")
            return
        }

        SupervisorScope.IO.launch {
            loading(true)
            val result = UserRepository.checkAuthenticate(appId, appSecret).getOrNull()
            loading(false)
            if (result != null && result.code() == 200) {
                authenticateSuccess(appId, appSecret)
                return@launch
            }

            if (result?.code() == 403) {
                ToastCenter.showError("认证失败，凭证不被允许访问")
            } else {
                ToastCenter.showError("认证过程中发生意外，请稍后重试")
            }
        }
    }

    /**
     * 显示/隐藏加载框
     */
    private suspend fun loading(show: Boolean) {
        if (activity is BaseActivity<*, *>) {
            withContext(Dispatchers.Main) {
                if (show) {
                    activity.showLoading()
                } else {
                    activity.hideLoading()
                }
            }
        }
    }

    /**
     * 认证成功
     */
    private fun authenticateSuccess(appId: String, appSecret: String) {
        DevelopConfig.setAppId(appId)
        DevelopConfig.setAppSecret(appSecret)
        ToastCenter.showSuccess("认证成功")
        onAuthenticate.invoke()
        dismiss()
    }
}