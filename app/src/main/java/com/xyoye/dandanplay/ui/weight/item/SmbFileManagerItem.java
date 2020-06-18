package com.xyoye.dandanplay.ui.weight.item;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.FileManagerBean;
import com.xyoye.dandanplay.ui.weight.dialog.FileManagerDialog;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import java.io.File;

import butterknife.BindView;

/**
 * Created by xyoye on 2018/7/4.
 */


public class SmbFileManagerItem implements AdapterItem<FileManagerBean> {
    @BindView(R.id.iv)
    ImageView iv;
    @BindView(R.id.tv)
    TextView tv;

    private View mView;
    private FileManagerDialog.OnItemClickListener listener;

    public SmbFileManagerItem(FileManagerDialog.OnItemClickListener listener){
        this.listener = listener;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.item_file_manager;
    }

    @Override
    public void initItemViews(View itemView) {
        mView = itemView;
    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(final FileManagerBean model, int position) {
        iv.setImageResource(R.drawable.ic_xml_file);
        tv.setText(model.getName());

        mView.setOnClickListener(v -> {
            String pathOrName = model.getName();
            listener.onItemClick(pathOrName, false);
        });
    }
}
