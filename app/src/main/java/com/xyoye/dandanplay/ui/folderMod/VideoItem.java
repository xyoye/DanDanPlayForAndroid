package com.xyoye.dandanplay.ui.folderMod;

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
import com.xyoye.dandanplay.utils.ImageLoadTask;
import com.xyoye.dandanplay.utils.TimeUtil;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindString;
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
    @BindString(R.string.danmaku_inexists)
    String danmaku_inexists_string;
    @BindString(R.string.danmaku_exists)
    String danmaku_exists_string;
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
        coverIv.setTag(model.getVideoPath());
        task.execute(model.getVideoPath());

        String videoName = model.getVideoName();
        int last = videoName.lastIndexOf(".");
        videoName = videoName.substring(0, last);
        titleTv.setText(videoName);

        durationTv.setText(TimeUtil.formatDuring(model.getVideoDuration()));

        if (StringUtils.isEmpty(model.getDanmuPath())){
            danmuTipsIv.setImageResource(R.drawable.ic_danmaku_inexist);
            danmuTipsIv.setContentDescription(danmaku_inexists_string);
        }else {
            danmuTipsIv.setImageResource(R.drawable.ic_danmaku_exists);
            danmuTipsIv.setContentDescription(danmaku_exists_string);
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
}
