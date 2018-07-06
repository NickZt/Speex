package com.personal;

import android.app.Application;
import android.content.Context;

import com.jess.arms.base.BaseApplication;

/**
 * Created by 山东御银智慧 on 2018/6/8.
 */

public class App extends BaseApplication {
    private static Context context;

    public static Context getInstance() {
        return context ;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }
}
