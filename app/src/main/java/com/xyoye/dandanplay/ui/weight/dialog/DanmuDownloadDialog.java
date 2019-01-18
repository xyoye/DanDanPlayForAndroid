package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.DanmuDownloadBean;
import com.xyoye.dandanplay.bean.DanmuMatchBean;
import com.xyoye.dandanplay.bean.event.OpenDanmuFolderEvent;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.net.CommOtherDataObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

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
    private String videoPath;

    public DanmuDownloadDialog(@NonNull Context context, int themeResId, String videoPath, DanmuMatchBean.MatchesBean bean) {
        super(context, themeResId);
        this.bean = bean;
        this.context = context;
        this.videoPath = videoPath;
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
                            + bean.getEpisodeTitle().replace(" ","_")
                            + ".xml";
                    String danmuPath;
                    //如果视频文件在下载路径中，下载弹幕至视频所在文件夹
                    //否则下载弹幕至默认下载文件夹
                    if (FileUtils.getDirName(videoPath).startsWith(AppConfig.getInstance().getDownloadFolder())){
                        danmuPath = FileUtils.getDirName(videoPath)
                                + "/_danmu"
                                + "/" + danmuName;
                    }else {
                        danmuPath = AppConfig.getInstance().getDownloadFolder()
                                + "/_danmu"
                                + "/" + danmuName;
                    }
                    //去除内容时间一样的弹幕
                    CommonUtils.saveDanmu(comments, danmuPath);

                    statusTv.setText("保存完成！");
                    ToastUtils.showShort("下载完成："+danmuPath);

                    EventBus.getDefault().post(
                            new OpenDanmuFolderEvent(danmuPath, bean.getEpisodeId(), false));
                }
                if (DanmuDownloadDialog.this.isShowing())
                    DanmuDownloadDialog.this.cancel();
            }

            @Override
            public void onError(int errorCode, String message) {
                System.out.println(message);
                ToastUtils.showShort(message);
                if (DanmuDownloadDialog.this.isShowing() && context!=null && getOwnerActivity() != null)
                    DanmuDownloadDialog.this.cancel();
            }
        }, new NetworkConsumer());
    }
}
