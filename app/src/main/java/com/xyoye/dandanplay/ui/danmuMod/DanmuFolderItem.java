package com.xyoye.dandanplay.ui.danmuMod;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.FileUtils;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.DanmuFolderBean;
import com.xyoye.dandanplay.event.OpenDanmuFolderEvent;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;

/**
 * Created by YE on 2018/7/4 0004.
 */


public class DanmuFolderItem implements AdapterItem<DanmuFolderBean> {
    @BindView(R.id.iv)
    ImageView iv;
    @BindView(R.id.tv)
    TextView tv;

    private View mView;

    @Override
    public int getLayoutResId() {
        return R.layout.item_danmu_folder;
    }

    @Override
    public void initItemViews(View itemView) {
        mView = itemView;
    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(final DanmuFolderBean model, int position) {
        if (model.getFile() == null){
            mView.setVisibility(View.GONE);
            return;
        }

        if (model.isFolder() && model.isParent()){
            iv.setImageResource(R.mipmap.ic_folder_back);
        }else if (model.isFolder() && !model.isParent()){
            iv.setImageResource(R.mipmap.ic_folder);
        }else {
            iv.setImageResource(R.mipmap.ic_xml);
        }

        tv.setText(model.getName());

        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (model.isParent()){
                    String parentPath = FileUtils.getDirName(model.getFile().getAbsolutePath());
                    EventBus.getDefault().post(
                            new OpenDanmuFolderEvent(parentPath, true));
                }else if(model.isFolder()){
                    EventBus.getDefault().post(
                            new OpenDanmuFolderEvent(model.getFile().getAbsolutePath(), true));
                }else {
                    EventBus.getDefault().post(
                            new OpenDanmuFolderEvent(model.getFile().getAbsolutePath(), false));
                }
            }
        });
    }
}
