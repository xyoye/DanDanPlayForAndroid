package com.xyoye.dandanplay.database;

/**
 *数据库表名，字段名，字段类型
 *
 * Modified by xyoye on 2015/12/29.
 */
public class DataBaseInfo {
    public static final String DATABASE_NAME = "db_data.db";
    public static final int DATABASE_VERSION = 21;

    private static String[][] FieldNames;
    private static String[][] FieldTypes;
    private static String[] TableNames;

    //表，字段名，字段类型
    static {

        TableNames = new String[]{
                "traverse_folder",  //0
                "folder",           //1
                "file",             //2
                "banner",           //3
                "anime_type",       //4
                "subgroup",         //5
                "torrent",          //6
                "smb_device",       //7
                "tracker",          //8 废弃
                "search_history",   //9
                "cloud_filter",     //10
                "scan_folder",      //11
                "torrent_file",     //12
                "danmu_block"       //13
        };

        FieldNames = new String[][] {
                {"_id", "folder_path"},
                {"_id", "folder_path", "file_number"},
                {"_id", "folder_path", "file_path", "danmu_path", "current_position", "duration", "danmu_episode_id", "file_size", "file_id"},
                {"_id", "title", "description", "url", "image_url"},
                {"_id", "type_id", "type_name"},
                {"_id", "subgroup_id", "subgroup_name"},
                {"_id", "torrent_path", "anime_title", "torrent_state", "torrent_done", "torrent_magnet"},
                {"_id", "device_name", "device_nick_name", "device_ip", "device_user_name", "device_user_password", "device_user_domain", "device_anonymous"},
                {"_id", "tracker"},
                {"_id", "text", "time"},
                {"_id", "filter"},
                {"_id", "folder_path", "folder_type"},
                {"_id", "torrent_path", "torrent_file_path", "danmu_path", "danmu_episode_id"},
                {"_id", "text"}
        };

        FieldTypes = new String[][] {
                {"INTEGER PRIMARY KEY AUTOINCREMENT","VARCHAR(255) NOT NULL"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT","VARCHAR(255) NOT NULL","INTEGER NOT NULL"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT","VARCHAR(255) NOT NULL","VARCHAR(255) NOT NULL","VARCHAR(255)", "INTEGER", "VARCHAR(255) NOT NULL", "INTEGER","VARCHAR(255)", "INTEGER" },
                {"INTEGER PRIMARY KEY AUTOINCREMENT", "VARCHAR(255)", "VARCHAR(255)", "VARCHAR(255)", "VARCHAR(255)"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT", "INTEGER", "VARCHAR(255)"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT", "INTEGER", "VARCHAR(255)"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT", "VARCHAR(255)", "VARCHAR(255)", "TEXT", "INTEGER", "VARCHAR(255)", "INTEGER", "VARCHAR(255)"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT", "VARCHAR(255)", "VARCHAR(255)", "VARCHAR(255) NOT NULL", "VARCHAR(255)", "VARCHAR(255)", "VARCHAR(255)", "INTEGER"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT","VARCHAR(255) NOT NULL"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT","VARCHAR(255) NOT NULL", "INTEGER"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT","VARCHAR(255) NOT NULL"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT","VARCHAR(255) NOT NULL", "INTEGER"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT","VARCHAR(255) NOT NULL", "VARCHAR(255) NOT NULL", "VARCHAR(255)", "INTEGER"},
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
