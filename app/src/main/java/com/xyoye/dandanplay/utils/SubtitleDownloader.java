package com.xyoye.dandanplay.utils;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.bean.event.UpdateFragmentEvent;
import com.xyoye.dandanplay.ui.fragment.PlayFragment;
import com.xyoye.dandanplay.utils.database.DataBaseManager;
import com.xyoye.dandanplay.utils.net.okhttp.OkHttpEngine;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by xyoye on 2020/6/17.
 */

public class SubtitleDownloader {
    private String downloadLink;
    private String videoPath;
    private String fileName;

    public SubtitleDownloader(String downloadLink, String videoPath, String fileName) {
        this.downloadLink = downloadLink;
        this.videoPath = videoPath;
        this.fileName = fileName;
    }

    public void start(SubtitleDownloadListener listener){
        Request request = new Request.Builder().url(downloadLink).build();
        Call call = OkHttpEngine.getInstance().getOkHttpClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                ToastUtils.showShort("下载字幕文件失败");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.body() != null) {
                    String folderPath = AppConfig.getInstance().getDownloadFolder() + Constants.DefaultConfig.subtitleFolder;
                    File folder = new File(folderPath);
                    if (!folder.exists()) {
                        if (!folder.mkdirs()) {
                            ToastUtils.showShort("创建字幕文件夹失败");
                            return;
                        }
                    }
                    String filePath = folderPath + "/" + fileName;
                    boolean isSaveFile = FileIOUtils.writeFileFromIS(filePath, response.body().byteStream());
                    if (isSaveFile) {
                        DataBaseManager.getInstance()
                                .selectTable("file")
                                .update()
                                .param("zimu_path", filePath)
                                .where("file_path", videoPath)
                                .postExecute();
                        EventBus.getDefault().post(UpdateFragmentEvent.updatePlay(PlayFragment.UPDATE_DATABASE_DATA));
                        IApplication.getMainHandler().post(() -> {
                            listener.onSuccess(filePath);
                        });
                    } else {
                        ToastUtils.showShort("保存字幕文件失败");
                    }
                }
            }
        });
    }

    public interface SubtitleDownloadListener{
        void onSuccess(String filePath);
    }
}
