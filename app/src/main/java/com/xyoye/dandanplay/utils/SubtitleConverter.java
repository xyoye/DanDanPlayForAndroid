package com.xyoye.dandanplay.utils;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.StringUtils;
import com.xyoye.player.commom.bean.SubtitleBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 以body转bean的原因是，okhttp采用的是gson来解析结果，
 * 而射手网在找不到字幕时，返回一个gson不能解析的字符串
 * 会导致zip直接转至OnError，因此在此处用JsonUtil来手动转换
 *
 * 但是目前仍有问题的是，假如某天射手或迅雷某一个api不可用时，会导致
 * 另一个虽然能获取到结果，但也不能用
 *
 * Created by xyoye on 2019/5/10.
 */

public class SubtitleConverter {

    public static List<SubtitleBean> transform(SubtitleBean.Thunder thunderBean, List<SubtitleBean.Shooter> shooterList, String videoPath){
        List<SubtitleBean> subtitleList = new ArrayList<>();
        subtitleList.addAll(shooter2subtitle(shooterList, videoPath));
        subtitleList.addAll(thunder2subtitle(thunderBean));
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
                subtitleBean.setName(FileUtils.getFileNameNoExtension(filePath) + "." + shooterFile.getExt());
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
