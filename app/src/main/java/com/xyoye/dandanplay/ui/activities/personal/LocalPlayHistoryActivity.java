package com.xyoye.dandanplay.ui.activities.personal;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvcActivity;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.LocalPlayHistoryBean;
import com.xyoye.dandanplay.database.DataBaseManager;
import com.xyoye.dandanplay.ui.weight.item.LocalPlayHistoryItem;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by xyoye on 2019/9/2.
 */

public class LocalPlayHistoryActivity extends BaseMvcActivity {

    @BindView(R.id.rv)
    RecyclerView recyclerView;

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_local_play_history;
    }

    @Override
    public void initPageView() {
        List<LocalPlayHistoryBean> historyList = new ArrayList<>();

        Cursor cursor = DataBaseManager.getInstance()
                .selectTable(18)
                .query()
                .setOrderByColumnDesc(6)
                .execute();

        while (cursor.moveToNext()) {
            LocalPlayHistoryBean historyBean = new LocalPlayHistoryBean();
            historyBean.setVideoPath(cursor.getString(1));
            historyBean.setVideoTitle(cursor.getString(2));
            historyBean.setDanmuPath(cursor.getString(3));
            historyBean.setEpisodeId(cursor.getInt(4));
            historyBean.setSourceOrigin(cursor.getInt(5));
            historyBean.setPlayTime(cursor.getLong(6));
            historyList.add(historyBean);
        }

        BaseRvAdapter<LocalPlayHistoryBean> adapter = new BaseRvAdapter<LocalPlayHistoryBean>(historyList) {
            @NonNull
            @Override
            public AdapterItem<LocalPlayHistoryBean> onCreateItem(int viewType) {
                return new LocalPlayHistoryItem();
            }
        };

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void initPageViewListener() {

    }
}
