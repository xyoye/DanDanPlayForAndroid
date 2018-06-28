package com.xyoye.core.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yzd on 2015/7/28.
 */
public class DataBaseManager {

    private AtomicInteger mOpenCounter = new AtomicInteger();
    private static DataBaseManager instance;
    private static SQLiteOpenHelper mDatabaseHelper;
    private SQLiteDatabase mDatabase;

    public static synchronized void initializeInstance(SQLiteOpenHelper helper) {
        if (instance == null) {
            instance = new DataBaseManager();
            mDatabaseHelper = helper;
        }
    }

    public static synchronized DataBaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(DataBaseManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }

        return instance;
    }

    public synchronized SQLiteDatabase getSQLiteDatabase() {
        if (mDatabase == null || !mDatabase.isOpen()) {
            return openDatabase();
        }
        return mDatabase;
    }

    public synchronized SQLiteDatabase openDatabase() {
        if(mOpenCounter.incrementAndGet() == 1) {
            // Opening new database
            mDatabase = mDatabaseHelper.getWritableDatabase();
           // mDatabase.beginTransaction();
        }
        return mDatabase;
    }

    public synchronized void closeDatabase() {
        if(mOpenCounter.decrementAndGet() == 0) {
            // Closing database
           // mDatabase.endTransaction();
            mDatabase.close();
        }
    }
}
