package com.lyzirving.test.videofilter;

import android.content.Context;

/**
 * @author lyzirving
 *         time        2018/10/9
 *         email       lyzirving@sina.com
 *         information
 */

public class ComponentContext {
    private static Context mContext;

    private ComponentContext() {
    }

    public static void setContext(Context context) {
        mContext = context.getApplicationContext();
    }

    public static Context getContext() {
        return mContext;
    }

    public static final String getPackageName() {
        return getContext().getPackageName();
    }

}
