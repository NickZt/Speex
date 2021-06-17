package com.personal.searchdevices22;

import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.personal.speex.R;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Main2Activity extends AppCompatActivity {
    private List<Search22Thread.DeviceBean> mDeviceList = new ArrayList<>();
    private TextView show1;
    private TextView show2;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            //This is the message from the send
            if (mDeviceList != null) {
                show1.setText(mDeviceList.toString());
            }
        }};
    private Button search_btn;

    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main2);

            show1 = (TextView) findViewById(R.id.show_tv);
            show2 = (TextView) findViewById(R.id.show2_tv);
            search_btn = (Button) findViewById(R.id.search_btn);
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchDevices_broadcast();
            }
        });

            initData();
        }


        private void initData() {
            new WaitSearch22Thread(this, "Daylight", "living room") {
                @Override
                public void onDeviceSearched(InetSocketAddress socketAddr) {
                    pushMsgToMain("Already online ， search for Host：" + socketAddr.getAddress().getHostAddress() + ":" + socketAddr.getPort());
                }
            }.start();
        }


        public void searchDevices_broadcast() {
            new Search22Thread() {
                @Override
                public void onSearchStart() {
                    startSearch(); // 主要用于在UI上展示正在 search for
                }

                @Override
                public void onSearchFinish(Set deviceSet) {
                    endSearch(); // End of the UI on the search for
                    mDeviceList.clear();
                    mDeviceList.addAll(deviceSet);

                    mHandler.sendEmptyMessage(0); // 在UI上更新equipment列表
                }
            }.start();
        }

        private void endSearch() {

        }

        private void startSearch() {
            Log.e("aaa", "startSearch: " );
        }

        private void pushMsgToMain(String s) {

        }

    }
