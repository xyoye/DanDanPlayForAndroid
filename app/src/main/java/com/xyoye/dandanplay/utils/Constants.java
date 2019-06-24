package com.xyoye.dandanplay.utils;

import android.os.Environment;

/**
 * Created by xyoye on 2018/6/29.
 */

public class Constants {
    public static class Config {
        //首次进入app
        public static final String FIRST_OPEN_APP = "first_open_app";
        //本地下载目录
        public static final String LOCAL_DOWNLOAD_FOLDER = "local_download_folder";
        //自动加载网络弹幕
        public static final String AUTO_LOAD_DANMU = "auto_load_danmu";
        //是否使用网络字幕
        public static final String USE_NETWORK_SUBTITLE = "use_network_subtitle";
        //自动加载同名字幕
        public static final String AUTO_LOAD_LOCAL_SUBTITLE = "auto_load_local_subtitle";
        //自动加载网络字幕
        public static final String AUTO_LOAD_NETWORK_SUBTITLE = "auto_load_network_subtitle";
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
        //补丁版本号
        public static final String PATCH_VERSION = "patch_version";
        //自动查询补丁模式
        public static final String AUTO_QUERY_PATCH = "auto_query_patch";
        //展示MKV提示
        public static final String SHOW_MKV_TIPS = "show_mkv_tips";
        //展示外链视频选择弹幕弹窗
        public static final String SHOW_OUTER_CHAIN_DANMU_DIALOG = "show_outer_chain_danmu_dialog";
        //外链打开时是否选择弹幕
        public static final String OUTER_CHAIN_DANMU_SELECT = "outer_chain_danmu_select";
        //季番排序
        public static final String SEASON_SORT = "season_sort";
        //弹幕云过滤
        public static final String CLOUD_DANMU_FILTER = "cloud_danmu_filter";
        //更新弹幕云过滤的时间
        public static final String UPDATE_FILTER_TIME = "update_filter_time";
        //上次播放的视频
        public static final String LAST_PLAY_VIDEO_PATH = "last_play_video_path";
        //是否已备份过屏蔽数据库
        public static final String IS_BACKUP_BLOCK = "is_backup_block";
        //SMB文件是否为Grid排序
        public static final String SMB_IS_GRID_LAYOUT = "smb_is_grid_layout";
    }

    public static class DefaultConfig{
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
        //备份文件路径
        public static final String backupFolder =  downloadPath + "/_backup";
    }

    public static class PlayerConfig{
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
    }

    public static class SmbType{
        public static final int SQL_DEVICE = 1;
        public static final int LAN_DEVICE = 2;
        public static final int FOLDER = 3;
        public static final int FILE = 4;
    }

    //文件排序方式
    public static class FolderSort {
        public static final int NAME_ASC = 1;
        public static final int NAME_DESC = 2;
        public static final int DURATION_ASC = 3;
        public static final int DURATION_DESC = 4;
    }
}
