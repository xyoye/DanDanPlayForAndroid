package com.xyoye.dandanplay.mvp.impl;

import android.arch.lifecycle.LifecycleOwner;
import android.os.Bundle;

import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.ShooterQuotaBean;
import com.xyoye.dandanplay.bean.ShooterSubDetailBean;
import com.xyoye.dandanplay.bean.ShooterSubtitleBean;
import com.xyoye.dandanplay.mvp.presenter.ShooterSubPresenter;
import com.xyoye.dandanplay.mvp.view.ShooterSubView;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.net.CommShooterDataObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by xyoye on 2020/2/23.
 */

public class ShooterSubPresenterImpl extends BaseMvpPresenterImpl<ShooterSubView> implements ShooterSubPresenter {

    public ShooterSubPresenterImpl(ShooterSubView view, LifecycleOwner lifecycleOwner) {
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
    public void updateQuota() {
        String apiSecret = AppConfig.getInstance().getShooterApiSecret();
        ShooterQuotaBean.getShooterQuota(apiSecret, new CommShooterDataObserver<ShooterQuotaBean>() {
            @Override
            public void onSuccess(ShooterQuotaBean shooterQuotaBean) {
                if (shooterQuotaBean != null && shooterQuotaBean.getUser() != null) {
                    getView().updateQuota(shooterQuotaBean.getUser().getQuota());
                } else {
                    getView().showError("解析配额数据失败");
                }
            }

            @Override
            public void onError(int errorCode, String message) {
                getView().showError(errorCode + ": " + message);
            }
        }, new NetworkConsumer());
    }

    @Override
    public void searchSubtitle(String text, int page) {
        String apiSecret = AppConfig.getInstance().getShooterApiSecret();
        ShooterSubtitleBean.searchSubtitle(apiSecret, text, page, new CommShooterDataObserver<ShooterSubtitleBean>() {
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
    public void querySubtitleDetail(int subtitleId) {
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
                String msg;
                if (unzip) {
                    msg = "文件下载并解压成功: " + resultFilePath;
                } else {
                    msg = "文件下载成功: " + resultFilePath;
                }
                getView().hideLoading();
                getView().showError(msg);
                getView().subtitleDownloadSuccess();
            }

            @Override
            public void onError(int errorCode, String message) {
                getView().hideLoading();
                getView().showError(errorCode + ": " + message);
            }
        }, new NetworkConsumer());
    }
}
