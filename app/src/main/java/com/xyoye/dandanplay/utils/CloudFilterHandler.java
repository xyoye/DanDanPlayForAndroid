package com.xyoye.dandanplay.utils;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Created by xyy on 2019/1/16.
 */

public class CloudFilterHandler extends DefaultHandler{
    private List<String> filter;
    private ParserFilterListener listener;
    private StringBuffer buffer;

    public CloudFilterHandler(ParserFilterListener listener) {
        this.listener = listener;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        filter = new ArrayList<>();
        buffer = new StringBuffer();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        String temp = new String(ch,start,length);
        temp = temp.trim();
        buffer.append(temp);
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        //以“|”作分隔符
        String[] strings = buffer.toString().split("\\|");
        //以HashSet过滤相同
        HashSet<String> hashSet = new HashSet<>(Arrays.asList(strings));
        filter.addAll(hashSet);
        listener.onParserEnd(filter);
    }

    public interface ParserFilterListener{
        void onParserEnd(List<String> filter);
    }
}
