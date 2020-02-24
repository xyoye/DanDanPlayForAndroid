package com.xyoye.dandanplay.bean;

import com.xyoye.dandanplay.utils.net.CommShooterDataObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;
import com.xyoye.dandanplay.utils.net.RetroFactory;
import com.xyoye.dandanplay.utils.net.utils.ShooterNetworkUtils;

/**
 * Created by xyoye on 2020/2/23.
 */

public class ShooterQuotaBean {

    /**
     * status : 0
     * user : {"quota":4,"result":"succeed","action":"quota"}
     */

    private int status;
    private UserBean user;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public UserBean getUser() {
        return user;
    }

    public void setUser(UserBean user) {
        this.user = user;
    }

    public static class UserBean {
        /**
         * quota : 4
         * result : succeed
         * action : quota
         */

        private int quota;
        private String result;
        private String action;

        public int getQuota() {
            return quota;
        }

        public void setQuota(int quota) {
            this.quota = quota;
        }

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
    }

    public static void getShooterQuota(String token, CommShooterDataObserver<ShooterQuotaBean> observer, NetworkConsumer consumer) {
        RetroFactory.getShooterInstance().getQuota(token)
                .doOnSubscribe(consumer)
                .compose(ShooterNetworkUtils.network())
                .subscribe(observer);
    }
}
