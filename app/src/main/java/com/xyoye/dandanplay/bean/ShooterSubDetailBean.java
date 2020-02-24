package com.xyoye.dandanplay.bean;

import com.blankj.utilcode.util.FileIOUtils;
import com.xyoye.dandanplay.utils.net.CommShooterDataObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;
import com.xyoye.dandanplay.utils.net.RetroFactory;
import com.xyoye.dandanplay.utils.net.utils.ShooterNetworkUtils;

import java.util.List;

import io.reactivex.functions.Function;
import okhttp3.ResponseBody;

/**
 * Created by xyoye on 2020/2/24.
 */

public class ShooterSubDetailBean {

    /**
     * status : 0
     * sub : {"result":"succeed","action":"detail","subs":[{"filename":"洛东江大决战.Does the Nak-Dong River Flow.1976.DVD.X264.AAC.HALFCDi.rar","native_name":"洛东江大决战/Commando on the Nakdong River/Does the Nak-Dong River Flow/洛東江大決戦","id":602333,"down_count":14,"revision":0,"upload_time":"2015-07-03 11:28:53","url":"http://file0.assrt.net/download/602333/洛东江大决战.Does the Nak-Dong River Flow.1976.DVD.X264.AAC.HALFCDi.rar?_=1450914208&-=f281c84ea1a1d01280bd105e5f4a0baf&api=1","size":20180,"producer":{"producer":"chenchun8219","verifier":"谢里登大道","source":"校订翻译","uploader":"谢里登大道"},"filelist":[{"url":"http://file0.assrt.net/onthefly/602333/-/1/洛东江大决战.Does the Nak-Dong River Flow.1976.DVD.X264.AAC.HALFCDi.srt?_=1450914208&-=af6b1e5c372713868f36e3c4f3864458&api=1","f":"洛东江大决战.Does the Nak-Dong River Flow.1976.DVD.X264.AAC.HALFCDi.srt","s":"52KB"}],"subtype":"VobSub","title":"洛东江大决战/Commando on the Nakdong River/Does the Nak-Dong River Flow/洛東江大決戦/洛东江大决战.Does the Nak-Dong River Flow.1976.DVD.X264.AAC.HALFCDi","vote_score":0,"release_site":"个人","videoname":"낙동강은 흐르는가","view_count":289,"lang":{"desc":"双语","langlist":{"langdou":true}}}]}
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
         * result : succeed
         * action : detail
         * subs : [{"filename":"洛东江大决战.Does the Nak-Dong River Flow.1976.DVD.X264.AAC.HALFCDi.rar","native_name":"洛东江大决战/Commando on the Nakdong River/Does the Nak-Dong River Flow/洛東江大決戦","id":602333,"down_count":14,"revision":0,"upload_time":"2015-07-03 11:28:53","url":"http://file0.assrt.net/download/602333/洛东江大决战.Does the Nak-Dong River Flow.1976.DVD.X264.AAC.HALFCDi.rar?_=1450914208&-=f281c84ea1a1d01280bd105e5f4a0baf&api=1","size":20180,"producer":{"producer":"chenchun8219","verifier":"谢里登大道","source":"校订翻译","uploader":"谢里登大道"},"filelist":[{"url":"http://file0.assrt.net/onthefly/602333/-/1/洛东江大决战.Does the Nak-Dong River Flow.1976.DVD.X264.AAC.HALFCDi.srt?_=1450914208&-=af6b1e5c372713868f36e3c4f3864458&api=1","f":"洛东江大决战.Does the Nak-Dong River Flow.1976.DVD.X264.AAC.HALFCDi.srt","s":"52KB"}],"subtype":"VobSub","title":"洛东江大决战/Commando on the Nakdong River/Does the Nak-Dong River Flow/洛東江大決戦/洛东江大决战.Does the Nak-Dong River Flow.1976.DVD.X264.AAC.HALFCDi","vote_score":0,"release_site":"个人","videoname":"낙동강은 흐르는가","view_count":289,"lang":{"desc":"双语","langlist":{"langdou":true}}}]
         */

        private String result;
        private String action;
        private List<SubsBean> subs;

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public List<SubsBean> getSubs() {
            return subs;
        }

        public void setSubs(List<SubsBean> subs) {
            this.subs = subs;
        }

        public static class SubsBean {
            /**
             * filename : 洛东江大决战.Does the Nak-Dong River Flow.1976.DVD.X264.AAC.HALFCDi.rar
             * native_name : 洛东江大决战/Commando on the Nakdong River/Does the Nak-Dong River Flow/洛東江大決戦
             * id : 602333
             * down_count : 14
             * revision : 0
             * upload_time : 2015-07-03 11:28:53
             * url : http://file0.assrt.net/download/602333/洛东江大决战.Does the Nak-Dong River Flow.1976.DVD.X264.AAC.HALFCDi.rar?_=1450914208&-=f281c84ea1a1d01280bd105e5f4a0baf&api=1
             * size : 20180
             * producer : {"producer":"chenchun8219","verifier":"谢里登大道","source":"校订翻译","uploader":"谢里登大道"}
             * filelist : [{"url":"http://file0.assrt.net/onthefly/602333/-/1/洛东江大决战.Does the Nak-Dong River Flow.1976.DVD.X264.AAC.HALFCDi.srt?_=1450914208&-=af6b1e5c372713868f36e3c4f3864458&api=1","f":"洛东江大决战.Does the Nak-Dong River Flow.1976.DVD.X264.AAC.HALFCDi.srt","s":"52KB"}]
             * subtype : VobSub
             * title : 洛东江大决战/Commando on the Nakdong River/Does the Nak-Dong River Flow/洛東江大決戦/洛东江大决战.Does the Nak-Dong River Flow.1976.DVD.X264.AAC.HALFCDi
             * vote_score : 0
             * release_site : 个人
             * videoname : 낙동강은 흐르는가
             * view_count : 289
             * lang : {"desc":"双语","langlist":{"langdou":true}}
             */

