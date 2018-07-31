package com.personal.AudioStream.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.personal.AudioStream.constants.SPConsts;
import com.personal.AudioStream.discover.SignInAndOutReq;
import com.personal.AudioStream.group.TallBackActivity;
import com.personal.AudioStream.input.Encoder;
import com.personal.AudioStream.input.MultiSender;
import com.personal.AudioStream.input.Recorder;
import com.personal.AudioStream.input.UniSender;
import com.personal.AudioStream.output.Decoder;
import com.personal.AudioStream.output.MultiReceiver;
import com.personal.AudioStream.output.UniReceiver;
import com.personal.AudioStream.output.Tracker;
import com.personal.AudioStream.constants.PCommand;
import com.personal.AudioStream.util.SPUtil;
import com.personal.AudioStream.util.TUtil;
import com.personal.speex.IIntercomService;
import com.personal.speex.IUserCallback;
import com.personal.speex.IntercomUserBean;
import com.personal.speex.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by 山东御银智慧 on 2018/6/8.
 */

public class IntercomService extends Service {
    // 创建循环任务线程用于间隔的发送上线消息，获取局域网内其他的用户
    private ScheduledExecutorService discoverService = Executors.newScheduledThreadPool(1);

    // 创建7个线程的固定大小线程池，分别执行DiscoverServer，以及输入、输出音频
    // 创建缓冲线程池用于录音和接收用户上线消息（录音线程可能长时间不用，应该让其超时回收）
    private ExecutorService threadPool = Executors.newCachedThreadPool();

    private ExecutorService singlePool = Executors.newSingleThreadExecutor();


    // 加入、退出组播组消息
    private SignInAndOutReq signInAndOutReq;

    // 音频输入
    private Recorder recorder;
    private Encoder encoder;
    private MultiSender multiSender;

    // 音频输出
    private MultiReceiver multiReceiver;
    private Decoder decoder;
    private Tracker tracker;


    /**
     * Service与Runnable的通信
     */
    private static class AudioHandler extends Handler {

        private IntercomService service;

        private AudioHandler(IntercomService service) {
            this.service = service;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.e("IntercomService", "handleMessage:what== " + msg.what + "\nobj==" + msg.obj);
            if (msg.what == PCommand.DISCOVERING_SEND) {
                Log.e("audio", "发送消息");
            } else if (msg.what == PCommand.DISCOVERING_RECEIVE) {
                Log.e("audio", "接收消息2");
                service.findNewUser((IntercomUserBean) msg.obj);
            } else if (msg.what == PCommand.DISCOVERING_LEAVE) {
                service.removeUser((IntercomUserBean) msg.obj);
            } else if (msg.what == PCommand.DISCOVERING_START_SINGLE_SUCCESS) {
                Log.e("audio", "对方响应成功-要发送语音，对个人");
               /* IntercomUserBean userBean = (IntercomUserBean) msg.obj;
                service.startRecorder(1,userBean);*/
            }else if (msg.what == PCommand.DISCOVERING_START_SINGLE_REFUSE) {
                Log.e("audio", "对方响应拒绝-显示,对个人");
                TUtil.showLong("对方正在通话中...");
            }else if (msg.what == PCommand.DISCOVERING_START_SINGLE) {
                Log.e("audio", "对方请求通话-显示界面，对个人");
               service.updateView();
            }else if (msg.what == PCommand.DISCOVERING_START_GROUP){
                Log.e("audio", "通话开始请求响应2");
            }else if (msg.what == PCommand.DISCOVERING_START_ALL){
                Log.e("audio", "通话开始请求响应3");
            }else if (msg.what == PCommand.DISCOVERING_STOP_SINGLE){
                Log.e("audio", "通话请求结束-关闭界面，单对单，暂时不响应");
            }else if (msg.what == PCommand.DISCOVERING_STOP_GROUP){
                Log.e("audio", "通话开始请求响应5");
            }else if (msg.what == PCommand.DISCOVERING_STOP_ALL){
                Log.e("audio", "通话开始请求响应6");
            }
        }
    }

    private void updateView() {

    }


    private AudioHandler handler = new AudioHandler(this);


    private RemoteCallbackList<IUserCallback> mCallbackList = new RemoteCallbackList<>();

