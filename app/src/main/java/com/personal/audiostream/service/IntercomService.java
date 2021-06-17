package com.personal.audiostream.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.personal.audiostream.constants.PCommand;
import com.personal.audiostream.discover.SignInAndOutReq;
import com.personal.audiostream.group.TallBackActivity;
import com.personal.audiostream.input.Encoder;
import com.personal.audiostream.input.MultiSender;
import com.personal.audiostream.input.Recorder;
import com.personal.audiostream.output.Decoder;
import com.personal.audiostream.output.MultiReceiver;
import com.personal.audiostream.output.Tracker;
import com.personal.audiostream.util.TUtil;
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
    // Create cyclic task threads for interval send  online Message, get other group names in the local area network
    private final ScheduledExecutorService discoverService = Executors.newScheduledThreadPool(1);

    // Create a fixed-size thread pool of 7 threads, execute DiscoverServer respectively, and input and output audio
    // Create a buffer thread pool for recording and receiving group name online messages (the recording thread may not be used for a long time, and it should be recycled over time）
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
            //  send Offline message
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


    private void updateView() {

    }


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
        //  Related Offline message
        signInAndOutReq = new SignInAndOutReq(handler);
        //规则：命令+group name+username（&隔开）
        signInAndOutReq.setCommand(PCommand.getDiscoverCommand());
        // 启动探测局域网内其余group name的线程（每分钟扫描一次）
        discoverService.scheduleAtFixedRate(signInAndOutReq, 0, 10, TimeUnit.SECONDS);
        //  Related JobHandler
        initJobHandler();
    }

    /**
     * Related JobHandler
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void initJobHandler() {
        //  Related 音频输入节点
        recorder = new Recorder(handler);
        encoder = new Encoder(handler);
        multiSender = new MultiSender(handler);
        //  Related 音频输出节点
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startNewForeground();
        } else {
            startOldForeground();

        }


    }

    private void startNewForeground() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            String NOTIFICATION_CHANNEL_ID = "om.personal.audiostream.service";
            String channelName = "My Background Service";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName,
                    NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,
                    NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setContentTitle(" Walkie talkie")
                    .setTicker(" Walkie talkie")
                    .setContentText("is using Walkie talkie")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(PCommand.FOREGROUND_SERVICE, notification);
        }
    }

    private void startOldForeground() {
        Intent notificationIntent = new Intent(this, TallBackActivity.class);
        notificationIntent.setAction(PCommand.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
//        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Notification notification;
        notification = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle(" Walkie talkie")
                .setTicker(" Walkie talkie")
                .setContentText("is using Walkie talkie")
                .setSmallIcon(R.mipmap.ic_launcher)
                // .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true).build();

        startForeground(PCommand.FOREGROUND_SERVICE, notification);
    }

    /**
     * Stop recording
     */
    private void stopRecorder(int level, IntercomUserBean userBean) {
        Log.e("audio", "startRecord2: " + recorder.isRecording());
        if (recorder.isRecording()) {
            recorder.onStop();
        }
        tracker.setPlaying(true);
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
                TUtil.showLong("Please turn off recording first");
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
                TUtil.showLong("Please turn off recording first");
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
                TUtil.showLong("Please turn off recording first");
            }
        }
    }

    /**
     * 删除group name显示
     *
     * @param userBean IPaddress
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
                Log.d("audio", "DISCOVERING_SEND send news ");
            } else if (msg.what == PCommand.DISCOVERING_RECEIVE) {
                Log.d("audio", "DISCOVERING_RECEIVE Receive message 2");
                service.findNewUser((IntercomUserBean) msg.obj);
            } else if (msg.what == PCommand.DISCOVERING_LEAVE) {
                service.removeUser((IntercomUserBean) msg.obj);
            } else if (msg.what == PCommand.DISCOVERING_START_SINGLE_SUCCESS) {
                Log.d("audio", "The other party responded successfully-to send the voice, to the individual");
               /* IntercomUserBean userBean = (IntercomUserBean) msg.obj;
                service.startRecorder(1,userBean);*/
            } else if (msg.what == PCommand.DISCOVERING_START_SINGLE_REFUSE) {
                Log.d("audio", "DISCOVERING_START_SINGLE_REFUSE The other party responds to rejection-display, to the individual");
                TUtil.showLong("The other party is on a call...");
            } else if (msg.what == PCommand.DISCOVERING_START_SINGLE) {
                Log.e("audio", "DISCOVERING_START_SINGLE The other party requests a call-display interface, the other party requests a call-display interface, and the individual");
                service.updateView();
            } else if (msg.what == PCommand.DISCOVERING_START_GROUP) {
                Log.e("audio", "DISCOVERING_START_GROUP Call start request response 2");
            } else if (msg.what == PCommand.DISCOVERING_START_ALL) {
                Log.e("audio", "DISCOVERING_START_ALL Call start request response 3");
            } else if (msg.what == PCommand.DISCOVERING_STOP_SINGLE) {
                Log.e("audio", "DISCOVERING_STOP_SINGLE Call request the end-close interface, one-to-one, no response temporarily");
            } else if (msg.what == PCommand.DISCOVERING_STOP_GROUP) {
                Log.e("audio", "DISCOVERING_STOP_GROUP Call start request response 5");
            } else if (msg.what == PCommand.DISCOVERING_STOP_ALL) {
                Log.e("audio", "DISCOVERING_STOP_ALL Call start request response 6");
            }
        }
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
