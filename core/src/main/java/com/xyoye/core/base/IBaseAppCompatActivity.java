package com.xyoye.core.base;

import android.app.Dialog;

import com.jaeger.library.StatusBarUtil;
import com.xyoye.core.R;
import com.xyoye.core.weight.BaseLoadingDialog;

/**
 *
 * Created by xyy on 2017/6/23.
 */
public abstract class IBaseAppCompatActivity extends BaseAppCompatActivity {


    @Override
    protected int getToolbarColor() {
        return this.getResources().getColor(R.color.theme_color);
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getToolbarColor());
    }


    protected Dialog dialog;

    @Override
    public void showLoadingDialog() {
        if (dialog == null || !dialog.isShowing()) {
            dialog = new BaseLoadingDialog(this);
            dialog.show();
        }
    }

    @Override
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
