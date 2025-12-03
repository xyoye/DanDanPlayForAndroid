package com.xyoye.dandanplay.ui.splash

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.view.KeyEvent
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatDelegate
import com.gyf.immersionbar.ImmersionBar
import com.xyoye.common_component.base.BaseAppCompatActivity
import com.xyoye.common_component.config.AppConfig
import com.xyoye.dandanplay.R
import com.xyoye.dandanplay.databinding.ActivitySplashBinding
import com.xyoye.dandanplay.ui.main.MainActivity
import com.xyoye.dandanplay.utils.image_anim.path.TextPathAnimView

/**
 * Created by xyoye on 2020/7/27.
 */

class SplashActivity : BaseAppCompatActivity<ActivitySplashBinding>() {
    override fun getLayoutId() = R.layout.activity_splash

    override fun initStatusBar() {
        ImmersionBar.with(this)
            .transparentBar()
            .statusBarDarkFont(false)
            .init()
    }

    override fun initView() {
        window.run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                insetsController?.hide(WindowInsets.Type.statusBars())
            } else {
                addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }
            statusBarColor = Color.TRANSPARENT
            addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        }

        val mode = AppConfig.getDarkMode()
        AppCompatDelegate.setDefaultNightMode(mode)

        if (!AppConfig.getShowSplashAnimation()) {
            launchActivity()
            return
        }

        val alphaAnimation = AlphaAnimation(0f, 1f)
        alphaAnimation.apply {
            repeatCount = Animation.ABSOLUTE
            interpolator = LinearInterpolator()
            duration = 2000
        }

        val appName = "弹弹play 概念版 v${packageManager.getPackageInfo(packageName, 0).versionName}"

        dataBinding.run {
            appNameTv.text = appName

            textPathView.setAnimListener(object : TextPathAnimView.AnimListener {
                override fun onStart() {

                }

                override fun onEnd() {
                    textPathView.postDelayed({
                        launchActivity()
                    }, 350)
                }

                override fun onLoop() {

                }
            })
            textPathView.startAnim()
            iconSvgView.start()
            appNameLl.startAnimation(alphaAnimation)
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        return if (event!!.keyCode == KeyEvent.KEYCODE_BACK) {
            true
        } else {
            super.dispatchKeyEvent(event)
        }
    }

    override fun onDestroy() {
        dataBinding.textPathView.cancelAnim()
        super.onDestroy()
    }

    private fun launchActivity() {
        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(
            com.xyoye.common_component.R.anim.anim_activity_enter,
            com.xyoye.common_component.R.anim.anim_activity_exit
        )
        finish()
    }
}