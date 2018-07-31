package com.personal.AudioStream.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.personal.AudioStream.constants.SPConsts;
import com.personal.AudioStream.group.TallBackActivity;
import com.personal.AudioStream.util.NetworkUtil;
import com.personal.AudioStream.util.SPUtil;
import com.personal.AudioStream.util.TUtil;
import com.personal.speex.R;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.functions.Consumer;

/**
 * Created by 山东御银智慧 on 2018/7/23.
 */

public class LoginActivity extends AppCompatActivity {

    private Button bt_save_member_info;
    private EditText group_name_et;
    private EditText user_name_et;
    private TextView group_name_tv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        user_name_et = (EditText) findViewById(R.id.user_name_et);
//        group_name_et = (EditText) findViewById(R.id.group_name_et);
        group_name_tv = (TextView) findViewById(R.id.group_name_tv);
        bt_save_member_info = (Button) findViewById(R.id.bt_save_member_info);

        requestPermissions();
        initView();
    }

    private void initView() {
        String groupName = SPUtil.getInstance().getString(SPConsts.GROUP_NAME, "");
        String userName = SPUtil.getInstance().getString(SPConsts.USER_NAME, "");
        if (TextUtils.isEmpty(groupName) || TextUtils.isEmpty(userName)) {

        } else {
            if (NetworkUtil.isWifiConnected()) {
                startActivity(new Intent(LoginActivity.this, TallBackActivity.class));
                finish();
            }else {
                TUtil.showLong("请开启网络");
                group_name_tv.setText(groupName);
                user_name_et.setText(userName);
            }
        }

        group_name_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogList();
            }
        });

        bt_save_member_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String groupNameTri = group_name_et.getText().toString().trim();
                String groupNameTri = group_name_tv.getText().toString().trim();
                if (TextUtils.isEmpty(groupNameTri)) {
                    Toast.makeText(LoginActivity.this, "组名不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                String userNameTri = user_name_et.getText().toString().trim();
                if (TextUtils.isEmpty(userNameTri)) {
                    Toast.makeText(LoginActivity.this, "用户名不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                SPUtil.getInstance().put(SPConsts.GROUP_NAME, groupNameTri);
                SPUtil.getInstance().put(SPConsts.USER_NAME, userNameTri);
                if (NetworkUtil.isWifiConnected()) {
                    startActivity(new Intent(LoginActivity.this, TallBackActivity.class));
                    finish();
                }else {
                    TUtil.showLong("请开启网络");
                }
            }
        });
    }

    private void requestPermissions() {
        Log.e("permission", "requestPermissions: " );
        RxPermissions rxPermission = new RxPermissions(LoginActivity.this);
        rxPermission.setLogging(true);
        rxPermission.requestEach(Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
              //  Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.RECEIVE_BOOT_COMPLETED)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        if (permission.granted) {
                            // 用户已经同意该权限
                            Log.e("permission", permission.name + " is granted.");
                        } else if (permission.shouldShowRequestPermissionRationale) {
                            // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                            Log.e("permission", permission.name + " is denied. More info should be provided.");
                        } else {
                            // 用户拒绝了该权限，并且选中『不再询问』
                            Log.e("permission", permission.name + " is denied.");
                        }
                    }
                });
    }


    private void dialogList() {
        final String items[] = {"A组", "B组", "C组", "D组", "E组", "F组", "G组", "H组"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this,3);
        builder.setTitle("请选择分组：");
        // builder.setMessage("是否确认退出?"); //设置内容
        builder.setIcon(R.mipmap.ic_launcher);
        // 设置列表显示，注意设置了列表显示就不要设置builder.setMessage()了，否则列表不起作用。
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                group_name_tv.setText(items[which]);
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                group_name_tv.setText(items[which]);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

}

