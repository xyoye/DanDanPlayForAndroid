package com.xyoye.dandanplay.utils.permission;

import com.xyoye.dandanplay.bean.DanmuDownloadBean;

import org.xml.sax.helpers.AttributesImpl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import static com.blankj.utilcode.util.FileUtils.createOrExistsFile;

/**
 * Created by YE on 2018/7/14.
 */


public class DownloadUtil {

    public static void saveDanmu(List<DanmuDownloadBean.CommentsBean> comments, String savePath){
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
            writeFileFromString(new File(savePath),xmlWriter.toString(),false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeFileFromString(File file, String content, boolean append) throws IOException {
        if (file == null || content == null) return;
        if (!createOrExistsFile(file)) return;
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file, append));
            bw.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null)
                bw.close();
        }
    }
}
