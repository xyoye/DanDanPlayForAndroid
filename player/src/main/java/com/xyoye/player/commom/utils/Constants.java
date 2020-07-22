package com.xyoye.player.commom.utils;

/**
 * Created by xyoye on 2018/7/1.
 */
public final class Constants {
    //打开弹幕选择
    public static final int INTENT_OPEN_DANMU = 1000;
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
    //播放失败
    public static final int INTENT_PLAY_FAILED = 1007;
    //播放结束（手动停止）
    public static final int INTENT_PLAY_END = 1008;
    //播放完成（自然结束）
    public static final int INTENT_PLAY_COMPLETE = 1009;


    static final String PLAYER_CONFIG = "player_config";   //播放器配置表

    //播放器
    public static final int EXO_PLAYER = 1;
    public static final int IJK_ANDROID_PLAYER = 2;
    public static final int IJK_PLAYER = 3;

    //弹幕
    static final String DANMU_SIZE = "danmu_size_v2";
    static final String DANMU_ALPHA = "danmu_alpha_v2";
    static final String DANMU_PROJECTION_ALPHA = "danmu_projection_alpha_v2";
    static final String DANMU_SPEED = "danmu_speed_v2";
    static final String DANMU_TOP = "danmu_top";
    static final String DANMU_BOTTOM = "danmu_bottom";
    static final String DANMU_MOBILE = "danmu_mobile";
    static final String DANMU_NUMBER_LIMIT = "danmu_number_limit";
    static final String DANMU_MAX_LINE = "danmu_max_line";

    //字幕
    static final String SUBTITLE_SIZE = "subtitle_size";

    //旋屏
    static final String ORIENTATION_CHANGE = "orientation_change";
}
