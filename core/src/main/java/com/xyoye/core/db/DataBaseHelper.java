package com.xyoye.core.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper {

	private static String[][] FieldNames;
	private static String[][] FieldTypes;
	private static String[] TableNames = DataBaseInfo.getTableNames();

	static {
		FieldNames = DataBaseInfo.getFieldNames();
		FieldTypes = DataBaseInfo.getFieldTypes();
	}

	public DataBaseHelper(Context context) {
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
