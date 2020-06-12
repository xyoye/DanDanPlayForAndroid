package com.xyoye.dandanplay.ui.activities.smb;

import android.arch.lifecycle.LifecycleOwner;
import android.os.Bundle;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.service.SmbService;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.smb.SmbServer;
import com.xyoye.player.commom.utils.CommonPlayerUtils;
import com.xyoye.smb.SmbManager;
import com.xyoye.smb.controller.Controller;
import com.xyoye.smb.info.SmbFileInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
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

    SmbFilePresenterImpl(SmbFileView view, LifecycleOwner lifecycleOwner) {
        super(view, lifecycleOwner);
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
    public void openFile(List<SmbFileInfo> smbFileInfoList, String fileName) {
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

        //是否自动加载同名字幕
        if (AppConfig.getInstance().isAutoLoadLocalSubtitle()) {
            String zimuName = checkZimuExist(smbFileInfoList, fileName);
            if (zimuName != null) {
                loadSmbSubtitlePlay(videoUrl, zimuName);
                return;
            }
        }
        getView().launchPlayerActivity(videoUrl, "");
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
                    LogUtils.e("smbController == null:"+(smbController==null));
                    List<SmbFileInfo> selfList = smbController.getSelfList();
                    LogUtils.e("selfList == null:"+(selfList==null));
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
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private String checkZimuExist(List<SmbFileInfo> smbFileInfoList, String fullVideoName) {
        //获取smb视频文件名：test.mp4 -> test.
        String videoName = FileUtils.getFileNameNoExtension(fullVideoName) + ".";

        for (SmbFileInfo fileInfo : smbFileInfoList) {
            String fileName = fileInfo.getFileName();
            //是否以视频文件名开头：可test.ass，不可test1.ass
            if (fileName.startsWith(videoName)) {
                //是否为可解析字幕：可.ass，不可.1ass
                for (String ext : CommonPlayerUtils.subtitleExtension) {
                    if (fileName.toUpperCase().endsWith("." + ext)) {
                        //是否只包含最多两个点：可test.ass、test.sc.ass，不可test.1.sc.ass
                        int pointCount = fileName.split(".").length - 1;
                        if (pointCount <= 2) {
                            return fileName;
                        }
                    }
                }

            }
        }
        return null;
    }

    private void loadSmbSubtitlePlay(String videoUrl, String subtitleName) {
        Observable.create((ObservableOnSubscribe<String>) emitter -> {
            FileOutputStream outputStream = null;
            InputStream inputStream = null;
            try {
                String folderPath = Constants.DefaultConfig.downloadPath + Constants.DefaultConfig.subtitleFolder;
                File folder = new File(folderPath);
                if (!folder.exists() || !folder.isDirectory()) {
                    folder.mkdirs();
                }
                File subtitleFile = new File(folder, subtitleName);
                if (subtitleFile.exists()) {
                    subtitleFile.delete();
                }
                subtitleFile.createNewFile();

                outputStream = new FileOutputStream(subtitleFile);

                inputStream = SmbManager.getInstance().getController().getFileInputStream(subtitleName);
                long contentLength = SmbManager.getInstance().getController().getFileLength(subtitleName);
                if (inputStream != null) {

                    int bufferSize = 512 * 1024;
                    long readTotalSize = 0;
                    byte[] readBuffer = new byte[bufferSize];
                    long readSize = (bufferSize > contentLength) ? contentLength : bufferSize;
                    int readLen = inputStream.read(readBuffer, 0, (int) readSize);

                    while (readLen > 0 && readTotalSize < contentLength) {
                        outputStream.write(readBuffer, 0, readLen);
                        readTotalSize += readLen;
                        readSize = (bufferSize > (contentLength - readTotalSize))
                                ? (contentLength - readTotalSize)
                                : bufferSize;
                        readLen = inputStream.read(readBuffer, 0, (int) readSize);
                    }
                    outputStream.flush();
                    emitter.onNext(subtitleFile.getAbsolutePath());
                } else {
                    emitter.onNext("");
                }
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                CommonUtils.closeResource(outputStream);
                CommonUtils.closeResource(inputStream);
            }
            emitter.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String subtitleFilePath) {
                        getView().launchPlayerActivity(videoUrl, subtitleFilePath);
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
