package com.personal.audiostream.output;

import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.personal.audiostream.constants.PBroadCastConfig;
import com.personal.audiostream.constants.PCommand;
import com.personal.audiostream.data.AudioData;
import com.personal.audiostream.data.MessageQueue;
import com.personal.audiostream.job.JobHandler;
import com.personal.audiostream.network.Multicast;
import com.personal.audiostream.network.Unicast;
import com.personal.audiostream.util.IPUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by yanghao1 on 2017/4/12.
 */

public class UniReceiver extends JobHandler {

    public UniReceiver(Handler handler) {
        super(handler);
    }

    @Override
    public void run() {
        while (true) {
            // 设置接收缓冲段
            byte[] receivedData = new byte[1024];
            DatagramPacket datagramPacket = new DatagramPacket(receivedData, receivedData.length);
            try {
                // 接收数据报文
                Unicast.getUnicast().getUnicastReceiveSocket().receive(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
           /* // 判断数据报文类型，并做相应处理
            if (datagramPacket.getLength() == PCommand.DISC_REQUEST.getBytes(Charset.forName("UTF-8")).length ||
                    datagramPacket.getLength() == PCommand.DISC_LEAVE.getBytes(Charset.forName("UTF-8")).length ||
                    datagramPacket.getLength() == PCommand.DISC_RESPONSE.getBytes(Charset.forName("UTF-8")).length) {
                handleCommandData(datagramPacket);
            } else {
                handleAudioData(datagramPacket);
            }*/
        }
    }

    /**
     * 处理命令数据
     *
     * @param packet 命令数据包
     */
    private void handleCommandData(DatagramPacket packet) {
        String content = new String(packet.getData()).trim();
        if (PCommand.DISC_REQUEST.equals(content) &&
                !packet.getAddress().toString().equals("/" + IPUtil.getLocalIPAddress())) {
            byte[] feedback = PCommand.DISC_RESPONSE.getBytes(Charset.forName("UTF-8"));
            //  send 数据
            DatagramPacket sendPacket = new DatagramPacket(feedback, feedback.length,
                    packet.getAddress(), PBroadCastConfig.MULTI_BROADCAST_PORT);
            try {
                Unicast.getUnicast().getUnicastReceiveSocket().send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //  send Handler消息
            sendMsg2MainThread(packet.getAddress().toString(), PCommand.DISCOVERING_RECEIVE);
        } else if (PCommand.DISC_RESPONSE.equals(content) &&
                !packet.getAddress().toString().equals("/" + IPUtil.getLocalIPAddress())) {
            //  send Handler消息
            sendMsg2MainThread(packet.getAddress().toString(), PCommand.DISCOVERING_RECEIVE);
        } else if (PCommand.DISC_LEAVE.equals(content) &&
                !packet.getAddress().toString().equals("/" + IPUtil.getLocalIPAddress())) {
            sendMsg2MainThread(packet.getAddress().toString(), PCommand.DISCOVERING_LEAVE);
        }
    }

    /**
     * 处理音频数据
     *
     * @param packet 音频数据包
     */
    private void handleAudioData(DatagramPacket packet) {
        byte[] encodedData = Arrays.copyOf(packet.getData(), packet.getLength());
        Log.e("audio", "handleAudioData: "+encodedData.length );
        AudioData audioData = new AudioData(encodedData);
        MessageQueue.getInstance(MessageQueue.DECODER_DATA_QUEUE).put(audioData);
    }

    /**
     *  send Handler消息
     *
     * @param content 内容
     */
    private void sendMsg2MainThread(String content, int msgWhat) {
        Message msg = new Message();
        msg.what = msgWhat;
        msg.obj = content;
        handler.sendMessage(msg);
    }

    @Override
    public void free() {
        Multicast.getMulticast().free();
    }
}
