package com.xyoye.dandanplay.ui.activities;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvcActivity;
import com.xyoye.dandanplay.ui.weight.IWebView;

import butterknife.BindView;

/**
 * Created by xyy on 2018/5/22.
 */

public class WebViewActivity extends BaseMvcActivity {
    @BindView(R.id.i_webview)
    IWebView IwebView;
    @BindView(R.id.toolbar)
    android.support.v7.widget.Toolbar toolbar;

    private boolean isSelectUrl = false;

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_webview;
    }

    @Override
    public void initPageView() {
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String link = intent.getStringExtra("link");
        isSelectUrl = intent.getBooleanExtra("isSelect", false);
        setTitle(title);
        IwebView.loadUrl(link);
    }

    @Override
    public void initPageViewListener() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                WebViewActivity.this.finish();
                break;
            case R.id.select_url:
                if(!StringUtils.isEmpty(IwebView.getUrl())){
                    Intent intent = getIntent();
                    intent.putExtra("selectUrl", IwebView.getUrl());
                    setResult(DownloadBilibiliActivity.SELECT_WEB, intent);
                    WebViewActivity.this.finish();
                }else {
                    ToastUtils.showShort("url不能为空");
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isSelectUrl)
            getMenuInflater().inflate(R.menu.menu_url_select, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPause() {
        IwebView.onPause();
        IwebView.pauseTimers();
        super.onPause();
    }

    @Override
    protected void onResume() {
        IwebView.onResume();
        IwebView.resumeTimers();
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && IwebView.canGoBack()) {
            IwebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
