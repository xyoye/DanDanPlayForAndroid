package com.xyoye.dandanplay.database;

/**
 *数据库表名，字段名，字段类型
 *
 * Modified by xyoye on 2015/12/29.
 */
public class DataBaseInfo {
    public static final String DATABASE_NAME = "db_data.db";
    public static final int DATABASE_VERSION = 23;

    private static String[][] FieldNames;
    private static String[][] FieldTypes;
    private static String[] TableNames;

    //表，字段名，字段类型
    static {

        TableNames = new String[]{
                "traverse_folder",  //0 废弃
                "folder",           //1 文件夹数据
                "file",             //2 文件数据
                "banner",           //3 废弃
                "anime_type",       //4 番剧分类
                "subgroup",         //5 字幕组
                "torrent",          //6 下载中的任务
                "smb_device",       //7 已连接的局域网设备
                "tracker",          //8 废弃
                "search_history",   //9 搜索历史
                "cloud_filter",     //10 云屏蔽数据
                "scan_folder",      //11 扫描文件夹
                "torrent_file",     //12 废弃
                "danmu_block",      //13 本地弹幕屏蔽数据
                "downloaded_task",  //14 已完成的任务
                "downloaded_file",  //15 已完成的任务中文件
        };

        FieldNames = new String[][] {
                {"_id", "folder_path"},
                {"_id", "folder_path", "file_number"},
                {"_id", "folder_path", "file_path", "danmu_path", "current_position", "duration", "danmu_episode_id", "file_size", "file_id"},
                {"_id", "title", "description", "url", "image_url"},
                {"_id", "type_id", "type_name"},
                {"_id", "subgroup_id", "subgroup_name"},
                {"_id", "torrent_path", "anime_title", "torrent_magnet", "isDone", "priorities"},
                {"_id", "device_name", "device_nick_name", "device_ip", "device_user_name", "device_user_password", "device_user_domain", "device_anonymous"},
                {"_id", "tracker"},
                {"_id", "text", "time"},
                {"_id", "filter"},
                {"_id", "folder_path", "folder_type"},
                {"_id", "torrent_path", "torrent_file_path", "danmu_path", "danmu_episode_id"},
                {"_id", "text"},
                {"_id", "title", "folder_path", "magnet", "total_size", "torrent_hash"},
                {"_id", "task_id", "file_path", "danmu_path", "danmu_episode_id"}
        };

        FieldTypes = new String[][] {
                {"INTEGER PRIMARY KEY AUTOINCREMENT", "VARCHAR(255) NOT NULL"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT", "VARCHAR(255) NOT NULL","INTEGER NOT NULL"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT", "VARCHAR(255) NOT NULL","VARCHAR(255) NOT NULL","VARCHAR(255)", "INTEGER", "VARCHAR(255) NOT NULL", "INTEGER","VARCHAR(255)", "INTEGER" },
                {"INTEGER PRIMARY KEY AUTOINCREMENT", "VARCHAR(255)", "VARCHAR(255)", "VARCHAR(255)", "VARCHAR(255)"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT", "INTEGER", "VARCHAR(255)"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT", "INTEGER", "VARCHAR(255)"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT", "VARCHAR(255)", "VARCHAR(255)", "VARCHAR(255)", "VARCHAR(255)", "INTEGER", "VARCHAR(255)"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT", "VARCHAR(255)", "VARCHAR(255)", "VARCHAR(255) NOT NULL", "VARCHAR(255)", "VARCHAR(255)", "VARCHAR(255)", "INTEGER"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT", "VARCHAR(255) NOT NULL"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT", "VARCHAR(255) NOT NULL", "INTEGER"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT", "VARCHAR(255) NOT NULL"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT", "VARCHAR(255) NOT NULL", "INTEGER"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT", "VARCHAR(255) NOT NULL", "VARCHAR(255) NOT NULL", "VARCHAR(255)", "INTEGER"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT", "VARCHAR(255) NOT NULL"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT", "VARCHAR(255) NOT NULL", "VARCHAR(255) NOT NULL", "VARCHAR(255) NOT NULL", "VARCHAR(255) NOT NULL", "VARCHAR(255) NOT NULL", "VARCHAR(255) NOT NULL"},
                {"INTEGER PRIMARY KEY AUTOINCREMENT", "INTEGER NOT NULL", "VARCHAR(255) NOT NULL", "VARCHAR(255)", "INTEGER"}
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
