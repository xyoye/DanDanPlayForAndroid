package com.xyoye.smb.info;

import android.support.annotation.Nullable;

/**
 * Created by xyoye on 2019/12/20.
 */

public class SmbLinkInfo {
    private String IP;
    private String account;
    private String password;
    private String domain;
    private String rootFolder;
    private boolean isAnonymous;

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
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

    @Nullable
    public String getDomain() {
        return domain;
    }

    public void setDomain(@Nullable String domain) {
        this.domain = domain;
    }

    @Nullable
    public String getRootFolder() {
        return rootFolder;
    }

    public void setRootFolder(@Nullable String rootFolder) {
        this.rootFolder = rootFolder;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }
}
