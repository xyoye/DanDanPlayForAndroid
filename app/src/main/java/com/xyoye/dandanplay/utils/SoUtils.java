package com.xyoye.dandanplay.utils;

import android.content.Context;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.app.IApplication;

/**
 * Created by xyoye on 2018/7/21.
 */

public class SoUtils {
    private static final String ERROR_RESULT = "error";
    private static final int DANDAN_APP_ID = 0xC1000001;
    private static final int DANDAN_APP_SECRET = 0xC1000002;
    private static final int BUGLY_APP_ID = 0xC1000003;

    private SoUtils() {

    }

    private static class Holder {
        static SoUtils instance = new SoUtils();
    }

    public static SoUtils getInstance() {
        return Holder.instance;
    }

    static {
        System.loadLibrary("key");
    }

    public boolean isOfficialApplication(){
        return !ERROR_RESULT.equals(getDanDanAppId());
    }

    public String getDanDanAppId() {
        return getKey(DANDAN_APP_ID);
    }

    public String getDanDanAppSecret() {
        return getKey(DANDAN_APP_SECRET);
    }

    public String getBuglyAppId() {
        return getKey(BUGLY_APP_ID);
    }

    private String getKey(int key) {
        String result = getKey(key, IApplication.get_context());
        if (ERROR_RESULT.equals(result)) {
            ToastUtils.showShort("错误，非官方应用");
        }
        return result;
    }

    private static native String getKey(int position, Context context);
}
