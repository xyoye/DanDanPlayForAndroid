package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.xyoye.core.adapter.BaseRvAdapter;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.AnimeTypeBean;
import com.xyoye.dandanplay.bean.SubGroupBean;
import com.xyoye.dandanplay.bean.event.SelectInfoEvent;
import com.xyoye.dandanplay.ui.weight.item.SelectAnimeTypeItem;
import com.xyoye.dandanplay.ui.weight.item.SelectSubGroupItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by xyy on 2018/10/15.
 */

public class SelectInfoDialog<T> extends Dialog {

    @BindView(R.id.dialog_title_tv)
    TextView dialogTitleTv;
    @BindView(R.id.dialog_rv)
    RecyclerView dialogRv;

    private int createType;

    private Context context;
    private List<T> beanList;
    private BaseRvAdapter<AnimeTypeBean.TypesBean> typesRvAdapter;
    private BaseRvAdapter<SubGroupBean.SubgroupsBean> subgroupsRvAdapter;

    public SelectInfoDialog(@NonNull Context context, int themeResId, int createType, List<T> beanList) {
        super(context, themeResId);
        this.context = context;
        this.createType = createType;
        this.beanList = beanList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_select_rv);
        ButterKnife.bind(this);

        dialogRv.setLayoutManager(new GridLayoutManager(context, 2));

        typesRvAdapter = new BaseRvAdapter<AnimeTypeBean.TypesBean>(new ArrayList<>()) {
            @NonNull
            @Override
            public AdapterItem<AnimeTypeBean.TypesBean> onCreateItem(int viewType) {
                return new SelectAnimeTypeItem();
            }
        };

        subgroupsRvAdapter = new BaseRvAdapter<SubGroupBean.SubgroupsBean>(new ArrayList<>()) {
            @NonNull
            @Override
            public AdapterItem<SubGroupBean.SubgroupsBean> onCreateItem(int viewType) {
                return new SelectSubGroupItem();
            }
        };

        if (createType == SelectInfoEvent.TYPE){
            typesRvAdapter.setData((List<AnimeTypeBean.TypesBean>) beanList);
            typesRvAdapter.notifyDataSetChanged();
            dialogRv.setAdapter(typesRvAdapter);
            dialogTitleTv.setText("选择资源分类");
        }else {
            dialogRv.setAdapter(subgroupsRvAdapter);
            subgroupsRvAdapter.setData((List<SubGroupBean.SubgroupsBean>)beanList);
            subgroupsRvAdapter.notifyDataSetChanged();
            dialogTitleTv.setText("选择字幕组");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SelectInfoEvent event) {
        SelectInfoDialog.this.hide();
    }

    @Override
    public void show() {
        super.show();
        EventBus.getDefault().register(this);
    }

    @Override
    public void hide() {
        super.hide();
        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.dialog_cancel_iv)
    public void onViewClicked() {
        SelectInfoDialog.this.hide();
    }
}
