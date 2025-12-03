package com.xyoye.user_component.ui.activities.commonly_folder

import android.os.Environment
import com.therouter.router.Route
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.AppConfig
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.weight.dialog.CommonDialog
import com.xyoye.common_component.weight.dialog.FileManagerDialog
import com.xyoye.data_component.enums.FileManagerAction

import com.xyoye.user_component.BR
import com.xyoye.user_component.R
import com.xyoye.user_component.databinding.ActivityCommonlyFolderBinding

@Route(path = RouteTable.User.CommonManager)
class CommonlyFolderActivity :
    BaseActivity<CommonlyFolderViewModel, ActivityCommonlyFolderBinding>() {

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            CommonlyFolderViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_commonly_folder

    override fun initView() {

        var commonFolder1Path = AppConfig.getCommonlyFolder1()
        commonFolder1Path =
            if (commonFolder1Path.isNullOrEmpty()) "路径：未设置" else "路径：$commonFolder1Path"
        dataBinding.commonlyFolder1Tv.text = commonFolder1Path

        var commonFolder2Path = AppConfig.getCommonlyFolder2()
        commonFolder2Path =
            if (commonFolder2Path.isNullOrEmpty()) "路径：未设置" else "路径：$commonFolder2Path"
        dataBinding.commonlyFolder2Tv.text = commonFolder2Path

        dataBinding.lastOpenFolderSw.isChecked = AppConfig.getLastOpenFolderEnable()

        initListener()
    }

    private fun initListener() {

        val defaultPath = Environment.getExternalStorageDirectory().absolutePath

        dataBinding.commonlyFolder1Ll.setOnClickListener {
            FileManagerDialog(
                this,
                FileManagerAction.ACTION_SELECT_DIRECTORY,
                defaultPath
            ) {
                AppConfig.setCommonlyFolder1(it)
                val path = "路径：$it"
                dataBinding.commonlyFolder1Tv.text = path
            }.show()
        }

        dataBinding.commonlyFolder2Ll.setOnClickListener {
            FileManagerDialog(
                this,
                FileManagerAction.ACTION_SELECT_DIRECTORY,
                defaultPath
            ) {
                AppConfig.setCommonlyFolder2(it)
                val path = "路径：$it"
                dataBinding.commonlyFolder2Tv.text = path
            }.show()
        }

        dataBinding.commonlyFolder1Ll.setOnLongClickListener {
            CommonDialog.Builder(this).apply {
                content = "确认删除常用文件夹1？"
                addPositive {
                    AppConfig.setCommonlyFolder1("")
                    dataBinding.commonlyFolder1Tv.text = "路径：未设置"
                    it.dismiss()
                }
                addNegative { it.dismiss() }
            }.build().show()
            return@setOnLongClickListener true
        }

        dataBinding.commonlyFolder2Ll.setOnLongClickListener {
            CommonDialog.Builder(this).apply {
                content = "确认删除常用文件夹2？"
                addPositive {
                    AppConfig.setCommonlyFolder2("")
                    dataBinding.commonlyFolder2Tv.text = "路径：未设置"
                    it.dismiss()
                }
                addNegative { it.dismiss() }
            }.build().show()
            return@setOnLongClickListener true
        }


        dataBinding.lastOpenFolderSw.setOnCheckedChangeListener { _, isChecked ->
            AppConfig.setLastOpenFolderEnable(isChecked)
        }
    }
}