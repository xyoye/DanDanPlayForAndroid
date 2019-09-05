package com.xyoye.player.subtitle.util;

import android.widget.TextView;

/**
 * 字幕控制接口
 */

public interface ISubtitleControl
{
    /**
     * 设置数据
     *
     * @param model
     */
    void setData(TimedTextObject model);

    /**
     * 设置字幕
     *
     * @param view
     * @param item
     */
    void setItemSubtitle(TextView view, String item);

    /**
     * 设置显示的语言
     *
     * @param type
     */
    void setLanguage(int type);

    /**
     * 开始
     */
    void start();

    /**
     * 暂停
     */
    void pause();

    /**
     * 定位设置字幕，单位毫秒
     *
     * @param position
     */
    void seekTo(long position);

    /**
     * 停止
     */
    void stop();

    /**
     * 后台播放
     * 
     * @param pb
     */
    void setPlayOnBackground(boolean pb);

    /**
     * 字体大小（中文）
     */
    void setTextSize(int languageType, float chineseSize);

    /**
     * 字体大小（中英文）
     */
    void setTextSize(float chineseSize, float englishSize);

    /**
     * 隐藏字幕
     */
    void hide();

    /**
     * 展示字幕
     */
    void show();
}
