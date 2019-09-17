package com.xyoye.dandanplay.bean;

import com.xyoye.dandanplay.bean.params.ChangePasswordParam;
import com.xyoye.dandanplay.bean.params.LoginParam;
import com.xyoye.dandanplay.bean.params.ResetPasswordParam;
import com.xyoye.dandanplay.utils.net.CommJsonEntity;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;
import com.xyoye.dandanplay.utils.net.RetroFactory;

import java.io.Serializable;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xyoye on 2018/7/22.
 */

public class PersonalBean extends CommJsonEntity implements Serializable {

    /**
     * registerRequired : true
     * userId : 0
     * legacyTokenNumber : 0
     * token : string
     * tokenExpireTime : 2018-07-22T05:58:22.661Z
     * userType : string
     * screenName : string
     * profileImage : string
     * appScope : string
     */

    private boolean registerRequired;
    private int userId;
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

    public static void login(LoginParam param, CommJsonObserver<PersonalBean> observer, NetworkConsumer consumer){
        RetroFactory.getInstance().login(param.getMap())
                .doOnSubscribe(consumer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public static void changePassword(ChangePasswordParam param, CommJsonObserver<CommJsonEntity> observer, NetworkConsumer consumer){
        RetroFactory.getInstance().changePassword(param.getMap())
                .doOnSubscribe(consumer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public static void resetPassword(ResetPasswordParam param, CommJsonObserver<CommJsonEntity> observer, NetworkConsumer consumer){
        RetroFactory.getInstance().resetPassword(param.getMap())
                .doOnSubscribe(consumer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public static void changeScreenName(String screenName, CommJsonObserver<CommJsonEntity> observer, NetworkConsumer consumer){
        RetroFactory.getInstance().changeScreenName(screenName)
                .doOnSubscribe(consumer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}
