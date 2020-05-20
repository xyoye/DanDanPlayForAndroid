package com.xyoye.player.subtitle;

/**
 * Created by XYJ on 2020/2/13.
 */

public class SubtitleUtils {
    private static boolean isTopSubtitle = false;
    private static boolean isEmptySubtitle = false;

    public static void showSubtitle(SubtitleView subtitleView, String subtitle, int textSize) {
        //字幕分行
        SubtitleView.SubtitleText[] subtitleTexts;
        if (subtitle.contains("\\N")) {
            subtitleTexts = getSubtitleTexts(textSize, subtitle.split("\\N{ACKNOWLEDGE}"));
        } else if (subtitle.contains("\n")) {
            subtitleTexts = getSubtitleTexts(textSize, subtitle.split("\n"));
        } else {
            subtitleTexts = getSubtitleTexts(textSize, subtitle);
        }

        if (isEmptySubtitle){;
            subtitleView.setTopTexts(subtitleTexts);
            subtitleView.setBottomTexts(subtitleTexts);
            isEmptySubtitle = false;
            return;
        }

        if (isTopSubtitle){
            subtitleView.setTopTexts(subtitleTexts);
        } else {
            subtitleView.setBottomTexts(subtitleTexts);
        }
    }

    private static SubtitleView.SubtitleText[] getSubtitleTexts(int textSize, String... subtitles) {
        isEmptySubtitle = false;
        //字幕为空，清除上一次的字幕
        if (subtitles.length == 1 && subtitles[0].length() == 0){
            SubtitleView.SubtitleText[] subtitleTexts = new SubtitleView.SubtitleText[1];
            subtitleTexts[0] = new SubtitleView.SubtitleText();
            isEmptySubtitle = true;
            return subtitleTexts;
        }

        isTopSubtitle = false;
        SubtitleView.SubtitleText[] subtitleTexts = new SubtitleView.SubtitleText[subtitles.length];
        for (int i = 0; i < subtitles.length; i++) {
            String subtitle = subtitles[i].trim();
            SubtitleView.SubtitleText subtitleText = new SubtitleView.SubtitleText();
            subtitleText.setSize(textSize);
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
