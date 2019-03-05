package com.xyoye.dandanplay.ui.weight.item;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.FileUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.FolderBean;
import com.xyoye.dandanplay.bean.event.DeleteFolderEvent;
import com.xyoye.dandanplay.bean.event.OpenFolderEvent;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import org.greenrobot.eventbus.EventBus;

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
    private Context mContext;

    @Override
    public int getLayoutResId() {
        return R.layout.item_folder;
    }

    @Override
    public void initItemViews(View itemView) {
        mView = itemView;
        mContext = mView.getContext();
    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(FolderBean model, int position) {
        String folder = model.getFolderPath();
        String title = FileUtils.getFileNameNoExtension(folder.substring(0, folder.length()-1));

        folderTitle.setText(title);
        fileNumber.setText(String.valueOf(model.getFileNumber() + " 视频"));

        //是否为上次播放的文件夹
        boolean isLastPlayFolder = AppConfig.getInstance().getLastPlayPath(true).equals(model.getFolderPath());

        folderTitle.setTextColor(isLastPlayFolder
                ? mContext.getResources().getColor(R.color.theme_color)
                : mContext.getResources().getColor(R.color.text_black));

        fileNumber.setTextColor(isLastPlayFolder
                ? mContext.getResources().getColor(R.color.theme_color)
                : mContext.getResources().getColor(R.color.text_gray));

        mView.setOnClickListener(v -> EventBus.getDefault().post(new OpenFolderEvent(model.getFolderPath())));

        mView.setOnLongClickListener(v -> {
            EventBus.getDefault().post(new DeleteFolderEvent(model.getFolderPath(), position));
            return true;
        });
    }
}
