package com.personal.audiostream.middle;

import android.os.Handler;

import com.personal.audiostream.job.JobHandler;

/**
 * 将编码后或者未编码的音频数据 Save 到本地文件，并记录
 * Created by personal on 2018/7/11.
 */

public class Writter extends JobHandler {
    public Writter(Handler handler) {
        super(handler);
    }

    @Override
    public void run() {

    }
}
