package com.xyoye.dandanplay.ui.activities.play;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
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
import com.xyoye.dandanplay.ui.weight.dialog.CommonDialog;
import com.xyoye.dandanplay.ui.weight.item.SmbItem;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by xyoye on 2019/3/30.
 */

public class SmbActivity extends BaseMvpActivity<SmbPresenter> implements SmbView {
    @BindView(R.id.path_tv)
    TextView pathTv;
    @BindView(R.id.path_rl)
    RelativeLayout pathRl;
    @BindView(R.id.smb_rv)
    RecyclerView smbRv;

    private BaseRvAdapter<SmbBean> adapter;
    private List<SmbBean> smbList;
    private MenuItem deviceAddItem, deviceScanItem, sortItem;

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
                return new SmbItem(new SmbItem.OnSmbItemClickListener() {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan_device:
                presenter.queryLanDevice();
                break;
            case R.id.add_device:
                showAuthDialog(null, -1);
                break;
            case R.id.sort_item:
                boolean isGrid = AppConfig.getInstance().smbIsGridLayout();
                smbRv.setLayoutManager(!isGrid
                        ? new GridLayoutManager(this, 4)
                        : new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
                AppConfig.getInstance().setSmbIsGridLayout(!isGrid);
                sortItem.setIcon(!isGrid
                        ? R.mipmap.ic_sort_liner
                        : R.mipmap.ic_sort_grid);
                adapter.notifyDataSetChanged();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_smb, menu);
        deviceAddItem = menu.findItem(R.id.add_device);
        deviceScanItem = menu.findItem(R.id.scan_device);
        sortItem = menu.findItem(R.id.sort_item);
        sortItem.setIcon(AppConfig.getInstance().smbIsGridLayout()
                ? R.mipmap.ic_sort_liner
                : R.mipmap.ic_sort_grid);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void refreshSqlDevice(List<SmbBean> deviceList) {
        if (smbRv == null)
            return;
        //设备列表必须为GridLayoutManager
        if (smbRv.getLayoutManager() instanceof LinearLayoutManager) {
            smbRv.setLayoutManager(new GridLayoutManager(this, 4));
        }
        pathRl.setVisibility(View.GONE);
        deviceAddItem.setVisible(true);
        deviceScanItem.setVisible(true);
        sortItem.setVisible(false);
        smbList.clear();
        smbList.addAll(deviceList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void refreshLanDevice(List<SmbBean> deviceList) {
        hideLoading();
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
        //设备列表必须为GridLayoutManager
        if (smbRv.getLayoutManager() instanceof LinearLayoutManager) {
            smbRv.setLayoutManager(new GridLayoutManager(this, 4));
        }
        smbList.addAll(deviceList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void refreshSmbFile(List<SmbBean> deviceList, String parentPath) {
        //文件列表根据设置，设置布局方式
        if (smbRv.getLayoutManager() instanceof GridLayoutManager) {
            if (!AppConfig.getInstance().smbIsGridLayout()) {
                smbRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            }
        } else {
            if (AppConfig.getInstance().smbIsGridLayout()) {
                smbRv.setLayoutManager(new GridLayoutManager(this, 4));
            }
        }

        pathRl.setVisibility(View.VISIBLE);
        deviceAddItem.setVisible(false);
        deviceScanItem.setVisible(false);
        sortItem.setVisible(true);
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

    private void onItemClick(int position) {
        SmbBean smbBean = smbList.get(position);
        switch (smbBean.getSmbType()) {
            case Constants.SmbType.LAN_DEVICE:
            case Constants.SmbType.SQL_DEVICE:
                //未输入账号名 && 不是匿名登录
                if (StringUtils.isEmpty(smbBean.getAccount()) && !smbBean.isAnonymous()) {
                    showAuthDialog(smbBean, position);
                    return;
                }
                presenter.loginSmb(smbBean, position);
                break;
            case Constants.SmbType.FOLDER:
                presenter.listSmbFolder(smbBean);
                break;
            case Constants.SmbType.FILE:
                presenter.openSmbFile(smbBean);
                break;
        }
    }

    private void onItemLongClick(int position) {
        String url = smbList.get(position).getUrl();
        new CommonDialog.Builder(this)
                .setOkListener(dialog -> {
                    SmbBean smbBean = smbList.get(position);
                    smbBean.setAccount("");
                    smbBean.setPassword("");
                    smbBean.setSmbType(Constants.SmbType.LAN_DEVICE);
                    smbBean.setAnonymous(false);
                    presenter.removeSqlDevice(url);
                    adapter.notifyItemChanged(position);
                })
                .setAutoDismiss()
                .build()
                .show("确认移除该设备？");
    }

    @Override
    public void LoginSuccess(int position, SmbBean loginSmbBean) {
        SmbBean smbBean = smbList.get(position);
        smbBean.setUrl(loginSmbBean.getUrl());
        smbBean.setAccount(loginSmbBean.getAccount());
        smbBean.setPassword(loginSmbBean.getPassword());
        smbBean.setDomain(loginSmbBean.getDomain());
        smbBean.setAnonymous(loginSmbBean.isAnonymous());
        smbBean.setSmbType(Constants.SmbType.SQL_DEVICE);
        presenter.addSqlDevice(smbBean);
        adapter.notifyItemChanged(position);
    }

    private void showAuthDialog(SmbBean smbBean, int position) {
        boolean isAddDevice = position == -1;

        //初始化view
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_auth_lan, null);
        TextInputEditText lanAccountEt = dialogView.findViewById(R.id.lan_account_et);
        TextInputEditText lanPasswordEt = dialogView.findViewById(R.id.lan_password_et);
        TextInputEditText lanDomainEt = dialogView.findViewById(R.id.lan_domain_et);
        CheckBox anonymousCb = dialogView.findViewById(R.id.anonymous_cb);
        TextInputEditText lanIpEt = dialogView.findViewById(R.id.lan_ip_et);
        TextInputLayout lanIpLayout = dialogView.findViewById(R.id.lan_ip_layout);

        lanIpEt.setHint("IP");
        lanAccountEt.setHint("帐号");
        lanPasswordEt.setHint("密码");
        lanDomainEt.setHint("域");
        lanIpLayout.setVisibility(isAddDevice ? View.VISIBLE : View.GONE);

        if (smbBean != null) {
            lanAccountEt.setText(smbBean.getAccount());
            lanPasswordEt.setText(smbBean.getPassword());
            lanDomainEt.setText(smbBean.getDomain());
            anonymousCb.setChecked(smbBean.isAnonymous());
        } else if (!isAddDevice) {
            ToastUtils.showShort("错误，登录数据为空");
            return;
        }

        //创建dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog authLanDialog = builder.setTitle("登录设备")
                .setView(dialogView)
                .setPositiveButton("确定", null)
                .setNegativeButton("取消", null)
                .create();
        authLanDialog.show();
        authLanDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    boolean anonymous = anonymousCb.isChecked();
                    String account = lanAccountEt.getEditableText().toString();
                    String password = lanPasswordEt.getEditableText().toString();
                    String domain = lanDomainEt.getEditableText().toString();
                    String ip = lanIpEt.getEditableText().toString();
                    if (isAddDevice && StringUtils.isEmpty(ip)) {
                        ToastUtils.showShort("请输入ip地址");
                        return;
                    }
                    if (!anonymous && StringUtils.isEmpty(account)) {
                        ToastUtils.showShort("请输入账号密码或选择匿名登陆");
                        return;
                    }

                    //组装数据
                    SmbBean authBean = new SmbBean();
                    authBean.setUrl(isAddDevice ? ip : smbBean.getUrl());
                    authBean.setAccount(account);
                    authBean.setPassword(password);
                    authBean.setDomain(domain);
                    authBean.setAnonymous(anonymous);
                    authBean.setNickName("UnKnow");
                    authBean.setSmbType(Constants.SmbType.SQL_DEVICE);
                    if (isAddDevice) {
                        presenter.addSqlDevice(authBean);
                        smbList.add(0, authBean);
                        adapter.notifyDataSetChanged();
                    } else {
                        presenter.loginSmb(authBean, position);
                    }
                    authLanDialog.dismiss();
                });
    }
}
