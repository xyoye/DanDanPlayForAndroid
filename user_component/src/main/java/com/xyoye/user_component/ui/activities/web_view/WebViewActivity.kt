package com.xyoye.user_component.ui.activities.web_view

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isGone
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.toResColor
import com.xyoye.common_component.network.config.Api
import com.xyoye.common_component.utils.dp2px
import com.xyoye.user_component.BR
import com.xyoye.user_component.R
import com.xyoye.user_component.databinding.ActivityWebViewBinding
import com.xyoye.user_component.ui.weight.WebViewProgress

@SuppressLint("SetJavaScriptEnabled")
@Route(path = RouteTable.User.WebView)
class WebViewActivity : BaseActivity<WebViewViewModel, ActivityWebViewBinding>() {

    @JvmField
    @Autowired
    var titleText: String = ""

    @JvmField
    @Autowired
    var url: String? = ""

    @JvmField
    @Autowired
    var isSelectMode: Boolean = false

    private lateinit var progressView: WebViewProgress

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            WebViewViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_web_view

    override fun initView() {
        ARouter.getInstance().inject(this)

        val realUrl = url
        if (realUrl.isNullOrEmpty()) {
            finish()
            return
        }

        title = titleText

        //进度条
        progressView = WebViewProgress(this).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(4))
            setColor(R.color.text_blue.toResColor())
            setProgress(10)
        }

        dataBinding.webView.apply {
            addView(progressView)
            settings.apply {
                allowFileAccess = true
                javaScriptEnabled = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_NO_CACHE
                javaScriptCanOpenWindowsAutomatically = true
                allowUniversalAccessFromFileURLs = true
            }
            webViewClient = object : WebViewClient() {

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val uri = request?.url ?: return false

                    // 选取b站视频时，考虑将APP协议地址转换为普通番剧地址
                    val bangumiUrl = considerMapBangumiUrl(uri)
                    if (bangumiUrl != null && bangumiUrl != view?.url) {
                        view?.loadUrl(bangumiUrl)
                        return true
                    }

                    // 非http(s)协议资源，不加载
                    if (uri.scheme?.startsWith("http") != true) {
                        return true
                    }

                    return false
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    if (newProgress == 100) {
                        //加载完毕进度条消失
                        progressView.isGone = true
                    } else {
                        //更新进度
                        progressView.setProgress(newProgress)
                    }
                    super.onProgressChanged(view, newProgress)
                }
            }
        }

        dataBinding.webView.loadUrl(realUrl)
    }

    /**
     * 将BiliBili APP协议地址转换为普通番剧地址
     */
    private fun considerMapBangumiUrl(schemeUri: Uri): String? {
        if (isSelectMode.not()) {
            return null
        }

        val isAppBangumiUrl = schemeUri.scheme == "bilibili"
                && schemeUri.host == "bangumi"
                && schemeUri.pathSegments.firstOrNull() == "season"
        if (isAppBangumiUrl.not()) {
            return null
        }

        val seasonId = schemeUri.pathSegments.lastOrNull()
        if (seasonId.isNullOrEmpty()) {
            return null
        }

        return Uri.parse(Api.BILI_BILI_M)
            .buildUpon()
            .appendPath("bangumi")
            .appendPath("play")
            .appendPath("ss$seasonId")
            .build()
            .toString()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (isSelectMode) {
            menuInflater.inflate(R.menu.menu_web_view, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.item_select_url) {
            val url = dataBinding.webView.url
            val intent = Intent()
            intent.putExtra("url_data", url)
            setResult(RESULT_OK, intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (dataBinding.webView.canGoBack()) {
                dataBinding.webView.goBack()
                return true
            } else {
                finish()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        dataBinding.webView.apply {
            loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
            clearHistory()
            (parent as ViewGroup).removeView(this)
            destroy()
        }
    }
}