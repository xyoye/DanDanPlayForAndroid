package com.xyoye.dandanplay.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.player.ijkplayer.utils.SoFileUtil;
import com.taobao.sophix.SophixManager;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tencent.bugly.beta.Beta;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.bean.FileDownloadBean;
import com.xyoye.dandanplay.bean.event.PatchFixEvent;
import com.xyoye.dandanplay.bean.params.DownloadSoParam;
import com.xyoye.dandanplay.mvp.impl.SettingPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.SettingPresenter;
import com.xyoye.dandanplay.mvp.view.SettingView;
import com.xyoye.dandanplay.ui.weight.dialog.CommonDialog;
import com.xyoye.dandanplay.ui.weight.dialog.PatchHisDialog;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.net.CommOtherDataObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import butterknife.BindView;
import io.reactivex.disposables.Disposable;

/**
 * Created by YE on 2018/7/24.
 */


public class SettingActivity extends BaseMvpActivity<SettingPresenter> implements SettingView, View.OnClickListener{
    public final static int SELECT_SETTING_FOLDER = 105;

    @BindView(R.id.path_rl)
    RelativeLayout pathRl;
    @BindView(R.id.auto_load_danmu_sw)
    Switch autoLoadDanmuSw;
    @BindView(R.id.bilibili_download_rl)
    RelativeLayout bilibiliDownloadRl;
    @BindView(R.id.extra_so_rl)
    RelativeLayout extraSoRl;
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
    private ProgressDialog dialog;

    @SuppressLint("SetTextI18n")
    @Override
    public void initView() {
        setTitle("设置");
        EventBus.getDefault().register(this);

        String downloadPath = AppConfig.getInstance().getDownloadFolder();
        pathTv.setText(downloadPath);
        version = CommonUtils.getLocalVersion(this);
        versionTv.setText(version);
        if (AppConfig.getInstance().isAutoLoadDanmu()){
            autoLoadDanmuSw.setChecked(true);
        }
        patchTv.setText(AppConfig.getInstance().getPatchVersion()+"");
        dialog = new ProgressDialog(SettingActivity.this);
    }

    @Override
    public void initListener() {
        pathRl.setOnClickListener(this);
        bilibiliDownloadRl.setOnClickListener(this);
        extraSoRl.setOnClickListener(this);
        versionRl.setOnClickListener(this);
        aboutRl.setOnClickListener(this);
        feedbackRl.setOnClickListener(this);
        patchRl.setOnClickListener(this);

        patchRl.setOnLongClickListener(v -> {
            new PatchHisDialog(SettingActivity.this, R.style.Dialog).show();
            return true;
        });

        autoLoadDanmuSw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppConfig.getInstance().setAutoLoadDanmu(isChecked);
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
                Intent intent = new Intent(SettingActivity.this, FileManagerActivity.class);
                intent.putExtra("file_type", FileManagerActivity.DEFAULT_FOLDER);
                startActivityForResult(intent, SELECT_SETTING_FOLDER);
                break;
            case R.id.bilibili_download_rl:
                launchActivity(DownloadBilibiliActivity.class);
                break;
            case R.id.extra_so_rl:
                //armeabi、armeabi-v7a、arm64-v8a、x86、x86_64
                // TODO: 2018/12/23 动态加载so库未成功，还需后续测试
                boolean isLoadExtra = SoFileUtil.getLoadedFile().size() > 0;
                String useSO = !isLoadExtra ? "本地库" : "扩展库";
                new CommonDialog.Builder(SettingActivity.this)
                        .setCancelListener(dialog -> {
                            SPUtils.getInstance().put("use_extra_so", false);
                            new RxPermissions(this).
                                    request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    .subscribe(granted -> {
                                        if (granted) {
                                            loadExtraSo(false);
                                        }
                                    });
                        })
                        .setExtraListener(dialog -> {
                            SPUtils.getInstance().put("use_extra_so", false);
                        })
                        .setAutoDismiss()
                        .hideOk()
                        .setHideCancel(isLoadExtra)
                        .setShowExtra(isLoadExtra)
                        .build()
                        .showExtra("你正在使用的库为："+useSO, "移除扩展库", "加载扩展库");
                break;
            case R.id.version_rl:
                Beta.checkUpgrade(false,false);
                break;
            case R.id.patch_rl:
                SophixManager.getInstance().queryAndLoadNewPatch();
                break;
            case R.id.about_rl:
                Intent intent_about = new Intent(SettingActivity.this, WebviewActivity.class);
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

    /**
     * 加载扩展文件，已存在直接加载，不存在调起下载
     * @param isNet 是否下载后加载
     */
    private void loadExtraSo(boolean isNet){
        String zipSoPath = AppConfig.getInstance().getDownloadFolder()+"/.extra_so/"+Build.CPU_ABI+".zip";
        File zipSoFile = new File(zipSoPath);
        if (zipSoFile.exists()){
            String soZipMd5 = EncryptUtils.encryptMD5File2String(zipSoFile);
            //验证已存在的文件是否为正确文件
            if (SoFileUtil.checkZipSoMd5(soZipMd5)){
                if (SoFileUtil.loadSoFile(this, zipSoPath)){
                    //这里需要让app重新加载so库                    ToastUtils.showShort("加载扩展文件成功");
                    //IjkMediaPlayer.mIsLibLoaded = false;
                    SPUtils.getInstance().put("use_extra_so", true);
                }else {
                    ToastUtils.showShort("加载扩展文件失败："+SoFileUtil.exception);
                }
            }else {
                if (!isNet){
                    downloadExtraSo();
                } else {
                    ToastUtils.showShort("校验下载文件失败");
                }
            }
        }else {
            if (!isNet){
                downloadExtraSo();
            }else {
                ToastUtils.showShort("下载文件不存在");
            }
        }
    }

    /**
     * 下载扩展文件
     */
    private void downloadExtraSo(){
        DownloadSoParam param = new DownloadSoParam();
        param.setAbi(Build.CPU_ABI);
        param.setFolder(AppConfig.getInstance().getDownloadFolder()+"/.extra_so");
        param.setFileName(Build.CPU_ABI+".zip");
        FileDownloadBean.downloadSo(param, new CommOtherDataObserver<FileDownloadBean>() {
            @Override
            public void onSubscribe(Disposable d) {
                super.onSubscribe(d);
                dialog.setMax(100);
                dialog.setMessage("正在下载扩展库");
            }

            @Override
            public void onSuccess(FileDownloadBean bean) {
                dialog.dismiss();
                dialog.setProgress(100);
                new RxPermissions(SettingActivity.this).
                        request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(granted -> {
                            if (granted) {
                                loadExtraSo(true);
                            }
                        });
                ToastUtils.showShort("下载完成，正在加载扩展文件");
            }

            @Override
            public void onError(int errorCode, String message) {
                dialog.dismiss();
            }

            @Override
            public void onProgress(int progress, long total) {
                dialog.setProgress(progress);
            }
        }, new NetworkConsumer());
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == SELECT_SETTING_FOLDER){
                String folderPath = data.getStringExtra("folder");
                pathTv.setText(folderPath);
                AppConfig.getInstance().setDownloadFolder(folderPath);
            }
        }
    }
}
