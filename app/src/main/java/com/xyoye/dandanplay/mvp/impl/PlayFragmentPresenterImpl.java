package com.xyoye.dandanplay.mvp.impl;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.StringUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.FolderBean;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.bean.event.UpdateFolderDanmuEvent;
import com.xyoye.dandanplay.mvp.presenter.PlayFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.PlayFragmentView;
import com.xyoye.dandanplay.ui.activities.play.PlayerManagerActivity;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.database.DataBaseInfo;
import com.xyoye.dandanplay.utils.database.DataBaseManager;
import com.xyoye.dandanplay.utils.database.callback.QueryAsyncResultCallback;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xyoye on 2018/6/29.
 */

public class PlayFragmentPresenterImpl extends BaseMvpPresenterImpl<PlayFragmentView> implements PlayFragmentPresenter {

    public PlayFragmentPresenterImpl(PlayFragmentView view, Lifeful lifeful) {
        super(view, lifeful);
    }

    @Override
    public void init() {

    }

    @Override
    public void process(Bundle savedInstanceState) {

    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void playLastVideo(Context context, String videoPath) {
        DataBaseManager.getInstance()
                .selectTable("file")
                .query()
                .queryColumns("danmu_path", "current_position", "danmu_episode_id", "zimu_path")
                .where("file_path", videoPath)
                .postExecute(new QueryAsyncResultCallback<VideoBean>(getLifeful()) {
                    @Override
                    public VideoBean onQuery(Cursor cursor) {
                        if (cursor == null) return null;
                        VideoBean videoBean = null;
                        if (cursor.moveToNext()) {
                            videoBean = new VideoBean();
                            videoBean.setVideoPath(videoPath);
                            videoBean.setDanmuPath(cursor.getString(0));
                            videoBean.setCurrentPosition(cursor.getInt(1));
                            videoBean.setEpisodeId(cursor.getInt(2));
                            videoBean.setZimuPath(cursor.getString(3));
                        }
                        return videoBean;
                    }

                    @Override
                    public void onResult(VideoBean videoBean) {
                        if (videoBean == null)
                            return;
                        //视频文件是否已被删除
                        File videoFile = new File(videoBean.getVideoPath());
                        if (!videoFile.exists())
                            return;
                        //弹幕文件是否已被删除
                        if (!StringUtils.isEmpty(videoBean.getDanmuPath())) {
                            File danmuFile = new File(videoBean.getDanmuPath());
                            if (!danmuFile.exists())
                                videoBean.setDanmuPath("");
                        }
                        //字幕幕文件是否已被删除
                        if (!StringUtils.isEmpty(videoBean.getZimuPath())) {
                            File zimuFile = new File(videoBean.getZimuPath());
                            if (!zimuFile.exists())
                                videoBean.setZimuPath("");
                        }

                        PlayerManagerActivity.launchPlayerLocal(
                                context,
                                FileUtils.getFileNameNoExtension(videoBean.getVideoPath()),
                                videoBean.getVideoPath(),
                                videoBean.getDanmuPath(),
                                videoBean.getZimuPath(),
                                videoBean.getCurrentPosition(),
                                videoBean.getEpisodeId());
                    }
                });
    }

    /**
     * 刷新视频文件
     *
     * @param reScan true: 重新扫描整个系统目录
     *               false: 只查询数据库数据
     */
    @Override
    public void refreshVideo(Context context, boolean reScan) {
        //通知系统刷新目录
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(Environment.getExternalStorageDirectory()));
        if (context != null)
            context.sendBroadcast(intent);

        if (reScan) {
            refreshAllVideo();
        } else {
            refreshDatabaseVideo();
        }

        EventBus.getDefault().post(new UpdateFolderDanmuEvent());
    }

    @Override
    public void filterFolder(String folderPath) {
        DataBaseManager.getInstance()
                .selectTable("scan_folder")
                .insert()
                .param("folder_path", folderPath)
                .param("folder_type", Constants.ScanType.BLOCK)
                .postExecute();
    }

