package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.utils.TrackerManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by xyy on 2018/12/6.
 */

public class AddTrackerDialog extends Dialog {

    @BindView(R.id.tracker_et)
    EditText trackerEt;

    private TrackerDialogListener dialogListener;

    public AddTrackerDialog(@NonNull Context context, int themeResId, TrackerDialogListener dialogListener) {
        super(context, themeResId);
        this.dialogListener = dialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_tracker_add);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.cancel_tv, R.id.confirm_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cancel_tv:
                AddTrackerDialog.this.dismiss();
                break;
            case R.id.confirm_tv:
                String trackerText = trackerEt.getText().toString().trim();
                if (!StringUtils.isEmpty(trackerText)){
                    if (!trackerText.contains("\n")){
                        if (IApplication.trackers.contains(trackerText)){
                            ToastUtils.showShort("该tracker已存在");
                            return;
                        }

                        IApplication.trackers.add(trackerText);
                        TrackerManager.addTracker(trackerText);
                        if (dialogListener != null){
                            dialogListener.onChanged();
                        }

                        ToastUtils.showShort("已添加");
                        AddTrackerDialog.this.dismiss();
                    }else {
                        List<String> trackerList = new ArrayList<>();
                        String[] trackers = trackerText.split("\n");
                        for (String tracker : trackers) {
                            tracker = tracker.replace(" ", "");
                            if (IApplication.trackers.contains(tracker)) {
                                continue;
                            }
                            trackerList.add(tracker);
                            IApplication.trackers.add(tracker);
                        }

                        TrackerManager.addTracker(trackerList);
                        if (dialogListener != null){
                            dialogListener.onChanged();
                        }

                        ToastUtils.showShort("已添加");
                        AddTrackerDialog.this.dismiss();
                    }
                }else {
                    ToastUtils.showShort("tracker不能为空");
                }
                break;
        }
    }

    public interface TrackerDialogListener{
        void onChanged();
    }
}
