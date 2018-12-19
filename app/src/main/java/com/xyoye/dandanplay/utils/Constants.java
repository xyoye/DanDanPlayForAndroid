package com.xyoye.dandanplay.utils;

/**
 * Created by YE on 2018/6/29 0029.
 */


public class Constants {
    public static String BilibiliUrl = "http://comment.bilibili.com/";

    public static class AppConfig {
        //首次进入app
        public static final String FIRST_OPEN_APP = "first_open_app";
        //本地下载目录
        public static final String LOCAL_DOWNLOAD_FOLDER = "local_download_folder";
        //sd卡目录
        public static final String LOCAL_SDCARD_FOLDER = "local_sdcard_folder";
        //自动加载网络弹幕
        public static final String AUTO_LOAD_DANMU = "auto_load_danmu";
        //token
        public static final String TOKEN = "token";
        //用户昵称
        public static final String USER_SCREEN_NAME = "user_screen_name";
        //用户名
        public static final String USER_NAME = "user_name";
        //用户头像
        public static final String USER_IMAGE = "user_image";
        //是否已登陆
        public static final String IS_LOGIN = "is_login";
        //文件夹排序方式
        public static final String FOLDER_COLLECTIONS = "folder_collection";
        //smb地址
        public static final String SMB_DEVICE = "smb_device";
        //补丁版本号
        public static final String PATCH_VERSION = "patch_version";
        //自动查询补丁模式
        public static final String AUTO_QUERY_PATCH = "auto_query_patch";
        //展示MKV提示
        public static final String SHOW_MKV_TIPS = "show_mkv_tips";
    }

    //文件排序方式
    public static class Collection {
        public static final int NAME_ASC = 1;
        public static final int NAME_DESC = 2;
        public static final int DURATION_ASC = 3;
        public static final int DURATION_DESC = 4;
    }

    //播放器设置
    public static final String SHARE_MEDIA_CODE_C = "media_code_c";
    public static final String SHARE_MEDIA_CODE_C_H265 = "media_code_c_h265";
    public static final String SHARE_OPEN_SLES = "open_sles";
    public static final String SHARE_SURFACE_RENDERS = "surface_renders";
    public static final String SHARE_PLAYER_TYPE = "player_type";
    public static final String SHARE_PIXEL_FORMAT = "pixel_format";

    //像素格式(Auto Select=,RGB 565=fcc-rv16,RGB 888X=fcc-rv32,YV12=fcc-yv12,默认为RGB 888X)
    public static final String PIXEL_AUTO = "";
    public static final String PIXEL_RGB565 = "fcc-rv16";
    public static final String PIXEL_RGB888 = "fcc-rv24";
    public static final String PIXEL_RGBX8888 = "fcc-rv32";
    public static final String PIXEL_YV12 = "fcc-yv12";
    public static final String PIXEL_OPENGL_ES2 = "fcc-_es2";

    //下载速度限制
    public static final String TORRENT_DOWNLOAD_SPEED = "torrent_download_speed";
    public static final String TORRENT_UPLOAD_SPEED = "torrent_upload_speed";
}
