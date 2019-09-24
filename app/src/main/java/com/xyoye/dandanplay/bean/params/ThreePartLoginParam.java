package com.xyoye.dandanplay.bean.params;

import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.utils.SoUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xyoye on 2018/7/22.
 */

public class ThreePartLoginParam implements Serializable {

    /**
     * source : qq
     * userId : string
     * accessToken : string
     * appId : string
     * unixTimestamp : 0
     * hash : string
     */

    private String source;
    private String userId;
    private String accessToken;
    private String appId;
    private long unixTimestamp;
    private String hash;

    public void setSource(String source) {
        this.source = source;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void buildHash() {
        unixTimestamp = System.currentTimeMillis() / 1000;
        appId = SoUtils.getInstance().getDanDanAppId();

        if (StringUtils.isEmpty(accessToken) ||
                StringUtils.isEmpty(appId) ||
                StringUtils.isEmpty(source) ||
                StringUtils.isEmpty(userId) ||
                unixTimestamp == 0) {
            ToastUtils.showShort("登录信息错误");
        } else {
            String builder = this.accessToken +
                    this.appId +
                    this.source +
                    this.unixTimestamp +
                    this.userId +
                    SoUtils.getInstance().getDanDanAppSecret();
            hash = EncryptUtils.encryptMD5ToString(builder);
        }
    }

    public Map<String, String> getMap() {
        Map<String, String> map = new HashMap<>();
        map.put("source", this.source);
        map.put("userId", this.userId);
        map.put("accessToken", this.accessToken);
        map.put("appId", this.appId);
        map.put("unixTimestamp", this.unixTimestamp + "");
        map.put("hash", this.hash);
        return map;
    }
}
