package com.xyoye.dandanplay.utils.database.builder;

import android.database.sqlite.SQLiteDatabase;

import io.reactivex.annotations.CheckReturnValue;

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

    @CheckReturnValue
    public InsertBuilder insert(){
        return new InsertBuilder(tablePosition, sqLiteDatabase);
    }

    @CheckReturnValue
    public DeleteBuilder delete(){
        return new DeleteBuilder(tablePosition, sqLiteDatabase);
    }

    @CheckReturnValue
    public QueryBuilder query(){
        return new QueryBuilder(tablePosition, sqLiteDatabase);
    }

    @CheckReturnValue
    public UpdateBuilder update(){
        return new UpdateBuilder(tablePosition, sqLiteDatabase);
    }

}