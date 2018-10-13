package com.lyzirving.test.videofilter;

import android.app.Application;

/**
 * @author lyzirving
 *         time        2018/10/9
 *         email       lyzirving@sina.com
 *         information
 */

public class AppApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ComponentContext.setContext(this);
    }
}
