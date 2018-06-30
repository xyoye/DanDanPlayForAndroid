package com.xyoye.dandanplay.ui.folderMod;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xyoye.core.interf.AdapterItem;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.VideoBean;

import java.io.File;

import butterknife.BindView;

/**
 * Created by YE on 2018/6/30 0030.
 */


public class VideoItem implements AdapterItem<VideoBean> {
    @BindView(R.id.video_iv)
    ImageView videoIv;
    @BindView(R.id.video_title)
    TextView videoTitle;

    @Override
    public int getLayoutResId() {
        return R.layout.item_video;
    }

    @Override
    public void initItemViews(View itemView) {

    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(VideoBean model, int position) {
        String videoName = model.getVideoName();
        int last = videoName.lastIndexOf(".");
        videoName = videoName.substring(0, last);
        videoTitle.setText(videoName);

        // TODO: 2018/6/30 0030 video图片
    }
}
