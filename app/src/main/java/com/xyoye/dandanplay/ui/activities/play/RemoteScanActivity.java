package com.xyoye.dandanplay.ui.activities.play;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvcActivity;
import com.xyoye.dandanplay.bean.RemoteScanBean;
import com.xyoye.dandanplay.utils.JsonUtils;
import com.xyoye.dandanplay.utils.scan.view.QRCodeReaderView;
import com.xyoye.dandanplay.utils.scan.view.ScanWindowView;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by xyoye on 2019/7/11.
 */

public class RemoteScanActivity extends BaseMvcActivity implements QRCodeReaderView.OnQRCodeReadListener {
    @BindView(R.id.qr_code_reader_view)
    QRCodeReaderView qrCodeReaderView;
    @BindView(R.id.scan_window_view)
    ScanWindowView scanWindowView;
    @BindView(R.id.title_tv)
    TextView titleTv;

    private boolean isScanOver = false;

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_remote_scan;
    }

    @Override
    public void initPageView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setTitle("扫一扫");
        qrCodeReaderView.setAutofocusInterval(2000L);
        qrCodeReaderView.setOnQRCodeReadListener(this);
        qrCodeReaderView.setBackCamera();
        scanWindowView.post(() ->
                qrCodeReaderView.getCameraManager().setFramingRectF(scanWindowView.getRectF()));
    }

    @Override
    public void initPageViewListener() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (qrCodeReaderView != null) {
            qrCodeReaderView.startCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (qrCodeReaderView != null) {
            qrCodeReaderView.stopCamera();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (qrCodeReaderView != null)
            qrCodeReaderView.stopCamera();
    }

    @Override
    public void onQRCodeRead(String result, PointF[] points) {
        if (!TextUtils.isEmpty(result) && !isScanOver) {
            isScanOver = true;
            RemoteScanBean scanBean = JsonUtils.fromJson(result, RemoteScanBean.class);
            if (scanBean != null && scanBean.getIp() != null && scanBean.getIp().size() != 0){
                Intent intent = new Intent(this, RemoteActivity.class);
                intent.putExtra("remote_data", scanBean);
                startActivity(intent);
                RemoteScanActivity.this.finish();
            }else {
                ToastUtils.showShort("错误，无法从该二维码读取远程访问数据");
                isScanOver = false;
            }
        }
    }

    @OnClick({R.id.back_iv})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.back_iv) {
            RemoteScanActivity.this.finish();
        }
    }
}

