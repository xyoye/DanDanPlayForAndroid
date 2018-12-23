package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.PersonalBean;
import com.xyoye.dandanplay.bean.event.ChangeScreenNameEvent;
import com.xyoye.dandanplay.ui.activities.FolderActivity;
import com.xyoye.dandanplay.ui.activities.PlayerActivity;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.net.CommJsonEntity;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by YE on 2018/8/11.
 */


public class CommonEditTextDialog extends Dialog {
    @BindView(R.id.edit_layout)
    TextInputLayout inputLayout;
    @BindView(R.id.edit_et)
    EditText editText;
    @BindView(R.id.confirm_bt)
    Button confirmBt;
    @BindView(R.id.tips)
    TextView tips;

    private int type;

    public CommonEditTextDialog(@NonNull Context context, int themeResId, int type) {
        super(context, themeResId);
        this.type = type;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_common_edittext);
        ButterKnife.bind(this);

        if (type == 0){
            tips.setText("网络串流");
            editText.setHint("https://");
            editText.setMaxLines(5);
        }else {
            tips.setText("修改昵称");
            editText.setHint("昵称");
            editText.setMaxLines(1);
        }

        confirmBt.setOnClickListener(v -> {
            if (type == 0){
                if (StringUtils.isEmpty(editText.getText().toString())) {
                    inputLayout.setErrorEnabled(true);
                    inputLayout.setError("链接不能为空");
                } else {
                    String link = editText.getText().toString();
                    int lastEx = link.lastIndexOf("/")+1;
                    String title = link;
                    if (lastEx < link.length())
                        title = link.substring(lastEx, link.length());
                    Intent intent = new Intent(getContext(), PlayerActivity.class);
                    intent.putExtra("title", title);
                    intent.putExtra("path", link);
                    intent.putExtra("danmu_path", "");
                    intent.putExtra("current", 0);
                    intent.putExtra("episode_id", "");
                    getContext().startActivity(intent);
                    CommonEditTextDialog.this.dismiss();
                }
            }else {
                if (StringUtils.isEmpty(editText.getText().toString())) {
                    inputLayout.setErrorEnabled(true);
                    inputLayout.setError("昵称不能为空");
                } else {
                    String screenName = editText.getText().toString();
                    changeScreenName(screenName);
                }
            }
        });
    }

    private void changeScreenName(String screenName) {
        PersonalBean.changeScreenName(screenName, new CommJsonObserver<CommJsonEntity>() {
            @Override
            public void onSuccess(CommJsonEntity commJsonEntity) {
                ToastUtils.showShort("修改昵称成功");
                AppConfig.getInstance().saveUserScreenName(screenName);
                EventBus.getDefault().post(new ChangeScreenNameEvent(screenName));
                CommonEditTextDialog.this.cancel();
            }

            @Override
            public void onError(int errorCode, String message) {
                LogUtils.e(message);
                ToastUtils.showShort(message);
            }
        }, new NetworkConsumer());
    }
}
