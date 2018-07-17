package com.personal.AudioStream.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.personal.AudioStream.service.MyService;

/**
 * 开机自动启动广播
 * Created by personal on 2018/7/10.
 */

public class AutoStartBroadCastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("autostart", "开机自动服务自动启动.....");
        /*//启动的服务
        Intent service = new Intent(context, MyService.class);
        context.startService(service);
        //启动应用，参数为需要自动启动的应用的包名
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        context.startActivity(intent);*/
    }
}
