package com.xyoye.dandanplay.bean;

import java.io.File;
import java.io.Serializable;

/**
 * Created by xyoye on 2018/7/2.
 */

public class FileManagerBean implements Serializable{
    private File file;
    private String name;
    private boolean isFolder;
    private boolean hasParent;

    public FileManagerBean() {
    }

    public FileManagerBean(File file, String name, boolean isFolder, boolean hasParent) {
        this.file = file;
        this.name = name;
        this.isFolder = isFolder;
        this.hasParent = hasParent;
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

    public boolean hasParent() {
        return hasParent;
    }

    public void setParent(boolean has) {
        hasParent = has;
    }
}
