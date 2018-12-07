package com.xyoye.dandanplay.ui.activities;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseItemDraggableAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback;
import com.chad.library.adapter.base.listener.OnItemSwipeListener;
import com.jaeger.library.StatusBarUtil;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.bean.event.MessageEvent;
import com.xyoye.dandanplay.database.DataBaseInfo;
import com.xyoye.dandanplay.database.DataBaseManager;
import com.xyoye.dandanplay.ui.weight.dialog.AddTrackerDialog;
import com.xyoye.dandanplay.ui.weight.dialog.CommonDialog;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.Constants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TrackerActivity extends AppCompatActivity {

    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar)
    android.support.v7.widget.Toolbar toolbar;
    @BindView(R.id.tracker_rv)
    RecyclerView trackerRv;
    @BindView(R.id.add_tracker_bt)
    FloatingActionButton addTrackerBt;

    private TrackerAdapter trackerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar actionBar =  getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        StatusBarUtil.setColor(this, ContextCompat.getColor(this, R.color.theme_color), 0);

        setTitle("tracker管理");

        initView();

        initListener();
    }

    private void initView(){
        trackerAdapter = new TrackerAdapter(R.layout.item_tracker, IApplication.trackers);
        trackerRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        trackerRv.setItemViewCacheSize(10);
        trackerRv.setAdapter(trackerAdapter);
        ItemDragAndSwipeCallback itemDragAndSwipeCallback = new ItemDragAndSwipeCallback(trackerAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemDragAndSwipeCallback);
        itemTouchHelper.attachToRecyclerView(trackerRv);
        trackerAdapter.enableSwipeItem();
        trackerAdapter.setOnItemSwipeListener(new OnItemSwipeListener() {
            @Override
            public void onItemSwipeStart(RecyclerView.ViewHolder viewHolder, int pos) {
                SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
                sqLiteDatabase.delete(DataBaseInfo.getTableNames()[8], "tracker = ?" , new String[]{IApplication.trackers.get(pos)});
            }

            @Override
            public void clearView(RecyclerView.ViewHolder viewHolder, int pos) {

            }

            @Override
            public void onItemSwiped(RecyclerView.ViewHolder viewHolder, int pos) {
            }

            @Override
            public void onItemSwipeMoving(Canvas canvas, RecyclerView.ViewHolder viewHolder, float dX, float dY, boolean isCurrentlyActive) {

            }
        });
    }

    private void initListener(){
        addTrackerBt.setOnLongClickListener(v -> {
            new CommonDialog.Builder(TrackerActivity.this)
                    .setOkListener(dialog -> {
                        IApplication.trackers.clear();
                        IApplication.trackers.addAll(CommonUtils.readTracker(getApplicationContext()));
                        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
                        sqLiteDatabase.delete(DataBaseInfo.getTableNames()[8], "", new String[]{});
                        for (String tracker : IApplication.trackers){
                            ContentValues values=new ContentValues();
                            values.put(DataBaseInfo.getFieldNames()[8][1], tracker);
                            sqLiteDatabase.insert(DataBaseInfo.getTableNames()[8], null, values);
                        }
                        trackerAdapter.notifyDataSetChanged();
                    })
                    .setAutoDismiss()
                    .build()
                    .show("恢复为弹弹提供的初始tracker？");
            return true;
        });
    }

    @OnClick(R.id.add_tracker_bt)
    public void onViewClicked() {
        AddTrackerDialog dialog = new AddTrackerDialog(this, R.style.Dialog);
        dialog.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateEvent(MessageEvent event){
        if (event.getMsg() == MessageEvent.UPDATE_TRACKER){
            if (trackerAdapter != null)
                trackerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.tracker_help:
                new CommonDialog.Builder(TrackerActivity.this)
                        .hideCancel()
                        .setAutoDismiss()
                        .build()
                        .show(getResources().getString(R.string.what_is_tracker), "什么是tracker", "确定", "");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tracker, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private class TrackerAdapter extends BaseItemDraggableAdapter<String, BaseViewHolder> {

        private TrackerAdapter(@LayoutRes int layoutResId, @Nullable List<String> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, String item) {
            helper.setText(R.id.tracker_tv, item);
        }
    }
}
