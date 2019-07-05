package com.xyoye.dandanplay.utils;

import com.blankj.utilcode.util.FileUtils;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.bean.DanmuDownloadBean;

import org.xml.sax.helpers.AttributesImpl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import static com.blankj.utilcode.util.FileUtils.createOrExistsFile;

/**
 * Created by xyoye on 2019/7/5.
 */

public class DanmuUtils {


    /**
     * 保存bilibili弹幕
     */
    public static void saveDanmuSourceFormBiliBili(String xmlContent, String fileName , String path){
        FileOutputStream fileOutputStream;
        BufferedWriter bufferedWriter = null;
        try {
            String localPath = path+ "/" + fileName+".xml";

            File folder = new File(FileUtils.getDirName(localPath));
            if (!folder.exists()) {
                if (folder.mkdirs()){
                    System.out.println("成功创建文件夹");
                }
            }

            fileOutputStream = new FileOutputStream(localPath, false);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, Charset.forName("utf-8")));
            bufferedWriter.write(xmlContent);
            bufferedWriter.newLine();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 保存弹弹弹幕
     */
    public static void saveDanmuSourceFormDanDan(List<DanmuDownloadBean.CommentsBean> comments, String savePath){
        try {
            StringWriter xmlWriter = new StringWriter();
            SAXTransformerFactory factory = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
            TransformerHandler handler = factory.newTransformerHandler();

            Transformer transformer = handler.getTransformer();     // 设置xml属性
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
            StreamResult result = new StreamResult(xmlWriter);      // 保存创建的xml
            handler.setResult(result);
            handler.startDocument();
            AttributesImpl attr = new AttributesImpl();
            attr.clear();
            handler.startElement("", "", "i", attr);
            for (DanmuDownloadBean.CommentsBean bean : comments){
                attr.clear();
                String[] pA = bean.getP().split(",");
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < pA.length; i++) {
                    stringBuilder.append(pA[i]).append(",");
                    if (i == 1){
                        stringBuilder.append("25,");
                    }
                }
                String pText = stringBuilder.toString().substring(0, stringBuilder.length()-1);
                String attribute = pText + ",0,0,0";
                attr.addAttribute("", "", "p", "", attribute);
                handler.startElement("", "", "d", attr);
                String text = bean.getM();
                handler.characters(text.toCharArray(), 0, text.length());
                handler.endElement("", "", "d");
            }
            handler.endElement("", "", "i");
            handler.endDocument();
            File file = new File(savePath);
            String content = xmlWriter.toString();
            if (content == null) return;
            if (!createOrExistsFile(file)) return;
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
                bw.write(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 向本地弹幕文件插入一条弹幕
     */
    public static void insertOneDanmu(String danmu, String path){
        File danmuFile = new File(path);
        if (!danmuFile.exists())
            return;

        try {
            //读取文件内容
            FileReader fileReader = new FileReader(danmuFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder danmuContentBuffer = new StringBuilder();
            String oneLine;
            while ((oneLine = bufferedReader.readLine()) != null) {
                danmuContentBuffer.append(oneLine);
            }
            //在最后添加当前弹幕
            String danmuContent = danmuContentBuffer.toString();
            if (danmuContent.endsWith("</i>")){
                danmuContent =  danmuContent.substring(0, danmuContent.length() - 4);
                danmuContent += danmu;
                danmuContent += "</i>";
            }

            //将所有内容写回文件
            FileOutputStream fileOutputStream = new FileOutputStream(danmuFile, false);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, Charset.forName("utf-8")));
            bufferedWriter.write(danmuContent);
            bufferedWriter.newLine();

            bufferedWriter.close();
            bufferedReader.close();
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
