package com.xyoye.player.commom.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.xyoye.player.commom.adapter.StreamAdapter;
import com.xyoye.player.commom.utils.CommonPlayerUtils;
import com.xyoye.player.R;
import com.xyoye.player.ijkplayer.media.IRenderView;
import com.xyoye.player.ijkplayer.media.VideoInfoTrack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xyy on 2019/2/25.
 */

public class SettingPlayerView extends LinearLayout implements View.OnClickListener {
    private LinearLayout speedCtrlLL;
    private TextView speed50Tv, speed75Tv, speed100Tv, speed125Tv, speed150Tv, speed200Tv;

    private RecyclerView audioRv;
    private RecyclerView subtitleRv;
    private Switch orientationChangeSw;
    private LinearLayout audioRl;
    private LinearLayout subtitleRl;
    private StreamAdapter audioStreamAdapter;
    private StreamAdapter subtitleStreamAdapter;
    private RadioGroup mAspectRatioOptions;

    private boolean isExoPlayer = false;
    //是否允许屏幕翻转
    private boolean isAllowScreenOrientation = true;

    private List<VideoInfoTrack> audioTrackList = new ArrayList<>();
    private List<VideoInfoTrack> subtitleTrackList = new ArrayList<>();
    private SettingVideoListener listener;

    public SettingPlayerView(Context context) {
        this(context, null);
    }

