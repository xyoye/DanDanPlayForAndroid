package com.xyoye.dandanplay.ui.weight.item;

import android.view.View;
import android.widget.TextView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.DanmuMatchBean;
import com.xyoye.dandanplay.bean.event.DownloadDanmuEvent;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;

/**
 * Created by xyoye on 2018/7/14.
 */

public class DanmuNetworkItem  implements AdapterItem<DanmuMatchBean.MatchesBean> {
    @BindView(R.id.video_title_tv)
    TextView videoTitleTv;
    @BindView(R.id.episode_title_tv)
    TextView episopeTitleTv;
    @BindView(R.id.video_type_tv)
    TextView videoTypeTv;

    private OnNetworkItemClickListener listener;
    private View mView;

    public DanmuNetworkItem(OnNetworkItemClickListener listener){
        this.listener = listener;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.item_network_danmu;
    }

    @Override
    public void initItemViews(View itemView) {
        mView = itemView;
    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(DanmuMatchBean.MatchesBean model, int position) {
        videoTitleTv.setText(model.getAnimeTitle());
        episopeTitleTv.setText(model.getEpisodeTitle());
        switch (model.getType()){
            case "tvseries":
                videoTypeTv.setText("TV放送");
                break;
            case "tvspecial":
                videoTypeTv.setText("TV特别放送");
                break;
            case "ova":
                videoTypeTv.setText("OVA");
                break;
            case "movie":
                videoTypeTv.setText("电影");
                break;
            case "musicvideo":
                videoTypeTv.setText("音乐MV");
                break;
            case "web":
                videoTypeTv.setText("网络放送");
                break;
            case "other":
                videoTypeTv.setText("其它");
                break;
            case "jpdrama":
                videoTypeTv.setText("电视剧");
                break;
            default:
                videoTypeTv.setText("未知");
                break;
        }

        mView.setOnClickListener(v -> listener.onClick(model));
    }

    public interface OnNetworkItemClickListener{
        void onClick(DanmuMatchBean.MatchesBean model);
    }
}
