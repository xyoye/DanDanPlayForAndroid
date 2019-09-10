package com.xyoye.dandanplay.ui.activities.play;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.BindDanmuBean;
import com.xyoye.dandanplay.bean.DanmuMatchBean;
import com.xyoye.dandanplay.bean.params.BindDanmuParam;
import com.xyoye.dandanplay.mvp.impl.DanmuNetworkPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.DanmuNetworkPresenter;
import com.xyoye.dandanplay.mvp.view.DanmuNetworkView;
import com.xyoye.dandanplay.ui.weight.ItemDecorationSpaces;
import com.xyoye.dandanplay.ui.weight.dialog.DanmuDownloadDialog;
import com.xyoye.dandanplay.ui.weight.dialog.FileManagerDialog;
import com.xyoye.dandanplay.ui.weight.dialog.SearchDanmuDialog;
import com.xyoye.dandanplay.ui.weight.item.DanmuNetworkItem;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by xyoye on 2018/7/4.
 *
 * 网络弹幕绑定界面
 */

public class DanmuNetworkActivity extends BaseMvpActivity<DanmuNetworkPresenter> implements DanmuNetworkView {
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private BaseRvAdapter<DanmuMatchBean.MatchesBean> adapter;
    private BindDanmuParam bindDanmuParam;

    @Override
    @SuppressLint("CheckResult")
    public void initView() {
        setTitle("选择网络弹幕");

        bindDanmuParam = getIntent().getParcelableExtra("bind_param");

        adapter = new BaseRvAdapter<DanmuMatchBean.MatchesBean>(new ArrayList<>()) {
            @NonNull
            @Override
            public AdapterItem<DanmuMatchBean.MatchesBean> onCreateItem(int viewType) {
                return new DanmuNetworkItem(model ->
                        new RxPermissions(DanmuNetworkActivity.this).
                                request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                .subscribe(new Observer<Boolean>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {

                                    }

                                    @Override
                                    public void onNext(Boolean aBoolean) {
                                        if (aBoolean) {
                                            showDownloadDialog(model);
                                        }
                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                    }

                                    @Override
                                    public void onComplete() {

                                    }
                                }));
            }
        };
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.addItemDecoration(new ItemDecorationSpaces(0, 0, 0, 1));
        recyclerView.setAdapter(adapter);

        if (StringUtils.isEmpty(bindDanmuParam.getVideoPath())) {
            ToastUtils.showShort("无匹配弹幕");
            return;
        }

        if (bindDanmuParam.isOutsideFile()) {
            //非手机本地文件，无法获取MD5
            String title = FileUtils.getFileNameNoExtension(bindDanmuParam.getVideoPath());
            presenter.searchDanmu(title, "");
        } else {
            presenter.matchDanmu(bindDanmuParam.getVideoPath());
        }
    }

    @Override
    public void initListener() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.local_danmu:
                new FileManagerDialog(this, FileManagerDialog.SELECT_DANMU, path ->
                        finishActivity(0, path)).show();
                break;
            case R.id.search_danmu:
                new SearchDanmuDialog(DanmuNetworkActivity.this, (anime, episode) ->
                        presenter.searchDanmu(anime, episode)).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_danmu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @NonNull
    @Override
    protected DanmuNetworkPresenter initPresenter() {
        return new DanmuNetworkPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_danmu_network;
    }

    @Override
    public void refreshAdapter(List<DanmuMatchBean.MatchesBean> beans) {
        adapter.setData(beans);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
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
    public void showLoading() {
        showLoadingDialog();
    }

    @Override
    public void hideLoading() {
        dismissLoadingDialog();
    }

    @Override
    public void showError(String message) {
        ToastUtils.showShort(message);
    }

    private void showDownloadDialog(DanmuMatchBean.MatchesBean model) {
        new DanmuDownloadDialog(DanmuNetworkActivity.this, bindDanmuParam.getVideoPath(), model,
                (danmuPath, episodeId) -> finishActivity(episodeId, danmuPath)).show();
    }

    public void finishActivity(int episodeId, String danmuPath) {
        BindDanmuBean danmuBean = new BindDanmuBean();
        danmuBean.setDanmuPath(danmuPath);
        danmuBean.setEpisodeId(episodeId);
        danmuBean.setItemPosition(bindDanmuParam.getItemPosition());
        danmuBean.setTaskFilePosition(bindDanmuParam.getTaskFilePosition());

        Intent intent = getIntent();
        intent.putExtra("bind_data", danmuBean);
        setResult(RESULT_OK, intent);
        finish();
    }
}
