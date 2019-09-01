package com.xyoye.dandanplay.ui.weight.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.utils.CommonUtils;

/**
 * Created by xyoye on 2018/11/30.
 */

public class CommonDialog extends Dialog {
    private Context context;
    private View view;
    private AppCompatTextView contentTv, tipsTv, extraTv, okTv, cancelTv;

    private boolean isTouchCancel;
    private boolean isAutoDismiss;
    private boolean isShowTips;
    private boolean isShowExtra;
    private boolean isHideOk;
    private boolean isHideCancel;
    private boolean isNightSkin;
    private onShowListener showListener;
    private onCancelListener cancelListener;
    private onExtraListener extraListener;
    private onOkListener okListener;
    private onDismissListener dismissListener;

    @SuppressLint("InflateParams")
    private CommonDialog(CommonDialog.Builder builder) {
        super(builder.context, R.style.CommonDialog);
        this.context = builder.context;
        this.view = LayoutInflater.from(context).inflate(R.layout.dialog_common, null);
        this.contentTv = view.findViewById(R.id.content_tv);
        this.tipsTv = view.findViewById(R.id.tips_tv);
        this.extraTv = view.findViewById(R.id.extra_tv);
        this.okTv = view.findViewById(R.id.ok_tv);
        this.cancelTv = view.findViewById(R.id.cancel_tv);

        this.isTouchCancel = builder.isTouchCancel;
        this.isAutoDismiss = builder.isAutoDismiss;
        this.isShowTips = builder.isShowTips;
        this.isShowExtra = builder.isShowExtra;
        this.isHideOk = !builder.isHideOk;
        this.isHideCancel = !builder.isHideCancel;
        this.isNightSkin = builder.isNightSkin;
        this.showListener = builder.showListener;
        this.extraListener = builder.extraListener;
        this.cancelListener = builder.cancelListener;
        this.okListener = builder.okListener;
        this.dismissListener = builder.dismissListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(this.view);

        //手动设置夜间模式的弹窗
        if (isNightSkin){
            view.setBackground(CommonUtils.getResDrawable(R.drawable.background_dialog_night));
            tipsTv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_white));
            contentTv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_white));
        }else {
            tipsTv.setTextColor(CommonUtils.getResColor(R.color.text_black));
            contentTv.setTextColor(CommonUtils.getResColor(R.color.text_black));
        }

        okTv.setOnClickListener(v -> {
            if (okListener != null)
                okListener.onOk(CommonDialog.this);
            if (isAutoDismiss)
                CommonDialog.this.dismiss();
        });
        cancelTv.setOnClickListener(v -> {
            if (cancelListener != null)
                cancelListener.onCancel(CommonDialog.this);
            if (isAutoDismiss)
                CommonDialog.this.dismiss();
        });
        extraTv.setOnClickListener(v -> {
            if (extraListener != null)
                extraListener.onExtra(CommonDialog.this);
            if (isAutoDismiss)
                CommonDialog.this.dismiss();
        });
        this.setOnShowListener(dialog1 -> {
            if (showListener != null)
                showListener.onShow(CommonDialog.this);
        });
        this.setOnDismissListener(dialog1 -> {
            if (dismissListener != null)
                dismissListener.onDismiss(CommonDialog.this
            );
        });

        this.setCanceledOnTouchOutside(isTouchCancel);

        tipsTv.setVisibility(isShowTips ? View.VISIBLE : View.GONE);
        extraTv.setVisibility(isShowExtra ?  View.VISIBLE : View.GONE);
        okTv.setVisibility(isHideOk ? View.VISIBLE : View.GONE);
        cancelTv.setVisibility(isHideCancel ? View.VISIBLE : View.GONE);
    }

    public void show(String content){
        contentTv.setText(content);
        if (!((Activity)context).isFinishing())
            CommonDialog.this.show();
    }

    public void show(String content, String ok, String cancel){
        contentTv.setText(content);
        okTv.setText(ok);
        cancelTv.setText(cancel);
        if (!((Activity)context).isFinishing())
            CommonDialog.this.show();
    }

    public void show(String content, String extra){
        contentTv.setText(content);
        extraTv.setText(extra);
        if (!((Activity)context).isFinishing())
            CommonDialog.this.show();
    }

    public void showExtra(String content, String extra, String cancel){
        contentTv.setText(content);
        extraTv.setText(extra);
        cancelTv.setText(cancel);
        if (!((Activity)context).isFinishing())
            CommonDialog.this.show();
    }

    public void show(String content, String tips, String ok, String cancel){
        contentTv.setText(content);
        tipsTv.setText(tips);
        okTv.setText(ok);
        cancelTv.setText(cancel);
        if (!((Activity)context).isFinishing())
            CommonDialog.this.show();
    }

    public static final class Builder {
        private Context context;
        //touch outside dismiss dialog
        private boolean isTouchCancel = true;
        //touch ok or cancel auto dismiss dialog
        private boolean isAutoDismiss = false;
        private boolean isShowTips = true;
        private boolean isShowExtra = false;
        private boolean isHideOk = false;
        private boolean isHideCancel = false;
        private boolean isNightSkin = false;
        private onShowListener showListener;
        private onCancelListener cancelListener;
        private onExtraListener extraListener;
        private onOkListener okListener;
        private onDismissListener dismissListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setTouchNotCancel(){
            this.isTouchCancel = false;
            return this;
        }

        public Builder setAutoDismiss(){
            this.isAutoDismiss = true;
            return this;
        }

        public Builder hideTips(){
            this.isShowTips = false;
            return this;
        }

        public Builder setShowExtra(boolean showExtra){
            this.isShowExtra = showExtra;
            return this;
        }

        public Builder setHideOk(boolean hideOk){
            this.isHideOk = hideOk;
            return this;
        }

        public Builder setHideCancel(boolean Chideancel){
            this.isHideCancel = Chideancel;
            return this;
        }

        public Builder showExtra(){
            this.isShowExtra = true;
            return this;
        }

        public Builder hideOk(){
            this.isHideOk = true;
            return this;
        }

        public Builder hideCancel(){
            this.isHideCancel = true;
            return this;
        }

        public Builder setNightSkin(){
            this.isNightSkin = true;
            return this;
        }

        public Builder setShowListener(onShowListener listener) {
            this.showListener = listener;
            return this;
        }

        public Builder setDismissListener(onDismissListener listener){
            this.dismissListener = listener;
            return this;
        }

        public Builder setExtraListener(onExtraListener listener) {
            this.extraListener = listener;
            return this;
        }

        public Builder setCancelListener(onCancelListener listener){
            this.cancelListener = listener;
            return this;
        }

        public Builder setOkListener(onOkListener listener){
            this.okListener = listener;
            return this;
        }

        public CommonDialog build() {
            return new CommonDialog(this);
        }
    }

    public interface onExtraListener{
        void onExtra(CommonDialog dialog);
    }

    public interface onOkListener{
        void onOk(CommonDialog dialog);
    }

    public interface onCancelListener{
        void onCancel(CommonDialog dialog);
    }

    public interface onDismissListener{
        void onDismiss(CommonDialog dialog);
    }

    public interface onShowListener{
        void onShow(CommonDialog dialog);
    }
}
