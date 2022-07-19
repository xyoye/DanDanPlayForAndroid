package com.xyoye.anime_component.ui.dialog

import android.app.Activity
import com.xyoye.anime_component.R
import com.xyoye.anime_component.databinding.DialogSearchDomainBinding
import com.xyoye.common_component.config.AppConfig
import com.xyoye.common_component.extension.startUrlActivity
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.common_component.weight.dialog.BaseBottomDialog

/**
 * Created by xyoye on 2021/2/24.
 */

class SearchDomainDialog(
    private val activity: Activity,
    private val callback: (String) -> Unit
) : BaseBottomDialog<DialogSearchDomainBinding>(activity) {

    override fun getChildLayoutId() = R.layout.dialog_search_domain

    override fun initView(binding: DialogSearchDomainBinding) {

        setTitle("完善节点链接")

        val domainLink = AppConfig.getMagnetResDomain()
        binding.searchDomainEt.setText(domainLink)

        binding.findDomainTv.setOnClickListener {
            activity.startUrlActivity("https://github.com/kansaer/dandanplay-apiNode")
        }

        setNegativeListener { dismiss() }

        setPositiveListener {
            val domain = binding.searchDomainEt.text.toString()
            if (domain.isEmpty()) {
                ToastCenter.showError("节点链接不能为空")
                return@setPositiveListener
            }

            if (!domain.startsWith("http", true)) {
                ToastCenter.showWarning("节点链接缺少协议：http/https")
                return@setPositiveListener
            }

            callback.invoke(domain)
            dismiss()
        }
    }
}