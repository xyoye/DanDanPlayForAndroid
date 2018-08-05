package com.xyoye.dandanplay.bean.params;

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
    private int unixTimestamp;
    private String hash;

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

    public int getUnixTimestamp() {
        return unixTimestamp;
    }

    public void setUnixTimestamp(int unixTimestamp) {
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
}
