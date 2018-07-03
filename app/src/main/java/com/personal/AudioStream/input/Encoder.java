package com.personal.AudioStream.input;

import android.os.Handler;
import android.util.Log;

import com.personal.AudioStream.data.AudioData;
import com.personal.AudioStream.data.MessageQueue;
import com.personal.AudioStream.job.JobHandler;
import com.personal.AudioStream.util.AudioDataUtil;

import java.util.Arrays;

/**
 * 音频编码,输入类型为short[]，输出为byte[]
 *
 */
public class Encoder extends JobHandler {

    public Encoder(Handler handler) {
        super(handler);
    }

    @Override
    public void free() {
        AudioDataUtil.free();
    }

    @Override
    public void run() {
        AudioData data;
        // 在MessageQueue为空时，take方法阻塞
        while ((data = MessageQueue.getInstance(MessageQueue.ENCODER_DATA_QUEUE).take()) != null) {
            Log.e("audio", "encoder1: "+data.getRawData().length+"\n");
            data.setEncodedData(AudioDataUtil.raw2spx(data.getRawData()));
            Log.e("audio", "encoder2: "+data.getEncodedData().length+"\n");
            MessageQueue.getInstance(MessageQueue.SENDER_DATA_QUEUE).put(data);
        }
    }
}
