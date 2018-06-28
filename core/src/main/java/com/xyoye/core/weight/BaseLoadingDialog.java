package com.xyoye.core.weight;

import android.app.Dialog;
import android.content.Context;

import com.xyoye.core.R;

/**
 * Created by Administrator on 2015/6/26.
 *
 */
public class BaseLoadingDialog extends Dialog {

    public BaseLoadingDialog(Context context) {
        super(context, R.style.Dialog_Style);
        setContentView(R.layout.base_loading_dialog);
    }

    public BaseLoadingDialog(Context context, int theme) {
        super(context, theme);
    }

}
