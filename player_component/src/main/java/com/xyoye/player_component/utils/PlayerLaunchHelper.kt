package com.xyoye.player_component.utils

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.config.RouteTable
import java.lang.ref.WeakReference

/**
 * Created by xyoye on 2022/12/24.
 */

class PlayerLaunchHelper: Application.ActivityLifecycleCallbacks {

    private object Holder {
        val instance = PlayerLaunchHelper()
    }

    companion object {
        @JvmStatic
        val instance = Holder.instance
    }
    
    private var mPlayerActivityRef: WeakReference<Activity>? = null


    fun register(activity: Activity) {
        mPlayerActivityRef = WeakReference(activity)
        activity.application.registerActivityLifecycleCallbacks(this)
    }

    fun unregister(activity: Activity) {
        mPlayerActivityRef = null
        activity.application.unregisterActivityLifecycleCallbacks(this)
    }

    fun onEnterPopupMode() {
        mPlayerActivityRef = null
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        //ignore
    }

    override fun onActivityStarted(activity: Activity) {
        considerLaunchPlayerActivity()
    }

    override fun onActivityResumed(activity: Activity) {
        //ignore
    }

    override fun onActivityPaused(activity: Activity) {
        //ignore
    }

    override fun onActivityStopped(activity: Activity) {
        //ignore
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {
        //ignore
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (mPlayerActivityRef?.get() == activity){
            mPlayerActivityRef = null
        }
    }

    private fun considerLaunchPlayerActivity() {
        val playerActivity = mPlayerActivityRef?.get() ?: return
        ARouter.getInstance()
            .build(RouteTable.Player.PlayerCenter)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .navigation(playerActivity)
    }
}