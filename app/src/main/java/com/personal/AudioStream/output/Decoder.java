package com.personal.AudioStream.output;

import android.os.Handler;
import android.util.Log;

import com.personal.AudioStream.data.AudioData;
import com.personal.AudioStream.data.MessageQueue;
import com.personal.AudioStream.job.JobHandler;
import com.personal.AudioStream.util.AudioDataUtil;

import java.util.Arrays;

/**
 * 音频解码
 *
 * @author yanghao1
 */
public class Decoder extends JobHandler {

    public Decoder(Handler handler) {
        super(handler);
    }

    @Override
    public void run() {
        AudioData audioData;
        // 当MessageQueue为空时，take方法阻塞
        while ((audioData = MessageQueue.getInstance(MessageQueue.DECODER_DATA_QUEUE).take()) != null) {
            Log.e("audio", "decoder1: "+audioData.getEncodedData().length+"\n");
            audioData.setRawData(AudioDataUtil.spx2raw(audioData.getEncodedData()));
            Log.e("audio", "decoder2: "+audioData.getRawData().length+"\n");
            MessageQueue.getInstance(MessageQueue.TRACKER_DATA_QUEUE).put(audioData);
        }
    }

    @Override
    public void free() {
        AudioDataUtil.free();
    }
}
