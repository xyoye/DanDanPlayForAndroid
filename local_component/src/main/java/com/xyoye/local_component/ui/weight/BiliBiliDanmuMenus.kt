package com.xyoye.local_component.ui.weight

import android.view.Menu
import android.view.MenuItem
import com.xyoye.common_component.config.AppConfig
import com.xyoye.common_component.weight.dialog.CommonEditDialog
import com.xyoye.data_component.bean.EditBean
import com.xyoye.local_component.R
import com.xyoye.local_component.ui.activities.bilibili_danmu.BilibiliDanmuActivity

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2024/9/2
 *    desc  :
 */
class BiliBiliDanmuMenus private constructor(
    private val activity: BilibiliDanmuActivity
) {

    companion object {
        fun inflater(activity: BilibiliDanmuActivity, menu: Menu): BiliBiliDanmuMenus {
            activity.menuInflater.inflate(R.menu.menu_bilibili_danmu, menu)
            return BiliBiliDanmuMenus(activity)
        }
    }

    fun onOptionsItemSelected(item: MenuItem) {
        if (item.itemId == R.id.item_modify_user_agent) {
            showModifyUserAgentDialog()
            return
        }
    }

    private fun showModifyUserAgentDialog() {
        CommonEditDialog(
            activity,
            EditBean(
                "修改User-Agent",
                "User-Agent不应为空",
                "请输入User-Agent",
                defaultText = AppConfig.getJsoupUserAgent(),
                inputTips = "使用【选取链接下载】时，会携带此User-Agent请求链接的网页内容"
            ),
        ) {
            AppConfig.setJsoupUserAgent(it)
        }.show()
    }
}