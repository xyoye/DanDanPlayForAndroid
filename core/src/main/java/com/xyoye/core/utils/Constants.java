package com.xyoye.core.utils;

/**
 *
 * Created by yzd on 2016/7/1.
 */
public final class Constants {

    //SharedPreference表名

    public static class AppConfig {
        public static final String VERSION = "version";                 //版本号
        public static final String CONFIGURATION = "configuration";     //配置
    }


    public static class UserTab {
        public static final String USER_ID = "userId";
        public static final String SESSION_KEY = "access_token";
        public static final String TOKEN_TIME = "long_token_time";
        public static final String USER_NAME = "userName";
        public static final String EXPIRES_IN = "expires_in";
        public static final String SESSION_REFRESH = "refresh_token";
        public static final String R1_EXPIRES_IN = "r1_expires_in";
        public static final String R2_EXPIRES_IN = "r2_expires_in";
        public static final String RE_EXPIRES_IN = "re_expires_in";
        public static final String W1_EXPIRES_IN = "w1_expires_in";
        public static final String W2_EXPIRES_IN = "w2_expires_in";

        public static final String LAST_LOGIN = "last_login";
        public static final String MOBILE = "mobile";
        public static final String TAOBAO_USER_NAME = "TaoBaoName";
        public static final String SAVE_DATA = "save_data";
        public static final String EMAIL = "email";
        public static final String ACCOUNT = "account";
        public static final String HASPAYPWD = "hasPayPwd";
        public static final String IDENTITY = "identity";
        public static final String AVATAR = "avatar";
        public static final String TAOBAO_USER_ID = "taobao_user_id";
        public static final String IS_ADMIN = "is_admin";
        public static final String QQ = "qq";
        public static final String WANGWANG = "wangwang";
        public static final String TAOBAONICK = "taobaoNick";
        public static final String APP_TYPE = "app_type";

        public static final String ALI_TIMEOUT = "refresh_token_timeout";
        public static final String ALI_ID = "aliId";
        public static final String ALI_OWNER = "resource_owner";
        public static final String ALI_MEMBER_ID = "memberId";
        public static final String ALI_EXPIRES_IN = "ali_expires_in";
        public static final String ALI_REFRESH_TOKEN = "ali_refresh_token";
        public static final String ALI_ACCESS_TOKEN = "ali_access_token";

        public static final String PDD_NICK = "pdd_nick";
        public static final String PDD_TOKEN = "pdd_token";
    }

    public static class AuthTab {
        public static final String TAOBAO_AUTHS = "taobao_auths";
        public static final String ALI_AUTHS = "ali_auths";
    }

    public static class Delegate {
        public static final String TYPE = "type";
        public static final String DELEGATE_NAME = "delegateName";
    }

    public static class SearchHistory {
        public static final String GOODS_SEARCH_HIS = "goods_search_his";
        public static final String SHOP_SEARCH_HIS = "shop_search_his";
        public static final String CARE_GOODS_SEARCH_HIS = "care_goods_searrch_his";
    }

    //淘宝授权页
    public static final String TB_URL = "http://api.huoniuniu.com/tb/choose";

    //文件夹名称
    public static final String DOWNLOAD = "download";
    public static final String IMAGE_LIB = "59PiImage";
    public static final String REFUND_IMG = "refund";
    public static final String HNN_IMG = "hnnimg";

    //api版本号
    public static int API_VERSION = 1;
    public static final int API_ONLINE = 0;
    public static final int API_LOCAL = 1;
    public static final int API_TEST = 2;
    public static final int API_TEST_2 = 3;

    //阿里平台key
    public static final String ALIPAPA_KEY = "";
    public static final String ALIPAPA_SECRET = "";

    //阿里反馈key
    public static final String ALI_FEEDBACK_KEY = "";

    //微信appkey
    public static final String WEIXIN_APPKEY = "";
    public static final String WEIXIN_SECRET = "";

    //腾讯QQappkey
    public static final String QQ_APPKEY = "";
    public static final String QQ_SECRET = "";

    //新浪appKey
    public static final String SINA_APPKEY = "";
    public static final String SINA_SECRET = "";

    //app文件存储目录文件名
    public static final String APPMAINFILES = "59wang";
    public static final String IMAGELIB = "59PiImage";
    public static final String MINIIMGLIB = "miniImgLib";
    public static final String TEMP = "temp";
    public static final String MINIIMAGE = "miniImage";

}
