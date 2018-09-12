package com.xyoye.dandanplay.ui.personalMod;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.xyoye.core.adapter.BaseRvAdapter;
import com.xyoye.core.base.BaseActivity;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.PlayHistoryBean;
import com.xyoye.dandanplay.mvp.impl.PersonalHistoryPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.PersonalHistoryPresenter;
import com.xyoye.dandanplay.mvp.view.PersonalHistoryView;

import butterknife.BindView;

/**
 * Created by YE on 2018/7/24.
 */

public class PersonalHistoryActivity extends BaseActivity<PersonalHistoryPresenter> implements PersonalHistoryView {
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private BaseRvAdapter<PlayHistoryBean.PlayHistoryAnimesBean> adapter;

    @Override
    public void initView() {
        setTitle("播放历史");
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        PlayHistoryBean playHistoryBean = (PlayHistoryBean)getIntent().getSerializableExtra("bean");
        if (playHistoryBean != null){
            adapter = new BaseRvAdapter<PlayHistoryBean.PlayHistoryAnimesBean>(playHistoryBean.getPlayHistoryAnimes()) {
                @NonNull
                @Override
                public AdapterItem<PlayHistoryBean.PlayHistoryAnimesBean> onCreateItem(int viewType) {
                    return new PersonalPlayHistoryItem();
                }
            };
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void initListener() {

    }

    @NonNull
    @Override
    protected PersonalHistoryPresenter initPresenter() {
        return new PersonalHistoryPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_personal_history;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}