    @Override
    public void deleteFolderVideo(String folderPath) {
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()){
            for (File file : folder.listFiles()){
                if (CommonUtils.isMediaFile(file.getAbsolutePath())){
                    file.delete();
                }
            }
        }
        getView().deleteFolderSuccess();
    }

    /**
     * 扫描所有文件，更新数据库，刷新界面数据
     */
    private void refreshAllVideo() {
        //获取需要扫描的目录
        DataBaseManager.getInstance()
                .selectTable("scan_folder")
                .query()
                .queryColumns("folder_path")
                .where("folder_type", String.valueOf(Constants.ScanType.SCAN))
                .postExecute(new QueryAsyncResultCallback<List<FolderBean>>(getLifeful()) {
                    @Override
                    public List<FolderBean> onQuery(Cursor cursor) {
                        if (cursor == null)
                            return new ArrayList<>();
                        List<String> scanFolderList = new ArrayList<>();
                        while (cursor.moveToNext()) {
                            scanFolderList.add(cursor.getString(0));
                        }

                        //是否获取系统中视频信息
                        boolean isScanMediaStore = scanFolderList.remove(Constants.DefaultConfig.SYSTEM_VIDEO_PATH);
                        if (isScanMediaStore) {
                            queryVideoFormMediaStore();
                        }

                        //遍历需要扫描的目录
                        queryVideoFormStorage(scanFolderList);

                        return DataBaseManager.getInstance()
                                .selectTable("scan_folder")
                                .query()
                                .queryColumns("folder_path", "folder_type")
                                .executeAsync(scanTypeCursor -> {
                                    if (scanTypeCursor == null)
                                        return new ArrayList<>();

                                    List<String> scanList = new ArrayList<>();
                                    List<String> blockList = new ArrayList<>();

                                    while (scanTypeCursor.moveToNext()) {
                                        if (Constants.ScanType.SCAN == scanTypeCursor.getInt(1)) {
                                            scanList.add(scanTypeCursor.getString(0));
                                        } else {
                                            blockList.add(scanTypeCursor.getString(0));
                                        }
                                    }
                                    return getVideoFormDatabase(scanList, blockList);
                                });
                    }

                    @Override
                    public void onResult(List<FolderBean> result) {
                        getView().refreshAdapter(result);
                    }
                });
    }

    /**
     * 仅根据数据库数据刷新界面
     */
    private void refreshDatabaseVideo() {
        DataBaseManager.getInstance()
                .selectTable("scan_folder")
                .query()
                .queryColumns("folder_path", "folder_type")
                .postExecute(new QueryAsyncResultCallback<List<FolderBean>>(getLifeful()) {

                    @Override
                    public List<FolderBean> onQuery(Cursor cursor) {
                        if (cursor == null)
                            return new ArrayList<>();
                        List<String> scanList = new ArrayList<>();
                        List<String> blockList = new ArrayList<>();

                        while (cursor.moveToNext()) {
                            if (Constants.ScanType.SCAN == cursor.getInt(1)) {
                                scanList.add(cursor.getString(0));
                            } else {
                                blockList.add(cursor.getString(0));
                            }
                        }
                        return getVideoFormDatabase(scanList, blockList);
                    }

                    @Override
                    public void onResult(List<FolderBean> result) {
                        getView().refreshAdapter(result);
                    }
                });
    }

    /**
     * 从数据库中读取文件夹目录，过滤屏蔽目录及不扫描目录
     */
    private List<FolderBean> getVideoFormDatabase(List<String> scanList, List<String> blockList) {
        List<FolderBean> folderBeanList = new ArrayList<>();
        Map<String, Integer> beanMap = new HashMap<>();
        Map<String, String> deleteMap = new HashMap<>();

        //查询所有视频
        DataBaseManager.getInstance()
                .selectTable("file")
                .query()
                .queryColumns("folder_path", "file_path")
                .executeAsync(cursor -> {
                    while (cursor.moveToNext()) {
                        String folderPath = cursor.getString(0);
                        String filePath = cursor.getString(1);

                        //过滤屏蔽目录
                        boolean isBlock = false;
                        for (String blockPath : blockList) {
                            //视频属于屏蔽目录下视频，过滤
                            if (filePath.startsWith(blockPath)) {
                                isBlock = true;
                                break;
                            }
                        }
                        if (isBlock) continue;

                        //过滤非扫描目录，扫描包括系统目录时不用过滤
                        if (!scanList.contains(Constants.DefaultConfig.SYSTEM_VIDEO_PATH)) {
                            boolean isNotScan = true;
                            for (String scanPath : scanList) {
                                if (filePath.startsWith(scanPath)) {
                                    isNotScan = false;
                                    break;
                                }
                            }
                            if (isNotScan) continue;
                        }

                        //计算文件夹中文件数量
                        //文件不存在记录需要删除的文件
                        File file = new File(filePath);
                        if (file.exists()) {
                            if (beanMap.containsKey(folderPath)) {
                                Integer number = beanMap.get(folderPath);
                                number = number == null ? 0 : number;
                                beanMap.put(folderPath, ++number);
                            } else {
                                beanMap.put(folderPath, 1);
                            }
                        } else {
                            deleteMap.put(folderPath, filePath);
                        }
                    }
                });

        //更新文件夹文件数量
        for (Map.Entry<String, Integer> entry : beanMap.entrySet()) {
            folderBeanList.add(new FolderBean(entry.getKey(), entry.getValue()));
            DataBaseManager.getInstance()
                    .selectTable("folder")
                    .update()
                    .param("file_number", entry.getValue())
                    .where("folder_path", entry.getKey())
                    .executeAsync();
        }

        //删除不存在的文件
        for (Map.Entry<String, String> entry : deleteMap.entrySet()) {
            DataBaseManager.getInstance()
                    .selectTable("file")
                    .delete()
                    .where("folder_path", entry.getKey())
                    .where("file_path", entry.getValue())
                    .executeAsync();
        }
        return folderBeanList;
    }

    /**
     * 保存视频信息到数据库
     * 跳过已存在的视频信息
     */
    private void saveVideoToDatabase(VideoBean videoBean) {
        String folderPath = FileUtils.getDirName(videoBean.getVideoPath());
        ContentValues values = new ContentValues();
        values.put(DataBaseInfo.getFieldNames()[2][1], folderPath);
        values.put(DataBaseInfo.getFieldNames()[2][2], videoBean.getVideoPath());
        values.put(DataBaseInfo.getFieldNames()[2][5], String.valueOf(videoBean.getVideoDuration()));
        values.put(DataBaseInfo.getFieldNames()[2][7], String.valueOf(videoBean.getVideoSize()));
        values.put(DataBaseInfo.getFieldNames()[2][8], videoBean.get_id());

        DataBaseManager.getInstance()
                .selectTable("file")
                .query()
                .where("folder_path", folderPath)
                .where("file_path", videoBean.getVideoPath())
                .executeAsync(cursor -> {
                    if (!cursor.moveToNext()) {
                        DataBaseManager.getInstance()
                                .selectTable("file")
                                .insert()
                                .param("folder_path", folderPath)
                                .param("file_path", videoBean.getVideoPath())
                                .param("duration", String.valueOf(videoBean.getVideoDuration()))
                                .param("file_size", String.valueOf(videoBean.getVideoSize()))
                                .param("file_id", videoBean.get_id())
                                .executeAsync();
                    }
                });
    }

    /**
     * 获取系统中视频信息
     */
    private void queryVideoFormMediaStore() {
        // TODO: 2019/11/5 3.5.1 临时性修改
        Context context = getView().getContext();
        if (context == null)
            return;
        Cursor cursor = getView().getContext().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {

                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));// 地址
                int _id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));// id
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));// 大小
                long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));// 时长

                VideoBean videoBean = new VideoBean();
                videoBean.set_id(_id);
                videoBean.setVideoPath(path);
                videoBean.setVideoDuration(duration);
                videoBean.setVideoSize(size);
                saveVideoToDatabase(videoBean);
            }
            cursor.close();
        }
    }

    /**
     * 遍历需要扫描的目录
     */
    private void queryVideoFormStorage(List<String> scanFolderList) {
        if (scanFolderList.size() == 0)
            return;
        File[] fileArray = new File[scanFolderList.size()];
        for (int i = 0; i < scanFolderList.size(); i++) {
            fileArray[i] = new File(scanFolderList.get(i));
        }
        for (File parentFile : fileArray) {
            for (File childFile : listFiles(parentFile)) {
                String filePath = childFile.getAbsolutePath();
                VideoBean videoBean = new VideoBean();
                videoBean.setVideoPath(filePath);
                videoBean.setVideoDuration(0);
                videoBean.setVideoSize(childFile.length());
                videoBean.set_id(0);
                saveVideoToDatabase(videoBean);
            }
        }
    }

    /**
     * 递归检查目录和文件
     */
    private List<File> listFiles(File file) {
        List<File> fileList = new ArrayList<>();
        if (file.isDirectory()) {
            File[] fileArray = file.listFiles();
            if (fileArray == null || fileArray.length == 0) {
                return new ArrayList<>();
            } else {
                for (File childFile : fileArray) {
                    if (childFile.isDirectory()) {
                        fileList.addAll(listFiles(childFile));
                    } else if (childFile.exists() && childFile.canRead() && CommonUtils.isMediaFile(childFile.getAbsolutePath())) {
                        fileList.add(childFile);
                    }
                }
            }
        } else if (file.exists() && file.canRead() && CommonUtils.isMediaFile(file.getAbsolutePath())) {
            fileList.add(file);
        }
        return fileList;
    }
}
