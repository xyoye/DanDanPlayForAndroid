package com.xyoye.dandanplay.ui.activities;

import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.base.BaseMvcActivity;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.TrackerBean;
import com.xyoye.dandanplay.ui.weight.dialog.AddTrackerDialog;
import com.xyoye.dandanplay.ui.weight.dialog.CommonDialog;
import com.xyoye.dandanplay.ui.weight.item.TrackerItem;
import com.xyoye.dandanplay.utils.TrackerManager;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class TrackerActivity extends BaseMvcActivity {

    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar)
    android.support.v7.widget.Toolbar toolbar;
    @BindView(R.id.tracker_rv)
    RecyclerView trackerRv;
    @BindView(R.id.add_tracker_bt)
    FloatingActionButton addTrackerBt;

    private MenuItem menuHelpItem, menuDeleteItem, menuCancelItem;

    private BaseRvAdapter<TrackerBean> trackerAdapter;
    private List<TrackerBean> trackerList;

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_tracker;
    }

    @Override
    public void initPageView() {
        setTitle("tracker管理");

        trackerList = new ArrayList<>();
        trackerAdapter = new BaseRvAdapter<TrackerBean>(trackerList) {
            @NonNull
            @Override
            public AdapterItem<TrackerBean> onCreateItem(int viewType) {
                return new TrackerItem(new TrackerItem.TrackerItemListener() {
                    @Override
                    public void onClick(int position, boolean isChecked) {
                        trackerList.get(position).setSelected(isChecked);
                        trackerAdapter.notifyItemChanged(position);
                    }

                    @Override
                    public void onLongClick(int position) {
                        for (TrackerBean trackerBean : trackerList){
                            trackerBean.setSelectType(true);
                            trackerBean.setSelected(false);
                        }
                        trackerList.get(position).setSelected(true);
                        trackerAdapter.notifyDataSetChanged();

                        setTitle("删除tracker");
                        menuCancelItem.setVisible(true);
                        menuDeleteItem.setVisible(true);
                        menuHelpItem.setVisible(false);
                        addTrackerBt.setVisibility(View.GONE);
                    }
                });
            }
        };

        trackerRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        trackerRv.setItemViewCacheSize(10);
        trackerRv.setAdapter(trackerAdapter);

        updateTracker();
    }

    @Override
    public void initPageViewListener() {
        addTrackerBt.setOnLongClickListener(v -> {
            new CommonDialog.Builder(TrackerActivity.this)
                    .setOkListener(dialog -> {
                        TrackerManager.resetTracker();
                        updateTracker();
                    })
                    .setAutoDismiss()
                    .build()
                    .show("恢复为弹弹提供的初始tracker？");
            return true;
        });
    }

    @OnClick(R.id.add_tracker_bt)
    public void onViewClicked() {
        AddTrackerDialog dialog = new AddTrackerDialog(this, R.style.Dialog, this::updateTracker);
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.tracker_help:
                new CommonDialog.Builder(TrackerActivity.this)
                        .hideCancel()
                        .setAutoDismiss()
                        .build()
                        .show(getResources().getString(R.string.what_is_tracker), "什么是tracker", "确定", "");
                break;
            case R.id.tracker_delete:
                Iterator iterator = trackerList.iterator();
                while (iterator.hasNext()){
                    TrackerBean trackerBean = (TrackerBean)iterator.next();
                    if (trackerBean.isSelected()){
                        IApplication.trackers.remove(trackerBean.getTracker());
                        iterator.remove();
                    }else {
                        trackerBean.setSelectType(false);
                        trackerBean.setSelected(false);
                    }
                }
                TrackerManager.deleteTracker();
                trackerAdapter.notifyDataSetChanged();

                setTitle("tracker管理");
                menuCancelItem.setVisible(false);
                menuDeleteItem.setVisible(false);
                menuHelpItem.setVisible(true);
                addTrackerBt.setVisibility(View.VISIBLE);
                break;
            case R.id.tracker_cancel:
                for (TrackerBean trackerBean : trackerList){
                    trackerBean.setSelected(false);
                    trackerBean.setSelectType(false);
                }
                trackerAdapter.notifyDataSetChanged();

                setTitle("tracker管理");
                menuCancelItem.setVisible(false);
                menuDeleteItem.setVisible(false);
                menuHelpItem.setVisible(true);
                addTrackerBt.setVisibility(View.VISIBLE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tracker, menu);
        menuHelpItem = menu.findItem(R.id.tracker_help);
        menuDeleteItem = menu.findItem(R.id.tracker_delete);
        menuCancelItem = menu.findItem(R.id.tracker_cancel);
        menuHelpItem.setVisible(true);
        menuDeleteItem.setVisible(false);
        menuCancelItem.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    private void updateTracker(){
        trackerList.clear();
        for (String tracker : IApplication.trackers){
            TrackerBean trackerBean = new TrackerBean();
            trackerBean.setSelected(false);
            trackerBean.setSelectType(false);
            trackerBean.setTracker(tracker);
            trackerList.add(trackerBean);
        }
        trackerAdapter.notifyDataSetChanged();
    }
}
