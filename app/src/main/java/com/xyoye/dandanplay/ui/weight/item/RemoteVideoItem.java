package com.xyoye.dandanplay.ui.weight.item;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.StringUtils;
import com.bumptech.glide.Glide;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.RemoteVideoBean;
import com.xyoye.dandanplay.ui.activities.play.PlayerManagerActivity;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import butterknife.BindView;

/**
 * Created by xyoye on 2019/7/11.
 */

public class RemoteVideoItem implements AdapterItem<RemoteVideoBean> {

    @BindView(R.id.cover_iv)
    ImageView coverIv;
    @BindView(R.id.duration_tv)
    TextView durationTv;
    @BindView(R.id.title_tv)
    TextView titleTv;
    @BindView(R.id.danmu_bind_iv)
    ImageView danmuBindIv;

    private View itemView;
    private Context context;
    private RemoteDanmuBindListener listener;

    public RemoteVideoItem(RemoteDanmuBindListener listener){
        this.listener = listener;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.item_remote_video;
    }

    @Override
    public void initItemViews(View itemView) {
        this.itemView = itemView;
        this.context = itemView.getContext();
    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(RemoteVideoBean model, int position) {
        String imageUrl = model.getOriginUrl() + "api/v1/image/" + model.getHash();
        coverIv.setScaleType(ImageView.ScaleType.FIT_XY);
        Glide.with(context)
                .load(imageUrl)
                .into(coverIv);

        String duration = CommonUtils.formatDuring(model.getDuration() * 1000);
        durationTv.setText(duration);

        titleTv.setText(model.getName());

        itemView.setOnClickListener(v -> {
            String videoUrl = model.getOriginUrl() + "api/v1/stream/" + model.getHash();
            int episodeId = StringUtils.isEmpty(model.getDanmuPath()) ? 0 : model.getEpisodeId();
            PlayerManagerActivity.launchPlayerRemote(
                    context,
                    model.getEpisodeTitle(),
                    videoUrl,
                    model.getDanmuPath(),
                    0,
                    episodeId);
        });

        if (StringUtils.isEmpty(model.getDanmuPath())){
            danmuBindIv.setImageResource(R.mipmap.ic_danmu_unexists);
        }else{
            danmuBindIv.setImageResource(R.mipmap.ic_danmu_exists);
        }

        danmuBindIv.setOnClickListener(v -> {
            if (listener != null){
                listener.onBindClick(position);
            }
        });
    }

    public interface RemoteDanmuBindListener{
        void onBindClick(int position);
    }
}
