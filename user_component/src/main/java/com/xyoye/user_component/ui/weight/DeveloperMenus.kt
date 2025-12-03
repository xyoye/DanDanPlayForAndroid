package com.xyoye.user_component.ui.weight

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.xyoye.common_component.config.DevelopConfig
import com.xyoye.common_component.extension.toResDrawable
import com.xyoye.common_component.utils.SecurityHelper
import com.xyoye.common_component.utils.SupervisorScope
import com.xyoye.user_component.R
import com.xyoye.user_component.ui.dialog.DeveloperAuthenticateDialog
import kotlinx.coroutines.launch

/**
 *    author: xyoye1997@outlook.com
 *    time  : 2025/1/22
 *    desc  :
 */
class DeveloperMenus private constructor(
    private val activity: AppCompatActivity,
    menu: Menu
) {

    companion object {
        fun inflater(activity: AppCompatActivity, menu: Menu): DeveloperMenus {
            activity.menuInflater.inflate(R.menu.menu_developer, menu)
            return DeveloperMenus(activity, menu)
        }
    }

    // 菜单项
    private val item = menu.findItem(R.id.item_source_authenticate)

    // 验证弹窗
    private var authenticateDialog: DeveloperAuthenticateDialog? = null

    private val isDeveloperAuthenticate: Boolean
        get() = DevelopConfig.getAppId().isNotEmpty() && DevelopConfig.getAppSecret().isNotEmpty()

    init {
        updateItem()

        // 考虑自动显示认证弹窗
        SupervisorScope.Main.launch { considerShowAuthenticateDialog() }
    }

    fun onOptionsItemSelected(item: MenuItem) {
        if (item.itemId == R.id.item_source_authenticate) {
            showAuthenticateDialog()
            return
        }
    }

    /**
     * 显示认证弹窗
     */
    private fun showAuthenticateDialog() {
        authenticateDialog?.dismiss()
        authenticateDialog = DeveloperAuthenticateDialog(activity) {
            SupervisorScope.Main.launch { updateItem() }
        }
        authenticateDialog?.show()
    }

    /**
     * 考虑显示认证弹窗
     */
    private fun considerShowAuthenticateDialog() {
        // 官方应用，不做处理
        if (SecurityHelper.getInstance().isOfficialApplication) {
            return
        }

        // 已认证，不做处理
        if (isDeveloperAuthenticate) {
            return
        }

        // 已自动提示认证弹窗
        if (DevelopConfig.getIsAutoShowAuthDialog()) {
            return
        }

        // 只自动提示一次
        DevelopConfig.setIsAutoShowAuthDialog(true)

        // 显示认证弹窗
        showAuthenticateDialog()
    }

    /**
     * 更新菜单项
     */
    private fun updateItem() {
        if (SecurityHelper.getInstance().isOfficialApplication) {
            item.isVisible = false
            return
        }
        item.isVisible = true

        val (title, iconRes) = if (isDeveloperAuthenticate) {
            "已认证" to R.drawable.ic_developer_authenticated
        } else {
            "未认证" to R.drawable.ic_developer_unauthenticated
        }

        item.title = title
        item.icon = iconRes.toResDrawable(activity)
    }
}