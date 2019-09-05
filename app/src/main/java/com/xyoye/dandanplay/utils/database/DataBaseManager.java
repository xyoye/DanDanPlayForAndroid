package com.xyoye.dandanplay.utils.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.xyoye.dandanplay.utils.database.builder.ActionBuilder;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Modified by xyoye on 2017/7/28.
 */
public class DataBaseManager {

    private AtomicInteger mOpenCounter = new AtomicInteger();
    private static DataBaseManager instance;
    private static SQLiteOpenHelper mDatabaseHelper;
    private SQLiteDatabase mDatabase;

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new DataBaseManager();
            mDatabaseHelper = new DataBaseHelper(context);
        }
    }

    public static synchronized DataBaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(DataBaseManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }

        return instance;
    }

    public synchronized void closeDatabase() {
        if(mOpenCounter.decrementAndGet() == 0) {
            mDatabase.close();
        }
    }

    //选择操作表
    public ActionBuilder selectTable(int tablePosition){
        return new ActionBuilder(tablePosition, getSQLiteDatabase());
    }

    private synchronized SQLiteDatabase getSQLiteDatabase() {
        if (mDatabase == null || !mDatabase.isOpen()) {
            return openDatabase();
        }
        return mDatabase;
    }

    private synchronized SQLiteDatabase openDatabase() {
        if(mOpenCounter.incrementAndGet() == 1) {
            mDatabase = mDatabaseHelper.getWritableDatabase();
        }
        return mDatabase;
    }

    //创建数据库
    static class DataBaseHelper extends SQLiteOpenHelper {
        private static String[][] FieldNames;
        private static String[][] FieldTypes;
        private static String[] TableNames = DataBaseInfo.getTableNames();

        static {
            FieldNames = DataBaseInfo.getFieldNames();
            FieldTypes = DataBaseInfo.getFieldTypes();
        }

        DataBaseHelper(Context context) {
            super(context, DataBaseInfo.DATABASE_NAME, null, DataBaseInfo.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            if (TableNames == null) return;
            String str1;
            String str2;
            for (int i = 0; i < TableNames.length; i++) {
                str1 = "CREATE TABLE " + TableNames[i] + " (";
                for (int j = 0; j < FieldNames[i].length; j++) {
                    str1 = str1 + FieldNames[i][j] + " " + FieldTypes[i][j] + ",";
                }
                str2 = str1.substring(0, str1.length() - 1) + ");";
                db.execSQL(str2);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i("db", "updata");
            for (String tab : TableNames) {
                db.execSQL("DROP TABLE IF EXISTS " + tab + ";");
            }
            onCreate(db);
        }

    }
}
