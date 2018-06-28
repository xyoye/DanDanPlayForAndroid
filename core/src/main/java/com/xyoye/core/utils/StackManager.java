package com.xyoye.core.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.support.annotation.NonNull;

import java.util.Stack;

/**
 * 管理所有Activity 当启动一个Activity时，就将其保存到Stack中， 退出时，从Stack中删除
 * Created by Administrator on 2015/7/3.
 */
public class StackManager {
    /**
     * 保存所有Activity
     */
    private volatile Stack<Activity> activityStack = new Stack<>();

    private static volatile StackManager instance;

    private StackManager() {
    }

    /**
     * 创建单例类，提供静态方法调用
     *
     * @return ActivityManager
     */
    public static StackManager getStackManager() {
        if (instance == null) {
            instance = new StackManager();
        }
        return instance;
    }

    /**
     * 退出Activity
     *
     * @param activity Activity
     */
    public void popActivity(Activity activity) {
        if (activity != null) {
            activity.finish();
            activityStack.remove(activity);
            TLog.e("remove");
        }
    }

    /**
     * 获得当前栈顶的Activity
     *
     * @return Activity Activity
     */
    public Activity currentActivity() {
        return activityStack.isEmpty() ? null : activityStack.lastElement();
    }

    /**
     * 获得当前栈底的Activity
     *
     * @return Activity Activity
     */
    public Activity lastActivity() {
        Activity activity = null;
        if (!activityStack.empty()) {
            activity = activityStack.firstElement();
        }
        return activity;
    }

    /**
     * 将当前Activity推入栈中
     *
     * @param activity Activity
     */
    public void pushActivity(Activity activity) {
        if (activityStack.size() > 6) {
            popActivity(activityStack.get(1));
        }
        activityStack.add(activity);
    }

    /**
     * 退出栈中其他所有Activity
     *
     * @param cls Class 类名
     */
    @SuppressWarnings("rawtypes")
    public void popOtherActivity(@NonNull Class cls) {
        while (true) {
            Activity a1 = currentActivity();
            Activity a2 = lastActivity();
            if (a1 != null && !a1.getClass().equals(cls)) {
                popActivity(a1);
                continue;
            }
            if (a2 != null && !a2.getClass().equals(cls)) {
                popActivity(a2);
                continue;
            }
            break;
        }
    }

    /**
     * 退出栈中所有Activity
     */
    public void popAllActivitys() {
        while (true) {
            Activity activity = currentActivity();
            if (activity == null) return;
            popActivity(activity);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Activity> T getActivtiy(@NonNull Class<T> cls) {
        try {
            for (Activity activity : activityStack) {
                if (activity != null && activity.getClass().equals(cls)) {
                    return (T) activity;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 退出应用
     */
    public void AppExit() {
        try {
            popAllActivitys();
            Process.killProcess(Process.myPid());
            System.exit(0);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public void startNextActivity(Class<?> activity) {
        Activity curActivity = currentActivity();
        Intent intent = new Intent(curActivity, activity);
        curActivity.startActivity(intent);
        //curActivity.overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
    }

    private String getRunningActivityName(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
    }

}
