package com.xyoye.dandanplay.bean;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by xyoye on 2018/11/19.
 */

public class LanDeviceBean implements Comparable<LanDeviceBean>,Serializable{
    private String ip;
    private String deviceName;
    private String domain;
    private String account;
    private String password;
    private boolean anonymous;

    public LanDeviceBean() {
    }

    public LanDeviceBean(String ip, String deviceName) {
        this.ip = ip;
        this.deviceName = deviceName;
    }

    public LanDeviceBean(String account, String password, boolean anonymous){
        this.account = account;
        this.password = password;
        this.anonymous = anonymous;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    @Override
    public int compareTo(@NonNull LanDeviceBean o) {
        return ip.compareTo(o.ip);
    }
}
