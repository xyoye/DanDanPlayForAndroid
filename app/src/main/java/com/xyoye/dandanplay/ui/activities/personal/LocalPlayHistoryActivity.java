package com.xyoye.dandanplay.ui.activities.personal;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvcActivity;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.LocalPlayHistoryBean;
import com.xyoye.dandanplay.utils.database.DataBaseManager;
import com.xyoye.dandanplay.ui.weight.ItemDecorationDivider;
import com.xyoye.dandanplay.ui.weight.item.LocalPlayHistoryItem;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;

/**
 * Created by xyoye on 2019/9/2.
 */

public class LocalPlayHistoryActivity extends BaseMvcActivity {

    @BindView(R.id.rv)
    RecyclerView recyclerView;

    private MenuItem menuDeleteCheckedItem, menuDeleteCancelItem, menuDeleteAllItem;

    private BaseRvAdapter<LocalPlayHistoryBean> adapter;
    private List<LocalPlayHistoryBean> historyList;

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_local_play_history;
    }

    @Override
    public void initPageView() {
        setTitle("本地播放历史");

        historyList = new ArrayList<>();

        //查询记录
        Cursor cursor = DataBaseManager.getInstance()
                .selectTable("local_play_history")
                .query()
                .setOrderByColumnDesc("play_time")
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

        adapter = new BaseRvAdapter<LocalPlayHistoryBean>(historyList) {
            @NonNull
            @Override
            public AdapterItem<LocalPlayHistoryBean> onCreateItem(int viewType) {
                return new LocalPlayHistoryItem(new LocalPlayHistoryItem.OnLocalHistoryItemClickListener() {
                    @Override
                    public boolean onLongClick(int position) {
                        //长按切换到选中删除模式
                        for (LocalPlayHistoryBean historyBean : historyList) {
                            historyBean.setDeleteMode(true);
                        }
                        setTitle("删除本地播放历史");
                        menuDeleteCheckedItem.setVisible(true);
                        menuDeleteCancelItem.setVisible(true);
                        menuDeleteAllItem.setVisible(false);
                        historyList.get(position).setChecked(true);
                        adapter.notifyDataSetChanged();
                        return true;
                    }

                    @Override
                    public void onCheckedChanged(int position) {
                        boolean isChecked = historyList.get(position).isChecked();
                        historyList.get(position).setChecked(!isChecked);
                        adapter.notifyItemChanged(position);
                    }
                });
            }
        };

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(
                new ItemDecorationDivider(
                        ConvertUtils.dp2px(1),
                        CommonUtils.getResColor(R.color.layout_bg_color),
                        1)
        );
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void initPageViewListener() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.item_delete_all:
                DataBaseManager.getInstance()
                        .selectTable("local_play_history")
                        .delete()
                        .postExecute();
                historyList.clear();
                adapter.notifyDataSetChanged();
                break;
            case R.id.item_delete_cancel:
                for (LocalPlayHistoryBean historyBean : historyList) {
                    historyBean.setDeleteMode(false);
                    historyBean.setChecked(false);
                }
                setTitle("本地播放历史");
                menuDeleteCheckedItem.setVisible(false);
                menuDeleteCancelItem.setVisible(false);
                menuDeleteAllItem.setVisible(true);
                adapter.notifyDataSetChanged();
                break;
            case R.id.item_delete_checked:
                Iterator iterator = historyList.iterator();
                boolean isRemove = false;
                while (iterator.hasNext()) {
                    LocalPlayHistoryBean historyBean = (LocalPlayHistoryBean) iterator.next();
                    if (historyBean.isChecked()) {
                        DataBaseManager.getInstance()
                                .selectTable("local_play_history")
                                .delete()
                                .where("video_path", historyBean.getVideoPath())
                                .where("source_origin", String.valueOf(historyBean.getSourceOrigin()))
                                .postExecute();
                        iterator.remove();
                        isRemove = true;
                    }
                }
                if (isRemove) {
                    adapter.notifyDataSetChanged();
                } else {
                    ToastUtils.showShort("未选中播放记录");
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_local_play_history, menu);
        menuDeleteCheckedItem = menu.findItem(R.id.item_delete_checked);
        menuDeleteCancelItem = menu.findItem(R.id.item_delete_cancel);
        menuDeleteAllItem = menu.findItem(R.id.item_delete_all);
        menuDeleteCheckedItem.setVisible(false);
        menuDeleteCancelItem.setVisible(false);
        menuDeleteAllItem.setVisible(true);
        return super.onCreateOptionsMenu(menu);
    }
}
