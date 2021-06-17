package com.personal.audiostream.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 开机自动启动广播
 * Created by personal on 2018/7/10.
 */

public class AutoStartBroadCastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("autostart", "Automatic service starts automatically at boot.....");
        //启动的服务
//        Intent service = new Intent(context, IntercomService.class);
//        context.startService(service);
//        //Start the application, the parameter is the package name of the application that needs to be automatically started
//        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
//        context.startActivity(intent);
//        */
    }
}
