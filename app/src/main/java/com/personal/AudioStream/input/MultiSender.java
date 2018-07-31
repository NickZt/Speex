package com.personal.AudioStream.input;

import android.os.Handler;
import android.util.Log;

import com.personal.AudioStream.constants.PBroadCastConfig;
import com.personal.AudioStream.data.AudioData;
import com.personal.AudioStream.data.MessageQueue;
import com.personal.AudioStream.job.JobHandler;
import com.personal.AudioStream.network.Multicast;
import com.personal.AudioStream.output.Decoder;
import com.personal.AudioStream.output.Tracker;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Socket发送,UDP多播发送
 *
 * @author personal
 */
public class MultiSender extends JobHandler {
    private String commond = "04";
    private String commond2 = "";

    public void setCommond2(String commond2) {
        this.commond2 = commond2;
    }

    public void setCommond(String commond) {
        this.commond = commond;
    }

    public MultiSender(Handler handler) {
        super(handler);
    }

    @Override
    public void run() {
        AudioData audioData;

        while ((audioData = MessageQueue.getInstance(MessageQueue.MULTI_SENDER_DATA_QUEUE).take()) != null) {
            int size = MessageQueue.getInstance(MessageQueue.MULTI_SENDER_DATA_QUEUE).getSize();
            Log.e("audio", "multisender: "+size);
            byte[] start = ("7E"+commond+"&"+commond2).getBytes(Charset.forName("UTF-8"));
            byte[] end = "&7F".getBytes(Charset.forName("UTF-8"));
            byte[] encodedData = audioData.getEncodedData();
            byte[] sendData = new byte[start.length + encodedData.length + end.length];
            System.arraycopy(start,0,sendData,0,start.length);
            System.arraycopy(encodedData,0,sendData,start.length,encodedData.length);
            System.arraycopy(end,0,sendData,start.length+encodedData.length,end.length);
            DatagramPacket datagramPacket = new DatagramPacket(
                    sendData,
                    sendData.length,
                    Multicast.getMulticast().getInetAddress(),
                    PBroadCastConfig.MULTI_BROADCAST_PORT
            );
            try {
                Multicast.getMulticast().getSendMulticastSocket().send(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void free() {
        Multicast.getMulticast().free();
    }
}
