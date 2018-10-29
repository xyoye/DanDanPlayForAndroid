package com.xyoye.dandanplay.bean.event;

import java.io.Serializable;

/**
 * Created by YE on 2018/6/30 0030.
 */


public class OpenFolderEvent implements Serializable {
    public final static String FOLDERTITLE = "FOLDERTITLE";
    public final static String FOLDERPATH = "FOLDERPATH";

    private String folderTitle;
    private String folderPath;

    public OpenFolderEvent(String folderTitle, String folderPath) {
        this.folderTitle = folderTitle;
        this.folderPath = folderPath;
    }

    public String getFolderTitle() {
        return folderTitle;
    }

    public void setFolderTitle(String folderTitle) {
        this.folderTitle = folderTitle;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }
}
