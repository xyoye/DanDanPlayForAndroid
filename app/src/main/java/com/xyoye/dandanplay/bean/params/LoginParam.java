package com.xyoye.dandanplay.bean.params;

import android.content.Context;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.utils.Encryptor;
import com.xyoye.core.utils.KeyUtil;
import com.xyoye.core.utils.StringUtils;
import com.xyoye.core.utils.TLog;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by YE on 2018/7/22.
 */


public class LoginParam implements Serializable {

    /**
     * userName : string
     * password : string
     * appId : string
     * unixTimestamp : 0
     * hash : string
     */

    private String userName;
    private String password;
    private String appId;
    private long unixTimestamp;
    private String hash;

    public LoginParam(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
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

    public void buildHash(Context context) {
        if (StringUtils.isEmpty(userName) ||
                StringUtils.isEmpty(password) ||
                StringUtils.isEmpty(appId) ||
                unixTimestamp == 0){
            TLog.e("登录信息错误");
            ToastUtils.showShort("登录信息错误");
        }else {
            String builder = this.appId +
                    this.password +
                    this.unixTimestamp +
                    this.userName +
                    KeyUtil.getAppSecret(context);
            hash = Encryptor.encryptMD5(builder);
        }
    }

    public Map<String, String> getMap(){
        Map<String, String> map = new HashMap<>();
        map.put("userName", this.userName);
        map.put("password", this.password);
        map.put("appId", this.appId);
        map.put("unixTimestamp", this.unixTimestamp+"");
        map.put("hash", this.hash);
        return map;
    }
}
