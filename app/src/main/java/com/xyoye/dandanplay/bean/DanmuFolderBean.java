package com.xyoye.dandanplay.bean;

import java.io.File;
import java.io.Serializable;

/**
 * Created by YE on 2018/7/2.
 */


public class DanmuFolderBean implements Serializable{
    private File file;
    private String name;
    private boolean isFolder;
    private boolean isParent = false;

    public DanmuFolderBean() {
    }

    public DanmuFolderBean(File file, String name, boolean isFolder, boolean isParent) {
        this.file = file;
        this.name = name;
        this.isFolder = isFolder;
        this.isParent = isParent;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFolder(boolean folder) {
        isFolder = folder;
    }

    public boolean isParent() {
        return isParent;
    }

    public void setParent(boolean parent) {
        isParent = parent;
    }
}
