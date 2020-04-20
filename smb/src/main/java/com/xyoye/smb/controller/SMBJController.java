package com.xyoye.smb.controller;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.msfscc.fileinformation.FileStandardInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.Directory;
import com.hierynomus.smbj.share.DiskEntry;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import com.xyoye.smb.exception.SmbLinkException;
import com.xyoye.smb.info.SmbFileInfo;
import com.xyoye.smb.info.SmbLinkInfo;
import com.xyoye.smb.info.SmbType;
import com.xyoye.smb.utils.SmbUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hierynomus.mssmb2.SMB2CreateDisposition.FILE_OPEN;

/**
 * Created by xyoye on 2019/12/20.
 */

public class SMBJController implements Controller {

    private String ROOT_FLAG = "\\";
    private String mPath;
    private DiskShare diskShare;
    private List<SmbFileInfo> rootFileList;

    private SMBClient smbClient;
    private Connection connection;
    private Session session;
    private InputStream inputStream;

    @Override
    public boolean linkStart(SmbLinkInfo smbLinkInfo, SmbLinkException exception) {
        String rootFolder = smbLinkInfo.getRootFolder();
        if (SmbUtils.isTextEmpty(rootFolder)) {
            exception.addException(SmbType.SMBJ, "Root Folder Must Not Empty");
            return false;
        }

        SmbConfig smbConfig = SmbConfig.builder()
                .withTimeout(5, TimeUnit.SECONDS)
                .withTimeout(5, TimeUnit.SECONDS)
                .withSoTimeout(5, TimeUnit.SECONDS)
                .build();

        try {
            smbClient = new SMBClient(smbConfig);
            connection = smbClient.connect(smbLinkInfo.getIP());
            AuthenticationContext ac = new AuthenticationContext(
                    smbLinkInfo.getAccount(), smbLinkInfo.getPassword().toCharArray(), smbLinkInfo.getDomain());
            session = connection.authenticate(ac);

            ROOT_FLAG += smbLinkInfo.getRootFolder();
            mPath = ROOT_FLAG;

            diskShare = (DiskShare) session.connectShare(smbLinkInfo.getRootFolder());
            rootFileList = getFileInfoList(diskShare.list(""), diskShare);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            exception.addException(SmbType.SMBJ, e.getMessage());
        }
        return false;
    }

    @Override
    public List<SmbFileInfo> getParentList() {
        if (isRootDir()) {
            return new ArrayList<>();
        }

        //get parent path by mPath
        int startIndex = mPath.indexOf("\\", 1) + 1;
        int endIndex = mPath.lastIndexOf("\\");
        String parentPath = startIndex >= endIndex ? "" : mPath.substring(startIndex, endIndex);

        mPath = mPath.substring(0, endIndex);

        List<SmbFileInfo> infoList = new ArrayList<>();
        try {
            //get folder normal info by path
            Directory parentDir = openDirectory(diskShare, parentPath);
            List<FileIdBothDirectoryInformation> parentDirInfoList = parentDir.list();
            //get folder detail info
            infoList.addAll(getFileInfoList(parentDirInfoList, diskShare));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return infoList;
    }

    @Override
    public List<SmbFileInfo> getSelfList() {
        if (isRootDir())
            return rootFileList;

        List<SmbFileInfo> infoList = new ArrayList<>();
        try {
            Directory selfDir = openDirectory(diskShare, getPathNotShare(""));
            List<FileIdBothDirectoryInformation> selfDirInfoList = selfDir.list();

            infoList.addAll(getFileInfoList(selfDirInfoList, diskShare));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return infoList;
    }

    @Override
    public List<SmbFileInfo> getChildList(String dirName) {
        List<SmbFileInfo> infoList = new ArrayList<>();
        try {

            Directory childDir = openDirectory(diskShare, getPathNotShare(dirName));
            List<FileIdBothDirectoryInformation> childDirInfoList = childDir.list();

            mPath += "\\" + dirName;
            infoList.addAll(getFileInfoList(childDirInfoList, diskShare));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return infoList;
    }

    @Override
    public InputStream getFileInputStream(String fileName) {
        String filePath = getPathNotShare(fileName);

        try {
            File file = openFile(diskShare, filePath);
            inputStream = file.getInputStream();
            return inputStream;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long getFileLength(String fileName) {
        String filePath = getPathNotShare(fileName);

        try {
            File file = openFile(diskShare, filePath);
            FileStandardInformation standardInfo = file.getFileInformation(FileStandardInformation.class);
            return standardInfo.getEndOfFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public String getCurrentPath() {
        return mPath.length() == 0 ? "/" : mPath.replace("\\", "/");
    }

    @Override
    public boolean isRootDir() {
        return mPath.equals(ROOT_FLAG);
    }

    @Override
    public void release() {
        try {
            if (inputStream != null)
                inputStream.close();
            if (diskShare != null)
                diskShare.close();
            if (session != null)
                session.close();
            if (connection != null)
                connection.close();
            if (smbClient != null)
                smbClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * traversal directory info list filtering does not use folders and get file types
     *
     * @param dirInfoList directory list
     * @param diskShare   share
     * @return file info list
     */
    private List<SmbFileInfo> getFileInfoList(List<FileIdBothDirectoryInformation> dirInfoList, DiskShare diskShare) {
        List<SmbFileInfo> fileInfoList = new ArrayList<>();
        for (FileIdBothDirectoryInformation dirInfo : dirInfoList) {
            //ignore directories beginning with '.', like '.', '..'
            if (dirInfo.getFileName().startsWith("."))
                continue;

            //get file standard info by disk entry because file type unknown
            DiskEntry diskEntry = openDiskEntry(diskShare, getPathNotShare(dirInfo.getFileName()));
            FileStandardInformation standardInformation = diskEntry.getFileInformation(FileStandardInformation.class);
            fileInfoList.add(new SmbFileInfo(dirInfo.getFileName(), standardInformation.isDirectory()));
        }
        return fileInfoList;
    }

    /**
     * splicing child file name to mPath and removing shareName
     *
     * @param fileName child file name
     * @return child file path
     */
    private String getPathNotShare(String fileName) {
        int index = mPath.indexOf("\\", 1);
        if (index == -1) {
            return fileName;
        } else {
            fileName = fileName.length() == 0 ? "" : ("\\" + fileName);
            return mPath.substring(index + 1) + fileName;
        }
    }

    /**
     * get smb file, just need reed permission
     *
     * @param share    share
     * @param filePath file path not share name
     * @return smb file
     */
    private File openFile(DiskShare share, String filePath) {
        return share.openFile(filePath,
                EnumSet.of(AccessMask.FILE_READ_DATA),
                null,
                SMB2ShareAccess.ALL,
                FILE_OPEN,
                null);
    }

    /**
     * get smb directory, just need reed permission
     *
     * @param share    share
     * @param filePath directory path not share name
     * @return smb directory
     */
    private Directory openDirectory(DiskShare share, String filePath) {
        return share.openDirectory(
                filePath,
                EnumSet.of(AccessMask.GENERIC_READ),
                null,
                SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OPEN,
                null);
    }

    /**
     * get smb disk entry, just need reed permission
     *
     * @param share    share
     * @param filePath file or directory path nor share name
     * @return dis entry
     */
    private DiskEntry openDiskEntry(DiskShare share, String filePath) {
        return share.open(
                filePath,
                EnumSet.of(AccessMask.GENERIC_READ),
                null,
                SMB2ShareAccess.ALL,
                FILE_OPEN,
                null);
    }
}
