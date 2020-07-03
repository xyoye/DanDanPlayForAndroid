package com.xyoye.dandanplay.mvp.impl;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LifecycleOwner;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.DanmuDownloadBean;
import com.xyoye.dandanplay.bean.DanmuMatchBean;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.bean.VideoBindAllDanmuBean;
import com.xyoye.dandanplay.bean.params.DanmuMatchParam;
import com.xyoye.dandanplay.mvp.presenter.FolderPresenter;
import com.xyoye.dandanplay.mvp.view.FolderView;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.DanmuUtils;
import com.xyoye.dandanplay.utils.MD5Util;
import com.xyoye.dandanplay.utils.RxUtils;
import com.xyoye.dandanplay.utils.database.DataBaseManager;
import com.xyoye.dandanplay.utils.database.callback.QueryAsyncResultCallback;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;
import com.xyoye.dandanplay.utils.net.RetroFactory;
import com.xyoye.player.commom.utils.CommonPlayerUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xyoye on 2018/6/30 0030.
 */

public class FolderPresenterImpl extends BaseMvpPresenterImpl<FolderView> implements FolderPresenter {

    public FolderPresenterImpl(FolderView view, LifecycleOwner lifecycleOwner) {
        super(view, lifecycleOwner);
    }

    @Override
    public void init() {

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

    @SuppressLint("CheckResult")
    @Override
    public void getVideoList(String folderPath) {
        DataBaseManager.getInstance()
                .selectTable("file")
                .query()
                .where("folder_path", folderPath)
                .postExecute(new QueryAsyncResultCallback<List<VideoBean>>(getLifecycle()) {
                    @Override
                    public List<VideoBean> onQuery(Cursor cursor) {
                        if (cursor == null) return new ArrayList<>();
                        List<VideoBean> videoBeans = new ArrayList<>();
                        while (cursor.moveToNext()) {
                            String filePath = cursor.getString(2);
                            File file = new File(filePath);
                            if (!file.exists()) {
                                DataBaseManager.getInstance()
                                        .selectTable("file")
                                        .delete()
                                        .where("folder_path", folderPath)
                                        .where("file_path", filePath)
                                        .postExecute();
                                continue;
                            }

                            VideoBean videoBean = new VideoBean();
                            videoBean.setVideoPath(filePath);
                            videoBean.setDanmuPath(cursor.getString(3));
                            videoBean.setCurrentPosition(cursor.getInt(4));
                            videoBean.setVideoDuration(Long.parseLong(cursor.getString(5)));
                            videoBean.setEpisodeId(cursor.getInt(6));
                            videoBean.setVideoSize(Long.parseLong(cursor.getString(7)));
                            videoBean.set_id(cursor.getInt(8));
                            videoBean.setZimuPath(cursor.getString(9));
                            videoBeans.add(videoBean);
                        }
                        return videoBeans;
                    }

                    @Override
                    public void onResult(List<VideoBean> result) {
                        getView().refreshAdapter(result);
                        getView().hideLoading();
                    }
                });
    }

    @Override
    public void updateDanmu(String danmuPath, int episodeId, String[] whereArgs) {
        DataBaseManager.getInstance()
                .selectTable("file")
                .update()
                .param("danmu_path", danmuPath)
                .param("danmu_episode_id", episodeId)
                .where("folder_path", whereArgs[0])
                .where("file_path", whereArgs[1])
                .postExecute();
    }

    @Override
    public void updateZimu(String zimuPath, String[] whereArgs) {
        DataBaseManager.getInstance()
                .selectTable("file")
                .update()
                .param("zimu_path", zimuPath)
                .where("folder_path", whereArgs[0])
                .where("file_path", whereArgs[1])
                .postExecute();
    }

    @Override
    public void getDanmu(String videoPath) {
        getView().showLoading();
        String title = FileUtils.getFileName(videoPath);
        DanmuMatchParam param = new DanmuMatchParam();
        String hash = MD5Util.getVideoFileHash(videoPath);
        long length = new File(videoPath).length();
        long duration = MD5Util.getVideoDuration(videoPath);
        param.setFileName(title);
        param.setFileHash(hash);
        param.setFileSize(length);
        param.setVideoDuration(duration);
        param.setMatchMode("hashAndFileName");
        DanmuMatchBean.matchDanmu(param, new CommJsonObserver<DanmuMatchBean>(getLifecycle()) {
            @Override
            public void onSuccess(DanmuMatchBean danmuMatchBean) {
                getView().hideLoading();
                if (danmuMatchBean.getMatches().size() > 0)
                    getView().downloadDanmu(danmuMatchBean.getMatches().get(0));
                else
                    getView().noMatchDanmu(videoPath);
            }

            @Override
            public void onError(int errorCode, String message) {
                getView().hideLoading();
                getView().noMatchDanmu(videoPath);
            }
        }, new NetworkConsumer());
    }

    @Override
    public void bindAllDanmu(List<VideoBean> videoList, String mFolderPath) {
        Observable.just(videoList)
                .map(videoBeans ->
                        //将所有弹幕的获取请求一起发出
                        Observable.zipIterable(getAllDanmuRequest(videoList), resultObjectArray -> {
                            List<VideoBindAllDanmuBean> resultBeanList = new ArrayList<>();
                            for (Object result : resultObjectArray) {
                                if (result instanceof VideoBindAllDanmuBean) {
                                    VideoBindAllDanmuBean resultBean = (VideoBindAllDanmuBean) result;
                                    //排除请求错误时，产生的对象
                                    if (TextUtils.isEmpty(resultBean.getDanmuPath()))
                                        continue;
                                    resultBeanList.add(resultBean);
                                }
                            }
                            return resultBeanList;
                        }, true, 1)
                )
                .flatMap((Function<Observable<List<VideoBindAllDanmuBean>>, ObservableSource<List<VideoBindAllDanmuBean>>>)
                        listObservable -> listObservable)
                //将所有弹幕绑定至数据库中对应的视频文件
                .map(videoBindAllDanmuBeans -> {
                    for (VideoBindAllDanmuBean videoBindAllDanmuBean : videoBindAllDanmuBeans) {
                        DataBaseManager.getInstance().selectTable("file")
                                .update()
                                .param("danmu_path", videoBindAllDanmuBean.getDanmuPath())
                                .where("file_path", videoBindAllDanmuBean.getVideoPath())
                                .postExecute();
                    }
                    return videoBindAllDanmuBeans.size();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .as(RxUtils.bindLifecycle(getLifecycle()))
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        getView().showLoading();
                    }

                    @Override
                    public void onNext(Integer count) {
                        getVideoList(mFolderPath);
                        ToastUtils.showLong("成功，共为"+count+"视频绑定弹幕");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        ToastUtils.showShort("失败: "+throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {
                    }
                });

    }

    @Override
    public void unbindAllDanmu(String folderPath) {
        DataBaseManager.getInstance()
                .selectTable("file")
                .update()
                .param("danmu_path", "")
                .where("folder_path", folderPath)
                .postExecute();

        getVideoList(folderPath);
    }

    @Override
    public void bindAllZimu(List<VideoBean> videoList, String folderPath) {
        int count = 0;
        String zimuDownloadFolder = AppConfig.getInstance().getDownloadFolder()
                + Constants.DefaultConfig.subtitleFolder;
        for (VideoBean videoBean : videoList){
            String zimuPath = CommonPlayerUtils.getSubtitlePath(videoBean.getVideoPath(), zimuDownloadFolder);
            if (!TextUtils.isEmpty(zimuPath)){
                DataBaseManager.getInstance()
                        .selectTable("file")
                        .update()
                        .param("zimu_path", zimuPath)
                        .where("file_path", videoBean.getVideoPath())
                        .postExecute();
                count++;
            }
        }

        getVideoList(folderPath);
        ToastUtils.showLong("成功，共为"+count+"视频绑定字幕");
    }

    @Override
    public void unbindAllZimu(String folderPath) {
        DataBaseManager.getInstance()
                .selectTable("file")
                .update()
                .param("zimu_path", "")
                .where("folder_path", folderPath)
                .postExecute();

        getVideoList(folderPath);
    }

    /**
     * 获取匹配弹幕的参数
     */
    private Map<String, String> getDanmuMatchParam(String videoPath) {
        String title = FileUtils.getFileName(videoPath);
        DanmuMatchParam param = new DanmuMatchParam();
        String hash = MD5Util.getVideoFileHash(videoPath);
        long length = new File(videoPath).length();
        long duration = MD5Util.getVideoDuration(videoPath);
        param.setFileName(title);
        param.setFileHash(hash);
        param.setFileSize(length);
        param.setVideoDuration(duration);
        param.setMatchMode("hashAndFileName");
        return param.getMap();
    }

    /**
     * 获取绑定弹幕的所有流程的观察者列表
     */
    private Set<Observable<VideoBindAllDanmuBean>> getAllDanmuRequest(List<VideoBean> videoBeans) {
        Set<Observable<VideoBindAllDanmuBean>> requestList = new HashSet<>();
        for (VideoBean videoBean : videoBeans) {
            requestList
                    //1.匹配单个视频的弹幕
                    .add(RetroFactory.getInstance().matchDanmu(getDanmuMatchParam(videoBean.getVideoPath()))
                            //2.提取弹幕列表中第一个弹幕信息
                            .flatMap((Function<DanmuMatchBean, ObservableSource<DanmuDownloadBean>>) danmuMatchBean -> {
                                String episodeId = "";
                                if (danmuMatchBean.getMatches() != null
                                        && danmuMatchBean.getMatches().size() > 0
                                        && danmuMatchBean.getMatches().get(0) != null) {
                                    episodeId = danmuMatchBean.getMatches().get(0).getEpisodeId() + "";
                                }
                                //3.下载第一个弹幕
                                return RetroFactory.getInstance().downloadDanmu(episodeId).doOnNext(danmuDownloadBean -> {
                                    if (danmuMatchBean.getMatches() != null
                                            && danmuMatchBean.getMatches().size() > 0
                                            && danmuMatchBean.getMatches().get(0) != null) {
                                        String animeTitle = danmuMatchBean.getMatches().get(0).getAnimeTitle();
                                        String episodeTitle = danmuMatchBean.getMatches().get(0).getEpisodeTitle();
                                        danmuDownloadBean.setAnimeTitle(animeTitle);
                                        danmuDownloadBean.setEpisodeTitle(episodeTitle);
                                    }
                                });
                            })
                            //4.保存弹幕到文件中
                            .map(danmuDownloadBean -> {
                                if (danmuDownloadBean == null || danmuDownloadBean.getComments() == null) {
                                    return new VideoBindAllDanmuBean("", "");
                                } else {
                                    List<DanmuDownloadBean.CommentsBean> comments = danmuDownloadBean.getComments();
                                    String danmuName = danmuDownloadBean.getAnimeTitle() + "_"
                                            + danmuDownloadBean.getEpisodeTitle().replace(" ", "_");
                                    if (danmuName.length() > 80) {
                                        danmuName = danmuName.substring(0, 80);
                                    }
                                    danmuName += ".xml";
                                    String danmuPath;
                                    //如果视频文件在下载路径中，下载弹幕至视频所在文件夹
                                    //否则下载弹幕至默认下载文件夹
                                    if (FileUtils.getDirName(videoBean.getVideoPath()).startsWith(AppConfig.getInstance().getDownloadFolder())) {
                                        String folderPath = FileUtils.getDirName(videoBean.getVideoPath());
                                        danmuPath = folderPath.substring(0, folderPath.length() - 1)
                                                + Constants.DefaultConfig.danmuFolder
                                                + "/" + danmuName;
                                    } else {
                                        danmuPath = AppConfig.getInstance().getDownloadFolder()
                                                + Constants.DefaultConfig.danmuFolder
                                                + "/" + danmuName;
                                    }
                                    //去除内容时间一样的弹幕
                                    DanmuUtils.saveDanmuSourceFormDanDan(comments, danmuPath);
                                    return new VideoBindAllDanmuBean(danmuPath, videoBean.getVideoPath());
                                }
                            })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            //请求错误时不抛出，返回不为NULL的空对象
                            .onErrorReturnItem(new VideoBindAllDanmuBean("", ""))
                    );
        }
        return requestList;
    }
}
