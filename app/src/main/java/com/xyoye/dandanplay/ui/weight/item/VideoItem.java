package com.xyoye.dandanplay.ui.weight.item;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.FileUtils;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.core.utils.StringUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.bean.event.OpenDanmuSettingEvent;
import com.xyoye.dandanplay.bean.event.OpenVideoEvent;
import com.xyoye.dandanplay.bean.event.VideoActionEvent;
import com.xyoye.dandanplay.utils.ImageLoadTask;
import com.xyoye.dandanplay.utils.TimeUtil;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;

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
    @BindView(R.id.video_info_rl)
    RelativeLayout videoInfoRl;
    @BindView(R.id.delete_action_ll)
    LinearLayout deleteActionLl;
    @BindView(R.id.bind_danmu_iv)
    ImageView bindDanmuIv;
    @BindView(R.id.bind_danmu_tv)
    TextView bindDanmuTv;
    @BindView(R.id.unbind_danmu_action_ll)
    LinearLayout unbindDanmuActionLl;
    @BindView(R.id.video_action_ll)
    LinearLayout videoActionLl;
    @BindView(R.id.close_action_ll)
    LinearLayout closeActionLl;
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
        if (!model.isNotCover()){
            ImageLoadTask task = new ImageLoadTask(coverIv);
            coverIv.setScaleType(ImageView.ScaleType.FIT_XY);
            coverIv.setTag(model.getVideoPath());
            task.execute(model.getVideoPath());
        }else {
            coverIv.setScaleType(ImageView.ScaleType.CENTER);
            coverIv.setImageResource(R.mipmap.ic_smb_video);
        }

        if (!StringUtils.isEmpty(model.getDanmuPath())){
            bindDanmuIv.setImageResource(R.mipmap.ic_download_bind_danmu);
            bindDanmuTv.setTextColor(mView.getContext().getResources().getColor(R.color.white));
            unbindDanmuActionLl.setEnabled(true);
        }else{
            bindDanmuIv.setImageResource(R.mipmap.id_cant_unbind_danmu);
            bindDanmuTv.setTextColor(mView.getContext().getResources().getColor(R.color.gray_color4));
            unbindDanmuActionLl.setEnabled(false);
        }

        titleTv.setText(FileUtils.getFileNameNoExtension(model.getVideoPath()));

        durationTv.setText(TimeUtil.formatDuring(model.getVideoDuration()));
        if (model.getVideoDuration()  == 0) durationTv.setVisibility(View.GONE);

        if (StringUtils.isEmpty(model.getDanmuPath())) {
            danmuTipsIv.setImageResource(R.drawable.ic_danmaku_inexist);
        } else {
            danmuTipsIv.setImageResource(R.drawable.ic_danmaku_exists);
        }

        danmuSetting.setOnClickListener(v -> {
            OpenDanmuSettingEvent event = new OpenDanmuSettingEvent(model.getVideoPath(), position);
            EventBus.getDefault().post(event);
        });

        videoInfoRl.setOnClickListener(view -> {
            OpenVideoEvent event = new OpenVideoEvent(model, position);
            EventBus.getDefault().post(event);
        });

        videoInfoRl.setOnLongClickListener(v -> {
            videoActionLl.setVisibility(View.VISIBLE);
            return true;
        });

        closeActionLl.setOnClickListener(v -> videoActionLl.setVisibility(View.GONE));

        unbindDanmuActionLl.setOnClickListener(v -> {
            EventBus.getDefault().post(new VideoActionEvent(VideoActionEvent.UN_BIND, position));
            videoActionLl.setVisibility(View.GONE);
        });


        deleteActionLl.setOnClickListener(v -> {
            EventBus.getDefault().post(new VideoActionEvent(VideoActionEvent.DELETE, position));
            videoActionLl.setVisibility(View.GONE);
        });
    }
}
