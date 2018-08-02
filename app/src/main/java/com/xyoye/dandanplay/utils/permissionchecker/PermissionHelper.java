package com.xyoye.dandanplay.utils.permissionchecker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * Created by san on 2017/12/19.
 */

public class PermissionHelper {
    //一个上下文  必须为Activity  因为我需要弹出 请求框
    public Activity mActivity;
//    private HashMap<String, String> map;//管理权限的map

    private static final HashMap<String, String> map;

    static {
        map = new HashMap<>();
        map.put("android.permission.WRITE_CONTACTS", "修改联系人");
        map.put("android.permission.GET_ACCOUNTS", "访问账户Gmail列表");
        map.put("android.permission.READ_CONTACTS", "读取联系人");
        map.put("android.permission.READ_CALL_LOG", "读取通话记录");
        map.put("android.permission.READ_PHONE_STATE", "读取电话状态");
        map.put("android.permission.CALL_PHONE", "拨打电话");
        map.put("android.permission.WRITE_CALL_LOG", "修改通话记录");
        map.put("android.permission.USE_SIP", "使用SIP视频");
        map.put("android.permission.PROCESS_OUTGOING_CALLS", "PROCESS_OUTGOING_CALLS");
        map.put("com.android.voicemail.permission.ADD_VOICEMAIL", "ADD_VOICEMAIL");
        map.put("android.permission.READ_CALENDAR", "读取日历");
        map.put("android.permission.WRITE_CALENDAR", "修改日历");
        map.put("android.permission.CAMERA", "拍照");
        map.put("android.permission.BODY_SENSORS", "传感器");
        map.put("android.permission.ACCESS_FINE_LOCATION", "获取精确位置");
        map.put("android.permission.ACCESS_COARSE_LOCATION", "获取粗略位置");
        map.put("android.permission.READ_EXTERNAL_STORAGE", "读存储卡");
        map.put("android.permission.WRITE_EXTERNAL_STORAGE", "修改存储卡");
        map.put("android.permission.RECORD_AUDIO", "录音");
        map.put("android.permission.READ_SMS", "读取短信内容");
        map.put("android.permission.RECEIVE_WAP_PUSH", "接收Wap Push");
        map.put("android.permission.RECEIVE_MMS", "接收短信");
        map.put("android.permission.SEND_SMS", "发送短信");
        map.put("android.permission.READ_CELL_BROADCASTS", "READ_CELL_BROADCASTS");
    }


    public PermissionHelper with(Activity activity) {
        mActivity = activity;
        return this;
    }

    public PermissionHelper with(Fragment fragment) {
        mActivity = fragment.getActivity();
        return this;
    }
    //门面模式   一键发布

    List<Permission> permissionList = new ArrayList<Permission>();

    public void request(final OnSuccessListener listener, String... permissions) {


        RxPermissions rxPermissions = new RxPermissions(mActivity);
        Observable.just(new Object()).compose(rxPermissions.ensureEach(permissions)).subscribe(new Consumer<Permission>() {
            //这里记录一下 所有
            @Override
            public void accept(Permission permission) {
                if (permission.granted) {
                    //尽管显示已经授权了，但是 我也不相信 各种国产手机的疑难杂症
                    boolean permissionGranted = PermissionsChecker.isPermissionGranted(mActivity, permission.name);
                    if (permissionGranted) {
                        //两次检测都已经过了  求你放过这个权限把    肯定可以的
                    } else {
                        permissionList.add(permission);
                    }
                } else {
                    permissionList.add(permission);
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                //在申请权限的时候发现异常  我一律交给用户处理
                initDialog(null);
                permissionList = new ArrayList<Permission>();
            }
        }, new Action() {
            @Override
            public void run() {
                //当上面遍历完成之后就弹出一个提示框  当然需要检测 是否有拒绝的权限
                if (permissionList.size() > 0) {
                    initDialog(permissionList);
                } else {
                    listener.onPermissionSuccess();
                }
                //重新搞一个
                permissionList = new ArrayList<Permission>();
            }
        });

    }

    public void initDialog(List<Permission> data) {
        //重新访问
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setCancelable(false).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                new Intent();
                Intent intent = PermissionsPageManager.getIntent(mActivity);
                mActivity.startActivity(intent);
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        if (data == null) {
            builder.setMessage("授权系统发生异常错误，请到用户中心设置");
        } else {
//            StringBuilder stringBuilder = new StringBuilder();
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < data.size(); i++) {
                stringBuilder.append(map.get(data.get(i).name));
                if (i != data.size() - 1) {
                    stringBuilder.append("、");
                }
            }
            builder.setMessage("本应用需要" + stringBuilder + "权限,请到用户中心设置。");
        }

        builder.create();
        AlertDialog alertDialog = builder.create();
        if (!alertDialog.isShowing())
            alertDialog.show();
    }


    public interface OnSuccessListener {
        void onPermissionSuccess();
    }

}
