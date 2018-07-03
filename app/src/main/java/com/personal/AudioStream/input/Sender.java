package com.personal.AudioStream.input;

import android.os.Handler;
import android.util.Log;

import com.personal.AudioStream.data.AudioData;
import com.personal.AudioStream.data.MessageQueue;
import com.personal.AudioStream.job.JobHandler;
import com.personal.AudioStream.network.Multicast;
import com.personal.AudioStream.output.Decoder;
import com.personal.AudioStream.output.Tracker;
import com.personal.AudioStream.util.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Socket发送,UDP多播发送
 *
 * @author yanghao1
 */
public class Sender extends JobHandler {

    private Decoder decoder;
    private Tracker tracker;

    public Sender(Handler handler) {
        super(handler);
    }
    private ExecutorService threadPool = Executors.newCachedThreadPool();
    @Override
    public void run() {
        AudioData audioData;
        while ((audioData = MessageQueue.getInstance(MessageQueue.SENDER_DATA_QUEUE).take()) != null) {
            // TODO: 2018/6/29 播放用测试
            MessageQueue.getInstance(MessageQueue.DECODER_DATA_QUEUE).put(audioData);
            Log.e("audio", "sender: "+audioData.getEncodedData().length);
            // TODO: 2018/6/29 播放用测试

           /* DatagramPacket datagramPacket = new DatagramPacket(
                    audioData.getEncodedData(), audioData.getEncodedData().length,
                    Multicast.getMulticast().getInetAddress(), Constants.MULTI_BROADCAST_PORT);
            try {
                Multicast.getMulticast().getMulticastSocket().send(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }
    }

    @Override
    public void free() {
        Multicast.getMulticast().free();
    }
}
