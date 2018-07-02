package com.xyoye.dandanplay.utils;

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

    public String getDanmuFolder(){
       return getShare().decryptLoad(Config.AppConfig.LOCAL_DANMU_FOLDER);
    }

}
