package com.xyoye.dandanplay.utils;

import com.xyoye.core.BaseApplication;
import com.xyoye.core.utils.Constants;
import com.xyoye.core.utils.SharedPreferencesUtil;

/**
 * Created by YE on 2018/7/22.
 */


public class TokenShare {
    private static class TokenHolder{
        private static TokenShare tokenShare = new TokenShare();
    }

    private TokenShare(){

    }

    public static TokenShare getInstance(){
        return TokenHolder.tokenShare;
    }

    private SharedPreferencesUtil getShare() {
        return SharedPreferencesUtil.getInstance(BaseApplication.get_context(), Constants.TOKEN);
    }

    public String getToken(){
        return getShare().load(Config.AppConfig.TOKEN,"");
    }

    public void saveToken(String token){
        getShare().save(Config.AppConfig.TOKEN, token);
    }
}
