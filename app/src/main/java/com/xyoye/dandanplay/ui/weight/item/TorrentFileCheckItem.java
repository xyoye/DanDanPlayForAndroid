package com.xyoye.dandanplay.ui.weight.item;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.TorrentCheckBean;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import butterknife.BindView;

/**
 * Created by xyoye on 2019/3/6.
 */

public class TorrentFileCheckItem implements AdapterItem<TorrentCheckBean> {

    @BindView(R.id.check_iv)
    ImageView checkIv;
    @BindView(R.id.file_name_tv)
    TextView fileNameTv;
    @BindView(R.id.file_size_tv)
    TextView fileSizeTv;

    private View mView;

    public TorrentFileCheckItem() {

    }

    @Override
    public int getLayoutResId() {
        return R.layout.item_torrent_file_check;
    }

    @Override
    public void initItemViews(View itemView) {
        mView = itemView;
    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(TorrentCheckBean model, int position) {
        checkIv.setBackground(getCheckBoxBg(model.isChecked()));
        fileNameTv.setText(model.getName());
        fileSizeTv.setText(CommonUtils.convertFileSize(model.getLength()));

        mView.setOnClickListener(v -> {
            model.setChecked(!model.isChecked());
            checkIv.setBackground(getCheckBoxBg(model.isChecked()));
        });
    }

    private Drawable getCheckBoxBg(boolean isCheck) {
        return isCheck
                ? CommonUtils.getResDrawable(R.drawable.ic_check_box_checked)
                : CommonUtils.getResDrawable(R.drawable.ic_check_box_uncheck);
    }
}
