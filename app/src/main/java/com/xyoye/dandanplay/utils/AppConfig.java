package com.xyoye.dandanplay.utils;

import com.blankj.utilcode.util.SPUtils;

/**
 * Created by xyoye on 2018/7/2.
 */

public class AppConfig {
    private String lastPlayPath = null;

    private static class ShareHolder {
        private static AppConfig appConfig = new AppConfig();
    }

    private AppConfig() {

    }

    public static AppConfig getInstance() {
        return ShareHolder.appConfig;
    }

    /**
     * 昵称
     */
    public String getUserScreenName() {
        return SPUtils.getInstance().getString(Constants.UserConfig.USER_SCREEN_NAME, "");
    }

    public void saveUserScreenName(String userScreenName) {
        SPUtils.getInstance().put(Constants.UserConfig.USER_SCREEN_NAME, userScreenName);
    }

    /**
     * 用户名
     */
    public String getUserName() {
        return SPUtils.getInstance().getString(Constants.UserConfig.USER_NAME, "");
    }

    public void saveUserName(String username) {
        SPUtils.getInstance().put(Constants.UserConfig.USER_NAME, username);
    }

    /**
     * 用户头像
     */
    public String getUserImage() {
        return SPUtils.getInstance().getString(Constants.UserConfig.USER_IMAGE, "");
    }

    public void saveUserImage(String userImage) {
        SPUtils.getInstance().put(Constants.UserConfig.USER_IMAGE, userImage);
    }

    /**
     * 是否已登陆
     */
    public boolean isLogin() {
        return SPUtils.getInstance().getBoolean(Constants.UserConfig.IS_LOGIN);
    }

    public void setLogin(boolean isLogin) {
        SPUtils.getInstance().put(Constants.UserConfig.IS_LOGIN, isLogin);
    }

    /**
     * 文件夹排序
     */
    public int getFolderSortType() {
        String type = SPUtils.getInstance().getString(Constants.Config.FOLDER_COLLECTIONS, Constants.FolderSort.NAME_ASC + "");
        return Integer.valueOf(type);
    }

    public void saveFolderSortType(int type) {
        SPUtils.getInstance().put(Constants.Config.FOLDER_COLLECTIONS, type + "");
    }

    /**
     * 季番排序
     */
    public int getSeasonSortType() {
        return SPUtils.getInstance().getInt(Constants.Config.SEASON_SORT, 0);
    }

    public void saveSeasonSortType(int type) {
        SPUtils.getInstance().put(Constants.Config.SEASON_SORT, type);
    }

    /**
     * Token
     */
    public String getToken() {
        return SPUtils.getInstance().getString(Constants.UserConfig.TOKEN, "");
    }

    public void saveToken(String token) {
        SPUtils.getInstance().put(Constants.UserConfig.TOKEN, token);
    }

    /**
     * 下载目录
     */
    public String getDownloadFolder() {
        return SPUtils.getInstance().getString(Constants.Config.LOCAL_DOWNLOAD_FOLDER, Constants.DefaultConfig.downloadPath);
    }

    public void setDownloadFolder(String path) {
        SPUtils.getInstance().put(Constants.Config.LOCAL_DOWNLOAD_FOLDER, path);
    }

    /**
     * 开启硬解码
     */
    public boolean isOpenMediaCodeC() {
        return SPUtils.getInstance().getBoolean(Constants.PlayerConfig.SHARE_MEDIA_CODE_C);
    }

    public void setOpenMediaCodeC(boolean isUse) {
        SPUtils.getInstance().put(Constants.PlayerConfig.SHARE_MEDIA_CODE_C, isUse);
    }

    /**
     * 开启H265硬解码
     */
    public boolean isOpenMediaCodeCH265() {
        return SPUtils.getInstance().getBoolean(Constants.PlayerConfig.SHARE_MEDIA_CODE_C_H265);
    }

    public void setOpenMediaCodeCH265(boolean isUse) {
        SPUtils.getInstance().put(Constants.PlayerConfig.SHARE_MEDIA_CODE_C_H265, isUse);
    }

    /**
     * OpenSLES
     */
    public boolean isOpenSLES() {
        return SPUtils.getInstance().getBoolean(Constants.PlayerConfig.SHARE_OPEN_SLES);
    }

