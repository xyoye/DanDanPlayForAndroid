package com.xyoye.dandanplay.ui.weight.dialog;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.DanmuDownloadBean;
import com.xyoye.dandanplay.bean.DanmuMatchBean;
import com.xyoye.dandanplay.bean.event.OpenDanmuFolderEvent;
import com.xyoye.dandanplay.utils.net.CommOtherDataObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;
import com.xyoye.dandanplay.utils.AppConfigShare;
import com.xyoye.dandanplay.utils.permission.DownloadUtil;
import com.xyoye.dandanplay.utils.permission.PermissionHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by YE on 2018/7/14.
 */


public class DanmuDownloadDialog extends Dialog{
    @BindView(R.id.video_title_tv)
    TextView videoTitleTv;
    @BindView(R.id.episode_title_tv)
    TextView episodeTitleTv;
    @BindView(R.id.status_tv)
    TextView statusTv;

    private DanmuMatchBean.MatchesBean bean;
    private Context context;

    public DanmuDownloadDialog(@NonNull Context context, int themeResId, DanmuMatchBean.MatchesBean bean) {
        super(context, themeResId);
        this.bean = bean;
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_download_danmu);
        ButterKnife.bind(this, this);

        videoTitleTv.setText(bean.getAnimeTitle());
        episodeTitleTv.setText(bean.getEpisodeTitle());
        statusTv.setText("下载中...");

        new PermissionHelper()
                .with((Activity) context)
                .request(this::startDownload,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
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
                    String path = AppConfigShare.getInstance().getDownloadFolder()
                            + "/" + bean.getAnimeTitle()+"_"
                            + bean.getEpisodeTitle().replace(" ","_")
                            + ".xml";
                    //去除内容时间一样的弹幕
                    DownloadUtil.saveDanmu(comments,path);

                    statusTv.setText("保存完成！");
                    ToastUtils.showShort("下载完成："+path);

                    EventBus.getDefault().post(
                            new OpenDanmuFolderEvent(path, bean.getEpisodeId(), false));
                }
                DanmuDownloadDialog.this.cancel();
            }

            @Override
            public void onError(int errorCode, String message) {
                System.out.println(message);
                ToastUtils.showShort(message);
                DanmuDownloadDialog.this.cancel();
            }
        }, new NetworkConsumer());
    }
}
