package com.player.commom.bean;

import java.util.List;

public class SubtitleBean {

    private String name;
    private String url;
    private int rank;
    private String origin;
    private String language;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public static class Shooter {
        public static final String SHOOTER = "射手网";

        /**
         * Desc :
         * Delay : 0
         * Files : [{"Ext":"ass","Link":"https://www.shooter.cn/api/subapi.php?fetch=MTU1NzI5ODQ2M3xrTzFQVFNQZTZCVjVUNWJCSWlJSUEzU2k2Z3JFQi1LWngzdGJXdGV1cmhlcTctS0o4ZDVjVkRpUXQwc0F6c3FGbXc1S1Fod1djZDFNSUo5aDN1bi1CaGhtd0RkM2syVnVUbnk5RVk5VmZ6NEFpUThvTkNIWFltaHd5RE9XbWtzX0tDVVlzOFcyOTlzSmJfUFNQT0VsaGRleG9PY1B3cmtERUNsc1kwWFBtTzNWZVpKUk9sbUZnYWY5QkE9PXwOlpwyrL2N5iOX-dqlaFREBLBpUZH__vxdlUzyf6nUKA==&nonce=%EEV%D7m+%91%7D%27%C4d%BBi%8AT%99%DE"}]
         */

        private String Desc;
        private int Delay;
        private List<FilesBean> Files;

        public String getDesc() {
            return Desc;
        }

        public void setDesc(String Desc) {
            this.Desc = Desc;
        }

        public int getDelay() {
            return Delay;
        }

        public void setDelay(int Delay) {
            this.Delay = Delay;
        }

        public List<FilesBean> getFiles() {
            return Files;
        }

        public void setFiles(List<FilesBean> Files) {
            this.Files = Files;
        }

        public static class FilesBean {
            /**
             * Ext : ass
             * Link : https://www.shooter.cn/api/subapi.php?fetch=MTU1NzI5ODQ2M3xrTzFQVFNQZTZCVjVUNWJCSWlJSUEzU2k2Z3JFQi1LWngzdGJXdGV1cmhlcTctS0o4ZDVjVkRpUXQwc0F6c3FGbXc1S1Fod1djZDFNSUo5aDN1bi1CaGhtd0RkM2syVnVUbnk5RVk5VmZ6NEFpUThvTkNIWFltaHd5RE9XbWtzX0tDVVlzOFcyOTlzSmJfUFNQT0VsaGRleG9PY1B3cmtERUNsc1kwWFBtTzNWZVpKUk9sbUZnYWY5QkE9PXwOlpwyrL2N5iOX-dqlaFREBLBpUZH__vxdlUzyf6nUKA==&nonce=%EEV%D7m+%91%7D%27%C4d%BBi%8AT%99%DE
             */

            private String Ext;
            private String Link;

            public String getExt() {
                return Ext;
            }

            public void setExt(String Ext) {
                this.Ext = Ext;
            }

            public String getLink() {
                return Link;
            }

            public void setLink(String Link) {
                this.Link = Link;
            }
        }
    }

    public static class Thunder {
        public static final String THUNDER = "迅雷";

        private List<SublistBean> sublist;

        public List<SublistBean> getSublist() {
            return sublist;
        }

        public void setSublist(List<SublistBean> sublist) {
            this.sublist = sublist;
        }

        public static class SublistBean {
            /**
             * scid : 7D3B4991BD812C3F4C30485570DCACEDDF6BE184
             * sname : [DHR&Hakugetsu][Sword Art Online][01][BDRip][1080P][AVC_Hi10P_FLAC][FBDAC466].tc.ass
             * language : 繁体
             * rate : 3
             * surl : http://subtitle.v.geilijiasu.com/7D/3B/7D3B4991BD812C3F4C30485570DCACEDDF6BE184.ass
             * svote : 3457
             * roffset : 5426795912
             */

            private String scid;
            private String sname;
            private String language;
            private String rate;
            private String surl;
            private int svote;
            private long roffset;

            public String getScid() {
                return scid;
            }

            public void setScid(String scid) {
                this.scid = scid;
            }

            public String getSname() {
                return sname;
            }

            public void setSname(String sname) {
                this.sname = sname;
            }

            public String getLanguage() {
                return language;
            }

            public void setLanguage(String language) {
                this.language = language;
            }

            public int getRate() {
                try {
                    return Integer.valueOf(rate);
                }catch (NumberFormatException e){
                    return 0;
                }
            }

            public void setRate(String rate) {
                this.rate = rate;
            }

            public String getSurl() {
                return surl;
            }

            public void setSurl(String surl) {
                this.surl = surl;
            }

            public int getSvote() {
                return svote;
            }

            public void setSvote(int svote) {
                this.svote = svote;
            }

            public long getRoffset() {
                return roffset;
            }

            public void setRoffset(long roffset) {
                this.roffset = roffset;
            }
        }
    }
}