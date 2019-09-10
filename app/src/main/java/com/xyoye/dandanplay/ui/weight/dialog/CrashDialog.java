package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by xyoye on 2019/9/10.
 */

public class CrashDialog extends Dialog {

    @BindView(R.id.content_tv)
    TextView contentTv;

    private String content;
    private Context context;

    public CrashDialog(@NonNull Context context, String content) {
        super(context, R.style.Dialog);
        this.context = context;
        this.content = content;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFullScreen();
        setContentView(R.layout.dialog_crash);
        ButterKnife.bind(this);

        contentTv.setText(content);
    }

    @OnClick({R.id.dialog_cancel_iv, R.id.copy_log_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.dialog_cancel_iv:
                CrashDialog.this.dismiss();
                break;
            case R.id.copy_log_tv:
                ClipboardManager clipboardManagerMagnet = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipDataMagnet = ClipData.newPlainText("Label", content);
                if (clipboardManagerMagnet != null) {
                    clipboardManagerMagnet.setPrimaryClip(mClipDataMagnet);
                    ToastUtils.showShort("已复制错误日志");
                }
                break;
        }
    }

    private void initFullScreen(){
        Window window = this.getWindow();
        if (window != null){
            View decorView = this.getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }
}
