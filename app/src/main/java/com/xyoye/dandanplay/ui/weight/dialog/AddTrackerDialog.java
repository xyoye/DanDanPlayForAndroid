package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.bean.event.MessageEvent;
import com.xyoye.dandanplay.database.DataBaseInfo;
import com.xyoye.dandanplay.database.DataBaseManager;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by xyy on 2018/12/6.
 */

public class AddTrackerDialog extends Dialog {

    @BindView(R.id.tracker_et)
    EditText trackerEt;
    @BindView(R.id.cancel_rl)
    RelativeLayout cancelRl;
    @BindView(R.id.confirm_rl)
    RelativeLayout confirmRl;

    public AddTrackerDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_tracker_add);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.cancel_rl, R.id.confirm_rl})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cancel_rl:
                AddTrackerDialog.this.dismiss();
                break;
            case R.id.confirm_rl:
                String tracker = trackerEt.getText().toString().trim();
                if (!StringUtils.isEmpty(tracker)){
                    if (IApplication.trackers.contains(tracker)){
                        ToastUtils.showShort("该tracker已存在");
                        return;
                    }
                    SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
                    ContentValues values=new ContentValues();
                    values.put(DataBaseInfo.getFieldNames()[8][1], tracker);
                    sqLiteDatabase.insert(DataBaseInfo.getTableNames()[8], null, values);
                    IApplication.trackers.add(tracker);
                    EventBus.getDefault().post(new MessageEvent(MessageEvent.UPDATE_TRACKER));
                    ToastUtils.showShort("已添加");
                    AddTrackerDialog.this.dismiss();
                }else {
                    ToastUtils.showShort("tracker不能为空");
                }
                break;
        }
    }
}
