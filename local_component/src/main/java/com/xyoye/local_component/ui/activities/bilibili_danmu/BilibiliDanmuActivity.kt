package com.xyoye.local_component.ui.activities.bilibili_danmu

import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.weight.BottomActionDialog
import com.xyoye.common_component.weight.dialog.CommonEditDialog
import com.xyoye.data_component.bean.EditBean
import com.xyoye.data_component.bean.SheetActionBean
import com.xyoye.local_component.BR
import com.xyoye.local_component.R
import com.xyoye.local_component.databinding.ActivityBilibiliDanmuBinding
import com.xyoye.local_component.ui.weight.BiliBiliDanmuMenus

@Route(path = RouteTable.Local.BiliBiliDanmu)
class BilibiliDanmuActivity : BaseActivity<BilibiliDanmuViewModel, ActivityBilibiliDanmuBinding>() {
    companion object {
        private const val REQUEST_CODE_SELECT_URL = 1001
    }

    // 标题栏菜单管理器
    private lateinit var mMenus: BiliBiliDanmuMenus

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        mMenus = BiliBiliDanmuMenus.inflater(this, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        mMenus.onOptionsItemSelected(item)
        return super.onOptionsItemSelected(item)
    }

    private fun showActionDialog() {
        BottomActionDialog(
            this,
            DownloadType.values().map { it.toAction() },
            "下载弹幕"
        ) {
            if (it.actionId == DownloadType.SELECT) {
                ARouter.getInstance().build(RouteTable.User.WebView)
                    .withString("titleText", "选择链接")
                    .withString("url", "http://www.bilibili.com")
                    .withBoolean("isSelectMode", true)
                    .navigation(this, REQUEST_CODE_SELECT_URL)
            } else {
                showInputDialog(it.actionId as DownloadType)
            }
            return@BottomActionDialog true
        }.show()
    }

    private fun showInputDialog(type: DownloadType) {
        val editBean = when (type) {
            DownloadType.URL -> EditBean("输入链接下载", "链接不能为空", "番剧链接")
            DownloadType.AV_CODE -> EditBean("输入AV号下载", "AV号不能为空", "纯数字AV号")
            DownloadType.BV_CODE -> EditBean("输入BV号下载", "BV号不能为空", "完整BV号")
            else -> return
        }

        CommonEditDialog(
            this, editBean, type == DownloadType.AV_CODE
        ) {
            when (type) {
                DownloadType.URL -> viewModel.downloadByUrl(it)
                DownloadType.AV_CODE -> viewModel.downloadByCode(it, true)
                DownloadType.BV_CODE -> viewModel.downloadByCode(it, false)
                else -> {}
            }
        }.show()
    }

    private enum class DownloadType(val title: String, val icon: Int) {
        SELECT("选取链接下载", R.drawable.ic_select_link),
        URL("输入链接下载", R.drawable.ic_input_code),
        AV_CODE("输入av号下载", R.drawable.ic_input_code),
        BV_CODE("输入bv号下载", R.drawable.ic_input_code);

        fun toAction() = SheetActionBean(this, title, icon)
    }
}