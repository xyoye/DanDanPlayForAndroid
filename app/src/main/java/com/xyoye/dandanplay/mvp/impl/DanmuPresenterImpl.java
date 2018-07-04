package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;
import android.os.Environment;

import com.blankj.utilcode.util.FileUtils;
import com.xyoye.core.base.BaseMvpPresenter;
import com.xyoye.core.rx.Lifeful;
import com.xyoye.core.utils.StringUtils;
import com.xyoye.dandanplay.bean.DanmuFolderBean;
import com.xyoye.dandanplay.mvp.presenter.DanmuPresenter;
import com.xyoye.dandanplay.mvp.view.DanmuView;
import com.xyoye.dandanplay.utils.AppConfigShare;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by YE on 2018/7/2.
 */


public class DanmuPresenterImpl extends BaseMvpPresenter<DanmuView> implements DanmuPresenter {
    private File parentFolder = null;
    private String parentPath = "";

    public DanmuPresenterImpl(DanmuView view, Lifeful lifeful) {
        super(view, lifeful);
    }

    @Override
    public void init() {
        parentPath = AppConfigShare.getInstance().getDanmuFolder();
        if (StringUtils.isEmpty(parentPath))
            parentPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        listFile(parentPath);
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
        if (!parentPath.equals(parentFolder.getAbsolutePath()))
            results.add(new DanmuFolderBean(parentFolder, "..." ,true, true));
        if (contents != null) {
            for (File file : contents) {
                DanmuFolderBean info = new DanmuFolderBean();
                if (file.isDirectory()){
                    info.setFolder(true);
                    info.setFile(file);
                    info.setName(file.getName());
                } else{
                    String ext = FileUtils.getFileExtension(file);
                    if ("xml".equals(ext)){
                        info.setFolder(false);
                        info.setFile(file);
                        info.setName(file.getName());
                    }else {
                        continue;
                    }
                }
                results.add(info);
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
