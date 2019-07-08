package com.xyoye.dandanplay.mvp.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;

import com.blankj.utilcode.util.FileUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.FolderBean;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.database.DataBaseInfo;
import com.xyoye.dandanplay.database.DataBaseManager;
import com.xyoye.dandanplay.mvp.presenter.PlayFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.PlayFragmentView;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.Lifeful;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xyoye on 2018/6/29.
 */

public class PlayFragmentPresenterImpl extends BaseMvpPresenterImpl<PlayFragmentView> implements PlayFragmentPresenter {
    private Disposable querySqlFileDis, refreshFileDis;

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
        if (querySqlFileDis != null)
            querySqlFileDis.dispose();
        if (refreshFileDis != null)
            refreshFileDis.dispose();
    }

    @Override
    public VideoBean getLastPlayVideo(String videoPath) {
        VideoBean videoBean = null;
        Cursor cursor = DataBaseManager.getInstance()
                .selectTable(2)
                .query()
                .setColumns(3, 4, 6)
                .where(2, videoPath)
                .execute();
        if (cursor.moveToNext()){
            videoBean = new VideoBean();
            videoBean.setVideoPath(videoPath);
            videoBean.setDanmuPath(cursor.getString(0));
            videoBean.setCurrentPosition(1);
            videoBean.setEpisodeId(cursor.getInt(2));
        }

        return videoBean;
    }

    @Override
    public void refreshVideo(boolean reScan) {
        if (reScan){
            scanAndRefreshVideo();
            return;
        }
        refreshVideo();
    }

    @Override
    public void deleteFolder(String folderPath) {
        DataBaseManager.getInstance()
                .selectTable(11)
                .insert()
                .param(1, folderPath)
                .param(2, "0")
                .execute();
    }

    /**
     * 仅根据数据库数据刷新界面
     */
    private void refreshVideo(){
        querySqlFileDis = Observable
                .create((ObservableOnSubscribe<List<FolderBean>>) emitter ->
                        emitter.onNext(getVideoFormDatabase()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(videoList -> getView().refreshAdapter(videoList));
    }

    /**
     * 扫描所有文件，更新数据库，刷新界面数据
     */
    private void scanAndRefreshVideo(){
        //获取需要扫描的目录
        List<String> scanFolderList = getScanFolder(Constants.ScanType.SCAN);
        boolean isScanMediaStore = scanFolderList.remove(Constants.DefaultConfig.SYSTEM_VIDEO_PATH);

        refreshFileDis = Observable.just(isScanMediaStore)
                //刷新系统文件
                .map(result -> queryVideoFormMediaStore(isScanMediaStore))
                //遍历需要扫描的目录
                .zipWith(flatMapFile(scanFolderList),
                        (aBoolean, aBoolean2) -> getVideoFormDatabase())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(videoList -> getView().refreshAdapter(videoList));
    }

    /**
     * 从数据库中读取文件夹目录，过滤屏蔽目录及不扫描目录
     */
    private List<FolderBean> getVideoFormDatabase(){
        List<FolderBean> folderBeanList = new ArrayList<>();
        Map<String, Integer> beanMap = new HashMap<>();
        Map<String, String> deleteMap = new HashMap<>();

        //查询所有屏蔽目录
        List<String> blockList = getScanFolder(Constants.ScanType.BLOCK);
        //查询所有扫描目录
        List<String> scanList = getScanFolder(Constants.ScanType.SCAN);

        //查询所有视频
        Cursor cursor = DataBaseManager.getInstance()
                .selectTable(2)
                .query()
                .setColumns(1, 2)
                .execute();
        while (cursor.moveToNext()){
            String folderPath = cursor.getString(0);
            String filePath = cursor.getString(1);

            //过滤屏蔽目录
            boolean isBlock = false;
            for (String blockPath : blockList){
                //视频属于屏蔽目录下视频，过滤
                if (filePath.startsWith(blockPath)){
                    isBlock = true;
                    break;
                }
            }
            if (isBlock) continue;

            //过滤非扫描目录，扫描包括系统目录时不用过滤
            if (!scanList.contains(Constants.DefaultConfig.SYSTEM_VIDEO_PATH)){
                boolean isNotScan = true;
                for (String scanPath : scanList){
                    if (filePath.startsWith(scanPath)){
                        isNotScan = false;
                        break;
                    }
                }
                if (isNotScan) continue;
            }

            //计算文件夹中文件数量
            //文件不存在记录需要删除的文件
            File file = new File(filePath);
            if (file.exists()){
                if (beanMap.containsKey(folderPath)){
                    int number = beanMap.get(folderPath);
                    beanMap.put(folderPath, ++number);
                }else {
                    beanMap.put(folderPath, 1);
                }
            }else {
                deleteMap.put(folderPath, filePath);
            }
        }
        cursor.close();

        //更新文件夹文件数量
        for (Map.Entry<String, Integer> entry : beanMap.entrySet()){
            folderBeanList.add(new FolderBean(entry.getKey(), entry.getValue()));
            DataBaseManager.getInstance()
                    .selectTable(1)
                    .update()
                    .param(2, entry.getValue())
                    .where(1, entry.getKey())
                    .execute();
        }

        //删除不存在的文件
        for (Map.Entry<String, String> entry : deleteMap.entrySet()){
            DataBaseManager.getInstance()
                    .selectTable(2)
                    .delete()
                    .where(1, entry.getKey())
                    .where(2, entry.getValue())
                    .execute();
        }
        return folderBeanList;
    }

    /**
     * 获取扫描或屏蔽目录
     * @param scanType ScanType.BLOCK || ScanType.SCAN
     */
    private List<String> getScanFolder(String scanType){
        List<String> folderList = new ArrayList<>();
        //查询屏蔽目录
        Cursor blockCursor = DataBaseManager.getInstance()
                .selectTable(11)
                .query()
                .setColumns(1)
                .where(2, scanType)
                .execute();

        //获取所有屏蔽目录
        while (blockCursor.moveToNext()){
            folderList.add(blockCursor.getString(0));
        }
        blockCursor.close();
        return folderList;
    }

    /**
     * 保存视频信息到数据库
     * 跳过已存在的视频信息
     */
    private void saveVideoToDatabase(VideoBean videoBean){
        String folderPath = FileUtils.getDirName(videoBean.getVideoPath());
        ContentValues values=new ContentValues();
        values.put(DataBaseInfo.getFieldNames()[2][1], folderPath);
        values.put(DataBaseInfo.getFieldNames()[2][2], videoBean.getVideoPath());
        values.put(DataBaseInfo.getFieldNames()[2][5], String.valueOf(videoBean.getVideoDuration()));
        values.put(DataBaseInfo.getFieldNames()[2][7], String.valueOf(videoBean.getVideoSize()));
        values.put(DataBaseInfo.getFieldNames()[2][8], videoBean.get_id());

        Cursor cursor = DataBaseManager.getInstance()
                .selectTable(2)
                .query()
                .where(1, folderPath)
                .where(2, videoBean.getVideoPath())
                .execute();
        if (!cursor.moveToNext()) {
            DataBaseManager.getInstance()
                    .selectTable(2)
                    .insert()
                    .param(1, folderPath)
                    .param(2, videoBean.getVideoPath())
                    .param(5, String.valueOf(videoBean.getVideoDuration()))
                    .param(7, String.valueOf(videoBean.getVideoSize()))
                    .param(8, videoBean.get_id())
                    .postExecute();
        }
        cursor.close();
    }

    /**
     * 获取系统中视频信息
     */
    private boolean queryVideoFormMediaStore(boolean isScanMediaStore){
        if (!isScanMediaStore)
            return false;
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
        return true;
    }

    /**
     * 将需要扫描的目录集合转换为文件数组
     */
    private File[] getScanFolderFiles(List<String> folderList){
        File[] files = new File[folderList.size()];

        for (int i = 0; i < folderList.size(); i++) {
            files[i] = new File(folderList.get(i));
        }
        return files;
    }

    /**
     * 遍历需要扫描的目录
     */
    private Observable<Boolean> flatMapFile(List<String> scanFolderList){
        if (scanFolderList.size() == 0)
            return Observable.just(true);
        return Observable.just(getScanFolderFiles(scanFolderList))
                .flatMap((Function<File[], ObservableSource<File>>)
                        fileList -> Observable
                                .fromArray(fileList)
                                .flatMap(this::listFiles)
                )
                .map(file -> {
                    String filePath = file.getAbsolutePath();
                    VideoBean videoBean = new VideoBean();
                    videoBean.setVideoPath(filePath);
                    videoBean.setVideoDuration(0);
                    videoBean.setVideoSize(file.length());
                    videoBean.set_id(0);
                    saveVideoToDatabase(videoBean);
                    return true;
                })
                .switchIfEmpty(Observable.just(true));
    }

    /**
     * 递归检查目录和文件
     */
    private Observable<File> listFiles(final File f){
        if(f != null && f.isDirectory()){
            return Observable
                    .fromArray(f.listFiles())
                    .flatMap(this::listFiles);
        } else {
            File fileTemp = f == null ? new File("") : f;
            return Observable
                    .just(fileTemp)
                    .filter(file -> file != null && file.exists() && file.canRead() && CommonUtils.isMediaFile(file.getAbsolutePath()));
        }
    }
}
