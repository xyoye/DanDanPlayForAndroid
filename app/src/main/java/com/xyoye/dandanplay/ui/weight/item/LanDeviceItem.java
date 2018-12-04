package com.xyoye.dandanplay.ui.weight.item;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.StringUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.LanDeviceBean;
import com.xyoye.dandanplay.bean.event.OpenLanEvent;
import com.xyoye.dandanplay.bean.event.UpdateDeviceEvent;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;

/**
 * Created by xyy on 2018/11/19.
 */

public class LanDeviceItem implements AdapterItem<LanDeviceBean> {
    @BindView(R.id.device_iv)
    ImageView deviceIv;
    @BindView(R.id.device_name_tv)
    TextView deviceNameTv;
    @BindView(R.id.device_ip_tv)
    TextView deviceIpTv;

    private View mView;

    @Override
    public int getLayoutResId() {
        return R.layout.item_lan_device;
    }

    @Override
    public void initItemViews(View itemView) {
        mView = itemView;
    }

    @Override
    public void onSetViews() {

    }

    @Override
    public void onUpdateViews(LanDeviceBean model, int position) {
        deviceNameTv.setText(model.getDeviceName());
        deviceIpTv.setText(model.getIp());

        if (StringUtils.isEmpty(model.getAccount()) && !model.isAnonymous()){
            deviceIv.setImageResource(R.mipmap.ic_lan_device_shallow);
        }else {
            deviceIv.setImageResource(R.mipmap.ic_lan_device);
        }

        mView.setOnClickListener(v -> EventBus.getDefault().post(new OpenLanEvent(position)));

        mView.setOnLongClickListener(v -> {
            if (!StringUtils.isEmpty(model.getAccount()) || model.isAnonymous() ){
                EventBus.getDefault().post(new UpdateDeviceEvent(false, position));
                return true;
            }
            return false;
        });
    }
}
