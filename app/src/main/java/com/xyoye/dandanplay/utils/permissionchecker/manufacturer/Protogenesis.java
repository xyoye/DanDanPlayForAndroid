package com.xyoye.dandanplay.utils.permissionchecker.manufacturer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

/**
 * Created by joker on 2017/8/4.
 */

public class Protogenesis implements PermissionsPage {
    private final Activity activity;

    public Protogenesis(Activity activity) {
        this.activity = activity;
    }

    // system details setting page
    @Override
    public Intent settingIntent() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);

        return intent;
    }
}
