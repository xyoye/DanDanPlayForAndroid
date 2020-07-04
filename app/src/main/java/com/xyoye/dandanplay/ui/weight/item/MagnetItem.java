package com.xyoye.dandanplay.ui.weight.item;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.bean.MagnetBean;
import com.xyoye.dandanplay.ui.weight.swipe_menu.EasySwipeMenuLayout;
import com.xyoye.dandanplay.ui.weight.swipe_menu.SwipeState;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import butterknife.BindView;

/**
 * Created by xyoye on 2018/10/15.
 */

public class MagnetItem implements AdapterItem<MagnetBean.ResourcesBean> {
    @BindView(R.id.magnet_title_tv)
    TextView magnetTitleTv;
    @BindView(R.id.magnet_size_tv)
    TextView magnetSizeTv;
    @BindView(R.id.magnet_subgroup_tv)
    TextView magnetSubgroupTv;
    @BindView(R.id.magnet_type_tv)
    TextView magnetTypeTv;
    @BindView(R.id.magnet_time_tv)
    TextView magnetTimeTv;
    @BindView(R.id.swipe_menu_layout)
    EasySwipeMenuLayout swipeMenuLayout;
    @BindView(R.id.content_view)
    LinearLayout contentView;
    @BindView(R.id.download_resource_tv)
    TextView downloadResourceTv;
    @BindView(R.id.play_resource_tv)
    TextView playResourceTv;
    @BindView(R.id.copy_magnet_tv)
    TextView copyMagnetTv;
    @BindView(R.id.update_torrent_tv)
    TextView updateResourceTv;

    private MagnetItemListener listener;

    public MagnetItem(MagnetItemListener listener) {
        this.listener = listener;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.item_magnet;
    }

    @Override
    public void initItemViews(View itemView) {

    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(MagnetBean.ResourcesBean model, int position) {
        magnetTitleTv.setText(model.getTitle());
        magnetSizeTv.setText(model.getFileSize());
        magnetSubgroupTv.setText(model.getSubgroupName());
        magnetTypeTv.setText(model.getTypeName());
        magnetTimeTv.setText(model.getPublishDate());

        updateResourceTv.setVisibility(TextUtils.isEmpty(model.getMagnetPath()) ? View.GONE : View.VISIBLE);

        contentView.setOnClickListener(v -> {
            if (swipeMenuLayout.getSwipeState() != null && swipeMenuLayout.getSwipeState() != SwipeState.SWIPE_CLOSE) {
                swipeMenuLayout.closeMenu();
            } else {
                swipeMenuLayout.openMenu(SwipeState.SWIPE_RIGHT);
            }
        });

        updateResourceTv.setOnClickListener(v -> {
            listener.onItemClick(position, true, false);
        });

        playResourceTv.setOnClickListener(v -> {
            listener.onItemClick(position, false, true);
        });

        downloadResourceTv.setOnClickListener(v -> {
            listener.onItemClick(position, false, false);
        });

        copyMagnetTv.setOnClickListener(v -> {
            String magnet = model.getMagnet();
            ClipboardManager clipboardManagerMagnet = (ClipboardManager) IApplication.get_context().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipDataMagnet = ClipData.newPlainText("Label", magnet);
            if (clipboardManagerMagnet != null) {
                clipboardManagerMagnet.setPrimaryClip(mClipDataMagnet);
                ToastUtils.showShort("已复制磁链：" + magnet);
            }
        });
    }

    public interface MagnetItemListener {
        void onItemClick(int position, boolean onlyDownload, boolean playResource);
    }
}
