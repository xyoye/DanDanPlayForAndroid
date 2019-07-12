package com.xyoye.dandanplay.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.Looper;
import android.view.Gravity;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.tencent.bugly.crashreport.CrashReport;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xyy on 2019/5/5.
 *
 *  UncaughtException处理类
 *  回调崩溃处理，保存错误日志到文件，上传错误到bugly
 */
public class CrashHandleUtils implements UncaughtExceptionHandler {
    //系统默认的UncaughtException处理类
    private UncaughtExceptionHandler mDefaultHandler;
    //CrashHandler实例
    private static CrashHandleUtils instance;
    //程序的Context对象
    private Context mContext;
    //用来存储版本信息
    private Map<String, String> infos = new HashMap<>();
    //用于格式化日期,作为日志文件名的一部分
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    //保存crash log文件路径
    private String path;
    //异常处理回调
    private CrashListener crashListener;

    private CrashHandleUtils() {

    }

    public static CrashHandleUtils getInstance() {
        if(instance == null)
            instance = new CrashHandleUtils();
        return instance;
    }

    /**
     * 初始化
     */
    public void init(CrashListener crashListener) {
        this.path = Environment.getExternalStorageDirectory() + "/DanDanPlayer/_crash/";
        this.crashListener = crashListener;

        mContext = IApplication.get_context();
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ex.printStackTrace();
        if (!handleException(ex) && mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            crashListener.onCrash();
        }
    }

    /**
     * 自定义错误处理，收集错误信息
     * 如果回调函数为空，则不处理异常
     */
    private boolean handleException(Throwable ex) {
        if (crashListener == null)
            return false;
        //收集版本信息
        collectDeviceInfo(mContext);
        //使用Toast来显示异常信息
        new Thread() {
            @Override
            public void run() {
                showErrorToast(mContext);
            }
        }.start();
        //保存日志文件
        saveCatchInfo2File(ex);
        //上传bugly
        CrashReport.postCatchedException(new Exception("程序崩溃异常", ex));
        return true;
    }

    /**
     * 收集版本信息
     */
    private void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                String time = formatter.format(new Date());
                infos.put("time", time);
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            LogUtils.e(e);
        }
    }

    /**
     * 保存错误信息到文件中
     */
    private String saveCatchInfo2File(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append("=").append(value).append("\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            String time = formatter.format(new Date());
            String fileName = "crash-" + time + ".log";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(path + fileName);
                fos.write(sb.toString().getBytes());
                fos.close();
            }
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(e);
        }
        return null;
    }

    public interface CrashListener{
        void onCrash();
    }

    private static void showErrorToast(Context context) {
        Looper myLooper = Looper.myLooper();
        if (myLooper == null) {
            Looper.prepare();
            myLooper = Looper.myLooper();
        }

        Toast toast = Toast.makeText(context, "程序出现异常，即将重启!", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.getView().setBackground(context.getResources().getDrawable(R.drawable.background_toast_theme));
        toast.show();

        if ( myLooper != null) {
            Looper.loop();
            myLooper.quit();
        }
    }

    /**
     * 保存错误信息到文件中
     */
    @SuppressLint("SimpleDateFormat")
    public static String saveCatchInfo2FileV2(Throwable ex) {
        String path = Environment.getExternalStorageDirectory() + "/DanDanPlayer/_crash/";
        StringBuilder sb = new StringBuilder();
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            String time = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String fileName = "exception-" + time + ".log";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(path + fileName);
                fos.write(sb.toString().getBytes());
                fos.close();
            }
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(e);
        }
        return null;
    }
}