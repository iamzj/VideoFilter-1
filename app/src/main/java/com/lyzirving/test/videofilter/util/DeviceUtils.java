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

}
