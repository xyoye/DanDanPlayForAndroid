package com.player.ijkplayer.widgets;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.player.ijkplayer.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by xyoye on 2019/4/30.
 */

public class DialogScreenShot extends Dialog {

    private ImageView shotIv;
    private Button shotCancelBt, shotSaveBt;
    private Bitmap bitmap;

    public DialogScreenShot(@NonNull Context context, Bitmap bitmap) {
        super(context, R.style.AnimateDialog);
        this.bitmap = bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_screen_shot);

        shotIv = findViewById(R.id.shot_iv);
        shotCancelBt = findViewById(R.id.shot_cancel_bt);
        shotSaveBt = findViewById(R.id.shot_save_bt);

        shotIv.setImageBitmap(bitmap);

        shotCancelBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogScreenShot.this.dismiss();
            }
        });

        shotSaveBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = saveImage(bitmap);
                if (path == null){
                    ToastUtils.showShort("保存截图失败");
                }else {
                    ToastUtils.showLong("保存成功："+path);
                }
                DialogScreenShot.this.dismiss();
            }
        });
    }

    @Override
    public void show() {
        super.show();

        if (getWindow() == null)
            return;

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = ConvertUtils.dp2px(450);
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        getWindow().getDecorView().setPadding(0, 0, 0, 0);
        getWindow().setAttributes(layoutParams);
    }

    @SuppressLint("SimpleDateFormat")
    public String saveImage(Bitmap bitmap) {
        FileOutputStream fos = null;
        String imagePath = null;
        try {
            //make folder
            String folderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DanDanPlayer/_image";
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            //make file name
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
            String curTime = formatter.format(curDate);
            String fileName = "/SHOT_" + curTime + ".jpg";

            //make file
            File file = new File(folder, fileName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            //save bitmap
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            imagePath = file.getAbsolutePath();

            //通知系统相册刷新
            getContext().sendBroadcast(
                    new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(file.getPath()))));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return imagePath;
    }
}
