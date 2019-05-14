package com.player.commom.utils;

/**
 *
 * Created by xyy on 2018/7/1.
 */
public final class Constants {
    //打开字幕选择
    public static final int INTENT_OPEN_SUBTITLE = 1001;
    //请求网络字幕
    public static final int INTENT_QUERY_SUBTITLE = 1002;
    //选择网络字幕
    public static final int INTENT_SELECT_SUBTITLE = 1003;
    //自动加载网络字幕
    public static final int INTENT_AUTO_SUBTITLE = 1004;
    //保存进度
    public static final int INTENT_SAVE_CURRENT = 1005;

    public static final String PLAYER_CONFIG = "player_config";   //播放器配置表

    //播放器
    public static final int EXO_PLAYER = 1;
    public static final int IJK_ANDROID_PLAYER = 2;
    public static final int IJK_PLAYER = 3;

    //弹幕
    public static final String DANMU_SIZE = "danmu_size_v2";
    public static final String DANMU_ALPHA = "danmu_alpha_v2";
    public static final String DANMU_SPEED = "danmu_speed_v2";
    public static final String DANMU_TOP = "danmu_top";
    public static final String DANMU_BOTTOM = "danmu_bottom";
    public static final String DANMU_MOBILE = "danmu_mobile";

    //字幕
    public static final String SUBTITLE_CHINESE_SIZE = "subtitle_chinese_size_v2";
    public static final String SUBTITLE_ENGLISH_SIZE = "subtitle_english_size_v2";
    public static final String SUBTITLE_LANGUAGE = "subtitle_language";

    //旋屏
    public static final String ORIENTATION_CHANGE = "orientation_change";
}
