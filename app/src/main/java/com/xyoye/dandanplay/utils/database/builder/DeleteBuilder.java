package com.xyoye.dandanplay.utils.database.builder;

import android.database.sqlite.SQLiteDatabase;

import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.utils.database.DataBaseInfo;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.CheckReturnValue;

/**
 * Created by xyoye on 2019/4/17.
 */
public class DeleteBuilder{
    private SQLiteDatabase sqLiteDatabase;
    private int tablePosition;
    private List<String> whereClause;
    private List<String> whereArgs;

    DeleteBuilder(int tablePosition, SQLiteDatabase sqLiteDatabase){
        this.sqLiteDatabase = sqLiteDatabase;
        this.tablePosition = tablePosition;
        whereClause = new ArrayList<>();
        whereArgs = new ArrayList<>();
    }

    @CheckReturnValue
    public DeleteBuilder where(int column, String value) {
        String whereClauseText = DataBaseInfo.getFieldNames()[tablePosition][column] + " = ?";
        whereClause.add(whereClauseText);
        whereArgs.add(value);
        return this;
    }

    public int execute(){

        // clauseList -> "clause1 = ? AND clause2 = ?"
        String clause;
        String[] args = new String[whereClause.size()];
        StringBuilder clauseBuilder = new StringBuilder();
        for (int i = 0; i < whereClause.size(); i++) {
            clauseBuilder.append(whereClause.get(i)).append(" AND ");
            args[i] = whereArgs.get(i);
        }
        if (clauseBuilder.length() > 5){
            clause = clauseBuilder.substring(0, clauseBuilder.length()-5);
        }else {
            clause = "";
        }

        return sqLiteDatabase.delete(DataBaseInfo.getTableNames()[tablePosition], clause , args);
    }

    public void postExecute(){
        IApplication.getExecutor().execute(this::execute);
    }

}