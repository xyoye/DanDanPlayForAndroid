package com.xyoye.dandanplay.bean.event;

/**
 * Created by xyy on 2018/11/20.
 */

public class AuthLanEvent {
    private String account;
    private String password;
    private String domain;
    private boolean anonymous;
    private int position;

    public AuthLanEvent(String account, String password, String domain,boolean anonymous, int position) {
        this.account = account;
        this.password = password;
        this.domain = domain;
        this.anonymous = anonymous;
        this.position = position;
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

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
