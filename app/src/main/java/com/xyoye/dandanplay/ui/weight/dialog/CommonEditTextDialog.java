package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.Context;
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
import com.xyoye.dandanplay.ui.activities.play.PlayerManagerActivity;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.net.CommJsonEntity;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by xyoye on 2018/8/11.
 */


public class CommonEditTextDialog extends Dialog implements Lifeful {
    public static final int NETWORK_LINK = 0;
    public static final int SCREEN_NAME = 1;
    public static final int ADD_BLOCK = 2;
    public static final int REMOTE_TOKEN = 3;
    public static final int MAX_DOWNLOAD_RATE = 4;

    @BindView(R.id.edit_layout)
    TextInputLayout inputLayout;
    @BindView(R.id.edit_et)
    EditText editText;
    @BindView(R.id.dialog_title_tv)
    TextView titleTv;

    private int type;
    private CommonEditTextListener listener;
    private CommonEditTextFullListener fullListener;
    private List<String> blockList;

    public CommonEditTextDialog(@NonNull Context context, int type) {
        super(context, R.style.Dialog);
        this.type = type;
        blockList = new ArrayList<>();
    }

    public CommonEditTextDialog(@NonNull Context context, int type, CommonEditTextListener listener) {
        super(context, R.style.Dialog);
        this.type = type;
        this.listener = listener;
        blockList = new ArrayList<>();
    }

    public CommonEditTextDialog(@NonNull Context context, int type, List<String> blockList, CommonEditTextListener listener) {
        super(context, R.style.Dialog);
        this.type = type;
        this.listener = listener;
        this.blockList = blockList;
    }

    public CommonEditTextDialog(@NonNull Context context, int type, CommonEditTextFullListener listener) {
        super(context, R.style.Dialog);
        this.type = type;
        this.fullListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_common_edittext);
        ButterKnife.bind(this);

        switch (type) {
            case NETWORK_LINK:
                titleTv.setText("网络串流");
                editText.setHint("https://");
                editText.setMaxLines(5);
                break;
            case SCREEN_NAME:
                titleTv.setText("修改昵称");
                editText.setHint("昵称");
                editText.setMaxLines(1);
                break;
            case ADD_BLOCK:
                titleTv.setText("添加屏蔽");
                editText.setHint("屏蔽数据（可以分号为间隔，批量添加）");
                editText.setMaxLines(5);
                break;
            case REMOTE_TOKEN:
                titleTv.setText("远程访问验证");
                editText.setHint("请输入远程访问秘钥");
                editText.setMaxLines(1);
                break;
            case MAX_DOWNLOAD_RATE:
                titleTv.setText("最大下载速度");
                editText.setHint("请输入最大下载速度(MS)");
                editText.setMaxLines(1);
                break;
        }
    }

    private void changeScreenName(String screenName) {
        PersonalBean.changeScreenName(screenName, new CommJsonObserver<CommJsonEntity>(this) {
            @Override
            public void onSuccess(CommJsonEntity commJsonEntity) {
                if (listener != null) {
                    listener.onConfirm(screenName);
                }
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
                if (type == REMOTE_TOKEN && fullListener != null) {
                    fullListener.onCancel();
                }
                CommonEditTextDialog.this.dismiss();
                break;
            case R.id.confirm_tv:
                doConfirm();
                break;
        }
    }

    private void doConfirm() {
        String inputData = editText.getText().toString();
        switch (type) {
            case NETWORK_LINK:
                if (StringUtils.isEmpty(inputData)) {
                    inputLayout.setErrorEnabled(true);
                    inputLayout.setError("链接不能为空");
                } else {
                    int lastEx = inputData.lastIndexOf("/") + 1;
                    String title = inputData;
                    if (lastEx < inputData.length())
                        title = inputData.substring(lastEx);
                    PlayerManagerActivity.launchPlayer(getContext(), title, inputData, "", 0, 0);
                    CommonEditTextDialog.this.dismiss();
                }
                break;
            case SCREEN_NAME:
                //昵称。长度不能超过50个字符，可以使用中文。
                if (StringUtils.isEmpty(inputData)) {
                    inputLayout.setErrorEnabled(true);
                    inputLayout.setError("昵称不能为空");
                    return;
                }
                if (inputData.length() > 50) {
                    inputLayout.setErrorEnabled(true);
                    inputLayout.setError("昵称长度过长");
                    return;
                }
                changeScreenName(inputData);
                break;
            case ADD_BLOCK:
                if (StringUtils.isEmpty(inputData)) {
                    inputLayout.setErrorEnabled(true);
                    inputLayout.setError("屏蔽数据不能为空");
                } else if (blockList.contains(inputData)) {
                    inputLayout.setErrorEnabled(true);
                    inputLayout.setError("当前关键字已屏蔽");
                } else if (traverseBlock(inputData)) {
                    inputLayout.setErrorEnabled(true);
                    inputLayout.setError("当前关键字已在屏蔽范围内");
                } else {
                    if (inputData.endsWith(";")) {
                        inputData = inputData.substring(0, inputData.length() - 1);
                    }
                    if (inputData.startsWith(";")) {
                        inputData = inputData.substring(1);
                    }
                    if (listener != null) {
                        if (inputData.contains(";")) {
                            String[] blockData = inputData.split(";");
                            listener.onConfirm(blockData);
                        } else {
                            listener.onConfirm(inputData);
                        }
                    }
                    CommonEditTextDialog.this.dismiss();
                }
                break;
            case REMOTE_TOKEN:
                if (StringUtils.isEmpty(inputData)) {
                    inputLayout.setErrorEnabled(true);
                    inputLayout.setError("秘钥数据不能为空");
                } else if (fullListener != null) {
                    fullListener.onConfirm(inputData);
                }
                break;
            case MAX_DOWNLOAD_RATE:
                if (StringUtils.isEmpty(inputData)) {
                    inputLayout.setErrorEnabled(true);
                    inputLayout.setError("请输入下载速度");
                } else if (CommonUtils.isNum(inputData)) {
                    inputLayout.setErrorEnabled(true);
                    inputLayout.setError("请输入正确的速度");
                } else if (listener != null) {
                    listener.onConfirm(inputData);
                }
                break;
        }
    }

    private boolean traverseBlock(String blockText) {
        boolean isContains = false;
        for (String text : blockList) {
            if (text.contains(blockText)) {
                isContains = true;
                break;
            }
        }
        return isContains;
    }

    @Override
    public boolean isAlive() {
        return isShowing();
    }

    public interface CommonEditTextListener {
        void onConfirm(String... data);
    }

    public interface CommonEditTextFullListener {
        void onConfirm(String... data);

        void onCancel();
    }
}
