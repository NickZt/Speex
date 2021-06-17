package com.personal.audiostream.input;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.personal.audiostream.constants.PCommand;
import com.personal.audiostream.data.AudioData;
import com.personal.audiostream.data.MessageQueue;
import com.personal.audiostream.job.JobHandler;
import com.personal.audiostream.util.AudioDataUtil;

/**
 * 音频编码,输入类型为short[]，输出为byte[]
 *
 */
public class Encoder extends JobHandler {

    private int SEND_COMMAND = 0;

    public Encoder(Handler handler) {
        super(handler);
    }

    @Override
    public void run() {
        AudioData data;
        // 在MessageQueue为空时，take方法阻塞
        while ((data = MessageQueue.getInstance(MessageQueue.ENCODER_DATA_QUEUE).take()) != null) {
            Log.e("audio", "encoder:getRawData "+data.getRawData().length);
            data.setEncodedData(AudioDataUtil.raw2spx(data.getRawData()));
            Log.e("audio", "encoder:getEncodedData "+data.getEncodedData().length);
            if (SEND_COMMAND == PCommand.UNI_FLAG_PER_LEVEL) {
                MessageQueue.getInstance(MessageQueue.MULTI_SENDER_DATA_QUEUE).put(data);
            }else  if (SEND_COMMAND == PCommand.MULTI_FLAG_GROUP_LEVEL) {
                MessageQueue.getInstance(MessageQueue.MULTI_SENDER_DATA_QUEUE).put(data);
            }else  if (SEND_COMMAND == PCommand.MULTI_FLAG_ALL_LEVEL) {
                MessageQueue.getInstance(MessageQueue.MULTI_SENDER_DATA_QUEUE).put(data);
            }else {
                //do nothing
                Message message = new Message();
                message.what = 111;
                handler.sendMessage(message);
            }

        }
    }

    /**
     * 必须在录音开始之前执行才有效果
     * @param SEND_COMMAND
     */
    public void setSEND_COMMAND(int SEND_COMMAND) {
        this.SEND_COMMAND = SEND_COMMAND;
    }

    @Override
    public void free() {
        AudioDataUtil.free();
    }

}
