package com.xyoye.dandanplay.bean;

/**
 * Created by xyoye on 2020/6/30.
 */

public class BiliBiliVideoInfo {

    /**
     * code : 0
     * data : {"title":"Android 的现代存储","cid":203126421}
     */

    private int code;
    private DataBean data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * title : Android 的现代存储
         * cid : 203126421
         */

        private String title;
        private int cid;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getCid() {
            return cid;
        }

        public void setCid(int cid) {
            this.cid = cid;
        }
    }
}
