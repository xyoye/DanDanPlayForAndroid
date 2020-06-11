package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.ShooterSubDetailBean;
import com.xyoye.dandanplay.bean.ShooterSubtitleBean;
import com.xyoye.dandanplay.mvp.presenter.BindZimuPresenter;
import com.xyoye.dandanplay.mvp.view.BindZimuView;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.HashUtils;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.SubtitleConverter;
import com.xyoye.dandanplay.utils.net.CommOtherDataObserver;
import com.xyoye.dandanplay.utils.net.CommShooterDataObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;
import com.xyoye.dandanplay.utils.net.RetroFactory;
import com.xyoye.dandanplay.utils.net.service.SubtitleRetrofitService;
import com.xyoye.player.commom.bean.SubtitleBean;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xyoye on 2018/7/4 0004.
 */

public class BindZimuPresenterImpl extends BaseMvpPresenterImpl<BindZimuView> implements BindZimuPresenter {

    public BindZimuPresenterImpl(BindZimuView view, Lifeful lifeful) {
        super(view, lifeful);
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

    @Override
    public void matchZimu(String videoPath) {
        String thunderHash = HashUtils.getFileSHA1(videoPath);
        String shooterHash = HashUtils.getFileHash(videoPath);
        if (StringUtils.isEmpty(thunderHash) || StringUtils.isEmpty(shooterHash)) {
            ToastUtils.showShort("无匹配字幕");
            return;
        }

        Map<String, String> shooterParams = new HashMap<>();
        shooterParams.put("filehash", shooterHash);
        shooterParams.put("pathinfo", FileUtils.getFileName(videoPath));
        shooterParams.put("format", "json");
        shooterParams.put("lang", "Chn");
        SubtitleRetrofitService service = RetroFactory.getSubtitleInstance();

        getView().showLoading();
        service.queryThunder(thunderHash)
                .onErrorReturnItem(new SubtitleBean.Thunder())
                .zipWith(service.queryShooter(shooterParams).onErrorReturnItem(new ArrayList<>()),
                        (thunder, shooters) -> SubtitleConverter.transform(thunder, shooters, videoPath))
                .doOnSubscribe(new NetworkConsumer())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommOtherDataObserver<List<SubtitleBean>>() {
                    @Override
                    public void onSuccess(List<SubtitleBean> subtitleList) {
                        getView().hideLoading();
                        if (subtitleList.size() > 0) {
                            //按评分排序
                            Collections.sort(subtitleList, (o1, o2) -> o2.getRank() - o1.getRank());
                            getView().refreshZimuAdapter(subtitleList);
                        } else {
                            getView().showError("未找到相关字幕，请手动搜索");
                        }
                    }

                    @Override
                    public void onError(int errorCode, String message) {
                        getView().hideLoading();
                        ToastUtils.showShort(message);
                    }
                });
    }

    @Override
    public void searchZimu(String videoName, int page) {
        String apiSecret = AppConfig.getInstance().getShooterApiSecret();
        ShooterSubtitleBean.searchSubtitle(apiSecret, videoName, page, new CommShooterDataObserver<ShooterSubtitleBean>() {
            @Override
            public void onSuccess(ShooterSubtitleBean shooterSubtitleBean) {
                getView().hideLoading();
                if (shooterSubtitleBean != null) {
                    if (shooterSubtitleBean.getSub() != null
                            && shooterSubtitleBean.getSub() != null
                            && shooterSubtitleBean.getSub().getSubs() != null
                            && shooterSubtitleBean.getSub().getSubs().size() > 0) {
                        getView().updateSubtitleList(shooterSubtitleBean.getSub().getSubs(), true);
                    } else {
                        getView().updateSubtitleList(new ArrayList<>(), false);
                        if (page == 0) {
                            getView().showError("未找到相关字幕");
                        }
                    }
                } else {
                    getView().showError("解析字幕数据失败");
                    getView().updateSubtitleFailed();
                }
            }

            @Override
            public void onError(int errorCode, String message) {
                getView().hideLoading();
                getView().updateSubtitleFailed();
                getView().showError(errorCode + ": " + message);
            }
        }, new NetworkConsumer());
    }


    @Override
    public void queryZimuDetail(int subtitleId) {
        String apiSecret = AppConfig.getInstance().getShooterApiSecret();
        ShooterSubDetailBean.querySubtitleDetail(apiSecret, subtitleId, new CommShooterDataObserver<ShooterSubDetailBean>() {
            @Override
            public void onSuccess(ShooterSubDetailBean detailBean) {
                getView().hideLoading();
                if (detailBean != null
                        && detailBean.getSub() != null
                        && detailBean.getSub().getSubs() != null
                        && detailBean.getSub().getSubs().size() > 0) {
                    getView().showSubtitleDetailDialog(detailBean.getSub().getSubs().get(0));
                } else {
                    getView().showError("解析字幕详情数据失败");
                }
            }

            @Override
            public void onError(int errorCode, String message) {
                getView().hideLoading();
                getView().showError(errorCode + ": " + message);
            }
        }, new NetworkConsumer());
    }

    @Override
    public void downloadSubtitleFile(String fileName, String downloadLink, boolean unzip) {
        String subtitleFolder = Constants.DefaultConfig.downloadPath + Constants.DefaultConfig.subtitleFolder;
        File subtitleFolderFile = new File(subtitleFolder);
        if (!subtitleFolderFile.exists() || !subtitleFolderFile.isDirectory()) {
            subtitleFolderFile.mkdirs();
        }
        File subtitleFile = new File(subtitleFolder, fileName);
        if (subtitleFile.exists())
            subtitleFile.delete();
        try {
            subtitleFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            getView().showError("下载文件创建失败，请检查剩余内存空间");
            return;
        }

        getView().showLoading();
        ShooterSubDetailBean.downloadSubtitle(downloadLink, subtitleFile.getAbsolutePath(), unzip, new CommShooterDataObserver<String>() {
            @Override
            public void onSuccess(String resultFilePath) {
                getView().hideLoading();
                getView().showError("文件下载成功，请选择需要绑定的字幕");
                getView().subtitleDownloadSuccess(resultFilePath, unzip);
            }

            @Override
            public void onError(int errorCode, String message) {
                getView().hideLoading();
                getView().showError(errorCode + ": " + message);
            }
        }, new NetworkConsumer());
    }
}
