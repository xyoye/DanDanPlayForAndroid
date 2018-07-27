package com.xyoye.dandanplay.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

import com.xyoye.core.BaseApplication;
import com.xyoye.core.utils.Constants;
import com.xyoye.core.utils.SharedPreferencesUtil;

/**
 * Created by YE on 2018/7/2.
 */

public class AppConfigShare {

    private static class ShareHolder{
        private static AppConfigShare appConfigShare = new AppConfigShare();
    }

    private AppConfigShare(){

    }

    public static AppConfigShare getInstance(){
        return ShareHolder.appConfigShare;
    }

    private SharedPreferencesUtil getShare() {
        return SharedPreferencesUtil.getInstance(BaseApplication.get_context(), Constants.APP_CONFIG);
    }

    public String getDownloadFolder(){
       return getShare().load(Config.AppConfig.LOCAL_DANMU_FOLDER, Environment.getExternalStorageDirectory().getAbsolutePath()+"/DanDanPlayer");
    }

    public void setDownloadFolder(String path){
        getShare().save(Config.AppConfig.LOCAL_DANMU_FOLDER, path);
    }

    /**
     * 获取本地软件版本号
	 */
    public static String getLocalVersion(Context context) {
        String localVersionName = "";
        try {
            PackageInfo packageInfo = context.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            localVersionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersionName;
    }

}
