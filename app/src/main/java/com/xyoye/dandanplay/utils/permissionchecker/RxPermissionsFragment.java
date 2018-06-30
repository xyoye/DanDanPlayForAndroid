package com.xyoye.dandanplay.utils.permissionchecker;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.subjects.PublishSubject;

/**
 * Created by san on 2017/12/19.
 */

public class RxPermissionsFragment extends Fragment {

    private static final int PERMISSIONS_REQUEST_CODE = 42;
    private Map<String, PublishSubject<Permission>> mSubjects = new HashMap<>();
    private boolean mLogging;

    public RxPermissionsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //主要是保存Fragment的状态
        setRetainInstance(true);
    }

    //申请权限！！
    @TargetApi(Build.VERSION_CODES.M)
    void requestPermissions(@NonNull String[] permissions) {
        requestPermissions(permissions, PERMISSIONS_REQUEST_CODE);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //判断权限是否真正确
        if (requestCode != PERMISSIONS_REQUEST_CODE) return;

        boolean[] shouldShowRequestPermissionRationale = new boolean[permissions.length];

        for (int i = 0; i < permissions.length; i++) {
            //一个不在提示的权限
            shouldShowRequestPermissionRationale[i] = shouldShowRequestPermissionRationale(permissions[i]);
        }
        //把信息传递出去 permissions 申请的权限 grantResults 同意的权限  shouldShowRequestPermissionRationale 不在询问的权限
        onRequestPermissionsResult(permissions, grantResults, shouldShowRequestPermissionRationale);
    }

    void onRequestPermissionsResult(String permissions[], int[] grantResults, boolean[] shouldShowRequestPermissionRationale) {
        for (int i = 0, size = permissions.length; i < size; i++) {
            log("onRequestPermissionsResult  " + permissions[i]);
            // Find the corresponding subject
            PublishSubject<Permission> subject = mSubjects.get(permissions[i]);
            if (subject == null) {
                // No subject found
                Log.e(RxPermissions.TAG, "RxPermissions.onRequestPermissionsResult invoked but didn't find the corresponding permission request.");
                return;
            }
            mSubjects.remove(permissions[i]);
            boolean granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
            //一个个去处理
            subject.onNext(new Permission(permissions[i], granted, shouldShowRequestPermissionRationale[i]));
            subject.onComplete();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    boolean isGranted(String permission) {
        return getActivity().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.M)
    boolean isRevoked(String permission) {
        return getActivity().getPackageManager().isPermissionRevokedByPolicy(permission, getActivity().getPackageName());
    }

    //是否打印
    void log(String message) {
        if (mLogging) {
            Log.d(RxPermissions.TAG, message);
        }
    }

    public void setLogging(boolean logging) {
        mLogging = logging;
    }

    public PublishSubject<Permission> getSubjectByPermission(@NonNull String permission) {
        return mSubjects.get(permission);
    }

    public boolean containsByPermission(@NonNull String permission) {
        return mSubjects.containsKey(permission);
    }

    public PublishSubject<Permission> setSubjectForPermission(@NonNull String permission, @NonNull PublishSubject<Permission> subject) {
        return mSubjects.put(permission, subject);
    }
}
