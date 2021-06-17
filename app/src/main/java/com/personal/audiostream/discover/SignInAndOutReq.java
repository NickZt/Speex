package com.personal.audiostream.discover;

import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.personal.audiostream.constants.PCommand;
import com.personal.audiostream.job.JobHandler;
import com.personal.audiostream.network.Multicast;
import com.personal.audiostream.constants.PBroadCastConfig;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.Charset;

public class SignInAndOutReq extends JobHandler {

    private String command;
    private DatagramSocket broadSocket;

    public SignInAndOutReq(Handler handler) {
        super(handler);
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public void run() {
        if (command != null) {
            byte[] data = (command).getBytes(Charset.forName("UTF-8"));
            DatagramPacket datagramPacket = new DatagramPacket(
                    data, data.length, Multicast.getMulticast().getInetAddress(), PBroadCastConfig.MULTI_BROADCAST_PORT);
            Log.e("audio", "SignAndOutReq: " + command + "===" + Multicast.getMulticast().getInetAddress());
            try {
                Multicast.getMulticast().getSendMulticastSocket().send(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *  send news 到主线程
     */
    private void sendMsg2MainThread() {
        Message message = new Message();
        message.what = PCommand.DISCOVERING_SEND;
        handler.sendMessage(message);
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
}
