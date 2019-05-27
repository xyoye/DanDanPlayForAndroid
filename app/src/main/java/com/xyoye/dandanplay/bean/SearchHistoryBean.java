package com.xyoye.dandanplay.bean;

/**
 * Created by xyoye on 2019/1/8.
 */

public class SearchHistoryBean {
    private int _id;
    private String text;
    private long time;

    public SearchHistoryBean(int _id, String text, long time) {
        this._id = _id;
        this.text = text;
        this.time = time;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
