package com.xyoye.dandanplay.base;

import android.app.Dialog;

import com.jaeger.library.StatusBarUtil;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.ui.weight.dialog.BaseLoadingDialog;

/**
 *
 * Created by xyy on 2017/6/23.
 */
public abstract class BaseMvcActivity extends BaseAppCompatActivity {


    @Override
    protected int getToolbarColor() {
        return this.getResources().getColor(R.color.theme_color);
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getToolbarColor());
    }


    protected Dialog dialog;

    public void showLoadingDialog() {
        if (dialog == null || !dialog.isShowing()) {
            dialog = new BaseLoadingDialog(BaseMvcActivity.this);
            dialog.show();
        }
    }

    public void dismissLoadingDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog = null;
    }
}
