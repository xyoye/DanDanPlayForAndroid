package com.xyoye.dandanplay.utils;

import android.content.Context;
import android.database.Cursor;
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

        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Cursor cursor = context.getContentResolver() .query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            null, null, null, null);
            if (cursor == null) return new ArrayList<>();
            while (cursor.moveToNext()) {
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                File file = new File(path);
                if (!file.exists()) {
                    continue;
                }
                VideoBean videoBean = new VideoBean();
                videoBean.setVideoPath(file.getAbsolutePath());
                videoBean.setVideoDuration(cursor.getLong(
                        cursor.getColumnIndex(MediaStore.Video.Media.DURATION)));
                videoList.add(videoBean);
            }
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
