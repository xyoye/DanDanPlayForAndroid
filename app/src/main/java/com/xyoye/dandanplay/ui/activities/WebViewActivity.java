package com.xyoye.dandanplay.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvcActivity;
import com.xyoye.dandanplay.ui.weight.ProgressView;

import butterknife.BindView;

/**
 * Created by xyoye on 2019/3/30.
 */

public class WebViewActivity extends BaseMvcActivity {

    @BindView(R.id.web_view)
    WebView mWebView;
    @BindView(R.id.toolbar)
    android.support.v7.widget.Toolbar toolbar;

    private ProgressView progressView;      //进度条
    private boolean isSelectUrl = false;    //选择url模式

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

        initView();

        initWebSetting();

        mWebView.setWebViewClient(mWebViewClient);
        mWebView.setWebChromeClient(mWebChromeClient);
        mWebView.loadUrl(link);
    }

    @Override
    public void initPageViewListener() {

    }

    private void initView() {
        //初始化进度条
        progressView = new ProgressView(this);
        progressView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ConvertUtils.dp2px(4)));
        progressView.setColor(Color.BLUE);
        progressView.setProgress(10);
        //把进度条加到WebView中
        mWebView.addView(progressView);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebSetting(){
        WebSettings webSetting = mWebView.getSettings();
        webSetting.setJavaScriptEnabled(true);
        //  WebSettings.LOAD_DEFAULT 如果本地缓存可用且没有过期则使用本地缓存，否加载网络数据 默认值
        //  WebSettings.LOAD_CACHE_ELSE_NETWORK 优先加载本地缓存数据，无论缓存是否过期
        //  WebSettings.LOAD_NO_CACHE  只加载网络数据，不加载本地缓存
        //  WebSettings.LOAD_CACHE_ONLY 只加载缓存数据，不加载网络数据
        //Tips:有网络可以使用LOAD_DEFAULT 没有网时用LOAD_CACHE_ELSE_NETWORK
        webSetting.setCacheMode(android.webkit.WebSettings.LOAD_NO_CACHE);
        webSetting.setAppCacheEnabled(true);    //开启H5(APPCache)缓存功能websettings.setAppCacheMaxSize(1024*1024*8);
        String appCachePath = this.getApplicationContext().getCacheDir().getAbsolutePath();
        webSetting.setAppCachePath(appCachePath);
        //开启 DOM storage API 功能 较大存储空间，使用简单
        webSetting.setDomStorageEnabled(true);
        //开启 Application Caches 功能 方便构建离线APP 不推荐使用
        webSetting.setAppCacheEnabled(true);
        //支持通过JS打开新窗口
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        //视图适应窗口
//        webSetting.setUseWideViewPort(true);
//        webSetting.setLoadWithOverviewMode(true);
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        //允许 WebView 使用 File 协议
        webSetting.setAllowFileAccess(true);
        //允许webview对文件的操作
        webSetting.setAllowUniversalAccessFromFileURLs(true);
        webSetting.setAllowFileAccessFromFileURLs(true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
                return true;
            } else {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                WebViewActivity.this.finish();
                break;
            case R.id.select_url:
                if (!StringUtils.isEmpty(mWebView.getUrl())) {
                    Intent intent = getIntent();
                    intent.putExtra("selectUrl", mWebView.getUrl());
                    setResult(RESULT_OK, intent);
                    WebViewActivity.this.finish();
                } else {
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
    protected void onDestroy() {
        super.onDestroy();
        //防止webView内存泄漏
        if (mWebView != null) {
            mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            mWebView.clearHistory();
            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            mWebView.destroy();
            mWebView = null;
        }
    }

    WebViewClient mWebViewClient = new WebViewClient() {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    };

    WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                //加载完毕进度条消失
                progressView.setVisibility(View.GONE);
            } else {
                //更新进度
                progressView.setProgress(newProgress);
            }
            super.onProgressChanged(view, newProgress);
        }
    };
}
