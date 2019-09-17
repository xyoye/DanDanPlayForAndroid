package com.xyoye.dandanplay.ui.weight.anim.path;

import android.graphics.Path;

import java.util.ArrayList;

/**
 * Created by xyoye on 2019/9/15.
 */

public class TextPath {
    private float mWidth;
    private float mHeight;

    private Path mPath;

    public TextPath(String str) {
        this(str, 1);
    }

    public TextPath(String str, float scale) {
        this(str, scale, 14);
    }

    public TextPath(String str, float scale, float textIntervalPx) {
        ArrayList<float[]> pathData = initPathData(str, scale, textIntervalPx);
        mPath = new Path();
        for (int i = 0; i < pathData.size(); i++) {
            float[] floats = pathData.get(i);
            mPath.moveTo(floats[0], floats[1]);
            mPath.lineTo(floats[2], floats[3]);
        }
    }

    /**
     * 初始化path
     *
     * @param str            文字，范围参照PathUtil
     * @param scale          缩放倍数
     * @param textIntervalPx 文字间距 px
     */
    private ArrayList<float[]> initPathData(String str, float scale, float textIntervalPx) {
        ArrayList<float[]> pathData = new ArrayList<>();
        //上下左右偏移5px
        int padding = 5;

        float offsetForWidth = padding;
        for (int i = 0; i < str.length(); i++) {
            int pos = str.charAt(i);
            int key = TextPathUtils.pointList.indexOfKey(pos);
            if (key == -1) {
                continue;
            }
            float[] points = TextPathUtils.pointList.get(pos);
            int pointCount = points.length / 4;


            //当前字最左点
            float minX = 0;
            //当前字最右点
            float maxX = 0;

            //对Path进行缩放，同时获取整体宽高
            for (int j = 0; j < pointCount; j++) {

                float[] line = new float[4];
                for (int k = 0; k < 4; k++) {
                    float l = points[j * 4 + k];
                    // x
                    if (k % 2 == 0) {
                        line[k] = offsetForWidth + (l * scale) ;

                        if (minX == 0 || minX > line[k]) {
                            minX = line[k];
                        }
                        if (maxX == 0 || maxX < line[k]) {
                            maxX = line[k];
                        }
                    }
                    // y
                    else {
                        line[k] = l * scale + padding;
                        if (mHeight == 0 || line[k] > mHeight) {
                            mHeight = line[k];
                        }
                    }
                }

                pathData.add(line);
            }
            float textSize = maxX - minX;
            offsetForWidth += textSize + textIntervalPx;
        }
        mWidth = offsetForWidth - textIntervalPx + padding;
        mHeight += padding;
        return pathData;
    }

    public float getWidth() {
        return mWidth;
    }

    public float getHeight() {
        return mHeight;
    }

    public Path getPath() {
        return mPath;
    }
}
