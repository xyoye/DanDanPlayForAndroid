package com.xyoye.dandanplay.ui.folderMod;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xyoye.core.interf.AdapterItem;
import com.xyoye.core.utils.StringUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.event.OpenDanmuSettingEvent;
import com.xyoye.dandanplay.event.OpenVideoEvent;
import com.xyoye.dandanplay.utils.BitmapUtil;
import com.xyoye.dandanplay.utils.ImageLoadTask;
import com.xyoye.dandanplay.utils.TimeUtil;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Created by YE on 2018/6/30 0030.
 */


public class VideoItem implements AdapterItem<VideoBean> {
    @BindView(R.id.cover_iv)
    ImageView coverIv;
    @BindView(R.id.duration_tv)
    TextView durationTv;
    @BindView(R.id.title_tv)
    TextView titleTv;
    @BindView(R.id.danmu_tips_iv)
    ImageView danmuTipsIv;
    @BindView(R.id.danmu_setting_rl)
    RelativeLayout danmuSetting;

    private View mView;

    @Override
    public int getLayoutResId() {
        return R.layout.item_video;
    }

    @Override
    public void initItemViews(View itemView) {
        mView = itemView;
    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(final VideoBean model, final int position) {
        ImageLoadTask task = new ImageLoadTask(coverIv);
        task.execute(model.getVideoPath() + model.getVideoName());

        String videoName = model.getVideoName();
        int last = videoName.lastIndexOf(".");
        videoName = videoName.substring(0, last);
        titleTv.setText(videoName);

        durationTv.setText(TimeUtil.formatDuring(model.getVideoDuration()));

        if (StringUtils.isEmpty(model.getDanmuPath())){
            danmuTipsIv.setImageResource(R.mipmap.ic_danmu_inexist);
        }else {
            danmuTipsIv.setImageResource(R.mipmap.ic_danmu_exist);
        }

        danmuSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenDanmuSettingEvent event = new OpenDanmuSettingEvent(model.getVideoPath(), position);
                EventBus.getDefault().post(event);
            }
        });

        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenVideoEvent event = new OpenVideoEvent(model, position);
                EventBus.getDefault().post(event);
            }
        });
    }


    private VideoBean getVideoInfoMore(VideoBean videoBean){
        FFmpegMediaMetadataRetriever fmmr = null;
        String videoCover = "";
        try {
            fmmr = new FFmpegMediaMetadataRetriever();
            fmmr.setDataSource(videoBean.getVideoPath());
            Bitmap bitmap = fmmr.getFrameAtTime();
            videoCover = BitmapUtil.bitmapToBase64(bitmap);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }finally {
            if (fmmr != null){
                fmmr.release();
            }
        }
        videoBean.setVideoCover(videoCover);
        return videoBean;
    }
}
