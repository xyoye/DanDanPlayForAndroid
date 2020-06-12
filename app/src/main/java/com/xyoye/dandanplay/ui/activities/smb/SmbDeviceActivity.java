package com.xyoye.dandanplay.ui.activities.smb;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.SmbDeviceBean;
import com.xyoye.dandanplay.service.SmbService;
import com.xyoye.dandanplay.ui.weight.dialog.CommonDialog;
import com.xyoye.dandanplay.ui.weight.dialog.CommonProgressDialog;
import com.xyoye.dandanplay.ui.weight.dialog.SmbDeviceDialog;
import com.xyoye.dandanplay.ui.weight.dialog.SmbToolsDialog;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.helper.SmbDeviceAction;
import com.xyoye.dandanplay.utils.interf.AdapterItem;
import com.xyoye.smb.SmbManager;
import com.xyoye.smb.info.SmbType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by xyoye on 2019/3/30.
 */

public class SmbDeviceActivity extends BaseMvpActivity<SmbDevicePresenter> implements SmbDeviceView {
    @BindView(R.id.smb_rv)
    RecyclerView smbRv;
    @BindView(R.id.add_device_tv)
    TextView addDeviceTv;
    @BindView(R.id.scan_device_tv)
    TextView scanDeviceTv;
    @BindView(R.id.edit_tv)
    TextView editTv;
    @BindView(R.id.delete_tv)
    TextView deleteTv;

    private BaseRvAdapter<SmbDeviceBean> adapter;
    private List<SmbDeviceBean> smbList;
    private boolean isEdit = false;

    private MenuItem exitEditItem, switchToolsItem;
    private CommonProgressDialog progressDialog;

    private SmbType smbType;

    @NonNull
    @Override
    protected SmbDevicePresenter initPresenter() {
        return new SmbDevicePresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_smb_device;
    }

