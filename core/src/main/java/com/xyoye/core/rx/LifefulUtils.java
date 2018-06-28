package com.xyoye.core.rx;

import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Created by yzd on 2017/4/8 0008.
 * <p>
 * 生命周期相关帮助类。
 */

public class LifefulUtils {

    private static final String TAG = LifefulUtils.class.getSimpleName();

    public static boolean shouldGoHome(WeakReference<Lifeful> lifefulWeakReference, boolean objectIsNull) {
        if (lifefulWeakReference == null) {
            Log.e(TAG, "Go home, lifefulWeakReference == null");
            return true;
        }
        Lifeful lifeful = lifefulWeakReference.get();
        /**
         * 如果传入的Lifeful不为null,但弱引用为null,则这个对象被回收了。
         */
        if (null == lifeful && !objectIsNull) {
            Log.e(TAG, "Go home, null == lifeful && !objectIsNull");
            return true;
        }
        /**
         * 对象的生命周期结束
         */
        if (null != lifeful && !lifeful.isAlive()) {
            Log.e(TAG, "Go home, null != lifeful && !lifeful.isAlive()");
            return true;
        }
        return false;
    }

    public static <T> boolean shouldGoHome(LifefulGenerator<T> lifefulGenerator) {
        if (null == lifefulGenerator) {
            Log.e(TAG, "Go home, null == lifefulGenerator");
            return true;
        }
        if (null == lifefulGenerator.getCallback()) {
            Log.e(TAG, "Go home, null == lifefulGenerator.getCallback()");
            return true;
        }
        return shouldGoHome(lifefulGenerator.getLifefulWeakReference(), lifefulGenerator.isLifefulNull());
    }
}
