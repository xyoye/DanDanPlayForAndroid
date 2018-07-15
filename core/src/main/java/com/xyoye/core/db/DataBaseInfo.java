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

        TableNames = new String[]{
                "traverse_folder",
                "folder",
                "file",
                "banner"
        };

        FieldNames = new String[][] {
                {"_id", "folder_path"},
                {"_id", "folder_path", "file_number"},
                {"_id", "folder_path", "file_name", "danmu_path", "current_position", "duration"},
                {"_id", "title", "description", "url", "image_url"}
        };

        FieldTypes = new String[][] {
                {"INTEGER PRIMARY KEY AUTOINCREMENT","VARCHAR(255) NOT NULL"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT","VARCHAR(255) NOT NULL","INTEGER NOT NULL"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT","VARCHAR(255) NOT NULL","VARCHAR(255) NOT NULL","VARCHAR(255)", "INTEGER", "VARCHAR(255) NOT NULL"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT", "VARCHAR(255)", "VARCHAR(255)", "VARCHAR(255)", "VARCHAR(255)"}
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
