package com.xyoye.dandanplay.utils;

import android.os.Environment;

import com.blankj.utilcode.util.SPUtils;

/**
 * Created by YE on 2018/7/2.
 */

public class AppConfig {

    private static class ShareHolder{
        private static AppConfig appConfig = new AppConfig();
    }

    private AppConfig(){

    }

    public static AppConfig getInstance(){
        return ShareHolder.appConfig;
    }

    /**
     * 首次进入app
     */
    public boolean isFirstStart(){
        boolean isFirst = SPUtils.getInstance().getBoolean(Constants.AppConfig.FIRST_OPEN_APP, true);
        if (isFirst) SPUtils.getInstance().put(Constants.AppConfig.FIRST_OPEN_APP, false);
        return isFirst;
    }

    /**
     * 昵称
     */
    public String getUserScreenName(){
        return SPUtils.getInstance().getString(Constants.AppConfig.USER_SCREEN_NAME, "");
    }

    public void saveUserScreenName(String userScreenName){
        SPUtils.getInstance().put(Constants.AppConfig.USER_SCREEN_NAME, userScreenName);
    }

    /**
     * 用户名
     */
    public String getUserName(){
        return SPUtils.getInstance().getString(Constants.AppConfig.USER_NAME, "");
    }

    public void saveUserName(String username){
        SPUtils.getInstance().put(Constants.AppConfig.USER_NAME, username);
    }

    /**
     * 用户头像
     */
    public String getUserImage(){
        return SPUtils.getInstance().getString(Constants.AppConfig.USER_IMAGE, "");
    }

    public void saveUserImage(String userImage){
        SPUtils.getInstance().put(Constants.AppConfig.USER_IMAGE, userImage);
    }
    
    /**
     * 是否已登陆
     */
    public boolean isLogin(){
        return SPUtils.getInstance().getBoolean(Constants.AppConfig.IS_LOGIN);
    }

    public void setLogin(boolean isLogin){
        SPUtils.getInstance().put(Constants.AppConfig.IS_LOGIN, isLogin);
    }

    /**
     * 文件夹排序
     */
    public int getFolderSortType(){
        String type = SPUtils.getInstance().getString(Constants.AppConfig.FOLDER_COLLECTIONS, Constants.Collection.NAME_ASC+"");
        return Integer.valueOf(type);
    }

    public void saveFolderSortType(int type){
        SPUtils.getInstance().put(Constants.AppConfig.FOLDER_COLLECTIONS, type+"");
    }

    /**
     * Token
     */
    public String getToken(){
        return SPUtils.getInstance().getString(Constants.AppConfig.TOKEN, "");
    }

    public void saveToken(String token){
        SPUtils.getInstance().put(Constants.AppConfig.TOKEN, token);
    }

    /**
     * 下载目录
     */
    public String getDownloadFolder(){
        return SPUtils.getInstance().getString(Constants.AppConfig.LOCAL_DOWNLOAD_FOLDER, Environment.getExternalStorageDirectory().getAbsolutePath()+"/DanDanPlayer");
    }

    public void setDownloadFolder(String path){
        SPUtils.getInstance().put(Constants.AppConfig.LOCAL_DOWNLOAD_FOLDER, path);
    }

    /**
     * SD卡路径
     */
    public String getSDFolderUri(){
        return SPUtils.getInstance().getString(Constants.AppConfig.LOCAL_SDCARD_FOLDER, "");
    }

    public void setSDFolderUri(String uri){
        SPUtils.getInstance().put(Constants.AppConfig.LOCAL_SDCARD_FOLDER, uri);
    }

    /**
     * 自动匹配弹幕
     */
    public boolean isAutoLoadDanmu(){
        return SPUtils.getInstance().getBoolean(Constants.AppConfig.AUTO_LOAD_DANMU);
    }

    public void setAutoLoadDanmu(boolean auto){
        SPUtils.getInstance().put(Constants.AppConfig.AUTO_LOAD_DANMU, auto);
    }

    /**
     * 开启硬解码
     */
    public boolean isOpenMediaCodeC(){
        return SPUtils.getInstance().getBoolean(Constants.SHARE_MEDIA_CODE_C);
    }

    public void setOpenMediaCodeC(boolean isUse){
        SPUtils.getInstance().put(Constants.SHARE_MEDIA_CODE_C, isUse);
    }

    /**
     * 开启H265硬解码
     */
    public boolean isOpenMediaCodeCH265(){
        return SPUtils.getInstance().getBoolean(Constants.SHARE_MEDIA_CODE_C_H265);
    }

    public void setOpenMediaCodeCH265(boolean isUse){
        SPUtils.getInstance().put(Constants.SHARE_MEDIA_CODE_C_H265, isUse);
    }

    /**
     * OpenSLES
     */
    public boolean isOpenSLES(){
        return SPUtils.getInstance().getBoolean(Constants.SHARE_OPEN_SLES);
    }

    public void setOpenSLES(boolean isUse){
        SPUtils.getInstance().put(Constants.SHARE_OPEN_SLES, isUse);
    }

    /**
     * SurfaceRenders
     */
    public boolean isSurfaceRenders(){
        return SPUtils.getInstance().getBoolean(Constants.SHARE_SURFACE_RENDERS);
    }

    public void setSurfaceRenders(boolean isUse){
        SPUtils.getInstance().put(Constants.SHARE_SURFACE_RENDERS, isUse);
    }

    /**
     * PlayerType
     */
    public int getPlayerType(){
        return SPUtils.getInstance().getInt(Constants.SHARE_PLAYER_TYPE, com.player.ijkplayer.utils.Constants.IJK_PLAYER);
    }

    public void setPlayerType(int type){
        SPUtils.getInstance().put(Constants.SHARE_PLAYER_TYPE, type);
    }

    /**
     * PixelFormat
     */
    public String getPixelFormat(){
        return SPUtils.getInstance().getString(Constants.SHARE_PIXEL_FORMAT, "");
    }

    public void setPixelFormat(String pixelFormat){
        SPUtils.getInstance().put(Constants.SHARE_PIXEL_FORMAT, pixelFormat);
    }

    /**
     * 下载速度限制
     */
    public int getTorrentDownloadSpeed(){
        return SPUtils.getInstance().getInt(Constants.TORRENT_DOWNLOAD_SPEED, -1);
    }

    public void setTorrentDownloadSpeed(int speed){
        SPUtils.getInstance().put(Constants.TORRENT_DOWNLOAD_SPEED, speed);
    }

    /**
     * 上传速度限制
     */
    public int getTorrentUploadSpeed(){
        return SPUtils.getInstance().getInt(Constants.TORRENT_UPLOAD_SPEED, -1);
    }

    public void setTorrentUploadSpeed(int speed){
        SPUtils.getInstance().put(Constants.TORRENT_UPLOAD_SPEED, speed);
    }

    /**
     * 补丁版本号
     */
    public int getPatchVersion(){
        return SPUtils.getInstance().getInt(Constants.AppConfig.PATCH_VERSION, 0);
    }

    public void setPatchVersion(int version){
        SPUtils.getInstance().put(Constants.AppConfig.PATCH_VERSION, version);
    }

    /**
     * 自动查询补丁
     */
    public boolean isAutoQueryPatch(){
        return SPUtils.getInstance().getBoolean(Constants.AppConfig.AUTO_QUERY_PATCH, true);
    }

    public void setAutoQueryPatch(boolean auto){
        SPUtils.getInstance().put(Constants.AppConfig.AUTO_QUERY_PATCH, auto);
    }

}
