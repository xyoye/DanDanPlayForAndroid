package com.xyoye.dandanplay.ui.activities.play;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.BindResourceBean;
import com.xyoye.dandanplay.bean.DanmuMatchBean;
import com.xyoye.dandanplay.bean.params.BindResourceParam;
import com.xyoye.dandanplay.mvp.impl.BindDanmuPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.BindDanmuPresenter;
import com.xyoye.dandanplay.mvp.view.BindDanmuView;
import com.xyoye.dandanplay.ui.weight.ItemDecorationSpaces;
import com.xyoye.dandanplay.ui.weight.dialog.DanmuDownloadDialog;
import com.xyoye.dandanplay.ui.weight.dialog.FileManagerDialog;
import com.xyoye.dandanplay.ui.weight.dialog.SearchDanmuDialog;
import com.xyoye.dandanplay.ui.weight.item.DanmuNetworkItem;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by xyoye on 2018/7/4.
 * <p>
 * 网络弹幕绑定界面
 */

public class BindDanmuActivity extends BaseMvpActivity<BindDanmuPresenter> implements BindDanmuView {
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.current_resource_path_ll)
    LinearLayout currentResourcePathLl;
    @BindView(R.id.current_resource_tips_tv)
    TextView currentResourceTipsTv;
    @BindView(R.id.current_resource_path_tv)
    TextView currentResourcePathTv;

    private BaseRvAdapter<DanmuMatchBean.MatchesBean> adapter;
    private BindResourceParam bindResourceParam;

    @Override
    @SuppressLint("CheckResult")
    public void initView() {
        setTitle("选择网络弹幕");

        bindResourceParam = getIntent().getParcelableExtra("bind_param");
        String currentDanmuPath = bindResourceParam.getCurrentResourcePath();
        if (!TextUtils.isEmpty(currentDanmuPath)) {
            currentResourcePathLl.setVisibility(View.VISIBLE);
            currentResourceTipsTv.setText("当前弹幕: ");
            currentResourcePathTv.setText(currentDanmuPath);
        }

        adapter = new BaseRvAdapter<DanmuMatchBean.MatchesBean>(new ArrayList<>()) {
            @NonNull
            @Override
            public AdapterItem<DanmuMatchBean.MatchesBean> onCreateItem(int viewType) {
                return new DanmuNetworkItem(BindDanmuActivity.this::showDownloadDialog);
            }
        };
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.addItemDecoration(new ItemDecorationSpaces(0, 0, 0, 1));
        recyclerView.setAdapter(adapter);

        if (bindResourceParam.isOutsideFile()) {
            if (StringUtils.isEmpty(bindResourceParam.getSearchWord())) {
                ToastUtils.showShort("无匹配弹幕");
                return;
            }
            //非手机本地文件，无法获取MD5
            String searchWord = bindResourceParam.getSearchWord();
            String episode = "";
            if (searchWord.trim().contains(" ")) {
                String[] wordAndEpisode = searchWord.split(" ");
                if (wordAndEpisode.length == 2 && CommonUtils.isNum(wordAndEpisode[1])) {
                    searchWord = wordAndEpisode[0];
                    episode = wordAndEpisode[1];
                }
            }
            presenter.searchDanmu(searchWord, episode);
        } else {
            if (StringUtils.isEmpty(bindResourceParam.getVideoPath())) {
                ToastUtils.showShort("无匹配弹幕");
                return;
            }
            presenter.matchDanmu(bindResourceParam.getVideoPath());
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
                new SearchDanmuDialog(BindDanmuActivity.this, (anime, episode) ->
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
    protected BindDanmuPresenter initPresenter() {
        return new BindDanmuPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_bind_resource;
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
        new DanmuDownloadDialog(BindDanmuActivity.this, bindResourceParam.getVideoPath(), model,
                (danmuPath, episodeId) -> finishActivity(episodeId, danmuPath)).show();
    }

    public void finishActivity(int episodeId, String danmuPath) {
        BindResourceBean danmuBean = new BindResourceBean();
        danmuBean.setDanmuPath(danmuPath);
        danmuBean.setEpisodeId(episodeId);
        danmuBean.setItemPosition(bindResourceParam.getItemPosition());
        danmuBean.setTaskFilePosition(bindResourceParam.getTaskFilePosition());

        Intent intent = getIntent();
        intent.putExtra("bind_data", danmuBean);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void refreshDanmuAdapter(List<DanmuMatchBean.MatchesBean> beans) {
        adapter.setData(beans);
    }
}
