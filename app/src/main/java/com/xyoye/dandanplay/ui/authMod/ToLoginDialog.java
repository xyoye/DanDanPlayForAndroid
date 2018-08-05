package com.xyoye.dandanplay.ui.authMod;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Button;

import com.xyoye.core.base.AppManager;
import com.xyoye.dandanplay.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by YE on 2018/8/5.
 */


public class ToLoginDialog extends Dialog {
    @BindView(R.id.to_login_bt)
    Button toLogin;

    public ToLoginDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_to_login);
        ButterKnife.bind(this, this);

        toLogin.setOnClickListener(v -> {
            ToLoginDialog.this.cancel();
            AppManager.finishActivity(RegisterActivity.class);
        });
    }
}