            private String filename;
            private String native_name;
            private int id;
            private int down_count;
            private int revision;
            private String upload_time;
            private String url;
            private long size;
            private ProducerBean producer;
            private String subtype;
            private String title;
            private int vote_score;
            private String release_site;
            private String videoname;
            private int view_count;
            private LangBean lang;
            private List<FilelistBean> filelist;

            public String getFilename() {
                return filename;
            }

            public void setFilename(String filename) {
                this.filename = filename;
            }

            public String getNative_name() {
                return native_name;
            }

            public void setNative_name(String native_name) {
                this.native_name = native_name;
            }

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public int getDown_count() {
                return down_count;
            }

            public void setDown_count(int down_count) {
                this.down_count = down_count;
            }

            public int getRevision() {
                return revision;
            }

            public void setRevision(int revision) {
                this.revision = revision;
            }

            public String getUpload_time() {
                return upload_time;
            }

            public void setUpload_time(String upload_time) {
                this.upload_time = upload_time;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public long getSize() {
                return size;
            }

            public void setSize(long size) {
                this.size = size;
            }

            public ProducerBean getProducer() {
                return producer;
            }

            public void setProducer(ProducerBean producer) {
                this.producer = producer;
            }

            public String getSubtype() {
                return subtype;
            }

            public void setSubtype(String subtype) {
                this.subtype = subtype;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public int getVote_score() {
                return vote_score;
            }

            public void setVote_score(int vote_score) {
                this.vote_score = vote_score;
            }

            public String getRelease_site() {
                return release_site;
            }

            public void setRelease_site(String release_site) {
                this.release_site = release_site;
            }

            public String getVideoname() {
                return videoname;
            }

            public void setVideoname(String videoname) {
                this.videoname = videoname;
            }

            public int getView_count() {
                return view_count;
            }

            public void setView_count(int view_count) {
                this.view_count = view_count;
            }

            public LangBean getLang() {
                return lang;
            }

            public void setLang(LangBean lang) {
                this.lang = lang;
            }

            public List<FilelistBean> getFilelist() {
                return filelist;
            }

            public void setFilelist(List<FilelistBean> filelist) {
                this.filelist = filelist;
            }

            public static class ProducerBean {
                /**
                 * producer : chenchun8219
                 * verifier : 谢里登大道
                 * source : 校订翻译
                 * uploader : 谢里登大道
                 */

                private String producer;
                private String verifier;
                private String source;
                private String uploader;

                public String getProducer() {
                    return producer;
                }

                public void setProducer(String producer) {
                    this.producer = producer;
                }

                public String getVerifier() {
                    return verifier;
                }

                public void setVerifier(String verifier) {
                    this.verifier = verifier;
                }

                public String getSource() {
                    return source;
                }

                public void setSource(String source) {
                    this.source = source;
                }

                public String getUploader() {
                    return uploader;
                }

                public void setUploader(String uploader) {
                    this.uploader = uploader;
                }
            }

            public static class LangBean {
                /**
                 * desc : 双语
                 * langlist : {"langdou":true}
                 */

                private String desc;
                private LanglistBean langlist;

                public String getDesc() {
                    return desc;
                }

                public void setDesc(String desc) {
                    this.desc = desc;
                }

                public LanglistBean getLanglist() {
                    return langlist;
                }

                public void setLanglist(LanglistBean langlist) {
                    this.langlist = langlist;
                }

                public static class LanglistBean {
                    /**
                     * langdou : true
                     */

                    private boolean langdou;

                    public boolean isLangdou() {
                        return langdou;
                    }

                    public void setLangdou(boolean langdou) {
                        this.langdou = langdou;
                    }
                }
            }

            public static class FilelistBean {
                /**
                 * url : http://file0.assrt.net/onthefly/602333/-/1/洛东江大决战.Does the Nak-Dong River Flow.1976.DVD.X264.AAC.HALFCDi.srt?_=1450914208&-=af6b1e5c372713868f36e3c4f3864458&api=1
                 * f : 洛东江大决战.Does the Nak-Dong River Flow.1976.DVD.X264.AAC.HALFCDi.srt
                 * s : 52KB
                 */

                private String url;
                private String f;
                private String s;

                public String getUrl() {
                    return url;
                }

                public void setUrl(String url) {
                    this.url = url;
                }

                public String getF() {
                    return f;
                }

                public void setF(String f) {
                    this.f = f;
                }

                public String getS() {
                    return s;
                }

                public void setS(String s) {
                    this.s = s;
                }
            }
        }
    }

    public static void querySubtitleDetail(String token, int subtitleId, CommShooterDataObserver<ShooterSubDetailBean> observer, NetworkConsumer consumer) {
        RetroFactory.getShooterInstance().querySubtitleDetail(token, subtitleId)
                .doOnSubscribe(consumer)
                .compose(ShooterNetworkUtils.network())
                .subscribe(observer);
    }

    public static void downloadSubtitle(String downloadLink, String filePath, CommShooterDataObserver<Boolean> observer, NetworkConsumer consumer) {
        RetroFactory.getShooterInstance().downloadSubtitle(downloadLink)
                .map(responseBody ->
                        FileIOUtils.writeFileFromIS(filePath, responseBody.byteStream()))
                .doOnSubscribe(consumer)
                .compose(ShooterNetworkUtils.network())
                .subscribe(observer);
    }
}
