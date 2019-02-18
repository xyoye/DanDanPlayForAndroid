package com.xyoye.dandanplay.ui.activities;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.jaeger.library.StatusBarUtil;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.ScanFolderBean;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.bean.event.ListFolderEvent;
import com.xyoye.dandanplay.database.DataBaseInfo;
import com.xyoye.dandanplay.database.DataBaseManager;
import com.xyoye.dandanplay.ui.weight.dialog.FileManagerDialog;
import com.xyoye.dandanplay.ui.weight.item.VideoScanItem;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class VideoScanActivity extends AppCompatActivity {

    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.folder_rv)
    RecyclerView folderRv;
    @BindView(R.id.delete_tv)
    TextView deleteTv;

    private BaseRvAdapter<ScanFolderBean> adapter;
    private List<ScanFolderBean> folderList;

    private VideoScanItem.OnFolderCheckListener onItemCheckListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_scan);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        StatusBarUtil.setColor(this, ContextCompat.getColor(this, R.color.theme_color), 0);

        setTitle("扫描管理");

        onItemCheckListener = (isCheck, position) -> {
            folderList.get(position).setCheck(isCheck);
            if (isCheck){
                deleteTv.setTextColor(VideoScanActivity.this.getResources().getColor(R.color.theme_color));
                deleteTv.setClickable(true);
            }else {
                for (ScanFolderBean bean : folderList){
                    if (bean.isCheck()){
                        deleteTv.setTextColor(VideoScanActivity.this.getResources().getColor(R.color.theme_color));
                        deleteTv.setClickable(true);
                        return;
                    }
                }
                deleteTv.setTextColor(VideoScanActivity.this.getResources().getColor(R.color.text_gray));
                deleteTv.setClickable(false);
            }
        };

        folderList = new ArrayList<>();
        adapter = new BaseRvAdapter<ScanFolderBean>(folderList) {
            @NonNull
            @Override
            public AdapterItem<ScanFolderBean> onCreateItem(int viewType) {
                return new VideoScanItem(onItemCheckListener);
            }
        };
        folderRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        folderRv.setNestedScrollingEnabled(false);
        folderRv.setItemViewCacheSize(10);
        folderRv.setAdapter(adapter);

        queryScanFolder();
    }

    @OnClick({R.id.scan_folder_tv, R.id.scan_file_tv, R.id.delete_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.scan_folder_tv:
                new FileManagerDialog(VideoScanActivity.this, FileManagerDialog.SELECT_FOLDER, this::listFolder).show();
                break;
            case R.id.scan_file_tv:
                new FileManagerDialog(VideoScanActivity.this, FileManagerDialog.SELECT_VIDEO, path -> {
                    VideoBean videoBean = new VideoBean();
                    queryFormSystem(videoBean, path);
                    boolean added = saveData(videoBean);
                    if (added)
                        EventBus.getDefault().post(new ListFolderEvent());
                    ToastUtils.showShort(added ? "扫描成功" : "文件已存在");
                }).show();
                break;
            case R.id.delete_tv:
                for (ScanFolderBean bean : folderList) {
                    if (bean.isCheck()) {
                        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
                        sqLiteDatabase.delete(DataBaseInfo.getTableNames()[11], DataBaseInfo.getFieldNames()[11][1] + " = ?", new String[]{bean.getFolder()});
                    }
                }
                queryScanFolder();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.add_scan:
                new FileManagerDialog(VideoScanActivity.this, FileManagerDialog.SELECT_FOLDER, path -> {
                    for (ScanFolderBean bean : folderList){
                        if (path.contains(bean.getFolder())){
                            ToastUtils.showShort("已在扫描范围内");
                            return;
                        }
                    }
                    SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
                    ContentValues values = new ContentValues();
                    values.put(DataBaseInfo.getFieldNames()[11][1], path);
                    sqLiteDatabase.insert(DataBaseInfo.getTableNames()[11], null, values);
                    queryScanFolder();
                }).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scan, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    private void queryScanFolder() {
        folderList.clear();
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        Cursor cursor = sqLiteDatabase.query(DataBaseInfo.getTableNames()[11], null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            folderList.add(new ScanFolderBean(cursor.getString(1), false));
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("CheckResult")
    public void listFolder(String path) {
        File rootFile = new File(path);
        Observable.just(rootFile)
                .flatMap(this::listFiles)
                .map(file -> {
                    VideoBean videoBean = new VideoBean();
                    queryFormSystem(videoBean, file.getAbsolutePath());
                    return saveData(videoBean);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                    EventBus.getDefault().post(new ListFolderEvent());
                    ToastUtils.showShort("扫描完成");
                });
    }

    //递归查询内存中的视频文件
    private Observable<File> listFiles(final File f){
        if(f.isDirectory()){
            return Observable
                    .fromArray(f.listFiles())
                    .flatMap(this::listFiles);
        } else {
            return Observable
                    .just(f)
                    .filter(file -> f.exists() && f.canRead() && CommonUtils.isMediaFile(f.getAbsolutePath()));
        }
    }

    //保存数据库中不存在的文件到数据库
    private boolean saveData(VideoBean videoBean){
        String folderPath = FileUtils.getDirName(videoBean.getVideoPath());
        ContentValues values=new ContentValues();
        values.put(DataBaseInfo.getFieldNames()[2][1], folderPath);
        values.put(DataBaseInfo.getFieldNames()[2][2], videoBean.getVideoPath());
        values.put(DataBaseInfo.getFieldNames()[2][5], String.valueOf(videoBean.getVideoDuration()));
        values.put(DataBaseInfo.getFieldNames()[2][7], String.valueOf(videoBean.getVideoSize()));
        values.put(DataBaseInfo.getFieldNames()[2][8], videoBean.get_id());
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        String sql = "SELECT * FROM "+DataBaseInfo.getTableNames()[2]+
                " WHERE "+DataBaseInfo.getFieldNames()[2][1]+ "=? " +
                "AND "+DataBaseInfo.getFieldNames()[2][2]+ "=? ";
        Cursor cursor = sqLiteDatabase.rawQuery(sql, new String[]{folderPath, videoBean.getVideoPath()});
        if (!cursor.moveToNext()) {
            sqLiteDatabase.insert(DataBaseInfo.getTableNames()[2], null, values);
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    //查询系统中是否保存对应视频数据
    public void queryFormSystem(VideoBean videoBean, String path){
        Cursor cursor = VideoScanActivity.this.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media._ID, MediaStore.Video.Media.DURATION},
                MediaStore.Video.Media.DATA+" = ?",
                new String[]{path}, null);
        File file = new File(path);
        videoBean.setVideoPath(path);
        videoBean.setVideoSize(file.length());
        if (cursor != null && cursor.moveToNext()){
            videoBean.setVideoDuration(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)));
            videoBean.set_id(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)));
            cursor.close();
        }else {
            if (cursor != null)
                cursor.close();
            videoBean.setVideoDuration(0);
            videoBean.set_id(0);
        }
    }
}
