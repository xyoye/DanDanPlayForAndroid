package com.xyoye.dandanplay.bean;

import com.xyoye.dandanplay.utils.net.CommJsonEntity;

import java.io.File;

/**
 * Created by xyoye on 2018/12/20.
 */

public class FileDownloadBean extends CommJsonEntity{
    private File file;

    public FileDownloadBean(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
