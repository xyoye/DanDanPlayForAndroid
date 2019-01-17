package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.PersonalBean;
import com.xyoye.dandanplay.bean.event.ChangeScreenNameEvent;
import com.xyoye.dandanplay.ui.activities.PlayerActivity;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.net.CommJsonEntity;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by YE on 2018/8/11.
 */


public class CommonEditTextDialog extends Dialog {
    @BindView(R.id.edit_layout)
    TextInputLayout inputLayout;
    @BindView(R.id.edit_et)
    EditText editText;
    @BindView(R.id.dialog_title_tv)
    TextView titleTv;

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

        if (type == 0) {
            titleTv.setText("网络串流");
            editText.setHint("https://");
            editText.setMaxLines(5);
        } else {
            titleTv.setText("修改昵称");
            editText.setHint("昵称");
            editText.setMaxLines(1);
        }
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

    @OnClick({R.id.cancel_tv, R.id.confirm_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cancel_tv:
                CommonEditTextDialog.this.dismiss();
                break;
            case R.id.confirm_tv:
                if (type == 0) {
                    if (StringUtils.isEmpty(editText.getText().toString())) {
                        inputLayout.setErrorEnabled(true);
                        inputLayout.setError("链接不能为空");
                    } else {
                        String link = editText.getText().toString();
                        int lastEx = link.lastIndexOf("/") + 1;
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
                } else {
                    //昵称。长度不能超过50个字符，可以使用中文。
                    String screenName = editText.getText().toString();
                    if (StringUtils.isEmpty(screenName)) {
                        inputLayout.setErrorEnabled(true);
                        inputLayout.setError("昵称不能为空");
                        return;
                    }
                    if (screenName.length() > 50) {
                        inputLayout.setErrorEnabled(true);
                        inputLayout.setError("昵称长度过长");
                        return;
                    }
                    changeScreenName(screenName);
                }
                break;
        }
    }
}