    public IIntercomService.Stub mBinder = new IIntercomService.Stub() {
        @Override
        public void startRecord(int  level, IntercomUserBean userBean) throws RemoteException {
            SignInAndOutReq signInAndOutReq = new SignInAndOutReq(handler);
            signInAndOutReq.setCommand(PCommand.getCallRequStartCommand(userBean));
            threadPool.execute(signInAndOutReq);
            /*if (level == 1){

            }else {*/
                startRecorder(level, userBean);
//            }
        }

        @Override
        public void stopRecord(int level, IntercomUserBean userBean) throws RemoteException {
            SignInAndOutReq signInAndOutReq = new SignInAndOutReq(handler);
            signInAndOutReq.setCommand(PCommand.getCallRequtStopCommand(userBean));
            threadPool.execute(signInAndOutReq);
            stopRecorder(level, userBean);
        }

        @Override
        public void leaveGroup() throws RemoteException {
            // 发送离线消息
            SignInAndOutReq signInAndOutReq = new SignInAndOutReq(handler);
            signInAndOutReq.setCommand(PCommand.getLeaveCommand());
            threadPool.execute(signInAndOutReq);
        }

        @Override
        public void registerCallback(IUserCallback callback) throws RemoteException {
            mCallbackList.register(callback);
        }

        @Override
        public void unRegisterCallback(IUserCallback callback) throws RemoteException {
            mCallbackList.unregister(callback);
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("IntercomService", "onCreate: ");
        initData();
        showNotification();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void initData() {
        // 初始化探测线程
        signInAndOutReq = new SignInAndOutReq(handler);
        //规则：命令+组名+用户名（&隔开）
        signInAndOutReq.setCommand(PCommand.getDiscoverCommand());
        // 启动探测局域网内其余用户的线程（每分钟扫描一次）
        discoverService.scheduleAtFixedRate(signInAndOutReq, 0, 10, TimeUnit.SECONDS);
        // 初始化JobHandler
        initJobHandler();
    }

    /**
     * 初始化JobHandler
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void initJobHandler() {
        // 初始化音频输入节点
        recorder = new Recorder(handler);
        encoder = new Encoder(handler);
        multiSender = new MultiSender(handler);
        // 初始化音频输出节点
        multiReceiver = new MultiReceiver(handler);
        decoder = new Decoder(handler);
        tracker = new Tracker(handler);
        // 开启音频输入、输出
        threadPool.execute(encoder);
        threadPool.execute(multiSender);
        singlePool.execute(multiReceiver);
        threadPool.execute(decoder);
        threadPool.execute(tracker);
    }

    /**
     * 前台Service
     */
    private void showNotification() {
        Intent notificationIntent = new Intent(this, TallBackActivity.class);
//        notificationIntent.setAction(PCommand.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle("对讲机")
                .setTicker("对讲机")
                .setContentText("正在使用对讲机")
                .setSmallIcon(R.mipmap.ic_launcher)
                // .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true).build();

        startForeground(PCommand.FOREGROUND_SERVICE, notification);
    }


    /**
     * 开始录音
     */
    private void startRecorder(int level, IntercomUserBean userBean) {
        if (PCommand.UNI_FLAG_PER_LEVEL == level) {
            if (!recorder.isRecording()) {
//                encoder.setSEND_COMMAND(PCommand.UNI_FLAG_PER_LEVEL);
                multiSender.setCommond("01");
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(userBean.getIpAddress());
                if (userBean.getIpAddress().length() < 16) {
                    for (int i = 0; i < 15 - userBean.getIpAddress().length(); i++) {
                        stringBuilder.append("&");
                    }
                }
                multiSender.setCommond2(stringBuilder.toString() + "&");
                encoder.setSEND_COMMAND(PCommand.MULTI_FLAG_GROUP_LEVEL);
                recorder.onStart();
                threadPool.execute(recorder);
                //tracker.setPlaying(false);
            }else {
                TUtil.showLong("请先关闭录音");
            }
        } else if (PCommand.MULTI_FLAG_GROUP_LEVEL == level) {
            Log.e("audio", "startRecord: " + recorder.isRecording());
            if (!recorder.isRecording()) {
                multiSender.setCommond("02");
               /* String NAME = SPUtil.getInstance().getString(SPConsts.GROUP_NAME, "");
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(NAME);
                if (NAME.length() < 6) {
                    for (int i = 0; i < 5 - NAME.length(); i++) {
                        stringBuilder.append("&");
                    }
                }
                multiSender.setCommond2(stringBuilder.toString()+"&");*/
                multiSender.setCommond2("");
                encoder.setSEND_COMMAND(PCommand.MULTI_FLAG_GROUP_LEVEL);
                recorder.onStart();
                threadPool.execute(recorder);
                //tracker.setPlaying(false);
            }else {
                TUtil.showLong("请先关闭录音");
            }
        } else if (PCommand.MULTI_FLAG_ALL_LEVEL == level) {
            if (!recorder.isRecording()) {
                multiSender.setCommond("03");
                multiSender.setCommond2("");
//                encoder.setSEND_COMMAND(PCommand.MULTI_FLAG_ALL_LEVEL);
                encoder.setSEND_COMMAND(PCommand.MULTI_FLAG_GROUP_LEVEL);
                Log.e("audio", "startRecord1111: " + recorder.isRecording());
                recorder.onStart();
                Log.e("audio", "startRecord2222: " + recorder.isRecording());
                threadPool.execute(recorder);
                Log.e("audio", "startRecord3333: " + recorder.isRecording());
                //tracker.setPlaying(true);
            }else {
                TUtil.showLong("请先关闭录音");
            }
        }
    }

    /**
     * 停止录音
     */
    private void stopRecorder(int level, IntercomUserBean userBean) {
        Log.e("audio", "startRecord2: " + recorder.isRecording());
        if (recorder.isRecording()) {
            recorder.onStop();
        }
        tracker.setPlaying(true);
    }

    /**
     * 发现新的组播成员
     *
     * @param userBean 对象
     */
    private void findNewUser(IntercomUserBean userBean) {
        final int size = mCallbackList.beginBroadcast();
        for (int i = 0; i < size; i++) {
            IUserCallback callback = mCallbackList.getBroadcastItem(i);
            if (callback != null) {
                try {
                    callback.findNewUser(userBean);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        mCallbackList.finishBroadcast();
    }

    /**
     * 删除用户显示
     *
     * @param userBean IP地址
     */
    private void removeUser(IntercomUserBean userBean) {
        final int size = mCallbackList.beginBroadcast();
        for (int i = 0; i < size; i++) {
            IUserCallback callback = mCallbackList.getBroadcastItem(i);
            if (callback != null) {
                try {
                    callback.removeUser(userBean);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        mCallbackList.finishBroadcast();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("IntercomService", "onBind: ");
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("IntercomService", "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("IntercomService", "onDestroy");
        SignInAndOutReq signInAndOutReq = new SignInAndOutReq(handler);
        signInAndOutReq.setCommand(PCommand.getLeaveCommand());
        threadPool.execute(signInAndOutReq);
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
        multiReceiver.free();
        multiSender.free();
        decoder.free();
        tracker.free();
        // 释放线程池
        discoverService.shutdown();
        threadPool.shutdown();
    }
}
