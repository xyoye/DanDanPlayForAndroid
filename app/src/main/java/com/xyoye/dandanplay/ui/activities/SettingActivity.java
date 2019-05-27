package com.xyoye.dandanplay.ui.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.taobao.sophix.SophixManager;
import com.tencent.bugly.beta.Beta;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.bean.event.PatchFixEvent;
import com.xyoye.dandanplay.mvp.impl.SettingPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.SettingPresenter;
import com.xyoye.dandanplay.mvp.view.SettingView;
import com.xyoye.dandanplay.ui.weight.dialog.FileManagerDialog;
import com.xyoye.dandanplay.ui.weight.dialog.PatchHisDialog;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.CommonUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;

/**
 * Created by xyoye on 2018/7/24.
 */

public class SettingActivity extends BaseMvpActivity<SettingPresenter> implements SettingView, View.OnClickListener{
    @BindView(R.id.path_rl)
    RelativeLayout pathRl;
    @BindView(R.id.cloud_filter_sw)
    Switch cloudFilterSw;
    @BindView(R.id.bilibili_download_rl)
    RelativeLayout bilibiliDownloadRl;
    @BindView(R.id.version_rl)
    RelativeLayout versionRl;
    @BindView(R.id.about_rl)
    RelativeLayout aboutRl;
    @BindView(R.id.patch_rl)
    RelativeLayout patchRl;
    @BindView(R.id.patch_tv)
    TextView patchTv;
    @BindView(R.id.feedback_rl)
    RelativeLayout feedbackRl;
    @BindView(R.id.version_tv)
    TextView versionTv;
    @BindView(R.id.download_path_tv)
    TextView pathTv;

    private String version;

    @SuppressLint("SetTextI18n")
    @Override
    public void initView() {
        setTitle("设置");
        EventBus.getDefault().register(this);

        String downloadPath = AppConfig.getInstance().getDownloadFolder();
        pathTv.setText(downloadPath);
        version = CommonUtils.getLocalVersion(this);
        versionTv.setText(version);
        if (AppConfig.getInstance().isCloudDanmuFilter()){
            cloudFilterSw.setChecked(true);
        }
        patchTv.setText(AppConfig.getInstance().getPatchVersion()+"");
        dialog = new ProgressDialog(SettingActivity.this);
    }

    @Override
    public void initListener() {
        pathRl.setOnClickListener(this);
        bilibiliDownloadRl.setOnClickListener(this);
        versionRl.setOnClickListener(this);
        aboutRl.setOnClickListener(this);
        feedbackRl.setOnClickListener(this);
        patchRl.setOnClickListener(this);

        patchRl.setOnLongClickListener(v -> {
            new PatchHisDialog(SettingActivity.this, R.style.Dialog).show();
            return true;
        });
        cloudFilterSw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppConfig.getInstance().setCloudDanmuFilter(isChecked);
        });
    }

    @NonNull
    @Override
    protected SettingPresenter initPresenter() {
        return new SettingPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_setting;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.path_rl:
                new FileManagerDialog(this, FileManagerDialog.SELECT_FOLDER, path -> {
                    pathTv.setText(path);
                    AppConfig.getInstance().setDownloadFolder(path);
                }).hideDefault().show();
                break;
            case R.id.bilibili_download_rl:
                launchActivity(DownloadBiliBiliActivity.class);
                break;
            case R.id.version_rl:
                Beta.checkUpgrade(false,false);
                break;
            case R.id.patch_rl:
                SophixManager.getInstance().queryAndLoadNewPatch();
                break;
            case R.id.about_rl:
                Intent intent_about = new Intent(SettingActivity.this, WebViewActivity.class);
                intent_about.putExtra("title","关于");
                intent_about.putExtra("link", "file:///android_asset/About_in_application.html");
                startActivity(intent_about);
                break;
            case R.id.feedback_rl:
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("选择反馈方式");
                final String[] ways = {"邮件", "Github Issue"};
                builder.setItems(ways, (dialog, which) -> {
                    if (ways[which].equals("邮件")) {
                        builder.show();
                        String android_version = "Android "+android.os.Build.VERSION.RELEASE;
                        String phone_version = android.os.Build.MODEL;
                        String app_version = getResources().getString(R.string.app_name)+" 版本"+version;

                        Intent mail_intent = new Intent(Intent.ACTION_SEND);
                        mail_intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"yeshao1997@outlook.com"});
                        mail_intent.putExtra(Intent.EXTRA_SUBJECT, "弹弹play - 反馈");
                        mail_intent.putExtra(Intent.EXTRA_TEXT, phone_version+"\n"+android_version+"\n\n"+app_version);
                        mail_intent.setType("text/plain");
                        startActivity(Intent.createChooser(mail_intent, "选择邮件客户端"));
                    }
                    else if (ways[which].equals("Github Issue")){
                        Uri uri = Uri.parse("https://github.com/xyoye/DanDanPlayForAndroid/issues");
                        Intent intent1 = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent1);
                    }
                });
                builder.show();
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPatchEvent(PatchFixEvent event){
        ToastUtils.showShort(event.getMsg());
        patchTv.setText(AppConfig.getInstance().getPatchVersion()+"");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
