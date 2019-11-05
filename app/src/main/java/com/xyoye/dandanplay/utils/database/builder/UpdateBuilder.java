package com.xyoye.dandanplay.utils.database.builder;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.utils.database.DataBaseInfo;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.CheckReturnValue;

/**
 * Created by xyoye on 2019/4/17.
 */
public class UpdateBuilder {
    private SQLiteDatabase sqLiteDatabase;
    private int tablePosition;
    private ContentValues mValues;
    private List<String> whereClause;
    private List<String> whereArgs;

    UpdateBuilder(int tablePosition, SQLiteDatabase sqLiteDatabase) {
        this.sqLiteDatabase = sqLiteDatabase;
        this.tablePosition = tablePosition;
        mValues = new ContentValues();
        whereArgs = new ArrayList<>();
        whereClause = new ArrayList<>();
    }

    @CheckReturnValue
    public UpdateBuilder where(int column, String value) {
        String whereClauseText = DataBaseInfo.getFieldNames()[tablePosition][column] + " = ?";
        whereClause.add(whereClauseText);
        whereArgs.add(value);
        return this;
    }

    @CheckReturnValue
    public UpdateBuilder where(String colName, String value) {
        DataBaseInfo.checkColumnName(colName, tablePosition);
        String whereClauseText = colName + " = ?";
        whereClause.add(whereClauseText);
        whereArgs.add(value);
        return this;
    }

    @CheckReturnValue
    public UpdateBuilder param(String colName, String value) {
        DataBaseInfo.checkColumnName(colName, tablePosition);
        mValues.put(colName, value);
        return this;
    }

    @CheckReturnValue
    public UpdateBuilder param(String colName, Byte value) {
        DataBaseInfo.checkColumnName(colName, tablePosition);
        mValues.put(colName, value);
        return this;
    }

    @CheckReturnValue
    public UpdateBuilder param(String colName, Short value) {
        DataBaseInfo.checkColumnName(colName, tablePosition);
        mValues.put(colName, value);
        return this;
    }

    @CheckReturnValue
    public UpdateBuilder param(String colName, Integer value) {
        DataBaseInfo.checkColumnName(colName, tablePosition);
        mValues.put(colName, value);
        return this;
    }

    @CheckReturnValue
    public UpdateBuilder param(String colName, Long value) {
        DataBaseInfo.checkColumnName(colName, tablePosition);
        mValues.put(colName, value);
        return this;
    }

    @CheckReturnValue
    public UpdateBuilder param(String colName, Float value) {
        DataBaseInfo.checkColumnName(colName, tablePosition);
        mValues.put(colName, value);
        return this;
    }

    @CheckReturnValue
    public UpdateBuilder param(String colName, Double value) {
        DataBaseInfo.checkColumnName(colName, tablePosition);
        mValues.put(colName, value);
        return this;
    }

    @CheckReturnValue
    public UpdateBuilder param(String colName, Boolean value) {
        DataBaseInfo.checkColumnName(colName, tablePosition);
        mValues.put(colName, value);
        return this;
    }

    @CheckReturnValue
    public UpdateBuilder param(String colName, byte[] value) {
        DataBaseInfo.checkColumnName(colName, tablePosition);
        mValues.put(colName, value);
        return this;
    }

    public synchronized void executeAsync() {
        ActionBuilder.checkThreadLocal();

        if (mValues == null)
            return;

        String clause;
        String[] args = new String[whereClause.size()];
        StringBuilder clauseBuilder = new StringBuilder();
        for (int i = 0; i < whereClause.size(); i++) {
            clauseBuilder.append(whereClause.get(i)).append(" AND ");
            args[i] = whereArgs.get(i);
        }
        if (clauseBuilder.length() > 5) {
            clause = clauseBuilder.substring(0, clauseBuilder.length() - 5);
        } else {
            clause = "";
        }
        if (sqLiteDatabase.isOpen())
            sqLiteDatabase.update(DataBaseInfo.getTableNames()[tablePosition], mValues, clause, args);
    }

    public void postExecute() {
        IApplication.getSqlThreadPool().execute(this::executeAsync);
    }
}