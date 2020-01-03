package com.xyoye.dandanplay.bean;

import android.support.annotation.NonNull;

/**
 * Created by xyoye on 2019/3/30.
 */

public class SmbDeviceBean implements Comparable<SmbDeviceBean>{
    private String url;
    private String name;
    private String nickName;
    private String domain;
    private String account;
    private String password;
    private String rootFolder;
    private boolean anonymous;

    private int smbType;
    private boolean isEditStatus;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
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

    public String getRootFolder() {
        return rootFolder;
    }

    public void setRootFolder(String rootFolder) {
        this.rootFolder = rootFolder;
    }

    public int getSmbType() {
        return smbType;
    }

    public void setSmbType(int smbType) {
        this.smbType = smbType;
    }

    public boolean isEditStatus() {
        return isEditStatus;
    }

    public void setEditStatus(boolean editStatus) {
        isEditStatus = editStatus;
    }

    @Override
    public int compareTo(@NonNull SmbDeviceBean o) {
        return url.compareTo(o.url);
    }
}
