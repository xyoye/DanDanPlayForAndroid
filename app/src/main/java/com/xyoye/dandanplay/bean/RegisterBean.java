package com.xyoye.dandanplay.bean;

import com.xyoye.dandanplay.bean.params.RegisterParam;
import com.xyoye.dandanplay.utils.net.CommJsonEntity;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;
import com.xyoye.dandanplay.utils.net.RetroFactory;

import java.io.Serializable;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by YE on 2018/8/5.
 */


public class RegisterBean extends CommJsonEntity implements Serializable {

    /**
     * registerRequired : true
     * userId : 0
     * userName : string
     * legacyTokenNumber : 0
     * token : string
     * tokenExpireTime : 2018-08-05T12:07:37.834Z
     * userType : string
     * screenName : string
     * profileImage : string
     * appScope : string
     */

    private boolean registerRequired;
    private int userId;
    private String userName;
    private int legacyTokenNumber;
    private String token;
    private String tokenExpireTime;
    private String userType;
    private String screenName;
    private String profileImage;
    private String appScope;

    public boolean isRegisterRequired() {
        return registerRequired;
    }

    public void setRegisterRequired(boolean registerRequired) {
        this.registerRequired = registerRequired;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getLegacyTokenNumber() {
        return legacyTokenNumber;
    }

    public void setLegacyTokenNumber(int legacyTokenNumber) {
        this.legacyTokenNumber = legacyTokenNumber;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenExpireTime() {
        return tokenExpireTime;
    }

    public void setTokenExpireTime(String tokenExpireTime) {
        this.tokenExpireTime = tokenExpireTime;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getAppScope() {
        return appScope;
    }

    public void setAppScope(String appScope) {
        this.appScope = appScope;
    }

    public static void register(RegisterParam param, CommJsonObserver<RegisterBean> observer, NetworkConsumer consumer){
        RetroFactory.getInstance().register(param.getMap())
                .doOnSubscribe(consumer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}
