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
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.SmbDeviceBean;
import com.xyoye.dandanplay.service.SmbService;
import com.xyoye.dandanplay.ui.weight.dialog.SmbDeviceDialog;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.helper.SmbDeviceAction;
import com.xyoye.dandanplay.utils.interf.AdapterItem;
import com.xyoye.libsmb.SmbManager;

import java.util.ArrayList;
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
    @BindView(R.id.edit_tv)
    TextView editTv;
    @BindView(R.id.delete_tv)
    TextView deleteTv;

    private BaseRvAdapter<SmbDeviceBean> adapter;
    private List<SmbDeviceBean> smbList;
    private boolean isEdit = false;

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
                        onItemLongClick(position);
                    }
                });
            }
        };
        smbRv.setLayoutManager(new GridLayoutManager(this, 4));
        smbRv.setAdapter(adapter);

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
                new SmbDeviceDialog(this, null, SmbDeviceAction.ACTION_DEVICE_ADD, deviceBean -> {
                    presenter.addSqlDevice(deviceBean);
                    smbList.add(0, deviceBean);
                    adapter.notifyDataSetChanged();
                    presenter.loginSmbDevice(deviceBean);
                }).show();
                break;
            case R.id.scan_device_tv:
                presenter.queryLanDevice();
                break;
            case R.id.edit_tv:
                SmbDeviceBean checkedBean = null;
                int checkedPosition = -1;
                for (int i = 0; i < smbList.size(); i++) {
                    if (smbList.get(i).isEditStatus()) {
                        checkedBean = smbList.get(i);
                        checkedPosition = i;
                        break;
                    }
                }
                if (!isEdit || checkedBean == null)
                    return;
                final int removePosition = checkedPosition;
                new SmbDeviceDialog(this, checkedBean, SmbDeviceAction.ACTION_DEVICE_EDIT, deviceBean -> {
                    presenter.addSqlDevice(deviceBean);
                    smbList.remove(removePosition);
                    smbList.add(0, deviceBean);
                    adapter.notifyDataSetChanged();
                    presenter.loginSmbDevice(deviceBean);
                }).show();
                break;
            case R.id.delete_tv:
                String ip = null;
                for (SmbDeviceBean deviceBean : smbList) {
                    if (deviceBean.isEditStatus()) {
                        ip = deviceBean.getUrl();
                        break;
                    }
                }
                if (!isEdit || TextUtils.isEmpty(ip))
                    return;
                presenter.removeSqlDevice(ip);
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
    public void refreshLanDevice(List<SmbDeviceBean> deviceList) {
        hideLoading();
        //在所有设备移除扫描到的设备
        Iterator iterator = smbList.iterator();
        while (iterator.hasNext()) {
            SmbDeviceBean smbDeviceBean = (SmbDeviceBean) iterator.next();
            if (smbDeviceBean.getSmbType() == Constants.SmbType.LAN_DEVICE)
                iterator.remove();
        }

        //在扫描设备中移除已有设备
        Iterator scanIterator = deviceList.iterator();
        while (scanIterator.hasNext()) {
            SmbDeviceBean scanBean = (SmbDeviceBean) scanIterator.next();
            for (SmbDeviceBean smbDeviceBean : smbList) {
                if (smbDeviceBean.getUrl().equals(scanBean.getUrl())) {
                    scanIterator.remove();
                    break;
                }
            }
        }
        //设备列表必须为GridLayoutManager
        if (smbRv.getLayoutManager() instanceof LinearLayoutManager) {
            smbRv.setLayoutManager(new GridLayoutManager(this, 4));
        }
        smbList.addAll(deviceList);
        adapter.notifyDataSetChanged();
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
        SmbManager.getInstance().getController().release();
        if (ServiceUtils.isServiceRunning(SmbService.class))
            ServiceUtils.stopService(SmbService.class);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            if (isEdit) {
                isEdit = false;
                editTv.setTextColor(CommonUtils.getResColor(R.color.text_gray));
                deleteTv.setTextColor(CommonUtils.getResColor(R.color.text_gray));
                for (int i = 0; i < smbList.size(); i++) {
                    smbList.get(i).setEditStatus(false);
                }
                adapter.notifyDataSetChanged();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
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
            case Constants.SmbType.LAN_DEVICE:
            case Constants.SmbType.SQL_DEVICE:
                //未输入账号名 && 不是匿名登录
                if (StringUtils.isEmpty(smbDeviceBean.getAccount()) && !smbDeviceBean.isAnonymous()) {
                    new SmbDeviceDialog(this, smbDeviceBean, SmbDeviceAction.ACTION_DEVICE_INIT, deviceBean -> {
                        smbList.remove(position);
                        presenter.addSqlDevice(deviceBean);
                        smbList.add(0, deviceBean);
                        adapter.notifyDataSetChanged();
                        presenter.loginSmbDevice(smbDeviceBean);
                    }).show();
                    return;
                }
                presenter.loginSmbDevice(smbDeviceBean);
                break;
        }
    }

    private void onItemLongClick(int position) {
        for (int i = 0; i < smbList.size(); i++) {
            smbList.get(i).setEditStatus(i == position);
        }
        adapter.notifyDataSetChanged();
        isEdit = true;
        editTv.setTextColor(CommonUtils.getResColor(R.color.text_black));
        deleteTv.setTextColor(CommonUtils.getResColor(R.color.text_black));
    }
}
