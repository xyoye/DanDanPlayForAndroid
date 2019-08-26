package com.xyoye.dandanplay.torrent.utils;

import com.blankj.utilcode.util.SPUtils;

/**
 * Created by xyoye on 2019/8/23.
 */

public class TorrentConfig {
    public static final int DEFAULT_PROXY_PORT = 6881;

    public static class Engine {
        public static final String LIB_TORRENT = "lib_torrent";
        public static final String THUNDER = "thunder";
    }

    public enum ProxyType {
        NONE(0),
        SOCKS4(1),
        SOCKS5(2),
        HTTP(3);

        private final int value;

        ProxyType(int value) {
            this.value = value;
        }

        public static ProxyType fromValue(int value) {
            ProxyType[] enumValues = ProxyType.class.getEnumConstants();
            for (ProxyType type : enumValues) {
                if (type.value() == value) {
                    return type;
                }
            }

            return NONE;
        }

        public int value() {
            return value;
        }
    }


    //引擎类别
    private static final String DOWNLOAD_ENGINE = "download_engine";
    //最大活动任务数量
    private static final String MAX_ACTIVITY_TASK = "max_activity_task";
    //最大下载速度
    private static final String MAX_DOWNLOAD_RATE = "max_download_rate";
    //最大下上传速度
    private static final String MAX_UPLOAD_RATE = "max_upload_rate";

    //仅wifi下下载
    private static final String DOWNLOAD_ONLY_WIFI = "download_only_wifi";
    //网络配置
    private static final String DHT_ENABLE = "dht_enable";
    private static final String LSD_ENABLE = "lsd_enable";
    private static final String UTP_ENABLE = "utp_enable";
    private static final String UPNP_ENABLE = "upnp_enable";
    private static final String NAT_PMP_ENABLE = "nat_pmp_enable";

    //代理配置
    private static final String PROXY_TYPE = "proxy_type";
    private static final String PROXY_IP = "proxy_ip";
    private static final String PROXY_PORT = "proxy_port";
    private static final String PROXY_PEER_ENABLE = "proxy_peer_enable";
    private static final String PROXY_AUTH_ENABLE = "proxy_auth_enable";
    private static final String PROXY_ACCOUNT = "proxy_ip";
    private static final String PROXY_PASSWORD = "proxy_ip";

    private static class Holder {
        private static TorrentConfig appConfig = new TorrentConfig();
    }

    private TorrentConfig() {

    }

    public static TorrentConfig getInstance() {
        return Holder.appConfig;
    }

    /**
     * 下载引擎
     */
    public String getDownloadEngine() {
        return SPUtils.getInstance().getString(DOWNLOAD_ENGINE, Engine.LIB_TORRENT);
    }

    public void setDownloadEngine(String engine) {
        SPUtils.getInstance().put(DOWNLOAD_ENGINE, engine);
    }

    /**
     * 最大同时活动任务数量
     */
    public int getMaxTaskCount() {
        return SPUtils.getInstance().getInt(MAX_ACTIVITY_TASK, 4);
    }

    public void setMaxTaskCount(int taskCount) {
        SPUtils.getInstance().put(MAX_ACTIVITY_TASK, taskCount);
    }

    /**
     * 最大下载速度
     */
    public int getMaxDownloadRate() {
        return SPUtils.getInstance().getInt(MAX_DOWNLOAD_RATE, 1000);
    }

    public void setMaxDownloadRate(int rate) {
        SPUtils.getInstance().put(MAX_DOWNLOAD_RATE, rate);
    }

    /**
     * 最大上传速度
     */
    public int getMaxUploadRate() {
        return SPUtils.getInstance().getInt(MAX_UPLOAD_RATE, 1000);
    }

    public void setMaxUploadRate(int rate) {
        SPUtils.getInstance().put(MAX_UPLOAD_RATE, rate);
    }

    /**
     * 仅允许wifi下载
     */
    public boolean isDownloadOnlyWifi() {
        return SPUtils.getInstance().getBoolean(DOWNLOAD_ONLY_WIFI, false);
    }

