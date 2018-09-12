package com.xyoye.dandanplay.ui.personalMod;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.xyoye.core.adapter.BaseRvAdapter;
import com.xyoye.core.base.BaseActivity;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.AnimeFavoriteBean;
import com.xyoye.dandanplay.mvp.impl.PersonalPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.PersonalFavoritePresenter;
import com.xyoye.dandanplay.mvp.view.PeronalFavoriteView;

import butterknife.BindView;

/**
 * Created by YE on 2018/7/24.
 */


public class PersonalFavoriteActivity extends BaseActivity<PersonalFavoritePresenter> implements PeronalFavoriteView {
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @Override
    public void initView() {
        setTitle("我的关注");

        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        AnimeFavoriteBean favoriteBean = (AnimeFavoriteBean)getIntent().getSerializableExtra("bean");
        if (favoriteBean != null){
            BaseRvAdapter<AnimeFavoriteBean.FavoritesBean> adapter = new BaseRvAdapter<AnimeFavoriteBean.FavoritesBean>(favoriteBean.getFavorites()) {
                @NonNull
                @Override
                public AdapterItem<AnimeFavoriteBean.FavoritesBean> onCreateItem(int viewType) {
                    return new PersonalFavoriteAnimaItem();
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
    protected PersonalFavoritePresenter initPresenter() {
        return new PersonalPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_personal_favorite;
    }
}
