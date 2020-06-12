package com.xyoye.smb.controller;

import android.text.TextUtils;

import com.xyoye.smb.exception.SmbLinkException;
import com.xyoye.smb.info.SmbFileInfo;
import com.xyoye.smb.info.SmbLinkInfo;
import com.xyoye.smb.info.SmbType;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import jcifs.Address;
import jcifs.CIFSContext;
import jcifs.config.PropertyConfiguration;
import jcifs.context.BaseContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Created by xyoye on 2019/12/20.
 */

public class JCIFS_NGController implements Controller {
    private static final String RootFlag = "/";

    private String mPath;
    private String mAuthUrl;
    private CIFSContext cifsContext;
    private List<SmbFileInfo> rootFileList;

    private InputStream inputStream;

    @Override
    public boolean linkStart(SmbLinkInfo smbLinkInfo, SmbLinkException exception) {
        try {
            //build smb url
            mAuthUrl = "smb://" + (smbLinkInfo.isAnonymous()
                    ? smbLinkInfo.getIP()
                    : smbLinkInfo.getAccount() + ":" + smbLinkInfo.getPassword() + "@" + smbLinkInfo.getIP()
            );

            NtlmPasswordAuthenticator auth = new NtlmPasswordAuthenticator(
                    smbLinkInfo.getDomain(), smbLinkInfo.getAccount(), smbLinkInfo.getPassword());

            // set config
            Properties properties = new Properties();
            properties.setProperty("jcifs.smb.client.responseTimeout", "5000");
            PropertyConfiguration configuration = new PropertyConfiguration(properties);

            cifsContext = new BaseContext(configuration).withCredentials(auth);
            Address address = cifsContext.getNameServiceClient().getByName(smbLinkInfo.getIP());
            cifsContext.getTransportPool().logon(cifsContext, address);

            SmbFile rootFile = new SmbFile(mAuthUrl, cifsContext);
            rootFileList = getFileInfoList(rootFile.listFiles());

            mPath = RootFlag;

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            exception.addException(SmbType.JCIFS_NG, e.getMessage());
        }
        return false;
    }

    @Override
    public List<SmbFileInfo> getParentList() {
        if (isRootDir())
            return new ArrayList<>();

        List<SmbFileInfo> fileInfoList = new ArrayList<>();
        try {
            //is first directory like smbJ share
            String parentPath = mPath.substring(0, mPath.length() - 1);
            int index = parentPath.indexOf("/", 1);

            //get parent path index
            int endIndex = parentPath.lastIndexOf("/");
            mPath = mPath.substring(0, endIndex) + "/";

            if (index == -1)
                return rootFileList;

            SmbFile smbFile = new SmbFile(mAuthUrl + mPath, cifsContext);
            if (smbFile.isDirectory() && smbFile.canRead()) {
                fileInfoList.addAll(getFileInfoList(smbFile.listFiles()));
            }
        } catch (MalformedURLException | SmbException e) {
            e.printStackTrace();
        }

        return fileInfoList;
    }

    @Override
    public List<SmbFileInfo> getSelfList() {
        if (isRootDir())
            return rootFileList;

        List<SmbFileInfo> fileInfoList = new ArrayList<>();
        try {
            SmbFile smbFile = new SmbFile(mAuthUrl + mPath, cifsContext);
            if (smbFile.isDirectory() && smbFile.canRead()) {
                fileInfoList.addAll(getFileInfoList(smbFile.listFiles()));
            }
        } catch (MalformedURLException | SmbException e) {
            e.printStackTrace();
        }

        return fileInfoList;
    }

    @Override
    public List<SmbFileInfo> getChildList(String dirName) {
        List<SmbFileInfo> fileInfoList = new ArrayList<>();
        try {
            mPath += dirName + "/";
            SmbFile smbFile = new SmbFile(mAuthUrl + mPath, cifsContext);
            if (smbFile.isDirectory() && smbFile.canRead()) {
                fileInfoList.addAll(getFileInfoList(smbFile.listFiles()));
            }
        } catch (MalformedURLException | SmbException e) {
            e.printStackTrace();
        }

        return fileInfoList;
    }

    @Override
    public InputStream getFileInputStream(String fileName) {
        try {
            String filePath = mPath + fileName + "/";
            SmbFile smbFile = new SmbFile(mAuthUrl + filePath, cifsContext);
            if (smbFile.isFile() && smbFile.canRead()) {
                inputStream = smbFile.getInputStream();
                return inputStream;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public long getFileLength(String fileName) {
        try {
            String filePath = mPath + fileName + "/";
            SmbFile smbFile = new SmbFile(mAuthUrl + filePath, cifsContext);
            if (smbFile.isFile() && smbFile.canRead()) {
                return smbFile.getContentLengthLong();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public String getCurrentPath() {
        if (TextUtils.isEmpty(mPath))
            return "";
        return mPath.length() == 1 ? mPath : mPath.substring(0, mPath.length() - 1);
    }

    @Override
    public boolean isRootDir() {
        return RootFlag.equals(mPath);
    }

    @Override
    public void release() {
        try {
            if (cifsContext != null)
                cifsContext.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<SmbFileInfo> getFileInfoList(SmbFile[] smbFiles) {
        List<SmbFileInfo> fileInfoList = new ArrayList<>();
        for (SmbFile smbFile : smbFiles) {
            boolean isDirectory = false;
            try {
                isDirectory = smbFile.isDirectory();
            } catch (SmbException ignore) {
            }

            //remove / at the end of the path
            String smbFileName = smbFile.getName();
            smbFileName = smbFileName.endsWith("/")
                    ? smbFileName.substring(0, smbFileName.length() - 1)
                    : smbFileName;

            fileInfoList.add(new SmbFileInfo(smbFileName, isDirectory));
        }

        return fileInfoList;
    }
}
