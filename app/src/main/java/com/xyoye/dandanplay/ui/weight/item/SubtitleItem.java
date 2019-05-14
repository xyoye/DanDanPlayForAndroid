package com.xyoye.dandanplay.ui.weight.item;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.player.commom.bean.SubtitleBean;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import butterknife.BindView;

/**
 * Created by xyoye on 2019/5/13.
 */

public class SubtitleItem implements AdapterItem<SubtitleBean> {

    @BindView(R.id.subtitle_title_tv)
    TextView subtitleTitleTv;
    @BindView(R.id.subtitle_rank_tv)
    TextView subtitleRankTv;
    @BindView(R.id.subtitle_origin_tv)
    TextView subtitleOriginTv;
    @BindView(R.id.item_view)
    RelativeLayout itemView;

    private SubtitleSelectCallBack callBack;

    public SubtitleItem(SubtitleSelectCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.item_subtitle;
    }

    @Override
    public void initItemViews(View itemView) {

    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(SubtitleBean model, int position) {
        subtitleTitleTv.setText(model.getName());
        subtitleOriginTv.setText(model.getOrigin());

        String rankText = model.getRank() >= 0
                            ? model.getRank() + "星"
                            : "无";
        subtitleRankTv.setText(rankText);
        itemView.setOnClickListener(v -> {
            if (callBack != null)
                callBack.onSelect(model.getName(), model.getUrl());
        });
    }

    public interface SubtitleSelectCallBack {
        void onSelect(String fileName, String link);
    }
}
