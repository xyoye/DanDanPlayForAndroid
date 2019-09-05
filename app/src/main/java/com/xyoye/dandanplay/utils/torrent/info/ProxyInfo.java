package com.xyoye.dandanplay.utils.torrent.info;

import com.xyoye.dandanplay.utils.torrent.utils.TorrentConfig;

/**
 * Created by xyoye on 2019/8/23.
 */

public class ProxyInfo {

    private TorrentConfig.ProxyType proxyType;
    private String IP;
    private int port;
    private boolean peerEnable;
    private boolean authEnable;
    private String account;
    private String password;

    public TorrentConfig.ProxyType getProxyType() {
        return proxyType;
    }

    public void setProxyType(TorrentConfig.ProxyType proxyType) {
        this.proxyType = proxyType;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setPort(String portStr) {
        try{
            port = Integer.valueOf(portStr);
        }catch (NumberFormatException e){
            port = TorrentConfig.DEFAULT_PROXY_PORT;
        }
    }

    public boolean isPeerEnable() {
        return peerEnable;
    }

    public void setPeerEnable(boolean peerEnable) {
        this.peerEnable = peerEnable;
    }

    public boolean isAuthEnable() {
        return authEnable;
    }

    public void setAuthEnable(boolean authEnable) {
        this.authEnable = authEnable;
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
}
