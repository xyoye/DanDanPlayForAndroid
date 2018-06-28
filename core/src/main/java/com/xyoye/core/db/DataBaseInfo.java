package com.xyoye.core.db;

/**
 *数据库表名，字段名，字段类型
 * Created by Administrator on 2015/12/29.
 */
public class DataBaseInfo {
    public static final String DATABASE_NAME = "db_data.db";
    public static final int DATABASE_VERSION = 1;

    private static String[][] FieldNames;
    private static String[][] FieldTypes;

    private static String[] TableNames;

    //表，字段名，字段类型
    static {
        //例："table_1","table_2"
        TableNames = new String[]{

        };

        //例：{"table_1_id","table_1_name"},
        //   {"table_2_id","table_2_name"}
        FieldNames = new String[][] {

        };

        //例：{"INTEGER PRIMARY KEY AUTOINCREMENT","VARCHAR(50) NOT NULL"},
        //   {"INTEGER PRIMARY KEY AUTOINCREMENT","VARCHAR(50) NOT NULL"}
        FieldTypes = new String[][] {

        };
    }

    public static String[][] getFieldNames() {
        return FieldNames;
    }

    public static String[][] getFieldTypes() {
        return FieldTypes;
    }

    public static String[] getTableNames() {
        return TableNames;
    }
}
