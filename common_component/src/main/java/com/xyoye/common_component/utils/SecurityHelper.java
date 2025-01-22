package com.xyoye.common_component.utils;

import android.content.Context;

import com.xyoye.common_component.base.app.BaseApplication;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xyoye on 2021/1/6.
 */

public class SecurityHelper {
    private static final String ERROR_RESULT = "error";
    private static final int KEY_DANDAN = 0xC1000001;
    private static final int KEY_BUGLY = 0xC1000002;
    private static final int KEY_ALIYUN = 0xC1000003;

    private final Context appContext;

    static {
        System.loadLibrary("security");
    }

    private SecurityHelper() {
        appContext = BaseApplication.Companion.getAppContext();
    }

    private static class Holder {
        static SecurityHelper instance = new SecurityHelper();
    }

    public static SecurityHelper getInstance() {
        return Holder.instance;
    }

    public String getBuglyId() {
        return getKey(KEY_BUGLY, appContext);
    }

    public String getAppId() {
        return getKey(KEY_DANDAN, appContext);
    }

    public String getAliyunSecret() {
        return getKey(KEY_ALIYUN, appContext);
    }

    public String buildHash(String hashInfo) {
        return buildHash(hashInfo, appContext);
    }

    public Boolean isOfficialApplication() {
        return !ERROR_RESULT.equals(getAppId());
    }

    public Map<String, String> getSignatureMap(String path, Context context) {
        Object signature = getSignature(path, context);
        if (signature == null) {
            return null;
        }

        if (signature instanceof Map) {
            HashMap<String, String> map = new HashMap<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) signature).entrySet()) {
                Object key = entry.getKey();
                if (key instanceof String) {
                    Object value = entry.getValue();
                    if (value instanceof String) {
                        map.put((String) key, (String) value);
                    }
                }
            }
            return map;
        }

        return null;
    }

    private static native String getKey(int position, Context context);

    private static native String buildHash(String hashInfo, Context context);

    private static native Object getSignature(String path, Context context);
}
