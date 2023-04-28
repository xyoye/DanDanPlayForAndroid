package com.xyoye.common_component.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.util.*

/**
 * Created by xyoye on 2022/9/17.
 */

class ActivityHelper private constructor() : Application.ActivityLifecycleCallbacks {

    private object Holder {
        val instance = ActivityHelper()
    }

    companion object {
        @JvmStatic
        val instance = Holder.instance
    }

    private val mActivityList = LinkedList<Activity>()

    fun init(application: Application) {
        mActivityList.clear()
        application.unregisterActivityLifecycleCallbacks(this)
        application.registerActivityLifecycleCallbacks(this)
    }

    fun getTopActivity(): Activity? {
        if (mActivityList.isEmpty()) {
            return null
        }
        return mActivityList.first { isActivityAlive(it) }
    }

    override fun onActivityCreated(activity: Activity, p1: Bundle?) {
        setTopActivity(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        //ignore
    }

    override fun onActivityResumed(activity: Activity) {
        setTopActivity(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        //ignore
    }

    override fun onActivityStopped(activity: Activity) {
        //ignore
    }

    override fun onActivitySaveInstanceState(activity: Activity, p1: Bundle) {
        //ignore
    }

    override fun onActivityDestroyed(activity: Activity) {
        mActivityList.remove(activity)
    }

    private fun setTopActivity(activity: Activity) {
        if (mActivityList.contains(activity)) {
            if (mActivityList.first != activity) {
                mActivityList.remove(activity)
                mActivityList.addFirst(activity)
            }
        } else {
            mActivityList.addFirst(activity)
        }
    }

    private fun isActivityAlive(activity: Activity): Boolean {
        return activity.isFinishing.not() && activity.isDestroyed.not()
    }

    fun findActivity(clazz: Class<*>): Activity? {
        return mActivityList.lastOrNull { it.javaClass == clazz }
    }
}