package com.player.ijkplayer.utils;

import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.player.ijkplayer.R;
import com.player.ijkplayer.media.VideoInfoTrack;

import java.util.List;

/**
 * Created by xyy on 2018/12/19.
 */

public class TrackAdapter extends BaseQuickAdapter<VideoInfoTrack, BaseViewHolder> {

    public TrackAdapter(@LayoutRes int layoutResId, @Nullable List<VideoInfoTrack> data) {
        super(layoutResId, data);
    }

    @Override
    public void convert(BaseViewHolder helper, VideoInfoTrack item) {
        helper.setText(R.id.track_name_tv, item.getName())
                .setChecked(R.id.track_select_cb, item.isSelect())
                .addOnClickListener(R.id.track_ll);
    }
}
