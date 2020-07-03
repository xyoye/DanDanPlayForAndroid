package com.xyoye.dandanplay.ui.activities.setting;

import android.annotation.SuppressLint;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvcActivity;
import com.xyoye.dandanplay.ui.weight.dialog.CommonEditTextDialog;
import com.xyoye.dandanplay.utils.jlibtorrent.TorrentConfig;
import com.xyoye.dandanplay.utils.jlibtorrent.TorrentEngine;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by xyoye on 2019/8/16.
 */

public class DownloadSettingActivity extends BaseMvcActivity {

    @BindView(R.id.download_engine_tv)
    TextView downloadEngineTv;
    @BindView(R.id.only_wifi_download_cb)
    CheckBox onlyWifiDownloadCb;
    @BindView(R.id.download_rate_tv)
    TextView downloadRateTv;
    @BindView(R.id.task_count_tv)
    TextView taskCountTv;

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_download_setting;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void initPageView() {
        setTitle("下载设置");

        String engine = TorrentConfig.getInstance().getDownloadEngine();
        downloadEngineTv.setText(engine);

        boolean onlyWifi = TorrentConfig.getInstance().isDownloadOnlyWifi();
        onlyWifiDownloadCb.setChecked(onlyWifi);

        long downloadRate = TorrentConfig.getInstance().getMaxDownloadRate() / 1000;
        if (downloadRate == 0) {
            downloadRateTv.setText("无限制");
        } else {
            downloadRateTv.setText(downloadRate + " k/s");
        }

        int taskCount = TorrentConfig.getInstance().getMaxTaskCount();
        taskCountTv.setText(taskCount + "");
    }

    @Override
    public void initPageViewListener() {
        onlyWifiDownloadCb.setOnCheckedChangeListener((buttonView, isChecked) ->
                TorrentConfig.getInstance().setDownloadOnlyWifi(isChecked));
    }

    @OnClick({R.id.download_engine_rl, R.id.max_download_rate_rl, R.id.max_task_count_rl})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.download_engine_rl:
                //setEngine();
                break;
            case R.id.max_download_rate_rl:
                setMaxDownloadRate();
                break;
            case R.id.max_task_count_rl:
                setMaxTaskCount();
                break;
        }
    }

    /**
     * 设置最大任务数量
     */
    @SuppressLint("SetTextI18n")
    private void setMaxTaskCount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择最大任务数量");
        final String[] engines = {"1", "2", "3", "4", "5"};
        builder.setItems(engines, (dialog, which) -> {
            TorrentConfig.getInstance().setMaxTaskCount(which + 1);
            taskCountTv.setText((which + 1) + "");
            TorrentEngine.getInstance().updateSetting();
        });
        builder.show();
    }


    @SuppressLint("SetTextI18n")
    private void setMaxDownloadRate() {
        new CommonEditTextDialog(this, CommonEditTextDialog.MAX_DOWNLOAD_RATE, result -> {
            long rate = 0;
            try {
                rate = Long.parseLong(result[0]);
            } catch (NumberFormatException ignore){
            }

            if (rate > Integer.MAX_VALUE / 1000){
                rate = Integer.MAX_VALUE / 1000;
            }
            int downloadRate = (int)rate;
            TorrentConfig.getInstance().setMaxDownloadRate(downloadRate * 1000);
            if (downloadRate == 0) {
                downloadRateTv.setText("无限制");
            } else {
                downloadRateTv.setText(downloadRate + " k/s");
            }
            TorrentEngine.getInstance().updateSetting();
        }).show();
    }
}
