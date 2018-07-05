package com.xyoye.dandanplay.utils;

/**
 * Created by YE on 2018/7/1.
 */


public class TimeUtil {

    public static String formatDuring(long mss) {
        int digit = 0;
        long hours = mss / (1000 * 60 * 60);
        long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (mss % (1000 * 60)) / 1000;
        StringBuilder stringBuilder=new StringBuilder();
        if (hours == 0){
            stringBuilder.append("");
        } else{
            if (hours < 10)
                stringBuilder.append("0").append(String.valueOf(hours)).append(":");
            else
                stringBuilder.append(String.valueOf(hours)).append(":");
        }
        if (minutes == 0){
            stringBuilder.append("00:");
        } else{
            if (minutes < 10)
                stringBuilder.append("0").append(String.valueOf(minutes)).append(":");
            else
                stringBuilder.append(String.valueOf(minutes)).append(":");
        }
        if (seconds == 0){
            stringBuilder.append("00");
        } else{
            if (seconds < 10)
                stringBuilder.append("0").append(String.valueOf(seconds));
            else
                stringBuilder.append(String.valueOf(seconds));
        }
        return stringBuilder.toString();
    }
}