    public void setDownloadOnlyWifi(boolean isOpen) {
        SPUtils.getInstance().put(DOWNLOAD_ONLY_WIFI, isOpen);
    }

    /**
     * dht配置
     */
    public boolean isDhtEnable() {
        return SPUtils.getInstance().getBoolean(DHT_ENABLE, false);
    }

    public void setDhtEnable(boolean isEnable) {
        SPUtils.getInstance().put(DHT_ENABLE, isEnable);
    }

    /**
     * lsd配置
     */
    public boolean isLsdEnable() {
        return SPUtils.getInstance().getBoolean(LSD_ENABLE, false);
    }

    public void setLsdEnable(boolean isEnable) {
        SPUtils.getInstance().put(LSD_ENABLE, isEnable);
    }

    /**
     * utp配置
     */
    public boolean isUtpEnable() {
        return SPUtils.getInstance().getBoolean(UTP_ENABLE, false);
    }

    public void setUtpEnable(boolean isEnable) {
        SPUtils.getInstance().put(UTP_ENABLE, isEnable);
    }

    /**
     * upnp配置
     */
    public boolean isUpnpEnable() {
        return SPUtils.getInstance().getBoolean(UPNP_ENABLE, false);
    }

    public void setUpnpEnable(boolean isEnable) {
        SPUtils.getInstance().put(UPNP_ENABLE, isEnable);
    }

    /**
     * nat-pmp配置
     */
    public boolean isNatPmpEnable() {
        return SPUtils.getInstance().getBoolean(NAT_PMP_ENABLE, false);
    }

    public void setNatPmpEnable(boolean isEnable) {
        SPUtils.getInstance().put(NAT_PMP_ENABLE, isEnable);
    }

    /**
     * 代理类别
     */
    public ProxyType getProxyType() {
        int proxyType = SPUtils.getInstance().getInt(PROXY_TYPE, ProxyType.NONE.value);
        return ProxyType.fromValue(proxyType);
    }

    public void setProxyType(ProxyType proxyType) {
        SPUtils.getInstance().put(PROXY_TYPE, proxyType.value);
    }

    /**
     * 代理IP
     */
    public String getProxyIp() {
        return SPUtils.getInstance().getString(PROXY_IP);
    }

    public void setProxyIp(String proxyIp) {
        SPUtils.getInstance().put(PROXY_IP, proxyIp);
    }

    /**
     * 代理端口
     */
    public String getProxyPort() {
        return SPUtils.getInstance().getString(PROXY_PORT);
    }

    public void setProxyPort(String proxyPort) {
        SPUtils.getInstance().put(PROXY_PORT, proxyPort);
    }

    /**
     * 是否用于用户间连接
     */
    public boolean isProxyPeerEnable() {
        return SPUtils.getInstance().getBoolean(PROXY_PEER_ENABLE, false);
    }

    public void setProxyPeerEnable(boolean isEnable) {
        SPUtils.getInstance().put(PROXY_PEER_ENABLE, isEnable);
    }

    /**
     * 是否使用密码验证
     */
    public boolean isProxyAuthEnable() {
        return SPUtils.getInstance().getBoolean(PROXY_AUTH_ENABLE, false);
    }

    public void setProxyAuthEnable(boolean isEnable) {
        SPUtils.getInstance().put(PROXY_AUTH_ENABLE, isEnable);
    }

    /**
     * 代理帐号
     */
    public String getProxyAccount() {
        return SPUtils.getInstance().getString(PROXY_ACCOUNT);
    }

    public void setProxyAccount(String proxyAccount) {
        SPUtils.getInstance().put(PROXY_ACCOUNT, proxyAccount);
    }

    /**
     * 代理密码
     */
    public String getProxyPassword() {
        return SPUtils.getInstance().getString(PROXY_PASSWORD);
    }

    public void setProxyPassword(String proxyPassword) {
        SPUtils.getInstance().put(PROXY_PASSWORD, proxyPassword);
    }
}
