package com.dl7.player.utils;

import android.content.Context;

/**
 * Created by YE on 2018/7/2.
 */

public class DanmuConfigShare {
    private static Context _context;

    public static void initDanmuConfigShare(Context context){
        _context = context;
    }

    private static class ShareHolder{
        private static DanmuConfigShare danmuConfigShare = new DanmuConfigShare();
    }

    private DanmuConfigShare(){

    }

    public static DanmuConfigShare getInstance(){
        return ShareHolder.danmuConfigShare;
    }

    private SharedPreferencesUtil getShare() {
        return SharedPreferencesUtil.getInstance(_context, Constants.DANMU_CONFIG);
    }

    public int getSize(){
        String size = getShare().load(Constants.DANMU_SIZE, 50+"");
        return Integer.valueOf(size);
    }

    public void saveSize(int size){
        getShare().save(Constants.DANMU_SIZE, size+"");
    }

    public float getSpeed(){
        String speed = getShare().load(Constants.DANMU_SPEED, Constants.SPEED_MIDDLE+"");
        return Float.valueOf(speed);
    }

    public void saveSpeed(float speed){
        getShare().save(Constants.DANMU_SPEED, speed+"");
    }

    public boolean isShowMobile(){
        return getShare().load(Constants.DANMU_MOBILE, true);
    }

    public void setShowMobile(boolean isShow){
        getShare().save(Constants.DANMU_MOBILE, isShow);
    }

    public boolean isShowTop(){
        return getShare().load(Constants.DANMU_TOP, true);
    }

    public void setShowTop(boolean isShow){
        getShare().save(Constants.DANMU_TOP, isShow);
    }

    public boolean isShowBottom(){
        return getShare().load(Constants.DANMU_BOTTOM, true);
    }

    public void setShowBottom(boolean isShow){
        getShare().save(Constants.DANMU_BOTTOM, isShow);
    }
}
