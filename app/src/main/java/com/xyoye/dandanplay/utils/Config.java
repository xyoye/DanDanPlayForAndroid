package com.xyoye.dandanplay.utils;

import java.util.ArrayList;
import java.util.List;

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
    }
}