    @SuppressLint("ClickableViewAccessibility")
    public SettingPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.view_setting_video, this);

        speedCtrlLL = this.findViewById(R.id.speed_ctrl_ll);
        speed50Tv = this.findViewById(R.id.speed50_tv);
        speed75Tv = this.findViewById(R.id.speed75_tv);
        speed100Tv = this.findViewById(R.id.speed100_tv);
        speed125Tv = this.findViewById(R.id.speed125_tv);
        speed150Tv = this.findViewById(R.id.speed150_tv);
        speed200Tv = this.findViewById(R.id.speed200_tv);
        audioRv = this.findViewById(R.id.audio_track_rv);
        subtitleRv = this.findViewById(R.id.subtitle_track_rv);
        audioRl = this.findViewById(R.id.audio_track_ll);
        subtitleRl = this.findViewById(R.id.subtitle_track_ll);
        mAspectRatioOptions = this.findViewById(R.id.aspect_ratio_group);
        orientationChangeSw = this.findViewById(R.id.orientation_change_sw);

        mAspectRatioOptions.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.aspect_fit_parent) {
                listener.setAspectRatio(IRenderView.AR_ASPECT_FIT_PARENT);
            } else if (checkedId == R.id.aspect_fit_screen) {
                listener.setAspectRatio(IRenderView.AR_ASPECT_FILL_PARENT);
            } else if (checkedId == R.id.aspect_16_and_9) {
                listener.setAspectRatio(IRenderView.AR_16_9_FIT_PARENT);
            } else if (checkedId == R.id.aspect_4_and_3) {
                listener.setAspectRatio(IRenderView.AR_4_3_FIT_PARENT);
            }
        });

        speed50Tv.setOnClickListener(this);
        speed75Tv.setOnClickListener(this);
        speed100Tv.setOnClickListener(this);
        speed125Tv.setOnClickListener(this);
        speed150Tv.setOnClickListener(this);
        speed200Tv.setOnClickListener(this);

        if (audioTrackList == null || audioTrackList.size() <= 0) {
            audioTrackList = new ArrayList<>();
            audioRl.setVisibility(GONE);
        }
        if (subtitleTrackList == null || subtitleTrackList.size() <= 0) {
            subtitleTrackList = new ArrayList<>();
            subtitleRl.setVisibility(GONE);
        }

        audioStreamAdapter = new StreamAdapter(R.layout.item_video_track, audioTrackList);
        audioRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        audioRv.setItemViewCacheSize(10);
        audioRv.setAdapter(audioStreamAdapter);

        subtitleStreamAdapter = new StreamAdapter(R.layout.item_video_track, subtitleTrackList);
        subtitleRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        subtitleRv.setItemViewCacheSize(10);
        subtitleRv.setAdapter(subtitleStreamAdapter);

        audioStreamAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (isExoPlayer) {
                for (int i = 0; i < audioTrackList.size(); i++) {
                    if (i == position)
                        audioTrackList.get(i).setSelect(true);
                    else
                        audioTrackList.get(i).setSelect(false);
                }
                listener.selectTrack(-1, audioTrackList.get(position).getLanguage(), true);
            } else {
                //deselectAll except position
                for (int i = 0; i < audioTrackList.size(); i++) {
                    if (i == position) continue;
                    listener.deselectTrack(audioTrackList.get(i).getStream(), audioTrackList.get(i).getLanguage(), true);
                    audioTrackList.get(i).setSelect(false);
                }
                //select or deselect position
                if (audioTrackList.get(position).isSelect()) {
                    listener.deselectTrack(audioTrackList.get(position).getStream(), audioTrackList.get(position).getLanguage(), true);
                    audioTrackList.get(position).setSelect(false);
                } else {
                    listener.selectTrack(audioTrackList.get(position).getStream(), audioTrackList.get(position).getLanguage(), true);
                    audioTrackList.get(position).setSelect(true);
                }
            }
            audioStreamAdapter.notifyDataSetChanged();
        });

        subtitleStreamAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            //ijk播放器暂不提供字幕流管理
            if (isExoPlayer) {
                for (int i = 0; i < subtitleTrackList.size(); i++) {
                    if (i == position)
                        subtitleTrackList.get(i).setSelect(true);
                    else
                        subtitleTrackList.get(i).setSelect(false);
                }
                listener.selectTrack(-1, subtitleTrackList.get(position).getLanguage(), false);
                subtitleStreamAdapter.notifyDataSetChanged();
            }
        });

        this.setOnTouchListener((v, event) -> true);

        orientationChangeSw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isAllowScreenOrientation = isChecked;
            listener.setOrientationStatus(isChecked);
        });

        setPlayerSpeedView(3);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.speed50_tv) {
            listener.setSpeed(0.5f);
            setPlayerSpeedView(1);
        } else if (id == R.id.speed75_tv) {
            listener.setSpeed(0.75f);
            setPlayerSpeedView(2);
        } else if (id == R.id.speed100_tv) {
            listener.setSpeed(1.0f);
            setPlayerSpeedView(3);
        } else if (id == R.id.speed125_tv) {
            listener.setSpeed(1.25f);
            setPlayerSpeedView(4);
        } else if (id == R.id.speed150_tv) {
            listener.setSpeed(1.5f);
            setPlayerSpeedView(5);
        } else if (id == R.id.speed200_tv) {
            listener.setSpeed(2.0f);
            setPlayerSpeedView(6);
        }
    }

    public SettingPlayerView setExoPlayerType() {
        this.isExoPlayer = true;
        return this;
    }

    public SettingPlayerView setOrientationAllow(boolean isAllow) {
        isAllowScreenOrientation = isAllow;
        return this;
    }

    public void setSettingListener(SettingVideoListener listener) {
        this.listener = listener;
    }

    public void setVideoTrackList(List<VideoInfoTrack> audioTrackList) {
        this.audioTrackList.clear();
        this.audioTrackList.addAll(audioTrackList);
        this.audioStreamAdapter.notifyDataSetChanged();
        this.audioRl.setVisibility(audioTrackList.size() < 1 ? GONE : VISIBLE);
    }

    public void setSubtitleTrackList(List<VideoInfoTrack> subtitleTrackList) {
        this.subtitleTrackList.clear();
        this.subtitleTrackList.addAll(subtitleTrackList);
        this.subtitleStreamAdapter.notifyDataSetChanged();
        this.subtitleRl.setVisibility(subtitleTrackList.size() < 1 ? GONE : VISIBLE);
    }

    public void setSpeedCtrlLLVis(boolean visibility) {
        speedCtrlLL.setVisibility(visibility ? VISIBLE : GONE);
    }

    public void setOrientationChangeEnable(boolean isEnable) {
        orientationChangeSw.setChecked(isEnable);
    }

    public void setPlayerSpeedView(int type) {
        switch (type) {
            case 1:
                speed50Tv.setBackgroundColor(CommonPlayerUtils.getResColor(getContext(), R.color.selected_view_bg));
                speed75Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                speed100Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                speed125Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                speed150Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                speed200Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                break;
            case 2:
                speed50Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                speed75Tv.setBackgroundColor(CommonPlayerUtils.getResColor(getContext(), R.color.selected_view_bg));
                speed100Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                speed125Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                speed150Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                speed200Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                break;
            case 3:
                speed50Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                speed75Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                speed100Tv.setBackgroundColor(CommonPlayerUtils.getResColor(getContext(), R.color.selected_view_bg));
                speed125Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                speed150Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                speed200Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                break;
            case 4:
                speed50Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                speed75Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                speed100Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                speed125Tv.setBackgroundColor(CommonPlayerUtils.getResColor(getContext(), R.color.selected_view_bg));
                speed150Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                speed200Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                break;
            case 5:
                speed50Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                speed75Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                speed100Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                speed125Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                speed150Tv.setBackgroundColor(CommonPlayerUtils.getResColor(getContext(), R.color.selected_view_bg));
                speed200Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                break;
            case 6:
                speed50Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                speed75Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                speed100Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                speed125Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                speed150Tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                speed200Tv.setBackgroundColor(CommonPlayerUtils.getResColor(getContext(), R.color.selected_view_bg));
                break;
        }
    }

    //是否允许屏幕翻转
    public boolean isAllowScreenOrientation() {
        return isAllowScreenOrientation;
    }

    public interface SettingVideoListener {
        void selectTrack(int streamId, String language, boolean isAudio);

        void deselectTrack(int streamId, String language, boolean isAudio);

        void setSpeed(float speed);

        void setAspectRatio(int type);

        void setOrientationStatus(boolean isEnable);
    }
}
