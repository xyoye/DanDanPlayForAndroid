package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.blankj.utilcode.util.StringUtils;
import com.xyoye.dandanplay.R;

/**
 * Modified by xyoye on 2015/6/26.
 *
 */
public class BaseLoadingDialog extends Dialog {
    private String msg;

    public BaseLoadingDialog(Context context) {
        super(context, R.style.Dialog);
    }

    public BaseLoadingDialog(Context context, String msg) {
        super(context, R.style.Dialog);
        this.msg = msg;
    }

    public BaseLoadingDialog(Context context, int theme) {
        super(context, theme);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_base_loading);
        if (!StringUtils.isEmpty(msg)){
            this.setCancelable(false);
            TextView textView = this.findViewById(R.id.msg_tv);
            textView.setText(msg);
        }
    }
}
