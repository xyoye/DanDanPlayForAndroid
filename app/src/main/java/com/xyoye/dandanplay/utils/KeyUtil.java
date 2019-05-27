package com.xyoye.dandanplay.utils;

import android.content.Context;

/**
 * Created by xyoye on 2018/7/21.
 */

public class KeyUtil {
    private static final int DANDAN_APP_ID = 0xC1000001;
    private static final int DANDAN_APP_SECRET = 0xC1000002;
    private static final int BUGLY_APP_ID = 0xC1000003;
    private static final int SOPHIX_ID_SECRET = 0xC1000004;
    private static final int SOPHIX_APP_SECRET = 0xC1000005;
    private static final int SOPHIX_RSA_SECRET = 0xC1000006;

    static {
        System.loadLibrary("key");
    }

    public static String getDanDanAppId(Context context){
        return getKey(DANDAN_APP_ID, context);
    }

    public static String getDanDanAppSecret(Context context){
        return getKey(DANDAN_APP_SECRET, context);
    }

    public static String getBuglyAppId(Context context){
        return getKey(BUGLY_APP_ID, context);
    }

    public static String getSophixIdSecret(Context context){
        return getKey(SOPHIX_ID_SECRET, context);
    }

    public static String getSophixAppSecret(Context context){
        return getKey(SOPHIX_APP_SECRET, context);
    }

    public static String getSophixResSecret(Context context){
        return getKey(SOPHIX_RSA_SECRET, context);
    }

    private static native String getKey(int position, Context context);
}
