package com.xyoye.dandanplay.ui.activities.anime;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.player.commom.utils.AnimHelper;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.AnimeTypeBean;
import com.xyoye.dandanplay.bean.MagnetBean;
import com.xyoye.dandanplay.bean.SearchHistoryBean;
import com.xyoye.dandanplay.bean.SubGroupBean;
import com.xyoye.dandanplay.bean.event.DeleteHistoryEvent;
import com.xyoye.dandanplay.bean.event.SearchHistoryEvent;
import com.xyoye.dandanplay.bean.event.SelectInfoEvent;
import com.xyoye.dandanplay.mvp.impl.SearchPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.SearchPresenter;
import com.xyoye.dandanplay.mvp.view.SearchView;
import com.xyoye.dandanplay.ui.activities.personal.DownloadManagerActivity;
import com.xyoye.dandanplay.ui.weight.dialog.CommonDialog;
import com.xyoye.dandanplay.ui.weight.dialog.SelectInfoDialog;
import com.xyoye.dandanplay.ui.weight.item.MagnetItem;
import com.xyoye.dandanplay.ui.weight.item.SearchHistoryItem;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by xyoye on 2019/1/8.
 */

public class SearchActivity extends BaseMvpActivity<SearchPresenter> implements SearchView {

    @BindView(R.id.search_et)
    EditText searchEt;
    @BindView(R.id.subgroup_tv)
    TextView subgroupTv;
    @BindView(R.id.type_tv)
    TextView typeTv;
    @BindView(R.id.history_rv)
    RecyclerView historyRv;
    @BindView(R.id.search_result_rv)
    RecyclerView resultRv;
    @BindView(R.id.history_rl)
    RelativeLayout historyRl;

    private boolean isSearch = false;
    private String animeTitle;
    private String searchWord;
    private int typeId = -1;
    private int subgroupsId = -1;

    private List<SearchHistoryBean> historyList;
    private List<MagnetBean.ResourcesBean> resultList;
    private BaseRvAdapter<SearchHistoryBean> historyAdapter;
    private BaseRvAdapter<MagnetBean.ResourcesBean> resultAdapter;

    @NonNull
    @Override
    protected SearchPresenter initPresenter() {
        return new SearchPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_search;
    }

    @Override
    public void initView() {
        historyList = new ArrayList<>();
        resultList = new ArrayList<>();
        animeTitle = getIntent().getStringExtra("anime_title");
        searchWord = getIntent().getStringExtra("search_word");
        boolean isAnime = getIntent().getBooleanExtra("is_anime", false);

        historyAdapter = new BaseRvAdapter<SearchHistoryBean>(historyList) {
            @NonNull
            @Override
            public AdapterItem<SearchHistoryBean> onCreateItem(int viewType) {
                return new SearchHistoryItem();
            }
        };
        resultAdapter = new BaseRvAdapter<MagnetBean.ResourcesBean>(resultList) {
            @NonNull
            @Override
            public AdapterItem<MagnetBean.ResourcesBean> onCreateItem(int viewType) {
                return new MagnetItem();
            }
        };
        resultRv.setAdapter(resultAdapter);

        historyRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        historyRv.setNestedScrollingEnabled(false);
        historyRv.setItemViewCacheSize(10);
        historyRv.setAdapter(historyAdapter);

        resultRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        resultRv.setNestedScrollingEnabled(false);
        resultRv.setItemViewCacheSize(10);
        resultRv.setAdapter(resultAdapter);

        presenter.getSearchHistory(isAnime);
        if (!isAnime){
            searchEt.postDelayed(() ->
                    KeyboardUtils.showSoftInput(searchEt), 200);
        }
    }

