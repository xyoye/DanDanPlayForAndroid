package com.xyoye.dandanplay.utils;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.StringUtils;
import com.xyoye.dandanplay.bean.SubtitleBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xyoye on 2019/5/10.
 */

public class SubtitleConverter {

    public static List<SubtitleBean> transform(List<SubtitleBean.Shooter> shooter, SubtitleBean.Thunder thunder, String videoPath){
        List<SubtitleBean> subtitleList = new ArrayList<>();
        subtitleList.addAll(shooter2subtitle(shooter, videoPath));
        subtitleList.addAll(thunder2subtitle(thunder));
        return subtitleList;
    }

    private static List<SubtitleBean> shooter2subtitle(List<SubtitleBean.Shooter> shooterList, String filePath){
        if (shooterList == null || shooterList.size() == 0){
            return new ArrayList<>();
        }

        List<SubtitleBean> subtitleList = new ArrayList<>();
        for (SubtitleBean.Shooter shooterBean : shooterList){
            for (SubtitleBean.Shooter.FilesBean shooterFile : shooterBean.getFiles()){
                if (StringUtils.isEmpty(shooterFile.getLink()))
                    continue;
                SubtitleBean subtitleBean = new SubtitleBean();
                subtitleBean.setOrigin(SubtitleBean.Shooter.SHOOTER);
                subtitleBean.setName(FileUtils.getFileNameNoExtension(filePath));
                subtitleBean.setRank(-1);
                subtitleBean.setUrl(shooterFile.getLink());
                subtitleList.add(subtitleBean);
            }
        }
        return subtitleList;
    }

    private static List<SubtitleBean> thunder2subtitle(SubtitleBean.Thunder thunderBean){
        if (thunderBean == null || thunderBean.getSublist() == null || thunderBean.getSublist().size() == 0){
            return new ArrayList<>();
        }

        List<SubtitleBean> subtitleList = new ArrayList<>();
        for (SubtitleBean.Thunder.SublistBean thunder : thunderBean.getSublist()){
            if (StringUtils.isEmpty(thunder.getSurl()))
                continue;
            SubtitleBean subtitleBean = new SubtitleBean();
            subtitleBean.setOrigin(SubtitleBean.Thunder.THUNDER);
            subtitleBean.setUrl(thunder.getSurl());
            subtitleBean.setName(thunder.getSname());
            subtitleBean.setLanguage(thunder.getLanguage());
            subtitleBean.setRank(thunder.getRate());
            subtitleList.add(subtitleBean);
        }
        return subtitleList;
    }
}
