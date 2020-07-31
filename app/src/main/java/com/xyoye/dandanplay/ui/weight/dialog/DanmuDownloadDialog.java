package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.DanmuDownloadBean;
import com.xyoye.dandanplay.bean.DanmuMatchBean;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.DanmuUtils;
import com.xyoye.dandanplay.utils.net.CommOtherDataObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by xyoye on 2018/7/14.
 */


public class DanmuDownloadDialog extends Dialog{
    @BindView(R.id.video_title_tv)
    TextView videoTitleTv;
    @BindView(R.id.episode_title_tv)
    TextView episodeTitleTv;
    @BindView(R.id.status_tv)
    TextView statusTv;

    private DanmuMatchBean.MatchesBean bean;
    private String videoPath;
    private OnDanmuDownloadListener listener;

    public DanmuDownloadDialog(@NonNull Context context, String videoPath, DanmuMatchBean.MatchesBean bean, OnDanmuDownloadListener listener) {
        super(context, R.style.Dialog);
        this.bean = bean;
        this.videoPath = videoPath;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_download_danmu);
        ButterKnife.bind(this, this);

        videoTitleTv.setText(bean.getAnimeTitle());
        episodeTitleTv.setText(bean.getEpisodeTitle());
        statusTv.setText("下载中...");

        startDownload();
    }

    private void startDownload(){
        DanmuDownloadBean.downloadDanmu(bean.getEpisodeId(), new CommOtherDataObserver<DanmuDownloadBean>() {
            @Override
            public void onSuccess(DanmuDownloadBean danmuDownloadBean) {
                statusTv.setText("下载完成...");
                if (danmuDownloadBean == null || danmuDownloadBean.getComments() == null){
                    ToastUtils.showShort("保存失败，弹幕内容获取失败");
                }else {
                    List<DanmuDownloadBean.CommentsBean> comments = danmuDownloadBean.getComments();
                    statusTv.setText("开始保存...");
                    String danmuName = bean.getAnimeTitle()+"_"
                            + bean.getEpisodeTitle().replace(" ","_");
                    if (danmuName.length() > 80) {
                        danmuName = danmuName.substring(0, 80);
                    }
                    danmuName += ".xml";
                    String danmuPath;
                    //如果视频文件在下载路径中，下载弹幕至视频所在文件夹
                    //否则下载弹幕至默认下载文件夹
                    if (FileUtils.getDirName(videoPath).startsWith(AppConfig.getInstance().getDownloadFolder())){
                        String folderPath = FileUtils.getDirName(videoPath);
                        danmuPath = folderPath.substring(0, folderPath.length() - 1)
                                + Constants.DefaultConfig.danmuFolder
                                + "/" + danmuName;
                    }else {
                        danmuPath = AppConfig.getInstance().getDownloadFolder()
                                + Constants.DefaultConfig.danmuFolder
                                + "/" + danmuName;
                    }
                    //去除内容时间一样的弹幕
                    DanmuUtils.saveDanmuSourceFormDanDan(comments, danmuPath);

                    statusTv.setText("保存完成！");
                    ToastUtils.showShort("下载完成："+danmuPath);

                    listener.onDownloaded(danmuPath, bean.getEpisodeId());
                }
                if (DanmuDownloadDialog.this.isShowing())
                    DanmuDownloadDialog.this.cancel();
            }

            @Override
            public void onError(int errorCode, String message) {
                ToastUtils.showShort(message);
                if (DanmuDownloadDialog.this.isShowing() && getOwnerActivity() != null && !getOwnerActivity().isFinishing())
                    DanmuDownloadDialog.this.cancel();
            }
        }, new NetworkConsumer());
    }

    public interface OnDanmuDownloadListener{
        void onDownloaded(String danmuPath, int episodeId);
    }
}
