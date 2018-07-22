package com.xyoye.dandanplay.ui.playMod;

import android.view.View;
import android.widget.TextView;

import com.xyoye.core.interf.AdapterItem;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.FolderBean;
import com.xyoye.dandanplay.event.OpenFolderEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import butterknife.BindView;

/**
 * Created by YE on 2018/6/29 0029.
 */

public class FolderItem implements AdapterItem<FolderBean>{
    @BindView(R.id.folder_title)
    TextView folderTitle;
    @BindView(R.id.file_number)
    TextView fileNumber;

    private View mView;

    @Override
    public int getLayoutResId() {
        return R.layout.item_folder;
    }

    @Override
    public void initItemViews(View itemView) {
        mView = itemView;
    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(FolderBean model, int position) {
        final String realPath = model.getFolderPath();
        String path = realPath.substring(0, realPath.length()-1);
        int last = path.lastIndexOf("/")+1;
        final String title = path.substring(last);
        if (last != 0){
            folderTitle.setText(path.substring(last));
            fileNumber.setText(String.valueOf(model.getFileNumber() + " 视频"));
        }

        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new OpenFolderEvent(title,realPath));
            }
        });
    }
}
