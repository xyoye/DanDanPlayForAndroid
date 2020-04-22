package com.xyoye.smb;

import com.xyoye.smb.controller.Controller;
import com.xyoye.smb.controller.JCIFSController;
import com.xyoye.smb.controller.JCIFS_NGController;
import com.xyoye.smb.controller.SMBJController;
import com.xyoye.smb.controller.SMBJ_RPCController;
import com.xyoye.smb.exception.SmbLinkException;
import com.xyoye.smb.info.SmbLinkInfo;
import com.xyoye.smb.info.SmbType;
import com.xyoye.smb.utils.SmbUtils;

/**
 * Created by xyoye on 2019/12/20.
 */

public class SmbManager {
    private SmbType mSmbType;
    private boolean isLinked;
    private Controller controller;
    private SmbLinkException smbLinkException;

    private static class Holder {
        static SmbManager instance = new SmbManager();
    }

    private SmbManager() {
        smbLinkException = new SmbLinkException();
    }

    public static SmbManager getInstance() {
        return Holder.instance;
    }

    /**
     * link to the smb server from smbV2 to smbV1
     *
     * @param smbLinkInfo link data
     */
    public boolean linkStart(SmbType smbType, SmbLinkInfo smbLinkInfo) {
        this.mSmbType = smbType;

        smbLinkException.clearException();

        if (!smbLinkInfo.isAnonymous()) {
            if (SmbUtils.containsEmptyText(smbLinkInfo.getAccount(), smbLinkInfo.getAccount())) {
                throw new NullPointerException("Account And Password Must NotNull");
            }
        }

        //SMB V2
        isLinked = true;

        if (mSmbType == SmbType.JCIFS_NG) {
            controller = new JCIFS_NGController();
            if (controller.linkStart(smbLinkInfo, smbLinkException)) {
                return true;
            }
        } else if (mSmbType == SmbType.SMBJ_RPC) {
            controller = new SMBJ_RPCController();
            if (controller.linkStart(smbLinkInfo, smbLinkException)) {
                ;
                return true;
            }
        } else if (mSmbType == SmbType.SMBJ && !SmbUtils.isTextEmpty(smbLinkInfo.getRootFolder())) {
            controller = new SMBJController();
            if (controller.linkStart(smbLinkInfo, smbLinkException)) {
                ;
                return true;
            }
        } else if (mSmbType == SmbType.JCIFS) {
            controller = new JCIFSController();
            if (controller.linkStart(smbLinkInfo, smbLinkException)) {
                return true;
            }
        }

        isLinked = false;
        return false;
    }

    /**
     * get smb tools name
     */
    public String getSmbTypeName() {
        return mSmbType == null ? "" : mSmbType.toString();
    }

    /**
     * is the link successful
     */
    public boolean isLinked() {
        return isLinked;
    }

    /**
     * get link controller
     */
    public Controller getController() {
        return controller;
    }

    /**
     * link error info
     */
    public SmbLinkException getException() {
        return smbLinkException;
    }
}
