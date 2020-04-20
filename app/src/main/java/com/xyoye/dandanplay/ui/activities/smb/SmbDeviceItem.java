package com.xyoye.dandanplay.ui.activities.smb;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.StringUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.SmbDeviceBean;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import butterknife.BindView;

/**
 * Created by xyoye on 2019/3/30.
 */

public class SmbDeviceItem implements AdapterItem<SmbDeviceBean> {
    @BindView(R.id.item_layout)
    RelativeLayout itemLayout;
    @BindView(R.id.smb_cover_iv)
    ImageView smbCoverIv;
    @BindView(R.id.smb_url_tv)
    TextView smbUrlTv;
    @BindView(R.id.smb_name_tv)
    TextView smbNameTv;

    private View mView;
    private OnSmbItemClickListener clickListener;

    public SmbDeviceItem(OnSmbItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.item_smb;
    }

    @Override
    public void initItemViews(View itemView) {
        mView = itemView;
    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(SmbDeviceBean model, int position) {
        String deviceName;
        if (!StringUtils.isEmpty(model.getNickName())) {
            deviceName = model.getNickName();
        } else if (!StringUtils.isEmpty(model.getName())) {
            deviceName = model.getName();
        } else {
            deviceName = "UnKnow";
        }

        smbNameTv.setText(deviceName);
        smbUrlTv.setText(model.getUrl());
        smbCoverIv.setImageResource(model.getSmbType() == Constants.SmbSourceType.SQL_DEVICE
                ? R.mipmap.ic_smb_sql_device
                : R.mipmap.ic_smb_lan_device);
        itemLayout.setBackground(model.isEditStatus()
                ? CommonUtils.getResDrawable(R.drawable.background_smb_device_checked)
                : CommonUtils.getResDrawable(R.drawable.background_smb_device_normal));

        mView.setOnClickListener(view -> {
            if (clickListener != null) {
                clickListener.onClick(position);
            }
        });

        mView.setOnLongClickListener(v -> {
            clickListener.onLongClick(position);
            return true;
        });
    }

    public interface OnSmbItemClickListener {
        void onClick(int position);

        void onLongClick(int position);
    }
}