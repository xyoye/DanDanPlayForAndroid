package com.xyoye.player.subtitle.ijk;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by XYJ on 2020/2/13.
 */

public class IJKSubtitleUtils {
    private static boolean isTopSubtitle = false;

    public static void showSubtitle(IJKSubtitleView subtitleView, IMediaPlayer mediaPlayer, String subtitle) {
        //字幕分行
        IJKSubtitleView.SubtitleText[] subtitleTexts;
        if (subtitle.contains("\\N")) {
            subtitleTexts = getSubtitleTexts(subtitle.split("\\N{ACKNOWLEDGE}"));
        } else if (subtitle.contains("\n")) {
            subtitleTexts = getSubtitleTexts(subtitle.split("\n"));
        } else {
            subtitleTexts = getSubtitleTexts(subtitle);
        }

        if (isTopSubtitle){
            subtitleView.setTopTexts(subtitleTexts);
        } else {
            subtitleView.setBottomTexts(subtitleTexts);
        }
    }

    private static IJKSubtitleView.SubtitleText[] getSubtitleTexts(String... subtitles) {
        //字幕为空，清除上一次的字幕
        if (subtitles.length == 1 && subtitles[0].length() == 0){
            IJKSubtitleView.SubtitleText[] subtitleTexts = new IJKSubtitleView.SubtitleText[1];
            subtitleTexts[0] = new IJKSubtitleView.SubtitleText();
            return subtitleTexts;
        }

        isTopSubtitle = false;
        IJKSubtitleView.SubtitleText[] subtitleTexts = new IJKSubtitleView.SubtitleText[subtitles.length];
        for (int i = 0; i < subtitles.length; i++) {
            String subtitle = subtitles[i].trim();
            IJKSubtitleView.SubtitleText subtitleText = new IJKSubtitleView.SubtitleText();
            //第一行以{开头，则认为是特殊字幕，现显示在顶部
            if (subtitle.startsWith("{")){
                if (i == 0){
                    isTopSubtitle = true;
                }
                //忽略{}中内容
                int endIndex = subtitle.lastIndexOf("}") + 1;
                if (endIndex != 0 && endIndex <= subtitle.length()){
                    subtitleText.setText(subtitle.substring(endIndex));
                } else {
                    subtitleText.setText(subtitle);
                }
            } else {
                //普通内容显示在底部
                isTopSubtitle = false;
                subtitleText.setText(subtitle);
            }

            subtitleTexts[i] = subtitleText;
        }
        return subtitleTexts;
    }
}
