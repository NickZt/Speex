package com.personal.audiostream.input;

import android.os.Handler;
import android.util.Log;

import com.personal.audiostream.constants.PBroadCastConfig;
import com.personal.audiostream.data.AudioData;
import com.personal.audiostream.data.MessageQueue;
import com.personal.audiostream.job.JobHandler;
import com.personal.audiostream.network.Multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.Charset;

/**
 * Socket send ,UDP多播 send 
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
