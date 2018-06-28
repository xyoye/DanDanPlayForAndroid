package com.dl7.player.danmaku;

/**
 * Created by long on 2016/12/22.
 * 弹幕数据基类，用来限定弹幕类型
 */
public abstract class BaseDanmakuData {

    public
    @DanmakuType
    int type;
    public String content;
    public long time;
    public float textSize;
    public int textColor;

    @Override
    public String toString() {
        return "BaseDanmakuData{" +
                "type=" + type +
                ", content='" + content + '\'' +
                ", time=" + time +
                ", textSize=" + textSize +
                ", textColor=" + textColor +
                '}';
    }
}
