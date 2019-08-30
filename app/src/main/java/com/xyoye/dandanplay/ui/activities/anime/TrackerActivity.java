package com.xyoye.dandanplay.ui.activities.anime;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.base.BaseMvcActivity;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.TrackerBean;
import com.xyoye.dandanplay.ui.weight.dialog.CommonDialog;
import com.xyoye.dandanplay.ui.weight.item.TrackerItem;
import com.xyoye.dandanplay.utils.TrackerManager;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by xyoye on 2019/3/30.
 */

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
                        for (TrackerBean trackerBean : trackerList) {
                            trackerBean.setSelectType(true);
                            trackerBean.setSelected(false);
                        }
                        trackerList.get(position).setSelected(true);
                        trackerAdapter.notifyDataSetChanged();

                        setTitle("删除tracker");
                        menuCancelItem.setVisible(true);
                        menuDeleteItem.setVisible(true);
                        menuHelpItem.setVisible(false);
                        addTrackerBt.hide();
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
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_tracker_add, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog addTrackerDialog = builder.setTitle("添加Tracker")
                .setView(dialogView)
                .setPositiveButton("确定", null)
                .setNegativeButton("取消", null)
                .create();
        addTrackerDialog.show();
        addTrackerDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    EditText trackerEt = dialogView.findViewById(R.id.tracker_et);
                    String trackerText = trackerEt.getText().toString().trim();
                    if (addTracker(trackerText))
                        addTrackerDialog.dismiss();
                });
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
        switch (item.getItemId()) {
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
                while (iterator.hasNext()) {
                    TrackerBean trackerBean = (TrackerBean) iterator.next();
                    if (trackerBean.isSelected()) {
                        IApplication.trackers.remove(trackerBean.getTracker());
                        iterator.remove();
                    } else {
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
                addTrackerBt.show();
                break;
            case R.id.tracker_cancel:
                for (TrackerBean trackerBean : trackerList) {
                    trackerBean.setSelected(false);
                    trackerBean.setSelectType(false);
                }
                trackerAdapter.notifyDataSetChanged();

                setTitle("tracker管理");
                menuCancelItem.setVisible(false);
                menuDeleteItem.setVisible(false);
                menuHelpItem.setVisible(true);
                addTrackerBt.show();
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

    public boolean addTracker(String trackerText) {
        //数据为空
        if (StringUtils.isEmpty(trackerText)) {
            ToastUtils.showShort("tracker不能为空");
            return false;
        }

        //添加一条
        if (!trackerText.contains("\n")) {
            if (IApplication.trackers.contains(trackerText)) {
                ToastUtils.showShort("该tracker已存在");
                return false;
            }
            IApplication.trackers.add(trackerText);
            TrackerManager.addTracker(trackerText);
            updateTracker();
            ToastUtils.showShort("已添加");
            return true;
        }

        //添加多条
        List<String> trackerList = new ArrayList<>();
        String[] trackers = trackerText.split("\n");
        for (String tracker : trackers) {
            tracker = tracker.replace(" ", "");
            if (IApplication.trackers.contains(tracker)) {
                continue;
            }
            trackerList.add(tracker);
            IApplication.trackers.add(tracker);
        }

        TrackerManager.addTracker(trackerList);
        updateTracker();
        ToastUtils.showShort("已添加");
        return true;
    }

    private void updateTracker() {
        trackerList.clear();
        for (String tracker : IApplication.trackers) {
            TrackerBean trackerBean = new TrackerBean();
            trackerBean.setSelected(false);
            trackerBean.setSelectType(false);
            trackerBean.setTracker(tracker);
            trackerList.add(trackerBean);
        }
        trackerAdapter.notifyDataSetChanged();
    }
}
