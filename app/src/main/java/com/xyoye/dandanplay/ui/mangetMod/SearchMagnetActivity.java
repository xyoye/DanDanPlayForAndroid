package com.xyoye.dandanplay.ui.mangetMod;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.xyoye.core.base.BaseActivity;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.mvp.impl.SearchMagnetPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.SearchMagnetPresenter;
import com.xyoye.dandanplay.mvp.view.SearchMagnetView;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by YE on 2018/10/13.
 */


public class SearchMagnetActivity extends BaseActivity<SearchMagnetPresenter> implements SearchMagnetView {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.search_magnet_subground_tv)
    TextView searchMagnetSubgroundTv;
    @BindView(R.id.search_magnet_type_tv)
    TextView searchMagnetTypeTv;
    @BindView(R.id.search_magnet_et)
    EditText searchMagnetEt;
    @BindView(R.id.search_magnet_bt)
    Button searchMagnetBt;
    @BindView(R.id.magnet_rv)
    RecyclerView magnetRv;

    @Override
    public void initView() {
        setTitle("播放资源列表");

        magnetRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        magnetRv.setNestedScrollingEnabled(false);
        magnetRv.setItemViewCacheSize(10);

        String animeName = getIntent().getStringExtra("anime");
        String episode = getIntent().getStringExtra("episode");
        String term = animeName + " " + episode;
        searchMagnetEt.setText(term);

        presenter.searchManget(term, -1,-1);
    }

    @Override
    public void initListener() {

    }

    @OnClick({R.id.search_magnet_subground_tv, R.id.search_magnet_type_tv, R.id.search_magnet_bt})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.search_magnet_subground_tv:
                break;
            case R.id.search_magnet_type_tv:
                break;
            case R.id.search_magnet_bt:
                break;
        }
    }

    @NonNull
    @Override
    protected SearchMagnetPresenter initPresenter() {
        return new SearchMagnetPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_search_magnet;
    }
}
