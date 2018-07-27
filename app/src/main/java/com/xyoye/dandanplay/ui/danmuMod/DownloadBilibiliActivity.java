package com.xyoye.dandanplay.ui.danmuMod;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.ui.webMod.WebviewActivity;
import com.xyoye.dandanplay.utils.BilibiliDownloadUtil;
import com.xyoye.dandanplay.weight.BilibiliDownloadDialog;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by YE on 2018/7/28.
 */

public class DownloadBilibiliActivity extends AppCompatActivity implements View.OnClickListener{
    public final static int SELECT_WEB = 106;
    
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.download_by_av)
    TextView downloadAvTv;
    @BindView(R.id.download_by_url)
    TextView downloadUrlTv;
    @BindView(R.id.select_url_bt)
    TextView selectUrlBt;
    @BindView(R.id.av_input_et)
    EditText avInputEt;
    @BindView(R.id.url_input_et)
    EditText urlInputEt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_bilibili);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar actionBar =  getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        setTitle("");
        toolbarTitle.setText("Bilibili弹幕下载");


        initListener();
    }

    private void initListener(){
        downloadAvTv.setOnClickListener(this);
        downloadUrlTv.setOnClickListener(this);
        selectUrlBt.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.download_by_av:
                String avNumber = avInputEt.getText().toString();
                if (avNumber.isEmpty()){
                    ToastUtils.showShort("AV号不能为空");
                }else if(!BilibiliDownloadUtil.isNum(avNumber)){
                    ToastUtils.showShort("请输入纯数字AV号");
                }else {
                    BilibiliDownloadDialog downloadByAvDialog = new BilibiliDownloadDialog(DownloadBilibiliActivity.this, R.style.Dialog, avNumber, "av");
                    downloadByAvDialog.show();
                }
                break;
            case R.id.download_by_url:
                String urlLink = urlInputEt.getText().toString();
                if (urlLink.isEmpty()){
                    ToastUtils.showShort("视频链接不能为空");
                }else if (!BilibiliDownloadUtil.isUrl(urlLink)){
                    ToastUtils.showShort("请输入正确视频链接");
                }else {
                    BilibiliDownloadDialog downloadByUrlDialog = new BilibiliDownloadDialog(DownloadBilibiliActivity.this, R.style.Dialog, urlLink, "url");
                    downloadByUrlDialog.show();
                }
                break;
            case R.id.select_url_bt:
                Intent intent = new Intent(DownloadBilibiliActivity.this, WebviewActivity.class);
                intent.putExtra("title","选择链接");
                intent.putExtra("link", "http://www.bilibili.com");
                intent.putExtra("isSelect", true);
                startActivityForResult(intent, SELECT_WEB);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_WEB && data!=null){
            String selectUrl = data.getStringExtra("selectUrl");
            if (selectUrl.isEmpty()){
                ToastUtils.showShort( "视频链接不能为空");
            }else if (!BilibiliDownloadUtil.isUrl(selectUrl)){
                ToastUtils.showShort("请输入正确视频链接");
            }else {
                BilibiliDownloadDialog downloadByUrlDialog = new BilibiliDownloadDialog(DownloadBilibiliActivity.this, R.style.Dialog, selectUrl, "url");
                downloadByUrlDialog.show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
