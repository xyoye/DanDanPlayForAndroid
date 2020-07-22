package com.xyoye.dandanplay.ui.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ConvertUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpFragment;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.AnimeBean;
import com.xyoye.dandanplay.bean.BangumiBean;
import com.xyoye.dandanplay.mvp.impl.AnimePresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.AnimePresenter;
import com.xyoye.dandanplay.mvp.view.AnimeView;
import com.xyoye.dandanplay.ui.weight.ItemDecorationSpaces;
import com.xyoye.dandanplay.ui.weight.item.AnimeItem;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;

/**
 * Created by xyoye on 2018/7/15.
 */

public class AnimeFragment extends BaseMvpFragment<AnimePresenter> implements AnimeView {
    @BindView(R.id.anime_rv)
    RecyclerView animeRv;

    public static AnimeFragment newInstance(BangumiBean bangumiBean){
        AnimeFragment animeFragment = new AnimeFragment();
        Bundle args = new Bundle();
        args.putSerializable("anime_data", bangumiBean);
        animeFragment.setArguments(args);
        return animeFragment;
    }

    @NonNull
    @Override
    protected AnimePresenter initPresenter() {
        return new AnimePresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutId() {
        return R.layout.fragment_anime;
    }

    @Override
    public void initView() {
        BangumiBean bangumiBean;
        Bundle args = getArguments();
        if (args == null) return;
        bangumiBean = (BangumiBean)getArguments().getSerializable("anime_data");
        if (bangumiBean ==null) return;

        List<AnimeBean> bangumiList = bangumiBean.getBangumiList();

        if (AppConfig.getInstance().isLogin()){
            Collections.sort(bangumiList, (o1, o2) -> {
                // 返回值为int类型，大于0表示正序，小于0表示逆序
                if (o1.isIsFavorited()) return -1;
                if (o2.isIsFavorited()) return 1;
                return 0;
            });
        }
        BaseRvAdapter<AnimeBean> adapter = new BaseRvAdapter<AnimeBean>(bangumiList) {
            @NonNull
            @Override
            public AdapterItem<AnimeBean> onCreateItem(int viewType) {
                return new AnimeItem();
            }
        };

        animeRv.setLayoutManager(new GridLayoutManager(getContext(), 3){});
        animeRv.addItemDecoration(new ItemDecorationSpaces(ConvertUtils.dp2px(5)));
        animeRv.setAdapter(adapter);
    }

    @Override
    public void initListener() {

    }

}
