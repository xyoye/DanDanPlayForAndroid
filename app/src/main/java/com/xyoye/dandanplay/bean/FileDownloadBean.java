package com.xyoye.dandanplay.bean;

import com.xyoye.dandanplay.bean.params.DownloadSoParam;
import com.xyoye.dandanplay.utils.net.CommJsonEntity;
import com.xyoye.dandanplay.utils.net.CommOtherDataObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;
import com.xyoye.dandanplay.utils.net.RetroFactory;
import com.xyoye.dandanplay.utils.net.DownloadConverter;

import java.io.File;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xyy on 2018/12/20.
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

    public static void downloadSo(DownloadSoParam param, CommOtherDataObserver<FileDownloadBean> observer, NetworkConsumer consumer){
        RetroFactory.getResInstance()
                .downloadSo(param.getAbi())
                .doOnSubscribe(consumer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new DownloadConverter(observer, param.getFolder(), param.getFileName()))
                .subscribe(observer);
    }
}