    public void setOpenSLES(boolean isUse) {
        SPUtils.getInstance().put(Constants.PlayerConfig.SHARE_OPEN_SLES, isUse);
    }

    /**
     * SurfaceRenders
     */
    public boolean isSurfaceRenders() {
        return SPUtils.getInstance().getBoolean(Constants.PlayerConfig.SHARE_SURFACE_RENDERS, true);
    }

    public void setSurfaceRenders(boolean isUse) {
        SPUtils.getInstance().put(Constants.PlayerConfig.SHARE_SURFACE_RENDERS, isUse);
    }

    /**
     * PlayerType
     */
    public int getPlayerType() {
        return SPUtils.getInstance().getInt(Constants.PlayerConfig.SHARE_PLAYER_TYPE, com.xyoye.player.commom.utils.Constants.IJK_PLAYER);
    }

    public void setPlayerType(int type) {
        SPUtils.getInstance().put(Constants.PlayerConfig.SHARE_PLAYER_TYPE, type);
    }

    /**
     * PixelFormat
     */
    public String getPixelFormat() {
        return SPUtils.getInstance().getString(Constants.PlayerConfig.SHARE_PIXEL_FORMAT, Constants.PlayerConfig.PIXEL_OPENGL_ES2);
    }

    public void setPixelFormat(String pixelFormat) {
        SPUtils.getInstance().put(Constants.PlayerConfig.SHARE_PIXEL_FORMAT, pixelFormat);
    }

    /**
     * 自动匹配弹幕
     */
    public boolean isAutoLoadDanmu() {
        return SPUtils.getInstance().getBoolean(Constants.PlayerConfig.AUTO_LOAD_DANMU);
    }

    public void setAutoLoadDanmu(boolean auto) {
        SPUtils.getInstance().put(Constants.PlayerConfig.AUTO_LOAD_DANMU, auto);
    }

    /**
     * 是否使用网络字幕
     */
    public boolean isUseNetWorkSubtitle() {
        return SPUtils.getInstance().getBoolean(Constants.PlayerConfig.USE_NETWORK_SUBTITLE, true);
    }

    public void setUseNetWorkSubtitle(boolean isUse) {
        SPUtils.getInstance().put(Constants.PlayerConfig.USE_NETWORK_SUBTITLE, isUse);
    }

    /**
     * 自动匹配同名字幕
     */
    public boolean isAutoLoadLocalSubtitle() {
        return SPUtils.getInstance().getBoolean(Constants.PlayerConfig.AUTO_LOAD_LOCAL_SUBTITLE);
    }

    public void setAutoLoadLocalSubtitle(boolean auto) {
        SPUtils.getInstance().put(Constants.PlayerConfig.AUTO_LOAD_LOCAL_SUBTITLE, auto);
    }

    /**
     * 自动匹配网络字幕
     */
    public boolean isAutoLoadNetworkSubtitle() {
        return SPUtils.getInstance().getBoolean(Constants.PlayerConfig.AUTO_LOAD_NETWORK_SUBTITLE);
    }

    public void setAutoLoadNetworkSubtitle(boolean auto) {
        SPUtils.getInstance().put(Constants.PlayerConfig.AUTO_LOAD_NETWORK_SUBTITLE, auto);
    }

    /**
     * 补丁版本号
     */
    public int getPatchVersion() {
        return SPUtils.getInstance().getInt(Constants.Config.PATCH_VERSION, 0);
    }

    public void setPatchVersion(int version) {
        SPUtils.getInstance().put(Constants.Config.PATCH_VERSION, version);
    }

    /**
     * 自动查询补丁
     */
    public boolean isAutoQueryPatch() {
        return SPUtils.getInstance().getBoolean(Constants.Config.AUTO_QUERY_PATCH, true);
    }

    public void setAutoQueryPatch(boolean auto) {
        SPUtils.getInstance().put(Constants.Config.AUTO_QUERY_PATCH, auto);
    }

    /**
     * MKV提示
     */
    public boolean isShowMkvTips() {
        return SPUtils.getInstance().getBoolean(Constants.Config.SHOW_MKV_TIPS, true);
    }

    public void hideMkvTips() {
        SPUtils.getInstance().put(Constants.Config.SHOW_MKV_TIPS, false);
    }

