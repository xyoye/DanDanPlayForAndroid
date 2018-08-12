package com.xyoye.dandanplay.utils;

import com.xyoye.core.BaseApplication;
import com.xyoye.core.utils.Constants;
import com.xyoye.core.utils.SharedPreferencesUtil;

/**
 * Created by YE on 2018/7/22.
 */


public class UserInfoShare {
    private static class UserInfoHolder{
        private static UserInfoShare userInfoShare = new UserInfoShare();
    }

    private UserInfoShare(){

    }

    public static UserInfoShare getInstance(){
        return UserInfoShare.UserInfoHolder.userInfoShare;
    }

    private SharedPreferencesUtil getShare() {
        return SharedPreferencesUtil.getInstance(BaseApplication.get_context(), Constants.USER_INFO);
    }

    public String getUserScreenName(){
        return getShare().decryptLoad(Config.AppConfig.USER_SCREEN_NAME);
    }

    public void saveUserScreenName(String userScreenName){
        getShare().encryptSave(Config.AppConfig.USER_SCREEN_NAME, userScreenName);
    }

    public String getUserName(){
        return getShare().decryptLoad(Config.AppConfig.USER_NAME);
    }

    public void saveUserName(String username){
        getShare().encryptSave(Config.AppConfig.USER_NAME, username);
    }

    public String getUserPassword(){
        return getShare().decryptLoad(Config.AppConfig.USER_NAME);
    }

    public void saveUserPassword(String username){
        getShare().encryptSave(Config.AppConfig.USER_NAME, username);
    }

    public String getUserImage(){
        return getShare().decryptLoad(Config.AppConfig.USER_IMAGE);
    }

    public void saveUserImage(String userImage){
        getShare().encryptSave(Config.AppConfig.USER_IMAGE, userImage);
    }

    public boolean isLogin(){
        return getShare().loadBooleanSharedPreference(Config.AppConfig.IS_LOGIN);
    }

    public void setLogin(boolean isLogin){
        getShare().saveSharedPreferences(Config.AppConfig.IS_LOGIN, isLogin);
    }

    public int getFolderCollectionsType(){
        String type = getShare().load(Config.AppConfig.FOLDER_COLLECTIONS, Config.Collection.NAME_ASC+"");
        return Integer.valueOf(type);
    }

    public void saveFolderCollectionsType(int type){
        getShare().save(Config.AppConfig.FOLDER_COLLECTIONS, type+"");
    }
}
