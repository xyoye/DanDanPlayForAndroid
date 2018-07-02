package com.xyoye.dandanplay.ui.danmuMod;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xyoye.core.adapter.BaseRvAdapter;
import com.xyoye.core.base.BaseActivity;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.DanmuFolderBean;
import com.xyoye.dandanplay.mvp.impl.DanmuPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.DanmuPresenter;
import com.xyoye.dandanplay.mvp.view.DanmuView;
import com.xyoye.dandanplay.weight.decorator.SpacesItemDecoration;

import java.util.List;

import butterknife.BindView;

/**
 * Created by YE on 2018/7/2.
 */


public class DanmuActivity extends BaseActivity<DanmuPresenter> implements DanmuView{
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.loading_ll)
    LinearLayout loadingLl;
    @BindView(R.id.path_tv)
    TextView pathTv;
    @BindView(R.id.network_tv)
    TextView networkTv;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private BaseRvAdapter<DanmuFolderBean> adapter;

    @Override
    public void initView() {
        setTitle("");
        toolbarTitle.setText("选择本地弹幕");

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.addItemDecoration(new SpacesItemDecoration(1,0,0,0));
    }

    @Override
    public void initListener() {

    }

    @NonNull
    @Override
    protected DanmuPresenter initPresenter() {
        return new DanmuPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_danmu;
    }

    @Override
    public void refreshAdapter(List<DanmuFolderBean> beans) {
        if (adapter == null){
            adapter = new BaseRvAdapter<DanmuFolderBean>(beans) {
                @NonNull
                @Override
                public AdapterItem<DanmuFolderBean> onCreateItem(int viewType) {
                    return null;
                }
            };
            recyclerView.setAdapter(adapter);
        }else {
            adapter.setData(beans);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void showLoading() {
        loadingLl.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        loadingLl.setVisibility(View.GONE);
    }


}
