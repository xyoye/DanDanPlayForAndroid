package com.xyoye.dandanplay.ui.activities.smb;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.ImageView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.interf.AdapterItem;
import com.xyoye.smb.info.SmbFileInfo;

import butterknife.BindView;

/**
 * Created by xyoye on 2020/1/3.
 */

public class SmbFileItem implements AdapterItem<SmbFileInfo> {

    @BindView(R.id.smb_cover_iv)
    ImageView smbCoverIv;
    @BindView(R.id.smb_name_tv)
    AppCompatTextView smbNameTv;
    @BindView(R.id.item_layout)
    ConstraintLayout itemLayout;

    private OnSmbFileItemClickListener listener;

    public SmbFileItem(OnSmbFileItemClickListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener can not be null");
        }
        this.listener = listener;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.item_smb_file;
    }

    @Override
    public void initItemViews(View itemView) {

    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(SmbFileInfo model, int position) {
        smbCoverIv.setImageResource(
                model.isDirectory()
                        ? R.mipmap.ic_smb_folder
                        : CommonUtils.isMediaFile(model.getFileName())
                        ? R.mipmap.ic_smb_video
                        : R.mipmap.ic_smb_file);
        smbNameTv.setText(model.getFileName());

        smbNameTv.setTextColor(CommonUtils.getResColor(R.color.text_black));

        itemLayout.setOnClickListener(v -> listener.onClick(model.getFileName(), model.isDirectory()));
    }

    public interface OnSmbFileItemClickListener {
        void onClick(String fileName, boolean isDir);
    }
}
