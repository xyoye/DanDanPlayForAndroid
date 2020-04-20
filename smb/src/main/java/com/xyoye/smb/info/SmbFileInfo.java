package com.xyoye.smb.info;

/**
 * Created by xyoye on 2019/12/22.
 */

public class SmbFileInfo {
    private String fileName;
    private boolean isDirectory;

    public SmbFileInfo(String fileName, boolean isDirectory) {
        this.fileName = fileName;
        this.isDirectory = isDirectory;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }
}
