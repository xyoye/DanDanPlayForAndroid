package com.xyoye.dandanplay.utils;

import com.xyoye.dandanplay.app.IApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xyoye on 2019/4/28.
 */

public class TrackerManager {

    //重置为默认的了tracker
    public static void resetTracker(){
        try {
            File trackerFile = new File(Constants.DefaultConfig.configPath);
            if (trackerFile.exists()){
                trackerFile.delete();
            }
            trackerFile.createNewFile();
            //读取asset中默认的trackers，并写入文件
            List<String> trackers = CommonUtils.readTracker(IApplication.get_context());
            IApplication.trackers.clear();
            IApplication.trackers.addAll(trackers);

            FileWriter fileWriter = new FileWriter(trackerFile);
            for (String tracker : trackers){
                fileWriter.append(tracker).append("\n");
            }
            fileWriter.flush();
            fileWriter.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //查询所有tracker
    public static void queryTracker(){
        try {
            File trackerFile = new File(Constants.DefaultConfig.configPath);
            FileReader fileReader = new FileReader(trackerFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String tracker;
            while ((tracker = bufferedReader.readLine()) != null) {
                IApplication.trackers.add(tracker);
            }
            bufferedReader.close();
            fileReader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //增加一条tracker
    public static void addTracker(String tracker){
        List<String> trackers = new ArrayList<>();
        trackers.add(tracker);
        addTracker(trackers);
    }

    //增加多条tracker
    public static void addTracker(List<String> trackers){
        try {
            File trackerFile = new File(Constants.DefaultConfig.configPath);
            if (!trackerFile.exists()){
                trackerFile.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(trackerFile);
            for (String tracker : trackers){
                fileWriter.append(tracker).append("\n");
            }
            fileWriter.flush();
            fileWriter.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //删除tracker
    public static void deleteTracker(){
        try {
            File trackerFile = new File(Constants.DefaultConfig.configPath);
            if (trackerFile.exists()){
                trackerFile.delete();
            }
            trackerFile.createNewFile();
            FileWriter fileWriter = new FileWriter(trackerFile);
            for (String tracker : IApplication.trackers){
                fileWriter.append(tracker).append("\n");
            }
            fileWriter.flush();
            fileWriter.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
