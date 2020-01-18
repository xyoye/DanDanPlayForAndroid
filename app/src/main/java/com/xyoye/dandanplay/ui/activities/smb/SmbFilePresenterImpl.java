package com.xyoye.dandanplay.ui.activities.smb;

import android.os.Bundle;

import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.service.SmbService;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.smb.SmbServer;
import com.xyoye.libsmb.SmbManager;
import com.xyoye.libsmb.controller.Controller;
import com.xyoye.libsmb.info.SmbFileInfo;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xyoye on 2020/1/3.
 */

public class SmbFilePresenterImpl extends BaseMvpPresenterImpl<SmbFileView> implements SmbFilePresenter {
    private static final int ACTION_REFRESH_SELF = 1001;
    private static final int ACTION_BACK_PARENT = 1002;
    private static final int ACTION_OPEN_CHILD = 1003;

    private Controller smbController;

    SmbFilePresenterImpl(SmbFileView view, Lifeful lifeful) {
        super(view, lifeful);
    }

    @Override
    public void init() {

    }

    @Override
    public void process(Bundle savedInstanceState) {
        smbController = SmbManager.getInstance().getController();
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void refreshSelfDirectory() {
        openDirectory(ACTION_REFRESH_SELF, "");
    }

    @Override
    public void backParentDirectory() {
        if (smbController.isRootDir())
            return;
        openDirectory(ACTION_BACK_PARENT, "");
    }

    @Override
    public void openChildDirectory(String dirName) {
        openDirectory(ACTION_OPEN_CHILD, dirName);

    }

    @Override
    public void openFile(String fileName) {
        if (!CommonUtils.isMediaFile(fileName)) {
            ToastUtils.showShort("不是可播放的视频文件");
            return;
        }
        if (!ServiceUtils.isServiceRunning(SmbService.class)) {
            ToastUtils.showShort("共享服务未启动，无法播放");
            return;
        }

        //文件Url由开启监听的IP和端口及视频地址组成
        String httpUrl = "http://" + SmbServer.SMB_IP + ":" + SmbServer.SMB_PORT;
        String videoUrl = httpUrl + "/smb/" + fileName;
        SmbServer.SMB_FILE_NAME = fileName;

        getView().launchPlayerActivity(videoUrl);
    }

    private void openDirectory(int actionType, String dirName) {
        Observable.create((ObservableOnSubscribe<List<SmbFileInfo>>) emitter -> {
            switch (actionType) {
                case ACTION_BACK_PARENT:
                    emitter.onNext(smbController.getParentList());
                    break;
                case ACTION_OPEN_CHILD:
                    emitter.onNext(smbController.getChildList(dirName));
                    break;
                default:
                    emitter.onNext(smbController.getSelfList());
                    break;
            }
            emitter.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<SmbFileInfo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<SmbFileInfo> smbFileInfoList) {
                        getView().updateFileList(smbFileInfoList);
                        getView().updatePathText(smbController.getCurrentPath());
                        getView().setPreviousEnabled(!smbController.isRootDir());
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
