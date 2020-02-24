package com.xyoye.dandanplay.bean;

import com.xyoye.dandanplay.utils.net.CommShooterDataObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;
import com.xyoye.dandanplay.utils.net.RetroFactory;
import com.xyoye.dandanplay.utils.net.utils.ShooterNetworkUtils;

import java.util.List;

/**
 * Created by xyoye on 2020/2/23.
 */

public class ShooterSubtitleBean {

    /**
     * status : 0
     * sub : {"subs":[{"native_name":"老大哥（美版） 第17季第26集/Big Brother US S17E26","videoname":"big.brother.us.s17e26.720p.hdtv.x264-bajskorv","revision":0,"subtype":"VobSub","upload_time":"2015-08-21 22:11:00","vote_score":0,"id":594897,"release_site":"人人影视YYeTs","lang":{"langlist":{"langdou":true,"langkor":true},"desc":"韩  双语"}},{"native_name":"老大哥（美版） 第17季第24集/Big Brother US S14E24","videoname":"big.brother.us.s17e24.720p.hdtv.x264-bajskorv","revision":0,"subtype":"Subrip(srt)","upload_time":"2015-08-18 10:02:00","vote_score":0,"id":594867,"release_site":"人人影视YYeTs","lang":{"langlist":{"langdou":true,"langkor":true},"desc":"韩 双语"}}],"action":"search","keyword":"big","result":"succeed"}
     */

    private int status;
    private SubBean sub;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public SubBean getSub() {
        return sub;
    }

    public void setSub(SubBean sub) {
        this.sub = sub;
    }

    public static class SubBean {
        /**
         * subs : [{"native_name":"老大哥（美版） 第17季第26集/Big Brother US S17E26","videoname":"big.brother.us.s17e26.720p.hdtv.x264-bajskorv","revision":0,"subtype":"VobSub","upload_time":"2015-08-21 22:11:00","vote_score":0,"id":594897,"release_site":"人人影视YYeTs","lang":{"langlist":{"langdou":true,"langkor":true},"desc":"韩  双语"}},{"native_name":"老大哥（美版） 第17季第24集/Big Brother US S14E24","videoname":"big.brother.us.s17e24.720p.hdtv.x264-bajskorv","revision":0,"subtype":"Subrip(srt)","upload_time":"2015-08-18 10:02:00","vote_score":0,"id":594867,"release_site":"人人影视YYeTs","lang":{"langlist":{"langdou":true,"langkor":true},"desc":"韩 双语"}}]
         * action : search
         * keyword : big
         * result : succeed
         */

        private String action;
        private String keyword;
        private String result;
        private List<SubsBean> subs;

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public List<SubsBean> getSubs() {
            return subs;
        }

        public void setSubs(List<SubsBean> subs) {
            this.subs = subs;
        }

        public static class SubsBean {
            /**
             * native_name : 老大哥（美版） 第17季第26集/Big Brother US S17E26
             * videoname : big.brother.us.s17e26.720p.hdtv.x264-bajskorv
             * revision : 0
             * subtype : VobSub
             * upload_time : 2015-08-21 22:11:00
             * vote_score : 0
             * id : 594897
             * release_site : 人人影视YYeTs
             * lang : {"langlist":{"langdou":true,"langkor":true},"desc":"韩  双语"}
             */

            private String native_name;
            private String videoname;
            private int revision;
            private String subtype;
            private String upload_time;
            private int vote_score;
            private int id;
            private String release_site;
            private LangBean lang;

            public String getNative_name() {
                return native_name;
            }

            public void setNative_name(String native_name) {
                this.native_name = native_name;
            }

            public String getVideoname() {
                return videoname;
            }

            public void setVideoname(String videoname) {
                this.videoname = videoname;
            }

            public int getRevision() {
                return revision;
            }

            public void setRevision(int revision) {
                this.revision = revision;
            }

            public String getSubtype() {
                return subtype;
            }

            public void setSubtype(String subtype) {
                this.subtype = subtype;
            }

            public String getUpload_time() {
                return upload_time;
            }

            public void setUpload_time(String upload_time) {
                this.upload_time = upload_time;
            }

            public int getVote_score() {
                return vote_score;
            }

            public void setVote_score(int vote_score) {
                this.vote_score = vote_score;
            }

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getRelease_site() {
                return release_site;
            }

            public void setRelease_site(String release_site) {
                this.release_site = release_site;
            }

            public LangBean getLang() {
                return lang;
            }

            public void setLang(LangBean lang) {
                this.lang = lang;
            }

            public static class LangBean {
                /**
                 * langlist : {"langdou":true,"langkor":true}
                 * desc : 韩  双语
                 */

                private LanglistBean langlist;
                private String desc;

                public LanglistBean getLanglist() {
                    return langlist;
                }

                public void setLanglist(LanglistBean langlist) {
                    this.langlist = langlist;
                }

                public String getDesc() {
                    return desc;
                }

                public void setDesc(String desc) {
                    this.desc = desc;
                }

                public static class LanglistBean {
                    /**
                     * langdou : true
                     * langkor : true
                     */

                    private boolean langdou;
                    private boolean langkor;

                    public boolean isLangdou() {
                        return langdou;
                    }

                    public void setLangdou(boolean langdou) {
                        this.langdou = langdou;
                    }

                    public boolean isLangkor() {
                        return langkor;
                    }

                    public void setLangkor(boolean langkor) {
                        this.langkor = langkor;
                    }
                }
            }
        }
    }

    public static void searchSubtitle(String token, String text, int page, CommShooterDataObserver<ShooterSubtitleBean> observer, NetworkConsumer consumer) {
        RetroFactory.getShooterInstance().searchSubtitle(token, text, page)
                .doOnSubscribe(consumer)
                .compose(ShooterNetworkUtils.network())
                .subscribe(observer);
    }
}
