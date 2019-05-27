package com.xyoye.dandanplay.bean.params;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xyoye on 2018/10/9.
 */

public class DanmuUploadParam {
    private String time;
    private String mode;
    private String color;
    private String comment;

    public DanmuUploadParam(String time, String mode, String color, String comment) {
        this.time = time;
        this.mode = mode;
        this.color = color;
        this.comment = comment;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Map<String, String> getMap(){
        Map<String, String> map = new HashMap<>();
        map.put("time", this.time);
        map.put("mode", this.mode);
        map.put("color", this.color+"");
        map.put("comment", this.comment);
        return map;
    }
}
