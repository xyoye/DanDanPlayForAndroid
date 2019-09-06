package com.xyoye.dandanplay.ui.weight.item;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blankj.utilcode.util.FileUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.interf.AdapterItem;
import com.xyoye.dandanplay.utils.jlibtorrent.TorrentChildFile;

import butterknife.BindView;

/**
 * Created by xyoye on 2019/3/5.
 */

public class TaskDownloadingFileItem implements AdapterItem<TorrentChildFile> {

    @BindView(R.id.file_name_tv)
    TextView fileNameTv;
    @BindView(R.id.duration_tv)
    TextView durationTv;
    @BindView(R.id.download_duration_pb)
    ProgressBar downloadDurationPb;

    public TaskDownloadingFileItem() {

    }

    @Override
    public int getLayoutResId() {
        return R.layout.item_task_downloading_file;
    }

    @Override
    public void initItemViews(View itemView) {

    }

    @Override
    public void onSetViews() {

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onUpdateViews(TorrentChildFile model, int position) {

        String fileName = FileUtils.getFileName(model.getFileName());

        fileNameTv.setText(fileName);

        //文件是否忽略下载
        if (model.isChecked()) {
            fileNameTv.setTextColor(IApplication.get_resource().getColor(R.color.text_black));
            int progress = model.getFileSize() == 0
                    ? 0
                    : (int) (model.getFileReceived() * 100 / model.getFileSize());
            downloadDurationPb.setProgress(progress);

            String duration = CommonUtils.convertFileSize(model.getFileSize()) + "/" + CommonUtils.convertFileSize(model.getFileReceived());
            duration += "  (" + progress + "%)";
            durationTv.setText(duration);
        } else {
            fileNameTv.setTextColor(IApplication.get_resource().getColor(R.color.text_gray));
            downloadDurationPb.setProgress(0);
            durationTv.setText("已忽略");
        }
    }
}
