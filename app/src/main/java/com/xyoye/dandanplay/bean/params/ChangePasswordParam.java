package com.xyoye.dandanplay.bean.params;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by YE on 2018/8/5.
 */


public class ChangePasswordParam implements Serializable {
    private String oldPassword;
    private String newPassword;

    public ChangePasswordParam(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
    public Map<String, String> getMap(){
        Map<String, String> map = new HashMap<>();
        map.put("oldPassword", this.oldPassword);
        map.put("newPassword", this.newPassword);
        return map;
    }
}
