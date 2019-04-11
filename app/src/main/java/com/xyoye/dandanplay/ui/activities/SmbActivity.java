package com.xyoye.dandanplay.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.SmbBean;
import com.xyoye.dandanplay.mvp.impl.SmbPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.SmbPresenter;
import com.xyoye.dandanplay.mvp.view.SmbView;
import com.xyoye.dandanplay.service.SmbService;
import com.xyoye.dandanplay.ui.weight.dialog.SmbDialog;
import com.xyoye.dandanplay.ui.weight.item.SmbItem;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by YE on 2019/3/30.
 */

public class SmbActivity extends BaseMvpActivity<SmbPresenter> implements SmbView {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.path_tv)
    TextView pathTv;
    @BindView(R.id.path_rl)
    RelativeLayout pathRl;
    @BindView(R.id.smb_rv)
    RecyclerView smbRv;

    private BaseRvAdapter<SmbBean> adapter;
    private List<SmbBean> smbList;
    private MenuItem deviceAddItem, deviceScanItem;

    @NonNull
    @Override
    protected SmbPresenter initPresenter() {
        return new SmbPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_smb;
    }

    @Override
    public void initView() {
        setTitle("局域网");
        smbList = new ArrayList<>();
        adapter = new BaseRvAdapter<SmbBean>(smbList) {
            @NonNull
            @Override
            public AdapterItem<SmbBean> onCreateItem(int viewType) {
                return new SmbItem(position -> {
                    SmbBean smbBean = smbList.get(position);
                    switch (smbBean.getSmbType()){
                        case Constants.SmbType.LAN_DEVICE:
                            //未输入账号名 && 不是匿名登录
                            if (StringUtils.isEmpty(smbBean.getAccount()) && !smbBean.isAnonymous()) {
                                new SmbDialog(SmbActivity.this, smbBean, position, (resultBean, resultPosition) -> {
                                    //已存储设备更新信息，未存储设备存储
                                    smbBean.setAccount(resultBean.getAccount());
                                    smbBean.setPassword(resultBean.getPassword());
                                    smbBean.setDomain(resultBean.getDomain());
                                    smbBean.setAnonymous(resultBean.isAnonymous());
                                    smbBean.setSmbType(Constants.SmbType.SQL_DEVICE);
                                    presenter.addSqlDevice(smbBean);
                                    presenter.loginSmb(smbBean);
                                }).show();
                                return;
                            }
                            presenter.loginSmb(smbBean);
                            break;
                        case Constants.SmbType.SQL_DEVICE:
                            //未输入账号名 && 不是匿名登录
                            if (StringUtils.isEmpty(smbBean.getAccount()) && !smbBean.isAnonymous()) {
                                new SmbDialog(SmbActivity.this, smbBean, position, (resultBean, resultPosition) -> {
                                    //已存储设备更新信息，未存储设备存储
                                    smbBean.setAccount(resultBean.getAccount());
                                    smbBean.setPassword(resultBean.getPassword());
                                    smbBean.setDomain(resultBean.getDomain());
                                    smbBean.setAnonymous(resultBean.isAnonymous());
                                    presenter.updateSqlDevice(smbBean);
                                    presenter.loginSmb(smbBean);
                                }).show();
                                return;
                            }
                            presenter.loginSmb(smbBean);
                            break;
                        case Constants.SmbType.FOLDER:
                            presenter.listSmbFolder(smbBean);
                            break;
                        case Constants.SmbType.FILE:
                            presenter.openSmbFile(smbBean);
                            break;
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
        }else {
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
        ToastUtils.showShort(message);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan_device:
                presenter.queryLanDevice();
                break;
            case R.id.add_device:
                new SmbDialog(this, null, -1, (smbBean, position) -> {
                    smbList.add(0, smbBean);
                    presenter.addSqlDevice(smbBean);
                }).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_smb, menu);
        deviceAddItem = menu.findItem(R.id.add_device);
        deviceScanItem = menu.findItem(R.id.scan_device);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void refreshSqlDevice(List<SmbBean> deviceList) {
        pathRl.setVisibility(View.GONE);
        deviceAddItem.setVisible(true);
        deviceScanItem.setVisible(true);
        smbList.clear();
        smbList.addAll(deviceList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void refreshLanDevice(List<SmbBean> deviceList) {
        //在所有设备移除扫描到的设备
        Iterator iterator = smbList.iterator();
        while (iterator.hasNext()) {
            SmbBean smbBean = (SmbBean) iterator.next();
            if (smbBean.getSmbType() == Constants.SmbType.LAN_DEVICE)
                iterator.remove();
        }

        //在扫描设备中移除已有设备
        Iterator scanIterator = deviceList.iterator();
        while (scanIterator.hasNext()) {
            SmbBean scanBean = (SmbBean) scanIterator.next();
            for (SmbBean smbBean : smbList) {
                if (smbBean.getUrl().equals(scanBean.getUrl())) {
                    scanIterator.remove();
                    break;
                }
            }
        }

        smbList.addAll(deviceList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void refreshSmbFile(List<SmbBean> deviceList, String parentPath) {
        pathRl.setVisibility(View.VISIBLE);
        deviceAddItem.setVisible(false);
        deviceScanItem.setVisible(false);
        pathTv.setText(parentPath);
        smbList.clear();
        smbList.addAll(deviceList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @OnClick(R.id.path_rl)
    public void onViewClicked() {
        presenter.returnParentFolder();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ServiceUtils.isServiceRunning(SmbService.class))
            ServiceUtils.stopService(SmbService.class);
    }
}
