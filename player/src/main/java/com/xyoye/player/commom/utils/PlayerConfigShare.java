package com.xyoye.player.commom.utils;

import android.content.Context;

/**
 * Created by xyoye on 2018/7/2.
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

    public int getDanmuNumberLimit(){
        return getShare().loadIntSharedPreference(Constants.DANMU_NUMBER_LIMIT);
    }

    public void setDanmuNumberLimit(int num){
        getShare().saveSharedPreferences(Constants.DANMU_NUMBER_LIMIT, num);
    }

    public int getDanmuMaxLine(){
        return getShare().loadIntSharedPreference(Constants.DANMU_MAX_LINE, -1);
    }

    public void setDanmuMaxLine(int num){
        getShare().saveSharedPreferences(Constants.DANMU_MAX_LINE, num);
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

    public void setSubtitleTextSize(int textSize){
        getShare().saveSharedPreferences(Constants.SUBTITLE_SIZE, textSize);
    }

    public int getSubtitleTextSize(){
        int size = getShare().loadIntSharedPreference(Constants.SUBTITLE_SIZE);
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
