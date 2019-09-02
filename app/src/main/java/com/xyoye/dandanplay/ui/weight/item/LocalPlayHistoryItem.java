package com.xyoye.dandanplay.ui.weight.item;

import android.view.View;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.LocalPlayHistoryBean;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

/**
 * Created by xyoye on 2019/9/2.
 */

public class LocalPlayHistoryItem implements AdapterItem<LocalPlayHistoryBean> {

    @Override
    public int getLayoutResId() {
        return R.layout.item_local_play_history;
    }

    @Override
    public void initItemViews(View itemView) {

    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(LocalPlayHistoryBean model, int position) {

    }
}
