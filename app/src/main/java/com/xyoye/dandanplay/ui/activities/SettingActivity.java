package com.xyoye.dandanplay.ui.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.tencent.bugly.beta.Beta;
import com.xyoye.core.base.BaseActivity;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.mvp.impl.SettingPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.SettingPresenter;
import com.xyoye.dandanplay.mvp.view.SettingView;
import com.xyoye.dandanplay.utils.AppConfigShare;
import com.xyoye.dandanplay.utils.FileUtils;

import butterknife.BindView;

/**
 * Created by YE on 2018/7/24.
 */


public class SettingActivity extends BaseActivity<SettingPresenter> implements SettingView, View.OnClickListener{
    public final static int SELECT_SETTING_FOLDER = 105;
    private static final int DIRECTORY_CHOOSE_REQ_CODE = 106;

    @BindView(R.id.path_rl)
    RelativeLayout pathRl;
    @BindView(R.id.auto_load_danmu_sw)
    Switch autoLoadDanmuSw;
    @BindView(R.id.download_rl)
    RelativeLayout downloadRl;
    @BindView(R.id.version_rl)
    RelativeLayout versionRl;
    @BindView(R.id.about_rl)
    RelativeLayout aboutRl;
    @BindView(R.id.feedback_rl)
    RelativeLayout feedbackRl;
    @BindView(R.id.version_tv)
    TextView versionTv;
    @BindView(R.id.download_path_tv)
    TextView pathTv;

    private String version;
    private String SDCardPath;

    @Override
    public void initView() {
        setTitle("设置");

        String downloadPath = AppConfigShare.getInstance().getDownloadFolder();
        if (downloadPath.startsWith(FileUtils.Base_Path)){
            pathTv.setText(downloadPath);
        }else {
            String SDPath = AppConfigShare.getInstance().getSDFolder();
            pathTv.setText(SDPath);
        }
        version = AppConfigShare.getLocalVersion(this);
        versionTv.setText(version);
        if (AppConfigShare.getInstance().isAutoLoadDanmu()){
            autoLoadDanmuSw.setChecked(true);
        }
    }

    @Override
    public void initListener() {
        pathRl.setOnClickListener(this);
        downloadRl.setOnClickListener(this);
        versionRl.setOnClickListener(this);
        aboutRl.setOnClickListener(this);
        feedbackRl.setOnClickListener(this);

        autoLoadDanmuSw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppConfigShare.getInstance().setAutoLoadDanmu(isChecked);
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
            case R.id.download_rl:
                launchActivity(DownloadBilibiliActivity.class);
                break;
            case R.id.version_rl:
                Beta.checkUpgrade(false,false);
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
                        mail_intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"yeshao1997@outlook.com,shine_5402@126.com"});
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == SELECT_SETTING_FOLDER){
                String folderPath = data.getStringExtra("folder");
                //根据系统根目录判断是否为SD卡路径
                if (folderPath.startsWith(FileUtils.Base_Path)){
                    pathTv.setText(folderPath);
                    AppConfigShare.getInstance().setDownloadFolder(folderPath);
                }else {
                    ToastUtils.showShort("SD卡目录需由系统授权，请再次选择目录");
                    SDCardPath = folderPath;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        startActivityForResult(intent, DIRECTORY_CHOOSE_REQ_CODE);
                    }
                }
            } else if(requestCode == DIRECTORY_CHOOSE_REQ_CODE){
                Uri SDCardUri = data.getData();
                if (SDCardUri != null){
                    getContentResolver().takePersistableUriPermission(SDCardUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    pathTv.setText(SDCardPath);
                    AppConfigShare.getInstance().setSDFolder(SDCardPath);
                    AppConfigShare.getInstance().setDownloadFolder(SDCardUri.toString());
                }else {
                    ToastUtils.showShort("未获取SD卡权限，无法设置修改默认下载路径");
                }
            }
        }
    }
}
