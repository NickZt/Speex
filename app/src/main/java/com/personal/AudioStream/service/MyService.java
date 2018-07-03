package com.personal.AudioStream.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.personal.AudioStream.discover.SignInAndOutReq;
import com.personal.AudioStream.input.Encoder;
import com.personal.AudioStream.input.Recorder;
import com.personal.AudioStream.input.Sender;
import com.personal.AudioStream.output.Decoder;
import com.personal.AudioStream.output.Receiver;
import com.personal.AudioStream.output.Tracker;
import com.personal.AudioStream.util.Command;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Created by 山东御银智慧 on 2018/6/8.
 */

public class MyService extends Service {
    // 创建循环任务线程用于间隔的发送上线消息，获取局域网内其他的用户
    private ScheduledExecutorService discoverService = Executors.newScheduledThreadPool(1);

    // 创建7个线程的固定大小线程池，分别执行DiscoverServer，以及输入、输出音频
   // 创建缓冲线程池用于录音和接收用户上线消息（录音线程可能长时间不用，应该让其超时回收）
    private ExecutorService threadPool = Executors.newCachedThreadPool();

    // 设置音频播放线程为守护线程
    private ExecutorService outputService = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull Runnable r) {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setDaemon(true);
            return thread;
        }
    });

    // 加入、退出组播组消息
    private SignInAndOutReq signInAndOutReq;

    // 音频输入
    private Recorder recorder;
    private Encoder encoder;
    private Sender sender;

    // 音频输出
     private Receiver receiver;
     private Decoder decoder;
     private Tracker tracker;

    public static final int DISCOVERING_SEND = 0;
    public static final int DISCOVERING_RECEIVE = 1;
    public static final int DISCOVERING_LEAVE = 2;

    /**
     * Service与Runnable的通信
     */
    private static class AudioHandler extends Handler {

        private MyService service;

        private AudioHandler(MyService service) {
            this.service = service;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.e("MyService", "handleMessage:what== "+msg.what+"\nobj=="+(String) msg.obj);
            if (msg.what == DISCOVERING_SEND) {
                Log.i("Service_SEND", "发送消息");
            } else if (msg.what == DISCOVERING_RECEIVE) {
                Log.i("Service_RECEIVE", (String) msg.obj);
            } else if (msg.what == DISCOVERING_LEAVE) {
                Log.i("Service_LEAVE", (String) msg.obj);
            }
        }
    }

    private Handler handler = new AudioHandler(this);

    @Override
    public void onCreate() {
        super.onCreate();
        initData();
       // showNotification();
    }

    private void initData() {
        // 初始化探测线程
        signInAndOutReq = new SignInAndOutReq(handler);
        signInAndOutReq.setCommand(Command.DISC_REQUEST);
        // 启动探测局域网内其余用户的线程（每分钟扫描一次）
        discoverService.scheduleAtFixedRate(signInAndOutReq, 0, 10, TimeUnit.SECONDS);
        // 初始化JobHandler
        initJobHandler();
    }

    /**
     * 初始化JobHandler
     */
    private void initJobHandler() {
        // 初始化音频输入节点
        recorder = new Recorder(handler);
        encoder = new Encoder(handler);
        sender = new Sender(handler);
        // 初始化音频输出节点
        receiver = new Receiver(handler);
        decoder = new Decoder(handler);
        tracker = new Tracker(handler);
        // 开启音频输入、输出
        threadPool.execute(encoder);
        threadPool.execute(sender);
       /* threadPool.execute(receiver);*/
        threadPool.execute(decoder);
        threadPool.execute(tracker);
    }

    /**
     * 前台Service
     */
  /*  private void showNotification() {
        Intent notificationIntent = new Intent(this, AudioActivity.class);
//        notificationIntent.setAction(Command.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.base_app_icon);
        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle("对讲机")
                .setTicker("对讲机")
                .setContentText("正在使用对讲机")
                .setSmallIcon(R.drawable.base_app_icon)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true).build();

        startForeground(Command.FOREGROUND_SERVICE, notification);
    }*/



    public class MyBinder extends Binder implements  IMyService{
        public MyService getMyService(){
            return MyService.this;
        }

        @Override
        public void startRecord() {
            if (!recorder.isRecording()) {
                recorder.setRecording(true);
                tracker.setPlaying(true);
                threadPool.execute(recorder);
            }
        }

        @Override
        public void stopRecord() {
            if (recorder.isRecording()) {
                recorder.setRecording(false);
                tracker.setPlaying(true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        MyBinder mBinder = new MyBinder();
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("IntercomService", "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 释放资源
        free();
        // 停止前台Service
        stopForeground(true);
        stopSelf();
    }

    /**
     * 释放系统资源
     */
    private void free() {
        // 释放线程资源
        recorder.free();
        encoder.free();
        sender.free();
        receiver.free();
        decoder.free();
        tracker.free();
        // 释放线程池
        discoverService.shutdown();
        threadPool.shutdown();
    }
}
