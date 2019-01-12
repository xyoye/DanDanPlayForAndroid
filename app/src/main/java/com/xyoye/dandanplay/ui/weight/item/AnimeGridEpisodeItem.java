package com.xyoye.dandanplay.ui.weight.item;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.StringUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.AnimeDetailBean;
import com.xyoye.dandanplay.bean.event.SearchMagnetEvent;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;

/**
 * Created by YE on 2019/1/13.
 */


public class AnimeGridEpisodeItem implements AdapterItem<AnimeDetailBean.BangumiBean.EpisodesBean> {
    @BindView(R.id.episode_number)
    TextView episodeNumber;
    @BindView(R.id.episode_title)
    TextView episodeTitle;
    @BindView(R.id.last_watch_tv)
    TextView lastWatchTv;

    private View mView;
    private boolean isGrid = false;

    public AnimeGridEpisodeItem(boolean isGrid) {
        this.isGrid = isGrid;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.item_anime_grid_episode;
    }

    @Override
    public void initItemViews(View itemView) {
        mView = itemView;
    }

    @Override
    public void onSetViews() {

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onUpdateViews(AnimeDetailBean.BangumiBean.EpisodesBean model, int position) {
        String info = model.getEpisodeTitle();
        String[] infoArray = info.split("\\s");
        if (infoArray.length > 1) {
            episodeNumber.setText(infoArray[0]);
            String title = info.substring(infoArray[0].length() + 1, info.length());
            episodeTitle.setText(title);
        } else if (infoArray.length == 1) {
            episodeTitle.setText(infoArray[0]);
        } else {
            episodeTitle.setText("未知剧集");
        }

        lastWatchTv.setVisibility(isGrid ? View.VISIBLE : View.GONE);
        lastWatchTv.setText("上次观看："+ (StringUtils.isEmpty(model.getLastWatched()) ? "无" : model.getLastWatched()));

        mView.setOnClickListener(v ->
                EventBus.getDefault().post(new SearchMagnetEvent(position, infoArray[0], model.getEpisodeId())));
    }
}
