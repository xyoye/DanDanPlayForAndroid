package com.xyoye.core.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.blankj.utilcode.util.ScreenUtils;
import com.xyoye.core.BaseApplication;


/**
 * 屏幕工具
 * 单位转换
 */
public class PixelUtil {

    private static int SCREEN_W;
    private static int SCREEN_H;

    /**
     * dip转px
     * param context
     * param dipValue
     * return
     */
    public static int dip2px(Context context, float dipValue) {

		return (int) TypedValue.applyDimension(1, dipValue
                , context.getApplicationContext().getResources().getDisplayMetrics());
        //return (int) (dipValue * (context.getResources().getDisplayMetrics().densityDpi / 160) + 0.5f);
    }

    /**
     * px转dip
     * param context
     * param pxValue
     * return
     */
    public static int px2dip(Context context, float pxValue) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pxValue,
				context.getApplicationContext().getResources().getDisplayMetrics());
        /*return (int) (pxValue * 160 / context.getApplicationContext()
                .getResources().getDisplayMetrics().density + 0.5f);*/

    }

    /**
     * px转sp
     *
     * @param paramContext
     * @param paramFloat
     * @return
     */
    public static int px2sp(Context paramContext, float paramFloat) {
        return (int) (0.5F + paramFloat / paramContext.getApplicationContext()
                .getResources().getDisplayMetrics().scaledDensity);
    }

    /**
     * sp转px
     *
     * @param paramContext
     * @param paramFloat
     * @return
     */
    public static int sp2px(Context paramContext, float paramFloat) {
        return (int) (0.5F + paramFloat * paramContext.getApplicationContext()
                .getResources().getDisplayMetrics().scaledDensity);
    }

    /**
     * 获取屏幕宽度和高度，单位为px
     * param context
     * return
     */
    public static Point getScreenMetrics(Context context) {
        DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels;
        int h_screen = dm.heightPixels;
        return new Point(w_screen, h_screen);

    }

    /**
     * 获取屏幕宽度
     *
     * @return
     */
    public static int getScreenW() {
        if (SCREEN_W > 0) {
            return SCREEN_W;
        }
        SCREEN_W = BaseApplication.get_resource().getDisplayMetrics().widthPixels;
        return SCREEN_W;
    }

    /**
     * 获取屏幕高度
     *
     * @return
     */
    public static int getScreenH() {
        if (SCREEN_H > 0) {
            return SCREEN_H;
        }
        SCREEN_H = BaseApplication.get_resource().getDisplayMetrics().heightPixels;
        return SCREEN_H;
    }

    /**
     * 获取屏幕长宽比
     * param context
     * return
     */
    public static float getScreenRate(Context context) {
        Point P = getScreenMetrics(context);
        float H = P.y;
        float W = P.x;
        return (H / W);
    }

    /**
     * 状态栏高度算法
     *
     * @param activity
     * @return
     */
    public static int getStatusHeight(Activity activity) {
        int statusHeight = 0;
        Rect localRect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
        statusHeight = localRect.top;
        if (0 == statusHeight) {
            Class<?> localClass;
            try {
                localClass = Class.forName("com.android.internal.R$dimen");
                Object localObject = localClass.newInstance();
                int i5 = Integer.parseInt(localClass.getField("status_bar_height").get(localObject).toString());
                statusHeight = activity.getResources().getDimensionPixelSize(i5);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return statusHeight;
    }

    public static int getDensityDpi(Context context) {
        DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
        return dm.densityDpi;
    }

    public static float getDensity(Context context) {
        DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
        return dm.density;
    }

    /**
     * 获取屏幕尺寸
     * @param context
     * @return
     */
    public static double getPixSize(Context context) {
        int densityDpi = getDensityDpi(context);
        Point point = getScreenMetrics(context);
        double s_w = point.x / densityDpi;
        double s_h = point.y / densityDpi;
        double s = Math.sqrt(Math.pow(s_h, 2) + Math.pow(s_w, 2));
        TLog.i("s", s+"_cun");
        return s;
    }

    /**
     * 计算出来的位置，y方向就在anchorView的上面和下面对齐显示，x方向就是与屏幕右边对齐显示
     * 如果anchorView的位置有变化，就可以适当自己额外加入偏移来修正
     * @param anchorView  呼出window的view
     * @param contentView   window的内容布局
     * @return window显示的左上角的xOff,yOff坐标
     */
    public static int[] calculatePopWindowPos(final View anchorView, final View contentView) {
        final int windowPos[] = new int[2];
        final int anchorLoc[] = new int[2];
         // 获取锚点View在屏幕上的左上角坐标位置
        anchorView.getLocationOnScreen(anchorLoc);
        final int anchorHeight = anchorView.getHeight();
        // 获取屏幕的高宽
        final int screenHeight = ScreenUtils.getScreenHeight();
        final int screenWidth = ScreenUtils.getScreenWidth();
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        // 计算contentView的高宽
        final int windowHeight = contentView.getMeasuredHeight();
        final int windowWidth = contentView.getMeasuredWidth();
        // 判断需要向上弹出还是向下弹出显示
        final boolean isNeedShowUp = (screenHeight - anchorLoc[1] - anchorHeight < windowHeight);
        if (isNeedShowUp) {
            windowPos[0] = screenWidth - windowWidth;
            windowPos[1] = anchorLoc[1] - windowHeight;
        } else {
            windowPos[0] = screenWidth - windowWidth;
            windowPos[1] = anchorLoc[1] + anchorHeight;
        }
        return windowPos;
    }
}