    @Override
    public void initView() {
        setTitle("局域网");

        smbType = AppConfig.getInstance().getSmbTools();

        smbList = new ArrayList<>();
        adapter = new BaseRvAdapter<SmbDeviceBean>(smbList) {
            @NonNull
            @Override
            public AdapterItem<SmbDeviceBean> onCreateItem(int viewType) {
                return new SmbDeviceItem(new SmbDeviceItem.OnSmbItemClickListener() {
                    @Override
                    public void onClick(int position) {
                        onItemClick(position);
                    }

                    @Override
                    public void onLongClick(int position) {
                        if (isEdit)
                            onItemClick(position);
                        else
                            switchEditMode(true, position);
                    }
                });
            }
        };
        smbRv.setLayoutManager(new GridLayoutManager(this, 4));
        smbRv.setAdapter(adapter);

        progressDialog = new CommonProgressDialog(this);

        smbRv.post(() -> presenter.querySqlDevice());

        //启动共享服务
        Intent intent = new Intent(this, SmbService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    @Override
    public void initListener() {

    }

    @Override
    public void showLoading() {
        showLoadingDialog();
    }

    @Override
    public void hideLoading() {
        dismissLoadingDialog();
    }

    @Override
    public void showError(String message) {
        ToastUtils.showLong(message);
    }


    @OnClick({R.id.add_device_tv, R.id.scan_device_tv, R.id.edit_tv, R.id.delete_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.add_device_tv:
                new SmbDeviceDialog(this, smbType, null, SmbDeviceAction.ACTION_DEVICE_ADD, deviceBean -> {
                    presenter.addSqlDevice(deviceBean);
                    smbList.add(0, deviceBean);
                    adapter.notifyDataSetChanged();
                    presenter.loginSmbDevice(deviceBean, smbType);
                }).show();
                break;
            case R.id.scan_device_tv:
                presenter.queryLanDevice();
                break;
            case R.id.edit_tv: {
                int position = getEditingDevice();
                if (position < 0)
                    return;

                SmbDeviceBean editDeviceBean = smbList.get(position);
                new SmbDeviceDialog(this, smbType, editDeviceBean, SmbDeviceAction.ACTION_DEVICE_EDIT, deviceBean -> {
                    switchEditMode(false, -1);
                    presenter.addSqlDevice(deviceBean);
                    smbList.remove(editDeviceBean);
                    smbList.add(0, deviceBean);
                    adapter.notifyDataSetChanged();
                    presenter.loginSmbDevice(deviceBean, smbType);
                }).show();
                break;
            }
            case R.id.delete_tv:
                int position = getEditingDevice();
                if (position < 0)
                    return;

                new CommonDialog.Builder(this)
                        .setOkListener(dialog -> {
                            String url = smbList.get(position).getUrl();
                            presenter.removeSqlDevice(url);
                            adapter.removeItem(position);

                            if (smbList.size() == 0) {
                                switchEditMode(false, -1);
                            } else if (position < smbList.size()) {
                                smbList.get(position).setEditStatus(true);
                                adapter.notifyItemChanged(position);
                            } else {
                                smbList.get(smbList.size() - 1).setEditStatus(true);
                                adapter.notifyItemChanged(smbList.size() - 1);
                            }
                        })
                        .setCancelListener(CommonDialog::dismiss)
                        .setAutoDismiss()
                        .build()
                        .show("确认移除该设备？");
                break;
        }
    }

    @Override
    public void refreshSqlDevice(List<SmbDeviceBean> deviceList) {
        if (smbRv == null)
            return;
        //设备列表必须为GridLayoutManager
        if (smbRv.getLayoutManager() instanceof LinearLayoutManager) {
            smbRv.setLayoutManager(new GridLayoutManager(this, 4));
        }
        smbList.clear();
        smbList.addAll(deviceList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void showRefreshLanDeviceDialog() {
        progressDialog.show();
        progressDialog.updateTips("扫描设备中");
        progressDialog.updateProgress(0);
        //在所有设备移除扫描到的设备
        Iterator iterator = smbList.iterator();
        while (iterator.hasNext()) {
            SmbDeviceBean smbDeviceBean = (SmbDeviceBean) iterator.next();
            if (smbDeviceBean.getSmbType() == Constants.SmbSourceType.LAN_DEVICE)
                iterator.remove();
        }
        adapter.notifyDataSetChanged();
        //设备列表必须为GridLayoutManager
        if (smbRv.getLayoutManager() instanceof LinearLayoutManager) {
            smbRv.setLayoutManager(new GridLayoutManager(this, 4));
        }
    }

    @Override
    public void hideRefreshLanDeviceDialog() {
        if (progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        Collections.sort(smbList, (o1, o2) -> {
            if (o1.getSmbType() == Constants.SmbSourceType.LAN_DEVICE
                    && o2.getSmbType() != Constants.SmbSourceType.LAN_DEVICE) {
                return 1;
            } else if (o1.getSmbType() != Constants.SmbSourceType.LAN_DEVICE
                    && o2.getSmbType() == Constants.SmbSourceType.LAN_DEVICE) {
                return -1;
            } else {
                return o1.getUrl().compareTo(o2.getUrl());
            }
        });
        adapter.notifyDataSetChanged();
    }

    @Override
    public void addLanDevice(int progress, SmbDeviceBean scanBean) {
        //在扫描设备中移除已有设备
        progressDialog.updateProgress(progress);
        for (SmbDeviceBean smbDeviceBean : smbList) {
            if (smbDeviceBean.getUrl().equals(scanBean.getUrl())) {
                return;
            }
        }
        adapter.addItem(scanBean);
    }

    @Override
    public void loginSuccess() {
        launchActivity(SmbFileActivity.class);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        if (SmbManager.getInstance().getController() != null) {
            IApplication.getExecutor().execute(() ->
                    SmbManager.getInstance().getController().release());
        }
        if (ServiceUtils.isServiceRunning(SmbService.class))
            ServiceUtils.stopService(SmbService.class);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isEdit) {
                switchEditMode(false, -1);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_smb, menu);
        exitEditItem = menu.findItem(R.id.exit_edit_item);
        exitEditItem.setVisible(false);
        switchToolsItem = menu.findItem(R.id.switch_tools_item);
        switchToolsItem.setVisible(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.exit_edit_item) {
            switchEditMode(false, -1);
        } else if (item.getItemId() == R.id.switch_tools_item) {
            new SmbToolsDialog(
                    this,
                    smbType -> SmbDeviceActivity.this.smbType = smbType
            ).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void onItemClick(int position) {
        if (isEdit) {
            for (int i = 0; i < smbList.size(); i++) {
                smbList.get(i).setEditStatus(i == position);
            }
            adapter.notifyDataSetChanged();
            return;
        }

        SmbDeviceBean smbDeviceBean = smbList.get(position);
        switch (smbDeviceBean.getSmbType()) {
            case Constants.SmbSourceType.LAN_DEVICE:
            case Constants.SmbSourceType.SQL_DEVICE:
                //未输入账号名 && 不是匿名登录
                if (StringUtils.isEmpty(smbDeviceBean.getAccount()) && !smbDeviceBean.isAnonymous()) {
                    new SmbDeviceDialog(this, smbType, smbDeviceBean, SmbDeviceAction.ACTION_DEVICE_INIT, deviceBean -> {
                        smbList.remove(position);
                        presenter.addSqlDevice(deviceBean);
                        smbList.add(0, deviceBean);
                        adapter.notifyDataSetChanged();
                        presenter.loginSmbDevice(deviceBean, smbType);
                    }).show();
                } else if (smbType == SmbType.SMBJ && TextUtils.isEmpty(smbDeviceBean.getRootFolder())) {
                    ToastUtils.showLong("请输入需要连接的共享目录");
                    new SmbDeviceDialog(this, smbType, smbDeviceBean, SmbDeviceAction.ACTION_DEVICE_EDIT, deviceBean -> {
                        smbList.remove(position);
                        presenter.addSqlDevice(deviceBean);
                        smbList.add(0, deviceBean);
                        adapter.notifyDataSetChanged();
                        presenter.loginSmbDevice(deviceBean, smbType);
                    }).show();
                } else {
                    presenter.loginSmbDevice(smbDeviceBean, smbType);
                }
                break;
        }
    }

    private int getEditingDevice() {
        for (int i = 0; i < smbList.size(); i++) {
            SmbDeviceBean deviceBean = smbList.get(i);
            if (deviceBean.isEditStatus()) {
                return i;
            }
        }
        return -1;
    }

    private void switchEditMode(boolean isEdit, int position) {
        this.isEdit = isEdit;
        if (isEdit) {
            editTv.setTextColor(CommonUtils.getResColor(R.color.text_black));
            deleteTv.setTextColor(CommonUtils.getResColor(R.color.text_black));
            scanDeviceTv.setTextColor(CommonUtils.getResColor(R.color.text_gray));
            addDeviceTv.setTextColor(CommonUtils.getResColor(R.color.text_gray));
            addDeviceTv.setClickable(false);
            scanDeviceTv.setClickable(false);
            editTv.setClickable(true);
            deleteTv.setClickable(true);

            exitEditItem.setVisible(true);
            switchToolsItem.setVisible(false);

            for (int i = 0; i < smbList.size(); i++) {
                smbList.get(i).setEditStatus(i == position);
            }
            adapter.notifyDataSetChanged();
        } else {
            editTv.setTextColor(CommonUtils.getResColor(R.color.text_gray));
            deleteTv.setTextColor(CommonUtils.getResColor(R.color.text_gray));
            scanDeviceTv.setTextColor(CommonUtils.getResColor(R.color.text_black));
            addDeviceTv.setTextColor(CommonUtils.getResColor(R.color.text_black));
            addDeviceTv.setClickable(true);
            scanDeviceTv.setClickable(true);
            editTv.setClickable(false);
            deleteTv.setClickable(false);

            exitEditItem.setVisible(false);
            switchToolsItem.setVisible(true);

            for (int i = 0; i < smbList.size(); i++) {
                smbList.get(i).setEditStatus(false);
            }
            adapter.notifyDataSetChanged();
        }
    }
}
