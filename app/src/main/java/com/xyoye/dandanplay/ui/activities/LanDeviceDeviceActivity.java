package com.xyoye.dandanplay.ui.activities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.LanDeviceBean;
import com.xyoye.dandanplay.bean.event.AddLanDeviceEvent;
import com.xyoye.dandanplay.bean.event.AuthLanEvent;
import com.xyoye.dandanplay.bean.event.MessageEvent;
import com.xyoye.dandanplay.bean.event.OpenLanEvent;
import com.xyoye.dandanplay.bean.event.UpdateDeviceEvent;
import com.xyoye.dandanplay.mvp.impl.LanDevicePresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.LanDevicePresenter;
import com.xyoye.dandanplay.mvp.view.LanDeviceView;
import com.xyoye.dandanplay.ui.weight.dialog.AuthLanDialog;
import com.xyoye.dandanplay.ui.weight.dialog.CommonDialog;
import com.xyoye.dandanplay.ui.weight.item.LanDeviceItem;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.JsonUtil;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by xyy on 2018/11/19.
 */

public class LanDeviceDeviceActivity extends BaseMvpActivity<LanDevicePresenter> implements LanDeviceView {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.device_rv)
    RecyclerView deviceRv;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refresh;

    private BaseRvAdapter<LanDeviceBean> adapter;
    private List<LanDeviceBean> lanDeviceList = new ArrayList<>();
    private AuthLanDialog authLanDialog;

    @NonNull
    @Override
    protected LanDevicePresenter initPresenter() {
        return new LanDevicePresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.acitivity_lan_device;
    }

    @Override
    public void initView() {
        setTitle("选择局域网设备");
        refresh.setColorSchemeResources(R.color.theme_color);
        refresh.post(() -> refresh.setRefreshing(true));

        deviceRv.setLayoutManager(new GridLayoutManager(this, 4){});
        presenter.getLanDevices();
        adapter = new BaseRvAdapter<LanDeviceBean>(lanDeviceList) {
            @NonNull
            @Override
            public AdapterItem<LanDeviceBean> onCreateItem(int viewType) {
                return new LanDeviceItem();
            }
        };
        deviceRv.setAdapter(adapter);
    }

    @Override
    public void initListener() {
        refresh.setOnRefreshListener(() -> presenter.getLanDevices());
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void authSuccess(LanDeviceBean deviceBean, int position) {
        if (authLanDialog != null && authLanDialog.isShowing())
            authLanDialog.dismiss();
        ToastUtils.showShort("登陆成功");
        showLoadingDialog("开始搜索视频文件");
        
        LanDeviceBean device = lanDeviceList.get(position);
        device.setAccount(deviceBean.getAccount());
        device.setPassword(deviceBean.getPassword());
        device.setDomain(deviceBean.getDomain());
        device.setAnonymous(deviceBean.isAnonymous());
        adapter.notifyItemChanged(position);
        //保存设备数据
        SPUtils.getInstance().put(Constants.AppConfig.SMB_DEVICE, JsonUtil.toJson(device));

        String smbUrl;
        if (StringUtils.isEmpty(device.getAccount()) || device.isAnonymous()){
            smbUrl = "smb://"+device.getIp()+"/";
        }else {
            smbUrl = "smb://"+device.getAccount()+":"+device.getPassword()+"@"+device.getIp()+"/";
        }
        presenter.searchVideo(smbUrl);
    }

    @Override
    public void searchOver() {
        dismissLoadingDialog();
        finish();
        EventBus.getDefault().post(new MessageEvent(MessageEvent.UPDATE_LAN_FOLDER));
    }

    @Override
    public void addDevice(LanDeviceBean device) {
        if (authLanDialog != null && authLanDialog.isShowing())
            authLanDialog.dismiss();
        ToastUtils.showShort("添加设备成功");
        showLoadingDialog("开始搜索视频文件");

        //存在则更新设备，不存在则添加设备
        boolean isEarlyExist = false;
        for (int i=0; i<lanDeviceList.size(); i++){
            if (lanDeviceList.get(i).getIp().equals(device.getIp())){
                device.setAccount(device.getAccount());
                device.setPassword(device.getPassword());
                device.setDomain(device.getDomain());
                device.setAnonymous(device.isAnonymous());
                adapter.notifyItemChanged(i);
                isEarlyExist = true;
                break;
            }
        }
        if (!isEarlyExist){
            lanDeviceList.add(0, device);
            adapter.notifyDataSetChanged();
        }
        //保存设备数据
        SPUtils.getInstance().put(Constants.AppConfig.SMB_DEVICE, JsonUtil.toJson(device));

        String smbUrl;
        if (StringUtils.isEmpty(device.getAccount()) || device.isAnonymous()){
            smbUrl = "smb://"+device.getIp()+"/";
        }else {
            smbUrl = "smb://"+device.getAccount()+":"+device.getPassword()+"@"+device.getIp()+"/";
        }
        presenter.searchVideo(smbUrl);
    }

    @Override
    public void refreshDevices(List<LanDeviceBean> devices) {
        lanDeviceList.clear();
        lanDeviceList.addAll(devices);
        adapter.notifyDataSetChanged();
        if (refresh != null)
            refresh.setRefreshing(false);
    }

    @Subscribe
    public void onOpenLan(OpenLanEvent event){
        LanDeviceBean deviceBean = lanDeviceList.get(event.getPosition());
        if (!StringUtils.isEmpty(deviceBean.getAccount()) || deviceBean.isAnonymous()){
            presenter.authLan(deviceBean, event.getPosition(), false);
        }else {
            authLanDialog = new AuthLanDialog(this, R.style.Dialog, deviceBean, event.getPosition(), false);
            authLanDialog.show();
        }
    }

    @Subscribe
    public void onAuthLan(AuthLanEvent event){
        LanDeviceBean lanDeviceBean = lanDeviceList.get(event.getPosition());
        lanDeviceBean.setAccount(event.getAccount());
        lanDeviceBean.setDomain(event.getDomain());
        lanDeviceBean.setPassword(event.getPassword());
        lanDeviceBean.setAnonymous(event.isAnonymous());
        presenter.authLan(lanDeviceBean, event.getPosition(), false);
    }

    @Subscribe
    public void updateDevice(UpdateDeviceEvent event){
        new CommonDialog.Builder(this)
                .setAutoDismiss()
                .setOkListener(dialog1 -> {
                    SPUtils.getInstance().remove(Constants.AppConfig.SMB_DEVICE);
                    LanDeviceBean lanDeviceBean = lanDeviceList.get(event.getPosition());
                    lanDeviceBean.setAccount("");
                    lanDeviceBean.setAnonymous(false);
                    lanDeviceBean.setPassword("");
                    lanDeviceBean.setDomain("");
                    adapter.notifyItemChanged(event.getPosition());
                })
                .build()
                .show("你希望清除登陆信息吗？");
    }

    @Subscribe
    public void addNewDevice(AddLanDeviceEvent event){
        presenter.authLan(event.getDeviceBean(), 0, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_lan_device:
                authLanDialog = new AuthLanDialog(this, R.style.Dialog, null, 0, true);
                authLanDialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lan_device, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void showLoading() {
        showLoadingDialog();
    }

    @Override
    public void hideLoading() {
        dismissLoadingDialog();
        if (refresh != null)
            refresh.setRefreshing(false);
    }

    @Override
    public void showError(String message) {
        ToastUtils.showShort(message);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }
}
