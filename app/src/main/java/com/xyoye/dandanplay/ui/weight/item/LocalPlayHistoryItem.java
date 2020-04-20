package com.xyoye.dandanplay.ui.weight.item;

import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.LocalPlayHistoryBean;
import com.xyoye.dandanplay.ui.activities.play.PlayerManagerActivity;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;

/**
 * Created by xyoye on 2019/9/2.
 */

public class LocalPlayHistoryItem implements AdapterItem<LocalPlayHistoryBean> {

    @BindView(R.id.video_path_tv)
    TextView videoPathTv;
    @BindView(R.id.source_origin_tv)
    TextView sourceOriginTv;
    @BindView(R.id.play_time_tv)
    TextView playTimeTv;
    @BindView(R.id.position_tv)
    TextView positionTv;
    @BindView(R.id.delete_cb)
    CheckBox deleteCb;
    @BindView(R.id.header_click_rl)
    RelativeLayout headerClickRl;
    @BindView(R.id.info_ll)
    LinearLayout infoLl;

    private View view;
    private OnLocalHistoryItemClickListener listener;

    public LocalPlayHistoryItem(OnLocalHistoryItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.item_local_play_history;
    }

    @Override
    public void initItemViews(View itemView) {
        this.view = itemView;
    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(LocalPlayHistoryBean model, int position) {
        positionTv.setText(String.valueOf((position+1)));
        videoPathTv.setText(CommonUtils.decodeHttpUrl(model.getVideoPath()));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        playTimeTv.setText(dateFormat.format(model.getPlayTime()));

        positionTv.setVisibility(model.isDeleteMode() ? View.GONE : View.VISIBLE);
        deleteCb.setVisibility(model.isDeleteMode() ? View.VISIBLE : View.GONE);
        deleteCb.setChecked(model.isChecked());

        switch (model.getSourceOrigin()) {
            case PlayerManagerActivity.SOURCE_ORIGIN_LOCAL:
                sourceOriginTv.setText("本地资源播放");
                break;
            case PlayerManagerActivity.SOURCE_ORIGIN_OUTSIDE:
                sourceOriginTv.setText("外部资源播放");
                break;
            case PlayerManagerActivity.SOURCE_ORIGIN_REMOTE:
                sourceOriginTv.setText("远程播放");
                break;
            case PlayerManagerActivity.SOURCE_ORIGIN_SMB:
                sourceOriginTv.setText("局域网播放");
                break;
            case PlayerManagerActivity.SOURCE_ORIGIN_STREAM:
                sourceOriginTv.setText("串流播放");
                break;
            case PlayerManagerActivity.SOURCE_ONLINE_PREVIEW:
                sourceOriginTv.setText("在线播放");
                break;
            default:
                sourceOriginTv.setText("未知来源播放");
                break;
        }

        infoLl.setOnClickListener(v -> {
            if(model.isDeleteMode()){
                if (listener != null)
                    listener.onCheckedChanged(position);
                return;
            }
            String danmuPath = model.getDanmuPath();
            int episodeId = model.getEpisodeId();
            if (!TextUtils.isEmpty(model.getDanmuPath())) {
                File danmuFile = new File(danmuPath);
                if (!danmuFile.exists()) {
                    danmuPath = "";
                    episodeId = 0;
                }
            }

            String zimuPath = model.getZimuPath();
            if (TextUtils.isEmpty(zimuPath)){
                zimuPath = "";
            } else {
                File zimuFile = new File(zimuPath);
                if (!zimuFile.exists()) {
                    zimuPath = "";
                }
            }

            PlayerManagerActivity.launchPlayerHistory(
                    view.getContext(),
                    model.getVideoTitle(),
                    model.getVideoPath(),
                    danmuPath,
                    zimuPath,
                    0,
                    episodeId,
                    model.getSourceOrigin());

        });

        infoLl.setOnLongClickListener(v -> {
            if (listener != null)
                return listener.onLongClick(position);
            return false;
        });

        headerClickRl.setOnClickListener(v -> {
            if (listener != null && model.isDeleteMode())
                listener.onCheckedChanged(position);
        });
    }

    public interface OnLocalHistoryItemClickListener {
        boolean onLongClick(int position);

        void onCheckedChanged(int position);
    }
}