    /**
     * 外链展示选择弹幕提示框
     */
    public boolean isShowOuterChainDanmuDialog() {
        return SPUtils.getInstance().getBoolean(Constants.PlayerConfig.SHOW_OUTER_CHAIN_DANMU_DIALOG, true);
    }

    public void setShowOuterChainDanmuDialog(boolean isShow) {
        SPUtils.getInstance().put(Constants.PlayerConfig.SHOW_OUTER_CHAIN_DANMU_DIALOG, isShow);
    }

    /**
     * 外链打开是否进入选择弹幕页面
     */
    public boolean isOuterChainDanmuSelect() {
        return SPUtils.getInstance().getBoolean(Constants.PlayerConfig.OUTER_CHAIN_DANMU_SELECT, true);
    }

    public void setOuterChainDanmuSelect(boolean isOpen) {
        SPUtils.getInstance().put(Constants.PlayerConfig.OUTER_CHAIN_DANMU_SELECT, isOpen);
    }

    /**
     * 在线播放是否生成日志
     */
    public boolean isOnlinePlayLogEnable() {
        return SPUtils.getInstance().getBoolean(Constants.PlayerConfig.ONLINE_PLAY_LOG, true);
    }

    public void setOnlinePlayLogEnable(boolean isEnable) {
        SPUtils.getInstance().put(Constants.PlayerConfig.ONLINE_PLAY_LOG, isEnable);
    }

    /**
     * 开启弹幕云过滤
     */
    public boolean isCloudDanmuFilter() {
        return SPUtils.getInstance().getBoolean(Constants.Config.CLOUD_DANMU_FILTER, false);
    }

    public void setCloudDanmuFilter(boolean isOpen) {
        SPUtils.getInstance().put(Constants.Config.CLOUD_DANMU_FILTER, isOpen);
    }

    /**
     * 上次更新云过滤的时间
     */
    public long getUpdateFilterTime() {
        return SPUtils.getInstance().getLong(Constants.Config.UPDATE_FILTER_TIME, 0);
    }

    public void setUpdateFilterTime(long time) {
        SPUtils.getInstance().put(Constants.Config.UPDATE_FILTER_TIME, time);
    }

    /**
     * 上次播放的视频
     */
    public String getLastPlayVideo() {
        if (lastPlayPath == null) {
            lastPlayPath = SPUtils.getInstance().getString(Constants.Config.LAST_PLAY_VIDEO_PATH, "");
        }
        return lastPlayPath;
    }

    public void setLastPlayVideo(String videoPath) {
        lastPlayPath = videoPath;
        SPUtils.getInstance().put(Constants.Config.LAST_PLAY_VIDEO_PATH, videoPath);
    }

    /**
     * smb文件排序是否为grid layout
     */
    public boolean smbIsGridLayout() {
        return SPUtils.getInstance().getBoolean(Constants.Config.SMB_IS_GRID_LAYOUT, true);
    }

    public void setSmbIsGridLayout(boolean isGridLayout) {
        SPUtils.getInstance().put(Constants.Config.SMB_IS_GRID_LAYOUT, isGridLayout);
    }

    /**
     * 上一次远程登录数据
     */
    public String getRemoteLoginData() {
        return SPUtils.getInstance().getString(Constants.Config.REMOTE_LOGIN_DATA);
    }

    public void setRemoteLoginData(String data) {
        SPUtils.getInstance().put(Constants.Config.REMOTE_LOGIN_DATA, data);
    }

    /**
     * 是否关闭启动页
     */
    public boolean isCloseSplashPage() {
        return SPUtils.getInstance().getBoolean(Constants.Config.CLOSE_SPLASH_PAGE);
    }

    public void setCloseSplashPage(boolean isClose) {
        SPUtils.getInstance().put(Constants.Config.CLOSE_SPLASH_PAGE, isClose);
    }

    /**
     * 上次登录时间
     */
    public long getLastLoginTime() {
        return SPUtils.getInstance().getLong(Constants.Config.LAST_LOGIN_TIME);
    }

    public void setLastLoginTime(long timeMs) {
        SPUtils.getInstance().put(Constants.Config.LAST_LOGIN_TIME, timeMs);
    }
}
