package com.xyoye.dandanplay.ui.weight.item;

import android.view.View;
import android.widget.TextView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.AnimeDetailBean;
import com.xyoye.dandanplay.bean.event.SearchMagnetEvent;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;

/**
 * Created by YE on 2018/7/21.
 */


public class AnimeEpisodeItem implements AdapterItem<AnimeDetailBean.BangumiBean.EpisodesBean> {
    @BindView(R.id.episode_number)
    TextView episodeNumber;
    @BindView(R.id.episode_title)
    TextView episodeTitle;

    private View mView;

    @Override
    public int getLayoutResId() {
        return R.layout.item_anime_episode;
    }

    @Override
    public void initItemViews(View itemView) {
        mView = itemView;
    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(AnimeDetailBean.BangumiBean.EpisodesBean model, int position) {
        String info = model.getEpisodeTitle();
        String[] infoArray = info.split("\\s");
        if (infoArray.length > 1){
            episodeNumber.setText(infoArray[0]);
            String title = info.substring(infoArray[0].length()+1, info.length());
            episodeTitle.setText(title);
        }else if (infoArray.length == 1){
            episodeTitle.setText(infoArray[0]);
        }else {
            episodeTitle.setText("未知剧集");
        }

        mView.setOnClickListener(v ->
                EventBus.getDefault().post(new SearchMagnetEvent(position, infoArray[0], model.getEpisodeId())));
    }
}
