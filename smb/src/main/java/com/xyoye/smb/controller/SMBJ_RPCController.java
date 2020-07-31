package com.xyoye.smb.controller;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.msfscc.fileinformation.FileStandardInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.Directory;
import com.hierynomus.smbj.share.DiskEntry;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import com.rapid7.client.dcerpc.mssrvs.ServerService;
import com.rapid7.client.dcerpc.mssrvs.dto.NetShareInfo0;
import com.rapid7.client.dcerpc.transport.RPCTransport;
import com.rapid7.client.dcerpc.transport.SMBTransportFactories;
import com.xyoye.smb.exception.SmbLinkException;
import com.xyoye.smb.info.SmbFileInfo;
import com.xyoye.smb.info.SmbLinkInfo;
import com.xyoye.smb.info.SmbType;

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

public class SMBJ_RPCController implements Controller {
    private static final String ROOT_FLAG = "";

    private String mPath;
    private List<SmbFileInfo> rootFileList;

    private SMBClient smbClient;
    private Session session;
    private Connection smbConnection;
    private DiskShare diskShare;
    private InputStream inputStream;

    public SMBJ_RPCController() {
        rootFileList = new ArrayList<>();
    }

    @Override
    public boolean linkStart(SmbLinkInfo smbLinkInfo, SmbLinkException exception) {

        //set smb config
        SmbConfig smbConfig = SmbConfig.builder()
                .withTimeout(180, TimeUnit.SECONDS)
                .withSoTimeout(180, TimeUnit.SECONDS)
                .build();

        try {
            smbClient = new SMBClient(smbConfig);
            smbConnection = smbClient.connect(smbLinkInfo.getIP());
            AuthenticationContext authContext = new AuthenticationContext(
                    smbLinkInfo.getAccount(), smbLinkInfo.getPassword().toCharArray(), smbLinkInfo.getDomain());
            session = smbConnection.authenticate(authContext);

            RPCTransport transport = SMBTransportFactories.SRVSVC.getTransport(session);
            ServerService serverService = new ServerService(transport);
            List<NetShareInfo0> shareInfoList = serverService.getShares0();

            mPath = ROOT_FLAG;

            //get root directory file list
            rootFileList = getFileInfoList(shareInfoList);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            exception.addException(SmbType.SMBJ_RPC, e.getMessage());
        }
        return false;
    }

    @Override
    public List<SmbFileInfo> getParentList() {
        //root directory not parent
        if (isRootDir())
            return new ArrayList<>();

        List<SmbFileInfo> infoList = new ArrayList<>();
        int index = mPath.indexOf("\\", 1);
        if (index == -1) {
            //in share directory, its parent is root directory
            mPath = ROOT_FLAG;
            return rootFileList;
        } else {
            try {
                //get parent path by mPath
                int startIndex = mPath.indexOf("\\", 1) + 1;
                int endIndex = mPath.lastIndexOf("\\");
                String parentPath = startIndex >= endIndex ? "" : mPath.substring(startIndex, endIndex);

                mPath = mPath.substring(0, endIndex);
                //get folder normal info by path
                Directory parentDir = openDirectory(diskShare, parentPath);
                List<FileIdBothDirectoryInformation> parentDirInfoList = parentDir.list();
                //get folder detail info
                infoList.addAll(getFileInfoList(parentDirInfoList, diskShare));
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            Directory childDir;
            if (isRootDir()) {
                //in root directory the child is share
                diskShare = (DiskShare) session.connectShare(dirName);
                childDir = openDirectory(diskShare, "");
            } else {
                childDir = openDirectory(diskShare, getPathNotShare(dirName));
            }

            mPath += "\\" + dirName;
            List<FileIdBothDirectoryInformation> childDirInfoList = childDir.list();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return inputStream;
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
        return mPath == null || mPath.length() == 0 ? "/" : mPath.replace("\\", "/");
    }

    @Override
    public boolean isRootDir() {
        return ROOT_FLAG.equals(mPath);
    }

    @Override
    public void release() {
        try {
            if (inputStream != null)
                inputStream.close();
            if (session != null)
                session.close();
            if (smbConnection != null)
                smbConnection.close();
            if (smbClient != null)
                smbClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * get share info list, just add file type directory
     *
     * @param shareInfoList file info list
     * @return file info list
     */
    private List<SmbFileInfo> getFileInfoList(List<NetShareInfo0> shareInfoList) {
        List<SmbFileInfo> fileInfoList = new ArrayList<>();
        for (NetShareInfo0 shareInfo : shareInfoList) {
            fileInfoList.add(new SmbFileInfo(shareInfo.getNetName(), true));
        }
        return fileInfoList;
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
     * get smb directory, just need read permission
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
