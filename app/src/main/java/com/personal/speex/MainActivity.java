package com.personal.speex;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.personal.AudioStream.service.IMyService;
import com.personal.AudioStream.service.MyService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private Button start_record_btn;
    private Button stop_record_btn;
    private Button play_video_btn;
    private Button stop_video_btn;
    private TextView show_audio_data;

    private IMyService mIBinder;
    private Intent intent;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIBinder = (IMyService) service;
            /*intercomService = IIntercomService.Stub.asInterface(service);
            try {
                intercomService.registerCallback(intercomCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }*/
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIBinder = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start_record_btn =(Button) findViewById(R.id.start_record_btn);
        stop_record_btn =(Button) findViewById(R.id.stop_record_btn);
        play_video_btn =(Button) findViewById(R.id.play_video_btn);
        stop_video_btn =(Button) findViewById(R.id.stop_video_btn);

        show_audio_data =(TextView) findViewById(R.id.show_audio_data);
        initData();

        start_record_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIBinder != null) {
                    mIBinder.startRecord();
                }
            }
        });

        stop_record_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIBinder != null) {
                    mIBinder.stopRecord();
                    show_audio_data.setText("");
                }
            }
        });

        play_video_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIBinder != null) {
                    mIBinder.startRecord();
                }
            }
        });

        stop_video_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIBinder != null) {
                    mIBinder.startRecord();
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        intent = new Intent(MainActivity.this, MyService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }


    private void initData() {
        // 初始化AudioManager配置
        initAudioManager();
        // 启动Service
        intent = new Intent(MainActivity.this, MyService.class);
        startService(intent);
    }

    /**
     * 初始化AudioManager配置
     */
    private void initAudioManager() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(true);
    }

    @Override
    protected void onDestroy() {
        try {
            unbindService(serviceConnection);
            stopService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

}
