package com.personal.audiostream.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.personal.audiostream.constants.SPConsts;
import com.personal.audiostream.group.TallBackActivity;
import com.personal.audiostream.util.NetworkUtil;
import com.personal.audiostream.util.SPUtil;
import com.personal.audiostream.util.TUtil;
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
                TUtil.showLong("Please turn on the network");
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
                    Toast.makeText(LoginActivity.this, "Group name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                String userNameTri = user_name_et.getText().toString().trim();
                if (TextUtils.isEmpty(userNameTri)) {
                    Toast.makeText(LoginActivity.this, "Username can not be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                SPUtil.getInstance().put(SPConsts.GROUP_NAME, groupNameTri);
                SPUtil.getInstance().put(SPConsts.USER_NAME, userNameTri);
                if (NetworkUtil.isWifiConnected()) {
                    startActivity(new Intent(LoginActivity.this, TallBackActivity.class));
                    finish();
                }else {
                    TUtil.showLong("Please turn on the network");
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
                            // The user has agreed to the permission
                            Log.e("permission", permission.name + " is granted.");
                        } else if (permission.shouldShowRequestPermissionRationale) {
                            // group name拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                            Log.e("permission", permission.name + " is denied. More info should be provided.");
                        } else {
                            // The user denied the permission and checked "Don't ask again"
                            Log.e("permission", permission.name + " is denied.");
                        }
                    }
                });
    }


    private void dialogList() {
        final String items[] = {"Group A", "Group B", "Group C", "Group D", "E组", "F组", "G组", "H组"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this,3);
        builder.setTitle("Please select a group：");
        // builder.setMessage("Confirm exit?"); //Set content
        builder.setIcon(R.mipmap.ic_launcher);
        // Setting list display，注意设置了列表显示就不要设置builder.setMessage()了，否则列表不起作用。
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                group_name_tv.setText(items[which]);
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("determine", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                group_name_tv.setText(items[which]);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

}

