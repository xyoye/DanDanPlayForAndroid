package com.xyoye.dandanplay.ui.weight.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.ShooterSubDetailBean;
import com.xyoye.dandanplay.utils.CommonUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by xyoye on 2020/2/24.
 */

public class ShooterSubDetailDialog extends Dialog {

    @BindView(R.id.subtitle_file_name_et)
    EditText subtitleFileNameEt;
    @BindView(R.id.subtitle_file_size_tv)
    TextView subtitleFileSizeTv;
    @BindView(R.id.subtitle_language_tv)
    TextView subtitleLanguageTv;
    @BindView(R.id.subtitle_source_tv)
    TextView subtitleSourceTv;

    private OnSubtitleDownloadListener listener;
    private ShooterSubDetailBean.SubBean.SubsBean detailBean;

    public ShooterSubDetailDialog(@NonNull Context context, ShooterSubDetailBean.SubBean.SubsBean detailBean, OnSubtitleDownloadListener listener) {
        super(context, R.style.Dialog);
        this.listener = listener;
        this.detailBean = detailBean;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_shooter_subtitle_detail);
        ButterKnife.bind(this);

        String fileName = detailBean.getFilename() == null ? "" : detailBean.getFilename();
        subtitleFileNameEt.setText(fileName);
        subtitleFileNameEt.setSelection(fileName.length());
        subtitleFileSizeTv.setText(CommonUtils.convertFileSize(detailBean.getSize()));
        subtitleLanguageTv.setText(detailBean.getLang() == null ? "无" : detailBean.getLang().getDesc());

        if (detailBean.getProducer() == null || TextUtils.isEmpty(detailBean.getProducer().getSource())) {
            subtitleSourceTv.setVisibility(View.GONE);
        } else {
            subtitleSourceTv.setVisibility(View.VISIBLE);
            subtitleSourceTv.setText("制作: "+detailBean.getProducer().getProducer());
        }
    }

    @OnClick({R.id.cancel_tv, R.id.confirm_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cancel_tv:
                ShooterSubDetailDialog.this.dismiss();
                break;
            case R.id.confirm_tv:
                String fileName = subtitleFileNameEt.getText().toString();
                if (TextUtils.isEmpty(fileName)) {
                    ToastUtils.showShort("字幕文件名不能为空");
                    return;
                }
                String downloadLink = detailBean.getUrl();
                if (TextUtils.isEmpty(downloadLink)) {
                    ToastUtils.showShort("该字幕文件下载链接为空，无法下载");
                    return;
                }
                if (listener != null) {
                    listener.onDownload(fileName, downloadLink);
                }
                break;
        }
    }

    public interface OnSubtitleDownloadListener {
        void onDownload(String fileName, String link);
    }
}
