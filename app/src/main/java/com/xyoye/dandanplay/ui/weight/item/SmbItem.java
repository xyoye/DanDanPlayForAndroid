package com.xyoye.dandanplay.ui.weight.item;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.StringUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.SmbBean;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import butterknife.BindView;

/**
 * Created by YE on 2019/3/30.
 */

public class SmbItem implements AdapterItem<SmbBean> {
    @BindView(R.id.smb_cover_iv)
    ImageView smbCoverIv;
    @BindView(R.id.smb_url_tv)
    TextView smbUrlTv;
    @BindView(R.id.smb_name_tv)
    TextView smbNameTv;

    private View mView;
    private OnSmbItemClickListener clickListener;

    public SmbItem(OnSmbItemClickListener clickListener){
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
        switch (model.getSmbType()){
            case Constants.SmbType.SQL_DEVICE:
                smbCoverIv.setImageResource(R.mipmap.ic_smb_sql_device);
                String sqlDeviceName = StringUtils.isEmpty(model.getNickName()) ? model.getName() : model.getNickName();
                smbUrlTv.setVisibility(View.VISIBLE);
                smbNameTv.setText(sqlDeviceName);
                smbUrlTv.setText(model.getUrl());
                break;
            case Constants.SmbType.LAN_DEVICE:
                smbCoverIv.setImageResource(R.mipmap.ic_smb_lan_device);
                String lanDeviceName = StringUtils.isEmpty(model.getNickName()) ? model.getName() : model.getNickName();
                smbUrlTv.setVisibility(View.VISIBLE);
                smbNameTv.setText(lanDeviceName);
                smbUrlTv.setText(model.getUrl());
                break;
            case Constants.SmbType.FOLDER:
                smbCoverIv.setImageResource(R.mipmap.ic_smb_folder);
                smbUrlTv.setVisibility(View.GONE);
                String folderName = model.getName();
                if (!StringUtils.isEmpty(folderName) && folderName.endsWith("/") && folderName.length() > 2){
                    folderName = folderName.substring(0, folderName.length()-1);
                }
                smbNameTv.setText(folderName);
                break;
            case Constants.SmbType.FILE:
                smbCoverIv.setImageResource(R.mipmap.ic_smb_file);
                smbUrlTv.setVisibility(View.GONE);
                smbNameTv.setText(model.getName());
                break;
        }

        mView.setOnClickListener(view -> {
            if (clickListener != null){
                clickListener.onClick(position);
            }
        });
    }

    public interface OnSmbItemClickListener{
        void onClick(int position);
    }
}