//package com.xyoye.dandanplay.ui.weight.item;
//
//import android.view.View;
//import android.widget.TextView;
//
//import com.xyoye.core.interf.AdapterItem;
//import com.xyoye.dandanplay.R;
//import com.xyoye.dandanplay.utils.FileUtils;
//import com.xyoye.dandanplay.utils.thunder.PlayListItem;
//
//import org.greenrobot.eventbus.EventBus;
//
//import butterknife.BindView;
//
///**
// * Created by xyy on 2018/10/17.
// */
//
//public class SelectPlayItem implements AdapterItem<PlayListItem> {
//
//    @BindView(R.id.info_tv)
//    TextView infoTv;
//    @BindView(R.id.extra_info_tv)
//    TextView extraInfoTv;
//
//    private View mView;
//
//    @Override
//    public int getLayoutResId() {
//        return R.layout.item_select_info;
//    }
//
//    @Override
//    public void initItemViews(View itemView) {
//        mView = itemView;
//    }
//
//    @Override
//    public void onSetViews() {
//
//    }
//
//    @Override
//    public void onUpdateViews(PlayListItem model, int position) {
//        extraInfoTv.setVisibility(View.VISIBLE);
//        infoTv.setText(model.getName());
//        extraInfoTv.setText(FileUtils.convertFileSize(model.getSize()));
//
//        mView.setOnClickListener(v ->
//                EventBus.getDefault().post(model));
//    }
//}