package com.xyoye.core.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public abstract class DataBaseService {

	protected Context ctx = null;
	protected SQLiteDatabase mSQLiteDatabase = null;

	public DataBaseService(Context ctx) {
		this.ctx = ctx.getApplicationContext();
		this.mSQLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
	}
	
	public void open() {
		mSQLiteDatabase = DataBaseManager.getInstance().openDatabase();
	}
	
	public void close() {
		DataBaseManager.getInstance().closeDatabase();
	}

}