    @Override
    public void initListener() {
        searchEt.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus){
                AnimHelper.doShowAnimator(historyRl);
            }
        });

        historyRl.setOnClickListener(v -> {
            AnimHelper.doHideAnimator(historyRl);
            searchEt.clearFocus();
            KeyboardUtils.hideSoftInput(searchEt);
        });

        searchEt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE){
                String searchText = searchEt.getText().toString().trim();
                if (StringUtils.isEmpty(searchText)) {
                    ToastUtils.showShort("请输入搜索条件");
                    return false;
                }
                search(searchText);
                return true;
            }
            return false;
        });
    }

    @OnClick({R.id.return_iv, R.id.subgroup_tv, R.id.type_tv, R.id.search_iv})
    public void onViewClicked(View view) {
        KeyboardUtils.hideSoftInput(searchEt);
        switch (view.getId()) {
            case R.id.return_iv:
                if (historyRl.getVisibility() == View.GONE || !isSearch)
                    SearchActivity.this.finish();
                else{
                    AnimHelper.doHideAnimator(historyRl);
                    searchEt.clearFocus();
                }
                break;
            case R.id.subgroup_tv:
                List<SubGroupBean.SubgroupsBean> subgroupList = presenter.getSubGroupList();
                if (subgroupList.size() > 0) {
                    SelectInfoDialog<SubGroupBean.SubgroupsBean> selectSubgroupDialog = new SelectInfoDialog<>(SearchActivity.this, R.style.Dialog, SelectInfoEvent.SUBGROUP, subgroupList);
                    selectSubgroupDialog.show();
                }
                break;
            case R.id.type_tv:
                List<AnimeTypeBean.TypesBean> typeList = presenter.getTypeList();
                if (typeList.size() > 0) {
                    SelectInfoDialog<AnimeTypeBean.TypesBean> selectTypeDialog = new SelectInfoDialog<>(SearchActivity.this, R.style.Dialog, SelectInfoEvent.TYPE, typeList);
                    selectTypeDialog.show();
                }
                break;
            case R.id.search_iv:
                String searchText = searchEt.getText().toString().trim();
                if (StringUtils.isEmpty(searchText)) {
                    ToastUtils.showShort("请输入搜索条件");
                    return;
                }
                search(searchText);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SelectInfoEvent event) {
        if (event.getType() == SelectInfoEvent.TYPE) {
            if (event.getSelectId() == -1) {
                typeId = -1;
                typeTv.setText("选分类");
            } else {
                typeId = event.getSelectId();
                typeTv.setText(event.getSelectName());
            }
        } else if (event.getType() == SelectInfoEvent.SUBGROUP) {

            if (event.getSelectId() == -1) {
                subgroupsId = -1;
                subgroupTv.setText("字幕组");
            } else {
                subgroupsId = event.getSelectId();
                subgroupTv.setText(event.getSelectName());
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DeleteHistoryEvent event) {
        if (event.isDeleteAll()){
            presenter.deleteAllHistory();
            historyList.clear();
            historyAdapter.notifyDataSetChanged();
        }else {
            if (historyList != null &&
                    historyList.size() > 0 &&
                    historyList.size() > event.getDeletePosition()) {
                presenter.deleteHistory(historyList.get(event.getDeletePosition()).get_id());
                historyList.remove(event.getDeletePosition());
                if (historyList.size() == 1)
                    historyList.clear();
                historyAdapter.notifyDataSetChanged();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SearchHistoryEvent event){
        if (StringUtils.isEmpty(event.getSearchText())) {
            ToastUtils.showShort("搜索条件不能为空");
            return;
        }
        search(event.getSearchText());
    }

    @SuppressLint("CheckResult")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MagnetBean.ResourcesBean model) {
        new RxPermissions(this).
                request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        presenter.searchLocalTorrent(model.getMagnet());
                    }
                });
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
    public void refreshHistory(List<SearchHistoryBean> historyBeanList, boolean doSearch) {
        if (historyBeanList != null) {
            historyList.clear();
            historyList.addAll(historyBeanList);
            historyAdapter.notifyDataSetChanged();
        }
        if (doSearch)
            search(searchWord);
    }

    @Override
    public void refreshSearch(List<MagnetBean.ResourcesBean> searchResult) {
        if (searchResult != null) {
            isSearch = true;
            resultList.clear();
            resultList.addAll(searchResult);
            resultAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void downloadTorrentOver(String torrentPath, String magnet) {
        Intent intent = new Intent(this, DownloadManagerActivity.class);
        intent.putExtra("anime_title", animeTitle);
        intent.putExtra("torrent_file_path", torrentPath);
        startActivity(intent);
    }

    @Override
    public void downloadExisted(String torrentPath, String magnet) {
        new CommonDialog.Builder(this)
                .setOkListener(dialog -> downloadTorrentOver(torrentPath, magnet))
                .setCancelListener(dialog -> presenter.downloadTorrent(magnet))
                .setAutoDismiss()
                .build()
                .show("检测到种子文件已存在是否重新下载", "用旧的", "重新下载");
    }

    @Override
    public String getDownloadFolder() {
        String downloadFolder = AppConfig.getInstance().getDownloadFolder();
        downloadFolder = StringUtils.isEmpty(animeTitle)
                ? downloadFolder
                : downloadFolder + "/" + animeTitle;
        return downloadFolder;
    }

    private void search(String searchText) {
        AnimHelper.doHideAnimator(historyRl);
        searchEt.setText(searchText);
        searchEt.clearFocus();
        KeyboardUtils.hideSoftInput(searchEt);

        boolean isExist = false;
        int existN = -1;
        for (int i=0; i<historyList.size(); i++){
            SearchHistoryBean historyBean = historyList.get(i);
            if(historyBean.getText().equals(searchText)){
                isExist = true;
                existN = i;
                break;
            }
        }
        if (!isExist){
            historyList.add(0, new SearchHistoryBean(historyList.size(), searchText, System.currentTimeMillis()));
            if (historyList.size() == 1){
                historyList.add(new SearchHistoryBean(-1, "", -1));
            }
            historyAdapter.notifyDataSetChanged();
            presenter.addHistory(searchText);
        }else {
            SearchHistoryBean historyBean = historyList.get(existN);
            historyList.remove(existN);
            historyList.add(0, historyBean);
            historyAdapter.notifyDataSetChanged();
            presenter.updateHistory(historyBean.get_id());
        }
        presenter.search(searchText, typeId, subgroupsId);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (historyRl.getVisibility() == View.GONE || !isSearch)
                SearchActivity.this.finish();
            else{
                AnimHelper.doHideAnimator(historyRl);
                KeyboardUtils.hideSoftInput(searchEt);
                searchEt.clearFocus();
            }
        }
        return true;
    }
}
