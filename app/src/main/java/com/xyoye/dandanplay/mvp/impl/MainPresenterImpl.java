package com.xyoye.dandanplay.mvp.impl;

import android.arch.lifecycle.LifecycleOwner;
import android.os.Bundle;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.AnimeTypeBean;
import com.xyoye.dandanplay.bean.SubGroupBean;
import com.xyoye.dandanplay.mvp.presenter.MainPresenter;
import com.xyoye.dandanplay.mvp.view.MainView;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.DanmuFilterUtils;
import com.xyoye.dandanplay.utils.TrackerManager;
import com.xyoye.dandanplay.utils.database.DataBaseManager;
import com.xyoye.dandanplay.utils.net.CommOtherDataObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

import java.io.File;

/**
 * Created by xyoye on 2018/6/28 0028.
 */

public class MainPresenterImpl extends BaseMvpPresenterImpl<MainView> implements MainPresenter {

    public MainPresenterImpl(MainView view, LifecycleOwner lifecycleOwner) {
        super(view, lifecycleOwner);
    }

    @Override
    public void init() {

    }

    @Override
    public void process(Bundle savedInstanceState) {
        initAnimeType();
        initSubGroup();
        DanmuFilterUtils.getInstance().updateCloudFilter();
        DanmuFilterUtils.getInstance().updateLocalFilter();
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
    public void initScanFolder() {
        DataBaseManager.getInstance()
                .selectTable("scan_folder")
                .query()
                .postExecute(cursor -> {
                    if (!cursor.moveToNext()) {
                        //增加默认扫描文件夹
                        DataBaseManager.getInstance()
                                .selectTable("scan_folder")
                                .insert()
                                .param("folder_path", Constants.DefaultConfig.SYSTEM_VIDEO_PATH)
                                .param("folder_type", Constants.ScanType.SCAN)
                                .postExecute();
                    }
                });
    }

    //Tracker
    @Override
    public void initTracker() {
        IApplication.getExecutor().execute(() -> {
            //trackers数据
            File configFolder = new File(FileUtils.getDirName(Constants.DefaultConfig.configPath));
            if (configFolder.isFile())
                configFolder.delete();
            if (!configFolder.exists())
                configFolder.mkdirs();

            File trackerFile = new File(Constants.DefaultConfig.configPath);

            //文件不存在，读取asset中默认的trackers，并写入文件
            if (!trackerFile.exists()) {
                TrackerManager.resetTracker();
            }
            //文件存在，直接读取
            else {
                TrackerManager.queryTracker();
            }
        });
    }

    //番剧分类
    private void initAnimeType() {
        AnimeTypeBean.getAnimeType(new CommOtherDataObserver<AnimeTypeBean>(getLifecycle()) {
            @Override
            public void onSuccess(AnimeTypeBean animeTypeBean) {
                if (animeTypeBean != null && animeTypeBean.getTypes() != null && animeTypeBean.getTypes().size() > 0) {
                    DataBaseManager.getInstance()
                            .selectTable("anime_type")
                            .delete()
                            .postExecute();
                    DataBaseManager.getInstance()
                            .selectTable("anime_type")
                            .insert()
                            .param("type_id", -1)
                            .param("type_name", "全部")
                            .postExecute();
                    for (AnimeTypeBean.TypesBean typesBean : animeTypeBean.getTypes()) {
                        DataBaseManager.getInstance()
                                .selectTable("anime_type")
                                .insert()
                                .param("type_id", typesBean.getId())
                                .param("type_name", typesBean.getName())
                                .postExecute();
                    }
                }
            }

            @Override
            public void onError(int errorCode, String message) {
                LogUtils.e(message);
                ToastUtils.showShort(message);
            }
        }, new NetworkConsumer());
    }

    //字幕组
    private void initSubGroup() {
        SubGroupBean.getSubGroup(new CommOtherDataObserver<SubGroupBean>(getLifecycle()) {
            @Override
            public void onSuccess(SubGroupBean subGroupBean) {
                if (subGroupBean != null && subGroupBean.getSubgroups() != null && subGroupBean.getSubgroups().size() > 0) {

                    DataBaseManager.getInstance()
                            .selectTable("subgroup")
                            .delete()
                            .postExecute();

                    DataBaseManager.getInstance()
                            .selectTable("subgroup")
                            .insert()
                            .param("subgroup_id", -1)
                            .param("subgroup_name", "全部")
                            .postExecute();

                    for (SubGroupBean.SubgroupsBean subgroupsBean : subGroupBean.getSubgroups()) {
                        DataBaseManager.getInstance()
                                .selectTable("subgroup")
                                .insert()
                                .param("subgroup_id", subgroupsBean.getId())
                                .param("subgroup_name", subgroupsBean.getName())
                                .postExecute();
                    }
                }
            }

            @Override
            public void onError(int errorCode, String message) {
                LogUtils.e(message);
                ToastUtils.showShort(message);
            }
        }, new NetworkConsumer());
    }
}
