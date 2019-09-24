package com.xyoye.dandanplay.utils;


import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by xyoye on 2019/9/24.
 */

public class JsonUtils {

    private JsonUtils() {

    }

    private static class GsonHolder {
        static Gson gson = new Gson();
    }
    
    /**
     * object to json string
     */
    public static String toJson(Object object) {
        return GsonHolder.gson.toJson(object);
    }

    /**
     * json string to class object
     */
    public static <T> T fromJson(String jsonStr, Class<T> clazz) {
        if (TextUtils.isEmpty(jsonStr)) {
            return null;
        }
        try {
            return GsonHolder.gson.fromJson(jsonStr, clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * json string to class list
     */
    public static <T> List<T> getObjectList(String jsonStr, Class<T> clazz) {
        List<T> objectList = new ArrayList<>();
        try {
            JsonElement rootElement = new JsonParser().parse(jsonStr);
            if (rootElement.isJsonArray()) {
                JsonArray jsonArray = rootElement.getAsJsonArray();
                for (JsonElement jsonElement : jsonArray) {
                    objectList.add(GsonHolder.gson.fromJson(jsonElement, clazz));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return objectList;
    }

    /**
     * json string to map
     */
    public static Map<String, Object> getMapForJson(String jsonStr) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonStr);

            Map<String, Object> resultMap = new HashMap<>();
            Iterator<String> keyIterator = jsonObject.keys();
            while (keyIterator.hasNext()) {
                String key = keyIterator.next();
                Object value = jsonObject.get(key);
                resultMap.put(key, value);
            }
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * json string to map list
     */
    public static List<Map<String, Object>> getListMapForJson(String jsonStr) {
        try {
            JsonElement rootElement = new JsonParser().parse(jsonStr);
            if (rootElement.isJsonArray()) {
                JSONArray jsonArray = new JSONArray(jsonStr);
                List<Map<String, Object>> mapList = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObj = (JSONObject) jsonArray.get(i);
                    mapList.add(getMapForJson(jsonObj.toString()));
                }
                return mapList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}