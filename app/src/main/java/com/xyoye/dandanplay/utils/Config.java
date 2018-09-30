package com.xyoye.dandanplay.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by YE on 2018/6/29 0029.
 */


public class Config {

    public static List<String> videoType = new ArrayList<>();

    static {
        videoType.add("3GP");
        videoType.add("AVI");
        videoType.add("FLV");
        videoType.add("MKV");
        videoType.add("MP4");
        videoType.add("MPEG");
        videoType.add("RMVB");
        videoType.add("WMV");
        videoType.add("ASX");
        videoType.add("MPG");
        videoType.add("MPE");
        videoType.add("MOV");
        videoType.add("M4V");
        videoType.add("DAT");
        videoType.add("VOB");
        videoType.add("TS");
        videoType.add("XV");
        videoType.add("F4V");
    }

    public static class AppConfig {
        public static final String LOCAL_DANMU_FOLDER = "local_danmu_folder";
        public static final String TOKEN = "token";
        public static final String USER_SCREEN_NAME = "user_screen_name";
        public static final String USER_NAME = "user_name";
        public static final String USER_IMAGE = "user_image";
        public static final String IS_LOGIN = "is_login";
        public static final String FOLDER_COLLECTIONS = "folder_collection";
    }

    public static class Collection {
        public static final int NAME_ASC = 1;
        public static final int NAME_DESC = 2;
        public static final int DURATION_ASC = 3;
        public static final int DURATION_DESC = 4;
    }

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
