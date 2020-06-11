package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.net.okhttp.OkHttpEngine;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by xyoye on 2018/7/14.
 */


public class ZimuDownloadDialog extends Dialog {
    @BindView(R.id.video_title_tv)
    TextView videoTitleTv;
    @BindView(R.id.episode_title_tv)
    TextView episodeTitleTv;
    @BindView(R.id.status_tv)
    TextView statusTv;

    private String zimuName;
    private String downloadUrl;
    private OnZimuDownloadListener listener;

    public ZimuDownloadDialog(@NonNull Context context, String zimuName, String downloadUrl, OnZimuDownloadListener listener) {
        super(context, R.style.Dialog);
        this.zimuName = zimuName;
        this.downloadUrl = downloadUrl;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_download_danmu);
        ButterKnife.bind(this, this);

        videoTitleTv.setText(zimuName);
        episodeTitleTv.setVisibility(View.GONE);
        statusTv.setText("下载中...");

        startDownload();
    }

    private void startDownload() {
        Request request = new Request.Builder().url(downloadUrl).build();
        Call call = OkHttpEngine.getInstance().getOkHttpClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                ToastUtils.showShort("下载字幕文件失败");
                if (ZimuDownloadDialog.this.isShowing() && getOwnerActivity() != null && !getOwnerActivity().isFinishing())
                    ZimuDownloadDialog.this.cancel();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                statusTv.setText("下载完成...");
                if (response.body() == null) {
                    ToastUtils.showShort("保存失败，字幕内容获取失败");
                    return;
                }
                String folderPath = AppConfig.getInstance().getDownloadFolder() + Constants.DefaultConfig.subtitleFolder;
                File folder = new File(folderPath);
                if (!folder.exists()) {
                    if (!folder.mkdirs()) {
                        ToastUtils.showShort("创建字幕文件夹失败");
                        return;
                    }
                }
                String filePath = folderPath + "/" + zimuName;
                boolean isSaveFile = FileIOUtils.writeFileFromIS(filePath, response.body().byteStream());
                if (isSaveFile) {
                    statusTv.setText("保存完成！");
                    listener.onDownloaded(filePath);
                } else {
                    ToastUtils.showShort("保存字幕文件失败");
                }
                if (ZimuDownloadDialog.this.isShowing() && getOwnerActivity() != null && !getOwnerActivity().isFinishing())
                    ZimuDownloadDialog.this.cancel();
            }
        });
    }

    public interface OnZimuDownloadListener {
        void onDownloaded(String zimuPath);
    }
}
