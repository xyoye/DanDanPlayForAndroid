package com.xyoye.dandanplay.bean.event;

import java.io.Serializable;

/**
 * Created by xyoye on 2018/6/30.
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
