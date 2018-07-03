package com.personal;

import android.app.Application;
import android.content.Context;

/**
 * Created by 山东御银智慧 on 2018/6/8.
 */

public class App extends Application {
    private static Context context;

    public static Context getInstance() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }
}
