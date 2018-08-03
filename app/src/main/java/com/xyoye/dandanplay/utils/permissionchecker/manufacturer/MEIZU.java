package com.xyoye.dandanplay.utils.permissionchecker.manufacturer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;

import com.xyoye.dandanplay.utils.permissionchecker.ManufacturerSupportUtil;

/**
 * Created by joker on 2017/8/24.
 */

public class MEIZU implements PermissionsPage {
    private final Activity activity;
    private final String N_MANAGER_OUT_CLS = "com.meizu.safe.permission.PermissionMainActivity";
    private final String L_MANAGER_OUT_CLS = "com.meizu.safe.SecurityMainActivity";
    private final String PKG = "com.meizu.safe";

    public MEIZU(Activity activity) {
        this.activity = activity;
    }

    @Override
    public Intent settingIntent() {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(PACK_TAG, activity.getPackageName());
        ComponentName comp = new ComponentName(PKG, getCls());
        intent.setComponent(comp);

        return intent;
    }

    private String getCls() {
        if (ManufacturerSupportUtil.isAndroidL()) {
            return L_MANAGER_OUT_CLS;
        } else {
            return N_MANAGER_OUT_CLS;
        }
    }
}
