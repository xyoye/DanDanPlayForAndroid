package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.xyoye.dandanplay.R;

/**
 * Created by xyy on 2018/8/21.
 */


public class DialogUtils {
    private Dialog dialog;
    private AppCompatTextView content;
    private TextView ok, cancel, extra;
    private Context context;

    private DialogUtils(Builder builder){
        context = builder.context;
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_common, null);
        content = view.findViewById(R.id.dialog_content);
        ok = view.findViewById(R.id.dialog_confirm);
        cancel = view.findViewById(R.id.dialog_cancel);
        extra = view.findViewById(R.id.dialog_extra);

        CustomDialog.Builder CBuilder = new CustomDialog.Builder(context);
        dialog  = CBuilder.cancelTouchout(true)
                .view(view)
                .addViewOnclick(R.id.dialog_confirm, v -> {
                    if (builder.okListener != null) builder.okListener.onOk(this);
                })
                .addViewOnclick(R.id.dialog_cancel, v -> {
                    if (builder.cancelListener != null) builder.cancelListener.onCancel(this);
                })
                .addViewOnclick(R.id.dialog_extra, v -> {
                    if (builder.extraListener != null) builder.extraListener.onAction(this);
                })
                .style(R.style.CommonDialog)
                .build();

        dialog.setOnShowListener(dialog1 -> {
            if (builder.showListener != null) builder.showListener.onShow();
        });
        dialog.setOnDismissListener(dialog1 -> {
            if (builder.dismissListener != null) builder.dismissListener.onDismiss();
        });
    }

    public void show(String text){
        content.setText(text);
        cancel.setVisibility(View.VISIBLE);
        ok.setVisibility(View.VISIBLE);
        if (!((Activity)context).isFinishing())
            dialog.show();
    }

    public void show(String text, boolean cancelVis, boolean okVis){
        content.setText(text);
        cancel.setVisibility(cancelVis ? View.VISIBLE : View.GONE);
        ok.setVisibility(okVis ? View.VISIBLE : View.GONE);
        if (!((Activity)context).isFinishing())
            dialog.show();
    }

    public void show(String text, String okText, String cancelText){
        content.setText(text);
        cancel.setVisibility(View.VISIBLE);
        ok.setVisibility(View.VISIBLE);
        ok.setText(okText);
        cancel.setText(cancelText);
        if (!((Activity)context).isFinishing())
            dialog.show();
    }

    public void show(String extra, String content, boolean cancelVis, boolean okVis){
        DialogUtils.this.extra.setText(extra);
        DialogUtils.this.content.setText(content);
        ok.setVisibility( okVis ? View.VISIBLE : View.GONE);
        cancel.setVisibility( cancelVis ? View.VISIBLE : View.GONE);
        DialogUtils.this.extra.setVisibility(View.VISIBLE);
        if (!((Activity)context).isFinishing())
            dialog.show();
    }

    public void dismiss(){
        dialog.dismiss();
    }

    public static final class Builder {
        private Context context;
        private onDialogShowListener showListener;
        private onDialogCancelListener cancelListener;
        private onDialogOkListener okListener;
        private onDialogDismissListener dismissListener;
        private onDialogExtraListener extraListener;

        public Builder(Context context){
            this.context = context;
        }

        public Builder setShowListener(onDialogShowListener listener) {
            this.showListener = listener;
            return this;
        }

        public Builder setDismissListener(onDialogDismissListener listener){
            this.dismissListener = listener;
            return this;
        }

        public Builder setCancelListener(onDialogCancelListener listener){
            this.cancelListener = listener;
            return this;
        }

        public Builder setOkListener(onDialogOkListener listener){
            this.okListener = listener;
            return this;
        }

        public Builder setExtraListener(onDialogExtraListener listener){
            this.extraListener = listener;
            return this;
        }

        public DialogUtils build() {
            return new DialogUtils(this);
        }
    }

    public interface onDialogOkListener{
        void onOk(DialogUtils dialog);
    }

    public interface onDialogCancelListener{
        void onCancel(DialogUtils dialog);
    }

    public interface onDialogExtraListener{
        void onAction(DialogUtils dialog);
    }

    public interface onDialogDismissListener{
        void onDismiss();
    }

    public interface onDialogShowListener{
        void onShow();
    }
}
