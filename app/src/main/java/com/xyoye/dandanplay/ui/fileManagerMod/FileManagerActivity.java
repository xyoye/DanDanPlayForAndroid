package com.xyoye.dandanplay.ui.fileManagerMod;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xyoye.core.adapter.BaseRvAdapter;
import com.xyoye.core.base.BaseActivity;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.DanmuFolderBean;
import com.xyoye.dandanplay.event.OpenDanmuFolderEvent;
import com.xyoye.dandanplay.mvp.impl.DanmuLocalPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.DanmuLocalPresenter;
import com.xyoye.dandanplay.mvp.view.DanmuLocalView;
import com.xyoye.dandanplay.ui.danmuMod.DanmuNetworkActivity;
import com.xyoye.dandanplay.weight.decorator.SpacesItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;

/**
 * Created by YE on 2018/7/2.
 */

public class FileManagerActivity extends BaseActivity<DanmuLocalPresenter> implements DanmuLocalView {
    public final static String IS_FOLDER = "isFolder";
    public final static String VIDEO_PATH = "videoPath";
    public final static int SELECT_NETWORK_DANMU = 104;

    @BindView(R.id.loading_ll)
    LinearLayout loadingLl;
    @BindView(R.id.path_tv)
    TextView pathTv;
    @BindView(R.id.network_tv)
    TextView networkTv;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private BaseRvAdapter<DanmuFolderBean> adapter;
    private boolean isFolder;

    @Override
    public void initView() {
        isFolder = isFolder();

        if (isFolder){
            setTitle("选择文件夹");
            networkTv.setVisibility(View.GONE);
        } else {
            setTitle("选择本地弹幕");
            networkTv.setVisibility(View.VISIBLE);
        }


        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.addItemDecoration(new SpacesItemDecoration(1,0,0,0));

        networkTv.setOnClickListener(v -> {
            Intent intent = new Intent(FileManagerActivity.this, DanmuNetworkActivity.class);
            String videoPath = getIntent().getStringExtra(VIDEO_PATH);
            intent.putExtra("path", videoPath);
            startActivityForResult(intent, SELECT_NETWORK_DANMU);
        });
    }

    @Override
    public void initListener() {

    }

    @NonNull
    @Override
    protected DanmuLocalPresenter initPresenter() {
        return new DanmuLocalPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_file_manager;
    }

    @Override
    public void refreshAdapter(List<DanmuFolderBean> beans) {
        if (adapter == null){
            adapter = new BaseRvAdapter<DanmuFolderBean>(beans) {
                @NonNull
                @Override
                public AdapterItem<DanmuFolderBean> onCreateItem(int viewType) {
                    return new FileManagerItem();
                }
            };
            recyclerView.setAdapter(adapter);
        }else {
            adapter.setData(beans);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean isFolder(){
        return getIntent().getBooleanExtra(IS_FOLDER, true);
    }

    @Override
    public void showLoading() {
        loadingLl.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        loadingLl.setVisibility(View.GONE);
    }

    @Override
    public void updatePathTitle(String path) {
        pathTv.setText(path);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onpenFolder(OpenDanmuFolderEvent event){
        if (event.isFolder()){
            presenter.listFile(event.getPath());
        }else {
            Intent intent = getIntent();
            intent.putExtra("danmu", event.getPath());
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.select_video_folder:
                selectedFolder();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isFolder)
            getMenuInflater().inflate(R.menu.menu_select, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void selectedFolder(){
        Intent intent = getIntent();
        intent.putExtra("folder", pathTv.getText().toString());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == SELECT_NETWORK_DANMU){
                Intent intent = getIntent();
                intent.putExtra("danmu", data.getStringExtra("path"));
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }
}
