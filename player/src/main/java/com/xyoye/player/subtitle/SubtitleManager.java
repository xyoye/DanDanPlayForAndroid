package com.xyoye.player.subtitle;

import android.support.annotation.NonNull;

import com.blankj.utilcode.util.ConvertUtils;
import com.xyoye.player.subtitle.util.Caption;
import com.xyoye.player.subtitle.util.TimedTextObject;

import java.util.SortedMap;

/**
 * Created by XYJ on 2020/2/14.
 *
 * Ex: External : 外挂字幕
 * Inner： 内置字幕
 */

public class SubtitleManager {
    private boolean showExternalSubtitle = false;
    private int textSize = 40;

    private SubtitleView subtitleView;
    private TimedTextObject subtitleData;

    public SubtitleManager(@NonNull SubtitleView subtitleView) {
        this.subtitleView = subtitleView;
    }

    public void setExSubData(TimedTextObject subtitleData) {
        this.subtitleData = subtitleData;
    }

    public void hideExSub() {
        if (showExternalSubtitle){
            showExternalSubtitle = false;
            clearSubtitle();
        }
    }

    public void showExSub() {
        if (!showExternalSubtitle){
            showExternalSubtitle = true;
            clearSubtitle();
        }
    }

    public void seekExSubTo(long duration) {
        if (!showExternalSubtitle){
            return;
        }
        if (subtitleData != null && !subtitleData.captions.isEmpty()) {
            Caption caption = searchSubtitle(duration);
            if (caption != null) {
                String content = caption.content.replace("<br />", "\n");
                SubtitleUtils.showSubtitle(subtitleView, content, textSize);
            } else {
                clearSubtitle();
            }
        }
    }

    public void setInnerSub(String subtitle){
        if (showExternalSubtitle){
            return;
        }
        SubtitleUtils.showSubtitle(subtitleView, subtitle, textSize);
    }

    public void setTextSizeProgress(int textSizeProgress) {
        this.textSize = (int)(((float)textSizeProgress / 100) * ConvertUtils.dp2px(36));
    }

    public boolean isShowExternalSubtitle(){
        return showExternalSubtitle;
    }

    private void clearSubtitle(){
        SubtitleUtils.showSubtitle(subtitleView, "", textSize);
    }

    private Caption searchSubtitle(long duration) {
        try {
            //最小时间
            int min = subtitleData.captions.firstKey();
            //时间大于最小时间才开始解析
            if (Integer.parseInt(String.valueOf(duration)) > min) {
                //比当前时间小的前一个字幕位置
                int start = subtitleData.captions.lowerKey(Integer.parseInt(String.valueOf(duration)));
                //比当前时间小的前一个到结尾的所有字幕
                SortedMap<Integer, Caption> temp =
                        subtitleData.captions.subMap(start, subtitleData.captions.lastKey());
                for (Integer key1 : temp.keySet()) {
                    Caption caption = temp.get(key1);
                    if (caption == null)
                        return null;
                    //开始时间小于当前时间，结束时间大于当前时间
                    if (duration >= caption.start.getMseconds() && duration <= caption.end.getMseconds()) {
                        return caption;
                    }
                    //减少查找时间，从开始大于当前时间开始break
                    if (caption.start.getMseconds() > duration) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
