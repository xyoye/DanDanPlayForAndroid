package com.xyoye.dandanplay.ui.weight.item;

import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.StringUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.SmbBean;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import butterknife.BindView;

/**
 * Created by xyoye on 2019/3/30.
 */

public class SmbItem implements AdapterItem<SmbBean> {
    @BindView(R.id.smb_cover_iv)
    ImageView smbCoverIv;
    @BindView(R.id.smb_url_tv)
    TextView smbUrlTv;
    @BindView(R.id.smb_name_tv)
    TextView smbNameTv;
    @BindView(R.id.grid_ll)
    LinearLayout gridLl;
    @BindView(R.id.liner_cl)
    ConstraintLayout linerCl;
    @BindView(R.id.liner_smb_cover_iv)
    ImageView linerSmbCoverIv;
    @BindView(R.id.liner_smb_name_tv)
    TextView linerSmbNameTv;

    private View mView;
    private OnSmbItemClickListener clickListener;

    public SmbItem(OnSmbItemClickListener clickListener) {
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
    public void onUpdateViews(SmbBean model, int position) {
        switch (model.getSmbType()) {
            case Constants.SmbType.SQL_DEVICE:
                gridLl.setVisibility(View.VISIBLE);
                linerCl.setVisibility(View.GONE);
                smbCoverIv.setImageResource(R.mipmap.ic_smb_sql_device);
                String sqlDeviceName = StringUtils.isEmpty(model.getNickName()) ? model.getName() : model.getNickName();
                smbUrlTv.setVisibility(View.VISIBLE);
                smbNameTv.setText(sqlDeviceName);
                smbUrlTv.setText(model.getUrl());
                break;
            case Constants.SmbType.LAN_DEVICE:
                gridLl.setVisibility(View.VISIBLE);
                linerCl.setVisibility(View.GONE);
                smbCoverIv.setImageResource(R.mipmap.ic_smb_lan_device);
                String lanDeviceName = StringUtils.isEmpty(model.getNickName()) ? model.getName() : model.getNickName();
                smbUrlTv.setVisibility(View.VISIBLE);
                smbNameTv.setText(lanDeviceName);
                smbUrlTv.setText(model.getUrl());
                break;
            case Constants.SmbType.FOLDER:
                String folderName = model.getName();
                if (!StringUtils.isEmpty(folderName) && folderName.endsWith("/") && folderName.length() > 2) {
                    folderName = folderName.substring(0, folderName.length() - 1);
                }
                if (AppConfig.getInstance().smbIsGridLayout()) {
                    gridLl.setVisibility(View.VISIBLE);
                    linerCl.setVisibility(View.GONE);
                    smbCoverIv.setImageResource(R.mipmap.ic_smb_folder);
                    smbUrlTv.setVisibility(View.GONE);
                    smbNameTv.setText(folderName);
                } else {
                    gridLl.setVisibility(View.GONE);
                    linerCl.setVisibility(View.VISIBLE);
                    linerSmbCoverIv.setImageResource(R.mipmap.ic_smb_folder);
                    linerSmbNameTv.setText(folderName);
                }
                break;
            case Constants.SmbType.FILE:
                if (AppConfig.getInstance().smbIsGridLayout()) {
                    gridLl.setVisibility(View.VISIBLE);
                    linerCl.setVisibility(View.GONE);
                    if (CommonUtils.isMediaFile(model.getUrl())) {
                        smbCoverIv.setImageResource(R.mipmap.ic_smb_video);
                    } else {
                        smbCoverIv.setImageResource(R.mipmap.ic_smb_file);
                    }
                    smbUrlTv.setVisibility(View.GONE);
                    smbNameTv.setText(model.getName());
                }else {
                    gridLl.setVisibility(View.GONE);
                    linerCl.setVisibility(View.VISIBLE);
                    if (CommonUtils.isMediaFile(model.getUrl())) {
                        linerSmbCoverIv.setImageResource(R.mipmap.ic_smb_video);
                    } else {
                        linerSmbCoverIv.setImageResource(R.mipmap.ic_smb_file);
                    }
                    linerSmbNameTv.setText(model.getName());
                }

                break;
        }

        mView.setOnClickListener(view -> {
            if (clickListener != null) {
                clickListener.onClick(position);
            }
        });

        mView.setOnLongClickListener(v -> {
            if (model.getSmbType() == Constants.SmbType.SQL_DEVICE){
                clickListener.onLongClick(position);
                return true;
            }
            return false;
        });
    }

    public interface OnSmbItemClickListener {
        void onClick(int position);

        void onLongClick(int position);
    }
}