package com.xyoye.dandanplay.ui.personalMod;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.xyoye.core.adapter.BaseRvAdapter;
import com.xyoye.core.base.BaseActivity;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.PlayHistoryBean;
import com.xyoye.dandanplay.event.OpenAnimaDetailEvent;
import com.xyoye.dandanplay.mvp.impl.PersonalHistoryPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.PersonalHistoryPresenter;
import com.xyoye.dandanplay.mvp.view.PersonalHistoryView;
import com.xyoye.dandanplay.ui.animaMod.AnimeDetailActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;

/**
 * Created by YE on 2018/7/24.
 */

public class PersonalHistoryActivity extends BaseActivity<PersonalHistoryPresenter> implements PersonalHistoryView {
    @BindView(R.id.toolbar_title)
    TextView toolBarTitle;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private BaseRvAdapter<PlayHistoryBean.PlayHistoryAnimesBean> adapter;

    @Override
    public void initView() {
        setTitle("");
        toolBarTitle.setText("播放历史");

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

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openAnimaDeatil(OpenAnimaDetailEvent event){
        Intent intent = new Intent(this, AnimeDetailActivity.class);
        intent.putExtra("animaId", event.getAnimaId());
        startActivity(intent);
    }
}
