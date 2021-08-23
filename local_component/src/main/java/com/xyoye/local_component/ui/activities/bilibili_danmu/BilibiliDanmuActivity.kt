package com.xyoye.local_component.ui.activities.bilibili_danmu

import android.content.Intent
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.weight.BottomActionDialog
import com.xyoye.common_component.weight.dialog.CommonEditDialog
import com.xyoye.data_component.bean.EditBean
import com.xyoye.data_component.bean.SheetActionBean
import com.xyoye.data_component.enums.SheetActionType
import com.xyoye.local_component.BR
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.ActivityBilibiliDanmuBinding

@Route(path = RouteTable.Local.BiliBiliDanmu)
class BilibiliDanmuActivity : BaseActivity<BilibiliDanmuViewModel, ActivityBilibiliDanmuBinding>() {
    companion object {
        private const val DOWNLOAD_BY_LINK = 1
        private const val DOWNLOAD_BY_URL = 2
        private const val DOWNLOAD_BY_AV_CODE = 3
        private const val DOWNLOAD_BY_BV_CODE = 4

        private const val REQUEST_CODE_SELECT_URL = 1001
    }

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            BilibiliDanmuViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_bilibili_danmu

    override fun initView() {
        title = "BiliBili弹幕下载"

        val firstMessage = "请选择下载模式\n"
        dataBinding.downloadMessageEt.isFocusable = false
        dataBinding.downloadMessageEt.isFocusableInTouchMode = false
        dataBinding.downloadMessageEt.setText(firstMessage)

        dataBinding.addDownloadBt.setOnClickListener {
            showActionDialog()
        }

        viewModel.downloadMessageLiveData.observe(this) {
            dataBinding.downloadMessageEt.append("$it\n")
        }
        viewModel.clearMessageLiveData.observe(this) {
            dataBinding.downloadMessageEt.setText(firstMessage)
        }

        showActionDialog()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_SELECT_URL) {
            if (resultCode == RESULT_OK) {
                data?.getStringExtra("url_data")?.let {
                    viewModel.downloadByUrl(it)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showActionDialog() {
        BottomActionDialog(
            mutableListOf(
                SheetActionBean(DOWNLOAD_BY_LINK, "选取链接下载", R.drawable.ic_select_link),
                SheetActionBean(DOWNLOAD_BY_URL, "输入链接下载", R.drawable.ic_input_code),
                SheetActionBean(DOWNLOAD_BY_AV_CODE, "输入av号下载", R.drawable.ic_input_code),
                SheetActionBean(DOWNLOAD_BY_BV_CODE, "输入bv号下载", R.drawable.ic_input_code)
            ), SheetActionType.VERTICAL, "下载弹幕"
        ) {
            when (it) {
                DOWNLOAD_BY_LINK -> {
                    ARouter.getInstance().build(RouteTable.User.WebView)
                        .withString("titleText", "选择链接")
                        .withString("url", "http://www.bilibili.com")
                        .withBoolean("isSelectMode", true)
                        .navigation(this, REQUEST_CODE_SELECT_URL)
                }
                else -> showInputDialog(it)
            }
            return@BottomActionDialog true
        }.show(this)
    }

    private fun showInputDialog(action: Int) {
        val (title, wranning, hint) = when (action) {
            DOWNLOAD_BY_URL -> Triple("输入链接下载", "链接不能为空", "番剧链接")
            DOWNLOAD_BY_AV_CODE -> Triple("输入AV号下载", "AV号不能为空", "纯数字AV号")
            DOWNLOAD_BY_BV_CODE -> Triple("输入BV号下载", "BV号不能为空", "完整BV号")
            else -> return
        }

        CommonEditDialog(
            EditBean(title, wranning, hint),
            inputOnlyDigit = action == DOWNLOAD_BY_AV_CODE
        ) {
            when (action) {
                DOWNLOAD_BY_URL -> viewModel.downloadByUrl(it)
                DOWNLOAD_BY_AV_CODE -> viewModel.downloadByCode(it, true)
                DOWNLOAD_BY_BV_CODE -> viewModel.downloadByCode(it, false)
            }
        }.show(this)
    }
}