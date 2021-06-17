package com.personal.audiostream.input;

import android.os.Handler;
import android.util.Log;

import com.personal.audiostream.constants.PBroadCastConfig;
import com.personal.audiostream.data.AudioData;
import com.personal.audiostream.data.MessageQueue;
import com.personal.audiostream.job.JobHandler;
import com.personal.audiostream.network.Multicast;
import com.personal.audiostream.network.Unicast;
import com.personal.audiostream.output.Decoder;
import com.personal.audiostream.output.Tracker;
import com.personal.speex.IntercomUserBean;

import java.io.IOException;
import java.net.DatagramPacket;

/**
 * Socket send ,UDP多播 send 
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
