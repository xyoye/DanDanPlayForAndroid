package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.AnimeTypeBean;
import com.xyoye.dandanplay.bean.SubGroupBean;
import com.xyoye.dandanplay.bean.event.SelectInfoEvent;
import com.xyoye.dandanplay.ui.weight.item.SelectAnimeTypeItem;
import com.xyoye.dandanplay.ui.weight.item.SelectSubGroupItem;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by xyoye on 2018/10/15.
 */

public class SelectInfoDialog extends Dialog {

    @BindView(R.id.dialog_title_tv)
    TextView dialogTitleTv;
    @BindView(R.id.dialog_rv)
    RecyclerView dialogRv;

    private int createType;

    private Context context;
    private List<AnimeTypeBean.TypesBean> animeTypeList;
    private List<SubGroupBean.SubgroupsBean> subgroupsList;

    public SelectInfoDialog(@NonNull Context context, List<AnimeTypeBean.TypesBean> animeTypeList) {
        super(context, R.style.Dialog);
        this.context = context;
        this.createType = SelectInfoEvent.TYPE;
        this.animeTypeList = animeTypeList;
    }

    public SelectInfoDialog(@NonNull Context context, List<SubGroupBean.SubgroupsBean> subgroupsList, int createType) {
        super(context, R.style.Dialog);
        this.context = context;
        this.createType = createType;
        this.subgroupsList = subgroupsList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_select_rv);
        ButterKnife.bind(this);

        if (createType == SelectInfoEvent.TYPE) {
            BaseRvAdapter<AnimeTypeBean.TypesBean> typesRvAdapter = new BaseRvAdapter<AnimeTypeBean.TypesBean>(animeTypeList) {
                @NonNull
                @Override
                public AdapterItem<AnimeTypeBean.TypesBean> onCreateItem(int viewType) {
                    return new SelectAnimeTypeItem();
                }
            };
            dialogRv.setLayoutManager(new GridLayoutManager(context, 2));
            dialogRv.setAdapter(typesRvAdapter);
            dialogTitleTv.setText("选择资源分类");
        } else {
            BaseRvAdapter<SubGroupBean.SubgroupsBean> subgroupsRvAdapter = new BaseRvAdapter<SubGroupBean.SubgroupsBean>(subgroupsList) {
                @NonNull
                @Override
                public AdapterItem<SubGroupBean.SubgroupsBean> onCreateItem(int viewType) {
                    return new SelectSubGroupItem();
                }
            };

            dialogRv.setLayoutManager(new GridLayoutManager(context, 2));
            dialogRv.setAdapter(subgroupsRvAdapter);
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
