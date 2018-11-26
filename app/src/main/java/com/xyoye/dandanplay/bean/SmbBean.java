package com.xyoye.dandanplay.bean;

/**
 * Created by xyy on 2018/11/21.
 */

public class SmbBean {
    private String name;
    private String url;

    public SmbBean() {
    }

    public SmbBean(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
