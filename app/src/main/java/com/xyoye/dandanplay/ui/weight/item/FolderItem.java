package com.xyoye.dandanplay.ui.weight.item;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.StringUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.FolderBean;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import butterknife.BindView;

/**
 * Created by xyoye on 2018/6/29.
 */

public class FolderItem implements AdapterItem<FolderBean> {
    @BindView(R.id.folder_title)
    TextView folderTitle;
    @BindView(R.id.file_number)
    TextView fileNumber;

    @BindView(R.id.item_layout)
    RelativeLayout contentLayout;
    @BindView(R.id.shield_folder_tv)
    TextView shieldFolderTv;
    @BindView(R.id.delete_folder_tv)
    TextView deleteFolderTv;

    private PlayFolderListener listener;

    public FolderItem(PlayFolderListener listener) {
        if (listener == null)
            throw new NullPointerException("call back not null");
        this.listener = listener;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.item_folder;
    }

    @Override
    public void initItemViews(View itemView) {

    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(FolderBean model, int position) {
        String folder = model.getFolderPath();
        String title = CommonUtils.getFolderName(folder);

        folderTitle.setText(title);
        fileNumber.setText(String.format("%s 视频", model.getFileNumber()));

        //是否为上次播放的文件夹
        boolean isLastPlayFolder = false;
        String lastVideoPath = AppConfig.getInstance().getLastPlayVideo();
        if (!StringUtils.isEmpty(lastVideoPath)) {
            String folderPath = FileUtils.getDirName(lastVideoPath);
            isLastPlayFolder = folderPath.equals(model.getFolderPath());
        }

        folderTitle.setTextColor(isLastPlayFolder
                ? CommonUtils.getResColor(R.color.immutable_text_theme)
                : CommonUtils.getResColor(R.color.text_black));

        fileNumber.setTextColor(isLastPlayFolder
                ? CommonUtils.getResColor(R.color.immutable_text_theme)
                : CommonUtils.getResColor(R.color.text_gray));

        contentLayout.setOnClickListener(v -> listener.onClick(model.getFolderPath()));

        shieldFolderTv.setOnClickListener(v -> listener.onShield(model.getFolderPath(), title));
        deleteFolderTv.setOnClickListener(v -> listener.onDelete(model.getFolderPath(), title));
    }

    public interface PlayFolderListener {
        void onClick(String folderPath);

        void onDelete(String folderPath, String title);

        void onShield(String folderPath, String title);
    }
}
