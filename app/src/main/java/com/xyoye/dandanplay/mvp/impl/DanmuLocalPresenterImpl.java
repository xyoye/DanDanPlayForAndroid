package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;
import android.os.Environment;

import com.blankj.utilcode.util.FileUtils;
import com.xyoye.core.base.BaseMvpPresenter;
import com.xyoye.core.rx.Lifeful;
import com.xyoye.dandanplay.bean.DanmuFolderBean;
import com.xyoye.dandanplay.mvp.presenter.DanmuLocalPresenter;
import com.xyoye.dandanplay.mvp.view.DanmuLocalView;
import com.xyoye.dandanplay.ui.activities.FileManagerActivity;
import com.xyoye.dandanplay.ui.weight.SDCardUtil;
import com.xyoye.dandanplay.utils.AppConfigShare;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by YE on 2018/7/2.
 */


public class DanmuLocalPresenterImpl extends BaseMvpPresenter<DanmuLocalView> implements DanmuLocalPresenter {
    private File parentFolder = null;
    private String rootPath = Environment.getExternalStorageDirectory().getPath();
    private int fileType;

    public DanmuLocalPresenterImpl(DanmuLocalView view, Lifeful lifeful) {
        super(view, lifeful);
    }

    @Override
    public void init() {
        fileType = getView().getFileType();
        String path = AppConfigShare.getInstance().getDownloadFolder();
        listFile(path);
    }

    @Override
    public void process(Bundle savedInstanceState) {

    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void listFile(String path){
        getView().updatePathTitle(path);
        getView().showLoading();
        parentFolder = new File(path);
        File[] contents = parentFolder.listFiles();
        List<DanmuFolderBean> results = new ArrayList<>();
        if (!rootPath.equals(parentFolder.getAbsolutePath()))
            results.add(new DanmuFolderBean(parentFolder, ".." ,true, true));
        if (contents != null) {
            for (File file : contents) {
                DanmuFolderBean info = new DanmuFolderBean();
                if (file.isDirectory()){
                    info.setFolder(true);
                    info.setFile(file);
                    info.setName(file.getName());
                    results.add(info);
                } else if (fileType == FileManagerActivity.FILE_DANMU){
                    String ext = FileUtils.getFileExtension(file);
                    if ("xml".equals(ext)){
                        info.setFolder(false);
                        info.setFile(file);
                        info.setName(file.getName());
                        results.add(info);
                    }
                }else if (fileType == FileManagerActivity.FILE_SUBTITLE){
                    String ext = FileUtils.getFileExtension(file);
                    switch (ext.toUpperCase()){
                        case "ASS":
                        case "SCC":
                        case "STL":
                        case "SRT":
                        case "TTML":
                            info.setFolder(false);
                            info.setFile(file);
                            info.setName(file.getName());
                            results.add(info);
                            break;
                    }
                }
            }
            Collections.sort(results, new FolderSorter());
        }
        getView().refreshAdapter(results);
        getView().hideLoading();
    }

    /**
     * 文件夹排序
     */
    private static class FolderSorter implements Comparator<DanmuFolderBean> {
        @Override
        public int compare(DanmuFolderBean lhs, DanmuFolderBean rhs) {
            return lhs.getName().compareTo(rhs.getName());
        }
    }
}
