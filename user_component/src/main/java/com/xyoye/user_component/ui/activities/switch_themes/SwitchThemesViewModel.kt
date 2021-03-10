package com.xyoye.user_component.ui.activities.switch_themes

import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import com.xyoye.common_component.base.BaseViewModel
import com.xyoye.common_component.base.app.BaseApplication
import com.xyoye.common_component.extension.isNightMode

class SwitchThemesViewModel : BaseViewModel() {

    val followSystem = ObservableField<Boolean>()
    val isDarkMode = ObservableField<Boolean>()

    //是否需要重启应用，仅当需要深色模式切换时为true
    val needReboot = ObservableField(false)
    //目标模式
    val targetMode = ObservableField<Int>()

    init {
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        followSystem.set(currentMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        isDarkMode.set(currentMode == AppCompatDelegate.MODE_NIGHT_YES)

        followSystem.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                val isFollowSystem = followSystem.get() ?: false
                if (isFollowSystem) {
                    switchMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                } else {
                    val mode = if (isSystemDarkMode()) {
                        AppCompatDelegate.MODE_NIGHT_YES
                    } else {
                        AppCompatDelegate.MODE_NIGHT_NO
                    }
                    switchMode(mode)
                }
            }
        })
    }

    fun switchMode(mode: Int) {
        targetMode.set(mode)

        val currentMode = AppCompatDelegate.getDefaultNightMode()

        //当前是跟随系统
        if (currentMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            when (mode){
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> {
                    needReboot.set(false)
                    followSystem.set(true)
                }
                AppCompatDelegate.MODE_NIGHT_YES -> {
                    needReboot.set(!isSystemDarkMode())
                    followSystem.set(false)
                    isDarkMode.set(true)
                }
                AppCompatDelegate.MODE_NIGHT_NO -> {
                    needReboot.set(isSystemDarkMode())
                    followSystem.set(false)
                    isDarkMode.set(false)
                }
            }
            return
        }

        //当前是手动设置
        when (mode){
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> {
                val reboot = (isSystemDarkMode() && currentMode == AppCompatDelegate.MODE_NIGHT_NO)
                        || (!isSystemDarkMode() && currentMode != AppCompatDelegate.MODE_NIGHT_NO)
                needReboot.set(reboot)
                followSystem.set(true)
            }
            AppCompatDelegate.MODE_NIGHT_YES -> {
                needReboot.set(currentMode == AppCompatDelegate.MODE_NIGHT_NO)
                followSystem.set(false)
                isDarkMode.set(true)
            }
            AppCompatDelegate.MODE_NIGHT_NO -> {
                needReboot.set(currentMode == AppCompatDelegate.MODE_NIGHT_YES)
                followSystem.set(false)
                isDarkMode.set(false)
            }
        }
    }

    private fun isSystemDarkMode() = BaseApplication.getAppContext().isNightMode()
}