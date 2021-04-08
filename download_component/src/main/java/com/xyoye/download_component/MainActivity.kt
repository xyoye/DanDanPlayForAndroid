package com.xyoye.download_component

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.launcher.ARouter
import com.gyf.immersionbar.ImmersionBar
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.isNightMode
import com.xyoye.common_component.weight.dialog.FileManagerDialog
import com.xyoye.data_component.enums.FileManagerAction

/**
 * Created by xyoye on 2020/12/28.
 */

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ImmersionBar.with(this)
            .statusBarColor(com.xyoye.common_component.R.color.status_bar_color)
            .fitsSystemWindows(true)
            .statusBarDarkFont(!isNightMode())
            .init()

        findViewById<Button>(R.id.launch_download_manager_bt).setOnClickListener {
            ARouter.getInstance()
                .build(RouteTable.Download.DownloadList)
                .navigation()
        }

        findViewById<Button>(R.id.add_torrent_task_bt).setOnClickListener {
            FileManagerDialog(FileManagerAction.ACTION_SELECT_TORRENT) {
                ARouter.getInstance()
                    .build(RouteTable.Download.DownloadSelection)
                    .withString("torrentPath", it)
                    .navigation()
            }.show(this)
        }

        val urlEt = findViewById<EditText>(R.id.play_url_et)
        findViewById<Button>(R.id.play_bt).setOnClickListener {
            val url = urlEt.text.toString()
            if (url.isNotEmpty()) {
                play(url)
            }
        }
    }

    private fun play(url: String) {
        ARouter.getInstance()
                .build(RouteTable.Download.PlaySelection)
                .withString("magnetLink", url)
                .navigation()
    }
}