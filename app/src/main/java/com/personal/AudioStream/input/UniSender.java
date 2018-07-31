package com.personal.AudioStream.input;

import android.os.Handler;
import android.util.Log;

import com.personal.AudioStream.constants.PBroadCastConfig;
import com.personal.AudioStream.data.AudioData;
import com.personal.AudioStream.data.MessageQueue;
import com.personal.AudioStream.job.JobHandler;
import com.personal.AudioStream.network.Multicast;
import com.personal.AudioStream.network.Unicast;
import com.personal.AudioStream.output.Decoder;
import com.personal.AudioStream.output.Tracker;
import com.personal.speex.IntercomUserBean;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Socket发送,UDP多播发送
 *
 * @author yanghao1
 */
public class UniSender extends JobHandler {

    private Decoder decoder;
    private Tracker tracker;
    private IntercomUserBean userBean;

    public UniSender(Handler handler) {
        super(handler);
    }

    public UniSender(Handler handler, IntercomUserBean userBean) {
        super(handler);
        this.userBean = userBean;
    }

    @Override
    public void run() {
        AudioData audioData;
        while ((audioData = MessageQueue.getInstance(MessageQueue.UNI_SENDER_DATA_QUEUE).take()) != null) {
            // TODO: 2018/6/29 播放用测试
           // MessageQueue.getInstance(MessageQueue.DECODER_DATA_QUEUE).put(audioData);
            Log.e("audio", "unisender: "+audioData.getEncodedData().length);
            // TODO: 2018/6/29 播放用测试

            DatagramPacket datagramPacket = new DatagramPacket(
                    audioData.getEncodedData(), audioData.getEncodedData().length,
                    Multicast.getMulticast().getInetAddress(), PBroadCastConfig.MULTI_BROADCAST_PORT);
            try {
                Unicast.getUnicast().getUnicastSendSocket().send(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void free() {
        Unicast.getUnicast().free();
    }
}
