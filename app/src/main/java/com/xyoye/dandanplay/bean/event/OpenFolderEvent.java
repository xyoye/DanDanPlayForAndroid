package com.xyoye.dandanplay.bean.event;

import java.io.Serializable;

/**
 * Created by YE on 2018/6/30 0030.
 */


public class OpenFolderEvent implements Serializable {
    public final static String FOLDERPATH = "FOLDERPATH";

    private String folderPath;

    public OpenFolderEvent(String folderPath) {
        this.folderPath = folderPath;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }
}
