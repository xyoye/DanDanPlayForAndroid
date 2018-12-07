package com.xyoye.dandanplay.database;

/**
 *数据库表名，字段名，字段类型
 * Created by Administrator on 2015/12/29.
 */
public class DataBaseInfo {
    public static final String DATABASE_NAME = "db_data.db";
    public static final int DATABASE_VERSION = 11;

    private static String[][] FieldNames;
    private static String[][] FieldTypes;
    private static String[] TableNames;

    //表，字段名，字段类型
    static {

        TableNames = new String[]{
                "traverse_folder",
                "folder",
                "file",
                "banner",
                "anime_type",
                "subgroup",
                "torrent",
                "smb_file",
                "tracker"
        };

        FieldNames = new String[][] {
                {"_id", "folder_path"},
                {"_id", "folder_path", "file_number"},
                {"_id", "folder_path", "file_path", "danmu_path", "current_position", "duration", "danmu_episode_id"},
                {"_id", "title", "description", "url", "image_url"},
                {"_id", "type_id", "type_name"},
                {"_id", "subgroup_id", "subgroup_name"},
                {"_id", "torrent_path", "torrent_state", "torrent_done", "torrent_danmu_path", "torrent_episode_id", "torrent_magnet"},
                {"_id", "folder", "file_path", "danmu_path", "current_position", "danmu_episode_id"},
                {"_id", "tracker"}
        };

        FieldTypes = new String[][] {
                {"INTEGER PRIMARY KEY AUTOINCREMENT","VARCHAR(255) NOT NULL"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT","VARCHAR(255) NOT NULL","INTEGER NOT NULL"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT","VARCHAR(255) NOT NULL","VARCHAR(255) NOT NULL","VARCHAR(255)", "INTEGER", "VARCHAR(255) NOT NULL", "INTEGER" },
                {"INTEGER PRIMARY KEY AUTOINCREMENT", "VARCHAR(255)", "VARCHAR(255)", "VARCHAR(255)", "VARCHAR(255)"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT", "INTEGER", "VARCHAR(255)"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT", "INTEGER", "VARCHAR(255)"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT", "VARCHAR(255)", "TEXT", "INTEGER", "VARCHAR(255)", "INTEGER", "VARCHAR(255)"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT","VARCHAR(255) NOT NULL","VARCHAR(255) NOT NULL","VARCHAR(255)", "INTEGER", "INTEGER" },
                {"INTEGER PRIMARY KEY AUTOINCREMENT","VARCHAR(255) NOT NULL"}
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
