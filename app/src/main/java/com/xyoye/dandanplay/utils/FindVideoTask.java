package com.xyoye.dandanplay.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;

import com.xyoye.dandanplay.bean.VideoBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by YE on 2018/7/4 0004.
 * 视频查找任务
 */

public class FindVideoTask extends AsyncTask<Context, Integer, List<VideoBean>> {

    private ArrayList<VideoBean> videoList;
    private QueryListener listener;

    public FindVideoTask() {
        videoList = new ArrayList<>();
    }

    public void setQueryListener(QueryListener listener) {
        this.listener = listener;
    }

    @Override
    protected List<VideoBean> doInBackground(Context... params) {
        Context context = params[0];
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    null, null, null, null);
            if (cursor == null) return new ArrayList<>();
            while (cursor.moveToNext()) {
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                File file = new File(path);
                if (!file.exists()) {
                    continue;
                }

                int _id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));// 视频的id
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));// 大小
                long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));// 时长

                VideoBean videoBean = new VideoBean();
                videoBean.set_id(_id);
                videoBean.setVideoPath(file.getAbsolutePath());
                videoBean.setVideoDuration(duration);
                videoBean.setVideoSize(size);
                videoList.add(videoBean);
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (cursor != null)
                cursor.close();
        }
        return videoList;
    }

    @Override
    protected void onPostExecute(List<VideoBean> VideoBeans) {
        if (listener != null) {
            listener.onResult(VideoBeans);
        }
    }

    public interface QueryListener {
        void onResult(List<VideoBean> videoList);
    }
}
