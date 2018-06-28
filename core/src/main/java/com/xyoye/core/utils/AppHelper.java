package com.xyoye.core.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;

import com.xyoye.core.BaseApplication;

import java.util.List;

public final class AppHelper {

    private AppHelper() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static boolean isDebug() {
        Context context = BaseApplication.get_context();
        if (context == null) return false;
        SharedPreferencesUtil shareUtil = SharedPreferencesUtil.getInstance(context, "total_app");
        return shareUtil.loadBooleanSharedPreference("debug");
    }

    public static void setDebug(boolean isDebug) {
        Context context = BaseApplication.get_context();
        if (context == null) return;
        SharedPreferencesUtil shareUtil = SharedPreferencesUtil.getInstance(context, "total_app");
        shareUtil.saveSharedPreferences("debug", isDebug);
    }

    /**
     * 获取包信息
     *
     * @return PackageInfo
     */
    public static PackageInfo getPackageInfo(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /**
     * 获取应用包名
     *
     * @return
     */
    public static String getPackageId() {
        try {
            return BaseApplication.get_context().getPackageName();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 判断指定包名的进程是否运行
     * param context
     * param packageName 指定包名
     * return 是否运行
     */
    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public static boolean isRunning(Context context, String packageName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> infos = am.getRunningAppProcesses();
        for (RunningAppProcessInfo rapi : infos) {
            if (rapi.processName.equals(packageName))
                return true;
        }
        return false;
    }

    /**
     * 用来判断服务是否运行.
     * param className 判断的服务名字
     * return true 在运行 false 不在运行
     */
    public static boolean isServiceRunning(Context mContext, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager)
                mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(30);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className)) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    /**
     * 根据包名判断应用是否存在
     * param context
     * param packageName
     * return
     */
    public static boolean isAppExist(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        //PackageManager.GET_UNINSTALLED_PACKAGES
        List<PackageInfo> list = manager.getInstalledPackages(0);
        for (PackageInfo info : list) {
            if (info.packageName.equalsIgnoreCase(packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取app当前版本名称
     * param context
     * return
     */
    public static String getVersionName(Context context) {
        PackageInfo info = getPackageInfo(context);
        if (info != null) {
            return info.versionName;
        } else {
            return "";
        }
    }

    /**
     * 获取app当前版本号
     * param context
     * return
     */
    public static int getVersionCode(Context context) {
        PackageInfo info = getPackageInfo(context);
        if (info != null) {
            return info.versionCode;
        } else {
            return 0;
        }
    }

    /**
     * 6.0权限申请
     *
     * @param context
     * @param permission
     * @param requestCode
     */
    public static boolean requestPermission(Activity context, String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {

            } else {
                ActivityCompat.requestPermissions(context, new String[]{permission}, requestCode);
            }
            return false;
        }
        return true;
    }

    public static boolean checkPermission(Activity context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 获取设备号
     *
     * @param context
     * @return
     */
    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return "";
        }
        if (telephonyManager == null) return "";
        return telephonyManager.getDeviceId();
    }

}
