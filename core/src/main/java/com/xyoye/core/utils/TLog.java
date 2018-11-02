package com.xyoye.core.utils;

import android.util.Log;

import com.xyoye.core.BaseApplication;

/**
 * 日志打印工具
 * Created by Administrator on 2015/12/2.
 */
public class TLog {
    public static boolean DEBUG = true;
    private static final String LOG_TAG = "SIMICO";

    public static void analytics(String paramString) {
        if (DEBUG)
            Log.d(LOG_TAG, StringUtils.isEmpty(paramString) ? "param is empty" : paramString);
    }

    public static void analytics(String paramString1, String paramString) {
        if (DEBUG)
            Log.d(paramString1, StringUtils.isEmpty(paramString) ? "param is empty" : paramString);
    }

    public static void e(String paramString) {
        if (DEBUG)
            Log.e(LOG_TAG, StringUtils.isEmpty(paramString) ? "param is empty" : paramString);
    }

    public static void i(String paramString) {
        if (DEBUG)
            Log.i(LOG_TAG, StringUtils.isEmpty(paramString) ? "param is empty" : paramString);
    }

    public static void i(String paramString1, String paramString2) {
       if (paramString1 == null)  paramString1 = "";
       if (paramString2 == null) paramString2 = "";
        if (DEBUG)
            Log.i(paramString1, paramString2);
    }

    public static void i(String paramString1, Object... paramString2) {
        if (DEBUG) {
            StringBuilder builder = new StringBuilder();
            for (Object param : paramString2) {
                builder.append(param).append("_");
            }
            Log.i(paramString1, builder.toString());
        }
    }

    public static void v(String paramString) {
        if (DEBUG)
            Log.v(LOG_TAG, StringUtils.isEmpty(paramString) ? "param is empty" : paramString);
    }

    public static void w(String paramString) {
        if (DEBUG)
            Log.w(LOG_TAG, StringUtils.isEmpty(paramString) ? "param is empty" : paramString);
    }
}
