package com.xyoye.dandanplay.utils;

import android.os.Environment;

/**
 * Created by xyoye on 2018/6/29.
 */

public class Constants {

    /**
     * 用户信息
     */
    static class UserConfig{
        //token
        static final String TOKEN = "token";
        //用户昵称
        static final String USER_SCREEN_NAME = "user_screen_name";
        //用户名
        static final String USER_NAME = "user_name";
        //用户头像
        static final String USER_IMAGE = "user_image";
        //是否已登陆
        static final String IS_LOGIN = "is_login";
    }

    /**
     * 播放器设置
     */
    public static class PlayerConfig{
        //播放器类型
        static final String SHARE_PLAYER_TYPE = "player_type";
        //像素格式
        static final String SHARE_PIXEL_FORMAT = "pixel_format";
        //硬解码
        static final String SHARE_MEDIA_CODE_C = "media_code_c";
        //h265硬解码
        static final String SHARE_MEDIA_CODE_C_H265 = "media_code_c_h265";
        //openSL es
        static final String SHARE_OPEN_SLES = "open_sles";
        //surface view
        static final String SHARE_SURFACE_RENDERS = "surface_renders";
        //自动加载网络弹幕
        static final String AUTO_LOAD_DANMU = "auto_load_danmu";
        //是否使用网络字幕
        static final String USE_NETWORK_SUBTITLE = "use_network_subtitle";
        //自动加载同名字幕
        static final String AUTO_LOAD_LOCAL_SUBTITLE = "auto_load_local_subtitle";
        //自动加载网络字幕
        static final String AUTO_LOAD_NETWORK_SUBTITLE = "auto_load_network_subtitle";
        //展示外链视频选择弹幕弹窗
        static final String SHOW_OUTER_CHAIN_DANMU_DIALOG = "show_outer_chain_danmu_dialog";
        //外链打开时是否选择弹幕
        static final String OUTER_CHAIN_DANMU_SELECT = "outer_chain_danmu_select";

        //像素格式子项
        public static final String PIXEL_AUTO = "";
        public static final String PIXEL_RGB565 = "fcc-rv16";
        public static final String PIXEL_RGB888 = "fcc-rv24";
        public static final String PIXEL_RGBX8888 = "fcc-rv32";
        public static final String PIXEL_YV12 = "fcc-yv12";
        public static final String PIXEL_OPENGL_ES2 = "fcc-_es2";
    }

    /**
     * 系统配置
     */
    public static class Config {
        //首次进入app
        static final String FIRST_OPEN_APP = "first_open_app_1";
        //本地下载目录
        static final String LOCAL_DOWNLOAD_FOLDER = "local_download_folder";
        //文件夹排序方式
        static final String FOLDER_COLLECTIONS = "folder_collection";
        //补丁版本号
        static final String PATCH_VERSION = "patch_version";
        //自动查询补丁模式
        static final String AUTO_QUERY_PATCH = "auto_query_patch";
        //展示MKV提示
        static final String SHOW_MKV_TIPS = "show_mkv_tips";
        //季番排序
        static final String SEASON_SORT = "season_sort";
        //弹幕云过滤
        static final String CLOUD_DANMU_FILTER = "cloud_danmu_filter";
        //更新弹幕云过滤的时间
        static final String UPDATE_FILTER_TIME = "update_filter_time";
        //上次播放的视频
        static final String LAST_PLAY_VIDEO_PATH = "last_play_video_path";
        //SMB文件是否为Grid排序
        static final String SMB_IS_GRID_LAYOUT = "smb_is_grid_layout";
        //远程登录数据
        static final String REMOTE_LOGIN_DATA = "remote_login_data";
    }

    /**
     * 默认配置
     */
    public static class DefaultConfig{
        //系统视频路径
        public static final String SYSTEM_VIDEO_PATH = "系统视频";
        //默认下载文件夹
        public static final String downloadPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/DanDanPlayer";
        //配置路径
        public static final String configPath = downloadPath + "/_config/config.txt";
        //默认种子下载文件夹名
        public static final String torrentFolder = "/_torrent";
        //默认弹幕下载文件夹名
        public static final String danmuFolder = "/_danmu";
        //默认弹幕下载文件夹名
        public static final String subtitleFolder = "/_zimu";
        //默认番剧封面缓存路径
        public static final String imageFolder =  downloadPath + "/_image";
    }

    /**
     * 局域网文件类型
     */
    public static class SmbType{
        public static final int SQL_DEVICE = 1;
        public static final int LAN_DEVICE = 2;
        public static final int FOLDER = 3;
        public static final int FILE = 4;
    }

    /**
     * 文件排序方式
     */
    public static class FolderSort {
        public static final int NAME_ASC = 1;
        public static final int NAME_DESC = 2;
        public static final int DURATION_ASC = 3;
        public static final int DURATION_DESC = 4;
    }

    /**
     * 扫描管理-目录类型
     */
    public static class ScanType {
        //屏蔽目录
        public static final String BLOCK = "0";
        //扫描目录
        public static final String SCAN = "1";
    }
}
