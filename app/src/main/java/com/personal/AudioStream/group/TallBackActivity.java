package com.personal.AudioStream.group;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.personal.AudioStream.constants.PCommand;
import com.personal.AudioStream.constants.SPConsts;
import com.personal.AudioStream.service.IntercomService;
import com.personal.AudioStream.util.IPUtil;
import com.personal.AudioStream.util.SPUtil;
import com.personal.speex.IIntercomService;
import com.personal.speex.IUserCallback;
import com.personal.speex.IntercomUserBean;
import com.personal.speex.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class TallBackActivity extends AppCompatActivity {

    private TextView tvTitle;
    private ImageView ivBack;
    private ImageView ivShare;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private ArrayList<BaseFragment> mFragments;



    private List<IntercomUserBean> userBeanList = new ArrayList<>();

    private IIntercomService intercomService;

    /**
     * onServiceConnected和onServiceDisconnected运行在UI线程中
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            intercomService = IIntercomService.Stub.asInterface(service);
            try {
                intercomService.registerCallback(intercomCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            intercomService = null;
        }
    };

    /**
     * 被调用的方法运行在Binder线程池中，不能更新UI
     */
    private IUserCallback intercomCallback = new IUserCallback.Stub() {
        @Override
        public void findNewUser(IntercomUserBean userBean) throws RemoteException {
            sendMsg2MainThread(userBean, FOUND_NEW_USER);
        }

        @Override
        public void removeUser(IntercomUserBean userBean) throws RemoteException {
            sendMsg2MainThread(userBean, REMOVE_USER);
        }
    };

    private static final int FOUND_NEW_USER = 0;
    private static final int REMOVE_USER = 1;
    private TabAdapter mTabAdapter;
    private Intent intent;
    private List<String> titles;
    private String user;
    private String group;

    /**
     * 跨进程回调更新界面
     */
    private static class DisplayHandler extends Handler {
        // 弱引用
        private WeakReference<TallBackActivity> activityWeakReference;

        DisplayHandler(TallBackActivity audioActivity) {
            activityWeakReference = new WeakReference<>(audioActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            TallBackActivity activity = activityWeakReference.get();
            if (activity != null) {
                if (msg.what == FOUND_NEW_USER) {
                    activity.foundNewUser((IntercomUserBean) msg.obj);
                } else if (msg.what == REMOVE_USER) {
                    activity.removeExistUser((IntercomUserBean) msg.obj);
                }
            }
        }
    }

    private Handler handler = new TallBackActivity.DisplayHandler(this);

    /**
     * 发送Handler消息
     *
     * @param content 内容
     * @param msgWhat 消息类型
     */
    private void sendMsg2MainThread(IntercomUserBean content, int msgWhat) {
        Message msg = new Message();
        msg.what = msgWhat;
        msg.obj = content;
        handler.sendMessage(msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tall_back);
        tvTitle = findViewById(R.id.tv_title);
        ivBack = findViewById(R.id.iv_back);
        ivShare = findViewById(R.id.iv_share);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.viewPager);
        initView();
        initData();
    }

    private boolean flag1 = true;
    private boolean flag2 = true;
    private boolean flag3 = true;
    private void initView() {

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        titles = new ArrayList<>();
        updateMyself();
        ivShare.setVisibility(View.GONE);

        mFragments = new ArrayList<>();
        mFragments.add(TallBackFragment.newInstance(0));
        mFragments.add(TallBackFragment.newInstance(1));
        mFragments.add(TallBackFragment.newInstance(2));

        mTabAdapter = new TabAdapter(getSupportFragmentManager(), mFragments, titles);
        viewPager.setAdapter(mTabAdapter);//给ViewPager设置适配器
        tabLayout.setupWithViewPager(viewPager);//将TabLayout和ViewPager关联起来。
        tabLayout.setTabMode(TabLayout.MODE_FIXED);//设置TabLayout可滑动
        viewPager.setOffscreenPageLimit(3);

        for (BaseFragment mFragment : mFragments) {
            mFragment.setOnItemClickListener(new BaseFragment.OnMyItemClickListener() {
                @Override
                public void onItemClick(View view, int status, IntercomUserBean userBean) {
                  if (status == PCommand.UNI_FLAG_PER_LEVEL){//单发
                      try {
                          Log.e("audio", "onItemClick: "+"单发");
                          userBean.setAudioLevel(2);
                          if (flag1){
                              flag1 = false;
                              intercomService.startRecord(PCommand.UNI_FLAG_PER_LEVEL,userBean);
                              view.setBackground(TallBackActivity.this.getDrawable(R.drawable.border_radius_gray));
                          }else {
                              flag1 = true;
                              intercomService.stopRecord(PCommand.UNI_FLAG_PER_LEVEL,userBean);
                              view.setBackground(TallBackActivity.this.getDrawable(R.drawable.border_radius_white));
                          }

                      } catch (RemoteException e) {
                          e.printStackTrace();
                      }
                  }else if (status == PCommand.MULTI_FLAG_GROUP_LEVEL){//组内群发
                      try {
                          Log.e("audio", "onItemClick: "+flag2 );
                          userBean.setAudioLevel(3);
                          if (flag2){
                              flag2 = false;
                              view.setBackground(TallBackActivity.this.getDrawable(R.drawable.border_radius_gray));
                              intercomService.startRecord(PCommand.MULTI_FLAG_GROUP_LEVEL,userBean);
                          }else {
                              flag2 = true;
                              view.setBackground(TallBackActivity.this.getDrawable(R.drawable.border_radius_white));
                              intercomService.stopRecord(PCommand.MULTI_FLAG_GROUP_LEVEL,userBean);
                          }
                      } catch (RemoteException e) {
                          e.printStackTrace();
                      }
                  }else if (status == PCommand.MULTI_FLAG_ALL_LEVEL){//全部群发
                      try {
                          userBean.setAudioLevel(4);
                          if (flag3){
                              flag3 = false;
                              view.setBackground(TallBackActivity.this.getDrawable(R.drawable.border_radius_gray));
                              intercomService.startRecord(PCommand.MULTI_FLAG_ALL_LEVEL,userBean);
                          }else {
                              flag3 = true;
                              view.setBackground(TallBackActivity.this.getDrawable(R.drawable.border_radius_white));
                              intercomService.stopRecord(PCommand.MULTI_FLAG_ALL_LEVEL,userBean);
                          }
                      } catch (RemoteException e) {
                          e.printStackTrace();
                      }
                  }
                }
            });
        }
    }

    private void initData() {
        // 初始化AudioManager配置
        initAudioManager();
        // 启动Service
        intent = new Intent(TallBackActivity.this, IntercomService.class);
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
    protected void onStart() {
        super.onStart();
        if (intent == null) {
            intent = new Intent(TallBackActivity.this, IntercomService.class);
        }
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    /**
     * 更新自身IP
     */
    public void updateMyself() {
       // currentIp.setText(IPUtil.getLocalIPAddress());
        if (TextUtils.isEmpty(user) || TextUtils.isEmpty(group)) {
            user = SPUtil.getInstance().getString(SPConsts.USER_NAME, "我");
            group = SPUtil.getInstance().getString(SPConsts.GROUP_NAME, "空");
        }
        if (titles != null && titles.size() != 3) {
            titles.clear();
            titles.add(group);
            titles.add("其他组");
            titles.add("全部");
        }
        tvTitle.setText(user +":"+IPUtil.getLocalIPAddress()+"("+ group +")");
    }

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_F2 ||
                keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            try {
                intercomService.startRecord();
                Log.e("keydown", "onKeyDown: " );
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_F2 ||
                keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            try {
                intercomService.stopRecord();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }*/

    @Override
    public void onBackPressed() {
        // 发送离开群组消息
        try {
            intercomService.leaveGroup();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        super.onBackPressed();
    }

    /**
     * 发现新的用户地址
     *
     * @param userBean
     */
    public void foundNewUser(IntercomUserBean userBean) {
        if (!userBeanList.contains(userBean)) {
            addNewUser(userBean);
            updateMyself();
        }
    }

    /**
     * 删除用户
     *
     * @param userBean
     */
    public void removeExistUser(IntercomUserBean userBean) {
        for (BaseFragment mFragment : mFragments) {
            mFragment.removeExistUser(userBean);
        }
    }

    /**
     * 增加新的用户
     *
     * @param userBean 新用户
     */
    public void addNewUser(IntercomUserBean userBean) {
        //往三个Fragemnt里面传送数据和刷新
        for (BaseFragment mFragment : mFragments) {
            mFragment.addNewUser(userBean);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (intercomService != null && intercomService.asBinder().isBinderAlive()) {
            try {
                intercomService.unRegisterCallback(intercomCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            unbindService(serviceConnection);
        }
    }
}
