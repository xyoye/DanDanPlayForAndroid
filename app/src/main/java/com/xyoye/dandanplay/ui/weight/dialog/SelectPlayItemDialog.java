package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import com.xyoye.dandanplay.R;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by xyy on 2018/10/17.
 */

public class SelectPlayItemDialog extends Dialog {
    @BindView(R.id.dialog_title_tv)
    TextView dialogTitleTv;
    @BindView(R.id.dialog_cancel_iv)
    ImageView dialogCancelIv;
    @BindView(R.id.dialog_rv)
    RecyclerView dialogRv;
//
//    private List<PlayListItem> playListItems;
//    private BaseRvAdapter<PlayListItem> playRvAdapter;
    private Context context;

    public SelectPlayItemDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

//    public SelectPlayItemDialog(@NonNull Context context, int themeResId, List<PlayListItem> playListItems) {
//        super(context, themeResId);
//        this.context = context;
//        this.playListItems = playListItems;
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_select_rv);
        ButterKnife.bind(this);

        dialogRv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        dialogRv.setNestedScrollingEnabled(false);
        dialogRv.setItemViewCacheSize(10);
//
//        playRvAdapter  = new BaseRvAdapter<PlayListItem>(new ArrayList<>()) {
//            @NonNull
//            @Override
//            public AdapterItem<PlayListItem> onCreateItem(int viewType) {
//                return new SelectPlayItem();
//            }
//        };
//
//        playRvAdapter.setData(playListItems);
//        playRvAdapter.notifyDataSetChanged();
//        dialogRv.setAdapter(playRvAdapter);
//        dialogTitleTv.setText("选择播放资源");
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEvent(PlayListItem model) {
//        SelectPlayItemDialog.this.hide();
//    }

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
        SelectPlayItemDialog.this.hide();
    }
}
