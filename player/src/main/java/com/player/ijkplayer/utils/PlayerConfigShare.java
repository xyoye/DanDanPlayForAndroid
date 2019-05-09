package com.player.ijkplayer.utils;

import android.content.Context;

import com.player.subtitle.SubtitleView;

/**
 * Created by YE on 2018/7/2.
 */

public class PlayerConfigShare {
    private static Context _context;

    public static void initPlayerConfigShare(Context context){
        _context = context;
    }

    private static class ShareHolder{
        private static PlayerConfigShare playerConfigShare = new PlayerConfigShare();
    }

    private PlayerConfigShare(){

    }

    public static PlayerConfigShare getInstance(){
        return ShareHolder.playerConfigShare;
    }

    private SharedPreferencesUtil getShare() {
        return SharedPreferencesUtil.getInstance(_context, Constants.PLAYER_CONFIG);
    }

    /**
     *  ===================弹幕======================
     */
    public int getDanmuSize(){
        String size = getShare().load(Constants.DANMU_SIZE, 50+"");
        return Integer.valueOf(size);
    }

    public void saveDanmuSize(int size){
        getShare().save(Constants.DANMU_SIZE, size+"");
    }

    public int getDanmuSpeed(){
        String speed = getShare().load(Constants.DANMU_SPEED, 50+"");
        return Integer.valueOf(speed);
    }

    public void saveDanmuSpeed(int speed){
        getShare().save(Constants.DANMU_SPEED, speed+"");
    }

    public int getDanmuAlpha(){
        String alpha = getShare().load(Constants.DANMU_ALPHA, 100+"");
        return Integer.valueOf(alpha);
    }

    public void saveDanmuAlpha(int alpha){
        getShare().save(Constants.DANMU_ALPHA, alpha+"");
    }

    public boolean isShowMobileDanmu(){
        return getShare().load(Constants.DANMU_MOBILE, true);
    }

    public void setShowMobileDanmu(boolean isShow){
        getShare().save(Constants.DANMU_MOBILE, isShow);
    }

    public boolean isShowTopDanmu(){
        return getShare().load(Constants.DANMU_TOP, true);
    }

    public void setShowTopDanmu(boolean isShow){
        getShare().save(Constants.DANMU_TOP, isShow);
    }

    public boolean isShowBottomDanmu(){
        return getShare().load(Constants.DANMU_BOTTOM, true);
    }

    public void setShowBottomDanmu(boolean isShow){
        getShare().save(Constants.DANMU_BOTTOM, isShow);
    }

    /**
     *  ===================字幕======================
     */

    public int getSubtitleLanguageType(){
        int type = getShare().loadIntSharedPreference(Constants.SUBTITLE_LANGUAGE);
        return type == 0 ? SubtitleView.LANGUAGE_TYPE_CHINA : type;
    }

    public void setSubtitleLanguageType(int languageType){
        getShare().saveSharedPreferences(Constants.SUBTITLE_LANGUAGE, languageType);
    }

    public void setSubtitleChineseSize(int chineseSize){
        getShare().saveSharedPreferences(Constants.SUBTITLE_CHINESE_SIZE, chineseSize);
    }

    public int getSubtitleChineseSize(){
        int size = getShare().loadIntSharedPreference(Constants.SUBTITLE_CHINESE_SIZE);
        return size == 0 ? 50 : size;
    }

    public void setSubtitleEnglishSize(int englishSize){
        getShare().saveSharedPreferences(Constants.SUBTITLE_ENGLISH_SIZE, englishSize);
    }

    public int getSubtitleEnglishSize(){
        int size = getShare().loadIntSharedPreference(Constants.SUBTITLE_ENGLISH_SIZE);
        return size == 0 ? 50 : size;
    }

    //旋屏
    public boolean isAllowOrientationChange(){
        return getShare().load(Constants.ORIENTATION_CHANGE, true);
    }

    public void setAllowOrientationChange(boolean isAllow){
        getShare().save(Constants.ORIENTATION_CHANGE, isAllow);
    }
}
