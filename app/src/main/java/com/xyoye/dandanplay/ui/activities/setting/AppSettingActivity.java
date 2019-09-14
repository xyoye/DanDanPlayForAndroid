package com.xyoye.dandanplay.ui.activities.setting;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RelativeLayout;
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
import com.xyoye.dandanplay.ui.activities.WebViewActivity;
import com.xyoye.dandanplay.ui.activities.personal.FeedbackActivity;
import com.xyoye.dandanplay.ui.weight.dialog.FileManagerDialog;
import com.xyoye.dandanplay.ui.weight.dialog.PatchHisDialog;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.SoUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;

/**
 * Created by xyoye on 2018/7/24.
 */

public class AppSettingActivity extends BaseMvpActivity<SettingPresenter> implements SettingView, View.OnClickListener {
    @BindView(R.id.path_rl)
    RelativeLayout pathRl;
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

    @SuppressLint("SetTextI18n")
    @Override
    public void initView() {
        setTitle("系统设置");
        EventBus.getDefault().register(this);

        String downloadPath = AppConfig.getInstance().getDownloadFolder();
        pathTv.setText(downloadPath);
        String version = SoUtils.getInstance().isOfficialApplication()
                ? CommonUtils.getLocalVersion(this)
                : "非官方应用";
        versionTv.setText(version);
        patchTv.setText(AppConfig.getInstance().getPatchVersion() + "");
    }

    @Override
    public void initListener() {
        pathRl.setOnClickListener(this);
        versionRl.setOnClickListener(this);
        aboutRl.setOnClickListener(this);
        feedbackRl.setOnClickListener(this);
        patchRl.setOnClickListener(this);

        patchRl.setOnLongClickListener(v -> {
            new PatchHisDialog(AppSettingActivity.this, R.style.Dialog).show();
            return true;
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
        switch (v.getId()) {
            case R.id.path_rl:
                new FileManagerDialog(this, FileManagerDialog.SELECT_FOLDER, path -> {
                    pathTv.setText(path);
                    AppConfig.getInstance().setDownloadFolder(path);
                }).hideDefault().show();
                break;
            case R.id.version_rl:
                Beta.checkUpgrade(false, false);
                break;
            case R.id.patch_rl:
                SophixManager.getInstance().queryAndLoadNewPatch();
                break;
            case R.id.about_rl:
                Intent intent_about = new Intent(AppSettingActivity.this, WebViewActivity.class);
                intent_about.putExtra("title", "关于我们");
                intent_about.putExtra("link", "file:///android_asset/DanDanPlay.html");
                startActivity(intent_about);
                break;
            case R.id.feedback_rl:
                launchActivity(FeedbackActivity.class);
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPatchEvent(PatchFixEvent event) {
        ToastUtils.showShort(event.getMsg());
        patchTv.setText(AppConfig.getInstance().getPatchVersion() + "");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
