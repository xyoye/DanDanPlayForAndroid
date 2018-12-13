package com.xyoye.dandanplay.bean.params;

import android.content.Context;

import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.utils.KeyUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by YE on 2018/8/5.
 */


public class ResetPasswordParam implements Serializable {

    /**
     * appId : string
     * userName : string
     * email : string
     * unixTimestamp : 0
     * hash : string
     */

    private String appId;
    private String userName;
    private String email;
    private long unixTimestamp;
    private String hash;

    public ResetPasswordParam(String userName, String email){
        this.userName = userName;
        this.email = email;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getUnixTimestamp() {
        return unixTimestamp;
    }

    public void setUnixTimestamp(long unixTimestamp) {
        this.unixTimestamp = unixTimestamp;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Map<String, String> getMap(){
        Map<String, String> map = new HashMap<>();
        map.put("appId", this.appId);
        map.put("userName", this.userName);
        map.put("email", this.email);
        map.put("unixTimestamp", this.unixTimestamp+"");
        map.put("hash", this.hash);
        return map;
    }
    public void buildHash(Context context) {
        if (StringUtils.isEmpty(userName) ||
                StringUtils.isEmpty(email) ||
                StringUtils.isEmpty(appId) ||
                unixTimestamp == 0){
            LogUtils.e("注册信息错误");
            ToastUtils.showShort("注册信息错误");
        }else {
            String builder = this.appId +
                    this.email +
                    this.unixTimestamp +
                    this.userName +
                    KeyUtil.getDanDanAppSecret(context);
            hash = EncryptUtils.encryptMD5ToString(builder);
        }
    }

}
