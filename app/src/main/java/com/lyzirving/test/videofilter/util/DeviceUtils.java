package com.lyzirving.test.videofilter.util;

import android.content.Context;
import android.util.DisplayMetrics;

import com.lyzirving.test.videofilter.ComponentContext;

/**
 * @author lyzirving
 *         time        2018/10/9
 *         email       lyzirving@sina.com
 *         information
 */

public class DeviceUtils {

    public DeviceUtils() {}

    private static int getScreenWidthPx(Context context) {
        return getDisplayMetrics(context).widthPixels;
    }

    private static DisplayMetrics getDisplayMetrics(Context context) {
        return context.getResources().getDisplayMetrics();
    }

    private static int getScreenHeightPx(Context context) {
        return getDisplayMetrics(context).heightPixels;
    }

    public static int getScreenHeightPx() {
        return getScreenHeightPx(ComponentContext.getContext());
    }

    public static int getScreenWidthPx() {
        return getScreenWidthPx(ComponentContext.getContext());
    }

    public static int dip2px(float dpValue) {
        return dip2px(ComponentContext.getContext(), dpValue);
    }

    public static int px2dip(float pxValue) {
        return px2dip(ComponentContext.getContext(), pxValue);
    }

    public static float sp2pxF(float spValue) {
        return sp2pxF(ComponentContext.getContext(), spValue);
    }

    private static float sp2pxF(Context context, float spValue) {
        float scale = getScreenScaleDensity(context);
        return spValue * scale + 0.5F;
    }

    private static int px2dip(Context context, float pxValue) {
        float scale = getScreenDensity(context);
        return (int)(pxValue / scale + 0.5F);
    }

    private static int dip2px(Context context, float dpValue) {
        float scale = getScreenDensity(context);
        return (int)(dpValue * scale + 0.5F);
    }

    public static float getScreenDensity(Context context) {
        return getDisplayMetrics(context).density;
    }

    public static float getScreenScaleDensity(Context context) {
        return getDisplayMetrics(context).scaledDensity;
    }

}
