package com.xyoye.dandanplay.ui.animeMod;

import android.view.View;
import android.widget.TextView;

import com.xyoye.core.interf.AdapterItem;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.AnimeDetailBean;

import butterknife.BindView;

/**
 * Created by YE on 2018/7/21.
 */


public class AnimeEpisodeItem implements AdapterItem<AnimeDetailBean.BangumiBean.EpisodesBean> {
    @BindView(R.id.episode_number)
    TextView episodeNumber;
    @BindView(R.id.episode_title)
    TextView episodeTitle;

    @Override
    public int getLayoutResId() {
        return R.layout.item_anime_episode;
    }

    @Override
    public void initItemViews(View itemView) {

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
    }
}
