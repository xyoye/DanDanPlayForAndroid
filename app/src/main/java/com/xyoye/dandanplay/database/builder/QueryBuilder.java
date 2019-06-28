package com.xyoye.dandanplay.database.builder;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.xyoye.dandanplay.database.DataBaseInfo;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.CheckReturnValue;

/**
 * Created by xyoye on 2019/4/17.
 */
public class QueryBuilder{
    private SQLiteDatabase sqLiteDatabase;
    private int tablePosition;
    private int[] columns;
    private List<String> whereClause;
    private List<String> whereArgs;
    private int groupColumn = -1;
    private String having = null;
    private boolean isAsc = true;
    private int orderByColumn = -1;
    private String limit = null;

    QueryBuilder(int tablePosition, SQLiteDatabase sqLiteDatabase){
        this.tablePosition = tablePosition;
        this.sqLiteDatabase = sqLiteDatabase;
        whereClause = new ArrayList<>();
        whereArgs = new ArrayList<>();
    }

    @CheckReturnValue
    public QueryBuilder setColumns(int... columns){
        this.columns = columns;
        return this;
    }

    @CheckReturnValue
    public QueryBuilder where(int column, String value) {
        String whereClauseText = DataBaseInfo.getFieldNames()[tablePosition][column] + " = ?";
        whereClause.add(whereClauseText);
        whereArgs.add(value);
        return this;
    }

    @CheckReturnValue
    public QueryBuilder setGroupByColumn(int column){
        this.groupColumn = column;
        return this;
    }

    @CheckReturnValue
    public QueryBuilder setHaving(String having){
        this.having = having;
        return this;
    }

    @CheckReturnValue
    public QueryBuilder setOrderByColumnAsc(int column){
        isAsc = true;
        this.orderByColumn = column;
        return this;
    }

    @CheckReturnValue
    public QueryBuilder setOrderByColumnDesc(int column){
        isAsc = false;
        this.orderByColumn = column;
        return this;
    }

    @CheckReturnValue
    public QueryBuilder setLimit(String limit){
        this.limit = limit;
        return this;
    }

    @CheckReturnValue
    public Cursor execute(){

        //select columns
        String[] columnsText = null;
        if (columns != null && columns.length > 0){
            columnsText = new String[columns.length];
            for (int i = 0; i < columns.length; i++) {
                columnsText[i] = DataBaseInfo.getFieldNames()[tablePosition][columns[i]];
            }
        }

        //selection
        String clause = null;
        String[] args = null;
        if (whereArgs.size() > 0){
            args = new String[whereClause.size()];
            StringBuilder clauseBuilder = new StringBuilder();
            for (int i = 0; i < whereClause.size(); i++) {
                clauseBuilder.append(whereClause.get(i)).append(" AND ");
                args[i] = whereArgs.get(i);
            }
            if (clauseBuilder.length() > 5){
                clause = clauseBuilder.substring(0, clauseBuilder.length()-5);
            }
        }

        //group by
        String groupByText = null;
        if (groupColumn != -1){
            groupByText = DataBaseInfo.getFieldNames()[tablePosition][groupColumn];
        }

        //order by
        String orderByText = null;
        if (orderByColumn != -1){
            orderByText = DataBaseInfo.getFieldNames()[tablePosition][orderByColumn];
        }
        if (isAsc){
            orderByText = orderByText == null ? null : orderByText+" ASC";
        }else {
            orderByText = orderByText == null ? null : orderByText+" DESC";
        }

        return sqLiteDatabase.query(DataBaseInfo.getTableNames()[tablePosition], columnsText, clause, args, groupByText, having, orderByText, limit);
    }
}