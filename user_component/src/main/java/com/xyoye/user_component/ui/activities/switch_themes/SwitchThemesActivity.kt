package com.xyoye.user_component.ui.activities.switch_themes

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import com.alibaba.android.arouter.facade.annotation.Route
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.AppConfig
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.weight.dialog.CommonDialog
import com.xyoye.user_component.BR
import com.xyoye.user_component.R
import com.xyoye.user_component.databinding.ActivitySwitchThemesBinding
import kotlin.system.exitProcess

@Route(path = RouteTable.User.SwitchTheme)
class SwitchThemesActivity : BaseActivity<SwitchThemesViewModel, ActivitySwitchThemesBinding>() {

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            SwitchThemesViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_switch_themes

    override fun initView() {
        title = "深色模式"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_switch_theme, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.item_save_theme) {
            val targetMode = viewModel.targetMode.get() ?: return true

            if (viewModel.needReboot.get() == true) {
                CommonDialog.Builder(this).apply {
                    content = "新的设置需要重启应用才能生效"
                    addPositive {
                        it.dismiss()

                        AppConfig.putDarkMode(targetMode)

                        val intent = packageManager.getLaunchIntentForPackage(packageName)
                        startActivity(intent)

                        exitProcess(0)
                    }
                    addNegative {
                        it.dismiss()
                    }
                }.build().show()
            } else {
                AppCompatDelegate.setDefaultNightMode(targetMode)
                AppConfig.putDarkMode(targetMode)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}