package com.personal.audiostream.middle;

import android.os.Handler;

import com.personal.audiostream.job.JobHandler;

/**
 * 将本地音频文件根据记录信息读取出来,并 send 给播放放
 * Created by personal on 2018/7/11.
 */

public class Reader extends JobHandler {

    public Reader(Handler handler) {
        super(handler);
    }

    @Override
    public void run() {

    }
}
