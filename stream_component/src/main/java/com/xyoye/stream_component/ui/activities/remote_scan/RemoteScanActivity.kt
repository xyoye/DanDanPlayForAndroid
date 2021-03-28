package com.xyoye.stream_component.ui.activities.remote_scan

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.widget.FrameLayout
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.gyf.immersionbar.ImmersionBar
import com.huawei.hms.hmsscankit.RemoteView
import com.huawei.hms.ml.scan.HmsScan
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.common_component.utils.getScreenHeight
import com.xyoye.common_component.utils.getScreenWidth
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.data.RemoteScanData
import com.xyoye.stream_component.BR
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.ActivityRemoteScanBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

@Route(path = RouteTable.Stream.RemoteScan)
class RemoteScanActivity : BaseActivity<RemoteScanViewModel, ActivityRemoteScanBinding>() {

    private lateinit var remoteView: RemoteView

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            RemoteScanViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_remote_scan

    override fun initStatusBar() {
        //do nothing
    }

    override fun initView() {
        ImmersionBar.with(this)
            .titleBar(dataBinding.toolbar, false)
            .transparentBar()
            .statusBarDarkFont(false)
            .init()

        title = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        remoteView = RemoteView.Builder()
            .setContext(this)
            .setBoundingBox(getScanRect())
            .setFormat(HmsScan.QRCODE_SCAN_TYPE)
            .build()
            .apply {
                onCreate(savedInstanceState)
                setOnResultCallback { result ->
                    if (result?.isNotEmpty() == true) {
                        if (result[0]?.originalValue?.isNotEmpty() == true) {
                            onScanResult(result[0].originalValue)
                        }
                    }
                }
            }

        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        dataBinding.scanContainer.addView(remoteView, layoutParams)
    }

    override fun onStart() {
        remoteView.onStart()
        super.onStart()
    }

    override fun onResume() {
        remoteView.onResume()
        super.onResume()
    }

    override fun onPause() {
        remoteView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        remoteView.onDestroy()
        super.onDestroy()
    }

    override fun onStop() {
        remoteView.onStop()
        super.onStop()
    }

    private fun onScanResult(scanResult: String) {
        var result = scanResult

        remoteView.pauseContinuouslyScan()

        //去除不可见字符
        if (!result.startsWith("{")) {
            result = result.substring(1)
        }

        //解析Json
        val remoteScanBean = JsonHelper.parseJson<RemoteScanData>(result)
        if (remoteScanBean == null) {
            lifecycleScope.launch {
                ToastCenter.showOriginalToast("无法识别的二维码")
                delay(1000L)
                remoteView.resumeContinuouslyScan()
            }
            return
        }

        //仅存在一个IP
        if (remoteScanBean.ip.size == 1) {
            remoteScanBean.selectedIP = remoteScanBean.ip[0]
            val intent = Intent()
            intent.putExtra("scan_data", remoteScanBean)
            setResult(RESULT_OK, intent)
            finish()
            return
        }

        //多个IP，选择其中一个
    }

    private fun getScanRect(): Rect {
        val width = getScreenWidth()
        val height = getScreenHeight()

        val frameSize = (min(width, height)) / 5f * 4f

        val bottom = (max(width, height) - frameSize) / 2f
        val right = (width - frameSize) / 2f
        val left = right + frameSize
        val top = bottom + frameSize

        return Rect(right.toInt(), bottom.toInt(), left.toInt(), top.toInt())
    }
}