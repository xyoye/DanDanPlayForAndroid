package com.xyoye.player.subtitle.util;

/**
 * Created by xyoye on 2018/9/20.
 */

public class SubtitleFormat {

    public static TimedTextFileFormat format(String path){
        if(path.contains(".")){
            TimedTextFileFormat fileFormat = null;
            int end = path.lastIndexOf(".");
            String ext = path.substring(end+1);
            switch (ext.toUpperCase()){
                case "ASS":
                    fileFormat = new FormatASS();
                    break;
                case "SCC":
                    fileFormat = new FormatSCC();
                    break;
                case "SRT":
                    fileFormat = new FormatSRT();
                    break;
                case "STL":
                    fileFormat = new FormatSTL();
                    break;
                case "XML":
                    fileFormat = new FormatTTML();
                    break;
            }
            return fileFormat;
        }
        return null;
    }
}
