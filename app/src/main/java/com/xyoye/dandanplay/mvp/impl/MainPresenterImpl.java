package com.xyoye.dandanplay.mvp.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.AnimeTypeBean;
import com.xyoye.dandanplay.bean.SubGroupBean;
import com.xyoye.dandanplay.database.DataBaseInfo;
import com.xyoye.dandanplay.database.DataBaseManager;
import com.xyoye.dandanplay.mvp.presenter.MainPresenter;
import com.xyoye.dandanplay.mvp.view.MainView;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.CloudFilterHandler;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.net.CommOtherDataObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

/**
 * Created by YE on 2018/6/28 0028.
 */


public class MainPresenterImpl extends BaseMvpPresenterImpl<MainView> implements MainPresenter {

    public MainPresenterImpl(MainView view, Lifeful lifeful) {
        super(view, lifeful);
    }

    @Override
    public void init() {

    }

    @Override
    public void process(Bundle savedInstanceState) {
        initAnimeType();
        initSubGroup();
        long lastUpdateTime = AppConfig.getInstance().getUpdateFilterTime();
        long nowTime = System.currentTimeMillis();
        //七天更新一次云过滤列表
        if (nowTime - lastUpdateTime > 7 * 24 * 60 * 60 * 1000){
            AppConfig.getInstance().setUpdateFilterTime(nowTime);
            initCloudFilter();
        }else {
            getCloudFilter();
        }
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

    //番剧分类
    private void initAnimeType(){
        AnimeTypeBean.getAnimeType(new CommOtherDataObserver<AnimeTypeBean>(getLifeful()) {
            @Override
            public void onSuccess(AnimeTypeBean animeTypeBean) {
                if (animeTypeBean != null && animeTypeBean.getTypes() != null && animeTypeBean.getTypes().size() > 0){
                    SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
                    sqLiteDatabase.delete(DataBaseInfo.getTableNames()[4], "", new String[]{});

                    ContentValues firstValues = new ContentValues();
                    firstValues.put(DataBaseInfo.getFieldNames()[4][1], -1);
                    firstValues.put(DataBaseInfo.getFieldNames()[4][2], "全部");
                    sqLiteDatabase.insert(DataBaseInfo.getTableNames()[4], null, firstValues);

                    for (AnimeTypeBean.TypesBean typesBean : animeTypeBean.getTypes()){
                        ContentValues values = new ContentValues();
                        values.put(DataBaseInfo.getFieldNames()[4][1],typesBean.getId());
                        values.put(DataBaseInfo.getFieldNames()[4][2],typesBean.getName());
                        sqLiteDatabase.insert(DataBaseInfo.getTableNames()[4], null, values);
                    }
                }
            }

            @Override
            public void onError(int errorCode, String message) {
                LogUtils.e(message);
                ToastUtils.showShort(message);
            }
        }, new NetworkConsumer());
    }

    //字幕组
    private void initSubGroup(){
        SubGroupBean.getSubGroup(new CommOtherDataObserver<SubGroupBean>(getLifeful()) {
            @Override
            public void onSuccess(SubGroupBean subGroupBean) {
                if (subGroupBean != null && subGroupBean.getSubgroups() != null && subGroupBean.getSubgroups().size() > 0){
                    SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
                    sqLiteDatabase.delete(DataBaseInfo.getTableNames()[5], "", new String[]{});

                    //全部
                    ContentValues firstValues = new ContentValues();
                    firstValues.put(DataBaseInfo.getFieldNames()[5][1], -1);
                    firstValues.put(DataBaseInfo.getFieldNames()[5][2], "全部");
                    sqLiteDatabase.insert(DataBaseInfo.getTableNames()[5], null, firstValues);

                    for (SubGroupBean.SubgroupsBean subgroupsBean : subGroupBean.getSubgroups()){
                        ContentValues values = new ContentValues();
                        values.put(DataBaseInfo.getFieldNames()[5][1],subgroupsBean.getId());
                        values.put(DataBaseInfo.getFieldNames()[5][2],subgroupsBean.getName());
                        sqLiteDatabase.insert(DataBaseInfo.getTableNames()[5], null, values);
                    }
                }
            }

            @Override
            public void onError(int errorCode, String message) {
                LogUtils.e(message);
                ToastUtils.showShort(message);
            }
        }, new NetworkConsumer());
    }

    //弹幕云过滤
    private void initCloudFilter(){
        new Thread(() -> {
            List<String> filters = getFilterString();
            IApplication.cloudFilterList.addAll(filters);
            SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
            //清空数据库
            sqLiteDatabase.delete(DataBaseInfo.getTableNames()[10], "", new String[]{});
            ContentValues values = new ContentValues();
            for (int i=0; i<filters.size(); i++){
                values.put("filter", filters.get(i));
                //写入数据库
                sqLiteDatabase.insert(DataBaseInfo.getTableNames()[10], null, values);
            }
        }).start();
    }

    //获取保存的云过滤数据
    private void getCloudFilter(){
        //云屏蔽数据
        List<String> cloudFilter = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT filter FROM cloud_filter",new String[]{});
        while (cursor.moveToNext()){
            String text = cursor.getString(0);
            cloudFilter.add(text);
        }
        cursor.close();
        IApplication.cloudFilterList.addAll(cloudFilter);
    }

    /**
     * 下载xml
     */
    private List<String> getFilterString(){
        InputStream is = null;
        List<String> filter = new ArrayList<>();
        try {
            String xmlUrl = "https://api.acplay.net/config/filter.xml";
            URL url = new URL(xmlUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                is = conn.getInputStream();
                SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
                saxParser.parse(is, new CloudFilterHandler(filter::addAll));
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if (is != null)
                    is.close();
            }catch (IOException e){
                e.printStackTrace();
            }

        }
        return filter;
    }
}
