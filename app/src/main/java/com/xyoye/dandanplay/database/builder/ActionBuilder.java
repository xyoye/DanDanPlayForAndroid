package com.xyoye.dandanplay.database.builder;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by xyoye on 2019/4/17.
 */
public class ActionBuilder{
    private int tablePosition;
    private SQLiteDatabase sqLiteDatabase;

    public ActionBuilder(int tablePosition, SQLiteDatabase sqLiteDatabase){
        this.sqLiteDatabase = sqLiteDatabase;
        this.tablePosition = tablePosition;
    }

    public InsertBuilder insert(){
        return new InsertBuilder(tablePosition, sqLiteDatabase);
    }

    public DeleteBuilder delete(){
        return new DeleteBuilder(tablePosition, sqLiteDatabase);
    }

    public QueryBuilder query(){
        return new QueryBuilder(tablePosition, sqLiteDatabase);
    }

    public UpdateBuilder update(){
        return new UpdateBuilder(tablePosition, sqLiteDatabase);
    }

}