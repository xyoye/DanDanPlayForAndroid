package com.xyoye.dandanplay.ui.weight.item;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.DownloadedTaskBean;
import com.xyoye.dandanplay.ui.activities.personal.DownloadManagerActivity;
import com.xyoye.dandanplay.ui.activities.play.DanmuNetworkActivity;
import com.xyoye.dandanplay.ui.activities.play.PlayerManagerActivity;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import java.io.File;

import butterknife.BindView;

/**
 * Created by xyoye on 2019/3/5.
 */

public class TaskDownloadedFileItem implements AdapterItem<DownloadedTaskBean.DownloadedTaskFileBean> {

    @BindView(R.id.file_name_tv)
    TextView fileNameTv;
    @BindView(R.id.file_size_tv)
    TextView fileSizeTv;
    @BindView(R.id.danmu_bind_iv)
    ImageView danmuBindIv;

    private Activity mActivity;
    private View mView;
    private int taskPosition;

    public TaskDownloadedFileItem(int taskPosition, Activity activity) {
        this.taskPosition = taskPosition;
        this.mActivity = activity;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.item_task_downloaded_file;
    }

    @Override
    public void initItemViews(View itemView) {
        mView = itemView;
    }

    @Override
    public void onSetViews() {

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onUpdateViews(DownloadedTaskBean.DownloadedTaskFileBean model, int position) {

        danmuBindIv.setImageResource(StringUtils.isEmpty(model.getDanmuPath())
                ? R.mipmap.ic_danmu_unexists
                : R.mipmap.ic_danmu_exists);

        String fileName = FileUtils.getFileName(model.getFilePath());
        fileNameTv.setText(fileName);

        fileSizeTv.setText(CommonUtils.convertFileSize(model.getFileLength()));

        if (!TextUtils.isEmpty(model.getDanmuPath())){
            File file = new File(model.getDanmuPath());
            if (file.exists() && file.isFile()){
                danmuBindIv.setImageResource(R.mipmap.ic_danmu_exists);
            }
        }

        danmuBindIv.setOnClickListener(v -> {
            if (CommonUtils.isMediaFile(model.getFilePath())) {
                Intent intent = new Intent(mView.getContext(), DanmuNetworkActivity.class);
                intent.putExtra("position", taskPosition);
                intent.putExtra("video_path", model.getFilePath());
                intent.putExtra("task_file_position", position);
                mActivity.startActivityForResult(intent, DownloadManagerActivity.TASK_DOWNLOADED_DANMU_BIND);
            } else {
                ToastUtils.showShort("不支持绑定弹幕的文件格式");
            }
        });

        mView.setOnClickListener(v -> {
            if (CommonUtils.isMediaFile(model.getFilePath())){
                PlayerManagerActivity.launchPlayer(
                        mView.getContext(),
                        fileName,
                        model.getFilePath(),
                        model.getDanmuPath(),
                        0,
                        model.getEpisode_id()
                );
            }else {
                ToastUtils.showShort("不支持播放的文件格式");
            }
        });
    }
}
